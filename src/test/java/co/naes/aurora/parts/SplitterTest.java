package co.naes.aurora.parts;

import co.naes.aurora.AuroraException;
import co.naes.aurora.AuroraSession;
import co.naes.aurora.PublicKeys;
import co.naes.aurora.msg.in.StringInMessage;
import co.naes.aurora.msg.out.StringOutMessage;
import net.nharyes.libsaltpack.Constants;
import net.nharyes.libsaltpack.Utils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.io.FileOutputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

class SplitterTest {

    @Test
    void evenParts() throws Exception {

        File file = File.createTempFile("temp", null);

        try {

            FileOutputStream fout = new FileOutputStream(file);
            fout.write("aaaabbbbccccddddeeee".getBytes());
            fout.close();

            Splitter sp = new Splitter(4, "aFile", file.getAbsolutePath());

            assertEquals(sp.getTotalParts(), 5);

            Part zero = sp.getPart(0);
            assertEquals(zero.getId().getFileId(), "aFile");
            assertEquals(zero.getId().getSequenceNumber(), 0);
            assertEquals(zero.getTotal(), 5);
            assertEquals(zero.getTotalSize(), 20);
            assertEquals(zero.getPartSize(), 4);
            assertArrayEquals(zero.getData(), "aaaa".getBytes());

            Part one = sp.getPart(1);
            assertEquals(one.getId().getFileId(), "aFile");
            assertEquals(one.getId().getSequenceNumber(), 1);
            assertEquals(one.getTotal(), 5);
            assertEquals(one.getTotalSize(), 20);
            assertEquals(one.getPartSize(), 4);
            assertArrayEquals(one.getData(), "bbbb".getBytes());

            assertArrayEquals(sp.getPart(2).getData(), "cccc".getBytes());
            assertArrayEquals(sp.getPart(3).getData(), "dddd".getBytes());
            assertArrayEquals(sp.getPart(4).getData(), "eeee".getBytes());

        } finally {

            file.deleteOnExit();
        }
    }

    @Test
    void oddParts() throws Exception {

        File file = File.createTempFile("temp2", null);

        try {

            FileOutputStream fout = new FileOutputStream(file);
            fout.write("aaaabbbbccccddddeeeeff".getBytes());
            fout.close();

            Splitter sp = new Splitter(4, "aFile", file.getAbsolutePath());

            assertEquals(sp.getTotalParts(), 6);

            Part zero = sp.getPart(0);
            assertEquals(zero.getId().getFileId(), "aFile");
            assertEquals(zero.getId().getSequenceNumber(), 0);
            assertEquals(zero.getTotal(), 6);
            assertEquals(zero.getTotalSize(), 22);
            assertEquals(zero.getPartSize(), 4);
            assertArrayEquals(zero.getData(), "aaaa".getBytes());

            Part one = sp.getPart(1);
            assertEquals(one.getId().getFileId(), "aFile");
            assertEquals(one.getId().getSequenceNumber(), 1);
            assertEquals(one.getTotal(), 6);
            assertEquals(one.getTotalSize(), 22);
            assertArrayEquals(one.getData(), "bbbb".getBytes());

            assertArrayEquals(sp.getPart(2).getData(), "cccc".getBytes());
            assertArrayEquals(sp.getPart(3).getData(), "dddd".getBytes());
            assertArrayEquals(sp.getPart(4).getData(), "eeee".getBytes());

            Part last = sp.getPart(5);
            assertEquals(last.getId().getFileId(), "aFile");
            assertEquals(last.getId().getSequenceNumber(), 5);
            assertEquals(last.getTotal(), 6);
            assertEquals(last.getTotalSize(), 22);
            assertEquals(last.getPartSize(), 4);
            assertArrayEquals(last.getData(), "ff".getBytes());

        } finally {

            file.deleteOnExit();
        }
    }

    @Test
    void wrongPartId() throws Exception {

        File file = File.createTempFile("temp3", null);

        try {

            FileOutputStream fout = new FileOutputStream(file);
            fout.write("aaaabbbbccccddddeeeeff".getBytes());
            fout.close();

            Splitter sp = new Splitter(3, "aFile", file.getAbsolutePath());

            assertEquals(sp.getTotalParts(), 8);

            Exception exception = assertThrows(AuroraException.class, () -> {

                sp.getPart(8);
            });
            assertTrue(exception.getMessage().contains("Wrong sequence number"));

        } finally {

            file.deleteOnExit();
        }
    }

    @Test
    void singlePart() throws Exception {

        File file = File.createTempFile("temp4", null);

        try {

            FileOutputStream fout = new FileOutputStream(file);
            fout.write("sample".getBytes());
            fout.close();

            Splitter sp = new Splitter(6, "aFile2", file.getAbsolutePath());

            assertEquals(sp.getTotalParts(), 1);

            Part zero = sp.getPart(0);
            assertEquals(zero.getId().getFileId(), "aFile2");
            assertEquals(zero.getId().getSequenceNumber(), 0);
            assertEquals(zero.getTotal(), 1);
            assertEquals(zero.getTotalSize(), 6);
            assertArrayEquals(zero.getData(), "sample".getBytes());

        } finally {

            file.deleteOnExit();
        }
    }

    @Test
    void binary() throws Exception {

        File file = File.createTempFile("temp5", null);

        try {

            byte[] random = new byte[514];
            new Random().nextBytes(random);

            FileOutputStream fout = new FileOutputStream(file);
            fout.write(random);
            fout.close();

            Splitter sp = new Splitter(3, "aRandomFile", file.getAbsolutePath());

            assertEquals(sp.getTotalParts(), 172);

            Part zero = sp.getPart(0);
            assertEquals(zero.getId().getFileId(), "aRandomFile");
            assertEquals(zero.getId().getSequenceNumber(), 0);
            assertEquals(zero.getTotal(), 172);
            assertEquals(zero.getTotalSize(), 514);
            assertEquals(zero.getPartSize(), 3);
            byte[] block = new byte[3];
            System.arraycopy(random, 0, block, 0, 3);
            assertArrayEquals(zero.getData(), block);

            Part half = sp.getPart(85);
            assertEquals(half.getId().getFileId(), "aRandomFile");
            assertEquals(half.getId().getSequenceNumber(), 85);
            assertEquals(half.getTotal(), 172);
            assertEquals(half.getTotalSize(), 514);
            System.arraycopy(random, 85 * 3, block, 0, 3);
            assertArrayEquals(half.getData(), block);

            Part last = sp.getPart(171);
            assertEquals(last.getId().getFileId(), "aRandomFile");
            assertEquals(last.getId().getSequenceNumber(), 171);
            assertEquals(last.getTotal(), 172);
            assertEquals(last.getTotalSize(), 514);
            assertEquals(last.getPartSize(), 3);
            int len = 514 - (171 * 3);
            byte[] lastBlock = new byte[len];
            System.arraycopy(random, 171 * 3, lastBlock, 0, len);
            assertArrayEquals(last.getData(), lastBlock);

        } finally {

            file.deleteOnExit();
        }
    }
}
