package com.encens.khipus.model.production;

import com.encens.khipus.model.BaseModel;
import com.encens.khipus.model.CompanyListener;
import com.encens.khipus.model.admin.Company;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * Bitacora de reversiones de un acopio de materia prima. Una fila por reversion.
 * Es registro de control interno: no se edita ni se borra desde la aplicacion.
 */
@Table(name = "acopiomp_reversion")
@Entity
@EntityListeners(CompanyListener.class)
public class CollectMaterialRevert implements Serializable, BaseModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idacopiomp_reversion", nullable = false)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "idacopiomp", nullable = false, updatable = false)
    private CollectMaterial collectMaterial;

    @Column(name = "fecha_hora", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date dateTime;

    @Column(name = "usuario", nullable = false, length = 100)
    private String user;

    @Column(name = "motivo", nullable = false, length = 500)
    private String reason;

    /** Estado del acopio antes de revertir: APR o CONTA. */
    @Column(name = "estado_anterior", nullable = false, length = 25)
    @Enumerated(EnumType.STRING)
    private CollectMaterialState previousState;

    /** Asiento vinculado al momento de revertir (NULL si no tenia). */
    @Column(name = "id_tmpenc")
    private Long voucherId;

    @Column(name = "saldo_antes", precision = 12, scale = 2)
    private BigDecimal balanceBefore;

    @Column(name = "saldo_despues", precision = 12, scale = 2)
    private BigDecimal balanceAfter;

    @Column(name = "costo_antes", precision = 16, scale = 6)
    private BigDecimal unitCostBefore;

    @Column(name = "costo_despues", precision = 16, scale = 6)
    private BigDecimal unitCostAfter;

    @Column(name = "version")
    private Long version = 0L;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "idcompania", nullable = false, updatable = false)
    private Company company;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public CollectMaterial getCollectMaterial() {
        return collectMaterial;
    }

    public void setCollectMaterial(CollectMaterial collectMaterial) {
        this.collectMaterial = collectMaterial;
    }

    public Date getDateTime() {
        return dateTime;
    }

    public void setDateTime(Date dateTime) {
        this.dateTime = dateTime;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public CollectMaterialState getPreviousState() {
        return previousState;
    }

    public void setPreviousState(CollectMaterialState previousState) {
        this.previousState = previousState;
    }

    public Long getVoucherId() {
        return voucherId;
    }

    public void setVoucherId(Long voucherId) {
        this.voucherId = voucherId;
    }

    public BigDecimal getBalanceBefore() {
        return balanceBefore;
    }

    public void setBalanceBefore(BigDecimal balanceBefore) {
        this.balanceBefore = balanceBefore;
    }

    public BigDecimal getBalanceAfter() {
        return balanceAfter;
    }

    public void setBalanceAfter(BigDecimal balanceAfter) {
        this.balanceAfter = balanceAfter;
    }

    public BigDecimal getUnitCostBefore() {
        return unitCostBefore;
    }

    public void setUnitCostBefore(BigDecimal unitCostBefore) {
        this.unitCostBefore = unitCostBefore;
    }

    public BigDecimal getUnitCostAfter() {
        return unitCostAfter;
    }

    public void setUnitCostAfter(BigDecimal unitCostAfter) {
        this.unitCostAfter = unitCostAfter;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }
}
