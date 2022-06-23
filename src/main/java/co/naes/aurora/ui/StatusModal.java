/*
 * Copyright (C) 2020  Luca Zanconato (<github.com/gherynos>)
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
