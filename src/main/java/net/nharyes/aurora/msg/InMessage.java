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

package net.nharyes.aurora.msg;

import net.nharyes.aurora.AuroraException;
import net.nharyes.aurora.AuroraSession;
import net.nharyes.aurora.PublicKeys;
import net.nharyes.aurora.msg.in.ConfInMessage;
import net.nharyes.aurora.msg.in.PartInMessage;
import net.nharyes.aurora.msg.in.PublicKeysInMessage;
import net.nharyes.aurora.msg.in.StringInMessage;
import net.nharyes.libsaltpack.InputParameters;
import net.nharyes.libsaltpack.MessageReader;
import net.nharyes.libsaltpack.SaltpackException;
import org.msgpack.core.MessagePack;
import org.msgpack.core.MessageUnpacker;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public abstract class InMessage<T> extends CiphertextMessage {

    public static final Map<String, Class<? extends InMessage<?>>> MAP;

    protected boolean decrypted;

    protected PublicKeys sender;

    private T data;

    protected abstract T unpackData(MessageUnpacker unpacker) throws IOException;

    static {

        MAP = new HashMap<>();
        MAP.put("Text", StringInMessage.class);
        MAP.put("Part", PartInMessage.class);
        MAP.put("Conf", ConfInMessage.class);
        MAP.put("PK", PublicKeysInMessage.class);
    }

    public static Class<? extends InMessage<?>> getClass(String identifier) throws AuroraException {

        if (!MAP.containsKey(identifier)) {

            throw new AuroraException("Unknown identifier");
        }

        return MAP.get(identifier);
    }

    public InMessage(byte[] ciphertext) {

        super();

        this.ciphertext = ciphertext.clone();
    }

    public void decrypt(AuroraSession session) throws AuroraException {

        try {

            // decrypt message
            ByteArrayInputStream in = new ByteArrayInputStream(this.ciphertext);
            InputParameters ip = new InputParameters(in);
            ip.setArmored(isArmored());

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            MessageReader dec = new MessageReader(ip, session.getSecretKey());
            while (dec.hasMoreBlocks()) {

                out.writeBytes(dec.getBlock());
            }
            sender = new PublicKeys(dec.getSender(), null);
            dec.destroy();

            // unpack data
            MessageUnpacker unpacker = MessagePack.newDefaultUnpacker(out.toByteArray());
            data = unpackData(unpacker);
            unpacker.close();

            decrypted = true;

        } catch (SaltpackException | IOException ex) {

            throw new AuroraException("Error while decrypting message: " + ex.getMessage(), ex);
        }
    }

    public PublicKeys getSender() throws AuroraException {

        if (!decrypted) {

            throw new AuroraException("Decrypt message first.");
        }

        return sender;
    }

    public T getData() throws AuroraException {

        if (!decrypted) {

            throw new AuroraException("Decrypt message first.");
        }

        return data;
    }
}
