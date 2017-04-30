package com.encens.khipus.exception.finances;

/**
 * @author
 * @version 2.23
 */

public class PaymentSumExceedsRotatoryFundAmountException extends Exception {
    public PaymentSumExceedsRotatoryFundAmountException() {
    }

    public PaymentSumExceedsRotatoryFundAmountException(String message) {
        super(message);
    }

    public PaymentSumExceedsRotatoryFundAmountException(Throwable cause) {
        super(cause);
    }
}