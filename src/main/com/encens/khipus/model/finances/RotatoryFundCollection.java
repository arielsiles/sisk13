package com.encens.khipus.model.finances;

import com.encens.khipus.model.BaseModel;
import com.encens.khipus.model.CompanyListener;
import com.encens.khipus.model.CompanyNumberListener;
import com.encens.khipus.model.UpperCaseStringListener;
import com.encens.khipus.model.admin.Company;
import com.encens.khipus.model.admin.User;
import com.encens.khipus.model.employees.Employee;
import com.encens.khipus.model.employees.GestionPayroll;
import com.encens.khipus.model.purchases.PurchaseOrderPayment;
import com.encens.khipus.model.warehouse.BeneficiaryType;
import com.encens.khipus.util.Constants;
import org.hibernate.annotations.Filter;
import org.hibernate.validator.Length;
import org.hibernate.validator.NotNull;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author
 * @version 2.24
 */

@TableGenerator(schema = Constants.KHIPUS_SCHEMA, name = "RotatoryFundCollection.tableGenerator",
        table = com.encens.khipus.util.Constants.SEQUENCE_TABLE_NAME,
        pkColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_PK_COLUMN_NAME,
        valueColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_VALUE_COLUMN_NAME,
        pkColumnValue = "COBROFONDOROTA",
        allocationSize = com.encens.khipus.util.Constants.SEQUENCE_ALLOCATION_SIZE)

@NamedQueries({
        @NamedQuery(name = "RotatoryFundCollection.maxNumber",
                query = "select max(o.code) from RotatoryFundCollection o"),
        @NamedQuery(name = "RotatoryFundCollection.findByRotatoryFund",
                query = "select rotatoryFundCollection from RotatoryFundCollection rotatoryFundCollection where rotatoryFundCollection.rotatoryFund =:rotatoryFund"),
        @NamedQuery(name = "RotatoryFundCollection.deleteRotatoryFundByGestionPayroll",
                query = "delete from RotatoryFundCollection rotatoryFundCollection where rotatoryFundCollection.gestionPayroll =:gestionPayroll and rotatoryFundCollection.state=:state"),
        @NamedQuery(name = "RotatoryFundCollection.findMaxDateByRotatoryFund", query = "select max(o.collectionDate) from RotatoryFundCollection o where o.rotatoryFund=:rotatoryFund"),
        @NamedQuery(name = "RotatoryFundCollection.findSumByRotatoryFund", query = "select sum(o.collectionAmount) from RotatoryFundCollection o where o.rotatoryFund=:rotatoryFund " +
                " and o.state=:state"),
        @NamedQuery(name = "RotatoryFundCollection.findSumByRotatoryFundButCurrent", query = "select sum(o.collectionAmount) from RotatoryFundCollection o " +
                " where o.rotatoryFund=:rotatoryFund and o.state=:state and o.id<>:id "),
        @NamedQuery(name = "RotatoryFundCollection.annulPendantRotatoryFundCollections", query = "update RotatoryFundCollection o set o.state=:state " +
                " where o.rotatoryFund=:rotatoryFund and o.state=:databaseState"),
        @NamedQuery(name = "RotatoryFundCollection.findRotatoryFundCollectionByGestionPayroll",
                query = "select rfp from RotatoryFundCollection rfp " +
                        " where rfp.gestionPayroll=:gestionPayroll" +
                        " and rfp.state=:state order by rfp.quota.id"),
        @NamedQuery(name = "RotatoryFundCollection.findRotatoryFundCollectionByGestionPayrollByEmployeeList",
                query = "select rfp from RotatoryFundCollection rfp " +
                        " left join rfp.rotatoryFund rotatoryFund" +
                        " left join rotatoryFund.jobContract jobContract" +
                        " left join jobContract.contract contract" +
                        " left join contract.employee employee" +
                        " where employee.id is not null and rfp.gestionPayroll=:gestionPayroll" +
                        " and rfp.state=:state and employee.id in (:employeeIdList) order by rfp.quota.id"),
        @NamedQuery(name = "RotatoryFundCollection.findByRotatoryFundByState",
                query = "select rotatoryFundCollection from RotatoryFundCollection rotatoryFundCollection where rotatoryFundCollection.rotatoryFund =:rotatoryFund " +
                        " and rotatoryFundCollection.state=:rotatoryFundCollectionState "),
        @NamedQuery(name = "RotatoryFundCollection.sumCollectionAmountByRotatoryFundAndMovementDate",
                query = "select sum(o.collectionAmount)" +
                        " from RotatoryFundCollection o " +
                        " left join o.rotatoryFund rotatoryFund" +
                        " where o.rotatoryFund.id=:rotatoryFundId and o.collectionDate<:movementDate and o.state=:state")
})


@Entity
@Filter(name = com.encens.khipus.util.Constants.COMPANY_FILTER_NAME)
@Table(name = "COBROFONDOROTA", schema = Constants.KHIPUS_SCHEMA,
        uniqueConstraints = @UniqueConstraint(columnNames = {"IDCOMPANIA", "CODIGO"}))
@EntityListeners({CompanyListener.class, CompanyNumberListener.class, UpperCaseStringListener.class})
public class RotatoryFundCollection implements BaseModel {

    @Id
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "RotatoryFundCollection.tableGenerator")
    @Column(name = "IDCOBRO", nullable = false)
    private Long id;

    @Column(name = "CODIGO", nullable = false)
    @NotNull
    private Integer code;

    @Column(name = "NOTRANS")
    @Length(max = 10)
    private String transactionNumber;

    @Column(name = "CUENTABANCO", length = 20)
    @Length(max = 20)
    private String bankAccountNumber;

    @Column(name = "NUMERODEPOSITO", length = 20)
    @Length(max = 20)
    private String bankDepositNumber;

    @Column(name = "ESTADO", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private RotatoryFundCollectionState state;

    @Column(name = "DESCRIPCION", length = 1000)
    @Length(max = 1000)
    private String description;

    @Column(name = "MONEDAORIGEN", length = 20)
    @Enumerated(EnumType.STRING)
    private FinancesCurrencyType sourceCurrency;

    @Column(name = "MONEDACOBRO", nullable = false)
    @Enumerated(EnumType.STRING)
    private FinancesCurrencyType collectionCurrency;

    @Column(name = "MONTOORIGEN", precision = 12, scale = 2)
    private BigDecimal sourceAmount;

    @Column(name = "MONTOCOBRO", precision = 12, scale = 2, nullable = false)
    private BigDecimal collectionAmount;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "IDCOMPANIA", nullable = false, updatable = false, insertable = true)
    @NotNull
    private Company company;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "IDDOCUMENTOCOBRO")
    private CollectionDocument collectionDocument;

    @Column(name = "NOMBREBENEFICIARIO", length = 200)
    @Length(max = 200)
    private String beneficiaryName;

    @Column(name = "TIPOBENEFICIARIO")
    @Enumerated(EnumType.STRING)
    private BeneficiaryType beneficiaryType;

    @Column(name = "TIPOCAMBIO", precision = 12, scale = 2)
    private BigDecimal exchangeRate;

    @Column(name = "TIPOCOBRO")
    @Enumerated(EnumType.STRING)
    private RotatoryFundCollectionType rotatoryFundCollectionType;

    @Column(name = "FECHACREACION")
    @Temporal(TemporalType.DATE)
    private Date creationDate;

    @Column(name = "FECHACOBRO")
    @Temporal(TemporalType.DATE)
    private Date collectionDate;

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

    @Column(name = "NO_CIA", length = 2)
    @Length(max = 2)
    private String companyNumber;

    @Version
    @Column(name = "VERSION")
    private Long version;


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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns({
            @JoinColumn(name = "IDFONDOROTATORIO", referencedColumnName = "IDFONDOROTATORIO")
    })
    private RotatoryFund rotatoryFund;

    /*in case of a Collection by payroll*/
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns({
            @JoinColumn(name = "IDCUOTA", referencedColumnName = "IDCUOTA")
    })
    private Quota quota;

    /*in case of a Collection by payroll to know how much was the residue at generating payroll time
    * if the payroll generation detects any change so the algorithm of payroll generation will
    * recreate all the Collections and will mark as outdated all the generated payrolls by the
    * corresponding gestion payroll */
    @Column(name = "SALDOCUOTA", precision = 13, scale = 2)
    private BigDecimal quotaResidue;

    /*in case of a Collection by payroll*/
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "IDGESTIONPLANILLA", referencedColumnName = "idgestionplanilla")
    private GestionPayroll gestionPayroll;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "IDPAGOORDENCOMPRA", referencedColumnName = "IDPAGOORDENCOMPRA")
    private PurchaseOrderPayment purchaseOrderPayment;

    @OneToMany(mappedBy = "rotatoryFundCollection", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<RotatoryFundCollectionSpendDistribution> rotatoryFundCollectionSpendDistributionList = new ArrayList<RotatoryFundCollectionSpendDistribution>(0);

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ENTREGADOA", referencedColumnName = "idempleado")
    private Employee receiver;

    @Column(name = "OBSERVACION", length = 1000)
    @Length(max = 1000)
    private String observation;

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

    @Column(name = "NO_TRANS_DEP", length = 10)
    private String depositAdjustmentTransaction;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns({
            @JoinColumn(name = "NO_CIA", referencedColumnName = "NO_CIA", updatable = false, insertable = false),
            @JoinColumn(name = "NO_TRANS_DEP", referencedColumnName = "NO_TRANS", updatable = false, insertable = false)
    })
    private FinanceDocument depositAdjustment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "NOTRANS", referencedColumnName = "NO_TRANS", insertable = false, updatable = false)
    private FinanceDocument financeDocument;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "NOTRANS", referencedColumnName = "NO_TRANS", insertable = false, updatable = false)
    private PayableDocument payableDocument;

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

    public RotatoryFundCollectionState getState() {
        return state;
    }

    public void setState(RotatoryFundCollectionState state) {
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

    public Date getCollectionDate() {
        return collectionDate;
    }

    public void setCollectionDate(Date collectionDate) {
        this.collectionDate = collectionDate;
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

    public FinancesCurrencyType getCollectionCurrency() {
        return collectionCurrency;
    }

    public void setCollectionCurrency(FinancesCurrencyType collectionCurrency) {
        this.collectionCurrency = collectionCurrency;
    }

    public BigDecimal getSourceAmount() {
        return sourceAmount;
    }

    public void setSourceAmount(BigDecimal sourceAmount) {
        this.sourceAmount = sourceAmount;
    }

    public BigDecimal getCollectionAmount() {
        return collectionAmount;
    }

    public void setCollectionAmount(BigDecimal collectionAmount) {
        this.collectionAmount = collectionAmount;
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

    public GestionPayroll getGestionPayroll() {
        return gestionPayroll;
    }

    public void setGestionPayroll(GestionPayroll gestionPayroll) {
        this.gestionPayroll = gestionPayroll;
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

    public RotatoryFundCollectionType getRotatoryFundCollectionType() {
        return rotatoryFundCollectionType;
    }

    public void setRotatoryFundCollectionType(RotatoryFundCollectionType rotatoryFundCollectionType) {
        this.rotatoryFundCollectionType = rotatoryFundCollectionType;
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

    public Quota getQuota() {
        return quota;
    }

    public void setQuota(Quota quota) {
        this.quota = quota;
    }

    public BigDecimal getQuotaResidue() {
        return quotaResidue;
    }

    public void setQuotaResidue(BigDecimal quotaResidue) {
        this.quotaResidue = quotaResidue;
    }

    public CollectionDocument getCollectionDocument() {
        return collectionDocument;
    }

    public void setCollectionDocument(CollectionDocument collectionDocument) {
        this.collectionDocument = collectionDocument;
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

    public String getBankDepositNumber() {
        return bankDepositNumber;
    }

    public void setBankDepositNumber(String bankDepositNumber) {
        this.bankDepositNumber = bankDepositNumber;
    }

    public PurchaseOrderPayment getPurchaseOrderPayment() {
        return purchaseOrderPayment;
    }

    public void setPurchaseOrderPayment(PurchaseOrderPayment purchaseOrderPayment) {
        this.purchaseOrderPayment = purchaseOrderPayment;
    }

    public Date getApprovalDate() {
        return approvalDate;
    }

    public void setApprovalDate(Date approvalDate) {
        this.approvalDate = approvalDate;
    }

    public List<RotatoryFundCollectionSpendDistribution> getRotatoryFundCollectionSpendDistributionList() {
        return rotatoryFundCollectionSpendDistributionList;
    }

    public void setRotatoryFundCollectionSpendDistributionList(List<RotatoryFundCollectionSpendDistribution> rotatoryFundCollectionSpendDistributionList) {
        this.rotatoryFundCollectionSpendDistributionList = rotatoryFundCollectionSpendDistributionList;
    }

    public Employee getReceiver() {
        return receiver;
    }

    public void setReceiver(Employee receiver) {
        this.receiver = receiver;
    }

    public String getObservation() {
        return observation;
    }

    public void setObservation(String observation) {
        this.observation = observation;
    }

    public CashAccount getCashAccountAdjustment() {
        return cashAccountAdjustment;
    }

    public void setCashAccountAdjustment(CashAccount cashAccountAdjustment) {
        this.cashAccountAdjustment = cashAccountAdjustment;
        setCashAccountCodeAdjustment(this.cashAccountAdjustment != null ? this.cashAccountAdjustment.getAccountCode() : null);
    }

    public String getCashAccountCodeAdjustment() {
        return cashAccountCodeAdjustment;
    }

    public void setCashAccountCodeAdjustment(String cashAccountCodeAdjustment) {
        this.cashAccountCodeAdjustment = cashAccountCodeAdjustment;
    }

    public String getDepositAdjustmentTransaction() {
        return depositAdjustmentTransaction;
    }

    public void setDepositAdjustmentTransaction(String depositAdjustmentTransaction) {
        this.depositAdjustmentTransaction = depositAdjustmentTransaction;
    }

    public FinanceDocument getDepositAdjustment() {
        return depositAdjustment;
    }

    public void setDepositAdjustment(FinanceDocument depositAdjustment) {
        this.depositAdjustment = depositAdjustment;
        setDepositAdjustmentTransaction(depositAdjustment != null ? depositAdjustment.getTransactionNumber() : null);
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
}