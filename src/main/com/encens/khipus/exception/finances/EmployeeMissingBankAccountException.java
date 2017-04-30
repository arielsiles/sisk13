package com.encens.khipus.exception.finances;

/**
 * @author
 * @version 2.18
 */

public class EmployeeMissingBankAccountException extends Exception {
    public EmployeeMissingBankAccountException() {
    }

    public EmployeeMissingBankAccountException(String message) {
        super(message);
    }

    public EmployeeMissingBankAccountException(Throwable cause) {
        super(cause);
    }
}