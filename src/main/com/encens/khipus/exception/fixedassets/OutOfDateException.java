package com.encens.khipus.exception.fixedassets;

/**
 * @author
 * @version 2.3
 */

public class OutOfDateException extends Exception {
    public OutOfDateException() {
    }

    public OutOfDateException(String message) {
        super(message);
    }

    public OutOfDateException(Throwable cause) {
        super(cause);
    }
}