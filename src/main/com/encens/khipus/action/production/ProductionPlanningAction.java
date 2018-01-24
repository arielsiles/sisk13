package com.encens.khipus.action.production;

import com.encens.khipus.action.SessionUser;
import com.encens.khipus.exception.ConcurrencyException;
import com.encens.khipus.exception.EntryDuplicatedException;
import com.encens.khipus.exception.EntryNotFoundException;
import com.encens.khipus.exception.ReferentialIntegrityException;
import com.encens.khipus.exception.finances.CompanyConfigurationNotFoundException;
import com.encens.khipus.exception.finances.FinancesCurrencyNotFoundException;
import com.encens.khipus.exception.finances.FinancesExchangeRateNotFoundException;
import com.encens.khipus.exception.production.ProductCompositionException;
import com.encens.khipus.exception.warehouse.*;
import com.encens.khipus.framework.action.GenericAction;
import com.encens.khipus.framework.action.Outcome;
import com.encens.khipus.framework.service.GenericService;
import com.encens.khipus.model.admin.BusinessUnit;
import com.encens.khipus.model.admin.User;
import com.encens.khipus.model.finances.CashAccount;
import com.encens.khipus.model.finances.Voucher;
import com.encens.khipus.model.production.*;
import com.encens.khipus.model.warehouse.*;
import com.encens.khipus.service.admin.BusinessUnitService;
import com.encens.khipus.service.employees.JobContractService;
import com.encens.khipus.service.finances.CostCenterService;
import com.encens.khipus.service.finances.VoucherService;
import com.encens.khipus.service.finances.VoucherServiceBean;
import com.encens.khipus.service.production.*;
import com.encens.khipus.service.warehouse.*;
import com.encens.khipus.util.*;
import com.encens.khipus.util.query.QueryUtils;
import com.encens.khipus.util.warehouse.InventoryMessage;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.*;
import org.jboss.seam.international.StatusMessage;

import javax.faces.event.ActionEvent;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;

import static com.encens.khipus.model.production.ProductionPlanningState.*;
import static org.jboss.seam.international.StatusMessage.Severity.ERROR;


@Name("productionPlanningAction")
@Scope(ScopeType.CONVERSATION)
public class ProductionPlanningAction extends GenericAction<ProductionPlanning> {

    private ProcessedProduct processedProduct;
    private ProductionOrder productionOrderMaterial;
    private BaseProduct baseProduct;
    private SingleProduct singleProduct;
    private ProductComposition productComposition;
    private ProductionOrder productionOrder;
    private Formulation existingFormulation;
    private OrderMaterial orderMaterial;
    private ProductItem productItem;
    private String codeGenerate;
    private List<ProductItemPK> selectedProductItems = new ArrayList<ProductItemPK>();
    private List<ProductItemPK> selectedBaseProductItems = new ArrayList<ProductItemPK>();
    private List<ProductItemPK> selectedProductToBaseproduct = new ArrayList<ProductItemPK>();
    private List<ProductItemPK> selectedSingleProductMaterial = new ArrayList<ProductItemPK>();
    private List<OrderMaterial> orderMaterials = new ArrayList<OrderMaterial>();
    private List<OrderInput> orderBaseInputs = new ArrayList<OrderInput>();
    private List<ProductProcessing> productProcessings = new ArrayList<ProductProcessing>();
    private List<OrderMaterial> orderSingleMaterial = new ArrayList<OrderMaterial>();
    private List<OrderInput> productIputToFormulation = new ArrayList<OrderInput>();

    // this map stores the MovementDetails that are under the minimal stock and the unitaryBalance of the Inventory
    protected Map<MovementDetail, BigDecimal> movementDetailUnderMinimalStockMap = new HashMap<MovementDetail, BigDecimal>();
    // this map stores the MovementDetails that are over the maximum stock and the unitaryBalance of the Inventory
    protected Map<MovementDetail, BigDecimal> movementDetailOverMaximumStockMap = new HashMap<MovementDetail, BigDecimal>();
    // this list stores the MovementDetails that should not show warnings
    protected List<MovementDetail> movementDetailWithoutWarnings = new ArrayList<MovementDetail>();

    private FormulaState formulaState = FormulaState.NONE;

    private Boolean dispobleBalance = true;
    private Boolean addMaterial = false;
    private Boolean showMaterialDetail = false;
    private Boolean showInputDetail = false;
    private Boolean showDetailOrder = false;
    private Boolean showProductionOrders = true;
    private Boolean showReprocessedProduct = false;
    private Boolean showSingleProduct = false;
    private Boolean showListReprocessedProduct = true;
    private Boolean showButtonReprocessed = true;
    private Boolean showButtonAddProduct = true;
    private Boolean showProductionList = true;
    private Boolean showDetailSingleProduct = false;
    private Boolean showGenerateAllVoucher = true;
    private Boolean showGenerateRequestByPlanning = true;
    private Boolean showGenerateAllAccountEntries = true;

    private Double expendOld;
    private Double containerOld;
    private Integer codeOrder = 0;

    private List<MovementDetail> movementDetailSelect;

    private List<AccountOrderProduction> accountOrderProductions;

    private WarehouseVoucher warehouseVoucherSelect;

    private static final Integer SCALE = 6;

    private Boolean hasMainProduction = false;

    @In
    private SessionUser sessionUser;
    @In
    User currentUser;
    @In
    private ProductionPlanningService productionPlanningService;
    @In
    private ProcessedProductService processedProductService;
    @In
    private EvaluatorMathematicalExpressionsService evaluatorMathematicalExpressionsService;
    @In
    private ProductionOrderCodeGenerator productionOrderCodeGenerator;
    @In
    private EmployeeTimeCardService employeeTimeCardService;
    @In
    private ArticleEstateService articleEstateService;
    @In
    private IndirectCostsService indirectCostsService;
    @In
    protected WarehouseService warehouseService;
    @In
    protected CostCenterService costCenterService;
    @In
    protected BusinessUnitService businessUnitService;
    @In
    protected InventoryService inventoryService;
    @In
    protected JobContractService jobContractService;
    @In
    private ApprovalWarehouseVoucherService approvalWarehouseVoucherService;
    @In
    private MovementDetailService movementDetailService;
    @In
    private MetaProductService metaProductService;
    @In
    private VoucherService voucherService;
    @In
    private ProductionOrderService productionOrderService;

    @In
    private ProductInventoryService productInventoryService;

    @In(create = true)
    private SingleProductAction singleProductAction;

    private boolean showButtonAddInput = false;
    private Double volumeTotalInputMain = 0.0;
    private boolean showMainProduct = true;

    @Override
    protected GenericService getService() {
        return productionPlanningService;
    }

    public FormulaState getFormulaState() {
        return formulaState;
    }

    @Create
    public void createdComponent() {
        clearFormulation();
    }

    @Factory(value = "productionPlanning", scope = ScopeType.STATELESS)
    public ProductionPlanning initProductionPanning() {
        return getInstance();
    }

    @Factory(value = "productCompositionForPlanning", scope = ScopeType.STATELESS)
    public ProductComposition initProductComposition() {
        return productComposition;
    }

    @Factory(value = "productionOrderForPlanning", scope = ScopeType.STATELESS)
    public ProductionOrder initProductionOrder() {
        return productionOrder;
    }

    @Factory(value = "productionOrderMaterialForPlanning", scope = ScopeType.STATELESS)
    public ProductionOrder initProductionOrderMaterial() {
        return productionOrderMaterial;
    }

    @Factory(value = "baseProduct", scope = ScopeType.STATELESS)
    public BaseProduct initBaseProduct() {
        return baseProduct;
    }

    @Factory(value = "singleProduct", scope = ScopeType.STATELESS)
    public SingleProduct initSingleProduct() {
        return singleProduct;
    }

    @Factory(value = "processedProductForPlanning", scope = ScopeType.STATELESS)
    public ProcessedProduct initProcessedProduct() {
        return processedProduct;
    }

    @Begin(ifOutcome = Outcome.SUCCESS, flushMode = FlushModeType.MANUAL)
    public String createNew() {
        return Outcome.SUCCESS;
    }

    public void regularizarOrdenes(){

        Calendar startDate = Calendar.getInstance();
        Calendar endDate = Calendar.getInstance();
        startDate.set(2014,Calendar.JANUARY,1);
        endDate.set(2014,Calendar.JANUARY,31);

        //List<ProductionPlanning> planningConcurrents = productionPlanningService.getAllProductionPlanningByDates(startDate.getTime(),endDate.getTime());
        ProductionPlanning planning = getInstance();
        //for(ProductionPlanning planning: planningConcurrents){
                for(ProductionOrder order:planning.getProductionOrderList())
                {

                    Double totalMaterial = 0.0;
                    Double totalInput = 0.0;
                    for(OrderInput input: order.getOrderInputs())
                    {
                        Double cost = RoundUtil.getRoundValue(input.getCostTotal().doubleValue(),2, RoundUtil.RoundMode.SYMMETRIC);
                        input.setCostTotal(new BigDecimal(cost));
                        totalInput += cost;
                    }
                    order.setTotalPriceInput(totalInput);
                    for(OrderMaterial material: order.getOrderMaterials())
                    {
                        Double cost = RoundUtil.getRoundValue(material.getCostTotal().doubleValue(),2, RoundUtil.RoundMode.SYMMETRIC);
                        material.setCostTotal(new BigDecimal(cost));
                        totalMaterial  += cost;
                    }
                    order.setTotalPriceMaterial(totalMaterial);
                    order.setTotalCostProduction(totalInput+totalMaterial+order.getTotalPriceJourney()+order.getTotalIndirectCosts());

                }

            /*try {
                getService().update(planning);
            } catch (EntryDuplicatedException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            } catch (ConcurrencyException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }*/
        //}
        update();
    }

    public void regularizarCostosIndirectos(){
        Calendar startDate = Calendar.getInstance();
        Calendar endDate = Calendar.getInstance();
        startDate.set(2014,Calendar.JANUARY,1);
        endDate.set(2014,Calendar.JANUARY,31);

        List<ProductionPlanning> planningConcurrents = productionPlanningService.getAllProductionPlanningByDates(startDate.getTime(),endDate.getTime());
        ProductionPlanning planning = getInstance();
        //for(ProductionPlanning planning: planningConcurrents){

                for(ProductionOrder order:planning.getProductionOrderList()){
                    setTotalIndiRectCost(order,planning.getDate());
                    setTotalCostProducticionAndUnitPrice(order);
                }

                for(BaseProduct base:planning.getBaseProducts())
                {
                    for(SingleProduct single:base.getSingleProducts())
                    {
                        setTotalIndiRectCost(single,planning.getDate());
                        setTotalCostProducticionAndUnitPrice(single);
                    }
                }
                /*try {
                    getService().update(planning);
                } catch (EntryDuplicatedException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                } catch (ConcurrencyException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }*/
        //}
        //borrar
        update();
    }

    public void verificarCostosIndirectos(){

        Calendar startDate = Calendar.getInstance();
        Calendar endDate = Calendar.getInstance();
        startDate.set(2014,Calendar.JANUARY,1);
        endDate.set(2014,Calendar.JANUARY,31);

        List<ProductionPlanning> planningConcurrent = productionPlanningService.getAllProductionPlanningByDates(startDate.getTime(),endDate.getTime());
    }

    public void getTotalIndirectCostByProductioPlannig(List<ProductionPlanning> productionPlannings){
        Double energiaElectrica = 0.0;
        Double gasNatural = 0.0;
        Double depreciacionMaquinaria = 0.0;
        Double depreciacionHerramientas = 0.0;
        Double sueldosProduccion = 0.0;
        Double depreciacionMaquinariaUHT = 0.0;
        Double aportesPatronalesProduccion = 0.0;
        Double provisionAguinaldosProduccion = 0.0;
        Double provisionIndeminizacionProduccion = 0.0;
        Double sueldosEventualesProduccion = 0.0;

        for(ProductionPlanning planning: productionPlannings)
        {
            for(ProductionOrder order: planning.getProductionOrderList())
            {
               List<IndirectCosts>  costs = order.getIndirectCostses();

                energiaElectrica += costs.get(0).getAmountBs().doubleValue();
                gasNatural += costs.get(1).getAmountBs().doubleValue();
                depreciacionMaquinaria += costs.get(2).getAmountBs().doubleValue();
                depreciacionHerramientas += costs.get(3).getAmountBs().doubleValue();
                sueldosProduccion += costs.get(4).getAmountBs().doubleValue();
                depreciacionMaquinariaUHT += costs.get(5).getAmountBs().doubleValue();
                aportesPatronalesProduccion += costs.get(6).getAmountBs().doubleValue();
                provisionAguinaldosProduccion += costs.get(7).getAmountBs().doubleValue();
                provisionIndeminizacionProduccion += costs.get(8).getAmountBs().doubleValue();
                sueldosEventualesProduccion += costs.get(9).getAmountBs().doubleValue();
            }
        }

    }

    public void generateOnlyAllVoucher(){

        Date dateConcurrent = getInstance().getDate();
        if(!wasFinishedAllOrders())
        {
            showErrorVoucherSingle();
            return;
        }
        if(!wasFinishedAllReProcessed())
        {
            showErrorVoucherSingle();
            return;
        }

        for(BaseProduct base : getInstance().getBaseProducts())
        {
            if(base.getSingleProducts().size() ==0)
            {
                showErrorVoucherNotsingles();
                return;
            }
        }
        for(ProductionOrder order:getInstance().getProductionOrderList()){
            if(order.getEstateOrder() == FINALIZED && order.getEstateOrder() != TABULATED)
                generateOnlyVoucherOrderProduction(order,dateConcurrent);
        }
        for(BaseProduct base : getInstance().getBaseProducts()){
            if(base.getState() == PENDING && base.getState() != TABULATED)
                generateOnlyVoucherSingleProduction(base,dateConcurrent);
        }
        getInstance().setState(INSTOCK);
        try {
            productionPlanningService.updateProductionPlanning(getInstance());
        } catch (ConcurrencyException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (EntryDuplicatedException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        addMessageEnterProducts();
    }

    private void addMessageEnterProducts() {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.INFO,"ProductionPlanning.message.enterProducts");
    }

    public void executerAllOrdersPending()
    {

        for(ProductionOrder order:getInstance().getProductionOrderList())
        {
                if(order.getSelected())
                order.setEstateOrder(EXECUTED);
        }

        for(BaseProduct base:getInstance().getBaseProducts())
        {
                if(base.getSelected())
                base.setState(EXECUTED);
        }

        update();
    }

    public void generateAllAccountingEntries()
    {

        if(!wasInStockAllOrders())
        {
            showErrorVoucherNotstock();
            return;
        }
        if(!wasInStockAllReProcessed())
        {
            showErrorVoucherNotstock();
            return;
        }

        for(BaseProduct base : getInstance().getBaseProducts())
        {
            if(base.getSingleProducts().size() ==0)
            {
                showErrorVoucherNotsingles();
                return;
            }
        }

        /*if(isCostIndirectValid(getInstance().getProductionOrderList(),getInstance().getBaseProducts(), getInstance().getDate()))
        {
            return;
        }*/

        List<ProductionOrder> orderList = new ArrayList<ProductionOrder>();
        orderList.addAll(getInstance().getProductionOrderList());
        for(ProductionOrder order:orderList)
        {
            if(order.getEstateOrder() == INSTOCK && order.getEstateOrder() != TABULATED)
            {
                createInventoryMovement(order);
                productionPlanningService.updateOrdenProduction(order);
            }
        }
        for(BaseProduct base : getInstance().getBaseProducts())
        {
            if(base.getState() == INSTOCK && base.getState() != TABULATED)
            {
                createInventoryMovement(base);
                productionPlanningService.updateProductionBase(base);
            }
        }

        getInstance().setState(TABULATED);

        productionPlanningService.updateProductionPlanningDirect(getInstance());

        /* todo: Aprobacion de asientos de producción con oracle */
        //approvedAllVoucherEntries();
    }

    public void approvedAllVoucherEntries(){
        try {
            Boolean band = false;
            for(ProductionOrder order :getInstance().getProductionOrderList()) {
                voucherService.approvedAllVoucherEntries(Constants.defaultCompanyNumber
                        , Constants.BUSINESS_UNIT_COD_DEFAULT
                        , getInstance().getDate()
                        , getInstance().getDate()
                        , order.getNumberTransaction()
                        , Constants.FINACESS_USER_UNIT_DEFAULT
                        , Constants.INPUT_PROD_WAREHOUSE);
                List<VoucherServiceBean.ObsApprovedEntries> obsApprovedEntrieses = voucherService.getInfoTrasaction(
                        Constants.INPUT_PROD_WAREHOUSE,
                        order.getNumberTransaction(),
                        getInstance().getDate(),
                        getInstance().getDate()
                );

                if(obsApprovedEntrieses.size()>0)
                {
                    addErrorFailApprovedMessage(order.getCode());
                    band = true;
                }
            }

            for(BaseProduct base :getInstance().getBaseProducts())
            {
                voucherService.approvedAllVoucherEntries(Constants.defaultCompanyNumber
                        , Constants.BUSINESS_UNIT_COD_DEFAULT
                        , getInstance().getDate()
                        , getInstance().getDate()
                        , base.getNumberTransaction()
                        , Constants.FINACESS_USER_UNIT_DEFAULT
                        , Constants.INPUT_PROD_WAREHOUSE);
                List<VoucherServiceBean.ObsApprovedEntries> obsApprovedEntrieses = voucherService.getInfoTrasaction(
                        Constants.INPUT_PROD_WAREHOUSE,
                        base.getNumberTransaction(),
                        getInstance().getDate(),
                        getInstance().getDate()
                );

                if(obsApprovedEntrieses.size()>0)
                {
                    addErrorFailApprovedMessage(base.getCode());
                    band = true;
                }
            }
            if(band){
                addMessageGenerateAccountingProduction();
            }

        } catch (CompanyConfigurationNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void addMessageGenerateAccountingProduction(){
        facesMessages.addFromResourceBundle(StatusMessage.Severity.INFO,"AccountEntries.message.messageGenerateAccountingProduction");
    }

    private void addErrorFailApprovedMessage(String code) {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,"AccountEntries.error.failApprovedProducction",code);
    }

    //todo: revisar la validacion de de costos inidirectos por dia
    private boolean isCostIndirectValid(List<ProductionOrder> productionOrderList,List<BaseProduct> baseProducts, Date dateConcurrent) {

        Double totalCostIndirectPlanificacion = 0.0;
        for(ProductionOrder order:productionOrderList){
            for(IndirectCosts costs:order.getIndirectCostses())
            {
                totalCostIndirectPlanificacion += costs.getAmountBs().doubleValue();
            }
        }
        for(BaseProduct product: baseProducts)
            for(SingleProduct singleProduct:product.getSingleProducts()){
                for(IndirectCosts costs:singleProduct.getIndirectCostses())
                {
                    totalCostIndirectPlanificacion += costs.getAmountBs().doubleValue();
                }
            }

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(dateConcurrent);
        int monthConcurrent = calendar.get(Calendar.MONTH);
        int daysOfMounth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
        calendar.set(Calendar.MONTH,monthConcurrent -1);
        PeriodIndirectCost periodIndirectCost = indirectCostsService.getConcurrentPeroidIndirectCost(calendar.getTime());

        Double totalCostIndirectGeneral = 0.0;
        List<IndirectCosts> indirectCostsesOfMounth = indirectCostsService.getIndirectCostGeneral(periodIndirectCost);

        for(IndirectCosts costs: indirectCostsesOfMounth)
        {
            totalCostIndirectGeneral += costs.getAmountBs().doubleValue();
        }

        Double costIdirectByDay = totalCostIndirectGeneral /daysOfMounth;

        if((totalCostIndirectPlanificacion-costIdirectByDay ) >= 1)
        {
            addMessageCostIndirectError(totalCostIndirectPlanificacion, costIdirectByDay);
            return true;
        }

        return false;
    }

    private void addMessageCostIndirectError(Double totalCostIndirectPlanificacion, Double costIdirectByDay) {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR, "ProductionPlanning.message.MessageCostIndirectError",totalCostIndirectPlanificacion,costIdirectByDay);
    }

    public void generateAllVoucher(List<ProductionPlanning> productionPlannings)
    {
        for(ProductionPlanning planning: productionPlannings)
        {
            generateAllVoucher(planning);
        }
    }

    public void finalizedAllProductioOrders(List<ProductionPlanning> productionPlannings)
    {
        for(ProductionPlanning planning: productionPlannings)
        {
            this.makeFinalizedOrder(planning);
        }
    }

    public void generateAllVoucher(ProductionPlanning planning)
    {
        for(ProductionOrder order:planning.getProductionOrderList())
        {
            if(order.getEstateOrder() == FINALIZED && order.getEstateOrder() != TABULATED)
                generateVoucherOrderProduction(order,planning.getDate());
        }
        for(BaseProduct base:planning.getBaseProducts())
        {
            if(base.getState() == FINALIZED && base.getState() != TABULATED)
                generateVoucherSingleProduction(base,planning.getDate());
        }
    }

    public void generateVoucherOrderProduction(ProductionOrder order,Date dateConcurrent){

        order.setEstateOrder(TABULATED);

        //productionOrder.setEstateOrder(INSTOCK);

        InventoryMovement inventoryMovement = createVale(order,dateConcurrent);
        approvalVoucher(inventoryMovement);

        closeDetail();
        showProductionOrders = true;
        update();
    }

    public void generateOnlyVoucherOrderProduction(ProductionOrder order,Date dateConcurrent){

        order.setEstateOrder(INSTOCK);

        InventoryMovement inventoryMovement = createVale(order,dateConcurrent);
        approvalOnlyVoucher();

        //closeDetail();
        //showProductionOrders = true;
        order.setNumberVoucher(warehouseVoucherSelect.getNumber());
        productionPlanningService.updateOrdenProduction(order);
        //update();
    }

    public Boolean wasFinishedAllOrders(){

        for(ProductionOrder order:getInstance().getProductionOrderList())
        {
            if(order.getEstateOrder() != FINALIZED )
                return false;

        }

        return true;
    }

    public Boolean wasFinishedAllReProcessed(){
        for(BaseProduct base:getInstance().getBaseProducts())
            for(SingleProduct single: base.getSingleProducts())
            {
                if(single.getState() != FINALIZED)
                {
                    return false;
                }
            }


        return true;
    }

    public Boolean wasInStockAllOrders(){

        for(ProductionOrder order:getInstance().getProductionOrderList())
        {
            if(order.getEstateOrder() != INSTOCK )
                return false;

        }

        return true;
    }

    public Boolean wasInStockAllReProcessed(){

        for(BaseProduct base:getInstance().getBaseProducts())
            for(SingleProduct single: base.getSingleProducts())
            {
                if(single.getState() != INSTOCK)
                {
                    return false;
                }
            }

        return true;
    }

    public String generateVoucherSingleProduction(){


        if(baseProduct.getSingleProducts().size() ==0)
        {
            showErrorVoucherNotsingles();
            return Outcome.FAIL;
        }
        for(SingleProduct single: baseProduct.getSingleProducts())
        {
            if(single.getState() != FINALIZED)
            {
                showErrorVoucherSingle();
                return Outcome.FAIL;
            }
        }

        baseProduct.setState(TABULATED);
        InventoryMovement inventoryMovement = createVale(baseProduct,getInstance().getDate());
        approvalVoucher(inventoryMovement);
        closeDetail();
        showProductionOrders = true;
        return update();
    }

    public void generateOnlyVoucherSingleProduction(BaseProduct base,Date dateConcurrent){

        base.setState(INSTOCK);
        InventoryMovement inventoryMovement = createOnlyVale(base,dateConcurrent);
        approvalOnlyVoucher();
        base.setNumberVoucher(warehouseVoucherSelect.getNumber());
        productionPlanningService.updateProductionBase(base);
        //update();
        //refreshInstance();
    }

    public void generateVoucherSingleProduction(BaseProduct base,Date dateConcurrent){


        if(base.getSingleProducts().size() ==0)
        {
            showErrorVoucherNotsingles();
            return;
        }
        for(SingleProduct single: base.getSingleProducts())
        {
            if(single.getState() != FINALIZED)
            {
                showErrorVoucherSingle();
                return;
            }
        }

        base.setState(TABULATED);
        InventoryMovement inventoryMovement = createVale(base,dateConcurrent);
        approvalVoucher(inventoryMovement);
        for(SingleProduct single: base.getSingleProducts())
        {
            single.setState(TABULATED);
        }
        closeDetail();
        showProductionOrders = true;
        update();
    }

    public void showErrorVoucherSingle(){
       facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR, "SingleProduct.message.errorState");
    }

    public void showErrorVoucherNotstock(){
        facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR, "SingleProduct.message.errorNotStock");
    }

    public void showErrorVoucherNotsingles(){
        facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR, "SingleProduct.message.notOrders");
    }

    public void initEditFormula() {
        clearFormulation();
        formulaState = FormulaState.NEW;

        if(getInstance().getId() == null && getInstance().getProductionOrderList().size() > 0 )
        {
            for (ProductionOrder order :getInstance().getProductionOrderList())
            {
                if(getCodeOrder(order.getCode()) > codeOrder)
                codeOrder = getCodeOrder(order.getCode());
            }
            String[] arr=productionOrderCodeGenerator.generateCode().split("\\-");
            String code = arr[0]+"-"+String.format("%04d", codeOrder+1);
            productionOrder.setCode(code);
        }
        else
        {
            codeOrder = getCodeOrder(productionOrderCodeGenerator.generateCode());
            productionOrder.setCode(productionOrderCodeGenerator.generateCode());
        }
        productionOrder.setExpendAmount(0.0);
        hideButtonGeneral();
        hideTablesIni();
    }

    private Integer getCodeOrder(String code)
    {
        String[] arr=code.split("\\-");
        Integer val = Integer.parseInt(arr[1]);
        return val;
    }

    @Begin(ifOutcome = Outcome.SUCCESS, flushMode = FlushModeType.MANUAL)
    public String select(ProductionPlanning productionPlanning) {
        showDetailOrder = false;
        try {
            ProductionPlanning aux = productionPlanningService.find(productionPlanning.getId());
            setInstance(aux);
            setOp(OP_UPDATE);
            return Outcome.SUCCESS;
        } catch (Exception ex) {
            log.error(ex);
            facesMessages.addFromResourceBundle(ERROR, "Common.globalError.description");
            return Outcome.REDISPLAY;
        }
    }

    public List<Consolidated> getConsolidatedInputs() {
        try {
            ProductionPlanning productionPlanning = getInstance();

            Map<Long, Consolidated> consolidated = new HashMap<Long, Consolidated>();
            for (ProductionOrder order : productionPlanning.getProductionOrderList()) {
                //evaluatorMathematicalExpressionsService.executeMathematicalFormulas(order);
                evaluatorMathematicalExpressionsService.excuteFormulate(order, order.getContainerWeight(), order.getExpendAmount());
                for (ProductionIngredient ingredient : order.getProductComposition().getProductionIngredientList()) {
                    Consolidated aux = consolidated.get(ingredient.getMetaProduct().getId());
                    if (aux == null) {
                        aux = new Consolidated();
                        aux.setProduct(ingredient.getMetaProduct());
                        aux.setIdMeta(ingredient.getMetaProduct().getId());
                        aux.setName(ingredient.getMetaProduct().getName());
                        aux.setCode(ingredient.getMetaProduct().getCode());
                        aux.setUnit(ingredient.getMetaProduct().getProductItem().getUsageMeasureCode());
                        aux.setAmountWarehouse(productionPlanningService.getMountInWarehouse(ingredient.getMetaProduct()));
                        consolidated.put(ingredient.getMetaProduct().getId(), aux);
                    }

                    aux.setAmount(aux.getAmount() + ingredient.getAmount());
                }
            }
            return new ArrayList<Consolidated>(consolidated.values());
        } catch (Exception ex) {
            log.error("Exception caught", ex);
            facesMessages.addFromResourceBundle(ERROR, "Common.globalError.description");
            return new ArrayList<Consolidated>();
        }
    }

    public double getTotalWeight() {
        if (productionOrder.getProductComposition() == null) return 0.0;

        double total = 0.0;
        for (ProductionIngredient ingredient : productionOrder.getProductComposition().getProductionIngredientList()) {
            if (ingredient.getMetaProduct().getProductItem().getUsageMeasureCode().equals("GR"))
                total = total + ingredient.getAmount() / 1000.0;
            else
                total = total + ingredient.getAmount();

        }
        return total;
    }

    public void productCompositionSelected(ActionEvent e) {
        try {
            //productionOrder.setExpendAmount(productComposition.getProducingAmount());
            productionOrder.setExpendAmount(productComposition.getSupposedAmount());
            productionOrder.setContainerWeight(productComposition.getContainerWeight());
            //productionOrder.setProducedAmount(productComposition.getSupposedAmount());
            //productionOrder.setProducedAmount(productionOrder.getExpendAmount());
            productionOrder.setProductComposition(productComposition);
            evaluatorMathematicalExpressionsService.excuteFormulate(productionOrder, productComposition.getContainerWeight(), productionOrder.getExpendAmount());
            setInputs();
            dispobleBalance = true;
            showButtonAddInput = true;
            showMainProduct = false;
            agregarInsumosDeProductosCompuestos();

        } catch (Exception ex) {
            log.error("Exception caught", ex);
            facesMessages.addFromResourceBundle(ERROR, "Common.globalError.description");
        }
    }

    private void agregarInsumosDeProductosCompuestos() throws IOException, ProductCompositionException {

    List<OrderInput> aux = new ArrayList<OrderInput>();
        aux.clear();
        aux.addAll(productionOrder.getOrderInputs());
     for(OrderInput orderInput:aux){
         if(articleEstateService.verifyEstate(orderInput.getProductItem(),Constants.ESTATE_ARTICLE_COMPOSITE))
         {
             ProcessedProduct compuesto = processedProductService.findByCode(orderInput.getProductItemCode());
             //todo: se tomara en cuenta la primera formulacion por defecto
             ProductComposition formulacion = compuesto.getProductCompositionList().get(0);
             evaluatorMathematicalExpressionsService.excuteFormulate(formulacion.getProductionIngredientList(),orderInput.getAmount(),orderInput.getAmount(),orderInput.getAmount());
             for (ProductionIngredient ingredient : formulacion.getProductionIngredientList()) {
                 int pos = yaSeEncuentra(ingredient.getMetaProduct().getProductItemCode());
                 if(pos != -1){
                     OrderInput input = productionOrder.getOrderInputs().get(pos);
                     input.setAmount(input.getAmount()+ingredient.getAmount());
                 }else {
                     OrderInput input = new OrderInput();
                     input.setProductItem(ingredient.getMetaProduct().getProductItem());
                     input.setProductionOrder(productionOrder);
                     input.setAmount(ingredient.getAmount());
                     input.setAmountStock(ingredient.getMountWareHouse());
                     input.setProductItemCode(ingredient.getMetaProduct().getProductItemCode());
                     input.setCompanyNumber(ingredient.getMetaProduct().getCompanyNumber());
                     input.setMathematicalFormula(ingredient.getMathematicalFormula());
                     BigDecimal costUnit;

                     if (articleEstateService.verifyEstate(ingredient.getMetaProduct().getProductItem(), Constants.ESTATE_ARTICLE_COMPOSITE))
                         costUnit = getCostUnitProdComposite(ingredient);
                     else
                         costUnit = ingredient.getMetaProduct().getProductItem().getUnitCost();

                     input.setCostUnit(costUnit);
                     input.setCostTotal(new BigDecimal(RoundUtil.getRoundValue((ingredient.getAmount() * costUnit.doubleValue()), 2, RoundUtil.RoundMode.SYMMETRIC)));
                     productionOrder.getOrderInputs().add(input);
                 }
             }
         }
     }
    }

    public int yaSeEncuentra(String codArt){
        int band = -1;
        int cont = 0;
        for(OrderInput input:productionOrder.getOrderInputs()){
            if(input.getProductItemCode().equals(codArt)){
                band = cont;
            }
            cont ++;
        }
        return band;
    }

    public void saveGreasePercentaje(ProductionOrder order)
    {
        //productionPlanningService.updateOrder(order);
        update();
        refreshInstance();
    }

    public void saveProduccingAmount()
    {
        update();
        refreshInstance();
    }

    public void changeState(ProductionOrder order)
    {
        order.setEstateOrder(PENDING);
        update();
        refreshInstance();
    }

    public void changeState(SingleProduct single)
    {
        single.setState(PENDING);
        update();
        refreshInstance();
    }

    public Boolean verifAmount(ProductionIngredient ingredient) {
        Boolean band = true;
        if (!articleEstateService.existArticleEstate(ingredient.getMetaProduct().getProductItem()))
            if (ingredient.getMountWareHouse().doubleValue() < ingredient.getAmount()) {
                band = false;
                dispobleBalance = false;
            }

        return band;
    }

    public void saveGreasePercentajeSingle()
    {
        update();
        refreshInstance();
    }

    public Boolean verifAmountInput(OrderInput orderInput) {
        Boolean band = true;

        if (!articleEstateService.existArticleEstate(orderInput.getProductItem()))
            if (orderInput.getAmountStock().doubleValue() < orderInput.getAmount()) {
                band = false;
                dispobleBalance = false;
            }else{
                dispobleBalance = true;
            }

        return band;
    }

    public Boolean verifStockInput(OrderInput orderInput) {
        Boolean band = true;
        BigDecimal stock = BigDecimal.ZERO;
        if (!articleEstateService.existArticleEstate(orderInput.getProductItem()))
            //if (productionPlanningService.getMountInWarehouse(orderInput.getProductItem()).doubleValue() < orderInput.getAmount()) {
            stock = productionPlanningService.getMountInWarehouse(orderInput.getProductItem());
            if (stock.doubleValue() < 0) {
                band = false;
                dispobleBalance = false;
            }

        orderInput.setAmountStock(stock);
        return band;
    }

    public Boolean isParameterized(OrderInput productItem) {
        return articleEstateService.verifyEstate(productItem.getProductItem(), "PARAMETRIZABLE");
    }

    public Boolean isNotCountAs(ProductItem productItem) {
        if(articleEstateService.verifyEstate(productItem, "NOCONTABILLIZABLE"))
        {
            addMessageInputWithoutAccount(productItem.getFullName());
            return true;
        }
        return false;
    }
    //todo: verificar que el valor inicial del total
    public void addFormulation() {

        for(OrderInput input: productionOrder.getOrderInputs())
        {
            if(input.getAmount() == 0.0)
            {
                addMessageAmountInputIsCero(input.getProductItem().getFullName());
                return;
            }
        }

        ProductionPlanning productionPlanning = getInstance();
        if(productionOrder.getProductMain() != null && productionOrder.getProductComposition() == null)
        {
            ProductOrder productOrder = new ProductOrder();
            productOrder.setProcessedProduct(processedProduct);
            productOrder.setProductionOrder(productionOrder);
            productOrder.setFullName(processedProduct.getFullName());

            if(productionOrder.getProductMain().getTotalCostInputMain() == 0.0 || productionOrder.getProductMain().getTotalCostInputMain() == null) {
                productionOrder.getProductMain().setTotalCostInputMain(productionOrder.getProductMain().getTotalPriceInput());
                try {
                    productionOrderService.update(productionOrder.getProductMain());
                } catch (EntryDuplicatedException e) {
                    e.printStackTrace();
                } catch (ConcurrencyException e) {
                    e.printStackTrace();
                }
            }


            productionOrder.getProductOrders().add(productOrder);
            productionOrder.setContainerWeight(0.0);
            productionOrder.setExpendAmount(0.0);
        }


        productionPlanning.getProductionOrderList().add(productionOrder);
        productionOrder.setProductionPlanning(productionPlanning);
        //productionOrder.setProducedAmount(productionOrder.getExpendAmount());
        if (productionOrder.getOrderInputs().size() == 0)
            setInputs();
        if(productionOrder.getProductMain() == null )
        productionOrder.getOrderInputs().addAll(productIputToFormulation);

        setTotalsInputsIni(productionOrder);
        setTotalCostProducticionAndUnitPrice(productionOrder);
        setValuesMilks();
        if (productionPlanning.getId() != null && !verifySotck(productionOrder))
            if (update() != Outcome.SUCCESS) {
                return;
            }
        if(productionOrder.getProductMain() == null)
        addMessageOrderCreateSuccess(productionOrder.getCode(),productionOrder.getProductComposition().getProcessedProduct().getFullName());
        else
        addMessageOrderCreateSuccess(productionOrder.getCode(), productionOrder.getProductOrders().get(0).getProcessedProduct().getFullName());

        clearFormulation();
        disableEditingFormula();
        showProductionOrders = true;
        showButtonAddInput = false;
        showInit();
    }

    private void adjustCostInput() {
            Double volumeTotal = totalVolumeInputs();
            for (ProductionOrder order : getInstance().getProductionOrderList()) {
                if (order.getProductMain().getId() == productionOrder.getProductMain().getId()) {
                    updateCostInputs(volumeTotal, order);
                }
            }
            updateCostInputs(volumeTotal, productionOrder.getProductMain());
    }

    private void updateCostInputs(Double volumeTotal,ProductionOrder  order)
    {
        Double orderVolume = 0.0;
        if(order.getProductMain() != null)
            orderVolume = calculateVolume(order.getProductOrders().get(0).getProcessedProduct(),order.getProductOrders().get(0).getProcessedProduct().getAmount(),order.getProducedAmount());
        else
            orderVolume = calculateVolume(order.getProductComposition().getProcessedProduct(),order.getProductComposition().getProcessedProduct().getAmount(),order.getProducedAmount());

        Double porcentageOrder = (orderVolume * 100) /volumeTotal;
        Double totalInput = 0.0;
        if(order.getProductMain() != null) {
            totalInput = order.getProductMain().getTotalCostInputMain();
            order.setTotalCostInputMain((porcentageOrder / 100) * totalInput);
        }
        else {
            totalInput = order.getTotalCostInputMain();
            order.setTotalPriceInput((porcentageOrder /100)* totalInput);
        }

    }
    //no esta funcionando
    private Double totalVolumeInputs()
    {
        Double volumeTotal = 0.0;
        Long idMain = new Long(0);
        if(productionOrder.getProductMain() != null) {
            volumeTotal = calculateVolume(productionOrder.getProductMain().getProductComposition().getProcessedProduct(),
                    productionOrder.getProductMain().getProductComposition().getProcessedProduct().getAmount(),
                    productionOrder.getProductMain().getProducedAmount());
            idMain = productionOrder.getProductMain().getId();
        }
        else {
            if (productionOrder.getTotalCostInputMain() > 0.0) {
                volumeTotal = calculateVolume(productionOrder.getProductComposition().getProcessedProduct(),
                        productionOrder.getProductComposition().getProcessedProduct().getAmount(),
                        productionOrder.getProducedAmount());
                idMain = productionOrder.getId();
            }
        }

        for(ProductionOrder order:getInstance().getProductionOrderList())
        {
            if(order.getProductMain() != null )
                if(order.getProductMain().getId() == idMain)
                {
                    volumeTotal += calculateVolume(order.getProductOrders().get(0).getProcessedProduct(),order.getProductOrders().get(0).getProcessedProduct().getAmount(),order.getProducedAmount());
                }
        }

        return volumeTotal;
    }

    private Double calculateVolume(ProcessedProduct processedProductMain,Double amountProductProced, Double amount)
    {
        Double volumeTotal;
        if(processedProductMain.getUnidMeasure() == "LT" || processedProductMain.getUnidMeasure() == "KG")
        {
            volumeTotal = amount * (amountProductProced * 1000);
        }else
        {
            volumeTotal = amount * amountProductProced;
        }

        return volumeTotal;
    }

    public void updateFormulation() {
        for(OrderInput input: productionOrder.getOrderInputs())
        {
            if(input.getAmount() == 0.0)
            {
                addMessageAmountInputIsCero(input.getProductItem().getFullName());
                return;
            }
        }
        if(productionOrder.getProductMain() == null)
        if (evaluateMathematicalExpression() == false) {
            return;
        }
        ProductionPlanning planning = getInstance();
        productionOrder.getOrderInputs().addAll(productIputToFormulation);

        setTotalCostProducticionAndUnitPrice(productionOrder);
        setValuesMilks();
        if (planning.getId() != null && !verifySotck(productionOrder))
            if (update() != Outcome.SUCCESS) {
                return;
            }

        existingFormulation = null;
        disableEditingFormula();
        showProductionOrders = true;
        showButtonAddInput = false;
        showInit();
        addMessageOrderUpdateSuccess(productionOrder.getCode(),processedProduct.getFullName());
        productIputToFormulation.clear();
    }

    private void addMessageOrderUpdateSuccess(String code, String fullName) {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.INFO,"ProductionOrder.messageOrderUpdateSuccess",code,fullName);
    }

    private void addMessageOrderCreateSuccess(String code, String fullName) {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.INFO,"ProductionOrder.messageOrderCreateSuccess",code,fullName);
    }

    private void addMessageAmountInputIsCero(String fullName) {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,"ProductionOrder.productionInput.messageAmountInputIsCero",fullName);
    }

    private Boolean verifySotck(ProductionOrder order) {
        Boolean band = false;
        if(order.getProductMain() == null)
        for (ProductionIngredient ingredient : order.getProductComposition().getProductionIngredientList()) {
            BigDecimal mountWareHouse = productionPlanningService.getMountInWarehouse(ingredient.getMetaProduct().getProductItem());
            if (!articleEstateService.existArticleEstate(ingredient.getMetaProduct().getProductItem()))
                if (ingredient.getAmount() > mountWareHouse.doubleValue()) {
                    addMessageError(ingredient.getMetaProduct().getProductItem().getName(), mountWareHouse.doubleValue());
                    band = true;
                }
        }
        return band;
    }

    @End
    public String create(List<Consolidated> consolidateds) {
        Boolean band = true;
        try {
            for (Consolidated consolidated : consolidateds) {
                BigDecimal mountWareHouse = productionPlanningService.getMountInWarehouse(consolidated.getProduct());
                if (!articleEstateService.existArticleEstate(consolidated.getProduct().getProductItem()))  //si lo encuentra en la lista no lo toma encuenta
                    if (consolidated.getAmount() > mountWareHouse.doubleValue()) {
                        //addMessageError(consolidated, mountWareHouse.doubleValue());
                        band = false;
                    }
            }

            if (band) {
                ProductionPlanning productionPlanning = getInstance();
                //setZeroProducedAmount(productionPlanning);
                getService().create(productionPlanning);
                addCreatedMessage();
                return Outcome.SUCCESS;
            } else {
                return Outcome.REDISPLAY;
            }

        } catch (EntryDuplicatedException e) {
            addDuplicatedMessage();
            return Outcome.REDISPLAY;
        }
    }

    @End
    public String createNewPlannig() {
        Boolean band = true;
        try {
            for (OrderInput input : productionOrder.getOrderInputs()) {
                BigDecimal mountWareHouse = productionPlanningService.getMountInWarehouse(input.getProductItem());
                if (!articleEstateService.existArticleEstate(input.getProductItem()))  //si lo encuentra en la lista no lo toma encuenta
                    if (input.getAmount() > mountWareHouse.doubleValue()) {
                        addMessageError(input, mountWareHouse.doubleValue());
                        band = false;
                    }
            }

            if (band) {
                ProductionPlanning productionPlanning = getInstance();
                getService().create(productionPlanning);
                addCreatedMessage();
                return Outcome.SUCCESS;
            } else {
                return Outcome.REDISPLAY;
            }

        } catch (EntryDuplicatedException e) {
            addDuplicatedMessage();
            return Outcome.REDISPLAY;
        }
    }

    /*private void setZeroProducedAmount(ProductionPlanning planning)
    {
        for(ProductionOrder productionOrder:planning.getProductionOrderList() )
        {
            productionOrder.setProducedAmount(0.0);
        }
    }*/

    public void addMessageGenerateAccountingEntry(String numOrder)
    {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.INFO, "ProductionPlanning.message.generateAllAccountingEntry",numOrder);
    }

    public void addMessageCantProducerCero()
    {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR, "ProductionPlanning.waning.amountCero");
    }

    public void addMessageCantCeroMaterial()
    {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR, "ProductionPlanning.waning.amountCeroMaterial");
    }

    public void addMessageMaterialCantUsedMajorRequerided()
    {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR, "ProductionPlanning.error.cantUsedMajorRequerided");
    }

    public void addMessageCreateWarehouseVoucherInput()
    {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.INFO, "WarehouseVoucher.generateVoucher");
    }

    public void addMessageInputWithoutAccount(String nameInput)
    {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.INFO, "WarehouseVoucher.inpuWithputAccount",nameInput);
    }

    public void addMessageVerifyMaterial()
    {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.INFO, "ProductionPlanning.material.size");
    }

    public void addMessageError(OrderInput input, Double mount) {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.WARN, "Common.message.errorMountWarehouse", input.getProductItem().getName(), mount);
    }

    public void addMessageError(String name, Double mount) {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.WARN, "Common.message.errorMountWarehouse", name, mount);
    }

    public void evaluateExpressionActionListener(ActionEvent e) {
        evaluateMathematicalExpression();
    }

    //public void evaluateParameterizedExpressionActionListener(ActionEvent e,OrderInput input) {
    public void evaluateParameterizedExpressionActionListener(OrderInput input) {
        if(input.getType().compareTo("ADD") != 0) {
            try {

                if (expendOld == null)
                    expendOld = productionOrder.getExpendAmount();
                if (containerOld == null)
                    containerOld = productionOrder.getContainerWeight();

                Double container = evaluatorMathematicalExpressionsService.excuteParemeterized(input, productionOrder, productionOrder.getProductComposition().getContainerWeight(), productionOrder.getProductComposition().getSupposedAmount());
                //productionOrder.getProductComposition().setContainerWeight(container);
                productionOrder.setContainerWeight(container);
                //productionOrder.setExpendAmount(evaluatorMathematicalExpressionsService.getAmountExpected(expendOld,containerOld,container));
                productionOrder.setExpendAmount(evaluatorMathematicalExpressionsService.getAmountExpected(productionOrder.getProductComposition().getSupposedAmount(), productionOrder.getProductComposition().getContainerWeight(), container));

            } catch (ProductCompositionException e1) {
                e1.printStackTrace();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            evaluateParameterizedExpression(input);
            productionOrder.getOrderInputs().addAll(productionPlanningService.getInputsAdd(productionOrder));
        }{
           input.setCostTotal(new BigDecimal(RoundUtil.getRoundValue(input.getAmount() * input.getCostUnit().doubleValue(),2, RoundUtil.RoundMode.SYMMETRIC)));
        }
        try {
            agregarInsumosDeProductosCompuestos();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ProductCompositionException e) {
            e.printStackTrace();
        }

    }

    private boolean evaluateParameterizedExpression(OrderInput input) {
        try {
            evaluatorMathematicalExpressionsService.excuteParemeterizadFormulate(productionOrder, productionOrder.getProductComposition().getContainerWeight(), productionOrder.getProductComposition().getSupposedAmount());
            setInputsParametrized(productionOrder.getProductComposition().getProductionIngredientList(), input);
            return true;
        } catch (Exception ex) {
            log.error("Exception caught", ex);
            facesMessages.addFromResourceBundle(ERROR, "Common.globalError.description");
            return false;
        }
    }

    private boolean evaluateMathematicalExpression() {
        try {
            if (containerOld != null) {
                productionOrder.setContainerWeight(containerOld);
                //  productionOrder.getProductComposition().setContainerWeight(containerOld);
            }

            evaluatorMathematicalExpressionsService.excuteFormulate(productionOrder, productionOrder.getProductComposition().getContainerWeight(), productionOrder.getProductComposition().getSupposedAmount());
            setInputs();
            return true;
        } catch (Exception ex) {
            log.error("Exception caught", ex);
            facesMessages.addFromResourceBundle(ERROR, "Common.globalError.description");
            return false;
        }
    }

    private void setInputs() {

        productionOrder.getOrderInputs().clear();
        if(productionOrder.getProductComposition() != null)
        for (ProductionIngredient ingredient : productionOrder.getProductComposition().getProductionIngredientList()) {
            OrderInput input = new OrderInput();
            input.setProductItem(ingredient.getMetaProduct().getProductItem());
            input.setProductionOrder(productionOrder);
            input.setAmount(ingredient.getAmount());
            input.setAmountStock(ingredient.getMountWareHouse());
            input.setProductItemCode(ingredient.getMetaProduct().getProductItemCode());
            input.setCompanyNumber(ingredient.getMetaProduct().getCompanyNumber());
            input.setMathematicalFormula(ingredient.getMathematicalFormula());
            BigDecimal costUnit;

            if(articleEstateService.verifyEstate(ingredient.getMetaProduct().getProductItem(), Constants.ESTATE_ARTICLE_COMPOSITE))
                costUnit = getCostUnitProdComposite(ingredient);
            else
                costUnit = ingredient.getMetaProduct().getProductItem().getUnitCost();

            input.setCostUnit(costUnit);
            input.setCostTotal(new BigDecimal(RoundUtil.getRoundValue( (ingredient.getAmount() * costUnit.doubleValue()),2, RoundUtil.RoundMode.SYMMETRIC)));
            productionOrder.getOrderInputs().add(input);
        }

        productionOrder.getOrderInputs().addAll(productionPlanningService.getInputsAdd(productionOrder));
    }

    private void clearOnlyAllInputs(List<OrderInput> orderInputs){
        List<OrderInput> aux = new ArrayList<OrderInput>();
        aux.addAll(orderInputs);

        for(OrderInput orderInput: aux)
        {
            if(orderInput.getId() != null)
            {
                orderInputs.remove(orderInput);
            }
        }

    }

    private void setInputsParametrized(List<ProductionIngredient> productionIngredientList, OrderInput inputParameterize) {
        productionOrder.getOrderInputs().clear();
        for (ProductionIngredient ingredient : productionOrder.getProductComposition().getProductionIngredientList()) {
            if (inputParameterize.getProductItem() != ingredient.getMetaProduct().getProductItem()) {
                OrderInput input = new OrderInput();
                input.setProductItem(ingredient.getMetaProduct().getProductItem());
                input.setProductionOrder(productionOrder);
                input.setAmount(ingredient.getAmount());
                input.setAmountStock(ingredient.getMountWareHouse());
                input.setProductItemCode(ingredient.getMetaProduct().getProductItemCode());
                input.setCompanyNumber(ingredient.getMetaProduct().getCompanyNumber());
                input.setMathematicalFormula(ingredient.getMathematicalFormula());
                BigDecimal costUnit;

                if(articleEstateService.verifyEstate(ingredient.getMetaProduct().getProductItem(), Constants.ESTATE_ARTICLE_COMPOSITE))
                costUnit = getCostUnitProdComposite(ingredient);
                else
                costUnit = ingredient.getMetaProduct().getProductItem().getUnitCost();

                input.setCostUnit(costUnit);
                input.setCostTotal(new BigDecimal(RoundUtil.getRoundValue((ingredient.getAmount() * costUnit.doubleValue()),2, RoundUtil.RoundMode.SYMMETRIC)));
                productionOrder.getOrderInputs().add(input);
            } else {

                if(articleEstateService.verifyEstate(inputParameterize.getProductItem(), Constants.ESTATE_ARTICLE_COMPOSITE))
                    inputParameterize.setCostUnit(getCostUnitProdComposite(ingredient));

                inputParameterize.setCostTotal(new BigDecimal(RoundUtil.getRoundValue((inputParameterize.getCostUnit().doubleValue() * inputParameterize.getAmount()),2, RoundUtil.RoundMode.SYMMETRIC)));
                productionOrder.getOrderInputs().add(inputParameterize);
            }
        }
    }

    private BigDecimal getCostUnitProdComposite(ProductionIngredient ingredient) {

        List<ProductionIngredient> aux = new ArrayList<ProductionIngredient>();

        ProductComposition composition = processedProductService.getProductComposite(ingredient.getMetaProduct().getId());
        aux = composition.getProductionIngredientList();
        try {
            evaluatorMathematicalExpressionsService.excuteFormulate(aux,ingredient.getAmount(),composition.getContainerWeight(),composition.getSupposedAmount());
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (ProductCompositionException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        Double totalcost = calculateCostTotal(aux);
        aux = null;
        return new BigDecimal(totalcost/ingredient.getAmount());
    }

    public WarehouseVoucher createWarehouseVoucherMaterial(){

        WarehouseVoucher warehouseVoucher =  new WarehouseVoucher();
        warehouseVoucher.setCompanyNumber(Constants.defaultCompanyNumber);
        warehouseVoucher.setDocumentCode(Constants.CODE_WAREHOUSEDOCUMENTYPE_EGRESS);
        warehouseVoucher.setDate(new Date());
        warehouseVoucher.setState(WarehouseVoucherState.PEN);
        warehouseVoucher.setWarehouseCode(Constants.CODE_WAREHOUSE_PRODUCT_MATERIAL);
        warehouseVoucher.setCostCenter(costCenterService.findCostCenterByCode(Constants.DEFAULT_COST_CENTER_PRODUCTION));
        warehouseVoucher.setCostCenterCode(Constants.DEFAULT_COST_CENTER_PRODUCTION);
        warehouseVoucher.setWarehouse(warehouseService.findWarehouseByCode(Constants.CODE_WAREHOUSE_PRODUCT_MATERIAL));
        warehouseVoucher.setDocumentType(productionPlanningService.getDefaultDocumentType());
        warehouseVoucher.setResponsible(currentUser.getEmployee());
        warehouseVoucher.setExecutorUnit(businessUnitService.findBusinessUnitByExecutorUnitCode(Constants.BUSINESS_UNIT_COD_DEFAULT));

        return warehouseVoucher;
    }

    public WarehouseVoucher createWarehouseVoucherInput(){
        /*Crear el vale*/
        WarehouseVoucher warehouseVoucher =  new WarehouseVoucher();
        warehouseVoucher.setCompanyNumber(Constants.defaultCompanyNumber);
        warehouseVoucher.setDocumentCode(Constants.CODE_WAREHOUSEDOCUMENTYPE_EGRESS);
        warehouseVoucher.setDate(new Date());
        warehouseVoucher.setState(WarehouseVoucherState.PEN);
        warehouseVoucher.setWarehouseCode(Constants.CODE_WAREHOUSE_PRODUCT_INPUT);
        warehouseVoucher.setCostCenter(costCenterService.findCostCenterByCode(Constants.DEFAULT_COST_CENTER_PRODUCTION));
        warehouseVoucher.setCostCenterCode(Constants.DEFAULT_COST_CENTER_PRODUCTION);
        Warehouse warehouse = warehouseService.findWarehouseByCode(Constants.CODE_WAREHOUSE_PRODUCT_INPUT);
        warehouseVoucher.setWarehouse(warehouse);
        warehouseVoucher.setDocumentType(productionPlanningService.getDefaultDocumentType());
        warehouseVoucher.setPetitionerJobContract(jobContractService.lastJobContractByEmployee(currentUser.getEmployee()));
        warehouseVoucher.setResponsible(warehouse.getResponsible());
        warehouseVoucher.setExecutorUnit(businessUnitService.findBusinessUnitByExecutorUnitCode(Constants.BUSINESS_UNIT_COD_DEFAULT));

        return warehouseVoucher;
    }

    public WarehouseVoucher createWarehouseVoucherOrder(Date dateConcurrent){

        WarehouseVoucher warehouseVoucher =  new WarehouseVoucher();
        warehouseVoucher.setCompanyNumber(Constants.defaultCompanyNumber);
        warehouseVoucher.setDocumentCode(Constants.CODE_WAREHOUSEDOCUMENTYPE_RECEPTION);
        warehouseVoucher.setDate(dateConcurrent);
        warehouseVoucher.setState(WarehouseVoucherState.PEN);
        warehouseVoucher.setWarehouseCode(Constants.CODE_WAREHOUSE_PRODUCT_END);
        warehouseVoucher.setCostCenter(costCenterService.findCostCenterByCode(Constants.DEFAULT_COST_CENTER_PRODUCTION));
        warehouseVoucher.setCostCenterCode(Constants.DEFAULT_COST_CENTER_PRODUCTION);
        Warehouse warehouse = warehouseService.findWarehouseByCode(Constants.CODE_WAREHOUSE_PRODUCT_END);
        warehouseVoucher.setWarehouse(warehouse);
        warehouseVoucher.setDocumentType(productionPlanningService.getRecepcionDocumentType());
        warehouseVoucher.setResponsible(warehouse.getResponsible());
        warehouseVoucher.setExecutorUnit(businessUnitService.findBusinessUnitByExecutorUnitCode(Constants.BUSINESS_UNIT_COD_DEFAULT));

        return warehouseVoucher;
    }

    public InventoryMovement createInventoryMovement(String description){
        InventoryMovement inventoryMovement = new InventoryMovement();
        inventoryMovement.setDescription(description);

        return inventoryMovement;
    }

    public void generateValeMaterial(){

        if(productionOrder.getOrderMaterials().size() == 0)
        {
            addMessageVerifyMaterial();
            return;
        }

        WarehouseVoucher warehouseVoucher = new WarehouseVoucher();
        warehouseVoucher = createWarehouseVoucherMaterial();
        InventoryMovement inventoryMovement = createInventoryMovement("egreso de materiales de prueba");
        List<MovementDetail> movementDetails = generateMovementDetailMaterial(productionOrder.getOrderMaterials());

        Map<MovementDetail, BigDecimal> movementDetailUnderMinimalStockMap = new HashMap<MovementDetail, BigDecimal>();
        Map<MovementDetail, BigDecimal> movementDetailOverMaximumStockMap = new HashMap<MovementDetail, BigDecimal>();
        List<MovementDetail> movementDetailWithoutWarnings = new ArrayList<MovementDetail>();
        try {
            warehouseService.saveWarehouseVoucher(warehouseVoucher, inventoryMovement, movementDetails,
                    movementDetailUnderMinimalStockMap,
                    movementDetailOverMaximumStockMap,
                    movementDetailWithoutWarnings);
            addMessageCreateWarehouseVoucherInput();
        } catch (InventoryException e) {
            addInventoryMessages(e.getInventoryMessages());
        } catch (ProductItemNotFoundException e) {
            addProductItemNotFoundMessage(e.getProductItem().getFullName());
        }

        addMaterial = false;
        showProductionOrders = true;
    }

    private String[] getGlossMessage(InventoryMovement inventoryMovement) {
        WarehouseVoucher warehouseVoucher = warehouseVoucherSelect;
        String gloss[] = new String[2];
        String dateString = DateUtils.format(warehouseVoucher.getDate(), MessageUtils.getMessage("patterns.date"));
        String productCodes = QueryUtils.toQueryParameter(movementDetailService.findDetailProductCodeByVoucher(warehouseVoucher));
        String documentName = warehouseVoucher.getDocumentType().getName();
        String sourceWarehouseName = warehouseVoucher.getWarehouse().getName();
        String movementDescription = inventoryMovement.getDescription();

        if (warehouseVoucher.isExecutorUnitTransfer()) {
            String targetWarehouseName = warehouseVoucher.getWarehouse().getName();
            gloss[0] = MessageUtils.getMessage("WarehouseVoucher.message.outTransferenceGloss", documentName, sourceWarehouseName, targetWarehouseName, productCodes, dateString, Constants.WAREHOUSEVOUCHER_NUMBER_PARAM, movementDescription);
            gloss[1] = MessageUtils.getMessage("WarehouseVoucher.message.inTransferenceGloss", documentName, sourceWarehouseName, targetWarehouseName, productCodes, dateString, Constants.WAREHOUSEVOUCHER_NUMBER_PARAM, movementDescription);
        } else {
            String voucherTypeName = messages.get(warehouseVoucher.getDocumentType().getWarehouseVoucherType().getResourceKey());
            gloss[0] = MessageUtils.getMessage("WarehouseVoucher.message.gloss", voucherTypeName, documentName, sourceWarehouseName, productCodes, dateString, Constants.WAREHOUSEVOUCHER_NUMBER_PARAM, movementDescription);
        }

        return gloss;

    }

    public String generateVoucherOrderProduction(){

        productionOrder.setEstateOrder(TABULATED);

        //productionOrder.setEstateOrder(INSTOCK);

        InventoryMovement inventoryMovement = createVale();
        approvalVoucher(inventoryMovement);

        closeDetail();
        showProductionOrders = true;
        showInit();
        update();
        return Outcome.SUCCESS;
    }

    private void addWarehouseVoucherApproveMessage() {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.INFO,
                "WarehouseVoucher.message.approved");
    }

    /**
     * Shows the warnings attribute according to the Maps and List mappings
     */
    public void showMovementDetailWarningMessages() {
        for (Map.Entry<MovementDetail, BigDecimal> movementDetailBigDecimalEntry : movementDetailUnderMinimalStockMap.entrySet()) {
            MovementDetail movementDetail = movementDetailBigDecimalEntry.getKey();
            // if under minimal Stock
            facesMessages.addFromResourceBundle(StatusMessage.Severity.WARN,
                    "MovementDetail.warning.underMinimalStock",
                    movementDetail.getMovementType().equals(MovementDetailType.E) ? messages.get("Common.math.sum") : messages.get("Common.math.subtraction"),
                    FormatUtils.formatNumber(movementDetailBigDecimalEntry.getValue(), messages.get("patterns.decimal6FNumber"), sessionUser.getLocale()),
                    FormatUtils.formatNumber(movementDetail.getQuantity(), messages.get("patterns.decimal6FNumber"), sessionUser.getLocale()),
                    movementDetail.getProductItem().getFullName(),
                    FormatUtils.formatNumber(movementDetail.getProductItem().getMinimalStock(), messages.get("patterns.decimal6FNumber"), sessionUser.getLocale()),
                    movementDetail.getWarehouse().getFullName());
        }
        for (Map.Entry<MovementDetail, BigDecimal> movementDetailBigDecimalEntry : movementDetailOverMaximumStockMap.entrySet()) {
            MovementDetail movementDetail = movementDetailBigDecimalEntry.getKey();
            // if over maximumStock
            facesMessages.addFromResourceBundle(StatusMessage.Severity.WARN,
                    "MovementDetail.warning.overMaximumStock",
                    movementDetail.getMovementType().equals(MovementDetailType.E) ? messages.get("Common.math.sum") : messages.get("Common.math.subtraction"),
                    FormatUtils.formatNumber(movementDetailBigDecimalEntry.getValue(), messages.get("patterns.decimal6FNumber"), sessionUser.getLocale()),
                    FormatUtils.formatNumber(movementDetail.getQuantity(), messages.get("patterns.decimal6FNumber"), sessionUser.getLocale()),
                    movementDetail.getProductItem().getFullName(),
                    FormatUtils.formatNumber(movementDetail.getProductItem().getMaximumStock(), messages.get("patterns.decimal6FNumber"), sessionUser.getLocale()),
                    movementDetail.getWarehouse().getFullName());
        }
    }

    public void approvalOnlyVoucher(){

        try {
            for (MovementDetail movementDetail : movementDetailSelect) {
                buildValidateQuantityMappings(movementDetail);
            }
            approvalWarehouseVoucherService.approveWarehouseVoucherOrderProduction(
                    warehouseVoucherSelect.getId(),
                    movementDetailUnderMinimalStockMap,
                    movementDetailOverMaximumStockMap,
                    movementDetailWithoutWarnings);
            //addWarehouseVoucherApproveMessage();
            showMovementDetailWarningMessages();
        } catch (InventoryException e) {
            addInventoryMessages(e.getInventoryMessages());
            return ;
        } catch (WarehouseVoucherApprovedException e) {
            addWarehouseVoucherApprovedMessage();
            return ;
        } catch (WarehouseVoucherEmptyException e) {
            addWarehouseVoucherEmptyException();
            return ;
        } catch (WarehouseVoucherNotFoundException e) {
            addNotFoundMessage();
            return ;
        } catch (ProductItemAmountException e) {
            addNotEnoughAmountMessage(e.getProductItem(), e.getAvailableAmount());
            return ;
        } catch (InventoryUnitaryBalanceException e) {
            addInventoryUnitaryBalanceErrorMessage(e.getAvailableUnitaryBalance(), e.getProductItem());
            return ;
        } catch (InventoryProductItemNotFoundException e) {
            addInventoryProductItemNotFoundErrorMessage(e.getExecutorUnitCode(),
                    e.getProductItem(), e.getWarehouse());
            return ;
        } catch (CompanyConfigurationNotFoundException e) {
            addCompanyConfigurationNotFoundErrorMessage();
            return ;
        } catch (FinancesExchangeRateNotFoundException e) {
            addFinancesExchangeRateNotFoundExceptionMessage();
            return ;
        } catch (FinancesCurrencyNotFoundException e) {
            addFinancesExchangeRateNotFoundExceptionMessage();
            return ;
        } catch (ConcurrencyException e) {
            addUpdateConcurrencyMessage();
            return ;
        } catch (ReferentialIntegrityException e) {
            addDeleteReferentialIntegrityMessage();
            return ;
        } catch (ProductItemNotFoundException e) {
            addProductItemNotFoundMessage(e.getProductItem().getFullName());
            return ;
        }
    }

    public String generateAccountToWarehouseVoucher(InventoryMovement inventoryMovement)
    { String numTrans = "";
        try {
            numTrans = approvalWarehouseVoucherService.crateAccountEntry(warehouseVoucherSelect,getGlossMessage(inventoryMovement),accountOrderProductions);
        } catch (CompanyConfigurationNotFoundException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (FinancesCurrencyNotFoundException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (FinancesExchangeRateNotFoundException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        return numTrans;
    }

    public void approvalVoucher(InventoryMovement inventoryMovement){

        try {
            for (MovementDetail movementDetail : movementDetailSelect) {
                buildValidateQuantityMappings(movementDetail);
            }
            approvalWarehouseVoucherService.approveWarehouseVoucherOrderProduction(
                    warehouseVoucherSelect.getId(),
                    getGlossMessage(inventoryMovement),
                    movementDetailUnderMinimalStockMap,
                    movementDetailOverMaximumStockMap,
                    movementDetailWithoutWarnings,
                    accountOrderProductions);
            //addWarehouseVoucherApproveMessage();
            showMovementDetailWarningMessages();
        } catch (InventoryException e) {
            addInventoryMessages(e.getInventoryMessages());
            return ;
        } catch (WarehouseVoucherApprovedException e) {
            addWarehouseVoucherApprovedMessage();
            return ;
        } catch (WarehouseVoucherEmptyException e) {
            addWarehouseVoucherEmptyException();
            return ;
        } catch (WarehouseVoucherNotFoundException e) {
            addNotFoundMessage();
            return ;
        } catch (ProductItemAmountException e) {
            addNotEnoughAmountMessage(e.getProductItem(), e.getAvailableAmount());
            return ;
        } catch (InventoryUnitaryBalanceException e) {
            addInventoryUnitaryBalanceErrorMessage(e.getAvailableUnitaryBalance(), e.getProductItem());
            return ;
        } catch (InventoryProductItemNotFoundException e) {
            addInventoryProductItemNotFoundErrorMessage(e.getExecutorUnitCode(),
                    e.getProductItem(), e.getWarehouse());
            return ;
        } catch (CompanyConfigurationNotFoundException e) {
            addCompanyConfigurationNotFoundErrorMessage();
            return ;
        } catch (FinancesExchangeRateNotFoundException e) {
            addFinancesExchangeRateNotFoundExceptionMessage();
            return ;
        } catch (FinancesCurrencyNotFoundException e) {
            addFinancesExchangeRateNotFoundExceptionMessage();
            return ;
        } catch (ConcurrencyException e) {
            addUpdateConcurrencyMessage();
            return ;
        } catch (ReferentialIntegrityException e) {
            addDeleteReferentialIntegrityMessage();
            return ;
        } catch (ProductItemNotFoundException e) {
            addProductItemNotFoundMessage(e.getProductItem().getFullName());
            return ;
        }
    }

    public void buildValidateQuantityMappings(MovementDetail movementDetail) throws ProductItemNotFoundException {
        BigDecimal requiredQuantity = movementDetail.getQuantity();
        if (null != requiredQuantity) {
            ProductItem productItem = null;
            try {
                productItem = getService().findById(ProductItem.class, movementDetail.getProductItem().getId(), true);
            } catch (EntryNotFoundException e) {
                throw new ProductItemNotFoundException(productItem);
            }
            Warehouse warehouse = movementDetail.getWarehouse();
            BigDecimal minimalStock = productItem.getMinimalStock();
            BigDecimal maximumStock = productItem.getMaximumStock();
            BigDecimal unitaryBalance = inventoryService.findUnitaryBalanceByProductItemAndArticle(warehouse.getId(), productItem.getId());
            BigDecimal totalQuantity = movementDetail.getMovementType().equals(MovementDetailType.E) ?
                    BigDecimalUtil.sum(unitaryBalance, requiredQuantity, SCALE) :
                    BigDecimalUtil.subtract(unitaryBalance, requiredQuantity, SCALE);
            // by default does not show warning until is verified
            boolean showWarning = false;

            if (null != minimalStock) {
                // minimalStock is not null
                int minimalComparison = totalQuantity.compareTo(minimalStock);
                if (minimalComparison < 0) {
                    // if under minimalStock
                    this.movementDetailUnderMinimalStockMap.put(movementDetail, unitaryBalance);
                    showWarning = true;
                }
            }
            if (null != maximumStock) {
                // maximumStock is not null
                int maximumComparison = totalQuantity.compareTo(maximumStock);
                if (maximumComparison > 0) {
                    // if over maximumStock
                    this.movementDetailOverMaximumStockMap.put(movementDetail, unitaryBalance);
                    showWarning = true;
                }
            }
            if (!showWarning) {
                movementDetailWithoutWarnings.add(movementDetail);
            }
        }
    }

    protected void addFinancesExchangeRateNotFoundExceptionMessage() {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                "PurchaseOrder.financesExchangeRateNotFound");
    }

    protected void addInventoryProductItemNotFoundErrorMessage(String executorUnitCode,
                                                               ProductItem productItem,
                                                               Warehouse warehouse) {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.WARN,
                "warehouseVoucher.approve.error.productItemNotFound",
                productItem.getName(),
                warehouse.getName(),
                executorUnitCode);
    }

    protected void addInventoryUnitaryBalanceErrorMessage(BigDecimal availableUnitaryBalance,
                                                          ProductItem productItem) {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                "warehouseVoucher.approve.error.notEnoughUnitaryBalance",
                productItem.getName(),
                availableUnitaryBalance);
    }

    public void addWarehouseVoucherApprovedMessage() {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                "WarehouseVoucher.error.approved");
    }

    protected void addWarehouseVoucherEmptyException() {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                "WarehouseVoucher.error.empty");
    }

    protected void addNotEnoughAmountMessage(ProductItem productItem,
                                             BigDecimal availableAmount) {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                "WarehouseVoucher.approve.error.notEnoughAmount",
                productItem.getName(),
                availableAmount);
    }

    public void createInventoryMovement(ProductionOrder order){
        warehouseVoucherSelect = approvalWarehouseVoucherService.findWarehouseVoucherByNumber(order.getNumberVoucher());
        InventoryMovement inventoryMovement = approvalWarehouseVoucherService.getMovement(warehouseVoucherSelect);
        List<MovementDetail> movementDetails = generateMovementDetailOrder(order);
        movementDetailSelect = movementDetails;
        accountOrderProductions = generateMovementDetailOrderProduction(order);

        String noTrans = generateAccountToWarehouseVoucher(inventoryMovement);
        Voucher voucher  = voucherService.findVoucherByNoTrans(noTrans);

        System.out.println("=====> ==> " + order.getCode() + " - " + noTrans + " - " + order.getNumberTransaction() + " - " + voucher.toString());
        order.setNumberTransaction(noTrans);
        order.setVoucherId(voucher.getId());
        //order.setVoucher(voucher);

        order.setEstateOrder(TABULATED);
        //this.update();
        //addMessageGenerateAccountingEntry(order.getCode());
    }

    public void createInventoryMovement(BaseProduct base){
        warehouseVoucherSelect = approvalWarehouseVoucherService.findWarehouseVoucherByNumber(base.getNumberVoucher());
        InventoryMovement inventoryMovement = approvalWarehouseVoucherService.getMovement(warehouseVoucherSelect);
        List<MovementDetail> movementDetails = generateMovementDetailOrder(base);
        movementDetailSelect = movementDetails;
        accountOrderProductions = generateMovementDetailOrderProduction(base);

        String noTrans = generateAccountToWarehouseVoucher(inventoryMovement);
        Voucher voucher  = voucherService.findVoucherByNoTrans(noTrans);

        base.setNumberTransaction(noTrans);
        base.setVoucherId(voucher.getId());
        base.setState(TABULATED);
        //addMessageGenerateAccountingEntry(base.getCode());
    }

    public InventoryMovement createVale(ProductionOrder order,Date dateConcurrent){

        WarehouseVoucher warehouseVoucher = new WarehouseVoucher();
        warehouseVoucher = createWarehouseVoucherOrder(dateConcurrent);
        warehouseVoucherSelect = warehouseVoucher;
        InventoryMovement inventoryMovement = createInventoryMovement(MessageUtils.getMessage("Warehousevoucher.gloss.productionOrder", order.getCode()));
        List<MovementDetail> movementDetails = generateMovementDetailOrder(order);
        movementDetailSelect = movementDetails;
        accountOrderProductions = generateMovementDetailOrderProduction(order);
        Map<MovementDetail, BigDecimal> movementDetailUnderMinimalStockMap = new HashMap<MovementDetail, BigDecimal>();
        Map<MovementDetail, BigDecimal> movementDetailOverMaximumStockMap = new HashMap<MovementDetail, BigDecimal>();
        List<MovementDetail> movementDetailWithoutWarnings = new ArrayList<MovementDetail>();
        try {
            warehouseService.saveWarehouseVoucher(warehouseVoucher, inventoryMovement, movementDetails,
                    movementDetailUnderMinimalStockMap,
                    movementDetailOverMaximumStockMap,
                    movementDetailWithoutWarnings);
            //addMessageCreateWarehouseVoucherInput();
        } catch (InventoryException e) {
            addInventoryMessages(e.getInventoryMessages());
            return null;
        } catch (ProductItemNotFoundException e) {
            addProductItemNotFoundMessage(e.getProductItem().getFullName());
            return null;
        }
        return inventoryMovement;
    }

    public InventoryMovement createVale(BaseProduct base,Date dateConcurrent){

        WarehouseVoucher warehouseVoucher = createWarehouseVoucherOrder(dateConcurrent);
        warehouseVoucherSelect = warehouseVoucher;
        InventoryMovement inventoryMovement = createInventoryMovement(MessageUtils.getMessage("Warehousevoucher.gloss.productionOrder", base.getCode()));
        List<MovementDetail> movementDetails = generateMovementDetailOrder(base);
        movementDetailSelect = movementDetails;
        accountOrderProductions = generateMovementDetailOrderProduction(base);
        Map<MovementDetail, BigDecimal> movementDetailUnderMinimalStockMap = new HashMap<MovementDetail, BigDecimal>();
        Map<MovementDetail, BigDecimal> movementDetailOverMaximumStockMap = new HashMap<MovementDetail, BigDecimal>();
        List<MovementDetail> movementDetailWithoutWarnings = new ArrayList<MovementDetail>();
        try {
            warehouseService.saveWarehouseVoucher(warehouseVoucher, inventoryMovement, movementDetails,
                    movementDetailUnderMinimalStockMap,
                    movementDetailOverMaximumStockMap,
                    movementDetailWithoutWarnings);
            addMessageCreateWarehouseVoucherInput();
        } catch (InventoryException e) {
            addInventoryMessages(e.getInventoryMessages());
            return null;
        } catch (ProductItemNotFoundException e) {
            addProductItemNotFoundMessage(e.getProductItem().getFullName());
            return null;
        }
        return inventoryMovement;
    }

    public InventoryMovement createOnlyVale(BaseProduct base,Date dateConcurrent){

        WarehouseVoucher warehouseVoucher = createWarehouseVoucherOrder(dateConcurrent);
        warehouseVoucherSelect = warehouseVoucher;
        InventoryMovement inventoryMovement = createInventoryMovement(MessageUtils.getMessage("Warehousevoucher.gloss.productionOrder", base.getCode()));
        List<MovementDetail> movementDetails = generateOnlyMovementDetailOrder(base);
        movementDetailSelect = movementDetails;
        accountOrderProductions = generateMovementDetailOrderProduction(base);
        Map<MovementDetail, BigDecimal> movementDetailUnderMinimalStockMap = new HashMap<MovementDetail, BigDecimal>();
        Map<MovementDetail, BigDecimal> movementDetailOverMaximumStockMap = new HashMap<MovementDetail, BigDecimal>();
        List<MovementDetail> movementDetailWithoutWarnings = new ArrayList<MovementDetail>();
        try {
            warehouseService.saveWarehouseVoucher(warehouseVoucher, inventoryMovement, movementDetails,
                    movementDetailUnderMinimalStockMap,
                    movementDetailOverMaximumStockMap,
                    movementDetailWithoutWarnings);
            addMessageCreateWarehouseVoucherInput();
        } catch (InventoryException e) {
            addInventoryMessages(e.getInventoryMessages());
            return null;
        } catch (ProductItemNotFoundException e) {
            addProductItemNotFoundMessage(e.getProductItem().getFullName());
            return null;
        }
        return inventoryMovement;
    }

    public InventoryMovement createVale(){

        WarehouseVoucher warehouseVoucher = new WarehouseVoucher();
        warehouseVoucher = createWarehouseVoucherOrder(getInstance().getDate());
        warehouseVoucherSelect = warehouseVoucher;
        InventoryMovement inventoryMovement = createInventoryMovement(MessageUtils.getMessage("Warehousevoucher.gloss.productionOrder", productionOrder.getCode()));
        List<MovementDetail> movementDetails = generateMovementDetailOrder(productionOrder);
        movementDetailSelect = movementDetails;
        accountOrderProductions = generateMovementDetailOrderProduction(productionOrder);
        Map<MovementDetail, BigDecimal> movementDetailUnderMinimalStockMap = new HashMap<MovementDetail, BigDecimal>();
        Map<MovementDetail, BigDecimal> movementDetailOverMaximumStockMap = new HashMap<MovementDetail, BigDecimal>();
        List<MovementDetail> movementDetailWithoutWarnings = new ArrayList<MovementDetail>();
        try {
            warehouseService.saveWarehouseVoucher(warehouseVoucher, inventoryMovement, movementDetails,
                    movementDetailUnderMinimalStockMap,
                    movementDetailOverMaximumStockMap,
                    movementDetailWithoutWarnings);
            addMessageCreateWarehouseVoucherInput();
        } catch (InventoryException e) {
            addInventoryMessages(e.getInventoryMessages());
            return null;
        } catch (ProductItemNotFoundException e) {
            addProductItemNotFoundMessage(e.getProductItem().getFullName());
            return null;
        }
        return inventoryMovement;
    }

    private List<AccountOrderProduction> generateMovementDetailOrderProduction(ProductionOrder order) {
        List<AccountOrderProduction> accountOrderProductions = new ArrayList<AccountOrderProduction>();
        accountOrderProductions.addAll(generateAccountOrderProductionsMaterial(order.getOrderMaterials()));
        accountOrderProductions.addAll(generateAccountOrderProductionsInput(order.getOrderInputs()));
        accountOrderProductions.addAll(generateAccountOrderProductionsIndirectCost(order.getIndirectCostses()));
        return accountOrderProductions;
    }

    private List<AccountOrderProduction> generateMovementDetailOrderProduction(BaseProduct base) {
        List<AccountOrderProduction> accountOrderProductions = new ArrayList<AccountOrderProduction>();
        accountOrderProductions.addAll(generateAccountOrderProductionsInput(base.getOrderInputs()));

        for(SingleProduct single:base.getSingleProducts()){
            accountOrderProductions.addAll(generateAccountOrderProductionsMaterial(single.getOrderMaterials()));
            accountOrderProductions.addAll(generateAccountOrderProductionsIndirectCost(single.getIndirectCostses()));
        }

        return accountOrderProductions;
    }

    private List<AccountOrderProduction> generateAccountOrderProductionsIndirectCost(List<IndirectCosts> indirectCostses) {
        List<AccountOrderProduction> accountOrderProductions = new ArrayList<AccountOrderProduction>();
        for(IndirectCosts costs: indirectCostses)
        {
            AccountOrderProduction accountOrderProduction = new AccountOrderProduction();
            accountOrderProduction.setCashAccount(costs.getCostsConifg().getCashAccount());
            accountOrderProduction.setCostCenterCode(warehouseVoucherSelect.getCostCenterCode());
            accountOrderProduction.setExecutorUnit(warehouseVoucherSelect.getExecutorUnit());
            accountOrderProduction.setVoucherAmount(costs.getAmountBs());
            accountOrderProductions.add(accountOrderProduction);
        }

        return accountOrderProductions;
    }

    private List<AccountOrderProduction> generateAccountOrderProductionsMaterial(List<OrderMaterial> materials) {
        List<AccountOrderProduction> accountOrderProductions = new ArrayList<AccountOrderProduction>();
        for(OrderMaterial material: materials)
        {
            AccountOrderProduction accountOrderProduction = new AccountOrderProduction();
            accountOrderProduction.setCashAccount(material.getProductItem().getCashAccount());
            accountOrderProduction.setCostCenterCode(warehouseVoucherSelect.getCostCenterCode());
            accountOrderProduction.setExecutorUnit(warehouseVoucherSelect.getExecutorUnit());
            accountOrderProduction.setVoucherAmount(material.getCostTotal());
            accountOrderProductions.add(accountOrderProduction);
        }

        return accountOrderProductions;
    }

    private List<AccountOrderProduction> generateAccountOrderProductionsInput(List<OrderInput> inputs) {
        List<AccountOrderProduction> accountOrderProductions = new ArrayList<AccountOrderProduction>();
        for(OrderInput orderInput: inputs)
        {
            AccountOrderProduction accountOrderProduction = new AccountOrderProduction();
            accountOrderProduction.setCashAccount(orderInput.getProductItem().getCashAccount());
            accountOrderProduction.setCostCenterCode(warehouseVoucherSelect.getCostCenterCode());
            accountOrderProduction.setExecutorUnit(warehouseVoucherSelect.getExecutorUnit());
            accountOrderProduction.setVoucherAmount(orderInput.getCostTotal());
            accountOrderProductions.add(accountOrderProduction);
        }

        return accountOrderProductions;
    }

    private List<MovementDetail> generateMovementDetailOrder(ProductionOrder productionOrder) {

        List<MovementDetail> movementDetails = new ArrayList<MovementDetail>();

        MovementDetail detail = new MovementDetail();
        ProductItem item = productionOrder.getProductComposition().getProcessedProduct().getProductItem();
        detail.setProductItemCode(item.getProductItemCode());
        detail.setQuantity(new BigDecimal(productionOrder.getProducedAmount()));
        detail.setProductItemAccount(item.getProductItemAccount());
        detail.setMeasureCode(item.getUsageMeasureUnit().getMeasureUnitCode());

        detail.setUnitPurchasePrice(productionOrder.getUnitCost());
        detail.setUnitCost(BigDecimal.valueOf(productionOrder.getUnitCost().doubleValue() * 0.87));

        detail.setPurchasePrice(new BigDecimal(productionOrder.getTotalCostProduction()));
        detail.setAmount(new BigDecimal(productionOrder.getTotalCostProduction() * 0.87));

        detail.setProductItem(item);
        detail.setWarehouse(warehouseService.findWarehouseByCode("2"));
        detail.setMeasureUnit(item.getUsageMeasureUnit());
        movementDetails.add(detail);

        return movementDetails;
    }

    private List<MovementDetail> generateOnlyMovementDetailOrder(BaseProduct base) {

        List<MovementDetail> movementDetails = new ArrayList<MovementDetail>();
        for(SingleProduct single:base.getSingleProducts()){
            single.setState(INSTOCK);
            movementDetails.add(createMovementDetail(single));
            //productionPlanningService.updateSingleProduct(single);
        }

        return movementDetails;
    }

    private List<MovementDetail> generateMovementDetailOrder(BaseProduct base) {

        List<MovementDetail> movementDetails = new ArrayList<MovementDetail>();
        for(SingleProduct single:base.getSingleProducts()){
            single.setState(TABULATED);
            movementDetails.add(createMovementDetail(single));
        }

        return movementDetails;
    }

    private MovementDetail createMovementDetail(SingleProduct singleProduct)
    {
        MovementDetail detail = new MovementDetail();
        ProductItem item = singleProduct.getProductProcessingSingle().getMetaProduct().getProductItem();
        detail.setProductItemCode(item.getProductItemCode());
        detail.setQuantity(new BigDecimal(singleProduct.getAmount()));
        detail.setProductItemAccount(item.getProductItemAccount());
        detail.setMeasureCode(item.getUsageMeasureUnit().getMeasureUnitCode());

        detail.setUnitPurchasePrice(singleProduct.getUnitCost());
        detail.setUnitCost(BigDecimal.valueOf(singleProduct.getUnitCost().doubleValue() * 0.87));

        detail.setPurchasePrice(singleProduct.getTotalCostProduction());
        detail.setAmount(BigDecimal.valueOf(singleProduct.getTotalCostProduction().doubleValue() * 0.87));

        detail.setProductItem(item);
        detail.setWarehouse(warehouseService.findWarehouseByCode("2"));
        detail.setMeasureUnit(item.getUsageMeasureUnit());

        return detail;
    }

    public void addProductItemsToFormulation(List<ProductItem> productItems) {
        Boolean aux = false;
        List<OrderInput> itemList = new ArrayList<OrderInput>();
        for (ProductItem item : productItems) {
            for(OrderInput orderInput :productionOrder.getOrderInputs())
            {
                if(orderInput.getProductItem().getProductItemCode().compareTo(item.getProductItemCode()) == 0)
                {
                    aux = true;
                    continue;
                }
            }
            if(aux){
                  addMessageDuplicateInput(item.getFullName());
            }
            else {
                OrderInput input = new OrderInput();
                input.setProductItem(item);
                input.setProductionOrder(productionOrder);
                input.setAmount(0.0);
                input.setType("ADD");
                input.setAmountStock(evaluatorMathematicalExpressionsService.getMountInWarehouse(item));
                input.setProductItemCode(item.getProductItemCode());
                input.setCompanyNumber(item.getCompanyNumber());
                input.setCostUnit(item.getUnitCost());
                itemList.add(input);
                productionOrder.getOrderInputs().add(input);
            }
        }
        productIputToFormulation.clear();
        productIputToFormulation.addAll(itemList);
    }

    private void addMessageDuplicateInput(String fullName) {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.WARN,"ProductionOrder.productionInput.messageDuplicateInput",fullName);
    }

    public void removeInputForFormulation(OrderInput orderInput) {
        productionOrder.getOrderInputs().remove(orderInput);
        productIputToFormulation.remove(orderInput);
        productionPlanningService.deleteOrderInput(orderInput);
    }

    public class AccountOrderProduction{

        private BusinessUnit executorUnit;
        private String costCenterCode;
        private CashAccount cashAccount;
        private BigDecimal voucherAmount;

        public BusinessUnit getExecutorUnit() {
            return executorUnit;
        }

        public void setExecutorUnit(BusinessUnit executorUnit) {
            this.executorUnit = executorUnit;
        }

        public String getCostCenterCode() {
            return costCenterCode;
        }

        public void setCostCenterCode(String costCenterCode) {
            this.costCenterCode = costCenterCode;
        }

        public CashAccount getCashAccount() {
            return cashAccount;
        }

        public void setCashAccount(CashAccount cashAccount) {
            this.cashAccount = cashAccount;
        }

        public BigDecimal getVoucherAmount() {
            return voucherAmount;
        }

        public void setVoucherAmount(BigDecimal voucherAmount) {
            this.voucherAmount = voucherAmount;
        }
    }

    //TODO: Revisar el almacen de productos
    private List<MovementDetail> generateMovementDetailMaterial(List<OrderMaterial> orderMaterials) {
        List<MovementDetail> movementDetails = new ArrayList<MovementDetail>();

        for(OrderMaterial material: orderMaterials)
        {
            if(!articleEstateService.verifyEstate(material.getProductItem(),Constants.ESTATE_ARTICLE_NOTVERIFY))
            {
                MovementDetail detail = new MovementDetail();
                detail.setProductItemCode(material.getProductItemCode());
                detail.setQuantity(new BigDecimal(material.getAmountUsed()));
                detail.setProductItemAccount(material.getProductItem().getProductItemAccount());
                detail.setMeasureCode(material.getProductItem().getUsageMeasureUnit().getMeasureUnitCode());
                detail.setUnitCost(material.getCostUnit());
                detail.setAmount(material.getCostTotal());
                detail.setProductItem(material.getProductItem());
                //detail.setWarehouse(inventoryService.findWarehouseByItemArticle(material.getProductItem()));
                detail.setWarehouse(warehouseService.findWarehouseByCode("2"));
                detail.setMeasureUnit(material.getProductItem().getUsageMeasureUnit());
                movementDetails.add(detail);
            }

        }

        return movementDetails;
    }

    public void generateValeInput(){

        WarehouseVoucher warehouseVoucher = new WarehouseVoucher();
        warehouseVoucher = createWarehouseVoucherInput();
        InventoryMovement inventoryMovement = createInventoryMovement("egreso de insumos de prueba");

        /*lista de insumos*/
        List<MovementDetail> movementDetails = generateMovementDetailInput(productionOrder.getOrderInputs());
        /*...*/
        Map<MovementDetail, BigDecimal> movementDetailUnderMinimalStockMap = new HashMap<MovementDetail, BigDecimal>();
        Map<MovementDetail, BigDecimal> movementDetailOverMaximumStockMap = new HashMap<MovementDetail, BigDecimal>();
        List<MovementDetail> movementDetailWithoutWarnings = new ArrayList<MovementDetail>();

        try {
            warehouseService.saveWarehouseVoucher(warehouseVoucher, inventoryMovement, movementDetails,
                    movementDetailUnderMinimalStockMap,
                    movementDetailOverMaximumStockMap,
                    movementDetailWithoutWarnings);
            addMessageCreateWarehouseVoucherInput();
        } catch (InventoryException e) {
            addInventoryMessages(e.getInventoryMessages());
            return;
        } catch (ProductItemNotFoundException e) {
            addProductItemNotFoundMessage(e.getProductItem().getFullName());
            return;
        }

        clearFormulation();
        disableEditingFormula();
        showProductionOrders = true;

    }

    public void addProductItemNotFoundMessage(String productItemName) {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.WARN,
                "ProductItem.error.notFound", productItemName);
    }
    public void addInventoryMessages(List<InventoryMessage> messages) {
        for (InventoryMessage message : messages) {
            if (message.isNotFound()) {
                facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                        "WarehouseVoucher.error.InventoryNotFound", message.getProductItem().getName());
                continue;
            }

            if (message.isNotEnough()) {
                facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                        "WarehouseVoucher.error.InventoryUnavailableProductItems", message.getProductItem().getName(),
                        message.getAvailableQuantity());
            }
        }
    }

    private List<MovementDetail> generateMovementDetailInput(List<OrderInput> orderInputs) {
        List<MovementDetail> movementDetails = new ArrayList<MovementDetail>();
        for(OrderInput input: orderInputs)
        {
            if(!articleEstateService.verifyEstate(input.getProductItem(),Constants.ESTATE_ARTICLE_NOTVERIFY))
            {   /*if(!articleEstateService.verifyEstate(input.getProductItem(),Constants.ESTATE_ARTICLE_COMPOSITE))
                { */
                    MovementDetail detail = new MovementDetail();
                    detail.setProductItemCode(input.getProductItemCode());
                    detail.setQuantity(new BigDecimal(input.getAmount()));
                    detail.setProductItemAccount(input.getProductItem().getProductItemAccount());
                    detail.setMeasureCode(input.getProductItem().getUsageMeasureUnit().getMeasureUnitCode());
                    detail.setUnitCost(input.getCostUnit());
                    detail.setAmount(input.getCostTotal());
                    detail.setProductItem(input.getProductItem());
                    detail.setWarehouse(warehouseService.findWarehouseByCode("2"));
                    detail.setMeasureUnit(input.getProductItem().getUsageMeasureUnit());
                    movementDetails.add(detail);

                /*}else{
                    ProductComposition composition = processedProductService.getProductComposite(ingredient.getMetaProduct().getId());
                    movementDetails.addAll(generateMovementDetailInput(productionPlanningService. input.));
                } */
            }

        }

        return movementDetails;
    }

    private Double calculateCostTotal(List<ProductionIngredient> ingredients)
    {
        Double costTotal = 0.0;
        for(ProductionIngredient ingredient : ingredients){
            costTotal += ingredient.getAmount() * ingredient.getMetaProduct().getProductItem().getUnitCost().doubleValue();
        }

        return costTotal;
    }

    //@End
    public String removeFormulation() {
    //public void removeFormulation() {
        ProductionPlanning productionPlanning = getInstance();
        //setPriceCostInput();
        String result = Outcome.FAIL;
        for (ProductionOrder po : productionPlanning.getProductionOrderList()) {
            if (po.getCode().equals(productionOrder.getCode())) {
                productionPlanning.getProductionOrderList().remove(po);
                result = deleteOrder(po);
                clearFormulation();
                break;
            }
        }
        for (ProductionOrder order : productionPlanning.getProductionOrderList()) {
            setTotalsMaterials(order);
            setTotalsInputs(order);
            setTotalIndiRectCost(order,productionPlanning.getDate());
            //setTotalHour(productionOrder);
            setTotalCostProducticionAndUnitPrice(order);
        }

        for(BaseProduct base:getInstance().getBaseProducts()){
            for (SingleProduct single : base.getSingleProducts()) {
                setTotalsMaterials(single);
                setTotalsInputs(base,single);
                setTotalIndiRectCost(single,productionPlanning.getDate());
                //setTotalHour(productionOrder);
                setTotalCostProducticionAndUnitPrice(single);
            }
        }
        setValuesMilks();
        disableEditingFormula();
        showProductionOrders = true;
        showInit();
        return Outcome.SUCCESS;
    }

    public String deleteOrder(ProductionOrder order) {
        try {
            getService().delete(order);
            addDeletedMessage();
        } catch (ConcurrencyException e) {
            entryNotFoundLog();
            addDeleteConcurrencyMessage();
        } catch (ReferentialIntegrityException e) {
            referentialIntegrityLog();
            addDeleteReferentialIntegrityMessage();
        }

        return Outcome.SUCCESS;
    }

    public void selectProcessedProduct(ProcessedProduct processedProduct) {
        dispobleBalance = true;
        try {
            this.processedProduct = processedProductService.find(processedProduct.getId());
            this.productComposition = new ProductComposition();
        } catch (Exception ex) {
            log.error(ex);
            facesMessages.addFromResourceBundle(ERROR, "Common.globalError.description");
        }
    }

    public void selectSingleProduct(ProcessedProduct processedProduct) {

        try {
            if(singleProduct.getProductProcessingSingle() == null)
            {
                ProductProcessingSingle processingSingle = new ProductProcessingSingle();
                processingSingle.setMetaProduct(processedProductService.find(processedProduct.getId()));
                singleProduct.setProductProcessingSingle(processingSingle);
                processingSingle.setSingleProduct(singleProduct);
            }else{
                singleProduct.getProductProcessingSingle().setMetaProduct(processedProductService.find(processedProduct.getId()));
            }


        } catch (Exception ex) {
            log.error(ex);
            facesMessages.addFromResourceBundle(ERROR, "Common.globalError.description");
        }

    }

    public void selectResultProcessedProduct(ProcessedProduct processedProduct) {
        try {
            processedProduct = getService().findById(ProcessedProduct.class, processedProduct.getId());

            OutputProductionVoucher outputProductionVoucher = new OutputProductionVoucher();
            outputProductionVoucher.setProcessedProduct(processedProduct);
            //outputProductionVoucher.setProducedAmount(0.0);
            outputProductionVoucher.setProductionOrder(productionOrder);
            productionOrder.getOutputProductionVoucherList().add(outputProductionVoucher);
        } catch (Exception ex) {
            log.error(ex);
            facesMessages.addFromResourceBundle(ERROR, "Common.globalError.description");
        }
    }

    public void selectModifiIput(ProductionOrder productionOrder) {

        cancelFormulation();
        //dispobleBalance = false;

        this.productionOrder = productionOrder;
        if(productionOrder.getProductMain() != null)
        {
            this.processedProduct = productionOrder.getProductOrders().get(0).getProcessedProduct();
        }else {
            existingFormulation = new Formulation();
            existingFormulation.producingAmount = productionOrder.getExpendAmount();
            existingFormulation.productComposition = productionOrder.getProductComposition();
            this.productComposition = productionOrder.getProductComposition();
            this.processedProduct = productComposition.getProcessedProduct();
            evaluateMathematicalExpression();
            try {
                agregarInsumosDeProductosCompuestos();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ProductCompositionException e) {
                e.printStackTrace();
            }
        }

        formulaState = FormulaState.EDIT;
        showProductionOrders = false;
        showButtonAddInput = true;
        hideButtonGeneral();
        hideTablesIni();

    }

    public void selectMaterial(ProductionOrder order) {

        //cancelFormulation();
        disableEditingFormula();
        showDetailOrder = false;
        productionOrder = order;
        this.productionOrderMaterial = productionOrder;
        this.productComposition = productionOrder.getProductComposition();
        if(order.getProductMain() != null)
            this.processedProduct = order.getProductOrders().get(0).getProcessedProduct();
        else
            this.processedProduct = productComposition.getProcessedProduct();
        /*
        this.productComposition = productionOrder.getProductComposition();
        this.processedProduct = productComposition.getProcessedProduct();

        if(orderMaterials.size() == 0)
        orderMaterials = order.getOrderMaterials();*/

        orderMaterials = new ArrayList<OrderMaterial>();
        orderMaterials.addAll(order.getOrderMaterials());

        addMaterial = true;
        showProductionOrders = false;
        hideButtonGeneral();
        hideTablesIni();
    }

    public void editSingle(BaseProduct base, SingleProduct single){
        baseProduct = base;
        singleProduct = single;
        orderSingleMaterial = single.getOrderMaterials();
        showSingleProduct = true;
        hideButtonGeneral();
        hideTablesIni();
    }

    public void selectMaterialDetail(ProductionOrder order) {
        disableEditingFormula();
        productionOrderMaterial = order;
        productionOrder = order;
        orderMaterials = new ArrayList<OrderMaterial>();
        orderMaterials.addAll(order.getOrderMaterials());

        showMaterialDetail = true;
        showProductionOrders = false;
    }

    public void selectDetail(ProductionOrder order) {
        cancelFormulation();
        addMaterial = false;
        //productionOrderMaterial = order;
        productionOrder = order;
        if (productionOrder.getId() != null && order.getEstateOrder().equals(PENDING)) {
            setTotalsMaterials(productionOrder);
            //setTotalsInputs(productionOrder);
            //setTotalHour(productionOrder);
            setTotalIndiRectCost(productionOrder,getInstance().getDate());
            setTotalCostProducticionAndUnitPrice(productionOrder);
        }
        //orderMaterials = new ArrayList<OrderMaterial>();
        //orderMaterials.addAll(order.getOrderMaterials());
        showDetailOrder = true;
        showProductionOrders = false;
        hideButtonGeneral();
        hideTablesIni();
    }

    public void makeExecutedOrder() {
        productionOrder.setEstateOrder(EXECUTED);
        ProductionPlanning productionPlanning = getInstance();
        for (ProductionOrder order : productionPlanning.getProductionOrderList()) {
            setTotalsMaterials(order);
            //setTotalsInputs(order);
            setTotalIndiRectCost(order,productionPlanning.getDate());
            //setTotalHour(productionOrder);
            setTotalCostProducticionAndUnitPrice(order);
        }

        for(BaseProduct base:getInstance().getBaseProducts()){
            for (SingleProduct single : base.getSingleProducts()) {
                setTotalsMaterials(single);
                setTotalsInputs(base,single);
                setTotalIndiRectCost(single,productionPlanning.getDate());
                //setTotalHour(productionOrder);
                setTotalCostProducticionAndUnitPrice(single);
            }
        }
        setValuesMilks();
        String outcome = update();

        if (outcome != Outcome.SUCCESS) {
            productionOrder.setEstateOrder(PENDING);
        }
        disableEditingFormula();
        showDetailOrder = false;
        showProductionOrders = true;
        showInit();
        refreshInstance();
    }

    public void setValuesMilks()
    {
        getInstance().setTotalMilk(calculateTotalMilk());
        getInstance().setTotalMilkCheese(calculateTotalMilkCheese());
        getInstance().setTotalMilkUHT(calculateTotalMilkUHT());
        getInstance().setTotalMilkYogurt(calculateTotalMilkYogurt());
        getInstance().setTotalMilkReprocessed(calculateTotalMilkReprocessed());
    }

    public void makeExecutedOrderSingle() {
        singleProduct.setState(EXECUTED);
        ProductionPlanning productionPlanning = getInstance();
        for (ProductionOrder order : productionPlanning.getProductionOrderList()) {
            setTotalsMaterials(order);
            setTotalsInputs(order);
            setTotalIndiRectCost(order,productionPlanning.getDate());
            setTotalCostProducticionAndUnitPrice(order);
        }
        for(BaseProduct base:productionPlanning.getBaseProducts()){
            for (SingleProduct single : base.getSingleProducts()) {
                setTotalsMaterials(single);
                setTotalsInputs(base,single);
                setTotalIndiRectCost(single,productionPlanning.getDate());
                //setTotalHour(productionOrder);
                setTotalCostProducticionAndUnitPrice(single);
            }
        }
        setValuesMilks();
        String outcome = update();

        if (outcome != Outcome.SUCCESS) {
            productionOrder.setEstateOrder(PENDING);
        }

        showDetailSingleProduct = false;
        showInit();
        refreshInstance();
    }

    public void makeFinalizedOrderSingle() {
        if(verifyAmounts(singleProduct))
        {
            return;
        }
        if(!hasIndirectCost())
        {
            notHasIndirectCostMessage();
            return;
        }
        singleProduct.setState(FINALIZED);
        ProductionPlanning productionPlanning = getInstance();
        for (ProductionOrder order : productionPlanning.getProductionOrderList()) {
            setTotalsMaterials(order);
            setTotalsInputs(order);
            setTotalIndiRectCost(order,productionPlanning.getDate());
            setTotalCostProducticionAndUnitPrice(order);
        }
        for(BaseProduct base:productionPlanning.getBaseProducts()){
            for (SingleProduct single : base.getSingleProducts()) {
                setTotalsMaterials(single);
                setTotalsInputs(base,single);
                setTotalIndiRectCost(single,productionPlanning.getDate());
                //setTotalHour(productionOrder);
                setTotalCostProducticionAndUnitPrice(single);
            }
        }
        setValuesMilks();
        String outcome = update();

        if (outcome != Outcome.SUCCESS) {
            productionOrder.setEstateOrder(EXECUTED);
        }

        //productInventoryService.inputProducts(singleProduct.getProductProcessingSingle().getMetaProduct().getProductItemCode(), singleProduct.getAmount().doubleValue());
        showDetailSingleProduct = false;
        showInit();
        refreshInstance();
    }

    private boolean verifyAmounts(ProductionOrder order)
    {
        Boolean result = false;
        if(order.getProducedAmount() == 0)
        {
            addMessageCantProducerCero();
            result = true;
        }
        for(OrderMaterial material:order.getOrderMaterials())
        {
            if(material.getAmountUsed() == 0)
            {
                addMessageCantCeroMaterial();
                result = true;
            }
        }
        return result;
    }

    private boolean verifyAmounts(SingleProduct single)
    {
        Boolean result = false;
        if(single.getAmount() == 0)
        {
            addMessageCantProducerCero();
            result = true;
        }
        for(OrderMaterial material:single.getOrderMaterials())
        {
            if(material.getAmountUsed() == 0)
            {
                addMessageCantCeroMaterial();
                result = true;
            }
        }
        return result;
    }

    public void makeFinalizedOrder() {

        if(verifyAmounts(productionOrder))
        {
            return;
        }
        if(!hasIndirectCost())
        {
            notHasIndirectCostMessage();
            return;
        }
        productionOrder.setEstateOrder(FINALIZED);
        ProductionPlanning productionPlanning = getInstance();

        if(productionOrder.getProductMain() != null )
        {
            if(productionOrder.getTotalCostInputMain() != null)
            {
                if(productionOrder.getTotalCostInputMain() > 0.0)
                   volumeTotalInputMain = totalVolumeInputs();
            }
            else
            volumeTotalInputMain = 0.0;
        }


        for (ProductionOrder order : productionPlanning.getProductionOrderList()) {
            setTotalsMaterials(order);
            setTotalsInputs(order);
            setTotalIndiRectCost(order,productionPlanning.getDate());
            //setTotalHour(productionOrder);
            setTotalCostProducticionAndUnitPrice(order);
        }

        for(BaseProduct base:getInstance().getBaseProducts()){
            for (SingleProduct single : base.getSingleProducts()) {
                setTotalsMaterials(single);
                setTotalsInputs(base,single);
                setTotalIndiRectCost(single,productionPlanning.getDate());
                //setTotalHour(productionOrder);
                setTotalCostProducticionAndUnitPrice(single);
            }
        }
        setValuesMilks();
        String outcome = update();

        if (outcome != Outcome.SUCCESS) {
            productionOrder.setEstateOrder(EXECUTED);
            //getInstance().setState(EXECUTED);
        }
        showProductionOrders = true;
        disableEditingFormula();
        //productionOrder = null;
        showDetailOrder = false;
        showInit();
        refreshInstance();

        String productItemCode = productionOrder.getProductComposition().getProcessedProduct().getProductItemCode();
        Double quantity = productionOrder.getProducedAmount();
        ProductInventory productInventory = productInventoryService.findProductInventoryByCode(productItemCode);

        //productInventoryService.inputProducts(productItemCode, quantity);  //DESCOMENTAR

    }

    public void makeFinalizedOrder(ProductionPlanning planning) {

        if(!hasIndirectCost())
        {
            notHasIndirectCostMessage();
            return;
        }

        for (ProductionOrder order : planning.getProductionOrderList()) {
            if(order.getEstateOrder().equals(EXECUTED)){
            setTotalsMaterials(order);
            setTotalsInputs(order);
            setTotalIndiRectCost(order,planning.getDate());
            //setTotalHour(productionOrder);
            setTotalCostProducticionAndUnitPrice(order);
            order.setEstateOrder(FINALIZED);
            }
        }

        for(BaseProduct base:planning.getBaseProducts()){
            if(base.getState().equals(EXECUTED))
            {
                for (SingleProduct single : base.getSingleProducts()) {
                    setTotalsMaterials(single);
                    setTotalsInputs(base,single);
                    setTotalIndiRectCost(single,planning.getDate());
                    //setTotalHour(productionOrder);
                    setTotalCostProducticionAndUnitPrice(single);
                }

            base.setState(FINALIZED);
            }
        }
        //todo:verificar que todos las ordenes de produccion terminen correctamente
        setValuesMilks();
        try {
            getService().update(planning);
        } catch (EntryDuplicatedException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (ConcurrencyException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

    }

    //public void deleteSingleProduct(SingleProduct singleProduct)
    public String deleteSingleProduct()
    {
        for(BaseProduct base:getInstance().getBaseProducts()){
            base.getSingleProducts().remove(singleProduct);
        }

        /*for (ProductionOrder order : getInstance().getProductionOrderList()) {
                setTotalsMaterials(order);
                setTotalsInputs(order);
                setTotalIndiRectCost(order,getInstance().getDate());
                //setTotalHour(productionOrder);
                setTotalCostProducticionAndUnitPrice(order);
        }

        for(BaseProduct base:getInstance().getBaseProducts()){

                for (SingleProduct single : base.getSingleProducts()) {
                    setTotalsMaterials(single);
                    setTotalsInputs(base,single);
                    setTotalIndiRectCost(single,getInstance().getDate());
                    //setTotalHour(productionOrder);
                    setTotalCostProducticionAndUnitPrice(single);
                }

        }
        setValuesMilks();*/
        try {
            getService().update(getInstance());
        } catch (EntryDuplicatedException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (ConcurrencyException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        return Outcome.SUCCESS;
    }

    private void notHasIndirectCostMessage() {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,"ProductionPlanning.error.notHasIndirectCostMessage");
    }

    public boolean hasIndirectCost()
    {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(getInstance().getDate());
        int monthConcurrent = calendar.get(Calendar.MONTH);
        //todo: muy importate solo para regularizar enero,febrero, marzo se tomara el mes actual en adelante se tomara el mes anterios (-1 para que tome el mes anterior)
        //todo: el mes comienza en 0 hasta el 11 que es diciembre
        calendar.set(Calendar.DAY_OF_MONTH,1);
        calendar.set(Calendar.MONTH,monthConcurrent -1);
        PeriodIndirectCost periodIndirectCost = indirectCostsService.getConcurrentPeroidIndirectCost(calendar.getTime());

        return periodIndirectCost != null;
    }

    @End
    public String update(ProductionOrder order) {
        Long currentVersion = (Long) getVersion(getInstance());
        try {
            getInstance().setTotalMilk(calculateTotalMilk());
            getInstance().setTotalMilkCheese(calculateTotalMilkCheese());
            getInstance().setTotalMilkUHT(calculateTotalMilkUHT());
            getInstance().setTotalMilkYogurt(calculateTotalMilkYogurt());
            getInstance().setTotalMilkReprocessed(calculateTotalMilkReprocessed());
            productionPlanningService.updateProductionPlanning(getInstance(),order);
        } catch (EntryDuplicatedException e) {
            addDuplicatedMessage();
            setVersion(getInstance(), currentVersion);
            return Outcome.REDISPLAY;
        } catch (ConcurrencyException e) {
            concurrencyLog();
            try {
                setInstance(getService().findById(getEntityClass(), getId(getInstance()), true));
            } catch (EntryNotFoundException e1) {
                entryNotFoundLog();
                addNotFoundMessage();
                return Outcome.FAIL;
            }
            addUpdateConcurrencyMessage();
            return Outcome.REDISPLAY;
        }
        addUpdatedMessage();
        return Outcome.SUCCESS;
    }

    public void closeDetail() {
        disableEditingFormula();
        //productionOrder = null;
        showDetailOrder = false;
        showProductionOrders = true;
        showInit();
    }

    public void closeSingleDetail() {

        showDetailSingleProduct = false;
        showInit();
    }

    public void showInit()
    {
        showListReprocessedProduct = true;
        showProductionOrders = true;
        showButtonReprocessed = true;
        showGenerateAllVoucher = true;
        showGenerateAllAccountEntries = true;
        showButtonAddProduct = true;
        showSingleProduct = false;
        showGenerateRequestByPlanning = true;
        hasMainProduction = false;
        volumeTotalInputMain = 0.0;
        showMainProduct = true;

        disableEditingFormula();
    }

    public void addProductItems(List<ProductItem> productItems) {

        if (selectedProductItems.size() == 0 && orderMaterials.size() > 0)
            for (OrderMaterial material : orderMaterials) {
                selectedProductItems.add(material.getProductItem().getId());
            }

        for (ProductItem productItem : productItems) {
            if (selectedProductItems.contains(productItem.getId())) {
                continue;
            }

            selectedProductItems.add(productItem.getId());

            OrderMaterial material = new OrderMaterial();
            material.setProductItem(productItem);
            material.setProductionOrder(productionOrder);
            material.setCompanyNumber(productItem.getCompanyNumber());
            orderMaterials.add(material);
        }
    }

    public void addBaseProductItems(List<ProductItem> productItems) {

        if (selectedBaseProductItems.size() == 0 && orderBaseInputs.size() > 0)
            for (OrderInput input: orderBaseInputs) {
                selectedBaseProductItems.add(input.getProductItem().getId());
            }

        for (ProductItem productItem : productItems) {
            if (selectedBaseProductItems.contains(productItem.getId())) {
                continue;
            }

            selectedBaseProductItems.add(productItem.getId());

            OrderInput input = new OrderInput();
            input.setProductItem(productItem);
            input.setBaseProductInput(baseProduct);
            input.setCompanyNumber(productItem.getCompanyNumber());
            orderBaseInputs.add(input);
        }
    }

    public void addProductToBaseProduct(List<ProductItem> productItems) {

        if (selectedProductToBaseproduct.size() == 0)
            for (ProductProcessing productProcessing: productProcessings) {
                selectedProductToBaseproduct.add(productProcessing.getMetaProduct().getProductItem().getId());
            }

        for (ProductItem productItem : productItems) {
            if (selectedProductToBaseproduct.contains(productItem.getId())) {
                continue;
            }

            selectedProductToBaseproduct.add(productItem.getId());
            ProductProcessing processing = new ProductProcessing();
            processing.setBaseProduct(baseProduct);
            processing.setMetaProduct(metaProductService.find(productItem));
            productProcessings.add(processing);
        }
    }


    public void addSingleProductMaterial(List<ProductItem> productItems) {

        if (selectedSingleProductMaterial.size() == 0 && orderSingleMaterial.size() > 0)
            for (OrderMaterial material: orderSingleMaterial) {
                selectedSingleProductMaterial.add(material.getProductItem().getId());
            }

        for (ProductItem productItem : productItems) {
            if (selectedSingleProductMaterial.contains(productItem.getId())) {
                continue;
            }

            selectedSingleProductMaterial.add(productItem.getId());

            OrderMaterial material = new OrderMaterial();
            material.setProductItem(productItem);
            material.setSingleProduct(singleProduct);
            material.setCompanyNumber(productItem.getCompanyNumber());
            orderSingleMaterial.add(material);

        }
    }

    public void addOrderProduced() {
        Double totalMaterial = 0.0;
        if(productionOrder.getEstateOrder() == EXECUTED)
        {
            for (OrderMaterial material : orderMaterials) {
                if (material.getAmountUsed() > material.getAmountRequired()) {
                    addMessageMaterialCantUsedMajorRequerided();
                    return;
                }
            }
        }
        for (OrderMaterial material : orderMaterials) {
            if (material.getAmountUsed() > 0) {
                Double amountReturn = material.getAmountRequired() - material.getAmountUsed();
                //el precio unitario sin redondear
                Double total = material.getAmountUsed() * ((BigDecimal) material.getProductItem().getUnitCost()).doubleValue();
                material.setAmountReturned(RoundUtil.getRoundValue(amountReturn, 2, RoundUtil.RoundMode.SYMMETRIC));
                material.setCostUnit(material.getProductItem().getUnitCost());
                material.setCostTotal(new BigDecimal(RoundUtil.getRoundValue(total,2, RoundUtil.RoundMode.SYMMETRIC)));
                totalMaterial += total;

            }
        }
        productionOrder.setTotalPriceMaterial(totalMaterial);
        ProductionPlanning productionPlanning = getInstance();
        int position = productionPlanning.getProductionOrderList().indexOf(productionOrder);
        productionPlanning.getProductionOrderList().get(position).getOrderMaterials().clear();
        productionPlanning.getProductionOrderList().get(position).setOrderMaterials(orderMaterials);


        addMaterial = false;
        if (productionPlanning.getId() != null) {
            setTotalsMaterials(productionOrder);
            setTotalsInputs(productionOrder);
            //setTotalIndiRectCost(productionOrder);
            //setTotalHour(productionOrder);
            setTotalCostProducticionAndUnitPrice(productionOrder);
            setValuesMilks();
            if (update() != Outcome.SUCCESS) {
                return;
            }

        }
        orderMaterials.clear();
        selectedProductItems.clear();
        showProductionOrders = true;
        showInit();
    }

    public void removeProductProcessing(ProductProcessing processing)
    {
        selectedProductToBaseproduct.remove(processing.getMetaProduct().getProductItem().getId());
        productProcessings.remove(processing);
    }

    public void removeMaterialProduct(OrderMaterial instance) {
        selectedProductItems.remove(instance.getProductItem().getId());
        orderMaterials.remove(instance);
    }

    public void removeMaterial(OrderMaterial instance) {
        selectedSingleProductMaterial.remove(instance.getProductItem().getId());
        orderSingleMaterial.remove(instance);
    }

    public void removeInput(OrderInput instance) {
        selectedBaseProductItems.remove(instance.getProductItem().getId());
        orderBaseInputs.remove(instance);
    }

    public void clearFormulation() {
        processedProduct = new ProcessedProduct();
        productComposition = new ProductComposition();
        productionOrder = new ProductionOrder();
    }


    private Boolean verifySotckByProductionPlannig(ProductionPlanning planning) {
        Boolean band = true;
        for (ProductionOrder order : planning.getProductionOrderList()) {
            band = verifySotck(order);
        }
        return band;
    }

    public void updateProducedAmount() {
        /*Double totalProducer = 0.0;
        for(OutputProductionVoucher outputProductionVoucher:productionOrder.getOutputProductionVoucherList())
        {
            totalProducer += outputProductionVoucher.getProducedAmount();
        }
        productionOrder.setExpendAmount(totalProducer);*/
        //setTotalsMaterials(productionOrder);
        //setTotalsInputs(productionOrder);
        //setTotalIndiRectCost(productionOrder);
        //setTotalHour(productionOrder);
        setTotalCostProducticionAndUnitPrice(productionOrder);
        getInstance().setTotalMilk(calculateTotalMilk());
        getInstance().setTotalMilkCheese(calculateTotalMilkCheese());
        getInstance().setTotalMilkUHT(calculateTotalMilkUHT());
        getInstance().setTotalMilkYogurt(calculateTotalMilkYogurt());
        getInstance().setTotalMilkReprocessed(calculateTotalMilkReprocessed());

        if (update() != Outcome.SUCCESS) {
            return;
        }
        existingFormulation = null;
        showInit();
    }

    private void setTotalIndiRectCost(BaseProduct base, Date date) {
        for(SingleProduct single:base.getSingleProducts()){
            setTotalIndiRectCost(single,date);
        }
    }

    public void cancelFormulation() {
        if (existingFormulation != null) {
            productionOrder.setProductComposition(existingFormulation.productComposition);
            productionOrder.setExpendAmount(existingFormulation.producingAmount);
            existingFormulation = null;
        }
        disableEditingFormula();

        dispobleBalance = true;
        addMaterial = false;
        showInputDetail = false;
        showMaterialDetail = false;
        showDetailOrder = false;
        showProductionOrders = true;
        showListReprocessedProduct = false;
        showButtonReprocessed = false;
        showGenerateAllVoucher = false;
        showGenerateAllAccountEntries = false;
        showButtonAddProduct = false;
        showButtonAddInput = false;
        hasMainProduction = false;
        showInit();
    }

    private void disableEditingFormula() {
        formulaState = FormulaState.NONE;
    }
    //todo: isNotCountAs verifica si el insumo tiene cuenta
    public void saveBaseProduct(){
        ProductionPlanning productionPlanning = getInstance();
        Double totalInput= 0.0;
        if(baseProduct.getId() == null)
        {
            productionPlanning.getBaseProducts().add(baseProduct);
            baseProduct.setProductionPlanningBase(productionPlanning);
        }

        if(baseProduct.getCode() == null)
        {
            baseProduct.setCode(codeGenerate);
            baseProduct.getProductProcessings().addAll(productProcessings);
            baseProduct.getOrderInputs().addAll(orderBaseInputs);
        }

        for (OrderInput input : baseProduct.getOrderInputs()) {
            if (!isNotCountAs(input.getProductItem()))
            {
                input.setCostTotal(new BigDecimal(RoundUtil.getRoundValue( input.getAmount() * input.getProductItem().getUnitCost().doubleValue(),2, RoundUtil.RoundMode.SYMMETRIC)));
                input.setCostUnit(new BigDecimal(input.getProductItem().getUnitCost().doubleValue()));
                totalInput = totalInput + input.getCostTotal().doubleValue();
            }
        }
        baseProduct.setTotalInput(new BigDecimal(totalInput));
        getInstance().setTotalMilk(calculateTotalMilk());
        getInstance().setTotalMilkCheese(calculateTotalMilkCheese());
        getInstance().setTotalMilkUHT(calculateTotalMilkUHT());
        getInstance().setTotalMilkYogurt(calculateTotalMilkYogurt());
        getInstance().setTotalMilkReprocessed(calculateTotalMilkReprocessed());
            if (update() != Outcome.SUCCESS) {
                return;
            }
        showReprocessedProduct = false;
        baseProduct = null;
        orderBaseInputs.clear();
        productProcessings.clear();
        refreshInstance();
        showInit();
    }

    public void deleteReprocessedProduct(){
        ProductionPlanning productionPlanning = getInstance();
        productionPlanning.getBaseProducts().remove(baseProduct);

        getInstance().setTotalMilk(calculateTotalMilk());
        getInstance().setTotalMilkCheese(calculateTotalMilkCheese());
        getInstance().setTotalMilkUHT(calculateTotalMilkUHT());
        getInstance().setTotalMilkYogurt(calculateTotalMilkYogurt());
        getInstance().setTotalMilkReprocessed(calculateTotalMilkReprocessed());
        if (update() != Outcome.SUCCESS) {
            return;
        }
        showReprocessedProduct = false;
        baseProduct = null;
        orderBaseInputs.clear();
        productProcessings.clear();
        refreshInstance();
        showInit();
    }

    private BigDecimal calculateTotalMilkCheese(){
        ProductionPlanning planning = getInstance();
        Double total = 0.0;
        ProductItem item;
        for(ProductionOrder order: planning.getProductionOrderList()){
            if(order.getProductMain() != null)
                item = order.getProductOrders().get(0).getProcessedProduct().getProductItem();
            else
                item = order.getProductComposition().getProcessedProduct().getProductItem();

            if(item.getGroupCode().compareTo(Constants.ID_ART_GROUP_CHEESE) == 0)
            for(OrderInput input: order.getOrderInputs())
            {
                if(input.getProductItem().getProductItemCode().compareTo(Constants.ID_ART_RAW_MILK) == 0)
                {
                    total += input.getAmount();
                }
            }
        }
        total = RoundUtil.getRoundValue(total,2, RoundUtil.RoundMode.SYMMETRIC);
        return new BigDecimal(total);
    }

    private BigDecimal calculateTotalMilkUHT(){
        ProductionPlanning planning = getInstance();
        Double total = 0.0;
        ProductItem item;
        for(ProductionOrder order: planning.getProductionOrderList()){

            if(order.getProductMain() != null)
                item = order.getProductOrders().get(0).getProcessedProduct().getProductItem();
            else
                item = order.getProductComposition().getProcessedProduct().getProductItem();

            if(item.getGroupCode().compareTo(Constants.ID_ART_GROUP_UHT) == 0)
            for(OrderInput input: order.getOrderInputs())
            {
                if(input.getProductItem().getProductItemCode().compareTo(Constants.ID_ART_RAW_MILK) == 0)
                {
                        total += input.getAmount();
                }
            }
        }
        total = RoundUtil.getRoundValue(total,2, RoundUtil.RoundMode.SYMMETRIC);
        return new BigDecimal(total);
    }

    private BigDecimal calculateTotalMilkYogurt(){
        ProductionPlanning planning = getInstance();
        Double total = 0.0;
        ProductItem item;
        for(ProductionOrder order: planning.getProductionOrderList()){
            if(order.getProductMain() != null)
                item = order.getProductOrders().get(0).getProcessedProduct().getProductItem();
            else
                item = order.getProductComposition().getProcessedProduct().getProductItem();

            if(item.getGroupCode().compareTo(Constants.ID_ART_GROUP_YOGURT) == 0)
            for(OrderInput input: order.getOrderInputs())
            {
                if(input.getProductItem().getProductItemCode().compareTo(Constants.ID_ART_RAW_MILK) == 0)
                {
                        total += input.getAmount();
                }
            }
        }
        total = RoundUtil.getRoundValue(total,2, RoundUtil.RoundMode.SYMMETRIC);
        return new BigDecimal(total);
    }


    private BigDecimal calculateTotalMilk(){
        ProductionPlanning planning = getInstance();
        Double total = 0.0;
        for(ProductionOrder order: planning.getProductionOrderList()){
            for(OrderInput input: order.getOrderInputs())
            {
                if(input.getProductItemCode().compareTo(Constants.ID_ART_RAW_MILK) == 0)
                    total += input.getAmount();
            }
        }

        for(BaseProduct base:planning.getBaseProducts())
        {
            for(OrderInput input:base.getOrderInputs())
            {
                if(input.getProductItem().getProductItemCode().compareTo(Constants.ID_ART_RAW_MILK) == 0)
                    total += input.getAmount();
            }

        }
        total = RoundUtil.getRoundValue(total,2, RoundUtil.RoundMode.SYMMETRIC);
        return new BigDecimal(total);
    }

    private BigDecimal calculateTotalMilkReprocessed(){
        ProductionPlanning planning = getInstance();
        Double total = 0.0;

        for(BaseProduct base:planning.getBaseProducts())
        {
            for(OrderInput input:base.getOrderInputs())
            {
                if(input.getProductItem().getProductItemCode().compareTo(Constants.ID_ART_RAW_MILK) == 0)
                    total += input.getAmount();
            }

        }
        total = RoundUtil.getRoundValue(total,2, RoundUtil.RoundMode.SYMMETRIC);
        return new BigDecimal(total);
    }

    public void saveSingleProduct(){

        if(singleProduct.getId() == null)
        {
            baseProduct.getSingleProducts().add(singleProduct);
            singleProduct.setBaseProduct(baseProduct);
            singleProduct.getOrderMaterials().addAll(orderSingleMaterial);
        }

        for (OrderMaterial material : singleProduct.getOrderMaterials()) {
            if (material.getAmountUsed() > 0) {
                Double amountReturn = material.getAmountRequired() - material.getAmountUsed();
                Double total = material.getAmountUsed() * ((BigDecimal) material.getProductItem().getUnitCost()).doubleValue();
                material.setAmountReturned(RoundUtil.getRoundValue(amountReturn, 2, RoundUtil.RoundMode.SYMMETRIC));
                material.setCostUnit(material.getProductItem().getUnitCost());
                material.setCostTotal(new BigDecimal(RoundUtil.getRoundValue(total,2, RoundUtil.RoundMode.SYMMETRIC)));

            }
        }

        if (update() != Outcome.SUCCESS) {
            return;
        }
        showSingleProduct = false;
        selectedSingleProductMaterial.clear();
        if(singleProduct.getId() != null)
        refreshInstance();
        showInit();
    }

    public void addProduct(BaseProduct product)
    {
        baseProduct = product;
        singleProduct = new SingleProduct();
        showSingleProduct = true;
        orderSingleMaterial = new ArrayList<OrderMaterial>();
        hideButtonGeneral();
        hideTablesIni();
    }

    @End(ifOutcome = Outcome.SUCCESS)
    public String makeExecuted() {
        productionOrder.setEstateOrder(EXECUTED);
        ProductionPlanning productionPlanning = getInstance();
        for (ProductionOrder productionOrder : productionPlanning.getProductionOrderList()) {
            setTotalsMaterials(productionOrder);
            setTotalsInputs(productionOrder);
            //setTotalHour(productionOrder);
            setTotalIndiRectCost(productionOrder,productionPlanning.getDate());
            setTotalCostProducticionAndUnitPrice(productionOrder);
        }
        setValuesMilks();
        String outcome = update();

        if (outcome != Outcome.SUCCESS) {
            productionOrder.setEstateOrder(PENDING);
        }
        refreshInstance();
        return outcome;
    }

    @End(ifOutcome = Outcome.SUCCESS)
    public String makeFinalized() {

        productionOrder.setEstateOrder(FINALIZED);
        ProductionPlanning productionPlanning = getInstance();
        for (ProductionOrder productionOrder : productionPlanning.getProductionOrderList()) {
            setTotalsMaterials(productionOrder);
            setTotalsInputs(productionOrder);
            //setTotalHour(productionOrder);
            setTotalIndiRectCost(productionOrder,productionPlanning.getDate());
            setTotalCostProducticionAndUnitPrice(productionOrder);
        }
        setValuesMilks();
        String outcome = update();

        if (outcome != Outcome.SUCCESS) {
            productionOrder.setEstateOrder(EXECUTED);
        }
        return outcome;
    }
    //todo: metodo fuera de servicio
    public void setTotalHour(ProductionOrder productionOrder) {
        productionOrder.setTotalPriceJourney(((BigDecimal) (employeeTimeCardService.getCostProductionOrder(productionOrder, getInstance().getDate(), getTotalVolumProductionPlaning(productionOrder)))).doubleValue());
    }

    public void setTotalCostProducticionAndUnitPrice(ProductionOrder productionOrder) {
        Double total = productionOrder.getTotalPriceMaterial() + productionOrder.getTotalPriceInput() + productionOrder.getTotalPriceJourney() + productionOrder.getTotalIndirectCosts();
        productionOrder.setTotalCostProduction(total);
        Double priceUnit = 0.0;
        if (productionOrder.getProducedAmount() > 0.0)
            priceUnit = total / productionOrder.getProducedAmount();
        //Todo: Revisar
        productionOrder.setUnitCost(new BigDecimal(priceUnit));
    }

    public void setTotalCostProducticionAndUnitPrice(SingleProduct singleProduct) {
        Double total = singleProduct.getTotalMaterial().doubleValue() + singleProduct.getTotalInput().doubleValue() + singleProduct.getCostLabor().doubleValue() + singleProduct.getTotalIndirecCost().doubleValue();
        singleProduct.setTotalCostProduction(new BigDecimal(total));
        Double priceUnit = 0.0;
        if (singleProduct.getAmount() > 0.0)
            priceUnit = total / singleProduct.getAmount();
        //Todo: Revisar
        singleProduct.setUnitCost(new BigDecimal(priceUnit));
    }

    public void setTotalIndiRectCost(ProductionOrder productionOrder, Date dateConcurrent) {

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(dateConcurrent);
        int monthConcurrent = calendar.get(Calendar.MONTH);
        //todo: muy importate solo para regularizar enero,febrero, marzo se tomara el mes actual en adelante se tomara el mes anterios (-1 para que tome el mes anterior)
        //todo: el mes comienza en 0 hasta el 11 que es diciembre
        calendar.set(Calendar.DAY_OF_MONTH,1);
//        calendar.set(Calendar.MONTH,monthConcurrent -1);
        calendar.set(Calendar.MONTH, monthConcurrent);
        PeriodIndirectCost periodIndirectCost = indirectCostsService.getConcurrentPeroidIndirectCost(calendar.getTime());
        int totalDaysNotProducer = indirectCostsService.calculateCantDaysProducer(dateConcurrent);

        List<IndirectCosts> list = indirectCostsService.getCostTotalIndirect(
                dateConcurrent,
                totalDaysNotProducer,
                productionOrder,
                getTotalVolumProductionPlaning(productionOrder),
                getTotalVolumGeneralProductionPlaning(),
                periodIndirectCost);
        if(list.size() > 0)
        {
            productionPlanningService.deleteIndirectCost(productionOrder);
            for(IndirectCosts costs: list)
            {
                costs.setProductionOrder(productionOrder);
            }

            productionOrder.setIndirectCostses(list);
        }

        setTotalIndirectCosts(productionOrder);
    }

    public void setTotalIndiRectCost(SingleProduct single,Date dateConcurrent) {

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(dateConcurrent);
        int monthConcurrent = calendar.get(Calendar.MONTH);
        //todo: muy importate solo para regularizar enero,febrero, marzo se tomara el mes actual en adelante se tomara el mes anterios (-1 para que tome el mes anterior)
        //todo: el mes comienza en 0 hasta el 11 que es diciembre
        //calendar.set(Calendar.MONTH,monthConcurrent -1);
        calendar.set(Calendar.MONTH,monthConcurrent);
        PeriodIndirectCost periodIndirectCost = indirectCostsService.getConcurrentPeroidIndirectCost(calendar.getTime());
        int totalDaysNotProducer = indirectCostsService.calculateCantDaysProducer(dateConcurrent);
        List<IndirectCosts> list = indirectCostsService.getCostTotalIndirectSingle(
                dateConcurrent,
                totalDaysNotProducer,
                single,
                getTotalVolumProductionPlaning(single),
                getTotalVolumGeneralProductionPlaning(),
                periodIndirectCost);
        if(list.size() > 0)
        {
            List<IndirectCosts> indirectCostFromSingle =productionPlanningService.findIndirectCostFromSingle(single);
            single.getIndirectCostses().removeAll(indirectCostFromSingle);
            for(IndirectCosts costs: list)
            {
                costs.setSingleProduct(single);
            }
            single.setIndirectCostses(list);

        }
        setTotalIndirectCosts(list,single);
    }

    //todo: la mano de obra directa se tomara diretamente de la tabla costosindirectos para no cambiar mucho
    //se fijara directamente desde eseta tabla temporalmente
    private void setTotalIndirectCosts(ProductionOrder productionOrder) {
        Double total = 0.0;
        productionOrder.setTotalPriceJourney(0.0);
        Double total_labor = 0.0;
        for(IndirectCosts costs :productionOrder.getIndirectCostses())
        {
            if(costs.getCostsConifg().getEstate() != null)
            {if(costs.getCostsConifg().getEstate().compareTo(Constants.ESTATE_COSTCONFIG) == 0)
            {
                total_labor = total_labor + costs.getAmountBs().doubleValue();
            }
            else
                total = total + costs.getAmountBs().doubleValue();
            }
            else
                total = total + costs.getAmountBs().doubleValue();
        }
        productionOrder.setTotalIndirectCosts(total);
        productionOrder.setTotalPriceJourney(total_labor);
    }

    private void setTotalIndirectCosts(List<IndirectCosts> indirectCosts, SingleProduct single) {
        Double total = 0.0;
        Double total_labor = 0.0;
        for(IndirectCosts costs :indirectCosts)
        {
            if(costs.getCostsConifg().getEstate() != null)
            {if(costs.getCostsConifg().getEstate().compareTo(Constants.ESTATE_COSTCONFIG) == 0)
                {
                    total_labor = total_labor + costs.getAmountBs().doubleValue();
                }
             else
                total = total + costs.getAmountBs().doubleValue();
            }
            else
                total = total + costs.getAmountBs().doubleValue();
        }
        single.setTotalIndirecCost(new BigDecimal(total));
        single.setCostLabor(new BigDecimal(total_labor));
    }

    private void setTotalIndirectCosts(SingleProduct single) {
        Double total = 0.0;
        single.setCostLabor(new BigDecimal(0.0));
        for(IndirectCosts costs :single.getIndirectCostses())
        {
            if(costs.getCostsConifg().getEstate() != null)
            {if(costs.getCostsConifg().getEstate().compareTo(Constants.ESTATE_COSTCONFIG) == 0)
                single.setCostLabor(new BigDecimal(single.getCostLabor().doubleValue() + costs.getAmountBs().doubleValue()));
            }
            else
                total = total + costs.getAmountBs().doubleValue();
        }
        single.setTotalIndirecCost(new BigDecimal(total));
    }

    //todo: isNotCountAs verifica si el insumo no tiene cuenta en ese caso lo salta
    public void setTotalsInputsIni(ProductionOrder productionOrder) {
        Double totalInput = 0.0;

        for (OrderInput input : productionOrder.getOrderInputs()) {
            //totalInput += RoundUtil.getRoundValue((input.getProductItem().getUnitCost().doubleValue()) * input.getAmount(),2, RoundUtil.RoundMode.SYMMETRIC);
            if (!isNotCountAs(input.getProductItem()))
                //totalInput = totalInput + ((input.getProductItem().getUnitCost().doubleValue()) * input.getAmount());
            //totalInput = totalInput + ((input.getCostUnit().doubleValue()) * input.getAmount());
            totalInput = totalInput + input.getCostTotal().doubleValue();
        }

        //productionOrder.setTotalPriceInput(RoundUtil.getRoundValue(totalInput, 2, RoundUtil.RoundMode.SYMMETRIC));
        productionOrder.setTotalPriceInput(totalInput);
    }

    public void setTotalsInputs(ProductionOrder productionOrder) {

        Double totalInput = 0.0;

        for (OrderInput input : productionOrder.getOrderInputs()) {
            //totalInput += RoundUtil.getRoundValue((input.getProductItem().getUnitCost().doubleValue()) * input.getAmount(),2, RoundUtil.RoundMode.SYMMETRIC);
            if (!isNotCountAs(input.getProductItem()))
                //totalInput = totalInput + ((input.getProductItem().getUnitCost().doubleValue()) * input.getAmount());
                //totalInput = totalInput + ((input.getCostUnit().doubleValue()) * input.getAmount());
                totalInput = totalInput + input.getCostTotal().doubleValue();
        }

        //productionOrder.setTotalPriceInput(RoundUtil.getRoundValue(totalInput, 2, RoundUtil.RoundMode.SYMMETRIC));
        if(productionOrder.getProductMain() != null) {
            if(volumeTotalInputMain == 0.0)
                return;

            updateCostInputs(volumeTotalInputMain, productionOrder);
            totalInput += productionOrder.getTotalCostInputMain();
        }
        if(productionOrder.getProductMain() != null)
        if(productionOrder.getProductMain() == null && productionOrder.getTotalCostInputMain() > 0.0) {
            if(volumeTotalInputMain == 0.0)
                return;

            updateCostInputs(volumeTotalInputMain, productionOrder);
            return;
        }

        productionOrder.setTotalPriceInput(totalInput);
    }

    public void setTotalsInputs(BaseProduct base, SingleProduct single) {
        Double porcentage = 0.0;
        Double totalProducerInput = 0.0;
        for(SingleProduct product: base.getSingleProducts()){
            totalProducerInput = totalProducerInput + product.getAmount();
        }
        porcentage = getProcentage(totalProducerInput,single.getAmount().doubleValue());
        Double totalInput = base.getTotalInput().doubleValue() * porcentage;
        single.setTotalInput(new BigDecimal(totalInput));
    }

    public double getProcentage(Double total,Double totalSingle){
        Double porcentage = 0.0;
        if(total != 0.0)
        porcentage = (totalSingle * 100)/total;

        return porcentage /100;
    }

    public void setTotalsMaterials(ProductionOrder productionOrder) {
        Double totalMaterial = 0.0;
        for (OrderMaterial material : productionOrder.getOrderMaterials()) {
            totalMaterial += material.getCostTotal().doubleValue();
        }
        //productionOrder.setTotalPriceMaterial(RoundUtil.getRoundValue(totalMaterial, 2, RoundUtil.RoundMode.SYMMETRIC));
        productionOrder.setTotalPriceMaterial(totalMaterial);
    }

    public void setTotalsMaterials(SingleProduct single) {
        Double totalMaterial = 0.0;
        for (OrderMaterial material : single.getOrderMaterials()) {
            totalMaterial += material.getCostTotal().doubleValue();
        }
        //productionOrder.setTotalPriceMaterial(RoundUtil.getRoundValue(totalMaterial, 2, RoundUtil.RoundMode.SYMMETRIC));
        single.setTotalMaterial(new BigDecimal(totalMaterial));
    }

    public ProductComposition getProductComposition() {
        return productComposition;
    }

    public void setProductComposition(ProductComposition productComposition) {
        this.productComposition = productComposition;
    }

    public void showDetailSingle(SingleProduct product,BaseProduct base)
    {
        singleProduct = product;
        baseProduct = base;
        showDetailSingleProduct = true;
        if(product.getAmount() == 0.0)
        {
            addMessageCantProducerCero();
        }
        hideButtonGeneral();
        hideTablesIni();
    }

    public void cancelDetailSingle()
    {
        showDetailSingleProduct = false;
        showInit();
    }

    public List<ProductComposition> getProductCompositionList() {
        List<ProductComposition> productCompositionList = new ArrayList<ProductComposition>();
        if (processedProduct.getProductCompositionList().size() == 0) {
            return productCompositionList;
        }

        for (ProductComposition pc : processedProduct.getProductCompositionList()) {
            if (Boolean.TRUE.equals(pc.getActive())) {
                productCompositionList.add(pc);
            }
        }
        return productCompositionList;
    }

    public void removeOutputProductionVoucher(OutputProductionVoucher outputProductionVoucher) {
        OutputProductionVoucher outputForRemove = null;

        for (OutputProductionVoucher output : productionOrder.getOutputProductionVoucherList()) {
            if (output.getId().equals(outputProductionVoucher.getId())) {
                outputForRemove = output;
                break;
            }
        }

        if (outputForRemove != null) {
            productionOrder.getOutputProductionVoucherList().remove(outputForRemove);
        }
    }

    public Double getTotalVolumGeneralProductionPlaning() {
        Double total = 0.0;
        for (ProductionOrder order : getInstance().getProductionOrderList()) {
            total += employeeTimeCardService.getTotalVolumeOrder(order);
        }

        for(BaseProduct base:getInstance().getBaseProducts())
        for(SingleProduct product: base.getSingleProducts())
        {
            total += employeeTimeCardService.getTotalVolumeSingle(product);
        }

        return total;
    }

    public Double getTotalVolumProductionPlaning(ProductionOrder productionOrder) {
        Double total = 0.0;
        ProductItem item,itemOrder;
        if(productionOrder.getProductMain() == null)
            item = productionOrder.getProductComposition().getProcessedProduct().getProductItem();
        else
            item = productionOrder.getProductOrders().get(0).getProcessedProduct().getProductItem();

        for (ProductionOrder order : getInstance().getProductionOrderList()) {
            if(order.getProductMain() == null)
                itemOrder = order.getProductComposition().getProcessedProduct().getProductItem();
            else
                itemOrder = order.getProductOrders().get(0).getProcessedProduct().getProductItem();

            if (item.getSubGroup().getGroup() == itemOrder.getSubGroup().getGroup())
                total += employeeTimeCardService.getTotalVolumeOrder(order);
        }
        return total;
    }

    public Double getTotalVolumProductionPlaning(SingleProduct product) {
        Double total = 0.0;
        for (ProductionOrder order : getInstance().getProductionOrderList()) {
            if (product.getProductProcessingSingle().getMetaProduct().getProductItem().getSubGroup().getGroup() == order.getProductComposition().getProcessedProduct().getProductItem().getSubGroup().getGroup())
                total += employeeTimeCardService.getTotalVolumeOrder(order);
        }
        return total;
    }

    public Double getSumTotalVolume(MetaProduct metaProduct)
    {
        Double total = 0.0;
        for (ProductionOrder order : getInstance().getProductionOrderList()) {
            if (metaProduct.getProductItem().getSubGroup().getGroup() == order.getProductComposition().getProcessedProduct().getProductItem().getSubGroup().getGroup())
                total += employeeTimeCardService.getTotalVolumeOrder(order);
        }

        for (SingleProduct single : baseProduct.getSingleProducts()) {
            if (metaProduct.getProductItem().getSubGroup().getGroup() == single.getProductProcessingSingle().getMetaProduct().getProductItem().getSubGroup().getGroup())
                total += employeeTimeCardService.getTotalVolumeSingle(single);
        }

        return total;
    }

    public static class Consolidated {
        private double amount;
        private MetaProduct product;
        private Long idMeta;
        private String name;
        private String code;
        private String unit;
        private BigDecimal amountWarehouse;
        private Boolean isVerifiably;

        public Consolidated(double amount, MetaProduct product) {
            this.amount = amount;
            this.product = product;
        }

        public Consolidated() {
        }

        public double getAmount() {
            return amount;
        }

        public void setAmount(double amount) {
            this.amount = amount;
        }

        public MetaProduct getProduct() {
            return product;
        }

        public void setProduct(MetaProduct product) {
            this.product = product;
        }

        public Long getIdMeta() {
            return idMeta;
        }

        public void setIdMeta(Long idMeta) {
            this.idMeta = idMeta;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public String getUnit() {
            return unit;
        }

        public void setUnit(String unit) {
            this.unit = unit;
        }

        public BigDecimal getAmountWarehouse() {
            return amountWarehouse;
        }

        public void setAmountWarehouse(BigDecimal amountWarehouse) {
            this.amountWarehouse = amountWarehouse;
        }

        public Boolean getVerifiably() {
            return isVerifiably;
        }

        public void setVerifiably(Boolean verifiably) {
            isVerifiably = verifiably;
        }
    }

    private static class Formulation {
        public ProductComposition productComposition;
        public Double producingAmount;
    }

    public static enum FormulaState {
        NONE, NEW, EDIT
    }

    public ProductionOrder getProductionOrder() {
        return productionOrder;
    }

    public void setProductionOrder(ProductionOrder productionOrder) {
        this.productionOrder = productionOrder;
    }

    public Boolean getDispobleBalance() {
        return dispobleBalance;
    }

    public void setDispobleBalance(Boolean dispobleBalance) {
        this.dispobleBalance = dispobleBalance;
    }

    public Boolean getAddMaterial() {
        return addMaterial;
    }

    public void setAddMaterial(Boolean addMaterial) {
        this.addMaterial = addMaterial;
    }

    public void cancelMaterial() {
        selectedProductItems.clear();
        orderMaterials.clear();
        //productionOrder = null;
        addMaterial = false;
        showProductionOrders = true;
        showInit();
    }

    public OrderMaterial getOrderMaterial() {
        return orderMaterial;
    }

    public void setOrderMaterial(OrderMaterial orderMaterial) {
        this.orderMaterial = orderMaterial;
    }

    public ProductItem getProductItem() {
        return productItem;
    }

    public void setProductItem(ProductItem productItem) {
        this.productItem = productItem;
    }

    public List<OrderMaterial> getOrderMaterials() {
        return orderMaterials;
    }

    public void setOrderMaterials(List<OrderMaterial> orderMaterials) {
        this.orderMaterials = orderMaterials;
    }

    public Boolean getShowMaterialDetail() {
        return showMaterialDetail;
    }

    public void setShowMaterialDetail(Boolean showMaterialDetail) {
        this.showMaterialDetail = showMaterialDetail;
    }

    public Boolean getShowDetailOrder() {
        return showDetailOrder;
    }

    public void setShowDetailOrder(Boolean showDetailOrder) {
        this.showDetailOrder = showDetailOrder;
    }

    public Boolean getShowProductionOrders() {
        return showProductionOrders;
    }

    public void setShowProductionOrders(Boolean showProductionOrders) {
        this.showProductionOrders = showProductionOrders;
    }

    public Boolean getShowReprocessedProduct() {
        return showReprocessedProduct;
    }

    public void setShowReprocessedProduct(Boolean showReprocessedProduct) {
        baseProduct = new BaseProduct();
        singleProduct = new SingleProduct();
        orderBaseInputs = new ArrayList<OrderInput>();
        productProcessings = new ArrayList<ProductProcessing>();
        this.showReprocessedProduct = showReprocessedProduct;
        codeGenerate = "";
        hideButtonGeneral();
        hideTablesIni();
    }

    public void cancelReprossecing(){
        baseProduct = new BaseProduct();
        singleProduct = new SingleProduct();
        this.showReprocessedProduct = false;
        showInit();
    }

    public void hideButtonGeneral()
    {
        showButtonAddProduct = false;
        showButtonReprocessed = false;
        showGenerateAllVoucher = false;
        showGenerateAllAccountEntries = false;
        showGenerateRequestByPlanning = false;
        showProductionOrders = true;
    }

    public void hideTablesIni()
    {
        showListReprocessedProduct = false;
        showProductionList = false;
        showProductionOrders = false;
    }

    public void editReprocessedProduct(BaseProduct base)
    {
        baseProduct = base;
        orderBaseInputs = new ArrayList<OrderInput>();
        orderBaseInputs = base.getOrderInputs();
        productProcessings = new ArrayList<ProductProcessing>();
        productProcessings = base.getProductProcessings();
        showReprocessedProduct = true;
        codeGenerate = base.getCode();
        hideButtonGeneral();
        hideTablesIni();
    }

    public List<OrderInput> getOrderBaseInputs() {
        return orderBaseInputs;
    }

    public void setOrderBaseInputs(List<OrderInput> orderBaseInputs) {
        this.orderBaseInputs = orderBaseInputs;
    }

    public Boolean getShowSingleProduct() {
        return showSingleProduct;
    }

    public void setShowSingleProduct(Boolean showSingleProduct) {
        this.showSingleProduct = showSingleProduct;
        showInit();
    }

    public void cancelSingleProduct()
    {
        showInit();
    }

    public SingleProduct getSingleProduct() {
        return singleProduct;
    }

    public void setSingleProduct(SingleProduct singleProduct) {
        this.singleProduct = singleProduct;
    }

    public String getCodeGenerate() {
        if(this.codeGenerate.length() == 0)
        this.codeGenerate = productionOrderCodeGenerator.generateCode();
        else
        return this.codeGenerate;

        return codeGenerate;
    }

    public void setCodeGenerate(String codeGenerate) {
        this.codeGenerate = codeGenerate;
    }

    public List<OrderMaterial> getOrderSingleMaterial() {
        return orderSingleMaterial;
    }

    public void setOrderSingleMaterial(List<OrderMaterial> orderSingleMaterial) {
        this.orderSingleMaterial = orderSingleMaterial;
    }

    public BaseProduct getBaseProduct() {
        return baseProduct;
    }

    public void setBaseProduct(BaseProduct baseProduct) {
        this.baseProduct = baseProduct;
    }

    public List<ProductProcessing> getProductProcessings() {
        return productProcessings;
    }

    public void setProductProcessings(List<ProductProcessing> productProcessings) {
        this.productProcessings = productProcessings;
    }

    public Boolean getShowListReprocessedProduct() {
        return showListReprocessedProduct;
    }

    public void setShowListReprocessedProduct(Boolean showListReprocessedProduct) {
        this.showListReprocessedProduct = showListReprocessedProduct;
    }

    public Boolean getShowButtonReprocessed() {
        return showButtonReprocessed;
    }

    public void setShowButtonReprocessed(Boolean showButtonReprocessed) {
        this.showButtonReprocessed = showButtonReprocessed;
    }

    public Boolean getShowButtonAddProduct() {
        return showButtonAddProduct;
    }

    public void setShowButtonAddProduct(Boolean showButtonAddProduct) {
        this.showButtonAddProduct = showButtonAddProduct;
    }

    public Boolean getShowProductionList() {
        return showProductionList;
    }

    public void setShowProductionList(Boolean showProductionList) {
        this.showProductionList = showProductionList;
    }

    public Boolean getShowDetailSingleProduct() {
        return showDetailSingleProduct;
    }

    public void setShowDetailSingleProduct(Boolean showDetailSingleProduct) {
        this.showDetailSingleProduct = showDetailSingleProduct;
    }

    public Boolean getShowGenerateAllVoucher() {
        return showGenerateAllVoucher;
    }

    public void setShowGenerateAllVoucher(Boolean showGenerateAllVoucher) {
        this.showGenerateAllVoucher = showGenerateAllVoucher;
    }

    public Boolean getShowGenerateRequestByPlanning() {
        return showGenerateRequestByPlanning;
    }

    public void setShowGenerateRequestByPlanning(Boolean showGenerateRequestByPlanning) {
        this.showGenerateRequestByPlanning = showGenerateRequestByPlanning;
    }

    public Boolean getShowGenerateAllAccountEntries() {
        return showGenerateAllAccountEntries;
    }

    public void setShowGenerateAllAccountEntries(Boolean showGenerateAllAccountEntries) {
        this.showGenerateAllAccountEntries = showGenerateAllAccountEntries;
    }

    public boolean isShowButtonAddInput() {
        return showButtonAddInput;
    }

    public void setShowButtonAddInput(boolean showButtonAddInput) {
        this.showButtonAddInput = showButtonAddInput;
    }

    public void clearProductMain()
    {
        productionOrder.setProductMain(null);
        hasMainProduction = false;
    }

    public void assignProductMain(ProductionOrder orderItem){
        productionOrder.setProductMain(orderItem);
        //productComposition = metaProductService.findProductoComposition(orderItem.getProductComposition());
        //productCompositionSelected(null);
        hasMainProduction = true;
    }

    public Boolean getHasMainProduction() {
        if(productionOrder.getProductMain() != null)
            hasMainProduction = true;
        else
            hasMainProduction = false;
        return hasMainProduction;
    }

    public void setHasMainProduction(Boolean hasMainProduction) {
        this.hasMainProduction = hasMainProduction;
    }

    public boolean isShowMainProduct() {
        return showMainProduct;
    }

    public void setShowMainProduct(boolean showMainProduct) {
        this.showMainProduct = showMainProduct;
    }
}
