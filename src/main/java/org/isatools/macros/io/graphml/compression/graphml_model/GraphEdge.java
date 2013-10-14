package org.isatools.macros.io.graphml.compression.graphml_model;

/**
 * Created by the ISA team
 *
 * @author Eamonn Maguire (eamonnmag@gmail.com)
 *         <p/>
 *         Date: 04/10/2012
 *         Time: 15:43
 */
public class GraphEdge {
    long source;
    long target;

    public GraphEdge(long source, long target) {
        this.source = source;
        this.target = target;
    }

    public long getSource() {
        return source;
    }

    public long getTarget() {
        return target;
    }

    public void setSource(long source) {
        this.source = source;
    }

    public void setTarget(long target) {
        this.target = target;
    }
    
    public String toString() {
        StringBuilder edgeAsString = new StringBuilder();
        edgeAsString.append("<edge source=\"" + source + "\" target=\"" + target + "\"/>");
        return edgeAsString.toString();
    }
}