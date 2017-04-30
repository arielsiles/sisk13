package com.encens.khipus.action.products;

import com.encens.khipus.framework.action.GenericAction;
import com.encens.khipus.model.products.Product;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.security.Restrict;

/**
 * Actions for Product
 *
 * @author:
 */

@Name("productAction")
@Scope(ScopeType.CONVERSATION)
public class ProductAction extends GenericAction<Product> {

    @Factory(value = "product", scope = ScopeType.STATELESS)
    @Restrict("#{s:hasPermission('PRODUCT','VIEW')}")
    public Product initProduct() {
        return getInstance();
    }

    @Override
    protected String getDisplayNameProperty() {
        return "name";
    }
}
