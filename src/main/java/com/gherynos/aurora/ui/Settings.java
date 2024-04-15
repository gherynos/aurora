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

package com.gherynos.aurora.ui;

import com.gherynos.aurora.AuroraException;
import com.gherynos.aurora.db.DBUtils;
import com.gherynos.aurora.transport.GmailOAuthUtils;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Properties;

@SuppressWarnings("PMD.CouplingBetweenObjects")
public final class Settings extends JFrame {

    private static final long serialVersionUID = 2349845739845727L;

    private static final Logger LOGGER = LogManager.getLogger();

    private static final String IMAP_HOST = "mail.imap.host";
    private static final String IMAP_PORT = "mail.imap.port";
    private static final String IMAP_SSL = "mail.imap.ssl.enable";
    private static final String IMAP_TLS = "mail.imap.starttls.enable";
    private static final String IMAP_AUTH_MECH = "mail.imap.auth.mechanisms";
    private static final String SMTP_HOST = "mail.smtp.host";
    private static final String SMTP_PORT = "mail.smtp.port";
    private static final String SMTP_SSL = "mail.smtp.ssl.enable";
    private static final String SMTP_TLS = "mail.smtp.starttls.enable";
    private static final String SMTP_AUTH = "mail.smtp.auth";
    private static final String SMTP_FROM = "mail.smtp.from";
    private static final String SMTP_AUTH_MECH = "mail.smtp.auth.mechanisms";
    private static final String MAIL_TRANSPORT_PROTOCOL = "mail.transport.protocol";
    private static final String MAIL_STORE_PROTOCOL = "mail.store.protocol";

    private static final String TRUE = "true";
    private static final String FALSE = "false";

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
    private JButton browseButton;
    private JTextField incomingTextField;
    private JTabbedPane tabbedPane;
    private JTextField clientIdTextField;
    private JPasswordField clientSecretPasswordField;
    private JButton authoriseButton;
    private JButton stepsButton;

    private final DBUtils db;
    private final Properties main;
    private final Properties mail;

    private GmailOAuthUtils gmailOAuthUtils;
    private GmailOAuthUtils.GmailOAuthUtilsUI gmailOAuthUtilsUI;
    private boolean authorised;

    {
// GUI initializer generated by IntelliJ IDEA GUI Designer
// >>> IMPORTANT!! <<<
// DO NOT EDIT OR ADD ANY CODE HERE!
        $$$setupUI$$$();
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        mainPanel = new JPanel();
        mainPanel.setLayout(new GridLayoutManager(5, 2, new Insets(8, 8, 8, 8), -1, -1));
        mainPanel.setMinimumSize(new Dimension(510, 681));
        mainPanel.setPreferredSize(new Dimension(510, 681));
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new GridLayoutManager(3, 3, new Insets(10, 10, 10, 10), -1, -1));
        mainPanel.add(panel1, new GridConstraints(0, 0, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        panel1.setBorder(BorderFactory.createTitledBorder(null, "General", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        final JLabel label1 = new JLabel();
        label1.setText("Full Name:");
        panel1.add(label1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        nameTextField = new JTextField();
        panel1.add(nameTextField, new GridConstraints(0, 1, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final JLabel label2 = new JLabel();
        label2.setText("Email:");
        panel1.add(label2, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        emailTextField = new JTextField();
        panel1.add(emailTextField, new GridConstraints(1, 1, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final JLabel label3 = new JLabel();
        label3.setText("Incoming dir:");
        panel1.add(label3, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        browseButton = new JButton();
        browseButton.setText("Browse");
        panel1.add(browseButton, new GridConstraints(2, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        incomingTextField = new JTextField();
        incomingTextField.setEditable(false);
        panel1.add(incomingTextField, new GridConstraints(2, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final JPanel panel2 = new JPanel();
        panel2.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
        mainPanel.add(panel2, new GridConstraints(4, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        okButton = new JButton();
        okButton.setText("OK");
        panel2.add(okButton);
        cancelButton = new JButton();
        cancelButton.setText("Cancel");
        panel2.add(cancelButton);
        final Spacer spacer1 = new Spacer();
        mainPanel.add(spacer1, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        tabbedPane = new JTabbedPane();
        mainPanel.add(tabbedPane, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, new Dimension(200, 200), null, 0, false));
        final JPanel panel3 = new JPanel();
        panel3.setLayout(new GridLayoutManager(3, 1, new Insets(0, 0, 0, 0), -1, -1));
        tabbedPane.addTab("Generic", panel3);
        final JPanel panel4 = new JPanel();
        panel4.setLayout(new GridLayoutManager(5, 2, new Insets(10, 10, 10, 10), -1, -1));
        panel3.add(panel4, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        panel4.setBorder(BorderFactory.createTitledBorder(null, "IMAP", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        final JLabel label4 = new JLabel();
        label4.setText("Host:");
        panel4.add(label4, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        imapHostTextField = new JTextField();
        panel4.add(imapHostTextField, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final JLabel label5 = new JLabel();
        label5.setText("Port:");
        panel4.add(label5, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        imapPortTextField = new JTextField();
        panel4.add(imapPortTextField, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final JLabel label6 = new JLabel();
        label6.setText("Mode:");
        panel4.add(label6, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel5 = new JPanel();
        panel5.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 5));
        panel4.add(panel5, new GridConstraints(2, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        imapSslRadioButton = new JRadioButton();
        imapSslRadioButton.setSelected(true);
        imapSslRadioButton.setText("SSL");
        panel5.add(imapSslRadioButton);
        imapTlsRadioButton = new JRadioButton();
        imapTlsRadioButton.setText("STARTTLS");
        panel5.add(imapTlsRadioButton);
        final JLabel label7 = new JLabel();
        label7.setText("Username:");
        panel4.add(label7, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        imapUsernameTextField = new JTextField();
        panel4.add(imapUsernameTextField, new GridConstraints(3, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final JLabel label8 = new JLabel();
        label8.setText("Password:");
        panel4.add(label8, new GridConstraints(4, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        imapPasswordField = new JPasswordField();
        panel4.add(imapPasswordField, new GridConstraints(4, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final Spacer spacer2 = new Spacer();
        panel3.add(spacer2, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        final JPanel panel6 = new JPanel();
        panel6.setLayout(new GridLayoutManager(6, 2, new Insets(10, 10, 10, 10), -1, -1));
        panel3.add(panel6, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        panel6.setBorder(BorderFactory.createTitledBorder(null, "SMTP", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        final JLabel label9 = new JLabel();
        label9.setText("Host:");
        panel6.add(label9, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        smtpHostTextField = new JTextField();
        panel6.add(smtpHostTextField, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final JLabel label10 = new JLabel();
        label10.setText("Port:");
        panel6.add(label10, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        smtpPortTextField = new JTextField();
        panel6.add(smtpPortTextField, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final JLabel label11 = new JLabel();
        label11.setText("Mode:");
        panel6.add(label11, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel7 = new JPanel();
        panel7.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 5));
        panel6.add(panel7, new GridConstraints(2, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        smtpSslRadioButton = new JRadioButton();
        smtpSslRadioButton.setSelected(true);
        smtpSslRadioButton.setText("SSL");
        panel7.add(smtpSslRadioButton);
        smtpTlsRadioButton = new JRadioButton();
        smtpTlsRadioButton.setText("STARTTLS");
        panel7.add(smtpTlsRadioButton);
        final JLabel label12 = new JLabel();
        label12.setText("Auth:");
        panel6.add(label12, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        smtpAuthCheckBox = new JCheckBox();
        smtpAuthCheckBox.setText("Enabled");
        panel6.add(smtpAuthCheckBox, new GridConstraints(3, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label13 = new JLabel();
        label13.setText("Username:");
        panel6.add(label13, new GridConstraints(4, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        smtpUsernameTextField = new JTextField();
        panel6.add(smtpUsernameTextField, new GridConstraints(4, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final JLabel label14 = new JLabel();
        label14.setText("Password:");
        panel6.add(label14, new GridConstraints(5, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        smtpPasswordField = new JPasswordField();
        panel6.add(smtpPasswordField, new GridConstraints(5, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final JPanel panel8 = new JPanel();
        panel8.setLayout(new GridLayoutManager(5, 2, new Insets(10, 10, 10, 10), -1, -1));
        tabbedPane.addTab("GMail", panel8);
        final JLabel label15 = new JLabel();
        label15.setText("Client ID:");
        panel8.add(label15, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer3 = new Spacer();
        panel8.add(spacer3, new GridConstraints(4, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        clientIdTextField = new JTextField();
        panel8.add(clientIdTextField, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final JLabel label16 = new JLabel();
        label16.setText("Client Secret:");
        panel8.add(label16, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel9 = new JPanel();
        panel9.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
        panel8.add(panel9, new GridConstraints(3, 0, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        authoriseButton = new JButton();
        authoriseButton.setText("Authorise");
        panel9.add(authoriseButton);
        final JPanel panel10 = new JPanel();
        panel10.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
        panel8.add(panel10, new GridConstraints(0, 0, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JLabel label17 = new JLabel();
        label17.setText("The steps to obtain the values below can be found here:");
        panel10.add(label17);
        stepsButton = new JButton();
        stepsButton.setText("Steps");
        panel10.add(stepsButton);
        clientSecretPasswordField = new JPasswordField();
        panel8.add(clientSecretPasswordField, new GridConstraints(2, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        label1.setLabelFor(nameTextField);
        label2.setLabelFor(emailTextField);
        label4.setLabelFor(imapHostTextField);
        label5.setLabelFor(imapPortTextField);
        label7.setLabelFor(imapUsernameTextField);
        label8.setLabelFor(imapPasswordField);
        label9.setLabelFor(smtpHostTextField);
        label10.setLabelFor(smtpPortTextField);
        label13.setLabelFor(smtpUsernameTextField);
        label14.setLabelFor(smtpPasswordField);
        ButtonGroup buttonGroup;
        buttonGroup = new ButtonGroup();
        buttonGroup.add(imapSslRadioButton);
        buttonGroup.add(imapTlsRadioButton);
        buttonGroup = new ButtonGroup();
        buttonGroup.add(smtpSslRadioButton);
        buttonGroup.add(smtpTlsRadioButton);
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return mainPanel;
    }

    public interface SettingsStatusHandler {

        void settingsClosed(boolean saved);
    }

    public Settings(DBUtils db, Properties projectProperties, Component relativeTo, SettingsStatusHandler statusHandler) {

        super("Settings");

        this.db = db;
        main = db.getProperties();
        mail = db.getMailProperties();

        populate();

        smtpUsernameTextField.setEnabled(smtpAuthCheckBox.isSelected());
        smtpPasswordField.setEnabled(smtpAuthCheckBox.isSelected());
        smtpAuthCheckBox.addItemListener(e -> {

            smtpUsernameTextField.setEnabled(smtpAuthCheckBox.isSelected());
            smtpPasswordField.setEnabled(smtpAuthCheckBox.isSelected());
        });

        setContentPane(mainPanel);
        setMinimumSize(new Dimension(mainPanel.getMinimumSize().width, mainPanel.getMinimumSize().height + 22));
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        pack();
        setLocationRelativeTo(relativeTo);
        addWindowListener(new WindowAdapter() {

            @Override
            public void windowClosing(WindowEvent e) {

                statusHandler.settingsClosed(false);
            }
        });

        okButton.addActionListener(e -> {

            String res = checkMainFields();
            if (res == null) {

                // General
                main.setProperty(DBUtils.ACCOUNT_NAME, nameTextField.getText());
                main.setProperty(DBUtils.SESSION_EMAIL_ADDRESS, emailTextField.getText());
                main.setProperty(DBUtils.INCOMING_DIRECTORY, incomingTextField.getText());

            } else {

                JOptionPane.showMessageDialog(this, res, "Account error", JOptionPane.ERROR_MESSAGE);
            }

            if (tabbedPane.getSelectedIndex() == 0) {

                saveGeneric(statusHandler);

            } else {

                saveGMail(statusHandler);
            }
        });

        cancelButton.addActionListener(e -> {

            dispose();
            statusHandler.settingsClosed(false);
        });

        browseButton.addActionListener(e -> {

            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setCurrentDirectory(new File(System.getProperty("user.home")));
            fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            int result = fileChooser.showOpenDialog(this);
            if (result == JFileChooser.APPROVE_OPTION) {

                incomingTextField.setText(fileChooser.getSelectedFile().getAbsolutePath());
            }
        });

        addGMailActionListeners(URI.create(projectProperties.getProperty("stepsURL")));

        setVisible(true);
    }

    @SuppressWarnings("PMD.CognitiveComplexity")
    private void addGMailActionListeners(URI stepsURL) {

        stepsButton.addActionListener(e -> {

            try {

                Desktop desktop = Desktop.getDesktop();
                desktop.browse(stepsURL);

            } catch (IOException ex) {

                if (LOGGER.isErrorEnabled()) {

                    LOGGER.error(ex.getMessage(), ex);
                }

                JOptionPane.showMessageDialog(this, "Unable to open URL in browser:\n" + stepsURL,
                        "Browser error", JOptionPane.ERROR_MESSAGE);
            }
        });

        authoriseButton.addActionListener(e -> {

            String res = checkGMailFields();
            if (res == null) {

                if (gmailOAuthUtils == null) {

                    gmailOAuthUtils = new GmailOAuthUtils(db);
                    Settings me = this;
                    gmailOAuthUtilsUI = new GmailOAuthUtils.GmailOAuthUtilsUI() {

                        @Override
                        public void openInBrowser(URI uri) {

                            try {

                                Desktop desktop = Desktop.getDesktop();
                                desktop.browse(uri);

                            } catch (IOException ex) {

                                if (LOGGER.isErrorEnabled()) {

                                    LOGGER.error(ex.getMessage(), ex);
                                }

                                JOptionPane.showMessageDialog(me, "Unable to open URL in browser",
                                        "Browser error", JOptionPane.ERROR_MESSAGE);
                            }
                        }

                        @Override
                        public void authCompleted() {

                            authorised = true;
                            authoriseButton.setEnabled(false);
                        }

                        @Override
                        public void authError(Exception ex) {

                            if (LOGGER.isErrorEnabled()) {

                                LOGGER.error(ex.getMessage(), ex);
                            }

                            JOptionPane.showMessageDialog(me, "Unable to authorise access to GMail",
                                    "GMail error", JOptionPane.ERROR_MESSAGE);
                        }
                    };
                }

                try {

                    gmailOAuthUtils.authorise(gmailOAuthUtilsUI, clientIdTextField.getText(), new String(clientSecretPasswordField.getPassword()));

                } catch (IOException ex) {

                    if (LOGGER.isErrorEnabled()) {

                        LOGGER.error(ex.getMessage(), ex);
                    }

                    JOptionPane.showMessageDialog(this, "Unable to authorise access to GMail",
                            "GMail error", JOptionPane.ERROR_MESSAGE);
                }

            } else {

                JOptionPane.showMessageDialog(this, res, "GMail error", JOptionPane.ERROR_MESSAGE);
            }
        });

        // no need to force re-authorisation if the token is present
        if (main.getProperty(DBUtils.OAUTH_GMAIL_ACCESS_TOKEN) != null) {

            authorised = true;
        }
    }

    private void populate() {

        // General
        nameTextField.setText(main.getProperty(DBUtils.ACCOUNT_NAME, ""));
        emailTextField.setText(main.getProperty(DBUtils.SESSION_EMAIL_ADDRESS, ""));
        incomingTextField.setText(main.getProperty(DBUtils.INCOMING_DIRECTORY,
                String.format("%s%cDownloads", System.getProperty("user.home"), File.separatorChar)));

        switch (main.getProperty(DBUtils.MAIL_MODE, DBUtils.MAIL_MODE_GENERIC)) {

            case DBUtils.MAIL_MODE_GENERIC: {

                tabbedPane.setSelectedIndex(0);

                // IMAP
                imapHostTextField.setText(mail.getProperty(IMAP_HOST, ""));
                imapPortTextField.setText(mail.getProperty(IMAP_PORT, ""));
                imapSslRadioButton.setSelected(Boolean.parseBoolean(mail.getProperty(IMAP_SSL, TRUE)));
                imapTlsRadioButton.setSelected(Boolean.parseBoolean(mail.getProperty(IMAP_TLS, FALSE)));
                imapUsernameTextField.setText(main.getProperty(DBUtils.MAIL_INCOMING_USERNAME, ""));
                imapPasswordField.setText(main.getProperty(DBUtils.MAIL_INCOMING_PASSWORD, ""));

                // SMTP
                smtpHostTextField.setText(mail.getProperty(SMTP_HOST, ""));
                smtpPortTextField.setText(mail.getProperty(SMTP_PORT, ""));
                smtpSslRadioButton.setSelected(Boolean.parseBoolean(mail.getProperty(SMTP_SSL, TRUE)));
                smtpTlsRadioButton.setSelected(Boolean.parseBoolean(mail.getProperty(SMTP_TLS, FALSE)));
                smtpAuthCheckBox.setSelected(Boolean.parseBoolean(mail.getProperty(SMTP_AUTH, FALSE)));
                smtpUsernameTextField.setText(main.getProperty(DBUtils.MAIL_OUTGOING_USERNAME, ""));
                smtpPasswordField.setText(main.getProperty(DBUtils.MAIL_OUTGOING_PASSWORD, ""));

            }
            break;

            case DBUtils.MAIL_MODE_GMAIL: {

                tabbedPane.setSelectedIndex(1);

                clientIdTextField.setText(main.getProperty(DBUtils.OAUTH_GMAIL_CLIENT_ID, ""));
                clientSecretPasswordField.setText(main.getProperty(DBUtils.OAUTH_GMAIL_CLIENT_SECRET, ""));

            }
            break;

            default:
                LOGGER.warn("Unknown mail mode");
        }
    }

    @SuppressWarnings({"PMD.CyclomaticComplexity", "PMD.NPathComplexity"})
    private String checkMainFields() {

        // Account
        if (nameTextField.getText().isEmpty()) {

            return "Please insert your full name";
        }

        if (!emailTextField.getText().contains("@")) {

            return "Please insert a vaild email address";
        }

        return null;
    }

    @SuppressWarnings({"PMD.CyclomaticComplexity", "PMD.NPathComplexity"})
    private String checkGenericFields() {

        // IMAP
        if (!imapHostTextField.getText().contains(".")) {

            return "Please insert a valid IMAP host";
        }

        try {

            Integer.parseInt(imapPortTextField.getText());

        } catch (NumberFormatException ex) {

            return "Plase insert a valid IMAP port value";
        }

        if (imapUsernameTextField.getText().isEmpty()) {

            return "Please insert the IMAP username";
        }

        if (imapPasswordField.getPassword().length == 0) {

            return "Please insert the IMAP password";
        }

        // SMTP
        if (!smtpHostTextField.getText().contains(".")) {

            return "Please insert a vaild SMTP host";
        }

        try {

            Integer.parseInt(smtpPortTextField.getText());

        } catch (NumberFormatException ex) {

            return "Plase insert a valid SMTP port value";
        }

        if (smtpUsernameTextField.isEnabled() && smtpUsernameTextField.getText().isEmpty()) {

            return "Please insert the SMTP username";
        }

        if (smtpPasswordField.isEnabled() && smtpPasswordField.getPassword().length == 0) {

            return "Please insert the SMTP password";
        }

        return null;
    }

    @SuppressWarnings({"PMD.CyclomaticComplexity", "PMD.NPathComplexity"})
    private String checkGMailFields() {

        if (clientIdTextField.getText().isEmpty()) {

            return "Please insert the Client ID";
        }

        if (clientSecretPasswordField.getPassword().length == 0) {

            return "Please insert the Client Secret";
        }

        return null;
    }

    private void saveGeneric(SettingsStatusHandler statusHandler) {

        String res = checkGenericFields();
        if (res == null) {

            try {

                main.setProperty(DBUtils.MAIL_MODE, DBUtils.MAIL_MODE_GENERIC);

                // IMAP
                mail.setProperty(MAIL_STORE_PROTOCOL, "imap");
                mail.remove(IMAP_AUTH_MECH);
                mail.setProperty(IMAP_HOST, imapHostTextField.getText());
                mail.setProperty(IMAP_PORT, imapPortTextField.getText());
                mail.setProperty(IMAP_SSL, Boolean.toString(imapSslRadioButton.isSelected()));
                mail.setProperty(IMAP_TLS, Boolean.toString(imapTlsRadioButton.isSelected()));
                main.setProperty(DBUtils.MAIL_INCOMING_USERNAME, imapUsernameTextField.getText());
                main.setProperty(DBUtils.MAIL_INCOMING_PASSWORD, new String(imapPasswordField.getPassword()));

                // SMTP
                mail.setProperty(MAIL_TRANSPORT_PROTOCOL, "smtp");
                mail.remove(SMTP_AUTH_MECH);
                mail.setProperty(SMTP_HOST, smtpHostTextField.getText());
                mail.setProperty(SMTP_PORT, smtpPortTextField.getText());
                mail.setProperty(SMTP_SSL, Boolean.toString(smtpSslRadioButton.isSelected()));
                mail.setProperty(SMTP_TLS, Boolean.toString(smtpTlsRadioButton.isSelected()));
                mail.setProperty(SMTP_AUTH, Boolean.toString(smtpAuthCheckBox.isSelected()));
                if (smtpAuthCheckBox.isSelected()) {

                    main.setProperty(DBUtils.MAIL_OUTGOING_USERNAME, smtpUsernameTextField.getText());
                    main.setProperty(DBUtils.MAIL_OUTGOING_PASSWORD, new String(smtpPasswordField.getPassword()));
                }
                mail.setProperty(SMTP_FROM, emailTextField.getText());

                db.saveProperties();

                dispose();
                statusHandler.settingsClosed(true);

            } catch (AuroraException ex) {

                if (LOGGER.isErrorEnabled()) {

                    LOGGER.error(ex.getMessage(), ex);
                }

                JOptionPane.showMessageDialog(this, "Unable to save settings",
                        "Save error", JOptionPane.ERROR_MESSAGE);
            }

        } else {

            JOptionPane.showMessageDialog(this, res, "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void saveGMail(SettingsStatusHandler statusHandler) {

        if (authorised) {

            try {

                main.setProperty(DBUtils.MAIL_MODE, DBUtils.MAIL_MODE_GMAIL);

                // IMAP
                mail.setProperty(MAIL_STORE_PROTOCOL, "imap");
                mail.setProperty(IMAP_AUTH_MECH, "XOAUTH2");
                mail.setProperty(IMAP_HOST, "imap.gmail.com");
                mail.setProperty(IMAP_PORT, "993");
                mail.setProperty(IMAP_SSL, TRUE);
                mail.setProperty(IMAP_TLS, FALSE);
                main.setProperty(DBUtils.MAIL_INCOMING_USERNAME, emailTextField.getText());

                // SMTP
                mail.setProperty(MAIL_TRANSPORT_PROTOCOL, "smtp");
                mail.setProperty(SMTP_AUTH_MECH, "XOAUTH2");
                mail.setProperty(SMTP_HOST, "smtp.gmail.com");
                mail.setProperty(SMTP_PORT, "587");
                mail.setProperty(SMTP_SSL, FALSE);
                mail.setProperty(SMTP_TLS, TRUE);
                mail.setProperty(SMTP_AUTH, TRUE);
                main.setProperty(DBUtils.MAIL_OUTGOING_USERNAME, emailTextField.getText());

                db.saveProperties();

                dispose();
                statusHandler.settingsClosed(true);

            } catch (AuroraException ex) {

                if (LOGGER.isErrorEnabled()) {

                    LOGGER.error(ex.getMessage(), ex);
                }

                JOptionPane.showMessageDialog(this, "Unable to save settings",
                        "Error", JOptionPane.ERROR_MESSAGE);
            }

        } else {

            JOptionPane.showMessageDialog(this, "Please authorise first", "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
}