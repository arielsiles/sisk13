package com.encens.khipus.action.finances;

import com.encens.khipus.exception.ConcurrencyException;
import com.encens.khipus.exception.EntryDuplicatedException;
import com.encens.khipus.framework.action.GenericAction;
import com.encens.khipus.framework.action.Outcome;
import com.encens.khipus.model.finances.CashAccount;
import com.encens.khipus.model.finances.CashAccountType;
import com.encens.khipus.model.finances.FinancesCurrencyType;
import com.encens.khipus.service.finances.CashAccountService;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.*;

@Name("cashAccountAction")
@Scope(ScopeType.CONVERSATION)
public class CashAccountAction extends GenericAction<CashAccount> {

    @In
    private CashAccountService cashAccountService;

    @Factory(value = "cashAccount", scope = ScopeType.STATELESS)
    public CashAccount initCashAccount() {
        return getInstance();
    }

    @Factory(value = "cashAccountTypes", scope = ScopeType.STATELESS)
    public CashAccountType[] getCashAccountTypes() {
        return CashAccountType.values();
    }

    @Factory(value = "financesCurrencyTypes", scope = ScopeType.STATELESS)
    public FinancesCurrencyType[] getFinancesCurrencyTypes() {
        return FinancesCurrencyType.values();
    }

    @Override
    @Begin(ifOutcome = Outcome.SUCCESS, flushMode = FlushModeType.MANUAL)
    public String select(CashAccount instance){

        return super.select(instance);
    }

    @Override
    @End
    public String update() {
        try {
            getService().update(getInstance());
        } catch (EntryDuplicatedException e) {
            e.printStackTrace();
        } catch (ConcurrencyException e) {
            e.printStackTrace();
        }
        return Outcome.SUCCESS;
    }

    @Override
    @End
    public String create() {

        Boolean result = cashAccountService.createCashAccount(getInstance());

        if (result) {
            addCreatedMessage();
            return Outcome.SUCCESS;
        } else {
            addDuplicatedMessage();
            return Outcome.REDISPLAY;
        }

    }

    public void assignRootCashAccount(CashAccount cashAccount) {
        getInstance().setRootCashAccount(cashAccount);
    }
}
