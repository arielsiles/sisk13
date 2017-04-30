package com.encens.khipus.model.finances;

/**
 * VoucherState, the values for this enumeration are
 * [PEN] pendiente, [APR] aprobado,[ANL] anulado
 *
 * @author
 * @version 2.24
 */
public enum VoucherState {
    PEN("VoucherState.PEN"),
    APR("VoucherState.APR"),
    ANL("VoucherState.ANL");

    private String resourceKey;

    VoucherState(String resourceKey) {
        this.resourceKey = resourceKey;
    }

    public String getResourceKey() {
        return resourceKey;
    }

    public void setResourceKey(String resourceKey) {
        this.resourceKey = resourceKey;
    }

}