package org.isatools.macros.benchmarking;

import com.google.common.collect.MapDifference;
import com.google.common.collect.Maps;
import commons.TestUtils;
import org.apache.commons.collections15.multimap.MultiHashMap;
import org.geneontology.oboedit.test.TestUtil;
import org.isatools.macros.graph.graphloader.Neo4JConnector;
import org.isatools.macros.gui.DBGraph;
import org.isatools.macros.io.graphml.GraphMLCreator;
import org.isatools.macros.loaders.isa.ISAWorkflowLoader;
import org.isatools.macros.motiffinder.AlternativeCompleteMotifFinder;
import org.isatools.macros.motiffinder.Motif;
import org.isatools.macros.motiffinder.MotifFinder;
import org.isatools.macros.utils.MotifProcessingUtils;
import org.junit.Test;
import org.neo4j.cypher.ExecutionEngine;
import org.neo4j.cypher.ExecutionResult;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.Date;
import java.util.Map;

/**
 * Created by the ISA team
 *
 * @author Eamonn Maguire (eamonnmag@gmail.com)
 *         <p/>
 *         Date: 27/11/2012
 *         Time: 19:30
 */
public class BenchMarks extends TestUtils {

    public static final int MAX_MOTIF_SIZE = 11;

    // we have to do this a number of times to get a good idea of the mean.
    public static final int ITERATIONS = 5;

    @Test
    public void benchmarkTest() throws FileNotFoundException {

        System.out.println("\nBenchmark test.\n");

        File datasetDirectory = new File("ProgramData/Test/Datasets/");

        PrintStream printStream = new PrintStream(new File("ProgramData/benchmark-results.txt"));
        PrintStream summaryPrintStream = new PrintStream(new File("ProgramData/benchmark-results-summary.txt"));

        summaryPrintStream.println("Run on " + new Date(System.currentTimeMillis()).toString());

        for (File dataset : datasetDirectory.listFiles()) {

            printStream.println(dataset.getName());
            Neo4JConnector connector = loadGraph(dataset);

            printGraphInfo(connector, printStream);
            printGraphInfo(connector, summaryPrintStream);

            Node startNode = connector.getGraphDB().getNodeById(0);
            printResultTableHeader(printStream);
            printResultTableHeader(summaryPrintStream);
            // we do the analysis for different sized motifs.
            for (int motifSize = 2; motifSize < MAX_MOTIF_SIZE; motifSize++) {
                int runningTotal = 0;
                for (int iterationCount = 0; iterationCount < ITERATIONS; iterationCount++) {
                    long startTime = System.currentTimeMillis();
                    AlternativeCompleteMotifFinder motifFinder = new AlternativeCompleteMotifFinder(motifSize);
                    motifFinder.performAnalysis(new DBGraph(startNode), new GraphMLCreator(new File("data/test.xml")));
                    long endTime = System.currentTimeMillis();
                    runningTotal += (endTime - startTime);
                    if (motifSize != 2) {
                        printRecord(motifSize, iterationCount, motifFinder, endTime - startTime, printStream);
                    }
                }
                if (motifSize != 2) {
                    printStream.println("\t\t\t" + (runningTotal / (ITERATIONS)));
                    summaryPrintStream.println(motifSize + "\t" + (runningTotal / (ITERATIONS)));
                }

                printStream.println();
            }
            summaryPrintStream.println();
            connector.getGraphDB().shutdown();
        }

        printStream.close();
        summaryPrintStream.close();
    }

    private void printGraphInfo(Neo4JConnector connector, PrintStream ps) {
        ExecutionEngine engine = new ExecutionEngine(connector.getGraphDB());
        ExecutionResult result = engine.execute("start n=node(*) return count(*)");

        ps.println("Vertices: " + result.dumpToString());

        result = engine.execute("start r=rel(*) return count(*)");

        ps.println("Edges: " + result.dumpToString());
    }

    private void printRecord(int motifSize, int iterationCount, MotifFinder motifFinder, long time, PrintStream ps) {
        Map<String, Motif> motifs = motifFinder.getMotifs();

        ps.println(motifSize + "\t" + motifs.size() + "\t" + (iterationCount + 1) + "\t" + time + " ms");

    }

    private void printResultTableHeader(PrintStream ps) {
        ps.println("Motif Size\t# Motifs Found\tIteration Count\tTime taken (ms)");
    }


    private void printDifference(int record1, int record2, Map<Integer, Map<String, Motif>> motifs) {

        Map<String, Motif> record1Map = motifs.get(record1);
        Map<String, Motif> record2Map = motifs.get(record2);

        MapDifference<String, Motif> difference = Maps.difference(record1Map, record2Map);

        System.out.println("Difference between " + record1 + " and " + record2 + "...");
        for (String key : difference.entriesDiffering().keySet()) {
            System.out.println("\t" + key);
        }

    }

    private void printMotifs(Map<String, Motif> motifs) {
        System.out.println(String.format("Found %d motifs.", motifs.size()));
        for (String motif : motifs.keySet()) {
            System.out.println(String.format("%s -> #%d (%d)", motif, motifs.get(motif).getCumulativeUsage(), MotifProcessingUtils.getNumberOfGroupsInMotifString(motif)));
        }
    }

    private MultiHashMap<Integer, String> partitionMotifsByLength(Map<String, Motif> motifs) {
        MultiHashMap<Integer, String> partitionedMotifMap = new MultiHashMap<Integer, String>();

        for (String key : motifs.keySet()) {
            partitionedMotifMap.put(MotifProcessingUtils.getNumberOfGroupsInMotifString(key), key);
        }

        return partitionedMotifMap;
    }


}
