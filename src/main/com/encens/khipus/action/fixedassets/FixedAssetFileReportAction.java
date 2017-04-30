package com.encens.khipus.action.fixedassets;

import com.encens.khipus.action.reports.GenericReportAction;
import com.encens.khipus.action.reports.PageFormat;
import com.encens.khipus.action.reports.PageOrientation;
import com.encens.khipus.model.admin.BusinessUnit;
import com.encens.khipus.model.employees.Employee;
import com.encens.khipus.model.finances.CostCenter;
import com.encens.khipus.model.fixedassets.FixedAssetGroup;
import com.encens.khipus.model.fixedassets.FixedAssetLocation;
import com.encens.khipus.model.fixedassets.FixedAssetMovementState;
import com.encens.khipus.model.fixedassets.FixedAssetSubGroup;
import com.jatun.titus.reportgenerator.util.TypedReportData;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Encens S.R.L.
 * This class implements the fixed asset file report action, this contains the main report and two subreports:
 * FixedAssetMovementsSubReport and FixedAsseteDepreciationsSubReport
 *
 * @author
 * @version 2.2
 */
@Name("fixedAssetFileReportAction")
@Scope(ScopeType.PAGE)
public class FixedAssetFileReportAction extends GenericReportAction {
    private FixedAssetMovementState fixedAssetMovementStateApproved = FixedAssetMovementState.APR;
    private Employee employee;
    private String executorUnitCode;
    private BusinessUnit businessUnit;
    private CostCenter costCenter;
    private Long fixedAssetCode;
    private String barCode;
    private FixedAssetGroup fixedAssetGroup;
    private FixedAssetSubGroup fixedAssetSubGroup;
    private FixedAssetLocation fixedAssetLocation;

    private Date initRegistrationDate;
    private Date endRegistrationDate;

    private Date initEndDate;
    private Date endEndDate;

    @Create
    public void init() {
        restrictions = new String[]{"custodian=#{fixedAssetFileReportAction.employee}",
                "lower(businessUnit.executorUnitCode) like concat(lower(#{fixedAssetFileReportAction.executorUnitCode}),'%')",
                "businessUnit=#{fixedAssetFileReportAction.businessUnit}",
                "lower(fixedAsset.costCenterCode) like concat(lower(#{fixedAssetFileReportAction.costCenter.code}),'%')",
                "fixedAssetGroup=#{fixedAssetFileReportAction.fixedAssetGroup}",
                "fixedAssetSubGroup=#{fixedAssetFileReportAction.fixedAssetSubGroup}",
                "fixedAssetLocation=#{fixedAssetFileReportAction.fixedAssetLocation}",
                "fixedAsset.fixedAssetCode = #{fixedAssetFileReportAction.fixedAssetCode}",
                "lower(fixedAsset.barCode) like concat('%',concat(lower(#{fixedAssetFileReportAction.barCode}),'%'))",
                "fixedAsset.registrationDate>=#{fixedAssetFileReportAction.initRegistrationDate}",
                "fixedAsset.registrationDate<=#{fixedAssetFileReportAction.endRegistrationDate}",
                "fixedAsset.endDate>=#{fixedAssetFileReportAction.initEndDate}",
                "fixedAsset.endDate<=#{fixedAssetFileReportAction.endEndDate}"};
        sortProperty = "fixedAssetGroup.id, fixedAssetSubGroup.id, fixedAsset.id";
    }

    protected String getEjbql() {
        return "SELECT fixedAsset.id, " +
                "      fixedAsset.description, " +
                "      fixedAsset.registrationDate, " +
                "      fixedAsset.endDate, " +
                "      fixedAsset.depreciationRate, " +
                "      fixedAsset.ufvOriginalValue, " +
                "      fixedAsset.improvement, " +
                "      fixedAsset.acumulatedDepreciation, " +
                "      custodian, " +
                "      businessUnit.executorUnitCode, " +
                "      fixedAsset.costCenterCode, " +
                "      costCenter.description, " +
                "      businessUnit.descriptionBU, " +
                "      (fixedAsset.ufvOriginalValue + fixedAsset.improvement), " +
                "      fixedAsset.trademark, " +
                "      fixedAsset.sequence, " +
                "      fixedAsset.model, " +
                "      fixedAsset.depreciation, " +
                "      fixedAsset.duration, " +
                "      (fixedAsset.ufvOriginalValue + fixedAsset.improvement - fixedAsset.acumulatedDepreciation), " +
                "      purchaseOrder.invoiceNumber, " +
                "      purchaseOrder.orderNumber, " +
                "      fixedAsset.detail, " +
                "      fixedAsset.measurement, " +
                "      provider, " +
                "      fixedAssetLocation.name, " +
                "      organizationalUnit, " +
                "      charge.name, " +
                "      fixedAssetGroup, " +
                "      fixedAssetSubGroup, " +
                "      fixedAsset.monthsGuaranty, " +
                "      warehouseCashAccount.accountCode, " +
                "      fixedAsset.state, " +
                "      fixedAsset.rubbish, " +
                "      fixedAsset.bsSusRate, " +
                "      fixedAsset.bsUfvRate, " +
                "      fixedAsset " +
                "FROM  FixedAsset as fixedAsset LEFT JOIN fixedAsset.costCenter costCenter " +
                "      LEFT JOIN fixedAsset.businessUnit businessUnit " +
                "      LEFT JOIN fixedAsset.fixedAssetSubGroup fixedAssetSubGroup " +
                "      LEFT JOIN fixedAsset.fixedAssetLocation fixedAssetLocation" +
                "      LEFT JOIN fixedAsset.custodianJobContract.contract.employee custodian " +
                "      LEFT JOIN fixedAsset.purchaseOrder purchaseOrder " +
                "      LEFT JOIN fixedAsset.purchaseOrder.provider provider " +
                "      LEFT JOIN fixedAsset.custodianJobContract.job.organizationalUnit organizationalUnit " +
                "      LEFT JOIN fixedAsset.custodianJobContract.job.charge charge " +
                "      LEFT JOIN fixedAssetSubGroup.fixedAssetGroup fixedAssetGroup " +
                "      LEFT JOIN fixedAssetSubGroup.warehouseCashAccount warehouseCashAccount ";
    }

    public void generateReport() {
        log.debug("generating fixedAssetFileReport......................................");
        HashMap<String, Object> reportParameters = new HashMap<String, Object>();
        reportParameters.put("fixedAssetMovementsSubReport", generateMovementsSubReport().getJasperReport());
        reportParameters.put("fixedAssetDepreciationsSubReport", generateDepreciationSubReport().getJasperReport());
        reportParameters.put("movementStateApproved", FixedAssetMovementState.APR);

        addFixedAssetMaintenanceSubReport(reportParameters);
        log.debug("The sub-report was generated...... ");

        super.generateReport(
                "fixedAssetFileReport",
                "/fixedassets/reports/fixedAssetFileReport.jrxml",
                PageFormat.CUSTOM,
                PageOrientation.LANDSCAPE,
                messages.get("FixedAssetFile.report.title"),
                reportParameters);
    }

    private TypedReportData generateMovementsSubReport() {
        HashMap params = new HashMap();
        params.put("movementStateApproved", FixedAssetMovementState.APR);
        TypedReportData reportData = super.generateSubReport(
                "fixedAssetMovementsReport",
                "/fixedassets/reports/fixedAssetMovementsSubReport.jrxml",
                PageFormat.CUSTOM,
                PageOrientation.LANDSCAPE,
                createQueryForSubreport("fixedAssetMovementsReport",
                        movementsSubReportEjbql,
                        Arrays.asList(movementsSubReportRestrictions),
                        movementsSubReportOrder),
                params);
        return (reportData);
    }

    private TypedReportData generateDepreciationSubReport() {
        HashMap params = new HashMap();
        TypedReportData reportData = super.generateSubReport(
                "fixedAssetDepreciationsReport",
                "/fixedassets/reports/fixedAssetDepreciationsSubReport.jrxml",
                PageFormat.CUSTOM,
                PageOrientation.LANDSCAPE,
                createQueryForSubreport("fixedAssetDepreciationsReport",
                        depreciationsSubReportEjbql,
                        Arrays.asList(depreciationsSubReportRestrictions),
                        depreciationsSubReportOrder),
                params);
        return (reportData);
    }

    private String movementsSubReportEjbql =
            "SELECT fixedAssetMovement.id, " +
                    "fixedAssetMovement.movementNumber, " +
                    "fixedAssetMovement.movementDate, " +
                    "fixedAssetMovement.fixedAssetMovementType.description, " +
                    "fixedAssetMovement.cause, " +
                    "fixedAssetMovement.ufvAmount, " +
                    "fixedAssetMovement.userNumber, " +
                    "custodian, " +
                    "lastCustodian, " +
                    "costCenter, " +
                    "lastCostCenter, " +
                    "lastFixedAssetLocation.name, " +
                    "newFixedAssetLocation.name, " +
                    "fixedAssetMovement " +
                    " FROM FixedAssetMovement fixedAssetMovement LEFT JOIN fixedAssetMovement.custodian custodian " +
                    "LEFT JOIN fixedAssetMovement.lastCustodian lastCustodian " +
                    "LEFT JOIN fixedAssetMovement.costCenter costCenter " +
                    "LEFT JOIN fixedAssetMovement.lastCostCenter lastCostCenter " +
                    "LEFT JOIN fixedAssetMovement.lastFixedAssetLocation lastFixedAssetLocation " +
                    "LEFT JOIN fixedAssetMovement.newFixedAssetLocation newFixedAssetLocation " +
                    "LEFT JOIN fixedAssetMovement.businessUnit businessUnit " +
                    "LEFT JOIN fixedAssetMovement.lastBusinessUnit lastBusinessUnit " +
                    " WHERE fixedAssetMovement.fixedAsset=$P{selectedFixedAsset} and fixedAssetMovement.state = $P{movementStateApproved}";

    private String[] movementsSubReportRestrictions = new String[]{};

    private String movementsSubReportOrder = "fixedAssetMovement.movementDate";

    public FixedAssetMovementState getFixedAssetMovementStateApproved() {
        return fixedAssetMovementStateApproved;
    }

    private String depreciationsSubReportEjbql =
            "SELECT depreciationRecord.id, " +
                    "       depreciationRecord.depreciationDate, " +
                    "       fixedAsset.duration, " +
                    "       depreciationRecord.totalValue, " +
                    "       (depreciationRecord.totalValue*depreciationRecord.bsUfvRate), " +
                    "       depreciationRecord.depreciationRate, " +
                    "       depreciationRecord.acumulatedDepreciation, " +
                    "      (fixedAsset.ufvOriginalValue + fixedAsset.improvement - depreciationRecord.acumulatedDepreciation), " +
                    "      depreciationRecord.bsAccumulatedDepreciation, " +
                    "      (fixedAsset.bsOriginalValue + (fixedAsset.improvement * fixedAsset.lastBsUfvRate) - depreciationRecord.bsAccumulatedDepreciation) " +
                    "FROM  FixedAssetDepreciationRecord as depreciationRecord " +
                    "      LEFT JOIN depreciationRecord.fixedAsset fixedAsset " +
                    "WHERE depreciationRecord.fixedAsset=$P{selectedFixedAsset}";

    private String[] depreciationsSubReportRestrictions = new String[]{
    };

    private String depreciationsSubReportOrder = "depreciationRecord.depreciationDate";

    /**
     * Add fixed asset request maintenance sub report
     *
     * @param mainReportParams
     */
    private void addFixedAssetMaintenanceSubReport(Map mainReportParams) {
        log.debug("Generating addFixedAssetMaintanceSubReport.............................");
        String subReportKey = "ROTATORYFUNDMAINTENANCESUBREPORT";

        Map<String, Object> params = new HashMap<String, Object>();

        String ejbql = "SELECT " +
                "fixedAssetMaintenanceRequest.code," +
                "businessUnit.executorUnitCode," +
                "organization.name," +
                "costCenter.code," +
                "costCenter.description," +
                "employee.lastName," +
                "employee.maidenName," +
                "employee.firstName," +
                "charge.name," +
                "fixedAsset.description," +
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
                "maintenance.currency" +
                " FROM FixedAsset fixedAsset" +
                " LEFT JOIN fixedAsset.fixedAssetMaintenanceRequestList fixedAssetMaintenanceRequest" +
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
                " WHERE fixedAsset = $P{selectedFixedAsset} AND fixedAsset.fixedAssetMaintenanceRequestList IS NOT EMPTY";

        String[] restrictions = new String[]{};

        String orderBy = "fixedAssetMaintenanceRequest.code";

        //generate the sub report
        TypedReportData subReportData = super.generateSubReport(
                subReportKey,
                "/fixedassets/reports/fixedAssetMaintenanceReqSubReport.jrxml",
                PageFormat.CUSTOM,
                PageOrientation.LANDSCAPE,
                createQueryForSubreport(subReportKey, ejbql, Arrays.asList(restrictions), orderBy),
                params);

        //add in main report params
        mainReportParams.putAll(subReportData.getReportParams());
        mainReportParams.put(subReportKey, subReportData.getJasperReport());
    }


    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
    }

    public String clearEmployee() {
        setEmployee(null);
        return null;
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

    public String clearCostCenter() {
        setCostCenter(null);
        return null;
    }

    public String getExecutorUnitCode() {
        return executorUnitCode;
    }

    public void setExecutorUnitCode(String executorUnitCode) {
        this.executorUnitCode = executorUnitCode;
    }

    public String getCostCenterFullName() {
        return getCostCenter() != null ? getCostCenter().getFullName() : null;
    }

    public Long getFixedAssetCode() {
        return fixedAssetCode;
    }

    public void setFixedAssetCode(Long fixedAssetCode) {
        this.fixedAssetCode = fixedAssetCode;
    }

    public String getBarCode() {
        return barCode;
    }

    public void setBarCode(String barCode) {
        this.barCode = barCode;
    }

    public FixedAssetGroup getFixedAssetGroup() {
        return fixedAssetGroup;
    }

    public void setFixedAssetGroup(FixedAssetGroup fixedAssetGroup) {
        if (null != fixedAssetGroup) {
            this.fixedAssetGroup = getEntityManager().find(FixedAssetGroup.class, fixedAssetGroup.getId());
            fixedAssetSubGroup = null;
        } else {
            this.fixedAssetGroup = null;
        }
    }

    public FixedAssetSubGroup getFixedAssetSubGroup() {
        return fixedAssetSubGroup;
    }

    public void setFixedAssetSubGroup(FixedAssetSubGroup fixedAssetSubGroup) {
        if (fixedAssetSubGroup != null) {
            this.fixedAssetSubGroup = getEntityManager().find(FixedAssetSubGroup.class, fixedAssetSubGroup.getId());
            this.fixedAssetGroup = getEntityManager().find(FixedAssetGroup.class, this.fixedAssetSubGroup.getFixedAssetGroup().getId());
        } else {
            this.fixedAssetSubGroup = null;
        }
    }

    public void clearFixedAssetGroup() {
        setFixedAssetGroup(null);
        fixedAssetSubGroup = null;
    }

    public void clearFixedAssetSubGroup() {
        setFixedAssetSubGroup(null);
    }

    public Date getEndEndDate() {
        return endEndDate;
    }

    public void setEndEndDate(Date endEndDate) {
        this.endEndDate = endEndDate;
    }

    public Date getInitEndDate() {
        return initEndDate;
    }

    public void setInitEndDate(Date initEndDate) {
        this.initEndDate = initEndDate;
    }

    public Date getEndRegistrationDate() {
        return endRegistrationDate;
    }

    public void setEndRegistrationDate(Date endRegistrationDate) {
        this.endRegistrationDate = endRegistrationDate;
    }

    public Date getInitRegistrationDate() {
        return initRegistrationDate;
    }

    public void setInitRegistrationDate(Date initRegistrationDate) {
        this.initRegistrationDate = initRegistrationDate;
    }

    public BusinessUnit getBusinessUnit() {
        return businessUnit;
    }

    public void setBusinessUnit(BusinessUnit businessUnit) {
        this.businessUnit = businessUnit;
        setExecutorUnitCode(null != businessUnit ? businessUnit.getExecutorUnitCode() : "");
    }

    public FixedAssetLocation getFixedAssetLocation() {
        return fixedAssetLocation;
    }

    public void setFixedAssetLocation(FixedAssetLocation fixedAssetLocation) {
        this.fixedAssetLocation = fixedAssetLocation;
    }
}
