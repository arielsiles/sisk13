package com.encens.khipus.model.fixedassets;

import org.hibernate.validator.Length;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;

/**
 * FixedAssetGroupPk embeddable class to use like Pk
 *
 * @author
 * @version 2.0
 */
@Embeddable
public class FixedAssetGroupPk implements Serializable {

    @Column(name = "grupo", nullable = false, updatable = false)
    @Length(max = 3)
    private String groupCode;

    @Column(name = "no_cia", nullable = false, updatable = false)
    @Length(max = 2)
    private String companyNumber;

    public FixedAssetGroupPk() {
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof FixedAssetGroupPk)) {
            return false;
        }

        FixedAssetGroupPk that = (FixedAssetGroupPk) o;

        if (companyNumber != null ? !companyNumber.equals(that.companyNumber) : that.companyNumber != null) {
            return false;
        }
        if (groupCode != null ? !groupCode.equals(that.groupCode) : that.groupCode != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = groupCode != null ? groupCode.hashCode() : 0;
        result = 31 * result + (companyNumber != null ? companyNumber.hashCode() : 0);
        return result;
    }

    public String getGroupCode() {
        return groupCode;
    }

    public void setGroupCode(String groupCode) {
        this.groupCode = groupCode;
    }

    public String getCompanyNumber() {
        return companyNumber;
    }

    public void setCompanyNumber(String companyNumber) {
        this.companyNumber = companyNumber;
    }
}
