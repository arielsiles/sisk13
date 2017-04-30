package com.encens.khipus.exception.finances;

/**
 * @author
 * @version 2.4.2
 */
public class AdjustmentForInflationAccountNullException extends Exception {
    public AdjustmentForInflationAccountNullException() {
    }

    public AdjustmentForInflationAccountNullException(String message) {
        super(message);
    }

    public AdjustmentForInflationAccountNullException(String message, Throwable cause) {
        super(message, cause);
    }

    public AdjustmentForInflationAccountNullException(Throwable cause) {
        super(cause);
    }
}