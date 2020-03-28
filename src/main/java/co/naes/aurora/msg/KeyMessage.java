package co.naes.aurora.msg;

import co.naes.aurora.AuroraException;
import net.nharyes.libsaltpack.Constants;
import net.nharyes.libsaltpack.SaltpackException;
import net.nharyes.libsaltpack.Utils;

public abstract class KeyMessage extends CiphertextMessage {

    protected static final byte[] SALT = "84e=tz+>4AH8L9A4".getBytes();

    private static final byte[] IDENTIFIER = "AuroraTempKey".getBytes();

    protected KeyMessage() {

        super();
    }

    protected byte[][] deriveKeyFromPassword(char ... password) throws AuroraException {

        try {

            // derive key from password
            byte[] bin = Utils.deriveKeyFromPassword(Constants.CRYPTO_BOX_SECRETKEYBYTES, password, SALT,
                    Constants.CRYPTO_PWHASH_OPSLIMIT_MODERATE, Constants.CRYPTO_PWHASH_MEMLIMIT_MODERATE);

            return new byte[][]{IDENTIFIER, bin};

        } catch (SaltpackException ex) {

            throw new AuroraException("Error while deriving key from password: " + ex.getMessage(), ex);
        }
    }
}
