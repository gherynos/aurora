/*
 * Copyright (C) 2020  Luca Zanconato (<luca.zanconato@naes.co>)
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

package co.naes.aurora.parts;

import co.naes.aurora.AuroraException;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

public class Splitter {

    public static final int PART_SIZE = 1024 * (1024 + 256);

    private final String fileId;

    private RandomAccessFile aFile;
    private FileChannel channel;

    private int totalParts;

    private boolean lastPartSmaller;

    public Splitter(String fileId, String filePath) throws AuroraException {

        this.fileId = fileId;

        try {

            aFile = new RandomAccessFile(filePath, "r");
            channel = aFile.getChannel();

            totalParts = (int) Math.ceil(channel.size() / (float) PART_SIZE);
            lastPartSmaller = channel.size() % PART_SIZE != 0;

        } catch (IOException ex) {

            throw new AuroraException("Unable to open input file: " + ex.getMessage(), ex);
        }
    }

    public int getTotalParts() {

        return totalParts;
    }

    public Part getPart(int sequenceNumber) throws AuroraException {  // NOPMD

        if (sequenceNumber > totalParts - 1 || sequenceNumber < 0) {

            throw new AuroraException("Wrong sequence number");
        }

        try {

            ByteBuffer data;
            channel.position(PART_SIZE * sequenceNumber);
            if (sequenceNumber == totalParts - 1 && lastPartSmaller) {

                data = ByteBuffer.allocate((int) channel.size() - PART_SIZE * (totalParts - 1));

            } else {

                data = ByteBuffer.allocate(PART_SIZE);
            }

            if (channel.read(data) != data.capacity()) {

                throw new AuroraException("Some bytes were not read...");
            }

            return new Part(new PartId(fileId, sequenceNumber), totalParts, channel.size(), data.array());

        } catch (IOException ex) {

            throw new AuroraException("Unable to read part from file: " + ex.getMessage(), ex);
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
