package com.encens.khipus.model.sales;

/**
 * Estados de la Orden de Venta.
 * <p/>
 * BORRADOR (BOR): comercial edita libremente.
 * REVISION (REV): enviada; Gerencia revisa y agrega observaciones.
 * APROBADO (APR): aprobada por Gerencia (no editable).
 * ANULADO  (ANL): anulada (no se elimina).
 *
 * @author
 * @version 1.0
 */
public enum SalesOrderState {
    BOR("SalesOrderState.BOR"),
    REV("SalesOrderState.REV"),
    APR("SalesOrderState.APR"),
    ANL("SalesOrderState.ANL");

    private String resourceKey;

    SalesOrderState(String resourceKey) {
        this.resourceKey = resourceKey;
    }

    public String getResourceKey() {
        return resourceKey;
    }

    public void setResourceKey(String resourceKey) {
        this.resourceKey = resourceKey;
    }
}
