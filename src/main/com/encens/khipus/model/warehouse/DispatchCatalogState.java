package com.encens.khipus.model.warehouse;

/**
 * Estado de los catalogos del modulo de Despacho (Driver, Vehicle).
 *
 *   VIG - Vigente (aparece en selectores)
 *   ANL - Anulado (borrado logico, ya no aparece en selectores
 *         pero queda en BD para no romper historial referenciado).
 */
public enum DispatchCatalogState {
    VIG("DispatchCatalogState.VIG"),
    ANL("DispatchCatalogState.ANL");

    private String resourceKey;

    DispatchCatalogState(String resourceKey) {
        this.resourceKey = resourceKey;
    }

    public String getResourceKey() {
        return resourceKey;
    }

    public void setResourceKey(String resourceKey) {
        this.resourceKey = resourceKey;
    }
}
