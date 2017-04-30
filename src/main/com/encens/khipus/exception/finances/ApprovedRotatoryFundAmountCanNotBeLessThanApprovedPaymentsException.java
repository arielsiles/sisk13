package com.encens.khipus.exception.finances;

/**
 * @author
 * @version 2.23
 */

public class ApprovedRotatoryFundAmountCanNotBeLessThanApprovedPaymentsException extends Exception {
    public ApprovedRotatoryFundAmountCanNotBeLessThanApprovedPaymentsException() {
    }

    public ApprovedRotatoryFundAmountCanNotBeLessThanApprovedPaymentsException(String message) {
        super(message);
    }

    public ApprovedRotatoryFundAmountCanNotBeLessThanApprovedPaymentsException(Throwable cause) {
        super(cause);
    }
}