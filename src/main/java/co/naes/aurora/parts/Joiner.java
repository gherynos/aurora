package co.naes.aurora.parts;

import co.naes.aurora.AuroraException;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

public class Joiner {

    private RandomAccessFile aFile;
    private FileChannel channel;

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

            channel.position(part.getId().getSequenceNumber() * Splitter.PART_SIZE);

            if (channel.write(ByteBuffer.wrap(part.getData())) != part.getData().length) {

                throw new AuroraException("Some bytes were not written...");
            }

        } catch (IOException ex) {

            throw new AuroraException("Unable to write part to file: " + ex.getMessage(), ex);
        }
    }
}
