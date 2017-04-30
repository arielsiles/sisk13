package com.encens.khipus.model.finances;

import org.hibernate.validator.Length;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;

/**
 * @author
 * @version 3.2
 */
@Embeddable
public class ProviderClassPk implements Serializable {
    @Column(name = "NO_CIA", updatable = false)
    @Length(max = 2)
    private String companyNumber;

    @Column(name = "CLASE", updatable = false)
    @Length(max = 4)
    private String code;

    public ProviderClassPk() {
    }

    public ProviderClassPk(String companyNumber, String code) {
        this.companyNumber = companyNumber;
        this.code = code;
    }

    public String getCompanyNumber() {
        return companyNumber;
    }

    public void setCompanyNumber(String companyNumber) {
        this.companyNumber = companyNumber;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ProviderClassPk that = (ProviderClassPk) o;

        if (companyNumber != null ? !companyNumber.equals(that.companyNumber) : that.companyNumber != null) {
            return false;
        }
        if (code != null ? !code.equals(that.code) : that.code != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = companyNumber != null ? companyNumber.hashCode() : 0;
        result = 31 * result + (code != null ? code.hashCode() : 0);
        return result;
    }
}
