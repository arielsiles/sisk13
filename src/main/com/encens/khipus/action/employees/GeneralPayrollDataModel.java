package com.encens.khipus.action.employees;

import com.encens.khipus.model.employees.GeneralPayroll;
import com.encens.khipus.model.employees.GeneratedPayroll;
import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import java.util.Arrays;
import java.util.List;

/**
 * Data model for GeneralPayroll
 *
 * @author
 */

@Name("generalPayrollDataModel")
@Scope(ScopeType.CONVERSATION)
public class GeneralPayrollDataModel extends GenericPayrollDataModel<Long, GeneralPayroll> {
    private static final String[] RESTRICTIONS = {
            "lower(generalPayroll.employee.idNumber) like concat(lower(#{generalPayrollDataModel.idNumber}), '%')",
            "lower(generalPayroll.employee.lastName) like concat('%', concat(lower(#{generalPayrollDataModel.lastName}), '%'))",
            "lower(generalPayroll.employee.maidenName) like concat('%', concat(lower(#{generalPayrollDataModel.maidenName}), '%'))",
            "lower(generalPayroll.employee.firstName) like concat('%', concat(lower(#{generalPayrollDataModel.firstName}), '%'))"
    };

    @Create
    public void init() {
        setGeneratedPayroll((GeneratedPayroll) Component.getInstance("generatedPayroll"));
        sortProperty = "generalPayroll.employee.lastName";
    }

    @Override
    public void search() {
        setGeneratedPayroll((GeneratedPayroll) Component.getInstance("generatedPayroll"));
        super.search();
    }

    @Override
    public String getEjbql() {
        return "select generalPayroll from GeneralPayroll generalPayroll" +
                " left join fetch generalPayroll.employee employee" +
                " where generalPayroll.generatedPayroll.id=" + getGeneratedPayroll().getId();
    }

    @Override
    public List<String> getRestrictions() {
        return Arrays.asList(RESTRICTIONS);
    }
}