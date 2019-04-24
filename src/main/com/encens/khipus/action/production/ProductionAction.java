package com.encens.khipus.action.production;

import com.encens.khipus.framework.action.GenericAction;
import com.encens.khipus.framework.action.Outcome;
import com.encens.khipus.model.production.*;
import com.encens.khipus.model.warehouse.ProductItem;
import com.encens.khipus.service.common.SequenceService;
import com.encens.khipus.service.production.ProductionService;
import com.encens.khipus.util.Constants;
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

    private List<Supply> ingredientSupplyList = new ArrayList<Supply>();
    private List<Supply> materialSupplyList = new ArrayList<Supply>();


    @In
    private ProductionService productionService;
    @In
    private SequenceService sequenceService;

    @Factory(value = "production", scope = ScopeType.STATELESS)
    public Production initProduction() {
        return getInstance();
    }


    @Override
    @Begin(nested=true, ifOutcome = Outcome.SUCCESS, flushMode = FlushModeType.MANUAL)
    public String select(Production instance) {
        String outCome = super.select(instance);


        System.out.println("Produccion: " + getInstance().getId());
        System.out.println("Formula: " + getInstance().getFormulation().getId() + " - " + getInstance().getFormulation().getName());
        System.out.println("Tanque: " + getInstance().getProductionTank().getName());

        setFormulation(getInstance().getFormulation());
        setProductionTank(getInstance().getProductionTank());
        setIngredientSupplyList(getInstance().getSupplyList());

        return outCome;
    }

    @Override
    public String create() {

        Production production = getInstance();
        production.setState(ProductionState.PEN);
        production.setProductionTank(productionTank);
        production.setFormulation(formulation);
        production.setProductionPlan(productionPlan);

        Long seq = sequenceService.findNextSequenceValue(Constants.PRODUCTION_CODE);
        production.setCode(seq.intValue());
        productionService.createProduction(production, ingredientSupplyList);

        /*setOp(OP_UPDATE);*/
        return Outcome.SUCCESS;
    }

    public void loadSupplies(){
        setIngredientSupplyList(new ArrayList<Supply>());
        for (FormulationInput formulationInput : this.formulation.getFormulationInputList()){
            Supply supply = new Supply();
            supply.setProductItemCode(formulationInput.getProductItemCode());
            supply.setProductItem(formulationInput.getProductItem());
            supply.setQuantity(formulationInput.getQuantity());
            ingredientSupplyList.add(supply);
        }
    }

    public void addMaterialProductItems(List<ProductItem> productItems) {
        for (ProductItem productItem : productItems) {
            if (materialSupplyList.contains(productItem.getProductItemCode())) {
                continue;
            }

            Supply supply = new Supply();
            supply.setProductItemCode(productItem.getProductItemCode());
            supply.setProductItem(productItem);

            materialSupplyList.add(supply);
        }
    }

    public void addIngredientItems(List<ProductItem> productItems) {
        for (ProductItem productItem : productItems) {
            if (ingredientSupplyList.contains(productItem.getProductItemCode())) {
                continue;
            }

            Supply supply = new Supply();
            supply.setProductItemCode(productItem.getProductItemCode());
            supply.setProductItem(productItem);

            ingredientSupplyList.add(supply);
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

    public ProductionPlan getProductionPlan() {
        return productionPlan;
    }

    public void setProductionPlan(ProductionPlan productionPlan) {
        this.productionPlan = productionPlan;
    }

    public List<Supply> getIngredientSupplyList() {
        return ingredientSupplyList;
    }

    public void setIngredientSupplyList(List<Supply> ingredientSupplyList) {
        this.ingredientSupplyList = ingredientSupplyList;
    }

    public List<Supply> getMaterialSupplyList() {
        return materialSupplyList;
    }

    public void setMaterialSupplyList(List<Supply> materialSupplyList) {
        this.materialSupplyList = materialSupplyList;
    }
}
