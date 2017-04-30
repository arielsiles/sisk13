package com.encens.khipus.action.employees;

import com.encens.khipus.framework.action.GenericAction;
import com.encens.khipus.model.employees.GeneralPayroll;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

/**
 * Actions for GeneralPayroll
 *
 * @author
 * @version 1.2.0
 */

@Name("generalPayrollAction")
@Scope(ScopeType.CONVERSATION)
public class GeneralPayrollAction extends GenericAction<GeneralPayroll> {
    @In(value = "generatedPayrollAction")
    GeneratedPayrollAction generatedPayrollAction;

    @Factory(value = "generalPayroll", scope = ScopeType.STATELESS)
    public GeneralPayroll initGeneralPayroll() {
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