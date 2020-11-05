package co.naes.aurora;

import co.naes.aurora.db.DBUtils;
import co.naes.aurora.db.PartToSendPO;
import co.naes.aurora.db.PublicKeysUtils;
import co.naes.aurora.db.StatusUtils;
import co.naes.aurora.parts.Splitter;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class MessengerTest {

    private Path tempDirUser1;
    private Path tempDirUser2;

    private DBUtils db1;
    private DBUtils db2;

    private MockTransport t1;
    private MockTransport t2;

    private PublicKeys k1;
    private PublicKeys k2;

    private MockHandler h1;
    private MockHandler h2;

    private Messenger m1;
    private Messenger m2;

    @BeforeEach
    void setUp() throws Exception {

        tempDirUser1 = Files.createTempDirectory("user1");
        tempDirUser2 = Files.createTempDirectory("user2");

        db1 = new DBUtils(tempDirUser1.toString(), "samplepass1");
        db2 = new DBUtils(tempDirUser2.toString(), "samplepass2");

        db1.getProperties().setProperty(DBUtils.SESSION_EMAIL_ADDRESS, "user1@test.com");
        db1.getProperties().setProperty(DBUtils.INCOMING_DIRECTORY, tempDirUser1.toString());
        db1.saveProperties();

        db2.getProperties().setProperty(DBUtils.SESSION_EMAIL_ADDRESS, "user2@test.com");
        db2.getProperties().setProperty(DBUtils.INCOMING_DIRECTORY, tempDirUser2.toString());
        db2.saveProperties();

        t1 = new MockTransport("user1");
        t2 = new MockTransport("user2");

        t1.setDestination(t2);
        t2.setDestination(t1);

        AuroraSession s1 = new AuroraSession(db1);
        AuroraSession s2 = new AuroraSession(db2);

        k1 = new PublicKeys(s1.getPublicKey(), s1.getPublicSignKey(), "user1@test.com");
        k2 = new PublicKeys(s2.getPublicKey(), s2.getPublicSignKey(), "user2@test.com");

        h1 = new MockHandler();
        h2 = new MockHandler();

        m1 = new Messenger(t1, s1, tempDirUser1.toString(), h1);
        m2 = new Messenger(t2, s2, tempDirUser2.toString(), h2);
    }

    @AfterEach
    void tearDown() throws Exception {

        Files.walk(tempDirUser1).filter(Files::isRegularFile).map(Path::toFile).forEach(File::delete);
        Files.walk(tempDirUser2).filter(Files::isRegularFile).map(Path::toFile).forEach(File::delete);

        tempDirUser1.resolve("incoming").toFile().delete();
        tempDirUser2.resolve("incoming").toFile().delete();

        tempDirUser1.toFile().delete();
        tempDirUser2.toFile().delete();
    }

    private void exchangeKeys() throws Exception {

        m1.sendKeys("user2@test.com");
        assertEquals(1, t2.getKeys().size());

        h2.setPasswordReceived(h1.getPasswordSent());
        m2.receive();
        assertTrue(PublicKeysUtils.listAddresses(db2).contains("user1@test.com"));

        m2.sendKeys("user1@test.com");
        assertEquals(1, t1.getKeys().size());

        h1.setPasswordReceived(h2.getPasswordSent());
        m1.receive();
        assertTrue(PublicKeysUtils.listAddresses(db1).contains("user2@test.com"));
    }

    @Test
    void happyPath() throws Exception {

        exchangeKeys();

        int maxParts = Messenger.MAX_PARTS_TO_SEND_PER_FILE;
        int totalParts = maxParts + 3;

        int len = totalParts * Splitter.DEFAULT_PART_SIZE;
        byte[] content = new byte[len];
        Random r = new Random();
        r.nextBytes(content);
        FileOutputStream fout = new FileOutputStream(tempDirUser1.resolve("sample.bin").toFile());
        fout.write(content);
        fout.close();

        assertTrue(m1.addFileToSend(k2, tempDirUser1.resolve("sample.bin").toString()));

        assertArrayEquals(new Object[]{"sample.bin", "user2@test.com", 0, totalParts, totalParts},
                StatusUtils.getOutgoingFiles(db1).get(0).asRow());

        m1.send();

        m2.receive();

        assertArrayEquals(new Object[]{"user1@test.com", "sample.bin", maxParts, totalParts},
                StatusUtils.getIncomingFiles(db2).get(0).asRow());

        assertEquals(maxParts, t1.getConfs().size());

        m1.receive();

        assertArrayEquals(new Object[]{"sample.bin", "user2@test.com", maxParts, 3, totalParts},
                StatusUtils.getOutgoingFiles(db1).get(0).asRow());

        m1.send();

        assertArrayEquals(new Object[]{"sample.bin", "user2@test.com", maxParts, 0, totalParts},
                StatusUtils.getOutgoingFiles(db1).get(0).asRow());

        assertEquals(3, t2.getParts().size());

        m2.receive();

        assertEquals(0, StatusUtils.getIncomingFiles(db2).size());

        m2.send();
        m1.receive();

        assertEquals(0, StatusUtils.getOutgoingFiles(db1).size());

        assertEquals(0, t1.getParts().size());
        assertEquals(0, t1.getConfs().size());
        assertEquals(0, t1.getKeys().size());

        assertEquals(0, t2.getParts().size());
        assertEquals(0, t2.getConfs().size());
        assertEquals(0, t2.getKeys().size());

        FileInputStream fin = new FileInputStream(tempDirUser2.resolve("sample.bin").toFile());
        byte[] data = new byte[fin.available()];
        fin.read(data);
        fin.close();

        assertArrayEquals(content, data);
    }

    @Test
    void missingParts() throws Exception {

        exchangeKeys();

        int maxParts = Messenger.MAX_PARTS_TO_SEND_PER_FILE;
        int totalParts = maxParts + 4;

        int len = totalParts * Splitter.DEFAULT_PART_SIZE;
        byte[] content = new byte[len];
        Random r = new Random();
        r.nextBytes(content);
        FileOutputStream fout = new FileOutputStream(tempDirUser1.resolve("sample.bin").toFile());
        fout.write(content);
        fout.close();

        assertTrue(m1.addFileToSend(k2, tempDirUser1.resolve("sample.bin").toString()));

        assertArrayEquals(new Object[]{"sample.bin", "user2@test.com", 0, totalParts, totalParts},
                StatusUtils.getOutgoingFiles(db1).get(0).asRow());

        m1.send();

        t2.getParts().remove(maxParts-1);
        t2.getParts().remove(0);

        m2.receive();

        assertArrayEquals(new Object[]{"user1@test.com", "sample.bin", maxParts - 2, totalParts},
                StatusUtils.getIncomingFiles(db2).get(0).asRow());

        assertEquals(maxParts - 2, t1.getConfs().size());

        m1.receive();

        assertArrayEquals(new Object[]{"sample.bin", "user2@test.com", maxParts - 2, 4, totalParts},
                StatusUtils.getOutgoingFiles(db1).get(0).asRow());

        m1.send();

        assertArrayEquals(new Object[]{"sample.bin", "user2@test.com", maxParts - 2, 0, totalParts},
                StatusUtils.getOutgoingFiles(db1).get(0).asRow());

        assertEquals(4, t2.getParts().size());

        m2.receive();

        assertArrayEquals(new Object[]{"user1@test.com", "sample.bin", totalParts - 2, totalParts},
                StatusUtils.getIncomingFiles(db2).get(0).asRow());

        m2.send();

        assertEquals(4, t1.getConfs().size());

        m1.receive();

        assertArrayEquals(new Object[]{"sample.bin", "user2@test.com", totalParts - 2, 0, totalParts},
                StatusUtils.getOutgoingFiles(db1).get(0).asRow());

        for (int i = 0; i < PartToSendPO.COUNTER - 1; i++) {

            m1.receive();
        }

        assertArrayEquals(new Object[]{"sample.bin", "user2@test.com", totalParts - 2, 2, totalParts},
                StatusUtils.getOutgoingFiles(db1).get(0).asRow());

        m1.send();

        assertEquals(2, t2.getParts().size());

        m2.receive();

        assertEquals(0, StatusUtils.getIncomingFiles(db2).size());

        m2.send();
        m1.receive();

        assertEquals(0, StatusUtils.getOutgoingFiles(db1).size());

        assertEquals(0, t1.getParts().size());
        assertEquals(0, t1.getConfs().size());
        assertEquals(0, t1.getKeys().size());

        assertEquals(0, t2.getParts().size());
        assertEquals(0, t2.getConfs().size());
        assertEquals(0, t2.getKeys().size());

        FileInputStream fin = new FileInputStream(tempDirUser2.resolve("sample.bin").toFile());
        byte[] data = new byte[fin.available()];
        fin.read(data);
        fin.close();

        assertArrayEquals(content, data);
    }

    @Test
    void missingConfirmations() throws Exception {

        exchangeKeys();

        int maxParts = Messenger.MAX_PARTS_TO_SEND_PER_FILE;
        int totalParts = maxParts + 2;

        int len = totalParts * Splitter.DEFAULT_PART_SIZE;
        byte[] content = new byte[len];
        Random r = new Random();
        r.nextBytes(content);
        FileOutputStream fout = new FileOutputStream(tempDirUser1.resolve("sample.bin").toFile());
        fout.write(content);
        fout.close();

        assertTrue(m1.addFileToSend(k2, tempDirUser1.resolve("sample.bin").toString()));

        assertArrayEquals(new Object[]{"sample.bin", "user2@test.com", 0, totalParts, totalParts},
                StatusUtils.getOutgoingFiles(db1).get(0).asRow());

        m1.send();

        m2.receive();

        assertArrayEquals(new Object[]{"user1@test.com", "sample.bin", maxParts, totalParts},
                StatusUtils.getIncomingFiles(db2).get(0).asRow());

        assertEquals(maxParts, t1.getConfs().size());

        t1.getConfs().remove(2);
        t1.getConfs().remove(2);

        m1.receive();

        assertArrayEquals(new Object[]{"sample.bin", "user2@test.com", maxParts - 2, 2, totalParts},
                StatusUtils.getOutgoingFiles(db1).get(0).asRow());

        m1.send();

        assertArrayEquals(new Object[]{"sample.bin", "user2@test.com", maxParts - 2, 0, totalParts},
                StatusUtils.getOutgoingFiles(db1).get(0).asRow());

        assertEquals(2, t2.getParts().size());

        m2.receive();

        assertEquals(0, StatusUtils.getIncomingFiles(db2).size());

        m2.send();

        assertEquals(2, t1.getConfs().size());

        m1.receive();

        assertArrayEquals(new Object[]{"sample.bin", "user2@test.com", maxParts, 0, totalParts},
                StatusUtils.getOutgoingFiles(db1).get(0).asRow());

        for (int i = 0; i < PartToSendPO.COUNTER - 1; i++) {

            m1.receive();
        }

        assertArrayEquals(new Object[]{"sample.bin", "user2@test.com", maxParts, 2, totalParts},
                StatusUtils.getOutgoingFiles(db1).get(0).asRow());

        m1.send();

        assertEquals(2, t2.getParts().size());

        m2.receive();
        m2.send();

        assertEquals(2, t1.getConfs().size());

        m1.receive();

        assertEquals(0, StatusUtils.getOutgoingFiles(db1).size());


        assertEquals(0, StatusUtils.getOutgoingFiles(db1).size());

        assertEquals(0, t1.getParts().size());
        assertEquals(0, t1.getConfs().size());
        assertEquals(0, t1.getKeys().size());

        assertEquals(0, t2.getParts().size());
        assertEquals(0, t2.getConfs().size());
        assertEquals(0, t2.getKeys().size());

        FileInputStream fin = new FileInputStream(tempDirUser2.resolve("sample.bin").toFile());
        byte[] data = new byte[fin.available()];
        fin.read(data);
        fin.close();

        assertArrayEquals(content, data);
    }
}