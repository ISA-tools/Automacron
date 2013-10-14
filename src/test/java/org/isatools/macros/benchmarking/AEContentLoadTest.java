package org.isatools.macros.benchmarking;

import org.isatools.macros.graph.graphloader.GraphFunctions;
import org.isatools.macros.graph.graphloader.Neo4JConnector;
import org.isatools.macros.gui.DBGraph;
import org.isatools.macros.loaders.stats.GraphStats;
import org.isatools.macros.loaders.stats.LoadingStatistics;
import org.junit.Test;
import org.neo4j.cypher.ExecutionEngine;
import org.neo4j.cypher.ExecutionResult;
import org.neo4j.graphdb.GraphDatabaseService;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;
import java.util.Map;

/**
 * Created by the ISA team
 *
 * @author Eamonn Maguire (eamonnmag@gmail.com)
 *         <p/>
 *         Date: 28/11/2012
 *         Time: 23:45
 */
public class AEContentLoadTest {

    @Test
    public void testLoad() {
        Neo4JConnector connector = new Neo4JConnector("/var/folders/r6/y47b44pj6j11ywvfbdk9j9p00000gn/T/automacron-graphs.db");

        GraphDatabaseService graphDatabaseService = connector.getGraphDB();
//        BatchISAWorkflowLoader loader = new BatchISAWorkflowLoader(graphDatabaseService);
//
//        loader.loadFiles(new File(BatchISAWorkflowLoader.FILE_CONVERSION_DIR));
//
        List<DBGraph> graphs = GraphFunctions.loadExperiments(graphDatabaseService);

        System.out.println("There are " + graphs.size());

        ExecutionEngine engine = new ExecutionEngine(connector.getGraphDB());

        for (DBGraph graph : graphs) {
            ExecutionResult result = engine.execute("start root=node("
                    + graph.getCorrespondingNodeInDB().getId() + ") " +
                    "match root-[*...5]->end" +
                    " return count(distinct end)");

            System.out.println(result.dumpToString());



        }

//        ThreadedMotifFinderImpl finder = new ThreadedMotifFinderImpl(6);
//        finder.performAnalysis(graphs);
    }

    @Test
    public void getStatMetrics() {
        try {
            Map<String, GraphStats> statsMap = LoadingStatistics.loadPreviousStatistics(new File("ProgramData/stats.txt"));

            LoadingStatistics.analyseStats(statsMap);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
}
