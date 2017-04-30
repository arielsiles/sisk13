package com.encens.khipus.exception.fixedassets;

/**
 * @author
 * @version 2.4.1
 */

public class NoChangeForTransferenceException extends Exception {
    public NoChangeForTransferenceException() {
    }

    public NoChangeForTransferenceException(String message) {
        super(message);
    }

    public NoChangeForTransferenceException(Throwable cause) {
        super(cause);
    }
}