package com.encens.khipus.model.production;

/**
 * @author
 * @version 2.0
 */
public enum CollectMaterialState {
    APR("CollectMaterial.state.approved"),
    PEN("CollectMaterial.state.pending"),
    ANL("CollectMaterial.state.nullfied"),
    PAY("CollectMaterial.state.toPay"),
    CONTA("CollectMaterial.state.accountig"),
    LIQ("CollectMaterial.state.liquidated");

    private String resourceKey;

    CollectMaterialState(String resourceKey) {
        this.resourceKey = resourceKey;
    }

    public String getResourceKey() {
        return resourceKey;
    }

    public void setResourceKey(String resourceKey) {
        this.resourceKey = resourceKey;
    }

}
