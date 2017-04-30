package com.encens.khipus.model.contacts;

/**
 * Enumeration for the state of Bank
 *
 * @author:
 */

public enum BankState {
    ACTIVE("BankState.active"),
    INACTIVE("BankState.inactive");

    private String resourceKey;

    BankState(String resourceKey) {
        this.resourceKey = resourceKey;
    }

    public String getResourceKey() {
        return resourceKey;
    }

    public void setResourceKey(String resourceKey) {
        this.resourceKey = resourceKey;
    }
}
