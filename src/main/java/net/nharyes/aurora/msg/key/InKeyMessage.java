/*
 * Copyright (C) 2020-2024  Luca Zanconato (<github.com/gherynos>)
 *
 * This file is part of Aurora.
 *
 * Aurora is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Aurora is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Aurora.  If not, see <http://www.gnu.org/licenses/>.
 */

package net.nharyes.aurora.msg.key;

import net.nharyes.aurora.AuroraException;
import net.nharyes.aurora.Identifier;
import net.nharyes.aurora.PublicKeys;
import net.nharyes.aurora.msg.KeyMessage;
import net.nharyes.libsaltpack.InputParameters;
import net.nharyes.libsaltpack.MessageReader;
import net.nharyes.libsaltpack.SaltpackException;
import org.msgpack.core.MessagePack;
import org.msgpack.core.MessageUnpacker;
import org.msgpack.value.Value;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class InKeyMessage extends KeyMessage {

    private final String sender;

    public InKeyMessage(byte[] ciphertext, String sender) {

        super();

        this.ciphertext = ciphertext.clone();
        this.sender = sender;
    }

    public String getSender() {

        return sender;
    }

    public PublicKeys getPublicKeys(char[] password) throws AuroraException {

        try {

            // derive key from password
            byte[][] key = deriveKeyFromPassword(password);

            // decrypt message
            ByteArrayInputStream in = new ByteArrayInputStream(this.ciphertext);
            InputParameters ip = new InputParameters(in);
            ip.setArmored(isArmored());

            ByteArrayOutputStream msg = new ByteArrayOutputStream();
            MessageReader dec = new MessageReader(ip, new byte[]{}, key);
            while (dec.hasMoreBlocks()) {

                msg.writeBytes(dec.getBlock());
            }
            byte[] publicSignKey = dec.getSender();
            dec.destroy();

            // unpack key
            MessageUnpacker unpacker = MessagePack.newDefaultUnpacker(msg.toByteArray());
            Identifier identifier = new Identifier(unpacker.unpackString());
            Value v = unpacker.unpackValue();
            byte[] publicKey = v.asBinaryValue().asByteArray();

            return new PublicKeys(publicKey, publicSignKey, identifier);

        } catch (SaltpackException | IOException ex) {

            throw new AuroraException("Error while decrypting key: " + ex.getMessage(), ex);
        }
    }
}
