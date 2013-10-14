package org.isatools.macros.gui.macro;

import org.isatools.macros.AutoMacronProperties;
import org.isatools.macros.gui.common.AutoMacronUIHelper;
import org.isatools.macros.gui.macro.renderer.MotifGraphRenderer;
import org.isatools.macros.gui.macro.renderer.RenderingFactory;
import org.isatools.macros.gui.macro.renderer.RenderingType;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.io.File;

/**
 * Created by the ISA team
 *
 * @author Eamonn Maguire (eamonnmag@gmail.com)
 *         <p/>
 *         Date: 07/06/2012
 *         Time: 10:44
 */
public class MacroUI extends JPanel implements Comparable<MacroUI> {

    public static final Border SELECTED_MACRO_BORDER = new LineBorder(AutoMacronUIHelper.DARK_ORANGE_COLOR, 2);
    public static final Border DEFAULT_MACRO_BORDER = new LineBorder(AutoMacronUIHelper.GREY_COLOR, 1);
    public static final Border DEFAULT_MACRO_HOVER_BORDER = new LineBorder(AutoMacronUIHelper.GREY_COLOR, 2);
    private Macro macro;
    private boolean selected;


    public MacroUI(Macro macro) {
        this.macro = macro;
        createGUI();
    }

    public MacroUI(MacroUI macro) {
        this.macro = macro.getMacro();
        createGUI();
    }

    public void createGUI() {
        setBackground(AutoMacronUIHelper.BG_COLOR);
        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(60, 60));

        generateMacros();
        ImageIcon icon = AutoMacronUIHelper.scaleImageIcon(macro.getGlyph(RenderingType.DETAILED).getAbsolutePath(), 50);

        add(new JLabel(icon), BorderLayout.CENTER);
        setBorder(DEFAULT_MACRO_BORDER);
    }

    public void generateMacros() {
        MotifGraphRenderer renderer = new MotifGraphRenderer(true);

        File fullImageFile = new File(AutoMacronProperties.dataDir + "full-" + macro.getMotif().getStringRepresentation().hashCode() + AutoMacronProperties.png);
        renderer.renderMacro(macro.getMotif(), fullImageFile);
        macro.addGlyph(RenderingType.FULL, fullImageFile);


        renderer.setShowLabels(false);
        renderer.renderMacro(macro.getMotif(), null);

        // now generate the rest of the icons.
        RenderingFactory.populateMacroWithFiles(macro, renderer.getGraph());
    }

    public void hover() {
        // border should be within a colour gradient reflecting the getCumulativeUsage of the motif. e.g.
        setBorder(DEFAULT_MACRO_HOVER_BORDER);
    }

    public void unHover() {
        if (getBorder() != SELECTED_MACRO_BORDER) {
            setBorder(DEFAULT_MACRO_BORDER);
        }
    }

    public Macro getMacro() {
        return macro;
    }

    @Override
    public String toString() {
        return macro.getGlyph(RenderingType.DETAILED).getAbsolutePath();
    }


    public int compareTo(MacroUI macroUI) {
        return macroUI.getMacro().getMotif().getScore() < getMacro().getMotif().getScore() ? -1 : 1;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public boolean isSelected() {
        return selected;
    }
}
