package co.naes.aurora;

import net.nharyes.libsaltpack.Constants;
import net.nharyes.libsaltpack.SaltpackException;
import net.nharyes.libsaltpack.Utils;

public class AuroraSession {

    private byte[] secretKey;
    private byte[] publicKey;

    private byte[] secretSignKey;
    private byte[] publicSignKey;

    private String identifier;

    AuroraSession() throws AuroraException {

        try {

            secretKey = Utils.deriveKeyFromPassword(Constants.CRYPTO_BOX_SECRETKEYBYTES, "ThePassword:D".toCharArray(), "theSaltToUse0123".getBytes(), 10000, 10000);
            publicKey = Utils.derivePublickey(secretKey);

            secretSignKey = new byte[Constants.CRYPTO_SIGN_SECRETKEYBYTES];
            publicSignKey = new byte[Constants.CRYPTO_SIGN_PUBLICKEYBYTES];
            Utils.generateSignKeypair(publicSignKey, secretSignKey);

            identifier = "service@naes.co";

        } catch (SaltpackException ex) {

            throw new AuroraException("Unable to derive keys: " + ex.getMessage(), ex);
        }
    }

    public byte[] getSecretKey() {

        return secretKey;
    }

    public byte[] getPublicKey() {

        return publicKey;
    }

    public byte[] getSignSecretKey() {

        return secretSignKey;
    }

    public byte[] getSignPublicKey() {

        return publicSignKey;
    }

    public String getIdentifier() {

        return identifier;
    }
}
