package com.encens.khipus.action.employees.reports;

import com.encens.khipus.action.SessionUser;
import com.encens.khipus.action.reports.GenericReportAction;
import com.encens.khipus.action.reports.ReportFormat;
import com.encens.khipus.exception.EntryNotFoundException;
import com.encens.khipus.framework.service.GenericService;
import com.encens.khipus.model.employees.ConfigurationTaxPayroll;
import com.encens.khipus.model.employees.TaxPayrollGenerated;
import com.encens.khipus.util.BigDecimalUtil;
import com.encens.khipus.util.DateUtils;
import com.encens.khipus.util.MessageUtils;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.security.Restrict;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * Encens S.R.L.
 * Action to generate fiscal payroll report
 *
 * @author
 * @version $Id: PayrollFiscalReportAction.java  03-dic-2010 20:14:59$
 */
@Name("payrollFiscalReportAction")
@Scope(ScopeType.PAGE)
@Restrict("#{s:hasPermission('APPROVETAXPAYROLL','VIEW')}")
public class PayrollFiscalReportAction extends GenericReportAction {

    @In
    private SessionUser sessionUser;
    @In
    private GenericService genericService;

    private TaxPayrollGenerated taxPayrollGenerated;

    public void generateReport(TaxPayrollGenerated taxPayrollGenerated) {
        log.debug("Generate PayrollFiscalReportAction......" + taxPayrollGenerated);

        try {
            taxPayrollGenerated = genericService.findById(TaxPayrollGenerated.class, taxPayrollGenerated.getId());
        } catch (EntryNotFoundException e) {
            log.debug("Not found taxPayrollGenerated....", e);
        }

        //set filter properties
        setTaxPayrollGenerated(taxPayrollGenerated);

        Map params = new HashMap();
        params.putAll(getPayrollInfoParamsMap(taxPayrollGenerated.getConfigurationTaxPayroll()));

        setReportFormat(ReportFormat.XLS);
        super.generateReport("payrollFinancialReport", "/employees/reports/payrollFiscalReport.jrxml", MessageUtils.getMessage("Reports.payrollFinancial.title"), params);
    }

    @Override
    protected String getEjbql() {

        return "SELECT " +
                "fiscalPayroll.number," +
                "fiscalPayroll.personalIdentifier," +
                "fiscalPayroll.name," +
                "fiscalPayroll.nationality," +
                "fiscalPayroll.birthday," +
                "fiscalPayroll.gender," +
                "fiscalPayroll.occupation," +
                "fiscalPayroll.entranceDate," +
                "fiscalPayroll.workedDays," +
                "fiscalPayroll.hourDayPayment," +
                "fiscalPayroll.basicAmount," +
                "fiscalPayroll.seniorityBonus," +
                "fiscalPayroll.extraHour," +
                "fiscalPayroll.extraHourCost," +
                "fiscalPayroll.productionBonus," +
                "fiscalPayroll.sundayBonus," +
                "fiscalPayroll.otherBonus," +
                "fiscalPayroll.totalGrained," +
                "fiscalPayroll.retentionAFP," +
                "fiscalPayroll.retentionClearance," +
                "fiscalPayroll.otherDiscount," +
                "fiscalPayroll.totalDiscount," +
                "fiscalPayroll.liquidPayment" +
                " FROM FiscalPayroll fiscalPayroll";
    }

    @Create
    public void init() {
        restrictions = new String[]{"fiscalPayroll.company=#{currentCompany}",
                "fiscalPayroll.taxPayrollGenerated = #{payrollFiscalReportAction.taxPayrollGenerated}"};

        sortProperty = "fiscalPayroll.number";
    }

    /**
     * get report header info
     *
     * @param configurationTaxPayroll
     * @return Map
     */
    protected Map<String, Object> getPayrollInfoParamsMap(ConfigurationTaxPayroll configurationTaxPayroll) {
        Map<String, Object> payrollInfoMap = new HashMap<String, Object>();

        if (configurationTaxPayroll != null) {
            String bussinesUnitParam = configurationTaxPayroll.getBusinessUnit().getPublicity() + " - " + MessageUtils.getMessage("Reports.payrollFinancial.bolivia");

            String month = MessageUtils.getMessage(configurationTaxPayroll.getMonth().getResourceKey());
            String year = DateUtils.getCurrentYear(configurationTaxPayroll.getStartDate()).toString();
            String subTitle = MessageUtils.getMessage("Reports.payrollFinancial.subTitle", month, year);

            payrollInfoMap.put("bussinesUnitParam", bussinesUnitParam);
            payrollInfoMap.put("subTitleParam", subTitle);
            payrollInfoMap.putAll(getCnsAfpRatesMap(configurationTaxPayroll));
        }

        return payrollInfoMap;
    }

    /**
     * CNS, AFP rates
     *
     * @param configurationTaxPayroll
     * @return Map
     */
    private Map<String, Object> getCnsAfpRatesMap(ConfigurationTaxPayroll configurationTaxPayroll) {
        Map<String, Object> ratesMap = new HashMap<String, Object>();
        ratesMap.put("cnsRateParam", configurationTaxPayroll.getCnsRate().getRate());
        ratesMap.put("afpProfeRiskRateParam", configurationTaxPayroll.getAfpRateProfessionalRisk().getRate());
        ratesMap.put("afpHousingRateParam", configurationTaxPayroll.getAfpRateProHousing().getRate());
        ratesMap.put("afpRateParam", configurationTaxPayroll.getAfpRate().getRate());

        return ratesMap;
    }

    public TaxPayrollGenerated getTaxPayrollGenerated() {
        return taxPayrollGenerated;
    }

    public void setTaxPayrollGenerated(TaxPayrollGenerated taxPayrollGenerated) {
        this.taxPayrollGenerated = taxPayrollGenerated;
    }

    public static BigDecimal calculateRateValue(BigDecimal amount, BigDecimal rate) {
        BigDecimal rateValue = null;
        if (amount != null && rate != null) {
            rateValue = BigDecimalUtil.getPercentage(amount, rate);
        }
        return rateValue;
    }

    public static BigDecimal calculateSumAfpRateValues(BigDecimal amount, BigDecimal afpProfessionalRiskRate, BigDecimal afpHousingRate, BigDecimal afpRate) {
        BigDecimal sumValue = null;
        if (amount != null) {
            sumValue = calculateRateValue(amount, afpProfessionalRiskRate);
            sumValue = sumRateValues(sumValue, calculateRateValue(amount, afpHousingRate));
            sumValue = sumRateValues(sumValue, calculateRateValue(amount, afpRate));
        }
        return sumValue;
    }

    public static BigDecimal calculateSumAllRateValues(BigDecimal amount, BigDecimal afpProfessionalRiskRate, BigDecimal afpHousingRate, BigDecimal afpRate, BigDecimal cnsRate) {
        BigDecimal sumValue = calculateSumAfpRateValues(amount, afpProfessionalRiskRate, afpHousingRate, afpRate);
        if (amount != null) {
            sumValue = sumRateValues(sumValue, calculateRateValue(amount, cnsRate));
        }
        return sumValue;
    }

    private static BigDecimal sumRateValues(BigDecimal value1, BigDecimal value2) {
        BigDecimal sumValue = null;
        if (value1 != null && value2 != null) {
            sumValue = BigDecimalUtil.sum(value1, value2);
        } else if (value1 != null) {
            sumValue = value1;
        } else if (value2 != null) {
            sumValue = value2;
        }
        return sumValue;
    }
}
