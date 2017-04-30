package com.encens.khipus.action.finances;

import com.encens.khipus.framework.action.GenericAction;
import com.encens.khipus.model.finances.CashBoxStatusType;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

/**
 * @author:
 */
@Name("cashBoxStatusTypeAction")
@Scope(ScopeType.CONVERSATION)
public class CashBoxStatusTypeAction extends GenericAction<CashBoxStatusType> {

    @Factory(value = "cashBoxStatusType", scope = ScopeType.STATELESS)
    public CashBoxStatusType initCashBoxStatusType() {
        return getInstance();
    }

    @Override
    protected String getDisplayNameProperty() {
        return "name";
    }
}
