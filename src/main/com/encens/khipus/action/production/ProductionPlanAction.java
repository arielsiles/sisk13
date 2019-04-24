package com.encens.khipus.action.production;

import com.encens.khipus.framework.action.GenericAction;
import com.encens.khipus.framework.action.Outcome;
import com.encens.khipus.model.production.ProductionPlan;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.*;


@Name("productionPlanAction")
@Scope(ScopeType.CONVERSATION)
public class ProductionPlanAction extends GenericAction<ProductionPlan> {

    @In(create = true)
    private ProductionAction productionAction;

    @Factory(value = "productionPlan", scope = ScopeType.STATELESS)
    public ProductionPlan initProductionPlan() {
        return getInstance();
    }


    @Begin(nested = true, flushMode = FlushModeType.MANUAL)
    public String addProduction() {
        productionAction.setProductionPlan(getInstance());
        /*setOp(OP_UPDATE);*/
        return Outcome.SUCCESS;
    }

}
