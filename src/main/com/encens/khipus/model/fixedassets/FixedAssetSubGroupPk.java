package com.encens.khipus.model.fixedassets;

import org.hibernate.validator.Length;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;

/**
 * FixedAssetSubGroupPk embeddable class to use like Pk
 *
 * @author
 * @version 2.0
 */
@Embeddable
public class FixedAssetSubGroupPk implements Serializable {
    @Column(name = "no_cia", nullable = false)
    @Length(max = 2)
    private String companyNumber;

    @Column(name = "subgrupo", nullable = false)
    @Length(max = 3)
    private String fixedAssetSubGroupCode;

    @Column(name = "grupo", nullable = false, insertable = true)
    private String fixedAssetGroupCode;

    public FixedAssetSubGroupPk() {
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof FixedAssetSubGroupPk)) {
            return false;
        }

        FixedAssetSubGroupPk that = (FixedAssetSubGroupPk) o;

        if (companyNumber != null ? !companyNumber.equals(that.companyNumber) : that.companyNumber != null) {
            return false;
        }
        if (fixedAssetGroupCode != null ? !fixedAssetGroupCode.equals(that.fixedAssetGroupCode) : that.fixedAssetGroupCode != null) {
            return false;
        }
        if (fixedAssetSubGroupCode != null ? !fixedAssetSubGroupCode.equals(that.fixedAssetSubGroupCode) : that.fixedAssetSubGroupCode != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = companyNumber != null ? companyNumber.hashCode() : 0;
        result = 31 * result + (fixedAssetSubGroupCode != null ? fixedAssetSubGroupCode.hashCode() : 0);
        result = 31 * result + (fixedAssetGroupCode != null ? fixedAssetGroupCode.hashCode() : 0);
        return result;
    }

    public String getCompanyNumber() {
        return companyNumber;
    }

    public void setCompanyNumber(String companyNumber) {
        this.companyNumber = companyNumber;
    }

    public String getFixedAssetSubGroupCode() {
        return fixedAssetSubGroupCode;
    }

    public void setFixedAssetSubGroupCode(String fixedAssetSubGroupCode) {
        this.fixedAssetSubGroupCode = fixedAssetSubGroupCode;
    }

    public String getFixedAssetGroupCode() {
        return fixedAssetGroupCode;
    }

    public void setFixedAssetGroupCode(String fixedAssetGroupCode) {
        this.fixedAssetGroupCode = fixedAssetGroupCode;
    }
}
