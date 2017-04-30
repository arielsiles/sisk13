package com.encens.khipus.model.employees;

import com.encens.khipus.model.admin.Company;
import com.encens.khipus.util.Constants;

import javax.persistence.*;

/**
 * EmployeeAcademicFormation
 *
 * @author
 * @version 2.25
 */
@Entity
@Table(schema = Constants.KHIPUS_SCHEMA, name = "formacionacademp")
@PrimaryKeyJoinColumns(value = {
        @PrimaryKeyJoinColumn(name = "idformacionacademp", referencedColumnName = "idformacionacademica")
})
public class EmployeeAcademicFormation extends AcademicFormation {
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "idempleado", nullable = false, updatable = false, insertable = true)
    private Employee employee;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "idcompania", nullable = false, updatable = false, insertable = true)
    private Company company;

    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }
}

