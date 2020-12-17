package com.encens.khipus.action.employees.reports;

import com.encens.khipus.action.reports.GenericReportAction;
import com.encens.khipus.action.reports.ReportFormat;
import com.encens.khipus.exception.EntryNotFoundException;
import com.encens.khipus.framework.service.GenericService;
import com.encens.khipus.model.employees.GeneratedPayroll;
import com.encens.khipus.model.employees.PayrollGenerationCycle;
import com.encens.khipus.util.MessageUtils;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.security.Restrict;
import org.joda.time.DateTime;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Action to generate payment slip to category fiscal payroll
 * @author
 * @version 3.4
 */
@Name("christmasPaymentSlipReportAction")
@Scope(ScopeType.PAGE)
@Restrict("#{s:hasPermission('GENERATEDPAYROLL','VIEW')}")
public class ChristmasPaymentSlipReportAction extends GenericReportAction {

    @In
    private GenericService genericService;

    private GeneratedPayroll generatedPayroll;


    public void generateReport(GeneratedPayroll generatedPayroll) {
        log.debug("Generate categoryPaymentSlipReportAction......" + generatedPayroll);

        try {
            //get the generated payroll with all relation ship
            generatedPayroll = genericService.findById(GeneratedPayroll.class, generatedPayroll.getId());
        } catch (EntryNotFoundException e) {
            log.debug("Not found GeneratedPayroll....", e);
        }

        //set filter properties
        setGeneratedPayroll(generatedPayroll);

        Map params = new HashMap();
        PayrollGenerationCycle payrollGenerationCycle = generatedPayroll.getPayrollGenerationCycle();
        params.putAll(getReportParamsInfo(payrollGenerationCycle));

        setReportFormat(ReportFormat.PDF);
        super.generateReport("categoryPaymentSlipReport", "/employees/reports/christmasPaymentSlipReport.jrxml", MessageUtils.getMessage("Reports.categoryPaymentSlip.title"), params);
    }

    @Override
    protected String getEjbql() {

        return "SELECT " +
                " " +
                " FROM ChristmasPayroll christmasPayroll" +
                " LEFT JOIN christmasPayroll.generatedPayroll generatedPayroll" +
                " LEFT JOIN christmasPayroll.employee employee" +
                " LEFT JOIN generatedPayroll.charge charge";
    }

    @Create
    public void init() {
        restrictions = new String[]{"categoryFiscalPayroll.company=#{currentCompany}",
                "generatedPayroll = #{christmasPaymentSlipReportAction.generatedPayroll}"};

        //sortProperty = "categoryFiscalPayroll.number";
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
        //paramMap.put("lastDayOfMonthParam", lastDayOfMonthDateTime.toDate());
        paramMap.put("lastDayOfMonthParam", new Date());

        return paramMap;
    }

    public GeneratedPayroll getGeneratedPayroll() {
        return generatedPayroll;
    }

    public void setGeneratedPayroll(GeneratedPayroll generatedPayroll) {
        this.generatedPayroll = generatedPayroll;
    }
}
