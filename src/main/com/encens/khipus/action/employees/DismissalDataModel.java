package com.encens.khipus.action.employees;

import com.encens.khipus.framework.action.QueryDataModel;
import com.encens.khipus.model.employees.Dismissal;
import com.encens.khipus.model.employees.Employee;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.security.Restrict;

import java.util.Arrays;
import java.util.List;

/**
 * @author
 * @version 3.4
 */

@Name("dismissalDataModel")
@Scope(ScopeType.PAGE)
@Restrict("#{s:hasPermission('DISMISSAL','VIEW')}")
public class DismissalDataModel extends QueryDataModel<Long, Dismissal> {
    private Employee employee;

    private static final String[] RESTRICTIONS =
            {
                    "dismissal.code = #{dismissalDataModel.criteria.code}",
                    "dismissal.jobContract.contract.employee = #{dismissalDataModel.employee}"
            };

    @Create
    public void init() {
        sortProperty = "dismissal.code";
        sortAsc = false;
    }

    @Override
    public String getEjbql() {
        return "select dismissal from Dismissal dismissal ";
    }

    @Override
    public List<String> getRestrictions() {
        return Arrays.asList(RESTRICTIONS);
    }

    @Override
    public void clear() {
        clearEmployee();
        super.clear();
    }

    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
    }

    @SuppressWarnings({"NullableProblems"})
    public void clearEmployee() {
        setEmployee(null);
    }
}