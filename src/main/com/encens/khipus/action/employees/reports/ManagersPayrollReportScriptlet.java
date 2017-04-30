package com.encens.khipus.action.employees.reports;

import com.encens.khipus.model.finances.BankAccount;
import com.encens.khipus.model.finances.PaymentType;
import com.encens.khipus.service.employees.PayrollReportService;
import net.sf.jasperreports.engine.JRDefaultScriptlet;
import net.sf.jasperreports.engine.JRScriptletException;
import org.jboss.seam.Component;

/**
 * Encens S.R.L.
 * scriplet to calculate inner values for managers payroll report
 *
 * @author
 * @version $Id: ManagersPayrollReportScriptlet.java  01-jun-2010 17:13:43$
 */
public class ManagersPayrollReportScriptlet extends JRDefaultScriptlet {

    PayrollReportService payrollReportService = (PayrollReportService) Component.getInstance("payrollReportService");

    public void beforeDetailEval() throws JRScriptletException {
        super.beforeDetailEval();

        Long employeeId = getFieldAsLong("employee.id");
        PaymentType paymentType = (PaymentType) getFieldValue("employee.paymentType");

        //find employee bank account number
        String accountNumberValue = null;
        String clientCodeValue = null;
        if (PaymentType.PAYMENT_BANK_ACCOUNT.equals(paymentType)) {
            BankAccount bankAccount = payrollReportService.getEmployeeDefaultBankAccount(employeeId);
            if (bankAccount != null) {
                accountNumberValue = bankAccount.getAccountNumber();
                clientCodeValue = bankAccount.getClientCod();
            }
        }

        this.setVariableValue("bankAccountNumberVar", accountNumberValue);
        this.setVariableValue("clientCodeVar", clientCodeValue);
    }

    private Long getFieldAsLong(String fieldName) throws JRScriptletException {
        Long longValue = null;
        Object fieldObj = this.getFieldValue(fieldName);
        if (fieldObj != null && fieldObj.toString().length() > 0) {
            longValue = new Long(fieldObj.toString());
        }
        return longValue;
    }
}
