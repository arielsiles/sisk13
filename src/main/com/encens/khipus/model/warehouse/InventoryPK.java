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
public class InventoryPK implements Serializable {
    @Column(name = "NO_CIA", nullable = false, length = 2)
    @Length(max = 2)
    private String companyNumber;

    @Column(name = "COD_ALM", nullable = false, length = 6)
    @Length(max = 6)
    private String warehouseCode;

    @Column(name = "COD_ART", nullable = false, length = 6)
    @Length(max = 6)
    private String articleCode;

    public InventoryPK() {
    }

    public InventoryPK(String companyNumber, String warehouseCode, String articleCode) {
        this.companyNumber = companyNumber;
        this.warehouseCode = warehouseCode;
        this.articleCode = articleCode;
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

    public String getArticleCode() {
        return articleCode;
    }

    public void setArticleCode(String articleCode) {
        this.articleCode = articleCode;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        InventoryPK that = (InventoryPK) o;

        if (!articleCode.equals(that.articleCode)) {
            return false;
        }
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
        result = 31 * result + articleCode.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "companyNumber:" + companyNumber + ", articleCode:" + articleCode + ", warehouseCode:" + warehouseCode;
    }
}
