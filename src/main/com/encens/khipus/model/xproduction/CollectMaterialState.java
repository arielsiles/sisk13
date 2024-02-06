package com.encens.khipus.model.xproduction;

/**
 * @author
 * @version 2.0
 */
public enum CollectMaterialState {
    APR("CollectMaterial.state.approved"),
    PEN("CollectMaterial.state.pending"),
    ANL("CollectMaterial.state.nullfied"),
    PAY("CollectMaterial.state.toPay"),
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
