package com.encens.khipus.action.finances;

import com.encens.khipus.framework.action.GenericAction;
import com.encens.khipus.framework.action.Outcome;
import com.encens.khipus.model.finances.CashAccount;
import com.encens.khipus.model.finances.CashAccountType;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.*;

@Name("cashAccountAction")
@Scope(ScopeType.CONVERSATION)
public class CashAccountAction extends GenericAction<CashAccount> {

    @Factory(value = "cashAccount", scope = ScopeType.STATELESS)
    public CashAccount initCashAccount() {
        return getInstance();
    }

    @Factory(value = "cashAccountTypes", scope = ScopeType.STATELESS)
    public CashAccountType[] getCashAccountTypes() {
        return CashAccountType.values();
    }

    @Override
    @Begin(ifOutcome = Outcome.SUCCESS, flushMode = FlushModeType.MANUAL)
    public String select(CashAccount instance){

        return super.select(instance);
    }
}
