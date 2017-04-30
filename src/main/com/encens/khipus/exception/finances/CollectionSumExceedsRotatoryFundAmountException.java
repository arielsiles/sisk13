package com.encens.khipus.exception.finances;

/**
 * @author
 * @version 2.23
 */

public class CollectionSumExceedsRotatoryFundAmountException extends Exception {
    public CollectionSumExceedsRotatoryFundAmountException() {
    }

    public CollectionSumExceedsRotatoryFundAmountException(String message) {
        super(message);
    }

    public CollectionSumExceedsRotatoryFundAmountException(Throwable cause) {
        super(cause);
    }
}