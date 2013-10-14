package org.isatools.macros.loaders.isa.classifications;


import au.com.bytecode.opencsv.CSVReader;
import org.apache.commons.collections15.set.ListOrderedSet;
import org.isatools.macros.utils.fuzzymap.FuzzyHashMap;
import org.neo4j.graphdb.Node;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;

/**
 * Created by the ISA team
 *
 * @author Eamonn Maguire (eamonnmag@gmail.com)
 *         <p/>
 *         Date: 23/07/2012
 *         Time: 09:02
 */
public class ClassificationReader {

    public static final String PROCESSES = "/classifications/processes.txt";
    public static final String MATERIALS = "/classifications/materials.txt";

    public static final String PROCESSES_GLYPHS = "ProgramData/images/glyphs/processes/";
    public static final String MATERIALS_GLYPHS = "ProgramData/images/glyphs/materials/";

    public static final int COLUMN_HEADER_INDEX = 1;
    public static final int TERM_INDEX = 0;
    public static final int START_DATA_INDEX = 2;

    private static final int FUZZINESS = 6;

    // cache for the results, since it's common to find, within one workflow many similar nodes.
    private static Map<String, Map<String, String>> cache = new HashMap<String, Map<String, String>>();

    private static Map<String, String> classificationTaxonomy;
    private static Map<String, FuzzyHashMap<String, Set<String>>> classifications;
    public static final String IMAGE_FILE_EXTENSION = ".png";

    public static void loadClassificationFiles() {
        classifications = new Hashtable<String, FuzzyHashMap<String, Set<String>>>();
        classificationTaxonomy = new HashMap<String, String>();
        try {
            loadClassificationFile(MATERIALS);
            loadClassificationFile(PROCESSES);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void loadClassificationFile(String toLoad) throws IOException {

        InputStream classificationFile = ClassificationReader.class.getResourceAsStream(toLoad);
        CSVReader csvReader = new CSVReader(new InputStreamReader(classificationFile), '\t');
        List<String[]> readClassificationFile = csvReader.readAll();

        classifications.put(toLoad, new FuzzyHashMap<String, Set<String>>(FuzzyHashMap.PRE_HASHING_METHOD.SOUNDEX));
        try {
            String taxonomyInformation = readClassificationFile.get(0)[0];
            classificationTaxonomy.put(toLoad, taxonomyInformation);
        } catch (NullPointerException nfe) {
            nfe.printStackTrace();
        }

        String[] columnHeaders = readClassificationFile.get(COLUMN_HEADER_INDEX);

        for (int rowIndex = START_DATA_INDEX; rowIndex < readClassificationFile.size(); rowIndex++) {
            String[] row = readClassificationFile.get(rowIndex);

            if (row.length > 1) {
                classifications.get(toLoad).putFuzzy(row[TERM_INDEX], new HashSet<String>());

                for (int columnIndex = 2; columnIndex < row.length; columnIndex++) {
                    if (row[columnIndex].equals("1")) {
                        // we add the classification string
                        classifications.get(toLoad).getFuzzy(row[TERM_INDEX]).add(columnHeaders[columnIndex]);
                    }
                }
            }
        }
    }

    /**
     * Returns a string representation of the taxonomy to be used for both recovery of the glyph for the term
     * and to display the taxonomy of the term itself
     *
     * @param node
     * @return String representing the taxonomy for recovery of the image for the glyph.
     */
    public static Map<String, String> getClassificationForTerm(Node node) {

        String nodeType = node.getProperty("type").toString();
        String nodeValue = node.getProperty("value").toString();

        // return here if there is something in the cache
        if (cache.containsKey(nodeValue)) {
            return cache.get(nodeValue);
        }

        Set<String> nodeValues;

        Map<String, String> imageAndTaxonomyToValue = new HashMap<String, String>();

        // we have different types of files we have to take in to consideration. Bio Materials, Data files (raw, processed and analysed), 
        // Label & Protocols
        String type = isProcess(nodeType) ? PROCESSES : MATERIALS;

        String[] taxonomyOrder = classificationTaxonomy.get(type).split(":");

        // we use node values as a Set since we require all protocols in the chain.
        nodeValues = type.contains(PROCESSES) ? processProtocol(nodeValue) : Collections.singleton(nodeValue);

        StringBuilder classificationString = inferImageTypeFromColumnName(nodeType);

        Set<String> classificationsForTerm = null;

        for (String candidateNodeValue : nodeValues) {
            classificationsForTerm = classifications.get(type).getFuzzy(candidateNodeValue, FUZZINESS);
            if (classificationsForTerm != null) break;
        }

        if (type.equals(MATERIALS)) {
            for (Object property : node.getPropertyKeys()) {
                String propertyType = property.toString();
                if (!propertyType.equals("value") && !propertyType.equals("type") && !propertyType.equals("image")) {
                    Set<String> candidateClassification;

                    String propertyToCheck = node.getProperty(propertyType).toString();

                    if (!propertyToCheck.isEmpty()) {

                        if (propertyToCheck.contains(":")) {
                            propertyToCheck = propertyToCheck.substring(propertyToCheck.indexOf(":") + 1);
                        }

                        if (propertyToCheck.contains("(")) {
                            propertyToCheck = propertyToCheck.substring(0, propertyToCheck.indexOf("("));
                        }

                        if ((candidateClassification = classifications.get(type).getFuzzy(propertyToCheck.trim())) != null) {
                            classificationsForTerm = candidateClassification;
                        }
                    }
                }
            }
        }

        String classificationTaxonomy;
        if (classificationsForTerm != null && classificationString.toString().isEmpty()) {
            for (String taxonomyLevel : taxonomyOrder) {
                // now we look in the available classifications for this term and order them.
                for (String classificationForTerm : classificationsForTerm) {
                    if (classificationForTerm.startsWith(taxonomyLevel)) {
                        classificationString.append(classificationForTerm.substring(classificationForTerm.indexOf(":") + 1).trim().replaceAll("\\s+", "_").toLowerCase()).append("-");
                    }
                }
            }
            classificationString = new StringBuilder(classificationString.toString().substring(0, classificationString.length() - 1));
            classificationTaxonomy = classificationString.toString();
        } else {
            classificationString = classificationString == null || classificationString.toString().isEmpty() ? type.equals(PROCESSES) ? new StringBuilder("process") : new StringBuilder("biological") : classificationString;
            classificationTaxonomy = classificationString.toString();
        }

        imageAndTaxonomyToValue.put("taxonomy", classificationTaxonomy);
        imageAndTaxonomyToValue.put("image", constructImageURI(type, classificationString.toString()));

        cache.put(nodeValue, imageAndTaxonomyToValue);

        return imageAndTaxonomyToValue;
    }

    private static boolean isProcess(String nodeType) {
        String lowerCaseNode = nodeType.toLowerCase();
        return lowerCaseNode.contains("protocol ref") || lowerCaseNode.contains("transformation") ||
                lowerCaseNode.contains("norm") || lowerCaseNode.contains("scan");
    }

    private static String constructImageURI(String type, String value) {
        StringBuilder imageURI = new StringBuilder();
        if (type.equals(PROCESSES)) {
            imageURI.append(PROCESSES_GLYPHS);
        } else {
            imageURI.append(MATERIALS_GLYPHS);
        }

        imageURI.append(value);
        imageURI.append(IMAGE_FILE_EXTENSION);

        return imageURI.toString();
    }

    private static StringBuilder inferImageTypeFromColumnName(String type) {

        // todo extract these to an enumeration which can be passed through for inference.
        String lowerCaseType = type.toLowerCase();

        if (lowerCaseType.equals("label")) {
            return new StringBuilder("chemical_entity");
        }

        if (lowerCaseType.contains("derived") || lowerCaseType.contains("processed")) {
            return new StringBuilder("data-processed_data");
        }

        if (lowerCaseType.contains("transformation") || lowerCaseType.contains("norm") || lowerCaseType.contains("feature")) {
            return new StringBuilder("in_silico-data_analysis");
        }

        if (lowerCaseType.contains("scan")) {
            return new StringBuilder("in_silico-data_acquisition");
        }

        if (lowerCaseType.contains("hybridization") || lowerCaseType.contains("file") || lowerCaseType.contains("assay name")) {
            return new StringBuilder("data-raw_data");
        }

        if (lowerCaseType.contains("start")) {
            return new StringBuilder("experiment");
        }

        return new StringBuilder();
    }

    private static Set<String> processProtocol(String protocolToProcess) {

        String[] protocols = protocolToProcess.split("\\.");

        Set<String> protocolResult = new ListOrderedSet<String>();
        if (protocols.length > 1) {
            protocolResult.addAll(Arrays.asList(protocols).subList(0, protocols.length - 1));
        }

        return protocolResult;
    }

    public static void clearCache() {
        cache.clear();
    }

}
