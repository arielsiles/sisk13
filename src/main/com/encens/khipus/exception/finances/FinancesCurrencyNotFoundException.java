package com.encens.khipus.exception.finances;

/**
 * @author
 * @version 2.3
 */

public class FinancesCurrencyNotFoundException extends Exception {
    public FinancesCurrencyNotFoundException() {
    }

    public FinancesCurrencyNotFoundException(String message) {
        super(message);
    }

    public FinancesCurrencyNotFoundException(Throwable cause) {
        super(cause);
    }
}