package org.isatools.macros.gui.common;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Created by the ISA team
 *
 * @author Eamonn Maguire (eamonnmag@gmail.com)
 *         <p/>
 *         Date: 25/11/2012
 *         Time: 10:15
 */
public class CursorChangeMouseAdapter extends MouseAdapter {

    private Container container;

    public CursorChangeMouseAdapter(Container container) {
        this.container = container;
    }

    @Override
    public void mouseEntered(MouseEvent mouseEvent) {
        container.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    @Override
    public void mouseExited(MouseEvent mouseEvent) {
        container.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
    }
}