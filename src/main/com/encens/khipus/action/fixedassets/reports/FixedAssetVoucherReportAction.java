package com.encens.khipus.action.fixedassets.reports;

import com.encens.khipus.action.reports.GenericReportAction;
import com.encens.khipus.action.reports.PageFormat;
import com.encens.khipus.action.reports.PageOrientation;
import com.encens.khipus.model.admin.BusinessUnit;
import com.encens.khipus.model.employees.Employee;
import com.encens.khipus.model.finances.CostCenter;
import com.encens.khipus.model.fixedassets.*;
import com.encens.khipus.util.DateUtils;
import com.encens.khipus.util.MessageUtils;
import com.jatun.titus.reportgenerator.util.TypedReportData;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.security.Restrict;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Action to generate fixed asset voucher report
 *
 * @author
 * @version 3.2
 */
@Name("fixedAssetVoucherReportAction")
@Scope(ScopeType.PAGE)
@Restrict("#{s:hasPermission('FIXEDASSETVOUCHERREPORT','VIEW')}")
public class FixedAssetVoucherReportAction extends GenericReportAction {

    private String voucherCode;
    private FixedAssetVoucherState state;
    private FixedAssetMovementType fixedAssetVoucherType;
    private String cause;
    private FixedAsset fixedAsset;
    private FixedAssetGroup fixedAssetGroup;
    private FixedAssetSubGroup fixedAssetSubGroup;
    private BusinessUnit businessUnit;
    private CostCenter costCenter;
    private Employee employee;
    private Date initMovementDate;
    private Date endMovementDate;

    public void generateReport() {

        log.debug("Generate FixedAssetVoucherReportAction......");

        Map params = new HashMap();
        params.putAll(readReportHeaderParamsInfo());

        //add sub reports
        addFixedAssetMovementCommonSubReport(params);
        addPurchaseOrderFixedAssetPartSubReport(params);

        super.generateReport("fixedAssetVoucherReport", "/fixedassets/reports/fixedAssetVoucherReport.jrxml",
                PageFormat.LEGAL,
                PageOrientation.LANDSCAPE,
                MessageUtils.getMessage("Reports.fixedAssetVoucher.title"),
                params);
    }

    @Override
    protected String getEjbql() {
        return "SELECT DISTINCT " +
                "fixedAssetVoucher.id, " +
                "fixedAssetVoucher.voucherCode, " +
                "fixedAssetVoucherType.movementCode, " +
                "fixedAssetVoucherType.description, " +
                "fixedAssetVoucher.cause, " +
                "fixedAssetVoucher.movementDate, " +
                "fixedAssetVoucher.state, " +
                "businessUnit.executorUnitCode," +
                "businessUnit.publicity," +
                "costCenter.code, " +
                "costCenter.description, " +
                "employee.lastName," +
                "employee.maidenName," +
                "employee.firstName," +
                "purchaseOrder.orderNumber," +
                "fixedAssetLocation.name, " +
                "purchaseOrder.id, " +
                "purchaseOrderCause " +
                " FROM  FixedAssetVoucher fixedAssetVoucher " +
                "      LEFT JOIN fixedAssetVoucher.fixedAssetVoucherType fixedAssetVoucherType " +
                "      LEFT JOIN fixedAssetVoucher.businessUnit businessUnit " +
                "      LEFT JOIN fixedAssetVoucher.costCenter costCenter " +
                "      LEFT JOIN fixedAssetVoucher.purchaseOrder purchaseOrder " +
                "      LEFT JOIN fixedAssetVoucher.fixedAssetLocation fixedAssetLocation " +
                "      LEFT JOIN fixedAssetVoucher.custodianJobContract custodianJobContract " +
                "      LEFT JOIN custodianJobContract.contract contract " +
                "      LEFT JOIN contract.employee employee " +
                "      LEFT JOIN fixedAssetVoucher.fixedAssetMovementList fixedAssetMovement " +
                "      LEFT JOIN fixedAssetMovement.fixedAsset fixedAsset " +
                "      LEFT JOIN fixedAsset.fixedAssetSubGroup fixedAssetSubGroup" +
                "      LEFT JOIN fixedAssetSubGroup.fixedAssetGroup fixedAssetGroup" +
                "      LEFT JOIN fixedAssetVoucher.purchaseOrder purchaseOrder" +
                "      LEFT JOIN purchaseOrder.purchaseOrderCause purchaseOrderCause";
    }

    @Create
    public void init() {

        restrictions = new String[]{
                "fixedAssetVoucher.voucherCode = #{fixedAssetVoucherReportAction.voucherCode}",
                "fixedAssetVoucher.state = #{fixedAssetVoucherReportAction.state}",
                "fixedAssetVoucherType = #{fixedAssetVoucherReportAction.fixedAssetVoucherType}",
                "fixedAssetVoucher.cause = #{fixedAssetVoucherReportAction.cause}",
                "fixedAsset = #{fixedAssetVoucherReportAction.fixedAsset}",
                "fixedAssetSubGroup = #{fixedAssetVoucherReportAction.fixedAssetSubGroup}",
                "fixedAssetGroup = #{fixedAssetVoucherReportAction.fixedAssetGroup}",
                "businessUnit=#{fixedAssetVoucherReportAction.businessUnit}",
                "costCenter = #{fixedAssetVoucherReportAction.costCenter}",
                "employee = #{fixedAssetVoucherReportAction.employee}",
                "fixedAssetVoucher.movementDate >= #{fixedAssetVoucherReportAction.initMovementDate}",
                "fixedAssetVoucher.movementDate <= #{fixedAssetVoucherReportAction.endMovementDate}"};

        sortProperty = "fixedAssetVoucher.voucherCode";
    }

    /**
     * add fixed asset movement detail sub report in main report
     *
     * @param mainReportParams main report Map params
     */
    private void addFixedAssetMovementCommonSubReport(Map mainReportParams) {
        log.debug("Generating addFixedAssetMovementCommonSubReport.............................");
        Map<String, Object> params = new HashMap<String, Object>();

        String ejbql = "SELECT " +
                "fixedAssetGroup.groupCode," +
                "fixedAssetGroup.description," +
                "fixedAssetSubGroup.fixedAssetSubGroupCode," +
                "fixedAssetSubGroup.description," +
                "fixedAsset.barCode," +
                "fixedAsset.detail," +
                "fixedAsset.measurement," +
                "fixedAsset.description," +
                "fixedAsset.trademark," +
                "fixedAsset.model," +
                "fixedAsset.duration," +
                "fixedAsset.ufvOriginalValue," +
                "fixedAsset.currencyType, " +
                "fixedAsset.sequence, " +
                "fixedAsset.registrationDate " +
                " FROM FixedAssetMovement fixedAssetMovement" +
                " LEFT JOIN fixedAssetMovement.fixedAsset fixedAsset" +
                " LEFT JOIN fixedAsset.fixedAssetSubGroup fixedAssetSubGroup" +
                " LEFT JOIN fixedAssetSubGroup.fixedAssetGroup fixedAssetGroup" +
                " WHERE fixedAssetMovement.fixedAssetVoucher.id=$P{fixedAssetVoucherIdParam}";

        String[] restrictions = new String[]{};

        String orderBy = "fixedAssetGroup.groupCode";

        //generate the sub report
        String subReportKey = "FIXEDASSETVOUCHERMOVCOMMONSUBREPORT";
        TypedReportData subReportData = super.generateSubReport(
                subReportKey,
                "/fixedassets/reports/fixedAssetVoucherMovementSubReport.jrxml",
                PageFormat.LETTER,
                PageOrientation.LANDSCAPE,
                createQueryForSubreport(subReportKey, ejbql, Arrays.asList(restrictions), orderBy),
                params);

        //add in main report params
        mainReportParams.putAll(subReportData.getReportParams());
        mainReportParams.put(subReportKey, subReportData.getJasperReport());
    }

    private void addPurchaseOrderFixedAssetPartSubReport(Map<String, Object> mainReportParams) {
        log.debug("Generating addPurchaseOrderFixedAssetPartSubReport.............................!!!!!!!");
        Map<String, Object> params = new HashMap<String, Object>();

        // execute purchaseOrderFixedAssetPartSubReport
        String subReportKey = "PURCHASEORDERFIXEDASSETPARTSUBREPORT";

        String ejbql = "SELECT " +
                "fixedAsset.barCode," +
                "fixedAsset.detail," +
                "fixedAssetPart.description, " +
                "measureUnit.measureUnitCode, " +
                "measureUnit.name, " +
                "fixedAssetPart.serialNumber, " +
                "fixedAssetPart.unitPrice " +
                " FROM PurchaseOrderFixedAssetPart fixedAssetPart" +
                " LEFT JOIN fixedAssetPart.measureUnit measureUnit" +
                " LEFT JOIN fixedAssetPart.fixedAsset fixedAsset" +
                " LEFT JOIN fixedAssetPart.purchaseOrder purchaseOrder" +
                " WHERE purchaseOrder.id=$P{purchaseOrderIdParam}";

        String[] restrictions = new String[]{};

        String orderBy = "fixedAsset.barCode, fixedAssetPart.description";

        //generate the sub report
        TypedReportData subReportData = super.generateSubReport(
                subReportKey,
                "/fixedassets/reports/purchaseOrderFixedAssetPartSubReport.jrxml",
                PageFormat.LETTER,
                PageOrientation.LANDSCAPE,
                createQueryForSubreport(subReportKey, ejbql, Arrays.asList(restrictions), orderBy),
                params);

        //add in main report params
        mainReportParams.putAll(subReportData.getReportParams());
        mainReportParams.put(subReportKey, subReportData.getJasperReport());
    }

    /**
     * Read report header fields and define as params
     *
     * @return Map
     */
    private Map readReportHeaderParamsInfo() {
        Map headerParamMap = new HashMap();
        String filtersInfo = "";

        if (initMovementDate != null) {
            filtersInfo = filtersInfo + MessageUtils.getMessage("Common.dateFrom") + " " + DateUtils.format(initMovementDate, MessageUtils.getMessage("patterns.date")) + " ";
        }

        if (endMovementDate != null) {
            filtersInfo = filtersInfo + MessageUtils.getMessage("Common.dateTo") + " " + DateUtils.format(endMovementDate, MessageUtils.getMessage("patterns.date"));
        }

        headerParamMap.put("filterInfoParam", filtersInfo);
        return headerParamMap;
    }

    public String getVoucherCode() {
        return voucherCode;
    }

    public void setVoucherCode(String voucherCode) {
        this.voucherCode = voucherCode;
    }

    public FixedAssetVoucherState getState() {
        return state;
    }

    public void setState(FixedAssetVoucherState state) {
        this.state = state;
    }

    public FixedAssetMovementType getFixedAssetVoucherType() {
        return fixedAssetVoucherType;
    }

    public void setFixedAssetVoucherType(FixedAssetMovementType fixedAssetVoucherType) {
        this.fixedAssetVoucherType = fixedAssetVoucherType;
    }

    public String getCause() {
        return cause;
    }

    public void setCause(String cause) {
        this.cause = cause;
    }

    public FixedAsset getFixedAsset() {
        return fixedAsset;
    }

    public void setFixedAsset(FixedAsset fixedAsset) {
        this.fixedAsset = fixedAsset;
    }

    public void clearFixedAsset() {
        setFixedAsset(null);
    }

    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employeeItem) {
        this.employee = employeeItem;
    }

    public String clearEmployee() {
        setEmployee(null);
        return null;
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

    public CostCenter getCostCenter() {
        return costCenter;
    }

    public void setCostCenter(CostCenter costCenter) {
        this.costCenter = costCenter;
    }

    public String getCostCenterFullName() {
        return getCostCenter() != null ? getCostCenter().getFullName() : null;
    }

    public void assignCostCenter(CostCenter costCenter) {
        this.costCenter = costCenter;
    }

    public void clearCostCenter() {
        setCostCenter(null);
    }

    public BusinessUnit getBusinessUnit() {
        return businessUnit;
    }

    public void setBusinessUnit(BusinessUnit businessUnit) {
        this.businessUnit = businessUnit;
    }

    public Date getInitMovementDate() {
        return initMovementDate;
    }

    public void setInitMovementDate(Date initMovementDate) {
        this.initMovementDate = initMovementDate;
    }

    public Date getEndMovementDate() {
        return endMovementDate;
    }

    public void setEndMovementDate(Date endMovementDate) {
        this.endMovementDate = endMovementDate;
    }
}
