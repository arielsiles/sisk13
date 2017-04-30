package com.encens.khipus.model.customers;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;

/**
 * Primary key class for Customer discount
 *
 * @author:
 */

@Embeddable
public class CustomerDiscountPk implements Serializable {

    @Column(name = "iddescuentocliente", nullable = false, updatable = false)
    private Long discountRuleId;

    @Column(name = "idcliente", nullable = false, updatable = false)
    private Long customerId;

    public CustomerDiscountPk() {
    }

    public CustomerDiscountPk(Long discountRuleId, Long customerId) {
        this.discountRuleId = discountRuleId;
        this.customerId = customerId;
    }

    public Long getDiscountRuleId() {
        return discountRuleId;
    }

    public void setDiscountRuleId(Long discountRuleId) {
        this.discountRuleId = discountRuleId;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        CustomerDiscountPk that = (CustomerDiscountPk) o;

        if (discountRuleId != null ? !discountRuleId.equals(that.discountRuleId) : that.discountRuleId != null) {
            return false;
        }
        if (customerId != null ? !customerId.equals(that.customerId) : that.customerId != null) {
            return false;
        }

        return true;
    }

    public int hashCode() {
        int result;
        result = (discountRuleId != null ? discountRuleId.hashCode() : 0);
        result = 31 * result + (customerId != null ? customerId.hashCode() : 0);
        return result;
    }
}
