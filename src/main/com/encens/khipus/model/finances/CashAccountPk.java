package com.encens.khipus.model.finances;

import org.hibernate.validator.Length;
import org.hibernate.validator.NotNull;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;

/**
 * CashAccountPk
 *
 * @author
 * @version 2.0
 */
@Embeddable
public class CashAccountPk implements Serializable {

    @Column(name = "NO_CIA", length = 2, nullable = false, updatable = false)
    @Length(max = 2)
    private String companyNumber;

    @Column(name = "CUENTA", length = 20, nullable = false, updatable = false)
    @Length(max = 20)
    @NotNull
    private String accountCode;

    public CashAccountPk() {
    }

    public CashAccountPk(String companyNumber, String accountCode) {
        this.companyNumber = companyNumber;
        this.accountCode = accountCode;
    }

    public String getCompanyNumber() {
        return companyNumber;
    }

    public void setCompanyNumber(String companyNumber) {
        this.companyNumber = companyNumber;
    }

    public String getAccountCode() {
        return accountCode;
    }

    public void setAccountCode(String accountCode) {
        this.accountCode = accountCode;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        CashAccountPk that = (CashAccountPk) o;

        if (accountCode != null ? !accountCode.equals(that.accountCode) : that.accountCode != null) {
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
        result = 31 * result + (accountCode != null ? accountCode.hashCode() : 0);
        return result;
    }
}
