package co.naes.aurora.msg;

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

import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StringMessageTest {

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

        StringOutMessage out = new StringOutMessage(session, pk, "aTestString", true);

        assertArrayEquals(out.getRecipient().getPublicKey(), pk.getPublicKey());
        assertEquals(out.getRecipient().getEmailAddress(), "sample2@test.com");

        byte[] ciphertext = out.getCiphertext();

        String sC = new String(ciphertext, StandardCharsets.UTF_8);
        assertTrue(sC.startsWith("BEGIN AURORA"));

        StringInMessage in = new StringInMessage(ciphertext);
        in.decrypt(session);

        assertArrayEquals(in.getSender().getPublicKey(), pk.getPublicKey());

        assertEquals(in.getData(), "aTestString");
    }

    @Test
    void binary() throws Exception {

        StringOutMessage out = new StringOutMessage(session, pk, "thisIsAnotherString to Send €‹!`~", false);

        assertArrayEquals(out.getRecipient().getPublicKey(), pk.getPublicKey());
        assertEquals(out.getRecipient().getEmailAddress(), "sample2@test.com");

        byte[] ciphertext = out.getCiphertext();

        String sC = new String(ciphertext, StandardCharsets.UTF_8);
        assertFalse(sC.startsWith("BEGIN AURORA"));

        StringInMessage in = new StringInMessage(ciphertext);
        in.decrypt(session);

        assertArrayEquals(in.getSender().getPublicKey(), pk.getPublicKey());

        assertEquals(in.getData(), "thisIsAnotherString to Send €‹!`~");
    }

    @Test
    void messageNotDecrypted() throws Exception {

        StringOutMessage out = new StringOutMessage(session, pk, "thisIsAnotherString to Send €‹!`~", false);

        assertArrayEquals(out.getRecipient().getPublicKey(), pk.getPublicKey());
        assertEquals(out.getRecipient().getEmailAddress(), "sample2@test.com");

        byte[] ciphertext = out.getCiphertext();

        StringInMessage in = new StringInMessage(ciphertext);

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

        PublicKeys wrong = new PublicKeys(publicKey, "sample2@test.com");

        StringOutMessage out = new StringOutMessage(session, wrong, "thisIsAnotherString to Send !!!", false);

        assertArrayEquals(out.getRecipient().getPublicKey(), publicKey);
        assertEquals(out.getRecipient().getEmailAddress(), "sample2@test.com");

        byte[] ciphertext = out.getCiphertext();

        StringInMessage in = new StringInMessage(ciphertext);

        Exception exception = assertThrows(AuroraException.class, () -> {

            in.decrypt(session);
        });
        assertTrue(exception.getMessage().contains("Error while decrypting message"));
    }

    @Test
    void longString() throws Exception {

        StringWriter sw = new StringWriter();
        Random r = new Random();
        for (int i = 0; i < 1024 * 1024 * 2 + 100; i++) {

            sw.write(r.nextInt(94) + 32);
        }
        String input = sw.toString();
        sw.close();

        StringOutMessage out = new StringOutMessage(session, pk, input, true);

        assertArrayEquals(out.getRecipient().getPublicKey(), pk.getPublicKey());
        assertEquals(out.getRecipient().getEmailAddress(), "sample2@test.com");

        byte[] ciphertext = out.getCiphertext();

        String sC = new String(ciphertext, StandardCharsets.UTF_8);
        assertTrue(sC.startsWith("BEGIN AURORA"));

        StringInMessage in = new StringInMessage(ciphertext);
        in.decrypt(session);

        assertArrayEquals(in.getSender().getPublicKey(), pk.getPublicKey());

        assertEquals(in.getData(), input);
    }
}
