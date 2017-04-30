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
public class GroupPK implements Serializable {
    @Column(name = "NO_CIA", nullable = false, length = 2)
    @Length(max = 2)
    private String companyNumber;

    @Column(name = "COD_GRU", nullable = false, length = 3)
    @Length(max = 3)
    private String groupCode;

    public GroupPK() {
    }

    public GroupPK(String companyNumber, String groupCode) {
        this.companyNumber = companyNumber;
        this.groupCode = groupCode;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        GroupPK groupPK = (GroupPK) o;

        if (!companyNumber.equals(groupPK.companyNumber)) {
            return false;
        }
        if (!groupCode.equals(groupPK.groupCode)) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = companyNumber.hashCode();
        result = 31 * result + groupCode.hashCode();
        return result;
    }
}
