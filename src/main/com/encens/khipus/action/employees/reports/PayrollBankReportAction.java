package com.encens.khipus.action.employees.reports;

import com.encens.khipus.action.reports.GenericReportAction;
import com.encens.khipus.action.reports.PageFormat;
import com.encens.khipus.action.reports.PageOrientation;
import com.encens.khipus.action.reports.ReportFormat;
import com.encens.khipus.model.employees.GeneratedPayroll;
import com.encens.khipus.model.employees.PayrollGenerationType;
import com.encens.khipus.model.finances.PaymentType;
import com.encens.khipus.util.MessageUtils;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import java.util.HashMap;
import java.util.Map;

/**
 * Encens S.R.L.
 *
 * @author
 * @version $Id: PayrollBankReportAction.java  28-ene-2010 15:55:38$
 */
@Name("payrollBankReportAction")
@Scope(ScopeType.PAGE)
public class PayrollBankReportAction extends GenericReportAction {
    private String ejbql;
    private GeneratedPayroll generatedPayroll;
    private PaymentType paymentTypeBankAccount = PaymentType.PAYMENT_BANK_ACCOUNT;

    public void generateReport(GeneratedPayroll generatedPayroll) {
        log.debug("payrollEmployeeWithBAReportAction executing.................................................");
        setGeneratedPayroll(getEntityManager().find(GeneratedPayroll.class, generatedPayroll.getId()));

        if (PayrollGenerationType.GENERATION_BY_SALARY.equals(getGeneratedPayroll().getGestionPayroll().getJobCategory().getPayrollGenerationType())) {//managers
            restrictions = restrictionsManager;
            sortProperty = sortPropertyManager;
            ejbql = ejbqlManager;
        } else if (PayrollGenerationType.GENERATION_BY_TIME.equals(getGeneratedPayroll().getGestionPayroll().getJobCategory().getPayrollGenerationType())) {
            restrictions = restrictionsGeneral;
            sortProperty = sortPropertyGeneral;
            ejbql = ejbqlGeneral;
        } else if (PayrollGenerationType.GENERATION_BY_PERIODSALARY.equals(getGeneratedPayroll().getGestionPayroll().getJobCategory().getPayrollGenerationType())) {
            restrictions = restrictionsFiscalProfessor;
            sortProperty = sortPropertyFiscalProfessor;
            ejbql = ejbqlFiscalProfessor;
        }

        this.setReportFormat(ReportFormat.XLS);
        Map<String, Object> params = new HashMap<String, Object>();
        super.generateReport("payrollBankReport", "/employees/reports/payrollBankReport.jrxml", PageFormat.LETTER, PageOrientation.LANDSCAPE, MessageUtils.getMessage("Reports.payrollBankReport"), params);
    }

    @Create
    public void init() {
        restrictions = restrictionsManager;
        sortProperty = sortPropertyManager;
        ejbql = ejbqlManager;
    }

    @Override
    protected String getEjbql() {
        return ejbql;
    }

    private String ejbqlManager = "SELECT managersPayroll.id, " +
            "managersPayroll.employee.id, " +
            "managersPayroll.liquid, " +
            "managersPayroll.employee.paymentType, " +
            "managersPayroll.employee.lastName, " +
            "managersPayroll.employee.maidenName, " +
            "managersPayroll.employee.firstName, " +
            "managersPayroll.employee.idNumber, " +
            "bankAccount.id, " +
            "bankAccount.defaultAccount, " +
            "bankAccount.accountNumber, " +
            "bankAccount.clientCod, " +
            "bankAccount.currency.currencyCode, " +
            "bankAccount.bankEntity.code " +
            "FROM ManagersPayroll managersPayroll LEFT JOIN managersPayroll.employee.bankAccountList bankAccount " +
            "WHERE bankAccount.defaultAccount = #{true} AND managersPayroll.employee.paymentType = #{payrollBankReportAction.paymentTypeBankAccount}";
    private String sortPropertyManager = " managersPayroll.employee.lastName," +
            " managersPayroll.employee.maidenName," +
            " managersPayroll.employee.firstName," +
            " managersPayroll.employee.id";
    private String[] restrictionsManager = new String[]{"managersPayroll.company = #{currentCompany}",
            "managersPayroll.generatedPayroll = #{payrollBankReportAction.generatedPayroll}"};
    private String ejbqlFiscalProfessor = "SELECT fiscalProfessorPayroll.id, " +
            "fiscalProfessorPayroll.employee.id, " +
            "fiscalProfessorPayroll.liquid, " +
            "fiscalProfessorPayroll.employee.paymentType, " +
            "fiscalProfessorPayroll.employee.lastName, " +
            "fiscalProfessorPayroll.employee.maidenName, " +
            "fiscalProfessorPayroll.employee.firstName, " +
            "fiscalProfessorPayroll.employee.idNumber, " +
            "bankAccount.id, " +
            "bankAccount.defaultAccount, " +
            "bankAccount.accountNumber, " +
            "bankAccount.clientCod, " +
            "bankAccount.currency.currencyCode, " +
            "bankAccount.bankEntity.code " +
            "FROM FiscalProfessorPayroll fiscalProfessorPayroll LEFT JOIN fiscalProfessorPayroll.employee.bankAccountList bankAccount " +
            "WHERE bankAccount.defaultAccount = #{true} AND fiscalProfessorPayroll.employee.paymentType = #{payrollBankReportAction.paymentTypeBankAccount}";
    private String sortPropertyFiscalProfessor = " fiscalProfessorPayroll.employee.lastName," +
            " fiscalProfessorPayroll.employee.maidenName," +
            " fiscalProfessorPayroll.employee.firstName," +
            " fiscalProfessorPayroll.employee.id";
    private String[] restrictionsFiscalProfessor = new String[]{"fiscalProfessorPayroll.company = #{currentCompany}",
            "fiscalProfessorPayroll.generatedPayroll = #{payrollBankReportAction.generatedPayroll}"};

    private String ejbqlGeneral = "SELECT generalPayroll.id, " +
            "generalPayroll.employee.id, " +
            "generalPayroll.liquid, " +
            "generalPayroll.employee.paymentType, " +
            "generalPayroll.employee.lastName, " +
            "generalPayroll.employee.maidenName, " +
            "generalPayroll.employee.firstName, " +
            "generalPayroll.employee.idNumber, " +
            "generalPayroll.employee.extension, " +
            "bankAccount.id, " +
            "bankAccount.defaultAccount, " +
            "bankAccount.accountNumber, " +
            "bankAccount.clientCod, " +
            "bankAccount.currency.currencyCode, " +
            "bankAccount.bankEntity.code " +
            "FROM GeneralPayroll generalPayroll LEFT JOIN generalPayroll.employee.bankAccountList bankAccount " +
            "WHERE bankAccount.defaultAccount = #{true} AND generalPayroll.employee.paymentType = #{payrollBankReportAction.paymentTypeBankAccount}";
    private String sortPropertyGeneral = " generalPayroll.employee.lastName," +
            " generalPayroll.employee.maidenName," +
            " generalPayroll.employee.firstName," +
            " generalPayroll.employee.id";
    private String[] restrictionsGeneral = new String[]{"generalPayroll.company = #{currentCompany}",
            "generalPayroll.generatedPayroll = #{payrollBankReportAction.generatedPayroll}"};

    public GeneratedPayroll getGeneratedPayroll() {
        return generatedPayroll;
    }

    public void setGeneratedPayroll(GeneratedPayroll generatedPayroll) {
        this.generatedPayroll = generatedPayroll;
    }

    public PaymentType getPaymentTypeBankAccount() {
        return paymentTypeBankAccount;
    }

    public void setPaymentTypeBankAccount(PaymentType paymentTypeBankAccount) {
        this.paymentTypeBankAccount = paymentTypeBankAccount;
    }
}
