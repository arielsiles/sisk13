package com.encens.khipus.action.customers;

import com.encens.khipus.framework.action.QueryDataModel;
import com.encens.khipus.model.warehouse.ProductDelivery;
import com.encens.khipus.model.warehouse.SoldProduct;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.security.Restrict;

import java.util.Arrays;
import java.util.List;

/**
 * @author
 * @version 2.4
 */
@Name("orderDeliveryDataModel")
@Scope(ScopeType.PAGE)
@Restrict("#{s:hasPermission('ORDERDELIVERYWAREHOUSE','VIEW')}")
public class OrderDeliveryDataModel extends QueryDataModel<Long, SoldProduct> {
    private static final String[] RESTRICTIONS = {
            "customerOrder = #{orderDeliveryDataModel.criteria.customerOrder.dateDelicery}"
    };

    @Create
    public void init() {
        this.setSortAsc(false);
        sortProperty = "soldProduct.customerOrder.dateDelicery";
    }

    @Override
    public String getEjbql() {
        return " select soldProduct from SoldProduct soldProduct" +
               " inner join soldProduct.customerOrder customerOrder ";
    }

    @Override
    public List<String> getRestrictions() {
        return Arrays.asList(RESTRICTIONS);
    }


}
