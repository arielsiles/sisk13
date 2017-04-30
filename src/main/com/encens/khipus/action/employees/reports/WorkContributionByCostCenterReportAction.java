package com.encens.khipus.action.employees.reports;

import com.encens.khipus.action.SessionUser;
import com.encens.khipus.action.reports.GenericReportAction;
import com.encens.khipus.action.reports.ReportFormat;
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
 * Action to generate work contribution summary by cost center report
 * @author
 * @version 3.4
 */
@Name("workContributionByCostCenterReportAction")
@Scope(ScopeType.PAGE)
@Restrict("#{s:hasPermission('PAYROLLGENERATIONCYCLE','VIEW')}")
public class WorkContributionByCostCenterReportAction   extends GenericReportAction {

    @In
    private SessionUser sessionUser;

    private PayrollGenerationCycle payrollGenerationCycle;

    public void generateReport(PayrollGenerationCycle payrollGenerationCycle) {
        log.debug("Generate WorkContributionByCostCenterReportAction......" + payrollGenerationCycle);

        //set filter properties
        setPayrollGenerationCycle(payrollGenerationCycle);
        String title = MessageUtils.getMessage("Reports.workContributionCosCenter.title");

        Map params = new HashMap();
        params.put("titleParam", title);
        params.putAll(getReportParamsInfo(payrollGenerationCycle));

        setReportFormat(ReportFormat.XLS);
        super.generateReport("workContributionByCostCenterReport", "/employees/reports/workContributionByCostCenterReport.jrxml", title, params);
    }

    @Override
    protected String getEjbql() {

        return "SELECT " +
                "costCenter.code," +
                "costCenter.description," +
                "pensionFundOrganization.id," +
                "pensionFundOrganization.name," +
                "payrollGenerationCycle.id," +

                "SUM(fiscalPayroll.totalGrained)," +
                "SUM(tributaryPayroll.cns)," +
                "SUM(fiscalPayroll.retentionAFP)," +
                "SUM(tributaryPayroll.patronalProffesionalRiskRetentionAFP)," +
                "SUM(tributaryPayroll.patronalProHomeRetentionAFP)," +
                "SUM(tributaryPayroll.patronalSolidaryRetentionAFP)" +
                " FROM FiscalPayroll fiscalPayroll" +
                " LEFT JOIN fiscalPayroll.payrollGenerationCycle payrollGenerationCycle" +
                " LEFT JOIN fiscalPayroll.tributaryPayroll tributaryPayroll" +
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
                "payrollGenerationCycle = #{workContributionByCostCenterReportAction.payrollGenerationCycle}"};

        sortProperty = "costCenter.code, costCenter.description, pensionFundOrganization.name";

        groupByProperty = "costCenter.code," +
                "costCenter.description," +
                "pensionFundOrganization.id, " +
                "pensionFundOrganization.name," +
                "payrollGenerationCycle.id";
    }

    /**
     * Read report params
     *
     * @return Map
     */
    private Map getReportParamsInfo(PayrollGenerationCycle payrollGenerationCycle) {
        Map paramMap = new HashMap();
        String businessUnitParam = payrollGenerationCycle.getBusinessUnit().getPublicity();
        String month = MessageUtils.getMessage(payrollGenerationCycle.getMonth().getResourceKey());
        String year = DateUtils.getCurrentYear(payrollGenerationCycle.getStartDate()).toString();
        String subTitle = MessageUtils.getMessage("Reports.workContributionCosCenter.subTitle", month, year);

        paramMap.put("subTitleParam", subTitle);
        paramMap.put("businessUnitParam", businessUnitParam);
        paramMap.put("cnsRateParam", payrollGenerationCycle.getCnsRate().getRate());
        paramMap.put("afpRateParam", payrollGenerationCycle.getAfpRate().getRate());
        paramMap.put("professionalRiskRateParam", payrollGenerationCycle.getProfessionalRiskAfpRate().getRate());
        paramMap.put("housingAfpRateParam", payrollGenerationCycle.getProHousingAfpRate().getRate());
        paramMap.put("solidaryRateParam", payrollGenerationCycle.getSolidaryAfpRate().getRate());

        return paramMap;
    }

    public PayrollGenerationCycle getPayrollGenerationCycle() {
        return payrollGenerationCycle;
    }

    public void setPayrollGenerationCycle(PayrollGenerationCycle payrollGenerationCycle) {
        this.payrollGenerationCycle = payrollGenerationCycle;
    }
}
