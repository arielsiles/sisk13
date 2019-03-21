package com.encens.khipus.action.warehouse.reports;

import com.encens.khipus.action.reports.GenericReportAction;
import com.encens.khipus.model.customers.ArticleOrder;
import com.encens.khipus.model.production.BaseProduct;
import com.encens.khipus.model.production.ProductionOrder;
import com.encens.khipus.model.production.SingleProduct;
import com.encens.khipus.model.warehouse.*;
import com.encens.khipus.service.customers.ArticleOrderService;
import com.encens.khipus.service.production.ProductionOrderService;
import com.encens.khipus.service.warehouse.InitialInventoryService;
import com.encens.khipus.service.warehouse.MovementDetailService;
import com.encens.khipus.service.warehouse.ProductInventoryService;
import com.encens.khipus.service.warehouse.ProductItemService;
import com.encens.khipus.util.BigDecimalUtil;
import com.encens.khipus.util.DateUtils;
import com.encens.khipus.util.JSFUtil;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import javax.faces.context.FacesContext;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;

/**
 * Encens S.R.L.
 * This class implements the purchaseOrder report action
 *
 * @author
 * @version 3.0
 */
@Name("productInventoryReportAction")
@Scope(ScopeType.PAGE)
public class ProductInventoryReportAction extends GenericReportAction {

    private Date startDate;
    private Date endDate;
    private ProductItem productItem;
    private Warehouse warehouse;
    private Boolean articlesWithMovement = true;

    @In
    private MovementDetailService movementDetailService;
    @In
    private ArticleOrderService articleOrderService;
    @In
    private ProductItemService productItemService;
    @In
    private ProductInventoryService productInventoryService;
    @In
    private ProductionOrderService productionOrderService;

    @In(create = true)
    KardexProductMovementAction kardexProductMovementAction;

    @In
    private InitialInventoryService initialInventoryService;

    @Create
    public void init() {
        restrictions = new String[]{};
    }


    protected String getEjbql() {
        return "";
    }

    public void startInventoryAnnual(){

        Collection<CollectionData> beanCollection = calculateCollectionData();

        initialInventoryService.createInitialInventory(beanCollection, warehouse.getWarehouseCode(), startDate);

    }

    public void generateReport() {

        log.debug("generating Product Inventory Report................................................");

        Collection<CollectionData> beanCollection = calculateCollectionData();

        HashMap parameters = new HashMap();
        Map<String, Object> paramMap = new HashMap<String, Object>();
        paramMap.put("reportTitle", "REPORTE DE INVENTARIO");
        paramMap.put("startDate", startDate);
        paramMap.put("endDate", endDate);
        paramMap.put("warehouse", warehouse.getFullName());

        parameters.putAll(paramMap);

        for (CollectionData data : beanCollection){
            System.out.println("|"+ data.getCode() +"|"+ data.getProductName() +"|"+ data.getInitialAmount() +"|"+ data.getUnitCost() +"|"+ data.getBalance());
        }

        try{
            File jasper = new File(JSFUtil.getRealPath("/warehouse/reports/productInventoryReport.jasper"));
            JasperPrint jasperPrint = JasperFillManager.fillReport(jasper.getPath(), parameters, new JRBeanCollectionDataSource(beanCollection));
            exportarPDF(jasperPrint);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * 1. Calcula inventario de articulos por almacen dadas Fecha inicio y Fecha fin
     * 2. Calcula Costo unitario promedio segun el movimiento entre las fechas dadas (Inicio y Fin)
     *
     *
     * @return
     */
    public Collection<CollectionData> calculateCollectionData(){

        Collection<CollectionData> beanCollection = new ArrayList();


        /** 1.- Listado de articulos con saldos iniciales de gestion **/
        // Inventario inicio gestion
        List<InitialInventory> initialInventoryList   = productInventoryService.findInitialInventory(warehouse.getWarehouseCode(), DateUtils.getCurrentYear(startDate).toString());

        /** 2.- Obtiene en listas entradas y salidas de articulos: vales, ordenes de produccion, ventas, pedidos, etc. **/
        // Vales de movimiento
        List<MovementDetail> movementDetailList;
        if (warehouse.getWarehouseCode().equals("2"))
            movementDetailList = movementDetailService.findListMovementByWarehouseAndTypeNull(warehouse.getWarehouseCode(), startDate, endDate, null);
        else
            movementDetailList = movementDetailService.findListMovementByWarehouseAndType(warehouse.getWarehouseCode(), startDate, endDate, null);

        // Ordenes de produccion
        List<ProductionOrder> productionOrderList = productionOrderService.findProductionOrders(startDate, endDate);
        List<BaseProduct> baseProductList         = productionOrderService.findBaseProductByDate(startDate, endDate);

        // Ventas al contado y pedidos
        List cashSaleDetailList = articleOrderService.findCashSaleDetailListGroupBy(startDate, endDate);
        List orderDetailList    = articleOrderService.findCustomerOrderDetailListGroupBy(startDate, endDate);

        /** 3.-  Calcula (x almacen) en la lista los saldos de los articulos, desde el 1er dia de la gestion hasta la fecha de inicio seleccionada **/
        // solo calcula correctamente saldos fisicos ?
        Collection<CollectionData> initialArticleList = calculateInitialArticle(warehouse.getWarehouseCode(), startDate);

        /** Se va llenando la lista final, con saldos fisicos a la fecha de inicio seleccionada **/
        /**  **/
        //* Inventario inicial inv_inicio **/
        for (InitialInventory initialInventory:initialInventoryList){

            /** Fijando el saldo inicial de un articulo **/
            BigDecimal initQuantity = BigDecimal.ZERO;
            for (CollectionData article:initialArticleList){
                if (initialInventory.getProductItemCode().equals(article.getCode())){
                    initQuantity = article.getBalance(); // Fijando solo el saldo fisico inicial
                    break;
                }
            }

            CollectionData data = new CollectionData(   initialInventory.getProductItemCode(),
                                                        initialInventory.getProductItem().getName(),
                                                        initialInventory.getProductItem().getUsageMeasureCode(),
                                                        initQuantity,
                                                        BigDecimal.ZERO,
                                                        BigDecimal.ZERO,
                                                        BigDecimal.ZERO,
                                                        //initialInventory.getUnitCost()
                                                        BigDecimal.ZERO
                                                    );
            /** Ordenes de produccion **/
            for (ProductionOrder productionOrder:productionOrderList){
                if (initialInventory.getProductItemCode().equals(productionOrder.getProductComposition().getProcessedProduct().getProductItem().getProductItemCode())){
                    data.setEntryAmount(BigDecimalUtil.sum(data.getEntryAmount(), BigDecimalUtil.toBigDecimal(productionOrder.getProducedAmount()), 6));
                }
            }
            /** Reprocesos **/
            for (BaseProduct baseProduct:baseProductList){
                for (SingleProduct singleProduct:baseProduct.getSingleProducts()){
                    if (initialInventory.getProductItemCode().equals(singleProduct.getProductProcessingSingle().getMetaProduct().getProductItem().getProductItemCode())){
                        data.setEntryAmount(BigDecimalUtil.sum(data.getEntryAmount(), BigDecimalUtil.toBigDecimal(singleProduct.getAmount()), 6));
                    }
                }

            }

            /** Sum Entry an Output **/
            for (MovementDetail detail:movementDetailList){
                if (initialInventory.getProductItemCode().equals(detail.getProductItemCode())) {
                    if (detail.getMovementType().equals(MovementDetailType.E)) {
                        data.setEntryAmount(BigDecimalUtil.sum(data.getEntryAmount(), detail.getQuantity(), 6));
                    }
                    if (detail.getMovementType().equals(MovementDetailType.S))
                        data.setOutputAmount(BigDecimalUtil.sum(data.getOutputAmount(), detail.getQuantity(), 6));
                }
            }

            /** Ventas al contado **/
            for (int i = 0; i < cashSaleDetailList.size(); i++) {
                Object[] row = (Object[]) cashSaleDetailList.get(i);
                String codart = (String)row[0];
                Long total = (Long) row[1];
                if (initialInventory.getProductItemCode().equals(codart)) {
                    data.setOutputAmount(BigDecimalUtil.sum(data.getOutputAmount(), BigDecimalUtil.toBigDecimal(total), 6));
                }
            }

            /** Pedidos **/
            for (int i = 0; i < orderDetailList.size(); i++) {
                Object[] row = (Object[]) orderDetailList.get(i);
                String codart = (String)row[0];
                Long total = (Long) row[1];
                if (initialInventory.getProductItemCode().equals(codart)) {
                    data.setOutputAmount(BigDecimalUtil.sum(data.getOutputAmount(), BigDecimalUtil.toBigDecimal(total), 6));
                }
            }

            /** Añade a la lista Todos los articulos ó solo los con movimiento, segun se elija en la vista**/
            if (articlesWithMovement){ // Articulos solo con Movimiento o inventario inicial
                if (data.getInitialAmount().compareTo(BigDecimal.ZERO) > 0 ||
                        data.getEntryAmount().compareTo(BigDecimal.ZERO)   > 0 ||
                        data.getOutputAmount().compareTo(BigDecimal.ZERO)  > 0){
                    beanCollection.add(data);
                }
            }else {
                beanCollection.add(data);
            }
        }

        /** ---------------------------------------------------------------------------------------------------- **/
        /** ---------------------------------------------------------------------------------------------------- **/
        /** Se crea una lista con todos los articulos como Entrada (E) ó Salida (S) considerando Cantidad, Costo Unit, Costo Total**/
        List<ArticleMovement> resultList = new ArrayList<ArticleMovement>();

        for (InitialInventory initialInventory:initialInventoryList){
                ArticleMovement articleMovement = new ArticleMovement(
                        DateUtils.firstDayOfYear(DateUtils.getCurrentYear(startDate)),
                        initialInventory.getProductItemCode(),
                        MovementDetailType.E,
                        initialInventory.getQuantity(),
                        initialInventory.getUnitCost(),
                        BigDecimalUtil.multiply(initialInventory.getQuantity(), initialInventory.getUnitCost(), 6));
                resultList.add(articleMovement);
        }

        /** Vales de movimiento **/
        List<MovementDetail> detailList;
        if (warehouse.getWarehouseCode().equals("2"))
            detailList = movementDetailService.findListMovementByWarehouseAndTypeNull(warehouse.getWarehouseCode(), DateUtils.firstDayOfYear(DateUtils.getCurrentYear(startDate)), endDate, null);
        else
            detailList = movementDetailService.findListMovementByWarehouseAndType(warehouse.getWarehouseCode(), DateUtils.firstDayOfYear(DateUtils.getCurrentYear(startDate)), endDate, null);

        for (MovementDetail detail:detailList){
            ArticleMovement articleMovement = new ArticleMovement(
                    //detail.getMovementDetailDate(),
                    DateUtils.parse(DateUtils.format(detail.getMovementDetailDate(), "yyyy-MM-dd"), "yyyy-MM-dd"),
                    detail.getProductItemCode(),
                    detail.getMovementType(),
                    detail.getQuantity(),
                    //detail.getUnitPurchasePrice(),
                    detail.getUnitCost(),
                    //detail.getPurchasePrice()
                    detail.getAmount()
            );
            resultList.add(articleMovement);
        }

        /** Ordenes de produccion **/
        for (ProductionOrder productionOrder:productionOrderList){
            //data.setEntryAmount(BigDecimalUtil.sum(data.getEntryAmount(), BigDecimalUtil.toBigDecimal(productionOrder.getProducedAmount()), 2));
            ArticleMovement articleMovement = new ArticleMovement(
                    //productionOrder.getProductionPlanning().getDate(),
                    DateUtils.parse(DateUtils.format(productionOrder.getProductionPlanning().getDate(), "yyyy-MM-dd"), "yyyy-MM-dd"),
                    productionOrder.getProductComposition().getProcessedProduct().getProductItemCode(),
                    MovementDetailType.E,
                    BigDecimalUtil.toBigDecimal(productionOrder.getProducedAmount()),
                    BigDecimalUtil.toBigDecimal(productionOrder.getUnitCost()),
                    BigDecimalUtil.toBigDecimal(productionOrder.getTotalCostProduction())
            );
            resultList.add(articleMovement);

        }
        /** Reprocesos **/
        for (BaseProduct baseProduct:baseProductList){
            for (SingleProduct singleProduct:baseProduct.getSingleProducts()){
                //data.setEntryAmount(BigDecimalUtil.sum(data.getEntryAmount(), BigDecimalUtil.toBigDecimal(singleProduct.getAmount()), 2));
                ArticleMovement articleMovement = new ArticleMovement(
                        //singleProduct.getBaseProduct().getProductionPlanningBase().getDate(),
                        DateUtils.parse(DateUtils.format(singleProduct.getBaseProduct().getProductionPlanningBase().getDate(), "yyyy-MM-dd"), "yyyy-MM-dd"),
                        singleProduct.getProductProcessingSingle().getMetaProduct().getProductItem().getProductItemCode(),
                        MovementDetailType.E,
                        BigDecimalUtil.toBigDecimal(singleProduct.getAmount()),
                        BigDecimalUtil.toBigDecimal(singleProduct.getUnitCost()),
                        BigDecimalUtil.toBigDecimal(singleProduct.getTotalCostProduction())
                );
                resultList.add(articleMovement);
            }
        }

        /** Ventas al contado **/
        List<ArticleOrder> cashSaleArticleOrderList = articleOrderService.findCashSaleDetailList(startDate, endDate);

        for (ArticleOrder articleOrder:cashSaleArticleOrderList){
            ArticleMovement articleMovement = new ArticleMovement(
                    articleOrder.getVentaDirecta().getFechaPedido(),
                    articleOrder.getCodArt(),
                    MovementDetailType.S,
                    BigDecimalUtil.toBigDecimal(articleOrder.getTotal()),
                    BigDecimalUtil.toBigDecimal(articleOrder.getCu()),
                    BigDecimalUtil.multiply(BigDecimalUtil.toBigDecimal(articleOrder.getTotal()), BigDecimalUtil.toBigDecimal(articleOrder.getCu()), 6)
                    );
            resultList.add(articleMovement);
        }

        /** Pedidos **/
        List<ArticleOrder> orderCustomerDetailList = articleOrderService.findCustomerOrderDetailList(startDate, endDate);
        for (ArticleOrder articleOrder:orderCustomerDetailList){
            ArticleMovement articleMovement = new ArticleMovement(
                    articleOrder.getCustomerOrder().getFechaEntrega(),
                    articleOrder.getCodArt(),
                    MovementDetailType.S,
                    BigDecimalUtil.toBigDecimal(articleOrder.getTotal()),
                    BigDecimalUtil.toBigDecimal(articleOrder.getCu()),
                    BigDecimalUtil.multiply(BigDecimalUtil.toBigDecimal(articleOrder.getTotal()), BigDecimalUtil.toBigDecimal(articleOrder.getCu()), 6)
            );
            resultList.add(articleMovement);
        }
        /** ---------------------------------------------------------------------------------------------------- **/
        //----------------------------------------------------------------
        // Ordenamiento de la Lista por fecha
        Collections.sort(resultList, new Comparator<ArticleMovement>() {
            @Override
            public int compare(ArticleMovement o1, ArticleMovement o2) {
                return o1.getDate().compareTo(o2.getDate());
            }
        });

        /*
        System.out.println("------- RESULT LIST  -------- ");
        for (ArticleMovement article: resultList){
            if (article.getProductCode().equals("118")){
                System.out.println(article.getDate() + "\t" + article.getMovementType() + "\t" + article.getUnitCost() + "\t\t" + article.getQuantity() + "\t" + article.getTotalCost());
            }
        }
        */

        //----------------------------------------------------------------
        /** -------------------------------------------------------------------------------------- **/
        /** Calculo del COSTO UNITARIO segun Costo Promedio **/
        /** Si hay irregularidad en el calculo, valores negativos, toma como costo unitario la ultima Entrada **/
        BigDecimal quantity  = BigDecimal.ZERO;
        BigDecimal unitCost  = BigDecimal.ZERO;
        BigDecimal totalCost = BigDecimal.ZERO;
        boolean band = false;

        for (CollectionData data:beanCollection){

            for (ArticleMovement art:resultList){
                if (data.getCode().equals(art.getProductCode())){

                    if (art.getMovementType().equals(MovementDetailType.E)){
                        quantity  = BigDecimalUtil.sum(quantity, art.getQuantity(), 6);
                        totalCost = BigDecimalUtil.sum(totalCost, art.getTotalCost(), 6);
                        if (quantity.doubleValue() > 0) unitCost = BigDecimalUtil.divide(totalCost, quantity, 6);

                        if (quantity.doubleValue() < 0) band = true;

                    }

                    if (art.getMovementType().equals(MovementDetailType.S)){
                        quantity  = BigDecimalUtil.subtract(quantity, art.getQuantity(), 6);
                        totalCost = BigDecimalUtil.subtract(totalCost,
                                                            BigDecimalUtil.multiply(art.getQuantity(), unitCost, 6),
                                                            6);
                        if (quantity.doubleValue() < 0) band = true;
                    }
                    /** Si hay irregularidad en el calculo, valores negativos, toma como costo unitario la ultima Entrada **/
                    if (!band)
                        data.setUnitCost(unitCost);
                    else {
                        if (art.getMovementType().equals(MovementDetailType.E))
                            data.setUnitCost(art.getUnitCost());

                        /** Si no hay ultima entrada del producto, asigna el costo unitario de inv_inicio **/
                        if (BigDecimalUtil.compareTo(data.getUnitCost(), BigDecimal.ZERO) == 0) {
                            //System.out.println("====> COSTO UNIT ZERO: " + data.getCode() + " - " + data.getProductName());
                            data.setUnitCost(productInventoryService.findUnitCostbyCode(art.getProductCode(), DateUtils.getCurrentYear(startDate).toString()));
                        }
                    }
                }
            }

            quantity  = BigDecimal.ZERO;
            unitCost  = BigDecimal.ZERO;
            totalCost = BigDecimal.ZERO;
            band = false;
        }

        /** ------------------------------------------------------------------------------ **/

        for (CollectionData data:beanCollection){
            data.setBalance(BigDecimalUtil.sum(data.getInitialAmount(), data.getEntryAmount(), 6));
            data.setBalance(BigDecimalUtil.subtract(data.getBalance(), data.getOutputAmount(), 6));
            data.setValuedBalance(BigDecimalUtil.multiply(data.getBalance(), data.getUnitCost(), 6));
        }

        return beanCollection;
    }


    /** Calcula Saldos Iniciales **/
    public Collection<CollectionData> calculateInitialArticle(String warehouseCode, Date initDate){

        Calendar calendar = Calendar.getInstance();

        /** 1er dia del año **/
        Date firstDate = DateUtils.firstDayOfYear(DateUtils.getCurrentYear(initDate));
        /** Restando un dia a la fecha **/
        calendar.setTime(initDate);
        calendar.add(Calendar.DAY_OF_YEAR, -1);
        initDate = calendar.getTime();
        /** ---- **/

        Collection<CollectionData> beanCollection = new ArrayList();
        /** Inventario inicio gestion ok **/
        List<InitialInventory> initialInventoryList   = productInventoryService.findInitialInventory(warehouseCode, DateUtils.getCurrentYear(startDate).toString());
        /** Vales de movimiento **/
        List<MovementDetail> movementDetailList;
        if (warehouse.getWarehouseCode().equals("2"))
            movementDetailList = movementDetailService.findListMovementByWarehouseAndTypeNull(warehouseCode, firstDate, initDate, null);
        else
            movementDetailList = movementDetailService.findListMovementByWarehouseAndType(warehouseCode, firstDate, initDate, null);

        /** Ordenes de produccion **/
        List<ProductionOrder> productionOrderList = productionOrderService.findProductionOrders(firstDate, initDate);
        List<BaseProduct> baseProductList         = productionOrderService.findBaseProductByDate(firstDate, initDate);
        /** Ventas al contado y pedidos **/
        List cashSaleDetailList = articleOrderService.findCashSaleDetailListGroupBy(firstDate, initDate);
        List orderDetailList    = articleOrderService.findCustomerOrderDetailListGroupBy(firstDate, initDate);

        /** Inventario inicial inv_inicio **/
        for (InitialInventory initialInventory:initialInventoryList){
            CollectionData data = new CollectionData(
                    initialInventory.getProductItemCode(),
                    initialInventory.getProductItem().getName(),
                    initialInventory.getProductItem().getUsageMeasureCode(),
                    initialInventory.getQuantity(),
                    //kardexProductMovementAction.calculateInitialAmountToKardex(initialInventory.getProductItemCode(), startDate),
                    BigDecimal.ZERO,
                    BigDecimal.ZERO,
                    BigDecimal.ZERO,
                    initialInventory.getUnitCost());

            /** Ordenes de produccion **/
            for (ProductionOrder productionOrder:productionOrderList){
                if (initialInventory.getProductItemCode().equals(productionOrder.getProductComposition().getProcessedProduct().getProductItem().getProductItemCode())){
                    data.setEntryAmount(BigDecimalUtil.sum(data.getEntryAmount(), BigDecimalUtil.toBigDecimal(productionOrder.getProducedAmount()), 2));
                }
            }
            /** Reprocesos **/
            for (BaseProduct baseProduct:baseProductList){
                for (SingleProduct singleProduct:baseProduct.getSingleProducts()){
                    if (initialInventory.getProductItemCode().equals(singleProduct.getProductProcessingSingle().getMetaProduct().getProductItem().getProductItemCode())){
                        data.setEntryAmount(BigDecimalUtil.sum(data.getEntryAmount(), BigDecimalUtil.toBigDecimal(singleProduct.getAmount()), 2));
                    }
                }

            }

            /** Sum Entry an Output **/
            BigDecimal quantity  = BigDecimal.ZERO;
            BigDecimal totalCost = BigDecimal.ZERO;
            for (MovementDetail detail:movementDetailList){
                if (initialInventory.getProductItemCode().equals(detail.getProductItemCode())) {
                    if (detail.getMovementType().equals(MovementDetailType.E)) {
                        data.setEntryAmount(BigDecimalUtil.sum(data.getEntryAmount(), detail.getQuantity(), 2));

                        quantity  = BigDecimalUtil.sum(quantity, BigDecimalUtil.toBigDecimal(detail.getQuantity()), 6);
                        totalCost = BigDecimalUtil.sum(totalCost, BigDecimalUtil.toBigDecimal(detail.getAmount()), 6);
                        if (quantity.doubleValue()>0) data.setUnitCost(BigDecimalUtil.divide(totalCost, quantity, 2));

                    }
                    if (detail.getMovementType().equals(MovementDetailType.S))
                        data.setOutputAmount(BigDecimalUtil.sum(data.getOutputAmount(), detail.getQuantity(), 2));
                }
            }

            for (int i = 0; i < cashSaleDetailList.size(); i++) {
                Object[] row = (Object[]) cashSaleDetailList.get(i);
                String codart = (String)row[0];
                Long total = (Long) row[1];
                if (initialInventory.getProductItemCode().equals(codart)) {
                    data.setOutputAmount(BigDecimalUtil.sum(data.getOutputAmount(), BigDecimalUtil.toBigDecimal(total), 2));
                }
            }

            for (int i = 0; i < orderDetailList.size(); i++) {
                Object[] row = (Object[]) orderDetailList.get(i);
                String codart = (String)row[0];
                Long total = (Long) row[1];
                if (initialInventory.getProductItemCode().equals(codart)) {
                    data.setOutputAmount(BigDecimalUtil.sum(data.getOutputAmount(), BigDecimalUtil.toBigDecimal(total), 2));
                }
            }

            /** Unit cost **/
            quantity  = BigDecimal.ZERO;
            totalCost = BigDecimal.ZERO;
            for (ProductionOrder productionOrder:productionOrderList){
                if (initialInventory.getProductItemCode().equals(productionOrder.getProductComposition().getProcessedProduct().getProductItem().getProductItemCode())){
                    quantity  = BigDecimalUtil.sum(quantity, BigDecimalUtil.toBigDecimal(productionOrder.getProducedAmount()), 6);
                    totalCost = BigDecimalUtil.sum(totalCost, BigDecimalUtil.toBigDecimal(productionOrder.getTotalCostProduction()), 6);

                    if (quantity.doubleValue()>0) data.setUnitCost(BigDecimalUtil.divide(totalCost, quantity, 2));

                }
            }

            beanCollection.add(data);
        }

        for (CollectionData data:beanCollection){
            data.setBalance(BigDecimalUtil.sum(data.getInitialAmount(), data.getEntryAmount(), 2));
            data.setBalance(BigDecimalUtil.subtract(data.getBalance(), data.getOutputAmount(), 2));

            data.setValuedBalance(BigDecimalUtil.multiply(data.getBalance(), data.getUnitCost(), 2));
        }

        return beanCollection;
    }


    public void exportarPDF(JasperPrint jasperPrint) throws IOException, JRException {

        HttpServletResponse response = (HttpServletResponse) FacesContext.getCurrentInstance().getExternalContext().getResponse();
        response.addHeader("Content-disposition", "attachment; filename=ReporteGeneralInv.pdf");
        ServletOutputStream stream = response.getOutputStream();
        JasperExportManager.exportReportToPdfStream(jasperPrint, stream);
        stream.flush();
        stream.close();
        FacesContext.getCurrentInstance().responseComplete();
    }

    public void cleanProductItem() {
        this.productItem = null;
    }

    public void assignProductItem(ProductItem productItem) {
        this.productItem = productItem;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public ProductItem getProductItem() {
        return productItem;
    }

    public void setProductItem(ProductItem productItem) {
        this.productItem = productItem;
    }

    public Warehouse getWarehouse() {
        return warehouse;
    }

    public void setWarehouse(Warehouse warehouse) {
        this.warehouse = warehouse;
    }

    public void cleanWarehouseField() {
        warehouse = null;
    }

    public Boolean getArticlesWithMovement() {
        return articlesWithMovement;
    }

    public void setArticlesWithMovement(Boolean articlesWithMovement) {
        this.articlesWithMovement = articlesWithMovement;
    }

    /**
     *
     */
    public class InitialArticle{

        private String code;
        private BigDecimal quantity;

        public InitialArticle(String code, BigDecimal quantity){
            this.code = code;
            this.quantity = quantity;
        }

        public BigDecimal getQuantity() {
            return quantity;
        }

        public void setQuantity(BigDecimal quantity) {
            this.quantity = quantity;
        }

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }
    }

    /**
     * CollectionData class for Product Inventory Report
     */
    public class CollectionData{

        private String code;
        private String productName;
        private String unit;
        private BigDecimal initialAmount;
        private BigDecimal entryAmount;
        private BigDecimal outputAmount;
        private BigDecimal balance;
        private BigDecimal unitCost;
        private BigDecimal valuedBalance;

        public CollectionData(String code, String productName, String unit, BigDecimal initialAmount,  BigDecimal entryAmount, BigDecimal outputAmount, BigDecimal balance, BigDecimal unitCost){

            this.setCode(code);
            this.setProductName(productName);
            this.unit = unit;
            this.setInitialAmount(initialAmount);
            this.setEntryAmount(entryAmount);
            this.setOutputAmount(outputAmount);
            this.setBalance(balance);
            this.unitCost = unitCost;
            this.valuedBalance = BigDecimal.ZERO;
        }


        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public String getProductName() {
            return productName;
        }

        public void setProductName(String productName) {
            this.productName = productName;
        }

        public BigDecimal getInitialAmount() {
            return initialAmount;
        }

        public void setInitialAmount(BigDecimal initialAmount) {
            this.initialAmount = initialAmount;
        }

        public BigDecimal getEntryAmount() {
            return entryAmount;
        }

        public void setEntryAmount(BigDecimal entryAmount) {
            this.entryAmount = entryAmount;
        }

        public BigDecimal getOutputAmount() {
            return outputAmount;
        }

        public void setOutputAmount(BigDecimal outputAmount) {
            this.outputAmount = outputAmount;
        }

        public BigDecimal getBalance() {
            return balance;
        }

        public void setBalance(BigDecimal balance) {
            this.balance = balance;
        }

        public String getUnit() {
            return unit;
        }

        public void setUnit(String unit) {
            this.unit = unit;
        }

        public BigDecimal getUnitCost() {
            return unitCost;
        }

        public void setUnitCost(BigDecimal unitCost) {
            this.unitCost = unitCost;
        }

        public BigDecimal getValuedBalance() {
            return valuedBalance;
        }

        public void setValuedBalance(BigDecimal valuedBalance) {
            this.valuedBalance = valuedBalance;
        }
    }


    public class ArticleMovement{

        private Date date;
        private String productCode;
        private MovementDetailType movementType;
        private BigDecimal quantity;
        private BigDecimal unitCost;
        private BigDecimal totalCost;

        ArticleMovement(Date date, String productCode, MovementDetailType movementType, BigDecimal quantity, BigDecimal unitCost, BigDecimal totalCost){
            this.setDate(date);
            this.setProductCode(productCode);
            this.setMovementType(movementType);
            this.setQuantity(quantity);
            this.setUnitCost(unitCost);
            this.setTotalCost(totalCost);
        }

        public Date getDate() {
            return date;
        }

        public void setDate(Date date) {
            this.date = date;
        }

        public String getProductCode() {
            return productCode;
        }

        public void setProductCode(String productCode) {
            this.productCode = productCode;
        }

        public BigDecimal getQuantity() {
            return quantity;
        }

        public void setQuantity(BigDecimal quantity) {
            this.quantity = quantity;
        }

        public BigDecimal getUnitCost() {
            return unitCost;
        }

        public void setUnitCost(BigDecimal unitCost) {
            this.unitCost = unitCost;
        }

        public BigDecimal getTotalCost() {
            return totalCost;
        }

        public void setTotalCost(BigDecimal totalCost) {
            this.totalCost = totalCost;
        }

        public MovementDetailType getMovementType() {
            return movementType;
        }

        public void setMovementType(MovementDetailType movementType) {
            this.movementType = movementType;
        }
    }

}