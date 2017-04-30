package com.encens.khipus.exception.finances;

/**
 * @author
 * @version 2.23
 */

public class RotatoryFundPaymentSumExceedsRotatoryFundAmountException extends Exception {
    public RotatoryFundPaymentSumExceedsRotatoryFundAmountException() {
    }

    public RotatoryFundPaymentSumExceedsRotatoryFundAmountException(String message) {
        super(message);
    }

    public RotatoryFundPaymentSumExceedsRotatoryFundAmountException(Throwable cause) {
        super(cause);
    }
}