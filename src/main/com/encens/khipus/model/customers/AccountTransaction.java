package com.encens.khipus.model.customers;

import com.encens.khipus.model.BaseModel;
import com.encens.khipus.model.CompanyListener;
import com.encens.khipus.model.admin.Company;
import org.hibernate.annotations.Filter;
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
                /*@NamedQuery(name = "CreditTransaction.transactions", query = "select ct from CreditTransaction ct where ct.credit=:credit"),
                @NamedQuery(name = "CreditTransaction.findLastTransaction", query = "select max(ct.date) from CreditTransaction ct where ct.credit=:credit")*/
        }
)
@TableGenerator(schema = com.encens.khipus.util.Constants.KHIPUS_SCHEMA, name = "AccountTransaction.tableGenerator",
        table = com.encens.khipus.util.Constants.SEQUENCE_TABLE_NAME,
        pkColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_PK_COLUMN_NAME,
        valueColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_VALUE_COLUMN_NAME,
        pkColumnValue = "transaccioncuenta",
        allocationSize = com.encens.khipus.util.Constants.SEQUENCE_ALLOCATION_SIZE)
@Entity
@Filter(name = com.encens.khipus.util.Constants.COMPANY_FILTER_NAME)
@EntityListeners(CompanyListener.class)
@Table(schema = com.encens.khipus.util.Constants.KHIPUS_SCHEMA, name = "transaccioncuenta")
public class AccountTransaction implements Serializable, BaseModel {

    @Id
    @Column(name = "idtransaccioncuenta", nullable = false)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "AccountTransaction.tableGenerator")
    private Long id;

    @Column(name = "importe", precision = 13, scale = 2, nullable = false)
    private BigDecimal amount;

    @Column(name = "glosa", length = 255)
    private String gloss;

    @Column(name = "fechatransaccion", nullable = false, updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date date = new Date();

    @Column(name = "tipotrans", updatable = false)
    @Enumerated(EnumType.STRING)
    private AccountTransactionType transactionType;

    @ManyToOne(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "idcuenta", nullable = false)
    private Account account;

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

    public String getGloss() {
        return gloss;
    }

    public void setGloss(String gloss) {
        this.gloss = gloss;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public AccountTransactionType getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(AccountTransactionType transactionType) {
        this.transactionType = transactionType;
    }

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }

    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }
}
