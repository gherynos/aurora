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

    private LocalDB db;

    private String confFolder;

    Main(String cf) {

        try {

            LogManager.getLogManager().readConfiguration(this.getClass().getResourceAsStream("/logging.properties"));

        } catch (IOException ex) {

            ex.printStackTrace();
        }

        if (cf != null)
            confFolder = cf;

        else
            confFolder = String.format("%s%c.aurora", System.getProperty("user.home"), File.separatorChar);

        // ask for db password
        boolean dbExists = LocalDB.exists(confFolder);
        JPanel panel = new JPanel();
        JLabel label = new JLabel(dbExists ? "Password to unlock the DB:" : "New password for the DB:");
        JPasswordField pass = new JPasswordField(15);
        panel.add(label);
        panel.add(pass);
        pass.addAncestorListener(new RequestFocusListener());
        String[] options = new String[]{"OK", "Cancel"};
        int option = JOptionPane.showOptionDialog(null, panel, dbExists ? "Unlock DB" : "New DB",
                JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE,
                null, options, options[0]);

        if (option == 0) {

            try {

                db = new LocalDB(confFolder, new String(pass.getPassword()));

            } catch (AuroraException ex) {

                ex.printStackTrace();

                JOptionPane.showMessageDialog(null, "Unable to unlock DB: wrong password?",
                        "Error", JOptionPane.ERROR_MESSAGE);
                System.exit(-1);
            }

        } else
            System.exit(-1);

        if (!db.getProperties().containsKey(LocalDB.MAIL_INCOMING_PASSWORD)) {

            // connection settings need to be entered before creating the other components
            new Settings(null, db, saved -> {

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
            new Messenger(db, transport, session, confFolder, mainFrame);

        } catch (AuroraException ex) {

            JOptionPane.showMessageDialog(null, ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            System.exit(-1);
        }
    }

    public static void main(String[] args) {

        new Main(args.length > 0 ? args[0] : null);
    }
}
