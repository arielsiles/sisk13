package com.encens.khipus.model.finances;

import com.encens.khipus.model.BaseModel;
import com.encens.khipus.model.CompanyListener;
import com.encens.khipus.model.UpperCaseStringListener;
import com.encens.khipus.model.admin.BusinessUnit;
import com.encens.khipus.model.admin.Company;
import com.encens.khipus.model.admin.User;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.Type;
import org.hibernate.validator.NotNull;

import javax.persistence.*;
import java.util.Date;

/**
 * Entity for Cash box record
 *
 * @author:
 */

@Entity
@Filter(name = com.encens.khipus.util.Constants.COMPANY_FILTER_NAME)
@EntityListeners({CompanyListener.class, UpperCaseStringListener.class})
@Table(schema = com.encens.khipus.util.Constants.KHIPUS_SCHEMA, name = "historialcaja")
public class CashBoxRecord implements BaseModel {

    @EmbeddedId
    private CashBoxRecordPk id;

    @ManyToOne
    @JoinColumn(name = "idcaja", referencedColumnName = "idcaja", insertable = false, updatable = false, nullable = false)
    private CashBox cashBox;

    @ManyToOne
    @JoinColumn(name = "idtipocaja", nullable = false)
    private CashBoxType type;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idusuario", referencedColumnName = "idusuario")
    private User user;

    @Column(name = "descripcion", nullable = false, length = 150)
    private String description;

    @Column(name = "fechacreacion", nullable = false, updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date creationDate;

    @Column(name = "autorizacionrequerida", nullable = false)
    @Type(type = com.encens.khipus.model.usertype.IntegerBooleanUserType.NAME)
    private Boolean requiredAuthorization = false;

    @Column(name = "estado", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private CashBoxState state;

    @ManyToOne
    @JoinColumn(name = "idunidadnegocio", referencedColumnName = "idunidadnegocio")
    private BusinessUnit businessUnit;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "idcompania", nullable = false, updatable = false, insertable = true)
    @NotNull
    private Company company;

    public CashBoxRecord() {
    }

    public CashBoxRecord(CashBox cashBox) {
        this.type = cashBox.getType();
        this.user = cashBox.getUser();
        this.description = cashBox.getDescription();
        this.requiredAuthorization = cashBox.getRequiredAuthorization();
        this.state = cashBox.getState();
        this.businessUnit = cashBox.getBusinessUnit();
        this.creationDate = cashBox.getCreationDate();
        this.id = new CashBoxRecordPk(cashBox.getId(), new Date());
    }

    public CashBoxRecordPk getId() {
        return id;
    }

    public void setId(CashBoxRecordPk id) {
        this.id = id;
    }

    public CashBox getCashBox() {
        return cashBox;
    }

    public void setCashBox(CashBox cashBox) {
        this.cashBox = cashBox;
    }

    public CashBoxType getType() {
        return type;
    }

    public void setType(CashBoxType type) {
        this.type = type;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public Boolean getRequiredAuthorization() {
        return requiredAuthorization;
    }

    public void setRequiredAuthorization(Boolean requiredAuthorization) {
        this.requiredAuthorization = requiredAuthorization;
    }

    public CashBoxState getState() {
        return state;
    }

    public void setState(CashBoxState state) {
        this.state = state;
    }

    public BusinessUnit getBusinessUnit() {
        return businessUnit;
    }

    public void setBusinessUnit(BusinessUnit businessUnit) {
        this.businessUnit = businessUnit;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }
}
