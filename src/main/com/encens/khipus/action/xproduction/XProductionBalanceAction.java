package com.encens.khipus.action.xproduction;

import com.encens.khipus.model.warehouse.Warehouse;
import com.encens.khipus.service.xproduction.BalanceGroup;
import com.encens.khipus.service.xproduction.WarehouseBalanceRow;
import com.encens.khipus.service.xproduction.XProductionBalanceService;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import javax.persistence.EntityManager;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Vista "Saldos de Almacen" (Produccion > Saldos): muestra el saldo RECALCULADO
 * desde el origen de los movimientos para los productos de un almacen de Materia
 * Prima o Producto Terminado. El saldo no se lee de inv_inventario.
 */
@Name("xproductionBalanceAction")
@Scope(ScopeType.CONVERSATION)
public class XProductionBalanceAction {

    @In(create = true)
    private XProductionBalanceService xproductionBalanceService;

    /**
     * Contexto de persistencia de la conversacion. Se limpia al inicio de refresh() para
     * recalcular los saldos frescos del DB (mismo tratamiento que el Kardex), de modo que
     * ambos reportes cuadren siempre y no muestren un snapshot/cache viejo.
     */
    @In
    private EntityManager entityManager;

    private Warehouse selectedWarehouse;
    /** Fecha de corte: los saldos se calculan hasta esta fecha. Por defecto, la fecha actual. */
    private Date balanceDate = new Date();
    private List<WarehouseBalanceRow> balanceList = new ArrayList<WarehouseBalanceRow>();

    /** Almacenes de Materia Prima y Producto Terminado para el filtro. */
    public List<Warehouse> getWarehouses() {
        return xproductionBalanceService.findBalanceWarehouses();
    }

    /** Recalcula los saldos del almacen seleccionado desde el origen de los movimientos. */
    public void refresh() {
        // Refresca el contexto de persistencia de la conversacion antes de recalcular, para no
        // arrastrar un snapshot/cache L1 viejo (mismo criterio que el Kardex). Asi Saldos y
        // Kardex siempre cuadran ante cambios hechos en otra sesion.
        entityManager.clear();
        if (selectedWarehouse == null) {
            balanceList = new ArrayList<WarehouseBalanceRow>();
            return;
        }
        balanceList = xproductionBalanceService.computeBalances(
                selectedWarehouse.getId().getCompanyNumber(),
                selectedWarehouse.getId().getWarehouseCode(),
                balanceDate);
    }

    public Warehouse getSelectedWarehouse() {
        return selectedWarehouse;
    }

    public void setSelectedWarehouse(Warehouse selectedWarehouse) {
        this.selectedWarehouse = selectedWarehouse;
    }

    public Date getBalanceDate() {
        return balanceDate;
    }

    public void setBalanceDate(Date balanceDate) {
        this.balanceDate = balanceDate;
    }

    public List<WarehouseBalanceRow> getBalanceList() {
        return balanceList;
    }

    /** Filas agrupadas por Subgrupo del articulo (la lista ya viene ordenada por subgrupo+nombre). */
    public List<BalanceGroup> getGroups() {
        List<BalanceGroup> groups = new ArrayList<BalanceGroup>();
        BalanceGroup current = null;
        for (WarehouseBalanceRow row : balanceList) {
            String code = row.getSubGroupCode() == null ? "" : row.getSubGroupCode();
            if (current == null || !code.equals(current.getSubGroupCode())) {
                current = new BalanceGroup(code, row.getSubGroupName());
                groups.add(current);
            }
            current.getRows().add(row);
        }
        return groups;
    }
}
