package com.encens.khipus.action.warehouse.reports;

import com.encens.khipus.action.reports.GenericReportAction;
import com.encens.khipus.exception.finances.CompanyConfigurationNotFoundException;
import com.encens.khipus.model.finances.CompanyConfiguration;
import com.encens.khipus.model.warehouse.InventoryPeriod;
import com.encens.khipus.model.warehouse.Warehouse;
import com.encens.khipus.service.accouting.VoucherAccoutingService;
import com.encens.khipus.service.fixedassets.CompanyConfigurationService;
import com.encens.khipus.service.warehouse.WarehouseService;
import com.encens.khipus.util.BigDecimalUtil;
import com.encens.khipus.util.Constants;
import com.encens.khipus.util.DateUtils;
import com.encens.khipus.util.JSFUtil;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
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
    @In
    private CompanyConfigurationService companyConfigurationService;

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
        CompanyConfiguration companyConfiguration = null;
        try {
            companyConfiguration = companyConfigurationService.findCompanyConfiguration();
        } catch (CompanyConfigurationNotFoundException e) {facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,"CompanyConfiguration.notFound");;}

        Collection<CollectionData> beanCollection = calculateValuedInventory();
        HashMap parameters = new HashMap();
        Map<String, Object> paramMap = new HashMap<String, Object>();
        paramMap.put("reportTitle", "REPORTE DE INVENTARIO FISICO - VALORADO");
        paramMap.put("companyName", companyConfiguration.getCompanyName());
        paramMap.put("systemName", companyConfiguration.getSystemName());
        paramMap.put("locationName", companyConfiguration.getLocationName());
        paramMap.put("startDate", startDate);
        paramMap.put("endDate", endDate);
        paramMap.put("cashAccount", this.warehouse.getWarehouseCashAccount().getFullName());
        parameters.putAll(paramMap);

        System.out.println("|Codigo|Articulo|Unidad|Costo Unit|Saldo Fis|Saldo Val");
        for (CollectionData data : beanCollection){
            System.out.println("|"+ data.getCodeArt() +"|"+ data.getName() +"|"+ data.getUnit() +"|"+ data.getUnitCost() +"|"+ data.getQuantity() + "|" + data.getAmount());
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
            System.out.println("=====> COD_art: " + codArt);
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


    public void generateReportExcel() {

        log.debug("generating Product Inventory Report EXCEL................................................");
        CompanyConfiguration companyConfiguration = null;
        try {
            companyConfiguration = companyConfigurationService.findCompanyConfiguration();
        } catch (CompanyConfigurationNotFoundException e) {facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,"CompanyConfiguration.notFound");}

        Collection<CollectionData> beanCollection = calculateValuedInventory();

        try {
            HSSFWorkbook workbook = new HSSFWorkbook();
            HSSFSheet sheet = workbook.createSheet("Inventario Valorado");

            // Estilo negrilla
            HSSFFont boldFont = workbook.createFont();
            boldFont.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
            HSSFCellStyle boldStyle = workbook.createCellStyle();
            boldStyle.setFont(boldFont);

            HSSFCellStyle boldNumberStyle = workbook.createCellStyle();
            boldNumberStyle.setFont(boldFont);
            HSSFCellStyle numberStyle = workbook.createCellStyle();

            // Anchos de columna (px * 36 aprox para convertir a unidades POI)
            sheet.setColumnWidth(0, 75 * 36);   // A: 75px
            sheet.setColumnWidth(1, 370 * 36);  // B: 370px
            sheet.setColumnWidth(2, 72 * 36);   // C: 72px
            sheet.setColumnWidth(3, 115 * 36);  // D: 115px
            sheet.setColumnWidth(4, 115 * 36);  // E: 115px
            sheet.setColumnWidth(5, 115 * 36);  // F: 115px

            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy");
            int rowNum = 0;

            // Encabezado del reporte
            HSSFRow row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(companyConfiguration.getCompanyName());

            row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue("REPORTE DE INVENTARIO FISICO - VALORADO");

            row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(this.warehouse.getWarehouseCashAccount().getFullName());

            row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue("Periodo: " + sdf.format(startDate) + " - " + sdf.format(endDate));

            rowNum++; // fila vacia

            // Encabezados de columna (fila 6 en negrilla)
            row = sheet.createRow(rowNum++);
            String[] headers = {"CODIGO", "ARTICULO", "UNIDAD", "COSTO UNIT.", "SALDO FIS.", "SALDO VAL."};
            for (int i = 0; i < headers.length; i++) {
                HSSFCell cell = row.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(boldStyle);
            }

            // Datos
            BigDecimal totalQuantity = BigDecimal.ZERO;
            BigDecimal totalAmount = BigDecimal.ZERO;

            for (CollectionData data : beanCollection) {
                row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(data.getCodeArt());
                row.createCell(1).setCellValue(data.getName());
                row.createCell(2).setCellValue(data.getUnit());
                row.createCell(3).setCellValue(data.getUnitCost().doubleValue());
                row.createCell(4).setCellValue(data.getQuantity().doubleValue());
                row.createCell(5).setCellValue(data.getAmount().doubleValue());

                totalQuantity = totalQuantity.add(data.getQuantity());
                totalAmount = totalAmount.add(data.getAmount());
            }

            // Fila de totales en negrilla
            row = sheet.createRow(rowNum);
            HSSFCell totalLabel = row.createCell(1);
            totalLabel.setCellValue("TOTALES:");
            totalLabel.setCellStyle(boldStyle);

            HSSFCell totalQtyCell = row.createCell(4);
            totalQtyCell.setCellValue(totalQuantity.doubleValue());
            totalQtyCell.setCellStyle(boldNumberStyle);

            HSSFCell totalAmtCell = row.createCell(5);
            totalAmtCell.setCellValue(totalAmount.doubleValue());
            totalAmtCell.setCellStyle(boldNumberStyle);

            // Escribir respuesta
            HttpServletResponse response = JSFUtil.getHttpServletResponse();
            response.setContentType("application/vnd.ms-excel");
            response.setHeader("Content-Disposition", "attachment; filename=ReporteInventarioValorado.xls");
            workbook.write(response.getOutputStream());
            response.getOutputStream().flush();
            response.getOutputStream().close();
            FacesContext.getCurrentInstance().responseComplete();

        } catch (Exception e) {
            e.printStackTrace();
        }
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

        System.out.println("----> start: " + startDate);
        System.out.println("----> end: " + endDate);

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
            inventory.setCompanyNumber(Constants.COD_COMPANY_DEFAULT);
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