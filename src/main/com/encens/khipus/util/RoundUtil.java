package com.encens.khipus.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * This class implements some methods to round numbers to a specified number of decimal and with a
 * specified round mode. Here we use the setScale method of BigDecimal
 *
 * @author
 * @version 1.1.5
 */
public class RoundUtil {

    public static RoundUtil i = new RoundUtil();

    private RoundUtil() {
    }

    public enum RoundMode {//Round modes
        TO_UPPER,
        TO_LOWER,
        SYMMETRIC
    }

    /**
     * Round a Double
     *
     * @param value     The number to round
     * @param precision The number of decimals
     * @param roundMode The round mode (TO_UPPER, TO_LOWER and SYMMETRIC)
     * @return
     */
    public static Double getRoundValue(Double value, Integer precision, RoundMode roundMode) {
        BigDecimal number = new BigDecimal(value);
        Double res = null;

        if (roundMode.equals(RoundMode.TO_LOWER)) {
            res = number.setScale(precision, RoundingMode.DOWN).doubleValue();
        } else if (roundMode.equals(RoundMode.TO_UPPER)) {
            res = number.setScale(precision, RoundingMode.UP).doubleValue();
        } else if (roundMode.equals(RoundMode.SYMMETRIC)) {
            res = number.setScale(precision, RoundingMode.HALF_UP).doubleValue();
        }
        return (res);
    }

    /**
     * Round a Double
     *
     * @param value        The number to round
     * @param precision    The number of decimals
     * @param roundingMode The round mode used by BigDecimals
     * @return
     */
    public static Double getRoundValue(Double value, Integer precision, RoundingMode roundingMode) {
        BigDecimal number = new BigDecimal(value);
        Double res = number.setScale(precision, roundingMode).doubleValue();
        return (res);
    }

    /**
     * Round a Double
     *
     * @param currentDecimalNumber   The number to round
     * @param nearRoundDecimalNumber The decimal number more near to round
     * @param precision              The number of decimals
     * @return
     */
    public static Double getRoundValueToMoreNearDecimalNumber(Double currentDecimalNumber, Integer nearRoundDecimalNumber, Integer precision) {
        if (nearRoundDecimalNumber != null) {
            String startSing = currentDecimalNumber.toString().startsWith("-") ? "-" : "";
            Integer integerPart = currentDecimalNumber.intValue();
            Integer floatPart = new Integer(FormatUtils.afterFillingWithZeros(currentDecimalNumber.toString().substring(currentDecimalNumber.toString().indexOf(".") + 1), precision));
            if (floatPart % nearRoundDecimalNumber != 0) {
                Integer residue = floatPart % nearRoundDecimalNumber;
                floatPart -= residue;
                floatPart += residue <= nearRoundDecimalNumber ? nearRoundDecimalNumber : residue + (residue - nearRoundDecimalNumber);
            }

            if (floatPart.toString().length() > precision) {
                Integer integerPartRound = new Integer(floatPart.toString().substring(0, floatPart.toString().length() - precision));

                //if (IntegerPart % IntegerPartRound != 0)
                {
                    Integer residue = integerPart % integerPartRound;
                    integerPart -= residue;
                    integerPart += residue <= integerPartRound ? integerPartRound : residue + (residue - integerPartRound);
                }


                floatPart = new Integer(floatPart.toString().substring(floatPart.toString().length() - precision));
            } else if (nearRoundDecimalNumber.toString().length() > precision) {

                Integer nearRoundDecimalNumberTemp = new Integer(nearRoundDecimalNumber.toString().substring(0, nearRoundDecimalNumber.toString().length() - precision));

                if (integerPart % nearRoundDecimalNumberTemp != 0) {
                    Integer residue = integerPart % nearRoundDecimalNumberTemp;
                    integerPart -= residue;
                    integerPart += residue <= nearRoundDecimalNumberTemp ? nearRoundDecimalNumberTemp : residue + (residue - nearRoundDecimalNumberTemp);
                }
            }
            Double resultValue = new Double(startSing + integerPart + "." + FormatUtils.beforeFillingWithZeros("" + floatPart, precision));
//            resultValue = new BigDecimal(resultValue).setScale(precision, RoundingMode.HALF_UP).doubleValue();
//            log.debug();
            return resultValue.doubleValue();
        }

        return currentDecimalNumber;
    }
}


