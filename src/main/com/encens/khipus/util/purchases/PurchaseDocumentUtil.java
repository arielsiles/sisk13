package com.encens.khipus.util.purchases;

import com.encens.khipus.util.BigDecimalUtil;
import com.encens.khipus.util.Constants;

import java.math.BigDecimal;

/**
 * @author
 * @version 2.25
 */
public class PurchaseDocumentUtil {

    public static final PurchaseDocumentUtil i = new PurchaseDocumentUtil();

    private PurchaseDocumentUtil() {
    }

    public boolean isValidExempt(BigDecimal amount, BigDecimal exempt) {
        return !BigDecimalUtil.isNegative(exempt) && amount.compareTo(exempt) == 1;
    }

    public boolean isValidICE(BigDecimal amount, BigDecimal ice) {
        return !BigDecimalUtil.isNegative(ice) && amount.compareTo(ice) == 1;
    }

    public boolean canCalculateNETAmount(BigDecimal amount, BigDecimal exempt, BigDecimal ice) {
        return amount.compareTo(BigDecimalUtil.sum(exempt, ice)) == 1;
    }

    public BigDecimal calculateNETAmount(BigDecimal amount, BigDecimal exempt, BigDecimal ice) {
        BigDecimal partial = BigDecimalUtil.sum(exempt, ice);
        return BigDecimalUtil.subtract(amount, partial);
    }

    public BigDecimal calculateIVAAmount(BigDecimal totalNETAmount) {
        return BigDecimalUtil.multiply(totalNETAmount, Constants.VAT);
    }
}
