package com.encens.khipus.action.warehouse.reconciliation.dto;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Resultado completo de la reconstruccion de un articulo. Lo consume el
 * modal de detalle y, si el usuario confirma, el service de aplicacion.
 *
 * Contiene:
 *  - el trace cronologico paso a paso
 *  - los valores reconstruidos finales (cantidad, costo, saldo monetario)
 *  - los valores actuales en BD para comparacion
 *  - alertas no bloqueantes (negativos en el camino, discrepancia con reporte)
 *  - flag bloqueante si el saldo final es negativo
 */
public class ReconciliationDetail {

    private String productItemCode;
    private String productItemName;
    private String warehouseCode;
    private String warehouseName;

    private List<ReconciliationStep> trace = new ArrayList<ReconciliationStep>();

    /** Reconstruccion */
    private BigDecimal reconstructedQuantity;
    private BigDecimal reconstructedUnitCost;
    private BigDecimal reconstructedMonetaryBalance;

    /** Valor de referencia del calculo del reporte general */
    private BigDecimal reportQuantity;

    /** Valores actuales en BD */
    private BigDecimal currentQuantity;
    private BigDecimal currentUnitCost;
    private BigDecimal currentMonetaryBalance;

    /** Alertas */
    private boolean hasIntermediateNegatives;     // hubo qty < 0 en algun paso
    private boolean reportMismatch;               // reconstructedQuantity != reportQuantity
    private boolean blockedByNegativeFinal;       // qty final < 0 -> no se puede aplicar

    /** Versions cargadas, usadas para detectar race en applyAdjustment */
    private Long inventoryVersion;
    private Long inventoryDetailVersion;
    private Long productItemVersion;

    public String getProductItemCode() { return productItemCode; }
    public void setProductItemCode(String productItemCode) { this.productItemCode = productItemCode; }

    public String getProductItemName() { return productItemName; }
    public void setProductItemName(String productItemName) { this.productItemName = productItemName; }

    public String getWarehouseCode() { return warehouseCode; }
    public void setWarehouseCode(String warehouseCode) { this.warehouseCode = warehouseCode; }

    public String getWarehouseName() { return warehouseName; }
    public void setWarehouseName(String warehouseName) { this.warehouseName = warehouseName; }

    public List<ReconciliationStep> getTrace() { return trace; }
    public void setTrace(List<ReconciliationStep> trace) { this.trace = trace; }

    public BigDecimal getReconstructedQuantity() { return reconstructedQuantity; }
    public void setReconstructedQuantity(BigDecimal reconstructedQuantity) { this.reconstructedQuantity = reconstructedQuantity; }

    public BigDecimal getReconstructedUnitCost() { return reconstructedUnitCost; }
    public void setReconstructedUnitCost(BigDecimal reconstructedUnitCost) { this.reconstructedUnitCost = reconstructedUnitCost; }

    public BigDecimal getReconstructedMonetaryBalance() { return reconstructedMonetaryBalance; }
    public void setReconstructedMonetaryBalance(BigDecimal reconstructedMonetaryBalance) { this.reconstructedMonetaryBalance = reconstructedMonetaryBalance; }

    public BigDecimal getReportQuantity() { return reportQuantity; }
    public void setReportQuantity(BigDecimal reportQuantity) { this.reportQuantity = reportQuantity; }

    public BigDecimal getCurrentQuantity() { return currentQuantity; }
    public void setCurrentQuantity(BigDecimal currentQuantity) { this.currentQuantity = currentQuantity; }

    public BigDecimal getCurrentUnitCost() { return currentUnitCost; }
    public void setCurrentUnitCost(BigDecimal currentUnitCost) { this.currentUnitCost = currentUnitCost; }

    public BigDecimal getCurrentMonetaryBalance() { return currentMonetaryBalance; }
    public void setCurrentMonetaryBalance(BigDecimal currentMonetaryBalance) { this.currentMonetaryBalance = currentMonetaryBalance; }

    public boolean isHasIntermediateNegatives() { return hasIntermediateNegatives; }
    public void setHasIntermediateNegatives(boolean hasIntermediateNegatives) { this.hasIntermediateNegatives = hasIntermediateNegatives; }

    public boolean isReportMismatch() { return reportMismatch; }
    public void setReportMismatch(boolean reportMismatch) { this.reportMismatch = reportMismatch; }

    public boolean isBlockedByNegativeFinal() { return blockedByNegativeFinal; }
    public void setBlockedByNegativeFinal(boolean blockedByNegativeFinal) { this.blockedByNegativeFinal = blockedByNegativeFinal; }

    public Long getInventoryVersion() { return inventoryVersion; }
    public void setInventoryVersion(Long inventoryVersion) { this.inventoryVersion = inventoryVersion; }

    public Long getInventoryDetailVersion() { return inventoryDetailVersion; }
    public void setInventoryDetailVersion(Long inventoryDetailVersion) { this.inventoryDetailVersion = inventoryDetailVersion; }

    public Long getProductItemVersion() { return productItemVersion; }
    public void setProductItemVersion(Long productItemVersion) { this.productItemVersion = productItemVersion; }

    /** Diferencias para mostrar en la UX */
    public BigDecimal getQuantityDiff() {
        if (reconstructedQuantity == null || currentQuantity == null) return null;
        return reconstructedQuantity.subtract(currentQuantity);
    }
    public BigDecimal getUnitCostDiff() {
        if (reconstructedUnitCost == null || currentUnitCost == null) return null;
        return reconstructedUnitCost.subtract(currentUnitCost);
    }
    public BigDecimal getMonetaryDiff() {
        if (reconstructedMonetaryBalance == null || currentMonetaryBalance == null) return null;
        return reconstructedMonetaryBalance.subtract(currentMonetaryBalance);
    }
}
