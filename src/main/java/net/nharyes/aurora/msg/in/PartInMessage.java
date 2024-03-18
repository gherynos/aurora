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

import net.nharyes.aurora.msg.InMessage;
import net.nharyes.aurora.parts.Part;
import net.nharyes.aurora.parts.PartId;
import org.msgpack.core.MessageUnpacker;
import org.msgpack.value.Value;

import java.io.IOException;

public class PartInMessage extends InMessage<Part> {

    @Override
    protected Part unpackData(MessageUnpacker unpacker) throws IOException {

        String fileId = unpacker.unpackString();
        int sequenceNumber = unpacker.unpackInt();
        int total = unpacker.unpackInt();
        long totalSize = unpacker.unpackLong();
        Value v = unpacker.unpackValue();
        byte[] data = v.asBinaryValue().asByteArray();

        return new Part(new PartId(fileId, sequenceNumber), total, totalSize, data);
    }

    public PartInMessage(byte[] ciphertext) {

        super(ciphertext);
    }
}
