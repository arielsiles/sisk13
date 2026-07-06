package com.encens.khipus.service.xproduction;

import com.encens.khipus.model.warehouse.Warehouse;

import javax.ejb.Local;
import java.util.Date;
import java.util.List;

/**
 * Servicio de "Saldos de Almacen" para Materias Primas y Productos Terminados.
 *
 * El saldo NO se lee de inv_inventario (se considera incorrecto): se RECALCULA
 * desde el origen de los movimientos (toda la historia) sumando entradas y
 * restando salidas de cada fuente. Mas adelante un cierre mensual servira de
 * saldo inicial; por ahora se recorre todo.
 */
@Local
public interface XProductionBalanceService {

    /** Almacenes de tipo Materia Prima y Producto Terminado, no bloqueados (para el filtro). */
    List<Warehouse> findBalanceWarehouses();

    /**
     * Recalcula el saldo de cada producto del almacen desde el origen:
     *   Saldo = Σ MovementDetail(E)  − Σ MovementDetail(S)     [vales + despachos]
     *         + Σ CollectMaterial    [acopio]
     *         + Σ XProductionProduct [PT producido]
     *         − Σ XSupply            [consumo en ordenes: MP o PT usado como insumo]
     * Atribuido por el almacen (cod_alm) del articulo. Excluye solo movimientos ANL.
     * Devuelve TODOS los productos del almacen, incluso con saldo 0.
     *
     * El saldo se calcula HASTA {@code date} (inclusive, fin del dia): cada fuente
     * cuenta solo los movimientos cuya fecha sea &lt;= a la fecha seleccionada.
     */
    List<WarehouseBalanceRow> computeBalances(String companyNumber, String warehouseCode, Date date);
}
