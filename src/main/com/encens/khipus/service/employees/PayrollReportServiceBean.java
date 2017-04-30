package com.encens.khipus.service.employees;

import com.encens.khipus.exception.EntryNotFoundException;
import com.encens.khipus.framework.service.GenericService;
import com.encens.khipus.model.employees.GeneratedPayroll;
import com.encens.khipus.model.employees.GestionPayroll;
import com.encens.khipus.model.finances.BankAccount;
import com.encens.khipus.model.finances.ExchangeRate;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.log.Log;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import java.util.HashMap;
import java.util.Map;

/**
 * Encens S.R.L.
 * Service to read properties to payroll report
 *
 * @author
 * @version $Id: PallrollReportServiceBean.java  30-dic-2009 17:43:44$
 */
@Stateless
@Name("payrollReportService")
@AutoCreate
public class PayrollReportServiceBean implements PayrollReportService {
    @Logger
    private Log log;

    @In
    private GenericService genericService;

    @In(value = "#{entityManager}")
    private EntityManager em;

    public PayrollReportServiceBean() {
    }

    /**
     * get generated payroll report header info
     *
     * @param generatedPayrollId
     * @return Map
     */
    public Map<String, Object> getGeneratedPayrollInfo(Long generatedPayrollId) {
        log.debug("Executing getGeneratedPayrollInfo method... " + generatedPayrollId);
        Map<String, Object> payrollInfoMap = new HashMap<String, Object>();

        GeneratedPayroll generatedPayroll = null;
        try {
            generatedPayroll = genericService.findById(GeneratedPayroll.class, generatedPayrollId);
        } catch (EntryNotFoundException e) {
            log.debug("Not found GeneratedPayroll with id:" + generatedPayrollId);
        }

        if (generatedPayroll != null) {
            payrollInfoMap.put("generationDate", generatedPayroll.getGenerationDate());
            payrollInfoMap.put("payrollType", generatedPayroll.getGeneratedPayrollType());

            GestionPayroll gestionPayroll = generatedPayroll.getGestionPayroll();
            if (gestionPayroll != null) {
                payrollInfoMap.put("month", gestionPayroll.getMonth());
                payrollInfoMap.put("year", gestionPayroll.getGestion().getYear().toString());
                payrollInfoMap.put("initDate", gestionPayroll.getInitDate());
                payrollInfoMap.put("endDate", gestionPayroll.getEndDate());

                ExchangeRate exchangeRate = gestionPayroll.getExchangeRate();
                if (exchangeRate != null) {
                    payrollInfoMap.put("rate", exchangeRate.getRate());
                }
            }
        }
        return payrollInfoMap;
    }

    /**
     * Get the default bank account number of employee
     * @param employeeId
     * @return String
     */
    public String getEmployeeDefaultBankAccountNumber(Long employeeId) {
        String accountNumber = null;

        try {
            BankAccount bankAccount = (BankAccount) em.createNamedQuery("BankAccount.findByEmployeeDefaulAccount").
                    setParameter("employeeId", employeeId).
                    setParameter("defaultAccount", Boolean.TRUE).
                    getSingleResult();

            accountNumber = bankAccount.getAccountNumber();
        } catch (Exception e) {
            log.debug("Not found default banck account.." + e);
        }
        return accountNumber;
    }

    /**
     * find employee default bank account by employee id.
     *
     * @param employeeId
     * @return BankAccount
     */
    public BankAccount getEmployeeDefaultBankAccount(Long employeeId) {
        BankAccount bankAccount = null;
        try {
            bankAccount = (BankAccount) em.createNamedQuery("BankAccount.findByEmployeeDefaulAccount").
                    setParameter("employeeId", employeeId).
                    setParameter("defaultAccount", Boolean.TRUE).
                    getSingleResult();
        } catch (Exception e) {
            log.debug("Not found default banck account.." + e);
        }
        return bankAccount;
    }
}
