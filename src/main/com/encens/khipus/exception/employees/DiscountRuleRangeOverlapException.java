package com.encens.khipus.exception.employees;

/**
 * @author
 * @version 3.4
 */
public class DiscountRuleRangeOverlapException extends Exception {
    private String overlapRuleName;

    public DiscountRuleRangeOverlapException(String overlapRuleName) {
        this.overlapRuleName = overlapRuleName;
    }

    public DiscountRuleRangeOverlapException(String message, String overlapRuleName) {
        super(message);
        this.overlapRuleName = overlapRuleName;
    }

    public DiscountRuleRangeOverlapException(String message, Throwable cause, String overlapRuleName) {
        super(message, cause);
        this.overlapRuleName = overlapRuleName;
    }

    public DiscountRuleRangeOverlapException(Throwable cause, String overlapRuleName) {
        super(cause);
        this.overlapRuleName = overlapRuleName;
    }

    public String getOverlapRuleName() {
        return overlapRuleName;
    }
}