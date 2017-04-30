package com.encens.khipus.exception.employees;

/**
 * DuplicatedScheduleEvaluationNameException
 *
 * @author
 * @version 2.24
 */
public class DuplicatedScheduleEvaluationNameException extends Exception {
    public DuplicatedScheduleEvaluationNameException() {
    }

    public DuplicatedScheduleEvaluationNameException(String message) {
        super(message);
    }

    public DuplicatedScheduleEvaluationNameException(String message, Throwable cause) {
        super(message, cause);
    }

    public DuplicatedScheduleEvaluationNameException(Throwable cause) {
        super(cause);
    }
}
