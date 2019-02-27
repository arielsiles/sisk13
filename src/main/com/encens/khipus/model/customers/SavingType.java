package com.encens.khipus.model.customers;

/**
 * Enumeration for the state of Bank
 *
 * @author:
 */

public enum SavingType {
    SOC("Account.savingType.savingsPartner"),
    CAJ("Account.savingType.savingsBank"),
    DPF("Account.savingType.fixedDeposit");

    private String resourceKey;

    SavingType(String resourceKey) {
        this.resourceKey = resourceKey;
    }

    public String getResourceKey() {
        return resourceKey;
    }

    public void setResourceKey(String resourceKey) {
        this.resourceKey = resourceKey;
    }
}
