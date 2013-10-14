package org.isatools.macros.launcher;

import org.isatools.macros.graph.graphloader.GraphLoader;
import org.isatools.macros.gui.DBPreference.DBMenuUI;
import org.isatools.macros.gui.MacroGraphUI;
import org.isatools.macros.gui.macro.Macro;
import org.isatools.macros.manager.AutoMacronApplicationManager;
import org.isatools.macros.motiffinder.Motif;
import org.isatools.macros.utils.MotifProcessingUtils;
import org.isatools.macros.utils.MotifStatCalculator;
import org.jdesktop.fuse.ResourceInjector;

import javax.swing.*;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by the ISA team
 *
 * @author Eamonn Maguire (eamonnmag@gmail.com)
 *         <p/>
 *         Date: 02/10/2012
 *         Time: 15:51
 */
public class AutoMacronApp {

    static {
        ResourceInjector.addModule("org.jdesktop.fuse.swing.SwingModule");
        ResourceInjector.get("ui-package.style").load(
                MacroGraphUI.class.getResource("/dependency_injections/ui-package.properties"));
    }

    public static void main(String[] args) {

//        final GraphLoader graphLoader = new GraphLoader("/Users/eamonnmaguire/dev/neo4j-db/automacron.db");

//        try {
//            System.out.println("Reading in object\r");
//            FileInputStream fis = new FileInputStream("ProgramData/motifs.ser");
//            ObjectInputStream ois = new ObjectInputStream(fis);
//            Object object = ois.readObject();
//            ois.close();
//
//            System.out.println("Finished!\r");
//
//            final Set<Macro> macros = new HashSet<Macro>();
//            if (object instanceof Map) {
//                final Map<String, Motif> motifs = (HashMap<String, Motif>) object;
//
//                System.out.println("Analysing motifs...");
//                MotifStatCalculator calculator = new MotifStatCalculator();
//                calculator.analyseMotifs(motifs);
//
//                System.out.println("Finished analysing, now adding macros\r");
//                for(Motif motif : motifs.values()){
//                    macros.add(new Macro(motif));
//                }
//
//                System.out.println("Going to display user interface\r");

                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        DBMenuUI menuUI = new DBMenuUI();
                        menuUI.createGUI();
                        menuUI.showUI();
                    }
                });
//            }
//
//            fis.close();
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }


    }
}
