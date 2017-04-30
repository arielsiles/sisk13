package com.encens.khipus.action.employees;

import com.encens.khipus.framework.action.GenericAction;
import com.encens.khipus.model.employees.ManagersPayroll;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

/**
 * Action for Managers Payroll
 *
 * @author
 * @version 1.1.10
 */
@Name("managersPayrollAction")
@Scope(ScopeType.CONVERSATION)
public class ManagersPayrollAction extends GenericAction<ManagersPayroll> {
    @In(value = "generatedPayrollAction")
    GeneratedPayrollAction generatedPayrollAction;

    @Factory(value = "managersPayroll", scope = ScopeType.STATELESS)
    public ManagersPayroll initManagersPayroll() {
        return getInstance();
    }

    @Override
    protected String getDisplayNameProperty() {
        return "employee.idNumber";
    }

    public GeneratedPayrollAction getGeneratedPayrollAction() {
        return generatedPayrollAction;
    }
}
