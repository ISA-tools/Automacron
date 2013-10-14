package org.isatools.macros.loaders.isa.fileprocessing.isatab;

import org.apache.commons.collections15.OrderedMap;
import org.apache.commons.collections15.map.ListOrderedMap;
import org.isatools.conversion.ArrayToListConversion;
import org.isatools.conversion.Converter;
import org.isatools.errorreporter.model.ErrorMessage;
import org.isatools.errorreporter.model.ISAFileErrorReport;
import org.isatools.isacreator.io.importisa.ISAtabFilesImporter;
import org.isatools.isacreator.model.Assay;
import org.isatools.isacreator.model.Investigation;
import org.isatools.isacreator.model.Protocol;
import org.isatools.isacreator.model.Study;
import org.isatools.manipulator.SpreadsheetManipulation;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.*;

/**
 * Will take all ISA records and collapse them in to one file to make the format amenable to the current workflow loader.
 *
 * @author Eamonn Maguire (eamonnmag@gmail.com)
 *         <p/>
 *         Date: 12/07/2012
 *         Time: 13:11
 */
public class ISAFileFlattener {

    private static final String CONFIGURATION_DIRECTORY = "Configurations/isaconfig-default_v2011-02-18/";
    public static final String FLATTENED_FILES_DIR = System.getProperty("java.io.tmpdir") + "/flattened";

    private static String[] sampleNameColumns;

    private static Map<String, String> protocolNameToTypes;

    static {
        File flattenedFilesDir = new File(FLATTENED_FILES_DIR);
        if (!flattenedFilesDir.exists()) {
            flattenedFilesDir.mkdir();
        }
    }

    public static Collection<File> flattenISATabFiles(File isatabDirectory) {
        // read in the isatab files

        try {
            Investigation investigation = importISATabFiles(isatabDirectory);
            if (investigation != null) {
                return flattenISATabFiles(isatabDirectory, investigation);
            }
        } catch (Exception exception) {
            System.err.println("Problem occurred when flattening files for " + isatabDirectory.getName());
            System.err.println(exception.getMessage());
        }
        return new ArrayList<File>();
    }

    public static List<File> flattenISATabFiles(File isatabDirectory, Investigation investigation) {
        return flattenISATabFiles(isatabDirectory, investigation, null);
    }

    public static List<File> flattenISATabFiles(File isatabDirectory, Investigation investigation, Set<String> sampleFilter) {
        List<File> flattenedFiles = new ArrayList<File>();
        if (investigation != null) {

            System.out.printf("Just imported %s with %d studies.\r", isatabDirectory.getName(), investigation.getStudies().size());

            createProtocolLookup(investigation);
            // for each study, create a merged representation of the information. Meaning, attach the sample information to each
            // corresponding row in the assay

            Converter<Object[][], List<String[]>> arrayToListConversion = new ArrayToListConversion();

            for (String studyId : investigation.getStudies().keySet()) {

                Study study = investigation.getStudies().get(studyId);
                if (study.getStudySample() != null && study.getStudySample().getTableReferenceObject() != null) {
                    List<String[]> studySampleRows = arrayToListConversion.convert(study.getStudySample().getAssayDataMatrix());

                    for (Assay assay : study.getAssays().values()) {
                        // strange to have to do this, but apparently I must. Otherwise assay.getAssayDataMatrix() fails with a null pointer.
                        if (assay.getTableReferenceObject() != null) {
                            OrderedMap<Integer, List<String>> merged = new ListOrderedMap<Integer, List<String>>();

                            Object[][] assayDataMatrix = assay.getAssayDataMatrix();
                            List<String[]> assayRows = arrayToListConversion.convert(assayDataMatrix);

                            performMergeAssayWithStudySample(merged, studySampleRows, assayRows, sampleFilter);

                            // now do output of file
                            File flattenedStudyFileName = constructFlattenedFileName(isatabDirectory, studyId + ":" + assay.getAssayReference());
                            printMergedInformationToFile(flattenedStudyFileName, merged);
                            flattenedFiles.add(flattenedStudyFileName);
                        }
                    }
                }
            }
        }

        return flattenedFiles;
    }

    private static void printMergedInformationToFile(File flattenedStudyFileName, OrderedMap<Integer, List<String>> merged) {
        try {
            PrintStream filePrintStream = new PrintStream(flattenedStudyFileName);

            for (Integer rowNumber : merged.keySet()) {
                int columnIndex = 0;
                for (String columnValue : merged.get(rowNumber)) {
                    filePrintStream.print(columnValue);
                    if (columnIndex != (merged.get(rowNumber).size() - 1)) {
                        filePrintStream.print("\t");
                    }
                }
                filePrintStream.print("\n");
            }

            filePrintStream.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Investigation importISATabFiles(File isatabDirectory) {
        ISAtabFilesImporter isatabFilesImporter = new ISAtabFilesImporter(CONFIGURATION_DIRECTORY);
        isatabFilesImporter.importFile(isatabDirectory.getAbsolutePath());
        Investigation inv = isatabFilesImporter.getInvestigation();

        if (isatabFilesImporter.getMessages().size() > 0) {
            System.err.println("Errors encountered on import of " + isatabDirectory.getName() + "\r");

            for (ISAFileErrorReport message : isatabFilesImporter.getMessages()) {
                for (ErrorMessage errorMessage : message.getMessages()) {
                    System.out.println("\t" + errorMessage.getMessage());
                }
            }
        }

        return inv;
    }

    private static void createProtocolLookup(Investigation investigation) {
        protocolNameToTypes = new HashMap<String, String>();

        for (Study study : investigation.getStudies().values()) {
            for (Protocol protocol : study.getProtocols()) {

                String protocolType = protocol.getProtocolType();
                if (protocolType.contains(":")) {
                    protocolType = protocolType.substring(protocolType.indexOf(":") + 1);
                }
                protocolType = protocolType.trim().replaceAll("_", " ");

                protocolNameToTypes.put(protocol.getProtocolName(), protocolType);
            }
        }
    }

    private static File constructFlattenedFileName(File isatabDirectory, String identifier) {
        return new File(FLATTENED_FILES_DIR + File.separatorChar + isatabDirectory.getName() + ":" + identifier + ".txt");
    }

    private static void performMergeAssayWithStudySample(Map<Integer, List<String>> mergedRepresentation,
                                                         List<String[]> studySamples, List<String[]> assay, Set<String> sampleFilter) {

        Set<Integer> sampleNameIndexesInAssay = SpreadsheetManipulation.getIndexesWithThisColumnName(assay, "Sample Name", true);

        int sampleNameIndexForAssay = retrieveSampleNameIndexInAssay(sampleNameIndexesInAssay);

        sampleNameColumns = studySamples.get(0);
        // add column headers if index 0 of merge is empty.
        if (!mergedRepresentation.containsKey(0)) {
            addColumnHeaders(mergedRepresentation, assay, studySamples, sampleNameIndexForAssay);
        }

        for (int rowIndex = 1; rowIndex < assay.size(); rowIndex++) {
            String sampleNameForAssay = assay.get(rowIndex)[sampleNameIndexForAssay];

            boolean doAddRow = false;
            if (sampleFilter != null) {
                if (sampleFilter.contains(sampleNameForAssay)) {
                    doAddRow = true;
                }
            } else {
                doAddRow = true;
            }

            String[] correspondingSampleRow = SpreadsheetManipulation.findRowWithValue(studySamples, "Sample Name", sampleNameForAssay);

            if (correspondingSampleRow != null && doAddRow) {
                mergeRow(mergedRepresentation, assay, rowIndex, correspondingSampleRow, sampleNameIndexForAssay);
            }

        }
    }

    /**
     * Adds the column headers for the study sample file and assay file
     *
     * @param mergedRepresentation
     * @param assay
     * @param samples
     * @param assaySampleNameIndex
     */
    private static void addColumnHeaders(Map<Integer, List<String>> mergedRepresentation, List<String[]> assay, List<String[]> samples,
                                         int assaySampleNameIndex) {
        mergeRow(mergedRepresentation, assay, 0, samples.get(0), assaySampleNameIndex);
    }

    private static void mergeRow(Map<Integer, List<String>> mergedRepresentation, List<String[]> assay, int rowIndex, String[] correspondingSampleRow, int assaySampleNameIndex) {
        int rowToMergeIn = mergedRepresentation.size();

        if (!mergedRepresentation.containsKey(rowToMergeIn)) {
            mergedRepresentation.put(rowToMergeIn, new ArrayList<String>());
        }

        int count = 0;
        for (String studySampleColumnValue : correspondingSampleRow) {
            if (count < sampleNameColumns.length) {
                String tmpValue = studySampleColumnValue;
                tmpValue = attachProtocolTypeIfRequired(tmpValue);
                mergedRepresentation.get(rowToMergeIn).add(tmpValue);
            }
            count++;
        }

        int currentAssayColumnIndex = 0;
        for (String assayColumnValue : assay.get(rowIndex)) {
            if (currentAssayColumnIndex != assaySampleNameIndex) {
                String tmpValue = assayColumnValue;
                tmpValue = attachProtocolTypeIfRequired(tmpValue);
                mergedRepresentation.get(rowToMergeIn).add(tmpValue);
            }
            currentAssayColumnIndex++;
        }
    }

    private static String attachProtocolTypeIfRequired(String protocol) {
        String protocolType = protocolNameToTypes.get(protocol);

        if (protocolType != null && !protocolType.isEmpty()) {
            if (protocolType.contains(":")) {
                protocolType = protocolType.substring(protocol.indexOf(":") + 1);
            }
            return protocolType.replaceAll("_", " ");
        }
        return protocol;
    }

    private static int retrieveSampleNameIndexInAssay(Set<Integer> sampleNameIndexesInAssay) {
        int sampleNameIndexForAssay = 0;
        if (sampleNameIndexesInAssay.size() > 0) {
            // we only want the first index.
            sampleNameIndexForAssay = sampleNameIndexesInAssay.iterator().next();
        } else {
            System.err.println("We cannot merge unless we have a sample name in the assay file to join on.\r");
        }
        return sampleNameIndexForAssay;
    }

}
