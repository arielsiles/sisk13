package com.encens.khipus.exception.employees;

/**
 * @author
 * @version 3.4
 */
public class VacationOverlapException extends Exception {
    private long overlapItems;

    public VacationOverlapException(long overlapItems) {
        this.overlapItems = overlapItems;
    }

    public VacationOverlapException(String message, long overlapItems) {
        super(message);
        this.overlapItems = overlapItems;
    }

    public VacationOverlapException(String message, Throwable cause, long overlapItems) {
        super(message, cause);
        this.overlapItems = overlapItems;
    }

    public VacationOverlapException(Throwable cause, long overlapItems) {
        super(cause);
        this.overlapItems = overlapItems;
    }

    public long getOverlapItems() {
        return overlapItems;
    }
}
