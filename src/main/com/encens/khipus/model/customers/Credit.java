package com.encens.khipus.model.customers;

import com.encens.khipus.model.BaseModel;
import com.encens.khipus.model.CompanyListener;
import com.encens.khipus.model.admin.Company;
import com.encens.khipus.model.finances.FinancesCurrencyType;
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
 * Entity for credits
 *
 * @author:
 */

@NamedQueries(
        {
                @NamedQuery(name = "Credit.findAllCredits", query = "select c from Credit c where c.capitalBalance > 0"),
                @NamedQuery(name = "Credit.findUnfinishedCredits", query = "select c from Credit c where c.state <> :creditStateFIN"),
                @NamedQuery(name = "Credit.findAllCreditsAll", query = "select c from Credit c"),
                @NamedQuery(name = "Credit.findCreditsByStateAndType", query = "select c from Credit c where c.state =:creditState and c.creditType =:creditType"),
                @NamedQuery(name = "Credit.findCreditById", query = "select c from Credit c where c.id=:creditId")
        }
)
@TableGenerator(schema = com.encens.khipus.util.Constants.KHIPUS_SCHEMA, name = "Credit.tableGenerator",
        table = com.encens.khipus.util.Constants.SEQUENCE_TABLE_NAME,
        pkColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_PK_COLUMN_NAME,
        valueColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_VALUE_COLUMN_NAME,
        pkColumnValue = "credito",
        allocationSize = com.encens.khipus.util.Constants.SEQUENCE_ALLOCATION_SIZE)

@javax.persistence.Entity
@Filter(name = com.encens.khipus.util.Constants.COMPANY_FILTER_NAME)
@EntityListeners(CompanyListener.class)
@Table(schema = com.encens.khipus.util.Constants.KHIPUS_SCHEMA, name = "credito")
public class Credit implements BaseModel {

    @Id
    @Column(name = "idcredito", nullable = false)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "Credit.tableGenerator")
    private Long id;

    @Column(name = "estado", nullable = true, length = 3)
    @Enumerated(EnumType.STRING)
    private CreditState state;

    @Column(name = "ue", nullable = true, length = 3)
    @Enumerated(EnumType.STRING)
    private CreditState lastState;

    @Column(name = "codigo", nullable = false, length = 25)
    @Length(max = 25)
    private String code;

    @Column(name = "codigoant", nullable = false, length = 15)
    @Length(max = 15)
    private String previousCode;

    @Column(name = "importe", precision = 13, scale = 2, nullable = false)
    private BigDecimal amount;

    @Column(name = "anual", nullable = false)
    private Integer annualRate;

    @Column(name = "ipenal", nullable = false)
    private BigDecimal criminalInterest;

    @Column(name = "plazo", nullable = false)
    private Integer term;

    @Column(name = "cuotas", nullable = false)
    private Integer numberQuota;

    @Column(name = "amortiza", nullable = false)
    private Integer amortization;

    @Column(name = "fechaconcesion", nullable = false)
    @Temporal(TemporalType.DATE)
    private Date grantDate;

        @Column(name = "fechapago", nullable = false)
        @Temporal(TemporalType.DATE)
        private Date firstPayment;

    @Column(name = "cuota", precision = 13, scale = 2, nullable = false)
    private BigDecimal quota;

    @Column(name = "saldo", precision = 13, scale = 2, nullable = false)
    private BigDecimal capitalBalance;

    @Column(name = "fechacreacion", nullable = false, updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date creationDate = new Date();

    @Column(name = "ultimopago", nullable = true)
    @Temporal(TemporalType.DATE)
    private Date lastPayment;

    @Column(name = "fechavence", nullable = true)
    @Temporal(TemporalType.DATE)
    private Date expirationDate;

    @Column(name = "entregado", nullable = false)
    @Type(type = com.encens.khipus.model.usertype.IntegerBooleanUserType.NAME)
    private Boolean delivered = Boolean.FALSE;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idtipocredito", referencedColumnName = "idtipocredito", nullable = true)
    private CreditType creditType;

    @OneToMany(mappedBy = "credit", fetch = FetchType.LAZY)
    private List<CreditTransaction> creditTransactionList = new ArrayList<CreditTransaction>(0);

    @Version
    @Column(name = "version", nullable = false)
    private long version;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "idcompania", nullable = false, updatable = false, insertable = true)
    @NotNull
    private Company company;

    /*@OneToOne(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "identidad", nullable = true)
    private Entity entity;*/

    @ManyToOne(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "idsocio", nullable = false)
    private Partner partner;

    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    /*public Entity getEntity() {
        return entity;
    }

    public void setEntity(Entity entity) {
        this.entity = entity;
    }*/

    public Partner getPartner() {
        return partner;
    }

    public void setPartner(Partner partner) {
        this.partner = partner;
    }

    public Integer getAnnualRate() {
        return annualRate;
    }

    public void setAnnualRate(Integer annualRate) {
        this.annualRate = annualRate;
    }

    public Integer getTerm() {
        return term;
    }

    public void setTerm(Integer term) {
        this.term = term;
    }

    public Integer getAmortization() {
        return amortization;
    }

    public void setAmortization(Integer amortization) {
        this.amortization = amortization;
    }

    public Date getGrantDate() {
        return grantDate;
    }

    public void setGrantDate(Date grantDate) {
        this.grantDate = grantDate;
    }

    public Date getFirstPayment() {
        return firstPayment;
    }

    public void setFirstPayment(Date firstPayment) {
        this.firstPayment = firstPayment;
    }

    public BigDecimal getQuota() {
        return quota;
    }

    public void setQuota(BigDecimal quota) {
        this.quota = quota;
    }

    public CreditState getState() {
        return state;
    }

    public void setState(CreditState state) {
        this.state = state;
    }

    public BigDecimal getCapitalBalance() {
        return capitalBalance;
    }

    public void setCapitalBalance(BigDecimal capitalBalance) {
        this.capitalBalance = capitalBalance;
    }

    public Boolean getDelivered() {
        return delivered;
    }

    public void setDelivered(Boolean delivered) {
        this.delivered = delivered;
    }

    public String getPreviousCode() {
        return previousCode;
    }

    public void setPreviousCode(String previousCode) {
        this.previousCode = previousCode;
    }

    public Integer getNumberQuota() {
        return numberQuota;
    }

    public void setNumberQuota(Integer numberQuota) {
        this.numberQuota = numberQuota;
    }

    public CreditType getCreditType() {
        return creditType;
    }

    public void setCreditType(CreditType creditType) {
        this.creditType = creditType;
    }

    public Date getLastPayment() {
        return lastPayment;
    }

    public void setLastPayment(Date lastPayment) {
        this.lastPayment = lastPayment;
    }

    public BigDecimal getCriminalInterest() {
        return criminalInterest;
    }

    public void setCriminalInterest(BigDecimal criminalInterest) {
        this.criminalInterest = criminalInterest;
    }

    public Date getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(Date expirationDate) {
        this.expirationDate = expirationDate;
    }

    public CreditState getLastState() {
        return lastState;
    }

    public void setLastState(CreditState lastState) {
        this.lastState = lastState;
    }

    public FinancesCurrencyType getFinancesCurrencyType(){
        FinancesCurrencyType result = FinancesCurrencyType.P;
        if (getCreditType().getCurrency().getSymbol().equals("USD"))
            result = FinancesCurrencyType.D;

        return result;
    }


    public List<CreditTransaction> getCreditTransactionList() {
        return creditTransactionList;
    }

    public void setCreditTransactionList(List<CreditTransaction> creditTransactionList) {
        this.creditTransactionList = creditTransactionList;
    }
}
