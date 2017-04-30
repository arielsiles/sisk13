package com.encens.khipus.exception.fixedassets;

/**
 * this exception is thrown when the user try to close a month
 * for FixedAsset Module, but the system date is still in the same month
 * so there is not possible to close the month for FixedAsset Module
 *
 * @author
 * @version 2.4.3
 */

public class DateBeforeModuleMonthException extends Exception {
    public DateBeforeModuleMonthException() {
    }

    public DateBeforeModuleMonthException(String message) {
        super(message);
    }

    public DateBeforeModuleMonthException(Throwable cause) {
        super(cause);
    }
}