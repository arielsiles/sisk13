package com.encens.khipus.service.employees;

import com.encens.khipus.framework.service.GenericService;
import com.encens.khipus.model.employees.GeneratedPayroll;

import javax.ejb.Local;
import java.util.List;

/**
 * @author
 * @version 0.3
 */
@Local
public interface ManagersPayrollService extends GenericService {

    @SuppressWarnings(value = "unchecked")
    List<Long> findEmployeeIdListByGeneratedPayroll(GeneratedPayroll generatedPayroll);

    @SuppressWarnings(value = "unchecked")
    List<Long> findEmployeeIdListByGeneratedPayrollInEmployeeIdList(GeneratedPayroll generatedPayroll, List<Long> employeeIdList);

    @SuppressWarnings(value = "unchecked")
    List<Object[]> findByGeneratedPayrollAndEmployeeIdList(GeneratedPayroll generatedPayroll, List<Long> employeeIdList);
}
