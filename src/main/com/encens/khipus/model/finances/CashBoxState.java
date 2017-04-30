package com.encens.khipus.model.finances;

/**
 * Enumeration for the name of CashBoxState
 *
 * @author:
 */
public enum CashBoxState {
    OPEN("CashBoxState.open"),
    CLOSED("CashBoxState.closed");

    private String resourceKey;

    CashBoxState(String resourceKey) {
        this.resourceKey = resourceKey;
    }

    public String getResourceKey() {
        return resourceKey;
    }

    public void setResourceKey(String resourceKey) {
        this.resourceKey = resourceKey;
    }
}
