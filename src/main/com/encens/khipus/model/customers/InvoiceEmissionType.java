package com.encens.khipus.model.customers;

/**
 * Enumeration
 *
 * @author:
 */

public enum InvoiceEmissionType {
    ONLINE("Invoice.emissionType.online"),
    OFFLINE("Invoice.emissionType.offline");

    private String resourceKey;

    InvoiceEmissionType(String resourceKey) {
        this.resourceKey = resourceKey;
    }

    public String getResourceKey() {
        return resourceKey;
    }

    public void setResourceKey(String resourceKey) {
        this.resourceKey = resourceKey;
    }
}
