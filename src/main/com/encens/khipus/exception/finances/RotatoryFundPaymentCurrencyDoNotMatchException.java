package com.encens.khipus.exception.finances;

/**
 * @author
 * @version 2.23
 */

public class RotatoryFundPaymentCurrencyDoNotMatchException extends Exception {
    public RotatoryFundPaymentCurrencyDoNotMatchException() {
    }

    public RotatoryFundPaymentCurrencyDoNotMatchException(String message) {
        super(message);
    }

    public RotatoryFundPaymentCurrencyDoNotMatchException(Throwable cause) {
        super(cause);
    }
}