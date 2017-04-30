package com.encens.khipus.model.customers;

/**
 * @author
 * @version 2.0
 */
public enum CreditTransactionType {
    EGR("CreditTransaction.payoutCredit"),
    ING("CreditTransaction.income");

    private String resourceKey;

    CreditTransactionType(String resourceKey) {
        this.resourceKey = resourceKey;
    }

    public String getResourceKey() {
        return resourceKey;
    }

    public void setResourceKey(String resourceKey) {
        this.resourceKey = resourceKey;
    }
}
