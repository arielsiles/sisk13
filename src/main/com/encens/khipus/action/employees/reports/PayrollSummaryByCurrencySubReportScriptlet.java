package com.encens.khipus.action.employees.reports;

import com.encens.khipus.service.employees.PayrollSummaryReportService;
import com.encens.khipus.util.BigDecimalUtil;
import com.encens.khipus.util.Constants;
import net.sf.jasperreports.engine.JRDefaultScriptlet;
import net.sf.jasperreports.engine.JRScriptletException;

import java.math.BigDecimal;


/**
 * Encens S.R.L.
 * Scriptlet to calculate payroll summary by currency, default in bs
 *
 * @author
 * @version $Id: PayrollSummaryByCurrencySubReportScriptlet.java  29-ene-2010 14:04:53$
 */
public class PayrollSummaryByCurrencySubReportScriptlet extends JRDefaultScriptlet {

    public void beforeDetailEval() throws JRScriptletException {
        super.beforeDetailEval();

        PayrollSummaryReportService payrollSummaryReportService = (PayrollSummaryReportService) this.getParameterValue("SUMMARYSERVICE_PARAM");

        Long generatedPayrollId = getFieldAsLong("generatedPayroll.id");
        Long jobCategoryId = getFieldAsLong("jobCategory.id");
        BigDecimal exchangeRate = getFieldAsBigDecimal("exchangeRate.rate");

        Long currencyId = (Long) this.getParameterValue("currencyId_Param");

        //calculate values
        BigDecimal sumLiquidBankAccountPaymentType = calculateLiquidWithBankAccountPaymentType(generatedPayrollId, jobCategoryId, currencyId, payrollSummaryReportService);

        if (Constants.currencyIdSus.equals(currencyId)) {
            //apply currency change type
            sumLiquidBankAccountPaymentType = BigDecimalUtil.divide(sumLiquidBankAccountPaymentType, exchangeRate);
        }

        //set in report variables
        String bankAccountPaymentVarName = "bankAccountPaymentVar";

        this.setVariableValue(bankAccountPaymentVarName, sumLiquidBankAccountPaymentType);
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
     * bank account payment type
     * @param generatedPayrollId
     * @param jobCategoryId
     * @param payrollSummaryReportService
     * @param currencyId
     * @return BigDecimal
     */
    private BigDecimal calculateLiquidWithBankAccountPaymentType(Long generatedPayrollId, Long jobCategoryId, Long currencyId, PayrollSummaryReportService payrollSummaryReportService) {
        BigDecimal sumResult = payrollSummaryReportService.calculateLiquidWithBankAccountPaymentTypeCurrency(generatedPayrollId, jobCategoryId,currencyId);
        return (sumResult != null ? sumResult : BigDecimal.ZERO);
    }
}
