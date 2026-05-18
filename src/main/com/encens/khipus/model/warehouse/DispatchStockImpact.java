package com.encens.khipus.model.warehouse;

import com.encens.khipus.model.finances.MeasureUnit;

import java.math.BigDecimal;

/**
 * POJO no persistente utilizado en el modal "Paso 2 - Confirmacion del
 * impacto en inventario" durante la aprobacion del Vale de Despacho.
 * Refleja el saldo actual y el saldo resultante por linea de producto.
 */
public class DispatchStockImpact {

    private ProductItem productItem;
    private MeasureUnit measureUnit;
    private BigDecimal currentStock;
    private BigDecimal requiredQuantity;
    private BigDecimal resultStock;
    private BigDecimal unitCost;
    private BigDecimal amount;
    private boolean sufficient;

    public DispatchStockImpact() {
    }

    public DispatchStockImpact(ProductItem productItem, MeasureUnit measureUnit,
                               BigDecimal currentStock, BigDecimal requiredQuantity,
                               BigDecimal resultStock, BigDecimal unitCost,
                               BigDecimal amount) {
        this.productItem = productItem;
        this.measureUnit = measureUnit;
        this.currentStock = currentStock;
        this.requiredQuantity = requiredQuantity;
        this.resultStock = resultStock;
        this.unitCost = unitCost;
        this.amount = amount;
        this.sufficient = resultStock != null && resultStock.signum() >= 0;
    }

    public ProductItem getProductItem() {
        return productItem;
    }

    public void setProductItem(ProductItem productItem) {
        this.productItem = productItem;
    }

    public MeasureUnit getMeasureUnit() {
        return measureUnit;
    }

    public void setMeasureUnit(MeasureUnit measureUnit) {
        this.measureUnit = measureUnit;
    }

    public BigDecimal getCurrentStock() {
        return currentStock;
    }

    public void setCurrentStock(BigDecimal currentStock) {
        this.currentStock = currentStock;
    }

    public BigDecimal getRequiredQuantity() {
        return requiredQuantity;
    }

    public void setRequiredQuantity(BigDecimal requiredQuantity) {
        this.requiredQuantity = requiredQuantity;
    }

    public BigDecimal getResultStock() {
        return resultStock;
    }

    public void setResultStock(BigDecimal resultStock) {
        this.resultStock = resultStock;
        this.sufficient = resultStock != null && resultStock.signum() >= 0;
    }

    public BigDecimal getUnitCost() {
        return unitCost;
    }

    public void setUnitCost(BigDecimal unitCost) {
        this.unitCost = unitCost;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public boolean isSufficient() {
        return sufficient;
    }

    public void setSufficient(boolean sufficient) {
        this.sufficient = sufficient;
    }
}
