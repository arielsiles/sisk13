package com.encens.khipus.action.sales;

import com.encens.khipus.framework.action.GenericAction;
import com.encens.khipus.model.sales.SalesOrderTerm;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.security.Restrict;

/**
 * Sales order header term catalog action.
 *
 * @author
 * @version 1.0
 */
@Name("salesOrderTermAction")
@Scope(ScopeType.CONVERSATION)
public class SalesOrderTermAction extends GenericAction<SalesOrderTerm> {

    @Factory(value = "salesOrderTerm", scope = ScopeType.STATELESS)
    @Restrict("#{s:hasPermission('SALESORDERTERM','VIEW')}")
    public SalesOrderTerm initSalesOrderTerm() {
        return getInstance();
    }

    @Override
    public String getDisplayNameProperty() {
        return "shortText";
    }
}
