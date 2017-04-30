package com.encens.khipus.action.employees.reports;

import com.encens.khipus.model.employees.GeneratedPayroll;
import com.encens.khipus.model.employees.Gestion;
import com.encens.khipus.model.employees.JobCategory;
import com.encens.khipus.model.employees.Month;
import com.encens.khipus.model.finances.PaymentType;
import com.encens.khipus.service.employees.ManagersPayrollSummaryService;
import com.encens.khipus.util.BigDecimalUtil;
import com.encens.khipus.util.Constants;
import com.encens.khipus.util.MessageUtils;
import net.sf.jasperreports.engine.JRDefaultScriptlet;
import net.sf.jasperreports.engine.JRScriptletException;
import org.jboss.seam.Component;
import org.jboss.seam.log.Log;
import org.jboss.seam.log.Logging;
import org.joda.time.DateTime;

import java.math.BigDecimal;

/**
 * Scriptlet to calculate summary values for managers payroll summary report
 * @author
 * @version 3.4
 */
public class ManagersPayrollSummaryReportScriptlet extends JRDefaultScriptlet {
    private Log log = Logging.getLog(this.getClass());
    private ManagersPayrollSummaryService managersPayrollSummaryService;
    private boolean isNewGroupInit;

    public ManagersPayrollSummaryReportScriptlet() {
        managersPayrollSummaryService = (ManagersPayrollSummaryService) Component.getInstance("managersPayrollSummaryService");
        isNewGroupInit = false;
    }

    @Override
    public void afterGroupInit(String groupName) throws JRScriptletException {
        super.afterGroupInit(groupName);

        String sedeGroupName = "sedeGroup";

        if (groupName.equals(sedeGroupName)) {
            isNewGroupInit = true;

            Long generatedPayrollId = getFieldAsLong("generatedPayroll.id");
            Long businessUnitId = getFieldAsLong("businessUnit.id");
            BigDecimal exchangeRate = getFieldAsBigDecimal("exchangeRate.rate");

            //OBS: all data is saved in BS for all managers
            BigDecimal liquidBsBankAccountBs = sumLiquidByBankAccountPaymentTypeAndCurrency(generatedPayrollId, Constants.currencyIdBs);

            BigDecimal liquidBsBankAccountSus = sumLiquidByBankAccountPaymentTypeAndCurrency(generatedPayrollId, Constants.currencyIdSus);
            BigDecimal liquidSusBankAccountSus = BigDecimalUtil.divide(liquidBsBankAccountSus, exchangeRate);

            BigDecimal liquidBsCheck = sumLiquidByCheckPaymentType(generatedPayrollId);

            this.setVariableValue("liquidBsBankAccountBsVar", liquidBsBankAccountBs);
            this.setVariableValue("liquidSusBankAccountSusVar", liquidSusBankAccountSus);
            this.setVariableValue("liquidBsBankAccountSusVar", liquidBsBankAccountSus);
            this.setVariableValue("liquidBsCheckVar", liquidBsCheck);

            calculateAndSetPreviousMonthVariableValues(businessUnitId, exchangeRate);
        }
    }

    private void calculateAndSetPreviousMonthVariableValues(Long businessUnitId, BigDecimal exchangeRate) throws JRScriptletException {
        JobCategory jobCategory = (JobCategory) this.getParameterValue("jobCategoryParam");
        Gestion gestion = (Gestion) this.getParameterValue("gestionParam");
        Month month = (Month) this.getParameterValue("monthParam");

        Long previousNroPeson = Long.valueOf(0);
        BigDecimal previousLiquidBs = BigDecimal.ZERO;
        BigDecimal previousLiquidSus = BigDecimal.ZERO;

        GeneratedPayroll previousGeneratedPayroll = getPreviousMonthGeneratedPayroll(jobCategory, gestion, month, businessUnitId);
        if (previousGeneratedPayroll != null) {
            previousNroPeson = calculateNroPersonPreviousMonth(previousGeneratedPayroll.getId());
            previousLiquidBs = sumLiquidPreviousMonth(previousGeneratedPayroll.getId());
            previousLiquidSus = BigDecimalUtil.divide(previousLiquidBs, exchangeRate);

        }

        DateTime dateTime = new DateTime(gestion.getYear(), month.getValueAsPosition(), 15, 0, 0, 0, 0);
        DateTime previousMonthDateTime = dateTime.minusMonths(1);
        Month previousMonth = Month.getMonth(previousMonthDateTime.getMonthOfYear());

        this.setVariableValue("previousNroPersonVar", previousNroPeson);
        this.setVariableValue("previousLiquidBsVar", previousLiquidBs);
        this.setVariableValue("previousLiquidSusVar", previousLiquidSus);
        this.setVariableValue("previousMonthVar", MessageUtils.getMessage(previousMonth.getResourceKey()));
    }

    public void beforeDetailEval() throws JRScriptletException {
        super.beforeDetailEval();

        Long generatedPayrollId = getFieldAsLong("generatedPayroll.id");
        String costCenterCode = (String) this.getFieldValue("costCenter.code");

        Long nroPersonVar = calculateNroPersonWithCostCenter(generatedPayrollId, costCenterCode);

        BigDecimal totalIncomeBs = sumTotalIncomeByCostCenter(generatedPayrollId, costCenterCode);
        BigDecimal discount = sumTotalDiscountByCostCenter(generatedPayrollId, costCenterCode);
        BigDecimal liquidBs = sumLiquidByCostCenter(generatedPayrollId, costCenterCode);

        //verify if is first row in the group
        boolean isFirstRow = false;
        if (isNewGroupInit) {
            isFirstRow = true;
            isNewGroupInit = false;
        }

        this.setVariableValue("nroPersonVar", nroPersonVar);
        this.setVariableValue("incomeBsVar", totalIncomeBs);
        this.setVariableValue("discountVar", discount);
        this.setVariableValue("liquidBsVar", liquidBs);

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
        Long countResult = managersPayrollSummaryService.countByCostCenter(generatedPayrollId, costCenterCode);
        return (countResult != null ? countResult : Long.valueOf(0));
    }

    private BigDecimal sumTotalIncomeByCostCenter(Long generatedPayrollId, String costCenterCode) {
        BigDecimal sumResult = managersPayrollSummaryService.sumTotalIncomeByCostCenter(generatedPayrollId, costCenterCode);
        return (sumResult != null ? sumResult : BigDecimal.ZERO);
    }

    private BigDecimal sumTotalDiscountByCostCenter(Long generatedPayrollId, String costCenterCode) {
        BigDecimal sumResult = managersPayrollSummaryService.sumTotalDiscountByCostCenter(generatedPayrollId, costCenterCode);
        return (sumResult != null ? sumResult : BigDecimal.ZERO);
    }

    private BigDecimal sumLiquidByCostCenter(Long generatedPayrollId, String costCenterCode) {
        BigDecimal sumResult = managersPayrollSummaryService.sumLiquidByCostCenter(generatedPayrollId, costCenterCode);
        return (sumResult != null ? sumResult : BigDecimal.ZERO);
    }

    private BigDecimal sumLiquidByBankAccountPaymentTypeAndCurrency(Long generatedPayrollId, Long currencyId) {
        BigDecimal sumResult = managersPayrollSummaryService.sumLiquidByBankAccountPaymentTypeAndCurrency(generatedPayrollId, currencyId);
        return (sumResult != null ? sumResult : BigDecimal.ZERO);
    }

    private BigDecimal sumLiquidByCheckPaymentType(Long generatedPayrollId) {
        BigDecimal sumResult = managersPayrollSummaryService.sumLiquidByPaymentType(generatedPayrollId, PaymentType.PAYMENT_WITH_CHECK);
        return (sumResult != null ? sumResult : BigDecimal.ZERO);
    }

    private GeneratedPayroll getPreviousMonthGeneratedPayroll(JobCategory jobCategory, Gestion gestion, Month month, Long businessUnitId) {
        return managersPayrollSummaryService.getPreviousMonthGeneratedPayroll(jobCategory, gestion, month, businessUnitId);
    }

    private Long calculateNroPersonPreviousMonth(Long generatedPayrollId) {
        Long countResult = managersPayrollSummaryService.countByGeneratedPayroll(generatedPayrollId);
        return (countResult != null ? countResult : Long.valueOf(0));
    }

    private BigDecimal sumLiquidPreviousMonth(Long generatedPayrollId) {
        BigDecimal sumResult = managersPayrollSummaryService.sumLiquidByGeneratedPayroll(generatedPayrollId);
        return (sumResult != null ? sumResult : BigDecimal.ZERO);
    }

}
