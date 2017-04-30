package com.encens.khipus.model.finances;

import org.hibernate.validator.Length;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;

/**
 * Encens S.R.L.
 * FinancesExecutorUnitPk
 *
 * @author
 * @version $Id: FinancesExecutorUnitPk.java  29-nov-2010 16:04:51$
 */
@Embeddable
public class FinancesExecutorUnitPk implements Serializable {
    @Column(name = "NO_CIA", nullable = false, updatable = false)
    @Length(max = 2)
    private String companyNumber;

    @Column(name = "COD_UEJ", nullable = false, updatable = false)
    @Length(max = 6)
    private String executorUnitCode;

    public FinancesExecutorUnitPk() {
    }

    public FinancesExecutorUnitPk(String companyNumber, String executorUnitCode) {
        this.companyNumber = companyNumber;
        this.executorUnitCode = executorUnitCode;
    }

    public String getCompanyNumber() {
        return companyNumber;
    }

    public void setCompanyNumber(String companyNumber) {
        this.companyNumber = companyNumber;
    }

    public String getExecutorUnitCode() {
        return executorUnitCode;
    }

    public void setExecutorUnitCode(String executorUnitCode) {
        this.executorUnitCode = executorUnitCode;
    }
}
