package com.encens.khipus.service.employees;

import com.encens.khipus.framework.service.GenericService;
import com.encens.khipus.model.employees.*;
import com.encens.khipus.model.finances.BankAccount;

import javax.ejb.Local;
import java.math.BigDecimal;
import java.util.Date;

/**
 * @author
 * @version 3.2
 */
@Local
public interface ChristmasPayrollService extends GenericService {
    ChristmasPayroll buildChristmasPayroll(GeneratedPayroll generatedPayroll, GestionPayroll gestionPayroll,
                                           Employee employee, Date initContractDate, BigDecimal salary,
                                           int workedDays, ManagersPayroll novemberManagersPayroll,
                                           BigDecimal septemberTotalIncome, BigDecimal octoberTotalIncome,
                                           BigDecimal novemberTotalIncome, BigDecimal averageSalary,
                                           BigDecimal contributableSalary, BankAccount bankAccount);
}
