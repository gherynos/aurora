package co.naes.aurora.ui;

import co.naes.aurora.AuroraException;
import co.naes.aurora.LocalDB;
import co.naes.aurora.Messenger;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Main implements Messenger.StatusHandler {

    protected final Logger logger = Logger.getLogger(getClass().getName());

    private JPanel mainPanel;
    private JButton sendAndReceiveButton;
    private JTable incomingTable;
    private JTable outgoingTable;
    private JLabel statusLabel;
    private JButton settingsButton;
    private JButton sendKeysButton;

    private LocalDB db;

    private JFrame frame;

    private DefaultTableModel incomingModel;
    private DefaultTableModel outgoingModel;

    private Messenger messenger;

    private StatusModal statusModal;

    private Settings settings;

    private SendKeys sendKeys;

    public Main(LocalDB db) {

        this.db = db;

        frame = new JFrame("Aurora");
        frame.setContentPane(mainPanel);
        frame.setMinimumSize(
                new Dimension(mainPanel.getMinimumSize().width, mainPanel.getMinimumSize().height + 22));
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        statusModal = new StatusModal(frame, "Aurora");

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
        sendAndReceiveButton.addActionListener(e -> {

            new Thread(() -> {

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

            }).start();
        });
        settingsButton.addActionListener(e -> {

            if (settings == null)
                settings = new Settings(mainPanel, db, (boolean saved) -> settings = null);

            else
                settings.requestFocus();
        });
        sendKeysButton.addActionListener(e -> {

            if (sendKeys == null)
                sendKeys = new SendKeys(mainPanel, new SendKeys.SendKeysStatusHandler() {

                    @Override
                    public void sendKeys(String email) {

                        new Thread(() -> {

                            statusModal.setRelativeTo(sendKeys);
                            statusModal.setMessage("Sending key message...");

                            try {

                                messenger.sendKeys(email);

                            } catch (AuroraException ex) {

                                JOptionPane.showMessageDialog(frame, "Unable to send keys to recipient",
                                        "Error", JOptionPane.ERROR_MESSAGE);

                            } finally {

                                statusModal.hide();
                                statusModal.setRelativeTo(frame);
                            }

                        }).start();
                    }

                    @Override
                    public void sendKeysClosed() {

                        sendKeys = null;
                    }
                });

            else
                sendKeys.requestFocus();
        });
    }

    private void updateTables() {

        try {

            incomingModel.setRowCount(0);
            for (IncomingFile iFile: db.getIncomingFiles())
                incomingModel.addRow(iFile.asRow());
            incomingModel.fireTableDataChanged();

            outgoingModel.setRowCount(0);
            for (OutgoingFile oFile: db.getOutgoingFiles())
                outgoingModel.addRow(oFile.asRow());
            outgoingModel.fireTableDataChanged();

        } catch (AuroraException ex) {

            logger.log(Level.SEVERE, ex.getMessage(), ex);

            JOptionPane.showMessageDialog(frame, "Unable to load files status",
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateStatus(String status) {

        if (status.length() > 100)
            status = status.substring(0, 100) + "...";

        statusLabel.setText(status);
    }

    private void clearStatus() {

        updateStatus(" ");
    }

    @Override
    public void self(Messenger messenger) {

        this.messenger = messenger;
    }

    @Override
    public void sendingPart(int sequenceNumber, String fileId, String emailAddress) {

        updateStatus(String.format("Sending part #%d of %s for %s", sequenceNumber, fileId, emailAddress));
    }

    @Override
    public void unableToSendPart(int sequenceNumber, String fileId, String emailAddress) {

        JOptionPane.showMessageDialog(frame,
                String.format("Unable to send part #%d of %s for %s", sequenceNumber, fileId, emailAddress),
                "Error", JOptionPane.ERROR_MESSAGE);
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

        updateStatus(String.format("Processing confirmation #%d of %s to %s", sequenceNumber, fileId, emailAddress));
    }

    @Override
    public void errorsWhileSendingMessages(String message) {

        JOptionPane.showMessageDialog(frame,
                String.format("Unable to send pending messages: %s", message),
                "Error", JOptionPane.ERROR_MESSAGE);
    }

    @Override
    public void errorsWhileReceivingMessages(String message) {

        JOptionPane.showMessageDialog(frame,
                String.format("Unable to receive messages: %s", message),
                "Error", JOptionPane.ERROR_MESSAGE);
    }

    @Override
    public void errorsWhileProcessingReceivedMessage(String message) {

        JOptionPane.showMessageDialog(frame,
                String.format("Unable to process incoming message: %s", message),
                "Error", JOptionPane.ERROR_MESSAGE);
    }

    @Override
    public void errorsWhileProcessingKeyMessage(String message) {

        JOptionPane.showMessageDialog(frame,
                String.format("Unable to process incoming key message: %s", message),
                "Error", JOptionPane.ERROR_MESSAGE);
    }

    @Override
    public void fileComplete(String fileId, String emailAddress, String path) {

        // TODO: UI
    }

    @Override
    public char[] keyMessageReceived(String sender) {

        var kr = new KeysReceived(frame, sender);
        kr.setVisible(true);

        return kr.getPassword();
    }

    @Override
    public void keyMessageSent(char[] password) {

        if (sendKeys == null)
            JOptionPane.showMessageDialog(frame, "Keys sent but the dialog is closed",
                    "Error", JOptionPane.ERROR_MESSAGE);

        else
            sendKeys.keysSent(password);
    }

    @Override
    public void keysStored(String emailAddress) {

        JOptionPane.showMessageDialog(frame, String.format("Keys for %s successfully stored", emailAddress),
                "Keys stored", JOptionPane.INFORMATION_MESSAGE);
    }
}
