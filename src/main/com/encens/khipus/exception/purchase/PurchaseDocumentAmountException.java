package com.encens.khipus.exception.purchase;

import java.math.BigDecimal;

/**
 * @author
 * @version 2.25
 */
public class PurchaseDocumentAmountException extends Exception {
    private BigDecimal limit;

    public PurchaseDocumentAmountException(BigDecimal limit) {
        super();
        this.limit = limit;
    }

    public BigDecimal getLimit() {
        return limit;
    }
}
