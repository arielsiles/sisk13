package com.encens.khipus.model.finances;

import org.hibernate.validator.Length;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;

/**
 * @author
 * @version 2.0
 */
@Embeddable
public class FinancesModulePK implements Serializable {
    @Column(name = "NO_CIA", nullable = false, length = 2)
    @Length(max = 2)
    private String companyNumber;

    @Column(name = "MODULO", nullable = false, length = 6)
    @Length(max = 6)
    private String module;

    public FinancesModulePK() {
    }

    public FinancesModulePK(String companyNumber, String module) {
        this.companyNumber = companyNumber;
        this.module = module;
    }

    public String getCompanyNumber() {
        return companyNumber;
    }

    public void setCompanyNumber(String companyNumber) {
        this.companyNumber = companyNumber;
    }

    public String getModule() {
        return module;
    }

    public void setModule(String module) {
        this.module = module;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        FinancesModulePK that = (FinancesModulePK) o;

        if (!companyNumber.equals(that.companyNumber)) {
            return false;
        }
        if (!module.equals(that.module)) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = companyNumber.hashCode();
        result = 31 * result + module.hashCode();
        return result;
    }
}
