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
public class InventoryHistoryPK implements Serializable {

    @Column(name = "NO_CIA", nullable = false, length = 2)
    @Length(max = 2)
    private String companyNumber;

    @Column(name = "MES", nullable = false, length = 6)
    @Length(max = 6)
    private String month;

    @Column(name = "COD_ALM", nullable = false, length = 6)
    @Length(max = 6)
    private String warehouseCode;

    @Column(name = "COD_ART", nullable = false, length = 6)
    @Length(max = 6)
    private String articleCode;

    public InventoryHistoryPK() {
    }

    public InventoryHistoryPK(String companyNumber, String month, String warehouseCode, String articleCode) {
        this.companyNumber = companyNumber;
        this.month = month;
        this.warehouseCode = warehouseCode;
        this.articleCode = articleCode;
    }

    public String getCompanyNumber() {
        return companyNumber;
    }

    public void setCompanyNumber(String companyNumber) {
        this.companyNumber = companyNumber;
    }

    public String getMonth() {
        return month;
    }

    public void setMonth(String month) {
        this.month = month;
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

        InventoryHistoryPK that = (InventoryHistoryPK) o;

        if (!articleCode.equals(that.articleCode)) {
            return false;
        }
        if (!companyNumber.equals(that.companyNumber)) {
            return false;
        }
        if (!month.equals(that.month)) {
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
        result = 31 * result + month.hashCode();
        result = 31 * result + warehouseCode.hashCode();
        result = 31 * result + articleCode.hashCode();
        return result;
    }
}
