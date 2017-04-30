package com.encens.khipus.action.production;

import com.encens.khipus.framework.action.GenericAction;
import com.encens.khipus.model.production.ProductionOrder;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

@Name("productionOrderAction")
@Scope(ScopeType.CONVERSATION)
public class ProductionOrderAction extends GenericAction<ProductionOrder> {

    @Factory(value = "productionOrder", scope = ScopeType.STATELESS)
    public ProductionOrder initProductionOrder() {
        return getInstance();
    }

    @Override
    protected String getDisplayNameProperty() {
        return "code";
    }
}
