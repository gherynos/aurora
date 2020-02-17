package co.naes.aurora.ui;

import co.naes.aurora.Constellations;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.logging.Logger;

public class SendKeys extends JFrame {

    public interface SendKeysStatusHandler {

        void sendKeys(String email);

        void sendKeysClosed();
    }

    protected final Logger logger = Logger.getLogger(getClass().getName());

    private JPanel mainPanel;
    private JTextField emailTextField;
    private JButton sendKeysButton;
    private JTextArea passwordTextArea;
    private JButton keysReceivedButton;
    private JButton closeButton;
    private JTextField block1TextField;
    private JTextField block2TextField;
    private JTextField block3TextField;
    private JTextField block4TextField;
    private JTextField block5TextField;
    private JTextField block6TextField;

    private String[] password;

    public SendKeys(Component relativeTo, SendKeysStatusHandler statusHandler) {

        super("Send key");

        setContentPane(mainPanel);
        setMinimumSize(new Dimension(mainPanel.getMinimumSize().width, mainPanel.getMinimumSize().height + 22));
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        pack();
        setLocationRelativeTo(relativeTo);
        addWindowListener(new WindowAdapter() {

            public void windowClosing(WindowEvent e) {

                statusHandler.sendKeysClosed();
            }
        });

        sendKeysButton.addActionListener(e -> {

            if (!emailTextField.getText().contains("@"))
                JOptionPane.showMessageDialog(this, "Please insert a vaild email address",
                        "Error", JOptionPane.ERROR_MESSAGE);

            else {

                sendKeysButton.setEnabled(false);
                statusHandler.sendKeys(emailTextField.getText());
            }
        });

        keysReceivedButton.addActionListener(e -> {

            keysReceivedButton.setEnabled(false);

            block1TextField.setText(password[0]);
            block2TextField.setText(password[1]);
            block3TextField.setText(password[2]);
            block4TextField.setText(password[3]);
            block5TextField.setText(password[4]);
            block6TextField.setText(password[5]);
        });

        closeButton.addActionListener(e -> {

            dispose();
            statusHandler.sendKeysClosed();
        });

        setVisible(true);
    }

    public void keysSent(char[] password) {

        keysReceivedButton.setEnabled(true);

        this.password = new String(password).split(Constellations.SEPARATOR);
    }
}
