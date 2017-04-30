package com.encens.khipus.exception.finances;

/**
 * @author
 * @version 2.17
 */

public class QuotaSumExceedsRotatoryFundAmountException extends Exception {
    public QuotaSumExceedsRotatoryFundAmountException() {
    }

    public QuotaSumExceedsRotatoryFundAmountException(String message) {
        super(message);
    }

    public QuotaSumExceedsRotatoryFundAmountException(Throwable cause) {
        super(cause);
    }
}