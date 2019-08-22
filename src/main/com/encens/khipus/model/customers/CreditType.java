package com.encens.khipus.model.customers;

import com.encens.khipus.model.BaseModel;
import com.encens.khipus.model.employees.Currency;
import com.encens.khipus.model.finances.CashAccount;

import javax.persistence.*;

/**
 * Entity for Document Types
 *
 * @author:
 * version: 2.7
 */

@TableGenerator(schema = com.encens.khipus.util.Constants.KHIPUS_SCHEMA, name = "CreditType.tableGenerator",
        table = com.encens.khipus.util.Constants.SEQUENCE_TABLE_NAME,
        pkColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_PK_COLUMN_NAME,
        valueColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_VALUE_COLUMN_NAME,
        pkColumnValue = "tipocredito",
        allocationSize = com.encens.khipus.util.Constants.SEQUENCE_ALLOCATION_SIZE)

@NamedQueries(
        {
                //@NamedQuery(name = "DocumentType.findDocumentTypeDefault", query = "select o from DocumentType o where o.id=:id")
        }
)

@Entity
@Table(schema = com.encens.khipus.util.Constants.KHIPUS_SCHEMA, name = "tipocredito")
public class CreditType implements BaseModel {
    @Id
    @Column(name = "idtipocredito", nullable = false)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "CreditType.tableGenerator")
    private Long id;

    @Column(name = "nombre", nullable = false, length = 150)
    private String name;

    @Column(name = "ctavig")
    private String currentAccountCode;

    @Column(name = "ctaven")
    private String expiredAccountCode;

    @Column(name = "ctaeje")
    private String executedAccountCode;

    @Column(name = "ictavig")
    private String currentInterestAccountCode;

    @Column(name = "ictaven")
    private String expiredInterestAccountCode;

    @Column(name = "ictaeje")
    private String executedInterestAccountCode;

    @Column(name = "ipctaeje")
    private String criminalInterestAccountCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ctavig", insertable = false, updatable = false, referencedColumnName = "cuenta")
    private CashAccount currentAccount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ctaven", insertable = false, updatable = false, referencedColumnName = "cuenta")
    private CashAccount expiredAccount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ctaeje", insertable = false, updatable = false, referencedColumnName = "cuenta")
    private CashAccount executedAccount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ictavig", insertable = false, updatable = false, referencedColumnName = "cuenta")
    private CashAccount currentInterestAccount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ictaven", insertable = false, updatable = false, referencedColumnName = "cuenta")
    private CashAccount expiredInterestAccount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ictaeje", insertable = false, updatable = false, referencedColumnName = "cuenta")
    private CashAccount executedInterestAccount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ipctaeje", insertable = false, updatable = false, referencedColumnName = "cuenta")
    private CashAccount criminalInterestAccount;

    @Version
    @Column(name = "version", nullable = false)
    private long version;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idmoneda")
    private Currency currency;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
    }

    public String getCurrentAccountCode() {
        return currentAccountCode;
    }

    public void setCurrentAccountCode(String currentAccountCode) {
        this.currentAccountCode = currentAccountCode;
    }

    public String getExpiredAccountCode() {
        return expiredAccountCode;
    }

    public void setExpiredAccountCode(String expiredAccountCode) {
        this.expiredAccountCode = expiredAccountCode;
    }

    public CashAccount getCurrentAccount() {
        return currentAccount;
    }

    public void setCurrentAccount(CashAccount currentAccount) {
        this.currentAccount = currentAccount;
        setCurrentAccountCode(this.currentAccount != null ? this.currentAccount.getAccountCode() : null);
    }

    public String getExecutedAccountCode() {
        return executedAccountCode;
    }

    public void setExecutedAccountCode(String executedAccountCode) {
        this.executedAccountCode = executedAccountCode;
    }

    public CashAccount getExecutedAccount() {
        return executedAccount;
    }

    public void setExecutedAccount(CashAccount executedAccount) {
        this.executedAccount = executedAccount;
        setExecutedAccountCode(this.executedAccount != null ? this.executedAccount.getAccountCode() : null);
    }

    public CashAccount getExpiredAccount() {
        return expiredAccount;
    }

    public void setExpiredAccount(CashAccount expiredAccount) {
        this.expiredAccount = expiredAccount;
        setExpiredAccountCode(this.expiredAccount != null ? this.expiredAccount.getAccountCode() : null);
    }


    public String getFullName(){
        return  currency.getSymbol() + " - " + getName();
    }

    public Currency getCurrency() {
        return currency;
    }

    public void setCurrency(Currency currency) {
        this.currency = currency;
    }

    public String getCurrentInterestAccountCode() {
        return currentInterestAccountCode;
    }

    public void setCurrentInterestAccountCode(String currentInterestAccountCode) {
        this.currentInterestAccountCode = currentInterestAccountCode;
    }

    public String getExpiredInterestAccountCode() {
        return expiredInterestAccountCode;
    }

    public void setExpiredInterestAccountCode(String expiredInterestAccountCode) {
        this.expiredInterestAccountCode = expiredInterestAccountCode;
    }

    public String getExecutedInterestAccountCode() {
        return executedInterestAccountCode;
    }

    public void setExecutedInterestAccountCode(String executedInterestAccountCode) {
        this.executedInterestAccountCode = executedInterestAccountCode;
    }

    public CashAccount getCurrentInterestAccount() {
        return currentInterestAccount;
    }

    public void setCurrentInterestAccount(CashAccount currentInterestAccount) {
        this.currentInterestAccount = currentInterestAccount;
        setCurrentInterestAccountCode(this.currentInterestAccount != null ? this.currentInterestAccount.getAccountCode() : null);
    }

    public CashAccount getExpiredInterestAccount() {
        return expiredInterestAccount;
    }

    public void setExpiredInterestAccount(CashAccount expiredInterestAccount) {
        this.expiredInterestAccount = expiredInterestAccount;
        setExpiredInterestAccountCode(this.expiredInterestAccount != null ? this.expiredInterestAccount.getAccountCode() : null);
    }

    public CashAccount getExecutedInterestAccount() {
        return executedInterestAccount;
    }

    public void setExecutedInterestAccount(CashAccount executedInterestAccount) {
        this.executedInterestAccount = executedInterestAccount;
        setExecutedInterestAccountCode(this.executedInterestAccount != null ? this.executedInterestAccount.getAccountCode() : null);
    }

    public String getCriminalInterestAccountCode() {
        return criminalInterestAccountCode;
    }

    public void setCriminalInterestAccountCode(String criminalInterestAccountCode) {
        this.criminalInterestAccountCode = criminalInterestAccountCode;
    }

    public CashAccount getCriminalInterestAccount() {
        return criminalInterestAccount;
    }

    public void setCriminalInterestAccount(CashAccount criminalInterestAccount) {
        this.criminalInterestAccount = criminalInterestAccount;
    }
}
