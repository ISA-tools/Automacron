package org.isatools.macros.graph.graphloader;

import org.isatools.macros.loaders.isa.ISAWorkflowLoader;
import org.junit.Test;

import java.io.*;

/**
 * Created by the ISA team
 *
 * @author Eamonn Maguire (eamonnmag@gmail.com)
 *         <p/>
 *         Date: 11/05/2012
 *         Time: 11:01
 */
public class GraphLoaderTest {

    @Test
    public void testLoadSimpleFile() throws IOException {

        String baseDir = System.getProperty("basedir");
        String filesPath = baseDir + "/target/test-classes/testdata/E-GEOD-26565_ChIP-Seq.txt";

        GraphLoader graphLoader = new GraphLoader();
        graphLoader.loadGraph(new ISAWorkflowLoader(graphLoader.getNeo4JConnector().getGraphDB()), new File(filesPath));

        graphLoader.printGraph(graphLoader.getNeo4JConnector().getGraphDB().getReferenceNode());
        System.out.println();
        System.out.println("");
        System.out.println();
        graphLoader.getNeo4JConnector().getGraphDB().shutdown();
    }


    
}
