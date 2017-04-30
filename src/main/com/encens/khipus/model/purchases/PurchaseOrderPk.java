package com.encens.khipus.model.purchases;

import org.hibernate.validator.Length;
import org.hibernate.validator.NotNull;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;

/**
 * PurchaseOrderPk
 *
 * @author
 * @version 2.0
 */
@Embeddable
public class PurchaseOrderPk implements Serializable {
    @Column(name = "NO_CIA", length = 2, nullable = false, updatable = false)
    private String companyNumber;

    @Column(name = "NO_ORDEN", length = 10, nullable = false, updatable = false)
    @NotNull
    @Length(max = 10)
    private String orderNumber;

    public PurchaseOrderPk() {
    }

    public PurchaseOrderPk(String companyNumber, String orderNumber) {
        this.companyNumber = companyNumber;
        this.orderNumber = orderNumber;
    }

    public String getCompanyNumber() {
        return companyNumber;
    }

    public void setCompanyNumber(String companyNumber) {
        this.companyNumber = companyNumber;
    }

    public String getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(String orderNumber) {
        this.orderNumber = orderNumber;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        PurchaseOrderPk that = (PurchaseOrderPk) o;

        if (companyNumber != null ? !companyNumber.equals(that.companyNumber) : that.companyNumber != null) {
            return false;
        }
        if (orderNumber != null ? !orderNumber.equals(that.orderNumber) : that.orderNumber != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = companyNumber != null ? companyNumber.hashCode() : 0;
        result = 31 * result + (orderNumber != null ? orderNumber.hashCode() : 0);
        return result;
    }
}
