package com.encens.khipus.exception.finances;

import com.encens.khipus.model.finances.FinancesCurrencyType;

import java.math.BigDecimal;

/**
 * @author
 * @version 2.23
 */
public class RotatoryFundCollectionAmountException extends Exception {
    private BigDecimal limit;

    public RotatoryFundCollectionAmountException(BigDecimal limit) {
        this.limit = limit;
    }

    public RotatoryFundCollectionAmountException(String message, BigDecimal limit) {
        super(message);
        this.limit = limit;
    }

    public RotatoryFundCollectionAmountException(String message, Throwable cause, BigDecimal limit) {
        super(message, cause);
        this.limit = limit;
    }

    public RotatoryFundCollectionAmountException(Throwable cause, BigDecimal limit) {
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