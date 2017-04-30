package com.encens.khipus.action.products;

import com.encens.khipus.framework.action.QueryDataModel;
import com.encens.khipus.model.products.Product;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.security.Restrict;

import java.util.Arrays;
import java.util.List;

/**
 * Data model for Product
 *
 * @author:
 */

@Name("productDataModel")
@Scope(ScopeType.PAGE)
@Restrict("#{s:hasPermission('PRODUCT','VIEW')}")
public class ProductDataModel extends QueryDataModel<Long, Product> {
    private static final String[] RESTRICTIONS = {
            "lower(product.code) like concat(lower(#{productDataModel.criteria.code}), '%')",
            "lower(product.name) like concat('%', concat(lower(#{productDataModel.criteria.name}), '%'))"};

    @Create
    public void init() {
        sortProperty = "product.name";
    }

    @Override
    public String getEjbql() {
        return "select product from Product product";
    }

    @Override
    public List<String> getRestrictions() {
        return Arrays.asList(RESTRICTIONS);
    }
}
