package com.encens.khipus.model.warehouse;

import com.encens.khipus.model.BaseModel;
import com.encens.khipus.model.CompanyNumberListener;
import com.encens.khipus.model.UpperCaseStringListener;
import com.encens.khipus.model.admin.BusinessUnit;
import com.encens.khipus.model.cashbox.Branch;
import com.encens.khipus.model.employees.Employee;
import com.encens.khipus.util.Constants;
import org.hibernate.annotations.Filter;
import org.hibernate.validator.Length;

import javax.persistence.*;

/**
 * @author
 * @version 2.0
 */
@NamedQueries({
        @NamedQuery(name = "Warehouse.findByCode", query = "select w from Warehouse w where w.warehouseCode =:warehouseCode")
})
@Entity
@Table(name = "inv_almacenes", schema = Constants.FINANCES_SCHEMA)
@EntityListeners({CompanyNumberListener.class, UpperCaseStringListener.class})
@Filter(name = com.encens.khipus.util.Constants.BUSINESS_UNIT_FILTER_NAME)
public class Warehouse implements BaseModel {

    @EmbeddedId
    private WarehousePK id = new WarehousePK();

    @Column(name = "DESCRI", nullable = true, length = 100)
    @Length(max = 100)
    private String name;

    @Column(name = "ID_RESPONSABLE", updatable = false, insertable = false)
    private Long responsibleId;

    @Column(name = "CUENTA",nullable = false)
    @Length(max = 20)
    private String cashAccount;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ID_RESPONSABLE", updatable = true, insertable = true)
    private Employee responsible;

    @Column(name = "ESTADO", nullable = true, length = 3)
    @Enumerated(EnumType.STRING)
    private WarehouseState state;

    @Column(name = "COD_ALM", nullable = false, insertable = false, updatable = false)
    private String warehouseCode;

    @Version
    @Column(name = "version")
    private long version;

    @com.encens.khipus.validator.BusinessUnit
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "IDUNIDADNEGOCIO", updatable = true, insertable = true)
    private BusinessUnit executorUnit;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "COD_EST")
    private Branch branch;

    public WarehousePK getId() {
        return id;
    }

    public void setId(WarehousePK id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public WarehouseState getState() {
        return state;
    }

    public void setState(WarehouseState state) {
        this.state = state;
    }

    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
    }

    public String getWarehouseCode() {
        return warehouseCode;
    }

    public void setWarehouseCode(String warehouseCode) {
        this.warehouseCode = warehouseCode;
    }

    public Employee getResponsible() {
        return responsible;
    }

    public void setResponsible(Employee responsible) {
        this.responsible = responsible;
    }

    public String getFullName() {
        return getWarehouseCode() + " - " + getName();
    }

    public Long getResponsibleId() {
        return responsibleId;
    }

    public void setResponsibleId(Long responsibleId) {
        this.responsibleId = responsibleId;
    }

    public BusinessUnit getExecutorUnit() {
        return executorUnit;
    }

    public void setExecutorUnit(BusinessUnit executorUnit) {
        this.executorUnit = executorUnit;
    }

    public Branch getBranch() {
        return branch;
    }

    public void setBranch(Branch branch) {
        this.branch = branch;
    }

    public String getCashAccount() {
        return cashAccount;
    }

    public void setCashAccount(String cashAccount) {
        this.cashAccount = cashAccount;
    }
}
