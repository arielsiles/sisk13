package com.encens.khipus.exception.finances;

/**
 * @author
 * @version 2.23
 */

public class RotatoryFundPaymentSumIsLessThanRotatoryFundAmountException extends Exception {
    public RotatoryFundPaymentSumIsLessThanRotatoryFundAmountException() {
    }

    public RotatoryFundPaymentSumIsLessThanRotatoryFundAmountException(String message) {
        super(message);
    }

    public RotatoryFundPaymentSumIsLessThanRotatoryFundAmountException(Throwable cause) {
        super(cause);
    }
}