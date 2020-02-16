package co.naes.aurora.ui;

import co.naes.aurora.AuroraException;
import co.naes.aurora.Messenger;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SendKeys {

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

    private JFrame frame;

    private String[] password;

    public SendKeys(Component relativeTo, SendKeysStatusHandler statusHandler) {

        frame = new JFrame("Send key");
        frame.setContentPane(mainPanel);
        frame.setMinimumSize(
                new Dimension(mainPanel.getMinimumSize().width, mainPanel.getMinimumSize().height + 22));
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.pack();
        frame.setLocationRelativeTo(relativeTo);
        frame.setVisible(true);
        frame.addWindowListener(new WindowAdapter() {

            public void windowClosing(WindowEvent e) {

                statusHandler.sendKeysClosed();
            }
        });

        sendKeysButton.addActionListener(e -> {

            if (!emailTextField.getText().contains("@"))
                JOptionPane.showMessageDialog(frame, "Please insert a vaild email address",
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

            frame.dispose();
            statusHandler.sendKeysClosed();
        });
    }

    public void keysSent(char[] password) {

        keysReceivedButton.setEnabled(true);

        this.password = new String(password).split(" ");
    }

    public void requestFocus() {

        frame.requestFocus();
    }
}
