package com.encens.khipus.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Encens S.R.L.
 *
 * @author
 * @version $Id: BigDecimalUtil.java  14-dic-2009 11:26:59$
 */
public final class BigDecimalUtil {
    private BigDecimalUtil() {
    }

    public static final BigDecimal ONE_NEGATIVE = new BigDecimal(-1);
    public static final BigDecimal ONE_HUNDRED = new BigDecimal(100);
    public static final BigDecimal ONE_THOUSAND = new BigDecimal(1000);
    public static final BigDecimal FOUR = new BigDecimal(4);

    public static BigDecimal sum(BigDecimal num1, BigDecimal num2, int scale) {
        return roundBigDecimal(num1.add(num2), scale);
    }

    public static BigDecimal sum(BigDecimal... amounts) {
        return sum(2, amounts);
    }

    public static BigDecimal sum(int scale, BigDecimal... amounts) {
        BigDecimal result = null;
        for (BigDecimal bigDecimal : amounts) {
            if (bigDecimal != null) {
                result = result == null ? bigDecimal : result.add(bigDecimal);
            }
        }
        return roundBigDecimal(result, scale);
    }

    public static BigDecimal subtract(BigDecimal amount, BigDecimal subtrahend, int scale) {
        return roundBigDecimal(amount.subtract(subtrahend), scale);
    }

    public static BigDecimal subtract(BigDecimal... amounts) {
        return subtract(2, amounts);
    }

    public static BigDecimal subtract(int scale, BigDecimal... amounts) {
        BigDecimal result = null;
        for (BigDecimal bigDecimal : amounts) {
            if (bigDecimal != null) {
                result = result == null ? bigDecimal : result.subtract(bigDecimal);
            }
        }
        return roundBigDecimal(result);
    }

    public static BigDecimal multiply(BigDecimal amount, BigDecimal multiplicand, int scale) {
        return roundBigDecimal(amount.multiply(multiplicand), scale);
    }

    public static BigDecimal multiply(BigDecimal... amounts) {
        return multiply(2, amounts);
    }

    public static BigDecimal multiply(int scale, BigDecimal... amounts) {
        BigDecimal result = null;
        for (BigDecimal bigDecimal : amounts) {
            if (bigDecimal != null) {
                result = result == null ? bigDecimal : result.multiply(bigDecimal);
            }
        }
        return roundBigDecimal(result, scale);
    }

    public static BigDecimal divide(BigDecimal amount, BigDecimal divisor) {
        return roundBigDecimal(amount.divide(divisor, 2, RoundingMode.HALF_UP));
    }

    public static BigDecimal divide(BigDecimal amount, BigDecimal divisor, int scale) {
        return amount.divide(divisor, scale, RoundingMode.HALF_UP);
    }

    public static BigDecimal avg(BigDecimal... amounts) {
        int notNullValues = 0;
        BigDecimal sumResult = null;
        for (BigDecimal bigDecimal : amounts) {
            if (bigDecimal != null) {
                notNullValues++;
                sumResult = sumResult == null ? bigDecimal : sumResult.add(bigDecimal);
            }
        }
        if (sumResult != null) {
            return divide(sumResult, new BigDecimal(notNullValues));
        }
        return null;
    }

    public static BigDecimal max(BigDecimal... amounts) {
        BigDecimal result = null;
        for (BigDecimal bigDecimal : amounts) {
            if (bigDecimal != null) {
                result = result == null ? bigDecimal : result.max(bigDecimal);
            }
        }
        return result;
    }

    public static BigDecimal min(BigDecimal... amounts) {
        BigDecimal result = null;
        for (BigDecimal bigDecimal : amounts) {
            if (bigDecimal != null) {
                result = result == null ? bigDecimal : result.min(bigDecimal);
            }
        }
        return result;
    }

    public static BigDecimal roundBigDecimal(BigDecimal num) {
        return roundBigDecimal(num, 2);
    }

    public static BigDecimal roundBigDecimal(BigDecimal num, int scale) {
        if (num != null) {
            return num.setScale(scale, RoundingMode.HALF_UP);
        }
        return num;
    }

    public static BigDecimal toBigDecimal(Integer num) {
        try {
            return roundBigDecimal(new BigDecimal(num));
        } catch (Exception ignored) {
        }
        return null;
    }

    public static BigDecimal toBigDecimal(Double num) {
        try {
            return roundBigDecimal(new BigDecimal(num));
        } catch (Exception ignored) {
        }
        return null;
    }

    public static BigDecimal toBigDecimal(Float num) {
        try {
            return roundBigDecimal(new BigDecimal(num));
        } catch (Exception ignored) {
        }
        return null;
    }

    public static BigDecimal toBigDecimal(String num) {
        try {
            return roundBigDecimal(new BigDecimal(num));
        } catch (Exception ignored) {
        }
        return null;
    }

    public static BigDecimal toBigDecimal(String num, Integer scale) {
        try {
            return roundBigDecimal(new BigDecimal(num), scale);
        } catch (Exception ignored) {
        }
        return null;
    }

    public static BigDecimal toBigDecimal(Object num) {
        try {
            return roundBigDecimal(num != null ? new BigDecimal(num.toString()) : null);
        } catch (Exception ignored) {
        }
        return null;
    }

    public static BigDecimal toSimpleBigDecimal(Object num) {
        try {
            return num != null ? new BigDecimal(num.toString()) : null;
        } catch (Exception ignored) {
        }
        return null;
    }

    public static Boolean isZeroOrNull(BigDecimal amount) {
        return amount == null || BigDecimal.ZERO.compareTo(amount) == 0;
    }

    public static Boolean isNegative(BigDecimal amount) {
        return null != amount && BigDecimal.ZERO.compareTo(amount) == 1;
    }

    public static Boolean isPositive(BigDecimal amount) {
        return !isZeroOrNull(amount) && amount.compareTo(BigDecimal.ZERO) == 1;
    }

    public static Boolean isZeroOrPositive(BigDecimal amount) {
        return amount != null && amount.compareTo(BigDecimal.ZERO) >= 0;
    }

    public static BigDecimal negate(BigDecimal amount) {
        return !isZeroOrNull(amount) ? amount.negate() : amount;
    }

    public static BigDecimal abs(BigDecimal amount) {
        return !isZeroOrNull(amount) ? amount.abs() : amount;
    }

    public static BigDecimal getPercentage(BigDecimal amount, BigDecimal percent) {
        return divide(amount.multiply(percent), ONE_HUNDRED);
    }

    public static BigDecimal getPercentage(BigDecimal amount, BigDecimal percent, Integer scale) {
        return divide(amount.multiply(percent), ONE_HUNDRED, scale);
    }

    public static int compareTo(Object value1, Object value2) {
        return new BigDecimal(String.valueOf(value1)).compareTo(new BigDecimal(String.valueOf(value2)));
    }

    public static BigDecimal changeSign(BigDecimal value) {
        return multiply(value.scale(), value, ONE_NEGATIVE);
    }
}
