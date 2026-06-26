package com.encens.khipus.service.xproduction;

import java.math.BigDecimal;

/**
 * Fila de saldo recalculado de un producto en un almacen. NO es entidad: es un
 * DTO en memoria para la vista de "Saldos de Almacen". El saldo se computa desde
 * el origen de los movimientos (no se lee de inv_inventario).
 */
public class WarehouseBalanceRow {

    private final String productItemCode;
    private final String name;
    private final String measureCode;
    private final String subGroupCode;
    private final String subGroupName;
    private BigDecimal balance;

    public WarehouseBalanceRow(String productItemCode, String name, String measureCode,
                               String subGroupCode, String subGroupName, BigDecimal balance) {
        this.productItemCode = productItemCode;
        this.name = name;
        this.measureCode = measureCode;
        this.subGroupCode = subGroupCode;
        this.subGroupName = subGroupName;
        this.balance = balance == null ? BigDecimal.ZERO : balance;
    }

    public void add(BigDecimal value) {
        if (value != null) {
            this.balance = this.balance.add(value);
        }
    }

    public void subtract(BigDecimal value) {
        if (value != null) {
            this.balance = this.balance.subtract(value);
        }
    }

    public String getProductItemCode() {
        return productItemCode;
    }

    public String getName() {
        return name;
    }

    public String getMeasureCode() {
        return measureCode;
    }

    public String getSubGroupCode() {
        return subGroupCode;
    }

    public String getSubGroupName() {
        return subGroupName;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }
}
