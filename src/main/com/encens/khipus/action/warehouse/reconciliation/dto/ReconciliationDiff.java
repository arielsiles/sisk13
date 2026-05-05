package com.encens.khipus.action.warehouse.reconciliation.dto;

import java.math.BigDecimal;

/**
 * Fila del listado principal de diferencias en la pantalla
 * "Actualizar Inventario". Una por articulo cuyo saldo calculado
 * por el reporte general difiere de inv_inventario.saldo_uni.
 */
public class ReconciliationDiff {

    private String productItemCode;
    private String productItemName;
    private BigDecimal calculatedQuantity;
    private BigDecimal currentQuantity;
    private BigDecimal difference;

    public ReconciliationDiff() {}

    public ReconciliationDiff(String productItemCode, String productItemName,
                              BigDecimal calculatedQuantity, BigDecimal currentQuantity) {
        this.productItemCode = productItemCode;
        this.productItemName = productItemName;
        this.calculatedQuantity = calculatedQuantity;
        this.currentQuantity = currentQuantity;
        this.difference = calculatedQuantity.subtract(currentQuantity);
    }

    public String getProductItemCode() { return productItemCode; }
    public void setProductItemCode(String productItemCode) { this.productItemCode = productItemCode; }

    public String getProductItemName() { return productItemName; }
    public void setProductItemName(String productItemName) { this.productItemName = productItemName; }

    public BigDecimal getCalculatedQuantity() { return calculatedQuantity; }
    public void setCalculatedQuantity(BigDecimal calculatedQuantity) { this.calculatedQuantity = calculatedQuantity; }

    public BigDecimal getCurrentQuantity() { return currentQuantity; }
    public void setCurrentQuantity(BigDecimal currentQuantity) { this.currentQuantity = currentQuantity; }

    public BigDecimal getDifference() { return difference; }
    public void setDifference(BigDecimal difference) { this.difference = difference; }
}
