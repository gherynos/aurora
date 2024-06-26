package com.gherynos.aurora.msg;

import com.gherynos.aurora.AuroraException;
import com.gherynos.aurora.AuroraSession;
import com.gherynos.aurora.Identifier;
import com.gherynos.aurora.PublicKeys;
import com.gherynos.aurora.msg.in.ConfInMessage;
import com.gherynos.aurora.msg.in.PartInMessage;
import com.gherynos.aurora.msg.out.ConfOutMessage;
import com.gherynos.aurora.parts.PartId;
import com.gherynos.libsaltpack.Constants;
import com.gherynos.libsaltpack.Utils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConfMessageTest {

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

        pk = new PublicKeys(publicKey, new Identifier("sample3", "sample3@test.com"));
    }

    @Test
    void armored() throws Exception {

        PartId id = new PartId("aTestFile", 12);

        ConfOutMessage out = new ConfOutMessage(session, pk, id, true);

        assertArrayEquals(out.getRecipient().getPublicKey(), pk.getPublicKey());
        assertEquals(out.getRecipient().getIdentifier().getName(), "sample3");
        assertEquals(out.getRecipient().getIdentifier().getEmail(), "sample3@test.com");

        byte[] ciphertext = out.getCiphertext();

        String sC = new String(ciphertext, StandardCharsets.UTF_8);
        assertTrue(sC.startsWith("BEGIN AURORA"));
        assertTrue(out.isArmored());

        ConfInMessage in = new ConfInMessage(ciphertext);
        in.decrypt(session);

        assertArrayEquals(in.getSender().getPublicKey(), pk.getPublicKey());

        assertEquals(in.getData().getFileId(), "aTestFile");
        assertEquals(in.getData().getSequenceNumber(), 12);
    }

    @Test
    void binary() throws Exception {

        PartId id = new PartId("anotherTestFile", 862);

        ConfOutMessage out = new ConfOutMessage(session, pk, id, false);

        assertArrayEquals(out.getRecipient().getPublicKey(), pk.getPublicKey());
        assertEquals(out.getRecipient().getIdentifier().getName(), "sample3");
        assertEquals(out.getRecipient().getIdentifier().getEmail(), "sample3@test.com");
        assertFalse(out.isArmored());

        byte[] ciphertext = out.getCiphertext();

        ConfInMessage in = new ConfInMessage(ciphertext);
        in.decrypt(session);

        assertArrayEquals(in.getSender().getPublicKey(), pk.getPublicKey());

        assertEquals(in.getData().getFileId(), "anotherTestFile");
        assertEquals(in.getData().getSequenceNumber(), 862);
    }

    @Test
    void messageNotDecrypted() throws Exception {

        PartId id = new PartId("anotherTestFile", 123);

        ConfOutMessage out = new ConfOutMessage(session, pk, id, false);

        assertArrayEquals(out.getRecipient().getPublicKey(), pk.getPublicKey());
        assertEquals(out.getRecipient().getIdentifier().getName(), "sample3");
        assertEquals(out.getRecipient().getIdentifier().getEmail(), "sample3@test.com");

        byte[] ciphertext = out.getCiphertext();

        ConfInMessage in = new ConfInMessage(ciphertext);

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

        PublicKeys wrong = new PublicKeys(publicKey, new Identifier("sample4", "sample4@test.com"));

        PartId id = new PartId("anotherTestFile", 12);

        ConfOutMessage out = new ConfOutMessage(session, wrong, id, false);

        assertArrayEquals(out.getRecipient().getPublicKey(), publicKey);
        assertEquals(out.getRecipient().getIdentifier().getName(), "sample4");
        assertEquals(out.getRecipient().getIdentifier().getEmail(), "sample4@test.com");

        byte[] ciphertext = out.getCiphertext();

        PartInMessage in = new PartInMessage(ciphertext);

        Exception exception = assertThrows(AuroraException.class, () -> {

            in.decrypt(session);
        });
        assertTrue(exception.getMessage().contains("Error while decrypting message"));
    }
}
