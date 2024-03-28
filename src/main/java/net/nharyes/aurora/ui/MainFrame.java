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

package net.nharyes.aurora.ui;

import net.nharyes.aurora.AuroraException;
import net.nharyes.aurora.Identifier;
import net.nharyes.aurora.Messenger;
import net.nharyes.aurora.PublicKeys;
import net.nharyes.aurora.db.PublicKeysUtils;
import net.nharyes.aurora.db.StatusUtils;

import net.nharyes.aurora.ui.icons.FileIcon;
import net.nharyes.aurora.ui.icons.KeyIcon;
import net.nharyes.aurora.ui.icons.RefreshIcon;
import net.nharyes.aurora.ui.icons.SettingsIcon;
import net.nharyes.aurora.ui.vo.IncomingFileVO;
import net.nharyes.aurora.ui.vo.OutgoingFileVO;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Insets;
import java.util.Properties;

public class MainFrame extends JFrame implements Messenger.StatusHandler {  // NOPMD

    protected static final Logger LOGGER = LogManager.getLogger();

    private static final int MAX_STATUS_LENGTH = 100;

    private JPanel mainPanel;
    private JButton sendAndReceiveButton;
    private JTable incomingTable;
    private JTable outgoingTable;
    private JLabel statusLabel;
    private JButton settingsButton;
    private JButton sendKeysButton;
    private JButton addFileButton;
    private JButton receivedButton;
    private JButton sentButton;

    private final DefaultTableModel incomingModel;
    private final DefaultTableModel outgoingModel;

    private Messenger messenger;

    private final StatusModal statusModal;

    private Settings settings;

    private CompletedItems receivedFiles;

    private CompletedItems sentFiles;

    private SendKeys sendKeys;

    public MainFrame(Properties projectProperties) {

        super("Aurora " + projectProperties.getProperty("version"));

        setContentPane(mainPanel);
        setMinimumSize(new Dimension(mainPanel.getMinimumSize().width, mainPanel.getMinimumSize().height + 22));
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        pack();
        setLocationRelativeTo(null);
        loadButtonIcons();

        statusModal = new StatusModal(this, "Aurora");

        // incoming files
        incomingModel = new DefaultTableModel() {

            @Override
            public boolean isCellEditable(int row, int column) {

                return false;
            }

            @Override
            public Class<?> getColumnClass(int columnIndex) {

                return String.class;
            }
        };
        incomingModel.setColumnIdentifiers(new String[]{"Sender", "File ID", "Received parts", "Total parts"});
        incomingTable.setModel(incomingModel);

        // outgoing files
        outgoingModel = new DefaultTableModel() {

            @Override
            public boolean isCellEditable(int row, int column) {

                return false;
            }

            @Override
            public Class<?> getColumnClass(int columnIndex) {

                return String.class;
            }
        };
        outgoingModel.setColumnIdentifiers(new String[]{"File ID", "Recipient", "Confirmed parts", "Parts to send", "Total parts"});
        outgoingTable.setModel(outgoingModel);

        // buttons actions
        addToolBarActionListeners(projectProperties);
        addItemsActionListeners();

        setVisible(true);
    }

    private JFrame self() {

        return this;
    }

    private void loadButtonIcons() {

        sendAndReceiveButton.setIcon(new RefreshIcon());
        sendAndReceiveButton.setVerticalTextPosition(SwingConstants.BOTTOM);
        sendAndReceiveButton.setHorizontalTextPosition(SwingConstants.CENTER);

        settingsButton.setIcon(new SettingsIcon());
        settingsButton.setVerticalTextPosition(SwingConstants.BOTTOM);
        settingsButton.setHorizontalTextPosition(SwingConstants.CENTER);

        sendKeysButton.setIcon(new KeyIcon());
        sendKeysButton.setVerticalTextPosition(SwingConstants.BOTTOM);
        sendKeysButton.setHorizontalTextPosition(SwingConstants.CENTER);

        addFileButton.setIcon(new FileIcon());
        addFileButton.setVerticalTextPosition(SwingConstants.BOTTOM);
        addFileButton.setHorizontalTextPosition(SwingConstants.CENTER);
    }

    private void addToolBarActionListeners(Properties projectProperties) {  // NOPMD

        sendAndReceiveButton.addActionListener(e -> new Thread(() -> {

            sendAndReceiveButton.setEnabled(false);

            statusModal.setMessage("Sending messages...");
            messenger.send();

            statusModal.setMessage("Receiving messages...");
            messenger.receive();

            statusModal.setMessage("Updating database...");
            updateTables();
            clearStatus();

            statusModal.hide();

            sendAndReceiveButton.setEnabled(true);

        }).start());

        settingsButton.addActionListener(e -> {

            if (settings == null) {

                settings = new Settings(messenger.getDBUtils(), projectProperties, mainPanel, (boolean saved) -> settings = null);

            } else {

                settings.requestFocus();
            }
        });

        sendKeysButton.addActionListener(e -> {

            if (sendKeys == null) {
                sendKeys = new SendKeys(mainPanel, new SendKeys.SendKeysStatusHandler() {

                    @Override
                    public void sendKeys(Identifier identifier) {

                        new Thread(() -> {

                            statusModal.setRelativeTo(sendKeys);
                            statusModal.setMessage("Sending key message...");

                            try {

                                messenger.sendKeys(identifier);

                            } catch (AuroraException ex) {

                                if (LOGGER.isErrorEnabled()) {

                                    LOGGER.error(ex.getMessage(), ex);
                                }

                                showError("Unable to send keys to recipient");

                            } finally {

                                statusModal.hide();
                                statusModal.setRelativeTo(self());
                            }

                        }).start();
                    }

                    @Override
                    public void sendKeysClosed() {

                        sendKeys = null;  // NOPMD
                    }
                });

            } else {

                sendKeys.requestFocus();
            }
        });

        addFileButton.addActionListener(e -> {

            try {

                AddFile af = new AddFile(this, PublicKeysUtils.listIdentifiers(messenger.getDBUtils()));
                af.setVisible(true);
                if (!af.isCanceled()) {

                    PublicKeys recipient = PublicKeysUtils.get(messenger.getDBUtils(), af.getSelectedRecipient());
                    if (messenger.addFileToSend(recipient, af.getSelectedFile().getAbsolutePath())) {

                        updateTables();

                    } else {

                        JOptionPane.showMessageDialog(this,
                                "File for the selected recipient already added/sent",
                                "Add file", JOptionPane.WARNING_MESSAGE);
                    }
                }

            } catch (AuroraException ex) {

                if (LOGGER.isErrorEnabled()) {

                    LOGGER.error(ex.getMessage(), ex);
                }

                showError("Unable to load recipients");
            }
        });
    }

    private void addItemsActionListeners() {

        receivedButton.addActionListener(e -> {

            try {

                if (receivedFiles == null) {

                    receivedFiles = new CompletedItems(this, () -> receivedFiles = null);

                } else {

                    receivedFiles.requestFocus();
                }
                receivedFiles.displayReceivedFiles(StatusUtils.getReceivedFiles(messenger.getDBUtils()));

            } catch (AuroraException ex) {

                if (LOGGER.isErrorEnabled()) {

                    LOGGER.error(ex.getMessage(), ex);
                }

                showError("Unable to load received files");
            }
        });

        sentButton.addActionListener(e -> {

            try {

                if (sentFiles == null) {

                    sentFiles = new CompletedItems(this, () -> sentFiles = null);

                } else {

                    sentFiles.requestFocus();
                }
                sentFiles.displaySentFiles(StatusUtils.getSentFiles(messenger.getDBUtils()));

            } catch (AuroraException ex) {

                if (LOGGER.isErrorEnabled()) {

                    LOGGER.error(ex.getMessage(), ex);
                }

                showError("Unable to load sent files");
            }
        });
    }

    private void updateTables() {

        try {

            incomingModel.setRowCount(0);
            for (IncomingFileVO iFile : StatusUtils.getIncomingFiles(messenger.getDBUtils())) {

                incomingModel.addRow(iFile.asRow());
            }
            incomingModel.fireTableDataChanged();

            outgoingModel.setRowCount(0);
            for (OutgoingFileVO oFile : StatusUtils.getOutgoingFiles(messenger.getDBUtils())) {

                outgoingModel.addRow(oFile.asRow());
            }
            outgoingModel.fireTableDataChanged();

        } catch (AuroraException ex) {

            if (LOGGER.isErrorEnabled()) {

                LOGGER.error(ex.getMessage(), ex);
            }

            showError("Unable to load files status");
        }
    }

    private void updateStatus(String status) {

        if (status.length() > MAX_STATUS_LENGTH) {

            statusLabel.setText(status.substring(0, MAX_STATUS_LENGTH) + "...");

        } else {

            statusLabel.setText(status);
        }
    }

    private void clearStatus() {

        updateStatus(" ");
    }

    private void showError(String message) {

        JOptionPane.showMessageDialog(this, message.replace(": ", ":\n"),
                "Error", JOptionPane.ERROR_MESSAGE);
    }

    @Override
    public void self(Messenger messenger) {

        this.messenger = messenger;

        // load current status
        updateTables();
    }

    @Override
    public void sendingPart(int sequenceNumber, String fileId, Identifier identifier) {

        updateStatus(String.format("Sending part #%d of %s to %s", sequenceNumber, fileId, identifier));
    }

    @Override
    public void unableToSendPart(int sequenceNumber, String fileId, Identifier identifier) {

        showError(String.format("Unable to send part #%d of %s to %s", sequenceNumber, fileId, identifier));
    }

    @Override
    public void processingPart(int sequenceNumber, String fileId, Identifier identifier) {

        updateStatus(String.format("Processing part #%d of %s from %s", sequenceNumber, fileId, identifier));
    }

    @Override
    public void discardedPart(int sequenceNumber, String fileId, Identifier identifier) {

        updateStatus(String.format("Discarded part #%d of %s from %s", sequenceNumber, fileId, identifier));
    }

    @Override
    public void processingConfirmation(int sequenceNumber, String fileId, Identifier identifier) {

        updateStatus(String.format("Processing confirmation #%d of %s from %s", sequenceNumber, fileId, identifier));
    }

    @Override
    public void errorsWhileSendingMessages(String message) {

        showError(message);
    }

    @Override
    public void errorsWhileReceivingMessages(String message) {

        showError(message);
    }

    @Override
    public void errorsWhileProcessingReceivedMessage(String message) {

        showError(message);
    }

    @Override
    public void errorsWhileProcessingKeyMessage(String message) {

        showError(message);
    }

    @Override
    public void fileComplete(String fileId, Identifier identifier, String path) {

        JOptionPane.showMessageDialog(this, String.format("File %s successfully received\nfrom %s",
                fileId, identifier), "File received", JOptionPane.INFORMATION_MESSAGE);
    }

    @Override
    public char[] keyMessageReceived(String sender) {

        var kr = new KeysReceived(this, sender);
        kr.setVisible(true);

        return kr.getPassword();
    }

    @Override
    public void keyMessageSent(char[] password) {

        if (sendKeys == null) {

            showError("Keys sent but the dialog is closed");

        } else {

            sendKeys.keysSent(password);
        }
    }

    @Override
    public void keysStored(Identifier identifier) {

        JOptionPane.showMessageDialog(this, String.format("Keys for %s successfully stored", identifier),
                "Keys stored", JOptionPane.INFORMATION_MESSAGE);
    }

    @Override
    public boolean publicKeysReceived(Identifier identifier) {

        int dialogResult = JOptionPane.showConfirmDialog(mainPanel,
                String.format("Public keys received from %s\nShould they be accepted?", identifier), "Keys received",
                JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE);

        return dialogResult == JOptionPane.YES_OPTION;
    }

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
        mainPanel.setLayout(new GridLayoutManager(4, 3, new Insets(8, 8, 8, 8), -1, -1));
        mainPanel.setMinimumSize(new Dimension(677, 586));
        mainPanel.setPreferredSize(new Dimension(677, 586));
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new GridLayoutManager(2, 1, new Insets(10, 10, 10, 10), -1, -1));
        mainPanel.add(panel1, new GridConstraints(1, 0, 1, 3, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        panel1.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Incoming files", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        final JScrollPane scrollPane1 = new JScrollPane();
        panel1.add(scrollPane1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        incomingTable = new JTable();
        incomingTable.setFillsViewportHeight(true);
        scrollPane1.setViewportView(incomingTable);
        final JPanel panel2 = new JPanel();
        panel2.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1));
        panel1.add(panel2, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        receivedButton = new JButton();
        receivedButton.setText("Received files");
        panel2.add(receivedButton, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer1 = new Spacer();
        panel2.add(spacer1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        final JPanel panel3 = new JPanel();
        panel3.setLayout(new GridLayoutManager(2, 1, new Insets(10, 10, 10, 10), -1, -1));
        mainPanel.add(panel3, new GridConstraints(2, 0, 1, 3, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        panel3.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Outgoing files", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        final JScrollPane scrollPane2 = new JScrollPane();
        panel3.add(scrollPane2, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        outgoingTable = new JTable();
        outgoingTable.setFillsViewportHeight(true);
        scrollPane2.setViewportView(outgoingTable);
        final JPanel panel4 = new JPanel();
        panel4.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1));
        panel3.add(panel4, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        sentButton = new JButton();
        sentButton.setText("Sent files");
        panel4.add(sentButton, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer2 = new Spacer();
        panel4.add(spacer2, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        final JToolBar toolBar1 = new JToolBar();
        toolBar1.setFloatable(false);
        mainPanel.add(toolBar1, new GridConstraints(0, 0, 1, 3, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(-1, 20), null, 0, false));
        sendAndReceiveButton = new JButton();
        sendAndReceiveButton.setBorderPainted(false);
        sendAndReceiveButton.setFocusable(false);
        sendAndReceiveButton.setMargin(new Insets(5, 5, 5, 5));
        sendAndReceiveButton.setText("Send and Receive");
        toolBar1.add(sendAndReceiveButton);
        settingsButton = new JButton();
        settingsButton.setBorderPainted(false);
        settingsButton.setFocusable(false);
        settingsButton.setMargin(new Insets(5, 5, 5, 5));
        settingsButton.setText("Settings");
        toolBar1.add(settingsButton);
        sendKeysButton = new JButton();
        sendKeysButton.setBorderPainted(false);
        sendKeysButton.setFocusable(false);
        sendKeysButton.setMargin(new Insets(5, 5, 5, 5));
        sendKeysButton.setText("Send Keys");
        toolBar1.add(sendKeysButton);
        addFileButton = new JButton();
        addFileButton.setBorderPainted(false);
        addFileButton.setFocusable(false);
        addFileButton.setMargin(new Insets(5, 5, 5, 5));
        addFileButton.setText("Add file");
        toolBar1.add(addFileButton);
        final JPanel panel5 = new JPanel();
        panel5.setLayout(new GridLayoutManager(1, 1, new Insets(8, 8, 8, 8), -1, -1));
        mainPanel.add(panel5, new GridConstraints(3, 0, 1, 3, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        panel5.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), null, TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        statusLabel = new JLabel();
        statusLabel.setForeground(new Color(-10000537));
        statusLabel.setText(" ");
        panel5.add(statusLabel, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return mainPanel;
    }

}
