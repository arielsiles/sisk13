package com.encens.khipus.action.employees.reports;

import com.encens.khipus.action.reports.GenericReportAction;
import com.encens.khipus.action.reports.ReportFormat;
import com.encens.khipus.model.employees.Employee;
import com.encens.khipus.model.employees.PayrollGenerationCycle;
import com.encens.khipus.model.employees.VacationPlanning;
import com.encens.khipus.model.employees.VacationState;
import com.encens.khipus.util.DateUtils;
import com.encens.khipus.util.MessageUtils;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.security.Restrict;

import java.util.HashMap;
import java.util.Map;

/**
 * @author
 * @version 3.4
 */
@Name("vacationPlanningEmployeeReportAction")
@Scope(ScopeType.PAGE)
@Restrict("#{s:hasPermission('VACATIONPLANNING','VIEW')}")
public class VacationPlanningEmployeeReportAction extends GenericReportAction {

    private VacationPlanning vacationPlanning;
    private Employee employee;
    private VacationState vacationState = VacationState.APPROVED;

    public void generateReport(VacationPlanning vacationPlanning) {
        //set filter properties
        setVacationPlanning(vacationPlanning);

        //add report format because this is generated from list
        setReportFormat(ReportFormat.PDF);
        generateReport();
    }

    public void generateReport() {
        log.debug("Generate VacationPlanningEmployeeReportAction......");
        Map params = new HashMap();
        super.generateReport("vacationPlanningReport", "/employees/reports/vacationPlanningEmployeeReport.jrxml", MessageUtils.getMessage("Reports.vacationPlanningEmployee.title"), params);
    }


    @Override
    protected String getEjbql() {

        return "SELECT " +
                "vacationPlanning.id," +
                "employee.idNumber," +
                "employee.lastName," +
                "employee.maidenName," +
                "employee.firstName," +
                "vacationPlanning.initDate," +
                "vacationPlanning.seniorityYears," +
                "vacationPlanning.vacationDays," +
                "vacationPlanning.daysOff," +
                "vacationPlanning.daysUsed," +
                "vacationGestion.gestion," +
                "vacationGestion.vacationDays," +
                "vacation.initDate," +
                "vacation.endDate," +
                "vacation.totalDays," +
                "vacation.daysOff," +
                "vacation.description," +
                "businessUnit.publicity," +
                "organizationalUnit.name," +
                "charge.name " +
                " FROM VacationPlanning vacationPlanning" +
                " LEFT JOIN vacationPlanning.vacationGestionList vacationGestion " +
                " LEFT JOIN vacationGestion.vacationList vacation " +
                " LEFT JOIN vacationPlanning.jobContract jobContract " +
                " LEFT JOIN jobContract.job job" +
                " LEFT JOIN job.charge charge" +
                " LEFT JOIN job.organizationalUnit organizationalUnit" +
                " LEFT JOIN organizationalUnit.businessUnit businessUnit" +
                " LEFT JOIN jobContract.contract contract" +
                " LEFT JOIN contract.employee employee" +
                " WHERE (vacation.state = #{vacationPlanningEmployeeReportAction.vacationState} OR vacation.state IS NULL)";
    }

    @Create
    public void init() {
        restrictions = new String[]{
                "vacationPlanning.company = #{currentCompany}",
                "vacationPlanning = #{vacationPlanningEmployeeReportAction.vacationPlanning}",
                "employee = #{vacationPlanningEmployeeReportAction.employee}"};

        sortProperty = "employee.lastName, employee.maidenName, employee.firstName, vacationPlanning.id, vacationGestion.gestion, vacation.initDate";
    }

    /**
     * Read report params
     *
     * @return Map
     */
    private Map getReportParamsInfo(PayrollGenerationCycle payrollGenerationCycle) {
        Map paramMap = new HashMap();

        String bussinesUnitParam = payrollGenerationCycle.getBusinessUnit().getPublicity() + " - " + MessageUtils.getMessage("Reports.fiscalPayroll.bolivia");
        String month = MessageUtils.getMessage(payrollGenerationCycle.getMonth().getResourceKey());
        String year = DateUtils.getCurrentYear(payrollGenerationCycle.getStartDate()).toString();
        String subTitle = MessageUtils.getMessage("Reports.fiscalPayroll.subTitle", month, year);

        paramMap.put("bussinesUnitParam", bussinesUnitParam);
        paramMap.put("subTitleParam", subTitle);
        paramMap.putAll(getColumnHeaderComposedLabelParam(payrollGenerationCycle));

        return paramMap;
    }

    private Map getColumnHeaderComposedLabelParam(PayrollGenerationCycle payrollGenerationCycle) {
        Map paramMap = new HashMap();
        paramMap.put("retentionAFPColumnParam", MessageUtils.getMessage("FiscalPayroll.retentionAFP", payrollGenerationCycle.getAfpRate().getRate()));
        paramMap.put("retentionIvaColumnParam", MessageUtils.getMessage("FiscalPayroll.retentionClearance", payrollGenerationCycle.getIvaRate().getRate()));
        return paramMap;
    }

    public VacationPlanning getVacationPlanning() {
        return vacationPlanning;
    }

    public void setVacationPlanning(VacationPlanning vacationPlanning) {
        this.vacationPlanning = vacationPlanning;
    }

    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
    }

    public VacationState getVacationState() {
        return vacationState;
    }

    public void setVacationState(VacationState vacationState) {
        this.vacationState = vacationState;
    }
}
