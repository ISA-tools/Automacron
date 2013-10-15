package org.isatools.macros.motiffinder;

import org.apache.commons.collections15.map.ListOrderedMap;
import org.isatools.macros.gui.DBGraph;
import org.isatools.macros.io.graphml.GraphMLCreator;
import org.isatools.macros.loaders.isa.ISAWorkflowRelationships;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;

import java.io.File;
import java.util.*;

/**
 * Created by the ISA team
 *
 * @author Eamonn Maguire (eamonnmag@gmail.com)
 *         <p/>
 *         Date: 04/09/2012
 *         Time: 14:24
 */
public abstract class CommonTraversalUtils {

    protected Set<Long> visitedNodes;
    protected Vector<Long> observedExperiments;
    protected Set<Relationship> observedRelationships;
    protected Set<String> addedMotifs;
    protected Map<String, Map<String, Set<Node>>> relationshipCache;

    protected Map<DBGraph, Set<Motif>> graphMotifs;

    public CommonTraversalUtils() {
        visitedNodes = new HashSet<Long>();
        observedExperiments = new Vector<Long>();
        observedRelationships = new HashSet<Relationship>();
        addedMotifs = new HashSet<String>();
        graphMotifs = new HashMap<DBGraph, Set<Motif>>();
        relationshipCache = new HashMap<String, Map<String, Set<Node>>>();
    }

    public void performAnalysis(List<DBGraph> graphsToAnalyse, File graphMLFile) {
        for (DBGraph DBGraph : graphsToAnalyse) {
            analyseGraph(DBGraph, DBGraph.getCorrespondingNodeInDB(), new GraphMLCreator(graphMLFile));
            DBGraph.setUpdating(false);
        }
    }


    public void performAnalysis(DBGraph dbGraph, GraphMLCreator graphMLCreator) {
        graphMLCreator.addAttribute("type", "string");
        graphMLCreator.addAttribute("value", "string");
        graphMLCreator.addAttribute("taxonomy", "string");
        graphMLCreator.addAttribute("image", "string");
        graphMLCreator.addAttribute("image@S", "string");
        graphMLCreator.addAttribute("image@M", "string");
        graphMLCreator.addAttribute("image@L", "string");
        graphMLCreator.addAttribute("image@F", "string");
        analyseGraph(dbGraph, dbGraph.getCorrespondingNodeInDB(), graphMLCreator);
        graphMLCreator.outputGraph(true);
    }

    public void analyseGraph(DBGraph dbGraph, Node nodeToCheck, GraphMLCreator graphMLCreator) {
        addedMotifs.clear();
        relationshipCache.clear();
        if (!graphMotifs.containsKey(dbGraph)) {
            graphMotifs.put(dbGraph, new HashSet<Motif>());
        }
        interrogateNode(dbGraph, nodeToCheck, graphMLCreator);
        processMyMotifs(dbGraph);
    }

    protected abstract void processMyMotifs(DBGraph dbGraph);

    public Vector<Long> getObservedGraphs() {
        return observedExperiments;
    }

    public Set<Long> getVisitedNodes() {
        return visitedNodes;
    }

    protected void interrogateNode(DBGraph dbGraph, Node nodeToCheck, GraphMLCreator graphMLCreator) {

        if (nodeToCheck.hasProperty("type") && nodeToCheck.getProperty("type").equals("start")) {
            getObservedGraphs().add(nodeToCheck.getId());
            System.out.print("Experiment " + graphMLCreator.getFile().getName() + "\r");
        }

        if (!getVisitedNodes().contains(nodeToCheck.getId())) {
            for (Relationship relationship : nodeToCheck.getRelationships(Direction.INCOMING)) {
                // the node start and ends are dictated by the relationship. It's not fully obvious.
                Node targetNode = relationship.getStartNode();
                Node originNode = relationship.getEndNode();

                getVisitedNodes().add(originNode.getId());

                if (targetNode.hasProperty("type")) {
                    addMotif(dbGraph, relationship, 1, null, graphMLCreator);
                }

                interrogateNode(dbGraph, targetNode, graphMLCreator);
            }
        }
    }

    public Map<String, Set<Node>> calculateNumberOfRelationshipsInDirection(Relationship relationship, Node originNode, Node targetNode, Direction direction, GraphMLCreator graphMLCreator) {

        // we should check how many times this relationship is defined, since this will support the branches.
        // should we also check that the relation is going to the same node type?

        String key = originNode.getId() + ":" + targetNode.getId() + ":" + relationship.getType().toString();

        if (relationshipCache.containsKey(key)) {
            return relationshipCache.get(key);

        } else {
            graphMLCreator.addNode(originNode);
            graphMLCreator.addNode(targetNode);

            Iterable<Relationship> nodeRelationships = (direction == Direction.INCOMING
                    ? originNode.getRelationships(relationship.getType(), direction)
                    : targetNode.getRelationships(relationship.getType(), direction));

            Map<String, Set<Node>> typeToFrequency = new ListOrderedMap<String, Set<Node>>();
            Set<String> nodesTypeAndValuesAdded = new HashSet<String>();

            while (nodeRelationships.iterator().hasNext()) {
                Relationship relation = nodeRelationships.iterator().next();

                if (relation.getType() != ISAWorkflowRelationships.PROPERTY_OF) {

                    Node relationNode = relation.getStartNode();
                    graphMLCreator.addEdge(direction == Direction.INCOMING ? originNode : targetNode, direction == Direction.INCOMING ? targetNode : originNode);

                    String type = relationNode.getProperty("type").toString();
                    String value = relationNode.getProperty("value").toString();

                    String nodeKey = type + "#" + value;

                    if (!typeToFrequency.containsKey(nodeKey)) {
                        typeToFrequency.put(nodeKey, new HashSet<Node>());
                    }

                    if (!nodesTypeAndValuesAdded.contains(nodeKey)) {
                        typeToFrequency.get(nodeKey).add(relationNode);
                        nodesTypeAndValuesAdded.add(nodeKey);
                    }
                }
            }

            relationshipCache.put(key, typeToFrequency);
            return typeToFrequency;
        }
    }

    protected abstract void addMotif(DBGraph DBGraph, Relationship relationship, int depth,
                                     Motif motifBlock, GraphMLCreator graphMLCreator);

    public Map<String, Motif> getMotifs() {
        return null;
    }
}
