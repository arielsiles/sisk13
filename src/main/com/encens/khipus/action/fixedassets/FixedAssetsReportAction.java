package com.encens.khipus.action.fixedassets;

import com.encens.khipus.action.reports.GenericReportAction;
import com.encens.khipus.action.reports.PageFormat;
import com.encens.khipus.action.reports.PageOrientation;
import com.encens.khipus.exception.finances.CompanyConfigurationNotFoundException;
import com.encens.khipus.model.admin.BusinessUnit;
import com.encens.khipus.model.employees.Employee;
import com.encens.khipus.model.finances.CompanyConfiguration;
import com.encens.khipus.model.finances.CostCenter;
import com.encens.khipus.model.fixedassets.FixedAssetGroup;
import com.encens.khipus.model.fixedassets.FixedAssetLocation;
import com.encens.khipus.model.fixedassets.FixedAssetState;
import com.encens.khipus.model.fixedassets.FixedAssetSubGroup;
import com.encens.khipus.service.fixedassets.CompanyConfigurationService;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;

/**
 * Encens S.R.L.
 * This class implements the fixed assets report action
 *
 * @author
 * @version 2.0.2
 */
@Name("fixedAssetsReportAction")
@Scope(ScopeType.PAGE)
public class FixedAssetsReportAction extends GenericReportAction {
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

    private FixedAssetState stateDefault = FixedAssetState.BAJ;

    @In
    private CompanyConfigurationService companyConfigurationService;
    @In
    private FacesMessages facesMessages;

    @Create
    public void init() {
        restrictions = new String[]{"custodian=#{fixedAssetsReportAction.employee}",
                "lower(businessUnit.executorUnitCode) like concat(lower(#{fixedAssetsReportAction.executorUnitCode}),'%')",
                "businessUnit=#{fixedAssetsReportAction.businessUnit}",
                "lower(fixedAsset.costCenterCode) like concat(lower(#{fixedAssetsReportAction.costCenter.code}),'%')",
                "fixedAssetLocation=#{fixedAssetsReportAction.fixedAssetLocation}",
                "fixedAssetGroup=#{fixedAssetsReportAction.fixedAssetGroup}",
                "fixedAssetSubGroup=#{fixedAssetsReportAction.fixedAssetSubGroup}",
                "fixedAsset.fixedAssetCode >= #{fixedAssetsReportAction.initFixedAssetCode}",
                "fixedAsset.fixedAssetCode <= #{fixedAssetsReportAction.endFixedAssetCode}",
                "lower(fixedAsset.barCode) like concat('%',concat(lower(#{fixedAssetsReportAction.barCode}),'%'))",
                "fixedAsset.registrationDate>=#{fixedAssetsReportAction.initRegistrationDate}",
                "fixedAsset.registrationDate<=#{fixedAssetsReportAction.endRegistrationDate}",
                "fixedAsset.ufvOriginalValue>=#{fixedAssetsReportAction.initOriginalValue}",
                "fixedAsset.ufvOriginalValue<=#{fixedAssetsReportAction.endOriginalValue}",
                "fixedAsset.state <> #{fixedAssetsReportAction.stateDefault}"};

        sortProperty = "fixedAssetGroup.id, fixedAssetSubGroup.id, fixedAsset.id";
    }

    protected String getEjbql() {
        return "SELECT " +
                "      fixedAsset.barCode, " +
                "      fixedAsset.description, " +
                "      custodian, " +
                "      businessUnit.executorUnitCode, " +
                "      fixedAsset.costCenterCode, " +
                "      fixedAssetLocation.name, " +
                "      fixedAsset.registrationDate, " +
                "      fixedAsset.endDate, " +
                "      fixedAsset.duration, " +
                "      fixedAsset.depreciationRate, " +
                "      fixedAsset.detail, " +
                "      fixedAsset.ufvOriginalValue, " +
                "      fixedAsset.bsOriginalValue, " +
                "      fixedAsset.improvement, " +
                "      (fixedAsset.improvement * fixedAsset.lastBsUfvRate), " +
                "      fixedAsset.sequence, " +
                "      fixedAsset.trademark, " +
                "      fixedAsset.model, " +
                "      costCenter.description, " +
                "      businessUnit.publicity, " +
                "      fixedAsset.measurement, " +
                "      custodianCharge.name, " +
                "      fixedAsset.id, " +
                "      fixedAssetGroup.id, " +
                "      fixedAssetSubGroup.id, " +
                "      fixedAssetGroup.description, " +
                "      fixedAssetSubGroup.description, " +
                "      fixedAsset.acumulatedDepreciation, " +
                "      fixedAsset.detail, " +
                "      (fixedAsset.bsOriginalValue + fixedAsset.improvement - fixedAsset.acumulatedDepreciation) as balance," +
                "      (fixedAsset.bsOriginalValue + fixedAsset.improvement) as totalBsOriginalValue " +
                "FROM  FixedAsset as fixedAsset" +
                "      LEFT JOIN fixedAsset.businessUnit businessUnit" +
                "      LEFT JOIN fixedAsset.costCenter costCenter" +
                "      LEFT JOIN fixedAsset.fixedAssetLocation fixedAssetLocation" +
                "      LEFT JOIN fixedAsset.fixedAssetSubGroup fixedAssetSubGroup" +
                "      LEFT JOIN fixedAssetSubGroup.fixedAssetGroup fixedAssetGroup" +
                "      LEFT JOIN fixedAsset.custodianJobContract custodianJobContract" +
                "      LEFT JOIN custodianJobContract.contract.employee custodian " +
                "      LEFT JOIN custodianJobContract.job custodianJob" +
                "      LEFT JOIN custodianJob.charge custodianCharge";
    }

    public void generateReport() {
        log.debug("generating fixedAssetsReport......................................");
        CompanyConfiguration companyConfiguration = null;
        try {
            companyConfiguration = companyConfigurationService.findCompanyConfiguration();
        } catch (CompanyConfigurationNotFoundException e) {facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,"CompanyConfiguration.notFound");;}

        HashMap<String, Object> reportParameters = new HashMap<String, Object>();

        reportParameters.put("companyName", companyConfiguration.getCompanyName());
        reportParameters.put("systemName", companyConfiguration.getSystemName());
        reportParameters.put("locationName", companyConfiguration.getLocationName());
        reportParameters.put("endDate", endRegistrationDate);

        super.generateReport(
                "fixedAssetReport",
                "/fixedassets/reports/fixedAssetsReport.jrxml",
                PageFormat.CUSTOM,
                PageOrientation.LANDSCAPE,
                messages.get("Fixedassets.report.fixedAssetsReport"),
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

    public FixedAssetState getStateDefault() {
        return stateDefault;
    }

    public void setStateDefault(FixedAssetState stateDefault) {
        this.stateDefault = stateDefault;
    }
}
