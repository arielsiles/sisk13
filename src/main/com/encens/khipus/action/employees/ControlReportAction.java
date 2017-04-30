package com.encens.khipus.action.employees;

import com.encens.khipus.framework.action.GenericAction;
import com.encens.khipus.framework.action.Outcome;
import com.encens.khipus.model.employees.ControlReport;
import com.encens.khipus.model.employees.GeneratedPayroll;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Begin;
import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

/**
 * Actions for ControlReport
 *
 * @author Ariel Siles
 */

@Name("controlReportAction")
@Scope(ScopeType.CONVERSATION)
public class ControlReportAction extends GenericAction<ControlReport> {

    private GeneratedPayroll generatedPayroll;

    @Factory(value = "controlReport", scope = ScopeType.STATELESS)
    public ControlReport initControlReport() {
        return getInstance();
    }

    @Override
    protected String getDisplayNameProperty() {
        return "id";
    }

    @Begin(nested = true)
    public String viewControlReport(GeneratedPayroll generatedPayroll) {
        this.setGeneratedPayroll(generatedPayroll);
        return Outcome.SUCCESS;
    }

    public GeneratedPayroll getGeneratedPayroll() {
        return generatedPayroll;
    }

    public void setGeneratedPayroll(GeneratedPayroll generatedPayroll) {
        this.generatedPayroll = generatedPayroll;
    }
}