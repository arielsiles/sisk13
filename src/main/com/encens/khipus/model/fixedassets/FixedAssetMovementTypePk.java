package com.encens.khipus.model.fixedassets;

import org.hibernate.validator.Length;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;

/**
 * FixedAssetMovementTypePk embeddable class to use like Pk
 *
 * @author
 * @version 2.0
 */
@Embeddable
public class FixedAssetMovementTypePk implements Serializable {

    @Column(name = "cod_mov", nullable = false, updatable = false)
    @Length(max = 3)
    private String movementCode;

    @Column(name = "no_cia", nullable = false, updatable = false)
    @Length(max = 2)
    private String companyNumber;

    public FixedAssetMovementTypePk() {
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof FixedAssetMovementTypePk)) {
            return false;
        }

        FixedAssetMovementTypePk that = (FixedAssetMovementTypePk) o;

        if (companyNumber != null ? !companyNumber.equals(that.companyNumber) : that.companyNumber != null) {
            return false;
        }
        if (movementCode != null ? !movementCode.equals(that.movementCode) : that.movementCode != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = movementCode != null ? movementCode.hashCode() : 0;
        result = 31 * result + (companyNumber != null ? companyNumber.hashCode() : 0);
        return result;
    }

    public String getMovementCode() {
        return movementCode;
    }

    public void setMovementCode(String movementCode) {
        this.movementCode = movementCode;
    }

    public String getCompanyNumber() {
        return companyNumber;
    }

    public void setCompanyNumber(String companyNumber) {
        this.companyNumber = companyNumber;
    }
}
