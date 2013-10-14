package commons;

import org.isatools.macros.graph.graphloader.Neo4JConnector;
import org.isatools.macros.loaders.isa.ISAWorkflowLoader;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Transaction;

import java.io.File;

/**
 * Created by the ISA team
 *
 * @author Eamonn Maguire (eamonnmag@gmail.com)
 *         <p/>
 *         Date: 03/12/2012
 *         Time: 11:33
 */
public class TestUtils {

    public Neo4JConnector loadGraph(File toLoad) {
        Neo4JConnector connector = new Neo4JConnector();

        GraphDatabaseService graphDatabaseService = connector.getGraphDB();
        Transaction tx = graphDatabaseService.beginTx();
        try {
            ISAWorkflowLoader loader = new ISAWorkflowLoader(graphDatabaseService);
            loader.loadFiles(toLoad);


            tx.success();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            tx.finish();
        }

        return connector;
    }

}
