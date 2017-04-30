package com.encens.khipus.exception.employees;

/**
 * @author
 * @version 3.4
 */
public class VacationAnnulledStateException extends Exception {
    public VacationAnnulledStateException() {
    }

    public VacationAnnulledStateException(String message) {
        super(message);
    }

    public VacationAnnulledStateException(String message, Throwable cause) {
        super(message, cause);
    }

    public VacationAnnulledStateException(Throwable cause) {
        super(cause);
    }
}
