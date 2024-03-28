/*
 * Copyright (C) 2020-2024  Luca Zanconato (<github.com/gherynos>)
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

package net.nharyes.aurora;

import net.nharyes.aurora.db.DBUtils;
import net.nharyes.aurora.transport.AuroraTransport;
import net.nharyes.aurora.transport.MailTransport;
import net.nharyes.aurora.ui.MainFrame;
import net.nharyes.aurora.ui.RequestFocusListener;
import net.nharyes.aurora.ui.Settings;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.UIManager;
import java.io.File;
import java.io.IOException;
import java.util.Properties;

public class Main {  // NOPMD

    protected static final Logger LOGGER = LogManager.getLogger();

    private final String confFolder;

    private Main(String cf) {

        Properties projectProperties = new Properties();
        try {

            projectProperties.load(Main.class.getResourceAsStream("/project.properties"));

        } catch (IOException ex) {

            ex.printStackTrace();  // NOPMD
        }

        confFolder = cf != null ? cf : String.format("%s%c.aurora", System.getProperty("user.home"), File.separatorChar);

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

        DBUtils db = null;
        if (option == 0) {

            try {

                db = new DBUtils(confFolder, new String(pass.getPassword()));

            } catch (AuroraException ex) {

                LOGGER.error(ex.getMessage(), ex);  // NOPMD

                JOptionPane.showMessageDialog(null, "Unable load the DB: wrong version or password?",
                        "Error", JOptionPane.ERROR_MESSAGE);
                System.exit(-1);
            }

        } else {

            System.exit(-1);
        }

        if (db.getProperties().containsKey(DBUtils.MAIL_INCOMING_PASSWORD) ||
                db.getProperties().containsKey(DBUtils.OAUTH_GMAIL_ACCESS_TOKEN)) {

            // normal flow
            showApplication(db, projectProperties);

        } else {

            // connection settings need to be entered before creating the other components
            final DBUtils fdb = db;
            new Settings(db, projectProperties, null, saved -> {

                if (!saved) {

                    System.exit(-1);
                }

                showApplication(fdb, projectProperties);
            });
        }
    }

    private void showApplication(DBUtils db, Properties projectProperties) {

        try {

            AuroraSession session = new AuroraSession(db);
            AuroraTransport transport = new MailTransport(db, projectProperties.getProperty("repository"));
            var mainFrame = new MainFrame(projectProperties);
            new Messenger(transport, session, confFolder, mainFrame);

        } catch (AuroraException ex) {

            JOptionPane.showMessageDialog(null, ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            System.exit(-1);
        }
    }

    public static void main(String[] args) {

        try {

            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

        } catch (Exception ex) {  // NOPMD

            /* not an issue if unable to change the look and feel */
        }

        new Main(args.length > 0 ? args[0] : null);  // NOPMD
    }
}
