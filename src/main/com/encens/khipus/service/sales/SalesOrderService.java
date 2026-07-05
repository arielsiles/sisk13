package com.encens.khipus.service.sales;

import com.encens.khipus.exception.EntryDuplicatedException;
import com.encens.khipus.model.sales.SalesOrder;

import javax.ejb.Local;

/**
 * Logica de negocio de la Orden de Venta: alta con numeracion/snapshot/totales,
 * edicion y transiciones de estado (revision, aprobacion, rechazo, anulacion).
 *
 * @author
 * @version 1.0
 */
@Local
public interface SalesOrderService {

    /** Crea la orden en estado BORRADOR: asigna numero, fecha, snapshot y totales. */
    SalesOrder createSalesOrder(SalesOrder salesOrder) throws EntryDuplicatedException;

    /** Actualiza la orden (solo en BORRADOR): renumera lineas y recalcula totales. */
    void updateSalesOrder(SalesOrder salesOrder) throws EntryDuplicatedException;

    /** BORRADOR -> REVISION (enviada a Gerencia para aprobacion). */
    void sendToReview(SalesOrder salesOrder);

    /** REVISION -> APROBADO (Gerencia). */
    void approve(SalesOrder salesOrder, String user);

    /** REVISION -> BORRADOR con observaciones de Gerencia. */
    void reject(SalesOrder salesOrder, String observations);

    /** Cualquier estado -> ANULADO (no elimina). */
    void nullify(SalesOrder salesOrder);

    /** Recalcula subtotal y total (subtotal - descuento + recargo) en la instancia. */
    void computeTotals(SalesOrder salesOrder);
}
