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

package net.nharyes.aurora.parts;

import net.nharyes.aurora.AuroraException;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

public class Joiner {

    private final RandomAccessFile aFile;
    private final FileChannel channel;

    public Joiner(String filePath) throws AuroraException {

        try {

            aFile = new RandomAccessFile(filePath, "rw");
            channel = aFile.getChannel();

        } catch (IOException ex) {

            throw new AuroraException("Unable to open input file: " + ex.getMessage(), ex);
        }
    }

    public void putPart(Part part) throws AuroraException {

        try {

            if (channel.size() == 0) {

                aFile.setLength(part.getTotalSize());
            }

            channel.position((long) part.getId().getSequenceNumber() * part.getPartSize());

            if (channel.write(ByteBuffer.wrap(part.getData())) != part.getData().length) {

                throw new AuroraException("Some bytes were not written...");
            }

        } catch (IOException ex) {

            throw new AuroraException("Unable to write part to file: " + ex.getMessage(), ex);
        }
    }

    public void close() throws AuroraException {

        try {

            channel.close();
            aFile.close();

        } catch (IOException ex) {

            throw new AuroraException("Unable to close file channel: " + ex.getMessage(), ex);
        }
    }
}
