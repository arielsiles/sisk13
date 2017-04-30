package com.encens.khipus.model.finances;

/**
 * @author
 * @version 3.2
 */
public enum FinancesEntityState {
    VIG("FinancesEntityState.VIG"),
    BLO("FinancesEntityState.BLO");

    private String resourceKey;

    FinancesEntityState(String resourceKey) {
        this.resourceKey = resourceKey;
    }

    public String getResourceKey() {
        return resourceKey;
    }

    public void setResourceKey(String resourceKey) {
        this.resourceKey = resourceKey;
    }
}
