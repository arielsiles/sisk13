package com.encens.khipus.action.employees;

import com.encens.khipus.framework.action.QueryDataModel;
import com.encens.khipus.model.employees.Employee;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import java.util.Arrays;
import java.util.List;

/**
 * Encens Team
 *
 * @author
 * @version : GeneralEmployeeDataModel, 26-11-2009 02:18:14 PM
 */
@Name("generalEmployeeDataModel")
@Scope(ScopeType.PAGE)
public class GeneralEmployeeDataModel extends QueryDataModel<Long, Employee> {
    private static final String[] RESTRICTIONS =
            {"lower(employee.lastName) like concat('%', concat(lower(#{generalEmployeeDataModel.criteria.lastName}), '%'))",
                    "lower(employee.maidenName) like concat('%', concat(lower(#{generalEmployeeDataModel.criteria.maidenName}), '%'))",
                    "lower(employee.firstName) like concat('%', concat(lower(#{generalEmployeeDataModel.criteria.firstName}), '%'))",
                    "employee.idNumber like concat(#{generalEmployeeDataModel.criteria.idNumber}, '%')"};

    @Create
    public void init() {
        sortProperty = "employee.lastName";
    }

    @Override
    public String getEjbql() {
        return "select employee from Employee employee";
    }

    @Override
    public List<String> getRestrictions() {
        return Arrays.asList(RESTRICTIONS);
    }
}

