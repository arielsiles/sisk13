package com.encens.khipus.exception.warehouse;

import java.math.BigDecimal;

/**
 * @author
 * @version 2.4
 */
public class DiscountAmountException extends Exception {
    private BigDecimal limit;

    public DiscountAmountException(BigDecimal limit) {
        this.limit = limit;
    }

    public DiscountAmountException(String message, BigDecimal limit) {
        super(message);
        this.limit = limit;
    }

    public DiscountAmountException(String message, Throwable cause, BigDecimal limit) {
        super(message, cause);
        this.limit = limit;
    }

    public DiscountAmountException(Throwable cause, BigDecimal limit) {
        super(cause);
        this.limit = limit;
    }

    public BigDecimal getLimit() {
        return limit;
    }
}
