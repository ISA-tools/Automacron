package org.isatools.macros.gui.macro.renderer;

import com.mxgraph.model.mxICell;
import com.mxgraph.view.mxGraph;
import org.isatools.isacreator.common.UIHelper;
import org.isatools.macros.AutoMacronProperties;
import org.isatools.macros.gui.common.AutoMacronUIHelper;
import org.isatools.macros.gui.macro.Macro;

import java.awt.*;
import java.io.File;
import java.util.*;
import java.util.List;

/**
 * Created by the ISA team
 *
 * @author Eamonn Maguire (eamonnmag@gmail.com)
 *         <p/>
 *         Date: 24/05/2012
 *         Time: 13:26
 */
public class DefaultMacroRenderer implements MacroRenderer {


    private static final int TOPOLOGY_WIDTH = 90;
    private static final int TOPOLOGY_HEIGHT = 90;

    private MotifGraphInfo motifGraphInfo;
    private Set<mxICell> visitedSet = new HashSet<mxICell>();

    private Map<String, Point> nodeIdToPoint;
    private Macro macro;

    private Point minPoints;
    private Point maxPoints;

    public DefaultMacroRenderer() {
        nodeIdToPoint = new HashMap<String, Point>();
    }

    private Map<RenderingType, File> drawMacros(mxICell parent, mxGraph graph) {
        // clear marker set
        ArrayList<DrawableItem> drawableItems = traverseGraph(parent, graph);
        ArrayList<Point> points = adjustPositions(drawableItems);

        Map<RenderingType, File> renderings = new HashMap<RenderingType, File>();

        if (motifGraphInfo.getRootVertex() != null) {

            MacroImage image = new MacroImage(RenderingType.ABSTRACT, drawableItems, points, nodeIdToPoint, macro);
            File abstractFile = generateMacroFile(RenderingType.ABSTRACT);
            image.renderMacro(abstractFile);

            renderings.put(RenderingType.ABSTRACT, abstractFile);

            image = new MacroImage(RenderingType.MEDIUM, drawableItems, points, nodeIdToPoint, macro);
            File mediumFile = generateMacroFile(RenderingType.MEDIUM);
            image.renderMacro(mediumFile);
            renderings.put(RenderingType.MEDIUM, mediumFile);

            image = new MacroImage(RenderingType.DETAILED, drawableItems, points, nodeIdToPoint, macro);
            File detailedFile = generateMacroFile(RenderingType.DETAILED);
            image.renderMacro(detailedFile);
            renderings.put(RenderingType.DETAILED, detailedFile);

        } else {
            System.err.println("Unable to render macro " + macro.toString());
        }

        return renderings;
    }

    private ArrayList<DrawableItem> traverseGraph(mxICell parent, mxGraph graph) {
        visitedSet = new HashSet<mxICell>();

        // create a queue Q
        List<mxICell> queue = new ArrayList<mxICell>();

        ArrayList<DrawableItem> drawableItems = new ArrayList<DrawableItem>();

        // enqueue v onto Q
        queue.add(parent);

        // mark v
        visit(parent);

        // while Q is not empty:
        while (!queue.isEmpty()) {

            // t <- Q.dequeue()
            mxICell cell = queue.get(0);
            queue.remove(cell);


            Point point = new Point((int) graph.getView().getState(cell).getX(), (int) graph.getView().getState(cell).getY());
            nodeIdToPoint.put(cell.getId(), point);
            DrawableItem item = new DrawableItem(cell.getStyle(), point);
            drawableItems.add(item);

            Object[] edges = graph.getOutgoingEdges(cell);
            for (Object edge : edges) {
                mxICell target = (mxICell) graph.getView().getVisibleTerminal(edge, false);
                item.children.add(target.getId());
                if (!isVisited(target)) {
                    visit(target);
                    queue.add(target);
                }
            }
        }
        return drawableItems;
    }


    /**
     * Adjusts the points to ensure that everything fits within the bounds required, and that the items are centered in the view
     * Performs:
     * Scaling
     * Centering
     *
     * @param drawableItems
     */
    private ArrayList<Point> adjustPositions(List<DrawableItem> drawableItems) {

        ArrayList<Point> points = extractPointsFromDrawableItems(drawableItems);
        getMinAndMaxPoints(points);

        scalePoints(points);

        int middlePointAdjustment = calculateMiddlePoint();

        for (Point point : points) {
            if (middlePointAdjustment != 0) {
                point.x += middlePointAdjustment;
            }
            point.y += 7;
        }

        return points;
    }

    private void scalePoints(List<Point> points) {

        double scaleFactorX = maxPoints.x > TOPOLOGY_WIDTH ? (double) TOPOLOGY_WIDTH / maxPoints.x : 1;
        double scaleFactorY = maxPoints.y > TOPOLOGY_HEIGHT ? (double) TOPOLOGY_HEIGHT / maxPoints.y : 1;

        for (Point point : points) {
            point.x = (int) (point.x * scaleFactorX);
            point.y = (int) (point.y * scaleFactorY);
        }
    }

    private int calculateMiddlePoint() {
        return ((TOPOLOGY_WIDTH - maxPoints.x) / 2);
    }

    /**
     * Gets max x and y points
     *
     * @param points - List of points to be analysed.
     * @return Point containing max X and Y.
     */
    private void getMinAndMaxPoints(List<Point> points) {
        maxPoints = new Point(Integer.MIN_VALUE, Integer.MIN_VALUE);
        minPoints = new Point(Integer.MAX_VALUE, Integer.MAX_VALUE);

        Map<Integer, List<Point>> levels = new HashMap<Integer, List<Point>>();

        for (Point point : points) {

            if (point.x > maxPoints.x) maxPoints.x = point.x;
            if (point.y > maxPoints.y) maxPoints.y = point.y;

            if (point.x < minPoints.x) minPoints.x = point.x;
            if (point.y > minPoints.y) minPoints.y = point.y;

            if (!levels.containsKey(point.x)) {
                levels.put(point.x, new ArrayList<Point>());
            }
            levels.get(point.x).add(point);
        }

    }

    private void visit(mxICell what) {
        visitedSet.add(what);
    }

    private boolean isVisited(mxICell what) {
        return visitedSet.contains(what);
    }

    private File generateMacroFile(RenderingType renderingType) {
        return new File(AutoMacronProperties.dataDir + "/" +
                (renderingType == RenderingType.ABSTRACT ? "abs" : renderingType == RenderingType.MEDIUM ? "med" : "det")
                + "-" + macro.getMotif().getStringRepresentation().hashCode() + AutoMacronProperties.png);
    }

    /**
     * Save the image to a pre-defined location
     *
     * @return @see File that has been created.
     */
    public Map<RenderingType, File> renderMacros(Macro macro, MotifGraphInfo motifGraphInfo) {

        this.motifGraphInfo = motifGraphInfo;
        this.macro = macro;

        return drawMacros((mxICell) motifGraphInfo.getRootVertex(), motifGraphInfo.getGraph());
    }


    public static ArrayList<Point> extractPointsFromDrawableItems(List<DrawableItem> drawableItems) {
        ArrayList<Point> points = new ArrayList<Point>();

        for (DrawableItem item : drawableItems) {
            points.add(item.point);
        }

        return points;
    }

}
