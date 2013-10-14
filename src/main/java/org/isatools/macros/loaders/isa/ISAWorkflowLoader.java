package org.isatools.macros.loaders.isa;

import au.com.bytecode.opencsv.CSVReader;
import org.apache.commons.collections15.map.ListOrderedMap;
import org.isatools.macros.loaders.GraphLoader;
import org.isatools.macros.loaders.isa.classifications.ClassificationReader;
import org.isatools.macros.loaders.stats.LoadingStatistics;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.Transaction;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by the ISA team
 *
 * @author Eamonn Maguire (eamonnmag@gmail.com)
 *         <p/>
 *         Date: 14/05/2012
 *         Time: 10:49
 */
public class ISAWorkflowLoader implements GraphLoader {

    public static final String SEPARATOR = "-";

    static {
        ClassificationReader.loadClassificationFiles();
    }

    private File toLoad;
    private GraphDatabaseService graph;

    // the same values should correspond to the same nodes, this makes it possible to create relationships between
    // nodes at different levels.
    private Map<String, Node> valueToNode;

    private Set<String> previouslyRecordedRelationships;
    private Node lastNode = null;

    public ISAWorkflowLoader(GraphDatabaseService graph) {

        this.graph = graph;
        valueToNode = new ListOrderedMap<String, Node>();
        previouslyRecordedRelationships = new HashSet<String>();
    }

    public void loadFiles(File toLoad) {
        this.toLoad = toLoad;

        int nodeCount = 0;
        int edgeCount = 0;

        Transaction tx = graph.beginTx();
        try {
            CSVReader reader = new CSVReader(new FileReader(toLoad), '\t');

            Node experimentNode = createExperimentNode();

            String[] columnHeaders = null;
            String[] nextLine;

            int count = 0;
            while ((nextLine = reader.readNext()) != null) {
                // we want to add each of the nodes to the graph. We need to hold nodes in memory so that we can form
                // relationships properly between different lines

                if (count == 0) {
                    columnHeaders = nextLine;
                } else {
                    for (int columnIndex = 0; columnIndex < columnHeaders.length; columnIndex++) {
                        String type = columnHeaders[columnIndex];
                        String value = nextLine[columnIndex];

                        boolean isProtocol = type.equals("Protocol REF") || type.equals("Label");

                        if (isProtocol) {
                            value = value + "." + lastNode.getProperty("value");
                        }

                        if (!isColumnToBeIgnored(type)) {

                            Node node;
                            // we should not just base a node being the same based on it's id. We should also look at the column
                            // type to make it more robust against duplication of values in different columns.
                            String key = type + value;

                            if (valueToNode.containsKey(key)) {
                                node = valueToNode.get(key);
                            } else {
                                node = graph.createNode();
                                nodeCount++;
                                node.setProperty("type", type);
                                node.setProperty("value", value);
                                valueToNode.put(key, node);
                            }

                            if (lastNode == null) {
                                String relationAsString = createUniqueRelationIdentifer(experimentNode, node, ISAWorkflowRelationships.SAMPLE_FOR);

                                if (!previouslyRecordedRelationships.contains(relationAsString)) {
                                    node.createRelationshipTo(experimentNode, ISAWorkflowRelationships.SAMPLE_FOR);
                                    previouslyRecordedRelationships.add(relationAsString);

                                    edgeCount++;
                                }

                            } else {
                                RelationshipType relationshipType = findRelationshipInRelationToLastNode(node);
                                String relationAsString = createUniqueRelationIdentifer(lastNode, node, relationshipType);
                                if (!previouslyRecordedRelationships.contains(relationAsString)) {
                                    node.createRelationshipTo(lastNode, findRelationshipInRelationToLastNode(node));
                                    previouslyRecordedRelationships.add(relationAsString);

                                    edgeCount++;
                                }
                            }
                            lastNode = node;
                        } else {
                            // we add annotations to the previous node. This is how things are done in the ISA graph too.
                            if (!value.trim().isEmpty()) {
                                lastNode.setProperty(type, value);
                            }
                        }
                    }
                }
                count++;
                lastNode = null;
            }

            // Post process nodes and add their image.
            postProcessNodes();

            LoadingStatistics.addStat(toLoad.getName(), nodeCount, edgeCount);

            tx.success();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        } finally {
            tx.finish();
        }
    }

    private void postProcessNodes() {
        String lastOrganism = "";
        String lastGranularity = "";

        String lastMaterialImage = "";

        for (Node node : valueToNode.values()) {
            boolean isProcessOrFile = node.getProperty("type").equals("Protocol REF") || isFile(node.getProperty("type").toString());

            Map<String, String> propertiesToAdd = ClassificationReader.getClassificationForTerm(node);
            for (String propertyKey : propertiesToAdd.keySet()) {
                String property = propertiesToAdd.get(propertyKey);

                // From the previous process, we can determine the granularity.
                // From the previous material, we can determine the organism and granularity.
                if (propertyKey.equals("image")) {
                    String nodeImageBasePath = property.substring(0, property.lastIndexOf(File.separator) + 1);
                    String nodeImageProperty = property.replace(nodeImageBasePath, "").replace(ClassificationReader.IMAGE_FILE_EXTENSION, "");

                    String[] fragmentedImage = nodeImageProperty.split("-");

                    if (isProcessOrFile) {
                        // do process stuff.
                        // in_vivo-material_amplification-organism.png

                        if (fragmentedImage.length == 3) {
                            // we have granularity
                            lastGranularity = fragmentedImage[2];
                        }

                        if (fragmentedImage.length == 4) {
                            // we have granularity
                            lastGranularity = fragmentedImage[3];
                        }
                    } else {
                        // do material stuff.
                        //biological-animalia-organ.png
                        if (fragmentedImage.length > 1) {
                            // we have the organism
                            if (!fragmentedImage[1].equals(lastOrganism) && !lastOrganism.equals("")) {
                                // here we resolve conflicts on the organism types by replacing conflicting organisms
                                // with the one seen before.
                                property = property.replaceAll(fragmentedImage[1], lastOrganism);
                            } else {
                                lastOrganism = fragmentedImage[1];
                            }
                        }

                        if (fragmentedImage.length == 1) {
                            if (!lastOrganism.isEmpty() && nodeImageProperty.startsWith("biological")) {

                                if (!nodeImageProperty.contains(lastOrganism)) {
                                    nodeImageProperty += SEPARATOR + lastOrganism;
                                }

                                if (!lastGranularity.isEmpty()) {
                                    if (!nodeImageProperty.contains(lastGranularity)) {
                                        nodeImageProperty += SEPARATOR + lastGranularity;
                                    }
                                }
                                property = nodeImageBasePath + nodeImageProperty + ClassificationReader.IMAGE_FILE_EXTENSION;
                            }
                        }
                        // if the current resolved image file doesn't exist, we replace it with the previous material file.
                        if (new File(property).exists()) {
                            if (!property.contains("chemical")) {
                                lastMaterialImage = property;
                            }
                        } else {
                            property = lastMaterialImage;
                        }
                    }


                    node.setProperty(propertyKey, property);

                    String taxonomyString = property.substring(property.lastIndexOf(File.separator) + 1).replace(ClassificationReader.IMAGE_FILE_EXTENSION, "");
                    taxonomyString = (isProcessOrFile ? taxonomyString.equals("process") ? "" : "process-" : "inputs_and_outputs-") + taxonomyString;
                    node.setProperty("taxonomy", taxonomyString);
                }


            }
        }
        // caching is cleared on the classification reader here since it's unlikely we'll 
        // see many duplicates across different graphs. 
        ClassificationReader.clearCache();
    }

    private String createUniqueRelationIdentifer(Node experimentNode, Node node, RelationshipType relationship) {
        return experimentNode.getProperty("value").toString() + relationship + node.getProperty("value").toString();
    }

    private boolean isFile(String columnName) {
        columnName = columnName.toLowerCase();
        return columnName.contains("file") || columnName.contains("scan name");
    }

    private boolean isColumnToBeIgnored(String columnName) {
        columnName = columnName.trim().toLowerCase();
        return (columnName.trim().isEmpty() || columnName.contains("characteristics") || columnName.contains("factor value") || columnName.contains("comment")
                || columnName.contains("unit") || columnName.contains("parameter value") || columnName.contains("material type")
                || columnName.contains("design ref"));
    }

    private RelationshipType findRelationshipInRelationToLastNode(Node currentNode) {
        String type = currentNode.getProperty("type").toString();

        if (isColumnToBeIgnored(type)) {
            return ISAWorkflowRelationships.PROPERTY_OF;
        } else if (type.equals("Protocol REF")) {
            return ISAWorkflowRelationships.TRANSFORMED_BY;
        } else {
            return ISAWorkflowRelationships.DERIVES;
        }
    }

    private Node createExperimentNode() {
        Node experimentNode = graph.createNode();
        experimentNode.setProperty("type", "start");
        experimentNode.setProperty("value", toLoad.getName());
        experimentNode.setProperty("image", ClassificationReader.MATERIALS_GLYPHS + "experiment" + ClassificationReader.IMAGE_FILE_EXTENSION);
        experimentNode.createRelationshipTo(graph.getReferenceNode(), ISAWorkflowRelationships.EXPERIMENT_OF);
        return experimentNode;
    }
}
