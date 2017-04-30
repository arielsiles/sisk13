package com.encens.khipus.exception.finances;

/**
 * @author
 * @version 2.14
 */

public class QuotaNotFoundException extends Exception {
    public QuotaNotFoundException() {
    }

    public QuotaNotFoundException(String message) {
        super(message);
    }

    public QuotaNotFoundException(Throwable cause) {
        super(cause);
    }
}