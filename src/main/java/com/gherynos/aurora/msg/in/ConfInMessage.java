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

package com.gherynos.aurora.msg.in;

import com.gherynos.aurora.msg.InMessage;
import com.gherynos.aurora.parts.PartId;
import org.msgpack.core.MessageUnpacker;

import java.io.IOException;

public class ConfInMessage extends InMessage<PartId> {

    @Override
    protected PartId unpackData(MessageUnpacker unpacker) throws IOException {

        String fileId = unpacker.unpackString();
        int sequenceNumber = unpacker.unpackInt();

        return new PartId(fileId, sequenceNumber);
    }

    public ConfInMessage(byte[] ciphertext) {

        super(ciphertext);
    }
}
