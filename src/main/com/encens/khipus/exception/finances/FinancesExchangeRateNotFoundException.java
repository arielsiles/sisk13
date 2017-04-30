package com.encens.khipus.exception.finances;

/**
 * @author
 * @version 2.3
 */

public class FinancesExchangeRateNotFoundException extends Exception {
    public FinancesExchangeRateNotFoundException() {
    }

    public FinancesExchangeRateNotFoundException(String message) {
        super(message);
    }

    public FinancesExchangeRateNotFoundException(Throwable cause) {
        super(cause);
    }
}