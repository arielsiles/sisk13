package com.encens.khipus.action.production;

import com.encens.khipus.framework.action.GenericAction;
import com.encens.khipus.framework.action.Outcome;
import com.encens.khipus.framework.service.GenericService;
import com.encens.khipus.model.production.MetaProduct;
import com.encens.khipus.model.production.OutputProductionVoucher;
import com.encens.khipus.model.production.ProductionPlanning;
import com.encens.khipus.model.warehouse.*;
import com.encens.khipus.service.warehouse.ProductionTransferLogService;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.*;

import java.util.Calendar;
import java.util.List;

import static com.encens.khipus.model.warehouse.ProductionTransferLogState.DELIVERED;
import static com.encens.khipus.model.warehouse.ProductionTransferLogState.RECEIVED;
import static org.jboss.seam.international.StatusMessage.Severity.ERROR;
import static org.jboss.seam.international.StatusMessage.Severity.INFO;

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
