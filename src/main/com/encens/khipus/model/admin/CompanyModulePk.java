package com.encens.khipus.model.admin;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;


/**
 * CompanyModulePk embeddable class to use like Pk
 *
 * @author
 * @version 1.0.18
 */
@Embeddable
public class CompanyModulePk implements Serializable {

    @Column(name = "idcompania", nullable = false, updatable = false)
    private Long companyId;

    @Column(name = "idmodulo", nullable = false, updatable = false)
    private Long systemModuleId;

    public CompanyModulePk() {
    }

    public CompanyModulePk(Long companyId, Long systemModuleId) {
        this.companyId = companyId;
        this.systemModuleId = systemModuleId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof CompanyModulePk)) {
            return false;
        }

        CompanyModulePk that = (CompanyModulePk) o;

        if (companyId != null ? !companyId.equals(that.companyId) : that.companyId != null) {
            return false;
        }
        if (systemModuleId != null ? !systemModuleId.equals(that.systemModuleId) : that.systemModuleId != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = companyId != null ? companyId.hashCode() : 0;
        result = 31 * result + (systemModuleId != null ? systemModuleId.hashCode() : 0);
        return result;
    }

    public Long getCompanyId() {
        return companyId;
    }

    public void setCompanyId(Long companyId) {
        this.companyId = companyId;
    }

    public Long getSystemModuleId() {
        return systemModuleId;
    }

    public void setSystemModuleId(Long systemModuleId) {
        this.systemModuleId = systemModuleId;
    }
}
