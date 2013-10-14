package org.isatools.macros.motiffinder.listeningutils;

import org.isatools.macros.gui.DBGraph;

/**
 * Created by the ISA team
 *
 * @author Eamonn Maguire (eamonnmag@gmail.com)
 *         <p/>
 *         Date: 18/07/2012
 *         Time: 18:33
 */
public interface MotifFinderSubject {
    public void registerObserver(MotifFinderObserver motifFinderObserver);
    public void deregisterObserver(MotifFinderObserver motifFinderObserver);
    public void notifyObservers(boolean lastGraph);
}
