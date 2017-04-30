package com.encens.khipus.action.employees;

import com.encens.khipus.framework.action.GenericAction;
import com.encens.khipus.framework.action.Outcome;
import com.encens.khipus.model.employees.EmployeeAcademicFormation;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.*;
import org.jboss.seam.annotations.security.Restrict;
import org.jboss.seam.framework.EntityQuery;

/**
 * EmployeeAcademicFormationAction
 *
 * @author
 * @version 2.25
 */
@Name("employeeAcademicFormationAction")
@Scope(ScopeType.CONVERSATION)
public class EmployeeAcademicFormationAction extends GenericAction<EmployeeAcademicFormation> {

    @In
    private EmployeeAction employeeAction;

    @In(create = true, value = "employeeAcademicFormationCountByNameQuery")
    private EntityQuery countByNameQuery;

    @Override
    protected String getDisplayNameProperty() {
        return "name";
    }

    @Factory(value = "employeeAcademicFormation", scope = ScopeType.STATELESS)
    public EmployeeAcademicFormation init() {
        return getInstance();
    }

    @Begin(nested = true, flushMode = FlushModeType.MANUAL)
    @Restrict("#{s:hasPermission('EMPLOYEEACADEMICFORMATION','CREATE')}")
    public String assignAcademicFormation() {
        return Outcome.SUCCESS;
    }

    @Override
    @Begin(nested = true, ifOutcome = Outcome.SUCCESS, flushMode = FlushModeType.MANUAL)
    @Restrict("#{s:hasPermission('EMPLOYEEACADEMICFORMATION','VIEW')}")
    public String select(EmployeeAcademicFormation instance) {
        return super.select(instance);
    }

    @Override
    @End(beforeRedirect = true)
    @Restrict("#{s:hasPermission('EMPLOYEEACADEMICFORMATION','CREATE')}")
    public String create() {
        if (!validate()) {
            return Outcome.REDISPLAY;
        }
        getInstance().setEmployee(employeeAction.getInstance());
        return super.create();
    }

    @Override
    @Restrict("#{s:hasPermission('EMPLOYEEACADEMICFORMATION','CREATE')}")
    public void createAndNew() {
        if (validate()) {
            getInstance().setEmployee(employeeAction.getInstance());
            super.createAndNew();
        }
    }

    @Override
    @End(beforeRedirect = true)
    @Restrict("#{s:hasPermission('EMPLOYEEACADEMICFORMATION','UPDATE')}")
    public String update() {
        if (!validate()) {
            return Outcome.REDISPLAY;
        }
        return super.update();
    }

    @Override
    @End(beforeRedirect = true)
    @Restrict("#{s:hasPermission('EMPLOYEEACADEMICFORMATION','DELETE')}")
    public String delete() {
        return super.delete();
    }

    public Boolean validate() {
        Boolean valid = true;
        Long countByName = (Long) countByNameQuery.getSingleResult();
        if (countByName > 0) {
            addDuplicatedMessage();
            valid = false;
        }
        return valid;
    }

}
