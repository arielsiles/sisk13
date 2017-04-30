package com.encens.khipus.action.employees;

import com.encens.khipus.framework.action.QueryDataModel;
import com.encens.khipus.model.employees.EmployeeAcademicFormation;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.security.Restrict;

import java.util.Arrays;
import java.util.List;

/**
 * EmployeeAcademicFormationDataModel
 *
 * @author
 * @version 2.25
 */
@Name("employeeAcademicFormationDataModel")
@Scope(ScopeType.PAGE)
@Restrict("#{s:hasPermission('EMPLOYEEACADEMICFORMATION','VIEW')}")
public class EmployeeAcademicFormationDataModel extends QueryDataModel<Long, EmployeeAcademicFormation> {

    private static final String[] RESTRICTIONS =
            {"employeeAcademicFormation.employee = #{employee}"};

    @Create
    public void init() {
        sortProperty = "employeeAcademicFormation.name";
    }

    @Override
    public String getEjbql() {
        return "select employeeAcademicFormation from EmployeeAcademicFormation employeeAcademicFormation";
    }

    @Override
    public List<String> getRestrictions() {
        return Arrays.asList(RESTRICTIONS);
    }
}
