package com.encens.khipus.exception.finances;

/**
 * @author
 * @version 2.18
 */

public class CannotChangeToOutdatedGeneratedPayrollTypeException extends Exception {
    public CannotChangeToOutdatedGeneratedPayrollTypeException() {
    }

    public CannotChangeToOutdatedGeneratedPayrollTypeException(String message) {
        super(message);
    }

    public CannotChangeToOutdatedGeneratedPayrollTypeException(Throwable cause) {
        super(cause);
    }
}