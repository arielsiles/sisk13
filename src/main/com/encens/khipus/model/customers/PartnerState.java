package com.encens.khipus.model.customers;

/**
 * @author
 * @version 2.0
 */
public enum PartnerState {
    VIG("Partner.state.valid"),
    BLO("Partner.state.blocked");

    private String resourceKey;

    PartnerState(String resourceKey) {
        this.resourceKey = resourceKey;
    }

    public String getResourceKey() {
        return resourceKey;
    }

    public void setResourceKey(String resourceKey) {
        this.resourceKey = resourceKey;
    }
}
