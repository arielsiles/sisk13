package com.encens.khipus.action.customers;

import com.encens.khipus.framework.action.GenericAction;
import com.encens.khipus.framework.action.Outcome;
import com.encens.khipus.model.customers.CreditType;
import com.encens.khipus.model.finances.CashAccount;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.*;

/**
 * @author
 * @version 3.4
 */
@SuppressWarnings({"SeamBijectionTypeMismatchInspection"})
@Name("creditTypeAction")
@Scope(ScopeType.CONVERSATION)
public class CreditTypeAction extends GenericAction<CreditType> {


    @Factory(value = "creditType", scope = ScopeType.STATELESS)
    public CreditType initCreditType() {
        return getInstance();
    }

    @Override
    protected String getDisplayNameProperty() {
        return "name";
    }

    @Override
    @Begin(ifOutcome = Outcome.SUCCESS, flushMode = FlushModeType.MANUAL)
    public String select(CreditType instance) {
        String outcome = super.select(instance);
        return outcome;
    }

    public void clearCurrentAccount() {
        getInstance().setCurrentAccount(null);
    }

    public void clearExpiredAccount() {
        getInstance().setExpiredAccount(null);
    }

    public void clearExecutedAccount() {
        getInstance().setExecutedAccount(null);
    }

    public void clearCurrentInterestAccount() {
        getInstance().setCurrentInterestAccount(null);
    }

    public void clearExpiredInterestAccount() {
        getInstance().setExpiredInterestAccount(null);
    }


    public void assignCurrentAccount(CashAccount cashAccount) {
        getInstance().setCurrentAccount(cashAccount);
    }

    public void assignExpiredAccount(CashAccount cashAccount) {
        getInstance().setExpiredAccount(cashAccount);
    }

    public void assignExecutedAccount(CashAccount cashAccount) {
        getInstance().setExecutedAccount(cashAccount);
    }

    public void assignCurrentInterestAccount(CashAccount cashAccount) {
        getInstance().setCurrentInterestAccount(cashAccount);
    }

    public void assignExpiredInterestAccount(CashAccount cashAccount) {
        getInstance().setExpiredInterestAccount(cashAccount);
    }

}
