package co.naes.aurora;

import co.naes.aurora.msg.AuroraInKeyMessage;
import co.naes.aurora.msg.AuroraInMessage;
import co.naes.aurora.msg.AuroraOutKeyMessage;
import co.naes.aurora.msg.AuroraOutMessage;
import co.naes.aurora.transport.AuroraIncomingMessageHandler;
import co.naes.aurora.transport.AuroraTransport;
import co.naes.aurora.transport.MailTransport;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class Main {

    Main() throws Exception {

        LocalDB db = new LocalDB("theDbPassword");
        AuroraSession session = new AuroraSession(db);

        AuroraTransport transport = new MailTransport(db);
        transport.setIncomingMessageHandler(new AuroraIncomingMessageHandler() {

            @Override
            public void messageReceived(AuroraInMessage message) {

                try {

                    message.decrypt(session);
                    System.out.println(message.getMessageId());
                    System.out.println(message.getSequenceNumber());
                    System.out.println(message.getTotal());

                    if (message.isText())
                        System.out.println(message.getText());

                    if (message.isBinary())
                        System.out.println(new String(message.getBinaryData(), StandardCharsets.UTF_8));

                    System.out.println(Arrays.toString(session.getPublicKey()));
                    System.out.println(Arrays.toString(message.getSender().getPublicKey()));

                } catch (Exception ex) {

                    ex.printStackTrace();
                }
            }

            @Override
            public void keyMessageReceived(AuroraInKeyMessage keyMessage) {

                try {

                    char[] password = "aRandomPasswordToGenerate".toCharArray(); // TODO: fix
                    PublicKeys keys = keyMessage.getPublicKeys(password);

                    System.out.println(Arrays.toString(session.getPublicKey()));
                    System.out.println(Arrays.toString(keys.getPublicKey()));

                    System.out.println(session.getEmailAddress());
                    System.out.println(keys.getEmailAddress());

                    System.out.println(Arrays.toString(keys.getPublicSignKey()));

                } catch (Exception ex) {

                    ex.printStackTrace();
                }
            }
        });

        PublicKeys r = new PublicKeys(session.getPublicKey(), session.getEmailAddress());

//        AuroraOutMessage am = new AuroraOutMessage(session, r, "MID001", 0, 1, "A text message", true);
//        transport.sendMessage(am);
//
//        AuroraOutMessage am2 = new AuroraOutMessage(session, r, "MID002", 0, 1, "A binary message".getBytes(), true);
//        transport.sendMessage(am2);

//        AuroraOutKeyMessage keyMessage = new AuroraOutKeyMessage(session, "service@naes.co", true);
//        transport.sendKeyMessage(keyMessage);

        transport.checkForMessages();
    }

    public static void main(String[] args) throws Exception {

        new Main();
    }
}
