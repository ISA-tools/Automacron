package org.isatools.macros.io.graphml.compression.graphml_model;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by the ISA team
 *
 * @author Eamonn Maguire (eamonnmag@gmail.com)
 *         <p/>
 *         Date: 04/10/2012
 *         Time: 15:42
 */
public class GraphNode {

    long id;
    Map<String, String> data;

    public GraphNode(long id) {
        this.id = id;
        data = new HashMap<String, String>();
    }

    public long getId() {
        return id;
    }

    public Map<String, String> getData() {
        return data;
    }

    public String toString() {

        StringBuilder nodeAsString = new StringBuilder();
        nodeAsString.append("<node id=\"").append(id).append("\">");

        for (String attribute : data.keySet()) {
            nodeAsString.append("<data key=\"").append(attribute).append("\">").append(data.get(attribute).trim()).append("</data>");
        }
        nodeAsString.append("</node>");

        return nodeAsString.toString();
    }
}