package com.encens.khipus.action.warehouse.reports;

import com.encens.khipus.action.reports.GenericReportAction;
import com.encens.khipus.model.customers.ArticleOrder;
import com.encens.khipus.model.production.BaseProduct;
import com.encens.khipus.model.production.ProductionOrder;
import com.encens.khipus.model.production.ProductionProduct;
import com.encens.khipus.model.production.SingleProduct;
import com.encens.khipus.model.warehouse.MovementDetail;
import com.encens.khipus.model.warehouse.MovementDetailType;
import com.encens.khipus.model.warehouse.ProductItem;
import com.encens.khipus.service.customers.ArticleOrderService;
import com.encens.khipus.service.production.ProductionOrderService;
import com.encens.khipus.service.warehouse.MovementDetailService;
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
import java.text.DecimalFormat;
import java.util.*;

/**
 * Encens S.R.L.
 * This class implements the purchaseOrder report action
 *
 * @author
 * @version 3.0
 */
@Name("kardexProductMovementAction")
@Scope(ScopeType.PAGE)
public class KardexProductMovementAction extends GenericReportAction {

    private Date startDate;
    private Date endDate;
    private ProductItem productItem;

    @In
    private MovementDetailService movementDetailService;
    @In
    private ArticleOrderService articleOrderService;
    @In
    private ProductItemService productItemService;
    @In
    private ProductionOrderService productionOrderService;

    @Create
    public void init() {
        restrictions = new String[]{};
    }


    protected String getEjbql() {
        return "";
    }

    public void generateReport() {

        log.debug("generating Kardex Product Movement................................................");

        Collection<CollectionData> beanCollection = calculateCollectionData();

        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy");
        DecimalFormat df = new DecimalFormat("#,###.00");

        BigDecimal previousAmount = BigDecimal.ZERO;

        if (startDate != null) {
            //previousAmount = movementDetailService.calculateInitialQuantityToKardex(Constants.defaultCompanyNumber, productItem.getProductItemCode(), startDate);
            previousAmount = calculateInitialAmountToKardex(productItem.getProductItemCode(), startDate);
        }

        HashMap parameters = new HashMap();
        Map<String, Object> paramMap = new HashMap<String, Object>();
        paramMap.put("reportTitle", "REPORTE DE MOVIMIENTOS");
        paramMap.put("productItemName", productItem.getFullName());
        paramMap.put("unit", productItem.getUsageMeasureCode());
        paramMap.put("startDate", startDate);
        paramMap.put("endDate", endDate);
        paramMap.put("previousAmount", previousAmount);

        parameters.putAll(paramMap);

        try{
            File jasper = new File(JSFUtil.getRealPath("/warehouse/reports/kardexProductMovement.jasper"));
            JasperPrint jasperPrint = JasperFillManager.fillReport(jasper.getPath(), parameters, new JRBeanCollectionDataSource(beanCollection));
            exportarPDF(jasperPrint);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public Collection<CollectionData> calculateCollectionData(){

        List<CollectionData> datas = new ArrayList<CollectionData>();

        List<MovementDetail> movementDetailList = movementDetailService.findDetailListByProductAndDate(productItem.getProductItemCode(), startDate, endDate);
        List<ArticleOrder> cashSaleDetailList   = articleOrderService.findCashSaleDetailByCodeAndDate(productItem.getProductItemCode(), startDate, endDate);
        List<ArticleOrder> orderDetailList      = articleOrderService.findOrderDetailByCodeAndDate(productItem.getProductItemCode(), startDate, endDate);

        List<ProductionOrder> productionOrderList = productionOrderService.findProductionOrdersByProductItem(productItem.getProductItemCode(), startDate, endDate);
        List<BaseProduct> baseProductList         = productionOrderService.findBaseProductByDate(startDate, endDate);

        List<ProductionProduct> productionProductList = productionOrderService.findProductionByProductItem(productItem.getProductItemCode(), startDate, endDate);

        for (ProductionOrder po:productionOrderList){
            CollectionData collectionData = new CollectionData(
                    po.getProductionPlanning().getDate(),
                    po.getCode(),
                    BigDecimalUtil.toBigDecimal(po.getProducedAmount()),
                    BigDecimal.ZERO,
                    "E",
                    "ORDEN DE PRODUCCION NRO. " + po.getCode());
            datas.add(collectionData);
        }

        for (ProductionProduct product : productionProductList){
            CollectionData collectionData = new CollectionData(
                    product.getProductionPlan().getDate(),
                    product.getProductItemCode() ,
                    product.getQuantity() ,
                    BigDecimal.ZERO,
                    "E",
                    "ORDEN DE PRODUCCION FECHA " + DateUtils.format(product.getProductionPlan().getDate(), "dd/MM/yyyy") );
            datas.add(collectionData);
        }

        for (BaseProduct baseProduct:baseProductList){
            for (SingleProduct singleProduct:baseProduct.getSingleProducts()){
                if (singleProduct.getProductProcessingSingle().getMetaProduct().getProductItem().getProductItemCode().equals(productItem.getProductItemCode())){
                    CollectionData collectionData = new CollectionData(
                            baseProduct.getProductionPlanningBase().getDate(),
                            baseProduct.getCode(),
                            BigDecimalUtil.toBigDecimal(singleProduct.getAmount()),
                            BigDecimal.ZERO,
                            "E",
                            "REPROCESO DE PRODUCCION NRO. " + baseProduct.getCode());
                    datas.add(collectionData);
                }
            }
        }

        for (MovementDetail md:movementDetailList){
            CollectionData collectionData = new CollectionData(
                    /*md.getMovementDetailDate(),*/
                    md.getInventoryMovement().getWarehouseVoucher().getDate(),
                    md.getInventoryMovement().getWarehouseVoucher().getNumber(),
                    md.getMovementType().name().equals("E") ? md.getQuantity() : BigDecimal.ZERO,
                    md.getMovementType().name().equals("S") ? md.getQuantity() : BigDecimal.ZERO,
                    md.getMovementType().name(),
                    md.getInventoryMovement().getDescription());
            datas.add(collectionData);
        }

        for (ArticleOrder ao:cashSaleDetailList){
            CollectionData collectionData = new CollectionData( ao.getVentaDirecta().getFechaPedido(),
                                                                ao.getVentaDirecta().getCodigo().toString(),
                                                                BigDecimal.ZERO,
                                                                BigDecimalUtil.toBigDecimal(ao.getTotal()),
                                                                "S",
                                                                "Venta al contado "+ao.getVentaDirecta().getCodigo() + " " + ao.getVentaDirecta().getCliente().getFullName());
            datas.add(collectionData);
        }

        for (ArticleOrder ao:orderDetailList){
            CollectionData collectionData = new CollectionData(
                                                                ao.getCustomerOrder().getOrderDate(),
                                                                ao.getCustomerOrder().getCode().toString(),
                                                                BigDecimal.ZERO,
                                                                BigDecimalUtil.toBigDecimal(ao.getTotal()),
                                                                "S",
                                                                "Venta a credito " + ao.getCustomerOrder().getCode() + " " + ao.getCustomerOrder().getClient().getFullName());
            datas.add(collectionData);
        }

        Collections.sort(datas, new Comparator<CollectionData>() {
            @Override
            public int compare(CollectionData o1, CollectionData o2) {
                return o1.getDate().toString().compareTo(o2.getDate().toString());
            }
        });

        Collection<CollectionData> beanCollection = new ArrayList();
        for (CollectionData data:datas){
            beanCollection.add(data);
        }

        return beanCollection;
    }

    public BigDecimal calculateInitialAmountToKardex(String productItemCode, Date initDate){

        Calendar calendar = Calendar.getInstance();
        BigDecimal initialQuantity = BigDecimal.ZERO;

        /** 1er dia del año **/
        Date firstDate = DateUtils.firstDayOfYear(DateUtils.getCurrentYear(initDate));
        /** Restando un dia a la fecha **/
        calendar.setTime(initDate);
        calendar.add(Calendar.DAY_OF_YEAR, -1);
        initDate = calendar.getTime();

        initialQuantity = productItemService.getInitialInventoryYear(productItemCode, DateUtils.getCurrentYear(firstDate).toString());

        List<MovementDetail> movementDetailList = movementDetailService.findDetailListByProductAndDate(productItemCode, firstDate, initDate);
        List<ArticleOrder> cashSaleDetailList   = articleOrderService.findCashSaleDetailByCodeAndDate(productItemCode, firstDate, initDate);
        List<ArticleOrder> orderDetailList     = articleOrderService.findOrderDetailByCodeAndDate(productItemCode, firstDate, initDate);

        List<ProductionOrder> productionOrderList = productionOrderService.findProductionOrdersByProductItem(productItemCode, firstDate, initDate);
        List<BaseProduct> baseProductList         = productionOrderService.findBaseProductByDate(firstDate, initDate);
        List<ProductionProduct> productionProductList = productionOrderService.findProductionByDate(firstDate, initDate);


        /** PR_PRODUCCION **/ /*** REVISAR DESDE EL 1 MAYO 2019 ***/
        for (ProductionProduct product : productionProductList){
            if (productItemCode.equals(product.getProductItemCode())){
                initialQuantity = BigDecimalUtil.sum(initialQuantity, product.getQuantity(), 2);
            }
        }
        for (ProductionOrder po:productionOrderList){
            initialQuantity = BigDecimalUtil.sum(initialQuantity, BigDecimalUtil.toBigDecimal(po.getProducedAmount()), 2);
        }
        for (BaseProduct baseProduct:baseProductList){
            for (SingleProduct singleProduct:baseProduct.getSingleProducts()){
                if (singleProduct.getProductProcessingSingle().getMetaProduct().getProductItem().getProductItemCode().equals(productItemCode)){
                    initialQuantity = BigDecimalUtil.sum(initialQuantity, BigDecimalUtil.toBigDecimal(singleProduct.getAmount()), 2);
                }
            }
        }

        for (MovementDetail md:movementDetailList){
            if (md.getMovementType().equals(MovementDetailType.E))
                initialQuantity = BigDecimalUtil.sum(initialQuantity, md.getQuantity(), 2);
            if (md.getMovementType().equals(MovementDetailType.S))
                initialQuantity = BigDecimalUtil.subtract(initialQuantity, md.getQuantity(), 2);
        }

        for (ArticleOrder ao:cashSaleDetailList){
            initialQuantity = BigDecimalUtil.subtract(initialQuantity, BigDecimalUtil.toBigDecimal(ao.getTotal()), 2);
        }

        for (ArticleOrder ao:orderDetailList){
            initialQuantity = BigDecimalUtil.subtract(initialQuantity, BigDecimalUtil.toBigDecimal(ao.getTotal()), 2);
        }

        return  initialQuantity;
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


    public class CollectionData{

        private Date date;
        private String code;
        private BigDecimal amountEntry;
        private BigDecimal amountOutput;
        private BigDecimal amount;
        private String movementType;
        private String description;

        public CollectionData(Date date, String code, BigDecimal amountEntry, BigDecimal amountOutput, String movementType, String description){
            this.date = date;
            this.code = code;
            this.amountEntry = amountEntry;
            this.amountOutput = amountOutput;
            this.movementType = movementType;
            this.description = description;
        }


        public Date getDate() {
            return date;
        }

        public void setDate(Date date) {
            this.date = date;
        }

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public BigDecimal getAmount() {
            return amount;
        }

        public void setAmount(BigDecimal amount) {
            this.amount = amount;
        }

        public String getMovementType() {
            return movementType;
        }

        public void setMovementType(String movementType) {
            this.movementType = movementType;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public BigDecimal getAmountEntry() {
            return amountEntry;
        }

        public void setAmountEntry(BigDecimal amountEntry) {
            this.amountEntry = amountEntry;
        }

        public BigDecimal getAmountOutput() {
            return amountOutput;
        }

        public void setAmountOutput(BigDecimal amountOutput) {
            this.amountOutput = amountOutput;
        }
    }


}