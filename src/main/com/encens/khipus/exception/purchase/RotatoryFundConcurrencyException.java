package com.encens.khipus.exception.purchase;

import javax.ejb.ApplicationException;

/**
 * RotatoryFundConcurrencyException
 *
 * @author
 * @version 2.26
 */
@ApplicationException(rollback = true)
public class RotatoryFundConcurrencyException extends Exception {
    public RotatoryFundConcurrencyException() {
    }

    public RotatoryFundConcurrencyException(String message) {
        super(message);
    }

    public RotatoryFundConcurrencyException(String message, Throwable cause) {
        super(message, cause);
    }

    public RotatoryFundConcurrencyException(Throwable cause) {
        super(cause);
    }
}
