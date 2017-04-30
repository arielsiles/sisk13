package com.encens.khipus.model.finances;

/**
 * @author
 * @version 2.0
 */
public enum FinancesModuleState {
    VIG("FinancesModule.state.valid"),
    BLO("FinancesModule.state.blocked");
    private String resourceKey;

    FinancesModuleState(String resourceKey) {
        this.resourceKey = resourceKey;
    }

    public String getResourceKey() {
        return resourceKey;
    }

    public void setResourceKey(String resourceKey) {
        this.resourceKey = resourceKey;
    }
}
