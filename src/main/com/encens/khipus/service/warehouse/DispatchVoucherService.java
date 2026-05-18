package com.encens.khipus.service.warehouse;

import com.encens.khipus.model.warehouse.WarehouseVoucherDispatch;

import javax.ejb.Local;

/**
 * Servicio para gestion del Vale de Despacho de Productos Terminados.
 * <p>
 * En esta fase solo expone operaciones de CRUD en estado BORRADOR. Los
 * metodos de aprobacion (con generacion de WarehouseVoucher, descuento
 * de inventario y asiento contable) y anulacion se agregaran en Fase 5
 * y Fase 6 respectivamente.
 */
@Local
public interface DispatchVoucherService {

    /**
     * Persiste un Despacho en estado BORRADOR.
     * Asigna auditoria createdBy/createdDate desde el usuario actual.
     */
    WarehouseVoucherDispatch saveDraft(WarehouseVoucherDispatch dispatch);

    /**
     * Actualiza un Despacho existente en estado BORRADOR.
     * Asigna auditoria updatedBy/updatedDate desde el usuario actual.
     */
    WarehouseVoucherDispatch updateDraft(WarehouseVoucherDispatch dispatch);

    /**
     * Elimina un Despacho en estado BORRADOR (con sus lineas, por cascade).
     * Lanza IllegalStateException si el despacho no esta en BORRADOR.
     */
    void deleteDraft(WarehouseVoucherDispatch dispatch);

    /**
     * Recupera un Despacho por id (refresca desde DB).
     */
    WarehouseVoucherDispatch findById(Long id);
}
