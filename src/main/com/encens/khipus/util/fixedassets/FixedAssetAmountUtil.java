package com.encens.khipus.util.fixedassets;

import com.encens.khipus.util.BigDecimalUtil;

import java.math.BigDecimal;

/**
 * @author
 * @version 2.26
 */
public class FixedAssetAmountUtil {
    public static final FixedAssetAmountUtil i = new FixedAssetAmountUtil();

    private FixedAssetAmountUtil() {

    }

    public BigDecimal convertToExchangeCurrency(BigDecimal bsAmount, BigDecimal exchangeRate) {
        return BigDecimalUtil.divide(bsAmount, exchangeRate);
    }

    public BigDecimal convertToLocalCurrency(BigDecimal exchangeAmount, BigDecimal exchangeRate) {
        return BigDecimalUtil.multiply(exchangeAmount, exchangeRate);
    }
}
