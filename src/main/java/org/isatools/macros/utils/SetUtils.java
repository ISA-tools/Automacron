package org.isatools.macros.utils;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;


public class SetUtils {

    public static <E> Set<E> flattenCollection(Collection<Set<E>> toFlatten) {
        Set<E> toReturn = new HashSet<E>();

        for(Set<E> set : toFlatten) {
            toReturn.addAll(set);
        }

        return toReturn;
    }

}
