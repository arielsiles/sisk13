package com.encens.khipus.model.finances;

import org.hibernate.validator.Length;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;

/**
 * FinancesBankAccountPk
 *
 * @author
 * @version 2.0
 */
@Embeddable
public class FinancesBankAccountPk implements Serializable {
    @Column(name = "NO_CIA", nullable = false, updatable = false)
    private String companyNumber;

    @Column(name = "CTA_BCO", length = 20, nullable = false, updatable = false)
    @Length(max = 20)
    private String accountNumber;

    public FinancesBankAccountPk() {
    }

    public FinancesBankAccountPk(String companyNumber, String accountNumber) {
        this.companyNumber = companyNumber;
        this.accountNumber = accountNumber;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        FinancesBankAccountPk that = (FinancesBankAccountPk) o;

        if (accountNumber != null ? !accountNumber.equals(that.accountNumber) : that.accountNumber != null) {
            return false;
        }
        if (companyNumber != null ? !companyNumber.equals(that.companyNumber) : that.companyNumber != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = companyNumber != null ? companyNumber.hashCode() : 0;
        result = 31 * result + (accountNumber != null ? accountNumber.hashCode() : 0);
        return result;
    }
}
