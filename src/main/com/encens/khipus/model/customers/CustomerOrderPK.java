package com.encens.khipus.model.customers;

import org.hibernate.validator.Length;

import javax.persistence.Column;
import java.io.Serializable;

/**
 * Created with IntelliJ IDEA.
 * User: Diego
 * Date: 18/12/13
 * Time: 19:38
 * To change this template use File | Settings | File Templates.
 */
public class CustomerOrderPK implements Serializable {

    @Column(name = "PEDIDO", nullable = false, length = 10)
    @Length(max = 10)
    private String order;

    @Column(name = "ID", nullable = false, length = 20)
    @Length(max = 20)
    private String orderID;

    @Column(name = "ID1", nullable = false, length = 10)
    @Length(max = 10)
    private Integer orderID1;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        CustomerOrderPK customerOrderPK = (CustomerOrderPK) o;

        if (!customerOrderPK.equals(customerOrderPK.order)) {
            return false;
        }

        if (!customerOrderPK.equals(customerOrderPK.orderID)) {
            return false;
        }
        if (!customerOrderPK.equals(customerOrderPK.orderID1)) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = orderID1.hashCode();
        result = 31 * result + order.hashCode() + orderID.hashCode();
        return result;
    }

    public String getOrder() {
        return order;
    }

    public void setOrder(String order) {
        this.order = order;
    }

    public String getOrderID() {
        return orderID;
    }

    public void setOrderID(String orderID) {
        this.orderID = orderID;
    }

    public Integer getOrderID1() {
        return orderID1;
    }

    public void setOrderID1(Integer orderID1) {
        this.orderID1 = orderID1;
    }
}
