package com.encens.khipus.model.finances;

import com.encens.khipus.model.BaseModel;
import com.encens.khipus.model.CompanyListener;
import com.encens.khipus.model.UpperCaseStringListener;
import com.encens.khipus.model.admin.Company;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.Type;
import org.hibernate.validator.NotNull;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;

/**
 * Entity for Quota
 *
 * @author
 * @version 2.14
 */

@NamedQueries(
        {
                @NamedQuery(name = "Quota.findAll", query = "select o from Quota o "),
                @NamedQuery(name = "Quota.findByRotatoryFund", query = "select o from Quota o where o.rotatoryFund=:rotatoryFund order by o.expirationDate, o.amount"),
                @NamedQuery(name = "Quota.findByRotatoryFundByState", query = "select o from Quota o where o.rotatoryFund=:rotatoryFund and o.state=:state order by o.expirationDate, o.amount"),
                @NamedQuery(name = "Quota.approvePendantQuotaList", query = "update Quota o set o.state=:state where o.rotatoryFund=:rotatoryFund and o.state=:quotaState "),
                @NamedQuery(name = "Quota.findMaxDateByRotatoryFund", query = "select max(o.expirationDate) from Quota o where o.rotatoryFund=:rotatoryFund and o.state<>:quotaState "),
                @NamedQuery(name = "Quota.findMinDateByRotatoryFund", query = "select min(o.expirationDate) from Quota o where o.rotatoryFund=:rotatoryFund and o.state<>:quotaState "),
                @NamedQuery(name = "Quota.findSumByRotatoryFundByState", query = "select sum(o.amount) from Quota o where o.rotatoryFund=:rotatoryFund and o.state=:state "),
                @NamedQuery(name = "Quota.findResidueSumByRotatoryFund", query = "select sum(o.residue) from Quota o where o.rotatoryFund=:rotatoryFund"),
                @NamedQuery(name = "Quota.checkCurrency", query = "select count(o) from Quota o where o.rotatoryFund=:rotatoryFund and o.currency<>:currency"),
                @NamedQuery(name = "Quota.findSumByRotatoryFundButCurrent", query = "select sum(o.amount) from Quota o where o.rotatoryFund=:rotatoryFund " +
                        "and o.id<>:id"),
                @NamedQuery(name = "Quota.isQuotaInfoStillValid", query = "select o from Quota o where o.id=:id " +
                        "and o.residue=:residue "),
                @NamedQuery(name = "Quota.findQuotaToCollectByPayrollEmployeeAndJobCategory",
                        query = "select q " +
                                " from Quota q" +
                                "  left join fetch q.rotatoryFund rf " +
                                "  left join fetch rf.documentType documentType" +
                                "  left join fetch rf.jobContract jobContract" +
                                "  left join fetch jobContract.job job" +
                                "  left join fetch job.jobCategory jobCategory" +
                                "  left join fetch jobContract.contract contract" +
                                "  left join fetch contract.employee employee" +
                                " where jobCategory=:jobCategory" +
                                " and employee=:employee" +
                                " and rf.state=:rotatoryFunState " +
                                " and q.discountByPayroll=:discountByPayroll " +
                                " and (q.state=:quotaApprovedSate or q.state=:quotaPartiallyLiquidatedSate) " +
                                " and q.expirationDate <=:gestionPayrollEndDate order by q.expirationDate, q.amount"),
                @NamedQuery(name = "Quota.sumResidueToCollectByPayrollEmployeeAndJobCategory",
                        query = "select sum(q.residue) " +
                                " from Quota q" +
                                "  left join q.rotatoryFund rf " +
                                "  left join rf.jobContract jobContract" +
                                "  left join jobContract.job job" +
                                "  left join job.jobCategory jobCategory" +
                                "  left join jobContract.contract contract" +
                                "  left join contract.employee employee" +
                                " where jobCategory=:jobCategory" +
                                " and employee=:employee" +
                                " and rf.state=:rotatoryFunState " +
                                " and q.discountByPayroll=:discountByPayroll " +
                                " and (q.state=:quotaApprovedSate or q.state=:quotaPartiallyLiquidatedSate) " +
                                " and q.expirationDate <=:gestionPayrollEndDate")
        }
)

@TableGenerator(schema = com.encens.khipus.util.Constants.KHIPUS_SCHEMA, name = "Quota.tableGenerator",
        table = com.encens.khipus.util.Constants.SEQUENCE_TABLE_NAME,
        pkColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_PK_COLUMN_NAME,
        valueColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_VALUE_COLUMN_NAME,
        pkColumnValue = "CUOTA",
        allocationSize = com.encens.khipus.util.Constants.SEQUENCE_ALLOCATION_SIZE)

@Entity
@Filter(name = com.encens.khipus.util.Constants.COMPANY_FILTER_NAME)
@EntityListeners({CompanyListener.class, UpperCaseStringListener.class})
@Table(schema = com.encens.khipus.util.Constants.KHIPUS_SCHEMA, name = "CUOTA")
public class Quota implements BaseModel {

    @Id
    @Column(name = "IDCUOTA", nullable = false, scale = 24)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "Quota.tableGenerator")
    private Long id;

    @Column(name = "CANTIDAD", nullable = false, precision = 13, scale = 2)
    private BigDecimal amount;

    @Column(name = "SALDO", precision = 13, scale = 2)
    private BigDecimal residue;

    @Column(name = "MONEDA", length = 20)
    @Enumerated(EnumType.STRING)
    private FinancesCurrencyType currency;

    @Column(name = "TIPOCAMBIO", precision = 16, scale = 6)
    private BigDecimal exchangeRate;

    @Column(name = "FECHAVENCIMIENTO")
    @Temporal(TemporalType.DATE)
    private Date expirationDate;

    @Column(name = "DESCRIPCION", length = 250)
    private String description;

    @Column(name = "ESTADO", nullable = false)
    @Enumerated(EnumType.STRING)
    @NotNull
    private QuotaState state;

    @Column(name = "PORPLANILLA")
    @Type(type = com.encens.khipus.model.usertype.IntegerBooleanUserType.NAME)
    private Boolean discountByPayroll;

    @ManyToOne
    @JoinColumn(name = "IDFONDOROTATORIO")
    private RotatoryFund rotatoryFund;

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

    public BigDecimal getExchangeRate() {
        return exchangeRate;
    }

    public void setExchangeRate(BigDecimal exchangeRate) {
        this.exchangeRate = exchangeRate;
    }

    public QuotaState getState() {
        return state;
    }

    public void setState(QuotaState state) {
        this.state = state;
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

    public Date getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(Date expirationDate) {
        this.expirationDate = expirationDate;
    }

    public FinancesCurrencyType getCurrency() {
        return currency;
    }

    public void setCurrency(FinancesCurrencyType currency) {
        this.currency = currency;
    }

    public RotatoryFund getRotatoryFund() {
        return rotatoryFund;
    }

    public void setRotatoryFund(RotatoryFund rotatoryFund) {
        this.rotatoryFund = rotatoryFund;
    }

    public BigDecimal getResidue() {
        return residue;
    }

    public void setResidue(BigDecimal residue) {
        this.residue = residue;
    }
}