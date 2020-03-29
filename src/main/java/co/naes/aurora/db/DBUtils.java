/*
 * Copyright (C) 2020  Luca Zanconato (<luca.zanconato@naes.co>)
 *
 * This file is part of Aurora.
 *
 * Aurora is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Aurora is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Aurora.  If not, see <http://www.gnu.org/licenses/>.
 */

package co.naes.aurora.db;

import co.naes.aurora.AuroraException;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public final class DBUtils {

    public static final String SESSION_EMAIL_ADDRESS = "aurora.session.email";
    public static final String SESSION_PUBLIC_KEY = "aurora.session.publickey";
    public static final String SESSION_SECRET_KEY = "aurora.session.secretkey";
    public static final String SESSION_SIGN_PUBLIC_KEY = "aurora.session.sign.publickey";
    public static final String SESSION_SIGN_SECRET_KEY = "aurora.session.sign.secretkey";
    public static final String MAIL_INCOMING_USERNAME = "aurora.mail.incoming.username";
    public static final String MAIL_INCOMING_PASSWORD = "aurora.mail.incoming.password";
    public static final String MAIL_OUTGOING_USERNAME = "aurora.mail.outgoing.username";
    public static final String MAIL_OUTGOING_PASSWORD = "aurora.mail.outgoing.password";
    public static final String ACCOUNT_NAME = "aurora.account.name";
    public static final String INCOMING_DIRECTORY = "aurora.incoming.directory";

    private static Properties properties = new Properties();

    private static Properties mailProperties = new Properties();

    private static String sConfFolder;

    private static String sPassword;

    public static boolean exists(String confFolder) {

        return new File(String.format("%s%saurora.mv.db", confFolder, File.separator)).exists();
    }

    public static void initialise(String confFolder, String password) throws AuroraException {

        sConfFolder = confFolder;
        sPassword = password;

        try (var conn = getConnection();
             var st = conn.createStatement()) {

            var rs = conn.getMetaData().getTables("", "", "PROPERTIES", null);
            if (rs.next()) {

                // load properties
                var props = st.executeQuery("SELECT * FROM PROPERTIES");
                while (props.next()) {

                    properties.put(props.getString(1), props.getString(2));
                }

                // load mail properties
                var mprops = st.executeQuery("SELECT * FROM MAIL_PROPERTIES");
                while (mprops.next()) {

                    mailProperties.put(mprops.getString(1), mprops.getString(2));
                }

            } else {

                // create tables
                st.execute("CREATE TABLE PROPERTIES (NAME VARCHAR PRIMARY KEY, VALUE VARCHAR);");
                st.execute("CREATE TABLE MAIL_PROPERTIES (NAME VARCHAR PRIMARY KEY, VALUE VARCHAR);");
                st.execute("CREATE TABLE PUBLIC_KEYS (EMAIL VARCHAR PRIMARY KEY, ENCRYPTION VARCHAR, SIGNATURE VARCHAR);");
                st.execute("CREATE TABLE OUTGOING_FILES (FILE_ID VARCHAR, PATH VARCHAR, EMAIL VARCHAR, TOTAL_PARTS INT, CONSTRAINT PK_OF PRIMARY KEY(FILE_ID, EMAIL), CONSTRAINT FK_EMAIL FOREIGN KEY(EMAIL) REFERENCES PUBLIC_KEYS(EMAIL));");
                st.execute("CREATE TABLE PARTS_TO_SEND (SEQUENCE INT, FILE_ID VARCHAR, EMAIL VARCHAR, SENT_ONCE BOOL, COUNTER INT, CONSTRAINT PK PRIMARY KEY (SEQUENCE, FILE_ID, EMAIL), CONSTRAINT FK_PS_FILE FOREIGN KEY(FILE_ID) REFERENCES OUTGOING_FILES(FILE_ID), CONSTRAINT FK_PS_EMAIL FOREIGN KEY(EMAIL) REFERENCES OUTGOING_FILES(EMAIL));");
                st.execute("CREATE TABLE INCOMING_FILES (FILE_ID VARCHAR, PATH VARCHAR, EMAIL VARCHAR, TOTAL_PARTS INT, COMPLETE BOOL, CONSTRAINT PK_IF PRIMARY KEY (FILE_ID, EMAIL), CONSTRAINT FK_INC_EMAIL FOREIGN KEY(EMAIL) REFERENCES PUBLIC_KEYS(EMAIL));");
                st.execute("CREATE TABLE PARTS_TO_RECEIVE (SEQUENCE INT, FILE_ID VARCHAR, EMAIL VARCHAR, CONSTRAINT PK_PR PRIMARY KEY (SEQUENCE, FILE_ID, EMAIL), CONSTRAINT FK_PR_FILE FOREIGN KEY(FILE_ID) REFERENCES INCOMING_FILES(FILE_ID), CONSTRAINT FK_PR_EMAIL FOREIGN KEY(EMAIL) REFERENCES INCOMING_FILES(EMAIL));");
            }

        } catch (SQLException ex) {

            throw new AuroraException("Error while loading/configuring the DB: " + ex.getMessage(), ex);
        }
    }

    public static Connection getConnection() throws AuroraException {

        if (sConfFolder == null || sPassword == null) {

            throw new AuroraException("Please initialise first");
        }

        try {

            // connect to the DB
            String url = String.format("jdbc:h2:%s/aurora;CIPHER=AES", sConfFolder);
            String user = "sa";
            String pwds = String.format("%s aurora", sPassword);
            return DriverManager.getConnection(url, user, pwds);

        } catch (SQLException ex) {

            throw new AuroraException("Error while connecting to the DB: " + ex.getMessage(), ex);
        }
    }

    public static void saveProperties() throws AuroraException {

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

    public static Properties getProperties() {

        return properties;
    }

    public static Properties getMailProperties() {

        return mailProperties;
    }

    private DBUtils() { }
}
