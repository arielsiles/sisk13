package com.encens.khipus.exception.finances;

/**
 * @author
 * @version 2.17
 */

public class QuotaSumIsLessThanRotatoryFundAmountException extends Exception {
    public QuotaSumIsLessThanRotatoryFundAmountException() {
    }

    public QuotaSumIsLessThanRotatoryFundAmountException(String message) {
        super(message);
    }

    public QuotaSumIsLessThanRotatoryFundAmountException(Throwable cause) {
        super(cause);
    }
}