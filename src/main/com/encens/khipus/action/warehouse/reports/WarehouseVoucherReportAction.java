package com.encens.khipus.action.warehouse.reports;

import com.encens.khipus.action.reports.GenericReportAction;
import com.encens.khipus.action.reports.PageFormat;
import com.encens.khipus.action.reports.PageOrientation;
import com.encens.khipus.model.admin.BusinessUnit;
import com.encens.khipus.model.employees.Employee;
import com.encens.khipus.model.finances.CostCenter;
import com.encens.khipus.model.purchases.PurchaseOrder;
import com.encens.khipus.model.warehouse.Warehouse;
import com.encens.khipus.model.warehouse.WarehouseDocumentType;
import com.encens.khipus.model.warehouse.WarehouseVoucherState;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.security.Restrict;

import java.util.Date;
import java.util.HashMap;

/**
 * @author
 */
@Name("warehouseVoucherReportAction")
@Scope(ScopeType.PAGE)
@Restrict("#{s:hasPermission('WAREHOUSEVOUCHERREPORT','VIEW')}")
public class WarehouseVoucherReportAction extends GenericReportAction {

    private String number;
    private CostCenter targetCostCenter;
    private CostCenter costCenter;
    private Date startDate;
    private Date endDate;
    private BusinessUnit executorUnit;
    private WarehouseVoucherState warehouseVoucherState;
    private Warehouse warehouse;
    private WarehouseDocumentType documentType;
    private Employee responsible;
    private PurchaseOrder purchaseOrder;
    private Employee petitioner;
    private BusinessUnit targetExecutorUnit;
    private Warehouse targetWarehouse;
    private Employee targetResponsible;

    @Override
    protected String getEjbql() {
        return "SELECT warehouseVoucher.id, " +
                "warehouseVoucher.number, " +
                "warehouseVoucher.date, " +
                "warehouseVoucher.state, " +
                "costCenter, " +
                "targetCostCenter, " +
                "warehouse, " +
                "targetWarehouse, " +
                "documentType, " +
                "responsible, " +
                "targetResponsible, " +
                "purchaseOrder, " +
                "executorUnit, " +
                "targetExecutorUnit, " +
                "petitioner, " +
                "parentWarehouseVoucher " +
                "FROM  WarehouseVoucher warehouseVoucher " +
                "      LEFT JOIN warehouseVoucher.costCenter costCenter" +
                "      LEFT JOIN warehouseVoucher.targetCostCenter targetCostCenter " +
                "      LEFT JOIN warehouseVoucher.warehouse warehouse " +
                "      LEFT JOIN warehouseVoucher.targetWarehouse targetWarehouse" +
                "      LEFT JOIN warehouseVoucher.responsible responsible" +
                "      LEFT JOIN warehouseVoucher.targetResponsible targetResponsible" +
                "      LEFT JOIN warehouseVoucher.executorUnit executorUnit" +
                "      LEFT JOIN warehouseVoucher.targetExecutorUnit targetExecutorUnit" +
                "      LEFT JOIN warehouseVoucher.documentType documentType" +
                "      LEFT JOIN warehouseVoucher.purchaseOrder purchaseOrder" +
                "      LEFT JOIN warehouseVoucher.parentWarehouseVoucher parentWarehouseVoucher" +
                "      LEFT JOIN warehouseVoucher.petitionerJobContract petitionerJobContract" +
                "      LEFT JOIN petitionerJobContract.contract contract " +
                "      LEFT JOIN contract.employee petitioner";
    }

    @Create
    public void init() {
        restrictions = new String[]{
                "warehouseVoucher.number=#{warehouseVoucherReportAction.number}",
                "costCenter=#{warehouseVoucherReportAction.costCenter}",
                "warehouseVoucher.date>=#{warehouseVoucherReportAction.startDate}",
                "warehouseVoucher.date<=#{warehouseVoucherReportAction.endDate}",
                "executorUnit = #{warehouseVoucherReportAction.executorUnit}",
                "warehouse = #{warehouseVoucherReportAction.warehouse}",
                "warehouseVoucher.state = #{warehouseVoucherReportAction.warehouseVoucherState}",
                "documentType = #{warehouseVoucherReportAction.documentType}",
                "purchaseOrder = #{warehouseVoucherReportAction.purchaseOrder}",
                "targetCostCenter = #{warehouseVoucherReportAction.targetCostCenter}",
                "petitioner = #{warehouseVoucherReportAction.petitioner}",
                "targetExecutorUnit = #{warehouseVoucherReportAction.targetExecutorUnit}",
                "targetWarehouse = #{warehouseVoucherReportAction.targetWarehouse}",
                "responsible = #{warehouseVoucherReportAction.responsible}",
                "targetResponsible = #{warehouseVoucherReportAction.targetResponsible}"
        };

        sortProperty = "warehouseVoucher.id";
    }

    public void generateReport() {
        log.debug("Generating warehouseVoucher report...................");
        HashMap<String, Object> reportParameters = new HashMap<String, Object>();
        super.generateReport(
                "warehouseVoucherReport",
                "/warehouse/reports/warehouseVoucherReport.jrxml",
                PageFormat.CUSTOM,
                PageOrientation.LANDSCAPE,
                messages.get("WarehouseVoucher.report.title"),
                reportParameters);
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public CostCenter getCostCenter() {
        return costCenter;
    }

    public void setCostCenter(CostCenter costCenter) {
        this.costCenter = costCenter;
    }

    public void clearCostCenter() {
        costCenter = null;
    }

    public void clearWarehouse() {
        this.warehouse = null;
    }

    public void assignCostCenter(CostCenter costCenter) {
        this.costCenter = costCenter;
    }

    public void assignWarehouse(Warehouse warehouse) {
        this.warehouse = warehouse;
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

    public BusinessUnit getExecutorUnit() {
        return executorUnit;
    }

    public void setExecutorUnit(BusinessUnit executorUnit) {
        this.executorUnit = executorUnit;
    }

    public WarehouseVoucherState getWarehouseVoucherState() {
        return warehouseVoucherState;
    }

    public void setWarehouseVoucherState(WarehouseVoucherState warehouseVoucherState) {
        this.warehouseVoucherState = warehouseVoucherState;
    }

    public Warehouse getWarehouse() {
        return warehouse;
    }

    public void setWarehouse(Warehouse warehouse) {
        this.warehouse = warehouse;
    }

    public WarehouseDocumentType getDocumentType() {
        return documentType;
    }

    public void setDocumentType(WarehouseDocumentType documentType) {
        this.documentType = documentType;
    }

    public Employee getResponsible() {
        return responsible;
    }

    public void setResponsible(Employee responsible) {
        this.responsible = responsible;
    }

    public void cleanResponsible() {
        this.responsible = null;
    }

    public PurchaseOrder getPurchaseOrder() {
        return purchaseOrder;
    }

    public void setPurchaseOrder(PurchaseOrder purchaseOrder) {
        this.purchaseOrder = purchaseOrder;
    }

    public void assignPurchaseOrder(PurchaseOrder purchaseOrder) {
        this.purchaseOrder = purchaseOrder;
    }

    public void clearPurchaseOrder() {
        this.purchaseOrder = null;
    }

    public CostCenter getTargetCostCenter() {
        return targetCostCenter;
    }

    public void setTargetCostCenter(CostCenter targetCostCenter) {
        this.targetCostCenter = targetCostCenter;
    }

    public void assignTargetCostCenter(CostCenter targetCostCenter) {
        this.targetCostCenter = targetCostCenter;
    }

    public void clearTargetCostCenter() {
        this.targetCostCenter = null;
    }

    public Employee getPetitioner() {
        return petitioner;
    }

    public void setPetitioner(Employee petitioner) {
        this.petitioner = petitioner;
    }

    public void assignPetitioner(Employee petitioner) {
        this.petitioner = petitioner;
    }

    public void clearPetitioner() {
        this.petitioner = null;
    }

    public BusinessUnit getTargetExecutorUnit() {
        return targetExecutorUnit;
    }

    public void setTargetExecutorUnit(BusinessUnit targetExecutorUnit) {
        this.targetExecutorUnit = targetExecutorUnit;
    }

    public Warehouse getTargetWarehouse() {
        return targetWarehouse;
    }

    public void setTargetWarehouse(Warehouse targetWarehouse) {
        this.targetWarehouse = targetWarehouse;
    }

    public void assignTargetWarehouse(Warehouse warehouse) {
        this.targetWarehouse = warehouse;
    }

    public void clearTargetWarehouse() {
        this.targetWarehouse = null;
    }

    public Employee getTargetResponsible() {
        return targetResponsible;
    }

    public void setTargetResponsible(Employee targetResponsible) {
        this.targetResponsible = targetResponsible;
    }

    public void cleanTargetResponsible() {
        this.targetResponsible = null;
    }
}
