package com.encens.khipus.action.fixedassets;

import com.encens.khipus.action.reports.GenericReportAction;
import com.encens.khipus.action.reports.PageFormat;
import com.encens.khipus.action.reports.PageOrientation;
import com.encens.khipus.model.admin.BusinessUnit;
import com.encens.khipus.model.admin.User;
import com.encens.khipus.model.employees.Employee;
import com.encens.khipus.model.finances.CostCenter;
import com.encens.khipus.model.fixedassets.FixedAssetGroup;
import com.encens.khipus.model.fixedassets.FixedAssetLocation;
import com.encens.khipus.model.fixedassets.FixedAssetSubGroup;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Encens S.R.L.
 * This class implements the fixed asset by custodian report action
 *
 * @author
 * @version 2.2
 */
@Name("fixedAssetByCustodianReportAction")
@Scope(ScopeType.PAGE)
public class FixedAssetByCustodianReportAction extends GenericReportAction {
    @In
    private User currentUser;

    private Employee employee;
    private FixedAssetGroup fixedAssetGroup;
    private FixedAssetSubGroup fixedAssetSubGroup;
    private FixedAssetLocation fixedAssetLocation;
    private Long fixedAssetCode;
    private String barCode;
    private Date initRegistrationDate;
    private Date endRegistrationDate;
    private CostCenter costCenter;
    private BusinessUnit businessUnit;

    @Create
    public void init() {
        restrictions = new String[]{
                "custodian=#{fixedAssetByCustodianReportAction.employee}",
                "businessUnit = #{fixedAssetByCustodianReportAction.businessUnit}",
                "costCenter = #{fixedAssetByCustodianReportAction.costCenter}",
                "fixedAssetGroup=#{fixedAssetByCustodianReportAction.fixedAssetGroup}",
                "fixedAssetSubGroup=#{fixedAssetByCustodianReportAction.fixedAssetSubGroup}",
                "fixedAssetLocation=#{fixedAssetByCustodianReportAction.fixedAssetLocation}",
                "fixedAsset.fixedAssetCode = #{fixedAssetByCustodianReportAction.fixedAssetCode}",
                "lower(fixedAsset.barCode) like concat('%',concat(lower(#{fixedAssetByCustodianReportAction.barCode}),'%'))",
                "fixedAsset.registrationDate>=#{fixedAssetByCustodianReportAction.initRegistrationDate}",
                "fixedAsset.registrationDate<=#{fixedAssetByCustodianReportAction.endRegistrationDate}"};

        sortProperty = "custodian.lastName, custodian.maidenName, custodian.firstName, custodian.id";
    }

    @Override
    protected String getEjbql() {
        return "SELECT " +
                "custodian.id," +
                "custodian.lastName," +
                "custodian.maidenName," +
                "custodian.firstName," +
                "charge.name," +
                "businessUnit.executorUnitCode," +
                "organization.name," +
                "costCenter.code," +
                "costCenter.description," +
                "fixedAsset.id," +
                "fixedAsset.barCode," +
                "fixedAssetGroup.groupCode," +
                "fixedAssetGroup.description," +
                "fixedAssetSubGroup.fixedAssetSubGroupCode," +
                "fixedAssetSubGroup.description," +
                "fixedAsset.detail," +
                "fixedAsset.measurement," +
                "fixedAsset.description," +
                "fixedAsset.trademark," +
                "fixedAsset.model," +
                "fixedAsset.duration," +
                "fixedAsset.ufvOriginalValue," +
                "fixedAsset.currencyType," +
                "fixedAsset.registrationDate, " +
                "fixedAssetLocation.name " +

                " FROM  FixedAsset fixedAsset " +
                " LEFT JOIN fixedAsset.custodianJobContract custodianJobContract" +
                " LEFT JOIN custodianJobContract.contract contract" +
                " LEFT JOIN contract.employee custodian" +
                " LEFT JOIN custodianJobContract.job job" +
                " LEFT JOIN job.charge charge" +
                " LEFT JOIN fixedAsset.businessUnit businessUnit" +
                " LEFT JOIN businessUnit.organization organization" +
                " LEFT JOIN fixedAsset.costCenter costCenter" +
                " LEFT JOIN fixedAsset.fixedAssetLocation fixedAssetLocation " +
                " LEFT JOIN fixedAsset.fixedAssetSubGroup fixedAssetSubGroup " +
                " LEFT JOIN fixedAssetSubGroup.fixedAssetGroup fixedAssetGroup " +
                " WHERE fixedAsset.state <> #{enumerationUtil.getEnumValue('com.encens.khipus.model.fixedassets.FixedAssetState','PEN')}";
    }

    public void generateReport() {
        log.debug("generating fixedAssetByCustodianReport......................................");
        HashMap<String, Object> reportParameters = new HashMap<String, Object>();
        reportParameters.putAll(getReportParams());

        super.generateReport(
                "fixedAssetByCustodianReport",
                "/fixedassets/reports/fixedAssetByCustodianReport.jrxml",
                PageFormat.CUSTOM,
                PageOrientation.LANDSCAPE,
                messages.get("FixedAssetByCustodianReport.report.title"),
                reportParameters);
    }

    private Map<String, Object> getReportParams() {

        Map<String, Object> paramMap = new HashMap<String, Object>();
        paramMap.put("userLoginParam", currentUser.getEmployee().getFullName());
        return paramMap;
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

    public void clearCostCenter() {
        setCostCenter(null);
    }

    public CostCenter getCostCenter() {
        return costCenter;
    }

    public void setCostCenter(CostCenter costCenter) {
        this.costCenter = costCenter;
    }

    public BusinessUnit getBusinessUnit() {
        return businessUnit;
    }

    public void setBusinessUnit(BusinessUnit businessUnit) {
        this.businessUnit = businessUnit;
    }

    public FixedAssetLocation getFixedAssetLocation() {
        return fixedAssetLocation;
    }

    public void setFixedAssetLocation(FixedAssetLocation fixedAssetLocation) {
        this.fixedAssetLocation = fixedAssetLocation;
    }
}
