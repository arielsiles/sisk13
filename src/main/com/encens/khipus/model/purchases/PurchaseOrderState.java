package com.encens.khipus.model.purchases;

import java.util.ArrayList;
import java.util.List;

/**
 * PurchaseOrderState, the values for this enumeration are
 * [PEN] pendiente, [APR] aprobado, [FIN] finalizado, LIQ] liquidado, [ANL] anulado
 *
 * @author
 * @version 2.0
 */
public enum PurchaseOrderState {
    PEN("PurchaseOrderState.PEN"),
    APR("PurchaseOrderState.APR"),
    FIN("PurchaseOrderState.FIN"),
    LIQ("PurchaseOrderState.LIQ"),
    ANL("PurchaseOrderState.ANL");

    private String resourceKey;

    PurchaseOrderState(String resourceKey) {
        this.resourceKey = resourceKey;
    }

    public String getResourceKey() {
        return resourceKey;
    }

    public void setResourceKey(String resourceKey) {
        this.resourceKey = resourceKey;
    }

    public static List<PurchaseOrderState> getInactiveStates() {
        List<PurchaseOrderState> inactiveStates = new ArrayList<PurchaseOrderState>();
        inactiveStates.add(PEN);
        inactiveStates.add(ANL);
        return inactiveStates;
    }

}
