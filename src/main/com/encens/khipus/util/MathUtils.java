package com.encens.khipus.util;

/**
 * MathUtils
 *
 * @author
 * @version 1.1.2
 */
public class MathUtils {
    private MathUtils() {
    }

    /**
     * This method return a result value respective to  equivalent percent, but taking 100 percetn
     * like a base value.
     *
     * @param value             current value thar will be used for the operation
     * @param equivalentPercent the value that is the representation to 100 percent
     * @return Double, a value that was calculated respecrive to equivalentPercent
     */
    public static Double calculeRespectiveToEquivalentPercent(Double value, Integer equivalentPercent) {
        try {
            return value * equivalentPercent / 100;
        } catch (Exception e) {

        }
        return null;
    }
}
