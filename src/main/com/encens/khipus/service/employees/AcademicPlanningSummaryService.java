package com.encens.khipus.service.employees;

import com.encens.khipus.model.employees.Cycle;
import com.encens.khipus.model.finances.OrganizationalUnit;
import com.encens.khipus.model.finances.Salary;

import javax.ejb.Local;

/**
 * Encens S.R.L.
 *
 * @author
 * @version $Id: AcademicPlanningSummaryService.java  09-jul-2010 12:29:46$
 */
@Local
public interface AcademicPlanningSummaryService {
    Salary findTeacherSalary(OrganizationalUnit organizationalUnit, Cycle cycle, String employeeCode);
}
