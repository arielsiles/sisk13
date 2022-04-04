package com.encens.khipus.action.employees.reports;

import com.encens.khipus.action.SessionUser;
import com.encens.khipus.action.reports.GenericReportAction;
import com.encens.khipus.action.reports.ReportFormat;
import com.encens.khipus.exception.EntryNotFoundException;
import com.encens.khipus.exception.finances.CompanyConfigurationNotFoundException;
import com.encens.khipus.framework.service.GenericService;
import com.encens.khipus.model.employees.GeneratedPayroll;
import com.encens.khipus.model.employees.PayrollGenerationCycle;
import com.encens.khipus.model.finances.CompanyConfiguration;
import com.encens.khipus.model.finances.PaymentType;
import com.encens.khipus.service.employees.ManagersPayrollService;
import com.encens.khipus.service.employees.ManagersPayrollSummaryService;
import com.encens.khipus.service.fixedassets.CompanyConfigurationService;
import com.encens.khipus.util.Constants;
import com.encens.khipus.util.DateUtils;
import com.encens.khipus.util.MessageUtils;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.security.Restrict;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Action to generate fiscal payroll by category report
 *
 * @author
 * @version 3.4
 */
@Name("payrollBankUnisueldo2ReportAction")
@Scope(ScopeType.PAGE)
@Restrict("#{s:hasPermission('GENERATEDPAYROLL','VIEW')}")
public class PayrollBankUnisueldo2ReportAction extends GenericReportAction {

    @In
    private SessionUser sessionUser;
    @In
    private GenericService genericService;
    @In
    private ManagersPayrollService managersPayrollService;
    @In
    private ManagersPayrollSummaryService managersPayrollSummaryService;
    @In
    private CompanyConfigurationService companyConfigurationService;

    private GeneratedPayroll generatedPayroll;

    public void generateReport(GeneratedPayroll generatedPayroll) throws CompanyConfigurationNotFoundException {

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
        super.generateReport("fiscalPayrollReport", "/employees/reports/PayrollBankUnisueldo2Report.jrxml", MessageUtils.getMessage("Reports.payrollBankUnisueldo.title"), params);
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
                "categoryFiscalPayroll.liquidPayment," +
                "categoryFiscalPayroll.laborIndividualAFP," +
                "categoryFiscalPayroll.laborCommonRiskAFP," +
                "categoryFiscalPayroll.laborSolidaryContributionAFP," +
                "categoryFiscalPayroll.laborComissionAFP," +
                "categoryFiscalPayroll.nightWorkBonus," +
                "categoryFiscalPayroll.transReturnBonus," +
                "categoryFiscalPayroll.refreshmentBonus," +
                "categoryFiscalPayroll.solidaryAFP," +
                "categoryFiscalPayroll.extension," +
                "categoryFiscalPayroll.employee.id," +
                "categoryFiscalPayroll.employee.paymentType" +
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
                "generatedPayroll = #{payrollBankUnisueldo2ReportAction.generatedPayroll}"};

        sortProperty = "categoryFiscalPayroll.number";
    }

    /**
     * Read report params
     *
     * @return Map
     */
    private Map getReportParamsInfo(PayrollGenerationCycle payrollGenerationCycle) throws CompanyConfigurationNotFoundException {

        CompanyConfiguration companyConfiguration = companyConfigurationService.findCompanyConfiguration();
        String companyNIT = Constants.NIT_COMPANY;
        String dateValue      = DateUtils.format(new Date(), "yyyyMMdd");
        String loadNumber = "1";
        String serviceName  = "UNISUELDO";
        String debitAccount = companyConfiguration.getForeignBankAccountForPayment().getAccountNumber();
        String currency = "1";
        String month        = generatedPayroll.getGestionPayroll().getMonth().getMonthLiteral();
        String year         = DateUtils.getCurrentYear(generatedPayroll.getGestionPayroll().getInitDate()).toString();
        String gloss  = MessageUtils.getMessage("ManagersPayroll.paymentUnisueldo") + " " + month + " " + year;
        String payrollType = "H";
        String email = "juana.pozo@ilvabolivia.com";

        Integer quantity = managersPayrollService.getManagersPayrollToUnisueldo(generatedPayroll).size();
        BigDecimal sumLiquid = managersPayrollSummaryService.sumLiquidByPaymentType(generatedPayroll.getId(), PaymentType.PAYMENT_BANK_ACCOUNT);


        Map paramMap = new HashMap();

        paramMap.put("companyNIT", companyNIT);
        paramMap.put("dateValue", dateValue);
        paramMap.put("loadNumber", loadNumber);
        paramMap.put("serviceName", serviceName);
        paramMap.put("debitAccount", debitAccount);
        paramMap.put("quantity", quantity);
        paramMap.put("sumLiquid", sumLiquid);
        paramMap.put("currency", currency);
        paramMap.put("gloss", gloss);
        paramMap.put("payrollType", payrollType);
        paramMap.put("email", email);

        paramMap.put("bussinesUnitParam", "");
        paramMap.put("subTitleParam", "");
        paramMap.putAll(getColumnHeaderComposedLabelParam(payrollGenerationCycle));

        return paramMap;
    }

    private Map getColumnHeaderComposedLabelParam(PayrollGenerationCycle payrollGenerationCycle) {
        Map paramMap = new HashMap();
        paramMap.put("retentionAFPColumnParam", MessageUtils.getMessage("FiscalPayroll.retentionAFP", payrollGenerationCycle.getAfpRate().getRate()));
        paramMap.put("retentionIvaColumnParam", MessageUtils.getMessage("FiscalPayroll.retentionClearance", payrollGenerationCycle.getIvaRate().getRate()));

        paramMap.put("laborIndividualAFPParam", MessageUtils.getMessage("FiscalPayroll.laborIndividualAFP", payrollGenerationCycle.getLaborIndividualAfpRate().getRate()));
        paramMap.put("laborCommonRiskAFPParam", MessageUtils.getMessage("FiscalPayroll.laborCommonRiskAFP", payrollGenerationCycle.getLaborCommonRiskAfpRate().getRate()));
        paramMap.put("laborSolidaryContributionAFPParam", MessageUtils.getMessage("FiscalPayroll.laborSolidaryContributionAFP", payrollGenerationCycle.getLaborSolidaryContributionAfpRate() .getRate()));
        paramMap.put("laborComissionAFPParam", MessageUtils.getMessage("FiscalPayroll.laborComissionAFP", payrollGenerationCycle.getLaborComissionAfpRate().getRate()));

        return paramMap;
    }

    public GeneratedPayroll getGeneratedPayroll() {
        return generatedPayroll;
    }

    public void setGeneratedPayroll(GeneratedPayroll generatedPayroll) {
        this.generatedPayroll = generatedPayroll;
    }
}
