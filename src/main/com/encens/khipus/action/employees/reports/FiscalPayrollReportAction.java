package com.encens.khipus.action.employees.reports;

import com.encens.khipus.action.reports.GenericReportAction;
import com.encens.khipus.action.reports.ReportFormat;
import com.encens.khipus.model.employees.PayrollGenerationCycle;
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
 * Action to generate global fiscal payroll report
 *
 * @author
 * @version 3.4
 */
@Name("fiscalPayrollReportAction")
@Scope(ScopeType.PAGE)
@Restrict("#{s:hasPermission('PAYROLLGENERATIONCYCLE','VIEW')}")
public class FiscalPayrollReportAction extends GenericReportAction {

    private PayrollGenerationCycle payrollGenerationCycle;

    public void generateReport(PayrollGenerationCycle payrollGenerationCycle) {
        log.debug("Generate FiscalPayrollReportAction......" + payrollGenerationCycle);

        //set filter properties
        setPayrollGenerationCycle(payrollGenerationCycle);

        Map params = new HashMap();
        params.putAll(getReportParamsInfo(payrollGenerationCycle));

        setReportFormat(ReportFormat.XLS);
        super.generateReport("fiscalPayrollReport", "/employees/reports/fiscalPayrollReport.jrxml", MessageUtils.getMessage("Reports.fiscalPayroll.title"), params);
    }

    @Override
    protected String getEjbql() {

        return "SELECT " +
                "fiscalPayroll.number," +
                "fiscalPayroll.personalIdentifier," +
                "contract.pensionFundRegistrationCode," +
                "fiscalPayroll.name," +
                "pensionFundOrganization.name," +
                "costCenter.code," +
                "fiscalPayroll.nationality," +
                "fiscalPayroll.birthday," +
                "fiscalPayroll.gender," +
                "fiscalPayroll.occupation," +
                "fiscalPayroll.newnessType," +
                "fiscalPayroll.entranceDate," +
                "fiscalPayroll.workedDays," +
                "fiscalPayroll.paidDays," +
                "fiscalPayroll.hourDayPayment," +
                "fiscalPayroll.basicAmount," +
                "fiscalPayroll.seniorityYears," +
                "fiscalPayroll.seniorityBonus," +
                "fiscalPayroll.extraHour," +
                "fiscalPayroll.extraHourCost," +
                "fiscalPayroll.productionBonus," +
                "fiscalPayroll.sundayBonus," +
                "fiscalPayroll.otherBonus," +
                "fiscalPayroll.totalGrained," +
                "fiscalPayroll.absenceMinutesDiscount," +
                "fiscalPayroll.tardinessMinutesDiscount," +
                "fiscalPayroll.loanDiscount," +
                "fiscalPayroll.advanceDiscount," +
                "fiscalPayroll.winDiscount," +
                "fiscalPayroll.retentionAFP," +
                "fiscalPayroll.retentionClearance," +
                "fiscalPayroll.otherDiscount," +
                "fiscalPayroll.totalDiscount," +
                "fiscalPayroll.liquidPayment" +
                " FROM FiscalPayroll fiscalPayroll" +
                " LEFT JOIN fiscalPayroll.payrollGenerationCycle payrollGenerationCycle" +
                " LEFT JOIN fiscalPayroll.jobContract jobContract" +
                " LEFT JOIN jobContract.contract contract" +
                " LEFT JOIN contract.pensionFundOrganization pensionFundOrganization" +
                " LEFT JOIN jobContract.job job" +
                " LEFT JOIN job.organizationalUnit organizationalUnit" +
                " LEFT JOIN organizationalUnit.costCenter costCenter";
    }

    @Create
    public void init() {
        restrictions = new String[]{"fiscalPayroll.company=#{currentCompany}",
                "payrollGenerationCycle = #{fiscalPayrollReportAction.payrollGenerationCycle}"};

        sortProperty = "fiscalPayroll.number";
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

    public PayrollGenerationCycle getPayrollGenerationCycle() {
        return payrollGenerationCycle;
    }

    public void setPayrollGenerationCycle(PayrollGenerationCycle payrollGenerationCycle) {
        this.payrollGenerationCycle = payrollGenerationCycle;
    }
}
