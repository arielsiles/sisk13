package com.encens.khipus.exception.employees;

/**
 * @author
 * @version 3.4
 */
public class VacationRuleIsActiveByDefaultException extends Exception {
    public VacationRuleIsActiveByDefaultException() {
    }

    public VacationRuleIsActiveByDefaultException(String message) {
        super(message);
    }

    public VacationRuleIsActiveByDefaultException(String message, Throwable cause) {
        super(message, cause);
    }

    public VacationRuleIsActiveByDefaultException(Throwable cause) {
        super(cause);
    }
}
