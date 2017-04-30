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
public class InventoryMovementPK implements Serializable {
    @Column(name = "NO_CIA", nullable = false, length = 2)
    @Length(max = 2)
    private String companyNumber;

    @Column(name = "NO_TRANS", nullable = false, length = 10)
    @Length(max = 10)
    private String transactionNumber;

    @Column(name = "ESTADO", nullable = false, length = 3)
    @Length(max = 3)
    private String state;

    public InventoryMovementPK() {
    }

    public InventoryMovementPK(String companyNumber, String transactionNumber, String state) {
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

        InventoryMovementPK that = (InventoryMovementPK) o;

        if (!companyNumber.equals(that.companyNumber)) {
            return false;
        }
        if (state != that.state) {
            return false;
        }
        if (!transactionNumber.equals(that.transactionNumber)) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = companyNumber.hashCode();
        result = 31 * result + transactionNumber.hashCode();
        result = 31 * result + state.hashCode();
        return result;
    }
}
