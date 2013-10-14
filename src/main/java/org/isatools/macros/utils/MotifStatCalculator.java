package org.isatools.macros.utils;

import org.apache.commons.math.stat.descriptive.moment.Mean;
import org.apache.commons.math.stat.descriptive.moment.StandardDeviation;
import org.apache.commons.math.stat.descriptive.rank.Max;
import org.apache.commons.math.stat.descriptive.rank.Min;
import org.isatools.macros.motiffinder.Motif;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by the ISA team
 *
 * @author Eamonn Maguire (eamonnmag@gmail.com)
 *         <p/>
 *         Date: 08/06/2012
 *         Time: 16:26
 */
public class MotifStatCalculator {

    private Map<Integer, Double> maxZScores, minZScores;

    public MotifStatCalculator() {
        maxZScores = new HashMap<Integer, Double>();
        minZScores = new HashMap<Integer, Double>();
    }

    public void analyseMotifs(Map<String, Motif> motifs) {

        double[] usages = new double[motifs.size()];
        double[] workflowAppearance = new double[motifs.size()];
        double[] msp = new double[motifs.size()];


        int index = 0;
        for (Motif motif : motifs.values()) {

            usages[index] = motif.getCumulativeUsage();
            workflowAppearance[index] = motif.getWorkflowOccurrence();
            msp[index] = motif.getTotalNodesInvolved();
            index++;
        }

        performCalculation(msp, MotifStats.MSP);
        performCalculation(usages, MotifStats.USAGE);
        performCalculation(workflowAppearance, MotifStats.WORKFLOW);
    }

    public Map<String, Motif> getOverRepresentedBlocks(Map<String, Motif> motifs) {

        Map<String, Motif> overRepresentedBlocks = new HashMap<String, Motif>();
        for (String key : motifs.keySet()) {
            Motif motif = motifs.get(key);
            motif.setScore(calculateMotifScore(motif));
            if (isMotifOverrepresented(motif)) {
                overRepresentedBlocks.put(key, motif);
            }
        }
        return overRepresentedBlocks;
    }

    public boolean isMotifOverrepresented(Motif motif) {
        return motif.getScore() > 0;
    }

    /**
     * Normalises the Z-Score to the range -1 to 1 with 0 being about the mean.
     *
     * @param value  - observed value
     * @param zScore - previously calculated z-score
     * @param type   - type of calculation being performed.
     * @return
     */
    private double mapToRange(double value, double zScore, int type) {

        if (type == MotifStats.WORKFLOW) {
            if (value < MotifStats.getMeanWorkflowAppearance()) {
                if (!minZScores.containsKey(type)) {
                    minZScores.put(type, (MotifStats.getMinWorkflowAppearance() - MotifStats.getMeanWorkflowAppearance()) / MotifStats.getStdDeviationWorkflowUsage());
                }
                return minValue(type) == value ? -1 : (zScore / minValue(type)) * -1;
            } else {
                if (!maxZScores.containsKey(type)) {
                    maxZScores.put(type, (MotifStats.getMaxWorkflowAppearance() - MotifStats.getMeanWorkflowAppearance()) / MotifStats.getStdDeviationWorkflowUsage());
                }
                return maxValue(type) == value ? 1 : zScore / maxValue(type);
            }
        } else if (type == MotifStats.USAGE) {
            if (value < MotifStats.getMeanUsage()) {
                if (!minZScores.containsKey(type)) {
                    minZScores.put(type, (MotifStats.getMinUsage() - MotifStats.getMeanUsage()) / MotifStats.getStdDeviationUsage());
                }
                return minValue(type) == value ? -1 : (zScore / minValue(type)) * -1;
            } else {
                if (!maxZScores.containsKey(type)) {
                    maxZScores.put(type, (MotifStats.getMaxUsage() - MotifStats.getMeanUsage()) / MotifStats.getStdDeviationUsage());
                }
                return maxValue(type) == value ? 1 : zScore / maxValue(type);
            }
        } else if (type == MotifStats.MSP) {
            if (value < MotifStats.getMeanMSP()) {
                if (!minZScores.containsKey(type)) {
                    minZScores.put(type, (MotifStats.getMinMSP() - MotifStats.getMeanMSP()) / MotifStats.getStdDeviationMSP());
                }
                return minValue(type) == value ? -1 : (zScore / minValue(type)) * -1;
            } else {
                if (!maxZScores.containsKey(type)) {
                    maxZScores.put(type, (MotifStats.getMaxMSP() - MotifStats.getMeanMSP()) / MotifStats.getStdDeviationMSP());
                }
                return maxValue(type) == value ? 1 : zScore / maxValue(type);
            }
        }
        return 0;
    }

    private Double minValue(int type) {
        return minZScores.get(type);
    }

    private Double maxValue(int type) {
        return maxZScores.get(type);
    }

    private double calculateMotifScore(Motif motif) {
        double usageZScore = calculateUsageZScore(motif);

        double workflowUsageZScore = calculateWorkflowUsageZScore(motif);

        double mspZScore = calculateMSPZScore(motif);

        return usageZScore + workflowUsageZScore + mspZScore;
    }

    public double calculateUsageZScore(Motif motif) {
        double usageZScore = (motif.getCumulativeUsage() - MotifStats.getMeanUsage()) / MotifStats.getStdDeviationUsage();
        usageZScore = mapToRange(motif.getCumulativeUsage(), usageZScore, MotifStats.USAGE);
        return usageZScore;
    }

    public double calculateWorkflowUsageZScore(Motif motif) {
        double workflowUsageZScore = (motif.getWorkflowOccurrence() - MotifStats.getMeanWorkflowAppearance()) / MotifStats.getStdDeviationWorkflowUsage();
        workflowUsageZScore = mapToRange(motif.getWorkflowOccurrence(), workflowUsageZScore, MotifStats.WORKFLOW);
        return workflowUsageZScore;
    }

    public double calculateMSPZScore(Motif motif) {
        double mspZScore = calculateMotifMSP(motif)/MotifStats.getStdDeviationMSP();
        mspZScore = mapToRange(motif.getTotalNodesInvolved(), mspZScore, MotifStats.MSP);
        return mspZScore;
    }

    public static double calculateMotifMSP(Motif motif) {
        return (motif.getTotalNodesInvolved() - MotifStats.getMeanMSP());
    }

    private void performCalculation(double[] values, int type) {

        Mean meanCalculator = new Mean();
        double mean = meanCalculator.evaluate(values);

        Max maxCalculator = new Max();
        double max = maxCalculator.evaluate(values);

        Min minCalculator = new Min();
        double min = minCalculator.evaluate(values);

        StandardDeviation standardDeviationCalculator = new StandardDeviation();
        double stdDeviation = standardDeviationCalculator.evaluate(values);

        if (type == MotifStats.WORKFLOW) {
            MotifStats.setMinWorkflowAppearance(min);
            MotifStats.setMaxWorkflowAppearance(max);
            MotifStats.setMeanWorkflowAppearance(mean);
            MotifStats.setStdDeviationWorkflowUsage(stdDeviation);
        } else if (type == MotifStats.USAGE) {
            MotifStats.setMinUsage(min);
            MotifStats.setMaxUsage(max);
            MotifStats.setMeanUsage(mean);
            MotifStats.setStdDeviationUsage(stdDeviation);
        } else if (type == MotifStats.MSP) {
            MotifStats.setMinMSP(min);
            MotifStats.setMaxMSP(max);
            MotifStats.setMeanMSP(mean);
            MotifStats.setStdDeviationMSP(stdDeviation);
        }
    }
}
