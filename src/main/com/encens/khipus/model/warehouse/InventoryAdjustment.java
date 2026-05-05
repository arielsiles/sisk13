package com.encens.khipus.model.warehouse;

import com.encens.khipus.model.BaseModel;
import com.encens.khipus.util.Constants;
import org.hibernate.validator.Length;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;

/**
 * Auditoria de ajustes manuales aplicados desde la pantalla
 * "Almacenes > Configuracion > Actualizar Inventario".
 * Una fila por aplicacion de ajuste. No se actualiza despues de creada.
 */
@Entity
@Table(name = "inv_ajuste_saldo", schema = Constants.FINANCES_SCHEMA)
public class InventoryAdjustment implements BaseModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_ajuste", nullable = false)
    private Long id;

    @Column(name = "no_cia", nullable = false, length = 2)
    @Length(max = 2)
    private String companyNumber;

    @Column(name = "cod_alm", nullable = false, length = 6)
    @Length(max = 6)
    private String warehouseCode;

    @Column(name = "cod_art", nullable = false, length = 6)
    @Length(max = 6)
    private String productItemCode;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "fecha_ajuste", nullable = false)
    private Date adjustmentDate;

    @Temporal(TemporalType.DATE)
    @Column(name = "fecha_inicio_calc", nullable = false)
    private Date calcStartDate;

    @Temporal(TemporalType.DATE)
    @Column(name = "fecha_fin_calc", nullable = false)
    private Date calcEndDate;

    @Column(name = "saldo_uni_anterior", precision = 16, scale = 2)
    private BigDecimal previousQuantity;

    @Column(name = "saldo_uni_nuevo", precision = 16, scale = 2)
    private BigDecimal newQuantity;

    @Column(name = "costo_uni_anterior", precision = 16, scale = 6)
    private BigDecimal previousUnitCost;

    @Column(name = "costo_uni_nuevo", precision = 16, scale = 6)
    private BigDecimal newUnitCost;

    @Column(name = "saldo_mon_anterior", precision = 20, scale = 6)
    private BigDecimal previousMonetaryBalance;

    @Column(name = "saldo_mon_nuevo", precision = 20, scale = 6)
    private BigDecimal newMonetaryBalance;

    @Column(name = "motivo", nullable = false, length = 500)
    @Length(max = 500)
    private String reason;

    @Column(name = "no_usr", length = 4)
    @Length(max = 4)
    private String userNumber;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCompanyNumber() { return companyNumber; }
    public void setCompanyNumber(String companyNumber) { this.companyNumber = companyNumber; }

    public String getWarehouseCode() { return warehouseCode; }
    public void setWarehouseCode(String warehouseCode) { this.warehouseCode = warehouseCode; }

    public String getProductItemCode() { return productItemCode; }
    public void setProductItemCode(String productItemCode) { this.productItemCode = productItemCode; }

    public Date getAdjustmentDate() { return adjustmentDate; }
    public void setAdjustmentDate(Date adjustmentDate) { this.adjustmentDate = adjustmentDate; }

    public Date getCalcStartDate() { return calcStartDate; }
    public void setCalcStartDate(Date calcStartDate) { this.calcStartDate = calcStartDate; }

    public Date getCalcEndDate() { return calcEndDate; }
    public void setCalcEndDate(Date calcEndDate) { this.calcEndDate = calcEndDate; }

    public BigDecimal getPreviousQuantity() { return previousQuantity; }
    public void setPreviousQuantity(BigDecimal previousQuantity) { this.previousQuantity = previousQuantity; }

    public BigDecimal getNewQuantity() { return newQuantity; }
    public void setNewQuantity(BigDecimal newQuantity) { this.newQuantity = newQuantity; }

    public BigDecimal getPreviousUnitCost() { return previousUnitCost; }
    public void setPreviousUnitCost(BigDecimal previousUnitCost) { this.previousUnitCost = previousUnitCost; }

    public BigDecimal getNewUnitCost() { return newUnitCost; }
    public void setNewUnitCost(BigDecimal newUnitCost) { this.newUnitCost = newUnitCost; }

    public BigDecimal getPreviousMonetaryBalance() { return previousMonetaryBalance; }
    public void setPreviousMonetaryBalance(BigDecimal previousMonetaryBalance) { this.previousMonetaryBalance = previousMonetaryBalance; }

    public BigDecimal getNewMonetaryBalance() { return newMonetaryBalance; }
    public void setNewMonetaryBalance(BigDecimal newMonetaryBalance) { this.newMonetaryBalance = newMonetaryBalance; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public String getUserNumber() { return userNumber; }
    public void setUserNumber(String userNumber) { this.userNumber = userNumber; }
}
