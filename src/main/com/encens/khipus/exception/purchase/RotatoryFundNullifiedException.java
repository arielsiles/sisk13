package com.encens.khipus.exception.purchase;

import javax.ejb.ApplicationException;

/**
 * RotatoryFundNullifiedException
 *
 * @author
 * @version 2.26
 */
@ApplicationException(rollback = true)
public class RotatoryFundNullifiedException extends Exception {

    public RotatoryFundNullifiedException() {
    }

    public RotatoryFundNullifiedException(String message) {
        super(message);
    }

    public RotatoryFundNullifiedException(String message, Throwable cause) {
        super(message, cause);
    }

    public RotatoryFundNullifiedException(Throwable cause) {
        super(cause);
    }
}
