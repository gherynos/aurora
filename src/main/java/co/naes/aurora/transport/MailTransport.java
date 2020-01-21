package co.naes.aurora.transport;

import co.naes.aurora.AuroraException;
import co.naes.aurora.LocalDB;
import co.naes.aurora.msg.*;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class MailTransport implements AuroraTransport {

    private final String HEADER = "X-Aurora";
    private final String HEADER_KEY = "Key";
    private final String HEADER_TEXT = "Text";
    private final String HEADER_PART = "Part";

    private LocalDB db;

    private AuroraIncomingMessageHandler messageHandler;

    public MailTransport(LocalDB db) {

        this.db = db;
    }

    private Session getSession(boolean incoming) {

        Authenticator auth = new Authenticator() {

            protected PasswordAuthentication getPasswordAuthentication() {

                if (incoming)
                    return new PasswordAuthentication(
                            db.getProperties().getProperty(LocalDB.MAIL_INCOMING_USERNAME),
                            db.getProperties().getProperty(LocalDB.MAIL_INCOMING_PASSWORD));

                else
                    return new PasswordAuthentication(
                            db.getProperties().getProperty(LocalDB.MAIL_OUTGOING_USERNAME),
                            db.getProperties().getProperty(LocalDB.MAIL_OUTGOING_PASSWORD));
            }
        };
        Session session = Session.getInstance(db.getMailProperties(), auth);
        session.setDebug(false);

        return session;
    }

    @Override
    public void setIncomingMessageHandler(AuroraIncomingMessageHandler messageHandler) {

        this.messageHandler = messageHandler;
    }

    @Override
    public void sendKeyMessage(OutKeyMessage key) throws AuroraException {

        if (!key.isArmored())
            throw new AuroraException("Please provide an armored key");

        Message message;
        try {

            // Create message
            message = new MimeMessage(getSession(false));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(key.getRecipientIdentifier()));
            message.setSubject("Aurora key");
            message.setHeader(HEADER, HEADER_KEY);

            // Content
            StringBuilder sb = new StringBuilder();
            sb.append("This is an Aurora key."); // TODO: change
            sb.append("\n");
            sb.append("\n");
            sb.append(new String(key.getCiphertext(), StandardCharsets.UTF_8));
            sb.append("\n");
            message.setContent(sb.toString(), "text/plain");

        } catch (MessagingException ex) {

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

        if (!msg.isArmored())
            throw new AuroraException("Please provide an armored message");

        Message message;
        try {

            // Create message
            message = new MimeMessage(getSession(false));
            message.setRecipients(Message.RecipientType.TO,
                    InternetAddress.parse(msg.getRecipient().getEmailAddress()));
            message.setSubject("Aurora message");

            if (msg instanceof StringOutMessage)
                message.setHeader(HEADER, HEADER_TEXT);

            else if (msg instanceof PartOutMessage)
                message.setHeader(HEADER, HEADER_PART);

            else
                throw new AuroraException("Unknow message type");

            // Content
            StringBuilder sb = new StringBuilder();
            sb.append("This is an Aurora message."); // TODO: change
            sb.append("\n");
            sb.append("\n");
            sb.append(new String(msg.getCiphertext(), StandardCharsets.UTF_8));
            sb.append("\n");
            message.setContent(sb.toString(), "text/plain");

        } catch (MessagingException ex) {

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

            String messageContent = "";
            Multipart multipart = (Multipart) content;
            for (int i = 0; i < multipart.getCount(); i++) {

                Part part = multipart.getBodyPart(i);
                if (part.isMimeType("text/plain")) {

                    messageContent = part.getContent().toString();
                    break;
                }
            }

            return messageContent;
        }
        return content.toString();
    }

    @Override
    public void checkForMessages() throws AuroraException {

        try (Store store = getSession(true).getStore()) {

            // Connect to the server
            store.connect();

            try (Folder inbox = store.getFolder("INBOX")) {

                inbox.open(Folder.READ_ONLY);
                Message[] messages = inbox.getMessages();
                for (Message message : messages) {

                    String[] header = message.getHeader(HEADER);
                    if (header != null) {

                        String content = getMessageContent(message);

                        int start = content.indexOf(InMessage.ARMOR_BEGIN);
                        int end = content.indexOf(InMessage.ARMOR_END);
                        if (start == -1 || end == -1) {

                            // TODO: log Unable to parse message content
                            continue;
                        }

                        content = content.substring(start, end + 38);
                        switch (header[0]) {

                            case HEADER_KEY: {

                                messageHandler.keyMessageReceived(new InKeyMessage(content.getBytes()));

                            } break;

                            case HEADER_TEXT: {

                                messageHandler.messageReceived(new StringInMessage(content.getBytes()));

                            } break;

                            case HEADER_PART: {

                                messageHandler.messageReceived(new PartInMessage(content.getBytes()));

                            } break;

                            default: {

                                // TODO: log wrong header content
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
