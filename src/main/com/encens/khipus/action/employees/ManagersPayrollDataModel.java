package com.encens.khipus.action.employees;

import com.encens.khipus.model.employees.GeneratedPayroll;
import com.encens.khipus.model.employees.ManagersPayroll;
import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import java.util.Arrays;
import java.util.List;

/**
 * Data model for ManagersPayroll
 *
 * @author
 */

@Name("managersPayrollDataModel")
@Scope(ScopeType.CONVERSATION)
public class ManagersPayrollDataModel extends GenericPayrollDataModel<Long, ManagersPayroll> {
    private static final String[] RESTRICTIONS = {
            "lower(managersPayroll.employee.idNumber) like concat(lower(#{managersPayrollDataModel.idNumber}), '%')",
            "lower(managersPayroll.employee.lastName) like concat('%', concat(lower(#{managersPayrollDataModel.lastName}), '%'))",
            "lower(managersPayroll.employee.maidenName) like concat('%', concat(lower(#{managersPayrollDataModel.maidenName}), '%'))",
            "lower(managersPayroll.employee.firstName) like concat('%', concat(lower(#{managersPayrollDataModel.firstName}), '%'))"
    };

    @Create
    public void init() {
        setGeneratedPayroll((GeneratedPayroll) Component.getInstance("generatedPayroll"));
        sortProperty = "managersPayroll.employee.lastName";
    }

    @Override
    public void search() {
        setGeneratedPayroll((GeneratedPayroll) Component.getInstance("generatedPayroll"));
        super.search();
    }

    @Override
    public String getEjbql() {
        return "select managersPayroll from ManagersPayroll managersPayroll" +
                " left join fetch managersPayroll.employee employee" +
                " where managersPayroll.generatedPayroll.id=" + getGeneratedPayroll().getId();
    }

    @Override
    public List<String> getRestrictions() {
        return Arrays.asList(RESTRICTIONS);
    }
}