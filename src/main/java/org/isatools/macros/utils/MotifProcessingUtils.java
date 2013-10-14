package org.isatools.macros.utils;

import org.apache.commons.collections15.map.ListOrderedMap;
import org.isatools.macros.motiffinder.Motif;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by the ISA team
 *
 * @author Eamonn Maguire (eamonnmag@gmail.com)
 *         <p/>
 *         Date: 31/10/2012
 *         Time: 07:57
 */
public class MotifProcessingUtils {

    private static final String SINGLE_MOTIF_PATTERN = "((:|\\*)*\\{*[\\w\\s]*#[\\w\\s]*(:|,|>)*[\\w\\s]*:*[\\w\\s]*\\}*)";
    private static final String WORD_PATTERN = "[ref]*_*(\\w+\\s*)+_*(\\d+)*";
    private static final String ALT_SINGLE_MOTIF_PATTERN = "((:|\\*|,)*\\{*[\\w\\s]*#[\\w\\s]*(:|,|>)*(\\w+:*)*\\d*(\\(\\w+\\))*(_)*:*\\}*)";
    private static final String NODE_PATTERN = "([\\w\\s_]+:\\d+)";

    private static Pattern motifGroupPattern = Pattern.compile(SINGLE_MOTIF_PATTERN + "+");
    private static Pattern singleMotifPattern = Pattern.compile(ALT_SINGLE_MOTIF_PATTERN);
    private static Pattern mergePattern = Pattern.compile(NODE_PATTERN);
    private static Pattern singleWordPattern = Pattern.compile(WORD_PATTERN);
    private static Pattern number = Pattern.compile(":\\d+");

    public static String findAndCollapseMergeEvents(String representation) {

        Matcher m = mergePattern.matcher(representation);
        Map<String, List<Integer>> idToStartIndex = new ListOrderedMap<String, List<Integer>>();

        while (m.find()) {
            String group = representation.substring(m.start(), m.end());
            if (!idToStartIndex.containsKey(group)) {
                idToStartIndex.put(group, new ArrayList<Integer>());
            }
            idToStartIndex.get(group).add(m.start());
        }

        for (String key : idToStartIndex.keySet()) {

            if (idToStartIndex.get(key).size() > 1) {

                String reference = "ref_" + key.substring(0, key.lastIndexOf(":")) + "_" + key.substring(key.lastIndexOf(":") + 1);
                representation = representation.replace(key, reference);
                representation = representation.replaceFirst(reference, key.substring(0, key.lastIndexOf(":")) + ">" + reference);
            }
        }

        representation = representation.replaceAll("\\(\\d+\\)", "");
        representation = representation.replaceAll(":\\d+", "");

        return representation;
    }

    public static int getNumberOfGroupsInMotifString(Motif motif) {
        return getNumberOfGroupsInMotifString(motif.getStringRepresentation());
    }

    public static int getNumberOfGroupsInMotifString(String representation) {
        List<String> branches = getBranchesInMotif(representation);
        int maxSize = 0;
        for (String branch : branches) {
            List<String> nodesInBranch = getNodesInBranch(branch);
            if (nodesInBranch.size() > maxSize) {
                maxSize = nodesInBranch.size();
            }
        }
        return maxSize;
    }

    /**
     * Returns an ordered set of Branches found in the motif.
     *
     * @param representation - Motif String representation
     * @return OrderedSet of branches as found in the String representation.
     */
    public static List<String> getBranchesInMotif(String representation) {
        List<String> branches = new ArrayList<String>();
        Matcher motifGroupMatcher = motifGroupPattern.matcher(representation);

        while (motifGroupMatcher.find()) {
            String targetGroup = representation.substring(motifGroupMatcher.start(), motifGroupMatcher.end());
            branches.add(targetGroup);
        }

        return branches;
    }

    /**
     * Returns an ordered set of nodes found in a branch.
     *
     * @param branch - String representation of branch to be processed
     * @return - OrderedSet of nodes within the branch.
     */
    public static List<String> getNodesInBranch(String branch) {
        Matcher singleBracketMatcher = singleMotifPattern.matcher(branch);

        List<String> nodes = new ArrayList<String>();
        while (singleBracketMatcher.find()) {
            nodes.add(branch.substring(singleBracketMatcher.start(), singleBracketMatcher.end()));
        }

        return nodes;
    }

    /**
     * Returns the parts of the node, usually of size 3 where 1:Relationship Type 2: Node Type 3:Count
     */
    public static List<String> getPartsOfNode(String node) {
        Matcher wordMatcher = singleWordPattern.matcher(node);
        // we want the 2nd word. This is always the node type in our motif representation.
        List<String> nodeParts = new ArrayList<String>();
        while (wordMatcher.find()) {
            nodeParts.add(node.substring(wordMatcher.start(), wordMatcher.end()));
        }

        return nodeParts;

    }

    /**
     * Returns all node ids contained in a motif's String representation.
     *
     * @param representation - Motif String representation
     * @return - Set of node ids as Longs.
     */
    public static Set<Long> getNodeIdsInString(String representation) {
        Matcher m = number.matcher(representation);

        Set<Long> nodeIds = new HashSet<Long>();

        while (m.find()) {
            nodeIds.add(Long.valueOf(representation.substring(m.start() + 1, m.end())));
        }

        return nodeIds;
    }

    /**
     * isMotifGood(String representation)
     * A good motif is one with the same start and end nodes. The code will be similar to that of the code
     * used to detect the group counts, except for each group, we process the last motif and determine
     * whether or not the node type is the same.
     *
     * @param representation - String representation of motif to be processed
     * @return true if the end nodes match, false otherwise.
     */
    public static boolean isMotifGood(String representation) {
        List<String> branches = getBranchesInMotif(representation);
        Set<String> endNodeTypes = new HashSet<String>();
        for (String branch : branches) {

            List<String> nodesInBranch = getNodesInBranch(branch);

            String lastNode = "";
            for (String node : nodesInBranch) {
                lastNode = node;
            }

            if (!lastNode.isEmpty()) {
                List<String> motifParts = getPartsOfNode(lastNode);

                // we want the 2nd word. This is always the node type in our motif representation.
                String type = motifParts.size() > 1 ? motifParts.get(1) : motifParts.get(0);
                if (type.startsWith("ref")) {
                    type = type.replaceAll("ref|_|(\\d+)", "");
                }
                if (!type.isEmpty()) {
                    endNodeTypes.add(type.trim());
                }
            }
        }


        // if this set is bigger than 1, then we have a 'bad' motif since there is more than one end node type.
        return endNodeTypes.size() == 1;
    }

    private static String removeSoloKeys(String representation, Map<String, List<Integer>> idToStartIndex) {
        // we do the replacement in reverse to avoid problems with indexes.
        Set<String> toRemove = new HashSet<String>();
        for (String key : idToStartIndex.keySet()) {
            if (idToStartIndex.get(key).size() == 1) {
                representation = representation.replaceAll(key, key.substring(0, key.indexOf(":")));
                toRemove.add(key);
            }
        }

        for (String key : toRemove) {
            idToStartIndex.remove(key);
        }

        return representation;
    }

    public static Set<Long> flattenNodeIds(Collection<Set<Long>> nodesInMotif) {
        Set<Long> nodes = new HashSet<Long>();

        synchronized (nodesInMotif) {
            try {
                for (Set<Long> motifNodes : nodesInMotif) {
                    nodes.addAll(motifNodes);
                }
            } catch (ConcurrentModificationException cme) {
                System.err.println("Error occurred " + cme.getMessage());
                // don't do anything
            }
        }

        return nodes;
    }
}
