package org.isatools.macros.gui.macro.renderer;

import org.isatools.isacreator.common.UIHelper;
import org.isatools.macros.gui.common.AutoMacronUIHelper;
import org.isatools.macros.gui.macro.Macro;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.geom.QuadCurve2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.List;

public class MacroImage extends JPanel {


    private static final int WIDTH = 95;
    private static final int HEIGHT = 140;

    private static final Color GREEN = new Color(129, 163, 62);
    private static final Color ORANGE = new Color(242, 107, 35);
    private static final Color LIGHT_ORANGE = new Color(241, 133, 46);
    private static final Color RED = new Color(239, 64, 61);

    private RenderingType renderingType;
    private ArrayList<DrawableItem> drawableItems;
    private ArrayList<Point> points;
    private Map<String, Point> nodeIdToPoint;
    private Macro macro;

    public MacroImage(RenderingType renderingType, ArrayList<DrawableItem> drawableItems, ArrayList<Point> points, Map<String, Point> nodeIdToPoint, Macro macro) {
        this.renderingType = renderingType;
        this.drawableItems = drawableItems;
        this.points = points;
        this.nodeIdToPoint = nodeIdToPoint;
        this.macro = macro;


        setLayout(new BorderLayout());
        setSize(new Dimension(WIDTH, HEIGHT));
        setBackground(Color.WHITE);
    }

    protected void drawAbstractDetailItem(Graphics2D graphics2D) {
        FastConvexHull convexHull = new FastConvexHull();
        ArrayList<Point> hull = convexHull.execute(points);

        Polygon polygon = new Polygon();

        boolean isFlat = true;
        int lastX = -1;
        for (Point p : hull) {
            polygon.addPoint(p.x, p.y);
            if (lastX == -1) {
                lastX = p.x;
            } else {
                if (lastX != p.x) isFlat = false;
            }
        }

        if (isFlat) {
            graphics2D.setStroke(new BasicStroke(10, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER));
            graphics2D.drawPolygon(polygon);
        }
        graphics2D.fillPolygon(polygon);

        // draw summary graphic...
        int itemWidth = getItemWidth();
        int xPos = 15;
        for (DrawableItem item : drawableItems) {

            if (item.style.equals("ROUNDED-GREEN")) {
                graphics2D.setColor(GREEN);
            } else if (item.style.equals("TRIANGLE-GREY")) {
                graphics2D.setColor(UIHelper.GREY_COLOR);
            } else if (item.style.equals("HEXAGON-ORANGE")) {
                graphics2D.setColor(LIGHT_ORANGE);
            } else if (item.style.equals("ROUNDED-RED")) {
                graphics2D.setColor(RED);
            } else if (item.style.equals("SQUARE-LIGHT-ORANGE")) {
                graphics2D.setColor(LIGHT_ORANGE);
            }
            graphics2D.fillRect(xPos, HEIGHT - 40, itemWidth, DrawableItem.height);
            xPos += itemWidth;
        }
    }

    private int getItemWidth() {
        return (int) ((double) (WIDTH - 30) / drawableItems.size());
    }

    protected void drawMediumDetailItem(Graphics2D graphics2D) {
        int xAdjustment = (DrawableItem.width / 2) * -1;
        graphics2D.setStroke(new BasicStroke(2, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        for (DrawableItem item : drawableItems) {
            if (item.style.equals("ROUNDED-GREEN")) {
                graphics2D.setColor(GREEN);
                graphics2D.fillRect(item.point.x + xAdjustment, item.point.y, DrawableItem.width, DrawableItem.width/2);
                graphics2D.drawRect(item.point.x + xAdjustment, item.point.y, DrawableItem.width, DrawableItem.width/2);
            } else if (item.style.equals("TRIANGLE-GREY")) {
                graphics2D.setColor(UIHelper.GREY_COLOR);
                Polygon p = new Polygon();
                p.addPoint(item.point.x + xAdjustment + (DrawableItem.width / 2), item.point.y);

                if (item.children.size() > 1) {
                    Point xSpan = getMinAndMaxXForChildren(item.children);
                    p.addPoint(xSpan.x + xAdjustment, item.point.y + DrawableItem.height);
                    p.addPoint(xSpan.y + DrawableItem.width + xAdjustment, item.point.y + DrawableItem.height);
                } else {
                    p.addPoint(item.point.x + xAdjustment, item.point.y + DrawableItem.height);
                    p.addPoint(item.point.x + DrawableItem.width + xAdjustment, item.point.y + DrawableItem.height);
                }
                graphics2D.fillPolygon(p);
                graphics2D.drawPolygon(p);
            } else if (item.style.equals("HEXAGON-ORANGE")) {
                graphics2D.setColor(ORANGE);
                graphics2D.fillRect(item.point.x + xAdjustment, item.point.y, DrawableItem.width, DrawableItem.width/2);
                graphics2D.drawRect(item.point.x + xAdjustment, item.point.y, DrawableItem.width, DrawableItem.width/2);
            } else if (item.style.equals("SQUARE-LIGHT-ORANGE")) {
                graphics2D.setColor(LIGHT_ORANGE);
                graphics2D.fillRect(item.point.x + xAdjustment, item.point.y, DrawableItem.width, DrawableItem.width/2);
                graphics2D.drawRect(item.point.x + xAdjustment, item.point.y, DrawableItem.width, DrawableItem.width/2);
            } else if (item.style.equals("ROUNDED-RED")) {
                graphics2D.setColor(RED);
                graphics2D.fillRect(item.point.x + xAdjustment, item.point.y, DrawableItem.width, DrawableItem.width/2);
                graphics2D.drawRect(item.point.x + xAdjustment, item.point.y, DrawableItem.width, DrawableItem.width/2);
            }
        }
    }

    private Point getMinAndMaxXForChildren(List<String> childNodeIds) {
        int minX = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE;
        for (String childItem : childNodeIds) {
            int childXPosition = nodeIdToPoint.get(childItem).x;
            minX = childXPosition < minX ? childXPosition : minX;
            maxX = childXPosition > maxX ? childXPosition : maxX;
        }
        return new Point(minX, maxX);
    }

    protected void drawDetailedItem(Graphics2D graphics2D) {
        int xAdjustment = (DrawableItem.width / 2) * -1;
        // we draw the lines linking nodes first, since they will then be below the nodes in the canvas. We use a quadratic
        // curve using an control point set as the X position of node B and Y position of node A.
        for (DrawableItem item : drawableItems) {
            for (String children : item.children) {
                QuadCurve2D curve = new QuadCurve2D.Double(item.point.x + xAdjustment + 3, item.point.y + 1, nodeIdToPoint.get(children).x + xAdjustment + 3, item.point.y + 1, nodeIdToPoint.get(children).x + xAdjustment + 3, nodeIdToPoint.get(children).y);
                graphics2D.draw(curve);
            }

            if (item.style.equals("ROUNDED-GREEN")) {
                graphics2D.setColor(GREEN);
                graphics2D.fillOval(item.point.x + xAdjustment, item.point.y, DrawableItem.width, DrawableItem.width);
                graphics2D.setColor(UIHelper.GREY_COLOR);
                graphics2D.drawOval(item.point.x + xAdjustment, item.point.y, DrawableItem.width, DrawableItem.width);
            } else if (item.style.equals("TRIANGLE-GREY")) {
                graphics2D.setColor(UIHelper.GREY_COLOR);
                Polygon p = new Polygon();
                p.addPoint(item.point.x + xAdjustment + (DrawableItem.width / 2), item.point.y + DrawableItem.height);

                p.addPoint(item.point.x + xAdjustment, item.point.y);
                p.addPoint(item.point.x + DrawableItem.width + xAdjustment, item.point.y);

                graphics2D.fillPolygon(p);
                graphics2D.drawPolygon(p);

            } else if (item.style.equals("HEXAGON-ORANGE")) {
                graphics2D.setColor(LIGHT_ORANGE);
                graphics2D.fillOval(item.point.x + xAdjustment, item.point.y, DrawableItem.width, DrawableItem.width);
                graphics2D.setColor(UIHelper.GREY_COLOR);
                graphics2D.drawOval(item.point.x + xAdjustment, item.point.y, DrawableItem.width, DrawableItem.width);
            } else if (item.style.equals("SQUARE-LIGHT-ORANGE")) {
                graphics2D.setColor(LIGHT_ORANGE);
                graphics2D.fillRect(item.point.x + xAdjustment, item.point.y, DrawableItem.width, DrawableItem.height);
                graphics2D.drawRect(item.point.x + xAdjustment, item.point.y, DrawableItem.width, DrawableItem.height);
            } else if (item.style.equals("ROUNDED-RED")) {
                graphics2D.setColor(RED);
                graphics2D.fillOval(item.point.x + xAdjustment, item.point.y, DrawableItem.width, DrawableItem.width);
                graphics2D.setColor(UIHelper.GREY_COLOR);
                graphics2D.drawOval(item.point.x + xAdjustment, item.point.y, DrawableItem.width, DrawableItem.width);
            }
        }
    }

    protected File renderMacro(File file) {
        int w = WIDTH;
        int h = HEIGHT;

        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = img.createGraphics();
        g2.setColor(Color.WHITE);
        g2.fillRect(0, 0, WIDTH, HEIGHT);

        paint(g2);
        g2.dispose();

        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }

        try {
            ImageIO.write(img, "png", file);
        } catch (IOException ioe) {
            System.err.println("Could not write image to " + file.getAbsolutePath());
        }

        return file;
    }

    private void drawMotif(Graphics2D graphics2D) {
        graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        graphics2D.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_SPEED);
        graphics2D.setColor(UIHelper.GREY_COLOR);

        graphics2D.setStroke(new BasicStroke(2, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        graphics2D.drawRect(0, 0, WIDTH - 1, HEIGHT - 30);

        if (renderingType == RenderingType.ABSTRACT) {
            drawAbstractDetailItem(graphics2D);
        } else if (renderingType == RenderingType.MEDIUM) {
            drawMediumDetailItem(graphics2D);
        } else if (renderingType == RenderingType.DETAILED) {
            drawDetailedItem(graphics2D);
        }

        if (macro.getAnnotation() != null && !macro.getAnnotation().isEmpty()) {
            graphics2D.setColor(AutoMacronUIHelper.LIGHT_GREY_COLOR);
            graphics2D.fillRect(3, HEIGHT - 26, WIDTH - 4, 25);
            graphics2D.setColor(UIHelper.GREY_COLOR);
            graphics2D.setFont(UIHelper.VER_10_BOLD);
            FontMetrics fontMetrics = graphics2D.getFontMetrics(UIHelper.VER_10_BOLD);

            int width = fontMetrics.stringWidth(macro.getAnnotation());
            int adjustment = (WIDTH - width) / 2;
            graphics2D.drawString(macro.getAnnotation(), adjustment, HEIGHT - 7);
        }

    }

    @Override
    public void paint(Graphics graphics) {
        super.paint(graphics);
        Graphics2D graphics2D = (Graphics2D) graphics;
        drawMotif(graphics2D);
    }


}
