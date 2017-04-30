package com.encens.khipus.action.employees;

import com.encens.khipus.framework.action.GenericAction;
import com.encens.khipus.framework.action.Outcome;
import com.encens.khipus.model.employees.Gestion;
import com.encens.khipus.service.employees.GeneratedPayrollService;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.*;
import org.jboss.seam.annotations.security.Restrict;
import org.jboss.seam.international.StatusMessage;

/**
 * Actions for Gestion
 *
 * @author
 */

@Name("gestionAction")
@Scope(ScopeType.CONVERSATION)
public class GestionAction extends GenericAction<Gestion> {
    @In
    private GeneratedPayrollService generatedPayrollService;

    @Factory(value = "gestion", scope = ScopeType.STATELESS)
    @Restrict("#{s:hasPermission('GESTION','VIEW')}")
    public Gestion initGestion() {
        return getInstance();
    }

    @Override
    protected String getDisplayNameProperty() {
        return "year";
    }

    @Override
    @End
    @Restrict("#{s:hasPermission('GESTION','UPDATE')}")
    public String update() {
        /* if there is at least one generated payroll, it is not possible to update the payroll*/
        if (generatedPayrollService.findGeneratedPayrollsByGestion(getInstance()).size() > 0) {
            addCannotUpdatedMessage();
            return Outcome.REDISPLAY;
        } else {
            return super.update();
        }
    }

    protected void addCannotUpdatedMessage() {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.INFO,
                "Gestion.message.cannotupdate", getInstance().getYear());
    }
}