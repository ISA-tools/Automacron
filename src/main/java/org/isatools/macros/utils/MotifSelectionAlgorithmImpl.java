package org.isatools.macros.utils;

import org.apache.commons.collections15.multimap.MultiHashMap;
import org.isatools.macros.motiffinder.Motif;

import java.util.*;

/**
 * Created by the ISA team
 *
 * @author Eamonn Maguire (eamonnmag@gmail.com)
 *         <p/>
 *         Date: 19/09/2012
 *         Time: 11:02
 */
public class MotifSelectionAlgorithmImpl implements MotifSelectionAlgorithm {

    public List<Motif> analyseMotifs(Map<String, Motif> motifs) {
        MotifStatCalculator motifStatCalculator = new MotifStatCalculator();

        // vectors are thread-safe, so we can safely work on them in a threaded environment.
        Map<String, Motif> vector = new HashMap<String, Motif>();
        vector.putAll(motifs);
        motifStatCalculator.analyseMotifs(vector);

        Map<String, Motif> overRepresentedBlocks = motifStatCalculator.getOverRepresentedBlocks(vector);

        // check which macros can be removed due to presence in larger macros e.g. path length 3 macros that contribute to path length 4 ones.

        overRepresentedBlocks = filterOutRedundantMotifs(overRepresentedBlocks, motifStatCalculator);


        ArrayList<Motif> resultList = new ArrayList<Motif>(overRepresentedBlocks.values());
        Collections.sort(resultList);
        return resultList;
    }

    // todo: there are still problems in filtering.
    private Map<String, Motif> filterOutRedundantMotifs(Map<String, Motif> toFilter, MotifStatCalculator motifStatCalculator) {
        // find sub motifs and remove those which have a lower MSP and/or whose sub occurrences cause the numbers
        // for the motif to be lower than the threshold.
        MultiHashMap<Integer, String> partitionedMotifMap = partitionMotifsByLength(toFilter);

        Map<String, Set<Long>> motifNodes = new HashMap<String, Set<Long>>();
        // remove sub-parts of motifs. motifs which are intermediary motifs before the final version was built.

        for (int key : partitionedMotifMap.keySet()) {
            // we do this for each partition.
            for (String motif : partitionedMotifMap.get(key)) {

                if (toFilter.containsKey(motif)) {

                    if (!motifNodes.containsKey(motif)) {
                        motifNodes.put(motif, MotifProcessingUtils.flattenNodeIds(toFilter.get(motif).getNodesInMotif()));
                    }

                    for (String candidate : partitionedMotifMap.get(key)) {
                        if (!motif.equals(candidate)) {
                            if (!motifNodes.containsKey(candidate)) {
                                motifNodes.put(candidate, MotifProcessingUtils.flattenNodeIds(toFilter.get(candidate).getNodesInMotif()));
                            }

                            if (motifNodes.get(motif).containsAll(motifNodes.get(candidate))) {
                                toFilter.remove(candidate);
                            }
                        }
                    }
                }
            }
        }

//        Set<String> removed = new HashSet<String>();
//        for (int key : partitionedMotifMap.keySet()) {
//            for (String motif : partitionedMotifMap.get(key)) {
//                String tmpMotif = motif.replaceAll("}", "");
//                for (int nextKey = key + 1; partitionedMotifMap.containsKey(nextKey) && !removed.contains(motif); nextKey++) {
//                    for (String parentMotif : partitionedMotifMap.get(nextKey)) {
//                        if (parentMotif.contains(tmpMotif)) {
//                            int parentMotifUsage = toFilter.get(parentMotif).getCumulativeUsage();
//                            Motif motifToReduce = toFilter.get(motif);
//                            motifToReduce.setCumulativeUsage(motifToReduce.getCumulativeUsage() - parentMotifUsage);
//                            if (!motifStatCalculator.isMotifOverrepresented(motifToReduce)) {
//                                toFilter.remove(motif);
//                                removed.add(motif);
//                                break;
//                            } else {
//                                motifToReduce.setCumulativeUsage(motifToReduce.getCumulativeUsage() + parentMotifUsage);
//                            }
//                        }
//                    }
//                }
//            }
//        }

        return toFilter;
    }

    private MultiHashMap<Integer, String> partitionMotifsByLength(Map<String, Motif> motifs) {
        MultiHashMap<Integer, String> partitionedMotifMap = new MultiHashMap<Integer, String>();

        for (String key : motifs.keySet()) {
            partitionedMotifMap.put(MotifProcessingUtils.getNumberOfGroupsInMotifString(key), key);
        }

        return partitionedMotifMap;
    }
}
