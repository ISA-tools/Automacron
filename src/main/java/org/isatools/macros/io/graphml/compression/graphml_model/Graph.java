package org.isatools.macros.io.graphml.compression.graphml_model;

import org.apache.commons.collections15.map.ListOrderedMap;

import java.util.*;

/**
 * Created by the ISA team
 *
 * @author Eamonn Maguire (eamonnmag@gmail.com)
 *         <p/>
 *         Date: 04/10/2012
 *         Time: 15:42
 */
public class Graph {

    String direction;
    List<GraphNode> graphNodes;
    List<GraphEdge> graphEdges;
    List<GraphProperty> graphProperties;

    private Map<Long, GraphNode> nodeIdsToNode;

    public Graph(String direction, List<GraphNode> graphNodes,
                 List<GraphEdge> graphEdges, List<GraphProperty> graphProperties) {
        this.direction = direction;
        this.graphNodes = graphNodes;

        nodeIdsToNode = new ListOrderedMap<Long, GraphNode>();

        if(!graphNodes.isEmpty()) {
            for(GraphNode graphNode : graphNodes) {
                nodeIdsToNode.put(graphNode.getId(), graphNode);
            }
        }

        this.graphEdges = graphEdges;
        this.graphProperties = graphProperties;
    }

    public List<GraphNode> getGraphNodes() {
        return graphNodes;
    }

    public List<GraphEdge> getGraphEdges() {
        return graphEdges;
    }

    public List<GraphProperty> getGraphProperties() {
        return graphProperties;
    }

    public Map<Long, GraphNode> getNodeIdsToNode() {
        return nodeIdsToNode;
    }

    public String getDirection() {
        return direction;
    }

    public void removeNodesFromGraph(Set<Long> nodeIdsToRemove) {

        for(Long node : nodeIdsToRemove) {
            nodeIdsToNode.remove(node);
        }

        graphNodes = new ArrayList<GraphNode>(nodeIdsToNode.values());
    }
}
