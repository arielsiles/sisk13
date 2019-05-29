package com.encens.khipus.action.production;

import com.encens.khipus.framework.action.GenericAction;
import com.encens.khipus.framework.action.Outcome;
import com.encens.khipus.model.employees.Gestion;
import com.encens.khipus.model.employees.Month;
import com.encens.khipus.model.production.*;
import com.encens.khipus.model.warehouse.ProductItem;
import com.encens.khipus.service.production.IndirectCostsService;
import com.encens.khipus.service.production.PeriodIndirectCostService;
import com.encens.khipus.service.production.ProductionPlanService;
import com.encens.khipus.service.production.ProductionService;
import com.encens.khipus.util.BigDecimalUtil;
import com.encens.khipus.util.Constants;
import com.encens.khipus.util.DateUtils;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.*;
import org.jboss.seam.international.StatusMessage;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


@Name("productionPlanAction")
@Scope(ScopeType.CONVERSATION)
public class ProductionPlanAction extends GenericAction<ProductionPlan> {

    @In(create = true)
    private ProductionAction productionAction;
    @In
    private ProductionPlanService productionPlanService;
    @In
    private PeriodIndirectCostService periodIndirectCostService;
    @In
    private IndirectCostsService indirectCostsService;

    @In
    private ProductionService productionService;

    private List<ProductionProduct> productList = new ArrayList<ProductionProduct>();

    /*private Date startDate;
    private Date endDate;*/
    private BigDecimal totalIndirectCost;
    private BigDecimal totalVolumePeriod;

    private Gestion gestion;
    private Month month;

    @Factory(value = "productionPlan", scope = ScopeType.STATELESS)
    public ProductionPlan initProductionPlan() {
        return getInstance();
    }

    @Override
    @Begin(nested=true, ifOutcome = Outcome.SUCCESS, flushMode = FlushModeType.MANUAL)
    public String select(ProductionPlan instance) {
        String outCome = super.select(instance);
        setProductList(getInstance().getProductionProductList());

        return outCome;
    }

    @Factory(value = "monthsEnum")
    public Month[] getMonthEnum() {
        return Month.values();
    }

    @Override
    @Begin(nested=true, ifOutcome = Outcome.SUCCESS, flushMode = FlushModeType.MANUAL)
    public String create() {
        getInstance().setState(ProductionPlanState.PEN);
        String outcome = super.create();
        setOp(OP_UPDATE);
        return outcome;
    }

    @Override
    public String update() {
        productionPlanService.updateProductionPlan(getInstance(), productList);
        addUpdatedMessage();
        return Outcome.SUCCESS;
    }

    @Begin(nested = true, flushMode = FlushModeType.MANUAL)
    public String addProduction() {
        productionAction.setProductionPlan(getInstance());
        /*setOp(OP_UPDATE);*/
        return Outcome.SUCCESS;
    }

    public void calculateTotalInditectCost(){
        PeriodIndirectCost periodIndirectCost = periodIndirectCostService.findPeriodIndirect(this.month, this.gestion);
        this.totalIndirectCost = indirectCostsService.getTotalIndirectCostByPeriod(periodIndirectCost);
    }

    /** Start Proceso Distribucion de costos indirectos (1) **/
    public void processIndirectCostDistribution(){

        PeriodIndirectCost periodIndirectCost = periodIndirectCostService.findPeriodIndirect(this.month, this.gestion);

        if (periodIndirectCost.getProcessed()){
            facesMessages.addFromResourceBundle(StatusMessage.Severity.WARN,"Production.message.distributedIndirectCosts");
            return;
        }

        this.totalIndirectCost = indirectCostsService.getTotalIndirectCostByPeriod(periodIndirectCost);
        Date startDate = DateUtils.getFirstDayOfMonth(this.month.getValueAsPosition(), this.gestion.getYear(), 0);
        Date endDate = DateUtils.getLastDayOfMonth(startDate);

        System.out.println("ºººººººººº>>> firstDayOfMonth: " + startDate);
        System.out.println("ºººººººººº>>> lastDayOfMonth: " + endDate);

        // 1. Obtener las producciones del mes
        // Calcula el Volumen total por Plan de produccion (x dia)
        // 2. Calcular el volumen Total de las produccion del mes
        List<ProductionPlan> productionPlanList = productionPlanService.getProductionPlanList(startDate, endDate);

        BigDecimal totalVolume = BigDecimal.ZERO;
        Boolean flagState = Boolean.TRUE;
        for (ProductionPlan productionPlan : productionPlanList){
            totalVolume = BigDecimalUtil.sum(totalVolume, calculateTotalVolumePlan(productionPlan), 2);
            if (!productionPlan.getState().equals(ProductionPlanState.APR)){
                flagState = Boolean.FALSE;
            }
        }

        if (!flagState){
            facesMessages.addFromResourceBundle(StatusMessage.Severity.WARN,"Production.message.pendingProductionOrders");
            return;
        }


        this.totalVolumePeriod = totalVolume;
        System.out.println("=-=-=-=---> totalIndirectCost: " + totalIndirectCost);
        System.out.println("=-=-=-=---> TotalVolumePlan: " + totalVolumePeriod);
        // 3. Calcular los porcentajes por dia de produccion
        BigDecimal totalDistribution = BigDecimal.ZERO;
        for (ProductionPlan productionPlan : productionPlanList){

            BigDecimal volumeDay = calculateTotalVolumePlan(productionPlan);
            BigDecimal percentageDay = BigDecimalUtil.multiply(volumeDay, BigDecimalUtil.toBigDecimal(100), 2);
            percentageDay = BigDecimalUtil.divide(percentageDay, totalVolumePeriod, 2);
            BigDecimal distributionDay = BigDecimalUtil.multiply(totalIndirectCost, (BigDecimalUtil.divide(percentageDay, BigDecimalUtil.toBigDecimal(100), 4)), 2);

            totalDistribution = BigDecimalUtil.sum(totalDistribution, distributionDay, 2);

            System.out.println("=-=-=-=---> Fecha: " + productionPlan.getDate() + " - " + volumeDay + " - " + percentageDay + " = " + distributionDay);
            distributionCostByDay(productionPlan, distributionDay);
        }
        System.out.println("=-=-=-=---> Total Dist: " + totalDistribution);

        // 4. Distribuir costos por dia
        // 5. Distribuir costos por producto
        // ----------------------------------
        /** Actualizando el costo unitario de los productos **/
        List<Supply> emptyList = new ArrayList<Supply>();
        for (ProductionPlan productionPlan : productionPlanList){
            System.out.println("-.-.-.-.-.-.-.-.-.----> Plan: " + productionPlan.getDate());
            for (Production production : productionPlan.getProductionList()){
                for (ProductionProduct product : production.getProductionProductList()){
                    BigDecimal  productCost = BigDecimalUtil.sum(product.getCostA(), product.getCostB(), 2);
                                productCost = BigDecimalUtil.sum(productCost, product.getCostC(), 2);

                    product.setCost(productCost);
                    product.setUnitCost(BigDecimalUtil.divide(productCost, product.getQuantity(), 2));
                }
                production.setState(ProductionState.FIN);
                productionService.updateProduction(production, emptyList, emptyList);
            }
            productionPlan.setState(ProductionPlanState.FIN);
            productionPlanService.updateProductionPlan(productionPlan, new ArrayList<ProductionProduct>());
        }
        periodIndirectCost.setProcessed(Boolean.TRUE);
        periodIndirectCostService.updatePeriodIndirectCost(periodIndirectCost);
        facesMessages.addFromResourceBundle(StatusMessage.Severity.INFO,"Production.message.indirectCostsCompleted");
    }
    /** Proceso Distribucion de costos indirectos (2) **/
    public void distributionCostByDay(ProductionPlan productionPlan, BigDecimal distributionDay){

        BigDecimal volumePlan = calculateTotalVolumePlan(productionPlan);
        BigDecimal totalDistribution = BigDecimal.ZERO;
        for (Production production : productionPlan.getProductionList()){
            BigDecimal volumeProduction = productionAction.calculateTotalVolume(production);
            BigDecimal percentage = BigDecimalUtil.multiply(volumeProduction, BigDecimalUtil.toBigDecimal(100), 2);
                       percentage = BigDecimalUtil.divide(percentage, volumePlan, 2);

            BigDecimal distributionProduction = BigDecimalUtil.multiply(distributionDay, (BigDecimalUtil.divide(percentage, BigDecimalUtil.toBigDecimal(100), 4)), 2);
            totalDistribution = BigDecimalUtil.sum(totalDistribution, distributionProduction, 2);
            System.out.println("-------==============> Dist: " + volumeProduction + " - " + percentage + " = " + distributionProduction);
            distributionCostbyProduction(production, distributionProduction);
        }
        System.out.println("-------==============> Total Dist: " + totalDistribution);
    }
    /** Proceso Distribucion de costos indirectos (3) **/
    public void distributionCostbyProduction(Production production, BigDecimal distributionProduction){

        BigDecimal volumeProduction = productionAction.calculateTotalVolume(production);
        BigDecimal totalDistribution = BigDecimal.ZERO;
        for (ProductionProduct product : production.getProductionProductList()){
            BigDecimal volumeProduct = BigDecimalUtil.multiply(product.getQuantity(), product.getProductItem().getBasicQuantity(), 2);
            BigDecimal percentage = BigDecimalUtil.multiply(volumeProduct, BigDecimalUtil.toBigDecimal(100), 2);
                       percentage = BigDecimalUtil.divide(percentage, volumeProduction, 2);

            BigDecimal distributionProduct = BigDecimalUtil.multiply(distributionProduction, BigDecimalUtil.divide(percentage, BigDecimalUtil.toBigDecimal(100), 4), 2);
            totalDistribution = BigDecimalUtil.sum(totalDistribution, distributionProduct, 2);
            System.out.println("-------==============--------------------> Dist: " + product.getProductItem().getFullName() + " - " + volumeProduct + " - " + percentage + " = " + distributionProduct);
            product.setCostC(distributionProduct);
        }
        System.out.println("-------==============--------------------> Total Dist: " + totalDistribution);
    }
    /** End Proceso Distribucion de costos indirectos **/


    public BigDecimal calculateTotalVolumePlan(ProductionPlan productionPlan){

        BigDecimal result = BigDecimal.ZERO;
        for (Production production : productionPlan.getProductionList()){
            result = BigDecimalUtil.sum(result, productionAction.calculateTotalVolume(production), 2);
        }
        return result;
    }

    public void changePlanStatus(ProductionPlan productionPlan){
        ProductionPlanState result = ProductionPlanState.APR;
        for (Production production : productionPlan.getProductionList()){
            if (!production.getState().equals(ProductionState.APR))
                result = ProductionPlanState.PEN;
        }
        productionPlan.setState(result);
    }

    public List<ProductionProduct> getProductList() {
        return productList;
    }

    public void setProductList(List<ProductionProduct> productList) {
        this.productList = productList;
    }

    public void addProductItems(List<ProductItem> productItems){

        for (ProductItem productItem : productItems) {
            ProductionProduct product = new ProductionProduct();
            product.setProductItemCode(productItem.getProductItemCode());
            product.setProductItem(productItem);
            productList.add(product);
        }
    }

    public String hasProduction(ProductionProduct productionProduct){

        String result = "NO";
        if (productionProduct.getProduction() != null)
            result = "SI";

        return  result;
    }

    public boolean isPending(){
        boolean result = false;
        if (isManaged()) {
            if (getInstance().getState().equals(ProductionPlanState.PEN))
                result = true;
        }
        return result;
    }

    public BigDecimal getDestinedMilk(Production production){

        BigDecimal result = BigDecimal.ZERO;

        for (Supply supply : production.getSupplyList()){
            if (supply.getProductItemCode().equals(Constants.ID_ART_RAW_MILK))
                result = BigDecimalUtil.sum(result, supply.getQuantity(), 2);
        }
        return result;
    }

    public BigDecimal calculateTotalRawMaterial(ProductionPlan productionPlan){

        BigDecimal result = BigDecimal.ZERO;

        for (Production production : productionPlan.getProductionList()){
            productionAction.setInstance(production);
            productionAction.setIngredientSupplyList(production.getSupplyList());
            result = BigDecimalUtil.sum(result, productionAction.calculateRawMaterial(), 2);
        }
        return result;

    }

    public double getTotalWeighed(ProductionPlan productionPlan){

        double result = productionPlanService.findTotalWeighed(productionPlan.getDate());
        return result;

    }

    public BigDecimal getTotalIndirectCost() {
        return totalIndirectCost;
    }

    public void setTotalIndirectCost(BigDecimal totalIndirectCost) {
        this.totalIndirectCost = totalIndirectCost;
    }

    public Gestion getGestion() {
        return gestion;
    }

    public void setGestion(Gestion gestion) {
        this.gestion = gestion;
    }

    public Month getMonth() {
        return month;
    }

    public void setMonth(Month month) {
        this.month = month;
    }

    public BigDecimal getTotalVolumePeriod() {
        return totalVolumePeriod;
    }

    public void setTotalVolumePeriod(BigDecimal totalVolumePeriod) {
        this.totalVolumePeriod = totalVolumePeriod;
    }
}
