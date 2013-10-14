package org.isatools.macros.utils;

/**
 * Created by the ISA team
 *
 * @author Eamonn Maguire (eamonnmag@gmail.com)
 *         Date: 25/07/2012
 *         Time: 09:59
 */
public class MotifStats {

    public static final int USAGE = 0;
    public static final int WORKFLOW = 1;
    public static final int MSP = 2;

    private static double maxUsage, maxMSP, maxWorkflowAppearance;
    private static double minUsage, minMSP, minWorkflowAppearance;
    private static double stdDeviationUsage, meanUsage;
    private static double stdDeviationWorkflowUsage, meanWorkflowAppearance;
    private static double stdDeviationMSP, meanMSP;
    private static int totalWorkflows;

    public static double getMaxUsage() {
        return maxUsage;
    }

    public static double getMaxZScore(int type) {
        switch (type) {
            case USAGE:
                return (maxUsage - meanMSP) / stdDeviationUsage;
            case WORKFLOW:
                return (maxWorkflowAppearance - meanWorkflowAppearance) / stdDeviationWorkflowUsage;
            case MSP:
                return (maxMSP - meanMSP) / stdDeviationWorkflowUsage;
            default:
                return 0;
        }
    }

    public static double getMinZScore(int type) {
        switch (type) {
            case USAGE:
                return (minUsage - meanMSP) / stdDeviationUsage;
            case WORKFLOW:
                return (minWorkflowAppearance - meanWorkflowAppearance) / stdDeviationWorkflowUsage;
            case MSP:
                return (minMSP - meanMSP) / stdDeviationWorkflowUsage;
            default:
                return 0;
        }
    }

    public static void setMaxUsage(double maxUsage) {
        MotifStats.maxUsage = maxUsage;
    }

    public static double getMinUsage() {
        return minUsage;
    }

    public static void setMinUsage(double minUsage) {
        MotifStats.minUsage = minUsage;
    }

    public static double getMaxMSP() {
        return maxMSP;
    }

    public static void setMaxMSP(double maxMSP) {
        MotifStats.maxMSP = maxMSP;
    }

    public static double getMaxWorkflowAppearance() {
        return maxWorkflowAppearance;
    }

    public static void setMaxWorkflowAppearance(double maxWorkflowAppearance) {
        MotifStats.maxWorkflowAppearance = maxWorkflowAppearance;
    }

    public static double getMinMSP() {
        return minMSP;
    }

    public static void setMinMSP(double minMSP) {
        MotifStats.minMSP = minMSP;
    }

    public static double getMinWorkflowAppearance() {
        return minWorkflowAppearance;
    }

    public static void setMinWorkflowAppearance(double minWorkflowAppearance) {
        MotifStats.minWorkflowAppearance = minWorkflowAppearance;
    }

    public static double getStdDeviationUsage() {
        return stdDeviationUsage;
    }

    public static void setStdDeviationUsage(double stdDeviationUsage) {
        MotifStats.stdDeviationUsage = stdDeviationUsage;
    }

    public static double getMeanUsage() {
        return meanUsage;
    }

    public static void setMeanUsage(double meanUsage) {
        MotifStats.meanUsage = meanUsage;
    }

    public static void setStdDeviationWorkflowUsage(double stdDeviationWorkflowUsage) {
        MotifStats.stdDeviationWorkflowUsage = stdDeviationWorkflowUsage;
    }

    public static void setMeanWorkflowAppearance(double meanWorkflowAppearance) {
        MotifStats.meanWorkflowAppearance = meanWorkflowAppearance;
    }

    public static double getStdDeviationWorkflowUsage() {
        return stdDeviationWorkflowUsage;
    }

    public static double getMeanWorkflowAppearance() {
        return meanWorkflowAppearance;
    }

    public static double getStdDeviationMSP() {
        return stdDeviationMSP;
    }

    public static void setStdDeviationMSP(double stdDeviationMSP) {
        MotifStats.stdDeviationMSP = stdDeviationMSP;
    }

    public static double getMeanMSP() {
        return meanMSP;
    }

    public static void setMeanMSP(double meanMSP) {
        MotifStats.meanMSP = meanMSP;
    }

    public static void setTotalWorkflows(int size) {
        MotifStats.totalWorkflows = size;
    }

    public static int getTotalWorkflows() {
        return totalWorkflows;
    }
}
