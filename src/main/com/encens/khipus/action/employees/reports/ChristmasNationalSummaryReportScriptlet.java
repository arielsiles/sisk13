package com.encens.khipus.action.employees.reports;

import com.encens.khipus.service.employees.ChristmasSummaryReportService;
import com.encens.khipus.util.BigDecimalUtil;
import com.encens.khipus.util.Constants;
import net.sf.jasperreports.engine.JRDefaultScriptlet;
import net.sf.jasperreports.engine.JRScriptletException;
import org.jboss.seam.Component;
import org.jboss.seam.log.Log;
import org.jboss.seam.log.Logging;

import java.math.BigDecimal;

/**
 * Scriptlet to calculate summary values from christmas payroll to national level
 *
 * @author
 * @version 3.2
 */
public class ChristmasNationalSummaryReportScriptlet extends JRDefaultScriptlet {
    private Log log = Logging.getLog(ChristmasNationalSummaryReportScriptlet.class);
    private ChristmasSummaryReportService christmasSummaryReportService;
    private boolean isNewGroupInit;

    public ChristmasNationalSummaryReportScriptlet() {
        christmasSummaryReportService = (ChristmasSummaryReportService) Component.getInstance("christmasSummaryReportService");
        isNewGroupInit = false;
    }

    @Override
    public void afterGroupInit(String groupName) throws JRScriptletException {
        super.afterGroupInit(groupName);

        String sedeGroupName = "sedeGroup";

        if (groupName.equals(sedeGroupName)) {
            isNewGroupInit = true;

            Long generatedPayrollId = getFieldAsLong("generatedPayroll.id");
            BigDecimal exchangeRate = getFieldAsBigDecimal("exchangeRate.rate");

            BigDecimal liquidBsBankAccountBs = calculateLiquidWithBankAccountPaymentType(generatedPayrollId, Constants.currencyIdBs);
            BigDecimal liquidBsBankAccountSus = calculateLiquidWithBankAccountPaymentType(generatedPayrollId, Constants.currencyIdSus);
            BigDecimal liquidBsCheck = calculateLiquidWithCheckPaymentType(generatedPayrollId);

            BigDecimal totalLiquidBs = BigDecimalUtil.sum(liquidBsBankAccountBs, liquidBsBankAccountSus, liquidBsCheck);
            BigDecimal totalLiquidSus = BigDecimalUtil.divide(totalLiquidBs, exchangeRate);

            this.setVariableValue("liquidBsBankAccountBsVar", liquidBsBankAccountBs);
            this.setVariableValue("liquidBsBankAccountSusVar", liquidBsBankAccountSus);
            this.setVariableValue("liquidBsCheckVar", liquidBsCheck);
            this.setVariableValue("totalLiquidBsVar", totalLiquidBs);
            this.setVariableValue("totalLiquidSusVar", totalLiquidSus);
        }
    }

    public void beforeDetailEval() throws JRScriptletException {
        super.beforeDetailEval();

        Long generatedPayrollId = getFieldAsLong("generatedPayroll.id");
        String costCenterCode = (String) this.getFieldValue("costCenter.code");

        Long nroPersonVar = calculateNroPersonWithCostCenter(generatedPayrollId, costCenterCode);

        BigDecimal incomeBs = calculateLiquidWithCostCenter(generatedPayrollId, costCenterCode);
        BigDecimal discountBs = BigDecimal.ZERO;
        BigDecimal liquidBs = BigDecimalUtil.subtract(incomeBs, discountBs);

        //verify if is first row in the group
        boolean isFirstRow = false;
        if (isNewGroupInit) {
            isFirstRow = true;
            isNewGroupInit = false;
        }

        this.setVariableValue("nroPersonVar", nroPersonVar);
        this.setVariableValue("incomeBsVar", incomeBs);
        this.setVariableValue("liquidBsVar", liquidBs);
        this.setVariableValue("discountBsVar", discountBs);
        this.setVariableValue("isFirstRowVar", Boolean.valueOf(isFirstRow));
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


    private Long calculateNroPersonWithCostCenter(Long generatedPayrollId, String costCenterCode) {
        Long countResult = christmasSummaryReportService.countByCostCenter(generatedPayrollId, costCenterCode);
        return (countResult != null ? countResult : Long.valueOf(0));
    }

    private BigDecimal calculateLiquidWithCostCenter(Long generatedPayrollId, String costCenterCode) {
        BigDecimal sumResult = christmasSummaryReportService.calculateLiquidByCostCenter(generatedPayrollId, costCenterCode);
        return (sumResult != null ? sumResult : BigDecimal.ZERO);
    }

    private BigDecimal calculateLiquidWithBankAccountPaymentType(Long generatedPayrollId, Long currencyId) {
        BigDecimal sumResult = christmasSummaryReportService.calculateLiquidWithBankAccountPaymentTypeCurrency(generatedPayrollId, currencyId);
        return (sumResult != null ? sumResult : BigDecimal.ZERO);
    }

    private BigDecimal calculateLiquidWithCheckPaymentType(Long generatedPayrollId) {
        BigDecimal sumResult = christmasSummaryReportService.calculateLiquidWithCheckPaymentType(generatedPayrollId);
        return (sumResult != null ? sumResult : BigDecimal.ZERO);
    }
}
