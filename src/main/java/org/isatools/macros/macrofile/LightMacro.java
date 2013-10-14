package org.isatools.macros.macrofile;

import java.util.HashMap;
import java.util.Map;

public class LightMacro {

    private String motif;
    private Map<String, String> glyphSizeToFileName;

    public LightMacro(String motif) {
        this.motif = motif;

        glyphSizeToFileName = new HashMap<String, String>();
    }

    public String getMotif() {
        return motif;
    }

    public void addGlyph(String size, String file) {
        glyphSizeToFileName.put(size, file);
    }

    public Map<String, String> getGlyphSizeToFileName() {
        return glyphSizeToFileName;
    }
}
