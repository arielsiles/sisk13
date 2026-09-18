package com.encens.khipus.service.xproduction;

import java.util.ArrayList;
import java.util.List;

/**
 * Grupo de filas de saldo agrupadas por el Subgrupo del articulo, para mostrar
 * la tabla de "Saldos de Almacen" agrupada con un encabezado por subgrupo.
 */
public class BalanceGroup {

    private final String subGroupCode;
    private final String subGroupName;
    private final List<WarehouseBalanceRow> rows = new ArrayList<WarehouseBalanceRow>();
    /** Filas de articulo con las de zona intercaladas debajo de las desplegadas. */
    private final List<BalanceLine> lines = new ArrayList<BalanceLine>();

    public BalanceGroup(String subGroupCode, String subGroupName) {
        this.subGroupCode = subGroupCode;
        this.subGroupName = subGroupName;
    }

    public String getSubGroupCode() {
        return subGroupCode;
    }

    public String getSubGroupName() {
        return subGroupName;
    }

    public List<WarehouseBalanceRow> getRows() {
        return rows;
    }

    public List<BalanceLine> getLines() {
        return lines;
    }
}
