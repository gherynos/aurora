package co.naes.aurora;

import net.nharyes.libsaltpack.Constants;
import net.nharyes.libsaltpack.SaltpackException;
import net.nharyes.libsaltpack.Utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class LocalDB {

    public static final String SESSION_EMAIL_ADDRESS = "aurora.session.email";
    public static final String SESSION_PUBLIC_KEY = "aurora.session.publickey";
    public static final String SESSION_SECRET_KEY = "aurora.session.secretkey";
    public static final String SESSION_SIGN_PUBLIC_KEY = "aurora.session.sign.publickey";
    public static final String SESSION_SIGN_SECRET_KEY = "aurora.session.sign.secretkey";
    public static final String MAIL_INCOMING_USERNAME = "aurora.mail.incoming.username";
    public static final String MAIL_INCOMING_PASSWORD = "aurora.mail.incoming.password";
    public static final String MAIL_OUTGOING_USERNAME = "aurora.mail.outgoing.username";
    public static final String MAIL_OUTGOING_PASSWORD = "aurora.mail.outgoing.password";

    private String password;

    private Properties properties = new Properties();

    private Properties mailProperties = new Properties();

    public LocalDB(String password) throws AuroraException {

        this.password = password;

        try (var conn = getConnection();
             var st = conn.createStatement()) {

            var rs = conn.getMetaData().getTables("", "", "PROPERTIES", null);
            if (!rs.next()) {

                // create tables
                st.execute("CREATE TABLE PROPERTIES (NAME VARCHAR PRIMARY KEY, VALUE VARCHAR);");
                st.execute("CREATE TABLE MAIL_PROPERTIES (NAME VARCHAR PRIMARY KEY, VALUE VARCHAR);");
                st.execute("CREATE TABLE PUBLIC_KEYS (EMAIL VARCHAR PRIMARY KEY, ENCRYPTION VARCHAR, SIGNATURE VARCHAR);");
                st.execute("CREATE TABLE OUTGOING_FILES (FILE_ID VARCHAR PRIMARY KEY, PATH VARCHAR, EMAIL VARCHAR, CONSTRAINT FK_EMAIL FOREIGN KEY(EMAIL) REFERENCES PUBLIC_KEYS(EMAIL));");
                st.execute("CREATE TABLE PART_TO_SEND (SEQUENCE INT, FILE_ID VARCHAR, SENT_ONCE BOOL, CONSTRAINT PK PRIMARY KEY (SEQUENCE, FILE_ID), CONSTRAINT FK_FILE FOREIGN KEY(FILE_ID) REFERENCES OUTGOING_FILES(FILE_ID));");
                st.execute("CREATE TABLE INCOMING_FILES (FILE_ID VARCHAR PRIMARY KEY, PATH VARCHAR, EMAIL VARCHAR, CONSTRAINT FK_INC_EMAIL FOREIGN KEY(EMAIL) REFERENCES PUBLIC_KEYS(EMAIL));");
                st.execute("CREATE TABLE PART_TO_RECEIVE (SEQUENCE INT, FILE_ID VARCHAR, CONSTRAINT PK_REC PRIMARY KEY (SEQUENCE, FILE_ID), CONSTRAINT FK_INC_FILE FOREIGN KEY(FILE_ID) REFERENCES INCOMING_FILES(FILE_ID));");

            } else {

                // load properties
                var props = st.executeQuery("SELECT * FROM PROPERTIES");
                while (props.next())
                    properties.put(props.getString(1), props.getString(2));

                // load mail properties
                var mprops = st.executeQuery("SELECT * FROM MAIL_PROPERTIES");
                while (mprops.next())
                    mailProperties.put(mprops.getString(1), mprops.getString(2));
            }

        } catch (SQLException ex) {

            throw new AuroraException("Error while loading/configuring the DB: " + ex.getMessage(), ex);
        }
    }

    private Connection getConnection() throws SQLException {

        String url = String.format("jdbc:h2:%s/aurora;CIPHER=AES", Main.CONF_FOLDER);
        String user = "sa";
        String pwds = String.format("%s aurora", password);

        return DriverManager.getConnection(url, user, pwds);
    }

    public void saveProperties() throws AuroraException {

        try (var conn = getConnection();
             var st = conn.createStatement()) {

            for (Object oKey : properties.keySet()) {

                String key = (String) oKey;
                st.execute(String.format("MERGE INTO PROPERTIES KEY(NAME) VALUES('%s', '%s')", key, properties.getProperty(key)));
            }

            for (Object oKey : mailProperties.keySet()) {

                String key = (String) oKey;
                st.execute(String.format("MERGE INTO MAIL_PROPERTIES KEY(NAME) VALUES('%s', '%s')", key, mailProperties.getProperty(key)));
            }

        } catch (SQLException ex) {

            throw new AuroraException("Error while saving the properties to the DB: " + ex.getMessage(), ex);
        }
    }

    public Properties getProperties() {

        return properties;
    }

    public Properties getMailProperties() {

        return mailProperties;
    }

    public void storePublicKeys(PublicKeys keys) throws AuroraException {

        try (var conn = getConnection();
             var st = conn.prepareStatement("MERGE INTO PUBLIC_KEYS KEY(EMAIL) VALUES(?, ?, ?)")) {

            st.setString(1, keys.getEmailAddress());
            st.setString(2, Utils.baseXencode(keys.getPublicKey(), Constants.ALPHABET_BASE62));
            st.setString(3, Utils.baseXencode(keys.getPublicSignKey(), Constants.ALPHABET_BASE62));

            st.execute();

        } catch (SQLException | SaltpackException ex) {

            throw new AuroraException("Error while storing keys to the DB: " + ex.getMessage(), ex);
        }
    }

    public PublicKeys getPublicKeys(String emailAddress) throws AuroraException {

        try (var conn = getConnection();
             var st = conn.prepareStatement("SELECT * FROM PUBLIC_KEYS WHERE EMAIL = ?")) {

            st.setString(1, emailAddress);
            var res = st.executeQuery();
            if (!res.next())
                throw new AuroraException(String.format("Key for '%s' not found.", emailAddress));

            String id = res.getString(1);
            String encryption = res.getString(2);
            String signature = res.getString(3);

            return new PublicKeys(Utils.baseXdecode(encryption, Constants.ALPHABET_BASE62),
                    Utils.baseXdecode(signature, Constants.ALPHABET_BASE62), id);

        } catch (SQLException | SaltpackException ex) {

            throw new AuroraException("Error while loading keys from the DB: " + ex.getMessage(), ex);
        }
    }

    public PublicKeys getPublicKeys(byte[] encryptionKey) throws AuroraException {

        try (var conn = getConnection();
             var st = conn.prepareStatement("SELECT * FROM PUBLIC_KEYS WHERE ENCRYPTION = ?")) {

            st.setString(1, Utils.baseXencode(encryptionKey, Constants.ALPHABET_BASE62));
            var res = st.executeQuery();
            if (!res.next())
                throw new AuroraException("Entry for encryption key provided not found.");

            String id = res.getString(1);
            String encryption = res.getString(2);
            String signature = res.getString(3);

            return new PublicKeys(Utils.baseXdecode(encryption, Constants.ALPHABET_BASE62),
                    Utils.baseXdecode(signature, Constants.ALPHABET_BASE62), id);

        } catch (SQLException | SaltpackException ex) {

            throw new AuroraException("Error while loading keys from the DB: " + ex.getMessage(), ex);
        }
    }

    public List<String> listPublicKeysAddresses() throws AuroraException {

        try (var conn = getConnection();
             var st = conn.createStatement()) {

            List<String> out = new ArrayList<>();
            var res = st.executeQuery("SELECT EMAIL FROM PUBLIC_KEYS");
            while (res.next())
                out.add(res.getString(1));

            return out;

        } catch (SQLException ex) {

            throw new AuroraException("Error while loading keys addresses from the DB: " + ex.getMessage(), ex);
        }
    }

    public void addOutgoingFile(String fileId, String path, String emailAddress) throws AuroraException {

        try (var conn = getConnection();
             var st = conn.prepareStatement("INSERT INTO OUTGOING_FILES VALUES(?, ?, ?)")) {

            st.setString(1, fileId);
            st.setString(2, path);
            st.setObject(3, emailAddress);

            st.execute();

        } catch (SQLException ex) {

            throw new AuroraException("Error while storing outgoing file to the DB: " + ex.getMessage(), ex);
        }
    }

    public void addPartsToSend(String fileId, int totalParts) throws AuroraException {

        try (var conn = getConnection();
             var st = conn.prepareStatement("INSERT INTO PART_TO_SEND VALUES(?, ?, FALSE)")) {

            conn.setAutoCommit(false);

            for (int i = 0; i < totalParts; i++) {

                st.setInt(1, i);
                st.setString(2, fileId);
                st.addBatch();
            }

            int[] res = st.executeBatch();
            if (res.length != totalParts)
                throw new SQLException("Unable to insert all the parts");

            conn.commit();

        } catch (SQLException ex) {

            throw new AuroraException("Error while storing outgoing file to the DB: " + ex.getMessage(), ex);
        }
    }

    public List<String[]> getPendingOutputFiles() throws AuroraException {

        try (var conn = getConnection();
             var st = conn.createStatement()) {

            List<String[]> out = new ArrayList<>();
            var res = st.executeQuery("SELECT OF.* FROM OUTGOING_FILES OF WHERE (SELECT COUNT(SEQUENCE) FROM PART_TO_SEND PS WHERE PS.FILE_ID = OF.FILE_ID) > 0;");
            while (res.next())
                out.add(new String[]{res.getString(1), res.getString(2), res.getString(3)});

            return out;

        } catch (SQLException ex) {

            throw new AuroraException("Error while loading keys addresses from the DB: " + ex.getMessage(), ex);
        }
    }

    public List<Integer> getPartsToSend(String fileId) throws AuroraException {

        try (var conn = getConnection();
             var st = conn.prepareStatement("SELECT SEQUENCE FROM PART_TO_SEND WHERE FILE_ID = ? AND SENT_ONCE = FALSE")) {

            List<Integer> out = new ArrayList<>();
            st.setString(1, fileId);
            var res = st.executeQuery();
            while (res.next())
                out.add(res.getInt(1));

            return out;

        } catch (SQLException ex) {

            throw new AuroraException("Error while loading keys addresses from the DB: " + ex.getMessage(), ex);
        }
    }

    public void markPartsAsSent(List<Integer> sequenceNumbers, String fileId) throws AuroraException {

        try (var conn = getConnection();
             var st = conn.prepareStatement("UPDATE PART_TO_SEND SET SENT_ONCE = TRUE WHERE SEQUENCE = ? AND FILE_ID = ?")) {

            conn.setAutoCommit(false);

            for (Integer sequenceNumber : sequenceNumbers) {

                st.setInt(1, sequenceNumber);
                st.setString(2, fileId);
                st.addBatch();
            }

            int[] res = st.executeBatch();
            if (res.length != sequenceNumbers.size())
                throw new SQLException("Unable to update all the parts");

            conn.commit();

        } catch (SQLException ex) {

            throw new AuroraException("Error while updating part on the DB: " + ex.getMessage(), ex);
        }
    }

    public void deletePartToSend(int sequenceNumber, String fileId) throws AuroraException {

        try (var conn = getConnection();
             var st = conn.prepareStatement("DELETE FROM PART_TO_SEND WHERE SEQUENCE = ? AND FILE_ID = ?")) {

            st.setInt(1, sequenceNumber);
            st.setString(2, fileId);

            st.execute();

        } catch (SQLException ex) {

            throw new AuroraException("Error while updating part on the DB: " + ex.getMessage(), ex);
        }
    }

    public void addIncomingFile(String fileId, String path, String emailAddress) throws AuroraException {

        try (var conn = getConnection();
             var st = conn.prepareStatement("INSERT INTO INCOMING_FILES VALUES(?, ?, ?)")) {

            st.setString(1, fileId);
            st.setString(2, path);
            st.setObject(3, emailAddress);

            st.execute();

        } catch (SQLException ex) {

            throw new AuroraException("Error while storing outgoing file to the DB: " + ex.getMessage(), ex);
        }
    }

    public String[] getIncomingFile(String fileId) throws AuroraException {

        try (var conn = getConnection();
             var st = conn.prepareStatement("SELECT * FROM INCOMING_FILES WHERE FILE_ID = ?")) {

            st.setString(1, fileId);
            var res = st.executeQuery();

            if (!res.next())
                return null;

            return new String[]{res.getString(1), res.getString(2), res.getString(3)};

        } catch (SQLException ex) {

            throw new AuroraException("Error while checking incoming file presence: " + ex.getMessage(), ex);
        }
    }

    public void addPartsToReceive(String fileId, int totalParts) throws AuroraException {

        try (var conn = getConnection();
             var st = conn.prepareStatement("INSERT INTO PART_TO_RECEIVE VALUES(?, ?)")) {

            conn.setAutoCommit(false);

            for (int i = 0; i < totalParts; i++) {

                st.setInt(1, i);
                st.setString(2, fileId);
                st.addBatch();
            }

            int[] res = st.executeBatch();
            if (res.length != totalParts)
                throw new SQLException("Unable to insert all the parts");

            conn.commit();

        } catch (SQLException ex) {

            throw new AuroraException("Error while storing outgoing file to the DB: " + ex.getMessage(), ex);
        }
    }

    public void deletePartToReceive(int sequenceNumber, String fileId) throws AuroraException {

        try (var conn = getConnection();
             var st = conn.prepareStatement("DELETE FROM PART_TO_RECEIVE WHERE SEQUENCE = ? AND FILE_ID = ?")) {

            st.setInt(1, sequenceNumber);
            st.setString(2, fileId);

            st.execute();

        } catch (SQLException ex) {

            throw new AuroraException("Error while updating part on the DB: " + ex.getMessage(), ex);
        }
    }
}
