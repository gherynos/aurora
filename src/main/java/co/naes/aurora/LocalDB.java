package co.naes.aurora;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
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

        String sFolder = String.format("%s%c.aurora", System.getProperty("user.home"), File.separatorChar);

        String url = String.format("jdbc:h2:%s/aurora;CIPHER=AES", sFolder);
        String user = "sa";
        String pwds = String.format("%s aurora", password);

        return DriverManager.getConnection(url, user, pwds);
    }

    public void saveProperties() throws AuroraException {

        try (var conn = getConnection();
             var st = conn.createStatement()) {

            for (Object oKey: properties.keySet()) {

                String key = (String) oKey;
                st.execute(String.format("MERGE INTO PROPERTIES KEY(NAME) VALUES('%s', '%s')", key, properties.getProperty(key)));
            }

            for (Object oKey: mailProperties.keySet()) {

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

        return properties;
    }
}
