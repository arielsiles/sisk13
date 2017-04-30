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
@Table(name = "cxp_movs", catalog = Constants.FINANCES_SCHEMA)
public class PayableDocumentMovement implements BaseModel {

    @EmbeddedId
    private PayableDocumentMovementPk id = new PayableDocumentMovementPk();

    @Column(name = "NO_TRANS", insertable = false, updatable = false, length = 10)
    private String transactionNumber;

    @Column(name = "NO_CIA", insertable = false, updatable = false, length = 2)
    @Length(max = 2)
    private String companyNumber;

    @Column(name = "COD_PROV")
    private String providerCode;

    @Column(name = "ESTADO", insertable = false, updatable = false)
    @Enumerated(EnumType.STRING)
    private PayableDocumentState state;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumns({
            @JoinColumn(name = "NO_CIA", nullable = false, insertable = false, updatable = false),
            @JoinColumn(name = "COD_PROV", nullable = false, insertable = false, updatable = false)
    })
    private Provider provider;

    @Column(name = "FECHA")
    @Temporal(TemporalType.DATE)
    private Date movementDate;

    @Column(name = "FECHA_CRE")
    @Temporal(TemporalType.DATE)
    private Date createdOnDate;

    //todo this value that the same that PayableDocumentType.movementType of the PayableDocument
    @Column(name = "TIPO_MOV", length = 1, updatable = false)
    @Enumerated(EnumType.STRING)
    private FinanceMovementType movementType;

    @Column(name = "DESCRI")
    private String description;

    @Column(name = "TIPO_COMPRO")
    private String voucherType;

    @Column(name = "NO_COMPRO")
    private String voucherNumber;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumns({
            @JoinColumn(name = "NO_CIA", nullable = false, insertable = false, updatable = false),
            @JoinColumn(name = "TIPO_COMPRO", nullable = false, insertable = false, updatable = false),
            @JoinColumn(name = "NO_COMPRO", nullable = false, insertable = false, updatable = false)
    })
    private AccountingMovement accountingMovement;

    @Column(name = "NO_USR")
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