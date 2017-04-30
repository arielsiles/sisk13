package com.encens.khipus.exception.warehouse;

/**
 * @author
 * @version 2.0
 */

public class MovementDetailNotFoundException extends Exception {
    public MovementDetailNotFoundException() {
    }

    public MovementDetailNotFoundException(String message) {
        super(message);
    }

    public MovementDetailNotFoundException(Throwable cause) {
        super(cause);
    }
}
