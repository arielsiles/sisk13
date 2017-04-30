package com.encens.khipus.exception.finances;

/**
 * @author
 * @version 2.22
 */

public class ExemptCanNotBeGreaterThanAmountException extends Exception {
    public ExemptCanNotBeGreaterThanAmountException() {
    }

    public ExemptCanNotBeGreaterThanAmountException(String message) {
        super(message);
    }

    public ExemptCanNotBeGreaterThanAmountException(Throwable cause) {
        super(cause);
    }
}