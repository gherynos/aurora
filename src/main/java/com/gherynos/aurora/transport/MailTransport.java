/*
 * Copyright (C) 2020-2024  Luca Zanconato (<github.com/gherynos>)
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

package com.gherynos.aurora.transport;

import com.gherynos.aurora.AuroraException;
import com.gherynos.aurora.db.DBUtils;
import com.gherynos.aurora.msg.InMessage;
import com.gherynos.aurora.msg.key.InKeyMessage;
import com.gherynos.aurora.msg.key.OutKeyMessage;
import com.gherynos.aurora.msg.OutMessage;
import com.gherynos.libsaltpack.Constants;
import com.gherynos.libsaltpack.SaltpackException;
import com.gherynos.libsaltpack.Utils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.activation.DataHandler;
import javax.mail.Authenticator;
import javax.mail.BodyPart;
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
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.search.SubjectTerm;
import javax.mail.util.ByteArrayDataSource;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.InvalidParameterException;
import java.util.Properties;
import java.util.Random;

@SuppressWarnings("PMD.ExcessiveImports")
public class MailTransport implements AuroraTransport {

    protected static final Logger LOGGER = LogManager.getLogger();

    private static final String HEADER = "X-Aurora-Type";
    private static final String HEADER_KEY = "Key";

    private static final String AURORA_MIME_TYPE = "text/plain";
    private static final String AURORA_FILE_NAME = "data.aurora";

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

    private String getRandomString() throws AuroraException {

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

        String iPassword = isGMail ? gmailOAuthUtils.getAccessToken() : main.getProperty(DBUtils.MAIL_INCOMING_PASSWORD);
        String oPassword = isGMail ? gmailOAuthUtils.getAccessToken() : main.getProperty(DBUtils.MAIL_OUTGOING_PASSWORD);

        Authenticator auth = new Authenticator() {

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

    private void sendMessage(String recipient, String subject, byte[] data, String header) throws AuroraException {

        Message message;
        try {

            // Create message
            message = new MimeMessage(getSession(false));
            message.setFrom(new InternetAddress(main.getProperty(DBUtils.SESSION_EMAIL_ADDRESS),
                    main.getProperty(DBUtils.ACCOUNT_NAME)));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipient));
            message.setSubject(subject);
            message.setHeader(HEADER, header);

            // Content
            BodyPart messageBodyPart = new MimeBodyPart();
            String content = String.format("This is an Aurora message.\nPlease check %s for more info.\n", repository);
            messageBodyPart.setText(content);
            Multipart multipart = new MimeMultipart();
            multipart.addBodyPart(messageBodyPart);

            // attachment
            BodyPart messageAttachmentPart = new MimeBodyPart();
            messageAttachmentPart.setDataHandler(new DataHandler(new ByteArrayDataSource(data, AURORA_MIME_TYPE)));
            messageAttachmentPart.setFileName(AURORA_FILE_NAME);
            messageAttachmentPart.setHeader("Content-Transfer-Encoding", "7bit");
            multipart.addBodyPart(messageAttachmentPart);
            message.setContent(multipart);

        } catch (MessagingException | UnsupportedEncodingException ex) {

            throw new AuroraException("Error while creating message: " + ex.getMessage(), ex);
        }

        try {

            Transport.send(message);

        } catch (MessagingException ex) {

            throw new AuroraException("Error while sending message: " + ex.getMessage(), ex);
        }
    }

    @Override
    public void sendKeyMessage(OutKeyMessage key) throws AuroraException {

        sendMessage(key.getRecipientIdentifier().getEmail(),
                String.format("Aurora key %s", getRandomString()), key.getCiphertext(),
                HEADER_KEY);
    }

    @Override
    public void sendMessage(OutMessage<?> msg) throws AuroraException {

        sendMessage(msg.getRecipient().getIdentifier().getEmail(),
                String.format("Aurora message %s", getRandomString()), msg.getCiphertext(),
                OutMessage.getIdentifier(msg.getClass()));
    }

    private byte[] getMessageContent(Message message) throws MessagingException, IOException {

        Object content = message.getContent();

        if (content instanceof Multipart multipart) {

            for (int i = 0; i < multipart.getCount(); i++) {

                Part part = multipart.getBodyPart(i);
                if (part.isMimeType(AURORA_MIME_TYPE) && AURORA_FILE_NAME.equals(part.getFileName())) {

                    try (var in = part.getInputStream();
                         ByteArrayOutputStream bout = new ByteArrayOutputStream()) {

                        in.transferTo(bout);
                        return bout.toByteArray();
                    }
                }
            }
        }

        throw new MessagingException("Aurora attachment not found");
    }

    @Override
    @SuppressWarnings({"PMD.CognitiveComplexity", "PMD.CyclomaticComplexity"})
    public void checkForMessages() throws AuroraException {

        try (Store store = getSession(true).getStore()) {

            // Connect to the server
            store.connect();

            try (Folder inbox = store.getFolder("INBOX")) {

                inbox.open(Folder.READ_WRITE);
                Message[] messages = inbox.search(new SubjectTerm("Aurora"));
                for (Message message : messages) {

                    String[] header = message.getHeader(HEADER);
                    if (header == null) {

                        if (LOGGER.isWarnEnabled()) {

                            LOGGER.warn("Discarded message {}", message.getMessageNumber());
                        }

                    } else {

                        if (HEADER_KEY.equals(header[0])) {

                            InternetAddress from = (InternetAddress) message.getFrom()[0];
                            String sender = String.format("%s - %s", from.getPersonal(), from.getAddress());
                            boolean res = messageHandler.keyMessageReceived(new InKeyMessage(getMessageContent(message), sender));

                            // mark message for deletion
                            message.setFlag(Flags.Flag.DELETED, res);

                        } else {

                            try {

                                var constructor = InMessage.getClass(header[0]).getConstructor(byte[].class);
                                boolean res = messageHandler.messageReceived(constructor.newInstance((Object) getMessageContent(message)));

                                // mark message for deletion
                                message.setFlag(Flags.Flag.DELETED, res);

                            } catch (InvalidParameterException | SecurityException | ReflectiveOperationException ex) {

                                // unknown identifier
                                if (LOGGER.isWarnEnabled()) {

                                    LOGGER.warn("Unknow message identifier {}}", header[0]);
                                }
                            }
                        }
                    }
                }

            } catch (MessagingException | IOException ex) {

                throw new AuroraException("Unable to fetch messages: " + ex.getMessage(), ex);
            }

        } catch (MessagingException ex) {  // NOPMD

            throw new AuroraException("Unable to connect to the server: " + ex.getMessage(), ex);
        }
    }

    @Override
    public boolean requiresArmoredMessages() {

        return true;
    }
}
