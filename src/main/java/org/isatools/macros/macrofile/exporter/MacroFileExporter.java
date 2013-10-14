package org.isatools.macros.macrofile.exporter;

import org.isatools.macros.gui.macro.Macro;
import org.isatools.macros.gui.macro.renderer.RenderingType;
import org.isatools.macros.utils.MotifProcessingUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.Collection;

/**
 * Exports a collection of selected macros as XML
 * which can be used for replacement purposes in other visualizations.
 *
 * User: eamonnmaguire
 * Date: 06/03/2013
 * Time: 17:16
 */
public class MacroFileExporter {

    /**
     <automacron>
        <macros>
            <macro name="myMacro">
                <motif>macro:>as_string</motif>
                <glyphs>
                    <glyph size="small" file="myfile1.png"/>
                    <glyph size="medium" file="myfile2.png"/>
                    <glyph size="large" file="myfile3.png"/>
                </glyphs>
            </macro>
        </macros>
     </automacron>
     */

    public void exportMacros(File saveLocation, Collection<Macro> macros) throws FileNotFoundException {

        PrintStream fileOutputStream = new PrintStream(saveLocation);

        fileOutputStream.println("<automacron><macros>");

        for(Macro macro : macros) {
            String motifAsString = MotifProcessingUtils.findAndCollapseMergeEvents(macro.getMotif().getStringRepresentation());
            fileOutputStream.println("<macro name=\"\">");
            fileOutputStream.println("<motif>" + motifAsString + "</motif>");
            fileOutputStream.println("<glyphs>");
            for(RenderingType detail : macro.getGlyphGranularities().keySet()) {
                fileOutputStream.println("<glyph size=\"\" file=\"" + macro.getGlyphGranularities().get(detail) + "\"/>");
            }
            fileOutputStream.println("</glyphs>");
            fileOutputStream.println("</macro>");
        }

        fileOutputStream.println("</macros></automacron>");
        fileOutputStream.close();
    }
}
