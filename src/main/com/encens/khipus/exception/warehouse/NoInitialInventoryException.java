package com.encens.khipus.exception.warehouse;

import javax.ejb.ApplicationException;

/**
 * Lanzada cuando no existe ninguna fila en inv_inicio para el articulo
 * en el almacen objetivo. Sin punto de partida no se puede reconstruir
 * el costo promedio cronologicamente.
 */
@ApplicationException(rollback = true)
public class NoInitialInventoryException extends Exception {

    public NoInitialInventoryException(String productItemCode, String warehouseCode) {
        super("Articulo " + productItemCode + " en almacen " + warehouseCode +
                " no tiene inv_inicio en ninguna gestion. No es posible reconstruir.");
    }
}
