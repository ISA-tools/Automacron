package org.isatools.macros.gui.macro;

import org.isatools.isacreator.common.CommonMouseAdapter;
import org.isatools.macros.gui.common.AutoMacronUIHelper;
import org.isatools.macros.gui.common.WrapLayout;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


/**
 * Created by the ISA team
 *
 * @author Eamonn Maguire (eamonnmag@gmail.com)
 *         <p/>
 *         Date: 06/06/2012
 *         Time: 18:18
 */
public class SwatchBoard extends JScrollPane {

    private JPanel swatches;

    private Map<MacroUI, MacroDetail> glyphToPopupUI;
    private Set<MacroUI> addedMacros;

    public SwatchBoard() {
        super(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
    }

    public void createGUI() {

        addedMacros = new HashSet<MacroUI>();

        glyphToPopupUI = new HashMap<MacroUI, MacroDetail>();

        setBackground(Color.WHITE);
        setPreferredSize(new Dimension(WIDTH, HEIGHT));

        swatches = new JPanel(new WrapLayout(FlowLayout.LEFT));
        swatches.setMinimumSize(new Dimension(240, 200));
        setViewportView(swatches);


        getViewport().setBackground(AutoMacronUIHelper.BG_COLOR);
        setPreferredSize(new Dimension(253, 200));
        setBorder(new EmptyBorder(1, 1, 1, 1));
    }

    public void clearSwatches() {
        swatches.removeAll();
        addedMacros.clear();
        glyphToPopupUI.clear();
    }

    private void resetBordersOnMacroUIElements(){
        for (MacroUI macroUI : addedMacros) {
            macroUI.setBorder(MacroUI.DEFAULT_MACRO_BORDER);
        }
    }

    public void addGlyph(MacroUI macroUI) {
        swatches.add(macroUI);
        addedMacros.add(macroUI);

        macroUI.addMouseListener(new CommonMouseAdapter() {
            @Override
            public void mousePressed(MouseEvent mouseEvent) {
                super.mousePressed(mouseEvent);
                if (mouseEvent.getSource() instanceof MacroUI) {
                    MacroUI macroUI = ((MacroUI) mouseEvent.getSource());
                    MacroDetail macroDetailPopup;
                    if (glyphToPopupUI.containsKey(macroUI)) {
                        macroDetailPopup = glyphToPopupUI.get(macroUI);
                    } else {
                        macroDetailPopup = new MacroDetail(macroUI.getMacro());
                        glyphToPopupUI.put(macroUI, macroDetailPopup);

                    }

                    macroDetailPopup.setLocation(mouseEvent.getLocationOnScreen().x + (MacroDetail.WIDTH / 2), mouseEvent.getLocationOnScreen().y - (MacroDetail.HEIGHT + 50));
                    macroDetailPopup.setVisible(true);

                    resetBordersOnMacroUIElements();
                    macroUI.setBorder(MacroUI.SELECTED_MACRO_BORDER);
                    firePropertyChange("swatchClicked", null, mouseEvent.getSource());
                }
            }

            @Override
            public void mouseExited(MouseEvent mouseEvent) {
                super.mouseExited(mouseEvent);
                if (mouseEvent.getSource() instanceof MacroUI) {
                    MacroUI macroUI = ((MacroUI) mouseEvent.getSource());
                    macroUI.unHover();
                    if (glyphToPopupUI.get(macroUI) != null) {
                        glyphToPopupUI.get(macroUI).setVisible(false);
                    }
                }

                if (mouseEvent.getSource() instanceof MacroUI) {
                    firePropertyChange("swatchExited", null, mouseEvent.getSource());
                }
            }

            @Override
            public void mouseEntered(MouseEvent mouseEvent) {
                super.mouseEntered(mouseEvent);
                if (mouseEvent.getSource() instanceof MacroUI) {
                    MacroUI macroUI = ((MacroUI) mouseEvent.getSource());
                    macroUI.hover();

                    firePropertyChange("swatchHovered", null, mouseEvent.getSource());
                }
            }
        }

        );
    }

    public Set<MacroUI> getAddedMacroUIs() {
        return addedMacros;
    }

    public Set<Macro> getAddedMacros() {
        Set<Macro> toReturn = new HashSet<Macro>();
        for(MacroUI macro : addedMacros) {
            toReturn.add(macro.getMacro());
        }

        return toReturn;
    }
}
