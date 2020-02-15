package co.naes.aurora.ui;

import co.naes.aurora.AuroraException;
import co.naes.aurora.LocalDB;

import javax.swing.*;
import java.awt.*;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Settings {

    public interface SettingsStatusHandler {

        void settingsClosed(boolean saved);
    }

    protected final Logger logger = Logger.getLogger(getClass().getName());

    private final String IMAP_HOST = "mail.imap.host";
    private final String IMAP_PORT = "mail.imap.port";
    private final String IMAP_SSL = "mail.imap.ssl.enable";
    private final String IMAP_TLS = "mail.imap.starttls.enable";
    private final String SMTP_HOST = "mail.smtp.host";
    private final String SMTP_PORT = "mail.smtp.port";
    private final String SMTP_SSL = "mail.smtp.ssl.enable";
    private final String SMTP_TLS = "mail.smtp.starttls.enable";
    private final String SMTP_AUTH = "mail.smtp.auth";
    private final String SMTP_FROM = "mail.smtp.from";

    private JTextField imapHostTextField;
    private JTextField imapPortTextField;
    private JRadioButton imapSslRadioButton;
    private JRadioButton imapTlsRadioButton;
    private JTextField imapUsernameTextField;
    private JPasswordField imapPasswordField;
    private JPanel mainPanel;
    private JTextField smtpHostTextField;
    private JTextField smtpPortTextField;
    private JRadioButton smtpSslRadioButton;
    private JRadioButton smtpTlsRadioButton;
    private JCheckBox smtpAuthCheckBox;
    private JTextField smtpUsernameTextField;
    private JPasswordField smtpPasswordField;
    private JTextField nameTextField;
    private JTextField emailTextField;
    private JButton okButton;
    private JButton cancelButton;

    private Properties main;
    private Properties mail;

    private JFrame frame;

    public Settings(LocalDB db, SettingsStatusHandler statusHandler) {

        main = db.getProperties();
        mail = db.getMailProperties();

        populate();

        smtpUsernameTextField.setEnabled(smtpAuthCheckBox.isSelected());
        smtpPasswordField.setEnabled(smtpAuthCheckBox.isSelected());
        smtpAuthCheckBox.addItemListener(e -> {

            smtpUsernameTextField.setEnabled(smtpAuthCheckBox.isSelected());
            smtpPasswordField.setEnabled(smtpAuthCheckBox.isSelected());
        });

        frame = new JFrame("Settings");
        frame.setContentPane(mainPanel);
        frame.setMinimumSize(
                new Dimension(mainPanel.getMinimumSize().width, mainPanel.getMinimumSize().height + 22));
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        okButton.addActionListener(e -> {

            String res = checkFields();
            if (res != null)
                JOptionPane.showMessageDialog(frame, res, "Error", JOptionPane.ERROR_MESSAGE);

            else {

                try {

                    // Account
                    main.setProperty(LocalDB.ACCOUNT_NAME, nameTextField.getText());
                    main.setProperty(LocalDB.SESSION_EMAIL_ADDRESS, emailTextField.getText());

                    // IMAP
                    mail.setProperty(IMAP_HOST, imapHostTextField.getText());
                    mail.setProperty(IMAP_PORT, imapPortTextField.getText());
                    mail.setProperty(IMAP_SSL, "" + imapSslRadioButton.isSelected());
                    mail.setProperty(IMAP_TLS, "" + imapTlsRadioButton.isSelected());
                    main.setProperty(LocalDB.MAIL_INCOMING_USERNAME, imapUsernameTextField.getText());
                    main.setProperty(LocalDB.MAIL_INCOMING_PASSWORD, imapPasswordField.getText());

                    // SMTP
                    mail.setProperty(SMTP_HOST, smtpHostTextField.getText());
                    mail.setProperty(SMTP_PORT, smtpPortTextField.getText());
                    mail.setProperty(SMTP_SSL, "" + smtpSslRadioButton.isSelected());
                    mail.setProperty(SMTP_TLS, "" + smtpTlsRadioButton.isSelected());
                    mail.setProperty(SMTP_AUTH, "" + smtpAuthCheckBox.isSelected());
                    if (smtpAuthCheckBox.isSelected()) {

                        main.setProperty(LocalDB.MAIL_OUTGOING_USERNAME, smtpUsernameTextField.getText());
                        main.setProperty(LocalDB.MAIL_OUTGOING_PASSWORD, smtpPasswordField.getText());
                    }
                    mail.setProperty(SMTP_FROM, emailTextField.getText());

                    db.saveProperties();

                    frame.dispose();
                    statusHandler.settingsClosed(true);

                } catch (AuroraException ex) {

                    logger.log(Level.SEVERE, ex.getMessage(), ex);

                    JOptionPane.showMessageDialog(frame, "Unable to save settings",
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        cancelButton.addActionListener(e -> {

            frame.dispose();
            statusHandler.settingsClosed(false);
        });
    }

    private void populate() {

        // Account
        nameTextField.setText(main.getProperty(LocalDB.ACCOUNT_NAME, ""));
        emailTextField.setText(main.getProperty(LocalDB.SESSION_EMAIL_ADDRESS, ""));

        // IMAP
        imapHostTextField.setText(mail.getProperty(IMAP_HOST, ""));
        imapPortTextField.setText(mail.getProperty(IMAP_PORT, ""));
        imapSslRadioButton.setSelected(Boolean.parseBoolean(mail.getProperty(IMAP_SSL, "true")));
        imapTlsRadioButton.setSelected(Boolean.parseBoolean(mail.getProperty(IMAP_TLS, "false")));
        imapUsernameTextField.setText(main.getProperty(LocalDB.MAIL_INCOMING_USERNAME, ""));
        imapPasswordField.setText(main.getProperty(LocalDB.MAIL_INCOMING_PASSWORD, ""));

        // SMTP
        smtpHostTextField.setText(mail.getProperty(SMTP_HOST, ""));
        smtpPortTextField.setText(mail.getProperty(SMTP_PORT, ""));
        smtpSslRadioButton.setSelected(Boolean.parseBoolean(mail.getProperty(SMTP_SSL, "true")));
        smtpTlsRadioButton.setSelected(Boolean.parseBoolean(mail.getProperty(SMTP_TLS, "false")));
        smtpAuthCheckBox.setSelected(Boolean.parseBoolean(mail.getProperty(SMTP_AUTH, "false")));
        smtpUsernameTextField.setText(main.getProperty(LocalDB.MAIL_OUTGOING_USERNAME, ""));
        smtpPasswordField.setText(main.getProperty(LocalDB.MAIL_OUTGOING_PASSWORD, ""));
    }

    private String checkFields() {

        // Account
        if (nameTextField.getText().isEmpty())
            return "Please insert your full name";

        if (!emailTextField.getText().contains("@"))
            return "Please insert a vaild email address";

        // IMAP
        if (!imapHostTextField.getText().contains("."))
            return "Please insert a vaild IMAP host";

        try {

            Integer.parseInt(imapPortTextField.getText());

        } catch (NumberFormatException ex) {

            return "Plase insert a valid IMAP port value";
        }

        if (imapUsernameTextField.getText().isEmpty())
            return "Please insert the IMAP username";

        if (imapPasswordField.getText().isEmpty())
            return "Please insert the IMAP password";

        // SMTP
        if (!smtpHostTextField.getText().contains("."))
            return "Please insert a vaild SMTP host";

        try {

            Integer.parseInt(smtpPortTextField.getText());

        } catch (NumberFormatException ex) {

            return "Plase insert a valid SMTP port value";
        }

        if (smtpUsernameTextField.isEnabled() && smtpUsernameTextField.getText().isEmpty())
            return "Please insert the SMTP username";

        if (smtpPasswordField.isEnabled() && smtpPasswordField.getText().isEmpty())
            return "Please insert the SMTP password";

        return null;
    }

    public void requestFocus() {

        frame.requestFocus();
    }
}
