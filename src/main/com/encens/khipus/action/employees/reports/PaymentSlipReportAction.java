package com.encens.khipus.action.employees.reports;

import com.encens.khipus.action.reports.GenericReportAction;
import com.encens.khipus.action.reports.ReportFormat;
import com.encens.khipus.model.employees.PayrollGenerationCycle;
import com.encens.khipus.util.MessageUtils;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.security.Restrict;
import org.joda.time.DateTime;

import java.util.HashMap;
import java.util.Map;

/**
 * Action to generate payment slip report
 *
 * @author
 * @version 3.4
 */
@Name("paymentSlipReportAction")
@Scope(ScopeType.PAGE)
@Restrict("#{s:hasPermission('PAYROLLGENERATIONCYCLE','VIEW')}")
public class PaymentSlipReportAction extends GenericReportAction {

    private PayrollGenerationCycle payrollGenerationCycle;

    public void generateReport(PayrollGenerationCycle payrollGenerationCycle) {
        log.debug("Generate PaymentSlipReportAction......" + payrollGenerationCycle);

        //set filter properties
        setPayrollGenerationCycle(payrollGenerationCycle);

        Map params = new HashMap();
        params.putAll(getReportParamsInfo(payrollGenerationCycle));

        setReportFormat(ReportFormat.PDF);
        super.generateReport("paymentSlipReport", "/employees/reports/paymentSlipReport.jrxml", MessageUtils.getMessage("Reports.paymentSlip.title"), params);
    }

    @Override
    protected String getEjbql() {

        return "SELECT " +
                "fiscalPayroll.id," +
                "fiscalPayroll.personalIdentifier," +
                "fiscalPayroll.name," +
                "organizationalUnit.name," +
                "charge.name," +
                "fiscalPayroll.entranceDate," +
                "fiscalPayroll.workedDays," +
                "fiscalPayroll.seniorityYears," +

                "fiscalPayroll.basicAmount," +
                "fiscalPayroll.seniorityBonus," +
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
                "fiscalPayroll.totalDiscount," +
                "fiscalPayroll.liquidPayment" +

                " FROM FiscalPayroll fiscalPayroll" +
                " LEFT JOIN fiscalPayroll.payrollGenerationCycle payrollGenerationCycle" +
                " LEFT JOIN fiscalPayroll.jobContract jobContract" +
                " LEFT JOIN jobContract.job job" +
                " LEFT JOIN job.organizationalUnit organizationalUnit" +
                " LEFT JOIN job.charge charge";
    }

    @Create
    public void init() {
        restrictions = new String[]{"fiscalPayroll.company=#{currentCompany}",
                "payrollGenerationCycle = #{paymentSlipReportAction.payrollGenerationCycle}"};

        sortProperty = "fiscalPayroll.number";
    }

    /**
     * Read report params
     *
     * @return Map
     */
    private Map getReportParamsInfo(PayrollGenerationCycle payrollGenerationCycle) {
        Map paramMap = new HashMap();

        String monthYear = MessageUtils.getMessage(payrollGenerationCycle.getMonth().getResourceKey()) + ", " + payrollGenerationCycle.getGestion().getYear();

        DateTime dateTime = new DateTime(payrollGenerationCycle.getGestion().getYear(), payrollGenerationCycle.getMonth().getValueAsPosition(), 15, 0, 0, 0, 0);
        DateTime lastDayOfMonthDateTime = dateTime.dayOfMonth().withMaximumValue();

        paramMap.put("monthYearParam", monthYear);
        paramMap.put("lastDayOfMonthParam", lastDayOfMonthDateTime.toDate());

        return paramMap;
    }

    public PayrollGenerationCycle getPayrollGenerationCycle() {
        return payrollGenerationCycle;
    }

    public void setPayrollGenerationCycle(PayrollGenerationCycle payrollGenerationCycle) {
        this.payrollGenerationCycle = payrollGenerationCycle;
    }
}
