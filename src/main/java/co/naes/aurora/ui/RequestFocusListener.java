package co.naes.aurora.ui;

import javax.swing.*;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;

public class RequestFocusListener implements AncestorListener {  // NOPMD

    public void ancestorAdded(final AncestorEvent e) {

        final AncestorListener al= this;
        SwingUtilities.invokeLater(() -> {

            JComponent component = e.getComponent();
            component.requestFocusInWindow();
            component.removeAncestorListener( al );
        });
    }

    public void ancestorMoved(AncestorEvent e) { }
    public void ancestorRemoved(AncestorEvent e) { }
}
