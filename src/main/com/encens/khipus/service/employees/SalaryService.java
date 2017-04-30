package com.encens.khipus.service.employees;

import com.encens.khipus.model.employees.Currency;
import com.encens.khipus.model.finances.KindOfSalary;
import com.encens.khipus.model.finances.Salary;

import javax.ejb.Local;
import java.math.BigDecimal;
import java.util.List;

/**
 * Encens Team
 *
 * @author
 * @version : SalaryService, 02-12-2009 07:10:59 PM
 */
@Local
public interface SalaryService {
    List<Salary> getSalariesByTypeCurrencyAndAmount(KindOfSalary kindOfSalary, Currency currency, BigDecimal amount);
}
