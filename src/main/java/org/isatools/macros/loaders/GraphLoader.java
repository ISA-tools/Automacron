package org.isatools.macros.loaders;

import org.neo4j.graphdb.GraphDatabaseService;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * Created by the ISA team
 *
 * @author Eamonn Maguire (eamonnmag@gmail.com)
 *         <p/>
 *         Date: 14/05/2012
 *         Time: 10:48
 */
public interface GraphLoader {
    
    public void loadFiles(File directory);
}
