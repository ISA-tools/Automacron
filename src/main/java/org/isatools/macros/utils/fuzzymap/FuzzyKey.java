package org.isatools.macros.utils.fuzzymap;

/**
 * Created by topacv - http://sourceforge.net/projects/fuzzyhashmap/
 *
 * @author topacv (http://sourceforge.net/users/topacv)
 */
public class FuzzyKey {
    private String strKey;
    private int threshold;
    private FuzzyHashMap.PRE_HASHING_METHOD method;

    public FuzzyKey(String key, int threshold) {
        this(key, threshold, FuzzyHashMap.PRE_HASHING_METHOD.SOUNDEX);
    }

    public FuzzyKey(String key, int threshold, FuzzyHashMap.PRE_HASHING_METHOD method) {
        this.strKey = key;
        this.threshold = threshold;
        this.method = method;
    }

    public String getKey() {
        return strKey;
    }

    public int hashCode() {
        // PRE Hashing
        String toBeHashed = "";
        if (method != null) {
            if (method == FuzzyHashMap.PRE_HASHING_METHOD.SOUNDEX) {
                // SOUNDEX PreHasing function
                toBeHashed = StringMetrics.soundexHash(strKey);
            } else if (method.getType().equals(FuzzyHashMap.FIRST_TYPE)) {
                // Starts with (n) PreHasing function
                int nrOfLetters = method.getValue();
                if (strKey.length() > nrOfLetters) {
                    // cut the word to "nrOfLetters" length, starting form the beginning
                    toBeHashed = strKey.substring(0, nrOfLetters);
                }
            }
        }
        // Hashing
        return toBeHashed.hashCode();
    }

    public boolean equals(Object fuzzyKey) {

        String searchedKey = null;

        if (fuzzyKey instanceof FuzzyKey) {
            searchedKey = ((FuzzyKey) fuzzyKey).getKey();
        } else if (fuzzyKey instanceof String) {
            searchedKey = (String) fuzzyKey;
        }

        if (searchedKey != null) {
            // todo should probably convert to lowercase on entry, otherwise, it's quite an overhead
            searchedKey = searchedKey.toLowerCase();
            strKey = strKey.toLowerCase();

            // Check for exact match before calculating words similarity
            if (searchedKey.equals(strKey)) {

                return true;
            }

            if (strKey.length() < 4) {
                return false;
            }

            if(searchedKey.contains(strKey) || strKey.contains(searchedKey)) {
                return true;
            }

            // Check words similarity as a last resort.
            if (StringMetrics.computeLevenshteinDistance(strKey, searchedKey) <= threshold) {
                return true;
            }
        }
        return false;
    }


    public String toString() {
        return strKey + " " + threshold;
    }

}
