package com.encens.khipus.action.finances;

import com.encens.khipus.framework.action.GenericAction;
import com.encens.khipus.model.finances.AccountingStateType;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

/**
 * @author:
 */
@Name("accountingStateTypeAction")
@Scope(ScopeType.CONVERSATION)
public class AccountingStateTypeAction extends GenericAction<AccountingStateType> {

    @Factory(value = "accountingStateType", scope = ScopeType.STATELESS)
    public AccountingStateType initAccountingStateType() {
        return getInstance();
    }

    @Override
    protected String getDisplayNameProperty() {
        return "name";
    }
}
