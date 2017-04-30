package com.encens.khipus.util;

import com.encens.khipus.model.BaseModel;
import com.jatun.util.literal.NumberToWord;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.*;

/**
 * This class has utilities for apply formatters
 *
 * @author
 * @version 1.1.5
 */
public class FormatUtils {
    private FormatUtils() {
    }

    private static final String DASH_SEPARATOR = " - ";
    private static final String SPACE_SEPARATOR = " ";
    private static final String PARENTHESES = "()";
    private static final String COMMA_SEPARATOR = " , ";

    /**
     * Translate an String to BigDecimal number.
     *
     * @param cadena       the string that contains the number
     * @param locale       the locale
     * @param maxIntPart   this the max of digits on the integer part
     * @param maxFloatPart this the max of digits on the decimal part
     * @return the bigdecimal number
     */
    public static BigDecimal unformatingDecimalNumber(String cadena, Locale locale, int maxIntPart, int maxFloatPart) {
        BigDecimal result = null;
        NumberFormat numberFormat = settingUpValuesOfFormat(locale, maxIntPart, maxFloatPart);
        if (null != cadena && !"".equals(cadena.trim())) {
            try {
                Number number = numberFormat.parse(cadena);
                result = new BigDecimal(Double.toString(number.doubleValue()));
            } catch (ParseException e) {
            }
        }
        return result;
    }

    /**
     * Validates if the cadena value represents an BigDecimal positive
     *
     * @param cadena       the string that contains the number
     * @param locale       the locale
     * @param maxIntPart   this the max of digits on the integer part
     * @param maxFloatPart this the max of digits on the decimal part
     * @return true if bigdecimal value is positive, false the other wise
     */
    public static boolean isPositiveDecimalNumber(String cadena, Locale locale, int maxIntPart, int maxFloatPart) {
        boolean isPositive = false;
        BigDecimal number = unformatingDecimalNumber(cadena, locale, maxIntPart, maxFloatPart);
        if (number != null && number.floatValue() >= 0) {
            isPositive = true;
        }
        return isPositive;
    }

    /**
     * formatting the object (obj) to a bigDecimal taking into account the locale
     *
     * @param obj          the object that contains the number
     * @param locale       the locale
     * @param maxIntPart   this the max of digits on the integer part
     * @param maxFloatPart this the max of digits on the decimal part
     * @return Object that contains the bigdecimal formatted for the views
     */
    public static Object formatingDecimalNumber(Object obj, Locale locale, int maxIntPart, int maxFloatPart) {
        Object result = obj;

        NumberFormat numberFormat = settingUpValuesOfFormat(locale, maxIntPart, maxFloatPart);
        try {
            Double number = new Double(obj.toString());
            result = numberFormat.format(number.doubleValue());
        } catch (NumberFormatException e) {
        }
        return result;
    }

    /**
     * Validates if the cadena value represents an percent number
     *
     * @param obj          the object that contains the number
     * @param locale       the locale
     * @param maxIntPart   this the max of digits on the integer part
     * @param maxFloatPart this the max of digits on the decimal part
     * @return Object that contains the bigdecimal formatted for the views
     */
    public static Object formatingPercentNumber(Object obj, Locale locale, int maxIntPart, int maxFloatPart) {
        Object result = obj;

        NumberFormat numberFormat = settingUpValuesOfFormat(locale, maxIntPart, maxFloatPart);
        try {
            Double number = new Double(obj.toString());
            result = numberFormat.format(number.doubleValue() * 100);
        } catch (NumberFormatException e) {
        }
        return result;
    }

    /**
     * Validates if the cadena value represents an BigDecimal is valid
     *
     * @param cadena       the string that contains the number
     * @param locale       the locale
     * @param maxIntPart   this the max of digits on the integer part
     * @param maxFloatPart this the max of digits on the decimal part
     * @return true if bigdecimal value is valid, false the other wise
     */
    public static boolean isValidDecimalNumber(String cadena, Locale locale, int maxIntPart, int maxFloatPart) {
        String value = cadena;
        boolean isValid = false;
        Number numero;

        NumberFormat numberFormat = settingUpValuesOfFormat(locale, maxIntPart, maxFloatPart);
        char decimalSeparator = ((DecimalFormat) numberFormat).getDecimalFormatSymbols().getDecimalSeparator();

        if (null != value && !"".equals(value.trim())) {
            try {
                numero = numberFormat.parse(value); //parse the number
                float number = numero.floatValue(); //extracts the float value

                String cad = ((DecimalFormat) numberFormat).format(number);   // formatting the parse number

                int intPart = (int) number; //extracts the int part
                Integer integerPart = new Integer(intPart);

                isValid = true;
                if (new Integer(intPart).toString().length() > maxIntPart) {
                    isValid = false;
                }
                if (!isCorrectParsed(cadena, cad)) {
                    if (integerPart.equals(getDecimalAndThousandthParts(cadena, decimalSeparator).get(1))) {
                        isValid = true;
                    } else {
                        isValid = false;
                    }
                }
                if (isDecimalSymbolValid(cadena, cad, decimalSeparator, maxFloatPart) == -1) {
                    isValid = false;
                }
            } catch (ParseException e) {
            }
        }
        return isValid;
    }

    /**
     * validates directly if source an parsedCad
     *
     * @param source    String that contains the source number
     * @param parsedCad Strinf that contains the parsed number
     * @return true if the source and parsedCad are equals false in otherwise
     */
    private static boolean isCorrectParsed(String source, String parsedCad) {
        boolean result = false;
        if (source.equals(parsedCad)) {
            result = true;
        }
        return result;
    }

    /**
     * validates if the decimal separator is valid
     *
     * @param n             the source string that contains the source number
     * @param n2            the parsed string that contains the pased number
     * @param decimalSymbol decimal separator
     * @param maxFloatPart  this the max of digits on the decimal part
     * @return 1 values if the decimal separator is correct -1 if the decimal separator is incorrect
     *         and 0 if decimal separator doesn't exist
     */
    private static int isDecimalSymbolValid(String n, String n2, char decimalSymbol, int maxFloatPart) {
        int result = 0;
        int counter = 0;
        int indexDecimalPoint = n2.indexOf(decimalSymbol);
        if (indexDecimalPoint > 0) {
            result = 1;
            String nDecimalPart = n.substring(indexDecimalPoint);
            StringTokenizer token = new StringTokenizer(nDecimalPart, String.valueOf(decimalSymbol));
            while (token.hasMoreElements()) {
                counter++;
                String aux = token.nextToken();
                if (aux != null || !"".equals(aux)) {
                    nDecimalPart = aux;
                }
            }
            if (counter == 1 && nDecimalPart.length() <= maxFloatPart) {
                result = 1;
            } else {
                result = -1;
            }
        }
        return result;
    }

    /**
     * change the string that represents an decimal number into list decimalpart and intpart
     *
     * @param source        source string that contains the number
     * @param decimalSymbol decimal separator
     * @return an list first element is decimal part and second element is integer part
     */
    private static ArrayList getDecimalAndThousandthParts(String source, char decimalSymbol) {
        ArrayList result = new ArrayList(2);
        String decimalPart = "";
        String intPart = "";

        int decimalPoint = source.indexOf(decimalSymbol);
        if (decimalPoint > 0) {
            //separates the source String into decimal an integer part
            decimalPart = source.substring(decimalPoint + 1);
            intPart = source.substring(0, decimalPoint);
        } else {
            intPart = source; // all source string setting up with intPart
        }
        try {
            result.add(Integer.valueOf(decimalPart));
        } catch (NumberFormatException e) {
            result.add(null); // if the decimal number cannot is parsed
        }
        try {
            result.add(Integer.valueOf(intPart));
        } catch (NumberFormatException e) {
            result.add(null); //if the integer number cannot is parsed
        }
        return result;
    }

    /**
     * Setting up the values for the instance an numberformat
     *
     * @param locale       the locale
     * @param maxIntPart   this the max of digits on the integer part
     * @param maxFloatPart this the max of digits on the decimal part
     * @return numberformat
     */
    private static NumberFormat settingUpValuesOfFormat(Locale locale, int maxIntPart, int maxFloatPart) {
        NumberFormat numberFormat = NumberFormat.getInstance(locale);
        numberFormat.setMaximumFractionDigits(maxFloatPart);
        numberFormat.setMaximumIntegerDigits(maxIntPart);
        return numberFormat;
    }


    /**
     * Convert each element of the list to a only one string using the separator(if defined)
     *
     * @param stringlist the list of strings
     * @param separator  the separator to apply, it can be null,if it is not necessary.
     * @return the string formatted
     */
    public static String listAsString(List stringlist, String separator) {
        StringBuffer sb = new StringBuffer();
        for (Iterator iterator = stringlist.iterator(); iterator.hasNext(); ) {
            try {
                sb.append((String) iterator.next());
                if (separator != null && iterator.hasNext()) {
                    sb.append(separator);
                }
            } catch (ClassCastException e) {
                new RuntimeException("Only a list of strings can be sorted by this utility method", e);
            }
        }
        return new String(sb);
    }

    public static BigDecimal adjustDecimalNumber(BigDecimal decimalNumber) {
        return new BigDecimal(adjustDoubleNumber(decimalNumber.doubleValue(), Constants.MAX_INTEGER_PART, Constants.MAX_FLOAT_PART));

    }

    /**
     * Adjust the decimal number to format with maxIntPart and maxFloatpart
     *
     * @param decimalNumber This is the decimal number
     * @param maxIntPart    This is the max of digits on the integer part
     * @param maxFloatPart  This is the max of digits on the decimal part
     * @return numberFormat This is the decimal number adjusted with the maxIntPart and maxFloatPart parameters.
     */

    public static BigDecimal adjustDecimalNumber(BigDecimal decimalNumber, int maxIntPart, int maxFloatPart) {
        return new BigDecimal(adjustDoubleNumber(decimalNumber.doubleValue(), maxIntPart, maxFloatPart));

    }

    public static Double adjustDoubleNumber(Double doubleNumber, int maxIntPart, int maxFloatPart) {
        NumberFormat numberFormat = NumberFormat.getInstance(Locale.getDefault());
        numberFormat.setMaximumFractionDigits(maxFloatPart);
        numberFormat.setMaximumIntegerDigits(maxIntPart);
        numberFormat.setMinimumFractionDigits(maxFloatPart);
        try {
            doubleNumber = numberFormat.parse(numberFormat.format(doubleNumber)).doubleValue();
        } catch (ParseException e) {
            new RuntimeException("The adjustDecimalNumber method haved an exception", e);
        }
        return doubleNumber;
    }

    public static String afterFillingWithZeros(String number, Integer maxLength) {
        if (number != null && number.length() < maxLength) {
            String addZeros = "" + ((int) Math.pow(10, maxLength - number.length()));
            number = number + addZeros.substring(1);
        }
        return number;
    }

    public static String beforeFillingWithZeros(String number, Integer maxLength) {
        if (number != null && number.length() < maxLength) {
            String addZeros = "" + ((int) Math.pow(10, maxLength - number.length()));
            number = addZeros.substring(1) + number;
        }
        return number;
    }

    public static String removePoint(Object obj) {
        return ("" + obj).replaceAll("\\.", "");
    }

    /**
     * format number by pattern and locale
     *
     * @param number  number
     * @param pattern pattern
     * @param locale  locale
     * @return String
     */
    public static String formatNumber(Object number, String pattern, Locale locale) {
        if (number instanceof String) {
            return (String) number;
        }

        DecimalFormatSymbols decimalFormatSymbols = new DecimalFormatSymbols(locale);
        NumberFormat parser = new DecimalFormat(pattern, decimalFormatSymbols);
        return parser.format(number);
    }

    /**
     * get literal expression of decimal number
     *
     * @param decimalValue
     * @param locale
     * @return String
     */
    public static String getLiteralExpression(BigDecimal decimalValue, Locale locale) {
        String literalValue = "";

        if (decimalValue != null) {
            NumberToWord converterLiteral = new NumberToWord(locale);
            Double decimal = decimalValue.doubleValue();
            Double floatingValue = (decimal - decimal.intValue()) * 100;
            Double aDouble = Math.round(floatingValue * Math.pow(10, 1)) / Math.pow(10, 1);

            String decimalSection = String.valueOf(aDouble.intValue());
            if (aDouble.intValue() < 9) {
                decimalSection = "0" + decimalSection;
            }

            literalValue = converterLiteral.toWord(decimal.intValue()) + "  " + decimalSection + "/100 ";
            literalValue = literalValue.toUpperCase();
        }
        return literalValue;
    }

    /**
     * Convert to literal expression, add "UN" prefix to number between 1000 to 1999.99
     *
     * @param decimalValue value
     * @param locale       locale
     * @return String
     */
    public static String getSpecialLiteralExpression(BigDecimal decimalValue, Locale locale) {
        String literalValue = getLiteralExpression(decimalValue, locale);
        //add 'UN' prefix to values between 1000 and 1999.99
        if (decimalValue.doubleValue() >= 1000 && decimalValue.doubleValue() < 2000) {
            literalValue = MessageUtils.getMessage("Common.oneThousandPrefix") + " " + literalValue;
        }
        return literalValue.toUpperCase();
    }

    /**
     * get literal expression of int number
     *
     * @param intValue int
     * @param locale   locale
     * @return String
     */
    public static String getLiteralExpression(int intValue, Locale locale) {
        String literalValue = "";
        NumberToWord converterLiteral = new NumberToWord(locale);
        literalValue = converterLiteral.toWord(intValue);
        return literalValue;
    }

    public static String evaluateValue(String value) {
        return ValidatorUtil.isBlankOrNull(value) ? MessageUtils.getMessage("Common.withoutNumber") : value;
    }

    public static String evaluateNotNullValue(String value) {
        return ValidatorUtil.isBlankOrNull(value) ? "" : value;
    }

    public static String toTitle(Object obj1, Object obj2) {
        return MessageUtils.getMessage("Common.titleSeparator", obj1, obj2);
    }

    public static String toAcronym(Object obj1, Object obj2) {
        return MessageUtils.getMessage("Common.acronymSeparator", obj1, obj2);
    }

    public static String toCodeName(Object obj1, Object obj2) {
        return MessageUtils.getMessage("Common.codeNameSeparator", obj1, obj2);
    }

    public static String concat(Object... objects) {
        return concatBySeparator(" ", objects);
    }

    public static String concatBySeparator(String separator, Object... objects) {
        if (objects == null) {
            return "null";
        }
        if (separator == null) {
            separator = " ";
        }
        int iMax = objects.length - 1;
        if (iMax == -1) {
            return "";
        }
        StringBuilder b = new StringBuilder();
        for (int i = 0; ; i++) {
            if (objects[i] != null) {
                b.append(objects[i]);
            }
            if (i == iMax) {
                return b.toString().trim();
            }
            if (objects[i] != null) {
                b.append(separator);
            }
        }
    }

    public static <T extends BaseModel> String concatIdBySeparator(String separator, List<T> objects) {
        if (objects == null) {
            return "null";
        }
        if (separator == null) {
            separator = " ";
        }
        int iMax = objects.size() - 1;
        if (iMax == -1) {
            return "";
        }
        StringBuilder b = new StringBuilder();
        for (int i = 0; ; i++) {
            if (objects.get(i) != null) {
                b.append(objects.get(i).getId());
            }
            if (i == iMax) {
                return b.toString().trim();
            }
            if (objects.get(i) != null) {
                b.append(separator);
            }
        }
    }

    /**
     * Concat a List of Strings separated by a \n\n
     *
     * @param discountCommentCauseList a list of Strings
     * @return a String concatenated and separated by \n\n
     */
    public static String concatLineSeparated(List<String> discountCommentCauseList) {
        String result = "";
        for (String discountCommentCause : discountCommentCauseList) {
            result += discountCommentCause + "\n\n";
        }
        if (discountCommentCauseList.size() > 0) {
            result = result.substring(0, result.lastIndexOf("\n\n"));
        }
        return result;
    }

    /**
     * Concat a List of Strings separated by a separator
     *
     * @param objects a set of objects to concatenate
     * @return a String concatenated and separated by \n\n
     */
    public static String concatDashSeparated(Object... objects) {
        return concatBySeparator(DASH_SEPARATOR, objects);
    }

    /**
     * Concat a List of BaseModel id separated by a separator
     *
     * @param baseModelList a list of BaseModel objects
     * @return a String concatenated and separated by \n\n
     */
    public static <T extends BaseModel> String concatIdCommaSeparated(List<T> baseModelList) {

        return concatIdBySeparator(COMMA_SEPARATOR, baseModelList);
    }

    /**
     * Wraps a string by space
     *
     * @param string the element to wrap
     * @return a space wrapped string
     */
    public static String wrapBySpace(String string) {
        return SPACE_SEPARATOR.concat(string).concat(SPACE_SEPARATOR);
    }

    /**
     * Wraps a string by parentheses
     *
     * @param string the element to wrap
     * @return a parentheses wrapped string
     */
    public static String wrapByParentheses(String string) {
        StringBuilder builder = new StringBuilder(PARENTHESES);
        return builder.insert(1, string).toString();

    }

}

