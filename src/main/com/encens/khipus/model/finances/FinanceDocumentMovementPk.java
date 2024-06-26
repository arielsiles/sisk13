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
public class FinanceDocumentMovementPk implements Serializable {

    @Column(name = "no_cia", length = 2)
    private String companyNumber;

    @Column(name = "no_trans", length = 10)
    private String transactionNumber;

    @Column(name = "estado", length = 3)
    @Length(max = 3)
    private String state;

    public FinanceDocumentMovementPk() {
    }

    public FinanceDocumentMovementPk(String transactionNumber, String state) {
        this.transactionNumber = transactionNumber;
        this.state = state;
    }

    public FinanceDocumentMovementPk(String companyNumber, String transactionNumber, String state) {
        this.companyNumber = companyNumber;
        this.transactionNumber = transactionNumber;
        this.state = state;
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

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        FinanceDocumentMovementPk that = (FinanceDocumentMovementPk) o;

        if (companyNumber != null ? !companyNumber.equals(that.companyNumber) : that.companyNumber != null) {
            return false;
        }
        if (state != null ? !state.equals(that.state) : that.state != null) {
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
        result = 31 * result + (state != null ? state.hashCode() : 0);
        return result;
    }
}
