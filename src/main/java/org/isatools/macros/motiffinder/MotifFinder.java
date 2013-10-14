package org.isatools.macros.motiffinder;

import org.isatools.macros.gui.DBGraph;
import org.isatools.macros.io.graphml.GraphMLCreator;

import java.io.File;
import java.util.List;
import java.util.Map;

/**
 * Created by the ISA team
 *
 * @author Eamonn Maguire (eamonnmag@gmail.com)
 *         <p/>
 *         Date: 14/05/2012
 *         Time: 10:48
 */
public interface MotifFinder {
    void performAnalysis(List<DBGraph> experimentsInDb, File graphMLFileToOutput);
    void performAnalysis(DBGraph startNode, GraphMLCreator graphMLCreator);
    Map<String, Motif> getMotifs();
}
