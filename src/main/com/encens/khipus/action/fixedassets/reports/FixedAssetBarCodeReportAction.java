package com.encens.khipus.action.fixedassets.reports;

import com.encens.khipus.action.reports.GenericReportAction;
import com.encens.khipus.action.reports.PageFormat;
import com.encens.khipus.action.reports.PageOrientation;
import com.encens.khipus.model.admin.BusinessUnit;
import com.encens.khipus.model.employees.Employee;
import com.encens.khipus.model.finances.CostCenter;
import com.encens.khipus.model.fixedassets.FixedAssetGroup;
import com.encens.khipus.model.fixedassets.FixedAssetLocation;
import com.encens.khipus.model.fixedassets.FixedAssetSubGroup;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;

/**
 * @author
 * @version 3.1
 */
@Name("fixedAssetBarCodeReportAction")
@Scope(ScopeType.PAGE)
public class FixedAssetBarCodeReportAction extends GenericReportAction {

    private Employee employee;
    private CostCenter costCenter;
    private FixedAssetLocation fixedAssetLocation;
    private String executorUnitCode;
    private BusinessUnit businessUnit;
    private FixedAssetGroup fixedAssetGroup;
    private FixedAssetSubGroup fixedAssetSubGroup;
    private Long initFixedAssetCode;
    private Long endFixedAssetCode;
    private String barCode;
    private Date initRegistrationDate;
    private Date endRegistrationDate;

    private BigDecimal initOriginalValue;
    private BigDecimal endOriginalValue;

    @Create
    public void init() {
        restrictions = new String[]{"custodian=#{fixedAssetBarCodeReportAction.employee}",
                "lower(businessUnit.executorUnitCode) like concat(lower(#{fixedAssetBarCodeReportAction.executorUnitCode}),'%')",
                "businessUnit=#{fixedAssetBarCodeReportAction.businessUnit}",
                "costCenter = #{fixedAssetBarCodeReportAction.costCenter}",
                "fixedAssetLocation=#{fixedAssetBarCodeReportAction.fixedAssetLocation}",
                "fixedAssetGroup=#{fixedAssetBarCodeReportAction.fixedAssetGroup}",
                "fixedAssetSubGroup=#{fixedAssetBarCodeReportAction.fixedAssetSubGroup}",
                "fixedAsset.fixedAssetCode >= #{fixedAssetBarCodeReportAction.initFixedAssetCode}",
                "fixedAsset.fixedAssetCode <= #{fixedAssetBarCodeReportAction.endFixedAssetCode}",
                "lower(fixedAsset.barCode) like concat('%',concat(lower(#{fixedAssetBarCodeReportAction.barCode}),'%'))",
                "fixedAsset.registrationDate>=#{fixedAssetBarCodeReportAction.initRegistrationDate}",
                "fixedAsset.registrationDate<=#{fixedAssetBarCodeReportAction.endRegistrationDate}",
                "fixedAsset.ufvOriginalValue>=#{fixedAssetBarCodeReportAction.initOriginalValue}",
                "fixedAsset.ufvOriginalValue<=#{fixedAssetBarCodeReportAction.endOriginalValue}"};

        sortProperty = "fixedAsset.barCode";
    }

    protected String getEjbql() {
        return "SELECT " +
                "      fixedAsset.barCode, " +
                "      fixedAsset.id, " +
                "      fixedAsset.detail " +
                "FROM  FixedAsset as fixedAsset " +
                "      LEFT JOIN fixedAsset.businessUnit businessUnit" +
                "      LEFT JOIN fixedAsset.costCenter costCenter" +
                "      LEFT JOIN fixedAsset.fixedAssetLocation fixedAssetLocation" +
                "      LEFT JOIN fixedAsset.fixedAssetSubGroup fixedAssetSubGroup" +
                "      LEFT JOIN fixedAssetSubGroup.fixedAssetGroup fixedAssetGroup" +
                "      LEFT JOIN fixedAsset.custodianJobContract custodianJobContract" +
                "      LEFT JOIN custodianJobContract.contract.employee custodian " +
                " WHERE fixedAsset.barCode IS NOT NULL ";
    }

    public void generateReport() {
        HashMap<String, Object> reportParameters = new HashMap<String, Object>();
        super.generateReport(
                "fixedAssetBarCodeReport",
                "/fixedassets/reports/fixedAssetBarCodeReport.jrxml",
                PageFormat.LETTER,
                PageOrientation.PORTRAIT,
                messages.get("Fixedassets.report.fixedAssetsBarCodeReport"),
                reportParameters);
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

    public String getExecutorUnitCode() {
        return executorUnitCode;
    }

    public void setExecutorUnitCode(String executorUnitCode) {
        this.executorUnitCode = executorUnitCode;
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

    public Long getInitFixedAssetCode() {
        return initFixedAssetCode;
    }

    public void setInitFixedAssetCode(Long initFixedAssetCode) {
        this.initFixedAssetCode = initFixedAssetCode;
    }

    public Long getEndFixedAssetCode() {
        return endFixedAssetCode;
    }

    public void setEndFixedAssetCode(Long endFixedAssetCode) {
        this.endFixedAssetCode = endFixedAssetCode;
    }

    public String getBarCode() {
        return barCode;
    }

    public void setBarCode(String barCode) {
        this.barCode = barCode;
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

    public Date getInitRegistrationDate() {
        return initRegistrationDate;
    }

    public void setInitRegistrationDate(Date initRegistrationDate) {
        this.initRegistrationDate = initRegistrationDate;
    }

    public Date getEndRegistrationDate() {
        return endRegistrationDate;
    }

    public void setEndRegistrationDate(Date endRegistrationDate) {
        this.endRegistrationDate = endRegistrationDate;
    }

    public BigDecimal getInitOriginalValue() {
        return initOriginalValue;
    }

    public void setInitOriginalValue(BigDecimal initOriginalValue) {
        this.initOriginalValue = initOriginalValue;
    }

    public BigDecimal getEndOriginalValue() {
        return endOriginalValue;
    }

    public void setEndOriginalValue(BigDecimal endOriginalValue) {
        this.endOriginalValue = endOriginalValue;
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
