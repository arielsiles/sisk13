package com.encens.khipus.exception.warehouse;

/**
 * @author
 * @version 2.4.1
 */
public class SoldProductDeliveredException extends Exception {
    public SoldProductDeliveredException() {
    }

    public SoldProductDeliveredException(String message) {
        super(message);
    }

    public SoldProductDeliveredException(String message, Throwable cause) {
        super(message, cause);
    }

    public SoldProductDeliveredException(Throwable cause) {
        super(cause);
    }
}
