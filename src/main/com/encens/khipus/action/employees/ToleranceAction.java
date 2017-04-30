package com.encens.khipus.action.employees;

import com.encens.khipus.framework.action.GenericAction;
import com.encens.khipus.model.employees.Tolerance;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.security.Restrict;

/**
 * Actions for Tolerance
 *
 * @author
 */

@Name("toleranceAction")
@Scope(ScopeType.CONVERSATION)
public class ToleranceAction extends GenericAction<Tolerance> {

    @Factory(value = "tolerance", scope = ScopeType.STATELESS)
    @Restrict("#{s:hasPermission('TOLERANCE','VIEW')}")
    public Tolerance initTolerance() {
        return getInstance();
    }

    @Override
    protected String getDisplayNameProperty() {
        return "afterInit";
    }
}