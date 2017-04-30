package com.encens.khipus.service.employees;

import com.encens.khipus.model.finances.BankAccount;

import javax.ejb.Local;
import java.util.Map;

/**
 * Encens S.R.L.
 *
 * @author
 * @version $Id: PallrollReportService.java  30-dic-2009 17:49:44$
 */
@Local
public interface PayrollReportService {

    Map<String, Object> getGeneratedPayrollInfo(Long generatedPayrollId);

    String getEmployeeDefaultBankAccountNumber(Long employeeId);

    BankAccount getEmployeeDefaultBankAccount(Long employeeId);
}
