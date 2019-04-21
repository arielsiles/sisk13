package com.encens.khipus.action.production;

import com.encens.khipus.action.SessionUser;
import com.encens.khipus.framework.action.GenericAction;
import com.encens.khipus.framework.action.Outcome;
import com.encens.khipus.model.admin.User;
import com.encens.khipus.model.production.*;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.*;

import java.util.ArrayList;
import java.util.List;


@Name("productionPlanAction")
@Scope(ScopeType.CONVERSATION)
public class ProductionPlanAction extends GenericAction<ProductionPlan> {

    private ProductionTank productionTank;
    private Formulation formulation;

    private List<Supply> mainSupplyList = new ArrayList<Supply>();

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

    public void loadSupplies(){
        setMainSupplyList(new ArrayList<Supply>());
        for (FormulationInput formulationInput : this.formulation.getFormulationInputList()){
            Supply supply = new Supply();
            supply.setProductItemCode(formulationInput.getProductItemCode());
            supply.setProductItem(formulationInput.getProductItem());
            supply.setQuantity(formulationInput.getQuantity());
            mainSupplyList.add(supply);
        }
    }

    public ProductionTank getProductionTank() {
        return productionTank;
    }

    public void setProductionTank(ProductionTank productionTank) {
        this.productionTank = productionTank;
    }

    public Formulation getFormulation() {
        return formulation;
    }

    public void setFormulation(Formulation formulation) {
        this.formulation = formulation;
    }

    public List<Supply> getMainSupplyList() {
        return mainSupplyList;
    }

    public void setMainSupplyList(List<Supply> mainSupplyList) {
        this.mainSupplyList = mainSupplyList;
    }
}
