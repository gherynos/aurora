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

package net.nharyes.aurora.msg.in;

import net.nharyes.aurora.Identifier;
import net.nharyes.aurora.PublicKeys;
import net.nharyes.aurora.msg.InMessage;
import org.msgpack.core.MessageUnpacker;

import java.io.IOException;

public class PublicKeysInMessage extends InMessage<PublicKeys> {

    @Override
    protected PublicKeys unpackData(MessageUnpacker unpacker) throws IOException {

        Identifier identifier = new Identifier(unpacker.unpackString());
        byte[] publicKey = unpacker.unpackValue().asBinaryValue().asByteArray();
        byte[] publicSignKey = unpacker.unpackValue().asBinaryValue().asByteArray();

        return new PublicKeys(publicKey, publicSignKey, identifier);
    }

    public PublicKeysInMessage(byte[] ciphertext) {

        super(ciphertext);
    }
}
