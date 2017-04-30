package com.encens.khipus.action.finances;

import com.encens.khipus.framework.action.GenericAction;
import com.encens.khipus.model.finances.CashBoxType;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

/**
 * Actions for Cash box type
 *
 * @author:
 */

@Name("cashBoxTypeAction")
@Scope(ScopeType.CONVERSATION)
public class CashBoxTypeAction extends GenericAction<CashBoxType> {

    @Factory(value = "cashBoxType", scope = ScopeType.STATELESS)
    public CashBoxType initCashBoxType() {
        return getInstance();
    }

    @Override
    protected String getDisplayNameProperty() {
        return "name";
    }
}
