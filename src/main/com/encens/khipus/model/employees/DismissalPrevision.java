package com.encens.khipus.model.employees;

import com.encens.khipus.model.BaseModel;
import com.encens.khipus.model.CompanyListener;
import com.encens.khipus.model.UpperCaseStringListener;
import com.encens.khipus.model.admin.BusinessUnit;
import com.encens.khipus.model.admin.Company;
import com.encens.khipus.model.finances.CostCenter;
import com.encens.khipus.model.finances.FinancesCurrencyType;
import com.encens.khipus.util.Constants;
import org.hibernate.annotations.Filter;
import org.hibernate.validator.Length;
import org.hibernate.validator.NotNull;

import javax.persistence.*;
import java.math.BigDecimal;

/**
 * @author
 * @version 4
 */
@TableGenerator(schema = Constants.KHIPUS_SCHEMA,
        name = "DismissalPrevision.tableGenerator",
        table = Constants.SEQUENCE_TABLE_NAME,
        pkColumnName = Constants.SEQUENCE_TABLE_PK_COLUMN_NAME,
        valueColumnName = Constants.SEQUENCE_TABLE_VALUE_COLUMN_NAME,
        allocationSize = Constants.SEQUENCE_ALLOCATION_SIZE,
        pkColumnValue = "PREVISIONRETIRO")

@Entity
@Filter(name = Constants.COMPANY_FILTER_NAME)
@EntityListeners({CompanyListener.class, UpperCaseStringListener.class})
@Table(schema = Constants.KHIPUS_SCHEMA, name = "PREVISIONRETIRO")
public class DismissalPrevision implements BaseModel {

    @Id
    @Column(name = "IDPREVISIONRETIRO", nullable = false)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "DismissalPrevision.tableGenerator")
    private Long id;

    @Column(name = "CODIGO", nullable = false)
    @NotNull
    private Long code;

    @Column(name = "NOTRANS")
    @Length(max = 10)
    private String transactionNumber;

    @Column(name = "ESTADO", length = Constants.STRING_LENGTH_20, nullable = false)
    @Enumerated(EnumType.STRING)
    @NotNull
    private DismissalPrevisionState state;

    @Column(name = "MONTO", nullable = false, precision = Constants.BIG_DECIMAL_DEFAULT_PRECISION, scale = Constants.BIG_DECIMAL_DEFAULT_SCALE)
    @NotNull
    private BigDecimal amount;

    @Column(name = "MONEDA", nullable = false, length = Constants.STRING_LENGTH_20)
    @Enumerated(EnumType.STRING)
    private FinancesCurrencyType currency;

    @Column(name = "TIPOCAMBIO", nullable = false, precision = Constants.BIG_DECIMAL_DEFAULT_PRECISION, scale = Constants.BIG_DECIMAL_DEFAULT_SCALE)
    private BigDecimal exchangeRate;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "IDGESTIONPLANILLA", nullable = false, updatable = false, insertable = true)
    private GestionPayroll gestionPayroll;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "IDUNIDADNEGOCIO", nullable = false, updatable = false, insertable = true)
    private BusinessUnit businessUnit;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "IDCATEGORIAPUESTO", nullable = false, updatable = false, insertable = true)
    private JobCategory jobCategory;

    @ManyToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumns({
            @JoinColumn(name = "NUMEROCOMPANIA", referencedColumnName = "NO_CIA", updatable = false, insertable = false),
            @JoinColumn(name = "CODIGOCENCOS", referencedColumnName = "COD_CC", updatable = false, insertable = false)
    })
    private CostCenter costCenter;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "IDRETIRO", nullable = false)
    @NotNull
    private Dismissal dismissal;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "IDCOMPANIA", nullable = false, updatable = false)
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

    public Long getCode() {
        return code;
    }

    public void setCode(Long code) {
        this.code = code;
    }

    public String getTransactionNumber() {
        return transactionNumber;
    }

    public void setTransactionNumber(String transactionNumber) {
        this.transactionNumber = transactionNumber;
    }

    public DismissalPrevisionState getState() {
        return state;
    }

    public void setState(DismissalPrevisionState state) {
        this.state = state;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public FinancesCurrencyType getCurrency() {
        return currency;
    }

    public void setCurrency(FinancesCurrencyType currency) {
        this.currency = currency;
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

    public BusinessUnit getBusinessUnit() {
        return businessUnit;
    }

    public void setBusinessUnit(BusinessUnit businessUnit) {
        this.businessUnit = businessUnit;
    }

    public JobCategory getJobCategory() {
        return jobCategory;
    }

    public void setJobCategory(JobCategory jobCategory) {
        this.jobCategory = jobCategory;
    }

    public CostCenter getCostCenter() {
        return costCenter;
    }

    public void setCostCenter(CostCenter costCenter) {
        this.costCenter = costCenter;
    }

    public Dismissal getDismissal() {
        return dismissal;
    }

    public void setDismissal(Dismissal dismissal) {
        this.dismissal = dismissal;
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
}
