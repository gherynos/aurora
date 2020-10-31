package co.naes.aurora.msg;

import co.naes.aurora.AuroraException;
import co.naes.aurora.AuroraSession;
import co.naes.aurora.PublicKeys;
import co.naes.aurora.msg.key.InKeyMessage;
import co.naes.aurora.msg.key.OutKeyMessage;
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

        when(session.getPublicKey()).thenReturn(publicKey);
        when(session.getSignSecretKey()).thenReturn(secretSignKey);
        when(session.getEmailAddress()).thenReturn("sample4@test.com");
    }

    @Test
    void armored() throws Exception {

        OutKeyMessage out = new OutKeyMessage(session, "theRecipient", true);

        assertEquals(out.getRecipientIdentifier(), "theRecipient");
        assertTrue(out.isArmored());

        byte[] ciphertext = out.getCiphertext();

        String sC = new String(ciphertext, StandardCharsets.UTF_8);
        assertTrue(sC.startsWith("BEGIN AURORA"));

        InKeyMessage in = new InKeyMessage(ciphertext, "theSender");

        PublicKeys received = in.getPublicKeys(out.getPassword());

        assertArrayEquals(received.getPublicKey(), session.getPublicKey());
        assertArrayEquals(received.getPublicSignKey(), publicSignKey);
        assertEquals(received.getEmailAddress(), "sample4@test.com");
    }

    @Test
    void binary() throws Exception {

        OutKeyMessage out = new OutKeyMessage(session, "theRecipient2", false);

        assertEquals(out.getRecipientIdentifier(), "theRecipient2");
        assertFalse(out.isArmored());

        byte[] ciphertext = out.getCiphertext();

        InKeyMessage in = new InKeyMessage(ciphertext, "theSender");

        PublicKeys received = in.getPublicKeys(out.getPassword());

        assertArrayEquals(received.getPublicKey(), session.getPublicKey());
        assertArrayEquals(received.getPublicSignKey(), publicSignKey);
        assertEquals(received.getEmailAddress(), "sample4@test.com");
    }

    @Test
    void wrongPassword() throws Exception {

        OutKeyMessage out = new OutKeyMessage(session, "theRecipient2", false);

        assertEquals(out.getRecipientIdentifier(), "theRecipient2");
        assertFalse(out.isArmored());

        byte[] ciphertext = out.getCiphertext();

        InKeyMessage in = new InKeyMessage(ciphertext, "theSender");

        Exception exception = assertThrows(AuroraException.class, () -> {

            in.getPublicKeys("wrong password".toCharArray());
        });
        assertTrue(exception.getMessage().contains("Error while decrypting key"));
    }
}
