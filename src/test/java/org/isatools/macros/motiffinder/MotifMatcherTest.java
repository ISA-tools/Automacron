package org.isatools.macros.motiffinder;

import org.junit.Test;

import java.util.Collections;

import static junit.framework.Assert.assertTrue;

/**
 * Created by the ISA team
 *
 * @author Eamonn Maguire (eamonnmag@gmail.com)
 *         <p/>
 *         Date: 31/10/2012
 *         Time: 22:18
 */
public class MotifMatcherTest {

    @Test
    public void testMotifPatternMatchingLinear() {
        String linearPattern = "typeA*\\(\\d\\)+\\:\\{REL_B#typeB\\(\\d+\\):\\{REL_A#typeD\\(\\d+\\)\\}\\}";

        String linearValue= "typeA(1):{" +
                                  "REL_B#typeB(1):{" +
                                      "REL_A#typeD(1)}}";
        
        
        boolean matches = MotifMatcher.isMotifATarget(linearValue, Collections.singleton(linearPattern));

        assertTrue("Doesn't match!!", matches);
    }

    @Test
    public void testMotifPatternMatchingBranch() {

        String branchPattern = "typeA*\\(\\d\\)+\\:\\{REL_B#typeB\\(\\d+\\),REL_B#typeB\\(\\d+\\)\\}";
        String branchValue = "typeA(1):{" +
                                        "REL_B#typeB(1)," +
                                        "REL_B#typeB(1)}";

        boolean matches = MotifMatcher.isMotifATarget(branchValue, Collections.singleton(branchPattern));
        assertTrue("Doesn't match!!", matches);
    }

    @Test
    public void testMotifPatternMatchingMerge() {

    }

    @Test
    public void testMotifPatternMatchingComplex() {
        
        String branchAndMergePattern = "typeA*\\:\\{REL_B#typeB,REL_B#typeB\\}";
        
        String branchAndMergeValue = "typeA:{" +
                            "REL_B#typeB," +
                            "REL_B#typeB}";


        boolean matches = MotifMatcher.isMotifATarget(branchAndMergeValue, Collections.singleton(branchAndMergePattern));
        assertTrue("Doesn't match!!", matches);
    }
}
