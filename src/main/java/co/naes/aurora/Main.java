package co.naes.aurora;

import co.naes.aurora.msg.key.InKeyMessage;
import co.naes.aurora.msg.InMessage;
import co.naes.aurora.msg.in.PartInMessage;
import co.naes.aurora.msg.in.StringInMessage;
import co.naes.aurora.parts.Part;
import co.naes.aurora.transport.IncomingMessageHandler;
import co.naes.aurora.transport.AuroraTransport;
import co.naes.aurora.transport.MailTransport;
import co.naes.aurora.ui.RequestFocusListener;
import co.naes.aurora.ui.Settings;

import javax.swing.*;
import java.io.File;
import java.util.Arrays;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

public class Main {

    public static String CONF_FOLDER = String.format("%s%c.aurora", System.getProperty("user.home"), File.separatorChar);

    private LocalDB db;

    Main() throws Exception {

        LogManager.getLogManager().readConfiguration(this.getClass().getResourceAsStream("/logging.properties"));

        // ask for db password
        JPanel panel = new JPanel();
        JLabel label = new JLabel("Please enter the password:");
        JPasswordField pass = new JPasswordField(15);
        panel.add(label);
        panel.add(pass);
        pass.addAncestorListener(new RequestFocusListener());
        String[] options = new String[]{"OK", "Cancel"};
        int option = JOptionPane.showOptionDialog(null, panel, "Unlock DB",
                JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE,
                null, options, options[0]);

        if (option == 0) {

            try {

                db = new LocalDB(new String(pass.getPassword()));

            } catch (AuroraException ex) {

                JOptionPane.showMessageDialog(null, "Unable to unlock DB: wrong password?",
                        "Error", JOptionPane.ERROR_MESSAGE);
                System.exit(-1);
            }

        } else
            System.exit(-1);

        AuroraSession session = new AuroraSession(db);

        AuroraTransport transport = new MailTransport(db);

        var mainFrame = new co.naes.aurora.ui.Main(db);

        Messenger messenger = new Messenger(db, transport, session, mainFrame);

//        new Settings(db);

//        messenger.sendKeys("luca.zanconato@naes.co");
//        Thread.sleep(30000);
//        messenger.receive();

//        PublicKeys self = db.getPublicKeys("service@naes.co");
//        messenger.addFileToSend(self, "/Users/gherynos/Downloads/BonificoOrdinario.pdf.pdf");
//        messenger.send();
//        messenger.receive();
    }

    public static void main(String[] args) throws Exception {

        new Main();
    }
}
