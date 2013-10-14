package org.isatools.macros.graph.graphloader;

import org.isatools.macros.gui.DBGraph;
import org.isatools.macros.loaders.isa.ISAWorkflowRelationships;
import org.neo4j.cypher.ExecutionEngine;
import org.neo4j.cypher.ExecutionResult;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.Transaction;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by the ISA team
 *
 * @author Eamonn Maguire (eamonnmag@gmail.com)
 *         <p/>
 *         Date: 17/07/2012
 *         Time: 14:31
 */
public class GraphFunctions {

    public static List<DBGraph> loadExperiments(GraphDatabaseService graphDB) {

        List<DBGraph> DBGraphNodes = new ArrayList<DBGraph>();

        // here as something to check if a graph has already been added. This can sometimes happen.
        Set<String> addedGraphs = new HashSet<String>();

        for (Relationship relationship : graphDB.getReferenceNode().getRelationships(ISAWorkflowRelationships.EXPERIMENT_OF)) {
            // the node start and ends are dictated by the relationship. It's not fully obvious.
            Node targetNode = relationship.getStartNode();

            if (targetNode.hasProperty("value")) {
                String value = targetNode.getProperty("value").toString();
                if (!addedGraphs.contains(value)) {
                    DBGraphNodes.add(new DBGraph(targetNode));
                    addedGraphs.add(value);
                }
            }
        }
        return DBGraphNodes;
    }

    public static boolean deleteExperiment(GraphDatabaseService graphDB, DBGraph experiment) {
        Transaction tx = graphDB.beginTx();
        try {
            ExecutionEngine engine = new ExecutionEngine( graphDB );
            ExecutionResult result = engine.execute( "START n=node(" + experiment.getCorrespondingNodeInDB().getId() +")\n" +
                    "MATCH n-[r?]-()\n" +
                    "WHERE ID(n) <> 0\n" +
                    "DELETE n,r" );
            System.out.println(result.toString());
            tx.success();
            return true;
        } catch (Exception e) {
            return false;
        } finally {
            tx.finish();
        }
    }
}


