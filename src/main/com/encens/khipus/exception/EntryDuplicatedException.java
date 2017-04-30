package com.encens.khipus.exception;

import javax.ejb.ApplicationException;

/**
 * Duplicate entity exception
 *
 * @author
 * @version 1.0
 */
@ApplicationException(rollback = true)
public class EntryDuplicatedException extends Exception {


    public EntryDuplicatedException() {
    }

    public EntryDuplicatedException(String message) {
        super(message);
    }

    public EntryDuplicatedException(Throwable cause) {
        super(cause);
    }
}
