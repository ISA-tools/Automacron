package org.isatools.macros.motiffinder;

import org.isatools.macros.gui.DBGraph;
import org.isatools.macros.io.graphml.GraphMLCreator;
import org.isatools.macros.utils.MotifProcessingUtils;
import org.isatools.macros.utils.MotifStatCalculator;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.*;

/**
 * Created by the ISA team
 *
 * @author Eamonn Maguire (eamonnmag@gmail.com)
 *         <p/>
 *         Date: 28/11/2012
 *         Time: 11:42
 */
public class AlternativeCompleteMotifFinder extends CommonTraversalUtils implements MotifFinder {

    protected Map<String, Motif> motifs;
    protected Map<DBGraph, Set<String>> addedMotifsForWorkflowOccurrence;

    private int maxNodeThreshold;

    /**
     * @param maxNodeThreshold - the maximum number of nodes to form a motif on
     */
    public AlternativeCompleteMotifFinder(int maxNodeThreshold) {
        this(maxNodeThreshold, new HashMap<String, Motif>());
    }

    public AlternativeCompleteMotifFinder(int maxNodeThreshold, Map<String, Motif> motifs) {
        super();

        this.maxNodeThreshold = maxNodeThreshold;

        addedMotifsForWorkflowOccurrence = new HashMap<DBGraph, Set<String>>();
        addedMotifs = Collections.synchronizedSet(addedMotifs);
        this.motifs = motifs;
    }

    public Set<Long> getVisitedNodes() {
        return visitedNodes;
    }

    public void setMotifs(Map<String, Motif> motifs) {
        this.motifs = motifs;
    }

    /**
     * This will record the relations between two nodes, including the number of branch events that come from it.
     *
     * @param relationship - relationship to interrogate.
     * @param motif        - current motif to be passed through to the next level.
     * @return Returns current motif
     */
    protected void addMotif(DBGraph dbGraph, Relationship relationship, int depth, Motif motif, GraphMLCreator graphMLCreator) {
        Node originNode = relationship.getEndNode();
        Node targetNode = relationship.getStartNode();

        try {
            if (motif == null || depth <= maxNodeThreshold) {

                if (originNode.hasProperty("type")) {
                    // we really want to be calculating the number of nodes in versus nodes out.
                    Map<String, Set<Node>> outgoingRelationshipCount = calculateNumberOfRelationshipsInDirection(
                            relationship, originNode, targetNode, Direction.INCOMING, graphMLCreator);

                    // we want to check both the motif on its own and the motif in combination with others.
                    for (String key : outgoingRelationshipCount.keySet()) {
                        for (Node outgoingNode : outgoingRelationshipCount.get(key)) {
                            Motif subMotif = new Motif(originNode, relationship.getType(), outgoingNode);

                            if (motif == null) {
                                motif = subMotif;
                            } else {
                                motif.addSubMotif(subMotif);
                            }

                            subMotif.setDepth(depth);
                            addGraphMotif(dbGraph, subMotif);

                            Iterable<Relationship> relationshipsOfOriginNode = outgoingNode.getRelationships(Direction.INCOMING);
                            while (relationshipsOfOriginNode.iterator().hasNext()) {
                                Relationship relation = relationshipsOfOriginNode.iterator().next();
                                // we call this method recursively, returning only when the maximum outgoing Node
                                // depth for a motif is reached
                                int nextDepth = depth + 1;
                                addMotif(dbGraph, relation, nextDepth, subMotif, graphMLCreator);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            // skip over this error.
        }
    }

    private void addGraphMotif(DBGraph dbGraph, Motif motif) {
        graphMotifs.get(dbGraph).add(motif);
    }


    protected void processMyMotifs(DBGraph dbGraph) {
        for (Motif motif : graphMotifs.get(dbGraph)) {
            if (motif != null) {
                addToMotifs(dbGraph, motif);
            }
        }
        graphMotifs.remove(dbGraph);
    }

    protected void addToMotifs(DBGraph dbGraph, Motif motif) {

        // contains the general pattern
        // contains a more detail definition with node ids, so it is unique, Avoids adding the exact same motif twice,
        String uniqueMotifString = motif.getStringRepresentation();

        if (!addedMotifs.contains(uniqueMotifString)) {

            String motifBlockAsString = MotifProcessingUtils.findAndCollapseMergeEvents(uniqueMotifString);

            if (MotifProcessingUtils.isMotifGood(motifBlockAsString)) {
                Motif correspondingBlock = findMotifBlock(motifBlockAsString);

                if (correspondingBlock == null) {
                    if (!motifBlockAsString.isEmpty()) {
                        motifs.put(motifBlockAsString, motif);
                    }
                } else {
                    // correspondingBlock.addMotifsToBlock(motifBlock.getMotifs());
                    // get an id back from addition of motifBlock motifs indicating if new nodes have been added or not.
                    // only increment if no new nodes have been added
                    correspondingBlock.addRelatedMotif(motif);
                    correspondingBlock.incrementUsage();
                }

                if (!addedMotifsForWorkflowOccurrence.containsKey(dbGraph)) {
                    addedMotifsForWorkflowOccurrence.put(dbGraph, new HashSet<String>());
                }

                if (!addedMotifsForWorkflowOccurrence.get(dbGraph).contains(motifBlockAsString)) {
                    addedMotifsForWorkflowOccurrence.get(dbGraph).add(motifBlockAsString);
                    motifs.get(motifBlockAsString).incrementWorkflowOccurrence();
                }

                addedMotifs.add(uniqueMotifString);
                dbGraph.addAssociatedMotif(motifBlockAsString.hashCode());
            }
        }
    }

    protected void clearAddedMotifsForWorkflowOccurrence(DBGraph DBGraph) {
        addedMotifsForWorkflowOccurrence.remove(DBGraph);
    }

    /**
     * This checks to see if there is the exact same motif already in the recorded motifs.
     * This is performed through inspection of the nodes
     *
     * @return the matching block or null if it has not been found.
     */
    protected Motif findMotifBlock(String motifRepresentation) {
        return motifs.get(motifRepresentation);
    }


    // we can now find motifs within 2 nodes. Next, find motifs within n nodes. we need to recursively go through elements in
    // the graph and build the motif based on n steps.

    public Map<String, Motif> getMotifs() {
        return Collections.synchronizedMap(motifs);
    }

    protected void printMotifsAndStats() {

        try {
            PrintStream printStream = new PrintStream(new FileOutputStream("ProgramData/analysis-output.txt"));

            for (Motif motif : getMotifs().values()) {
                printStream.println(motif.toString());
            }

            printStream.close();

            System.out.println();
            System.out.println("Analysing motif getCumulativeUsage frequencies");
            System.out.println();
            MotifStatCalculator motifStatCalculator = new MotifStatCalculator();
            motifStatCalculator.analyseMotifs(getMotifs());
            System.out.println("There are " + getMotifs().size() + " motif blocks.");

            System.out.println("Finished analysis of neo4j db contents");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }


}