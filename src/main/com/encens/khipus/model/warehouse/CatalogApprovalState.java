package com.encens.khipus.model.warehouse;

/**
 * Ciclo de estados para catalogos donde las entradas se vuelven inmutables
 * una vez aprobadas (porque despachos historicos las referencian).
 *
 *   BORRADOR - Editable. NO aparece en selectores del despacho.
 *   APROBADO - Inmutable. Aparece en selectores del despacho.
 *   INACTIVO - Inmutable. NO aparece en selectores (terminal).
 *
 * Transiciones permitidas:
 *   BORRADOR -> APROBADO -> INACTIVO    (sin vuelta atras)
 *
 * Si se requiere modificar una entrada APROBADA, se INACTIVA y se crea
 * una nueva (politica unica, sin verificar si tiene referencias).
 *
 * Usado por:
 *   - {@link ProductDescription}  (catalogo de descripciones tecnicas por producto)
 *   - {@link DispatchRoute}       (catalogo de rutas de despacho)
 *   - {@link DispatchObservation} (catalogo de observaciones de despacho)
 */
public enum CatalogApprovalState {
    BORRADOR("CatalogApprovalState.BORRADOR"),
    APROBADO("CatalogApprovalState.APROBADO"),
    INACTIVO("CatalogApprovalState.INACTIVO");

    private String resourceKey;

    CatalogApprovalState(String resourceKey) {
        this.resourceKey = resourceKey;
    }

    public String getResourceKey() {
        return resourceKey;
    }

    public void setResourceKey(String resourceKey) {
        this.resourceKey = resourceKey;
    }
}
