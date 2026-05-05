package com.encens.khipus.exception.warehouse;

import javax.ejb.ApplicationException;
import java.util.List;

/**
 * Lanzada cuando un articulo aparece registrado en mas de un almacen
 * dentro de inv_inventario. Bloquea la reconciliacion porque la
 * herramienta asume 1 articulo = 1 almacen.
 */
@ApplicationException(rollback = true)
public class MultipleWarehouseException extends Exception {

    private final List<String> warehouseCodes;

    public MultipleWarehouseException(String productItemCode, List<String> warehouseCodes) {
        super("Articulo " + productItemCode + " registrado en almacenes: " + warehouseCodes);
        this.warehouseCodes = warehouseCodes;
    }

    public List<String> getWarehouseCodes() {
        return warehouseCodes;
    }
}
