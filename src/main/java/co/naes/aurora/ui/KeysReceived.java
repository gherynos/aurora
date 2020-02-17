package co.naes.aurora.ui;

import co.naes.aurora.Constellations;

import javax.swing.*;
import java.awt.*;
import java.util.Objects;

public class KeysReceived extends JDialog {

    private JPanel mainPanel;
    private JComboBox<String> block1ComboBox;
    private JComboBox<String> block2ComboBox;
    private JComboBox<String> block3ComboBox;
    private JComboBox<String> block4ComboBox;
    private JComboBox<String> block5ComboBox;
    private JComboBox<String> block6ComboBox;
    private JButton unlockButton;
    private JButton cancelButton;

    private char[] password;

    public KeysReceived(Frame owner) {

        super(owner, "Keys received", true);

        setContentPane(mainPanel);
        setMinimumSize(new Dimension(mainPanel.getMinimumSize().width, mainPanel.getMinimumSize().height + 22));
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(owner);

        unlockButton.addActionListener(e -> {

            String sb = block1ComboBox.getSelectedItem() +
                    Constellations.SEPARATOR +
                    block2ComboBox.getSelectedItem() +
                    Constellations.SEPARATOR +
                    block3ComboBox.getSelectedItem() +
                    Constellations.SEPARATOR +
                    block4ComboBox.getSelectedItem() +
                    Constellations.SEPARATOR +
                    block5ComboBox.getSelectedItem() +
                    Constellations.SEPARATOR +
                    block6ComboBox.getSelectedItem();
            password = sb.toCharArray();

            dispose();
        });

        cancelButton.addActionListener(e -> {

            int dialogResult = JOptionPane.showConfirmDialog(mainPanel,
                    "Are you sure you want to discard these keys?","Warning",
                    JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (dialogResult == JOptionPane.YES_OPTION){

                dispose();
            }
        });
    }

    private void createUIComponents() {

        block1ComboBox = new JComboBox<>(Constellations.LIST);
        block2ComboBox = new JComboBox<>(Constellations.LIST);
        block3ComboBox = new JComboBox<>(Constellations.LIST);
        block4ComboBox = new JComboBox<>(Constellations.LIST);
        block5ComboBox = new JComboBox<>(Constellations.LIST);
        block6ComboBox = new JComboBox<>(Constellations.LIST);
    }

    public char[] getPassword() {

        return password;
    }
}
