package com.encens.khipus.model.customers;

/**
 * Enumeration for the state of Bank
 *
 * @author:
 */

public enum AccountState {
    ACTIVE("AccountState.active"),
    INACTIVE("AccountState.inactive");

    private String resourceKey;

    AccountState(String resourceKey) {
        this.resourceKey = resourceKey;
    }

    public String getResourceKey() {
        return resourceKey;
    }

    public void setResourceKey(String resourceKey) {
        this.resourceKey = resourceKey;
    }
}
