package com.encens.khipus.model.budget;

/**
 * Enum for BudgetDistributionType
 *
 * @author
 * @version 3.2
 */
public enum BudgetDistributionType {
    GLOBAL("BudgetDistributionType.GLOBAL"),
    BUDGET("BudgetDistributionType.BUDGET");

    private String resourceKey;

    BudgetDistributionType(String resourceKey) {
        this.resourceKey = resourceKey;
    }

    public String getResourceKey() {
        return resourceKey;
    }

    public void setResourceKey(String resourceKey) {
        this.resourceKey = resourceKey;
    }
}
