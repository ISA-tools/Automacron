package org.isatools.macros.plugin.glyphbasedworkflow;

import org.apache.log4j.Logger;
import org.isatools.isacreator.managers.ApplicationManager;
import org.isatools.isacreator.model.Assay;
import org.isatools.isacreator.model.Investigation;

import org.isatools.macros.graph.graphloader.GraphFunctions;
import org.isatools.macros.graph.graphloader.Neo4JConnector;
import org.isatools.macros.gui.DBGraph;
import org.isatools.macros.io.graphml.GraphMLCreator;
import org.isatools.macros.loaders.isa.ISAWorkflowLoader;
import org.isatools.macros.loaders.isa.fileprocessing.isatab.ISAFileFlattener;
import org.isatools.macros.macrofile.LightMacro;
import org.isatools.macros.macrofile.importer.MacroFileImporter;
import org.isatools.macros.motiffinder.GraphTraversalImpl;
import org.isatools.macros.motiffinder.MotifFinder;
import org.isatools.macros.motiffinder.TargetedMotifFinderImpl;
import org.isatools.macros.plugin.workflowvisualization.WorkflowInformation;
import org.neo4j.graphdb.GraphDatabaseService;

import javax.swing.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by the ISA team
 *
 * @author Eamonn Maguire (eamonnmag@gmail.com)
 *         <p/>
 *         Date: 03/09/2012
 *         Time: 14:52
 */
public class WorkflowGraphCreator {


    private Neo4JConnector connector;

    private Logger log = Logger.getLogger(WorkflowGraphCreator.class.getName());

    private static final String AUTOMACRON_FILE_PATH = "ProgramData/automacron/automacron.xml";

    // pass in filter on study samples to look at and create the tree based on that.

    public WorkflowInformation createGraphMLFileForExperiment(boolean targetedAssay) {
        return createGraphMLFileForExperiment(targetedAssay, null);
    }

    public WorkflowInformation createGraphMLFileForExperiment(boolean isTargetedAssay, Set<String> selectedSamples) {

        connector = new Neo4JConnector();
        MotifFinder finder;
        WorkflowInformation workflowInformation;
        try {

            Investigation investigation = ApplicationManager.getCurrentApplicationInstance().getDataEntryEnvironment().getInvestigation();

            Collection<File> flattenedFiles = ISAFileFlattener.flattenISATabFiles(new File(investigation.getReference()).getParentFile(),
                    investigation, selectedSamples);

            String assayInView = getNameOfAssayInView().substring(0, getNameOfAssayInView().lastIndexOf("."));

            System.out.println("Loading experiments");
            GraphDatabaseService graphDatabaseService = connector.getGraphDB();

            for (File file : flattenedFiles) {
                String fileName = file.getName();
                if (isTargetedAssay && fileName.contains(assayInView)) {
                    ISAWorkflowLoader loader = new ISAWorkflowLoader(connector.getGraphDB());
                    loader.loadFiles(file);
                }
            }


            List<DBGraph> experiments = GraphFunctions.loadExperiments(graphDatabaseService);

            System.out.println("Loaded " + experiments.size() + " experiments in to graph db.");

            String systemTempDir = System.getProperty("java.io.tmpdir");

            for (DBGraph experiment : experiments) {
                if (isTargetedAssay && experiment.toString().contains(assayInView)) {

                    String fileAppender = "";
                    if (selectedSamples != null) {
                        fileAppender += selectedSamples.hashCode();
                    }

                    File graphMLFile = new File(systemTempDir + "graphml-" + experiment.toString() +
                            (fileAppender.equals("") ? "" : ("-" + fileAppender)) + ".xml");

                    Collection<LightMacro> targetedMacros = getTargetMacros();

                    if (targetedMacros != null && !targetedMacros.isEmpty()) {
                        Set<String> searchTargets = new HashSet<String>();
                        for (LightMacro macro : targetedMacros) {
                            searchTargets.add(macro.getMotif());
                        }
                        finder = new TargetedMotifFinderImpl(searchTargets);
                    } else {
                        finder = new GraphTraversalImpl();
                    }

                    finder.performAnalysis(experiment, new GraphMLCreator(graphMLFile));

                    workflowInformation = new WorkflowInformation(graphMLFile, finder.getMotifs());

                    if (finder.getMotifs() != null) {
                        System.out.println("Found " + finder.getMotifs().size() + " existing motifs.");
                        for(LightMacro macro : targetedMacros){
                            for(String macroSize : macro.getGlyphSizeToFileName().keySet()) {
                                workflowInformation.addMacroForMotif(macro.getMotif(), macroSize, macro.getGlyphSizeToFileName().get(macroSize));
                            }
                        }
                    } else {
                        System.out.println("No motifs found since creation was only performed for the graph.");
                    }

                    return workflowInformation;
                }
            }
        } catch (Exception e) {
            log.error(e.getMessage());
            JOptionPane.showMessageDialog(null, "Sorry, we were unable to generate the XML files. Please try saving the ISA-Tab and trying again!", "Error occurred", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        } finally {
            connector.shutdown();
        }

        return null;
    }

    private Collection<LightMacro> getTargetMacros() {
        MacroFileImporter importer = new MacroFileImporter();
        Collection<LightMacro> macros = null;
        try {
            if (new File(AUTOMACRON_FILE_PATH).exists()) {
                macros = importer.importFile(new File(AUTOMACRON_FILE_PATH));
                System.out.println("Macro size " + macros.size());
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return macros;
    }

    private String getNameOfAssayInView() {
        if (ApplicationManager.getScreenInView() instanceof Assay) {
            Assay assay = (Assay) ApplicationManager.getScreenInView();
            return assay.getAssayReference();
        }
        return "";
    }

}
