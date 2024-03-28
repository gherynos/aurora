package net.nharyes.aurora;

import net.nharyes.aurora.db.DBUtils;
import net.nharyes.aurora.db.PartToSendPO;
import net.nharyes.aurora.db.PublicKeysUtils;
import net.nharyes.aurora.db.StatusUtils;
import net.nharyes.aurora.parts.Splitter;
import net.nharyes.aurora.ui.vo.ReceivedFileVO;
import net.nharyes.aurora.ui.vo.SentFileVO;
import org.apache.logging.log4j.Level;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.apache.logging.log4j.core.config.Configurator;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
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

    private Identifier i1;
    private Identifier i2;

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

        db1.getProperties().setProperty(DBUtils.ACCOUNT_NAME, "user1");
        db1.getProperties().setProperty(DBUtils.SESSION_EMAIL_ADDRESS, "user1@test.com");
        db1.getProperties().setProperty(DBUtils.INCOMING_DIRECTORY, tempDirUser1.toString());
        db1.saveProperties();

        db2.getProperties().setProperty(DBUtils.ACCOUNT_NAME, "user2");
        db2.getProperties().setProperty(DBUtils.SESSION_EMAIL_ADDRESS, "user2@test.com");
        db2.getProperties().setProperty(DBUtils.INCOMING_DIRECTORY, tempDirUser2.toString());
        db2.saveProperties();

        t1 = new MockTransport("user1");
        t2 = new MockTransport("user2");

        t1.setDestination(t2);
        t2.setDestination(t1);

        AuroraSession s1 = new AuroraSession(db1);
        AuroraSession s2 = new AuroraSession(db2);

        i1 = s1.getPublicKeys().getIdentifier();
        i2 = s2.getPublicKeys().getIdentifier();

        k1 = s1.getPublicKeys();
        k2 = s2.getPublicKeys();

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

        m1.sendKeys(i2);
        assertEquals(1, t2.getKeys().size());

        h2.setPasswordReceived(h1.getPasswordSent());
        m2.receive();
        Assertions.assertTrue(PublicKeysUtils.listIdentifiers(db2).contains(i1));

        m1.receive();
        assertTrue(PublicKeysUtils.listIdentifiers(db1).contains(i2));
    }

    private void finalState(byte[] content) throws Exception {

        Assertions.assertEquals(0, StatusUtils.getIncomingFiles(db2).size());

        m2.send();
        m1.receive();

        assertEquals(0, StatusUtils.getOutgoingFiles(db1).size());

        assertEquals(0, t1.getParts().size());
        assertEquals(0, t1.getConfs().size());
        assertEquals(0, t1.getKeys().size());

        assertEquals(0, t2.getParts().size());
        assertEquals(0, t2.getConfs().size());
        assertEquals(0, t2.getKeys().size());

        assertEquals(0, StatusUtils.getReceivedFiles(db1).size());
        List<ReceivedFileVO> received = StatusUtils.getReceivedFiles(db2);
        assertEquals(1, received.size());
        assertEquals(i1, received.get(0).asRow()[1]);
        assertEquals("sample.bin", received.get(0).asRow()[2]);
        assertEquals(tempDirUser2.toString() + File.separator + "sample.bin",
                received.get(0).asRow()[3]);

        assertEquals(0, StatusUtils.getSentFiles(db2).size());
        List<SentFileVO> sent = StatusUtils.getSentFiles(db1);
        assertEquals(1, sent.size());
        assertEquals(i2, sent.get(0).asRow()[1]);
        assertEquals("sample.bin", sent.get(0).asRow()[2]);
        assertEquals(tempDirUser1.toString() + File.separator + "sample.bin",
                sent.get(0).asRow()[3]);

        FileInputStream fin = new FileInputStream(tempDirUser2.resolve("sample.bin").toFile());
        byte[] data = new byte[fin.available()];
        fin.read(data);
        fin.close();

        assertArrayEquals(content, data);
    }

    @Test
    void wrongKeyPassword() throws Exception {

        m1.sendKeys(i2);
        assertEquals(1, t2.getKeys().size());

        h2.setPasswordReceived("wrong".toCharArray());
        m2.receive();
        assertFalse(PublicKeysUtils.listIdentifiers(db2).contains(i1));

        assertTrue(h2.getKeyMessage().contains("Wrong password"));

        m2.sendKeys(i1);
        assertEquals(1, t1.getKeys().size());

        h1.setPasswordReceived("".toCharArray());
        m1.receive();
        assertFalse(PublicKeysUtils.listIdentifiers(db1).contains(i2));
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

        assertArrayEquals(new Object[]{"sample.bin", i2, 0, totalParts, totalParts},
                StatusUtils.getOutgoingFiles(db1).get(0).asRow());

        m1.send();

        m2.receive();

        assertArrayEquals(new Object[]{i1, "sample.bin", maxParts, totalParts},
                StatusUtils.getIncomingFiles(db2).get(0).asRow());

        assertEquals(maxParts, t1.getConfs().size());

        m1.receive();

        assertArrayEquals(new Object[]{"sample.bin", i2, maxParts, 3, totalParts},
                StatusUtils.getOutgoingFiles(db1).get(0).asRow());

        m1.send();

        assertArrayEquals(new Object[]{"sample.bin", i2, maxParts, 0, totalParts},
                StatusUtils.getOutgoingFiles(db1).get(0).asRow());

        assertEquals(3, t2.getParts().size());

        assertEquals(0, StatusUtils.getReceivedFiles(db1).size());
        assertEquals(0, StatusUtils.getSentFiles(db2).size());

        m2.receive();

        finalState(content);
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

        assertArrayEquals(new Object[]{"sample.bin", i2, 0, totalParts, totalParts},
                StatusUtils.getOutgoingFiles(db1).get(0).asRow());

        m1.send();

        t2.getParts().remove(maxParts-1);
        t2.getParts().remove(0);

        m2.receive();

        assertArrayEquals(new Object[]{i1, "sample.bin", maxParts - 2, totalParts},
                StatusUtils.getIncomingFiles(db2).get(0).asRow());

        assertEquals(maxParts - 2, t1.getConfs().size());

        m1.receive();

        assertArrayEquals(new Object[]{"sample.bin", i2, maxParts - 2, 4, totalParts},
                StatusUtils.getOutgoingFiles(db1).get(0).asRow());

        m1.send();

        assertArrayEquals(new Object[]{"sample.bin", i2, maxParts - 2, 0, totalParts},
                StatusUtils.getOutgoingFiles(db1).get(0).asRow());

        assertEquals(4, t2.getParts().size());

        m2.receive();

        assertArrayEquals(new Object[]{i1, "sample.bin", totalParts - 2, totalParts},
                StatusUtils.getIncomingFiles(db2).get(0).asRow());

        m2.send();

        assertEquals(4, t1.getConfs().size());

        m1.receive();

        assertArrayEquals(new Object[]{"sample.bin", i2, totalParts - 2, 0, totalParts},
                StatusUtils.getOutgoingFiles(db1).get(0).asRow());

        for (int i = 0; i < PartToSendPO.COUNTER - 1; i++) {

            m1.receive();
        }

        assertArrayEquals(new Object[]{"sample.bin", i2, totalParts - 2, 2, totalParts},
                StatusUtils.getOutgoingFiles(db1).get(0).asRow());

        m1.send();

        assertEquals(2, t2.getParts().size());

        m2.receive();

        finalState(content);
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

        assertArrayEquals(new Object[]{"sample.bin", i2, 0, totalParts, totalParts},
                StatusUtils.getOutgoingFiles(db1).get(0).asRow());

        m1.send();

        m2.receive();

        assertArrayEquals(new Object[]{i1, "sample.bin", maxParts, totalParts},
                StatusUtils.getIncomingFiles(db2).get(0).asRow());

        assertEquals(maxParts, t1.getConfs().size());

        t1.getConfs().remove(2);
        t1.getConfs().remove(2);

        m1.receive();

        assertArrayEquals(new Object[]{"sample.bin", i2, maxParts - 2, 2, totalParts},
                StatusUtils.getOutgoingFiles(db1).get(0).asRow());

        m1.send();

        assertArrayEquals(new Object[]{"sample.bin", i2, maxParts - 2, 0, totalParts},
                StatusUtils.getOutgoingFiles(db1).get(0).asRow());

        assertEquals(2, t2.getParts().size());

        m2.receive();

        assertEquals(0, StatusUtils.getIncomingFiles(db2).size());

        m2.send();

        assertEquals(2, t1.getConfs().size());

        m1.receive();

        assertArrayEquals(new Object[]{"sample.bin", i2, maxParts, 0, totalParts},
                StatusUtils.getOutgoingFiles(db1).get(0).asRow());

        for (int i = 0; i < PartToSendPO.COUNTER - 1; i++) {

            m1.receive();
        }

        assertArrayEquals(new Object[]{"sample.bin", i2, maxParts, 2, totalParts},
                StatusUtils.getOutgoingFiles(db1).get(0).asRow());

        m1.send();

        assertEquals(2, t2.getParts().size());

        m2.receive();
        m2.send();

        assertEquals(2, t1.getConfs().size());

        m1.receive();

        finalState(content);
    }

    @Test
    void fileAlreadyAdded() throws Exception {

        exchangeKeys();

        FileOutputStream fout = new FileOutputStream(tempDirUser1.resolve("sample.bin").toFile());
        fout.write("test".getBytes());
        fout.close();

        assertTrue(m1.addFileToSend(k2, tempDirUser1.resolve("sample.bin").toString()));

        assertFalse(m1.addFileToSend(k2, tempDirUser1.resolve("sample.bin").toString()));
    }

    @Test
    void recipientNotFound() throws Exception {

        FileOutputStream fout = new FileOutputStream(tempDirUser1.resolve("sample.bin").toFile());
        fout.write("test".getBytes());
        fout.close();

        Exception exception = assertThrows(AuroraException.class, () -> {

            m2.addFileToSend(k1, tempDirUser1.resolve("sample.bin").toString());
        });
        assertTrue(exception.getMessage().contains("Recipient 'user1 <user1@test.com>' not found"));
    }

    @Test
    void duplicateConfirmation() throws Exception {

        exchangeKeys();

        int totalParts = 2;

        int len = totalParts * Splitter.DEFAULT_PART_SIZE;
        byte[] content = new byte[len];
        Random r = new Random();
        r.nextBytes(content);
        FileOutputStream fout = new FileOutputStream(tempDirUser1.resolve("sample.bin").toFile());
        fout.write(content);
        fout.close();

        assertTrue(m1.addFileToSend(k2, tempDirUser1.resolve("sample.bin").toString()));

        assertArrayEquals(new Object[]{"sample.bin", i2, 0, totalParts, totalParts},
                StatusUtils.getOutgoingFiles(db1).get(0).asRow());

        m1.send();

        assertEquals(totalParts, t2.getParts().size());

        m2.receive();

        assertEquals(totalParts, t1.getConfs().size());

        byte[] conf = t1.getConfs().get(0).clone();

        m1.receive();

        assertEquals(0, StatusUtils.getOutgoingFiles(db1).size());

        m1.send();

        assertEquals(0, StatusUtils.getOutgoingFiles(db1).size());

        t1.getConfs().add(conf);

        m1.receive();

        assertEquals(0, StatusUtils.getOutgoingFiles(db1).size());

        finalState(content);
    }

    @Test
    void exceptions() throws Exception {

        Configurator.setLevel("net.nharyes.aurora", Level.OFF);

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

        assertArrayEquals(new Object[]{"sample.bin", i2, 0, totalParts, totalParts},
                StatusUtils.getOutgoingFiles(db1).get(0).asRow());

        t1.setRaiseException(true);
        m1.send();

        assertTrue(h1.hasErrorsWhileSendingMessages());
        assertFalse(h1.hasErrorsWhileReceivingMessages());
        h1.resetErrors();

        t1.setRaiseException(false);
        m1.send();

        assertFalse(h1.hasErrorsWhileSendingMessages());
        assertFalse(h1.hasErrorsWhileReceivingMessages());
        h1.resetErrors();

        t2.setRaiseException(true);
        m2.receive();

        assertFalse(h2.hasErrorsWhileSendingMessages());
        assertTrue(h2.hasErrorsWhileReceivingMessages());
        h2.resetErrors();

        t2.setRaiseException(false);
        m2.receive();

        assertFalse(h2.hasErrorsWhileSendingMessages());
        assertFalse(h2.hasErrorsWhileReceivingMessages());
        h2.resetErrors();

        assertArrayEquals(new Object[]{i1, "sample.bin", maxParts, totalParts},
                StatusUtils.getIncomingFiles(db2).get(0).asRow());

        assertEquals(maxParts, t1.getConfs().size());

        m1.receive();

        assertArrayEquals(new Object[]{"sample.bin", i2, maxParts, 3, totalParts},
                StatusUtils.getOutgoingFiles(db1).get(0).asRow());

        t1.setRaiseException(true);
        m1.send();

        t1.setRaiseException(false);
        m1.send();

        assertArrayEquals(new Object[]{"sample.bin", i2, maxParts, 0, totalParts},
                StatusUtils.getOutgoingFiles(db1).get(0).asRow());

        assertEquals(3, t2.getParts().size());

        m2.receive();

        finalState(content);
    }
}
