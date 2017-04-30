package com.encens.khipus.action.employees.reports;

import com.encens.khipus.action.reports.GenericReportAction;
import com.encens.khipus.model.admin.BusinessUnit;
import com.encens.khipus.model.employees.Employee;
import com.encens.khipus.model.employees.HoraryBandStateType;
import com.encens.khipus.model.employees.MarkStateType;
import com.encens.khipus.model.finances.CostCenter;
import com.encens.khipus.model.finances.JobContract;
import com.encens.khipus.util.MessageUtils;
import com.encens.khipus.util.ValidatorUtil;
import com.encens.khipus.util.employees.MarkStateReportType;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.security.Restrict;

import java.util.*;

/**
 * @author
 * @version 3.0
 */
@Name("markStateReportAction")
@Scope(ScopeType.PAGE)
@Restrict("#{s:hasPermission('REPORTMARKSTATE','VIEW')}")
public class MarkStateReportAction extends GenericReportAction {

    private Date startDate = new Date();
    private Date endDate = new Date();
    private BusinessUnit businessUnit;
    private CostCenter costCenter;
    private Employee employee;
    private List<JobContract> jobCategoryList;
    private List<HoraryBandStateType> horaryBandStateTypeList = new ArrayList<HoraryBandStateType>();
    private List<MarkStateType> markStateTypeList = new ArrayList<MarkStateType>();
    private List<MarkStateReportType> markStateReportTypeList = null;

    public void generateReport() {
        log.debug("Generate MarkStateReport......");
        putMarkStateValues();
        Map params = new HashMap();
        super.generateReport("markStateReport", "/employees/reports/markStateReport.jrxml", MessageUtils.getMessage("Reports.markState.fileName"), params);
    }

    @Override
    protected String getEjbql() {
        Boolean hasBothValues = !getHoraryBandStateTypeList().isEmpty() && !getMarkStateTypeList().isEmpty();
        Boolean hasAnyValues = !getHoraryBandStateTypeList().isEmpty() || !getMarkStateTypeList().isEmpty();
        return "SELECT " +
                "businessUnit.position," +
                "businessUnit.executorUnitCode," +
                "organization.name," +
                "jobCategory.acronym," +
                "jobCategory.name," +
                "costCenter.code," +
                "costCenter.description," +
                "employee.markCode," +
                "employee.lastName," +
                "employee.maidenName," +
                "employee.firstName," +
                "markState.marTime," +
                "horaryBandState.id," +
                "horaryBandState.date," +
                "horaryBandState.initHour," +
                "horaryBandState.endHour," +
                "horaryBandState.type," +
                "markStateHoraryBandState.minutesDiscount," +
                "markStateHoraryBandState.type" +
                " FROM HoraryBandState horaryBandState " +
                " LEFT JOIN horaryBandState.horaryBandContract horaryBandContract" +
                " LEFT JOIN horaryBandContract.jobContract jobContract" +
                " LEFT JOIN jobContract.job job" +
                " LEFT JOIN job.jobCategory jobCategory" +
                " LEFT JOIN horaryBandState.businessUnit businessUnit" +
                " LEFT JOIN businessUnit.organization organization" +
                " LEFT JOIN horaryBandState.costCenter costCenter" +
                " LEFT JOIN horaryBandState.employee employee" +
                " LEFT JOIN horaryBandState.organizationalUnit organizationalUnit" +
                " LEFT JOIN horaryBandState.markStateHoraryBandStateList markStateHoraryBandState " +
                " LEFT JOIN markStateHoraryBandState.markState markState" +
                (hasAnyValues ?
                        " WHERE " +
                                (hasBothValues ? "(" : "") +
                                (!getHoraryBandStateTypeList().isEmpty() ? "horaryBandState.type in (#{markStateReportAction.horaryBandStateTypeList})" : "") +
                                (hasBothValues ? " or " : "") +
                                (!getMarkStateTypeList().isEmpty() ? "markStateHoraryBandState.type in (#{markStateReportAction.markStateTypeList})" : "") +
                                (hasBothValues ? ")" : "") : "");
    }

    @Create
    public void init() {
        restrictions = new String[]{"horaryBandState.company=#{currentCompany}",
                "horaryBandState.date>=#{markStateReportAction.startDate}",
                "horaryBandState.date<=#{markStateReportAction.endDate}",
                "businessUnit=#{markStateReportAction.businessUnit}",
                "costCenter=#{markStateReportAction.costCenter}",
                "employee=#{markStateReportAction.employee}",
                "jobCategory in (#{markStateReportAction.jobCategoryList})"};

        sortProperty = "businessUnit.position, jobCategory.name, costCenter.code, horaryBandState.date, employee.lastName, employee.maidenName, employee.firstName, markState.marTime, horaryBandState.initHour, horaryBandState.endHour";
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

    public BusinessUnit getBusinessUnit() {
        return businessUnit;
    }

    public void setBusinessUnit(BusinessUnit businessUnit) {
        this.businessUnit = businessUnit;
    }

    public CostCenter getCostCenter() {
        return costCenter;
    }

    public void setCostCenter(CostCenter costCenter) {
        this.costCenter = costCenter;
    }

    public void clearCostCenter() {
        setCostCenter(null);
    }

    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
    }

    public void clearEmployee() {
        setEmployee(null);
    }

    public List<JobContract> getJobCategoryList() {
        if (jobCategoryList != null && jobCategoryList.isEmpty()) {
            jobCategoryList = null;
        }
        return jobCategoryList;
    }

    public void setJobCategoryList(List<JobContract> jobCategoryList) {
        this.jobCategoryList = jobCategoryList;
    }

    public List<HoraryBandStateType> getHoraryBandStateTypeList() {
        return horaryBandStateTypeList;
    }

    public void setHoraryBandStateTypeList(List<HoraryBandStateType> horaryBandStateTypeList) {
        this.horaryBandStateTypeList = horaryBandStateTypeList;
    }


    public List<MarkStateType> getMarkStateTypeList() {
        return markStateTypeList;
    }

    public void setMarkStateTypeList(List<MarkStateType> markStateTypeList) {
        this.markStateTypeList = markStateTypeList;
    }

    @SuppressWarnings({"unchecked"})
    public List<MarkStateReportType> getMarkStateReportTypeList() {
        return markStateReportTypeList;
    }

    public void setMarkStateReportTypeList(List<MarkStateReportType> markStateReportTypeList) {
        this.markStateReportTypeList = markStateReportTypeList;
    }

    public void putMarkStateValues() {
        log.info("Execute  putMarkStateValues().....");
        getHoraryBandStateTypeList().clear();
        getMarkStateTypeList().clear();
        if (!ValidatorUtil.isEmptyOrNull(markStateReportTypeList)) {
            for (MarkStateReportType markStateReportType : markStateReportTypeList) {
                if (markStateReportType.getHoraryBandStateType() != null) {
                    if (horaryBandStateTypeList == null) {
                        horaryBandStateTypeList = new ArrayList<HoraryBandStateType>();
                    }
                    horaryBandStateTypeList.add(markStateReportType.getHoraryBandStateType());
                }
                if (markStateReportType.getMarkStateType() != null) {
                    if (markStateTypeList == null) {
                        markStateTypeList = new ArrayList<MarkStateType>();
                    }
                    markStateTypeList.add(markStateReportType.getMarkStateType());
                }
            }
        }
        log.info("horaryBandStateTypeList = " + horaryBandStateTypeList);
        log.info("markStateTypeList = " + markStateTypeList);
    }
}
