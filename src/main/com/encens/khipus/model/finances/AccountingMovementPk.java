package com.encens.khipus.model.finances;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;

/**
 * AccountingMovementPk
 *
 * @author
 * @version 2.5
 */
@Embeddable
public class AccountingMovementPk implements Serializable {
    @Column(name = "NO_CIA", nullable = false, updatable = false)
    private String companyNumber;

    @Column(name = "TIPO_COMPRO", nullable = false, updatable = false)
    private String voucherType;

    @Column(name = "NO_COMPRO", nullable = false, updatable = false)
    private String voucherNumber;

    public AccountingMovementPk() {
    }

    public AccountingMovementPk(String companyNumber, String voucherType, String voucherNumber) {
        this.companyNumber = companyNumber;
        this.voucherType = voucherType;
        this.voucherNumber = voucherNumber;
    }

    public String getCompanyNumber() {
        return companyNumber;
    }

    public void setCompanyNumber(String companyNumber) {
        this.companyNumber = companyNumber;
    }

    public String getVoucherType() {
        return voucherType;
    }

    public void setVoucherType(String voucherType) {
        this.voucherType = voucherType;
    }

    public String getVoucherNumber() {
        return voucherNumber;
    }

    public void setVoucherNumber(String voucherNumber) {
        this.voucherNumber = voucherNumber;
    }
}
