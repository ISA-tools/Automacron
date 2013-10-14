package org.isatools.macros.macrofile.importer;

import org.isatools.macros.macrofile.LightMacro;
import org.w3c.dom.NodeList;
import uk.ac.ebi.utils.xml.XPathReader;

import javax.xml.xpath.XPathConstants;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

public class MacroFileImporter {

    // import file
    public List<LightMacro> importFile(File file) throws FileNotFoundException {

        List<LightMacro> macroResult = new ArrayList<LightMacro>();
        XPathReader reader = new XPathReader(new FileInputStream(file));
        NodeList macros = (NodeList) reader.read("/automacron/macros/macro", XPathConstants.NODESET);

        for(int macroIndex = 1 ; macroIndex <= macros.getLength(); macroIndex++ ){

            String motif = (String) reader.read("/automacron/macros/macro[" + macroIndex + "]/motif", XPathConstants.STRING);
            LightMacro newMacro = new LightMacro(motif);
            NodeList glyphList = (NodeList) reader.read("/automacron/macros/macro[" + macroIndex + "]/glyphs/glyph", XPathConstants.NODESET);

            for(int glyphIndex = 1; glyphIndex <= glyphList.getLength(); glyphIndex++) {
                String glyphSize = (String) reader.read("/automacron/macros/macro[" + macroIndex + "]/glyphs/glyph[" + glyphIndex + "]/@size", XPathConstants.STRING);
                String glyphFile = (String) reader.read("/automacron/macros/macro[" + macroIndex + "]/glyphs/glyph[" + glyphIndex + "]/@file", XPathConstants.STRING);
                newMacro.addGlyph(glyphSize, glyphFile);
            }
            macroResult.add(newMacro);
        }

        return macroResult;
    }

}
