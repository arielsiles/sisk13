package com.encens.khipus.action.warehouse;

import com.encens.khipus.framework.action.QueryDataModel;
import com.encens.khipus.model.warehouse.ProductItem;
import com.encens.khipus.model.warehouse.ProductItemPK;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.security.Restrict;

import java.util.Arrays;
import java.util.List;

/**
 * @author
 * @version 2.0
 */
@Name("productItemDataModel")
@Scope(ScopeType.PAGE)
@Restrict("#{s:hasPermission('PRODUCTITEM','VIEW')}")
public class ProductItemDataModel extends QueryDataModel<ProductItemPK, ProductItem> {
    private static final String[] RESTRICTIONS = {
            "lower(productItem.id.productItemCode) like concat(lower(#{productItemDataModel.criteria.id.productItemCode}), '%')",
            "lower(productItem.name) like concat('%', concat(lower(#{productItemDataModel.criteria.name}), '%'))"
    };

    @Create
    public void init() {
        sortProperty = "productItem.name";
    }

    @Override
    public String getEjbql() {
        return "select productItem from ProductItem productItem";
    }

    @Override
    public List<String> getRestrictions() {
        return Arrays.asList(RESTRICTIONS);
    }
}
