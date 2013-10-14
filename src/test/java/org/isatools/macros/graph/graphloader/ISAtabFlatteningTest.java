package org.isatools.macros.graph.graphloader;

import org.isatools.isacreator.model.Investigation;
import org.isatools.macros.gui.DBGraph;
import org.isatools.macros.loaders.isa.fileprocessing.isatab.ISAFileFlattener;
import org.isatools.macros.loaders.isa.ISAWorkflowLoader;
import org.junit.Test;

import java.io.File;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertTrue;

/**
 * Created by the ISA team
 *
 * @author Eamonn Maguire (eamonnmag@gmail.com)
 *         <p/>
 *         Date: 12/07/2012
 *         Time: 15:06
 */
public class ISAtabFlatteningTest {
    
    @Test
    public void flattenISATabTest() {
        String baseDir = System.getProperty("basedir");
        String filesPath = baseDir + "/target/test-classes/testdata/BII-I-1";
        File isatabDir = new File(filesPath);
        System.out.println(isatabDir);
        System.out.println(isatabDir.exists());

        Collection<File> flattenedFiles = ISAFileFlattener.flattenISATabFiles(isatabDir);

        assertTrue("Flattened file count is not what I was expecting, was " + flattenedFiles.size() + ", not 4.", flattenedFiles.size() == 4);

        GraphLoader graphLoader = new GraphLoader();

        long startLoadTime = System.currentTimeMillis();
        for(File flattenedFile : flattenedFiles) {
            System.out.println(flattenedFile.getAbsolutePath());
            graphLoader.loadGraph(new ISAWorkflowLoader( graphLoader.getNeo4JConnector().getGraphDB()), flattenedFile);
        }
        long endLoadTime = System.currentTimeMillis();
        System.out.printf("Took %d milliseconds to load files", endLoadTime - startLoadTime);

        graphLoader.getNeo4JConnector().getGraphDB().shutdown();

    }

    @Test
    public void flattenISATabTestWithFilter() {
        String baseDir = System.getProperty("basedir");
        String filesPath = baseDir + "/target/test-classes/testdata/BII-I-1";
        File isatabDir = new File(filesPath);
        System.out.println(isatabDir);
        System.out.println(isatabDir.exists());

        Investigation investigation = ISAFileFlattener.importISATabFiles(isatabDir);

        Set<String> samplesToFilter = new HashSet<String>();
        samplesToFilter.add("S-0.2-aliquot8");

        Collection<File> flattenedFiles = ISAFileFlattener.flattenISATabFiles(isatabDir, investigation, samplesToFilter);

        assertTrue("Flattened file count is not what I was expecting, was " + flattenedFiles.size() + ", not 4.", flattenedFiles.size() == 4);

        GraphLoader graphLoader = new GraphLoader();
        for(File file : flattenedFiles) {
            System.out.println("Loading: " + file.getAbsolutePath());
            graphLoader.loadGraph(new ISAWorkflowLoader(graphLoader.getNeo4JConnector().getGraphDB()), file);
        }

        List<DBGraph> DBGraphs = GraphFunctions.loadExperiments(graphLoader.getNeo4JConnector().getGraphDB());
        for(DBGraph DBGraph : DBGraphs) {
            File graphML = graphLoader.createGraphMLForExperiment(DBGraph);
            System.out.println("GraphML for experiment is in: " + graphML.getAbsolutePath());
        }
        graphLoader.getNeo4JConnector().getGraphDB().shutdown();
    }
}
