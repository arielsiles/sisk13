package com.encens.khipus.model.finances;

/**
 * @author
 * @version 3.5
 */
public enum RotatoryFundMovementState {
    PEN("RotatoryFundMovementState.PEN"),
    APR("RotatoryFundMovementState.APR"),
    ANL("RotatoryFundMovementState.ANL");

    private String resourceKey;

    RotatoryFundMovementState(String resourceKey) {
        this.resourceKey = resourceKey;
    }

    public String getResourceKey() {
        return resourceKey;
    }

    public void setResourceKey(String resourceKey) {
        this.resourceKey = resourceKey;
    }

}
