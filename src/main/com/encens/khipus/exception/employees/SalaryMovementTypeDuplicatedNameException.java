package com.encens.khipus.exception.employees;

/**
 * @author
 * @version 3.4
 */
public class SalaryMovementTypeDuplicatedNameException extends Exception {
    private String name;

    public SalaryMovementTypeDuplicatedNameException(String name) {
        this.name = name;
    }

    public SalaryMovementTypeDuplicatedNameException(String message, String name) {
        super(message);
        this.name = name;
    }

    public SalaryMovementTypeDuplicatedNameException(String message, Throwable cause, String name) {
        super(message, cause);
        this.name = name;
    }

    public SalaryMovementTypeDuplicatedNameException(Throwable cause, String name) {
        super(cause);
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
