package com.encens.khipus.exception.finances;

/**
 * @author
 * @version 2.14
 */

public class QuotaEmptyException extends Exception {
    public QuotaEmptyException() {
    }

    public QuotaEmptyException(String message) {
        super(message);
    }

    public QuotaEmptyException(Throwable cause) {
        super(cause);
    }
}