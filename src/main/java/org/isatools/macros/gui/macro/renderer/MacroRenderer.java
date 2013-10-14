package org.isatools.macros.gui.macro.renderer;

import org.isatools.macros.gui.macro.Macro;
import org.isatools.macros.motiffinder.Motif;

import java.io.File;
import java.util.Map;

/**
 * Created by the ISA team
 *
 * @author Eamonn Maguire (eamonnmag@gmail.com)
 *         <p/>
 *         Date: 03/12/2012
 *         Time: 08:11
 */
public interface MacroRenderer {


    public Map<RenderingType, File> renderMacros(Macro macro, MotifGraphInfo motifGraphInfo);
}
