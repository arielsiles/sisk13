package com.encens.khipus.service.employees;

import com.encens.khipus.framework.service.GenericService;
import com.encens.khipus.model.employees.Employee;
import com.encens.khipus.model.employees.GestionPayroll;

import javax.ejb.Local;
import java.math.BigDecimal;

/**
 * @author
 * @version 2.26
 */
@Local
public interface RetentionValidatorService extends GenericService {
    boolean applyRetention(Employee employee,
                           GestionPayroll gestionPayroll,
                           BigDecimal amount);
}
