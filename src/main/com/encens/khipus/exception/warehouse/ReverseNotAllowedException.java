package com.encens.khipus.exception.warehouse;

/**
 * Thrown when a warehouse voucher or purchase order cannot be reversed/annulled
 * due to its state, type, origin or any business rule.
 *
 * @version 6.0.70
 */
public class ReverseNotAllowedException extends Exception {

    public ReverseNotAllowedException() {
    }

    public ReverseNotAllowedException(String message) {
        super(message);
    }

    public ReverseNotAllowedException(String message, Throwable cause) {
        super(message, cause);
    }

    public ReverseNotAllowedException(Throwable cause) {
        super(cause);
    }
}
