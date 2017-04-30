package com.encens.khipus.model.customers;

/**
 * Enumeration for the state of Bank
 *
 * @author:
 */

public enum AccountTransactionType {
    DEPOSIT("AccountTransactionType.deposit"),
    WITHDRAWAL("AccountTransactionType.withdrawal");

    private String resourceKey;

    AccountTransactionType(String resourceKey) {
        this.resourceKey = resourceKey;
    }

    public String getResourceKey() {
        return resourceKey;
    }

    public void setResourceKey(String resourceKey) {
        this.resourceKey = resourceKey;
    }
}
