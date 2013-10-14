package org.isatools.macros.utils.fuzzymap;

import org.isatools.macros.utils.fuzzymap.FuzzyHashMap;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

/**
 * Created by the ISA team
 *
 * @author Eamonn Maguire (eamonnmag@gmail.com)
 *         <p/>
 *         Date: 13/09/2012
 *         Time: 10:34
 */
public class FuzzyMapTest {

    @Test
    public void testFuzzyMap() {
        FuzzyHashMap<String, String> testMap = new FuzzyHashMap<String, String>(FuzzyHashMap.PRE_HASHING_METHOD.SOUNDEX);

        testMap.putFuzzy("growth", "protocol1");
        testMap.putFuzzy("grow", "protocol2");
        testMap.putFuzzy("growing", "protocol3");
        testMap.putFuzzy("label", "protocol4");
        testMap.putFuzzy("labeling", "protocol5");
        testMap.putFuzzy("labeled", "protocol6");
        testMap.putFuzzy("Saccharomyces cerevisiae (Baker's yeast)", "material1");

        String value0 = testMap.getFuzzy("growt");
        assertTrue("Value should not be null.", value0.equals("protocol1"));

        String value1 = testMap.getFuzzy("growth");
        assertTrue("Value should not be null.", value1.equals("protocol1"));

        String value2 = testMap.getFuzzy("labelin");
        assertTrue("Value should not be null.", value2.equals("protocol5"));

        String value3 = testMap.getFuzzy("label");
        assertTrue("Value should not be null.", value3.equals("protocol4"));

        String value4 = testMap.getFuzzy("saccharomyces");
        assertTrue("Value should not be null.", value4.equals("material1"));

        String value5 = testMap.getFuzzy("Saccharomyces cerevisiae (Baker's yeast)");
        assertTrue("Value should not be null.", value5.equals("material1"));

        String value6 = testMap.getFuzzy("Saccharomyces cerevisiae");
        assertTrue("Value should not be null.", value6.equals("material1"));
    }

}