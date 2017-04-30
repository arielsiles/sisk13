package com.encens.khipus.model.finances;

/**
 * FinanceMovementType
 *
 * @author
 * @version 2.5
 */
public enum FinanceMovementType {
    // Debit movements type
    D("FinanceMovementType.D"),
    // Credits movements type
    C("FinanceMovementType.C");

    private String resourceKey;

    FinanceMovementType(String resourceKey) {
        this.resourceKey = resourceKey;
    }

    public String getResourceKey() {
        return resourceKey;
    }

    public void setResourceKey(String resourceKey) {
        this.resourceKey = resourceKey;
    }
}
