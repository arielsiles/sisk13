package com.encens.khipus.action.warehouse.reports;

import com.encens.khipus.action.reports.GenericReportAction;
import com.encens.khipus.model.production.BaseProduct;
import com.encens.khipus.model.production.ProductionOrder;
import com.encens.khipus.model.production.SingleProduct;
import com.encens.khipus.model.warehouse.*;
import com.encens.khipus.service.customers.ArticleOrderService;
import com.encens.khipus.service.production.ProductionOrderService;
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

    @Create
    public void init() {
        restrictions = new String[]{};
    }


    protected String getEjbql() {
        return "";
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

        try{
            File jasper = new File(JSFUtil.getRealPath("/warehouse/reports/productInventoryReport.jasper"));
            JasperPrint jasperPrint = JasperFillManager.fillReport(jasper.getPath(), parameters, new JRBeanCollectionDataSource(beanCollection));
            exportarPDF(jasperPrint);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public Collection<CollectionData> calculateCollectionData(){

        Collection<CollectionData> beanCollection = new ArrayList();
        /** Inventario inicio gestion **/
        List<InitialInventory> initialInventoryList   = productInventoryService.findInitialInventory(warehouse.getWarehouseCode(), DateUtils.getCurrentYear(startDate).toString());
        /** Vales de movimiento **/
        List<MovementDetail> movementDetailList;
        if (warehouse.getWarehouseCode().equals("2"))
            movementDetailList = movementDetailService.findListMovementByWarehouseAndTypeNull(warehouse.getWarehouseCode(), startDate, endDate, null);
        else
            movementDetailList = movementDetailService.findListMovementByWarehouseAndType(warehouse.getWarehouseCode(), startDate, endDate, null);

        /** Ordenes de produccion **/
        List<ProductionOrder> productionOrderList = productionOrderService.findProductionOrders(startDate, endDate);
        List<BaseProduct> baseProductList         = productionOrderService.findBaseProductByDate(startDate, endDate);
        /** Ventas al contado y pedidos **/
        List cashSaleDetailList = articleOrderService.findCashSaleDetailListGroupBy(startDate, endDate);
        List orderDetailList    = articleOrderService.findCustomerOrderDetailListGroupBy(startDate, endDate);
        /** Saldos iniciales de gestion calculados **/
        Collection<CollectionData> initialArticleList = calculateInitialArticle(warehouse.getWarehouseCode(), startDate);

        /** Inventario inicial inv_inicio **/
        for (InitialInventory initialInventory:initialInventoryList){
            /** Fijando el saldo inicial de un articulo **/
            BigDecimal initQuantity = BigDecimal.ZERO;
            for (CollectionData article:initialArticleList){
                if (initialInventory.getProductItemCode().equals(article.getCode())){
                    initQuantity = article.getBalance();
                    break;
                }
            }

            CollectionData data = new CollectionData(
                    initialInventory.getProductItemCode(),
                    initialInventory.getProductItemName(),
                    initialInventory.getProductItem().getUsageMeasureCode(),
                    //initialInventory.getQuantity(),
                    //kardexProductMovementAction.calculateInitialAmountToKardex(initialInventory.getProductItemCode(), startDate),
                    initQuantity,
                    BigDecimal.ZERO,
                    BigDecimal.ZERO,
                    BigDecimal.ZERO);

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
                        totalCost = BigDecimalUtil.sum(totalCost, BigDecimalUtil.toBigDecimal(detail.getPurchasePrice()), 6);
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


    //private List<InitialArticle> calculateInitialArticle(String warehouseCode, Date startDate)
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
                    initialInventory.getProductItemName(),
                    initialInventory.getProductItem().getUsageMeasureCode(),
                    initialInventory.getQuantity(),
                    //kardexProductMovementAction.calculateInitialAmountToKardex(initialInventory.getProductItemCode(), startDate),
                    BigDecimal.ZERO,
                    BigDecimal.ZERO,
                    BigDecimal.ZERO);

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
                        totalCost = BigDecimalUtil.sum(totalCost, BigDecimalUtil.toBigDecimal(detail.getPurchasePrice()), 6);
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
        response.addHeader("Content-disposition", "attachment; filename=kardexProductMovement.pdf");
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

        public CollectionData(String code, String productName, String unit, BigDecimal initialAmount,  BigDecimal entryAmount, BigDecimal outputAmount, BigDecimal balance){

            this.setCode(code);
            this.setProductName(productName);
            this.unit = unit;
            this.setInitialAmount(initialAmount);
            this.setEntryAmount(entryAmount);
            this.setOutputAmount(outputAmount);
            this.setBalance(balance);
            this.unitCost = BigDecimal.ZERO;
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


}