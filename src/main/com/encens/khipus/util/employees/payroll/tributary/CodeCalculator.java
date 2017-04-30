package com.encens.khipus.util.employees.payroll.tributary;

import com.encens.khipus.model.employees.CategoryTributaryPayroll;
import com.encens.khipus.model.employees.Employee;
import com.encens.khipus.util.employees.payroll.structure.Calculator;

/**
 * @author
 * @version 2.26
 */
public class CodeCalculator extends Calculator<CategoryTributaryPayroll> {
    private Employee employee;

    public CodeCalculator(Employee employee) {
        this.employee = employee;
    }

    @Override
    public void execute(CategoryTributaryPayroll instance) {
        instance.setCode(employee.getIdNumber());
    }
}
