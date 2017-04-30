package com.encens.khipus.exception.finances;

/**
 * @author
 * @version 2.21
 */

public class SpendDistributionPercentageSumExceedsOneHundredException extends Exception {
    public SpendDistributionPercentageSumExceedsOneHundredException() {
    }

    public SpendDistributionPercentageSumExceedsOneHundredException(String message) {
        super(message);
    }

    public SpendDistributionPercentageSumExceedsOneHundredException(Throwable cause) {
        super(cause);
    }
}