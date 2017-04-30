package com.encens.khipus.exception.finances;

/**
 * @author
 * @version 2.20
 */

public class CurrencyDoNotMatchException extends Exception {
    public CurrencyDoNotMatchException() {
    }

    public CurrencyDoNotMatchException(String message) {
        super(message);
    }

    public CurrencyDoNotMatchException(Throwable cause) {
        super(cause);
    }
}