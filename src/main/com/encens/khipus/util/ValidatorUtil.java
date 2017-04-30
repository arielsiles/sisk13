package com.encens.khipus.util;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * ValidatorUtil
 *
 * @author
 * @version 2.0
 */
public class ValidatorUtil {

    private static Pattern QUESTION_SYMBOL_PATTERN = Pattern.compile("(\\?(.*))", Pattern.CASE_INSENSITIVE);

    public static boolean isEmptyOrNull(Collection collection) {
        return (collection == null || collection.isEmpty() || collection.size() == 0);
    }

    public static boolean isEmptyOrNull(Object[] array) {
        return (array == null || array.length == 0);
    }

    public static boolean isEmptyOrNull(Map map) {
        return (map == null || map.isEmpty() || map.size() == 0);
    }


    /**
     * &ltp&gtChecks if the field isn't null and length of the field is greater than zero not
     * including whitespace.</p>
     *
     * @param value The value validation is being performed on.
     * @return validation result
     */
    public static boolean isBlankOrNull(String value) {
        return ((value == null) || (value.trim().length() == 0) || "null".equalsIgnoreCase(value));
    }

    /**
     * &ltp&gtChecks if the value can safely be converted to a byte primitive.</p>
     *
     * @param value The value validation is being performed on.
     * @return validation result
     */
    public static boolean isByte(String value) {
        return (formatByte(value) != null);
    }

    /**
     * &ltp&gtChecks if the value can safely be converted to a short primitive.</p>
     *
     * @param value The value validation is being performed on.
     * @return validation result
     */
    public static boolean isShort(String value) {
        return (formatShort(value) != null);
    }

    /**
     * &ltp&gtChecks if the value can safely be converted to a int primitive.</p>
     *
     * @param value The value validation is being performed on.
     * @return validation result
     */
    public static boolean isInt(String value) {
        return (formatInt(value) != null);
    }

    /**
     * &ltp&gtChecks if the value can safely be converted to a long primitive.</p>
     *
     * @param value The value validation is being performed on.
     * @return validation result
     */
    public static boolean isLong(String value) {
        return (formatLong(value) != null);
    }

    /**
     * &ltp&gtChecks if the value can safely be converted to a float primitive.</p>
     *
     * @param value The value validation is being performed on.
     * @return validation result
     */
    public static boolean isFloat(String value) {
        return (formatFloat(value) != null);
    }

    /**
     * &ltp&gtChecks if the value can safely be converted to a double primitive.</p>
     *
     * @param value The value validation is being performed on.
     * @return validation result
     */
    public static boolean isDouble(String value) {
        return (formatDouble(value) != null);
    }


    /**
     * &ltp&gtChecks if the value can safely be converted to a BigDecimal.</p>
     *
     * @param value The value validation is being performed on.
     * @return validation result
     */
    public static boolean isBigDecimal(String value) {
        return (formatBigDecimal(value) != null);
    }


    /**
     * &ltp&gtChecks if the value has the question symbol (?).</p>
     *
     * @param value The value validation is being performed on.
     * @return validation result
     */
    public static Boolean hasQuestionSymbol(String value) {
        if (isBlankOrNull(value)) {
            return false;
        }
        Matcher questionMatcher = QUESTION_SYMBOL_PATTERN.matcher(value);
        return questionMatcher.find();
    }


    /**
     * Checks if the value can safely be converted to a byte primitive.
     *
     * @param value The value validation is being performed on.
     * @return format result
     */
    public static Byte formatByte(String value) {
        if (value == null) {
            return null;
        }

        try {
            return new Byte(value);
        } catch (NumberFormatException e) {
            return null;
        }

    }

    /**
     * Checks if the value can safely be converted to a short primitive.
     *
     * @param value The value validation is being performed on.
     * @return format result
     */
    public static Short formatShort(String value) {
        if (value == null) {
            return null;
        }

        try {
            return new Short(value);
        } catch (NumberFormatException e) {
            return null;
        }

    }

    /**
     * Checks if the value can safely be converted to a int primitive.
     *
     * @param value The value validation is being performed on.
     * @return format result
     */
    public static Integer formatInt(String value) {
        if (isBlankOrNull(value)) {
            return null;
        }

        try {
            return new Integer(value);
        } catch (NumberFormatException e) {
            return null;
        }

    }

    /**
     * Checks if the value can safely be converted to a long primitive.
     *
     * @param value The value validation is being performed on.
     * @return format result
     */
    public static Long formatLong(String value) {
        if (value == null) {
            return null;
        }

        try {
            return new Long(value);
        } catch (NumberFormatException e) {
            return null;
        }

    }

    /**
     * Checks if the value can safely be converted to a float primitive.
     *
     * @param value The value validation is being performed on.
     * @return format result
     */
    public static Float formatFloat(String value) {
        if (value == null) {
            return null;
        }

        try {
            return new Float(value);
        } catch (NumberFormatException e) {
            return null;
        }

    }

    /**
     * Checks if the value can safely be converted to a double primitive.
     *
     * @param value The value validation is being performed on.
     * @return format result
     */
    public static Double formatDouble(String value) {
        if (value == null) {
            return null;
        }

        try {
            return new Double(value);
        } catch (NumberFormatException e) {
            return null;
        }

    }

    /**
     * Checks if the value can safely be converted to a BigDecimal.
     *
     * @param value The value validation is being performed on.
     * @return validation result
     */
    public static BigDecimal formatBigDecimal(String value) {
        if (value == null) {
            return null;
        }

        try {
            return new BigDecimal(value);
        } catch (NumberFormatException e) {
            return null;
        }

    }
}

