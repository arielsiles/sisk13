package com.encens.khipus.model.finances;

import org.hibernate.validator.Length;
import org.hibernate.validator.NotNull;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;

/**
 * @author
 * @version 2.8
 */
@Embeddable
public class AccountingTemplatePk implements Serializable {

    @Column(name = "NO_CIA", length = 2, nullable = false, updatable = false)
    @Length(max = 2)
    private String companyNumber;

    @Column(name = "COD_PLANTI", length = 6, nullable = false, updatable = false)
    @Length(max = 6)
    @NotNull
    private String templateCode;

    public AccountingTemplatePk() {
    }

    public AccountingTemplatePk(String companyNumber, String templateCode) {
        this.companyNumber = companyNumber;
        this.templateCode = templateCode;
    }

    public String getCompanyNumber() {
        return companyNumber;
    }

    public void setCompanyNumber(String companyNumber) {
        this.companyNumber = companyNumber;
    }

    public String getTemplateCode() {
        return templateCode;
    }

    public void setTemplateCode(String templateCode) {
        this.templateCode = templateCode;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        AccountingTemplatePk that = (AccountingTemplatePk) o;

        if (companyNumber != null ? !companyNumber.equals(that.companyNumber) : that.companyNumber != null) {
            return false;
        }
        if (templateCode != null ? !templateCode.equals(that.templateCode) : that.templateCode != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = companyNumber != null ? companyNumber.hashCode() : 0;
        result = 31 * result + (templateCode != null ? templateCode.hashCode() : 0);
        return result;
    }
}
