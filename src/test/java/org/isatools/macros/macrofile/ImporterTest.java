package org.isatools.macros.macrofile;

import org.isatools.macros.graph.graphloader.Neo4JConnector;
import org.isatools.macros.gui.DBGraph;
import org.isatools.macros.io.graphml.GraphMLCreator;
import org.isatools.macros.loaders.isa.BatchISAWorkflowLoader;
import org.isatools.macros.loaders.isa.ISAWorkflowLoader;
import org.isatools.macros.macrofile.importer.MacroFileImporter;
import org.isatools.macros.motiffinder.MotifFinder;
import org.isatools.macros.motiffinder.TargetedMotifFinderImpl;
import org.isatools.macros.utils.MotifSelectionAlgorithm;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static junit.framework.Assert.assertTrue;

public class ImporterTest {

    private static Neo4JConnector connector = new Neo4JConnector();
    private MotifSelectionAlgorithm motifSelectionAlgorithm;
    private Node startNode;

    @Before
    public void setUp() throws Exception {

        GraphDatabaseService graphDatabaseService = connector.getGraphDB();
        Transaction tx = graphDatabaseService.beginTx();
        try {
            String baseDir = System.getProperty("basedir");
            String filesPath = baseDir + "/target/test-classes/testdata/BII-I-1";
            File isaTabFiles = new File(filesPath);

            System.out.println("ISA-Tab file exists? " + isaTabFiles.exists());

            BatchISAWorkflowLoader loader = new BatchISAWorkflowLoader(graphDatabaseService);
            loader.loadFiles(isaTabFiles);

            startNode = graphDatabaseService.getNodeById(0);
            tx.success();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            tx.finish();
        }
    }

    @After
    public void cleanUp() throws Exception {
        Neo4JConnector.shutdown();
    }


    @Ignore
    public void testFileImport() {
        String baseDir = System.getProperty("basedir");
        String filesPath = baseDir + "/target/test-classes/testdata/macrofiles/macro-output.xml";

        MacroFileImporter importer = new MacroFileImporter();
        try {
            List<LightMacro> macros = importer.importFile(new File(filesPath));

            System.out.println("Macro size " + macros.size());
            assertTrue("Macro size isn't 2 :(", macros.size() == 2);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }


    @Test
    public void testFileImportAndFind() {
        String baseDir = System.getProperty("basedir");
        String filesPath = baseDir + "/target/test-classes/testdata/macrofiles/macro-output.xml";

        MacroFileImporter importer = new MacroFileImporter();
        try {
            List<LightMacro> macros = importer.importFile(new File(filesPath));

            System.out.println("Macro size " + macros.size());
            assertTrue("Macro size isn't 2 :(", macros.size() == 3);

            Set<String> targetMotifs = new HashSet<String>();
            for (LightMacro macro : macros) {
                targetMotifs.add(macro.getMotif());
            }

            MotifFinder finder = new TargetedMotifFinderImpl(targetMotifs);
            finder.performAnalysis(new DBGraph(startNode), new GraphMLCreator(new File("data/testFileImportAndFind.xml")));

            System.out.println("Motif size is " + finder.getMotifs().size());
            assertTrue("Found motif size is incorrect. ", finder.getMotifs().size() == 2);

            for (String motif : finder.getMotifs().keySet()) {
                System.out.printf("There are %d occurrences of %s\n", finder.getMotifs().get(motif).getCumulativeUsage(), motif);
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
}
