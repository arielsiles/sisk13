package com.encens.khipus.model.fixedassets;

import com.encens.khipus.model.BaseModel;
import com.encens.khipus.model.CompanyNumberListener;
import com.encens.khipus.model.UpperCaseStringListener;
import com.encens.khipus.model.admin.BusinessUnit;
import com.encens.khipus.model.finances.CashAccount;
import com.encens.khipus.model.finances.FinancesBankAccount;
import com.encens.khipus.model.finances.FinancesCurrencyType;
import com.encens.khipus.model.purchases.PurchaseOrderPaymentType;
import com.encens.khipus.model.warehouse.FixedAssetBeneficiaryType;
import com.encens.khipus.util.Constants;
import org.hibernate.validator.Length;
import org.hibernate.validator.NotNull;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;

/**
 * @author
 * @version 3.5.2.2
 */

@TableGenerator(schema = com.encens.khipus.util.Constants.KHIPUS_SCHEMA, name = "FixedAssetPayment.tableGenerator",
        table = com.encens.khipus.util.Constants.SEQUENCE_TABLE_NAME,
        pkColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_PK_COLUMN_NAME,
        valueColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_VALUE_COLUMN_NAME,
        pkColumnValue = "af_pago",
        allocationSize = com.encens.khipus.util.Constants.SEQUENCE_ALLOCATION_SIZE)

@Entity
@Table(name = "af_pago", schema = Constants.FINANCES_SCHEMA)
@EntityListeners({CompanyNumberListener.class, UpperCaseStringListener.class})
public class FixedAssetPayment implements BaseModel {

    @Id
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "FixedAssetPayment.tableGenerator")
    @Column(name = "idpago", nullable = false)
    private Long id;

    @Column(name = "estado", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private FixedAssetPaymentState state;

    @Column(name = "descripcion", nullable = false, length = 250)
    @Length(max = 250)
    @NotNull
    private String description;

    @Column(name = "monedaorigen", length = 20)
    @Enumerated(EnumType.STRING)
    private FinancesCurrencyType sourceCurrency;

    @Column(name = "monedapago", nullable = false)
    @Enumerated(EnumType.STRING)
    private FinancesCurrencyType payCurrency;

    @Column(name = "montoorigen", precision = 12, scale = 2)
    private BigDecimal sourceAmount;

    @Column(name = "montopago", precision = 12, scale = 2, nullable = false)
    @NotNull
    private BigDecimal payAmount;

    @Column(name = "nombrebeneficiario", length = 200)
    @Length(max = 200)
    private String beneficiaryName;

    @Column(name = "tipobeneficiario")
    @Enumerated(EnumType.STRING)
    private FixedAssetBeneficiaryType fixedAssetBeneficiaryType;

    @Column(name = "tipocambio", precision = 16, scale = 6)
    private BigDecimal exchangeRate;

    @Column(name = "tipopago")
    @Enumerated(EnumType.STRING)
    private PurchaseOrderPaymentType paymentType;

    @Column(name = "fechacreacion")
    @Temporal(TemporalType.DATE)
    private Date creationDate;

    @Column(name = "cuentabanco", length = 20)
    @Length(max = 20)
    private String bankAccountNumber;

    @Column(name = "notrans", length = 10)
    @Length(max = 10)
    private String transactionNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idsededestinocheque", referencedColumnName = "idunidadnegocio")
    private BusinessUnit checkDestination;

    @Column(name = "no_cia", nullable = false, length = 2)
    @Length(max = 2)
    private String companyNumber;

    @Version
    @Column(name = "version")
    private Long version;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns({
            @JoinColumn(name = "no_cia", referencedColumnName = "no_cia", nullable = true, updatable = false, insertable = false),
            @JoinColumn(name = "cuentabanco", referencedColumnName = "cta_bco", nullable = true, updatable = false, insertable = false)
    })
    private FinancesBankAccount bankAccount;

    /* the cash box account*/
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns({
            @JoinColumn(name = "no_cia", referencedColumnName = "no_cia", updatable = false, insertable = false),
            @JoinColumn(name = "cuentacaja", referencedColumnName = "cuenta", updatable = false, insertable = false)
    })
    private CashAccount cashBoxCashAccount;

    /* the cash box account code*/
    @Column(name = "cuentacaja", length = 20)
    @Length(max = 20)
    private String cashBoxAccount;


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public FinancesCurrencyType getSourceCurrency() {
        return sourceCurrency;
    }

    public void setSourceCurrency(FinancesCurrencyType sourceCurrency) {
        this.sourceCurrency = sourceCurrency;
    }

    public FinancesCurrencyType getPayCurrency() {
        return payCurrency;
    }

    public void setPayCurrency(FinancesCurrencyType payCurrency) {
        this.payCurrency = payCurrency;
    }

    public BigDecimal getSourceAmount() {
        return sourceAmount;
    }

    public void setSourceAmount(BigDecimal sourceAmount) {
        this.sourceAmount = sourceAmount;
    }

    public BigDecimal getPayAmount() {
        return payAmount;
    }

    public void setPayAmount(BigDecimal payAmount) {
        this.payAmount = payAmount;
    }

    public String getCompanyNumber() {
        return companyNumber;
    }

    public void setCompanyNumber(String companyNumber) {
        this.companyNumber = companyNumber;
    }

    public String getBeneficiaryName() {
        return beneficiaryName;
    }

    public void setBeneficiaryName(String beneficiaryName) {
        this.beneficiaryName = beneficiaryName;
    }

    public FixedAssetPaymentState getState() {
        return state;
    }

    public void setState(FixedAssetPaymentState state) {
        this.state = state;
    }

    public FixedAssetBeneficiaryType getFixedAssetBeneficiaryType() {
        return fixedAssetBeneficiaryType;
    }

    public void setFixedAssetBeneficiaryType(FixedAssetBeneficiaryType fixedAssetBeneficiaryType) {
        this.fixedAssetBeneficiaryType = fixedAssetBeneficiaryType;
    }

    public BigDecimal getExchangeRate() {
        return exchangeRate;
    }

    public void setExchangeRate(BigDecimal exchangeRate) {
        this.exchangeRate = exchangeRate;
    }

    public PurchaseOrderPaymentType getPaymentType() {
        return paymentType;
    }

    public void setPaymentType(PurchaseOrderPaymentType paymentType) {
        this.paymentType = paymentType;
    }

    public String getBankAccountNumber() {
        return bankAccountNumber;
    }

    public void setBankAccountNumber(String bankAccountNumber) {
        this.bankAccountNumber = bankAccountNumber;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    public FinancesBankAccount getBankAccount() {
        return bankAccount;
    }

    public void setBankAccount(FinancesBankAccount bankAccount) {
        this.bankAccount = bankAccount;
        setBankAccountNumber(this.bankAccount != null ? this.bankAccount.getId().getAccountNumber() : null);
        setSourceCurrency(this.bankAccount != null ? this.bankAccount.getCurrency() : null);
    }

    public CashAccount getCashBoxCashAccount() {
        return cashBoxCashAccount;
    }

    public void setCashBoxCashAccount(CashAccount cashBoxCashAccount) {
        this.cashBoxCashAccount = cashBoxCashAccount;
        setCashBoxAccount(this.cashBoxCashAccount != null ? this.cashBoxCashAccount.getAccountCode() : null);
        setSourceCurrency(this.cashBoxCashAccount != null ? this.cashBoxCashAccount.getCurrency() : null);
    }

    public String getCashBoxAccount() {
        return cashBoxAccount;
    }

    public void setCashBoxAccount(String cashBoxAccount) {
        this.cashBoxAccount = cashBoxAccount;
    }

    public String getTransactionNumber() {
        return transactionNumber;
    }

    public void setTransactionNumber(String transactionNumber) {
        this.transactionNumber = transactionNumber;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public BusinessUnit getCheckDestination() {
        return checkDestination;
    }

    public void setCheckDestination(BusinessUnit checkDestination) {
        this.checkDestination = checkDestination;
    }
}