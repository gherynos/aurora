package net.nharyes.aurora.msg;

import net.nharyes.aurora.AuroraException;
import net.nharyes.aurora.AuroraSession;
import net.nharyes.aurora.Identifier;
import net.nharyes.aurora.PublicKeys;
import net.nharyes.aurora.msg.in.PartInMessage;
import net.nharyes.aurora.msg.in.PublicKeysInMessage;
import net.nharyes.aurora.msg.out.PartOutMessage;
import net.nharyes.aurora.msg.out.PublicKeysOutMessage;
import net.nharyes.aurora.parts.Part;
import net.nharyes.aurora.parts.PartId;
import net.nharyes.libsaltpack.Constants;
import net.nharyes.libsaltpack.Utils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PublicKeysMessageTest {

    @Mock
    private AuroraSession session;

    private PublicKeys pk;

    private PublicKeys pk2;

    @BeforeEach
    void setUp() throws Exception {

        byte[] publicKey = new byte[Constants.CRYPTO_BOX_PUBLICKEYBYTES];
        byte[] secretKey = new byte[Constants.CRYPTO_BOX_SECRETKEYBYTES];
        byte[] secretSignKey = new byte[Constants.CRYPTO_SIGN_SECRETKEYBYTES];
        byte[] publicSignKey = new byte[Constants.CRYPTO_SIGN_PUBLICKEYBYTES];

        Utils.generateKeypair(publicKey, secretKey);
        Utils.generateSignKeypair(publicSignKey, secretSignKey);

        when(session.getSecretKey()).thenReturn(secretKey);

        pk = new PublicKeys(publicKey, new Identifier("sample2", "sample2@test.com"));

        publicKey = new byte[Constants.CRYPTO_BOX_PUBLICKEYBYTES];
        secretKey = new byte[Constants.CRYPTO_BOX_SECRETKEYBYTES];
        secretSignKey = new byte[Constants.CRYPTO_SIGN_SECRETKEYBYTES];
        publicSignKey = new byte[Constants.CRYPTO_SIGN_PUBLICKEYBYTES];

        Utils.generateKeypair(publicKey, secretKey);
        Utils.generateSignKeypair(publicSignKey, secretSignKey);

        pk2 = new PublicKeys(publicKey, publicSignKey, new Identifier("another", "another@test.com"));
    }

    @Test
    void armored() throws Exception {

        PublicKeysOutMessage out = new PublicKeysOutMessage(session, pk, pk2, true);

        assertArrayEquals(out.getRecipient().getPublicKey(), pk.getPublicKey());
        assertEquals(out.getRecipient().getIdentifier().getName(), "sample2");
        assertEquals(out.getRecipient().getIdentifier().getEmail(), "sample2@test.com");

        byte[] ciphertext = out.getCiphertext();

        String sC = new String(ciphertext, StandardCharsets.UTF_8);
        assertTrue(sC.startsWith("BEGIN AURORA"));

        PublicKeysInMessage in = new PublicKeysInMessage(ciphertext);
        in.decrypt(session);

        assertArrayEquals(in.getSender().getPublicKey(), pk.getPublicKey());

        assertArrayEquals(pk2.getPublicKey(), in.getData().getPublicKey());
        assertArrayEquals(pk2.getPublicSignKey(), in.getData().getPublicSignKey());
        assertEquals(pk2.getIdentifier().serialise(), pk2.getIdentifier().serialise());
    }

    @Test
    void binary() throws Exception {

        PublicKeysOutMessage out = new PublicKeysOutMessage(session, pk, pk2, false);

        assertArrayEquals(out.getRecipient().getPublicKey(), pk.getPublicKey());
        assertEquals(out.getRecipient().getIdentifier().getName(), "sample2");
        assertEquals(out.getRecipient().getIdentifier().getEmail(), "sample2@test.com");

        byte[] ciphertext = out.getCiphertext();

        PublicKeysInMessage in = new PublicKeysInMessage(ciphertext);
        in.decrypt(session);

        assertArrayEquals(in.getSender().getPublicKey(), pk.getPublicKey());

        assertArrayEquals(pk2.getPublicKey(), in.getData().getPublicKey());
        assertArrayEquals(pk2.getPublicSignKey(), in.getData().getPublicSignKey());
        assertEquals(pk2.getIdentifier().getName(), pk2.getIdentifier().getName());
        assertEquals(pk2.getIdentifier().getEmail(), pk2.getIdentifier().getEmail());
    }

    @Test
    void messageNotDecrypted() throws Exception {

        PublicKeysOutMessage out = new PublicKeysOutMessage(session, pk, pk2, false);

        assertArrayEquals(out.getRecipient().getPublicKey(), pk.getPublicKey());
        assertEquals(out.getRecipient().getIdentifier().getName(), "sample2");
        assertEquals(out.getRecipient().getIdentifier().getEmail(), "sample2@test.com");

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

        PublicKeys wrong = new PublicKeys(publicKey, new Identifier("sample3", "sample3@test.com"));

        Part p = new Part(new PartId("temp", 0), 1, 6, "Sample".getBytes());

        PartOutMessage out = new PartOutMessage(session, wrong, p, false);

        assertArrayEquals(out.getRecipient().getPublicKey(), publicKey);
        assertEquals(out.getRecipient().getIdentifier().getName(), "sample3");
        assertEquals(out.getRecipient().getIdentifier().getEmail(), "sample3@test.com");
        assertFalse(out.isArmored());

        byte[] ciphertext = out.getCiphertext();

        PartInMessage in = new PartInMessage(ciphertext);
        assertFalse(in.isArmored());

        Exception exception = assertThrows(AuroraException.class, () -> {

            in.decrypt(session);
        });
        assertTrue(exception.getMessage().contains("Error while decrypting message"));
    }
}
