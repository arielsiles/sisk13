package com.encens.khipus.action.warehouse.reports;

import com.encens.khipus.action.reports.GenericReportAction;
import com.encens.khipus.exception.finances.CompanyConfigurationNotFoundException;
import com.encens.khipus.model.finances.CompanyConfiguration;
import com.encens.khipus.model.warehouse.Group;
import com.encens.khipus.model.warehouse.Warehouse;
import com.encens.khipus.service.accouting.VoucherAccoutingService;
import com.encens.khipus.service.fixedassets.CompanyConfigurationService;
import com.encens.khipus.service.warehouse.WarehouseService;
import com.encens.khipus.util.BigDecimalUtil;
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
@Name("warehouseValuedPhysicalReportAction")
@Scope(ScopeType.PAGE)
public class WarehouseValuedPhysicalReportAction extends GenericReportAction {

    private Date startDate;
    private Date endDate;
    private Warehouse warehouse;
    private Group group;

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


    public void generateReport() {

        log.debug("generating Product Inventory Report................................................");
        CompanyConfiguration companyConfiguration = null;
        try {
            companyConfiguration = companyConfigurationService.findCompanyConfiguration();
        } catch (CompanyConfigurationNotFoundException e) {facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,"CompanyConfiguration.notFound");;}

        String groupName = "";
        if(group != null){
            groupName = " - " + group.getName();
        }

        Collection<CollectionData> beanCollection = calculateValuedInventory();
        HashMap parameters = new HashMap();
        Map<String, Object> paramMap = new HashMap<String, Object>();
        paramMap.put("reportTitle", "REPORTE DE ALMACEN - ENTRADAS Y SALIDAS");
        paramMap.put("companyName", companyConfiguration.getCompanyName());
        paramMap.put("systemName", companyConfiguration.getSystemName());
        paramMap.put("locationName", companyConfiguration.getLocationName());
        paramMap.put("startDate", startDate);
        paramMap.put("endDate", endDate);
        paramMap.put("cashAccount", this.warehouse.getWarehouseCashAccount().getFullName() + groupName);
        parameters.putAll(paramMap);

        try{
            File jasper = new File(JSFUtil.getRealPath("/warehouse/reports/valuedPhysicalReport.jasper")); /* Edited with iReport 3.7.4 */
            JasperPrint jasperPrint = JasperFillManager.fillReport(jasper.getPath(), parameters, new JRBeanCollectionDataSource(beanCollection));
            exportarPDF(jasperPrint);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public Collection<CollectionData> calculateValuedInventory(){

        Collection<CollectionData> beanCollection = new ArrayList();
        List<Object[]> valuedInventory = voucherAccoutingService.getWarehouseValuedPhysical(startDate, endDate, this.warehouse.getWarehouseCashAccount(), group.getGroupCode());

        for ( Object[] value : valuedInventory){
            String codArt = (String)value[0];
            String name = (String)value[1];
            String unit = (String)value[2];
            BigDecimal debit = (BigDecimal)value[3];
            BigDecimal credit = (BigDecimal)value[4];
            BigDecimal input = (BigDecimal)value[5];
            BigDecimal output = (BigDecimal)value[6];

            System.out.println("=====> COD_art: " + codArt + " - " + name + " - " + input + " - " + output);
            BigDecimal physicalBalance = BigDecimalUtil.subtract(input, output, 2);
            BigDecimal valuedBalance   = BigDecimalUtil.subtract(debit, credit, 2);

            BigDecimal unitCost = BigDecimal.ZERO;
            if (physicalBalance.doubleValue() != 0)
                unitCost = BigDecimalUtil.divide(valuedBalance, physicalBalance, 4);

            CollectionData data = new CollectionData(codArt, name, unit, physicalBalance, valuedBalance, unitCost, input, output, debit, credit);
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

    public Group getGroup() {
        return group;
    }

    public void setGroup(Group group) {
        this.group = group;
    }

    public void cleanGroupField() {
        setGroup(null);
    }

    /**
     * CollectionData class for Product Inventory Report
     */
    public class CollectionData {

        private String codeArt;
        private String name;
        private String unit;

        private BigDecimal quantity;
        private BigDecimal amount;
        private BigDecimal unitCost;

        private BigDecimal quantityInput;
        private BigDecimal quantityOutput;
        private BigDecimal amountInput;
        private BigDecimal amountOutput;

        public CollectionData(String codeArt, String name, String unit, BigDecimal quantity, BigDecimal amount, BigDecimal unitCost) {
            setCodeArt(codeArt);
            setName(name);
            setUnit(unit);

            setQuantity(quantity);
            setAmount(amount);
            setUnitCost(unitCost);
        }

        public CollectionData(String codeArt, String name, String unit,BigDecimal quantityInput, BigDecimal quantityOutput, BigDecimal amountInput, BigDecimal amountOutput) {

        }

        public CollectionData(String codeArt, String name, String unit, BigDecimal quantity, BigDecimal amount, BigDecimal unitCost,
                              BigDecimal quantityInput, BigDecimal quantityOutput, BigDecimal amountInput, BigDecimal amountOutput) {
            setCodeArt(codeArt);
            setName(name);
            setUnit(unit);

            setQuantity(quantity);
            setAmount(amount);
            setUnitCost(unitCost);

            setQuantityInput(quantityInput);
            setQuantityOutput(quantityOutput);
            setAmountInput(amountInput);
            setAmountOutput(amountOutput);
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

        public String getUnit() {
            return unit;
        }

        public void setUnit(String unit) {
            this.unit = unit;
        }

        public BigDecimal getQuantityInput() {
            return quantityInput;
        }

        public void setQuantityInput(BigDecimal quantityInput) {
            this.quantityInput = quantityInput;
        }

        public BigDecimal getQuantityOutput() {
            return quantityOutput;
        }

        public void setQuantityOutput(BigDecimal quantityOutput) {
            this.quantityOutput = quantityOutput;
        }

        public BigDecimal getAmountInput() {
            return amountInput;
        }

        public void setAmountInput(BigDecimal amountInput) {
            this.amountInput = amountInput;
        }

        public BigDecimal getAmountOutput() {
            return amountOutput;
        }

        public void setAmountOutput(BigDecimal amountOutput) {
            this.amountOutput = amountOutput;
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
    }




}