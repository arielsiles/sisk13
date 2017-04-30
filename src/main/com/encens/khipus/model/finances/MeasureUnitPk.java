package com.encens.khipus.model.finances;

import org.hibernate.validator.Length;
import org.hibernate.validator.NotNull;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;

/**
 * MeasureUnitPk
 *
 * @author
 * @version 2.0
 */
@Embeddable
public class MeasureUnitPk implements Serializable {
    @Column(name = "NO_CIA", length = 2, nullable = false, updatable = false)
    @Length(max = 2)
    private String companyNumber;
    @Column(name = "COD_MED", length = 6, nullable = false)
    @NotNull
    @Length(max = 6)
    private String measureUnitCode;

    public MeasureUnitPk() {
    }

    public MeasureUnitPk(String companyNumber, String measureUnitCode) {
        this.companyNumber = companyNumber;
        this.measureUnitCode = measureUnitCode;
    }

    public String getCompanyNumber() {
        return companyNumber;
    }

    public void setCompanyNumber(String companyNumber) {
        this.companyNumber = companyNumber;
    }

    public String getMeasureUnitCode() {
        return measureUnitCode;
    }

    public void setMeasureUnitCode(String measureUnitCode) {
        this.measureUnitCode = measureUnitCode;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        MeasureUnitPk that = (MeasureUnitPk) o;

        if (companyNumber != null ? !companyNumber.equals(that.companyNumber) : that.companyNumber != null) {
            return false;
        }
        if (measureUnitCode != null ? !measureUnitCode.equals(that.measureUnitCode) : that.measureUnitCode != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = companyNumber != null ? companyNumber.hashCode() : 0;
        result = 31 * result + (measureUnitCode != null ? measureUnitCode.hashCode() : 0);
        return result;
    }
}
