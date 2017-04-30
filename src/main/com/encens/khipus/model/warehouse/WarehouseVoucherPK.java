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
public class WarehouseVoucherPK implements Serializable {
    @Column(name = "NO_CIA", nullable = false, length = 2)
    @Length(max = 2)
    private String companyNumber;

    @Column(name = "NO_TRANS", nullable = false, length = 10)
    @Length(max = 10)
    private String transactionNumber;

    public WarehouseVoucherPK() {
    }

    public WarehouseVoucherPK(String companyNumber, String transactionNumber) {
        this.companyNumber = companyNumber;
        this.transactionNumber = transactionNumber;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        WarehouseVoucherPK that = (WarehouseVoucherPK) o;

        if (!companyNumber.equals(that.companyNumber)) {
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
        return result;
    }
}
