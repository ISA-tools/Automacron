package org.isatools.macros.exporters.html;

import org.isatools.macros.exporters.Exporter;
import org.isatools.macros.gui.macro.Macro;
import org.isatools.macros.gui.macro.renderer.MotifGraphRenderer;
import org.isatools.macros.motiffinder.Motif;
import org.isatools.macros.utils.MotifProcessingUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public class HTMLExporter implements Exporter {
    private static final String BASE_HTML_FILE = "/exporters/html_templates/base.html";
    private static final String TABLE_HTML_FILE = "/exporters/html_templates/motif-info.html";

    private String injectedTableHTML;
    private String baseHTML;

    public void export(Set<Macro> macrosToOutput, File exportDir) {
        loadFiles();

        StringBuilder macros = new StringBuilder();

        SortedSet<Integer> depths = new TreeSet<Integer>();

        File macrosDir = new File(exportDir.getAbsolutePath() + File.separator + "macros");
        if(!macrosDir.exists()) macrosDir.mkdirs();

        int index = 0;
        for (Macro macro: macrosToOutput) {

            String macroInformation = injectedTableHTML;

            Motif code = macro.getMotif();
            // generate motif and store in directory under a HTML output directory
            int depth = MotifProcessingUtils.getNumberOfGroupsInMotifString(code.getStringRepresentation());
            depths.add(depth);

            macroInformation = macroInformation.replace("DEPTH", "" + depth);
            macroInformation = macroInformation.replace("INDEX", "" + index);

            MotifGraphRenderer renderer = new MotifGraphRenderer(true);
            File macroFile = new File(macrosDir.getAbsolutePath() + "/macro-" + index + ".png");

            renderer.renderMacro(code, macroFile);


            macroInformation = macroInformation.replace("MACRO_IMAGE", macroFile.getAbsolutePath());
            macroInformation = macroInformation.replace("CUMULATIVE_USAGE", Integer.toString(code.getCumulativeUsage()));
            macroInformation = macroInformation.replace("SCORE", Double.toString(code.getScore()));

            macros.append(macroInformation);

            index++;
        }

        baseHTML = baseHTML.replace("TIME_TAKEN_TAG", "n/a");
        baseHTML = baseHTML.replace("<INJECTED_MACRO_CODE/>", macros.toString());

        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Date date = new Date();
        System.out.println(dateFormat.format(date));

        baseHTML = baseHTML.replace("DATE_TAG", dateFormat.format(date));

        StringBuilder filterHTML = new StringBuilder();
        filterHTML.append("<li class=\"selected\"><a href=\"#filter\" data-value=\"all\">All</a></li>");
        for (Integer depth : depths) {
            filterHTML.append(String.format("<li><a href=\"#filter\" data-value=\"%d\">Depth %d</a></li>", depth, depth));
        }

        baseHTML = baseHTML.replace("<INJECTED_FILTER_CODE/>", filterHTML.toString());

        PrintStream ps = null;
        try {
            ps = new PrintStream(exportDir.getAbsolutePath() + File.separator + "automacron-output.html");
            ps.print(baseHTML);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            if (ps != null) {
                ps.close();
            }
        }
    }

    public void export(Map<String, Motif> motifs, String timeTaken, File exportDir) {
        loadFiles();

        StringBuilder macros = new StringBuilder();

        SortedSet<Integer> depths = new TreeSet<Integer>();

        File macrosDir = new File(exportDir.getAbsolutePath() + File.separator + "macros");
        if(!macrosDir.exists()) macrosDir.mkdirs();

        int index = 0;
        for (String sampleName : motifs.keySet()) {
            System.out.println(sampleName);
            String macroInformation = injectedTableHTML;

            Motif code = motifs.get(sampleName);
            // generate motif and store in directory under a HTML output directory
            int depth = MotifProcessingUtils.getNumberOfGroupsInMotifString(code.getStringRepresentation());
            depths.add(depth);

            macroInformation = macroInformation.replace("DEPTH", "" + depth);
            macroInformation = macroInformation.replace("INDEX", "" + index);

            MotifGraphRenderer renderer = new MotifGraphRenderer(true);
            File macroFile = new File(macrosDir.getAbsolutePath() + "/macro-" + index + ".png");

            renderer.renderMacro(code, macroFile);

            macroInformation = macroInformation.replace("DETAIL", sampleName);
            macroInformation = macroInformation.replace("MACRO_IMAGE", macroFile.getAbsolutePath());
            macroInformation = macroInformation.replace("CUMULATIVE_USAGE", Integer.toString(code.getCumulativeUsage()));
            macroInformation = macroInformation.replace("SCORE", Double.toString(code.getScore()));

            macros.append(macroInformation);

            index++;
        }

        baseHTML = baseHTML.replace("TIME_TAKEN_TAG", timeTaken);
        baseHTML = baseHTML.replace("<INJECTED_MACRO_CODE/>", macros.toString());

        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Date date = new Date();
        System.out.println(dateFormat.format(date));

        baseHTML = baseHTML.replace("DATE_TAG", dateFormat.format(date));

        StringBuilder filterHTML = new StringBuilder();
        filterHTML.append("<li class=\"selected\"><a href=\"#filter\" data-value=\"all\">All</a></li>");
        for (Integer depth : depths) {
            filterHTML.append(String.format("<li><a href=\"#filter\" data-value=\"%d\">Depth %d</a></li>", depth, depth));
        }

        baseHTML = baseHTML.replace("<INJECTED_FILTER_CODE/>", filterHTML.toString());

        PrintStream ps = null;
        try {
            ps = new PrintStream(exportDir.getAbsolutePath() + File.separator + "automacron-output.html");
            ps.print(baseHTML);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            if (ps != null) {
                ps.close();
            }
        }
    }

    private void loadFiles() {
        baseHTML = loadAndAssignFileContents(BASE_HTML_FILE);
        injectedTableHTML = loadAndAssignFileContents(TABLE_HTML_FILE);
    }

    private String loadAndAssignFileContents(String fileLocation) {
        Scanner fileScanner = new Scanner(getClass().getResourceAsStream(fileLocation));

        StringBuilder sb = new StringBuilder();
        while (fileScanner.hasNextLine()) {
            sb.append(fileScanner.nextLine());
        }

        return sb.toString();
    }
}
