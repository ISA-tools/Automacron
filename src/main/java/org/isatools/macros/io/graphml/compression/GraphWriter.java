package org.isatools.macros.io.graphml.compression;

import org.isatools.macros.io.graphml.GraphMLCreator;
import org.isatools.macros.io.graphml.compression.graphml_model.Graph;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.List;

/**
 * Created by the ISA team
 *
 * @author Eamonn Maguire (eamonnmag@gmail.com)
 *         <p/>
 *         Date: 04/10/2012
 *         Time: 15:41
 */
public class GraphWriter {
    public void write(Graph graph, File outputFile) {
        // use toString method of each object type.
        // just need wrapper for the XML tag.
        PrintStream printStream = null;
        try {
            printStream = new PrintStream(outputFile);

            outputStart(graph, printStream);
            outputGraph(graph, printStream);
            outputEnd(printStream);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            assert printStream != null;
            printStream.close();
        }
    }

    public void outputGraph(Graph graph, PrintStream printStream) {
        printListContents(graph.getGraphProperties(), printStream);
        printListContents(graph.getGraphNodes(), printStream);
        printListContents(graph.getGraphEdges(), printStream);
    }

    private void printListContents(List<?> toPrint, PrintStream printStream) {
        for (Object graphItem : toPrint) {
            printStream.println(graphItem);
        }
    }

    private void outputStart(Graph graph, PrintStream printStream) {
        printStream.println(GraphMLCreator.GRAPHML_START_TAG);
        printStream.println("<graph edgedefault=\"" + graph.getDirection() + "\">");
    }

    private void outputEnd(PrintStream printStream) {
        printStream.println(GraphMLCreator.GRAPHML_END_TAG);
    }
}
