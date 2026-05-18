package com.encens.khipus.model.warehouse;

/**
 * Estados del Vale de Despacho de Productos Terminados.
 */
public enum DispatchState {
    BORRADOR("WarehouseDispatch.state.BORRADOR"),
    APROBADO("WarehouseDispatch.state.APROBADO"),
    ANULADO("WarehouseDispatch.state.ANULADO");

    private String resourceKey;

    DispatchState(String resourceKey) {
        this.resourceKey = resourceKey;
    }

    public String getResourceKey() {
        return resourceKey;
    }

    public void setResourceKey(String resourceKey) {
        this.resourceKey = resourceKey;
    }
}
