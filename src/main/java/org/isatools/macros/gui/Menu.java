package org.isatools.macros.gui;

import org.jdesktop.fuse.InjectedResource;
import org.jdesktop.fuse.ResourceInjector;

import javax.swing.*;
import java.awt.*;

/**
 * Created by the ISA team
 *
 * @author Eamonn Maguire (eamonnmag@gmail.com)
 *         <p/>
 *         Date: 06/07/2012
 *         Time: 19:07
 */
public class Menu extends JFrame {

    static {
        ResourceInjector.addModule("org.jdesktop.fuse.swing.SwingModule");
        ResourceInjector.get("ui-package.style").load(
                Menu.class.getResource("/dependency_injections/ui-package.properties"));
    }

    @InjectedResource
    private ImageIcon logo;

    public Menu() {
        ResourceInjector.get("ui-package.style").inject(this);
    }
    
    public void createGUI() {
        setLayout(new BorderLayout());
        setUndecorated(true);

        setPreferredSize(new Dimension(400,400));
        setBackground(Color.white);
        addLogo();
        addMenu();
        addProgressIndicator();
        pack();
        setVisible(true);
    }

    private void addMenu() {
        //To change body of created methods use File | Settings | File Templates.

        // menu for help and to load content.
    }

    private void addLogo() {
        JPanel logoContainer = new JPanel();
        logoContainer.setOpaque(false);
        logoContainer.add(new JLabel(logo));

        add(logoContainer, BorderLayout.NORTH);
    }

    private void addProgressIndicator() {
        // add progress indicator with text indicating what is happening
    }
}
