package com.encens.khipus.action.production;

import com.encens.khipus.framework.action.GenericAction;
import com.encens.khipus.framework.action.Outcome;
import com.encens.khipus.model.production.*;
import com.encens.khipus.service.production.ProductionService;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.*;

import java.util.ArrayList;
import java.util.List;


@Name("productionAction")
@Scope(ScopeType.CONVERSATION)
public class ProductionAction extends GenericAction<Production> {

    private ProductionTank productionTank;
    private Formulation formulation;
    private ProductionPlan productionPlan;

    private List<Supply> mainSupplyList = new ArrayList<Supply>();


    @In
    private ProductionService productionService;

    @Factory(value = "production", scope = ScopeType.STATELESS)
    public Production initProduction() {
        return getInstance();
    }


    @Override
    @End
    public String create() {

        Production production = getInstance();
        production.setProductionTank(productionTank);
        production.setFormulation(formulation);
        production.setProductionPlan(productionPlan);
        productionService.createProduction(production);

        //setOp(OP_UPDATE);
        return Outcome.SUCCESS;
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

    public ProductionPlan getProductionPlan() {
        return productionPlan;
    }

    public void setProductionPlan(ProductionPlan productionPlan) {
        this.productionPlan = productionPlan;
    }
}
