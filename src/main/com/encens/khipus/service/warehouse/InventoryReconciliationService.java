package com.encens.khipus.service.warehouse;

import com.encens.khipus.action.warehouse.reconciliation.dto.ReconciliationDetail;
import com.encens.khipus.action.warehouse.reconciliation.dto.ReconciliationDiff;
import com.encens.khipus.exception.warehouse.MultipleWarehouseException;
import com.encens.khipus.exception.warehouse.NegativeFinalBalanceException;
import com.encens.khipus.exception.warehouse.NoInitialInventoryException;

import javax.ejb.Local;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Servicio de la pantalla "Almacenes > Configuracion > Actualizar Inventario".
 * Compara saldos calculados (provistos por el caller) contra inv_inventario,
 * reconstruye el costo promedio cronologico de un articulo y aplica el ajuste
 * sobre inv_inventario, inv_inventario_detalle e inv_articulos.
 */
@Local
public interface InventoryReconciliationService {

    /**
     * Compara los saldos calculados del reporte contra inv_inventario.saldo_uni
     * para todos los articulos del almacen y devuelve solo los que difieren.
     *
     * @param warehouseCode    almacen objetivo
     * @param calculatedSaldos mapa cod_art -> saldo calculado por el reporte
     * @return diferencias ordenadas por |diff| descendente
     */
    List<ReconciliationDiff> listDifferences(String warehouseCode,
                                             Map<String, BigDecimal> calculatedSaldos);

    /**
     * Reconstruye cronologicamente, desde el primer inv_inicio del articulo,
     * la cantidad y el costo promedio aplicando todos los movimientos APR
     * hasta endDate.
     *
     * @throws MultipleWarehouseException si el articulo aparece en mas de un almacen
     * @throws NoInitialInventoryException si no hay inv_inicio en ninguna gestion
     */
    ReconciliationDetail reconstructArticle(String productItemCode,
                                            String warehouseCode,
                                            Date startDate,
                                            Date endDate,
                                            BigDecimal reportQuantity)
            throws MultipleWarehouseException, NoInitialInventoryException;

    /**
     * Aplica el ajuste calculado a las 4 tablas e inserta auditoria.
     * Re-valida multi-almacen y saldo final no negativo dentro de la
     * misma transaccion.
     *
     * @throws MultipleWarehouseException     si entre el calculo y el apply
     *                                        el articulo paso a estar en mas almacenes
     * @throws NegativeFinalBalanceException  si la cantidad final es negativa
     */
    void applyAdjustment(ReconciliationDetail detail,
                         Date calcStartDate,
                         Date calcEndDate,
                         String reason,
                         String userNumber)
            throws MultipleWarehouseException, NegativeFinalBalanceException;
}
