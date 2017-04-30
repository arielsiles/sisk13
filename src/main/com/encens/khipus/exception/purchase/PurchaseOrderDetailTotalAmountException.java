package com.encens.khipus.exception.purchase;

import java.math.BigDecimal;

/**
 * @author
 * @version 2.3
 */

public class PurchaseOrderDetailTotalAmountException extends Exception {
    private BigDecimal lowerLimit;

    public PurchaseOrderDetailTotalAmountException(BigDecimal lowerLimit) {
        this.lowerLimit = lowerLimit;
    }

    public PurchaseOrderDetailTotalAmountException(String message, BigDecimal lowerLimit) {
        super(message);
        this.lowerLimit = lowerLimit;
    }

    public PurchaseOrderDetailTotalAmountException(String message, Throwable cause, BigDecimal lowerLimit) {
        super(message, cause);
        this.lowerLimit = lowerLimit;
    }

    public PurchaseOrderDetailTotalAmountException(Throwable cause, BigDecimal lowerLimit) {
        super(cause);
        this.lowerLimit = lowerLimit;
    }

    public BigDecimal getLowerLimit() {
        return lowerLimit;
    }
}
