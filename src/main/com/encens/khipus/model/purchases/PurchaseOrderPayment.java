package com.encens.khipus.model.purchases;

import com.encens.khipus.model.BaseModel;
import com.encens.khipus.model.CompanyNumberListener;
import com.encens.khipus.model.UpperCaseStringListener;
import com.encens.khipus.model.admin.BusinessUnit;
import com.encens.khipus.model.admin.User;
import com.encens.khipus.model.finances.*;
import com.encens.khipus.model.warehouse.BeneficiaryType;
import com.encens.khipus.util.BigDecimalUtil;
import com.encens.khipus.util.Constants;
import org.hibernate.validator.Length;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;

/**
 * @author
 * @version 3.5.2.2
 */

@TableGenerator(schema = com.encens.khipus.util.Constants.KHIPUS_SCHEMA, name = "PurchaseOrderPayment.tableGenerator",
        table = com.encens.khipus.util.Constants.SEQUENCE_TABLE_NAME,
        pkColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_PK_COLUMN_NAME,
        valueColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_VALUE_COLUMN_NAME,
        pkColumnValue = "pagoordencompra",
        allocationSize = com.encens.khipus.util.Constants.SEQUENCE_ALLOCATION_SIZE)

@NamedQueries({
        @NamedQuery(name = "PurchaseOrderPayment.findByState",
                query = "select o from PurchaseOrderPayment o where o.purchaseOrder =:purchaseOrder and o.state in (:states)"),
        @NamedQuery(name = "PurchaseOrderPayment.findByStateAndKind",
                query = "select o from PurchaseOrderPayment o where o.purchaseOrder =:purchaseOrder and o.state in (:states)" +
                        " and o.purchaseOrderPaymentKind=:purchaseOrderPaymentKind"),
        @NamedQuery(name = "PurchaseOrderPayment.findByStateAndKindPurchaseOrder",
                query = "select o from PurchaseOrderPayment o where o.purchaseOrder =:purchaseOrder and o.state in (:states)" +
                        " and o.purchaseOrderPaymentKind=:purchaseOrderPaymentKind"),
        @NamedQuery(name = "PurchaseOrderPayment.findByPurchaseOrder",
                query = "select o from PurchaseOrderPayment o where o.purchaseOrder=:purchaseOrder"),
        @NamedQuery(name = "PurchaseOrderPayment.countByStateAndPurchaseOrder",
                query = "select count(o) from PurchaseOrderPayment o where o.purchaseOrder=:purchaseOrder and o.state=:state"),
        @NamedQuery(name = "PurchaseOrderPayment.findByStatesAndPaymentType",
                query = "select payment from PurchaseOrderPayment payment " +
                        " left join fetch payment.purchaseOrder purchaseOrder " +
                        "where purchaseOrder.state in (:purchaseOrderStateList) " +
                        " and payment.state in (:paymentStateList) " +
                        " and payment.paymentType in (:paymentTypeList)" +
                        " order by payment.purchaseOrderId")


})


@Entity
@Table(name = "pagoordencompra", schema = Constants.FINANCES_SCHEMA)
@EntityListeners({CompanyNumberListener.class, UpperCaseStringListener.class})
public class PurchaseOrderPayment implements BaseModel {

    @Id
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "PurchaseOrderPayment.tableGenerator")
    @Column(name = "IDPAGOORDENCOMPRA", nullable = false)
    private Long id;

    @Column(name = "CUENTABANCO", length = 20)
    @Length(max = 20)
    private String bankAccountNumber;

    @Column(name = "ESTADO", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private PurchaseOrderPaymentState state;

    @Column(name = "DESCRIPCION", nullable = false, length = 1000)
    @Length(max = 1000)
    private String description;

    @Column(name = "IDORDENCOMPRA", nullable = false, insertable = false, updatable = false)
    private Long purchaseOrderId;

    @Column(name = "MONEDAORIGEN", length = 20)
    @Enumerated(EnumType.STRING)
    private FinancesCurrencyType sourceCurrency;

    @Column(name = "MONEDAPAGO", nullable = false)
    @Enumerated(EnumType.STRING)
    private FinancesCurrencyType payCurrency;

    @Column(name = "MONTOORIGEN", precision = 12, scale = 2)
    private BigDecimal sourceAmount;

    @Column(name = "MONTOPAGO", precision = 12, scale = 2, nullable = false)
    private BigDecimal payAmount;

    @Column(name = "NO_CIA", nullable = false)
    @Length(max = 2)
    private String companyNumber;

    @Column(name = "NOMBREBENEFICIARIO", length = 200)
    @Length(max = 200)
    private String beneficiaryName;

    @Column(name = "TIPOBENEFICIARIO")
    @Enumerated(EnumType.STRING)
    private BeneficiaryType beneficiaryType;

    @Column(name = "TIPOCAMBIO", precision = 12, scale = 2)
    private BigDecimal exchangeRate;

    @Column(name = "TIPOPAGO")
    @Enumerated(EnumType.STRING)
    private PurchaseOrderPaymentType paymentType;

    @Column(name = "CLASEPAGO")
    @Enumerated(EnumType.STRING)
    private PurchaseOrderPaymentKind purchaseOrderPaymentKind;

    @Column(name = "FECHACREACION")
    @Temporal(TemporalType.DATE)
    private Date creationDate;

    @Column(name = "FECHAAPROBACION")
    @Temporal(TemporalType.DATE)
    private Date approvalDate;

    @ManyToOne
    @JoinColumn(name = "CREADOPOR")
    private User registerEmployee;

    @ManyToOne
    @JoinColumn(name = "APROBADOPOR")
    private User approvedByEmployee;

    @Column(name = "NOTRANS")
    @Length(max = 10)
    private String transactionNumber;

    @Version
    @Column(name = "VERSION")
    private Long version;

    @Column(name = "id_tmpenc", nullable = true, updatable = false, insertable = false)
    private Long idtmpenc;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumns({
            @JoinColumn(name = "NO_CIA", referencedColumnName = "NO_CIA", updatable = false, insertable = false),
            @JoinColumn(name = "CUENTABANCO", referencedColumnName = "CTA_BCO", updatable = false, insertable = false)
    })
    private FinancesBankAccount bankAccount;

    /* the cash box account*/
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns({
            @JoinColumn(name = "NO_CIA", referencedColumnName = "NO_CIA", updatable = false, insertable = false),
            @JoinColumn(name = "CUENTACAJA", referencedColumnName = "CUENTA", updatable = false, insertable = false)
    })
    private CashAccount cashBoxCashAccount;

    /* the cash box account code*/
    @Column(name = "CUENTACAJA", length = 20)
    @Length(max = 20)
    private String cashBoxAccount;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumns({
            @JoinColumn(name = "IDORDENCOMPRA", referencedColumnName = "ID_COM_ENCOC", nullable = false, updatable = false, insertable = true)
    })
    private PurchaseOrder purchaseOrder;

    @OneToOne(mappedBy = "purchaseOrderPayment", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private RotatoryFundCollection rotatoryFundCollection;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "IDFONDOROTATORIO", referencedColumnName = "IDFONDOROTATORIO")
    private RotatoryFund rotatoryFund;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "IDSEDEDESTINOCHEQUE", referencedColumnName = "idunidadnegocio")
    private BusinessUnit checkDestination;

    @ManyToOne
    @JoinColumn(name = "id_tmpenc", nullable = true)
    private Voucher voucher;

    @Transient
    private Date accountingEntryDefaultDate;
    @Transient
    private String accountingEntryDefaultUserNumber;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getBankAccountNumber() {
        return bankAccountNumber;
    }

    public void setBankAccountNumber(String bankAccountNumber) {
        this.bankAccountNumber = bankAccountNumber;
    }

    public PurchaseOrderPaymentState getState() {
        return state;
    }

    public void setState(PurchaseOrderPaymentState state) {
        this.state = state;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getPurchaseOrderId() {
        return purchaseOrderId;
    }

    public void setPurchaseOrderId(Long purchaseOrderId) {
        this.purchaseOrderId = purchaseOrderId;
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

    public BeneficiaryType getBeneficiaryType() {
        return beneficiaryType;
    }

    public void setBeneficiaryType(BeneficiaryType beneficiaryType) {
        this.beneficiaryType = beneficiaryType;
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

    public PurchaseOrderPaymentKind getPurchaseOrderPaymentKind() {
        return purchaseOrderPaymentKind;
    }

    public void setPurchaseOrderPaymentKind(PurchaseOrderPaymentKind purchaseOrderPaymentKind) {
        this.purchaseOrderPaymentKind = purchaseOrderPaymentKind;
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

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public PurchaseOrder getPurchaseOrder() {
        return purchaseOrder;
    }

    public void setPurchaseOrder(PurchaseOrder purchaseOrder) {
        this.purchaseOrder = purchaseOrder;
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

    public RotatoryFundCollection getRotatoryFundCollection() {
        return rotatoryFundCollection;
    }

    public void setRotatoryFundCollection(RotatoryFundCollection rotatoryFundCollection) {
        this.rotatoryFundCollection = rotatoryFundCollection;
    }

    public RotatoryFund getRotatoryFund() {
        return rotatoryFund;
    }

    public void setRotatoryFund(RotatoryFund rotatoryFund) {
        this.rotatoryFund = rotatoryFund;
        setSourceCurrency(this.rotatoryFund != null ? this.rotatoryFund.getPayCurrency() : null);
    }

    public boolean useExchangeCurrency() {
        return null != this.sourceCurrency
                && null != this.payCurrency
                && (FinancesCurrencyType.D.equals(this.sourceCurrency)
                || FinancesCurrencyType.D.equals(this.payCurrency));
    }


    public BigDecimal changePayAmountToExchangeCurrency() {
        if (!useExchangeCurrency()) {
            throw new RuntimeException("Cannot apply exchangeRate because the entity dont use exchangeCurrency");
        }

        if (null == exchangeRate) {
            throw new RuntimeException("Cannot apply exchangeRate because it is null");
        }

        if (FinancesCurrencyType.D.equals(payCurrency)) {
            return payAmount;
        }

        return BigDecimalUtil.divide(payAmount, exchangeRate);
    }

    public BigDecimal changePayAmountToLocalCurrency() {
        if (!useExchangeCurrency()) {
            throw new RuntimeException("Cannot apply exchangeRate because the entity dont use exchangeCurrency");
        }

        if (null == exchangeRate) {
            throw new RuntimeException("Cannot apply exchangeRate because it is null");
        }

        if (FinancesCurrencyType.P.equals(payCurrency)) {
            return payAmount;
        }

        return BigDecimalUtil.multiply(payAmount, exchangeRate);
    }

    public boolean isApproved() {
        return null != getState() && PurchaseOrderPaymentState.APPROVED.equals(getState());
    }

    public boolean isNullified() {
        return null != getState() && PurchaseOrderPaymentState.NULLIFIED.equals(getState());
    }

    public boolean isCashboxPaymentType() {
        return null != getPaymentType() && PurchaseOrderPaymentType.PAYMENT_CASHBOX.equals(getPaymentType());
    }

    public boolean isBankAccountPaymentType() {
        return null != getPaymentType() && PurchaseOrderPaymentType.PAYMENT_BANK_ACCOUNT.equals(getPaymentType());
    }

    public boolean isCheckPaymentType() {
        return null != getPaymentType() && PurchaseOrderPaymentType.PAYMENT_WITH_CHECK.equals(getPaymentType());
    }

    public boolean isLiquidationPayment() {
        return null != getPurchaseOrderPaymentKind()
                && PurchaseOrderPaymentKind.LIQUIDATION_PAYMENT.equals(getPurchaseOrderPaymentKind());
    }

    public boolean isAdvancePayment() {
        return null != getPurchaseOrderPaymentKind()
                && PurchaseOrderPaymentKind.ADVANCE_PAYMENT.equals(getPurchaseOrderPaymentKind());
    }

    public Date getApprovalDate() {
        return approvalDate;
    }

    public void setApprovalDate(Date approvalDate) {
        this.approvalDate = approvalDate;
    }

    public User getRegisterEmployee() {
        return registerEmployee;
    }

    public void setRegisterEmployee(User registerEmployee) {
        this.registerEmployee = registerEmployee;
    }

    public User getApprovedByEmployee() {
        return approvedByEmployee;
    }

    public void setApprovedByEmployee(User approvedByEmployee) {
        this.approvedByEmployee = approvedByEmployee;
    }

    public Date getAccountingEntryDefaultDate() {
        return accountingEntryDefaultDate;
    }

    public void setAccountingEntryDefaultDate(Date accountingEntryDefaultDate) {
        this.accountingEntryDefaultDate = accountingEntryDefaultDate;
    }

    public String getAccountingEntryDefaultUserNumber() {
        return accountingEntryDefaultUserNumber;
    }

    public void setAccountingEntryDefaultUserNumber(String accountingEntryDefaultUserNumber) {
        this.accountingEntryDefaultUserNumber = accountingEntryDefaultUserNumber;
    }

    public BusinessUnit getCheckDestination() {
        return checkDestination;
    }

    public void setCheckDestination(BusinessUnit checkDestination) {
        this.checkDestination = checkDestination;
    }

    public Voucher getVoucher() {
        return voucher;
    }

    public void setVoucher(Voucher voucher) {
        this.voucher = voucher;
    }

    public Long getIdtmpenc() {
        return idtmpenc;
    }

    public void setIdtmpenc(Long idtmpenc) {
        this.idtmpenc = idtmpenc;
    }
}
