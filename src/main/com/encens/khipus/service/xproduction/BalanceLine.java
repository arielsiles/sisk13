package com.encens.khipus.service.xproduction;

/**
 * Linea de la tabla de "Saldos de Almacen": la fila de un articulo o, debajo de el, la de
 * una de sus zonas productivas. Existe porque RichFaces no anida
 * un subTable dentro de otro: el desglose se aplana en la misma lista.
 */
public class BalanceLine {

    private final WarehouseBalanceRow row;
    private final ZoneBalanceRow zone;

    private BalanceLine(WarehouseBalanceRow row, ZoneBalanceRow zone) {
        this.row = row;
        this.zone = zone;
    }

    public static BalanceLine item(WarehouseBalanceRow row) {
        return new BalanceLine(row, null);
    }

    public static BalanceLine zone(WarehouseBalanceRow row, ZoneBalanceRow zone) {
        return new BalanceLine(row, zone);
    }

    public WarehouseBalanceRow getRow() {
        return row;
    }

    public ZoneBalanceRow getZone() {
        return zone;
    }

    public boolean isZoneLine() {
        return zone != null;
    }

    /**
     * Clase de las celdas: las de zona salen ocultas y marcadas con el articulo
     * ({@code zoneOf_<codArt>}) para que el chevron de ese articulo las despliegue en el navegador.
     */
    public String getCellClass() {
        return zone != null ? "zoneCell zoneOf_" + row.getProductItemCode() : "";
    }
}
