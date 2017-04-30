package com.encens.khipus.action.fixedassets;

import com.encens.khipus.action.reports.GenericReportAction;
import com.encens.khipus.action.reports.PageFormat;
import com.encens.khipus.action.reports.PageOrientation;
import com.encens.khipus.model.admin.BusinessUnit;
import com.encens.khipus.model.employees.Currency;
import com.encens.khipus.model.employees.Employee;
import com.encens.khipus.model.finances.CostCenter;
import com.encens.khipus.model.fixedassets.FixedAssetMaintenanceReceiptState;
import com.encens.khipus.model.fixedassets.FixedAssetMaintenanceRequestState;
import com.encens.khipus.model.fixedassets.FixedAssetMaintenanceRequestType;
import com.encens.khipus.util.ValidatorUtil;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;

/**
 * @author
 */
@Name("fixedAssetMaintenanceRequestReportAction")
@Scope(ScopeType.PAGE)
public class FixedAssetMaintenanceRequestReportAction extends GenericReportAction {
    private String maintenanceRequestCode;
    private Date requestStartDate;
    private Date requestEndDate;
    private Date receiptStartDate;
    private Date receiptEndDate;
    private Date estimatedReceiptStartDate;
    private Date estimatedReceiptEndDate;
    private Employee petitioner;
    private String historyStateDescription;
    private CostCenter costCenter;
    private Date historyStartDate;
    private Date historyEndDate;
    private BusinessUnit executorUnit;
    private FixedAssetMaintenanceReceiptState maintenanceReceiptState;
    private FixedAssetMaintenanceRequestState requestState;
    private BigDecimal startAmount;
    private BigDecimal endAmount;
    private FixedAssetMaintenanceRequestType requestType;
    private Currency currency;
    private String maintenanceReason;

    private FixedAssetMaintenanceRequestState approvedState = FixedAssetMaintenanceRequestState.APPROVED;
    private FixedAssetMaintenanceRequestState rejectedState = FixedAssetMaintenanceRequestState.REJECTED;

    @Create
    public void init() {
        restrictions = new String[]{
                "fixedAssetMaintenanceRequest.code=#{fixedAssetMaintenanceRequestReportAction.maintenanceRequestCode}",
                "fixedAssetMaintenanceRequest.requestState=#{fixedAssetMaintenanceRequestReportAction.requestState}",
                "fixedAssetMaintenanceRequest.requestDate>=#{fixedAssetMaintenanceRequestReportAction.requestStartDate}",
                "fixedAssetMaintenanceRequest.requestDate<=#{fixedAssetMaintenanceRequestReportAction.requestEndDate}",
                "fixedAssetMaintenanceRequest.type=#{fixedAssetMaintenanceRequestReportAction.requestType}",
                "maintenance.receiptDate>=#{fixedAssetMaintenanceRequestReportAction.receiptStartDate}",
                "maintenance.receiptDate<=#{fixedAssetMaintenanceRequestReportAction.receiptEndDate}",
                "maintenance.estimatedReceiptDate>=#{fixedAssetMaintenanceRequestReportAction.estimatedReceiptStartDate}",
                "maintenance.estimatedReceiptDate<=#{fixedAssetMaintenanceRequestReportAction.estimatedReceiptEndDate}",
                "employee=#{fixedAssetMaintenanceRequestReportAction.petitioner}",
                "costCenter=#{fixedAssetMaintenanceRequestReportAction.costCenter}",
                "maintenanceRequestStateHistory.date>=#{fixedAssetMaintenanceRequestReportAction.historyStartDate}",
                "maintenanceRequestStateHistory.date<=#{fixedAssetMaintenanceRequestReportAction.historyEndDate}",
                "businessUnit=#{fixedAssetMaintenanceRequestReportAction.executorUnit}",
                "maintenance.receiptState=#{fixedAssetMaintenanceRequestReportAction.maintenanceReceiptState}",
                "maintenance.amount>=#{fixedAssetMaintenanceRequestReportAction.startAmount}",
                "maintenance.amount<=#{fixedAssetMaintenanceRequestReportAction.endAmount}",
                "currency=#{fixedAssetMaintenanceRequestReportAction.currency}",
                "lower(maintenanceRequestStateHistory.description.value) like concat(lower(#{fixedAssetMaintenanceRequestReportAction.historyStateDescription}),'%')",
                "lower(fixedAssetMaintenanceRequest.maintenanceReason.value) like concat(lower(#{fixedAssetMaintenanceRequestReportAction.maintenanceReason}),'%')"
        };
        sortProperty = "fixedAssetMaintenanceRequest.code";
    }

    protected String getEjbql() {
        String query = "SELECT " +
                "fixedAssetMaintenanceRequest.code," +
                "businessUnit.executorUnitCode," +
                "organization.name," +
                "costCenter.code," +
                "costCenter.description," +
                "employee.lastName," +
                "employee.maidenName," +
                "employee.firstName," +
                "charge.name," +
                "maintenanceReason.value," +
                "fixedAssetMaintenanceRequest.requestDate," +
                "fixedAssetMaintenanceRequest.type," +
                "fixedAssetMaintenanceRequest.requestState," +
                "maintenance.deliveryDate," +
                "deliveryDescription.value," +
                "maintenance.receiptDate," +
                "receiptDescription.value," +
                "receiptState.name," +
                "maintenance.state," +
                "maintenance.amount," +
                "currency, " +
                "fixedAssetMaintenanceRequest.id " +
                " FROM FixedAssetMaintenanceRequest fixedAssetMaintenanceRequest" +
                " LEFT JOIN fixedAssetMaintenanceRequest.executorUnit businessUnit" +
                " LEFT JOIN businessUnit.organization organization" +
                " LEFT JOIN fixedAssetMaintenanceRequest.costCenter costCenter" +
                " LEFT JOIN fixedAssetMaintenanceRequest.petitioner jobContract" +
                " LEFT JOIN jobContract.contract contract" +
                " LEFT JOIN contract.employee employee" +
                " LEFT JOIN jobContract.job job" +
                " LEFT JOIN job.charge charge" +
                " LEFT JOIN fixedAssetMaintenanceRequest.maintenanceReason maintenanceReason" +
                " LEFT JOIN fixedAssetMaintenanceRequest.maintenance maintenance" +
                " LEFT JOIN maintenance.deliveryDescription deliveryDescription" +
                " LEFT JOIN maintenance.receiptDescription receiptDescription" +
                " LEFT JOIN maintenance.receiptState receiptState" +
                " LEFT JOIN maintenance.currency currency" +
                " LEFT JOIN fixedAssetMaintenanceRequest.stateHistoryList maintenanceRequestStateHistory";
        if (!ValidatorUtil.isBlankOrNull(getHistoryStateDescription()) ||
                getHistoryStartDate() != null ||
                getHistoryEndDate() != null) {
            query += " WHERE (maintenanceRequestStateHistory.state=#{fixedAssetMaintenanceRequestReportAction.approvedState} OR maintenanceRequestStateHistory.state=#{fixedAssetMaintenanceRequestReportAction.rejectedState}) ";
        }
        return query;
    }

    public void generateReport() {
        log.debug("generating fixedAssetMaintenanceRequestReport......................................");

        HashMap<String, Object> reportParameters = new HashMap<String, Object>();

        super.generateReport(
                "fixedAssetMaintenanceRequestReport",
                "/fixedassets/reports/fixedAssetMaintenanceRequestReport.jrxml",
                PageFormat.CUSTOM,
                PageOrientation.LANDSCAPE,
                messages.get("FixedAssetMaintenanceRequestReport.report.title"),
                reportParameters);
    }

    public String getMaintenanceRequestCode() {
        return maintenanceRequestCode;
    }

    public void setMaintenanceRequestCode(String maintenanceRequestCode) {
        this.maintenanceRequestCode = maintenanceRequestCode;
    }

    public Date getRequestStartDate() {
        return requestStartDate;
    }

    public void setRequestStartDate(Date requestStartDate) {
        this.requestStartDate = requestStartDate;
    }

    public Date getRequestEndDate() {
        return requestEndDate;
    }

    public void setRequestEndDate(Date requestEndDate) {
        this.requestEndDate = requestEndDate;
    }

    public Date getReceiptStartDate() {
        return receiptStartDate;
    }

    public void setReceiptStartDate(Date receiptStartDate) {
        this.receiptStartDate = receiptStartDate;
    }

    public Date getReceiptEndDate() {
        return receiptEndDate;
    }

    public void setReceiptEndDate(Date receiptEndDate) {
        this.receiptEndDate = receiptEndDate;
    }

    public Date getEstimatedReceiptStartDate() {
        return estimatedReceiptStartDate;
    }

    public void setEstimatedReceiptStartDate(Date estimatedReceiptStartDate) {
        this.estimatedReceiptStartDate = estimatedReceiptStartDate;
    }

    public Date getEstimatedReceiptEndDate() {
        return estimatedReceiptEndDate;
    }

    public void setEstimatedReceiptEndDate(Date estimatedReceiptEndDate) {
        this.estimatedReceiptEndDate = estimatedReceiptEndDate;
    }

    public Employee getPetitioner() {
        return petitioner;
    }

    public void setPetitioner(Employee petitioner) {
        this.petitioner = petitioner;
    }

    public void clearPetitioner() {
        this.petitioner = null;
    }

    public void assignPetitioner(Employee employee) {
        this.petitioner = employee;
    }

    public String getHistoryStateDescription() {
        return historyStateDescription;
    }

    public void setHistoryStateDescription(String historyStateDescription) {
        this.historyStateDescription = historyStateDescription;
    }

    public FixedAssetMaintenanceRequestState getApprovedState() {
        return approvedState;
    }

    public FixedAssetMaintenanceRequestState getRejectedState() {
        return rejectedState;
    }

    public CostCenter getCostCenter() {
        return costCenter;
    }

    public void setCostCenter(CostCenter costCenter) {
        this.costCenter = costCenter;
    }

    public void assignCostCenter(CostCenter costCenter) {
        this.costCenter = costCenter;
    }

    public void clearCostCenter() {
        this.costCenter = null;
    }

    public Date getHistoryStartDate() {
        return historyStartDate;
    }

    public void setHistoryStartDate(Date historyStartDate) {
        this.historyStartDate = historyStartDate;
    }

    public Date getHistoryEndDate() {
        return historyEndDate;
    }

    public void setHistoryEndDate(Date historyEndDate) {
        this.historyEndDate = historyEndDate;
    }

    public BusinessUnit getExecutorUnit() {
        return executorUnit;
    }

    public void setExecutorUnit(BusinessUnit executorUnit) {
        this.executorUnit = executorUnit;
    }

    public FixedAssetMaintenanceReceiptState getMaintenanceReceiptState() {
        return maintenanceReceiptState;
    }

    public void setMaintenanceReceiptState(FixedAssetMaintenanceReceiptState maintenanceReceiptState) {
        this.maintenanceReceiptState = maintenanceReceiptState;
    }

    public FixedAssetMaintenanceRequestState getRequestState() {
        return requestState;
    }

    public void setRequestState(FixedAssetMaintenanceRequestState requestState) {
        this.requestState = requestState;
    }

    public BigDecimal getStartAmount() {
        return startAmount;
    }

    public void setStartAmount(BigDecimal startAmount) {
        this.startAmount = startAmount;
    }

    public BigDecimal getEndAmount() {
        return endAmount;
    }

    public void setEndAmount(BigDecimal endAmount) {
        this.endAmount = endAmount;
    }

    public FixedAssetMaintenanceRequestType getRequestType() {
        return requestType;
    }

    public void setRequestType(FixedAssetMaintenanceRequestType requestType) {
        this.requestType = requestType;
    }

    public Currency getCurrency() {
        return currency;
    }

    public void setCurrency(Currency currency) {
        this.currency = currency;
    }

    public String getMaintenanceReason() {
        return maintenanceReason;
    }

    public void setMaintenanceReason(String maintenanceReason) {
        this.maintenanceReason = maintenanceReason;
    }
}
