package com.encens.khipus.model.finances;

/**
 * RotatoryFundCollectionState, the values for this enumeration are
 * [PEN] pendant, [APR] approved, [ANL] annulled
 *
 * @author
 * @version 2.23
 */
public enum RotatoryFundCollectionState {
    PEN("RotatoryFundCollectionState.PEN"),
    APR("RotatoryFundCollectionState.APR"),
    ANL("RotatoryFundCollectionState.ANL");

    private String resourceKey;

    RotatoryFundCollectionState(String resourceKey) {
        this.resourceKey = resourceKey;
    }

    public String getResourceKey() {
        return resourceKey;
    }

    public void setResourceKey(String resourceKey) {
        this.resourceKey = resourceKey;
    }

}