package com.encens.khipus.action.warehouse.reports;

import com.encens.khipus.action.reports.GenericReportAction;
import com.encens.khipus.model.customers.ArticleOrder;
import com.encens.khipus.model.employees.MovementType;
import com.encens.khipus.model.warehouse.*;
import com.encens.khipus.service.customers.ArticleOrderService;
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

        List<InitialInventory> initialInventoryList   = productInventoryService.findInitialInventory(warehouse.getWarehouseCode(), DateUtils.getCurrentYear(startDate).toString());
        List<MovementDetail> entryMovementDetailList  = movementDetailService.findListMovementByWarehouseAndType(warehouse.getWarehouseCode(), startDate, endDate, MovementDetailType.E);
        List<MovementDetail> outputMovementDetailList = movementDetailService.findListMovementByWarehouseAndType(warehouse.getWarehouseCode(), startDate, endDate, MovementDetailType.S);

        List cashSaleDetailList = articleOrderService.findCashSaleDetailListGroupBy(startDate, endDate);
        List orderDetailList    = articleOrderService.findCustomerOrderDetailListGroupBy(startDate, endDate);


        for (InitialInventory initialInventory:initialInventoryList){
            CollectionData data = new CollectionData(
                    initialInventory.getProductItemCode(),
                    initialInventory.getProductItemName(),
                    initialInventory.getProductItem().getUsageMeasureCode(),
                    initialInventory.getQuantity(),
                    BigDecimal.ZERO,
                    BigDecimal.ZERO,
                    BigDecimal.ZERO);
            /** Sum entry **/
            for (MovementDetail detail:entryMovementDetailList){
                if (initialInventory.getProductItemCode().equals(detail.getProductItemCode())) {
                    data.setEntryAmount(BigDecimalUtil.sum(data.getEntryAmount(), detail.getQuantity(), 2));
                }
            }

            /** Sum output **/
            for (MovementDetail detail:outputMovementDetailList){
                if (initialInventory.getProductItemCode().equals(detail.getProductItemCode())) {
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

            beanCollection.add(data);
        }

        for (CollectionData data:beanCollection){
            data.setBalance(BigDecimalUtil.sum(data.getInitialAmount(), data.getEntryAmount(), 2));
            data.setBalance(BigDecimalUtil.subtract(data.getBalance(), data.getOutputAmount(), 2));
        }

        return beanCollection;
    }

    /*public BigDecimal calculateInitialAmountToKardex(String productItemCode, Date initDate){

        Calendar calendar = Calendar.getInstance();
        BigDecimal initialQuantity = BigDecimal.ZERO;

        return  initialQuantity;
    }*/

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

        public CollectionData(String code, String productName, String unit, BigDecimal initialAmount,  BigDecimal entryAmount, BigDecimal outputAmount, BigDecimal balance){

            this.setCode(code);
            this.setProductName(productName);
            this.unit = unit;
            this.setInitialAmount(initialAmount);
            this.setEntryAmount(entryAmount);
            this.setOutputAmount(outputAmount);
            this.setBalance(balance);
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
    }


}