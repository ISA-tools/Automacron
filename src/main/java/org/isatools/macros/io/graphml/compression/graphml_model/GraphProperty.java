package org.isatools.macros.io.graphml.compression.graphml_model;

/**
 * Created by the ISA team
 *
 * @author Eamonn Maguire (eamonnmag@gmail.com)
 *         <p/>
 *         Date: 04/10/2012
 *         Time: 15:43
 */
public class GraphProperty {

    private String id, forType, attr_name, attr_type;

    public GraphProperty(String id, String forType, String attr_name, String attr_type) {
        this.id = id;
        this.forType = forType;
        this.attr_name = attr_name;
        this.attr_type = attr_type;
    }

    public String toString() {
        StringBuilder propertiesAsString = new StringBuilder();

        propertiesAsString.append("<key id=\"" + id + "\" for=\"" + forType + "\" attr.name=\""
                + attr_name + "\" attr.type=\"" + attr_type + "\"/>");

        return propertiesAsString.toString();
    }
}
