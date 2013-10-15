package org.isatools.macros.io.graphml;

import org.neo4j.graphdb.Node;

import java.io.*;
import java.util.*;

/**
 * Creates a GraphML representation of the Neo4J database.
 *
 * @author Eamonn Maguire (eamonnmag@gmail.com)
 *         <p/>
 *         Date: 03/07/2012
 *         Time: 15:28
 */
public class GraphMLCreator {


    public static final String GRAPHML_START_TAG = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<graphml xmlns=\"http://graphml.graphdrawing.org/xmlns\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
            "xsi:schemaLocation=\"http://graphml.graphdrawing.org/xmlns http://graphml.graphdrawing.org/xmlns/1.0/graphml.xsd\">";
    public static final String GRAPHML_END_TAG = "</graph>\n" + "</graphml>";

    private Map<String, String> attributeToType;
    private List<String> nodeInformation;
    private List<String> edgeInformation;
    private Set<Long> addedNodeIds;
    private Set<String> addedEdges;

    private File file;

    public GraphMLCreator(File file) {
        this.file = file;
        attributeToType = new HashMap<String, String>();
        nodeInformation = new ArrayList<String>();
        edgeInformation = new ArrayList<String>();
        addedNodeIds = new HashSet<Long>();
        addedEdges = new HashSet<String>();

        addedNodeIds = Collections.synchronizedSet(addedNodeIds);
        addedEdges = Collections.synchronizedSet(addedEdges);
    }

    public void addAttribute(String attributeName, String type) {
        attributeToType.put(attributeName, type);
    }

    public void addNode(Node node) {
        if (!addedNodeIds.contains(node.getId())) {
            StringBuilder nodeSection = new StringBuilder();
            nodeSection.append("<node id=\"").append(node.getId()).append("\">");

            nodeSection.append("<data key=\"id\">").append(node.getId()).append("</data>");

            String defaultImage = "";
            for (String attribute : attributeToType.keySet()) {
                if (!attribute.equals("id") && node.hasProperty(attribute)) {
                    String property = node.getProperty(attribute).toString();
                    if (attribute.equals("image")) defaultImage = property;
                    if (property.contains("&")) {
                        property = property.replaceAll("&", "&amp;");
                    }
                    nodeSection.append("<data key=\"").append(attribute).append("\">").append(property).append("</data>");

                } else if (attribute.startsWith("image")) {
                    if (defaultImage.equals("")) {
                        defaultImage = getDefaultImage(node);
                    }
                    nodeSection.append("<data key=\"").append(attribute).append("\">").append(defaultImage).append("</data>");
                }
            }

            nodeSection.append("</node>");
            addedNodeIds.add(node.getId());
            nodeInformation.add(nodeSection.toString());
        }
    }

    private String getDefaultImage(Node node) {
        if (attributeToType.keySet().contains("image")) {
            if (node.hasProperty("image")) {
                String property = node.getProperty("image").toString();
                if (property.contains("&")) {
                    property = property.replaceAll("&", "&amp;");
                }
                return property;
            }
        }
        return "";
    }

    public void addEdge(Node fromNode, Node toNode) {
        String edgeValue1 = String.valueOf(toNode.getId()) + String.valueOf(fromNode.getId());
        String edgeValue2 = String.valueOf(fromNode.getId()) + String.valueOf(toNode.getId());
        if (!addedEdges.contains(edgeValue1) && !addedEdges.contains(edgeValue2)) {
            StringBuilder edgeSection = new StringBuilder();
            edgeSection.append("<edge source=\"" + fromNode.getId() + "\" target=\"" + toNode.getId() + "\"/>");
            edgeInformation.add(edgeSection.toString());
            addedEdges.add(edgeValue1);
        }
    }

    public void outputGraph(boolean directed) {

        StringBuilder graph = new StringBuilder();

        graph.append(GRAPHML_START_TAG);

        outputAttributes(graph);

        graph.append("<graph edgedefault=\"" + (directed ? "directed" : "undirected") + "\">");
        graph.append(createConcatenatedStringFromList(Collections.synchronizedList(nodeInformation)));
        graph.append(createConcatenatedStringFromList(Collections.synchronizedList(edgeInformation)));

        graph.append(GRAPHML_END_TAG);

        PrintStream filePrintStream;
        try {
            filePrintStream = new PrintStream(new FileOutputStream(file), true, "utf-8");
            filePrintStream.print(graph.toString());
            filePrintStream.close();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void outputAttributes(StringBuilder output) {
        attributeToType.put("id", "integer");
        for (String attribute : attributeToType.keySet()) {
            output.append("<key id=\"" + attribute + "\" for=\"node\" attr.name=\""
                    + attribute + "\" attr.type=\"" + attributeToType.get(attribute) + "\"/>");
        }
    }

    public File getFile() {
        return file;
    }

    private synchronized String createConcatenatedStringFromList(List<String> strings) {
        StringBuilder output = new StringBuilder();

        for (String string : strings) {
            output.append(string);
        }

        return output.toString();
    }


}
