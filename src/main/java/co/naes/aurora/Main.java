/*
 * Copyright (C) 2020  Luca Zanconato (<luca.zanconato@naes.co>)
 *
 * This file is part of Aurora.
 *
 * Aurora is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Aurora is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Aurora.  If not, see <http://www.gnu.org/licenses/>.
 */

package co.naes.aurora;

import co.naes.aurora.db.DBUtils;
import co.naes.aurora.transport.AuroraTransport;
import co.naes.aurora.transport.MailTransport;
import co.naes.aurora.ui.MainFrame;
import co.naes.aurora.ui.RequestFocusListener;
import co.naes.aurora.ui.Settings;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import java.io.File;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

public class Main {  // NOPMD

    protected final Logger logger = Logger.getLogger(getClass().getName());

    private final String confFolder;

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

        if (DBUtils.getProperties().containsKey(DBUtils.MAIL_INCOMING_PASSWORD) ||
                DBUtils.getProperties().containsKey(DBUtils.OAUTH_GMAIL_ACCESS_TOKEN)) {

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

            Properties p = new Properties();
            p.load(Main.class.getResourceAsStream("/project.properties"));

            AuroraSession session = new AuroraSession();
            AuroraTransport transport = new MailTransport(p.getProperty("repository"));
            var mainFrame = new MainFrame(p.getProperty("version"));
            new Messenger(transport, session, confFolder, mainFrame);

        } catch (AuroraException | IOException ex) {

            JOptionPane.showMessageDialog(null, ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            System.exit(-1);
        }
    }

    public static void main(String[] args) {

        new Main(args.length > 0 ? args[0] : null);  // NOPMD
    }
}
