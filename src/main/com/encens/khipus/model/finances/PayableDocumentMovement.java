package com.encens.khipus.model.finances;

import com.encens.khipus.model.BaseModel;
import com.encens.khipus.model.CompanyNumberListener;
import com.encens.khipus.model.UpperCaseStringListener;
import com.encens.khipus.util.Constants;
import org.hibernate.validator.Length;

import javax.persistence.*;
import java.util.Date;

/**
 * @author
 * @version 3.2.9
 */


@Entity
@EntityListeners({CompanyNumberListener.class, UpperCaseStringListener.class})
@Table(name = "cxp_movs", schema = Constants.FINANCES_SCHEMA)
public class PayableDocumentMovement implements BaseModel {

    @EmbeddedId
    private PayableDocumentMovementPk id = new PayableDocumentMovementPk();

    @Column(name = "no_trans", insertable = false, updatable = false, length = 10)
    private String transactionNumber;

    @Column(name = "no_cia", insertable = false, updatable = false, length = 2)
    @Length(max = 2)
    private String companyNumber;

    @Column(name = "cod_prov")
    private String providerCode;

    @Column(name = "estado", insertable = false, updatable = false)
    @Enumerated(EnumType.STRING)
    private PayableDocumentState state;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumns({
            @JoinColumn(name = "no_cia", nullable = false, insertable = false, updatable = false),
            @JoinColumn(name = "cod_prov", nullable = false, insertable = false, updatable = false)
    })
    private Provider provider;

    @Column(name = "fecha")
    @Temporal(TemporalType.DATE)
    private Date movementDate;

    @Column(name = "fecha_cre")
    @Temporal(TemporalType.DATE)
    private Date createdOnDate;

    //todo this value that the same that PayableDocumentType.movementType of the PayableDocument
    @Column(name = "tipo_mov", length = 1, updatable = false)
    @Enumerated(EnumType.STRING)
    private FinanceMovementType movementType;

    @Column(name = "descri")
    private String description;

    @Column(name = "tipo_compro")
    private String voucherType;

    @Column(name = "no_compro")
    private String voucherNumber;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumns({
            @JoinColumn(name = "no_cia", nullable = false, insertable = false, updatable = false),
            @JoinColumn(name = "tipo_compro", nullable = false, insertable = false, updatable = false),
            @JoinColumn(name = "no_compro", nullable = false, insertable = false, updatable = false)
    })
    private AccountingMovement accountingMovement;

    @Column(name = "no_usr")
    private String userNumber;

    public PayableDocumentMovementPk getId() {
        return id;
    }

    public void setId(PayableDocumentMovementPk id) {
        this.id = id;
    }

    public String getTransactionNumber() {
        return transactionNumber;
    }

    public void setTransactionNumber(String transactionNumber) {
        this.transactionNumber = transactionNumber;
    }

    public String getCompanyNumber() {
        return companyNumber;
    }

    public void setCompanyNumber(String companyNumber) {
        this.companyNumber = companyNumber;
    }

    public String getProviderCode() {
        return providerCode;
    }

    public void setProviderCode(String providerCode) {
        this.providerCode = providerCode;
    }

    public PayableDocumentState getState() {
        return state;
    }

    public void setState(PayableDocumentState state) {
        this.state = state;
    }

    public Provider getProvider() {
        return provider;
    }

    public void setProvider(Provider provider) {
        this.provider = provider;
        setProviderCode(provider != null ? provider.getProviderCode() : null);
    }

    public Date getMovementDate() {
        return movementDate;
    }

    public void setMovementDate(Date movementDate) {
        this.movementDate = movementDate;
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

    @Override
    public String toString() {
        return "PayableDocumentMovement{" +
                ", transactionNumber='" + id.getTransactionNumber() + '\'' +
                ", companyNumber='" + id.getCompanyNumber() + '\'' +
                ", state=" + id.getState() +
                ", providerCode='" + providerCode + '\'' +
                ", movementDate=" + movementDate +
                ", createdOnDate=" + createdOnDate +
                ", movementType='" + movementType + '\'' +
                ", description='" + description + '\'' +
                ", voucherType='" + voucherType + '\'' +
                ", voucherNumber='" + voucherNumber + '\'' +
                ", userNumber='" + userNumber + '\'' +
                '}';
    }
}