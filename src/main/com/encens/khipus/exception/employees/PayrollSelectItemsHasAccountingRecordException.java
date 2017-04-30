package com.encens.khipus.exception.employees;

/**
 * PayrollSelectItemsHasAccountingRecordException
 *
 * @author
 * @version 2.20
 */
public class PayrollSelectItemsHasAccountingRecordException extends Exception {
    public PayrollSelectItemsHasAccountingRecordException() {
    }

    public PayrollSelectItemsHasAccountingRecordException(String message) {
        super(message);
    }

    public PayrollSelectItemsHasAccountingRecordException(String message, Throwable cause) {
        super(message, cause);
    }

    public PayrollSelectItemsHasAccountingRecordException(Throwable cause) {
        super(cause);
    }
}