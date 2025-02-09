package com.encens.khipus.action.xproduction;

import com.encens.khipus.framework.action.GenericAction;
import com.encens.khipus.model.xproduction.ProductionLine;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

@Name("productionLineAction")
@Scope(ScopeType.CONVERSATION)
public class ProductionLineAction extends GenericAction<ProductionLine> {

    @Factory(value = "productionLine", scope = ScopeType.STATELESS)
    public ProductionLine initXProductionLine() {
        return getInstance();
    }

}