package com.encens.khipus.action.warehouse.reports;

import com.encens.khipus.action.reports.GenericReportAction;
import com.encens.khipus.action.reports.PageFormat;
import com.encens.khipus.action.reports.PageOrientation;
import com.encens.khipus.exception.finances.CompanyConfigurationNotFoundException;
import com.encens.khipus.model.finances.CompanyConfiguration;
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
@Name("analyticalInventoryExpenseReportAction")
@Scope(ScopeType.PAGE)
public class AnalyticalInventoryExpenseReportAction extends GenericReportAction {

    private Date startDate;
    private Date endDate;
    private String flagReport = "";

    private final String REPORT_ANALYTIC = "ANALYTIC";
    private final String REPORT_ANALYTIC_PRODUCTION = "ANALYTIC_PRODUCTION";

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

        // Reporte Analitico simple
        String queryA = " SELECT " +
                        " destination.name as area, " +
                        " analyticDetail.name as analytic, " +
                        " count(warehouseVoucher.transactionNumber) as frecuency, " +
                        " sum(movementDetail.amount) as amount " +
                        " FROM MovementDetail movementDetail " +
                        " JOIN movementDetail.inventoryMovement inventoryMovement " +
                        " JOIN inventoryMovement.warehouseVoucher warehouseVoucher " +
                        " JOIN warehouseVoucher.analyticDetail analyticDetail " +
                        " JOIN warehouseVoucher.destination destination " +
                        " WHERE warehouseVoucher.date BETWEEN #{analyticalInventoryExpenseReportAction.startDate} AND #{analyticalInventoryExpenseReportAction.endDate}  " +
                        " AND warehouseVoucher.state = #{analyticalInventoryExpenseReportAction.state} " +
                        " AND warehouseVoucher.documentCode = 'EGR' " +
                        " GROUP BY destination.name, analyticDetail.name " +
                        " ORDER BY destination.name, analyticDetail.name ";

        // Reporte Analitico de Produccion y Mantenimiento
        String queryB = " SELECT " +
                " destination.name as area, " +
                " productionLine.name as line, " +
                " productionProcess.name as process, " +
                " count(warehouseVoucher.transactionNumber) as frecuency, " +
                " sum(movementDetail.amount) as amount " +
                " FROM MovementDetail movementDetail " +
                " JOIN movementDetail.inventoryMovement inventoryMovement " +
                " JOIN inventoryMovement.warehouseVoucher warehouseVoucher " +
                " JOIN warehouseVoucher.destination destination " +
                " JOIN warehouseVoucher.productionProcess productionProcess " +
                " JOIN productionProcess.productionLine productionLine " +
                " WHERE warehouseVoucher.date BETWEEN #{analyticalInventoryExpenseReportAction.startDate} AND #{analyticalInventoryExpenseReportAction.endDate}  " +
                " AND warehouseVoucher.state = #{analyticalInventoryExpenseReportAction.state} " +
                " AND warehouseVoucher.documentCode = 'EGR' " +
                " GROUP BY destination.name, productionLine.name, productionProcess.name " +
                " ORDER BY destination.name, productionLine.name, productionProcess.name ";

        String resultQuery = getFlagReport().equals("ANALYTIC") ? queryA : queryB;

        return resultQuery;
    }

    public void generateAnalyticalProductionReport() {
        log.debug("generateAnalyticalProductionReport................................................");
        setFlagReport(REPORT_ANALYTIC_PRODUCTION);

        CompanyConfiguration companyConfiguration = getCompanyConfiguration();
        String period = DateUtils.format(startDate, "dd/MM/yyyy") + " - " + DateUtils.format(endDate, "dd/MM/yyyy");

        String reportTitle = "REPORTE DE GASTO DE INVENTARIO - ANALITICO DE PRODUCCION";

        HashMap parameters = new HashMap();
        Map<String, Object> paramMap = new HashMap<String, Object>();
        paramMap.put("reportTitle", reportTitle);
        paramMap.put("companyName", companyConfiguration.getCompanyName());
        paramMap.put("systemName", companyConfiguration.getSystemName());
        paramMap.put("locationName", companyConfiguration.getLocationName());

        paramMap.put("startDate", startDate);
        paramMap.put("endDate", endDate);

        super.generateReport(
                "analyticalProducctionInventoryExpenseReport",
                "/warehouse/reports/analyticalProducctionInventoryExpenseReport.jrxml",
                PageFormat.CUSTOM,
                PageOrientation.LANDSCAPE,
                reportTitle,
                paramMap);
    }

    public void generateAnalyticalReport() {
        log.debug("analyticalInventoryExpenseReport................................................");
        setFlagReport(REPORT_ANALYTIC);

        CompanyConfiguration companyConfiguration = getCompanyConfiguration();
        String period = DateUtils.format(startDate, "dd/MM/yyyy") + " - " + DateUtils.format(endDate, "dd/MM/yyyy");

        String reportTitle = "REPORTE DE GASTO DE INVENTARIO - ANALITICO";

        HashMap parameters = new HashMap();
        Map<String, Object> paramMap = new HashMap<String, Object>();
        paramMap.put("reportTitle", reportTitle);
        paramMap.put("companyName", companyConfiguration.getCompanyName());
        paramMap.put("systemName", companyConfiguration.getSystemName());
        paramMap.put("locationName", companyConfiguration.getLocationName());

        paramMap.put("startDate", startDate);
        paramMap.put("endDate", endDate);

        super.generateReport(
                "analyticalInventoryExpenseReport",
                "/warehouse/reports/analyticalInventoryExpenseReport.jrxml",
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

    public WarehouseVoucherState getState() {
        return state;
    }

    public void setState(WarehouseVoucherState state) {
        this.state = state;
    }

    public String getFlagReport() {
        return flagReport;
    }

    public void setFlagReport(String flagReport) {
        this.flagReport = flagReport;
    }
}