package com.encens.khipus.action.products;

import com.encens.khipus.framework.action.GenericAction;
import com.encens.khipus.model.products.ProductType;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.security.Restrict;

/**
 * @author:
 */
@Name("productTypeAction")
@Scope(ScopeType.CONVERSATION)
public class ProductTypeAction extends GenericAction<ProductType> {

    @Factory(value = "productType", scope = ScopeType.STATELESS)
    @Restrict("#{s:hasPermission('PRODUCTTYPE','VIEW')}")
    public ProductType initProductType() {
        return getInstance();
    }

    @Override
    protected String getDisplayNameProperty() {
        return "name";
    }
}
