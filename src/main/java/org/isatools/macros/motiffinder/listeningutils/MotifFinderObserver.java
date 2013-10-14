package org.isatools.macros.motiffinder.listeningutils;

import org.isatools.macros.motiffinder.Motif;

import java.util.Collection;

/**
 * Created by the ISA team
 *
 * @author Eamonn Maguire (eamonnmag@gmail.com)
 *         <p/>
 *         Date: 18/07/2012
 *         Time: 18:33
 */
public interface MotifFinderObserver {

    public void notifyOfEvent(Collection<Motif> motifBlocks);
}
