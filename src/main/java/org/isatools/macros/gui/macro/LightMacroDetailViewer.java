package org.isatools.macros.gui.macro;

import org.isatools.isacreator.common.UIHelper;
import org.isatools.isacreator.effects.HUDTitleBar;
import org.isatools.macros.gui.common.AutoMacronUIHelper;
import org.isatools.macros.manager.AutoMacronApplicationManager;
import org.jdesktop.fuse.InjectedResource;
import org.jdesktop.fuse.ResourceInjector;

import javax.swing.*;
import javax.swing.border.MatteBorder;
import java.awt.*;
import java.io.File;

public class LightMacroDetailViewer extends JFrame {

    @InjectedResource
    private Image closeIcon, closeOverIcon, closePressedIcon;

    private JLabel fullImage, abstractMacro, mediumMacro, detailedMacro;

    public LightMacroDetailViewer() {
        ResourceInjector.get("ui-package.style").inject(this);
    }

    public void createGUI() {
        setLayout(new BorderLayout());
        setUndecorated(true);
        setLocationRelativeTo(AutoMacronApplicationManager.getCurrentApplicationInstance());
        setPreferredSize(new Dimension(350, 300));
        setBackground(UIHelper.BG_COLOR);
        setAlwaysOnTop(true);

        createTopPanel();

        fullImage = new JLabel();
        abstractMacro = new JLabel();
        mediumMacro = new JLabel();
        detailedMacro = new JLabel();

        Box detailContainer = Box.createVerticalBox();
        detailContainer.add(UIHelper.createLabel(" Detailed Rendering ", UIHelper.VER_12_BOLD, AutoMacronUIHelper.GREY_COLOR));
        detailContainer.add(Box.createVerticalStrut(20));
        detailContainer.add(UIHelper.wrapComponentInPanel(fullImage));
        detailContainer.setBorder(new MatteBorder(0, 0, 0, 1, AutoMacronUIHelper.LIGHT_GREY_COLOR));

        add(detailContainer, BorderLayout.CENTER);

        Box otherIconContainer = Box.createVerticalBox();

        otherIconContainer.add(UIHelper.createLabel(" Macro Renderings ", UIHelper.VER_12_BOLD, AutoMacronUIHelper.GREY_COLOR));
        otherIconContainer.add(Box.createVerticalStrut(30));
        otherIconContainer.add(UIHelper.createLabel("Abstract Macro", UIHelper.VER_9_PLAIN, AutoMacronUIHelper.GREY_COLOR));
        otherIconContainer.add(abstractMacro);
        otherIconContainer.add(Box.createVerticalStrut(5));
        otherIconContainer.add(UIHelper.createLabel("Medium Macro", UIHelper.VER_9_PLAIN, AutoMacronUIHelper.GREY_COLOR));
        otherIconContainer.add(mediumMacro);
        otherIconContainer.add(Box.createVerticalStrut(5));
        otherIconContainer.add(UIHelper.createLabel("Detailed Macro", UIHelper.VER_9_PLAIN, AutoMacronUIHelper.GREY_COLOR));
        otherIconContainer.add(detailedMacro);

        add(otherIconContainer, BorderLayout.EAST);

        pack();
    }

    public void updateImages(File fullImageFile, File abstractImageFile, File mediumImageFile, File detailedImageFile) {
        fullImage.setIcon(AutoMacronUIHelper.scaleImageIcon(fullImageFile.getAbsolutePath(), 250, 250));
        abstractMacro.setIcon(AutoMacronUIHelper.scaleImageIcon(abstractImageFile.getAbsolutePath(), 70,70));
        mediumMacro.setIcon(AutoMacronUIHelper.scaleImageIcon(mediumImageFile.getAbsolutePath(), 70,70));
        detailedMacro.setIcon(AutoMacronUIHelper.scaleImageIcon(detailedImageFile.getAbsolutePath(), 70,70));
    }

    private void createTopPanel() {
        HUDTitleBar titleBar = new HUDTitleBar(null, null, closeIcon, closeIcon, closeOverIcon, closePressedIcon);
        add(titleBar, BorderLayout.NORTH);
        titleBar.installListeners();
    }
}
