package com.encens.khipus.action.warehouse.reconciliation.dto;

import java.math.BigDecimal;
import java.util.Date;

/**
 * Una fila del trace cronologico que se muestra al usuario en el modal
 * de detalle, representando el efecto de un movimiento sobre el saldo
 * y el costo promedio del articulo.
 */
public class ReconciliationStep {

    public enum StepType { INV_INICIO, ENTRADA, SALIDA }

    private Date date;
    private String reference;        // INV_INICIO[gestion] o no_trans del vale
    private StepType type;
    private BigDecimal quantity;
    private BigDecimal movementUnitCost;   // costo del movimiento (solo para entradas)
    private BigDecimal runningQuantity;    // saldo despues del movimiento
    private BigDecimal runningUnitCost;    // costo promedio despues del movimiento
    private BigDecimal runningTotalValue;  // saldo monetario despues del movimiento
    private BigDecimal accountingAmount;   // valor contable (debe + haber de sf_tmpdet)
    private boolean negativeFlag;          // true si runningQuantity < 0 en este punto

    public ReconciliationStep() {}

    public Date getDate() { return date; }
    public void setDate(Date date) { this.date = date; }

    public String getReference() { return reference; }
    public void setReference(String reference) { this.reference = reference; }

    public StepType getType() { return type; }
    public void setType(StepType type) { this.type = type; }

    public BigDecimal getQuantity() { return quantity; }
    public void setQuantity(BigDecimal quantity) { this.quantity = quantity; }

    public BigDecimal getMovementUnitCost() { return movementUnitCost; }
    public void setMovementUnitCost(BigDecimal movementUnitCost) { this.movementUnitCost = movementUnitCost; }

    public BigDecimal getRunningQuantity() { return runningQuantity; }
    public void setRunningQuantity(BigDecimal runningQuantity) { this.runningQuantity = runningQuantity; }

    public BigDecimal getRunningUnitCost() { return runningUnitCost; }
    public void setRunningUnitCost(BigDecimal runningUnitCost) { this.runningUnitCost = runningUnitCost; }

    public BigDecimal getRunningTotalValue() { return runningTotalValue; }
    public void setRunningTotalValue(BigDecimal runningTotalValue) { this.runningTotalValue = runningTotalValue; }

    public BigDecimal getAccountingAmount() { return accountingAmount; }
    public void setAccountingAmount(BigDecimal accountingAmount) { this.accountingAmount = accountingAmount; }

    public boolean isNegativeFlag() { return negativeFlag; }
    public void setNegativeFlag(boolean negativeFlag) { this.negativeFlag = negativeFlag; }

    public String getTypeLabel() {
        if (type == null) return "";
        switch (type) {
            case INV_INICIO: return "INI";
            case ENTRADA:    return "E";
            case SALIDA:     return "S";
            default:         return "";
        }
    }
}
