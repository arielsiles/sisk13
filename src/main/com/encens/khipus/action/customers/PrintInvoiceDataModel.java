package com.encens.khipus.action.customers;

import com.encens.khipus.framework.action.QueryDataModel;
import com.encens.khipus.model.customers.CustomerOrder;
import com.encens.khipus.model.warehouse.ProductDelivery;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.security.Restrict;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * @author
 * @version 2.4
 */
@Name("printInvoiceDataModel")
@Scope(ScopeType.PAGE)
@Restrict("#{s:hasPermission('PRODUCTDELIVERY','VIEW')}")
public class PrintInvoiceDataModel extends QueryDataModel<Long, CustomerOrder> {
    private Date date;
    private static final String[] RESTRICTIONS = {
            "customerOrder.dateDelicery = #{printInvoiceDataModel.date}"

    };

    @Create
    public void init() {
        sortProperty = "customerOrder.dateDelicery";
        sortAsc = false;
    }

    @Override
    public String getEjbql() {
        return "select customerOrder from CustomerOrder customerOrder";
    }

    @Override
    public List<String> getRestrictions() {
        return Arrays.asList(RESTRICTIONS);
    }

    public Date getDate() {
        if(date == null)
            date = new Date();

        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }
}
