package co.naes.aurora.msg;

import co.naes.aurora.AuroraException;
import co.naes.aurora.AuroraSession;
import co.naes.aurora.PublicKeys;
import co.naes.aurora.msg.in.PartInMessage;
import co.naes.aurora.msg.out.PartOutMessage;
import co.naes.aurora.parts.Part;
import co.naes.aurora.parts.PartId;
import net.nharyes.libsaltpack.Constants;
import net.nharyes.libsaltpack.Utils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.charset.StandardCharsets;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PartMessageTest {

    @Mock
    private AuroraSession session;

    private PublicKeys pk;

    @BeforeEach
    void setUp() throws Exception {

        byte[] publicKey = new byte[Constants.CRYPTO_BOX_PUBLICKEYBYTES];
        byte[] secretKey = new byte[Constants.CRYPTO_BOX_SECRETKEYBYTES];
        byte[] secretSignKey = new byte[Constants.CRYPTO_SIGN_SECRETKEYBYTES];
        byte[] publicSignKey = new byte[Constants.CRYPTO_SIGN_PUBLICKEYBYTES];

        Utils.generateKeypair(publicKey, secretKey);
        Utils.generateSignKeypair(publicSignKey, secretSignKey);

        when(session.getSecretKey()).thenReturn(secretKey);

        pk = new PublicKeys(publicKey, "sample2@test.com");
    }

    @Test
    void armored() throws Exception {

        Part p = new Part(new PartId("blah", 2), 12, 1024, "The content of the part".getBytes());

        PartOutMessage out = new PartOutMessage(session, pk, p, true);

        assertArrayEquals(out.getRecipient().getPublicKey(), pk.getPublicKey());
        assertEquals(out.getRecipient().getIdentifier(), "sample2@test.com");

        byte[] ciphertext = out.getCiphertext();

        String sC = new String(ciphertext, StandardCharsets.UTF_8);
        assertTrue(sC.startsWith("BEGIN AURORA"));

        PartInMessage in = new PartInMessage(ciphertext);
        in.decrypt(session);

        assertArrayEquals(in.getSender().getPublicKey(), pk.getPublicKey());

        assertArrayEquals(in.getData().getData(), "The content of the part".getBytes());
        assertEquals(in.getData().getId().getFileId(), "blah");
        assertEquals(in.getData().getId().getSequenceNumber(), 2);
        assertEquals(in.getData().getTotal(), 12);
        assertEquals(in.getData().getTotalSize(), 1024);
    }

    @Test
    void binary() throws Exception {

        Part p = new Part(new PartId("blah2", 0), 4, 512, "Another part".getBytes());

        PartOutMessage out = new PartOutMessage(session, pk, p, false);

        assertArrayEquals(out.getRecipient().getPublicKey(), pk.getPublicKey());
        assertEquals(out.getRecipient().getIdentifier(), "sample2@test.com");

        byte[] ciphertext = out.getCiphertext();

        PartInMessage in = new PartInMessage(ciphertext);
        in.decrypt(session);

        assertArrayEquals(in.getSender().getPublicKey(), pk.getPublicKey());

        assertArrayEquals(in.getData().getData(), "Another part".getBytes());
        assertEquals(in.getData().getId().getFileId(), "blah2");
        assertEquals(in.getData().getId().getSequenceNumber(), 0);
        assertEquals(in.getData().getTotal(), 4);
        assertEquals(in.getData().getTotalSize(), 512);
    }

    @Test
    void messageNotDecrypted() throws Exception {

        Part p = new Part(new PartId("blah2", 0), 4, 512, "Another part".getBytes());

        PartOutMessage out = new PartOutMessage(session, pk, p, false);

        assertArrayEquals(out.getRecipient().getPublicKey(), pk.getPublicKey());
        assertEquals(out.getRecipient().getIdentifier(), "sample2@test.com");

        byte[] ciphertext = out.getCiphertext();

        PartInMessage in = new PartInMessage(ciphertext);

        Exception exception = assertThrows(AuroraException.class, in::getData);
        assertTrue(exception.getMessage().contains("Decrypt message first"));

        exception = assertThrows(AuroraException.class, in::getSender);
        assertTrue(exception.getMessage().contains("Decrypt message first"));
    }

    @Test
    void wrongKey() throws Exception {

        byte[] publicKey = new byte[Constants.CRYPTO_BOX_PUBLICKEYBYTES];
        byte[] secretKey = new byte[Constants.CRYPTO_BOX_SECRETKEYBYTES];
        Utils.generateKeypair(publicKey, secretKey);

        PublicKeys wrong = new PublicKeys(publicKey, "sample3@test.com");

        Part p = new Part(new PartId("temp", 0), 1, 6, "Sample".getBytes());

        PartOutMessage out = new PartOutMessage(session, wrong, p, false);

        assertArrayEquals(out.getRecipient().getPublicKey(), publicKey);
        assertEquals(out.getRecipient().getIdentifier(), "sample3@test.com");
        assertFalse(out.isArmored());

        byte[] ciphertext = out.getCiphertext();

        PartInMessage in = new PartInMessage(ciphertext);
        assertFalse(in.isArmored());

        Exception exception = assertThrows(AuroraException.class, () -> {

            in.decrypt(session);
        });
        assertTrue(exception.getMessage().contains("Error while decrypting message"));
    }

    @Test
    void longArray() throws Exception {

        int len = 1024 * 1024 * 3 + 123;
        byte[] content = new byte[len];
        Random r = new Random();
        r.nextBytes(content);
        Part p = new Part(new PartId("blah", 2), 12, len * 12, content);

        PartOutMessage out = new PartOutMessage(session, pk, p, true);

        assertArrayEquals(out.getRecipient().getPublicKey(), pk.getPublicKey());
        assertEquals(out.getRecipient().getIdentifier(), "sample2@test.com");
        assertTrue(out.isArmored());

        byte[] ciphertext = out.getCiphertext();

        String sC = new String(ciphertext, StandardCharsets.UTF_8);
        assertTrue(sC.startsWith("BEGIN AURORA"));

        PartInMessage in = new PartInMessage(ciphertext);
        assertTrue(in.isArmored());
        in.decrypt(session);

        assertArrayEquals(in.getSender().getPublicKey(), pk.getPublicKey());

        assertArrayEquals(in.getData().getData(), content);
        assertEquals(in.getData().getId().getFileId(), "blah");
        assertEquals(in.getData().getId().getSequenceNumber(), 2);
        assertEquals(in.getData().getTotal(), 12);
        assertEquals(in.getData().getTotalSize(), len * 12);
    }
}
