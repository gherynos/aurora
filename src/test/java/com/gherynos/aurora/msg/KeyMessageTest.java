package com.gherynos.aurora.msg;

import com.gherynos.aurora.AuroraException;
import com.gherynos.aurora.AuroraSession;
import com.gherynos.aurora.Identifier;
import com.gherynos.aurora.PublicKeys;
import com.gherynos.aurora.msg.key.InKeyMessage;
import com.gherynos.aurora.msg.key.OutKeyMessage;
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
class KeyMessageTest {

    @Mock
    private AuroraSession session;

    private final byte[] publicSignKey = new byte[Constants.CRYPTO_SIGN_PUBLICKEYBYTES];

    @BeforeEach
    void setUp() throws Exception {

        byte[] publicKey = new byte[Constants.CRYPTO_BOX_PUBLICKEYBYTES];
        byte[] secretKey = new byte[Constants.CRYPTO_BOX_SECRETKEYBYTES];
        byte[] secretSignKey = new byte[Constants.CRYPTO_SIGN_SECRETKEYBYTES];

        Utils.generateKeypair(publicKey, secretKey);
        Utils.generateSignKeypair(publicSignKey, secretSignKey);

        when(session.getPublicKeys()).thenReturn(new PublicKeys(publicKey, publicSignKey,
                new Identifier("sample4", "sample4@test.com")));
        when(session.getSignSecretKey()).thenReturn(secretSignKey);
    }

    @Test
    void armored() throws Exception {

        Identifier id = new Identifier("theRecipient", "aaa@bbb.com");

        OutKeyMessage out = new OutKeyMessage(session, id, true);

        assertEquals(out.getRecipientIdentifier().getName(), id.getName());
        assertEquals(out.getRecipientIdentifier().getEmail(), id.getEmail());
        assertTrue(out.isArmored());

        byte[] ciphertext = out.getCiphertext();

        String sC = new String(ciphertext, StandardCharsets.UTF_8);
        assertTrue(sC.startsWith("BEGIN AURORA"));

        InKeyMessage in = new InKeyMessage(ciphertext, "theSender");

        PublicKeys received = in.getPublicKeys(out.getPassword());

        assertArrayEquals(received.getPublicKey(), session.getPublicKeys().getPublicKey());
        assertArrayEquals(received.getPublicSignKey(), publicSignKey);
        assertEquals(received.getIdentifier().getEmail(), "sample4@test.com");
    }

    @Test
    void binary() throws Exception {

        Identifier id = new Identifier("theRecipient2", "aaa2@bbb.com");

        OutKeyMessage out = new OutKeyMessage(session, id, false);

        assertEquals(out.getRecipientIdentifier().getName(), id.getName());
        assertEquals(out.getRecipientIdentifier().getEmail(), id.getEmail());
        assertFalse(out.isArmored());

        byte[] ciphertext = out.getCiphertext();

        InKeyMessage in = new InKeyMessage(ciphertext, "theSender");

        PublicKeys received = in.getPublicKeys(out.getPassword());

        assertArrayEquals(received.getPublicKey(), session.getPublicKeys().getPublicKey());
        assertArrayEquals(received.getPublicSignKey(), publicSignKey);
        assertEquals(received.getIdentifier().getEmail(), "sample4@test.com");
    }

    @Test
    void wrongPassword() throws Exception {

        Identifier id = new Identifier("theRecipient2", "aaa2@bbb.com");

        OutKeyMessage out = new OutKeyMessage(session, id, false);

        assertEquals(out.getRecipientIdentifier().getName(), id.getName());
        assertEquals(out.getRecipientIdentifier().getEmail(), id.getEmail());
        assertFalse(out.isArmored());

        byte[] ciphertext = out.getCiphertext();

        InKeyMessage in = new InKeyMessage(ciphertext, "theSender");

        Exception exception = assertThrows(AuroraException.class, () -> {

            in.getPublicKeys("wrong password".toCharArray());
        });
        assertTrue(exception.getMessage().contains("Error while decrypting key"));
    }
}
