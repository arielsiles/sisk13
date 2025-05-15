package com.encens.khipus.action.warehouse.reports;

import com.encens.khipus.action.reports.GenericReportAction;
import com.encens.khipus.action.reports.PageFormat;
import com.encens.khipus.action.reports.PageOrientation;
import com.encens.khipus.exception.finances.CompanyConfigurationNotFoundException;
import com.encens.khipus.model.finances.CompanyConfiguration;
import com.encens.khipus.model.warehouse.Destination;
import com.encens.khipus.model.warehouse.WarehouseDocumentType;
import com.encens.khipus.model.warehouse.WarehouseVoucherState;
import com.encens.khipus.service.fixedassets.CompanyConfigurationService;
import com.encens.khipus.util.DateUtils;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Encens S.R.L.
 * This class implements the purchaseOrder report action
 *
 * @author
 * @version 3.0
 */
@Name("inventoryExpenseSummaryReportAction")
@Scope(ScopeType.PAGE)
public class InventoryExpenseSummaryReportAction extends GenericReportAction {

    private Date startDate;
    private Date endDate;

    private WarehouseDocumentType documentType;

    private Destination destination;

    private WarehouseVoucherState state = WarehouseVoucherState.APR;

    @In
    private CompanyConfigurationService companyConfigurationService;
    @In
    private FacesMessages facesMessages;

    @Create
    public void init() {
        restrictions = new String[]{};
    }


    protected String getEjbql() {

        String queryA = " SELECT " +
                        " destination.code, " +
                        " destination.name, " +
                        " sum(movementDetail.amount) as amount " +
                        " FROM MovementDetail movementDetail " +
                        " JOIN movementDetail.inventoryMovement inventoryMovement " +
                        " JOIN inventoryMovement.warehouseVoucher warehouseVoucher " +
                        " JOIN warehouseVoucher.destination destination " +
                        " WHERE warehouseVoucher.date BETWEEN #{inventoryExpenseSummaryReportAction.startDate} AND #{inventoryExpenseSummaryReportAction.endDate}  " +
                        " AND warehouseVoucher.state = #{inventoryExpenseSummaryReportAction.state} " +
                        " AND warehouseVoucher.documentCode = 'EGR' " +
                        " GROUP BY destination.code, destination.name" +
                        " ORDER BY destination.name ";

        String queryB = " SELECT " +
                        " productItem.productItemCode as code, " +
                        " productItem.name, " +
                        " sum(movementDetail.quantity) as quantity, " +
                        " sum(movementDetail.amount) as amount " +
                        " FROM MovementDetail movementDetail " +
                        " JOIN movementDetail.inventoryMovement inventoryMovement " +
                        " JOIN inventoryMovement.warehouseVoucher warehouseVoucher " +
                        " JOIN warehouseVoucher.destination destination " +
                        " JOIN movementDetail.productItem productItem " +
                        " WHERE warehouseVoucher.date BETWEEN #{inventoryExpenseSummaryReportAction.startDate} AND #{inventoryExpenseSummaryReportAction.endDate}  " +
                        " AND warehouseVoucher.state = #{inventoryExpenseSummaryReportAction.state} " +
                        " AND warehouseVoucher.documentCode = 'EGR' " +
                        " AND destination = #{inventoryExpenseSummaryReportAction.destination} " +
                        " GROUP BY productItem.productItemCode, productItem.name " +
                        " ORDER BY productItem.name ";

        String queryResult = destination == null ? queryA : queryB;

        System.out.println("----> query: " + queryResult);

        return queryResult;
    }

    public void generateReport() {

        if ( destination == null ) {
            log.debug("Inventory Expenses Summary Report................................................");
            generateSummaryReport();
        } else {
            log.debug("Inventory Expenses Detail Report................................................");
            generateDetailReport();
        }

    }

    private void generateDetailReport() {

        CompanyConfiguration companyConfiguration = getCompanyConfiguration();
        String period = DateUtils.format(startDate, "dd/MM/yyyy") + " - " + DateUtils.format(endDate, "dd/MM/yyyy");

        String reportTitle = "DETALLE DE GASTO DE INVENTARIO POR AREA";

        HashMap parameters = new HashMap();
        Map<String, Object> paramMap = new HashMap<String, Object>();
        paramMap.put("reportTitle", reportTitle);
        paramMap.put("companyName", companyConfiguration.getCompanyName());
        paramMap.put("systemName", companyConfiguration.getSystemName());
        paramMap.put("locationName", companyConfiguration.getLocationName());
        paramMap.put("area", destination.getName());

        paramMap.put("startDate", startDate);
        paramMap.put("endDate", endDate);

        super.generateReport(
                "inventoryExpenseSummaryReport",
                "/warehouse/reports/inventoryExpenseDetailReport.jrxml",
                PageFormat.CUSTOM,
                PageOrientation.LANDSCAPE,
                reportTitle,
                paramMap);
    }

    public void generateSummaryReport() {

        CompanyConfiguration companyConfiguration = getCompanyConfiguration();
        String period = DateUtils.format(startDate, "dd/MM/yyyy") + " - " + DateUtils.format(endDate, "dd/MM/yyyy");

        String reportTitle = "RESUMEN DE GASTO DE INVENTARIO POR AREA";

        HashMap parameters = new HashMap();
        Map<String, Object> paramMap = new HashMap<String, Object>();
        paramMap.put("reportTitle", reportTitle);
        paramMap.put("companyName", companyConfiguration.getCompanyName());
        paramMap.put("systemName", companyConfiguration.getSystemName());
        paramMap.put("locationName", companyConfiguration.getLocationName());

        paramMap.put("startDate", startDate);
        paramMap.put("endDate", endDate);

        super.generateReport(
                "inventoryExpenseSummaryReport",
                "/warehouse/reports/inventoryExpenseSummaryReport.jrxml",
                PageFormat.CUSTOM,
                PageOrientation.LANDSCAPE,
                reportTitle,
                paramMap);

    }

    private CompanyConfiguration getCompanyConfiguration(){
        CompanyConfiguration companyConfiguration = null;
        try {
            companyConfiguration = companyConfigurationService.findCompanyConfiguration();
            return companyConfiguration;
        } catch (CompanyConfigurationNotFoundException e) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,"CompanyConfiguration.notFound");
            return null;
        }
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

    public WarehouseDocumentType getDocumentType() {
        return documentType;
    }

    public void setDocumentType(WarehouseDocumentType documentType) {
        this.documentType = documentType;
    }

    public WarehouseVoucherState getState() {
        return state;
    }

    public void setState(WarehouseVoucherState state) {
        this.state = state;
    }

    public Destination getDestination() {
        return destination;
    }

    public void setDestination(Destination destination) {
        this.destination = destination;
    }
}