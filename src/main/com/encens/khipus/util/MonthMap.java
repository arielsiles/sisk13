package com.encens.khipus.util;

import java.util.Hashtable;
import java.util.Map;

/**
 * @author EDSON_TERCEROS
 *         Date: 07-jul-2009
 *         Time: 16:20:56
 */
public class MonthMap {
    private MonthMap() {
    }

    public static String monthIntToString(Integer key) {
        Map monthIntToString;
        monthIntToString = new Hashtable();
        monthIntToString.put(0, "ENERO");
        monthIntToString.put(1, "FEBRERO");
        monthIntToString.put(2, "MARZO");
        monthIntToString.put(3, "ABRIL");
        monthIntToString.put(4, "MAYO");
        monthIntToString.put(5, "JUNIO");
        monthIntToString.put(6, "JULIO");
        monthIntToString.put(7, "AGOSTO");
        monthIntToString.put(8, "SEPTIEMBRE");
        monthIntToString.put(9, "OCTUBRE");
        monthIntToString.put(10, "NOVIEMBRE");
        monthIntToString.put(11, "DICIEMBRE");
        return (String) monthIntToString.get(key);

    }

    public static int monthStringToInt(String key) {
        Map monthStringToInt;
        monthStringToInt = new Hashtable();
        monthStringToInt.put("ENERO", 0);
        monthStringToInt.put("FEBRERO", 1);
        monthStringToInt.put("MARZO", 2);
        monthStringToInt.put("ABRIL", 3);
        monthStringToInt.put("MAYO", 4);
        monthStringToInt.put("JUNIO", 5);
        monthStringToInt.put("JULIO", 6);
        monthStringToInt.put("AGOSTO", 7);
        monthStringToInt.put("SEPTIEMBRE", 8);
        monthStringToInt.put("OCTUBRE", 9);
        monthStringToInt.put("NOVIEMBRE", 10);
        monthStringToInt.put("DICIEMBRE", 11);
        return (Integer) monthStringToInt.get(key);
    }
}
