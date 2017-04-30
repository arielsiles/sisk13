package com.encens.khipus.action.products;

import com.encens.khipus.framework.action.GenericAction;
import com.encens.khipus.model.products.ProductState;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.security.Restrict;

/**
 * @author:
 */
@Name("productStateAction")
@Scope(ScopeType.CONVERSATION)
public class ProductStateAction extends GenericAction<ProductState> {

    @Factory(value = "productState", scope = ScopeType.STATELESS)
    @Restrict("#{s:hasPermission('PRODUCTSTATE','VIEW')}")
    public ProductState initProductState() {
        return getInstance();
    }

    @Override
    protected String getDisplayNameProperty() {
        return "name";
    }
}
