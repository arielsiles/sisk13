package com.encens.khipus.exception.finances;

/**
 * @author
 * @version 2.1.2
 */
public class FinancesUserException extends Exception {
    public FinancesUserException() {
    }

    public FinancesUserException(String message) {
        super(message);
    }

    public FinancesUserException(String message, Throwable cause) {
        super(message, cause);
    }

    public FinancesUserException(Throwable cause) {
        super(cause);
    }
}
