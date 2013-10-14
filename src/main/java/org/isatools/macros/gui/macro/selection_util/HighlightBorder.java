package org.isatools.macros.gui.macro.selection_util;

import javax.swing.border.AbstractBorder;
import java.awt.*;

/**
 * Created by the ISA team
 *
 * @author Eamonn Maguire (eamonnmag@gmail.com)
 *         <p/>
 *         Date: 05/12/2012
 *         Time: 17:02
 */
public class HighlightBorder extends AbstractBorder {
    private Color borderColor;
    private Color fillColor;

    public HighlightBorder(Color borderColor, Color fillColor) {
        this.borderColor = borderColor;
        this.fillColor = fillColor;
    }

    public Insets getBorderInsets(Component c, Insets insets) {
        insets.left = insets.top = insets.right = insets.bottom = 4;
        return insets;
    }

    public void paintBorder(Component c, Graphics g, int x, int y,
                            int width, int height) {

        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.translate(x, y);

        if (fillColor != null) {
            g2d.setColor(fillColor);
            g2d.fillRect(0, 0, width + 3, height + 7);
        } else {
            g2d.setColor(borderColor);
            g2d.drawRect(0, 0, width - 3, height - 7);
        }

        g2d.translate(-x, -y);
    }

    public boolean isBorderOpaque() {
        return true;
    }
}
