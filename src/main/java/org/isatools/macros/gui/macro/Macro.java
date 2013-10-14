package org.isatools.macros.gui.macro;

import org.isatools.macros.gui.macro.renderer.MotifGraphRenderer;
import org.isatools.macros.gui.macro.renderer.RenderingFactory;
import org.isatools.macros.gui.macro.renderer.RenderingType;
import org.isatools.macros.motiffinder.Motif;

import java.io.File;
import java.util.*;

/**
 * Created by the ISA team
 *
 * @author Eamonn Maguire (eamonnmag@gmail.com)
 *         <p/>
 *         Date: 07/06/2012
 *         Time: 10:32
 */
public class Macro {

    private Motif motif;

    private Map<RenderingType, File> glyphGranularities;
    private String annotation = "";
    private double penalty;

    public Macro(Motif motif) {
        this.motif = motif;

        penalty = 0;
        glyphGranularities = new HashMap<RenderingType, File>();
    }

    public Motif getMotif() {
        return motif;
    }

    public void addGlyph(RenderingType detailLevel, File file) {
        glyphGranularities.put(detailLevel, file);
    }

    /**
     * Returns a glyph of a specified detail level
     * @param detail - One of Macro.ABSTRACT, Macro.MEDIUM, or Macro.DETAILED
     * @return @see File - a glyph of a specified detail level
     */
    public File getGlyph(RenderingType detail) {
        return glyphGranularities.get(detail);
    }

    public Set<String> getNodeIdsInMacroAsString() {
        Set<String> nodes = new HashSet<String>();

        Collection<Set<Long>> nodesInMotif = motif.getNodesInMotif();

        for (Set<Long> relatedMotifs : nodesInMotif) {
            for (Long nodeId : relatedMotifs) {
                nodes.add(String.valueOf(nodeId));
            }
        }

        return nodes;
    }

    public Set<Set<Long>> getNodeIdsInMacro() {
        Set<Set<Long>> nodes = new HashSet<Set<Long>>();

        nodes.addAll(motif.getNodesInMotif());

        for (Set<Long> relatedMotifs : motif.getRelatedNodeIds()) {
            nodes.add(relatedMotifs);
        }

        return nodes;
    }

    public Map<RenderingType, File> getGlyphGranularities() {
        return glyphGranularities;
    }

    public String toString() {
        return motif.getStringRepresentation();
    }

    public void setAnnotation(String annotation) {
        this.annotation = annotation;
    }

    public String getAnnotation() {
        return annotation;
    }

    public double getPenalty() {
        return penalty;
    }

    public void setPenalty(double penalty) {
        this.penalty = penalty;
    }
}
