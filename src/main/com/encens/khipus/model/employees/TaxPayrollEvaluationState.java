package com.encens.khipus.model.employees;

/**
 * @author
 * @version 2.26
 */
public enum TaxPayrollEvaluationState {
    TEST("TaxPayrollEvaluationState.test"),
    OFFICIAL("TaxPayrollEvaluationState.official");

    private String resourceKey;

    TaxPayrollEvaluationState(String resourceKey) {
        this.resourceKey = resourceKey;
    }

    public String getResourceKey() {
        return resourceKey;
    }
}
