package com.encens.khipus.exception.finances;

/**
 * @author
 * @version 2.21
 */

public class SpendDistributionEmptyException extends Exception {
    public SpendDistributionEmptyException() {
    }

    public SpendDistributionEmptyException(String message) {
        super(message);
    }

    public SpendDistributionEmptyException(Throwable cause) {
        super(cause);
    }
}