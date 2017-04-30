package com.encens.khipus.exception;

import javax.ejb.ApplicationException;

/**
 * Referential integrity entity exception.
 * This exception will be thrown when an entity which is referenced by
 * other one is trying to be deleted.
 *
 * @author
 * @version 1.0
 */
@ApplicationException(rollback = true)
public class ReferentialIntegrityException extends Exception {


    public ReferentialIntegrityException() {
    }

    public ReferentialIntegrityException(String message) {
        super(message);
    }

    public ReferentialIntegrityException(Throwable cause) {
        super(cause);
    }
}