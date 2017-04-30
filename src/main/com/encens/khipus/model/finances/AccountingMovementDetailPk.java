package com.encens.khipus.model.finances;

import org.hibernate.validator.Length;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;

/**
 * AccountingMovementDetailPk
 *
 * @author
 * @version 2.5
 */
@Embeddable
public class AccountingMovementDetailPk implements Serializable {
    @Column(name = "NO_CIA", nullable = false, updatable = false)
    private String companyNumber;

    @Column(name = "NO_TRANS", nullable = false, insertable = true)
    @Length(max = 10)
    private String transactionNumber;

    @Column(name = "NO_LINEA", nullable = false, insertable = true)
    @Length(max = 10)
    private String detailNumber;

    public AccountingMovementDetailPk() {
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

    public String getDetailNumber() {
        return detailNumber;
    }

    public void setDetailNumber(String detailNumber) {
        this.detailNumber = detailNumber;
    }
}
