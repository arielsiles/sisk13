package com.encens.khipus.service.warehouse;

import com.encens.khipus.exception.warehouse.InventoryProductItemNotFoundException;
import com.encens.khipus.exception.warehouse.InventoryUnitaryBalanceException;
import com.encens.khipus.exception.warehouse.ReverseNotAllowedException;
import com.encens.khipus.framework.service.GenericService;
import com.encens.khipus.model.purchases.PurchaseOrder;

import javax.ejb.Local;

/**
 * Orquesta la anulacion de una Orden de Compra y de todas sus entidades
 * asociadas segun el estado en el que se encuentre:
 *
 *   - PEN / APR: solo cambia estado a ANL + auditoria (no hay efectos fisicos
 *     ni contables que revertir).
 *   - FIN: revierte el vale de RECEPCION auto-generado (via
 *     ReverseWarehouseVoucherService con skipValidation=true) y genera el
 *     contra-asiento IA simetrico.
 *   - LIQ: lo de FIN + anula los asientos CP de liquidacion (sin
 *     contra-asiento, solo state=ANL y motivo en glosa) y marca todos los
 *     PurchaseOrderPayment asociados (incluidos anticipos y pagos con cheque)
 *     y PurchaseDocument como NULLIFIED.
 *
 * @version 6.0.70
 */
@Local
public interface ReversePurchaseOrderService extends GenericService {

    /**
     * Anula una orden de compra revirtiendo todos sus efectos asociados.
     *
     * @param purchaseOrder orden de compra a anular (debe estar en APR/FIN/LIQ)
     * @param reason        motivo de anulacion (requerido)
     * @param userCode      usuario que ejecuta la anulacion
     */
    void reversePurchaseOrder(PurchaseOrder purchaseOrder,
                              String reason,
                              String userCode)
            throws ReverseNotAllowedException,
                   InventoryUnitaryBalanceException,
                   InventoryProductItemNotFoundException;

    /**
     * Valida si la orden de compra puede anularse por el flujo de reversion.
     * Usado por la UI para renderizar el boton Anular/Revertir.
     */
    void validateReversibility(PurchaseOrder purchaseOrder)
            throws ReverseNotAllowedException;
}
