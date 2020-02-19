package co.naes.aurora.ui;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.List;

public class AddFile extends JDialog {

    private JPanel mainPanel;
    private JList<String> recipientList;
    private JButton browseButton;
    private JTextField fileTextField;
    private JButton okButton;
    private JButton cancelButton;

    private List<String> recipients;

    private String selectedRecipient;
    private File selectedFile;

    private boolean canceled = true;

    public AddFile(Frame owner, List<String> recipients) {

        super(owner, "Add file to send", true);

        this.recipients = recipients;

        setContentPane(mainPanel);
        setMinimumSize(new Dimension(mainPanel.getMinimumSize().width, mainPanel.getMinimumSize().height + 22));
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(owner);

        browseButton.addActionListener(e -> {

            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setCurrentDirectory(new File(System.getProperty("user.home")));
            int result = fileChooser.showOpenDialog(this);
            if (result == JFileChooser.APPROVE_OPTION) {

                selectedFile = fileChooser.getSelectedFile();
                fileTextField.setText(selectedFile.getName());
            }
        });

        okButton.addActionListener(e -> {

            if (recipientList.getSelectedIndex() == -1)
                JOptionPane.showMessageDialog(this, "Please select a recipient",
                        "Error", JOptionPane.WARNING_MESSAGE);

            else
                if (selectedFile == null)
                    JOptionPane.showMessageDialog(this, "Please select a file",
                            "Error", JOptionPane.WARNING_MESSAGE);

                else {

                    selectedRecipient = recipientList.getSelectedValue();

                    canceled = false;
                    dispose();
                }
        });

        cancelButton.addActionListener(e -> {

            canceled = true;
            dispose();
        });
    }

    private void createUIComponents() {

        recipientList = new JList<>(recipients.toArray(new String[]{}));
    }

    public File getSelectedFile() {

        return selectedFile;
    }

    public String getSelectedRecipient() {

        return selectedRecipient;
    }

    public boolean isCanceled() {

        return canceled;
    }
}
