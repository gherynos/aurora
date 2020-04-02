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

package co.naes.aurora.ui;

import co.naes.aurora.AuroraException;
import co.naes.aurora.Messenger;
import co.naes.aurora.PublicKeys;
import co.naes.aurora.db.PublicKeysUtils;
import co.naes.aurora.db.StatusUtils;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;

import org.openide.util.ImageUtilities;
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
import javax.swing.table.DefaultTableModel;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Insets;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MainFrame extends JFrame implements Messenger.StatusHandler {  // NOPMD

    protected final Logger logger = Logger.getLogger(getClass().getName());

    private static final int MAX_STATUS_LENGTH = 100;

    private JPanel mainPanel;
    private JButton sendAndReceiveButton;
    private JTable incomingTable;
    private JTable outgoingTable;
    private JLabel statusLabel;
    private JButton settingsButton;
    private JButton sendKeysButton;
    private JButton addFileButton;

    private final DefaultTableModel incomingModel;
    private final DefaultTableModel outgoingModel;

    private Messenger messenger;

    private final StatusModal statusModal;

    private Settings settings;

    private SendKeys sendKeys;

    public MainFrame(String version) {

        super("Aurora " + version);

        setIconImage(ImageUtilities.loadImageIcon("icons/message.svg", false).getImage());

        setContentPane(mainPanel);
        setMinimumSize(new Dimension(mainPanel.getMinimumSize().width, mainPanel.getMinimumSize().height + 22));
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        pack();
        setLocationRelativeTo(null);
        loadButtonIcons();
        setVisible(true);

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

        // load current status
        updateTables();

        // buttons actions
        final JFrame me = this;
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

                settings = new Settings(mainPanel, (boolean saved) -> settings = null);

            } else {

                settings.requestFocus();
            }
        });
        sendKeysButton.addActionListener(e -> {

            if (sendKeys == null) {
                sendKeys = new SendKeys(mainPanel, new SendKeys.SendKeysStatusHandler() {

                    @Override
                    public void sendKeys(String email) {

                        new Thread(() -> {

                            statusModal.setRelativeTo(sendKeys);
                            statusModal.setMessage("Sending key message...");

                            try {

                                messenger.sendKeys(email);

                            } catch (AuroraException ex) {

                                showError("Unable to send keys to recipient");

                            } finally {

                                statusModal.hide();
                                statusModal.setRelativeTo(me);
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

                AddFile af = new AddFile(this, PublicKeysUtils.listAddresses());
                af.setVisible(true);
                if (!af.isCanceled()) {

                    PublicKeys recipient = PublicKeysUtils.get(af.getSelectedRecipient());
                    if (messenger.addFileToSend(recipient, af.getSelectedFile().getAbsolutePath())) {

                        updateTables();

                    } else {

                        JOptionPane.showMessageDialog(this,
                                "File for the selected recipient already added/sent",
                                "Add file", JOptionPane.WARNING_MESSAGE);
                    }
                }

            } catch (AuroraException ex) {

                logger.log(Level.SEVERE, ex.getMessage(), ex);

                showError("Unable to load recipients");
            }
        });
    }

    private void loadButtonIcons() {

        sendAndReceiveButton.setIcon(ImageUtilities.loadImageIcon("icons/refresh.svg", false));
        sendAndReceiveButton.setVerticalTextPosition(SwingConstants.BOTTOM);
        sendAndReceiveButton.setHorizontalTextPosition(SwingConstants.CENTER);

        settingsButton.setIcon(ImageUtilities.loadImageIcon("icons/settings.svg", false));
        settingsButton.setVerticalTextPosition(SwingConstants.BOTTOM);
        settingsButton.setHorizontalTextPosition(SwingConstants.CENTER);

        sendKeysButton.setIcon(ImageUtilities.loadImageIcon("icons/lock.svg", false));
        sendKeysButton.setVerticalTextPosition(SwingConstants.BOTTOM);
        sendKeysButton.setHorizontalTextPosition(SwingConstants.CENTER);

        addFileButton.setIcon(ImageUtilities.loadImageIcon("icons/file.svg", false));
        addFileButton.setVerticalTextPosition(SwingConstants.BOTTOM);
        addFileButton.setHorizontalTextPosition(SwingConstants.CENTER);
    }

    private void updateTables() {

        try {

            incomingModel.setRowCount(0);
            for (IncomingFile iFile : StatusUtils.getIncomingFiles()) {

                incomingModel.addRow(iFile.asRow());
            }
            incomingModel.fireTableDataChanged();

            outgoingModel.setRowCount(0);
            for (OutgoingFile oFile : StatusUtils.getOutgoingFiles()) {

                outgoingModel.addRow(oFile.asRow());
            }
            outgoingModel.fireTableDataChanged();

        } catch (AuroraException ex) {

            logger.log(Level.SEVERE, ex.getMessage(), ex);

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
    }

    @Override
    public void sendingPart(int sequenceNumber, String fileId, String emailAddress) {

        updateStatus(String.format("Sending part #%d of %s to %s", sequenceNumber, fileId, emailAddress));
    }

    @Override
    public void unableToSendPart(int sequenceNumber, String fileId, String emailAddress) {

        showError(String.format("Unable to send part #%d of %s to %s", sequenceNumber, fileId, emailAddress));
    }

    @Override
    public void processingPart(int sequenceNumber, String fileId, String emailAddress) {

        updateStatus(String.format("Processing part #%d of %s from %s", sequenceNumber, fileId, emailAddress));
    }

    @Override
    public void discardedPart(int sequenceNumber, String fileId, String emailAddress) {

        updateStatus(String.format("Discarded part #%d of %s from %s", sequenceNumber, fileId, emailAddress));
    }

    @Override
    public void processingConfirmation(int sequenceNumber, String fileId, String emailAddress) {

        updateStatus(String.format("Processing confirmation #%d of %s from %s", sequenceNumber, fileId, emailAddress));
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
    public void fileComplete(String fileId, String emailAddress, String path) {

        JOptionPane.showMessageDialog(this, String.format("File %s successfully received\nfrom %s",
                fileId, emailAddress), "File received", JOptionPane.INFORMATION_MESSAGE);
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
    public void keysStored(String emailAddress) {

        JOptionPane.showMessageDialog(this, String.format("Keys for %s successfully stored", emailAddress),
                "Keys stored", JOptionPane.INFORMATION_MESSAGE);
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
        panel1.setLayout(new GridLayoutManager(1, 1, new Insets(10, 10, 10, 10), -1, -1));
        mainPanel.add(panel1, new GridConstraints(1, 0, 1, 3, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        panel1.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Incoming files"));
        final JScrollPane scrollPane1 = new JScrollPane();
        panel1.add(scrollPane1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        incomingTable = new JTable();
        scrollPane1.setViewportView(incomingTable);
        final JPanel panel2 = new JPanel();
        panel2.setLayout(new GridLayoutManager(1, 1, new Insets(10, 10, 10, 10), -1, -1));
        mainPanel.add(panel2, new GridConstraints(2, 0, 1, 3, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        panel2.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Outgoing files"));
        final JScrollPane scrollPane2 = new JScrollPane();
        panel2.add(scrollPane2, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        outgoingTable = new JTable();
        scrollPane2.setViewportView(outgoingTable);
        final JToolBar toolBar1 = new JToolBar();
        toolBar1.setFloatable(false);
        mainPanel.add(toolBar1, new GridConstraints(0, 0, 1, 3, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(-1, 20), null, 0, false));
        sendAndReceiveButton = new JButton();
        sendAndReceiveButton.setText("Send and Receive");
        toolBar1.add(sendAndReceiveButton);
        settingsButton = new JButton();
        settingsButton.setText("Settings");
        toolBar1.add(settingsButton);
        sendKeysButton = new JButton();
        sendKeysButton.setText("Send Keys");
        toolBar1.add(sendKeysButton);
        addFileButton = new JButton();
        addFileButton.setText("Add file");
        toolBar1.add(addFileButton);
        final JPanel panel3 = new JPanel();
        panel3.setLayout(new GridLayoutManager(1, 1, new Insets(8, 8, 8, 8), -1, -1));
        mainPanel.add(panel3, new GridConstraints(3, 0, 1, 3, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        panel3.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), null));
        statusLabel = new JLabel();
        statusLabel.setForeground(new Color(-10000537));
        statusLabel.setText(" ");
        panel3.add(statusLabel, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return mainPanel;
    }

}
