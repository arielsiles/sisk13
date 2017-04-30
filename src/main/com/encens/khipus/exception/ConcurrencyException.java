package com.encens.khipus.exception;

import javax.ejb.ApplicationException;

/**
 * Concurrency entity exception
 *
 * @author
 * @version 1.0
 */
@ApplicationException(rollback = true)
public class ConcurrencyException extends Exception {


    public ConcurrencyException() {
    }

    public ConcurrencyException(String message) {
        super(message);
    }

    public ConcurrencyException(Throwable cause) {
        super(cause);
    }
}
