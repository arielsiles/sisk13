package com.encens.khipus.service.warehouse;

import com.encens.khipus.exception.warehouse.InventoryProductItemNotFoundException;
import com.encens.khipus.exception.warehouse.InventoryUnitaryBalanceException;
import com.encens.khipus.framework.service.GenericService;
import com.encens.khipus.model.warehouse.MovementDetail;
import com.encens.khipus.model.warehouse.Warehouse;
import com.encens.khipus.model.warehouse.WarehouseVoucher;

import javax.ejb.Local;

/**
 * Servicio para revertir los efectos de inventario y costo promedio que se
 * generaron al aprobar un WarehouseVoucher. Reverso simetrico: revierte
 * cantidades y montos con los mismos valores del movimiento original.
 *
 * @version 6.0.70
 */
@Local
public interface ReverseInventoryService extends GenericService {

    /**
     * Revierte la cantidad fisica afectada por un MovementDetail sobre
     * inv_inventario e inv_inventario_detalle. Segun el tipo de movimiento
     * (E=entrada, S=salida), suma o resta para dejar el inventario como
     * estaba antes del movimiento.
     *
     * @param warehouseVoucher vale original (para resolver U.Ejec./C.Costo)
     * @param warehouse almacen sobre el que se revierte
     * @param movementDetail detalle a revertir
     * @throws InventoryUnitaryBalanceException si al revertir una entrada el
     *         stock quedaria negativo (ej. se consumio lo ingresado y ya no
     *         hay como devolverlo)
     */
    void reverseInventory(WarehouseVoucher warehouseVoucher,
                          Warehouse warehouse,
                          MovementDetail movementDetail)
            throws InventoryUnitaryBalanceException,
                   InventoryProductItemNotFoundException;

    /**
     * Revierte el impacto del movimiento sobre ProductItem (unitCost, cu,
     * investmentAmount, ct). Reverso simetrico con los valores amount y
     * purchasePrice grabados en el MovementDetail original.
     * Solo opera si el ProductItem tiene controlValued=true.
     *
     * @param warehouseVoucher vale original
     * @param movementDetail detalle a revertir
     */
    void reverseProductItemCost(WarehouseVoucher warehouseVoucher,
                                MovementDetail movementDetail);

    /**
     * Revierte los acumulados mensuales de inv_invmes: si el movimiento
     * original fue entrada, descuenta de incomingQuantity/incomingAmount;
     * si fue salida, descuenta de outgoingQuantity/outgoingAmount.
     *
     * @param movementDetail detalle a revertir
     */
    void reverseInventoryHistory(MovementDetail movementDetail);
}
