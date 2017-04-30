package com.encens.khipus.model.finances;

import org.hibernate.validator.Length;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;

/**
 * FinancesCurrencyPk embeddable class to use like Pk
 *
 * @author
 * @version 2.3
 */
@Embeddable
public class FinancesCurrencyPk implements Serializable {

    @Column(name = "COD_MON", nullable = false, updatable = false)
    @Length(max = 2)
    private String currencyCode;

    @Column(name = "NO_CIA", nullable = false, updatable = false)
    @Length(max = 2)
    private String companyNumber;

    public FinancesCurrencyPk() {
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof FinancesCurrencyPk)) {
            return false;
        }

        FinancesCurrencyPk that = (FinancesCurrencyPk) o;
        return !(companyNumber != null ? !companyNumber.equals(that.companyNumber) : that.companyNumber != null) && !(currencyCode != null ? !currencyCode.equals(that.currencyCode) : that.currencyCode != null);
    }

    @Override
    public int hashCode() {
        int result = currencyCode != null ? currencyCode.hashCode() : 0;
        result = 31 * result + (companyNumber != null ? companyNumber.hashCode() : 0);
        return result;
    }

    public String getCurrencyCode() {
        return currencyCode;
    }

    public void setCurrencyCode(String currencyCode) {
        this.currencyCode = currencyCode;
    }

    public String getCompanyNumber() {
        return companyNumber;
    }

    public void setCompanyNumber(String companyNumber) {
        this.companyNumber = companyNumber;
    }
}
