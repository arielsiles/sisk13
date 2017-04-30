package com.encens.khipus.exception.warehouse;

import javax.ejb.ApplicationException;

/**
 * @author
 * @version 2.0
 */
@ApplicationException(rollback = true)
public class MonthProcessValidException extends Exception {
    public MonthProcessValidException() {
    }

    public MonthProcessValidException(String message) {
        super(message);
    }

    public MonthProcessValidException(String message, Throwable cause) {
        super(message, cause);
    }

    public MonthProcessValidException(Throwable cause) {
        super(cause);
    }
}
