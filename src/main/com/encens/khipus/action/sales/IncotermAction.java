package com.encens.khipus.action.sales;

import com.encens.khipus.framework.action.GenericAction;
import com.encens.khipus.model.sales.Incoterm;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.security.Restrict;

/**
 * Incoterm catalog action.
 *
 * @author
 * @version 1.0
 */
@Name("incotermAction")
@Scope(ScopeType.CONVERSATION)
public class IncotermAction extends GenericAction<Incoterm> {

    @Factory(value = "incoterm", scope = ScopeType.STATELESS)
    @Restrict("#{s:hasPermission('INCOTERM','VIEW')}")
    public Incoterm initIncoterm() {
        return getInstance();
    }

    @Override
    public String getDisplayNameProperty() {
        return "name";
    }
}
