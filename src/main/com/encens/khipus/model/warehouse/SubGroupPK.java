package com.encens.khipus.model.warehouse;

import org.hibernate.validator.Length;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;

/**
 * @author
 * @version 2.0
 */
@Embeddable
public class SubGroupPK implements Serializable {


    @Column(name = "NO_CIA", nullable = false, length = 2)
    @Length(max = 2)
    private String companyNumber;

    @Column(name = "COD_GRU", nullable = false, length = 3)
    @Length(max = 3)
    private String groupCode;

    @Column(name = "COD_SUB", nullable = false, length = 3)
    @Length(max = 3)
    private String subGroupCode;

    public SubGroupPK() {
    }

    public SubGroupPK(String companyNumber, String groupCode, String subGroupCode) {
        this.companyNumber = companyNumber;
        this.groupCode = groupCode;
        this.subGroupCode = subGroupCode;
    }

    public String getCompanyNumber() {
        return companyNumber;
    }

    public void setCompanyNumber(String companyNumber) {
        this.companyNumber = companyNumber;
    }

    public String getGroupCode() {
        return groupCode;
    }

    public void setGroupCode(String groupCode) {
        this.groupCode = groupCode;
    }

    public String getSubGroupCode() {
        return subGroupCode;
    }

    public void setSubGroupCode(String subGroupCode) {
        this.subGroupCode = subGroupCode;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        SubGroupPK that = (SubGroupPK) o;

        if (!companyNumber.equals(that.companyNumber)) {
            return false;
        }
        if (!groupCode.equals(that.groupCode)) {
            return false;
        }
        if (!subGroupCode.equals(that.subGroupCode)) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = companyNumber.hashCode();
        result = 31 * result + groupCode.hashCode();
        result = 31 * result + subGroupCode.hashCode();
        return result;
    }
}
