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
 * Entity for RotatoryFundCollectionSpendDistribution
 *
 * @author
 * @version 2.23
 */

@NamedQueries(
        {
                @NamedQuery(name = "RotatoryFundCollectionSpendDistribution.findAll", query = "select o from RotatoryFundCollectionSpendDistribution o "),
                @NamedQuery(name = "RotatoryFundCollectionSpendDistribution.findByRotatoryFundCollection",
                        query = "select r from RotatoryFundCollectionSpendDistribution r where r.rotatoryFundCollection=:rotatoryFundCollection"),
                @NamedQuery(name = "RotatoryFundCollectionSpendDistribution.findAmountSumByRotatoryFundCollection", query = "select sum(o.amount) from RotatoryFundCollectionSpendDistribution  o where o.rotatoryFundCollection=:rotatoryFundCollection"),
                @NamedQuery(name = "RotatoryFundCollectionSpendDistribution.findAmountSumByRotatoryFundCollectionButCurrent", query = "select sum(o.amount) from RotatoryFundCollectionSpendDistribution o where o.rotatoryFundCollection=:rotatoryFundCollection " +
                        "and o.id<>:id"),
                @NamedQuery(name = "RotatoryFundCollectionSpendDistribution.findPercentageSumByRotatoryFundCollection", query = "select sum(o.percentage) from RotatoryFundCollectionSpendDistribution o where o.rotatoryFundCollection=:rotatoryFundCollection"),
                @NamedQuery(name = "RotatoryFundCollectionSpendDistribution.findPercentageSumByRotatoryFundCollectionButCurrent", query = "select sum(o.percentage) from RotatoryFundCollectionSpendDistribution o where o.rotatoryFundCollection=:rotatoryFundCollection " +
                        "and o.id<>:id")
        }
)

@TableGenerator(schema = Constants.KHIPUS_SCHEMA, name = "RotatoryFundCollectionSpendDistribution.tableGenerator",
        table = com.encens.khipus.util.Constants.SEQUENCE_TABLE_NAME,
        pkColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_PK_COLUMN_NAME,
        valueColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_VALUE_COLUMN_NAME,
        pkColumnValue = "DISTRIBUCIONGASTOCOBROFON",
        allocationSize = com.encens.khipus.util.Constants.SEQUENCE_ALLOCATION_SIZE)

@Entity
@Filter(name = com.encens.khipus.util.Constants.COMPANY_FILTER_NAME)
@EntityListeners({CompanyListener.class, CompanyNumberListener.class, UpperCaseStringListener.class})
@Table(schema = Constants.KHIPUS_SCHEMA, name = "DISTRIBUCIONGASTOCOBROFON")
public class RotatoryFundCollectionSpendDistribution implements BaseModel {

    @Id
    @Column(name = "IDDISTRIBUCIONGASTOCOBROFON", nullable = false, scale = 24)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "RotatoryFundCollectionSpendDistribution.tableGenerator")
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

    @Column(name = "CUENTA", length = 20)
    @Length(max = 20)
    private String accountCode;

    @ManyToOne(optional = true)
    @JoinColumns({
            @JoinColumn(name = "NUMEROCOMPANIA", referencedColumnName = "NO_CIA", updatable = false, insertable = false),
            @JoinColumn(name = "CUENTA", referencedColumnName = "CUENTA", updatable = false, insertable = false)
    })
    private CashAccount cashAccount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "IDCOBRO", referencedColumnName = "IDCOBRO")
    private RotatoryFundCollection rotatoryFundCollection;

    @Column(name = "NUMEROCOMPANIA", updatable = false)
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

    public RotatoryFundCollection getRotatoryFundCollection() {
        return rotatoryFundCollection;
    }

    public void setRotatoryFundCollection(RotatoryFundCollection rotatoryFundCollection) {
        this.rotatoryFundCollection = rotatoryFundCollection;
    }

    public Boolean hasValues() {
        return getCostCenter() != null || getCashAccount() != null || getAmount() != null;
    }

    public Boolean isValid() {
        return getBusinessUnit() != null && getCostCenter() != null && getCashAccount() != null && getAmount() != null;
    }


}