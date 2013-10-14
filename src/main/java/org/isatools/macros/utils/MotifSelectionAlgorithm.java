package org.isatools.macros.utils;

import org.isatools.macros.motiffinder.Motif;

import java.util.List;
import java.util.Map;

/**
 * Created by the ISA team
 *
 * @author Eamonn Maguire (eamonnmag@gmail.com)
 *         <p/>
 *         Date: 19/09/2012
 *         Time: 11:10
 */
public interface MotifSelectionAlgorithm {
    List<Motif> analyseMotifs(Map<String, Motif> motifs);
}
