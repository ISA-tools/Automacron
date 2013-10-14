package org.isatools.macros.gui.macro;

import org.isatools.isacreator.common.UIHelper;
import org.isatools.isacreator.effects.FooterPanel;
import org.isatools.isacreator.effects.HUDTitleBar;
import org.jdesktop.fuse.InjectedResource;
import org.jdesktop.fuse.ResourceInjector;

import javax.swing.*;
import java.awt.*;

/**
 * Created by the ISA team
 *
 * @author Eamonn Maguire (eamonnmag@gmail.com)
 *         <p/>
 *         Date: 03/11/2012
 *         Time: 15:44
 */
public class CompleteSwatchBoard extends JFrame {


    @InjectedResource
    private Image swatchHeadIcon, closeIcon, closeOverIcon, closePressedIcon;
    private ImageIcon resizeIcon;
    private SwatchBoard swatches;

    public CompleteSwatchBoard(SwatchBoard swatches) {
        this.swatches = swatches;
        ResourceInjector.get("ui-package.style").inject(this);
    }

    public void createGUI() {
        setLayout(new BorderLayout());
        setUndecorated(true);
        setPreferredSize(new Dimension(700, 600));
        setBackground(UIHelper.BG_COLOR);

        createTopPanel();

        add(swatches, BorderLayout.CENTER);

        FooterPanel footerPanel = new FooterPanel(this, UIHelper.BG_COLOR, resizeIcon);
        add(footerPanel, BorderLayout.SOUTH);

        pack();
    }

    public void showUI(Component relativeTo) {
        setLocationRelativeTo(relativeTo);
        setVisible(true);
    }

    private void createTopPanel() {
        HUDTitleBar titleBar = new HUDTitleBar(swatchHeadIcon, swatchHeadIcon, closeIcon, closeIcon, closeOverIcon, closePressedIcon);
        add(titleBar, BorderLayout.NORTH);
        titleBar.installListeners();
    }


}
