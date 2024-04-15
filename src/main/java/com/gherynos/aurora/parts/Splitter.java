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

package com.gherynos.aurora.parts;

import com.gherynos.aurora.AuroraException;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

public class Splitter {

    public static final int DEFAULT_PART_SIZE = 1024 * (1024 + 256);

    private final String fileId;

    private final RandomAccessFile aFile;
    private final FileChannel channel;

    private final int totalParts;

    private final boolean lastPartSmaller;

    private final int partSize;

    public static int getPartSize(long totalFileSize, int totalParts) {

        return (int) Math.ceil((float) totalFileSize / totalParts);
    }

    public Splitter(int maxPartSize, String fileId, String filePath) throws AuroraException {

        this.fileId = fileId;

        try {

            aFile = new RandomAccessFile(filePath, "r");
            channel = aFile.getChannel();

            totalParts = (int) Math.ceil(channel.size() / (float) maxPartSize);
            lastPartSmaller = channel.size() % maxPartSize != 0;

            partSize = getPartSize(channel.size(), totalParts);

        } catch (IOException ex) {

            throw new AuroraException("Unable to open input file: " + ex.getMessage(), ex);
        }
    }

    public Splitter(String fileId, String filePath) throws AuroraException {

        this(DEFAULT_PART_SIZE, fileId, filePath);
    }

    public int getTotalParts() {

        return totalParts;
    }

    @SuppressWarnings("PMD.CyclomaticComplexity")
    public Part getPart(int sequenceNumber) throws AuroraException {

        if (sequenceNumber > totalParts - 1 || sequenceNumber < 0) {

            throw new AuroraException("Wrong sequence number");
        }

        try {

            ByteBuffer data;
            channel.position((long) partSize * sequenceNumber);
            if (sequenceNumber == totalParts - 1 && lastPartSmaller) {

                data = ByteBuffer.allocate((int) channel.size() - partSize * (totalParts - 1));

            } else {

                data = ByteBuffer.allocate(partSize);
            }

            if (channel.read(data) != data.capacity()) {

                throw new AuroraException("Some bytes were not read...");
            }

            return new Part(new PartId(fileId, sequenceNumber), totalParts, channel.size(), data.array());

        } catch (IOException ex) {  // NOPMD

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
