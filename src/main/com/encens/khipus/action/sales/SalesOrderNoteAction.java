package com.encens.khipus.action.sales;

import com.encens.khipus.framework.action.GenericAction;
import com.encens.khipus.model.sales.SalesOrderNote;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.security.Restrict;

/**
 * Sales order note catalog action.
 *
 * @author
 * @version 1.0
 */
@Name("salesOrderNoteAction")
@Scope(ScopeType.CONVERSATION)
public class SalesOrderNoteAction extends GenericAction<SalesOrderNote> {

    @Factory(value = "salesOrderNote", scope = ScopeType.STATELESS)
    @Restrict("#{s:hasPermission('SALESORDERNOTE','VIEW')}")
    public SalesOrderNote initSalesOrderNote() {
        return getInstance();
    }

    @Override
    public String getDisplayNameProperty() {
        return "shortText";
    }
}
