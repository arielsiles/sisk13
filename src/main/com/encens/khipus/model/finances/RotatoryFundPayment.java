package com.encens.khipus.model.finances;

import com.encens.khipus.model.BaseModel;
import com.encens.khipus.model.CompanyListener;
import com.encens.khipus.model.CompanyNumberListener;
import com.encens.khipus.model.UpperCaseStringListener;
import com.encens.khipus.model.admin.BusinessUnit;
import com.encens.khipus.model.admin.Company;
import com.encens.khipus.model.admin.User;
import com.encens.khipus.model.warehouse.BeneficiaryType;
import com.encens.khipus.util.Constants;
import org.hibernate.annotations.Filter;
import org.hibernate.validator.Length;
import org.hibernate.validator.NotNull;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;

/**
 * @author
 * @version 3.5.2.2
 */

@TableGenerator(schema = Constants.KHIPUS_SCHEMA, name = "RotatoryFundPayment.tableGenerator",
        table = com.encens.khipus.util.Constants.SEQUENCE_TABLE_NAME,
        pkColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_PK_COLUMN_NAME,
        valueColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_VALUE_COLUMN_NAME,
        pkColumnValue = "PAGOFONDOROTA",
        allocationSize = com.encens.khipus.util.Constants.SEQUENCE_ALLOCATION_SIZE)

@NamedQueries({
        @NamedQuery(name = "RotatoryFundPayment.maxNumber",
                query = "select max(o.code) from RotatoryFundPayment o"),
        @NamedQuery(name = "RotatoryFundPayment.checkCurrency", query = "select count(o) from RotatoryFundPayment o where o.rotatoryFund=:rotatoryFund and o.paymentCurrency<>:currency"),
        @NamedQuery(name = "RotatoryFundPayment.findByRotatoryFund",
                query = "select rotatoryFundPayment from RotatoryFundPayment rotatoryFundPayment where rotatoryFundPayment.rotatoryFund =:rotatoryFund"),
        @NamedQuery(name = "RotatoryFundPayment.findByRotatoryFundByState",
                query = "select rotatoryFundPayment from RotatoryFundPayment rotatoryFundPayment where rotatoryFundPayment.rotatoryFund =:rotatoryFund " +
                        " and rotatoryFundPayment.state=:rotatoryFundPaymentState "),
        @NamedQuery(name = "RotatoryFundPayment.findMaxDateByRotatoryFund", query = "select max(o.paymentDate) from RotatoryFundPayment o where o.rotatoryFund=:rotatoryFund"),
        @NamedQuery(name = "RotatoryFundPayment.findSumByRotatoryFund", query = "select sum(o.paymentAmount) from RotatoryFundPayment o where o.rotatoryFund=:rotatoryFund " +
                " and o.state=:state"),
        @NamedQuery(name = "RotatoryFundPayment.findSumByRotatoryFundButCurrent", query = "select sum(o.paymentAmount) from RotatoryFundPayment o " +
                " where o.rotatoryFund=:rotatoryFund and o.state=:state and o.id<>:id "),
        @NamedQuery(name = "RotatoryFundPayment.annulPendantRotatoryFundPayments", query = "update RotatoryFundPayment o set o.state=:state " +
                " where o.rotatoryFund=:rotatoryFund and o.state=:databaseState"),
        @NamedQuery(name = "RotatoryFundPayment.sumPaymentAmountByRotatoryFundAndMovementDate",
                query = "select sum(o.paymentAmount)" +
                        " from RotatoryFundPayment o " +
                        " left join o.rotatoryFund rotatoryFund" +
                        " where o.rotatoryFund.id=:rotatoryFundId and o.paymentDate<:movementDate and o.state=:state")
})


@Entity
@Filter(name = com.encens.khipus.util.Constants.COMPANY_FILTER_NAME)
@Table(name = "PAGOFONDOROTA", schema = Constants.KHIPUS_SCHEMA,
        uniqueConstraints = @UniqueConstraint(columnNames = {"IDCOMPANIA", "CODIGO"}))
@EntityListeners({CompanyListener.class, CompanyNumberListener.class, UpperCaseStringListener.class})

public class RotatoryFundPayment implements BaseModel {

    @Id
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "RotatoryFundPayment.tableGenerator")
    @Column(name = "IDPAGO", nullable = false)
    private Long id;

    @Column(name = "CODIGO", nullable = true)
    private Integer code;

    @Column(name = "NOTRANS")
    @Length(max = 10)
    private String transactionNumber;

    @Column(name = "CUENTABANCO", length = 20)
    @Length(max = 20)
    private String bankAccountNumber;

    @Column(name = "ESTADO", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private RotatoryFundPaymentState state;

    @Column(name = "DESCRIPCION", length = 1000)
    @Length(max = 1000)
    private String description;

    @Column(name = "MOTIVOREVERSION", length = 1000)
    @Length(max = 1000)
    private String reversionCause;

    @Column(name = "MONEDAORIGEN", length = 20)
    @Enumerated(EnumType.STRING)
    private FinancesCurrencyType sourceCurrency;

    @Column(name = "MONEDAPAGO", nullable = false)
    @Enumerated(EnumType.STRING)
    private FinancesCurrencyType paymentCurrency;

    @Column(name = "MONTOORIGEN", precision = 12, scale = 2)
    private BigDecimal sourceAmount;

    @Column(name = "MONTOPAGO", precision = 12, scale = 2, nullable = false)
    private BigDecimal paymentAmount;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "IDCOMPANIA", nullable = false, updatable = false, insertable = true)
    @NotNull
    private Company company;

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
    private RotatoryFundPaymentType rotatoryFundPaymentType;

    @Column(name = "FECHACREACION")
    @Temporal(TemporalType.DATE)
    private Date creationDate;

    @Column(name = "FECHAPAGO")
    @Temporal(TemporalType.DATE)
    private Date paymentDate;

    @Column(name = "FECHAAPROBACION")
    @Temporal(TemporalType.DATE)
    private Date approvalDate;

    @ManyToOne
    @JoinColumn(name = "CREADOPOR")
    private User registerEmployee;

    @ManyToOne
    @JoinColumn(name = "APROBADOPOR")
    private User approvedByEmployee;

    @ManyToOne
    @JoinColumn(name = "ANULADOPOR")
    private User annulledByEmployee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "IDSEDEDESTINOCHEQUE", referencedColumnName = "idunidadnegocio")
    private BusinessUnit checkDestination;

    @Version
    @Column(name = "VERSION")
    private Long version;

    @ManyToOne(fetch = FetchType.LAZY)
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "IDFONDOROTATORIO", referencedColumnName = "IDFONDOROTATORIO")
    private RotatoryFund rotatoryFund;

    @Column(name = "NO_CIA", length = 2)
    @Length(max = 2)
    private String companyNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "NOTRANS", referencedColumnName = "NO_TRANS", insertable = false, updatable = false)
    private FinanceDocument financeDocument;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "NOTRANS", referencedColumnName = "NO_TRANS", insertable = false, updatable = false)
    private PayableDocument payableDocument;

    /* the cash account adjustment*/
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns({
            @JoinColumn(name = "NO_CIA", referencedColumnName = "NO_CIA", updatable = false, insertable = false),
            @JoinColumn(name = "CUENTAAJUSTE", referencedColumnName = "CUENTA", updatable = false, insertable = false)
    })
    private CashAccount cashAccountAdjustment;
    @Column(name = "CUENTAAJUSTE", length = 20)
    @Length(max = 20)
    private String cashAccountCodeAdjustment;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTransactionNumber() {
        return transactionNumber;
    }

    public void setTransactionNumber(String transactionNumber) {
        this.transactionNumber = transactionNumber;
    }

    public String getBankAccountNumber() {
        return bankAccountNumber;
    }

    public void setBankAccountNumber(String bankAccountNumber) {
        this.bankAccountNumber = bankAccountNumber;
    }

    public RotatoryFundPaymentState getState() {
        return state;
    }

    public void setState(RotatoryFundPaymentState state) {
        this.state = state;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public Date getPaymentDate() {
        return paymentDate;
    }

    public void setPaymentDate(Date paymentDate) {
        this.paymentDate = paymentDate;
    }

    public RotatoryFund getRotatoryFund() {
        return rotatoryFund;
    }

    public void setRotatoryFund(RotatoryFund rotatoryFund) {
        this.rotatoryFund = rotatoryFund;
    }

    public FinancesCurrencyType getSourceCurrency() {
        return sourceCurrency;
    }

    public void setSourceCurrency(FinancesCurrencyType sourceCurrency) {
        this.sourceCurrency = sourceCurrency;
    }

    public FinancesCurrencyType getPaymentCurrency() {
        return paymentCurrency;
    }

    public void setPaymentCurrency(FinancesCurrencyType paymentCurrency) {
        this.paymentCurrency = paymentCurrency;
    }

    public BigDecimal getSourceAmount() {
        return sourceAmount;
    }

    public void setSourceAmount(BigDecimal sourceAmount) {
        this.sourceAmount = sourceAmount;
    }

    public BigDecimal getPaymentAmount() {
        return paymentAmount;
    }

    public void setPaymentAmount(BigDecimal paymentAmount) {
        this.paymentAmount = paymentAmount;
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

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    public FinancesBankAccount getBankAccount() {
        return bankAccount;
    }

    public RotatoryFundPaymentType getRotatoryFundPaymentType() {
        return rotatoryFundPaymentType;
    }

    public void setRotatoryFundPaymentType(RotatoryFundPaymentType rotatoryFundPaymentType) {
        this.rotatoryFundPaymentType = rotatoryFundPaymentType;
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

    public User getAnnulledByEmployee() {
        return annulledByEmployee;
    }

    public void setAnnulledByEmployee(User annulledByEmployee) {
        this.annulledByEmployee = annulledByEmployee;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getCompanyNumber() {
        return companyNumber;
    }

    public void setCompanyNumber(String companyNumber) {
        this.companyNumber = companyNumber;
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

    public Boolean isNullified() {
        return null != getState() && RotatoryFundPaymentState.ANL.equals(getState());
    }

    public Boolean isApproved() {
        return null != getState() && RotatoryFundPaymentState.APR.equals(getState());
    }

    public Boolean isCashboxPaymentType() {
        return null != getRotatoryFundPaymentType() && RotatoryFundPaymentType.PAYMENT_CASHBOX.equals(getRotatoryFundPaymentType());
    }

    public Boolean isCashAccountAdjustmentPaymentType() {
        return null != getRotatoryFundPaymentType() && RotatoryFundPaymentType.PAYMENT_CASH_ACCOUNT_ADJ.equals(getRotatoryFundPaymentType());
    }

    public Boolean isBankAccountPaymentType() {
        return null != getRotatoryFundPaymentType() && RotatoryFundPaymentType.PAYMENT_BANK_ACCOUNT.equals(getRotatoryFundPaymentType());
    }

    public Boolean isCheckPaymentType() {
        return null != getRotatoryFundPaymentType() && RotatoryFundPaymentType.PAYMENT_WITH_CHECK.equals(getRotatoryFundPaymentType());
    }

    public Date getApprovalDate() {
        return approvalDate;
    }

    public void setApprovalDate(Date approvalDate) {
        this.approvalDate = approvalDate;
    }

    public String getReversionCause() {
        return reversionCause;
    }

    public void setReversionCause(String reversionCause) {
        this.reversionCause = reversionCause;
    }

    public FinanceDocument getFinanceDocument() {
        return financeDocument;
    }

    public void setFinanceDocument(FinanceDocument financeDocument) {
        this.financeDocument = financeDocument;
    }

    public PayableDocument getPayableDocument() {
        return payableDocument;
    }

    public void setPayableDocument(PayableDocument payableDocument) {
        this.payableDocument = payableDocument;
    }

    public CashAccount getCashAccountAdjustment() {
        return cashAccountAdjustment;
    }

    public void setCashAccountAdjustment(CashAccount cashAccountAdjustment) {
        this.cashAccountAdjustment = cashAccountAdjustment;
        setCashAccountCodeAdjustment(this.cashAccountAdjustment != null ? this.cashAccountAdjustment.getAccountCode() : null);
        setSourceCurrency(this.cashAccountAdjustment != null ? this.cashAccountAdjustment.getCurrency() : null);
    }

    public String getCashAccountCodeAdjustment() {
        return cashAccountCodeAdjustment;
    }

    public void setCashAccountCodeAdjustment(String cashAccountCodeAdjustment) {
        this.cashAccountCodeAdjustment = cashAccountCodeAdjustment;
    }

    public BusinessUnit getCheckDestination() {
        return checkDestination;
    }

    public void setCheckDestination(BusinessUnit checkDestination) {
        this.checkDestination = checkDestination;
    }
}