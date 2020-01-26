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

        Messenger messenger = new Messenger(db, transport, session);

        PublicKeys self = db.getPublicKeys("service@naes.co");
//        messenger.addFileToSend(self, "/Users/gherynos/Downloads/BonificoOrdinario.pdf.pdf");
//        messenger.send();
//        messenger.receive();
    }

    public static void main(String[] args) throws Exception {

        new Main();
    }
}
