package com.encens.khipus.model.customers;

import com.encens.khipus.model.BaseModel;
import com.encens.khipus.model.CompanyListener;
import com.encens.khipus.model.admin.Company;
import com.encens.khipus.model.finances.FinancesCurrencyType;
import org.hibernate.annotations.Filter;
import org.hibernate.validator.Length;
import org.hibernate.validator.NotNull;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;

/**
 * Entity for accounts
 *
 * @author:
 */

@NamedQueries(
        {

        }
)
@TableGenerator(schema = com.encens.khipus.util.Constants.KHIPUS_SCHEMA, name = "Account.tableGenerator",
        table = com.encens.khipus.util.Constants.SEQUENCE_TABLE_NAME,
        pkColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_PK_COLUMN_NAME,
        valueColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_VALUE_COLUMN_NAME,
        pkColumnValue = "cuenta",
        allocationSize = com.encens.khipus.util.Constants.SEQUENCE_ALLOCATION_SIZE)

@Entity
@Filter(name = com.encens.khipus.util.Constants.COMPANY_FILTER_NAME)
@EntityListeners(CompanyListener.class)
@Table(schema = com.encens.khipus.util.Constants.KHIPUS_SCHEMA, name = "cuenta")
public class Account implements BaseModel {

    @Id
    @Column(name = "idcuenta", nullable = false)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "Account.tableGenerator")
    private Long id;

    @Column(name = "nocuenta", nullable = false, length = 20)
    @Length(max = 20)
    private String accountNumber;

    @Column(name = "moneda", updatable = false)
    @Enumerated(EnumType.STRING)
    private FinancesCurrencyType currency;

    @Column(name = "estado", updatable = false)
    @Enumerated(EnumType.STRING)
    private AccountState accountState;

    @Column(name = "saldo", precision = 13, scale = 2, nullable = false)
    private BigDecimal balance = new BigDecimal(0);

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idsocio", nullable = false)
    private Partner partner;

    @ManyToOne
    @JoinColumn(name = "idtipocuenta", nullable = false)
    private AccountType accountType;

    @Version
    @Column(name = "version", nullable = false)
    private long version;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "idcompania", nullable = false, updatable = false, insertable = true)
    @NotNull
    private Company company;


    @Override
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public FinancesCurrencyType getCurrency() {
        return currency;
    }

    public void setCurrency(FinancesCurrencyType currency) {
        this.currency = currency;
    }

    public AccountState getAccountState() {
        return accountState;
    }

    public void setAccountState(AccountState accountState) {
        this.accountState = accountState;
    }

    public Partner getPartner() {
        return partner;
    }

    public void setPartner(Partner partner) {
        this.partner = partner;
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

    public AccountType getAccountType() {
        return accountType;
    }

    public void setAccountType(AccountType accountType) {
        this.accountType = accountType;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public String getFullAccountName(){
        return getAccountNumber() + " - " + partner.getFullName();
    }

}
