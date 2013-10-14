package org.isatools.macros.loaders.stats;

/**
 * Created by the ISA team
 *
 * @author Eamonn Maguire (eamonnmag@gmail.com)
 *         <p/>
 *         Date: 29/11/2012
 *         Time: 10:49
 */
public class GraphStats {

    int edgeCount;
    int vertexCount;

    public GraphStats(int edgeCount, int vertexCount) {
        this.edgeCount = edgeCount;
        this.vertexCount = vertexCount;
    }

    public int getEdgeCount() {
        return edgeCount;
    }

    public int getVertexCount() {
        return vertexCount;
    }
}
