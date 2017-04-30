package com.encens.khipus.action.warehouse;

import com.encens.khipus.framework.action.QueryDataModel;
import com.encens.khipus.model.warehouse.ProductDelivery;
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
@Name("productDeliveryDataModel")
@Scope(ScopeType.PAGE)
@Restrict("#{s:hasPermission('PRODUCTDELIVERY','VIEW')}")
public class ProductDeliveryDataModel extends QueryDataModel<Long, ProductDelivery> {
    private static final String[] RESTRICTIONS = {
            "lower(productDelivery.invoiceNumber) like concat(lower(#{productDeliveryDataModel.criteria.invoiceNumber}), '%')"
    };

    @Create
    public void init() {
        sortProperty = "productDelivery.invoiceNumber";
    }

    @Override
    public String getEjbql() {
        return "select productDelivery from ProductDelivery productDelivery";
    }

    @Override
    public List<String> getRestrictions() {
        return Arrays.asList(RESTRICTIONS);
    }
}
