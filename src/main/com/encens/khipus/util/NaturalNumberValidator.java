package com.encens.khipus.util;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

/**
 * @author
 * @version 1.4.1
 */
public class NaturalNumberValidator {

    public static enum ValidationCode {
        INVALID_NUMBER,
        DECIMAL_SYMBOL_PRESENT,
        INVALID_PATTERN_USAGE,
        INVALID_GROUP_SYMBOL_USAGE,
        IS_VALID_NUMBER
    }

    private static final String DIGITS = "0123456789";

    private char decimalSymbol;
    private char groupSymbol;

    private String pattern = null;

    public NaturalNumberValidator(Locale locale, String pattern) {

        DecimalFormat decimalFormat;

        if (null != pattern && !"".equals(pattern.trim())) {
            this.pattern = pattern;

            DecimalFormatSymbols symbols = new DecimalFormatSymbols(locale);
            decimalFormat = new DecimalFormat(pattern, symbols);
        } else {
            decimalFormat = (DecimalFormat) DecimalFormat.getInstance(locale);
        }

        decimalSymbol = decimalFormat.getDecimalFormatSymbols().getDecimalSeparator();
        groupSymbol = decimalFormat.getDecimalFormatSymbols().getGroupingSeparator();
    }


    public ValidationCode validate(String value) {
        if (!isNumber(value)) {
            return ValidationCode.INVALID_NUMBER;
        }

        if (decimalSymbolIsPresent(value)) {
            return ValidationCode.DECIMAL_SYMBOL_PRESENT;
        }

        if (null == pattern) {
            if (groupSymbolIsPresent(value)) {
                return ValidationCode.INVALID_PATTERN_USAGE;
            }

        } else {
            if (groupSymbolIsPresent(value) && !groupSymbolIsValid(value)) {
                return ValidationCode.INVALID_GROUP_SYMBOL_USAGE;
            }
        }

        return ValidationCode.IS_VALID_NUMBER;
    }

    private boolean groupSymbolIsValid(String value) {

        if (value.lastIndexOf(groupSymbol) == value.length() - 1) {
            return false;
        }

        String[] elements = value.split(String.valueOf("(\\" + groupSymbol + ")"));

        for (int i = 0; i < elements.length; i++) {
            String element = elements[i];

            if (i == 0) {
                if (0 == element.length()) {
                    return false;
                }

                continue;
            }

            if (3 != element.length()) {
                return false;
            }
        }

        return true;
    }

    private boolean groupSymbolIsPresent(String value) {
        return -1 != value.indexOf(groupSymbol);
    }

    private boolean decimalSymbolIsPresent(String value) {
        return -1 != value.indexOf(decimalSymbol);
    }

    private boolean isNumber(String value) {

        boolean containAlmostOneDigit = false;

        char[] elements = value.toCharArray();
        for (char element : elements) {
            if (element == decimalSymbol || element == groupSymbol || element == '-') {
                continue;
            }

            if (-1 != DIGITS.indexOf(element)) {
                containAlmostOneDigit = true;
                continue;
            }

            return false;
        }

        return containAlmostOneDigit;
    }
}
