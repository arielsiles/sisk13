package com.encens.khipus.action.xproduction;


import com.encens.khipus.exception.finances.CompanyConfigurationNotFoundException;
import com.encens.khipus.framework.action.GenericAction;
import com.encens.khipus.framework.action.Outcome;
import com.encens.khipus.model.employees.Gestion;
import com.encens.khipus.model.employees.Month;
import com.encens.khipus.model.finances.*;
import com.encens.khipus.model.production.IndirectCosts;
import com.encens.khipus.model.production.PeriodIndirectCost;
import com.encens.khipus.model.production.ProductionPlanState;
import com.encens.khipus.model.production.ProductionState;
import com.encens.khipus.model.warehouse.ProductItem;
import com.encens.khipus.model.xproduction.*;
import com.encens.khipus.service.accouting.VoucherAccoutingService;
import com.encens.khipus.service.finances.CashAccountService;
import com.encens.khipus.service.fixedassets.CompanyConfigurationService;
import com.encens.khipus.service.production.IndirectCostsService;
import com.encens.khipus.service.production.PeriodIndirectCostService;
import com.encens.khipus.service.warehouse.InventoryService;
import com.encens.khipus.service.xproduction.XProductionPlanService;
import com.encens.khipus.service.xproduction.XProductionService;
import com.encens.khipus.util.BigDecimalUtil;
import com.encens.khipus.util.Constants;
import com.encens.khipus.util.DateUtils;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.*;
import org.jboss.seam.international.StatusMessage;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;


@Name("xproductionPlanAction")
@Scope(ScopeType.CONVERSATION)
public class XProductionPlanAction extends GenericAction<XProductionPlan> {

    @In(create = true)
    private XProductionAction xproductionAction;
    @In
    private XProductionPlanService xproductionPlanService;
    @In
    private PeriodIndirectCostService periodIndirectCostService;
    @In
    private IndirectCostsService indirectCostsService;
    @In
    private VoucherAccoutingService voucherAccoutingService;
    @In
    private XProductionService xproductionService;
    @In
    private CompanyConfigurationService companyConfigurationService;
    @In
    private CashAccountService cashAccountService;

    private List<XProductionProduct> productList = new ArrayList<XProductionProduct>();

    /*private Date startDate;
    private Date endDate;*/
    private BigDecimal totalIndirectCost;
    private BigDecimal totalVolumePeriod;

    private Gestion gestion;
    private Month month;

    private XProductionProduct previousProduct;
    private XProductionProduct productToRemove;
    private XProductionProduct productToSum;
    private XProductionProduct productToSubtract;
    private BigDecimal quantitySum;
    private BigDecimal quantitySubtract;
    private String gloss;

    @Factory(value = "xproductionPlan", scope = ScopeType.STATELESS)
    public XProductionPlan initProductionPlan() {
        return getInstance();
    }

    @Override
    @Begin(nested=true, ifOutcome = Outcome.SUCCESS, flushMode = FlushModeType.MANUAL)
    public String select(XProductionPlan instance) {
        String outCome = super.select(instance);
        setProductList(getInstance().getProductionProductList());

        return outCome;
    }

   /* @Factory(value = "xmonthsEnum")
    public Month[] getMonthEnum() {
        return Month.values();
    }*/

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
        xproductionPlanService.updateProductionPlan(getInstance(), productList);
        addUpdatedMessage();
        return Outcome.SUCCESS;
    }

    @Begin(nested = true, flushMode = FlushModeType.MANUAL)
    public String addProduction() {
        xproductionAction.clearAction();
        xproductionAction.setProductionPlan(getInstance());
        /*setOp(OP_UPDATE);*/
        return Outcome.SUCCESS;
    }

    public void assignProductToSum(XProductionProduct productionProduct) {
        setPreviousProduct(productionProduct);
        productToSum = new XProductionProduct();
        productToSum.setProductItem(productionProduct.getProductItem());
        productToSum.setProductItemCode(productionProduct.getProductItem().getProductItemCode());
        productToSum.setQuantity(BigDecimal.ZERO);
    }

    public void assignProductToSubtract(XProductionProduct productionProduct) {
        setPreviousProduct(productionProduct);
        productToSubtract = new XProductionProduct();
        productToSubtract.setProductItem(productionProduct.getProductItem());
        productToSubtract.setProductItemCode(productionProduct.getProductItem().getProductItemCode());
        productToSubtract.setQuantity(BigDecimal.ZERO);
    }

    public void addQuantityProductionProduct(){
        System.out.println("===================> Product sum: " + this.productToSum.getProductItem().getFullName2() + " - " + this.quantitySum);
        this.productToSum.setQuantity(this.quantitySum);
        previousProduct.setQuantity(BigDecimalUtil.sum(previousProduct.getQuantity(), quantitySum));

        //Actualizar ProductItem
        xproductionPlanService.updateProductForProduction(productToSum);
        //Actualizar inventario
        inventoryService.updateInventoryForProduction(productToSum);

        clearAddProductQuantity();
    }

    public void removeQuantityProductionProduct(){
        System.out.println("===================> Product remove: " + this.productToSubtract.getProductItem().getFullName2() + " - " + this.quantitySubtract);
        this.productToSubtract.setQuantity(this.quantitySubtract);
        previousProduct.setQuantity(BigDecimalUtil.subtract(previousProduct.getQuantity(), quantitySubtract));

        //Actualizar ProductItem
        xproductionPlanService.updateProductItemRemoveFromProduction(productToSubtract);
        //Actualizar inventario
        inventoryService.updateInventoryRemoveFromProduction(productToSubtract);

        clearRemoveProductQuantity();
    }

    public void clearAddProductQuantity(){
        setQuantitySum(BigDecimal.ZERO);
        setGloss(null);
        setPreviousProduct(null);
    }

    public void clearRemoveProductQuantity(){
        setQuantitySubtract(BigDecimal.ZERO);
        setGloss(null);
        setPreviousProduct(null);
    }

    public void calculateTotalInditectCost(){
        PeriodIndirectCost periodIndirectCost = periodIndirectCostService.findPeriodIndirect(this.month, this.gestion);
        this.totalIndirectCost = indirectCostsService.getTotalIndirectCostByPeriod(periodIndirectCost);
    }

    public void accountingProduction() throws CompanyConfigurationNotFoundException{

        PeriodIndirectCost periodIndirectCost = periodIndirectCostService.findPeriodIndirect(this.month, this.gestion);
        if (periodIndirectCost == null){
            facesMessages.addFromResourceBundle(StatusMessage.Severity.WARN,"Production.message.withoutIndirectCost");
            return;
        }

        Date startDate = DateUtils.getFirstDayOfMonth(this.month.getValueAsPosition(), this.gestion.getYear(), 0);
        Date endDate   = DateUtils.getLastDayOfMonth(startDate);
        List<XProductionPlan> productionPlanList = xproductionPlanService.getProductionPlanList(startDate, endDate);

        for (XProductionPlan productionPlan : productionPlanList){
            if (!productionPlan.getState().equals(ProductionPlanState.FIN)){
                facesMessages.addFromResourceBundle(StatusMessage.Severity.WARN,"Production.message.unfinishedProduction");
                return;
            }
        }
        /** todo: Restriccion, controlar que toda la produccion este aprobada **/

        System.out.println("------> COSTOS INDIRECTOS <-------");
        BigDecimal totalIndirectCostValue = indirectCostsService.getTotalIndirectCostByPeriod(periodIndirectCost);
        System.out.println("Total Costo indirecto: " + totalIndirectCostValue);
        List<IndirectAux> indirectAuxList = new ArrayList<IndirectAux>();
        List<IndirectCosts> indirectCostList = periodIndirectCost.getIndirectCostList();
        /** Calcula Lista de costos indirectos con porcentajes **/
        Double test = 0.0;
        for (IndirectCosts param : indirectCostList){
            System.out.println("===> costo indirecto bs: " + param.getAmountBs());
            BigDecimal aux        = BigDecimalUtil.multiply(param.getAmountBs(), BigDecimalUtil.toBigDecimal(100), 6);
            BigDecimal percentage = BigDecimalUtil.divide(aux, totalIndirectCostValue, 4);
            System.out.println("===> percentage: " + percentage);
            IndirectAux indirect  = new IndirectAux(param.getCostsConifg().getAccount(), param.getAmountBs(), percentage, BigDecimal.ZERO);
            indirectAuxList.add(indirect);
            test = test + param.getAmountBs().doubleValue();
        }

        /** Calcula el valor total de costos indirectos, segun productos y porcentajes anteriores **/ // ??? no es necesario
        for (XProductionPlan productionPlan : productionPlanList){
            for (XProduction production : productionPlan.getProductionList()){
                for (XProductionProduct product : production.getProductionProductList()){
                    for (IndirectAux indirectAux : indirectAuxList){
                        BigDecimal percentageDec = BigDecimalUtil.divide(indirectAux.getPercentage(), BigDecimalUtil.toBigDecimal(100), 6);
                        BigDecimal value = BigDecimalUtil.multiply(product.getCostC(), percentageDec, 6);
                        BigDecimal totalValue = indirectAux.getValue();
                        totalValue = BigDecimalUtil.sum(totalValue, value, 6);
                        indirectAux.setValue(totalValue);
                    }
                }
            }
        }

        /** sout **/
        System.out.println("-------INDIRECT AUX LIST------");
        for (IndirectAux indirectAux : indirectAuxList){
            System.out.println("----> " + indirectAux.getAccountCode() + " - " + indirectAux.getAmount() + " - " + indirectAux.getPercentage() + " - " + indirectAux.getValue());
        }


        List<DataVoucherDetail> dataVoucherDetailList = new ArrayList<DataVoucherDetail>();
        CompanyConfiguration companyConfiguration = companyConfigurationService.findCompanyConfiguration();

        for (XProductionPlan productionPlan : productionPlanList){
            for (XProduction production : productionPlan.getProductionList()){

                /** ---- */
                Voucher voucher = new Voucher();
                voucher.setDocumentType(Constants.PD_VOUCHER_DOCTYPE);
                voucher.setGloss("ORDEN DE PRODUCCION EN FECHA " + DateUtils.format(productionPlan.getDate(), "dd/MM/yyyy")+ ", " + production.getFormulation().getCategory().getName());
                voucher.setDate(production.getProductionPlan().getDate());
                /** ---- */

                for (XProductionProduct product : production.getProductionProductList()){
                    BigDecimal productionCost = BigDecimalUtil.sum(product.getCostA(), product.getCostB(), 6);
                               productionCost = BigDecimalUtil.sum(productionCost, product.getCostC(), 6);

                    DataVoucherDetail dataVoucherDetail = new DataVoucherDetail(
                            production.getCode().toString(),
                            companyConfiguration.getCtaAlmPT(),
                            productionCost,
                            BigDecimal.ZERO,
                            product.getProductItemCode(),
                            product.getQuantity());
                    dataVoucherDetailList.add(dataVoucherDetail);

                    VoucherDetail voucherDetail = createVoucherDetail(dataVoucherDetail);
                    voucher.getDetails().add(voucherDetail);

                }

                for (XSupply supply : production.getSupplyList()){

                    if (supply.hasFormula() && supply.getFormulationInput().hasSecondFormula()){

                            BigDecimal quantityParam = supply.getQuantity();
                            XFormulation formulation = supply.getFormulationInput().getSecondFormulation();
                            HashMap<String, BigDecimal> formulationInputMap = new HashMap<String, BigDecimal>();
                            for (XFormulationInput formulationInput : formulation.getFormulationInputList()){
                                BigDecimal quantityVal = formulationInput.getQuantity();
                                formulationInputMap.put(formulationInput.getProductItemCode(), quantityVal);
                            }

                            for (XFormulationInput formulationInput : formulation.getFormulationInputList()){
                                BigDecimal quantityFormulationInput = formulationInputMap.get(formulationInput.getProductItemCode());
                                BigDecimal newQuantity = BigDecimalUtil.multiply(quantityParam, quantityFormulationInput, 6);
                                newQuantity = BigDecimalUtil.divide(newQuantity, formulation.getTotalEquivalent(), 6);

                                BigDecimal cost = BigDecimalUtil.multiply(newQuantity, formulationInput.getProductItem().getUnitCost(), 6);

                                //totalCost = BigDecimalUtil.sum(totalCost, cost, 6);
                                //System.out.println("=======> " + formulationInput.getProductItem().getFullName() + " - " + newQuantity + " - " + cost);

                                DataVoucherDetail dataVoucherDetail = new DataVoucherDetail(
                                        production.getCode().toString(),
                                        //supply.getProductItem().getCashAccount(),
                                        formulationInput.getProductItem().getCashAccount(),
                                        BigDecimal.ZERO,
                                        //BigDecimalUtil.multiply(supply.getQuantity(), supply.getUnitCost(), 6),
                                        cost,
                                        null, null);
                                dataVoucherDetailList.add(dataVoucherDetail);

                                VoucherDetail voucherDetail = createVoucherDetail(dataVoucherDetail);
                                voucher.getDetails().add(voucherDetail);
                            }
                    }else {
                        DataVoucherDetail dataVoucherDetail = new DataVoucherDetail(
                                production.getCode().toString(),
                                supply.getProductItem().getCashAccount(),
                                BigDecimal.ZERO,
                                BigDecimalUtil.multiply(supply.getQuantity(), supply.getUnitCost(), 6), null, null);
                        dataVoucherDetailList.add(dataVoucherDetail);

                        VoucherDetail voucherDetail = createVoucherDetail(dataVoucherDetail);
                        voucher.getDetails().add(voucherDetail);
                    }
                }

                for (IndirectAux indirect : indirectAuxList){
                    BigDecimal percentage = BigDecimalUtil.divide(indirect.getPercentage(), BigDecimalUtil.ONE_HUNDRED, 6);
                    BigDecimal totalCostC     = BigDecimal.ZERO;
                    for (XProductionProduct product : production.getProductionProductList()){
                        totalCostC = BigDecimalUtil.sum(totalCostC, product.getCostC(), 6);
                    }

                    BigDecimal amount = BigDecimalUtil.multiply(percentage, totalCostC);

                    DataVoucherDetail dataVoucherDetail = new DataVoucherDetail(
                            production.getCode().toString(),
                            cashAccountService.findByAccountCode(indirect.getAccountCode()),
                            BigDecimal.ZERO, amount, null, null);
                    dataVoucherDetailList.add(dataVoucherDetail);

                    VoucherDetail voucherDetail = createVoucherDetail(dataVoucherDetail);
                    voucher.getDetails().add(voucherDetail);
                }

                /** Si existe ajusta diferencia de montos del asiento generado **/
                BigDecimal totalDebit = BigDecimal.ZERO;
                BigDecimal totalCredit = BigDecimal.ZERO;
                for (VoucherDetail detail : voucher.getDetails()){
                    totalDebit  = BigDecimalUtil.sum(totalDebit, detail.getDebit(), 2);
                    totalCredit = BigDecimalUtil.sum(totalCredit, detail.getCredit(), 2);
                }
                BigDecimal difference = BigDecimalUtil.subtract(totalDebit, totalCredit, 2);
                if (difference.doubleValue() > 0){
                    BigDecimal debitValue = voucher.getDetails().get(0).getDebit();
                    debitValue = BigDecimalUtil.subtract(debitValue, difference, 2);
                    voucher.getDetails().get(0).setDebit(debitValue);
                }
                if (difference.doubleValue() < 0){
                    BigDecimal debitValue = voucher.getDetails().get(0).getDebit();
                    debitValue = BigDecimalUtil.sum(debitValue, BigDecimalUtil.abs(difference), 2);
                    voucher.getDetails().get(0).setDebit(debitValue);
                }

                if (difference.doubleValue() != 0) {
                    System.out.println("======================> Difference: " + difference + " - " +
                            DateUtils.format(production.getProductionPlan().getDate(), "dd/MM/yyyy") + " - Code: " +
                            production.getCode());

                    facesMessages.addFromResourceBundle(StatusMessage.Severity.WARN,
                            "Production.message.differenceVoucher",
                            DateUtils.format(production.getProductionPlan().getDate(), "dd/MM/yyyy"), production.getCode(), difference);
                }
                /** End **/

                voucherAccoutingService.saveVoucher(voucher);
                production.setVoucher(voucher);
                xproductionService.updateProduction(production, new ArrayList<XSupply>(), new ArrayList<XSupply>(), new ArrayList<XProductionLabor>());
            }
            productionPlan.setState(ProductionPlanState.SUS);
            xproductionPlanService.updateProductionPlan(productionPlan);
        }
        periodIndirectCost.setAccounting(Boolean.TRUE);
        periodIndirectCostService.updatePeriodIndirectCost(periodIndirectCost);
    }

    private VoucherDetail createVoucherDetail(DataVoucherDetail data){
        VoucherDetail voucherDetail = new VoucherDetail();
        voucherDetail.setCashAccount(data.getCashAccount());
        voucherDetail.setAccount(data.getCashAccount().getAccountCode());
        voucherDetail.setCompanyNumber(Constants.defaultCompanyNumber);
        voucherDetail.setDebit(data.getDebit());
        voucherDetail.setCredit(data.getCredit());
        voucherDetail.setCurrency(FinancesCurrencyType.P);
        voucherDetail.setDebitMe(BigDecimal.ZERO);
        voucherDetail.setCreditMe(BigDecimal.ZERO);
        voucherDetail.setProductItemCode(data.getProductItemCode());
        voucherDetail.setQuantityArt(data.getQuantity());
        return voucherDetail;
    }

    public Boolean indirectCostProcesssed(){
        Boolean result = Boolean.FALSE;
        if (this.month != null && this.gestion != null) {
            PeriodIndirectCost periodIndirectCost = periodIndirectCostService.findPeriodIndirect(this.month, this.gestion);
            result = periodIndirectCost.getProcessed();
        }
        return result;
    }

    public Boolean indirectCostAccounting(){
        Boolean result = Boolean.FALSE;
        if (this.month != null && this.gestion != null) {
            PeriodIndirectCost periodIndirectCost = periodIndirectCostService.findPeriodIndirect(this.month, this.gestion);
            result = periodIndirectCost.getAccounting();
        }
        return result;
    }

    /** Start Proceso Distribucion de costos indirectos (1) **/
    public void processIndirectCostDistribution(){

        System.out.println("-------> month: " + this.month);
        System.out.println("-------> gestion: " + this.gestion);

        PeriodIndirectCost periodIndirectCost = periodIndirectCostService.findPeriodIndirectX(this.month.getValue()+1, this.gestion);

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
        List<XProductionPlan> productionPlanList = xproductionPlanService.getProductionPlanList(startDate, endDate);

        BigDecimal totalVolume = BigDecimal.ZERO;
        Boolean flagState = Boolean.TRUE;
        /** Para verificar si las ordenes de produccion estan aprobadas **/
        /** Calcula el total de volumen **/
        for (XProductionPlan productionPlan : productionPlanList){
            totalVolume = BigDecimalUtil.sum(totalVolume, calculateTotalVolumePlan(productionPlan), 2);
            if (!productionPlan.getState().equals(ProductionPlanState.APR)){
                flagState = Boolean.FALSE;
                break;
            }
        }
        if (!flagState){
            facesMessages.addFromResourceBundle(StatusMessage.Severity.WARN,"Production.message.pendingProductionOrders");
            return;
        }
        /** End Para verificar si las ordenes de produccion estan aprobadas **/



        /**  test **/
        for (XProductionPlan productionPlan : productionPlanList){
            for (XProduction production : productionPlan.getProductionList()){
                xproductionAction.select(production);
                xproductionAction.approve();
                System.out.println("-------> aprobando: " + production.getCode());
            }
        }



        this.totalVolumePeriod = totalVolume;
        System.out.println("=-=-=-=---> totalIndirectCost: " + totalIndirectCost);
        System.out.println("=-=-=-=---> TotalVolumePlan: " + totalVolumePeriod);
        // 3. Calcular los porcentajes por dia de produccion
        BigDecimal totalDistribution = BigDecimal.ZERO;
        for (XProductionPlan productionPlan : productionPlanList){
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
        List<XSupply> emptyList = new ArrayList<XSupply>();
        for (XProductionPlan productionPlan : productionPlanList){
            System.out.println("-.-.-.-.-.-.-.-.-.----> Plan: " + productionPlan.getDate());
            for (XProduction production : productionPlan.getProductionList()){
                BigDecimal productionTotalCost = BigDecimal.ZERO;
                for (XProductionProduct product : production.getProductionProductList()){
                    BigDecimal  productCost = BigDecimalUtil.sum(product.getCostA(), product.getCostB(), product.getCostC(), product.getCostMo());

                    product.setCost(productCost);
                    product.setUnitCost(BigDecimalUtil.divide(productCost, product.getQuantity(), 2));
                    productionTotalCost = BigDecimalUtil.sum(productionTotalCost, productCost);
                }
                production.setState(ProductionState.FIN);
                production.setTotalCost(productionTotalCost);
                xproductionService.updateProduction(production, emptyList, emptyList, new ArrayList<XProductionLabor>());
            }
            productionPlan.setState(ProductionPlanState.FIN);
            xproductionPlanService.updateProductionPlan(productionPlan, new ArrayList<XProductionProduct>());
        }
        periodIndirectCost.setProcessed(Boolean.TRUE);
        periodIndirectCostService.updatePeriodIndirectCost(periodIndirectCost);
        facesMessages.addFromResourceBundle(StatusMessage.Severity.INFO,"Production.message.indirectCostsCompleted");
    }
    /** Proceso Distribucion de costos indirectos (2) **/
    public void distributionCostByDay(XProductionPlan productionPlan, BigDecimal distributionDay){

        BigDecimal volumePlan = calculateTotalVolumePlan(productionPlan);
        BigDecimal totalDistribution = BigDecimal.ZERO;
        for (XProduction production : productionPlan.getProductionList()){
            BigDecimal volumeProduction = xproductionAction.calculateTotalVolume(production);
            BigDecimal percentage = BigDecimalUtil.multiply(volumeProduction, BigDecimalUtil.toBigDecimal(100), 2);
                       percentage = BigDecimalUtil.divide(percentage, volumePlan, 2);

            BigDecimal distributionProduction = BigDecimalUtil.multiply(distributionDay, (BigDecimalUtil.divide(percentage, BigDecimalUtil.toBigDecimal(100), 4)), 2);
            totalDistribution = BigDecimalUtil.sum(totalDistribution, distributionProduction, 2);
            System.out.println("-------==============> Dist: " + volumeProduction + " - " + percentage + " = " + distributionProduction);
            distributionCostbyProduction(production, distributionProduction);

            /** Para Mano de Obra **/
            BigDecimal laborCostProduction = calculateLaborProduction(production);
            distributionLaborCostByProduction(production, laborCostProduction);

        }
        System.out.println("-------==============> Total Dist: " + totalDistribution);
    }
    /** Proceso Distribucion de costos indirectos (3) **/
    public void distributionCostbyProduction(XProduction production, BigDecimal distributionProduction){

        BigDecimal volumeProduction = xproductionAction.calculateTotalVolume(production);
        BigDecimal totalDistribution = BigDecimal.ZERO;
        for (XProductionProduct product : production.getProductionProductList()){
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

    /** Proceso Distribucion de Mano de Obra **/
    public void distributionLaborCostByProduction(XProduction production, BigDecimal distributionLabor){

        BigDecimal volumeProduction = xproductionAction.calculateTotalVolume(production);
        BigDecimal totalDistribution = BigDecimal.ZERO;

        for (XProductionProduct product : production.getProductionProductList()){
            BigDecimal volumeProduct = BigDecimalUtil.multiply(product.getQuantity(), product.getProductItem().getBasicQuantity(), 2);
            BigDecimal percentage    = BigDecimalUtil.multiply(volumeProduct, BigDecimalUtil.toBigDecimal(100), 2);
            percentage = BigDecimalUtil.divide(percentage, volumeProduction, 2);

            BigDecimal distributionProduct = BigDecimalUtil.multiply(distributionLabor, BigDecimalUtil.divide(percentage, BigDecimalUtil.toBigDecimal(100), 4), 2);
            totalDistribution = BigDecimalUtil.sum(totalDistribution, distributionProduct, 2);

            product.setCostMo(distributionProduct);
        }
        System.out.println("-------==============--------------------> Total Dist MO: " + totalDistribution);
    }


    public BigDecimal calculateLaborProduction(XProduction production){

        BigDecimal result = BigDecimal.ZERO;

        for (XProductionLabor labor : production.getProductionLaborList()){
            BigDecimal laborCost = BigDecimalUtil.multiply(labor.getHours(), labor.getCostPerHour());
            result = BigDecimalUtil.sum(result, laborCost);
        }
        return result;
    }

    public BigDecimal calculateTotalVolumePlan(XProductionPlan productionPlan){

        BigDecimal result = BigDecimal.ZERO;
        for (XProduction production : productionPlan.getProductionList()){
            result = BigDecimalUtil.sum(result, xproductionAction.calculateTotalVolume(production), 2);
        }
        return result;
    }

    public void changePlanStatus(XProductionPlan productionPlan){
        ProductionPlanState result = ProductionPlanState.APR;
        for (XProduction production : productionPlan.getProductionList()){
            if (!production.getState().equals(ProductionState.APR)) {
                result = ProductionPlanState.PEN;
                break;
            }
        }

        for (XProductionProduct product : productionPlan.getProductionProductList()){
            if (!hasProduction2(product)){
                result = ProductionPlanState.PEN;
                break;
            }
        }
        productionPlan.setState(result);
    }

    public List<XProductionProduct> getProductList() {
        return productList;
    }

    public void setProductList(List<XProductionProduct> productList) {
        this.productList = productList;
    }

    public void addProductItems(List<ProductItem> productItems){

        for (ProductItem productItem : productItems) {
            XProductionProduct product = new XProductionProduct();
            product.setUnitCost(BigDecimal.ZERO);
            product.setCostA(BigDecimal.ZERO);
            product.setCostB(BigDecimal.ZERO);
            product.setCostC(BigDecimal.ZERO);
            product.setCostMo(BigDecimal.ZERO);
            product.setCost(BigDecimal.ZERO);
            product.setProductItemCode(productItem.getProductItemCode());
            product.setProductItem(productItem);
            product.setQuantity(BigDecimal.ZERO);
            productList.add(product);
        }
    }

    @In
    private InventoryService inventoryService;

    public void removeProduct(XProductionProduct product){

        if (product.getId() != null){
            System.out.println("-----------------------------Z>>> Removing product: " + product.getProductItem().getFullName());
            productList.remove(product);
            xproductionPlanService.removeProduct(product);
            inventoryService.updateInventoryRemoveFromProduction(product);
            xproductionPlanService.updateProductItemRemoveFromProduction(product);
        }else {
            productList.remove(product);
        }
    }

    public String hasProduction(XProductionProduct productionProduct){

        String result = "NO";
        if (productionProduct.getProduction() != null)
            result = "SI";

        return  result;
    }

    public Boolean hasProduction2(XProductionProduct productionProduct){

        Boolean result = Boolean.FALSE;
        if (productionProduct.getProduction() != null)
            result = Boolean.TRUE;

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

    public BigDecimal getDestinedMilk(XProduction production){

        BigDecimal result = BigDecimal.ZERO;

        for (XSupply supply : production.getSupplyList()){
            if (supply.getProductItemCode().equals(Constants.ID_ART_RAW_MILK))
                result = BigDecimalUtil.sum(result, supply.getQuantity(), 2);
        }
        return result;
    }

    public BigDecimal calculateTotalRawMaterial(XProductionPlan productionPlan){

        BigDecimal result = BigDecimal.ZERO;

        for (XProduction production : productionPlan.getProductionList()){
            xproductionAction.setInstance(production);
            xproductionAction.setIngredientSupplyList(production.getSupplyList());
            result = BigDecimalUtil.sum(result, xproductionAction.calculateRawMaterial(), 2);
        }
        return result;

    }

    public double getTotalWeighed(XProductionPlan productionPlan){

        double result = xproductionPlanService.findTotalWeighed(productionPlan.getDate());
        return result;

    }

    public List<XProductionProduct> getProductionProductList(XProductionPlan productionPlan){

        List<XProductionProduct> productionProductList = new ArrayList<XProductionProduct>();
        //System.out.println("=====> Plan: " + productionPlan.getDate());
        for (XProductionProduct product : productionPlan.getProductionProductList()){
            //System.out.println("---------->>>> Plan Product:" + productionPlan.getDate() + " - " + product.getProductItem().getFullName() + " - " + product.getQuantity());
            productionProductList.add(product);
        }


        return productionProductList;
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

    public XProductionProduct getProductToRemove() {
        return productToRemove;
    }

    public void setProductToRemove(XProductionProduct productToRemove) {
        this.productToRemove = productToRemove;
    }

    public void removeProductionProduct(){
        removeProduct(this.productToRemove);
        setProductToRemove(null);
    }

    public XProductionProduct getProductToSum() {
        return productToSum;
    }

    public void setProductToSum(XProductionProduct productToSum) {
        this.productToSum = productToSum;
    }

    public BigDecimal getQuantitySum() {
        return quantitySum;
    }

    public void setQuantitySum(BigDecimal quantitySum) {
        this.quantitySum = quantitySum;
    }

    public BigDecimal getQuantitySubtract() {
        return quantitySubtract;
    }

    public void setQuantitySubtract(BigDecimal quantitySubtract) {
        this.quantitySubtract = quantitySubtract;
    }

    public String getGloss() {
        return gloss;
    }

    public void setGloss(String gloss) {
        this.gloss = gloss;
    }

    public XProductionProduct getProductToSubtract() {
        return productToSubtract;
    }

    public void setProductToSubtract(XProductionProduct productToSubtract) {
        this.productToSubtract = productToSubtract;
    }

    public XProductionProduct getPreviousProduct() {
        return previousProduct;
    }

    public void setPreviousProduct(XProductionProduct previousProduct) {
        this.previousProduct = previousProduct;
    }

    /**
     * Private class
     */

    private class IndirectAux {

        private String accountCode;
        private BigDecimal amount;
        private BigDecimal percentage;
        private BigDecimal value;

        IndirectAux(String accountCode, BigDecimal amount, BigDecimal percentage, BigDecimal value){
            this.setAccountCode(accountCode);
            this.setAmount(amount);
            this.setPercentage(percentage);
            this.setValue(value);
        }

        public String getAccountCode() {
            return accountCode;
        }

        public void setAccountCode(String accountCode) {
            this.accountCode = accountCode;
        }

        public BigDecimal getAmount() {
            return amount;
        }

        public void setAmount(BigDecimal amount) {
            this.amount = amount;
        }

        public BigDecimal getPercentage() {
            return percentage;
        }

        public void setPercentage(BigDecimal percentage) {
            this.percentage = percentage;
        }

        public BigDecimal getValue() {
            return value;
        }

        public void setValue(BigDecimal value) {
            this.value = value;
        }
    }

    private class DataVoucherDetail{

        private String productionCode;
        private CashAccount cashAccount;
        private BigDecimal debit;
        private BigDecimal credit;
        private String productItemCode;
        private BigDecimal quantity;

        DataVoucherDetail(String productionCode, CashAccount cashAccount, BigDecimal debit, BigDecimal credit, String productItemCode, BigDecimal quantity){
            this.setProductionCode(productionCode);
            this.setCashAccount(cashAccount);
            this.setDebit(debit);
            this.setCredit(credit);
            this.setProductItemCode(productItemCode);
            this.setQuantity(quantity);
        }


        public BigDecimal getDebit() {
            return debit;
        }

        public void setDebit(BigDecimal debit) {
            this.debit = debit;
        }

        public BigDecimal getCredit() {
            return credit;
        }

        public void setCredit(BigDecimal credit) {
            this.credit = credit;
        }

        public String getProductionCode() {
            return productionCode;
        }

        public void setProductionCode(String productionCode) {
            this.productionCode = productionCode;
        }

        public CashAccount getCashAccount() {
            return cashAccount;
        }

        public void setCashAccount(CashAccount cashAccount) {
            this.cashAccount = cashAccount;
        }

        public String getProductItemCode() {
            return productItemCode;
        }

        public void setProductItemCode(String productItemCode) {
            this.productItemCode = productItemCode;
        }

        public BigDecimal getQuantity() {
            return quantity;
        }

        public void setQuantity(BigDecimal quantity) {
            this.quantity = quantity;
        }
    }

    /** **/

}
