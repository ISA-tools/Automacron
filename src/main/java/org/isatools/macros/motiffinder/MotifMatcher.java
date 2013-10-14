package org.isatools.macros.motiffinder;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Created by the ISA team
 *
 * @author Eamonn Maguire (eamonnmag@gmail.com)
 *         <p/>
 *         Date: 31/10/2012
 *         Time: 05:13
 */
public class MotifMatcher {

    public static boolean isMotifATarget(String representation, Set<String> targetMotifs) {

        for(String targetMotifPattern  : targetMotifs){
            if(representation.matches(targetMotifPattern)){
                return true;
            }
        }

        return false;
    }

   

}
