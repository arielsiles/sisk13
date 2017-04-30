package com.encens.khipus.exception.employees;

/**
 * DuplicatedScheduleEvaluationCycleException
 *
 * @author
 * @version 2.24
 */
public class DuplicatedScheduleEvaluationCycleException extends Exception {

    public DuplicatedScheduleEvaluationCycleException() {
    }

    public DuplicatedScheduleEvaluationCycleException(String message) {
        super(message);
    }

    public DuplicatedScheduleEvaluationCycleException(String message, Throwable cause) {
        super(message, cause);
    }

    public DuplicatedScheduleEvaluationCycleException(Throwable cause) {
        super(cause);
    }
}
