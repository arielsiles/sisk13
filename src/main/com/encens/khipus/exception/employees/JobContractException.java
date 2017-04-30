package com.encens.khipus.exception.employees;

import com.encens.khipus.model.employees.Employee;

import java.util.ArrayList;
import java.util.List;

/**
 * @author
 * @version 2.26
 */
public class JobContractException extends TaxPayrollException {
    private List<Employee> employees = new ArrayList<Employee>();

    public JobContractException(List<Employee> employees) {
        this.employees = employees;
    }

    public List<Employee> getEmployees() {
        return employees;
    }
}
