package org.isatools.macros.io.graphml.compression;

import org.isatools.macros.io.graphml.compression.graphml_model.Graph;
import org.isatools.macros.io.graphml.compression.graphml_model.GraphEdge;
import org.isatools.macros.io.graphml.compression.graphml_model.GraphNode;
import org.isatools.macros.io.graphml.compression.graphml_model.GraphProperty;
import org.w3c.dom.NodeList;
import uk.ac.ebi.utils.xml.XPathReader;

import javax.xml.xpath.XPathConstants;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

public class GraphMLReader {
    public GraphMLReader() {
    }

    List<GraphProperty> readGraphProperties(XPathReader reader) {
        List<GraphProperty> graphProperties = new ArrayList<GraphProperty>();

        NodeList edges = (NodeList) reader.read("/graphml/key", XPathConstants.NODESET);

        if (edges.getLength() > 0) {
            for (int edgeIndex = 1; edgeIndex < edges.getLength() + 1; edgeIndex++) {
                String id = (String) reader.read("/graphml/key[" + edgeIndex + "]/@id", XPathConstants.STRING);
                String forType = (String) reader.read("/graphml/key[" + edgeIndex + "]/@for", XPathConstants.STRING);
                String attr_name = (String) reader.read("/graphml/key[" + edgeIndex + "]/@attr.name", XPathConstants.STRING);
                String attr_type = (String) reader.read("/graphml/key[" + edgeIndex + "]/@attr.type", XPathConstants.STRING);
                graphProperties.add(new GraphProperty(id, forType, attr_name, attr_type));
            }
        }


        return graphProperties;
    }

    public Graph readGraph(File graphMLFile) throws FileNotFoundException {
        XPathReader reader = new XPathReader(new FileInputStream(graphMLFile));

        String direction = readIsGraphDirected(reader);
        List<GraphNode> graphNodes = readGraphNodes(reader);
        List<GraphEdge> graphEdges = readGraphEdges(reader);
        List<GraphProperty> graphProperties = readGraphProperties(reader);

        return new Graph(direction, graphNodes, graphEdges, graphProperties);
    }

    private String readIsGraphDirected(XPathReader reader) {
        return (String) reader.read("/graphml/graph/@edgedefault", XPathConstants.STRING);
    }

    private List<GraphNode> readGraphNodes(XPathReader reader) {
        List<GraphNode> graphNodes = new ArrayList<GraphNode>();

        NodeList nodes = (NodeList) reader.read("/graphml/graph/node", XPathConstants.NODESET);

        if (nodes.getLength() > 0) {
            for (int nodeIndex = 1; nodeIndex < nodes.getLength() + 1; nodeIndex++) {
                long id = Long.valueOf((String) reader.read("/graphml/graph/node[" + nodeIndex + "]/@id", XPathConstants.STRING));

                NodeList nodeData = (NodeList) reader.read("/graphml/graph/node[" + nodeIndex + "]/data", XPathConstants.NODESET);

                GraphNode graphNode = new GraphNode(id);

                if (nodeData.getLength() > 0) {
                    for (int nodeDataIndex = 1; nodeDataIndex < nodeData.getLength() + 1; nodeDataIndex++) {
                        String key = (String) reader.read("/graphml/graph/node[" + nodeIndex + "]/data[" + nodeDataIndex + "]/@key", XPathConstants.STRING);
                        String value = (String) reader.read("/graphml/graph/node[" + nodeIndex + "]/data[" + nodeDataIndex + "]", XPathConstants.STRING);
                        graphNode.getData().put(key, value.trim());
                    }
                }
                graphNodes.add(graphNode);
            }
        }
        return graphNodes;
    }

    private List<GraphEdge> readGraphEdges(XPathReader reader) {
        List<GraphEdge> graphEdges = new ArrayList<GraphEdge>();

        NodeList edges = (NodeList) reader.read("/graphml/graph/edge", XPathConstants.NODESET);

        if (edges.getLength() > 0) {
            for (int edgeIndex = 1; edgeIndex < edges.getLength() + 1; edgeIndex++) {
                long source = Long.valueOf((String) reader.read("/graphml/graph/edge[" + edgeIndex + "]/@source", XPathConstants.STRING));
                long target = Long.valueOf((String) reader.read("/graphml/graph/edge[" + edgeIndex + "]/@target", XPathConstants.STRING));
                graphEdges.add(new GraphEdge(source, target));
            }
        }

        return graphEdges;
    }


}