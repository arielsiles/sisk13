package com.encens.khipus.exception.employees;

/**
 * @author
 * @version 3.4
 */
public class VacationRuleUndefinedYearException extends Exception {
    private int seniorityYear;

    public VacationRuleUndefinedYearException(int seniorityYear) {
        this.seniorityYear = seniorityYear;
    }

    public VacationRuleUndefinedYearException(String message, int seniorityYear) {
        super(message);
        this.seniorityYear = seniorityYear;
    }

    public VacationRuleUndefinedYearException(String message, Throwable cause, int seniorityYear) {
        super(message, cause);
        this.seniorityYear = seniorityYear;
    }

    public VacationRuleUndefinedYearException(Throwable cause, int seniorityYear) {
        super(cause);
        this.seniorityYear = seniorityYear;
    }

    public int getSeniorityYear() {
        return seniorityYear;
    }

    public void setSeniorityYear(int seniorityYear) {
        this.seniorityYear = seniorityYear;
    }
}