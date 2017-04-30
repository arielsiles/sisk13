package com.encens.khipus.model.finances;

import com.encens.khipus.model.BaseModel;
import com.encens.khipus.model.UpperCaseStringListener;
import com.encens.khipus.util.Constants;
import org.hibernate.validator.Length;

import javax.persistence.*;

/**
 * FinancesBankAccount
 *
 * @author
 * @version 2.0
 */
@NamedQueries({
        @NamedQuery(name = "FinancesBankAccount.findByCurrencyType", query = "select f from FinancesBankAccount f where f.state='VIG' and f.currency=:currency"),
        @NamedQuery(name = "FinancesBankAccount.findByAccountNumber", query = "select f from FinancesBankAccount f where f.state='VIG' and f.id.accountNumber=:accountNumber")
})
@Entity
@EntityListeners(UpperCaseStringListener.class)
@Table(name = "ck_ctas_bco", schema = Constants.FINANCES_SCHEMA)
public class FinancesBankAccount implements BaseModel {

    @EmbeddedId
    private FinancesBankAccountPk id = new FinancesBankAccountPk();

    @Column(name = "NO_CIA", nullable = false, updatable = false, insertable = false)
    private String companyNumber;

    @Column(name = "CTA_BCO", nullable = false, updatable = false, insertable = false)
    private String accountNumber;

    @Column(name = "DESCRI", length = 100)
    @Length(max = 100)
    private String description;

    @Column(name = "COD_BCO", length = 6)
    @Length(max = 6)
    private String bankCode;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "COD_BCO", nullable = false, updatable = false, insertable = false)
    private FinancesBank financesBank;

    @Column(name = "CUENTA", length = 31)
    @Length(max = 31)
    private String accountingAccountCode;

    @ManyToOne(optional = true)
    @JoinColumns({
            @JoinColumn(name = "NO_CIA", referencedColumnName = "NO_CIA", updatable = false, insertable = false),
            @JoinColumn(name = "CUENTA", referencedColumnName = "CUENTA", updatable = false, insertable = false)
    })
    private CashAccount cashAccount;

    @Column(name = "MONEDA")
    @Enumerated(EnumType.STRING)
    private FinancesCurrencyType currency;

    @Column(name = "ESTADO", length = 3)
    @Length(max = 3)
    private String state;

    public FinancesBankAccountPk getId() {
        return id;
    }

    public void setId(FinancesBankAccountPk id) {
        this.id = id;
    }

    public String getCompanyNumber() {
        return companyNumber;
    }

    public void setCompanyNumber(String companyNumber) {
        this.companyNumber = companyNumber;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getBankCode() {
        return bankCode;
    }

    public void setBankCode(String bankCode) {
        this.bankCode = bankCode;
    }

    public String getAccountingAccountCode() {
        return accountingAccountCode;
    }

    public void setAccountingAccountCode(String accountingAccountCode) {
        this.accountingAccountCode = accountingAccountCode;
    }

    public FinancesCurrencyType getCurrency() {
        return currency;
    }

    public void setCurrency(FinancesCurrencyType currency) {
        this.currency = currency;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public Boolean isNationalCurrency() {
        return FinancesCurrencyType.P.equals(getState());
    }

    public CashAccount getCashAccount() {
        return cashAccount;
    }

    public void setCashAccount(CashAccount cashAccount) {
        this.cashAccount = cashAccount;
        setAccountingAccountCode(cashAccount != null ? cashAccount.getAccountCode() : null);
    }

    public FinancesBank getFinancesBank() {
        return financesBank;
    }

    public void setFinancesBank(FinancesBank financesBank) {
        this.financesBank = financesBank;
    }

    public String getFullName() {
        return getAccountNumber() + " - " + getDescription();
    }

    @Override
    public String toString() {
        return "FinancesBankAccount{" +
                "id=" + id +
                ", companyNumber='" + companyNumber + '\'' +
                ", accountNumber='" + accountNumber + '\'' +
                ", description='" + description + '\'' +
                ", bankCode='" + bankCode + '\'' +
                ", accountingAccountCode='" + accountingAccountCode + '\'' +
                ", currency='" + currency + '\'' +
                ", state='" + state + '\'' +
                '}';
    }
}


