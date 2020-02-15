package co.naes.aurora.ui;

import javax.swing.*;

public class StatusModal {

    private JOptionPane pane;

    private JDialog dialog;

    JProgressBar progressBar;

    private String title;

    public StatusModal(String title) {

        this.title = title;

        progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);

        pane = new JOptionPane();
        pane.setOptions(new Object[]{});
    }

    private void createDialog() {

        dialog = pane.createDialog(null, title);
        dialog.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
    }

    public void setMessage(String message) {

        SwingUtilities.invokeLater(() -> {

            pane.setMessage(message);
            pane.add(progressBar, 1);

            if (dialog == null)
                createDialog();

            dialog.pack();
            if (!dialog.isShowing())
                dialog.setVisible(true);
        });
    }

    public void hide() {

        dialog.dispose();
        dialog = null;
    }
}
