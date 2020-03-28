package co.naes.aurora.ui;

import javax.swing.*;
import java.awt.*;

public class StatusModal {

    private final JOptionPane pane;

    private JDialog dialog;

    private final JProgressBar progressBar;

    private final String title;

    private Component relativeTo;

    public StatusModal(Component relativeTo, String title) {

        this.relativeTo = relativeTo;
        this.title = title;

        progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);

        pane = new JOptionPane();
        pane.setOptions(new Object[]{});
    }

    private void createDialog() {

        dialog = pane.createDialog(relativeTo, title);
        dialog.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        dialog.setLocationRelativeTo(relativeTo);
    }

    public void setMessage(String message) {

        SwingUtilities.invokeLater(() -> {

            pane.setMessage(message);
            pane.add(progressBar, 1);

            if (dialog == null) {

                createDialog();
            }

            dialog.pack();
            if (!dialog.isShowing()) {

                dialog.setVisible(true);
            }
        });
    }

    public void hide() {

        dialog.dispose();
        dialog = null;  // NOPMD
    }

    public void setRelativeTo(Component component) {

        relativeTo = component;

        if (dialog != null) {

            dialog.setLocationRelativeTo(component);
        }
    }
}
