package org.isatools.macros.gui.macro;

import com.sun.awt.AWTUtilities;
import org.isatools.isacreator.common.UIHelper;
import org.isatools.macros.gui.common.AutoMacronUIHelper;
import org.isatools.macros.gui.macro.renderer.MotifGraphRenderer;
import org.isatools.macros.gui.macro.renderer.RenderingType;
import org.isatools.macros.gui.macro.selection_util.CircularScoreIndicator;

import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.border.MatteBorder;
import java.awt.*;
import java.io.File;

/**
 * Created by the ISA team
 *
 * @author Eamonn Maguire (eamonnmag@gmail.com)
 *         <p/>
 *         Date: 07/06/2012
 *         Time: 13:37
 */
public class MacroDetail extends JWindow {

    public static final int WIDTH = 500;
    public static final int HEIGHT = 360;

    private Macro macro;

    public MacroDetail(Macro macro) {
        this.macro = macro;

        setLayout(new BorderLayout());

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createGUI();
            }
        });
    }

    public void createGUI() {
        setBackground(AutoMacronUIHelper.BG_COLOR);
        setAlwaysOnTop(true);

        ((JComponent) getContentPane()).setBorder(new LineBorder(AutoMacronUIHelper.LIGHT_GREY_COLOR, 2));
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        AWTUtilities.setWindowOpacity(this, .95f);

        Box scoreContainer = Box.createVerticalBox();
        scoreContainer.add(UIHelper.createLabel(" Score ", UIHelper.VER_12_BOLD, AutoMacronUIHelper.GREY_COLOR));
        scoreContainer.add(Box.createVerticalStrut(20));
        scoreContainer.add(new CircularScoreIndicator(macro.getMotif()));
        scoreContainer.setBorder(new MatteBorder(0, 0, 0, 1, AutoMacronUIHelper.LIGHT_GREY_COLOR));

        add(scoreContainer, BorderLayout.WEST);

        // We wish to show e

        ImageIcon icon = AutoMacronUIHelper.scaleImageIcon(macro.getGlyphGranularities().get(RenderingType.FULL).getAbsolutePath(), 250, 250);

        ImageIcon abstractIcon = AutoMacronUIHelper.scaleImageIcon(macro.getGlyphGranularities().get(RenderingType.ABSTRACT).getAbsolutePath(), 70, 70);
        ImageIcon mediumIcon = AutoMacronUIHelper.scaleImageIcon(macro.getGlyphGranularities().get(RenderingType.MEDIUM).getAbsolutePath(), 70, 70);
        ImageIcon detailedIcon = AutoMacronUIHelper.scaleImageIcon(macro.getGlyphGranularities().get(RenderingType.DETAILED).getAbsolutePath(), 70, 70);

        Box detailContainer = Box.createVerticalBox();
        detailContainer.add(UIHelper.createLabel(" Detailed Rendering ", UIHelper.VER_12_BOLD, AutoMacronUIHelper.GREY_COLOR));
        detailContainer.add(Box.createVerticalStrut(20));
        detailContainer.add(UIHelper.wrapComponentInPanel(new JLabel(icon)));
        detailContainer.setBorder(new MatteBorder(0, 0, 0, 1, AutoMacronUIHelper.LIGHT_GREY_COLOR));

        add(detailContainer, BorderLayout.CENTER);

        Box otherIconContainer = Box.createVerticalBox();

        otherIconContainer.add(UIHelper.createLabel(" Macro Renderings ", UIHelper.VER_12_BOLD, AutoMacronUIHelper.GREY_COLOR));
        otherIconContainer.add(Box.createVerticalStrut(30));
        otherIconContainer.add(UIHelper.createLabel("Abstract Macro", UIHelper.VER_9_PLAIN, AutoMacronUIHelper.GREY_COLOR));
        otherIconContainer.add(new JLabel(abstractIcon));
        otherIconContainer.add(Box.createVerticalStrut(5));
        otherIconContainer.add(UIHelper.createLabel("Medium Macro", UIHelper.VER_9_PLAIN, AutoMacronUIHelper.GREY_COLOR));
        otherIconContainer.add(new JLabel(mediumIcon));
        otherIconContainer.add(Box.createVerticalStrut(5));
        otherIconContainer.add(UIHelper.createLabel("Detailed Macro", UIHelper.VER_9_PLAIN, AutoMacronUIHelper.GREY_COLOR));
        otherIconContainer.add(new JLabel(detailedIcon));

        add(otherIconContainer, BorderLayout.EAST);

        pack();
    }

}
