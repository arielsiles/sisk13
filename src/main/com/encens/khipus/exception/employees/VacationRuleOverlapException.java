package com.encens.khipus.exception.employees;

/**
 * @author
 * @version 3.4
 */
public class VacationRuleOverlapException extends Exception {
    private String overlapRuleName;

    public VacationRuleOverlapException(String overlapRuleName) {
        this.overlapRuleName = overlapRuleName;
    }

    public VacationRuleOverlapException(String message, String overlapRuleName) {
        super(message);
        this.overlapRuleName = overlapRuleName;
    }

    public VacationRuleOverlapException(String message, Throwable cause, String overlapRuleName) {
        super(message, cause);
        this.overlapRuleName = overlapRuleName;
    }

    public VacationRuleOverlapException(Throwable cause, String overlapRuleName) {
        super(cause);
        this.overlapRuleName = overlapRuleName;
    }

    public String getOverlapRuleName() {
        return overlapRuleName;
    }
}