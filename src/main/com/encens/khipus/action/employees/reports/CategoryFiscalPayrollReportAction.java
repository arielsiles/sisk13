package com.encens.khipus.action.employees.reports;

import com.encens.khipus.action.SessionUser;
import com.encens.khipus.action.reports.GenericReportAction;
import com.encens.khipus.action.reports.ReportFormat;
import com.encens.khipus.exception.EntryNotFoundException;
import com.encens.khipus.framework.service.GenericService;
import com.encens.khipus.model.employees.GeneratedPayroll;
import com.encens.khipus.model.employees.PayrollGenerationCycle;
import com.encens.khipus.util.DateUtils;
import com.encens.khipus.util.MessageUtils;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.security.Restrict;

import java.util.HashMap;
import java.util.Map;

/**
 * Action to generate fiscal payroll by category report
 *
 * @author
 * @version 3.4
 */
@Name("categoryFiscalPayrollReportAction")
@Scope(ScopeType.PAGE)
@Restrict("#{s:hasPermission('GENERATEDPAYROLL','VIEW')}")
public class CategoryFiscalPayrollReportAction extends GenericReportAction {

    @In
    private SessionUser sessionUser;
    @In
    private GenericService genericService;

    private GeneratedPayroll generatedPayroll;

    public void generateReport(GeneratedPayroll generatedPayroll) {
        log.debug("Generate CategoryFiscalPayrollReportAction......" + generatedPayroll);

        try {
            //get the generated payroll with all relation ship
            generatedPayroll = genericService.findById(GeneratedPayroll.class, generatedPayroll.getId());
        } catch (EntryNotFoundException e) {
            log.debug("Not found GeneratedPayroll....", e);
        }

        PayrollGenerationCycle payrollGenerationCycle = generatedPayroll.getPayrollGenerationCycle();

        //set filter properties
        setGeneratedPayroll(generatedPayroll);

        Map params = new HashMap();
        params.putAll(getReportParamsInfo(payrollGenerationCycle));

        setReportFormat(ReportFormat.XLS);
        super.generateReport("fiscalPayrollReport", "/employees/reports/categoryFiscalPayrollReport.jrxml", MessageUtils.getMessage("Reports.categoryFiscalPayroll.title"), params);
    }

    @Override
    protected String getEjbql() {

        return "SELECT " +
                "categoryFiscalPayroll.number," +
                "categoryFiscalPayroll.personalIdentifier," +
                "contract.pensionFundRegistrationCode," +
                "categoryFiscalPayroll.name," +
                "pensionFundOrganization.name," +
                "costCenter.code," +
                "categoryFiscalPayroll.nationality," +
                "categoryFiscalPayroll.birthday," +
                "categoryFiscalPayroll.gender," +
                "categoryFiscalPayroll.occupation," +
                "categoryFiscalPayroll.newnessType," +
                "categoryFiscalPayroll.entranceDate," +
                "categoryFiscalPayroll.workedDays," +
                "categoryFiscalPayroll.paidDays," +
                "categoryFiscalPayroll.hourDayPayment," +
                "categoryFiscalPayroll.basicAmount," +
                "categoryFiscalPayroll.seniorityYears," +
                "categoryFiscalPayroll.seniorityBonus," +
                "categoryFiscalPayroll.extraHour," +
                "categoryFiscalPayroll.extraHourCost," +
                "categoryFiscalPayroll.productionBonus," +
                "categoryFiscalPayroll.sundayBonus," +
                "categoryFiscalPayroll.otherBonus," +
                "categoryFiscalPayroll.totalGrained," +
                "categoryFiscalPayroll.absenceMinutesDiscount," +
                "categoryFiscalPayroll.tardinessMinutesDiscount," +
                "categoryFiscalPayroll.loanDiscount," +
                "categoryFiscalPayroll.advanceDiscount," +
                "categoryFiscalPayroll.winDiscount," +
                "categoryFiscalPayroll.retentionAFP," +
                "categoryFiscalPayroll.retentionClearance," +
                "categoryFiscalPayroll.otherDiscount," +
                "categoryFiscalPayroll.totalDiscount," +
                "categoryFiscalPayroll.liquidPayment" +
                " FROM CategoryFiscalPayroll categoryFiscalPayroll" +
                " LEFT JOIN categoryFiscalPayroll.generatedPayroll generatedPayroll" +
                " LEFT JOIN categoryFiscalPayroll.jobContract jobContract" +
                " LEFT JOIN jobContract.contract contract" +
                " LEFT JOIN contract.pensionFundOrganization pensionFundOrganization" +
                " LEFT JOIN jobContract.job job" +
                " LEFT JOIN job.organizationalUnit organizationalUnit" +
                " LEFT JOIN organizationalUnit.costCenter costCenter";
    }

    @Create
    public void init() {
        restrictions = new String[]{"categoryFiscalPayroll.company=#{currentCompany}",
                "generatedPayroll = #{categoryFiscalPayrollReportAction.generatedPayroll}"};

        sortProperty = "categoryFiscalPayroll.number";
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
        String subTitle = MessageUtils.getMessage("Reports.categoryFiscalPayroll.subTitle", month, year);

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

    public GeneratedPayroll getGeneratedPayroll() {
        return generatedPayroll;
    }

    public void setGeneratedPayroll(GeneratedPayroll generatedPayroll) {
        this.generatedPayroll = generatedPayroll;
    }
}
