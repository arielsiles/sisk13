package com.encens.khipus.model.customers;

import org.hibernate.validator.Length;

import javax.persistence.Column;
import java.io.Serializable;

/**
 * Created with IntelliJ IDEA.
 * User: Diego
 * Date: 20/12/14
 * Time: 16:11
 * To change this template use File | Settings | File Templates.
 */
public class ArticleOrderPK implements Serializable {
    @Column(name = "ID_CUENTA", nullable = false, length = 10)
    @Length(max = 10)
    private Long idAccount;

    @Column(name = "ID", nullable = false, length = 20,columnDefinition = "VARCHAR2(20 BYTE)")
    @Length(max = 20)
    private String orderID;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ArticleOrderPK articleOrderPK = (ArticleOrderPK) o;

        if (!articleOrderPK.equals(articleOrderPK.idAccount)) {
            return false;
        }

        if (!articleOrderPK.equals(articleOrderPK.orderID)) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = 31 * idAccount.hashCode() + orderID.hashCode();
        return result;
    }

    public Long getIdAccount() {
        return idAccount;
    }

    public void setIdAccount(Long idAccount) {
        this.idAccount = idAccount;
    }

    public String getOrderID() {
        return orderID;
    }

    public void setOrderID(String orderID) {
        this.orderID = orderID;
    }

}
