package com.encens.khipus.exception.warehouse;

/**
 * @author
 * @version 2.3
 */
public class AdvancePaymentPendingException extends Exception {
    public AdvancePaymentPendingException() {
    }

    public AdvancePaymentPendingException(String message) {
        super(message);
    }

    public AdvancePaymentPendingException(String message, Throwable cause) {
        super(message, cause);
    }

    public AdvancePaymentPendingException(Throwable cause) {
        super(cause);
    }
}
