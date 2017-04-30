package com.encens.khipus.model.budget;

/**
 * This class enclose the budget states
 *
 * @author
 * @version 2.0
 */
public enum BudgetState {
    ELABORATED(1, "BudgetState.elaborated"),
    CHECKED(2, "BudgetState.checked"),
    APPROVED(3, "BudgetState.approved"),
    BLOCKED(2, "BudgetState.blocked");

    private int code;
    private String resourceKey;

    BudgetState(int code, String resourceKey) {
        this.code = code;
        this.resourceKey = resourceKey;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getResourceKey() {
        return resourceKey;
    }

    public void setResourceKey(String resourceKey) {
        this.resourceKey = resourceKey;
    }
}
