package com.encens.khipus.model.warehouse;

/**
 * Tipo de Lugar de Despacho/Entrega del catalogo (inv_lugardespacho).
 *  - ORIGEN: lugar valido como origen del despacho.
 *  - DESTINO: lugar valido como destino del despacho.
 *  - AMBOS: lugar valido como origen y destino.
 */
public enum DispatchPlaceKind {
    ORIGEN("DispatchPlace.kind.ORIGEN"),
    DESTINO("DispatchPlace.kind.DESTINO"),
    AMBOS("DispatchPlace.kind.AMBOS");

    private String resourceKey;

    DispatchPlaceKind(String resourceKey) {
        this.resourceKey = resourceKey;
    }

    public String getResourceKey() {
        return resourceKey;
    }

    public void setResourceKey(String resourceKey) {
        this.resourceKey = resourceKey;
    }
}
