package com.encens.khipus.model.finances;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;

/**
 * @author
 * @version 2.25
 */

@Embeddable
public class FinanceAccountingDocumentPk implements Serializable {
    @Column(name = "no_cia", length = 2, updatable = false, nullable = false)
    private String companyNumber;

    @Column(name = "cod_enti", length = 6, updatable = false, nullable = false)
    private String entityCode;

    @Column(name = "no_auto", updatable = false, nullable = false)
    private String authorizationNumber;

    @Column(name = "no_fact", length = 20, updatable = false, nullable = false)
    private String invoiceNumber;

    public String getCompanyNumber() {
        return companyNumber;
    }

    public void setCompanyNumber(String companyNumber) {
        this.companyNumber = companyNumber;
    }

    public String getEntityCode() {
        return entityCode;
    }

    public void setEntityCode(String entityCode) {
        this.entityCode = entityCode;
    }

    public String getAuthorizationNumber() {
        return authorizationNumber;
    }

    public void setAuthorizationNumber(String authorizationNumber) {
        this.authorizationNumber = authorizationNumber;
    }

    public String getInvoiceNumber() {
        return invoiceNumber;
    }

    public void setInvoiceNumber(String invoiceNumber) {
        this.invoiceNumber = invoiceNumber;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof FinanceAccountingDocumentPk)) {
            return false;
        }

        FinanceAccountingDocumentPk that = (FinanceAccountingDocumentPk) o;

        return authorizationNumber.equals(that.authorizationNumber) && companyNumber.equals(that.companyNumber) && entityCode.equals(that.entityCode) && invoiceNumber.equals(that.invoiceNumber);

    }

    @Override
    public int hashCode() {
        int result = companyNumber.hashCode();
        result = 31 * result + entityCode.hashCode();
        result = 31 * result + authorizationNumber.hashCode();
        result = 31 * result + invoiceNumber.hashCode();
        return result;
    }
}
