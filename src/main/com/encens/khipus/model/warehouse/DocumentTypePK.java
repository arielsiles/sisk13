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
public class DocumentTypePK implements Serializable {
    @Column(name = "NO_CIA", nullable = false, length = 2)
    @Length(max = 2)
    private String companyNumber;

    @Column(name = "COD_DOC", nullable = false, length = 3)
    @Length(max = 3)
    private String documentCode;

    public DocumentTypePK() {
    }

    public DocumentTypePK(String companyNumber, String documentCode) {
        this.companyNumber = companyNumber;
        this.documentCode = documentCode;
    }

    public String getCompanyNumber() {
        return companyNumber;
    }

    public void setCompanyNumber(String companyNumber) {
        this.companyNumber = companyNumber;
    }

    public String getDocumentCode() {
        return documentCode;
    }

    public void setDocumentCode(String documentCode) {
        this.documentCode = documentCode;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        DocumentTypePK that = (DocumentTypePK) o;

        if (!companyNumber.equals(that.companyNumber)) {
            return false;
        }
        if (!documentCode.equals(that.documentCode)) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = companyNumber.hashCode();
        result = 31 * result + documentCode.hashCode();
        return result;
    }
}
