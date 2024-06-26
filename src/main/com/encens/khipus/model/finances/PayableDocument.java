package com.encens.khipus.model.finances;

import com.encens.khipus.model.BaseModel;
import com.encens.khipus.model.CompanyNumberListener;
import com.encens.khipus.model.UpperCaseStringListener;
import com.encens.khipus.util.Constants;
import org.hibernate.annotations.Parameter;
import org.hibernate.annotations.Type;
import org.hibernate.validator.Length;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;

/**
 * @author
 * @version 3.2.9
 */

@NamedQueries({
        @NamedQuery(name = "PayableDocument.findByAccountingMovement",
                query = "select payableDocument from PayableDocument payableDocument" +
                        " where payableDocument.accountingMovement=:accountingMovement" +
                        " order by payableDocument.voucherType,payableDocument.voucherNumber"),
        @NamedQuery(name = "PayableDocument.findByTransactionNumber",
                query = "select payableDocument from PayableDocument payableDocument" +
                        " where payableDocument.id.transactionNumber=:transactionNumber" +
                        " order by payableDocument.voucherType,payableDocument.voucherNumber")
})


@Entity
@EntityListeners({CompanyNumberListener.class, UpperCaseStringListener.class})
@Table(name = "cxp_docus", schema = Constants.FINANCES_SCHEMA)
public class PayableDocument implements BaseModel {

    @EmbeddedId
    private PayableDocumentPk id = new PayableDocumentPk();

    @Column(name = "no_trans", insertable = false, updatable = false)
    private String transactionNumber;

    @Column(name = "no_cia", insertable = false, updatable = false)
    private String companyNumber;

    @Column(name = "cod_enti")
    private String entityCode;

    @Column(name = "cod_prov")
    private String providerCode;

    @Column(name = "tipo_doc")
    private String documentTypeCode;

    @Column(name = "no_doc", length = 20)
    @Length(max = 20)
    private String documentNumber;

    @Column(name = "ctaxpagar", length = 31)
    @Length(max = 31)
    private String payableAccountCode;

    @Column(name = "fecha")
    @Temporal(TemporalType.DATE)
    private Date movementDate;

    @Column(name = "monto", precision = 16, scale = 2)
    private BigDecimal amount;

    @Column(name = "saldo", precision = 16, scale = 2)
    private BigDecimal residue;

    @Column(name = "moneda", length = 20)
    @Length(max = 20)
    private String currencyCode;

    @Column(name = "tc", precision = 10, scale = 6)
    private BigDecimal exchangeRate;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cod_enti", nullable = false, updatable = false, insertable = false)
    private FinancesEntity entity;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumns({
            @JoinColumn(name = "no_cia", nullable = false, insertable = false, updatable = false),
            @JoinColumn(name = "cod_prov", nullable = false, insertable = false, updatable = false)
    })
    private Provider provider;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumns({
            @JoinColumn(name = "no_cia", nullable = false, insertable = false, updatable = false),
            @JoinColumn(name = "tipo_doc", nullable = false, insertable = false, updatable = false)
    })
    private PayableDocumentType documentType;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumns({
            @JoinColumn(name = "no_cia", nullable = false, insertable = false, updatable = false),
            @JoinColumn(name = "moneda", nullable = false, insertable = false, updatable = false)
    })
    private FinancesCurrency currency;

    @Column(name = "estado")
    @Enumerated(EnumType.STRING)
    private PayableDocumentState state;

    @ManyToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumns({
            @JoinColumn(name = "no_cia", referencedColumnName = "no_cia", nullable = false, updatable = false, insertable = false),
            @JoinColumn(name = "ctaxpagar", referencedColumnName = "cuenta", nullable = false, insertable = false, updatable = false)
    })
    private CashAccount payableAccount;

    @Column(name = "tipo_compro", length = 2)
    @Length(max = 2)
    private String voucherType;

    @Column(name = "no_compro", length = 10)
    @Length(max = 10)
    private String voucherNumber;

    @Column(name = "fecha_ven")
    @Temporal(TemporalType.DATE)
    private Date expirationDate;

    @Column(name = "mod_aut", updatable = false)
    @Type(type = com.encens.khipus.model.usertype.StringBooleanUserType.NAME, parameters = {
            @Parameter(
                    name = com.encens.khipus.model.usertype.StringBooleanUserType.TRUE_PARAMETER,
                    value = com.encens.khipus.model.usertype.StringBooleanUserType.TRUE_VALUE
            ),
            @Parameter(
                    name = com.encens.khipus.model.usertype.StringBooleanUserType.FALSE_PARAMETER,
                    value = com.encens.khipus.model.usertype.StringBooleanUserType.FALSE_VALUE
            )
    })
    private Boolean allowUpdate;

    @Column(name = "fecha_emis")
    @Temporal(TemporalType.DATE)
    private Date registrationDate;


    @Column(name = "pendiente_registro", updatable = false)
    @Type(type = com.encens.khipus.model.usertype.StringBooleanUserType.NAME, parameters = {
            @Parameter(
                    name = com.encens.khipus.model.usertype.StringBooleanUserType.TRUE_PARAMETER,
                    value = com.encens.khipus.model.usertype.StringBooleanUserType.TRUE_VALUE
            ),
            @Parameter(
                    name = com.encens.khipus.model.usertype.StringBooleanUserType.FALSE_PARAMETER,
                    value = com.encens.khipus.model.usertype.StringBooleanUserType.FALSE_VALUE
            )
    })
    private Boolean registrationPending;

    @Column(name = "no_factura", length = 20)
    @Length(max = 20)
    private String invoiceNumber;

    @Column(name = "tipo_compro_regfac", length = 2)
    @Length(max = 2)
    private String regularizeVoucherType;

    @Column(name = "no_compro_regfac", length = 10)
    @Length(max = 10)
    private String regularizeVoucherNumber;

    @Column(name = "beneficiario", length = 100)
    @Length(max = 100)
    private String beneficiary;

    @Column(name = "saldo_regfac", precision = 16, scale = 2)
    private BigDecimal regularizeVoucherResidue;

    @ManyToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumns({
            @JoinColumn(name = "no_cia", referencedColumnName = "no_cia", nullable = false, updatable = false, insertable = false),
            @JoinColumn(name = "tipo_compro", referencedColumnName = "tipo_compro", nullable = false, insertable = false, updatable = false),
            @JoinColumn(name = "no_compro", referencedColumnName = "no_compro", nullable = false, insertable = false, updatable = false)
    })
    private AccountingMovement accountingMovement;

    public PayableDocumentPk getId() {
        return id;
    }

    public void setId(PayableDocumentPk id) {
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

    public String getEntityCode() {
        return entityCode;
    }

    public void setEntityCode(String entityCode) {
        this.entityCode = entityCode;
    }

    public String getProviderCode() {
        return providerCode;
    }

    public void setProviderCode(String providerCode) {
        this.providerCode = providerCode;
    }

    public String getDocumentTypeCode() {
        return documentTypeCode;
    }

    public void setDocumentTypeCode(String documentTypeCode) {
        this.documentTypeCode = documentTypeCode;
    }

    public String getDocumentNumber() {
        return documentNumber;
    }

    public void setDocumentNumber(String documentNumber) {
        this.documentNumber = documentNumber;
    }

    public String getPayableAccountCode() {
        return payableAccountCode;
    }

    public void setPayableAccountCode(String payableAccountCode) {
        this.payableAccountCode = payableAccountCode;
    }

    public Date getMovementDate() {
        return movementDate;
    }

    public void setMovementDate(Date movementDate) {
        this.movementDate = movementDate;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public BigDecimal getResidue() {
        return residue;
    }

    public void setResidue(BigDecimal residue) {
        this.residue = residue;
    }

    public String getCurrencyCode() {
        return currencyCode;
    }

    public void setCurrencyCode(String currencyCode) {
        this.currencyCode = currencyCode;
    }

    public BigDecimal getExchangeRate() {
        return exchangeRate;
    }

    public void setExchangeRate(BigDecimal exchangeRate) {
        this.exchangeRate = exchangeRate;
    }

    public FinancesEntity getEntity() {
        return entity;
    }

    public void setEntity(FinancesEntity entity) {
        this.entity = entity;
        //setEntityCode(entity != null ? entity.getCode() : null);
        setEntityCode(entity != null ? entity.getId().toString() : null);
    }

    public Provider getProvider() {
        return provider;
    }

    public void setProvider(Provider provider) {
        this.provider = provider;
        setProviderCode(provider != null ? provider.getProviderCode() : null);
    }

    public PayableDocumentType getDocumentType() {
        return documentType;
    }

    public void setDocumentType(PayableDocumentType documentType) {
        this.documentType = documentType;
        setDocumentTypeCode(documentType != null ? documentType.getDocumentType() : null);
    }

    public FinancesCurrency getCurrency() {
        return currency;
    }

    public void setCurrency(FinancesCurrency currency) {
        this.currency = currency;
        setCurrencyCode(currency != null ? currency.getAcronym() : null);
    }

    public PayableDocumentState getState() {
        return state;
    }

    public void setState(PayableDocumentState state) {
        this.state = state;
    }

    public CashAccount getPayableAccount() {
        return payableAccount;
    }

    public void setPayableAccount(CashAccount payableAccount) {
        this.payableAccount = payableAccount;
        setPayableAccountCode(payableAccount != null ? payableAccount.getAccountCode() : null);
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

    public Date getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(Date expirationDate) {
        this.expirationDate = expirationDate;
    }

    public Boolean getAllowUpdate() {
        return allowUpdate;
    }

    public void setAllowUpdate(Boolean allowUpdate) {
        this.allowUpdate = allowUpdate;
    }

    public Date getRegistrationDate() {
        return registrationDate;
    }

    public void setRegistrationDate(Date registrationDate) {
        this.registrationDate = registrationDate;
    }

    public Boolean getRegistrationPending() {
        return registrationPending;
    }

    public void setRegistrationPending(Boolean registrationPending) {
        this.registrationPending = registrationPending;
    }

    public String getInvoiceNumber() {
        return invoiceNumber;
    }

    public void setInvoiceNumber(String invoiceNumber) {
        this.invoiceNumber = invoiceNumber;
    }

    public String getRegularizeVoucherType() {
        return regularizeVoucherType;
    }

    public void setRegularizeVoucherType(String regularizeVoucherType) {
        this.regularizeVoucherType = regularizeVoucherType;
    }

    public String getRegularizeVoucherNumber() {
        return regularizeVoucherNumber;
    }

    public void setRegularizeVoucherNumber(String regularizeVoucherNumber) {
        this.regularizeVoucherNumber = regularizeVoucherNumber;
    }

    public String getBeneficiary() {
        return beneficiary;
    }

    public void setBeneficiary(String beneficiary) {
        this.beneficiary = beneficiary;
    }

    public BigDecimal getRegularizeVoucherResidue() {
        return regularizeVoucherResidue;
    }

    public void setRegularizeVoucherResidue(BigDecimal regularizeVoucherResidue) {
        this.regularizeVoucherResidue = regularizeVoucherResidue;
    }

    public AccountingMovement getAccountingMovement() {
        return accountingMovement;
    }

    public void setAccountingMovement(AccountingMovement accountingMovement) {
        this.accountingMovement = accountingMovement;
        setVoucherNumber(accountingMovement != null ? accountingMovement.getVoucherNumber() : null);
        setVoucherType(accountingMovement != null ? accountingMovement.getVoucherType() : null);
    }

    public String getFullName() {
        return getDocumentTypeCode() + Constants.HYPHEN_SEPARATOR + getDocumentNumber();
    }

    public String getFullVoucherNumber() {
        return getVoucherType() != null && getVoucherNumber() != null ?
                getVoucherType() + Constants.HYPHEN_SEPARATOR + getVoucherNumber() :
                Constants.BLANK_SEPARATOR;
    }

    @Override
    public String toString() {
        return "PayableDocument{" +
                ", transactionNumber='" + id.getTransactionNumber() + '\'' +
                ", companyNumber='" + id.getCompanyNumber() + '\'' +
                ", entityCode='" + entityCode + '\'' +
                ", providerCode='" + providerCode + '\'' +
                ", documentTypeCode='" + documentTypeCode + '\'' +
                ", documentNumber='" + documentNumber + '\'' +
                ", payableAccountCode='" + payableAccountCode + '\'' +
                ", movementDate=" + movementDate +
                ", amount=" + amount +
                ", residue=" + residue +
                ", currencyCode='" + currencyCode + '\'' +
                ", exchangeRate=" + exchangeRate +
                ", state=" + state +
                ", voucherType='" + voucherType + '\'' +
                ", voucherNumber='" + voucherNumber + '\'' +
                ", expirationDate=" + expirationDate +
                ", allowUpdate=" + allowUpdate +
                ", registrationDate=" + registrationDate +
                ", registrationPending=" + registrationPending +
                ", invoiceNumber='" + invoiceNumber + '\'' +
                ", regularizeVoucherType='" + regularizeVoucherType + '\'' +
                ", regularizeVoucherNumber='" + regularizeVoucherNumber + '\'' +
                ", beneficiary='" + beneficiary + '\'' +
                ", regularizeVoucherResidue=" + regularizeVoucherResidue +
                '}';
    }
}
