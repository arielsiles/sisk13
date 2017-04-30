package com.encens.khipus.model.finances;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;

/**
 * @author
 * @version 3.2.9
 */
@Embeddable
public class PayableDocumentPk implements Serializable {
    @Column(name = "NO_CIA", length = 2)
    private String companyNumber;

    @Column(name = "NO_TRANS", length = 10)
    private String transactionNumber;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        PayableDocumentPk that = (PayableDocumentPk) o;

        if (companyNumber != null ? !companyNumber.equals(that.companyNumber) : that.companyNumber != null) {
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
        return result;
    }
}
