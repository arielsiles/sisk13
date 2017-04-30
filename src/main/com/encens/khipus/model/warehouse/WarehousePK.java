package com.encens.khipus.model.warehouse;

import org.hibernate.validator.Length;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;

/**
 * @author
 */
@Embeddable
public class WarehousePK implements Serializable {
    @Column(name = "NO_CIA", nullable = false, length = 2)
    @Length(max = 2)
    private String companyNumber;

    @Column(name = "COD_ALM", nullable = false, length = 6)
    @Length(max = 6)
    private String warehouseCode;

    public WarehousePK() {
    }

    public WarehousePK(String companyNumber, String warehouseCode) {
        this.companyNumber = companyNumber;
        this.warehouseCode = warehouseCode;
    }

    public String getCompanyNumber() {
        return companyNumber;
    }

    public void setCompanyNumber(String companyNumber) {
        this.companyNumber = companyNumber;
    }

    public String getWarehouseCode() {
        return warehouseCode;
    }

    public void setWarehouseCode(String warehouseCode) {
        this.warehouseCode = warehouseCode;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        WarehousePK that = (WarehousePK) o;

        if (!companyNumber.equals(that.companyNumber)) {
            return false;
        }
        if (!warehouseCode.equals(that.warehouseCode)) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = companyNumber.hashCode();
        result = 31 * result + warehouseCode.hashCode();
        return result;
    }
}
