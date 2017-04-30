package com.encens.khipus.exception.employees;

/**
 * PayrollSelectItemsEmptyException
 *
 * @author
 * @version 2.20
 */
public class PayrollSelectItemsEmptyException extends Exception {
    public PayrollSelectItemsEmptyException() {
        super();
    }

    public PayrollSelectItemsEmptyException(String message) {
        super(message);
    }

    public PayrollSelectItemsEmptyException(String message, Throwable cause) {
        super(message, cause);
    }

    public PayrollSelectItemsEmptyException(Throwable cause) {
        super(cause);
    }
}
