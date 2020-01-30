package com.encens.khipus.model.customers;

import com.encens.khipus.model.BaseModel;
import com.encens.khipus.model.CompanyListener;
import com.encens.khipus.model.admin.Company;
import com.encens.khipus.model.finances.Voucher;
import com.encens.khipus.model.usertype.IntegerBooleanUserType;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.Type;
import org.hibernate.validator.NotNull;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * Entity for transaction credit
 *
 * @author:
 */

@NamedQueries(
        {
                @NamedQuery(name = "CreditTransaction.transactions", query = "select ct from CreditTransaction ct where ct.credit=:credit"),
                @NamedQuery(name = "CreditTransaction.transactionsBefore", query = "select ct from CreditTransaction ct where ct.credit=:credit and ct.date <=:date"),
                @NamedQuery(name = "CreditTransaction.findIncomePayments", query = "select ct from CreditTransaction ct where ct.credit=:credit and ct.creditTransactionType=:creditTransactionType"),
                @NamedQuery(name = "CreditTransaction.findLastTransaction", query = "select max(ct.date) " +
                                                                                    "from CreditTransaction ct " +
                                                                                    "where ct.credit=:credit " +
                                                                                    "and ct.creditTransactionType=:transactionType"),
                @NamedQuery(name = "CreditTransaction.findLastTransactionEndPeriod", query =    "select max(ct.date) " +
                                                                                                "from CreditTransaction ct " +
                                                                                                "where ct.credit=:credit " +
                                                                                                "and ct.creditTransactionType=:transactionType " +
                                                                                                "and ct.date <=:endPeriod "),
                @NamedQuery(name = "CreditTransaction.findLastTransactionBeforeDate", query =    "select max(ct.date) " +
                                                                                                "from CreditTransaction ct " +
                                                                                                "where ct.credit=:credit " +
                                                                                                "and ct.date <=:date ")
        }
)
@TableGenerator(schema = com.encens.khipus.util.Constants.KHIPUS_SCHEMA, name = "CreditTransaction.tableGenerator",
        table = com.encens.khipus.util.Constants.SEQUENCE_TABLE_NAME,
        pkColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_PK_COLUMN_NAME,
        valueColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_VALUE_COLUMN_NAME,
        pkColumnValue = "transaccioncredito",
        allocationSize = com.encens.khipus.util.Constants.SEQUENCE_ALLOCATION_SIZE)
@Entity
@Filter(name = com.encens.khipus.util.Constants.COMPANY_FILTER_NAME)
@EntityListeners(CompanyListener.class)
@Table(schema = com.encens.khipus.util.Constants.KHIPUS_SCHEMA, name = "transaccioncredito")
public class CreditTransaction implements Serializable, BaseModel {

    @Id
    @Column(name = "idtransaccioncredito", nullable = false)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "CreditTransaction.tableGenerator")
    private Long id;

    @Column(name = "capital", precision = 13, scale = 2, nullable = false)
    private BigDecimal capital;

    @Column(name = "interes", precision = 13, scale = 2, nullable = false)
    private BigDecimal interest;

    @Column(name = "interespenal", precision = 13, scale = 2, nullable = false)
    private BigDecimal criminalInterest;

    @Column(name = "importe", precision = 13, scale = 2, nullable = false)
    private BigDecimal amount;

    @Column(name = "diff", precision = 13, scale = 2)
    private BigDecimal difference;

    @Column(name = "glosa", length = 255)
    private String gloss;

    @Column(name = "fechatransaccion", nullable = false, updatable = false)
    @Temporal(TemporalType.DATE)
    private Date date;

    @Column(name = "fechacreacion", nullable = false, updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date creationDate = new Date();

    @Column(name = "dias", nullable = true)
    private Integer days;

    @Column(name = "saldo", precision = 13, scale = 2, nullable = false)
    private BigDecimal capitalBalance;

    @Column(name = "tipo", nullable = true, length = 1)
    @Enumerated(EnumType.STRING)
    private CreditTransactionType creditTransactionType;

    @Column(name = "traspaso", nullable = false)
    @Type(type = IntegerBooleanUserType.NAME)
    private Boolean transfer = Boolean.FALSE;

    @ManyToOne(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "idcredito", nullable = false)
    private Credit credit;

    /*@OneToOne(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "idfactura", nullable = false)
    private Invoice invoice;*/

    @ManyToOne
    @JoinColumn(name = "id_tmpenc", nullable = true)
    private Voucher voucher;

    @Column(name = "id_tmpenc", nullable = true, updatable = false, insertable = false)
    private Long voucherId;

    @Version
    @Column(name = "version", nullable = false)
    private long version;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "idcompania", nullable = false, updatable = false, insertable = true)
    @NotNull
    private Company company;

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

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Credit getCredit() {
        return credit;
    }

    public void setCredit(Credit credit) {
        this.credit = credit;
    }

    /*public Invoice getInvoice() {
        return invoice;
    }

    public void setInvoice(Invoice invoice) {
        this.invoice = invoice;
    }*/

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public String getGloss() {
        return gloss;
    }

    public void setGloss(String gloss) {
        this.gloss = gloss;
    }

    public BigDecimal getCapital() {
        return capital;
    }

    public void setCapital(BigDecimal capital) {
        this.capital = capital;
    }

    public BigDecimal getInterest() {
        return interest;
    }

    public void setInterest(BigDecimal interest) {
        this.interest = interest;
    }

    public Integer getDays() {
        return days;
    }

    public void setDays(Integer days) {
        this.days = days;
    }

    public BigDecimal getCapitalBalance() {
        return capitalBalance;
    }

    public void setCapitalBalance(BigDecimal capitalBalance) {
        this.capitalBalance = capitalBalance;
    }

    public CreditTransactionType getCreditTransactionType() {
        return creditTransactionType;
    }

    public void setCreditTransactionType(CreditTransactionType creditTransactionType) {
        this.creditTransactionType = creditTransactionType;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public Voucher getVoucher() {
        return voucher;
    }

    public void setVoucher(Voucher voucher) {
        this.voucher = voucher;
    }

    public Long getVoucherId() {
        return voucherId;
    }

    public void setVoucherId(Long voucherId) {
        this.voucherId = voucherId;
    }

    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
    }

    public BigDecimal getCriminalInterest() {
        return criminalInterest;
    }

    public void setCriminalInterest(BigDecimal criminalInterest) {
        this.criminalInterest = criminalInterest;
    }

    public Boolean getTransfer() {
        return transfer;
    }

    public void setTransfer(Boolean transfer) {
        this.transfer = transfer;
    }

    public BigDecimal getDifference() {
        return difference;
    }

    public void setDifference(BigDecimal difference) {
        this.difference = difference;
    }
}
