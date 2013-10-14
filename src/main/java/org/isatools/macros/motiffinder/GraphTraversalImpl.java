package org.isatools.macros.motiffinder;

import org.isatools.macros.gui.DBGraph;
import org.isatools.macros.io.graphml.GraphMLCreator;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;

/**
 * Created by the ISA team
 *
 * @author Eamonn Maguire (eamonnmag@gmail.com)
 *         <p/>
 *         Date: 16/05/2012
 *         Time: 14:01
 */
public class GraphTraversalImpl extends CommonTraversalUtils implements MotifFinder {

    @Override
    protected void processMyMotifs(DBGraph dbGraph) {
    }

    /**
     * This will record the relations between two nodes, including the number of branch events that come from it.
     *
     * @param relationship - relationship to interrogate.
     * @param motif        - current motif to be passed through to the next level.
     * @return Returns current motif
     */
    protected void addMotif(DBGraph DBGraph, Relationship relationship, int depth, Motif motif, GraphMLCreator graphMLCreator) {
        Node originNode = relationship.getEndNode();
        Node targetNode = relationship.getStartNode();

        if (originNode.hasProperty("type")) {

            // we really want to be calculating the number of nodes in versus nodes out.
            calculateNumberOfRelationshipsInDirection(relationship, originNode, targetNode, Direction.INCOMING, graphMLCreator);
            calculateNumberOfRelationshipsInDirection(relationship, originNode, targetNode, Direction.OUTGOING, graphMLCreator);

            // we call this method recursively, returning only when the maximum node depth for a motif is reached
            Iterable<Relationship> relationshipsOfOriginNode = targetNode.getRelationships(Direction.INCOMING);
            while (relationshipsOfOriginNode.iterator().hasNext()) {
                Relationship relation = relationshipsOfOriginNode.iterator().next();
                if (motif != null) {
                    addMotif(DBGraph, relation, depth + 1, new Motif(motif), graphMLCreator);
                }
            }
        }
    }

}
