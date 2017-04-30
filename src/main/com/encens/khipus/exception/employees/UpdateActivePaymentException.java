package com.encens.khipus.exception.employees;

/**
 * UpdateActivePaymentException
 *
 * @author
 * @version 2.20
 */
public class UpdateActivePaymentException extends Exception {
    public UpdateActivePaymentException() {
        super();
    }

    public UpdateActivePaymentException(String message) {
        super(message);
    }

    public UpdateActivePaymentException(String message, Throwable cause) {
        super(message, cause);
    }

    public UpdateActivePaymentException(Throwable cause) {
        super(cause);
    }
}
