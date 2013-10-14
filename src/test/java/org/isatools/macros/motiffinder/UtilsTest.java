package org.isatools.macros.motiffinder;

import org.isatools.macros.utils.MotifProcessingUtils;
import org.junit.Test;

import java.util.Set;

import static junit.framework.Assert.assertTrue;

/**
 * Created by the ISA team
 *
 * @author Eamonn Maguire (eamonnmag@gmail.com)
 *         <p/>
 *         Date: 31/10/2012
 *         Time: 07:57
 */
public class UtilsTest {

    @Test
    public void testRegexFinder() {
        String testString = "typeA(1):{REL_B#typeB:23(1):{REL_A#typeD:25(1)},REL_B#typeC:24(1):{REL_A#typeD:25(1)}}";

        String result = MotifProcessingUtils.findAndCollapseMergeEvents(testString);

        System.out.println(result);

        assertTrue("Result doesn't contain the reference assignment :(", result.contains("typeD>ref_typeD"));
        assertTrue("Result doesn't contain the reference :(", result.contains("ref_typeD"));
    }

    @Test
    public void testIDExtractor() {
        String testString = "typeA:27(1):{REL_A#typeB:28(1):{REL_A#typeC:30(1):{REL_A#typeD:32(1):{REL_A#typeE:34(1)}},REL_A#typeC:31(1):{REL_A#typeD:33(1):{REL_A#typeE:34(1)}}}}";

        Set<Long> ids = MotifProcessingUtils.getNodeIdsInString(testString);
        for (Long id : ids) {
            System.out.println(id);
        }

        assertTrue("Node ids found not as expected", ids.size() == 7);
    }

    @Test
    public void testSameStartEndNodes() {
        String testString = "typeA:{REL_B#typeB:{REL_A#typeD>ref_typeD},REL_B#typeC:{REL_A#ref_typeD}}";
        boolean valid = MotifProcessingUtils.isMotifGood(testString);
        assertTrue("Oh dear, this motif isn't valid and it should be.", valid);

        String testString2 = "typeA:{REL_A#typeB:{REL_A#typeC>ref_typeC,REL_A#typeB:{REL_A#ref_typeC},REL_A#typeB:{REL_A#ref_typeC},REL_A#typeB:{REL_A#ref_typeC},REL_A#typeB:{REL_A#ref_typeC},REL_A#typeB:{REL_A#ref_typeC}}}";
        valid = MotifProcessingUtils.isMotifGood(testString2);
        assertTrue("Oh dear, this motif isn't valid and it should be.", valid);

        String testString3 = "Assay Name:{" +
                "DERIVES#Raw Data File:{" +
                    "TRANSFORMED_BY#Protocol REF:{" +
                        "DERIVES#Data Transformation Name>ref_Data Transformation Name:{" +
                            "DERIVES#Derived Data File>ref_Derived Data File}}," +
                "DERIVES#Raw Data File:{" +
                    "TRANSFORMED_BY#Protocol REF:{" +
                        "DERIVES#ref_Data Transformation Name:{" +
                            "DERIVES#ref_Derived Data File}}}," +
                "DERIVES#Raw Data File:{" +
                    "TRANSFORMED_BY#Protocol REF:{" +
                        "DERIVES#ref_Data Transformation Name:{" +
                            "DERIVES#ref_Derived Data File}}}," +
                "DERIVES#Raw Data File:{" +
                    "TRANSFORMED_BY#Protocol REF:{" +
                        "DERIVES#ref_Data Transformation Name:{" +
                            "DERIVES#ref_Derived Data File}}}," +
                "DERIVES#Raw Data File:{" +
                    "TRANSFORMED_BY#Protocol REF:{" +
                        "DERIVES#ref_Data Transformation Name:{" +
                            "DERIVES#ref_Derived Data File}}}," +
                "DERIVES#Raw Data File:{" +
                    "TRANSFORMED_BY#Protocol REF:{" +
                        "DERIVES#ref_Data Transformation Name:{" +
                            "DERIVES#ref_Derived Data File}}}}}";

        valid = MotifProcessingUtils.isMotifGood(testString3);
        assertTrue("Oh dear, this motif isn't valid and it should be.", valid);

    }

    @Test
    public void testDifferentEndNodes() {
        String testString = "typeA(1):{REL_B#type B(1):{REL_A#type E(1)},REL_B#type C(1):{REL_A#type D(1)}}";
        boolean valid = MotifProcessingUtils.isMotifGood(testString);
        assertTrue("Oh dear, this motif is valid and it shouldn't be.", !valid);

    }

    @Test
    public void testCollapseEvent() {
        String testString = "Labeled Extract Name:78:{" +
                    "DERIVES#Label:79:{" +
                        "DERIVES#MS Assay Name:66:{" +
                            "DERIVES#Raw Spectral Data File:11:{" +

                                "DERIVES#Normalization Name:12:{" +
                                    "DERIVES#Protein Assignment File:13:{" +
                                        "DERIVES#Peptide Assignment File:14:{" +
                                            "DERIVES#Post Translational Modification Assignment File:15:{" +
                                                "DERIVES#Data Transformation Name:16," +
                                                "DERIVES#Data Transformation Name:46," +
                                                "DERIVES#Data Transformation Name:68}}}}," +
                                "DERIVES#Normalization Name:45:{" +
                                    "DERIVES#Protein Assignment File:13:{" +
                                        "DERIVES#Peptide Assignment File:14:{" +
                                            "DERIVES#Post Translational Modification Assignment File:15:{" +
                                                    "DERIVES#Data Transformation Name:16," +
                                                    "DERIVES#Data Transformation Name:46," +
                                                    "DERIVES#Data Transformation Name:68}}}}," +
                                "DERIVES#Normalization Name:67:{" +
                                    "DERIVES#Protein Assignment File:13:{" +
                                        "DERIVES#Peptide Assignment File:14:{" +
                                            "DERIVES#Post Translational Modification Assignment File:15:{" +
                                                "DERIVES#Data Transformation Name:16," +
                                                "DERIVES#Data Transformation Name:46," +
                                                "DERIVES#Data Transformation Name:68}}}}}}," +
                     "DERIVES#Label:80:{" +
                            "DERIVES#MS Assay Name:66:{DERIVES#Raw Spectral Data File:11:{DERIVES#Normalization Name:12:{DERIVES#Protein Assignment File:13:{DERIVES#Peptide Assignment File:14:{DERIVES#Post Translational Modification Assignment File:15:{DERIVES#Data Transformation Name:16,DERIVES#Data Transformation Name:46,DERIVES#Data Transformation Name:68}}}},DERIVES#Normalization Name:45:{DERIVES#Protein Assignment File:13:{DERIVES#Peptide Assignment File:14:{DERIVES#Post Translational Modification Assignment File:15:{DERIVES#Data Transformation Name:16,DERIVES#Data Transformation Name:46,DERIVES#Data Transformation Name:68}}}},DERIVES#Normalization Name:67:{DERIVES#Protein Assignment File:13:{DERIVES#Peptide Assignment File:14:{DERIVES#Post Translational Modification Assignment File:15:{DERIVES#Data Transformation Name:16,DERIVES#Data Transformation Name:46,DERIVES#Data Transformation Name:68}}}}}}}}}";

        String collapsed = MotifProcessingUtils.findAndCollapseMergeEvents(testString);
        System.out.println(collapsed);
    }

}
