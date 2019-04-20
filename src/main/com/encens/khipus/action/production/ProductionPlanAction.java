package com.encens.khipus.action.production;

import com.encens.khipus.action.SessionUser;
import com.encens.khipus.framework.action.GenericAction;
import com.encens.khipus.framework.action.Outcome;
import com.encens.khipus.model.admin.User;
import com.encens.khipus.model.production.ProductionPlan;
import com.encens.khipus.model.production.ProductionTank;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.*;


@Name("productionPlanAction")
@Scope(ScopeType.CONVERSATION)
public class ProductionPlanAction extends GenericAction<ProductionPlan> {

    private ProductionTank productionTank;

    @In
    private SessionUser sessionUser;
    @In
    private User currentUser;

    @Factory(value = "productionPlan", scope = ScopeType.STATELESS)
    public ProductionPlan initProductionPlan() {
        return getInstance();
    }


    @Override
    @End
    public String create() {
        super.create();
        setOp(OP_UPDATE);
        return Outcome.REDISPLAY;
    }


    public ProductionTank getProductionTank() {
        return productionTank;
    }

    public void setProductionTank(ProductionTank productionTank) {
        this.productionTank = productionTank;
    }
}
