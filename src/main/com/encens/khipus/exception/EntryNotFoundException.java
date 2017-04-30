package com.encens.khipus.exception;

import javax.ejb.ApplicationException;

/**
 * Entity not found  exception
 *
 * @author
 * @version 1.0
 */
@ApplicationException(rollback = true)
public class EntryNotFoundException extends Exception {


    public EntryNotFoundException() {
    }

    public EntryNotFoundException(String message) {
        super(message);
    }

    public EntryNotFoundException(Throwable cause) {
        super(cause);
    }
}
