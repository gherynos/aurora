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

                    if (message.isText()) {

                        System.out.println(message.getText());
                        System.out.println(message.getSize());
                        System.out.println(message.getText().length());
                    }

                    if (message.isBinary()) {

                        System.out.println(new String(message.getBinaryData(), StandardCharsets.UTF_8));
                        System.out.println(message.getSize());
                        System.out.println(message.getBinaryData().length);
                    }

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

                    db.storePublicKeys(keys);

                } catch (Exception ex) {

                    ex.printStackTrace();
                }
            }
        });

//        AuroraOutKeyMessage keyMessage = new AuroraOutKeyMessage(session, "service@naes.co", true);
//        transport.sendKeyMessage(keyMessage);

//        PublicKeys self = db.getPublicKeys("service@naes.co");

//        AuroraOutMessage am = new AuroraOutMessage(session, self, "MID001", "A text message", true);
//        transport.sendMessage(am);

//        byte[] data = "A test binary message".getBytes();
//        AuroraOutMessage am2 = new AuroraOutMessage(session, self, "MID002", 0, 1, data.length, data, true);
//        transport.sendMessage(am2);

        transport.checkForMessages();
    }

    public static void main(String[] args) throws Exception {

        new Main();
    }
}
