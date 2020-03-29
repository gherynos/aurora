package co.naes.aurora;

import co.naes.aurora.db.DBUtils;
import co.naes.aurora.transport.AuroraTransport;
import co.naes.aurora.transport.MailTransport;
import co.naes.aurora.ui.MainFrame;
import co.naes.aurora.ui.RequestFocusListener;
import co.naes.aurora.ui.Settings;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

public class Main {  // NOPMD

    protected final Logger logger = Logger.getLogger(getClass().getName());

    private String confFolder;

    private Main(String cf) {

        try {

            LogManager.getLogManager().readConfiguration(this.getClass().getResourceAsStream("/logging.properties"));

        } catch (IOException ex) {

            ex.printStackTrace();  // NOPMD
        }

        if (cf == null) {

            confFolder = String.format("%s%c.aurora", System.getProperty("user.home"), File.separatorChar);

        } else {

            confFolder = cf;
        }

        // ask for db password
        boolean dbExists = DBUtils.exists(confFolder);
        JPanel panel = new JPanel();
        JLabel label = new JLabel(dbExists ? "Password to unlock the DB:" : "New password for the DB:");
        JPasswordField pass = new JPasswordField(15);
        panel.add(label);
        panel.add(pass);
        pass.addAncestorListener(new RequestFocusListener());
        String[] options = {"OK", "Cancel"};
        int option = JOptionPane.showOptionDialog(null, panel, dbExists ? "Unlock DB" : "New DB",
                JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE,
                null, options, options[0]);

        if (option == 0) {

            try {

                DBUtils.initialise(confFolder, new String(pass.getPassword()));

            } catch (AuroraException ex) {

                logger.log(Level.SEVERE, ex.getMessage(), ex);

                JOptionPane.showMessageDialog(null, "Unable to unlock DB: wrong password?",
                        "Error", JOptionPane.ERROR_MESSAGE);
                System.exit(-1);
            }

        } else {

            System.exit(-1);
        }

        if (DBUtils.getProperties().containsKey(DBUtils.MAIL_INCOMING_PASSWORD)) {

            // normal flow
            showApplication();

        } else {

            // connection settings need to be entered before creating the other components
            new Settings(null, saved -> {

                if (!saved) {

                    System.exit(-1);
                }

                showApplication();
            });
        }
    }

    private void showApplication() {

        try {

            AuroraSession session = new AuroraSession();
            AuroraTransport transport = new MailTransport();
            var mainFrame = new MainFrame();
            new Messenger(transport, session, confFolder, mainFrame);

        } catch (AuroraException ex) {

            JOptionPane.showMessageDialog(null, ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            System.exit(-1);
        }
    }

    public static void main(String[] args) {

        new Main(args.length > 0 ? args[0] : null);  // NOPMD
    }
}
