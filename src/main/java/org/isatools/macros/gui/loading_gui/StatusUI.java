package org.isatools.macros.gui.loading_gui;

import org.isatools.isacreator.common.UIHelper;
import org.isatools.isacreator.effects.borders.RoundedBorder;
import org.isatools.macros.gui.common.AutoMacronUIHelper;
import org.isatools.macros.manager.AutoMacronApplicationManager;

import javax.swing.*;
import java.awt.*;

/**
 * Created by the ISA team
 *
 * @author Eamonn Maguire (eamonnmag@gmail.com)
 *         <p/>
 *         Date: 02/10/2012
 *         Time: 10:20
 */
public class StatusUI extends JWindow {

    private static ImageIcon loadingImage = new ImageIcon(StatusUI.class.getResource("/images/ui/loader/automacron-loading.gif"));
    private static ImageIcon compressingImage = new ImageIcon(StatusUI.class.getResource("/images/ui/loader/automacron-compressing.gif"));
    private static ImageIcon working = new ImageIcon(StatusUI.class.getResource("/images/ui/working.gif"));

    public static final int LOADING = 0;
    public static final int COMPRESSING = 1;
    public static final int WORKING = 2;
    
    private JLabel image;
    
    private JLabel status;

    public StatusUI() {
        setLayout(new BorderLayout());
        setBackground(UIHelper.BG_COLOR);
//        ((JComponent) getContentPane()).setBorder(new RoundedBorder(UIHelper.GREY_COLOR, 4));
        createGUI();
        setAlwaysOnTop(true);
        pack();

    }

    public void showUI(int type) {
        showUI(type, AutoMacronApplicationManager.getCurrentApplicationInstance());
    }

    public void showUI(int type, Component locationRelativeTo) {
        image.setIcon(type == LOADING ? loadingImage : type == COMPRESSING ? compressingImage : working);
        setLocationRelativeTo(locationRelativeTo);
        setVisible(true);
        validate();
    }

    public void createGUI() {
        image = new JLabel(loadingImage);
        add(UIHelper.wrapComponentInPanel(image), BorderLayout.CENTER);

        status = UIHelper.createLabel("", UIHelper.VER_10_PLAIN, AutoMacronUIHelper.DARK_BLUE_COLOR);
        add(UIHelper.wrapComponentInPanel(status), BorderLayout.SOUTH);
    }

    public void updateStatus(String message) {
        status.setText("<html>" + message + "<html>");
    }

    public static void main(String[] args) {
        new StatusUI();
    }


}
