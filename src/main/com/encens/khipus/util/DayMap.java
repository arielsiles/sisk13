package com.encens.khipus.util;

import java.util.Hashtable;
import java.util.Map;

/**
 * @author EDSON_TERCEROS
 *         Date: 07-jul-2009
 *         Time: 16:20:56
 */
public class DayMap {
    private DayMap() {
    }

    public static String dayIntToString(Integer key) {
        Map dayIntToString;
        dayIntToString = new Hashtable();
        dayIntToString.put(2, "LUNES");
        dayIntToString.put(3, "MARTES");
        dayIntToString.put(4, "MIERCOLES");
        dayIntToString.put(5, "JUEVES");
        dayIntToString.put(6, "VIERNES");
        dayIntToString.put(7, "SABADO");
        dayIntToString.put(1, "DOMINGO");
        return (String) dayIntToString.get(key);

    }

    public static int dayStringToInt(String key) {
        Map dayStringToInt;
        dayStringToInt = new Hashtable();
        dayStringToInt.put("LUNES", 2);
        dayStringToInt.put("MARTES", 3);
        dayStringToInt.put("MIERCOLES", 4);
        dayStringToInt.put("JUEVES", 5);
        dayStringToInt.put("VIERNES", 6);
        dayStringToInt.put("SABADO", 7);
        dayStringToInt.put("DOMINGO", 1);
        return (Integer) dayStringToInt.get(key);
    }
}