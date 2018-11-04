package com.encens.khipus.model.customers;

/**
 * Enumeration for the state of Bank
 *
 * @author:
 */

public enum AccountTypeEnun {
    SAVINGS_BANK("Account.savingsBank"),
    CURRENT_ACCOUNT("Account.currentAccount");

    private String resourceKey;

    AccountTypeEnun(String resourceKey) {
        this.resourceKey = resourceKey;
    }

    public String getResourceKey() {
        return resourceKey;
    }

    public void setResourceKey(String resourceKey) {
        this.resourceKey = resourceKey;
    }
}
