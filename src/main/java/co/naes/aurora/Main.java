package co.naes.aurora;

import co.naes.aurora.msg.key.InKeyMessage;
import co.naes.aurora.msg.InMessage;
import co.naes.aurora.msg.in.PartInMessage;
import co.naes.aurora.msg.in.StringInMessage;
import co.naes.aurora.parts.Part;
import co.naes.aurora.transport.IncomingMessageHandler;
import co.naes.aurora.transport.AuroraTransport;
import co.naes.aurora.transport.MailTransport;

import java.io.File;
import java.util.Arrays;

public class Main {

    public static String CONF_FOLDER = String.format("%s%c.aurora", System.getProperty("user.home"), File.separatorChar);

    Main() throws Exception {

        LocalDB db = new LocalDB("theDbPassword");
        AuroraSession session = new AuroraSession(db);

        AuroraTransport transport = new MailTransport(db);
        transport.setIncomingMessageHandler(new IncomingMessageHandler() {

            @Override
            public void messageReceived(InMessage<?> message) {

                try {

                    message.decrypt(session);

                    if (message instanceof StringInMessage) {

                        System.out.println(((StringInMessage) message).getData());
                    }

                    if (message instanceof PartInMessage) {

                        Part p = ((PartInMessage) message).getData();
                    }

                    System.out.println(Arrays.toString(session.getPublicKey()));
                    System.out.println(Arrays.toString(message.getSender().getPublicKey()));

                } catch (Exception ex) {

                    ex.printStackTrace();
                }
            }

            @Override
            public void keyMessageReceived(InKeyMessage keyMessage) {

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

//        OutKeyMessage keyMessage = new OutKeyMessage(session, "service@naes.co", true);
//        transport.sendKeyMessage(keyMessage);
//
//        PublicKeys self = db.getPublicKeys("service@naes.co");
//
//        StringOutMessage am = new StringOutMessage(session, self, "A text message", true);
//        transport.sendMessage(am);

        transport.checkForMessages();
    }

    public static void main(String[] args) throws Exception {

        new Main();
    }
}
