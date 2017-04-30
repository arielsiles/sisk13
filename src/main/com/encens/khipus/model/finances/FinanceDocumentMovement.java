package com.encens.khipus.model.finances;

import com.encens.khipus.model.BaseModel;
import com.encens.khipus.model.CompanyNumberListener;
import com.encens.khipus.model.UpperCaseStringListener;
import com.encens.khipus.util.Constants;

import javax.persistence.*;
import java.util.Date;

/**
 * @author
 * @version 3.2.9
 */
@NamedQueries({
        @NamedQuery(name = "FinanceDocumentMovement.findMovementByFinancesDocumentState",
                query = "select movement from FinanceDocumentMovement movement" +
                        " where movement.companyNumber=:companyNumber" +
                        " and movement.transactionNumber=:transactionNumber" +
                        " and movement.state=:state")
})

@Entity
@EntityListeners({CompanyNumberListener.class, UpperCaseStringListener.class})
@Table(name = "ck_movs", schema = Constants.FINANCES_SCHEMA)
public class FinanceDocumentMovement implements BaseModel {

    @EmbeddedId
    private FinanceDocumentMovementPk id = new FinanceDocumentMovementPk();

    @Column(name = "NO_CIA", length = 2, updatable = false, insertable = false)
    private String companyNumber;

    @Column(name = "NO_TRANS", length = 10, updatable = false, insertable = false)
    private String transactionNumber;

    @Column(name = "FECHA")
    @Temporal(TemporalType.DATE)
    private Date date;

    @Column(name = "FECHA_CRE")
    @Temporal(TemporalType.DATE)
    private Date createdOnDate;

    @Column(name = "TIPO_MOV", length = 1, updatable = false)
    @Enumerated(EnumType.STRING)
    private FinanceMovementType movementType;

    @Column(name = "DESCRI")
    private String description;

    @Column(name = "TIPO_COMPRO", length = 2)
    private String voucherType;

    @Column(name = "NO_COMPRO", length = 10)
    private String voucherNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns({
            @JoinColumn(name = "TIPO_COMPRO", referencedColumnName = "TIPO_COMPRO", updatable = false, insertable = false),
            @JoinColumn(name = "NO_COMPRO", referencedColumnName = "NO_COMPRO", updatable = false, insertable = false),
            @JoinColumn(name = "NO_CIA", referencedColumnName = "NO_CIA", updatable = false, insertable = false)
    })
    private AccountingMovement accountingMovement;

    @Column(name = "NO_USR")
    private String userNumber;

    @Column(name = "ESTADO", insertable = false, updatable = false)
    @Enumerated(EnumType.STRING)
    private FinanceDocumentState state;

    public FinanceDocumentMovementPk getId() {
        return id;
    }

    public void setId(FinanceDocumentMovementPk id) {
        this.id = id;
    }

    public String getCompanyNumber() {
        return companyNumber;
    }

    public void setCompanyNumber(String companyNumber) {
        this.companyNumber = companyNumber;
    }

    public String getTransactionNumber() {
        return transactionNumber;
    }

    public void setTransactionNumber(String transactionNumber) {
        this.transactionNumber = transactionNumber;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Date getCreatedOnDate() {
        return createdOnDate;
    }

    public void setCreatedOnDate(Date createdOnDate) {
        this.createdOnDate = createdOnDate;
    }

    public FinanceMovementType getMovementType() {
        return movementType;
    }

    public void setMovementType(FinanceMovementType movementType) {
        this.movementType = movementType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getVoucherType() {
        return voucherType;
    }

    public void setVoucherType(String voucherType) {
        this.voucherType = voucherType;
    }

    public String getVoucherNumber() {
        return voucherNumber;
    }

    public void setVoucherNumber(String voucherNumber) {
        this.voucherNumber = voucherNumber;
    }

    public AccountingMovement getAccountingMovement() {
        return accountingMovement;
    }

    public void setAccountingMovement(AccountingMovement accountingMovement) {
        this.accountingMovement = accountingMovement;
        setVoucherType(accountingMovement != null ? accountingMovement.getVoucherType() : null);
        setVoucherNumber(accountingMovement != null ? accountingMovement.getVoucherNumber() : null);
    }

    public String getUserNumber() {
        return userNumber;
    }

    public void setUserNumber(String userNumber) {
        this.userNumber = userNumber;
    }

    public FinanceDocumentState getState() {
        return state;
    }

    public void setState(FinanceDocumentState state) {
        this.state = state;
    }
}