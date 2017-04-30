package com.encens.khipus.exception.warehouse;

/**
 * @author
 * @version 2.4.1
 */
public class SoldProductNotFoundException extends Exception {
    public SoldProductNotFoundException() {
    }

    public SoldProductNotFoundException(String message) {
        super(message);
    }

    public SoldProductNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public SoldProductNotFoundException(Throwable cause) {
        super(cause);
    }
}
