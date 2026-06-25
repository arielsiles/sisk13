package com.encens.khipus.action.xproduction;


import com.encens.khipus.framework.action.GenericAction;
import com.encens.khipus.framework.action.Outcome;
import com.encens.khipus.model.admin.User;
import com.encens.khipus.model.employees.Employee;
import com.encens.khipus.model.finances.JobContract;
import com.encens.khipus.model.production.MeasurementUnit;
import com.encens.khipus.model.production.ProductionState;
import com.encens.khipus.model.production.SupplyType;
import com.encens.khipus.model.warehouse.ProductItem;
import com.encens.khipus.model.xproduction.*;
import com.encens.khipus.service.common.SequenceService;
import com.encens.khipus.service.employees.JobContractService;
import com.encens.khipus.service.warehouse.ProductItemService;
import com.encens.khipus.service.xproduction.XProductionPlanService;
import com.encens.khipus.service.xproduction.XProductionService;
import com.encens.khipus.service.xproduction.XProductionUlexitaCalc;
import com.encens.khipus.service.xproduction.XProductionUlexitaService;
import com.encens.khipus.service.xproduction.XProductionBaritinaService;
import com.encens.khipus.util.BigDecimalUtil;
import com.encens.khipus.util.Constants;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.*;
import org.jboss.seam.international.StatusMessage;
import org.jboss.seam.security.Identity;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


@Name("xproductionAction")
@Scope(ScopeType.CONVERSATION)
public class XProductionAction extends GenericAction<XProduction> {

    private XProductionTank productionTank;
    private XFormulation formulation;
    private XProductionPlan productionPlan;
    private ProductionProcess process;
    private ProductionLine productionLine;
    private ProductionGroup productionGroup;
    private ProductionShiftType productionShiftType;

    private List<XSupply> ingredientSupplyList = new ArrayList<XSupply>();
    private List<XSupply> materialSupplyList = new ArrayList<XSupply>();
    private List<XProductionLabor> laborList = new ArrayList<XProductionLabor>();

    //private BigDecimal totalCost;
    //private BigDecimal totalRawMaterial;

    private XSupply supplyAssign;
    private String activeTabName = "productsTab";

    private XProductionUlexita ulexitaData;
    /* Articulo destino del Reproceso final (resuelto desde la linea, solo para mostrar). */
    private ProductItem reprocFinalArticle;
    private boolean reprocFinalArticleResolved;

    private XProductionBaritina baritinaData;
    private List<XProductionBaritinaZona> baritinaZonaList = new ArrayList<XProductionBaritinaZona>();

    @In
    private XProductionPlanAction xproductionPlanAction;

    @In
    private XProductionService xproductionService;
    @In
    private XProductionPlanService xproductionPlanService;
    @In
    private SequenceService sequenceService;
    @In
    private JobContractService jobContractService;
    @In
    private XProductionUlexitaService xproductionUlexitaService;
    @In
    private XProductionBaritinaService xproductionBaritinaService;
    @In(create = true)
    private ProductItemService productItemService;
    @In(required = false)
    private User currentUser;

    @Factory(value = "xproduction", scope = ScopeType.STATELESS)
    public XProduction initProduction() {
        return getInstance();
    }


    @Override
    @Begin(nested=true, ifOutcome = Outcome.SUCCESS, flushMode = FlushModeType.MANUAL)
    public String select(XProduction instance) {
        String outCome = super.select(instance);
        setFormulation(getInstance().getFormulation());
        setProductionTank(getInstance().getProductionTank());
        setProcess(getInstance().getProcess());
        setProductionPlan(getInstance().getProductionPlan());
        setProductionLine(getInstance().getProductionLine());
        setProductionGroup(getInstance().getProductionGroup());
        setProductionShiftType(getInstance().getProductionShiftType());

        setIngredientSupplyList(xproductionService.getSupplyList(getInstance(), SupplyType.INGREDIENT));
        setMaterialSupplyList(xproductionService.getSupplyList(getInstance(), SupplyType.MATERIAL));
        setLaborList(xproductionService.getLaborList(getInstance()));

        // Default initDate = fecha del plan a las 00:00 si no esta seteada
        if (getInstance().getInitDate() == null && getInstance().getProductionPlan() != null
                && getInstance().getProductionPlan().getDate() != null) {
            java.util.Calendar c = java.util.Calendar.getInstance();
            c.setTime(getInstance().getProductionPlan().getDate());
            c.set(java.util.Calendar.HOUR_OF_DAY, 0);
            c.set(java.util.Calendar.MINUTE, 0);
            c.set(java.util.Calendar.SECOND, 0);
            c.set(java.util.Calendar.MILLISECOND, 0);
            getInstance().setInitDate(c.getTime());
        }

        loadUlexitaData();
        loadBaritinaData();

        return outCome;
    }

    /**
     * Carga los datos especificos de ULEXITA si la linea aplica. Si la linea es
     * ULEXITA y aun no existe registro satelite, instancia uno vacio en memoria
     * para que la pestaña Datos del Proceso pueda enlazar campos sin NPE.
     */
    private void loadUlexitaData() {
        ulexitaData = null;
        if (getInstance() == null || getInstance().getId() == null) return;
        if (getInstance().getProductionLine() == null || !getInstance().getProductionLine().isUlexitaTemplate()) return;
        ulexitaData = xproductionUlexitaService.findByProduction(getInstance());
        if (ulexitaData == null) {
            ulexitaData = new XProductionUlexita();
            ulexitaData.setProduction(getInstance());
        }
    }

    /**
     * Carga los datos especificos de BARITINA si la linea aplica: cabecera
     * (uso MP, PT, despacho, turnos) y la distribucion por zonas productivas.
     * Si aun no existe cabecera, instancia una vacia en memoria para enlazar
     * campos sin NPE.
     */
    private void loadBaritinaData() {
        baritinaData = null;
        baritinaZonaList = new ArrayList<XProductionBaritinaZona>();
        if (getInstance() == null || getInstance().getId() == null) return;
        if (getInstance().getProductionLine() == null || !getInstance().getProductionLine().isBaritinaTemplate()) return;
        baritinaData = xproductionBaritinaService.findByProduction(getInstance());
        if (baritinaData == null) {
            baritinaData = new XProductionBaritina();
            baritinaData.setProduction(getInstance());
        }
        baritinaZonaList = xproductionBaritinaService.findZonasByProduction(getInstance());
        if (baritinaZonaList == null) {
            baritinaZonaList = new ArrayList<XProductionBaritinaZona>();
        }
    }

    private void persistBaritinaData() {
        if (getInstance() == null || getInstance().getId() == null) return;
        if (getInstance().getProductionLine() == null || !getInstance().getProductionLine().isBaritinaTemplate()) return;
        recalcBaritinaZonas();
        if (baritinaData != null) {
            if (baritinaData.getProduction() == null) {
                baritinaData.setProduction(getInstance());
            }
            // Snapshot de los valores derivados (Insumo / Productos terminados) en TN.
            baritinaData.setUsoMpTn(getBaritinaMpUsed());
            baritinaData.setPtTn(getBaritinaPtProduced());
            xproductionBaritinaService.save(baritinaData);
        }
        for (XProductionBaritinaZona zona : baritinaZonaList) {
            zona.setProduction(getInstance());
            xproductionBaritinaService.saveZona(zona);
        }
    }

    private void persistUlexitaData() {
        if (ulexitaData == null) return;
        if (getInstance() == null || getInstance().getId() == null) return;
        if (getInstance().getProductionLine() == null || !getInstance().getProductionLine().isUlexitaTemplate()) return;
        if (ulexitaData.getProduction() == null) {
            ulexitaData.setProduction(getInstance());
        }
        // Persistir el articulo destino del Reproceso final configurado en la linea,
        // para que la orden conserve con que articulo se haran los movimientos de inventario.
        ulexitaData.setCodArtReprocFinal(getInstance().getProductionLine().getCodArtReprocFinal());
        xproductionUlexitaService.save(ulexitaData);
    }

    @Override
    public String create() {

        XProduction production = getInstance();
        production.setState(ProductionState.PEN);
        production.setProductionTank(productionTank);
        production.setFormulation(formulation);
        production.setProductionPlan(productionPlan);
        production.setProcess(process);
        production.setProductionLine(productionLine);
        production.setProductionGroup(productionGroup);
        production.setProductionShiftType(productionShiftType);

        Long seq = sequenceService.createOrUpdateNextSequenceValue(Constants.PRODUCTION_CODE);
        production.setCode(seq.intValue());
        syncMpFromConsumo();
        syncBaritinaMpFromPt();
        recalcBaritinaZonas();
        xproductionService.createProduction(production, ingredientSupplyList, materialSupplyList);

        production.setInitDate(production.getProductionPlan().getDate());

        /*setOp(OP_UPDATE);*/
        super.select(production);
        return Outcome.SUCCESS;
    }

    @Override
    public String update() {
        if (!validateBaritina()) {
            return Outcome.REDISPLAY;
        }

        XProduction production = getInstance();
        production.setProductionTank(productionTank);
        production.setFormulation(formulation);
        production.setProductionGroup(productionGroup);
        production.setProductionShiftType(productionShiftType);

        syncMpFromConsumo();
        syncBaritinaMpFromPt();
        recalcBaritinaZonas();
        production.setTotalCost(calculateTotalCost());
        production.setTotalRawMaterial(calculateRawMaterial());
        xproductionService.updateProduction(production, ingredientSupplyList, materialSupplyList, laborList);
        persistUlexitaData();
        persistBaritinaData();

        // Re-snapshot si la orden ya esta aprobada (caso edicion de lab data
        // con permiso PRODUCTION_LAB_DATA:UPDATE). Asi los snapshots reflejan
        // el ultimo estado autorizado.
        if (production.isApproved()
                && production.getProductionLine() != null
                && production.getProductionLine().isUlexitaTemplate()) {
            xproductionUlexitaService.persistSnapshots(production, currentUserCode());
        }

        return Outcome.SUCCESS;
    }

    @Override
    public String delete() {
        xproductionBaritinaService.deleteByProduction(getInstance());
        xproductionService.deleteProduction(getInstance());
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
        xproductionPlanAction.changePlanStatus(getInstance().getProductionPlan());
        xproductionService.updateProduction(getInstance(), ingredientSupplyList, materialSupplyList, laborList);
        facesMessages.addFromResourceBundle(StatusMessage.Severity.INFO,"Production.message.disapproveProduction");
    }
    /**
     * Al aprobar la produccion calcula los costos y lo distribuye por cada producto en la produccion
     */
    public void approve(){

        if (!validateBaritina()) {
            return;
        }

        syncMpFromConsumo();
        syncBaritinaMpFromPt();
        recalcBaritinaZonas();

        if (!validateSupplyQuantities()) {
            return;
        }

        for (XProductionProduct product : getInstance().getProductionProductList()){
            BigDecimal productCost = BigDecimal.ZERO;
            /** Calcula para los insumos asignados a un producto especifico **/
            for (XSupply ingredient : this.ingredientSupplyList){
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
            for (XSupply material : this.materialSupplyList){
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
        for (XSupply ingredient : this.ingredientSupplyList){
            if (ingredient.getProductionProduct() == null) {
                BigDecimal ingredientCost = BigDecimalUtil.multiply(ingredient.getQuantity(), ingredient.getUnitCost(), 6);
                remainingCost = BigDecimalUtil.sum(remainingCost, ingredientCost, 6);
                remainingCost = BigDecimalUtil.roundBigDecimal(remainingCost, 2);
            }
        }

        System.out.println("====> TOTAL INGREDIENT: " + remainingCost);
        System.out.println("====> TOTAL remainingCost: " + remainingCost);

        for (XSupply material : this.materialSupplyList){
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

        for (XProductionProduct product : getInstance().getProductionProductList()){
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
        xproductionPlanAction.changePlanStatus(getInstance().getProductionPlan());

        xproductionService.updateProduction(getInstance(), ingredientSupplyList, materialSupplyList, laborList);
        persistUlexitaData();
        persistBaritinaData();

        if (getInstance().getProductionLine() != null && getInstance().getProductionLine().isUlexitaTemplate()) {
            xproductionUlexitaService.persistSnapshots(getInstance(), currentUserCode());
        }

        facesMessages.addFromResourceBundle(StatusMessage.Severity.INFO,"Production.message.approveProduction");
    }

    /**
     * Codigo del usuario actual para auditoria de snapshots (financesCode 4-char).
     * Si no hay usuario o no tiene financesCode, retorna null.
     */
    private String currentUserCode() {
        if (currentUser == null) return null;
        return currentUser.getFinancesCode();
    }

    /** Calcula el VOLUMEN TOTAL de una produccion **/
    public BigDecimal calculateTotalVolume(XProduction production){
        BigDecimal totalVolume = BigDecimal.ZERO;
        for (XProductionProduct product : production.getProductionProductList()){
            BigDecimal productVolume   = BigDecimalUtil.multiply(product.getQuantity(), product.getProductItem().getBasicQuantity(), 2);
            totalVolume = BigDecimalUtil.sum(totalVolume, productVolume, 2);
        }
        return totalVolume;
    }

    public void updateUnitCostProducts(XProduction production){

        for (XProductionProduct product : production.getProductionProductList()){
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

    /**
     * Permiso para EDITAR los datos generales/de produccion de la orden (datos de
     * produccion, insumos, materiales, zonas, PT, cabecera). Solo usuarios de
     * produccion (PRODUCTION:UPDATE) y solo mientras la orden esta pendiente.
     */
    public boolean isCanEditProduction() {
        return isPending() && Identity.instance().hasPermission("PRODUCTION", "UPDATE");
    }

    /**
     * Permiso para EDITAR los Datos de Laboratorio. SOLO usuarios de laboratorio
     * (PRODUCTION_LAB_DATA:UPDATE), mientras la orden esta pendiente. Los usuarios
     * de produccion (PRODUCTION:UPDATE) NO pueden modificar estos datos: el
     * registro de laboratorio es exclusivo de laboratorio. En aprobado nadie edita.
     */
    public boolean isCanEditLabData() {
        return isPending() && Identity.instance().hasPermission("PRODUCTION_LAB_DATA", "UPDATE");
    }

    /**
     * Guardado dedicado para usuarios de laboratorio: registra los Datos de
     * Laboratorio y persiste tambien los datos derivados ('Datos Calculados'):
     * al cambiar las leyes se recalcula el Consumo MP y se vuelca al insumo MP por
     * defecto, por lo que hay que persistir los insumos ademas de la data ULEXITA.
     */
    public String saveLabData() {
        XProduction production = getInstance();
        // Recalcular y volcar el Consumo MP al insumo MP por defecto (no-op si no es ULEXITA).
        syncMpFromConsumo();
        production.setTotalCost(calculateTotalCost());
        production.setTotalRawMaterial(calculateRawMaterial());
        // Persistir insumos/materiales (con la cantidad de MP recalculada) y la data de lab.
        xproductionService.updateProduction(production, ingredientSupplyList, materialSupplyList, laborList);
        persistUlexitaData();
        facesMessages.addFromResourceBundle(StatusMessage.Severity.INFO, "Production.message.labDataSaved");
        return Outcome.SUCCESS;
    }

    public void clearAction(){
        setOp(null);
        setInstance(null);
        setProductionTank(null);
        setFormulation(null);
        setProductionLine(null);
        setProductionGroup(null);
        setProductionShiftType(null);

        setIngredientSupplyList(new ArrayList<XSupply>());
        setMaterialSupplyList(new ArrayList<XSupply>());
        ulexitaData = null;
        baritinaData = null;
        baritinaZonaList = new ArrayList<XProductionBaritinaZona>();
    }

    public void loadSupplies(){
        setIngredientSupplyList(new ArrayList<XSupply>());
        for (XFormulationInput formulationInput : this.formulation.getFormulationInputList()){
            XSupply supply = new XSupply();
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

            XSupply supply = new XSupply();
            supply.setProductItemCode(productItem.getProductItemCode());
            supply.setProductItem(productItem);
            supply.setType(SupplyType.MATERIAL);
            //supply.setUnitCost(productItem.getUnitCost());
            materialSupplyList.add(supply);
        }
    }

    /**
     * Agrega productos terminados directamente a la orden (sin pasar por el plan).
     * Multi-seleccion. La cantidad inicia en 0 y se edita en la tabla "Productos Terminados".
     * La persistencia es diferida: los nuevos XProductionProduct se guardan al hacer "Guardar".
     * El idplan se enlaza al plan de la orden si existe (para mantener trazabilidad).
     */
    public void addFinishedProductsDirect(List<ProductItem> productItems) {
        enableProductsTab();
        if (productItems == null || productItems.isEmpty()) return;
        for (ProductItem productItem : productItems) {
            if (containsFinishedProduct(productItem.getProductItemCode())) continue;

            XProductionProduct product = new XProductionProduct();
            product.setProductItemCode(productItem.getProductItemCode());
            product.setProductItem(productItem);
            product.setQuantity(BigDecimal.ZERO);
            product.setCost(BigDecimal.ZERO);
            product.setUnitCost(BigDecimal.ZERO);
            product.setCostA(BigDecimal.ZERO);
            product.setCostB(BigDecimal.ZERO);
            product.setCostC(BigDecimal.ZERO);
            product.setCostMo(BigDecimal.ZERO);
            product.setProductionPlan(getInstance().getProductionPlan());

            // Persistencia inmediata del PT con cantidad 0. La cantidad real se
            // edita inline y se guarda al hacer Guardar via updateProduction (em.merge).
            xproductionService.addFinishedProductDirect(getInstance(), product);
            getInstance().getProductionProductList().add(product);
        }
    }

    private boolean containsFinishedProduct(String codArt) {
        if (codArt == null || getInstance() == null) return false;
        for (XProductionProduct p : getInstance().getProductionProductList()) {
            if (codArt.equals(p.getProductItemCode())) return true;
        }
        return false;
    }

    public void addIngredientItems(List<ProductItem> productItems) {
        for (ProductItem productItem : productItems) {
            /** does not work **/
            if (ingredientSupplyList.contains(productItem.getProductItemCode())) {
                continue;
            }

            XSupply supply = new XSupply();
            supply.setProductItemCode(productItem.getProductItemCode());
            supply.setProductItem(productItem);
            supply.setQuantity(BigDecimal.ZERO);
            supply.setType(SupplyType.INGREDIENT);
            ingredientSupplyList.add(supply);
        }
    }

    public void addProductionLabor(Employee employee){
        enableLaborTab();

        BigDecimal costPerHour = BigDecimal.ZERO;
        JobContract jobContract = jobContractService.getJobContract(employee);

        if (jobContract != null)
            costPerHour = BigDecimalUtil.divide(jobContract.getJob().getSalary().getBasicAmount(), BigDecimalUtil.toBigDecimal(30));

        XProductionLabor labor = new XProductionLabor();
        labor.setEmployee(employee);
        labor.setHours(BigDecimal.ONE);
        labor.setProduction(getInstance());
        labor.setCostPerHour(costPerHour);

        getLaborList().add(labor);
    }

    public BigDecimal getSupplyUnitCost(XSupply supplyDetail){

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


    public List<XProductionProduct> getProductionProductList(){
        List<XProductionProduct> productionProductList = xproductionPlanService.getProductionProductList(getInstance().getProductionPlan());
        return  productionProductList;
    }

    public boolean existProductionProducts(){
        boolean result = false;
        if (this.getProductionProductList().size() > 0)
            result = true;

        return result;
    }

    public void assignProduct(XProductionProduct product){
        enableProductsTab();
        xproductionService.assignProduct(getInstance(), product);
        addMaterialDefault(product, product.getQuantity());
    }

    public void assignSupply(XSupply supply){
        setSupplyAssign(supply);
    }

    public void assignProductAffect(){

        System.out.println("============> Afecta: " + supplyAssign.getProductItem().getFullName());

    }

    private void addMaterialDefault(XProductionProduct product, BigDecimal quantity){

        List<XMaterialInput> materialInputList = xproductionService.getIngredientOrMaterialInput(product.getProductItemCode(), SupplyType.MATERIAL);
        List<XMaterialInput> ingredientInputList = xproductionService.getIngredientOrMaterialInput(product.getProductItemCode(), SupplyType.INGREDIENT);

        for (XMaterialInput materialInput : materialInputList){
            XSupply supply = new XSupply();
            supply.setProductItemCode(materialInput.getProductItemMaterialCode());
            supply.setProductItem(materialInput.getProductItemMaterial());

            if (materialInput.getQuantityFlag())
                supply.setQuantity(quantity);
            else
                supply.setQuantity(BigDecimal.ZERO);

            supply.setProductionProduct(product);
            supply.setType(SupplyType.MATERIAL);

            materialSupplyList.add(supply);
            xproductionService.assignMaterial(getInstance(), supply);
        }

        for (XMaterialInput ingredientInput : ingredientInputList){
            XSupply supply = new XSupply();
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
            xproductionService.assignMaterial(getInstance(), supply);
        }

    }

    public void removeSupply(XSupply supply){
        if (supply == null) return;
        xproductionService.removeSupply(supply);
        if (SupplyType.INGREDIENT.equals(supply.getType())) {
            removeSupplyById(ingredientSupplyList, supply.getId());
        }
        if (SupplyType.MATERIAL.equals(supply.getType())) {
            removeSupplyById(materialSupplyList, supply.getId());
        }
    }

    private void removeSupplyById(List<XSupply> list, Long id) {
        if (list == null || id == null) return;
        java.util.Iterator<XSupply> it = list.iterator();
        while (it.hasNext()) {
            XSupply s = it.next();
            if (id.equals(s.getId())) {
                it.remove();
            }
        }
    }

    public void removeProductionProduct(XProductionProduct product){
        xproductionService.removeProductionProduct(product, getInstance());
    }

    public void removeLabor(XProductionLabor labor){

        /** todo **/
        laborList.remove(labor);
    }

    public boolean hasFormula(XSupply supply){

        boolean result = false;
        if (supply.getFormulationInput() != null)
            result = true;

        return result;
    }

    public void recalculateSupplies(){

        List<XFormulationInput> formulationInputList = getInstance().getFormulation().getFormulationInputList();

        for (XFormulationInput formulationInput : formulationInputList){
            System.out.println("--> " + formulationInput.getProductItem().getFullName() + " - " + formulationInput.getQuantity() + " - " + formulationInput.getInputDefault());
        }

        HashMap<String, BigDecimal> formulationInputMap = new HashMap<String, BigDecimal>();
        HashMap<String, BigDecimal> supplyMap = new HashMap<String, BigDecimal>();

        String defaultInputCode = "";

        for (XFormulationInput formulationInput : formulationInputList){
            formulationInputMap.put(formulationInput.getProductItemCode(), formulationInput.getQuantity());
            if (formulationInput.getInputDefault())
                defaultInputCode = formulationInput.getProductItemCode();
        }

        for (XSupply supply : ingredientSupplyList){
            supplyMap.put(supply.getProductItemCode(), supply.getQuantity());
        }

        //BigDecimal defaultSupplyInput = supplyMap.get(defaultInputCode);
        for (XSupply supply : ingredientSupplyList){
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


    public BigDecimal calculateCost_compoundSupply(XSupply supplyDetail){

        BigDecimal quantityParam = supplyDetail.getQuantity();
        System.out.println("=======> Supply quantityParam: " + supplyDetail.getProductItem().getFullName() + " - Q: " + quantityParam);
        XFormulation formulation = supplyDetail.getFormulationInput().getSecondFormulation();

        HashMap<String, BigDecimal> formulationInputMap = new HashMap<String, BigDecimal>();
        for (XFormulationInput formulationInput : formulation.getFormulationInputList()){
            BigDecimal quantityVal = formulationInput.getQuantity();
            formulationInputMap.put(formulationInput.getProductItemCode(), quantityVal);
        }

        BigDecimal totalCost = BigDecimal.ZERO;
        for (XFormulationInput formulationInput : formulation.getFormulationInputList()){
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

    public BigDecimal calculateTotalRawMaterial_compoundSupply(XSupply supplyDetail){

        BigDecimal quantityParam = supplyDetail.getQuantity();
        XFormulation formulation = supplyDetail.getFormulationInput().getSecondFormulation();

        HashMap<String, BigDecimal> formulationInputMap = new HashMap<String, BigDecimal>();
        for (XFormulationInput formulationInput : formulation.getFormulationInputList()){
            BigDecimal quantityVal = formulationInput.getQuantity();
            formulationInputMap.put(formulationInput.getProductItemCode(), quantityVal);
        }

        BigDecimal totalRawMaterial = BigDecimal.ZERO;
        for (XFormulationInput formulationInput : formulation.getFormulationInputList()){
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

        for (XSupply supply : ingredientSupplyList){
            BigDecimal cost = BigDecimalUtil.multiply(nz(supply.getQuantity()), nz(supply.getUnitCost()), 6);
            ingredientCost = BigDecimalUtil.sum(ingredientCost, cost, 6);
            //System.out.println("===>>> " + supply.getProductItem().getFullName() + "\t\t\t " + supply.getQuantity() + "\t\t - " + supply.getUnitCost() + "\t\t - " + cost);
        }

        for (XSupply supply : materialSupplyList){
            BigDecimal cost = BigDecimalUtil.multiply(nz(supply.getQuantity()), nz(supply.getUnitCost()), 6);
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

        for (XSupply supply : ingredientSupplyList){

            /*if (supply.getProductItemCode().equals(Constants.ID_ART_RAW_MILK))
                result = BigDecimalUtil.sum(result, supply.getQuantity(), 6);*/

            if (supply.hasFormula()){
                if (supply.getFormulationInput().getInputDefault()){
                    result = BigDecimalUtil.sum(result, nz(supply.getQuantity()), 6);
                }
            }

            if (supply.hasFormula()){
                if (supply.getFormulationInput().hasSecondFormula()){
                    result = BigDecimalUtil.sum(result, calculateTotalRawMaterial_compoundSupply(supply), 6);
                }
            }
        }
        result = BigDecimalUtil.roundBigDecimal(result, 2);
        return result;
    }

    public XProductionTank getProductionTank() {
        return productionTank;
    }

    public void setProductionTank(XProductionTank productionTank) {
        this.productionTank = productionTank;
    }

    public XFormulation getFormulation() {
        return formulation;
    }

    public void setFormulation(XFormulation formulation) {
        this.formulation = formulation;
    }

    public XProductionPlan getProductionPlan() {
        return productionPlan;
    }

    public void setProductionPlan(XProductionPlan productionPlan) {
        this.productionPlan = productionPlan;
    }

    public List<XSupply> getIngredientSupplyList() {
        return ingredientSupplyList;
    }

    public void setIngredientSupplyList(List<XSupply> ingredientSupplyList) {
        this.ingredientSupplyList = ingredientSupplyList;
    }

    public List<XSupply> getMaterialSupplyList() {
        return materialSupplyList;
    }

    public void setMaterialSupplyList(List<XSupply> materialSupplyList) {
        this.materialSupplyList = materialSupplyList;
    }

    public XSupply getSupplyAssign() {
        return supplyAssign;
    }

    public void setSupplyAssign(XSupply supplyAssign) {
        this.supplyAssign = supplyAssign;
    }

    public ProductionProcess getProcess() {
        return process;
    }

    public void setProcess(ProductionProcess process) {
        this.process = process;
    }

    public List<XProductionLabor> getLaborList() {
        return laborList;
    }

    public void setLaborList(List<XProductionLabor> laborList) {
        this.laborList = laborList;
    }

    public String getActiveTabName() {
        return activeTabName;
    }

    public void setActiveTabName(String activeTabName) {
        this.activeTabName = activeTabName;
    }

    public void enableLaborTab() {
        setActiveTabName("laborTab");
    }

    public void enableProductsTab() {
        setActiveTabName("productsTab");
    }

    public ProductionLine getProductionLine() {
        return productionLine;
    }

    public void setProductionLine(ProductionLine productionLine) {
        this.productionLine = productionLine;
    }

    public ProductionGroup getProductionGroup() {
        return productionGroup;
    }

    public void setProductionGroup(ProductionGroup productionGroup) {
        this.productionGroup = productionGroup;
    }

    public ProductionShiftType getProductionShiftType() {
        return productionShiftType;
    }

    public void setProductionShiftType(ProductionShiftType productionShiftType) {
        this.productionShiftType = productionShiftType;
    }

    public XProductionUlexita getUlexitaData() {
        if (ulexitaData == null && isUlexitaTemplate()) {
            if (getInstance() != null && getInstance().getId() != null) {
                ulexitaData = xproductionUlexitaService.findByProduction(getInstance());
            }
            if (ulexitaData == null) {
                ulexitaData = new XProductionUlexita();
                ulexitaData.setProduction(getInstance());
            }
        }
        return ulexitaData;
    }

    public void setUlexitaData(XProductionUlexita ulexitaData) {
        this.ulexitaData = ulexitaData;
    }

    public boolean isUlexitaTemplate() {
        return getInstance() != null
                && getInstance().getProductionLine() != null
                && getInstance().getProductionLine().isUlexitaTemplate();
    }

    /**
     * Calc en vivo para la pestaña Datos del Proceso. Re-instancia en cada
     * llamada para reflejar cambios de inputs sin necesidad de persistir.
     */
    public XProductionUlexitaCalc getUlexitaCalc() {
        if (!isUlexitaTemplate()) return null;
        return new XProductionUlexitaCalc(
                getInstance(), ulexitaData, getInstance().getProductionLine(),
                ingredientSupplyList, getInstance().getProductionProductList());
    }

    /**
     * Sincroniza la cantidad del insumo de Materia Prima por defecto (ingrediente
     * marcado inputDefault en la formulacion) con el 'Consumo Materia Prima (TN)'
     * calculado (getConsumoMpCalc = MERMA + Kpa*PT - consumo reproceso),
     * respetando la unidad del insumo (KG -> TN*1000). Se invoca al editar los
     * datos del proceso/PT (ajax) y antes de persistir, mientras la orden no
     * este aprobada. No-op para lineas que no son ULEXITA.
     */
    public void syncMpFromConsumo() {
        if (!isUlexitaTemplate() || getInstance() == null || getInstance().isApproved()) return;
        XProductionUlexitaCalc calc = getUlexitaCalc();
        if (calc == null) return;
        BigDecimal consumoTn = calc.getConsumoMpCalc();
        if (consumoTn == null || ingredientSupplyList == null) return;
        // Tomar el valor tal como se muestra en Datos Calculados (2 decimales),
        // no el valor con los decimales finos del calculo interno. Asi 33,57 TN
        // se vuelca como 33.570,0000 KG y no 33.572,7740.
        BigDecimal consumoShown = BigDecimalUtil.roundBigDecimal(consumoTn, 2);
        for (XSupply supply : ingredientSupplyList) {
            if (supply.hasFormula()
                    && Boolean.TRUE.equals(supply.getFormulationInput().getInputDefault())) {
                String unit = supply.getProductItem() != null ? supply.getProductItem().getUsageMeasureCode() : null;
                BigDecimal qty = XProductionUlexitaCalc.UNIT_KG.equalsIgnoreCase(unit)
                        ? BigDecimalUtil.multiply(consumoShown, BigDecimalUtil.ONE_THOUSAND, 4)
                        : BigDecimalUtil.roundBigDecimal(consumoShown, 4);
                supply.setQuantity(qty);
                break;
            }
        }
    }

    // ------------------------------------------------------------------------
    // BARITINA - distribucion de materia prima por zonas productivas
    // ------------------------------------------------------------------------

    public boolean isBaritinaTemplate() {
        return getInstance() != null
                && getInstance().getProductionLine() != null
                && getInstance().getProductionLine().isBaritinaTemplate();
    }

    public XProductionBaritina getBaritinaData() {
        if (baritinaData == null && isBaritinaTemplate()) {
            if (getInstance() != null && getInstance().getId() != null) {
                baritinaData = xproductionBaritinaService.findByProduction(getInstance());
            }
            if (baritinaData == null) {
                baritinaData = new XProductionBaritina();
                baritinaData.setProduction(getInstance());
            }
        }
        return baritinaData;
    }

    public void setBaritinaData(XProductionBaritina baritinaData) {
        this.baritinaData = baritinaData;
    }

    public List<XProductionBaritinaZona> getBaritinaZonaList() {
        return baritinaZonaList;
    }

    public void setBaritinaZonaList(List<XProductionBaritinaZona> baritinaZonaList) {
        this.baritinaZonaList = baritinaZonaList;
    }

    /** Agrega una fila vacia de zona a la distribucion. */
    public void addBaritinaZona() {
        XProductionBaritinaZona zona = new XProductionBaritinaZona();
        zona.setProduction(getInstance());
        zona.setPorcentaje(BigDecimal.ZERO);
        zona.setCantidadTn(BigDecimal.ZERO);
        baritinaZonaList.add(zona);
    }

    /** Quita una fila de zona (elimina de BD si estaba persistida). */
    public void removeBaritinaZona(XProductionBaritinaZona zona) {
        if (zona == null) return;
        if (zona.getId() != null) {
            xproductionBaritinaService.removeZona(zona);
        }
        baritinaZonaList.remove(zona);
        recalcBaritinaZonas();
    }

    /**
     * Uso de materia prima Baritina (TN) tomado del Insumo: cantidad del insumo
     * marcado como 'por defecto' en la formulacion, convertida de KG a TN.
     */
    public BigDecimal getBaritinaMpUsed() {
        BigDecimal kg = BigDecimal.ZERO;
        if (ingredientSupplyList != null) {
            for (XSupply supply : ingredientSupplyList) {
                if (supply.hasFormula()
                        && Boolean.TRUE.equals(supply.getFormulationInput().getInputDefault())
                        && supply.getQuantity() != null) {
                    kg = BigDecimalUtil.sum(kg, supply.getQuantity(), 6);
                }
            }
        }
        return BigDecimalUtil.roundBigDecimal(BigDecimalUtil.divide(kg, BigDecimalUtil.ONE_THOUSAND, 6), 4);
    }

    /**
     * Baritina producto terminado (TN): suma de las cantidades de los productos
     * terminados de la orden, convertida de KG a TN.
     */
    public BigDecimal getBaritinaPtProduced() {
        BigDecimal kg = BigDecimal.ZERO;
        if (getInstance() != null && getInstance().getProductionProductList() != null) {
            for (XProductionProduct product : getInstance().getProductionProductList()) {
                if (product.getQuantity() != null) {
                    kg = BigDecimalUtil.sum(kg, product.getQuantity(), 6);
                }
            }
        }
        return BigDecimalUtil.roundBigDecimal(BigDecimalUtil.divide(kg, BigDecimalUtil.ONE_THOUSAND, 6), 4);
    }

    /**
     * BARITINA: al ingresar la cantidad del Producto Terminado principal de la
     * linea (cod_art = line.codArtPtPrincipal), calcula la Materia Prima por
     * defecto como MP = PT * factor (line.factorPtMp) y la vuelca en la cantidad
     * del insumo marcado inputDefault, respetando la unidad (KG/TN).
     *
     * No-op si: no es baritina, la orden esta aprobada, la linea no tiene factor
     * configurado (nulo o <= 0), o no hay PT principal cargado. El valor se
     * redondea a 2 decimales (el mostrado).
     */
    public void syncBaritinaMpFromPt() {
        if (!isBaritinaTemplate() || getInstance() == null || getInstance().isApproved()) return;
        ProductionLine line = getInstance().getProductionLine();
        if (line == null) return;
        BigDecimal factor = line.getFactorPtMp();
        if (factor == null || factor.signum() <= 0) return;

        String ptCod = line.getCodArtPtPrincipal();
        if (ptCod == null) return;

        BigDecimal ptQty = BigDecimal.ZERO;
        String ptUnit = null;
        boolean found = false;
        if (getInstance().getProductionProductList() != null) {
            for (XProductionProduct p : getInstance().getProductionProductList()) {
                if (ptCod.equals(p.getProductItemCode()) && p.getQuantity() != null) {
                    ptQty = BigDecimalUtil.sum(ptQty, p.getQuantity(), 6);
                    if (ptUnit == null && p.getProductItem() != null) {
                        ptUnit = p.getProductItem().getUsageMeasureCode();
                    }
                    found = true;
                }
            }
        }
        if (!found || ingredientSupplyList == null) return;

        for (XSupply supply : ingredientSupplyList) {
            if (supply.hasFormula()
                    && Boolean.TRUE.equals(supply.getFormulationInput().getInputDefault())) {
                String mpUnit = supply.getProductItem() != null ? supply.getProductItem().getUsageMeasureCode() : null;
                BigDecimal ptInMpUnit = convertQtyToUnit(ptQty, ptUnit, mpUnit);
                BigDecimal mpQty = BigDecimalUtil.roundBigDecimal(BigDecimalUtil.multiply(ptInMpUnit, factor, 6), 2);
                supply.setQuantity(mpQty);
                break;
            }
        }
    }

    /**
     * Nombre del articulo configurado en la linea (cod_art_reproc_final) donde se
     * acumula el Reproceso final (TN) al aprobar. Devuelve el nombre completo; si no
     * se resuelve el articulo, el codigo; {@code null} si no hay nada configurado.
     * Se cachea por codigo para no golpear la BD en cada render.
     */
    public String getReprocFinalArticleName() {
        // Fuente: el articulo guardado en la orden si ya existe (refleja lo persistido,
        // aunque luego cambie la config); si no, la config actual de la linea.
        String code = ulexitaData != null ? ulexitaData.getCodArtReprocFinal() : null;
        if (code == null || code.trim().isEmpty()) {
            ProductionLine line = getInstance() == null ? null : getInstance().getProductionLine();
            code = line == null ? null : line.getCodArtReprocFinal();
        }
        if (code == null || code.trim().isEmpty()) {
            return null;
        }
        if (!reprocFinalArticleResolved || reprocFinalArticle == null
                || !code.equals(reprocFinalArticle.getProductItemCode())) {
            try {
                reprocFinalArticle = productItemService.findProductItemByCode(code);
            } catch (Exception e) {
                reprocFinalArticle = null;
            }
            reprocFinalArticleResolved = true;
        }
        return reprocFinalArticle == null ? code : reprocFinalArticle.getFullName();
    }

    /** Convierte una cantidad de la unidad origen a la destino (KG<->TN). Igual unidad: sin cambio. */
    private BigDecimal convertQtyToUnit(BigDecimal value, String fromUnit, String toUnit) {
        if (value == null) return BigDecimal.ZERO;
        boolean fromKg = "KG".equalsIgnoreCase(fromUnit);
        boolean toKg = "KG".equalsIgnoreCase(toUnit);
        if (fromKg == toKg) return value;
        if (fromKg) return BigDecimalUtil.divide(value, BigDecimalUtil.ONE_THOUSAND, 6); // KG -> TN
        return BigDecimalUtil.multiply(value, BigDecimalUtil.ONE_THOUSAND, 6);           // TN -> KG
    }

    /**
     * Despachador para el evento de cambio de cantidad de un Producto Terminado.
     * Aplica el recalculo segun el tipo de linea (cada sync es no-op si no aplica):
     * ULEXITA vuelca el Consumo MP; BARITINA vuelca PT*factor a la MP y recalcula
     * la distribucion por zonas.
     */
    public void recalcOnPtChange() {
        syncMpFromConsumo();
        syncBaritinaMpFromPt();
        recalcBaritinaZonas();
    }

    /** Recalcula cantidad por zona = uso_mp_baritina (TN) * porcentaje / 100. */
    public void recalcBaritinaZonas() {
        BigDecimal mp = getBaritinaMpUsed();
        for (XProductionBaritinaZona zona : baritinaZonaList) {
            BigDecimal pct = zona.getPorcentaje() != null ? zona.getPorcentaje() : BigDecimal.ZERO;
            BigDecimal cantidad = BigDecimalUtil.divide(BigDecimalUtil.multiply(mp, pct, 6), BigDecimalUtil.ONE_HUNDRED, 6);
            zona.setCantidadTn(BigDecimalUtil.roundBigDecimal(cantidad, 4));
        }
    }

    /** Suma de los porcentajes de todas las zonas (debe ser 100). */
    public BigDecimal getBaritinaZonaPctTotal() {
        BigDecimal total = BigDecimal.ZERO;
        for (XProductionBaritinaZona zona : baritinaZonaList) {
            if (zona.getPorcentaje() != null) {
                total = BigDecimalUtil.sum(total, zona.getPorcentaje(), 4);
            }
        }
        return total;
    }

    /** True si la suma de porcentajes es 100% (tolerancia 0.01). Para colorear en la vista. */
    public boolean isBaritinaZonaPctComplete() {
        BigDecimal diff = getBaritinaZonaPctTotal().subtract(BigDecimalUtil.toBigDecimal(100)).abs();
        return diff.compareTo(new BigDecimal("0.01")) <= 0;
    }

    /**
     * Valida la distribucion por zonas de una orden BARITINA: cada fila debe
     * tener zona seleccionada y la suma de porcentajes debe ser 100% (tolerancia
     * 0.01). Sin zonas registradas no bloquea (la orden aun puede ser parcial).
     */
    private boolean validateBaritina() {
        if (!isBaritinaTemplate()) return true;
        if (baritinaZonaList == null || baritinaZonaList.isEmpty()) return true;
        for (XProductionBaritinaZona zona : baritinaZonaList) {
            if (zona.getProductiveZone() == null) {
                facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR, "XProduction.baritina.error.zoneRequired");
                return false;
            }
        }
        BigDecimal total = getBaritinaZonaPctTotal();
        BigDecimal diff = total.subtract(BigDecimalUtil.toBigDecimal(100)).abs();
        if (diff.compareTo(new BigDecimal("0.01")) > 0) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR, "XProduction.baritina.error.pctSum");
            return false;
        }
        return true;
    }

    /**
     * Valida que ningun insumo ni material tenga cantidad nula o en cero antes
     * de aprobar la orden. Lista los articulos afectados en el area de mensajes
     * por defecto. Retorna false (bloquea la aprobacion) si encuentra alguno.
     */
    private boolean validateSupplyQuantities() {
        StringBuilder zeros = new StringBuilder();
        for (XSupply s : ingredientSupplyList) {
            if (isZeroQuantity(s)) appendSupplyName(zeros, s);
        }
        for (XSupply s : materialSupplyList) {
            if (isZeroQuantity(s)) appendSupplyName(zeros, s);
        }
        if (zeros.length() > 0) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                    "XProduction.error.supplyQuantityZero", zeros.toString());
            return false;
        }
        return true;
    }

    private boolean isZeroQuantity(XSupply s) {
        return s == null || s.getQuantity() == null || s.getQuantity().signum() == 0;
    }

    private void appendSupplyName(StringBuilder sb, XSupply s) {
        if (sb.length() > 0) sb.append(", ");
        sb.append(s.getProductItem() != null ? s.getProductItem().getFullName() : s.getProductItemCode());
    }

    /** Devuelve ZERO si el valor es nulo. Para calculos tolerantes a cantidades/costos sin cargar. */
    private static BigDecimal nz(BigDecimal v) {
        return v == null ? BigDecimal.ZERO : v;
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
