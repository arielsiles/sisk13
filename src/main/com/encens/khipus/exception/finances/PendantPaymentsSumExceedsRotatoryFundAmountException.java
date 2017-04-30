package com.encens.khipus.exception.finances;

/**
 * @author
 * @version 2.23
 */

public class PendantPaymentsSumExceedsRotatoryFundAmountException extends Exception {
    public PendantPaymentsSumExceedsRotatoryFundAmountException() {
    }

    public PendantPaymentsSumExceedsRotatoryFundAmountException(String message) {
        super(message);
    }

    public PendantPaymentsSumExceedsRotatoryFundAmountException(Throwable cause) {
        super(cause);
    }
}