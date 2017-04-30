package com.encens.khipus.action.employees.reports;

import com.encens.khipus.service.employees.PayrollSummaryReportService;
import com.encens.khipus.util.BigDecimalUtil;
import com.encens.khipus.util.Constants;
import net.sf.jasperreports.engine.JRDefaultScriptlet;
import net.sf.jasperreports.engine.JRScriptletException;

import java.math.BigDecimal;

/**
 * Encens S.R.L.
 * Scriptlet to calculate summary report values
 *
 * @author
 * @version $Id: PayrollSummaryBySedeSubReportScriplet.java  28-ene-2010 15:20:31$
 */
public class PayrollSummaryBySedeSubReportScriptlet extends JRDefaultScriptlet {

    public void beforeDetailEval() throws JRScriptletException {
        super.beforeDetailEval();

        PayrollSummaryReportService payrollSummaryReportService = (PayrollSummaryReportService) this.getParameterValue("SUMMARYSERVICE_PARAM");

        Long generatedPayrollId = getFieldAsLong("generatedPayroll.id");
        Long jobCategoryId = getFieldAsLong("jobCategory.id");
        BigDecimal exchangeRate = getFieldAsBigDecimal("exchangeRate.rate");

        //calculate values
        BigDecimal sumLiquidBankAccountBs = calculateLiquidWithBankAccountPaymentType(generatedPayrollId, jobCategoryId, Constants.currencyIdBs, payrollSummaryReportService);
        BigDecimal sumLiquidBankAccountDollar = BigDecimalUtil.divide(calculateLiquidWithBankAccountPaymentType(generatedPayrollId, jobCategoryId, Constants.currencyIdSus, payrollSummaryReportService), exchangeRate);
        BigDecimal sumLiquidCheckBs = calculateLiquidWithCheckPaymentType(generatedPayrollId, jobCategoryId, payrollSummaryReportService);

        BigDecimal sumLiquidBankAccountBsAsDollar = BigDecimalUtil.divide(sumLiquidBankAccountBs, exchangeRate);
        BigDecimal sumLiquidCheckAsDollar = BigDecimalUtil.divide(sumLiquidCheckBs, exchangeRate);
        BigDecimal totalInDollars = BigDecimalUtil.sum(sumLiquidBankAccountBsAsDollar, sumLiquidCheckAsDollar, sumLiquidBankAccountDollar);
        
        //set in report variables
        String checkPaymentVarName = "checkPaymentVar";
        String bankAccountPaymentVarName = "bankAccountPaymentVar";
        String bankAccountPaymentSusVarName = "bankAccountPaymentSusVar";
        String totalInDollarVarName = "totalInDollarVar";

        this.setVariableValue(checkPaymentVarName, sumLiquidCheckBs);
        this.setVariableValue(bankAccountPaymentVarName, sumLiquidBankAccountBs);
        this.setVariableValue(bankAccountPaymentSusVarName, sumLiquidBankAccountDollar);
        this.setVariableValue(totalInDollarVarName, totalInDollars);
    }

    private Long getFieldAsLong(String fieldName) throws JRScriptletException {
        Long longValue = null;
        Object fieldObj = this.getFieldValue(fieldName);
        if (fieldObj != null && fieldObj.toString().length() > 0) {
            longValue = new Long(fieldObj.toString());
        }
        return longValue;
    }

    private BigDecimal getFieldAsBigDecimal(String fieldName) throws JRScriptletException {
        BigDecimal bigDecimalValue = null;
        Object fieldObj = this.getFieldValue(fieldName);
        if (fieldObj != null && fieldObj.toString().length() > 0) {
            bigDecimalValue = new BigDecimal(fieldObj.toString());
        }
        return bigDecimalValue;
    }

    /**
     * calculate sum of liquid field for this generated pay roll with
     * check payment type
     * @param generatedPayrollId
     * @param jobCategoryId
     * @param payrollSummaryReportService
     * @return BigDecimal
     */
    private BigDecimal calculateLiquidWithCheckPaymentType(Long generatedPayrollId, Long jobCategoryId, PayrollSummaryReportService payrollSummaryReportService) {
        BigDecimal sumResult = payrollSummaryReportService.calculateLiquidWithCheckPaymentType(generatedPayrollId, jobCategoryId);
        return (sumResult != null ? sumResult : BigDecimal.ZERO);
    }

    /**
     * calculate sum of liquid field for this generated pay roll with
     * bank account payment type
     * @param generatedPayrollId
     * @param jobCategoryId
     * @param payrollSummaryReportService
     * @return BigDecimal
     */
    private BigDecimal calculateLiquidWithBankAccountPaymentType(Long generatedPayrollId, Long jobCategoryId, Long currencyId, PayrollSummaryReportService payrollSummaryReportService) {
        BigDecimal sumResult = payrollSummaryReportService.calculateLiquidWithBankAccountPaymentTypeCurrency(generatedPayrollId, jobCategoryId, currencyId);
        return (sumResult != null ? sumResult : BigDecimal.ZERO);
    }
}
