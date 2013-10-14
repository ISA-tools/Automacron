package org.isatools.macros.gui.macro.renderer;

import com.mxgraph.view.mxGraph;
import org.isatools.macros.AutoMacronProperties;
import org.isatools.macros.gui.motifdrawer.DrawerUtils;
import org.isatools.macros.gui.motifdrawer.MotifDrawer;
import org.isatools.macros.motiffinder.Motif;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by the ISA team
 *
 * @author Eamonn Maguire (eamonnmag@gmail.com)
 *         <p/>
 *         Date: 08/11/2012
 *         Time: 16:09
 */
public class MotifGraphRenderer {


    private boolean showLabels = false;
    private Map<Long, Object> addedNodes;
    private Map<Integer, Integer> motifDepth;
    private MotifDrawer motifDrawer;
    private Object rootVertex;

    public MotifGraphRenderer(boolean showLabels) {
        this.showLabels = showLabels;
    }

    public boolean isShowLabels() {
        return showLabels;
    }

    public void setShowLabels(boolean showLabels) {
        this.showLabels = showLabels;
    }

    public File renderMacro(Motif motif, File imageFile) {

        addedNodes = new HashMap<Long, Object>();
        motifDepth = new HashMap<Integer, Integer>();
        motifDrawer = new MotifDrawer();

        int xPos = 40;
        int yPos = 100;

        String startNode = motif.getInputNodeType();
        String outputNode = motif.getOutputNodeType();

        rootVertex = motifDrawer.insertVertex(showLabels ? startNode : "", xPos, yPos, RenderingUtils.inferNodeType(startNode));
        Object outputVertex = motifDrawer.insertVertex(showLabels ? outputNode : "", xPos, yPos, RenderingUtils.inferNodeType(outputNode));

        addedNodes.put(motif.getInputNode(), rootVertex);
        addedNodes.put(motif.getOutputNode(), outputVertex);

        motifDrawer.insertEdge(rootVertex, outputVertex, "#58595B");

        int motifId = motif.hashCode();
        motifDepth.put(motifId, 1);

        buildRepresentation(motif, motifDrawer, motifId);

        motif.setDepth(motifDepth.get(motifId));

        motifDrawer.layoutGraph();

        try {
            if (imageFile != null) DrawerUtils.saveGraphAsImage(motifDrawer.getMxGraph(), imageFile);
        } catch (IOException e) {
            System.err.println("Oh no! " + e.getMessage());
        }

        return imageFile;
    }

    public MotifGraphInfo getGraph() {
        return new MotifGraphInfo(motifDrawer.getMxGraph(), rootVertex);
    }

    public Object getRootVertex() {
        return rootVertex;
    }

    private void buildRepresentation(Motif motif, MotifDrawer drawer, int motifId) {

        for (Motif subMotif : motif.getSubMotifs()) {
            String outputNodeType = subMotif.getOutputNodeType();
            String inputNodeType = subMotif.getInputNodeType();

            if (!addedNodes.containsKey(subMotif.getInputNode())) {
                insertVertex(drawer, inputNodeType, subMotif.getInputNode());

            }
            insertVertex(drawer, outputNodeType, subMotif.getOutputNode());
            drawer.insertEdge(addedNodes.get(subMotif.getInputNode()), addedNodes.get(subMotif.getOutputNode()), "#58595B");

            motifDepth.put(motifId, motifDepth.get(motifId) + 1);

            buildRepresentation(subMotif, drawer, motifId);
        }
    }

    private void insertVertex(MotifDrawer drawer, String outputNodeType, Long nodeId) {
        if (!addedNodes.containsKey(nodeId)) {
            Object subMotifVertex = drawer.insertVertex(showLabels ? outputNodeType : "", 40, 40, RenderingUtils.inferNodeType(outputNodeType));
            addedNodes.put(nodeId, subMotifVertex);
        }
    }

}
