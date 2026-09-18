package com.encens.khipus.service.xproduction;

import java.math.BigDecimal;

/**
 * Saldo de un articulo atribuido a una zona productiva: lo acopiado de la zona menos lo que
 * las ordenes de produccion declararon consumir de ella (insumo MP x porcentaje de la zona).
 * DTO en memoria para el desglose por zona de "Saldos de Almacen" y el disponible por zona
 * de la orden. La fila "Sin zona asignada" ({@code zoneId == null}) es el remanente del saldo
 * del articulo que ninguna zona explica: vales, ajustes y ordenes sin distribucion.
 */
public class ZoneBalanceRow {

    private final Long zoneId;
    private final String zoneName;
    private BigDecimal collected = BigDecimal.ZERO;
    private BigDecimal consumed = BigDecimal.ZERO;
    private BigDecimal balance;

    public ZoneBalanceRow(Long zoneId, String zoneName) {
        this.zoneId = zoneId;
        this.zoneName = zoneName;
    }

    /** Fila "Sin zona asignada" con el remanente ya calculado. */
    public static ZoneBalanceRow unassigned(BigDecimal balance) {
        ZoneBalanceRow row = new ZoneBalanceRow(null, null);
        row.balance = balance;
        return row;
    }

    public void addCollected(BigDecimal value) {
        if (value != null) {
            collected = collected.add(value);
        }
    }

    public void addConsumed(BigDecimal value) {
        if (value != null) {
            consumed = consumed.add(value);
        }
    }

    public Long getZoneId() {
        return zoneId;
    }

    public String getZoneName() {
        return zoneName;
    }

    public BigDecimal getCollected() {
        return collected;
    }

    public BigDecimal getConsumed() {
        return consumed;
    }

    /** Acopiado - consumido; en la fila "Sin zona" es el remanente del saldo del articulo. */
    public BigDecimal getBalance() {
        return balance != null ? balance : collected.subtract(consumed);
    }

    public boolean isUnassigned() {
        return zoneId == null;
    }

    public boolean isNegative() {
        return getBalance().signum() < 0;
    }
}
