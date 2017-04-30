package com.encens.khipus.service.employees;

import com.encens.khipus.model.employees.Employee;
import com.encens.khipus.model.finances.BankAccount;

import javax.ejb.Local;

/**
 * BankAccountService
 *
 * @author
 * @version 1.1.10
 */
@Local
public interface BankAccountService {
    Boolean hasDefaultAccount(Employee employee);

    BankAccount getDefaultAccount(Employee employee);

    Boolean hasDefaultAccount(Employee employee, BankAccount bankAccount);
}
