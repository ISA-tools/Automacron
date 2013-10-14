package org.isatools.macros.io.graphml.compression;

import org.apache.commons.collections15.CollectionUtils;
import org.apache.commons.collections15.Predicate;
import org.isatools.macros.gui.macro.Macro;
import org.isatools.macros.gui.macro.renderer.RenderingType;
import org.isatools.macros.io.graphml.compression.graphml_model.Graph;
import org.isatools.macros.io.graphml.compression.graphml_model.GraphEdge;
import org.isatools.macros.io.graphml.compression.graphml_model.GraphNode;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

/**
 * Created by the ISA team
 *
 * @author Eamonn Maguire (eamonnmag@gmail.com)
 *         <p/>
 *         Date: 03/10/2012
 *         Time: 11:25
 */
public class CompressedGraphMLCreator {

    private Collection<Macro> macrosToBeSubstitutedIn;

    public CompressedGraphMLCreator(Collection<Macro> macrosToBeSubstituted) {
        this.macrosToBeSubstitutedIn = macrosToBeSubstituted;
    }

    // this will read in a GraphML file and store the nodes. It will then substitute the nodes that are covered by macros
    // in the Collection macrosToBeSubstituted and add in any required links to the new node.

    public File compressFile(File fileToCompress) {
        try {
            GraphMLReader graphMLReader = new GraphMLReader();
            Graph graph = graphMLReader.readGraph(fileToCompress);
            final Set<Long> substitutionsMade = new HashSet<Long>();
//            final Set<Macro> macrosInGraph = new HashSet<Macro>();

            int originalSize = graph.getGraphNodes().size();

            Map<Macro, Collection<Set<Long>>> substitutedMacros = removeRedundantNodes(graph, substitutionsMade);

            // now we need to filter out the edges.
            removeRedundantEdges(graph, substitutionsMade);

            long nextNodeId = getNextNodeIdInGraph(graph);

            for (Macro macro : substitutedMacros.keySet()) {
                for (Set<Long> macroNodeIds : substitutedMacros.get(macro)) {
                    GraphNode newMacroNode = createNewMacroNode(nextNodeId, macro);
                    graph.getGraphNodes().add(newMacroNode);
                    replaceNodeIdsInEdgesWithGivenId(graph, macroNodeIds, nextNodeId);
                    nextNodeId++;
                }

            }

            System.out.println("In the end, we have " + graph.getGraphNodes().size() + " nodes. Original size was: " + originalSize);
            File outputFile = getCompressedFile(fileToCompress);
            writeOutCompressedFile(graph, outputFile);
            System.out.println("Compressed GraphML Output To: " + outputFile.getAbsolutePath());
            return outputFile;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static File getCompressedFile(File fileToCompress) {
        return new File(fileToCompress.getParentFile().getAbsolutePath() + File.separator + fileToCompress.getName().replaceAll(".xml", "") + "-compressed.xml");
    }

    public static File getUnCompressedFile(File compressedFileName) {
        return new File(compressedFileName.getAbsolutePath().replaceAll("-compressed", ""));
    }

    private void removeRedundantEdges(Graph graph, final Set<Long> substitutionsMade) {
        CollectionUtils.filter(graph.getGraphEdges(), new Predicate<GraphEdge>() {
            public boolean evaluate(GraphEdge graphEdge) {
                return !(substitutionsMade.contains(graphEdge.getSource()) && substitutionsMade.contains(graphEdge.getTarget()));
            }
        });
    }

    private Map<Macro, Collection<Set<Long>>> removeRedundantNodes(Graph graph, final Set<Long> substitutionsMade) {

        // this is a variable which indicates that a macro can be inserted. It cannot be inserted when one of it's node ids has
        // already been removed.
        Map<Macro, Collection<Set<Long>>> canBeSubstituted = new HashMap<Macro, Collection<Set<Long>>>();

        for (final Macro macro : macrosToBeSubstitutedIn) {

            final Set<Set<Long>> nodeIDsToSubstitute = macro.getNodeIdsInMacro();

            for (Set<Long> macroIds : nodeIDsToSubstitute) {
                if (!CollectionUtils.containsAny(macroIds, substitutionsMade)) {

                    if (CollectionUtils.isSubCollection(macroIds, graph.getNodeIdsToNode().keySet())) {

                        if (!canBeSubstituted.containsKey(macro)) {
                            canBeSubstituted.put(macro, new ArrayList<Set<Long>>());
                        }
                        canBeSubstituted.get(macro).add(macroIds);

                        substitutionsMade.addAll(macroIds);

                        graph.removeNodesFromGraph(macroIds);
                    }

                }
            }
            System.out.println("Graph node size is now " + graph.getGraphNodes().size());
        }

        return canBeSubstituted;
    }

    private GraphNode createNewMacroNode(long nextNodeId, Macro macro) {
        GraphNode newMacroNode = new GraphNode(nextNodeId);
        newMacroNode.getData().put("image", macro.getGlyph(RenderingType.ABSTRACT).getAbsolutePath());
        newMacroNode.getData().put("image@L", macro.getGlyph(RenderingType.ABSTRACT).getAbsolutePath());
        newMacroNode.getData().put("image@M", macro.getGlyph(RenderingType.MEDIUM).getAbsolutePath());
        newMacroNode.getData().put("image@S", macro.getGlyph(RenderingType.DETAILED).getAbsolutePath());
        newMacroNode.getData().put("image@F", macro.getGlyph(RenderingType.FULL).getAbsolutePath());
        newMacroNode.getData().put("type", "macro");
        return newMacroNode;
    }

    private void writeOutCompressedFile(Graph graph, File outputFile) {
        GraphWriter writer = new GraphWriter();

        writer.write(graph, outputFile);
    }

    private void replaceNodeIdsInEdgesWithGivenId(Graph graph, Set<Long> macroNodeIds, long nextNodeId) {
        for (GraphEdge graphEdge : graph.getGraphEdges()) {
            if (macroNodeIds.contains(graphEdge.getSource())) graphEdge.setSource(nextNodeId);
            if (macroNodeIds.contains(graphEdge.getTarget())) graphEdge.setTarget(nextNodeId);
        }
    }

    private long getNextNodeIdInGraph(Graph graph) {
        long maxNode = 0;
        for (GraphEdge graphEdge : graph.getGraphEdges()) {
            if (graphEdge.getTarget() > maxNode) maxNode = graphEdge.getTarget();
        }
        return maxNode + 1;
    }


}
