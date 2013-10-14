package org.isatools.macros.utils.motifutils;

import org.isatools.macros.gui.macro.CompleteSwatchBoard;
import org.isatools.macros.gui.macro.Macro;
import org.isatools.macros.gui.macro.MacroUI;
import org.isatools.macros.gui.macro.SwatchBoard;
import org.isatools.macros.gui.macro.selection_util.MacroSelectionUtilUI;
import org.isatools.macros.motiffinder.Motif;
import org.isatools.macros.utils.MotifStatCalculator;

import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.util.*;

/**
 * Created by the ISA team
 *
 * @author Eamonn Maguire (eamonnmag@gmail.com)
 *         <p/>
 *         Date: 07/12/2012
 *         Time: 21:09
 */
public class MotifProcessor {

    public static void main(String[] args) {
        FileInputStream fis;
        Map<String, Motif> motifs;
        try {
            fis = new FileInputStream("ProgramData/motifs.ser");

            ObjectInputStream ois = new ObjectInputStream(fis);
            Object object = ois.readObject();

            ois.close();

            if(object instanceof Map) {
                motifs = (HashMap<String, Motif>) object;

                MotifStatCalculator calculator = new MotifStatCalculator();
                calculator.analyseMotifs(motifs);

                SwatchBoard swatchBoard = new SwatchBoard();
                swatchBoard.createGUI();

                MacroSelectionUtilUI macroSelectionUtilUI = new MacroSelectionUtilUI(2, 6);
                macroSelectionUtilUI.createGUI();

                List<MacroUI> macros = new ArrayList<MacroUI>();

                for(Motif motif : motifs.values()) {
                    MacroUI macroUI = new MacroUI(new Macro(motif));
                    macros.add(macroUI);
                }

                Collections.sort(macros);

                for(MacroUI macroUI : macros){
                    macroSelectionUtilUI.addMacro(macroUI.getMacro().getMotif().getDepth(), macroUI);
                    swatchBoard.addGlyph(macroUI);
                }

                CompleteSwatchBoard completeSwatchBoard = new CompleteSwatchBoard(swatchBoard);
                completeSwatchBoard.createGUI();

                completeSwatchBoard.showUI(null);
                macroSelectionUtilUI.showUI(null, new HashSet<MacroUI>());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
