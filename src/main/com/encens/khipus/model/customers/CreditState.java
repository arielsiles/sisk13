package com.encens.khipus.model.customers;

/**
 * @author
 * @version 2.0
 */
public enum CreditState {
    VIG("Partner.state.valid"),
    VEN("Credit.state.expired"),
    EJE("Credit.state.execution");

    private String resourceKey;

    CreditState(String resourceKey) {
        this.resourceKey = resourceKey;
    }

    public String getResourceKey() {
        return resourceKey;
    }

    public void setResourceKey(String resourceKey) {
        this.resourceKey = resourceKey;
    }
}
