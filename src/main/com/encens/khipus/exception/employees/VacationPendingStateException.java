package com.encens.khipus.exception.employees;

/**
 * @author
 * @version 3.4
 */
public class VacationPendingStateException extends Exception {
    public VacationPendingStateException() {
    }

    public VacationPendingStateException(String message) {
        super(message);
    }

    public VacationPendingStateException(String message, Throwable cause) {
        super(message, cause);
    }

    public VacationPendingStateException(Throwable cause) {
        super(cause);
    }
}
