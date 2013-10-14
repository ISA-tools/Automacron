package org.isatools.macros.gui.macro.renderer;

import com.mxgraph.view.mxGraph;

/**
 * Immutable Object giving info about the motif graph.
 */
public class MotifGraphInfo {

    private final mxGraph graph;
    private final Object rootVertex;

    public MotifGraphInfo(mxGraph graph, Object rootVertex) {
        this.graph = graph;
        this.rootVertex = rootVertex;
    }

    public mxGraph getGraph() {
        return graph;
    }

    public Object getRootVertex() {
        return rootVertex;
    }
}
