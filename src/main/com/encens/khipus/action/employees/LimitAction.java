package com.encens.khipus.action.employees;

import com.encens.khipus.framework.action.GenericAction;
import com.encens.khipus.model.employees.Limit;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.security.Restrict;

/**
 * Actions for Limit
 *
 * @author
 */

@Name("limitAction")
@Scope(ScopeType.CONVERSATION)
public class LimitAction extends GenericAction<Limit> {

    @Factory(value = "limit", scope = ScopeType.STATELESS)
    @Restrict("#{s:hasPermission('LIMIT','VIEW')}")
    public Limit initLimit() {
        return getInstance();
    }

    @Override
    protected String getDisplayNameProperty() {
        return "afterInit";
    }
}