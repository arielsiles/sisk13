package com.encens.khipus.action.xproduction;

import com.encens.khipus.framework.action.GenericAction;
import com.encens.khipus.framework.service.GenericService;
import com.encens.khipus.model.production.ProductionPlanning;
import com.encens.khipus.service.warehouse.ProductionTransferLogService;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

@Name("productionTransferLogAction")
@Scope(ScopeType.CONVERSATION)
public class ProductionTransferLogAction extends GenericAction<ProductionPlanning> {

    @In
    private ProductionTransferLogService productionTransferLogService;

    @Override
    protected GenericService getService() {
        return productionTransferLogService;
    }

    @Factory(value = "productionTransferLog", scope = ScopeType.STATELESS)
    public ProductionPlanning initProductionTransferLog() {
        return getInstance();
    }


}
