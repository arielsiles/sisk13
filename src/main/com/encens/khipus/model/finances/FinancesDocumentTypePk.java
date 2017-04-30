package com.encens.khipus.model.finances;

import org.hibernate.validator.Length;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;

/**
 * FinancesDocumentTypePk
 *
 * @author
 * @version 2.10
 */
@Embeddable
public class FinancesDocumentTypePk implements Serializable {
    @Column(name = "NO_CIA", nullable = false, updatable = false)
    @Length(max = 2)
    private String companyNumber;

    @Column(name = "TIPO_DOC", nullable = false, updatable = false)
    @Length(max = 3)
    private String documentType;

    public FinancesDocumentTypePk() {
    }

    public FinancesDocumentTypePk(String companyNumber, String documentType) {
        this.companyNumber = companyNumber;
        this.documentType = documentType;
    }

    public String getCompanyNumber() {
        return companyNumber;
    }

    public void setCompanyNumber(String companyNumber) {
        this.companyNumber = companyNumber;
    }

    public String getDocumentType() {
        return documentType;
    }

    public void setDocumentType(String documentType) {
        this.documentType = documentType;
    }
}
