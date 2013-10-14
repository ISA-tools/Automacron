package org.isatools.macros.gui.macro.renderer;

import org.apache.commons.collections15.OrderedMap;
import org.apache.commons.collections15.map.ListOrderedMap;
import org.isatools.macros.gui.common.AutoMacronUIHelper;
import org.isatools.macros.gui.motifdrawer.MotifDrawer;
import org.isatools.macros.utils.MotifProcessingUtils;
import uk.ac.ebi.utils.collections.Pair;

import java.awt.*;
import java.util.List;

/**
 * Created by the ISA team
 *
 * @author Eamonn Maguire (eamonnmag@gmail.com)
 *         <p/>
 *         Date: 03/12/2012
 *         Time: 09:26
 */
public class RenderingUtils {

    public static String inferNodeType(String nodeType) {
        nodeType = nodeType.toLowerCase();

        if (nodeType.contains("file")) {
            return MotifDrawer.SQUARE_LIGHT_ORANGE;
        } else if (nodeType.contains("protocol")) {
            return MotifDrawer.TRIANGLE_GREY;
        } else if (nodeType.contains("name")) {
            return MotifDrawer.ROUNDED_GREEN;
        } else if (nodeType.contains("label")) {
            return MotifDrawer.ROUNDED_RED;
        } else if (nodeType.contains("data")) {
            return MotifDrawer.TRIANGLE_GREY;
        }else {
            System.out.println("Unknown node type " + nodeType);
            return MotifDrawer.ROUNDED_GREEN;
        }
    }

    public static Color getColorForNode(String nodeType) {
        nodeType = nodeType.toLowerCase();

        if (nodeType.contains("data") || nodeType.contains("file")) {
            return AutoMacronUIHelper.LIGHT_ORANGE_COLOR;
        } else if (nodeType.contains("protocol")) {
            return AutoMacronUIHelper.GREY_COLOR;
        } else if (nodeType.contains("name")) {
            return AutoMacronUIHelper.GREEN_COLOR;
        } else if (nodeType.contains("label")) {
            return AutoMacronUIHelper.RED_COLOR;
        } else {
            return AutoMacronUIHelper.GREEN_COLOR;
        }
    }

    /**
     * Returns two OrderedMaps composed of Strings to Integers describing the structure of a motif in terms of it's
     * topological features (in order) and node types with frequencies.
     *
     * @param representation - The motif representation
     * @return Returns two OrderedMaps composed of Strings to Integers
     */
    public static Pair<OrderedMap<String, Integer>, OrderedMap<String, Integer>> getMotifStructure(String representation) {

        // e.g. Linear:Sample Name -> 1 or Branch:Protocol REF -> 4
        OrderedMap<String, Integer> topologyToSize = new ListOrderedMap<String, Integer>();
        OrderedMap<String, Integer> nodeTypeToSize = new ListOrderedMap<String, Integer>();

        List<String> subMotifsInMotif = MotifProcessingUtils.getNodesInBranch(representation);

        for (String subMotif : subMotifsInMotif) {
            processNodeType(nodeTypeToSize, subMotif);
        }

        return new Pair<OrderedMap<String, Integer>, OrderedMap<String, Integer>>(topologyToSize, nodeTypeToSize);
    }

    private static void processNodeType(OrderedMap<String, Integer> nodeTypeToSize, String subMotif) {
        String type = subMotif;
        System.out.println(type);
        if (type.contains("#")) {
            type = type.substring(type.indexOf("#") + 1);
        }
        type = type.replaceAll(":|\\{|\\}", "");

        if (!type.isEmpty()) {
            if (!nodeTypeToSize.containsKey(type)) {
                nodeTypeToSize.put(type, 0);
            }

            nodeTypeToSize.put(type, nodeTypeToSize.get(type) + 1);
        }
    }

}
