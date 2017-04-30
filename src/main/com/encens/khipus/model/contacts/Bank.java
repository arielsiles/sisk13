package com.encens.khipus.model.contacts;

import com.encens.khipus.model.CompanyListener;
import com.encens.khipus.model.UpperCaseStringListener;
import com.encens.khipus.model.admin.Company;

import javax.persistence.*;
import java.util.Date;

/**
 * Represents the Bank entity model
 *
 * @author
 * @version 1.2.3
 */

@javax.persistence.Entity
@EntityListeners({CompanyListener.class, UpperCaseStringListener.class})
@Table(schema = com.encens.khipus.util.Constants.KHIPUS_SCHEMA, name = "banco", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"idcompania", "numerocontrato"}),
        @UniqueConstraint(columnNames = {"idcompania", "numeroadenda"})
})
@PrimaryKeyJoinColumns(value = {
        @PrimaryKeyJoinColumn(name = "idbanco", referencedColumnName = "idinstitucion")/*,
    @PrimaryKeyJoinColumn(name = "NO_IDENTIFICACION", referencedColumnName = "NO_IDENTIFICACION")*/
})


public class Bank extends Organization {

    @Column(name = "fechaactivacion", nullable = false, updatable = false)
    @Temporal(TemporalType.DATE)
    private Date activationDate = new Date();

    @Column(name = "numerocontrato", length = 50, nullable = false)
    private String contractNumber;

    @Column(name = "fechafincontrato")
    @Temporal(TemporalType.DATE)
    private Date contractEndDate;

    @Column(name = "numeroadenda", length = 30, nullable = false)
    private String bankNumber;

    @Column(name = "estado", length = 15, nullable = false)
    @Enumerated(EnumType.STRING)
    private BankState state;

    @Column(name = "fechabaja")
    @Temporal(TemporalType.DATE)
    private Date cancelDate;

    @Column(name = "fechaestado")
    @Temporal(TemporalType.DATE)
    private Date stateDate;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "idcompania", nullable = false, updatable = false, insertable = true)
    private Company company;


    public Company getCompany() {
        return company;
    }


    public void setCompany(Company company) {
        this.company = company;
    }


    public Date getActivationDate() {
        return activationDate;
    }

    public void setActivationDate(Date activationDate) {
        this.activationDate = activationDate;
    }

    public String getContractNumber() {
        return contractNumber;
    }

    public void setContractNumber(String contractNumber) {
        this.contractNumber = contractNumber;
    }

    public Date getContractEndDate() {
        return contractEndDate;
    }

    public void setContractEndDate(Date contractEndDate) {
        this.contractEndDate = contractEndDate;
    }

    public String getBankNumber() {
        return bankNumber;
    }

    public void setBankNumber(String bankNumber) {
        this.bankNumber = bankNumber;
    }

    public BankState getState() {
        return state;
    }

    public void setState(BankState state) {
        this.state = state;
    }

    public Date getCancelDate() {
        return cancelDate;
    }

    public void setCancelDate(Date cancelDate) {
        this.cancelDate = cancelDate;
    }

    public Date getStateDate() {
        return stateDate;
    }

    public void setStateDate(Date stateDate) {
        this.stateDate = stateDate;
    }
}
