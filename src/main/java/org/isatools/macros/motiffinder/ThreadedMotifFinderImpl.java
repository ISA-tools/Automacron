package org.isatools.macros.motiffinder;

import org.isatools.macros.gui.DBGraph;
import org.isatools.macros.io.graphml.GraphMLCreator;
import org.isatools.macros.motiffinder.listeningutils.MotifFinderObserver;
import org.isatools.macros.motiffinder.listeningutils.MotifFinderSubject;
import org.isatools.macros.utils.MotifSelectionAlgorithm;
import org.isatools.macros.utils.MotifSelectionAlgorithmImpl;
import org.isatools.macros.utils.MotifStats;

import javax.swing.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by the ISA team
 *
 * @author Eamonn Maguire (eamonnmag@gmail.com)
 *         <p/>
 *         Date: 17/07/2012
 *         Time: 08:40
 */
public class ThreadedMotifFinderImpl extends AlternativeCompleteMotifFinder implements MotifFinderSubject {

    public static final String PROGRAM_DATA_GRAPHML = System.getProperty("java.io.tmpdir");
    private ExecutorService threadPool;

    List<MotifFinderObserver> motifFinderObservers;
    private final MotifSelectionAlgorithm motifSelectionAlgorithm;


    public ThreadedMotifFinderImpl(int maxNodeThreshold) {
        super(maxNodeThreshold);

        motifFinderObservers = new ArrayList<MotifFinderObserver>();
        this.threadPool = Executors.newFixedThreadPool(2);

        motifSelectionAlgorithm = new MotifSelectionAlgorithmImpl();
    }


    public void performAnalysis(List<DBGraph> dbGraphs) {
        int count = 0;
        MotifStats.setTotalWorkflows(dbGraphs.size());

        System.out.println("There are " + dbGraphs.size() + " graphs to be checked.");
        for (DBGraph dbGraph : dbGraphs) {

            if (observedExperiments.size() < dbGraphs.size()) {
                if (dbGraph.isUpdating()) {
                    threadPool.execute(new NodeAnalysisHandler(dbGraph, count == dbGraphs.size() - 1));
                }
            }
            count++;
        }
    }

    public void registerObserver(MotifFinderObserver motifFinderObserver) {
        motifFinderObservers.add(motifFinderObserver);
    }

    public void deregisterObserver(MotifFinderObserver motifFinderObserver) {
        motifFinderObservers.remove(motifFinderObserver);
    }

    public void notifyObservers(boolean lastGraph) {

        // only update if finished all analyses...
        if (lastGraph) {
            Thread motifSelectionThread = new Thread(new Runnable() {
                public void run() {
                    List<Motif> overRepresentedMotifs = motifSelectionAlgorithm.analyseMotifs(getMotifs());

                    System.out.println("There are " + getMotifs().size() + " motifs.");
                    Vector<Motif> topMotifs = new Vector<Motif>();

                    topMotifs.addAll(getMotifs().values());

                    if (overRepresentedMotifs.size() > 0) {
                        for (MotifFinderObserver motifFinderObserver : motifFinderObservers) {
                            motifFinderObserver.notifyOfEvent(topMotifs);
                        }
                    }

                    threadPool.shutdown();
                    observedExperiments.clear();
                }
            });

            motifSelectionThread.start();
        } else {
            System.out.println("Not reach last graph yet...");
        }
    }


    class NodeAnalysisHandler implements Runnable {

        private DBGraph dbGraph;
        private boolean lastGraph;

        NodeAnalysisHandler(DBGraph dbGraph, boolean lastGraph) {
            this.dbGraph = dbGraph;
            this.lastGraph = lastGraph;
        }

        public void run() {
            //
            System.out.println("Checking " + dbGraph.toString() + " -> " + dbGraph.getCorrespondingNodeInDB());
            performAnalysis(dbGraph, new GraphMLCreator(new File(PROGRAM_DATA_GRAPHML + dbGraph.toString() + ".xml")));
            dbGraph.setUpdating(false);
            System.out.println("Found " + motifs.size() + "motifs.");
            notifyObservers(lastGraph);
            clearAddedMotifsForWorkflowOccurrence(dbGraph);
        }
    }
}
