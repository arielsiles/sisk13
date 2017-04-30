package com.encens.khipus.model.finances;

/**
 * Enum for FinancesCurrencyState
 *
 * @author
 * @version 2.3
 */
public enum FinancesCurrencyState {
    BLO("FinancesCurrencyState.state.blocked"),
    VIG("FinancesCurrencyState.state.valid");

    private String resourceKey;

    FinancesCurrencyState(String resourceKey) {
        this.resourceKey = resourceKey;
    }

    public String getResourceKey() {
        return resourceKey;
    }

    public void setResourceKey(String resourceKey) {
        this.resourceKey = resourceKey;
    }
}
