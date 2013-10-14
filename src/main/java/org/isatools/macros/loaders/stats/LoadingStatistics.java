package org.isatools.macros.loaders.stats;

import au.com.bytecode.opencsv.CSVReader;
import org.apache.commons.collections15.OrderedMap;
import org.apache.commons.collections15.map.ListOrderedMap;
import org.apache.commons.math.MathException;
import org.apache.commons.math.distribution.NormalDistribution;
import org.apache.commons.math.distribution.NormalDistributionImpl;
import org.apache.commons.math.stat.descriptive.moment.Mean;
import org.apache.commons.math.stat.descriptive.moment.StandardDeviation;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by the ISA team
 *
 * @author Eamonn Maguire (eamonnmag@gmail.com)
 *         <p/>
 *         Date: 29/11/2012
 *         Time: 10:45
 */
public class LoadingStatistics {

    private static Map<String, GraphStats> graphStatsMap = new HashMap<String, GraphStats>();


    public static void addStat(String graphIdentifier, int vertexCount, int edgeCount) {
        graphStatsMap.put(graphIdentifier, new GraphStats(edgeCount, vertexCount));
    }

    public static Map<String, GraphStats> getGraphStatsMap() {
        return graphStatsMap;
    }

    public static Map<String, GraphStats> loadPreviousStatistics(File statFile) throws FileNotFoundException {
        CSVReader reader = new CSVReader(new FileReader(statFile), '\t');

        Map<String, GraphStats> stats = new HashMap<String, GraphStats>();

        try {
            // skip first line...
            reader.readNext();

            String[] nextLine;
            while ((nextLine = reader.readNext()) != null) {
                stats.put(nextLine[0],
                        new GraphStats(
                                Integer.valueOf(nextLine[1]),
                                Integer.valueOf(nextLine[2])));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return stats;
    }

    public static void analyseStats(Map<String, GraphStats> graphStatsMap) {


        double[] values = new double[graphStatsMap.size()];

        OrderedMap<Integer, Integer> bins = new ListOrderedMap<Integer, Integer>();
        bins.put(100, 0);
        bins.put(200, 0);
        bins.put(300, 0);
        bins.put(400, 0);
        bins.put(500, 0);
        bins.put(600, 0);
        bins.put(700, 0);
        bins.put(800, 0);
        bins.put(900, 0);
        bins.put(1000, 0);
        bins.put(1100, 0);
        bins.put(1200, 0);
        bins.put(1300, 0);
        bins.put(1400, 0);
        bins.put(1500, 0);
        bins.put(1600, 0);
        bins.put(1700, 0);
        bins.put(1800, 0);
        bins.put(1900, 0);
        bins.put(2000, 0);
        bins.put(2100, 0);
        bins.put(2200, 0);
        bins.put(2300, 0);
        bins.put(2400, 0);

        int count = 0;
        for (String graph : graphStatsMap.keySet()) {
            values[count] = graphStatsMap.get(graph).getVertexCount();

            for (Integer bin : bins.keySet()) {
                if (values[count] <= bin) {
                    bins.put(bin, bins.get(bin) + 1);
                }
            }

            count++;
        }

        Mean mean = new Mean();
        double meanVertexCount = mean.evaluate(values);

        double normalDist = calculateNormalDistributionScore(values);


        System.out.println("Mean is: " + meanVertexCount);
        System.out.println("Normal distribution is: " + normalDist);


        for (Integer bin : bins.keySet()) {
            int value = bins.get(bin);

            if (bins.containsKey(bin-100)) {
                value = value - bins.get(bin - 100);
            }
            System.out.println(bin + " - " + value + " - (" + String.format("%.2f", ((double)bins.get(bin)/values.length)*100) + "%)");
        }
    }

    private static double calculateStandardDeviation(double[] values) {
        StandardDeviation dev = new StandardDeviation();
        return dev.evaluate(values);
    }

    public static double calculateNormalDistributionScore(double[] values) {
        Mean mean = new Mean();
        double meanValue = mean.evaluate(values);
        double stdDev = calculateStandardDeviation(values);
        if (stdDev < 0.000001) {
            return 1;
        } else {
            NormalDistribution distribution = new NormalDistributionImpl(meanValue, stdDev);
            try {
                // we are calculating the probability that the value falls within +-1 standard deviations of the mean value.
                // This is normally calculated on the std deviation, however, since that changes per test, calculating on a
                // constant value 1 is better for getting a normalised value.
                return distribution.cumulativeProbability(meanValue - stdDev, meanValue + stdDev);
            } catch (MathException e) {
                return 0;
            }
        }
    }

}
