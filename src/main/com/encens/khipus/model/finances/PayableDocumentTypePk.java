package com.encens.khipus.model.finances;

import org.hibernate.validator.Length;
import org.hibernate.validator.NotNull;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;

/**
 * @author
 * @version 3.2.9
 */
@Embeddable
public class PayableDocumentTypePk implements Serializable {

    @Column(name = "NO_CIA", length = 2, nullable = false, updatable = false)
    @Length(max = 2)
    private String companyNumber;

    @Column(name = "TIPO_DOC", length = 3, nullable = false, updatable = false)
    @Length(max = 3)
    @NotNull
    private String documentType;

    public PayableDocumentTypePk() {
    }

    public PayableDocumentTypePk(String companyNumber, String documentType) {
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

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        PayableDocumentTypePk that = (PayableDocumentTypePk) o;

        if (companyNumber != null ? !companyNumber.equals(that.companyNumber) : that.companyNumber != null) {
            return false;
        }
        if (documentType != null ? !documentType.equals(that.documentType) : that.documentType != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = companyNumber != null ? companyNumber.hashCode() : 0;
        result = 31 * result + (documentType != null ? documentType.hashCode() : 0);
        return result;
    }
}
