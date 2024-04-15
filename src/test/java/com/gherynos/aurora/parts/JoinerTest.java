package com.gherynos.aurora.parts;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class JoinerTest {

    @Test
    void evenParts() throws Exception {

        File file = File.createTempFile("tempfile", null);

        try {

            Part p1 = new Part(new PartId("theFile", 0), 2, 10, "[{(!+".getBytes());
            Part p2 = new Part(new PartId("theFile", 1), 2, 10, "+!)}]".getBytes());

            Joiner j = new Joiner(file.getAbsolutePath());
            j.putPart(p2);
            j.close();

            j = new Joiner(file.getAbsolutePath());
            j.putPart(p1);
            j.close();

            assertEquals(file.length(), 10);

            FileInputStream fin = new FileInputStream(file);
            byte[] data = new byte[fin.available()];
            fin.read(data);
            fin.close();

            assertArrayEquals("[{(!++!)}]".getBytes(), data);

        } finally {

            file.deleteOnExit();
        }
    }

    @Test
    void oddParts() throws Exception {

        File file = File.createTempFile("tempfile2", null);

        try {

            Part p1 = new Part(new PartId("theFile2", 0), 3, 13, "[{(!+".getBytes());
            Part p2 = new Part(new PartId("theFile2", 1), 3, 13, "+!)}]".getBytes());
            Part p3 = new Part(new PartId("theFile2", 2), 3, 13, " :)".getBytes());

            Joiner j = new Joiner(file.getAbsolutePath());
            j.putPart(p2);
            j.putPart(p1);
            j.putPart(p3);
            j.close();

            assertEquals(file.length(), 13);

            FileInputStream fin = new FileInputStream(file);
            byte[] data = new byte[fin.available()];
            fin.read(data);
            fin.close();

            assertArrayEquals("[{(!++!)}] :)".getBytes(), data);

        } finally {

            file.deleteOnExit();
        }
    }

    @Test
    void singlePart() throws Exception {

        File file = File.createTempFile("tempfile3", null);

        try {

            Part p = new Part(new PartId("theFile3", 0), 1, 6, "AaBbCc".getBytes());

            Joiner j = new Joiner(file.getAbsolutePath());
            j.putPart(p);
            j.close();

            assertEquals(file.length(), 6);

            FileInputStream fin = new FileInputStream(file);
            byte[] data = new byte[fin.available()];
            fin.read(data);
            fin.close();

            assertArrayEquals("AaBbCc".getBytes(), data);

        } finally {

            file.deleteOnExit();
        }
    }

    @Test
    void position() throws Exception {

        File file = File.createTempFile("tempfile4", null);

        try {

            Part p2 = new Part(new PartId("theFile4", 1), 3, 13, "+!)}]".getBytes());

            Joiner j = new Joiner(file.getAbsolutePath());
            j.putPart(p2);
            j.close();

            assertEquals(file.length(), 13);

            FileInputStream fin = new FileInputStream(file);
            byte[] data = new byte[fin.available()];
            fin.read(data);
            fin.close();

            byte[] block = new byte[5];
            System.arraycopy(data, 5, block, 0, 5);
            assertArrayEquals("+!)}]".getBytes(), block);

        } finally {

            file.deleteOnExit();
        }
    }

    @Test
    void binary() throws Exception {

        File source = File.createTempFile("tempsource", null);
        File dest = File.createTempFile("tempdest", null);

        try {

            byte[] random = new byte[1024 * 1024 * 3];
            new Random().nextBytes(random);
            FileOutputStream fout = new FileOutputStream(source);
            fout.write(random);
            fout.close();

            Splitter sp = new Splitter(813, "source", source.getAbsolutePath());
            Joiner j = new Joiner(dest.getAbsolutePath());

            for (int i = 0; i < sp.getTotalParts(); i++) {

                j.putPart(sp.getPart(i));
            }
            sp.close();
            j.close();

            FileInputStream fin1 = new FileInputStream(source);
            byte[] data1 = new byte[fin1.available()];
            fin1.read(data1);
            fin1.close();

            FileInputStream fin2 = new FileInputStream(dest);
            byte[] data2 = new byte[fin2.available()];
            fin2.read(data2);
            fin2.close();

            assertArrayEquals(data1, random);
            assertArrayEquals(data1, data2);

        } finally {

            source.deleteOnExit();
            dest.deleteOnExit();
        }
    }
}
