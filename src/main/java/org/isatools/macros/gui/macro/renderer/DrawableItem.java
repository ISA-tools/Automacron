package org.isatools.macros.gui.macro.renderer;

import java.awt.*;
import java.util.*;
import java.util.List;


public class DrawableItem {
    public static final int width = 6;
    public static final int height = 8;
    String style;
    Point point;
    List<String> children;

    public DrawableItem(String style, Point point) {
        this.style = style;
        this.point = point;
        children = new ArrayList<String>();
    }
}
