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

package net.nharyes.aurora.msg.out;

import net.nharyes.aurora.AuroraException;
import net.nharyes.aurora.AuroraSession;
import net.nharyes.aurora.PublicKeys;
import net.nharyes.aurora.msg.OutMessage;
import net.nharyes.aurora.parts.Part;
import org.msgpack.core.MessageBufferPacker;

import java.io.IOException;

public class PartOutMessage extends OutMessage<Part> {

    @Override
    protected void packData(MessageBufferPacker packer, Part part) throws IOException {

        packer.packString(part.getId().getFileId());
        packer.packInt(part.getId().getSequenceNumber());
        packer.packInt(part.getTotal());
        packer.packLong(part.getTotalSize());
        packer.packBinaryHeader(part.getData().length);
        packer.writePayload(part.getData());
    }

    public PartOutMessage(AuroraSession session, PublicKeys recipient, Part part, boolean armored) throws AuroraException {

        super(session, recipient, part, armored);
    }
}
