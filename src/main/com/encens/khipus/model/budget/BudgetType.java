package com.encens.khipus.model.budget;

/**
 * BudgetType
 *
 * @author
 * @version 2.5
 */
public enum BudgetType {
    ENTRY("BudgetType.entry"),
    EXPENSE("BudgetType.expense");

    private String resourceKey;

    BudgetType(String resourceKey) {
        this.resourceKey = resourceKey;
    }

    public String getResourceKey() {
        return resourceKey;
    }

    public void setResourceKey(String resourceKey) {
        this.resourceKey = resourceKey;
    }
}
