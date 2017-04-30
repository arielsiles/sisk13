package com.encens.khipus.exception.purchase;

import javax.ejb.ApplicationException;

/**
 * RotatoryFundLiquidatedException
 *
 * @author
 * @version 2.26
 */
@ApplicationException(rollback = true)
public class RotatoryFundLiquidatedException extends Exception {
    public RotatoryFundLiquidatedException() {
    }

    public RotatoryFundLiquidatedException(String message) {
        super(message);
    }

    public RotatoryFundLiquidatedException(String message, Throwable cause) {
        super(message, cause);
    }

    public RotatoryFundLiquidatedException(Throwable cause) {
        super(cause);
    }
}
