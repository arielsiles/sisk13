package com.encens.khipus.model.finances;

/**
 * QuotaState, the values for this enumeration are
 * [PEN] pendiente, [PAG] pagado, [FIN] finalizado, [ANL] anulado
 *
 * @author
 * @version 2.14
 */
public enum QuotaState {
    PEN("QuotaState.PEN"),
    APR("QuotaState.APR"),
    PLI("QuotaState.PLI"),
    LIQ("QuotaState.LIQ"),
    ANL("QuotaState.ANL");

    private String resourceKey;

    QuotaState(String resourceKey) {
        this.resourceKey = resourceKey;
    }

    public String getResourceKey() {
        return resourceKey;
    }

    public void setResourceKey(String resourceKey) {
        this.resourceKey = resourceKey;
    }

}