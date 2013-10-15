package org.isatools.macros.motiffinder;

import org.isatools.macros.gui.DBGraph;
import org.isatools.macros.utils.MotifProcessingUtils;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by the ISA team
 *
 * @author Eamonn Maguire (eamonnmag@gmail.com)
 *         Date: 25/10/2012
 *         Time: 08:57
 */
public class TargetedMotifFinderImpl extends AlternativeCompleteMotifFinder {


    private Set<String> targetedMotifPatterns;

    public TargetedMotifFinderImpl(Set<String> targetedMotifPatterns) {
        super(10);
        this.targetedMotifPatterns = targetedMotifPatterns;
    }

    /**
     * This method overrides the superclass method so that only
     * those motifs matching in a set of target motifs, @see targetedMotifPatterns are added.
     * @param dbGraph - graph being analysed
     * @param motif - motif to be added.
     */
    protected void addToMotifs(DBGraph dbGraph, Motif motif) {
        // and we're getting weird results.
        // the problem would appear to be here. More motifs are being added than should be.
        String motifBlockAsString = motif.getStringRepresentation();
        motifBlockAsString = MotifProcessingUtils.findAndCollapseMergeEvents(motifBlockAsString);

        Motif correspondingBlock = findMotifBlock(motifBlockAsString);

        if (targetedMotifPatterns.contains(motifBlockAsString)) {
            if (correspondingBlock == null) {
                if (!motifBlockAsString.isEmpty()) {
                    motifs.put(motifBlockAsString, motif);
                }
            } else {
                // correspondingBlock.addMotifsToBlock(motif.getMotifs());
                // get an id back from addition of motif motifs indicating if new nodes have been added or not.
                // only increment if no new nodes have been added
                correspondingBlock.addRelatedMotif(motif);
                correspondingBlock.incrementUsage();
            }

            if (!addedMotifsForWorkflowOccurrence.containsKey(dbGraph)) {
                addedMotifsForWorkflowOccurrence.put(dbGraph, new HashSet<String>());
            }

            if (!addedMotifsForWorkflowOccurrence.get(dbGraph).contains(motifBlockAsString)) {
                addedMotifsForWorkflowOccurrence.get(dbGraph).add(motifBlockAsString);
                motifs.get(motifBlockAsString).incrementWorkflowOccurrence();
            }
        }
    }

}
