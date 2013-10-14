package org.isatools.macros.gui;

import javax.swing.*;
import java.awt.*;

/**
 * This will where graphs will be shown, or parallel coordinates etc. to select the most common motifs.
 *
 * @author Eamonn Maguire (eamonnmag@gmail.com)
 *         <p/>
 *         Date: 19/07/2012
 *         Time: 16:36
 */
public class CentralPanel extends JPanel {

    public CentralPanel() {
        setLayout(new BorderLayout());
    }

    public void switchView(final Container view) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                removeAll();
                add(view);
                updateUI();
            }
        });

    }
}
