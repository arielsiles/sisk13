package com.encens.khipus.model.finances;

/**
 * RotatoryFundPaymentState, the values for this enumeration are
 * [PEN] pendant, [APR] approved, [ANL] annulled
 *
 * @author
 * @version 2.23
 */
public enum RotatoryFundPaymentState {
    PEN("RotatoryFundPaymentState.PEN"),
    APR("RotatoryFundPaymentState.APR"),
    ANL("RotatoryFundPaymentState.ANL");

    private String resourceKey;

    RotatoryFundPaymentState(String resourceKey) {
        this.resourceKey = resourceKey;
    }

    public String getResourceKey() {
        return resourceKey;
    }

    public void setResourceKey(String resourceKey) {
        this.resourceKey = resourceKey;
    }

}