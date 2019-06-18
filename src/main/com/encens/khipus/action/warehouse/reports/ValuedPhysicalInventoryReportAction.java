package com.encens.khipus.action.warehouse.reports;

import com.encens.khipus.action.reports.GenericReportAction;
import com.encens.khipus.model.warehouse.InventoryPeriod;
import com.encens.khipus.model.warehouse.Warehouse;
import com.encens.khipus.service.accouting.VoucherAccoutingService;
import com.encens.khipus.service.warehouse.WarehouseService;
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
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage;

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
@Name("valuedPhysicalInventoryReportAction")
@Scope(ScopeType.PAGE)
public class ValuedPhysicalInventoryReportAction extends GenericReportAction {

    private Date startDate;
    private Date endDate;
    private Warehouse warehouse;
    //private CashAccount cashAccount;

    @In
    private WarehouseService warehouseService;
    @In
    private VoucherAccoutingService voucherAccoutingService;
    @In
    protected FacesMessages facesMessages;

    @Create
    public void init() {
        restrictions = new String[]{};
    }


    protected String getEjbql() {
        return "";
    }

    /*public void clearAccount() {
        setCashAccount(null);
    }*/

    public void generateReport() {

        log.debug("generating Product Inventory Report................................................");
        Collection<CollectionData> beanCollection = calculateValuedInventory();
        HashMap parameters = new HashMap();
        Map<String, Object> paramMap = new HashMap<String, Object>();
        paramMap.put("reportTitle", "REPORTE DE INVENTARIO FISICO - VALORADO");
        paramMap.put("startDate", startDate);
        paramMap.put("endDate", endDate);
        paramMap.put("cashAccount", this.warehouse.getWarehouseCashAccount().getFullName());
        parameters.putAll(paramMap);

        for (CollectionData data : beanCollection){
            System.out.println("|"+ data.getCodeArt() +"|"+ data.getName() +"|"+ data.getUnitCost() +"|"+ data.getQuantity() + "|" + data.getAmount());
        }
        try{
            File jasper = new File(JSFUtil.getRealPath("/warehouse/reports/valuedPhysicalInventory.jasper"));
            JasperPrint jasperPrint = JasperFillManager.fillReport(jasper.getPath(), parameters, new JRBeanCollectionDataSource(beanCollection));
            exportarPDF(jasperPrint);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public Collection<CollectionData> calculateValuedInventory(){

        Collection<CollectionData> beanCollection = new ArrayList();
        //List<Object[]> valuedInventory = voucherAccoutingService.getValuedInventory(startDate, endDate, getCashAccount());
        List<Object[]> valuedInventory = voucherAccoutingService.getValuedInventory(startDate, endDate, this.warehouse.getWarehouseCashAccount());

        for ( Object[] value : valuedInventory){
            String codArt = (String)value[0];
            String name = (String)value[1];
            String unit = (String)value[2];
            BigDecimal debit = (BigDecimal)value[3];
            BigDecimal credit = (BigDecimal)value[4];
            BigDecimal input = (BigDecimal)value[5];
            BigDecimal output = (BigDecimal)value[6];

            System.out.println("=====> $$$$$$$$$$$$$$$$");
            System.out.println("=====> INPUT: " + input);
            System.out.println("=====> OUTPUT: " + output);
            BigDecimal physicalBalance = BigDecimalUtil.subtract(input, output, 2);
            BigDecimal valuedBalance   = BigDecimalUtil.subtract(debit, credit, 2);

            BigDecimal unitCost = BigDecimal.ZERO;
            if (physicalBalance.doubleValue() != 0)
                unitCost = BigDecimalUtil.divide(valuedBalance, physicalBalance, 4);

            CollectionData data = new CollectionData(codArt, name, unit, physicalBalance, valuedBalance, unitCost);
            beanCollection.add(data);
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

    public void transferInventoryPeriod(){

        Collection<CollectionData> collectionData = calculateValuedInventory();
        Integer year  = DateUtils.getCurrentYear(this.endDate);
        Integer month = DateUtils.getCurrentMonth(this.endDate);

        if (month == 12){
            month = 1;
            year++;
        }else {
            month++;
        }

        if (warehouseService.wasTransferred(this.warehouse.getWarehouseCode(), year, month)){
            facesMessages.addFromResourceBundle(StatusMessage.Severity.WARN,"Warehouse.message.transferredInventoryPeriod");
            return;
        }

        List<InventoryPeriod> inventoryPeriodList = new ArrayList<InventoryPeriod>();
        for (CollectionData data : collectionData){
            InventoryPeriod inventory = new InventoryPeriod();
            inventory.setProductItemCode(data.getCodeArt());
            inventory.setQuantity(data.getQuantity());
            inventory.setAmount(data.getAmount());
            inventory.setUnitCost(data.getUnitCost());
            inventory.setWarehouseCode(this.warehouse.getWarehouseCode());
            inventory.setYear(year);
            inventory.setMonth(month);
            inventoryPeriodList.add(inventory);
        }
        warehouseService.createTransferInventoryPeriod(inventoryPeriodList);
        facesMessages.addFromResourceBundle(StatusMessage.Severity.INFO,"Warehouse.message.successfulTransferInventoryPeriod");
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

    public Warehouse getWarehouse() {
        return warehouse;
    }

    public void setWarehouse(Warehouse warehouse) {
        this.warehouse = warehouse;
    }

    public void cleanWarehouseField() {
        warehouse = null;
    }

    /*public CashAccount getCashAccount() {
        return cashAccount;
    }

    public void setCashAccount(CashAccount cashAccount) {
        this.cashAccount = cashAccount;
    }*/


    /**
     * CollectionData class for Product Inventory Report
     */
    public class CollectionData{

        private String codeArt;
        private String name;
        private String unit;
        private BigDecimal quantity;
        private BigDecimal amount;
        private BigDecimal unitCost;

        public CollectionData(String codeArt, String name, String unit, BigDecimal quantity, BigDecimal amount, BigDecimal unitCost){
            this.setCodeArt(codeArt);
            this.setName(name);
            this.setUnit(unit);
            this.setQuantity(quantity);
            this.setAmount(amount);
            this.setUnitCost(unitCost);
        }

        public String getCodeArt() {
            return codeArt;
        }

        public void setCodeArt(String codeArt) {
            this.codeArt = codeArt;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public BigDecimal getQuantity() {
            return quantity;
        }

        public void setQuantity(BigDecimal quantity) {
            this.quantity = quantity;
        }

        public BigDecimal getAmount() {
            return amount;
        }

        public void setAmount(BigDecimal amount) {
            this.amount = amount;
        }

        public BigDecimal getUnitCost() {
            return unitCost;
        }

        public void setUnitCost(BigDecimal unitCost) {
            this.unitCost = unitCost;
        }

        public String getUnit() {
            return unit;
        }

        public void setUnit(String unit) {
            this.unit = unit;
        }
    }




}