package org.isatools.macros.motiffinder;

import org.isatools.macros.graph.graphloader.Neo4JConnector;
import org.isatools.macros.gui.DBGraph;
import org.isatools.macros.io.graphml.GraphMLCreator;
import org.isatools.macros.utils.MotifProcessingUtils;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.Transaction;

import java.io.File;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static junit.framework.Assert.assertTrue;

/**
 * Created by the ISA team
 *
 * @author Eamonn Maguire (eamonnmag@gmail.com)
 *         <p/>
 *         Date: 23/10/2012
 *         Time: 13:10
 */
public class MotifFinderTest {

    Node mixedRelationTestNode;
    Node sameRelationTestNode;
    Node sameRelationTestNodeMoreComplex;
    Node linearTestNode;
    Node mergeTestNode;
    Node complicatedCombinationTestCaseNode;
    Node largeBranchAndMergeTestNode;
    Node largeBranchingTestNode;

    private static Neo4JConnector connector = new Neo4JConnector();

    @Before
    public void setUp() throws Exception {

        GraphDatabaseService graphDatabaseService = connector.getGraphDB();
        Transaction tx = graphDatabaseService.beginTx();
        try {
            createMixedRelationTestNode(graphDatabaseService);
            createSameRelationTestCase(graphDatabaseService);
            createComplexSameRelationTestCase(graphDatabaseService);
            createLinearRelationTestCase(graphDatabaseService);
            createMergeRelationTestCase(graphDatabaseService);
            createComplicatedCombinationTestCase(graphDatabaseService);
            createLargeBranchAndMergeTestCase(graphDatabaseService);
            createLargeBranchingTestCase(graphDatabaseService);
            tx.success();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            tx.finish();
        }
    }

    private void createLinearRelationTestCase(GraphDatabaseService graphDatabaseService) {

        linearTestNode = createNode(graphDatabaseService, "start", "root");

        Node nodeA = createNode(graphDatabaseService, "typeA", "Value1");
        Node nodeB = createNode(graphDatabaseService, "typeB", "Value2");
        Node nodeC = createNode(graphDatabaseService, "typeC", "Value3");

        nodeA.createRelationshipTo(linearTestNode, TestRelationshipTypes.REL_A);
        nodeB.createRelationshipTo(nodeA, TestRelationshipTypes.REL_B);
        nodeC.createRelationshipTo(nodeB, TestRelationshipTypes.REL_B);
    }

    private void createMergeRelationTestCase(GraphDatabaseService graphDatabaseService) {

        mergeTestNode = createNode(graphDatabaseService, "start", "root");

        Node nodeA = createNode(graphDatabaseService, "typeA", "Value1");
        Node nodeB = createNode(graphDatabaseService, "typeB", "Value2");
        Node nodeC = createNode(graphDatabaseService, "typeC", "Value3");
        Node nodeD = createNode(graphDatabaseService, "typeD", "Value4");

        nodeA.createRelationshipTo(mergeTestNode, TestRelationshipTypes.REL_A);
        nodeB.createRelationshipTo(nodeA, TestRelationshipTypes.REL_B);
        nodeC.createRelationshipTo(nodeA, TestRelationshipTypes.REL_B);

        nodeD.createRelationshipTo(nodeB, TestRelationshipTypes.REL_A);
        nodeD.createRelationshipTo(nodeC, TestRelationshipTypes.REL_A);
    }

    private void createMixedRelationTestNode(GraphDatabaseService graphDatabaseService) {
        mixedRelationTestNode = createNode(graphDatabaseService, "start", "root");

        Node nodeA = createNode(graphDatabaseService, "typeA", "Value1");
        Node nodeB = createNode(graphDatabaseService, "typeB", "Value2");
        Node nodeC = createNode(graphDatabaseService, "typeC", "Value3");

        nodeA.createRelationshipTo(mixedRelationTestNode, TestRelationshipTypes.REL_A);
        nodeB.createRelationshipTo(nodeA, TestRelationshipTypes.REL_A);
        nodeC.createRelationshipTo(nodeA, TestRelationshipTypes.REL_B);
    }


    private void createSameRelationTestCase(GraphDatabaseService graphDatabaseService) {

        sameRelationTestNode = createNode(graphDatabaseService, "start", "root");

        Node nodeA = createNode(graphDatabaseService, "typeA", "Value1");
        Node nodeB = createNode(graphDatabaseService, "typeB", "Value2");
        Node nodeC = createNode(graphDatabaseService, "typeB", "Value3");

        nodeA.createRelationshipTo(sameRelationTestNode, TestRelationshipTypes.REL_A);
        nodeB.createRelationshipTo(nodeA, TestRelationshipTypes.REL_B);
        nodeC.createRelationshipTo(nodeA, TestRelationshipTypes.REL_B);
    }

    private void createComplexSameRelationTestCase(GraphDatabaseService graphDatabaseService) {

        sameRelationTestNodeMoreComplex = createNode(graphDatabaseService, "start", "root");

        Node nodeA = createNode(graphDatabaseService, "typeA", "Value1");
        Node nodeB = createNode(graphDatabaseService, "typeB", "Value2");
        Node nodeBB = createNode(graphDatabaseService, "typeB", "Value3");
        Node nodeC = createNode(graphDatabaseService, "typeC", "Value3");
        Node nodeD = createNode(graphDatabaseService, "typeD", "Value4");
        Node nodeD2 = createNode(graphDatabaseService, "typeD", "Value5");
        Node nodeE = createNode(graphDatabaseService, "typeE", "Value6");

        nodeA.createRelationshipTo(sameRelationTestNodeMoreComplex, TestRelationshipTypes.REL_A);
        nodeBB.createRelationshipTo(nodeA, TestRelationshipTypes.REL_A);
        nodeB.createRelationshipTo(nodeA, TestRelationshipTypes.REL_A);
        nodeC.createRelationshipTo(nodeA, TestRelationshipTypes.REL_A);
        nodeD.createRelationshipTo(nodeC, TestRelationshipTypes.REL_A);
        nodeD2.createRelationshipTo(nodeC, TestRelationshipTypes.REL_A);
        nodeE.createRelationshipTo(nodeD2, TestRelationshipTypes.REL_B);
    }

    private void createComplicatedCombinationTestCase(GraphDatabaseService graphDatabaseService) {
        complicatedCombinationTestCaseNode = createNode(graphDatabaseService, "start", "root");

        Node nodeA = createNode(graphDatabaseService, "typeA", "Value1");
        Node nodeB = createNode(graphDatabaseService, "typeB", "Value2");
        Node nodeB2 = createNode(graphDatabaseService, "typeB", "Value3");

        // B branch
        Node nodeC = createNode(graphDatabaseService, "typeC", "Value3");
        Node nodeC2 = createNode(graphDatabaseService, "typeC", "Value4");
        Node nodeD = createNode(graphDatabaseService, "typeD", "Value4");
        Node nodeD2 = createNode(graphDatabaseService, "typeD", "Value5");
        Node nodeE = createNode(graphDatabaseService, "typeE", "Value4");

        Node nodeF = createNode(graphDatabaseService, "typeE", "Value4");

        nodeA.createRelationshipTo(complicatedCombinationTestCaseNode, TestRelationshipTypes.REL_A);
        nodeB.createRelationshipTo(nodeA, TestRelationshipTypes.REL_A);
        nodeB2.createRelationshipTo(nodeA, TestRelationshipTypes.REL_A);

        nodeC.createRelationshipTo(nodeB, TestRelationshipTypes.REL_A);
        nodeC2.createRelationshipTo(nodeB, TestRelationshipTypes.REL_A);

        nodeD.createRelationshipTo(nodeC, TestRelationshipTypes.REL_A);
        nodeD2.createRelationshipTo(nodeC2, TestRelationshipTypes.REL_A);

        nodeE.createRelationshipTo(nodeD, TestRelationshipTypes.REL_A);
        nodeE.createRelationshipTo(nodeD2, TestRelationshipTypes.REL_A);

        nodeF.createRelationshipTo(nodeB2, TestRelationshipTypes.REL_B);
    }

    private void createLargeBranchAndMergeTestCase(GraphDatabaseService graphDatabaseService) {
        largeBranchAndMergeTestNode = createNode(graphDatabaseService, "start", "root");

        Node nodeA = createNode(graphDatabaseService, "typeA", "Value1");
        Node nodeB1 = createNode(graphDatabaseService, "typeB", "Value2");
        Node nodeB2 = createNode(graphDatabaseService, "typeB", "Value3");
        Node nodeB3 = createNode(graphDatabaseService, "typeB", "Value4");
        Node nodeB4 = createNode(graphDatabaseService, "typeB", "Value5");
        Node nodeB5 = createNode(graphDatabaseService, "typeB", "Value6");
        Node nodeB6 = createNode(graphDatabaseService, "typeB", "Value7");

        // B branch
        Node nodeC = createNode(graphDatabaseService, "typeC", "Value3");

        nodeA.createRelationshipTo(largeBranchAndMergeTestNode, TestRelationshipTypes.REL_A);

        nodeB1.createRelationshipTo(nodeA, TestRelationshipTypes.REL_A);
        nodeB2.createRelationshipTo(nodeA, TestRelationshipTypes.REL_A);
        nodeB3.createRelationshipTo(nodeA, TestRelationshipTypes.REL_A);
        nodeB4.createRelationshipTo(nodeA, TestRelationshipTypes.REL_A);
        nodeB5.createRelationshipTo(nodeA, TestRelationshipTypes.REL_A);
        nodeB6.createRelationshipTo(nodeA, TestRelationshipTypes.REL_A);

        nodeC.createRelationshipTo(nodeB1, TestRelationshipTypes.REL_A);
        nodeC.createRelationshipTo(nodeB2, TestRelationshipTypes.REL_A);
        nodeC.createRelationshipTo(nodeB3, TestRelationshipTypes.REL_A);
        nodeC.createRelationshipTo(nodeB4, TestRelationshipTypes.REL_A);
        nodeC.createRelationshipTo(nodeB5, TestRelationshipTypes.REL_A);
        nodeC.createRelationshipTo(nodeB6, TestRelationshipTypes.REL_A);
    }

    private void createLargeBranchingTestCase(GraphDatabaseService graphDatabaseService) {

        largeBranchingTestNode = createNode(graphDatabaseService, "start", "root");

        Node nodeA = createNode(graphDatabaseService, "typeA", "Value1");
        Node nodeB = createNode(graphDatabaseService, "typeB", "Value2");
        Node nodeC = createNode(graphDatabaseService, "typeC", "Value3");
        Node nodeD = createNode(graphDatabaseService, "typeD", "Value4");
        Node nodeE = createNode(graphDatabaseService, "typeE", "Value5");
        Node nodeF = createNode(graphDatabaseService, "typeF", "Value6");
        Node nodeF1 = createNode(graphDatabaseService, "typeF", "Value7");
        Node nodeF2 = createNode(graphDatabaseService, "typeF", "Value8");
        Node nodeF3 = createNode(graphDatabaseService, "typeF", "Value9");
        Node nodeF4 = createNode(graphDatabaseService, "typeF", "Value10");
        Node nodeF5 = createNode(graphDatabaseService, "typeF", "Value11");

        Node nodeG = createNode(graphDatabaseService, "typeG", "Value6");
        Node nodeG1 = createNode(graphDatabaseService, "typeG", "Value7");
        Node nodeG2 = createNode(graphDatabaseService, "typeG", "Value8");
        Node nodeG3 = createNode(graphDatabaseService, "typeG", "Value9");
        Node nodeG4 = createNode(graphDatabaseService, "typeG", "Value10");
        Node nodeG5 = createNode(graphDatabaseService, "typeG", "Value11");

        Node nodeH = createNode(graphDatabaseService, "typeH", "Value7");
        Node nodeI = createNode(graphDatabaseService, "typeI", "Value7");

        nodeA.createRelationshipTo(largeBranchingTestNode, TestRelationshipTypes.REL_B);
        nodeB.createRelationshipTo(nodeA, TestRelationshipTypes.REL_B);
        nodeC.createRelationshipTo(nodeB, TestRelationshipTypes.REL_B);
        nodeD.createRelationshipTo(nodeC, TestRelationshipTypes.REL_B);
        nodeE.createRelationshipTo(nodeD, TestRelationshipTypes.REL_B);

        nodeF.createRelationshipTo(nodeE, TestRelationshipTypes.REL_B);
        nodeF1.createRelationshipTo(nodeE, TestRelationshipTypes.REL_B);
        nodeF2.createRelationshipTo(nodeE, TestRelationshipTypes.REL_B);
        nodeF3.createRelationshipTo(nodeE, TestRelationshipTypes.REL_B);
        nodeF4.createRelationshipTo(nodeE, TestRelationshipTypes.REL_B);
        nodeF5.createRelationshipTo(nodeE, TestRelationshipTypes.REL_B);

        nodeG.createRelationshipTo(nodeF, TestRelationshipTypes.REL_B);
        nodeG1.createRelationshipTo(nodeF1, TestRelationshipTypes.REL_B);
        nodeG2.createRelationshipTo(nodeF2, TestRelationshipTypes.REL_B);
        nodeG3.createRelationshipTo(nodeF3, TestRelationshipTypes.REL_B);
        nodeG4.createRelationshipTo(nodeF4, TestRelationshipTypes.REL_B);
        nodeG5.createRelationshipTo(nodeF5, TestRelationshipTypes.REL_B);

        nodeH.createRelationshipTo(nodeG, TestRelationshipTypes.REL_B);
        nodeH.createRelationshipTo(nodeG1, TestRelationshipTypes.REL_B);
        nodeH.createRelationshipTo(nodeG2, TestRelationshipTypes.REL_B);
        nodeH.createRelationshipTo(nodeG3, TestRelationshipTypes.REL_B);
        nodeH.createRelationshipTo(nodeG4, TestRelationshipTypes.REL_B);
        nodeH.createRelationshipTo(nodeG5, TestRelationshipTypes.REL_B);

        nodeI.createRelationshipTo(nodeH, TestRelationshipTypes.REL_B);
    }

    private Node createNode(GraphDatabaseService graphDatabaseService, String type, String value) {
        Node nodeE = graphDatabaseService.createNode();
        nodeE.setProperty("type", type);
        nodeE.setProperty("value", value);
        return nodeE;
    }

    @Test
    public void testRelationshipsOfDifferentTypesForMotif() {

        MotifFinder motifFinder = new AlternativeCompleteMotifFinder(4);
        motifFinder.performAnalysis(new DBGraph(mixedRelationTestNode), new GraphMLCreator(new File("data/test.xml")));

        System.out.println(mixedRelationTestNode.getRelationships().iterator().next().getType());

        Map<String, Motif> motifs = motifFinder.getMotifs();

        printMotifs(motifs);

        System.out.println(motifs.size());

        assertTrue("Awww, motif size not as expected", motifs.size() == 3);
    }

    private void printMotifs(Map<String, Motif> motifs) {
        System.out.println("In printmotifs()");
        System.out.println(String.format("Found %d motifs.", motifs.size()));
        for (String motif : motifs.keySet()) {
            System.out.println(String.format("%s -> #%d (%d)", motif, motifs.get(motif).getCumulativeUsage(),
                    MotifProcessingUtils.getNumberOfGroupsInMotifString(motif)));
        }
    }

    @Test
    public void testRelationshipsOfSameTypeForMotif() {

        MotifFinder motifFinder = new AlternativeCompleteMotifFinder(4);
        motifFinder.performAnalysis(new DBGraph(sameRelationTestNode), new GraphMLCreator(new File("data/test.xml")));

        Map<String, Motif> motifs = motifFinder.getMotifs();

        printMotifs(motifs);

        System.out.println(motifs.size());

        assertTrue("Awww, motif size not as expected", motifs.size() == 3);
    }

    @Test
    public void testRelationshipsOfSameTypeForMotifMoreComplex() {

        MotifFinder motifFinder = new AlternativeCompleteMotifFinder(8);
        motifFinder.performAnalysis(new DBGraph(sameRelationTestNodeMoreComplex), new GraphMLCreator(new File("data/test.xml")));

        Map<String, Motif> motifs = motifFinder.getMotifs();

        printMotifs(motifs);

        assertTrue("Awww, motif size not as expected.", motifs.size() == 8);
    }

    @Test
    public void testPatternFindLinear() {
        Set<String> targets = new HashSet<String>();
        targets.add("typeA:{REL_B#typeB}");

        TargetedMotifFinderImpl motifFinder = new TargetedMotifFinderImpl(targets);
        motifFinder.performAnalysis(new DBGraph(linearTestNode), new GraphMLCreator(new File("data/test.xml")));

        Map<String, Motif> motifs = motifFinder.getMotifs();

        printMotifs(motifs);

        assertTrue("Awww, motif size not as expected", motifs.size() == 1);
    }

    @Test
    public void testPatternFindMerge() {
        MotifFinder motifFinder = new AlternativeCompleteMotifFinder(8);
        motifFinder.performAnalysis(new DBGraph(mergeTestNode), new GraphMLCreator(new File("data/test.xml")));

        Map<String, Motif> motifs = motifFinder.getMotifs();

        printMotifs(motifs);
        assertTrue("Awww, motif size not as expected", motifs.size() == 7);
    }

    @Test
    public void testComplicatedCombination() {
        MotifFinder motifFinder = new AlternativeCompleteMotifFinder(4);
        motifFinder.performAnalysis(new DBGraph(complicatedCombinationTestCaseNode), new GraphMLCreator(new File("data/test.xml")));

        Map<String, Motif> motifs = motifFinder.getMotifs();

        System.out.println();
        printMotifs(motifs);
        assertTrue("Awww, motif size not as expected", motifs.size() == 12);
    }

    @Test
    public void testLargeBranchAndMergeTest() {
        AlternativeCompleteMotifFinder motifFinder = new AlternativeCompleteMotifFinder(4);
        motifFinder.performAnalysis(new DBGraph(largeBranchAndMergeTestNode), new GraphMLCreator(new File("data/test.xml")));

        Map<String, Motif> motifs = motifFinder.getMotifs();

        printMotifs(motifs);
        assertTrue("Awww, motif size not as expected", motifs.size() == 5);
    }

    @Test
    public void testLargeBranchingEvent() {
        MotifFinder motifFinder = new AlternativeCompleteMotifFinder(8);
        motifFinder.performAnalysis(new DBGraph(largeBranchingTestNode), new GraphMLCreator(new File("data/test.xml")));

        Map<String, Motif> motifs = motifFinder.getMotifs();

        System.out.println();

        printMotifs(motifs);

        assertTrue("Awww, motif size not as expected", motifs.size() == 22);
    }

    @Test
    public void testExampleISATab() {

    }

    enum TestRelationshipTypes implements RelationshipType {
        REL_A, REL_B
    }
}
