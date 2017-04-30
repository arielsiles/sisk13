package com.encens.khipus.exception.finances;

/**
 * @author
 * @version 2.17
 */

public class ExpirationDateBeforeStartDateException extends Exception {
    public ExpirationDateBeforeStartDateException() {
    }

    public ExpirationDateBeforeStartDateException(String message) {
        super(message);
    }

    public ExpirationDateBeforeStartDateException(Throwable cause) {
        super(cause);
    }
}