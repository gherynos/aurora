package co.naes.aurora;

import co.naes.aurora.db.DBUtils;
import net.nharyes.libsaltpack.Constants;
import net.nharyes.libsaltpack.SaltpackException;
import net.nharyes.libsaltpack.Utils;

import java.util.Properties;

public class AuroraSession {

    private byte[] secretKey;
    private byte[] publicKey;

    private byte[] secretSignKey;
    private byte[] publicSignKey;

    private String emailAddress;

    protected AuroraSession() throws AuroraException {

        try {

            Properties p = DBUtils.getProperties();
            emailAddress = p.getProperty(DBUtils.SESSION_EMAIL_ADDRESS);

            // load existing keys from DB
            if (p.containsKey(DBUtils.SESSION_PUBLIC_KEY)) {

                publicKey = Utils.baseXdecode(p.getProperty(DBUtils.SESSION_PUBLIC_KEY), Constants.ALPHABET_BASE62);
                secretKey = Utils.baseXdecode(p.getProperty(DBUtils.SESSION_SECRET_KEY), Constants.ALPHABET_BASE62);
                publicSignKey = Utils.baseXdecode(p.getProperty(DBUtils.SESSION_SIGN_PUBLIC_KEY), Constants.ALPHABET_BASE62);
                secretSignKey = Utils.baseXdecode(p.getProperty(DBUtils.SESSION_SIGN_SECRET_KEY), Constants.ALPHABET_BASE62);

            } else {

                // generate encryption keys
                publicKey = new byte[Constants.CRYPTO_BOX_PUBLICKEYBYTES];
                secretKey = new byte[Constants.CRYPTO_BOX_SECRETKEYBYTES];
                Utils.generateKeypair(publicKey, secretKey);

                // generate signature keys
                secretSignKey = new byte[Constants.CRYPTO_SIGN_SECRETKEYBYTES];
                publicSignKey = new byte[Constants.CRYPTO_SIGN_PUBLICKEYBYTES];
                Utils.generateSignKeypair(publicSignKey, secretSignKey);

                // store keys
                p.setProperty(DBUtils.SESSION_PUBLIC_KEY, Utils.baseXencode(publicKey, Constants.ALPHABET_BASE62));
                p.setProperty(DBUtils.SESSION_SECRET_KEY, Utils.baseXencode(secretKey, Constants.ALPHABET_BASE62));
                p.setProperty(DBUtils.SESSION_SIGN_PUBLIC_KEY, Utils.baseXencode(publicSignKey, Constants.ALPHABET_BASE62));
                p.setProperty(DBUtils.SESSION_SIGN_SECRET_KEY, Utils.baseXencode(secretSignKey, Constants.ALPHABET_BASE62));
                DBUtils.saveProperties();
            }

        } catch (SaltpackException ex) {

            throw new AuroraException("Unable to generate keys: " + ex.getMessage(), ex);
        }
    }

    public byte[] getSecretKey() {

        return secretKey.clone();
    }

    public byte[] getPublicKey() {

        return publicKey.clone();
    }

    public byte[] getSignSecretKey() {

        return secretSignKey.clone();
    }

    public byte[] getPublicSignKey() {

        return publicSignKey.clone();
    }

    public String getEmailAddress() {

        return emailAddress;
    }
}
