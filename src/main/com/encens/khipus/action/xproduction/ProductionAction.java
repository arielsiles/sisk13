package com.encens.khipus.action.xproduction;

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

    //private BigDecimal totalCost;
    //private BigDecimal totalRawMaterial;

    private Supply supplyAssign;

    @In
    private ProductionPlanAction productionPlanAction;

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

        //System.out.println("-------> Update Total cost: " + calculateTotalCost());
        //System.out.println("-------> Update Total mil: " + calculateRawMaterial());
        production.setTotalCost(calculateTotalCost());
        production.setTotalRawMaterial(calculateRawMaterial());
        productionService.updateProduction(production, ingredientSupplyList, materialSupplyList);

        return Outcome.SUCCESS;
    }

    @Override
    public String delete() {
        productionService.deleteProduction(getInstance());
        addDeletedMessage();
        return Outcome.SUCCESS;
    }

    @Override
    @End(beforeRedirect = true)
    public String cancel() {
        clearAction();
        return Outcome.CANCEL;
    }

    public void disapprove(){
        getInstance().setState(ProductionState.PEN);
        productionPlanAction.changePlanStatus(getInstance().getProductionPlan());
        productionService.updateProduction(getInstance(), ingredientSupplyList, materialSupplyList);
        facesMessages.addFromResourceBundle(StatusMessage.Severity.INFO,"Production.message.disapproveProduction");
    }
    /**
     * Al aprobar la produccion calcula los costos y lo distribuye por cada producto en la produccion
     */
    public void approve(){

        for (ProductionProduct product : getInstance().getProductionProductList()){
            BigDecimal productCost = BigDecimal.ZERO;
            /** Calcula para los insumos asignados a un producto especifico **/
            for (Supply ingredient : this.ingredientSupplyList){
                if (ingredient.getProductionProduct() != null) {
                    System.out.println("***Yes 1: " + ingredient.getProductionProduct().getProductItemCode());
                    if (product.getProductItemCode().equals(ingredient.getProductionProduct().getProductItem().getProductItemCode())) {
                        System.out.println("***Yes 2: " + ingredient.getProductionProduct().getProductItemCode());
                        //BigDecimal productCost = product.getCost();
                        BigDecimal ingredientCost = BigDecimalUtil.multiply(ingredient.getQuantity(), ingredient.getUnitCost(), 6);
                        productCost = BigDecimalUtil.sum(productCost, ingredientCost);
                        productCost = BigDecimalUtil.roundBigDecimal(productCost, 2);
                        product.setCostA(productCost);
                    }
                }
            }

            /** Calcula para los materiales asignados a un producto especifico **/
            for (Supply material : this.materialSupplyList){
                if (material.getProductionProduct() != null) {
                    System.out.println("***Yesss 1: " + material.getProductionProduct().getProductItemCode());
                    if (product.getProductItemCode().equals(material.getProductionProduct().getProductItem().getProductItemCode())) {
                        System.out.println("***Yesss 2: " + material.getProductionProduct().getProductItemCode());
                        //BigDecimal productCost = product.getCost();
                        BigDecimal materialCost = BigDecimalUtil.multiply(material.getQuantity(), material.getUnitCost(), 6);
                        productCost = BigDecimalUtil.sum(productCost, materialCost);
                        productCost = BigDecimalUtil.roundBigDecimal(productCost, 2);
                        product.setCostA(productCost);
                    }
                }
            }
            System.out.println("-*-*-*-*-*-*-*---> Costo Producto: " + product.getProductItem().getFullName() + " - " + product.getCost());

        }

        /** Calculando Costo Restante */
        BigDecimal remainingCost = BigDecimal.ZERO;
        for (Supply ingredient : this.ingredientSupplyList){
            if (ingredient.getProductionProduct() == null) {
                BigDecimal ingredientCost = BigDecimalUtil.multiply(ingredient.getQuantity(), ingredient.getUnitCost(), 6);
                remainingCost = BigDecimalUtil.sum(remainingCost, ingredientCost, 6);
                remainingCost = BigDecimalUtil.roundBigDecimal(remainingCost, 2);
            }
        }

        System.out.println("====> TOTAL INGREDIENT: " + remainingCost);
        System.out.println("====> TOTAL remainingCost: " + remainingCost);

        for (Supply material : this.materialSupplyList){
            if (material.getProductionProduct() == null) {
                BigDecimal materialCost = BigDecimalUtil.multiply(material.getQuantity(), material.getUnitCost(), 6);
                remainingCost = BigDecimalUtil.sum(remainingCost, materialCost, 6);
                remainingCost = BigDecimalUtil.roundBigDecimal(remainingCost, 2);
            }
        }
        System.out.println("====> TOTAL remainingCost: " + remainingCost);
        /** end **/

        /** Calculando volumen total de lo productos */
        BigDecimal totalVolume = calculateTotalVolume(getInstance());
        System.out.println("====> TOTAL VOLUME: " + totalVolume);

        for (ProductionProduct product : getInstance().getProductionProductList()){
            BigDecimal productCost = BigDecimal.ZERO;
            BigDecimal productVolume     = BigDecimalUtil.multiply(product.getQuantity(), product.getProductItem().getBasicQuantity(), 2);
            BigDecimal productPercentage = BigDecimalUtil.multiply(productVolume, BigDecimalUtil.toBigDecimal(100), 2);
                       productPercentage = BigDecimalUtil.divide(productPercentage, totalVolume, 2);

                       productPercentage = BigDecimalUtil.divide(productPercentage, BigDecimalUtil.ONE_HUNDRED, 6);

            productCost = BigDecimalUtil.multiply(remainingCost, productPercentage, 2);
            System.out.println("====> TOTAL VOLUME producto: " +
                    product.getProductItem().getFullName() + " : " +
                    productPercentage + " x " + remainingCost + " = " + productCost);
            product.setCostB(productCost);
        }

        System.out.println("-*-*-*-*-*-*-*---> Costo restante: " + remainingCost);

        updateUnitCostProducts(getInstance());
        getInstance().setState(ProductionState.APR);
        productionPlanAction.changePlanStatus(getInstance().getProductionPlan());

        productionService.updateProduction(getInstance(), ingredientSupplyList, materialSupplyList);
        facesMessages.addFromResourceBundle(StatusMessage.Severity.INFO,"Production.message.approveProduction");
    }

    /** Calcula el VOLUMEN TOTAL de una produccion **/
    public BigDecimal calculateTotalVolume(Production production){
        BigDecimal totalVolume = BigDecimal.ZERO;
        for (ProductionProduct product : production.getProductionProductList()){
            BigDecimal productVolume   = BigDecimalUtil.multiply(product.getQuantity(), product.getProductItem().getBasicQuantity(), 2);
            totalVolume = BigDecimalUtil.sum(totalVolume, productVolume, 2);
        }
        return totalVolume;
    }

    public void updateUnitCostProducts(Production production){

        for (ProductionProduct product : production.getProductionProductList()){
            BigDecimal totalCostProduct = BigDecimal.ZERO;
            totalCostProduct = BigDecimalUtil.sum(product.getCostA(), product.getCostB(), 2);
            totalCostProduct = BigDecimalUtil.sum(totalCostProduct, product.getCostC(), 2);
            product.setCost(totalCostProduct);
            product.setUnitCost(BigDecimalUtil.divide(totalCostProduct, product.getQuantity(), 2));
        }

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
            //supply.setUnitCost(formulationInput.getProductItem().getUnitCost());
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
            //supply.setUnitCost(productItem.getUnitCost());
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
            supply.setQuantity(BigDecimal.ZERO);
            supply.setType(SupplyType.INGREDIENT);
            ingredientSupplyList.add(supply);
        }
    }


    public BigDecimal getSupplyUnitCost(Supply supplyDetail){

        System.out.println("====> " + supplyDetail.getId());
        System.out.println("====> cod_art: " + supplyDetail.getProductItemCode());
        System.out.println("====> " + supplyDetail.getProductItem().getFullName());
        BigDecimal unitCost = supplyDetail.getProductItem().getUnitCost();

        if (supplyDetail.hasFormula() ){
            //unitCost = supplyDetail.getProductItem().getUnitCost();
            if (supplyDetail.getFormulationInput().hasSecondFormula()){
                unitCost = calculateCost_compoundSupply(supplyDetail);
                supplyDetail.setUnitCost(unitCost);
            }
        }

        return unitCost;
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

    public void assignSupply(Supply supply){
        setSupplyAssign(supply);
    }

    public void assignProductAffect(){

        System.out.println("============> Afecta: " + supplyAssign.getProductItem().getFullName());

    }

    private void addMaterialDefault(ProductionProduct product, BigDecimal quantity){

        List<MaterialInput> materialInputList = productionService.getIngredientOrMaterialInput(product.getProductItemCode(), SupplyType.MATERIAL);
        List<MaterialInput> ingredientInputList = productionService.getIngredientOrMaterialInput(product.getProductItemCode(), SupplyType.INGREDIENT);

        for (MaterialInput materialInput : materialInputList){
            Supply supply = new Supply();
            supply.setProductItemCode(materialInput.getProductItemMaterialCode());
            supply.setProductItem(materialInput.getProductItemMaterial());

            if (materialInput.getQuantityFlag())
                supply.setQuantity(quantity);
            else
                supply.setQuantity(BigDecimal.ZERO);

            supply.setProductionProduct(product);
            supply.setType(SupplyType.MATERIAL);

            materialSupplyList.add(supply);
            productionService.assignMaterial(getInstance(), supply);
        }

        for (MaterialInput ingredientInput : ingredientInputList){
            Supply supply = new Supply();
            supply.setProductItemCode(ingredientInput.getProductItemMaterialCode());
            supply.setProductItem(ingredientInput.getProductItemMaterial());

            BigDecimal weightTwo = BigDecimal.ZERO;

            if (ingredientInput.getVolumeOne() != null && ingredientInput.getWeightOne() != null) {

                if (product.getProductItem().getBasicMeasure().equals(MeasurementUnit.GR)) {}

                if (product.getProductItem().getBasicMeasure().equals(MeasurementUnit.ML)) {
                    BigDecimal basicLitre = BigDecimalUtil.divide(product.getProductItem().getBasicQuantity(), BigDecimalUtil.ONE_THOUSAND);
                    BigDecimal volumeTwo = BigDecimalUtil.multiply(product.getQuantity(), basicLitre); /** Calculado volumen 2 **/
                    weightTwo = BigDecimalUtil.multiply(volumeTwo, ingredientInput.getWeightOne()); /** vol2 * peso1 **/
                    weightTwo = BigDecimalUtil.divide(weightTwo, ingredientInput.getVolumeOne());
                }
            }

            supply.setQuantity(weightTwo);

            supply.setProductionProduct(product);
            supply.setType(SupplyType.INGREDIENT);

            ingredientSupplyList.add(supply);
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
                }
            }
            System.out.println("-------> Recalculado: " + supply.getProductItem().getFullName() + " - " + supply.getQuantity());
        }

        getInstance().setTotalCost(calculateTotalCost());
        getInstance().setTotalRawMaterial(calculateRawMaterial());

    }


    public BigDecimal calculateCost_compoundSupply(Supply supplyDetail){

        BigDecimal quantityParam = supplyDetail.getQuantity();
        System.out.println("=======> Supply quantityParam: " + supplyDetail.getProductItem().getFullName() + " - Q: " + quantityParam);
        Formulation formulation = supplyDetail.getFormulationInput().getSecondFormulation();

        HashMap<String, BigDecimal> formulationInputMap = new HashMap<String, BigDecimal>();
        for (FormulationInput formulationInput : formulation.getFormulationInputList()){
            BigDecimal quantityVal = formulationInput.getQuantity();
            formulationInputMap.put(formulationInput.getProductItemCode(), quantityVal);
        }

        BigDecimal totalCost = BigDecimal.ZERO;
        for (FormulationInput formulationInput : formulation.getFormulationInputList()){
            BigDecimal quantityFormulationInput = formulationInputMap.get(formulationInput.getProductItemCode());
            BigDecimal newQuantity = BigDecimalUtil.multiply(quantityParam, quantityFormulationInput, 6);
                       newQuantity = BigDecimalUtil.divide(newQuantity, formulation.getTotalEquivalent(), 6);

            BigDecimal cost = BigDecimalUtil.multiply(newQuantity, formulationInput.getProductItem().getUnitCost(), 6);
            totalCost = BigDecimalUtil.sum(totalCost, cost, 6);

            System.out.println("=======> " + formulationInput.getProductItem().getFullName() + " - " + newQuantity + " - " + cost);
        }
        System.out.println("=======> Costo Total: " + totalCost);
        System.out.println("=======> Costo unit: " + BigDecimalUtil.divide(totalCost, quantityParam, 6));

        return BigDecimalUtil.divide(totalCost, quantityParam, 6);
    }

    public BigDecimal calculateTotalRawMaterial_compoundSupply(Supply supplyDetail){

        BigDecimal quantityParam = supplyDetail.getQuantity();
        Formulation formulation = supplyDetail.getFormulationInput().getSecondFormulation();

        HashMap<String, BigDecimal> formulationInputMap = new HashMap<String, BigDecimal>();
        for (FormulationInput formulationInput : formulation.getFormulationInputList()){
            BigDecimal quantityVal = formulationInput.getQuantity();
            formulationInputMap.put(formulationInput.getProductItemCode(), quantityVal);
        }

        BigDecimal totalRawMaterial = BigDecimal.ZERO;
        for (FormulationInput formulationInput : formulation.getFormulationInputList()){
            BigDecimal quantityFormulationInput = formulationInputMap.get(formulationInput.getProductItemCode());
            BigDecimal newQuantity = BigDecimalUtil.multiply(quantityParam, quantityFormulationInput, 6);
            newQuantity = BigDecimalUtil.divide(newQuantity, formulation.getTotalEquivalent(), 6);

            if (formulationInput.getProductItemCode().equals(Constants.ID_ART_RAW_MILK)){
                totalRawMaterial = BigDecimalUtil.sum(totalRawMaterial, newQuantity, 6);
            }
        }

        return totalRawMaterial;
    }

    public BigDecimal calculateTotalCost(){
        BigDecimal result = BigDecimal.ZERO;
        BigDecimal ingredientCost = BigDecimal.ZERO;
        BigDecimal materialCost = BigDecimal.ZERO;

        for (Supply supply : ingredientSupplyList){
            BigDecimal cost = BigDecimalUtil.multiply(supply.getQuantity(), supply.getUnitCost(), 6);
            ingredientCost = BigDecimalUtil.sum(ingredientCost, cost, 6);
            //System.out.println("===>>> " + supply.getProductItem().getFullName() + "\t\t\t " + supply.getQuantity() + "\t\t - " + supply.getUnitCost() + "\t\t - " + cost);
        }

        for (Supply supply : materialSupplyList){
            BigDecimal cost = BigDecimalUtil.multiply(supply.getQuantity(), supply.getUnitCost(), 6);
            materialCost = BigDecimalUtil.sum(materialCost, cost, 6);
            //System.out.println("===>>> " + supply.getProductItem().getFullName() + "\t\t\t " + supply.getQuantity() + "\t\t - " + supply.getUnitCost() + "\t\t - " + cost);
        }


        result = BigDecimalUtil.sum(ingredientCost, materialCost, 6);
        result = BigDecimalUtil.roundBigDecimal(result, 2);
        //System.out.println("===>>> TOTAL COST: " + result);

        return result;
    }

    /** Calcula la cantidad total de materia prima (leche) de la produccion **/
    public BigDecimal calculateRawMaterial(){
        BigDecimal result = BigDecimal.ZERO;

        for (Supply supply : ingredientSupplyList){
            if (supply.getProductItemCode().equals(Constants.ID_ART_RAW_MILK))
                result = BigDecimalUtil.sum(result, supply.getQuantity(), 6);

            if (supply.hasFormula()){
                if (supply.getFormulationInput().hasSecondFormula()){
                    result = BigDecimalUtil.sum(result, calculateTotalRawMaterial_compoundSupply(supply), 6);
                }
            }
        }
        result = BigDecimalUtil.roundBigDecimal(result, 2);
        return result;
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

    public Supply getSupplyAssign() {
        return supplyAssign;
    }

    public void setSupplyAssign(Supply supplyAssign) {
        this.supplyAssign = supplyAssign;
    }

    /*public BigDecimal getTotalCost() {
        totalCost = calculateTotalCost();
        return totalCost;
    }

    public void setTotalCost(BigDecimal totalCost) {
        this.totalCost = totalCost;
    }

    public BigDecimal getTotalRawMaterial() {
        this.totalRawMaterial = calculateRawMaterial();
        return totalRawMaterial;
    }

    public void setTotalRawMaterial(BigDecimal totalRawMaterial) {
        this.totalRawMaterial = totalRawMaterial;
    }*/
}
