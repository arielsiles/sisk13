package com.encens.khipus.exception.warehouse;

import com.encens.khipus.model.finances.FinancesCurrencyType;

import java.math.BigDecimal;

/**
 * @author
 * @version 2.3
 */
public class AdvancePaymentAmountException extends Exception {
    private BigDecimal limit;

    public AdvancePaymentAmountException(BigDecimal limit) {
        this.limit = limit;
    }

    public AdvancePaymentAmountException(String message, BigDecimal limit) {
        super(message);
        this.limit = limit;
    }

    public AdvancePaymentAmountException(String message, Throwable cause, BigDecimal limit) {
        super(message, cause);
        this.limit = limit;
    }

    public AdvancePaymentAmountException(Throwable cause, BigDecimal limit) {
        super(cause);
        this.limit = limit;
    }

    public BigDecimal getLimit() {
        return limit;
    }

    public String getDefaultCurrencySymbol() {
        return FinancesCurrencyType.P.getSymbolResourceKey();
    }
}
