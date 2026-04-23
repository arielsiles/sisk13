package com.encens.khipus.service.warehouse;

import com.encens.khipus.exception.warehouse.InventoryProductItemNotFoundException;
import com.encens.khipus.exception.warehouse.InventoryUnitaryBalanceException;
import com.encens.khipus.exception.warehouse.ReverseNotAllowedException;
import com.encens.khipus.exception.warehouse.WarehouseVoucherNotFoundException;
import com.encens.khipus.framework.service.GenericService;
import com.encens.khipus.model.warehouse.WarehouseVoucher;
import com.encens.khipus.model.warehouse.WarehouseVoucherPK;

import javax.ejb.Local;

/**
 * Orquesta la anulacion de un {@link WarehouseVoucher} aprobado: revierte los
 * movimientos de inventario, recalcula el costo promedio del ProductItem,
 * revierte el historial mensual y genera el contra-asiento contable
 * correspondiente. Marca el vale como ANL y registra auditoria.
 *
 * Este servicio es reutilizado tambien por la anulacion de Ordenes de Compra,
 * que delega en el para revertir su vale de RECEPCION auto-generado.
 *
 * @version 6.0.70
 */
@Local
public interface ReverseWarehouseVoucherService extends GenericService {

    /**
     * Anula un vale aprobado o parcial y revierte todos sus efectos.
     *
     * @param id              clave primaria del vale
     * @param reason          motivo de la anulacion (requerido, no vacio)
     * @param userCode        usuario de finanzas que ejecuta la anulacion
     * @param skipValidation  si true, permite que el llamador (p.ej. flujo de
     *                        anulacion de OC) salte ciertas validaciones que
     *                        aplican al vale independiente (como bloqueo de
     *                        vales con purchaseOrder asociada)
     */
    void reverseWarehouseVoucher(WarehouseVoucherPK id,
                                 String reason,
                                 String userCode,
                                 boolean skipValidation)
            throws ReverseNotAllowedException,
                   WarehouseVoucherNotFoundException,
                   InventoryUnitaryBalanceException,
                   InventoryProductItemNotFoundException;

    /**
     * Valida si un vale es anulable por el flujo independiente (sin OC).
     * Lanza {@link ReverseNotAllowedException} con mensaje i18n indicando
     * la causa del rechazo.
     */
    void validateReversibility(WarehouseVoucher warehouseVoucher)
            throws ReverseNotAllowedException;
}
