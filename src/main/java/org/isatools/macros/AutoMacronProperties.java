package org.isatools.macros;

import java.util.HashMap;
import java.util.Map;


public class AutoMacronProperties{

    public static String dataDir = "data/images/";
    public static String png = ".png";


    private static Map<String, Object> properties = new HashMap<String, Object>();

    public static void setProperty(String key, Object value) {
        properties.put(key, value);
    }

    public static Object getProperty(String key) {
        return properties.get(key);
    }

}
