package com.encens.khipus.model.finances;

import org.hibernate.validator.Length;
import org.hibernate.validator.NotNull;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;

/**
 * ProviderPk
 *
 * @author
 * @version 2.0
 */
@Embeddable
public class ProviderPk implements Serializable {
    @Column(name = "NO_CIA", length = 2, nullable = false, updatable = false)
    private String companyNumber;

    @Column(name = "COD_PROV", length = 6, nullable = false, updatable = false)
    @Length(max = 6)
    @NotNull
    private String providerCode;

    public ProviderPk() {
    }

    public ProviderPk(String companyNumber, String providerCode) {
        this.companyNumber = companyNumber;
        this.providerCode = providerCode;
    }

    public String getCompanyNumber() {
        return companyNumber;
    }

    public void setCompanyNumber(String companyNumber) {
        this.companyNumber = companyNumber;
    }

    public String getProviderCode() {
        return providerCode;
    }

    public void setProviderCode(String providerCode) {
        this.providerCode = providerCode;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ProviderPk that = (ProviderPk) o;

        if (companyNumber != null ? !companyNumber.equals(that.companyNumber) : that.companyNumber != null) {
            return false;
        }
        if (providerCode != null ? !providerCode.equals(that.providerCode) : that.providerCode != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = companyNumber != null ? companyNumber.hashCode() : 0;
        result = 31 * result + (providerCode != null ? providerCode.hashCode() : 0);
        return result;
    }
}
