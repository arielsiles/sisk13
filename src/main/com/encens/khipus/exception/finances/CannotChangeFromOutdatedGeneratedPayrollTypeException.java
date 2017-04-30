package com.encens.khipus.exception.finances;

/**
 * @author
 * @version 2.18
 */

public class CannotChangeFromOutdatedGeneratedPayrollTypeException extends Exception {
    public CannotChangeFromOutdatedGeneratedPayrollTypeException() {
    }

    public CannotChangeFromOutdatedGeneratedPayrollTypeException(String message) {
        super(message);
    }

    public CannotChangeFromOutdatedGeneratedPayrollTypeException(Throwable cause) {
        super(cause);
    }
}