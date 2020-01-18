package co.naes.aurora.transport;

import co.naes.aurora.AuroraException;
import co.naes.aurora.msg.AuroraInKeyMessage;
import co.naes.aurora.msg.AuroraInMessage;
import co.naes.aurora.msg.AuroraOutKeyMessage;
import co.naes.aurora.msg.AuroraOutMessage;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

public class MailTransport implements AuroraTransport {

    private final String HEADER = "X-Aurora";
    private final String HEADER_KEY = "Key";
    private final String HEADER_MESSAGE = "Message";

    private Properties props;

    private AuroraIncomingMessageHandler messageHandler;

    public MailTransport() {

        props = new Properties();

        props.setProperty("mail.store.protocol", "imap");
        props.setProperty("mail.imap.host", "imap.mail.eu-west-1.awsapps.com");
        props.setProperty("mail.imap.port", "993");
        props.setProperty("mail.imap.ssl.enable", "true");
        props.setProperty("mail.imap.starttls.enable", "false");

        props.setProperty("mail.smtp.host", "smtp.mail.eu-west-1.awsapps.com");
        props.setProperty("mail.smtp.port", "465");
        props.setProperty("mail.smtp.auth", "true");
        props.setProperty("mail.smtp.ssl.enable", "true");
        props.setProperty("mail.smtp.starttls.enable", "false");
        props.setProperty("mail.smtp.from", "service@naes.co");
//        props.setProperty("mail.smtp.localaddress", "127.0.0.1");
    }

    private Session getSession() {

        Authenticator auth = new Authenticator() {

            //override the getPasswordAuthentication method
            protected PasswordAuthentication getPasswordAuthentication() {

                return new PasswordAuthentication("service@naes.co", "22U3jA/PRr2I|2jzadsY");
            }
        };
        Session session = Session.getInstance(props, auth);
        session.setDebug(false);

        return session;
    }

    @Override
    public void setIncomingMessageHandler(AuroraIncomingMessageHandler messageHandler) {

        this.messageHandler = messageHandler;
    }

    @Override
    public void sendKeyMessage(AuroraOutKeyMessage key) throws AuroraException {

        if (!key.isArmored())
            throw new AuroraException("Please provide an armored key");

        Message message;
        try {

            // Create message
            message = new MimeMessage(getSession());
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
    public void sendMessage(AuroraOutMessage msg) throws AuroraException {

        if (!msg.isArmored())
            throw new AuroraException("Please provide an armored message");

        Message message;
        try {

            // Create message
            message = new MimeMessage(getSession());
            message.setRecipients(Message.RecipientType.TO,
                    InternetAddress.parse(msg.getRecipient().getIdentifier()));
            message.setSubject("Aurora message");
            message.setHeader(HEADER, HEADER_MESSAGE);

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

        try (Store store = getSession().getStore()) {

            // Connect to the server
            store.connect();

            try (Folder inbox = store.getFolder("INBOX")) {

                inbox.open(Folder.READ_ONLY);
                Message[] messages = inbox.getMessages();
                for (Message message : messages) {

                    String[] header = message.getHeader(HEADER);
                    if (header != null) {

                        String content = getMessageContent(message);

                        int start = content.indexOf(AuroraInMessage.ARMOR_BEGIN);
                        int end = content.indexOf(AuroraInMessage.ARMOR_END);
                        if (start == -1 || end == -1)
                            throw new AuroraException("Unable to parse message content");

                        content = content.substring(start, end + 38);

                        if (header[0].equals(HEADER_KEY))
                            messageHandler.keyMessageReceived(new AuroraInKeyMessage(content.getBytes()));
                        else if (header[0].equals(HEADER_MESSAGE))
                            messageHandler.messageReceived(new AuroraInMessage(content.getBytes()));
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
