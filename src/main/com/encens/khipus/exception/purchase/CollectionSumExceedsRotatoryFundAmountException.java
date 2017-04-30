package com.encens.khipus.exception.purchase;

import javax.ejb.ApplicationException;

/**
 * CollectionSumExceedsRotatoryFundAmountException
 *
 * @author
 * @version 2.26
 */
@ApplicationException(rollback = true)
public class CollectionSumExceedsRotatoryFundAmountException extends Exception {
    public CollectionSumExceedsRotatoryFundAmountException() {
    }

    public CollectionSumExceedsRotatoryFundAmountException(String message) {
        super(message);
    }

    public CollectionSumExceedsRotatoryFundAmountException(String message, Throwable cause) {
        super(message, cause);
    }

    public CollectionSumExceedsRotatoryFundAmountException(Throwable cause) {
        super(cause);
    }
}
