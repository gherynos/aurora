package co.naes.aurora.ui;

import co.naes.aurora.AuroraException;
import co.naes.aurora.LocalDB;
import co.naes.aurora.Messenger;
import co.naes.aurora.PublicKeys;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Main extends JFrame implements Messenger.StatusHandler {

    protected final Logger logger = Logger.getLogger(getClass().getName());

    private JPanel mainPanel;
    private JButton sendAndReceiveButton;
    private JTable incomingTable;
    private JTable outgoingTable;
    private JLabel statusLabel;
    private JButton settingsButton;
    private JButton sendKeysButton;
    private JButton addFileButton;

    private LocalDB db;

    private DefaultTableModel incomingModel;
    private DefaultTableModel outgoingModel;

    private Messenger messenger;

    private StatusModal statusModal;

    private Settings settings;

    private SendKeys sendKeys;

    public Main(LocalDB db) {

        super("Aurora");

        this.db = db;

        setContentPane(mainPanel);
        setMinimumSize(new Dimension(mainPanel.getMinimumSize().width, mainPanel.getMinimumSize().height + 22));
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        pack();
        setLocationRelativeTo(null);
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

                                showError("Unable to send keys to recipient");

                            } finally {

                                statusModal.hide();
                                statusModal.setRelativeTo(me);
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
        addFileButton.addActionListener(e -> {

            try {

                AddFile af = new AddFile(this, db.listPublicKeysAddresses());
                af.setVisible(true);
                if (!af.isCanceled()) {

                    PublicKeys recipient = db.getPublicKeys(af.getSelectedRecipient());
                    messenger.addFileToSend(recipient, af.getSelectedFile().getAbsolutePath());
                    updateTables();
                }

            } catch (AuroraException ex) {

                logger.log(Level.SEVERE, ex.getMessage(), ex);

                showError("Unable to load recipients");
            }
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

            showError("Unable to load files status");
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

        updateStatus(String.format("Sending part #%d of %s for %s", sequenceNumber, fileId, emailAddress));
    }

    @Override
    public void unableToSendPart(int sequenceNumber, String fileId, String emailAddress) {

        showError(String.format("Unable to send part #%d of %s for %s", sequenceNumber, fileId, emailAddress));
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

        // TODO: UI
    }

    @Override
    public char[] keyMessageReceived(String sender) {

        var kr = new KeysReceived(this, sender);
        kr.setVisible(true);

        return kr.getPassword();
    }

    @Override
    public void keyMessageSent(char[] password) {

        if (sendKeys == null)
            showError("Keys sent but the dialog is closed");

        else
            sendKeys.keysSent(password);
    }

    @Override
    public void keysStored(String emailAddress) {

        JOptionPane.showMessageDialog(this, String.format("Keys for %s successfully stored", emailAddress),
                "Keys stored", JOptionPane.INFORMATION_MESSAGE);
    }
}
