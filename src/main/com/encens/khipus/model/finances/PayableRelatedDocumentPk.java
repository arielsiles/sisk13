package com.encens.khipus.model.finances;

import org.hibernate.validator.Length;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;

/**
 * @author
 * @version 3.2.9
 */
@Embeddable
public class PayableRelatedDocumentPk implements Serializable {

    @Column(name = "NO_CIA", updatable = false, length = 2)
    @Length(max = 2)
    private String companyNumber;

    @Column(name = "NO_TRANS", updatable = false, length = 10)
    @Length(max = 10)
    private String transactionNumber;

    @Column(name = "NO_CONCI", updatable = false, length = 10)
    @Length(max = 10)
    private String conciliationNumber;

    public PayableRelatedDocumentPk() {
    }

    public PayableRelatedDocumentPk(String transactionNumber, String conciliationNumber) {
        this.transactionNumber = transactionNumber;
        this.conciliationNumber = conciliationNumber;
    }

    public PayableRelatedDocumentPk(String companyNumber, String transactionNumber, String conciliationNumber) {
        this.companyNumber = companyNumber;
        this.transactionNumber = transactionNumber;
        this.conciliationNumber = conciliationNumber;
    }

    public String getCompanyNumber() {
        return companyNumber;
    }

    public void setCompanyNumber(String companyNumber) {
        this.companyNumber = companyNumber;
    }

    public String getTransactionNumber() {
        return transactionNumber;
    }

    public void setTransactionNumber(String transactionNumber) {
        this.transactionNumber = transactionNumber;
    }

    public String getConciliationNumber() {
        return conciliationNumber;
    }

    public void setConciliationNumber(String conciliationNumber) {
        this.conciliationNumber = conciliationNumber;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        PayableRelatedDocumentPk that = (PayableRelatedDocumentPk) o;

        if (companyNumber != null ? !companyNumber.equals(that.companyNumber) : that.companyNumber != null) {
            return false;
        }
        if (conciliationNumber != null ? !conciliationNumber.equals(that.conciliationNumber) : that.conciliationNumber != null) {
            return false;
        }
        if (transactionNumber != null ? !transactionNumber.equals(that.transactionNumber) : that.transactionNumber != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = companyNumber != null ? companyNumber.hashCode() : 0;
        result = 31 * result + (transactionNumber != null ? transactionNumber.hashCode() : 0);
        result = 31 * result + (conciliationNumber != null ? conciliationNumber.hashCode() : 0);
        return result;
    }
}
