package com.encens.khipus.action.production;

import com.encens.khipus.framework.action.GenericAction;
import com.encens.khipus.framework.action.Outcome;
import com.encens.khipus.model.production.*;
import com.encens.khipus.model.warehouse.ProductItem;
import com.encens.khipus.service.common.SequenceService;
import com.encens.khipus.service.production.ProductionPlanService;
import com.encens.khipus.service.production.ProductionService;
import com.encens.khipus.util.BigDecimalUtil;
import com.encens.khipus.util.Constants;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.*;
import org.jboss.seam.international.StatusMessage;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
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
    private ProductionPlanService productionPlanService;
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
        setFormulation(getInstance().getFormulation());
        setProductionTank(getInstance().getProductionTank());
        setIngredientSupplyList(productionService.getSupplyList(getInstance(), SupplyType.INGREDIENT));
        setMaterialSupplyList(productionService.getSupplyList(getInstance(), SupplyType.MATERIAL));

        return outCome;
    }

    @Override
    public String create() {

        Production production = getInstance();
        production.setState(ProductionState.PEN);
        production.setProductionTank(productionTank);
        production.setFormulation(formulation);
        production.setProductionPlan(productionPlan);

        Long seq = sequenceService.createOrUpdateNextSequenceValue(Constants.PRODUCTION_CODE);
        production.setCode(seq.intValue());
        productionService.createProduction(production, ingredientSupplyList, materialSupplyList);

        /*setOp(OP_UPDATE);*/
        super.select(production);
        return Outcome.SUCCESS;
    }

    @Override
    public String update() {
        Production production = getInstance();
        production.setProductionTank(productionTank);
        production.setFormulation(formulation);

        productionService.updateProduction(production, ingredientSupplyList, materialSupplyList);

        return Outcome.SUCCESS;
    }

    @Override
    @End(beforeRedirect = true)
    public String cancel() {
        clearAction();
        return Outcome.CANCEL;
    }

    public void approve(){

        getInstance().setState(ProductionState.APR);
        productionService.updateProduction(getInstance(), ingredientSupplyList, materialSupplyList);

        facesMessages.addFromResourceBundle(StatusMessage.Severity.INFO,"Production.message.approveProduction");
    }

    public boolean isPending(){
        boolean result = false;
        if (isManaged()) {
            if (getInstance().getState().equals(ProductionState.PEN))
                result = true;
        }
        return result;
    }

    public boolean isApproved(){
        boolean result = false;
        if (isManaged()) {
            if (getInstance().getState().equals(ProductionState.APR))
                result = true;
        }
        return result;
    }

    public void clearAction(){
        setOp(null);
        setInstance(null);
        setProductionTank(null);
        setFormulation(null);
        setIngredientSupplyList(new ArrayList<Supply>());
        setMaterialSupplyList(new ArrayList<Supply>());
    }

    public void loadSupplies(){
        setIngredientSupplyList(new ArrayList<Supply>());
        for (FormulationInput formulationInput : this.formulation.getFormulationInputList()){
            Supply supply = new Supply();
            supply.setProductItemCode(formulationInput.getProductItemCode());
            supply.setProductItem(formulationInput.getProductItem());
            supply.setQuantity(formulationInput.getQuantity());
            supply.setFormulationInput(formulationInput);
            ingredientSupplyList.add(supply);
        }
    }

    public void addMaterialProductItems(List<ProductItem> productItems) {
        for (ProductItem productItem : productItems) {
            /** does not work **/
            if (materialSupplyList.contains(productItem.getProductItemCode())) {
                continue;
            }

            Supply supply = new Supply();
            supply.setProductItemCode(productItem.getProductItemCode());
            supply.setProductItem(productItem);
            supply.setType(SupplyType.MATERIAL);
            materialSupplyList.add(supply);
        }
    }

    public void addIngredientItems(List<ProductItem> productItems) {
        for (ProductItem productItem : productItems) {
            /** does not work **/
            if (ingredientSupplyList.contains(productItem.getProductItemCode())) {
                continue;
            }

            Supply supply = new Supply();
            supply.setProductItemCode(productItem.getProductItemCode());
            supply.setProductItem(productItem);
            supply.setType(SupplyType.INGREDIENT);
            ingredientSupplyList.add(supply);
        }
    }

    public List<ProductionProduct> getProductionProductList(){
        List<ProductionProduct> productionProductList = productionPlanService.getProductionProductList(getInstance().getProductionPlan());
        return  productionProductList;
    }

    public boolean existProductionProducts(){
        boolean result = false;
        if (this.getProductionProductList().size() > 0)
            result = true;

        return result;
    }

    public void assignProduct(ProductionProduct product){
        productionService.assignProduct(getInstance(), product);
        addMaterialDefault(product, product.getQuantity());
    }

    private void addMaterialDefault(ProductionProduct product, BigDecimal quantity){

        List<MaterialInput> materialInputList = productionService.getMaterialInput(product.getProductItemCode());

        for (MaterialInput materialInput : materialInputList){
            Supply supply = new Supply();
            supply.setProductItemCode(materialInput.getProductItemMaterialCode());
            supply.setProductItem(materialInput.getProductItemMaterial());

            if (materialInput.getQuantityFlag())
                supply.setQuantity(quantity);
            else
                supply.setQuantity(BigDecimal.ZERO);

            supply.setProductionProduct(product);

            materialSupplyList.add(supply);
            productionService.assignMaterial(getInstance(), supply);
        }

    }

    public void removeSupply(Supply supply){
        productionService.removeSupply(supply);
        if (supply.getType().equals(SupplyType.INGREDIENT))
            ingredientSupplyList.remove(supply);
        if (supply.getType().equals(SupplyType.MATERIAL))
            materialSupplyList.remove(supply);
    }

    public void removeProductionProduct(ProductionProduct product){
        productionService.removeProductionProduct(product, getInstance());
    }

    public boolean hasFormula(Supply supply){

        boolean result = false;
        if (supply.getFormulationInput() != null)
            result = true;

        return result;
    }

    public void recalculateSupplies(){

        List<FormulationInput> formulationInputList = getInstance().getFormulation().getFormulationInputList();

        for (FormulationInput formulationInput : formulationInputList){
            System.out.println("--> " + formulationInput.getProductItem().getFullName() + " - " + formulationInput.getQuantity() + " - " + formulationInput.getInputDefault());
        }

        HashMap<String, BigDecimal> formulationInputMap = new HashMap<String, BigDecimal>();
        HashMap<String, BigDecimal> supplyMap = new HashMap<String, BigDecimal>();

        String defaultInputCode = "";

        for (FormulationInput formulationInput : formulationInputList){
            formulationInputMap.put(formulationInput.getProductItemCode(), formulationInput.getQuantity());
            if (formulationInput.getInputDefault())
                defaultInputCode = formulationInput.getProductItemCode();
        }

        for (Supply supply : ingredientSupplyList){
            supplyMap.put(supply.getProductItemCode(), supply.getQuantity());
        }

        //BigDecimal defaultSupplyInput = supplyMap.get(defaultInputCode);
        for (Supply supply : ingredientSupplyList){
            if (!supply.getProductItemCode().equals(defaultInputCode) && supply.getFormulationInput() != null){
                String supplyCode = supply.getProductItemCode();
                if (formulationInputMap.get(defaultInputCode).doubleValue() > 0) {
                    BigDecimal newQuantity = BigDecimalUtil.multiply(formulationInputMap.get(supplyCode), supplyMap.get(defaultInputCode), 6);
                    newQuantity = BigDecimalUtil.divide(newQuantity, formulationInputMap.get(defaultInputCode), 6);
                    supply.setQuantity(newQuantity);
                }else
                    supply.setQuantity(BigDecimal.ZERO);
            }
            System.out.println("-------> Recalculado: " + supply.getProductItem().getFullName() + " - " + supply.getQuantity());
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
