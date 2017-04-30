package com.encens.khipus.action.employees;

import com.encens.khipus.framework.action.EntityQuery;
import com.encens.khipus.model.employees.FiscalProfessorPayroll;
import com.encens.khipus.model.employees.GeneratedPayroll;
import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import java.util.Arrays;
import java.util.List;

/**
 * @author
 * @version 3.4
 */
@Name("fiscalProfessorPayrollDataModel")
@Scope(ScopeType.CONVERSATION)
public class FiscalProfessorPayrollDataModel extends GenericPayrollDataModel<Long, FiscalProfessorPayroll> {
    private static final String[] RESTRICTIONS = {
            "lower(fiscalProfessorPayroll.employee.idNumber) like concat(lower(#{fiscalProfessorPayrollDataModel.idNumber}), '%')",
            "lower(fiscalProfessorPayroll.employee.lastName) like concat('%', concat(lower(#{fiscalProfessorPayrollDataModel.lastName}), '%'))",
            "lower(fiscalProfessorPayroll.employee.maidenName) like concat('%', concat(lower(#{fiscalProfessorPayrollDataModel.maidenName}), '%'))",
            "lower(fiscalProfessorPayroll.employee.firstName) like concat('%', concat(lower(#{fiscalProfessorPayrollDataModel.firstName}), '%'))"
    };

    @Create
    public void init() {
        setGeneratedPayroll((GeneratedPayroll) Component.getInstance("generatedPayroll"));
        sortProperty = "fiscalProfessorPayroll.employee.lastName, fiscalProfessorPayroll.employee.maidenName, fiscalProfessorPayroll.employee.firstName";
    }

    @Override
    protected void postInitEntityQuery(EntityQuery entityQuery) {
        sortProperty = "fiscalProfessorPayroll.employee.lastName, fiscalProfessorPayroll.employee.maidenName, fiscalProfessorPayroll.employee.firstName";
    }

    @Override
    public void search() {
        setGeneratedPayroll((GeneratedPayroll) Component.getInstance("generatedPayroll"));
        super.search();
    }

    @Override
    public String getEjbql() {
        return "select fiscalProfessorPayroll from FiscalProfessorPayroll fiscalProfessorPayroll" +
                " left join fetch fiscalProfessorPayroll.employee employee" +
                " where fiscalProfessorPayroll.generatedPayroll.id=" + getGeneratedPayroll().getId();
    }

    @Override
    public List<String> getRestrictions() {
        return Arrays.asList(RESTRICTIONS);
    }
}