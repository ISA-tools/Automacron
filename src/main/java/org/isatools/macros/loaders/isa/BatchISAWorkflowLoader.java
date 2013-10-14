package org.isatools.macros.loaders.isa;

import au.com.bytecode.opencsv.CSVReader;
import org.isatools.macros.graph.graphloader.GraphFunctions;
import org.isatools.macros.graph.graphloader.Neo4JConnector;
import org.isatools.macros.gui.DBGraph;
import org.isatools.macros.io.graphml.GraphMLCreator;
import org.isatools.macros.loaders.GraphLoader;
import org.isatools.macros.loaders.isa.fileprocessing.isatab.ISAFileFlattener;
import org.isatools.macros.motiffinder.AlternativeCompleteMotifFinder;
import org.isatools.macros.motiffinder.Motif;
import org.isatools.macros.utils.MotifProcessingUtils;
import org.isatools.macros.utils.MotifStatCalculator;
import org.neo4j.graphdb.GraphDatabaseService;

import java.io.*;
import java.util.*;

/**
 * Created by the ISA team
 *
 * @author Eamonn Maguire (eamonnmag@gmail.com)
 *         <p/>
 *         Date: 16/07/2012
 *         Time: 13:19
 */
public class BatchISAWorkflowLoader implements GraphLoader {

    public static final String FILE_CONVERSION_DIR = System.getProperty("java.io.tmpdir") + "/converted";


    private Set<String> filesUnableToImport;

    private GraphDatabaseService graph;

    public BatchISAWorkflowLoader(GraphDatabaseService graph) {
        this.graph = graph;
        filesUnableToImport = new HashSet<String>();
    }

    /**
     * Flattens then loads those flattened files.
     *
     * @param loadingDirectory
     */
    public void loadFiles(File loadingDirectory) {
        // this will call the simple workflow loader on all the files that have been converted.
        Collection<File> flattenedFiles = flattenISAFiles(loadingDirectory);
        loadFlattenedFiles(flattenedFiles);
    }

    public void loadFlattenedFiles(File[] flattenedFiles) {
        ISAWorkflowLoader loader;
        int count = 1;
        int size = flattenedFiles.length;

        for (File isaFile : flattenedFiles) {
            System.out.println("Loading " + count + " of " + size + ": " + isaFile.getAbsolutePath() + "\r");
            loader = new ISAWorkflowLoader(graph);
            loader.loadFiles(isaFile);
            count++;
        }
    }

    public void loadFlattenedFiles(Collection<File> flattenedFiles) {
        ISAWorkflowLoader loader;
        int count = 1;
        int size = flattenedFiles.size();

        for (File isaFile : flattenedFiles) {
            System.out.println("Loading " + count + " of " + size + ": " + isaFile.getAbsolutePath() + "\r");
            loader = new ISAWorkflowLoader(graph);
            loader.loadFiles(isaFile);
            count++;
        }
    }


    private Collection<File> flattenISAFiles(File loadingDirectory) {

        if (topLevelContainsISAFiles(loadingDirectory)) {
            System.out.println("There is 1 ISATab Directory to flatten.");
            return ISAFileFlattener.flattenISATabFiles(loadingDirectory);
        } else {

            int numberOfFiles = loadingDirectory.listFiles().length;
            System.out.println("There are " + numberOfFiles + " files to flatten.");
            int count = 0;
            Collection<File> addedFiles = new ArrayList<File>();
            for (File isaFile : loadingDirectory.listFiles()) {
                if (!isaFile.isHidden() && !isaFile.getName().startsWith(".") && topLevelContainsISAFiles(isaFile)) {
                    addedFiles.addAll(ISAFileFlattener.flattenISATabFiles(isaFile));
                    count++;
                    System.out.printf("%d out of %d flattened!\r", count, numberOfFiles);
                } else {
                    filesUnableToImport.add(isaFile.getName());
                }
            }
            return addedFiles;
        }
    }

    private boolean topLevelContainsISAFiles(File directory) {
        try {
            return directory.exists() && directory.listFiles(new FileFilter() {
                public boolean accept(File file) {
                    return file != null && file.getName() != null && file.getName().startsWith("i_");
                }
            }).length > 0;
        } catch (Exception e) {
            System.err.printf("Uh oh, error occurred in %s, it was %s\n", BatchISAWorkflowLoader.class.getName(), e.getMessage());
            return false;
        }
    }

    public static void main(String[] args) {
        Neo4JConnector connector = new Neo4JConnector("/Users/eamonnmaguire/dev/neo4j-db/automacron.db");

        GraphDatabaseService graphDatabaseService = connector.getGraphDB();

//        BatchISAWorkflowLoader loader = new BatchISAWorkflowLoader(graphDatabaseService);
//        loader.loadFlattenedFiles(new File(ISAFileFlattener.FLATTENED_FILES_DIR).listFiles());

//        Map<String, GraphStats> stats = LoadingStatistics.getGraphStatsMap();
//
//        try {
//            PrintStream statsOutput = new PrintStream("ProgramData/stats.txt");
//
//            statsOutput.println("Graph Name\tEdge Count\tNode Count");
//
//            for (String graph : stats.keySet()) {
//                statsOutput.println(graph + "\t" + stats.get(graph).getEdgeCount() + "\t" + stats.get(graph).getVertexCount());
//            }
//        } catch (FileNotFoundException e) {
//            System.err.println("File for loading stats not found.");
//        }

        Set<String> badFiles = new HashSet<String>();
        try {
            CSVReader badFileReader = new CSVReader(new FileReader(new File("ProgramData/Bad-files.txt")));

            String[] nextLine;
            while ((nextLine = badFileReader.readNext()) != null) {
                badFiles.add(nextLine[0]);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        List<DBGraph> graphs = GraphFunctions.loadExperiments(graphDatabaseService);

        System.out.println("There are " + graphs.size());

        System.out.println("Starting analysis\r");

        Map<String, Motif> motifs = new HashMap<String, Motif>();

        int count = 1;
        int motifSize = 6;

        for (DBGraph graph : graphs) {
            if (count == 2100) {
                break;
            }
            System.out.println(String.format("Analysing %d of %d\r", count, graphs.size()));
            if (!badFiles.contains(graph.toString())) {

                long startTime = System.currentTimeMillis();

                System.out.println("Checking " + graph.toString());
                AlternativeCompleteMotifFinder motifFinder = new AlternativeCompleteMotifFinder(motifSize, motifs);
                motifFinder.performAnalysis(graph, new GraphMLCreator(new File("data/test.xml")));
                long endTime = System.currentTimeMillis();
                System.out.println(motifSize + "\t" + motifFinder.getMotifs().size() + "\t" + (endTime - startTime) + " ms");
            }
            count++;
        }

        System.out.println("All motifs found\r");
        System.out.println("Partitioning Results...\r");
        // we want numbers. How many motifs are of size k, how often did they appear, in how many workflows?
        Map<Integer, Set<String>> partitionedMap = new HashMap<Integer, Set<String>>();



        Map<String, Motif> motifsToKeep = new HashMap<String, Motif>();

        for (String motifString : motifs.keySet()) {
            int numberOfGroupsInMotifString = MotifProcessingUtils.getNumberOfGroupsInMotifString(motifString);

            if (numberOfGroupsInMotifString <= motifSize) {
                motifsToKeep.put(motifString, motifs.get(motifString));
                if (!partitionedMap.containsKey(numberOfGroupsInMotifString)) {
                    partitionedMap.put(numberOfGroupsInMotifString, new HashSet<String>());
                }

                motifsToKeep.get(motifString).setDepth(numberOfGroupsInMotifString);
                partitionedMap.get(numberOfGroupsInMotifString).add(motifString);
            }
        }

        System.out.println("Now we have " + motifsToKeep.size() + " motifs...");

        MotifStatCalculator calculator = new MotifStatCalculator();
        calculator.analyseMotifs(motifs);

        motifsToKeep = calculator.getOverRepresentedBlocks(motifsToKeep);

        System.out.println("Now we have " + motifsToKeep.size() + " motifs...");

        System.out.println("Partitioning Complete\r");

        System.out.println("Printing Results\r");
        // print partitions
        PrintStream ps = null;
        try {
            ps = new PrintStream("ProgramData/full-graph-count.txt");
            ps.println("Partition Size\tCount");
            for (Integer partition : partitionedMap.keySet()) {
                ps.println(partition + "\t" + partitionedMap.get(partition).size());
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            if (ps != null) {
                ps.close();
            }
        }
        System.out.println("Process complete\r");

        FileOutputStream fos;
        try {
            fos = new FileOutputStream("ProgramData/motifs.ser");

            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(motifsToKeep);
            oos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
