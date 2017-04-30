package com.encens.khipus.action.employees;

import com.encens.khipus.model.employees.ChristmasPayroll;
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
 * @version 3.2
 */
@Name("christmasPayrollDataModel")
@Scope(ScopeType.PAGE)
public class ChristmasPayrollDataModel extends GenericPayrollDataModel<Long, ChristmasPayroll> {
    private static final String[] RESTRICTIONS = {
            "lower(christmasPayroll.employee.idNumber) like concat(lower(#{christmasPayrollDataModel.idNumber}), '%')",
            "lower(christmasPayroll.employee.lastName) like concat('%', concat(lower(#{christmasPayrollDataModel.lastName}), '%'))",
            "lower(christmasPayroll.employee.maidenName) like concat('%', concat(lower(#{christmasPayrollDataModel.maidenName}), '%'))",
            "lower(christmasPayroll.employee.firstName) like concat('%', concat(lower(#{christmasPayrollDataModel.firstName}), '%'))"
    };

    @Create
    public void init() {
        setGeneratedPayroll((GeneratedPayroll) Component.getInstance("generatedPayroll"));
        sortProperty = "christmasPayroll.employee.lastName";
    }

    @Override
    public void search() {
        setGeneratedPayroll((GeneratedPayroll) Component.getInstance("generatedPayroll"));
        super.search();
    }

    @Override
    public String getEjbql() {
        return "select christmasPayroll from ChristmasPayroll christmasPayroll" +
                " left join fetch christmasPayroll.employee employee" +
                " where christmasPayroll.generatedPayroll.id=" + getGeneratedPayroll().getId();
    }

    @Override
    public List<String> getRestrictions() {
        return Arrays.asList(RESTRICTIONS);
    }
}
