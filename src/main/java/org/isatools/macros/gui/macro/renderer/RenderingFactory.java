package org.isatools.macros.gui.macro.renderer;

import org.isatools.macros.gui.macro.Macro;
import org.isatools.macros.motiffinder.Motif;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by the ISA team
 *
 * @author Eamonn Maguire (eamonnmag@gmail.com)
 *         <p/>
 *         Date: 06/12/2012
 *         Time: 17:48
 */
public class RenderingFactory {

    private static MacroRenderer macroRenderer = new DefaultMacroRenderer();


    public static Macro populateMacroWithFiles(Macro macro, MotifGraphInfo motifGraphInfo) {

        Map<RenderingType, File> renderings = macroRenderer.renderMacros(macro, motifGraphInfo);
        macro.getGlyphGranularities().putAll(renderings);
        return macro;
    }

}
