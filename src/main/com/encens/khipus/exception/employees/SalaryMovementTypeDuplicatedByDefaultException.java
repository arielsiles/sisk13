package com.encens.khipus.exception.employees;

/**
 * @author
 * @version 3.4
 */
public class SalaryMovementTypeDuplicatedByDefaultException extends Exception {
    public SalaryMovementTypeDuplicatedByDefaultException() {
    }

    public SalaryMovementTypeDuplicatedByDefaultException(String message) {
        super(message);
    }

    public SalaryMovementTypeDuplicatedByDefaultException(String message, Throwable cause) {
        super(message, cause);
    }

    public SalaryMovementTypeDuplicatedByDefaultException(Throwable cause) {
        super(cause);
    }
}
