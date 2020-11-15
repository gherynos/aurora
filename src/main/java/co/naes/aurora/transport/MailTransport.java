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

package co.naes.aurora.transport;  // NOPMD

import co.naes.aurora.AuroraException;
import co.naes.aurora.db.DBUtils;
import co.naes.aurora.msg.key.InKeyMessage;
import co.naes.aurora.msg.InMessage;
import co.naes.aurora.msg.key.OutKeyMessage;
import co.naes.aurora.msg.OutMessage;
import net.nharyes.libsaltpack.Constants;
import net.nharyes.libsaltpack.SaltpackException;
import net.nharyes.libsaltpack.Utils;

import javax.mail.Authenticator;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.search.SubjectTerm;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidParameterException;
import java.util.Properties;
import java.util.Random;
import java.util.logging.Logger;

public class MailTransport implements AuroraTransport {

    protected final Logger logger = Logger.getLogger(getClass().getName());

    private static final String HEADER = "X-Aurora-Type";
    private static final String HEADER_KEY = "Key";

    private IncomingMessageHandler messageHandler;

    private final String repository;

    private GmailOAuthUtils gmailOAuthUtils;

    private final DBUtils db;

    private final Properties main;

    private final Properties mail;

    public MailTransport(DBUtils db, String repository) {

        this.db = db;
        this.repository = repository;

        main = db.getProperties();
        mail = db.getMailProperties();
    }

    private String getRandomString() throws AuroraException  {

        try {

            Random r = new Random();
            byte[] data = new byte[10];
            r.nextBytes(data);

            return Utils.baseXencode(data, Constants.ALPHABET_BASE62);

        } catch (SaltpackException ex) {

            throw new AuroraException("Unable to generate random string.", ex);
        }
    }

    private Session getSession(boolean incoming) throws AuroraException {

        boolean isGMail = main.getProperty(DBUtils.MAIL_MODE).equals(DBUtils.MAIL_MODE_GMAIL);

        if (gmailOAuthUtils == null && isGMail) {

            gmailOAuthUtils = new GmailOAuthUtils(db);
        }

        String iPassword = isGMail ? gmailOAuthUtils.getAccessToken() : main.getProperty(DBUtils.MAIL_INCOMING_PASSWORD);  // NOPMD
        String oPassword = isGMail ? gmailOAuthUtils.getAccessToken() : main.getProperty(DBUtils.MAIL_OUTGOING_PASSWORD);  // NOPMD

        Authenticator auth = new Authenticator() {  // NOPMD

            @Override
            protected PasswordAuthentication getPasswordAuthentication() {

                if (incoming) {

                    return new PasswordAuthentication(main.getProperty(DBUtils.MAIL_INCOMING_USERNAME), iPassword);

                } else {

                    return new PasswordAuthentication(main.getProperty(DBUtils.MAIL_OUTGOING_USERNAME), oPassword);
                }
            }
        };
        Session session = Session.getInstance(mail, auth);
        session.setDebug(false);

        return session;
    }

    @Override
    public void setIncomingMessageHandler(IncomingMessageHandler messageHandler) {

        this.messageHandler = messageHandler;
    }

    @Override
    public void sendKeyMessage(OutKeyMessage key) throws AuroraException {

        if (!key.isArmored()) {

            throw new AuroraException("Please provide an armored key");
        }

        Message message;
        try {

            // Create message
            message = new MimeMessage(getSession(false));
            message.setFrom(new InternetAddress(main.getProperty(DBUtils.SESSION_EMAIL_ADDRESS),
                    main.getProperty(DBUtils.ACCOUNT_NAME)));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(key.getRecipientIdentifier().getEmail()));
            message.setSubject(String.format("Aurora key %s", getRandomString()));
            message.setHeader(HEADER, HEADER_KEY);

            // Content
            String content = String.format("This is an Aurora key.\nPlease check %s for more info.\n\n%s\n",
                    repository, new String(key.getCiphertext(), StandardCharsets.UTF_8));
            message.setContent(content, "text/plain");

        } catch (MessagingException | UnsupportedEncodingException ex) {

            throw new AuroraException("Error while creating key message: " + ex.getMessage(), ex);
        }

        try {

            Transport.send(message);

        } catch (MessagingException ex) {

            throw new AuroraException("Error while sending key message: " + ex.getMessage(), ex);
        }
    }

    @Override
    public void sendMessage(OutMessage<?> msg) throws AuroraException {

        if (!msg.isArmored()) {

            throw new AuroraException("Please provide an armored message");
        }

        Message message;
        try {

            // Create message
            message = new MimeMessage(getSession(false));
            message.setFrom(new InternetAddress(main.getProperty(DBUtils.SESSION_EMAIL_ADDRESS),
                    main.getProperty(DBUtils.ACCOUNT_NAME)));
            message.setRecipients(Message.RecipientType.TO,
                    InternetAddress.parse(msg.getRecipient().getIdentifier().getEmail()));
            message.setSubject(String.format("Aurora message %s", getRandomString()));
            message.setHeader(HEADER, OutMessage.getIdentifier(msg.getClass()));

            // Content
            String content = String.format("This is an Aurora message.\nPlease check %s for more info.\n\n%s\n",
                    repository, new String(msg.getCiphertext(), StandardCharsets.UTF_8));
            message.setContent(content, "text/plain");

        } catch (MessagingException | UnsupportedEncodingException ex) {

            throw new AuroraException("Error while creating message: " + ex.getMessage(), ex);
        }

        try {

            Transport.send(message);

        } catch (MessagingException ex) {

            throw new AuroraException("Error while sending message: " + ex.getMessage(), ex);
        }
    }

    private String getMessageContent(Message message) throws MessagingException, IOException {

        Object content = message.getContent();

        if (content instanceof Multipart) {

            Multipart multipart = (Multipart) content;
            for (int i = 0; i < multipart.getCount(); i++) {

                Part part = multipart.getBodyPart(i);
                if (part.isMimeType("text/plain")) {

                    return part.getContent().toString();
                }
            }
        }

        return content.toString();
    }

    @Override
    public void checkForMessages() throws AuroraException {  // NOPMD

        try (Store store = getSession(true).getStore()) {

            // Connect to the server
            store.connect();

            try (Folder inbox = store.getFolder("INBOX")) {

                inbox.open(Folder.READ_WRITE);
                Message[] messages = inbox.search(new SubjectTerm("Aurora"));
                for (Message message : messages) {

                    String[] header = message.getHeader(HEADER);
                    if (header == null) {

                        logger.finer(String.format("Discarded message %d", message.getMessageNumber()));

                    } else {

                        String content = getMessageContent(message);

                        int start = content.indexOf(InMessage.ARMOR_BEGIN);
                        int end = content.indexOf(InMessage.ARMOR_END);
                        if (start == -1 || end == -1) {

                            // unable to parse message content
                            logger.warning(String.format("Unable to parse message content '%s'", content));
                            continue;
                        }

                        content = content.substring(start, end + 38);
                        if (header[0].equals(HEADER_KEY)) {

                            InternetAddress from = (InternetAddress) message.getFrom()[0];
                            String sender = String.format("%s - %s", from.getPersonal(), from.getAddress());
                            boolean res = messageHandler.keyMessageReceived(new InKeyMessage(content.getBytes(), sender));  // NOPMD

                            // mark message for deletion
                            message.setFlag(Flags.Flag.DELETED, res);

                        } else {

                            try {

                                var constructor = InMessage.getClass(header[0]).getConstructor(byte[].class);
                                boolean res = messageHandler.messageReceived(constructor.newInstance((Object) content.getBytes()));

                                // mark message for deletion
                                message.setFlag(Flags.Flag.DELETED, res);

                            } catch (InvalidParameterException | SecurityException | ReflectiveOperationException ex) {

                                // unknown identifier
                                logger.warning(String.format("Unknow message identifier '%s'", header[0]));
                            }
                        }
                    }
                }

            } catch (MessagingException | IOException ex) {

                throw new AuroraException("Unable to fetch messages: " + ex.getMessage(), ex);
            }

        } catch (MessagingException ex) {

            throw new AuroraException("Unable to connect to the server: " + ex.getMessage(), ex);
        }
    }
}
