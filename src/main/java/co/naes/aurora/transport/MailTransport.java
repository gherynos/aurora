package co.naes.aurora.transport;

import co.naes.aurora.AuroraException;
import co.naes.aurora.LocalDB;
import co.naes.aurora.msg.key.InKeyMessage;
import co.naes.aurora.msg.InMessage;
import co.naes.aurora.msg.key.OutKeyMessage;
import co.naes.aurora.msg.OutMessage;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.search.SubjectTerm;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidParameterException;
import java.util.logging.Logger;

public class MailTransport implements AuroraTransport {

    protected final Logger logger = Logger.getLogger(getClass().getName());

    private static final String HEADER = "X-Aurora-Type";
    private static final String HEADER_KEY = "Key";

    private final LocalDB db;

    private IncomingMessageHandler messageHandler;

    public MailTransport(LocalDB db) {

        this.db = db;
    }

    private Session getSession(boolean incoming) {

        Authenticator auth = new Authenticator() {

            protected PasswordAuthentication getPasswordAuthentication() {

                if (incoming) {

                    return new PasswordAuthentication(
                            db.getProperties().getProperty(LocalDB.MAIL_INCOMING_USERNAME),
                            db.getProperties().getProperty(LocalDB.MAIL_INCOMING_PASSWORD));

                } else {

                    return new PasswordAuthentication(
                            db.getProperties().getProperty(LocalDB.MAIL_OUTGOING_USERNAME),
                            db.getProperties().getProperty(LocalDB.MAIL_OUTGOING_PASSWORD));
                }
            }
        };
        Session session = Session.getInstance(db.getMailProperties(), auth);
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
            message.setFrom(new InternetAddress(
                    db.getProperties().getProperty(LocalDB.SESSION_EMAIL_ADDRESS),
                    db.getProperties().getProperty(LocalDB.ACCOUNT_NAME)));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(key.getRecipientIdentifier()));
            message.setSubject("Aurora key");  // TODO: change
            message.setHeader(HEADER, HEADER_KEY);

            // Content
            StringBuilder sb = new StringBuilder();
            sb.append("This is an Aurora key."); // TODO: change
            sb.append("\n");
            sb.append("\n");
            sb.append(new String(key.getCiphertext(), StandardCharsets.UTF_8));
            sb.append("\n");
            message.setContent(sb.toString(), "text/plain");

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
            message.setFrom(new InternetAddress(
                    db.getProperties().getProperty(LocalDB.SESSION_EMAIL_ADDRESS),
                    db.getProperties().getProperty(LocalDB.ACCOUNT_NAME)));
            message.setRecipients(Message.RecipientType.TO,
                    InternetAddress.parse(msg.getRecipient().getEmailAddress()));
            message.setSubject("Aurora message");  // TODO: change
            message.setHeader(HEADER, OutMessage.getIdentifier(msg.getClass()));

            // Content
            StringBuilder sb = new StringBuilder();
            sb.append("This is an Aurora message."); // TODO: change
            sb.append("\n");
            sb.append("\n");
            sb.append(new String(msg.getCiphertext(), StandardCharsets.UTF_8));
            sb.append("\n");
            message.setContent(sb.toString(), "text/plain");

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
                Message[] messages = inbox.search(new SubjectTerm("Aurora"));  // TODO: change
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
                            boolean res = messageHandler.keyMessageReceived(new InKeyMessage(content.getBytes(), sender));

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
