package com.encens.khipus.model.finances;

import com.encens.khipus.model.BaseModel;
import com.encens.khipus.model.CompanyListener;
import com.encens.khipus.model.CompanyNumberListener;
import com.encens.khipus.model.UpperCaseStringListener;
import com.encens.khipus.model.admin.BusinessUnit;
import com.encens.khipus.model.admin.Company;
import com.encens.khipus.util.Constants;
import org.hibernate.annotations.Filter;
import org.hibernate.validator.Length;
import org.hibernate.validator.NotNull;

import javax.persistence.*;
import java.math.BigDecimal;

/**
 * Entity for SpendDistribution
 *
 * @author
 * @version 2.2 1
 */

@NamedQueries(
        {
                @NamedQuery(name = "SpendDistribution.findAll", query = "select o from SpendDistribution o "),
                @NamedQuery(name = "SpendDistribution.findByRotatoryFund",
                        query = "select spendDistribution from SpendDistribution spendDistribution where spendDistribution.rotatoryFund=:rotatoryFund"),
                @NamedQuery(name = "SpendDistribution.findCashAccountByRotatoryFund",
                        query = "select distinct cashAccount " +
                                " from SpendDistribution spendDistribution " +
                                " left join spendDistribution.rotatoryFund rotatoryFund" +
                                " left join spendDistribution.cashAccount cashAccount" +
                                " where cashAccount is not null and rotatoryFund=:rotatoryFund"),
                @NamedQuery(name = "SpendDistribution.findCostCenterByRotatoryFund",
                        query = "select distinct costCenter " +
                                " from SpendDistribution spendDistribution " +
                                " left join spendDistribution.rotatoryFund rotatoryFund" +
                                " left join spendDistribution.costCenter costCenter" +
                                " where costCenter is not null and rotatoryFund=:rotatoryFund"),
                @NamedQuery(name = "SpendDistribution.findAmountSumByRotatoryFund", query = "select sum(o.amount) from SpendDistribution o where o.rotatoryFund=:rotatoryFund"),
                @NamedQuery(name = "SpendDistribution.findAmountSumByRotatoryFundButCurrent", query = "select sum(o.amount) from SpendDistribution o where o.rotatoryFund=:rotatoryFund " +
                        "and o.id<>:id"),
                @NamedQuery(name = "SpendDistribution.findPercentageSumByRotatoryFund", query = "select sum(o.percentage) from SpendDistribution o where o.rotatoryFund=:rotatoryFund"),
                @NamedQuery(name = "SpendDistribution.findPercentageSumByRotatoryFundButCurrent", query = "select sum(o.percentage) from SpendDistribution o where o.rotatoryFund=:rotatoryFund " +
                        "and o.id<>:id")
        }
)

@TableGenerator(schema = Constants.KHIPUS_SCHEMA, name = "SpendDistribution.tableGenerator",
        table = com.encens.khipus.util.Constants.SEQUENCE_TABLE_NAME,
        pkColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_PK_COLUMN_NAME,
        valueColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_VALUE_COLUMN_NAME,
        pkColumnValue = "DISTRIBUCIONGASTO",
        allocationSize = com.encens.khipus.util.Constants.SEQUENCE_ALLOCATION_SIZE)

@Entity
@Filter(name = com.encens.khipus.util.Constants.COMPANY_FILTER_NAME)
@EntityListeners({CompanyListener.class, CompanyNumberListener.class, UpperCaseStringListener.class})
@Table(schema = Constants.KHIPUS_SCHEMA, name = "DISTRIBUCIONGASTO")
public class SpendDistribution implements BaseModel {

    @Id
    @Column(name = "IDDISTRIBUCIONGASTO", nullable = false, scale = 24)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "SpendDistribution.tableGenerator")
    private Long id;

    @Column(name = "MONTO", precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(name = "PORCENTAJE", precision = 12, scale = 2)
    private BigDecimal percentage;

    @ManyToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumns({
            @JoinColumn(name = "NUMEROCOMPANIA", referencedColumnName = "NO_CIA", updatable = false, insertable = false),
            @JoinColumn(name = "CODIGOCENCOS", referencedColumnName = "COD_CC", updatable = false, insertable = false)
    })
    private CostCenter costCenter;

    @Column(name = "CODIGOCENCOS", length = 6)
    @Length(max = 6)
    private String costCenterCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "IDUNIDADNEGOCIO", referencedColumnName = "idunidadnegocio")
    private BusinessUnit businessUnit;

    @Column(name = "CUENTA", length = 20, updatable = false)
    @Length(max = 20)
    private String accountCode;

    @ManyToOne(optional = true)
    @JoinColumns({
            @JoinColumn(name = "NUMEROCOMPANIA", referencedColumnName = "NO_CIA", updatable = false, insertable = false),
            @JoinColumn(name = "CUENTA", referencedColumnName = "CUENTA", updatable = false, insertable = false)
    })
    private CashAccount cashAccount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "IDFONDOROTATORIO", referencedColumnName = "IDFONDOROTATORIO")
    private RotatoryFund rotatoryFund;

    @Column(name = "NUMEROCOMPANIA", updatable = false, length = 2)
    @Length(max = 2)
    private String companyNumber;

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

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public BigDecimal getPercentage() {
        return percentage;
    }

    public void setPercentage(BigDecimal percentage) {
        this.percentage = percentage;
    }

    public CostCenter getCostCenter() {
        return costCenter;
    }

    public void setCostCenter(CostCenter costCenter) {
        this.costCenter = costCenter;
        setCompanyNumber(costCenter != null ? costCenter.getCompanyNumber() : null);
        setCostCenterCode(costCenter != null ? costCenter.getCode() : null);
    }

    public String getCostCenterCode() {
        return costCenterCode;
    }

    public void setCostCenterCode(String costCenterCode) {
        this.costCenterCode = costCenterCode;
    }

    public BusinessUnit getBusinessUnit() {
        return businessUnit;
    }

    public void setBusinessUnit(BusinessUnit businessUnit) {
        this.businessUnit = businessUnit;
    }

    public String getAccountCode() {
        return accountCode;
    }

    public void setAccountCode(String accountCode) {
        this.accountCode = accountCode;
    }

    public CashAccount getCashAccount() {
        return cashAccount;
    }

    public void setCashAccount(CashAccount cashAccount) {
        this.cashAccount = cashAccount;
        setAccountCode(cashAccount != null ? cashAccount.getAccountCode() : null);
        setCompanyNumber(cashAccount != null ? cashAccount.getCompanyNumber() : null);
    }

    public RotatoryFund getRotatoryFund() {
        return rotatoryFund;
    }

    public void setRotatoryFund(RotatoryFund rotatoryFund) {
        this.rotatoryFund = rotatoryFund;
    }

    public String getCompanyNumber() {
        return companyNumber;
    }

    public void setCompanyNumber(String companyNumber) {
        this.companyNumber = companyNumber;
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