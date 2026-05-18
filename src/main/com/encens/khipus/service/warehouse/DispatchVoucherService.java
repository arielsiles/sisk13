package com.encens.khipus.service.warehouse;

import com.encens.khipus.exception.ConcurrencyException;
import com.encens.khipus.exception.ReferentialIntegrityException;
import com.encens.khipus.exception.finances.CompanyConfigurationNotFoundException;
import com.encens.khipus.exception.finances.FinancesCurrencyNotFoundException;
import com.encens.khipus.exception.finances.FinancesExchangeRateNotFoundException;
import com.encens.khipus.exception.warehouse.*;
import com.encens.khipus.model.warehouse.DispatchStockImpact;
import com.encens.khipus.model.warehouse.WarehouseVoucherDispatch;
import com.encens.khipus.util.ValidatorUtil;

import javax.ejb.Local;
import java.util.List;

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

    /**
     * Calcula el impacto en inventario sin persistir nada. Se usa para el
     * Paso 2 del modal de aprobacion: muestra saldo actual y saldo resultante
     * por cada linea del despacho.
     */
    List<DispatchStockImpact> calculateInventoryImpact(WarehouseVoucherDispatch dispatch);

    /**
     * Aprueba el Despacho:
     *   1. Re-valida (estado BORRADOR, saldo suficiente por linea).
     *   2. Asigna el N de Orden de Entrega desde la secuencia
     *      'DISPATCH_ORDER_NUMBER'.
     *   3. Construye un WarehouseVoucher de egreso (cod_doc='DSP',
     *      tipo_vale=S) con su InventoryMovement y MovementDetail.
     *   4. Persiste y aprueba el vale via warehouseService +
     *      approvalWarehouseVoucherService (que descuenta inventario y
     *      genera el asiento contable).
     *   5. Enlaza el vale al Despacho y cambia su estado a APROBADO.
     * <p>
     * Toda la operacion va en una transaccion REQUIRES_NEW; ante cualquier
     * excepcion checked se hace rollback y el despacho queda en BORRADOR.
     */
    WarehouseVoucherDispatch approve(WarehouseVoucherDispatch dispatch)
            throws InventoryException,
                   WarehouseVoucherApprovedException,
                   WarehouseVoucherNotFoundException,
                   WarehouseVoucherEmptyException,
                   ProductItemAmountException,
                   InventoryUnitaryBalanceException,
                   InventoryProductItemNotFoundException,
                   CompanyConfigurationNotFoundException,
                   FinancesCurrencyNotFoundException,
                   FinancesExchangeRateNotFoundException,
                   ConcurrencyException,
                   ReferentialIntegrityException,
                   ProductItemNotFoundException,
                   WarehouseAccountCashNotFoundException;

    /**
     * Anula un Despacho aprobado:
     *   1. Re-valida estado APROBADO y existencia del WarehouseVoucher
     *      enlazado.
     *   2. Delega en ReverseWarehouseVoucherService.reverseWarehouseVoucher
     *      (con skipValidation=true porque la anulacion es manejada por
     *      este flujo y no por el flujo independiente de vale): revierte
     *      inventario, costo promedio, historial y genera contra-asiento.
     *   3. Marca el Despacho como ANULADO con auditoria (annulDate,
     *      annulUser, annulReason).
     */
    WarehouseVoucherDispatch annul(WarehouseVoucherDispatch dispatch, String reason)
            throws ReverseNotAllowedException,
                   WarehouseVoucherNotFoundException,
                   InventoryUnitaryBalanceException,
                   InventoryProductItemNotFoundException;
}
