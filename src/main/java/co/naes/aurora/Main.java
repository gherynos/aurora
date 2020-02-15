package co.naes.aurora;

import co.naes.aurora.transport.AuroraTransport;
import co.naes.aurora.transport.MailTransport;
import co.naes.aurora.ui.RequestFocusListener;
import co.naes.aurora.ui.Settings;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.util.logging.LogManager;

public class Main {

    public static String CONF_FOLDER = String.format("%s%c.aurora", System.getProperty("user.home"), File.separatorChar);

    private LocalDB db;

    Main() {

        try {

            LogManager.getLogManager().readConfiguration(this.getClass().getResourceAsStream("/logging.properties"));

        } catch (IOException ex) {

            ex.printStackTrace();
        }

        // ask for db password
        JPanel panel = new JPanel();
        JLabel label = new JLabel(LocalDB.exists()? "Password to unlock the DB:" : "New password for the DB:");
        JPasswordField pass = new JPasswordField(15);
        panel.add(label);
        panel.add(pass);
        pass.addAncestorListener(new RequestFocusListener());
        String[] options = new String[]{"OK", "Cancel"};
        int option = JOptionPane.showOptionDialog(null, panel, LocalDB.exists()? "Unlock DB" : "New DB",
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

        if (!db.getProperties().containsKey(LocalDB.MAIL_INCOMING_PASSWORD)) {

            // connection settings need to be entered before creating the other components
            new Settings(db, saved -> {

                if (!saved)
                    System.exit(-1);

                showApplication();
            });

        } else {

            // normal flow
            showApplication();
        }
    }

    private void showApplication() {

        try {

            AuroraSession session = new AuroraSession(db);
            AuroraTransport transport = new MailTransport(db);
            var mainFrame = new co.naes.aurora.ui.Main(db);
            new Messenger(db, transport, session, mainFrame);

        } catch (AuroraException ex) {

            JOptionPane.showMessageDialog(null, ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            System.exit(-1);
        }
    }

    public static void main(String[] args) {

        new Main();
    }
}
