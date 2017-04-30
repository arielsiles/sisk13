package com.encens.khipus.model.finances;

import com.encens.khipus.model.BaseModel;
import com.encens.khipus.model.CompanyListener;
import com.encens.khipus.model.CompanyNumberListener;
import com.encens.khipus.model.UpperCaseStringListener;
import com.encens.khipus.model.admin.BusinessUnit;
import com.encens.khipus.model.admin.Company;
import com.encens.khipus.model.admin.User;
import com.encens.khipus.model.employees.Employee;
import com.encens.khipus.util.FormatUtils;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.Type;
import org.hibernate.validator.Length;
import org.hibernate.validator.NotNull;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Entity for RotatoryFund
 *
 * @author
 * @version 2.23
 */

@NamedQueries(
        {
                @NamedQuery(name = "RotatoryFund.findById", query = "select o from RotatoryFund o " +
                        " left join fetch o.documentType documentType" +
                        " left join fetch o.jobContract jobContract" +
                        " left join fetch o.businessUnit businessUnit" +
                        " left join fetch o.costCenter costCenter" +
                        " left join fetch o.employee employee" +
                        " left join fetch o.cashAccount cashAccount" +
                        " left join fetch o.provider provider" +
                        " left join fetch o.registerEmployee registerUserEmployee" +
                        " left join fetch registerUserEmployee.employee registerEmployee" +
                        " left join fetch o.approvedByEmployee approvedByUserEmployee" +
                        " left join fetch approvedByUserEmployee.employee approvedUserEmployee" +
                        " left join fetch o.annulledByEmployee annulledByUserEmployee" +
                        " left join fetch annulledByUserEmployee.employee annulledByEmployee" +
                        " where o.id=:rotatoryFundId"),
                @NamedQuery(name = "RotatoryFund.maxNumber",
                        query = "select max(o.code) from RotatoryFund o"),
                @NamedQuery(name = "RotatoryFund.findByEmployeeByInitDateByEndDate", query = "select o from RotatoryFund o " +
                        " where o.employee=:employee and o.date >=:initDate and o.date <=:endDate"),
                @NamedQuery(name = "RotatoryFund.sumByEmployeeByTypeByCurrencyByState", query = "select sum(o.receivableResidue)" +
                        " from RotatoryFund o left join o.documentType documentType" +
                        " where o.employee=:employee and documentType.rotatoryFundType =:rotatoryFundType and o.payCurrency=:payCurrency and o.state=:state "),
                @NamedQuery(name = "RotatoryFund.findByEmployeeByTypeByCurrencyByState", query = "select o" +
                        " from RotatoryFund o left join o.documentType documentType " +
                        " where o.employee=:employee and documentType.rotatoryFundType =:rotatoryFundType and o.payCurrency=:payCurrency and o.state=:state "),
                @NamedQuery(name = "RotatoryFund.sumReceivableResidueByEmployeeDocumentTypeCashAccountByState",
                        query = "select sum(o.receivableResidue) from RotatoryFund o left join o.documentType documentType" +
                                " where o.employee.id=:employeeId and documentType.id=:documentTypeId and o.companyNumber=:companyNumber and o.cashAccountCode=:cashAccountCode and o.state in (:stateList)")

        }
)

@TableGenerator(schema = com.encens.khipus.util.Constants.KHIPUS_SCHEMA, name = "RotatoryFund.tableGenerator",
        table = com.encens.khipus.util.Constants.SEQUENCE_TABLE_NAME,
        pkColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_PK_COLUMN_NAME,
        valueColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_VALUE_COLUMN_NAME,
        pkColumnValue = "FONDOROTATORIO",
        allocationSize = com.encens.khipus.util.Constants.SEQUENCE_ALLOCATION_SIZE)

@Entity
@Filter(name = com.encens.khipus.util.Constants.COMPANY_FILTER_NAME)
@EntityListeners({CompanyListener.class, CompanyNumberListener.class, UpperCaseStringListener.class})
@Table(schema = com.encens.khipus.util.Constants.KHIPUS_SCHEMA, name = "FONDOROTATORIO",
        uniqueConstraints = @UniqueConstraint(columnNames = {"IDCOMPANIA", "CODIGO"}))
public class RotatoryFund implements BaseModel {

    @Id
    @Column(name = "IDFONDOROTATORIO", nullable = false, scale = 24)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "RotatoryFund.tableGenerator")
    private Long id;

    @Column(name = "CODIGO", nullable = false)
    private Integer code;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "IDTIPODOCFONDOROTA")
    @NotNull
    private RotatoryFundDocumentType documentType;

    @Column(name = "FECHA", nullable = false)
    @Temporal(TemporalType.DATE)
    private Date date;

    @Column(name = "MONTO", nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(name = "RESIDUOPORCOBRAR", precision = 12, scale = 2)
    private BigDecimal receivableResidue;

    @Column(name = "RESIDUOPORPAGAR", precision = 12, scale = 2)
    private BigDecimal payableResidue;

    @Column(name = "CUOTAS", nullable = false, precision = 12)
    private Integer paymentsNumber;

    @ManyToOne
    @JoinColumn(name = "IDEMPLEADO")
    private Employee employee;

    @ManyToOne
    @JoinColumn(name = "IDCONTRATOPUESTO", referencedColumnName = "idcontratopuesto")
    private JobContract jobContract;

    @Column(name = "MONEDAPAGO", nullable = false)
    @Enumerated(EnumType.STRING)
    private FinancesCurrencyType payCurrency;

    @Column(name = "TIPOCAMBIO", precision = 16, scale = 6)
    private BigDecimal exchangeRate;

    @Column(name = "FECHAINICIO")
    @Temporal(TemporalType.DATE)
    private Date startDate;

    @Column(name = "FECHAVENCIMIENTO")
    @Temporal(TemporalType.DATE)
    private Date expirationDate;

    @Column(name = "DESCRIPCION", length = 1000)
    @Length(max = 1000)
    private String description;

    @Column(name = "ESTADO", nullable = false)
    @Enumerated(EnumType.STRING)
    @NotNull
    private RotatoryFundState state;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "IDUNIDADNEGOCIO", referencedColumnName = "idunidadnegocio")
    private BusinessUnit businessUnit;

    @ManyToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumns({
            @JoinColumn(name = "NO_CIA", referencedColumnName = "NO_CIA", updatable = false, insertable = false),
            @JoinColumn(name = "COD_CC", referencedColumnName = "COD_CC", updatable = false, insertable = false)
    })
    private CostCenter costCenter;

    @Column(name = "COD_CC", length = 6, nullable = false)
    @Length(max = 6)
    private String costCenterCode;

    @ManyToOne
    @JoinColumn(name = "CREADOPOR")
    private User registerEmployee;

    @ManyToOne
    @JoinColumn(name = "APROBADOPOR")
    private User approvedByEmployee;

    @ManyToOne
    @JoinColumn(name = "ANULADOPOR")
    private User annulledByEmployee;

    @Column(name = "PORPLANILLA")
    @Type(type = com.encens.khipus.model.usertype.IntegerBooleanUserType.NAME)
    private Boolean discountByPayroll;

    @Column(name = "NO_CIA", length = 2)
    @Length(max = 2)
    private String companyNumber;

    @Column(name = "CUENTACTB", length = 20)
    @Length(max = 20)
    private String cashAccountCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns({
            @JoinColumn(name = "NO_CIA", insertable = false, updatable = false, referencedColumnName = "NO_CIA"),
            @JoinColumn(name = "CUENTACTB", insertable = false, updatable = false, referencedColumnName = "CUENTA")
    })
    private CashAccount cashAccount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns({
            @JoinColumn(name = "NO_CIA", updatable = false, insertable = false),
            @JoinColumn(name = "COD_PROV", updatable = false, insertable = false)
    })
    private Provider provider;

    @Column(name = "COD_PROV", length = 6)
    @Length(max = 6)
    private String providerCode;

    @OneToMany(mappedBy = "rotatoryFund", fetch = FetchType.LAZY)
    private List<RotatoryFundPayment> rotatoryFundPaymentList = new ArrayList<RotatoryFundPayment>(0);

    @OneToMany(mappedBy = "rotatoryFund", fetch = FetchType.LAZY)
    private List<RotatoryFundCollection> rotatoryFundCollectionList = new ArrayList<RotatoryFundCollection>(0);

    @OneToMany(mappedBy = "rotatoryFund", fetch = FetchType.LAZY)
    private List<RotatoryFundMovement> rotatoryFundMovementList = new ArrayList<RotatoryFundMovement>(0);

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "IDCOMPANIA", nullable = false, updatable = false, insertable = true)
    @NotNull
    private Company company;

    @Version
    @Column(name = "VERSION", nullable = false)
    private long version;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
    }

    public BigDecimal getExchangeRate() {
        return exchangeRate;
    }

    public void setExchangeRate(BigDecimal exchangeRate) {
        this.exchangeRate = exchangeRate;
    }

    public RotatoryFundState getState() {
        return state;
    }

    public void setState(RotatoryFundState state) {
        this.state = state;
    }

    public BusinessUnit getBusinessUnit() {
        return businessUnit;
    }

    public void setBusinessUnit(BusinessUnit businessUnit) {
        this.businessUnit = businessUnit;
    }

    public CostCenter getCostCenter() {
        return costCenter;
    }

    public void setCostCenter(CostCenter costCenter) {
        this.costCenter = costCenter;
        setCostCenterCode(costCenter != null ? costCenter.getCode() : null);
    }

    public String getCostCenterCode() {
        return costCenterCode;
    }

    public void setCostCenterCode(String costCenterCode) {
        this.costCenterCode = costCenterCode;
    }

    public Boolean getDiscountByPayroll() {
        return discountByPayroll;
    }

    public void setDiscountByPayroll(Boolean discountByPayroll) {
        this.discountByPayroll = discountByPayroll;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
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

    public Integer getPaymentsNumber() {
        return paymentsNumber;
    }

    public void setPaymentsNumber(Integer paymentsNumber) {
        this.paymentsNumber = paymentsNumber;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(Date expirationDate) {
        this.expirationDate = expirationDate;
    }

    public JobContract getJobContract() {
        return jobContract;
    }

    public void setJobContract(JobContract jobContract) {
        this.jobContract = jobContract;
    }

    public RotatoryFundDocumentType getDocumentType() {
        return documentType;
    }

    public void setDocumentType(RotatoryFundDocumentType documentType) {
        this.documentType = documentType;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public FinancesCurrencyType getPayCurrency() {
        return payCurrency;
    }

    public void setPayCurrency(FinancesCurrencyType payCurrency) {
        this.payCurrency = payCurrency;
    }

    public String getCompanyNumber() {
        return companyNumber;
    }

    public void setCompanyNumber(String companyNumber) {
        this.companyNumber = companyNumber;
    }

    public String getCashAccountCode() {
        return cashAccountCode;
    }

    public void setCashAccountCode(String cashAccountCode) {
        this.cashAccountCode = cashAccountCode;
    }

    public CashAccount getCashAccount() {
        return cashAccount;
    }

    public void setCashAccount(CashAccount cashAccount) {
        this.cashAccount = cashAccount;
        setCashAccountCode(this.cashAccount != null ? this.cashAccount.getAccountCode() : null);
    }

    public BigDecimal getReceivableResidue() {
        return receivableResidue;
    }

    public void setReceivableResidue(BigDecimal receivableResidue) {
        this.receivableResidue = receivableResidue;
    }

    public User getAnnulledByEmployee() {
        return annulledByEmployee;
    }

    public void setAnnulledByEmployee(User annulledByEmployee) {
        this.annulledByEmployee = annulledByEmployee;
    }

    public BigDecimal getPayableResidue() {
        return payableResidue;
    }

    public void setPayableResidue(BigDecimal payableResidue) {
        this.payableResidue = payableResidue;
    }

    public Provider getProvider() {
        return provider;
    }

    public void setProvider(Provider provider) {
        this.provider = provider;
        setProviderCode(this.provider != null ? this.provider.getProviderCode() : null);
    }

    public String getProviderCode() {
        return providerCode;
    }

    public void setProviderCode(String providerCode) {
        this.providerCode = providerCode;
    }

    public List<RotatoryFundPayment> getRotatoryFundPaymentList() {
        return rotatoryFundPaymentList;
    }

    public void setRotatoryFundPaymentList(List<RotatoryFundPayment> rotatoryFundPaymentList) {
        this.rotatoryFundPaymentList = rotatoryFundPaymentList;
    }

    public List<RotatoryFundCollection> getRotatoryFundCollectionList() {
        return rotatoryFundCollectionList;
    }

    public void setRotatoryFundCollectionList(List<RotatoryFundCollection> rotatoryFundCollectionList) {
        this.rotatoryFundCollectionList = rotatoryFundCollectionList;
    }

    public List<RotatoryFundMovement> getRotatoryFundMovementList() {
        return rotatoryFundMovementList;
    }

    public void setRotatoryFundMovementList(List<RotatoryFundMovement> rotatoryFundMovementList) {
        this.rotatoryFundMovementList = rotatoryFundMovementList;
    }

    public boolean isNullified() {
        return null != getState() && RotatoryFundState.ANL.equals(getState());
    }

    public boolean isLiquidated() {
        return null != getState() && RotatoryFundState.LIQ.equals(getState());
    }

    public String getFullName() {
        return FormatUtils.concatBySeparator(" - ", getCode(), getDocumentType().getName());
    }

    /*state helpers*/
    public boolean isAnnulled() {
        return state != null && state.equals(RotatoryFundState.ANL);
    }
}