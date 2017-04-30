package com.encens.khipus.util.employees.payroll.tributary;

import com.encens.khipus.model.employees.CategoryTributaryPayroll;
import com.encens.khipus.model.employees.Employee;
import com.encens.khipus.util.employees.payroll.structure.Calculator;

/**
 * @author
 * @version 2.26
 */
public class NameCalculator extends Calculator<CategoryTributaryPayroll> {
    private Employee employee;

    public NameCalculator(Employee employee) {
        this.employee = employee;
    }

    @Override
    public void execute(CategoryTributaryPayroll instance) {
        instance.setName(formatName(employee));
    }

    private String formatName(Employee employee) {
        String name = employee.getLastName() + " " + employee.getMaidenName();

        if (!"".equals(name.trim())) {
            name += ", ";
        }

        name += employee.getFirstName();

        return name;
    }
}
