package com.encens.khipus.action.employees.reports;

import com.encens.khipus.model.finances.PaymentType;
import com.encens.khipus.service.employees.PayrollReportService;
import com.encens.khipus.util.BigDecimalUtil;
import net.sf.jasperreports.engine.JRDefaultScriptlet;
import net.sf.jasperreports.engine.JRScriptletException;
import org.jboss.seam.Component;
import org.jboss.seam.international.Messages;

import java.math.BigDecimal;

/**
 * Encens S.R.L.
 * Scriptlet to calculate inner report values
 *
 * @author
 * @version $Id: ManagersPayrollExtendedReportScriptlet.java  04-mar-2010 18:57:41$
 */
public class ManagersPayrollExtendedReportScriptlet extends JRDefaultScriptlet {

    PayrollReportService payrollReportService = (PayrollReportService) Component.getInstance("payrollReportService");

    public void beforeDetailEval() throws JRScriptletException {
        super.beforeDetailEval();

        Long employeeId = getFieldAsLong("employee.id");
        PaymentType paymentType = (PaymentType) getFieldValue("employee.paymentType");
        BigDecimal salary = getFieldAsBigDecimal("managersPayroll.salary");
        BigDecimal totalIncome = getFieldAsBigDecimal("managersPayroll.totalIncome");
        BigDecimal totalDiscount = getFieldAsBigDecimal("managersPayroll.totalDiscount");
        BigDecimal otherIncomes = getFieldAsBigDecimal("managersPayroll.otherIncomes");

        //calculate employee account number
        String accountNumberValue = null;
        if (PaymentType.PAYMENT_BANK_ACCOUNT.equals(paymentType)) {
            accountNumberValue = payrollReportService.getEmployeeDefaultBankAccountNumber(employeeId);
        } else if(PaymentType.PAYMENT_WITH_CHECK.equals(paymentType)) {
            accountNumberValue = Messages.instance().get(PaymentType.PAYMENT_WITH_CHECK.getResourceKey());
        }

        //calculate compose column values
        BigDecimal failDiscount = BigDecimalUtil.subtract(salary, totalIncome);

        BigDecimal totalPay = totalIncome;
        if (otherIncomes != null) {
            totalPay = BigDecimalUtil.sum(totalPay, otherIncomes);
        }
        if (totalDiscount != null) {
            totalPay = BigDecimalUtil.subtract(totalPay, totalDiscount);
        }


        this.setVariableValue("bankAccountNumberVar", accountNumberValue);
        this.setVariableValue("failDiscountVar", failDiscount);
        this.setVariableValue("totalPayVar", totalPay);
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
}
