package co.naes.aurora.db;

import co.naes.aurora.AuroraException;
import co.naes.aurora.PublicKeys;
import net.nharyes.libsaltpack.Constants;
import net.nharyes.libsaltpack.SaltpackException;
import net.nharyes.libsaltpack.Utils;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public final class PublicKeysUtils {

    public static void store(PublicKeys keys) throws AuroraException {

        try (var conn = DBUtils.getConnection();
             var st = conn.prepareStatement("MERGE INTO PUBLIC_KEYS KEY(EMAIL) VALUES(?, ?, ?)")) {

            st.setString(1, keys.getEmailAddress());
            st.setString(2, Utils.baseXencode(keys.getPublicKey(), Constants.ALPHABET_BASE62));
            st.setString(3, Utils.baseXencode(keys.getPublicSignKey(), Constants.ALPHABET_BASE62));

            st.execute();

        } catch (SQLException | SaltpackException ex) {

            throw new AuroraException("Error while storing keys to the DB: " + ex.getMessage(), ex);
        }
    }

    public static PublicKeys get(String emailAddress) throws AuroraException {

        try (var conn = DBUtils.getConnection();
             var st = conn.prepareStatement("SELECT * FROM PUBLIC_KEYS WHERE EMAIL = ?")) {

            st.setString(1, emailAddress);
            var res = st.executeQuery();
            if (!res.next()) {

                throw new AuroraException(String.format("Key for '%s' not found.", emailAddress));
            }

            String id = res.getString(1);
            String encryption = res.getString(2);
            String signature = res.getString(3);

            return new PublicKeys(Utils.baseXdecode(encryption, Constants.ALPHABET_BASE62),
                    Utils.baseXdecode(signature, Constants.ALPHABET_BASE62), id);

        } catch (SQLException | SaltpackException ex) {

            throw new AuroraException("Error while loading keys from the DB: " + ex.getMessage(), ex);
        }
    }

    public static PublicKeys get(byte[] encryptionKey) throws AuroraException {

        try (var conn = DBUtils.getConnection();
             var st = conn.prepareStatement("SELECT * FROM PUBLIC_KEYS WHERE ENCRYPTION = ?")) {

            st.setString(1, Utils.baseXencode(encryptionKey, Constants.ALPHABET_BASE62));
            var res = st.executeQuery();
            if (!res.next()) {

                throw new AuroraException("Entry for encryption key provided not found.");
            }

            String id = res.getString(1);
            String encryption = res.getString(2);
            String signature = res.getString(3);

            return new PublicKeys(Utils.baseXdecode(encryption, Constants.ALPHABET_BASE62),
                    Utils.baseXdecode(signature, Constants.ALPHABET_BASE62), id);

        } catch (SQLException | SaltpackException ex) {

            throw new AuroraException("Error while loading keys from the DB: " + ex.getMessage(), ex);
        }
    }

    public static List<String> listAddresses() throws AuroraException {

        try (var conn = DBUtils.getConnection();
             var st = conn.createStatement()) {

            List<String> out = new ArrayList<>();
            var res = st.executeQuery("SELECT EMAIL FROM PUBLIC_KEYS");
            while (res.next()) {

                out.add(res.getString(1));
            }

            return out;

        } catch (SQLException ex) {

            throw new AuroraException("Error while loading keys addresses from the DB: " + ex.getMessage(), ex);
        }
    }

    private PublicKeysUtils() { }
}
