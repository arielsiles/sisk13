package com.encens.khipus.action.customers;

import com.encens.khipus.framework.action.GenericAction;
import com.encens.khipus.framework.action.Outcome;
import com.encens.khipus.model.customers.AccountType;
import com.encens.khipus.model.customers.SavingType;
import com.encens.khipus.model.finances.CashAccount;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.*;
import org.jboss.seam.international.StatusMessage;

/**
 * ABM de tipos de cuenta de ahorro (tabla tipocuenta).
 * <p/>
 * De aca salen las cuentas contables que arman los asientos del modulo: CTACF_MN /
 * CTACF_ME son el pasivo de la provision mensual de intereses sobre DPF y CTAP_MN /
 * CTAP_ME el pasivo del capital. Por eso un tipo DPF no se puede guardar sin ellas.
 *
 * @author
 */
@SuppressWarnings({"SeamBijectionTypeMismatchInspection"})
@Name("accountTypeAction")
@Scope(ScopeType.CONVERSATION)
public class AccountTypeAction extends GenericAction<AccountType> {

    @Factory(value = "accountType", scope = ScopeType.STATELESS)
    public AccountType initAccountType() {
        return getInstance();
    }

    @Override
    protected String getDisplayNameProperty() {
        return "name";
    }

    @Override
    @Begin(ifOutcome = Outcome.SUCCESS, flushMode = FlushModeType.MANUAL)
    public String select(AccountType instance) {
        return super.select(instance);
    }

    @Override
    @End
    public String create() {
        if (!validateAccounts()) {
            return Outcome.REDISPLAY;
        }
        return super.create();
    }

    @Override
    @End
    public String update() {
        if (!validateAccounts()) {
            return Outcome.REDISPLAY;
        }
        return super.update();
    }

    /**
     * Un tipo DPF sin las cuentas de cargos financieros por pagar no puede armar el
     * asiento de la provision mensual. Conviene cortar en el alta y no a fin de mes,
     * cuando el error ya se arrastro por todos los certificados de ese tipo.
     */
    private boolean validateAccounts() {
        if (!SavingType.DPF.equals(getInstance().getSavingType())) {
            return true;
        }
        if (getInstance().getCashAccountChargeMn() == null
                || getInstance().getCashAccountChargeMe() == null) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                    "AccountType.error.chargeAccountsRequiredForDPF");
            return false;
        }
        return true;
    }

    public void clearCashAccountMn() {
        getInstance().setCashAccountMn(null);
    }

    public void clearCashAccountMe() {
        getInstance().setCashAccountMe(null);
    }

    public void clearCashAccountMv() {
        getInstance().setCashAccountMv(null);
    }

    public void clearCashAccountChargeMn() {
        getInstance().setCashAccountChargeMn(null);
    }

    public void clearCashAccountChargeMe() {
        getInstance().setCashAccountChargeMe(null);
    }

    public void assignCashAccountMn(CashAccount cashAccount) {
        getInstance().setCashAccountMn(cashAccount);
    }

    public void assignCashAccountMe(CashAccount cashAccount) {
        getInstance().setCashAccountMe(cashAccount);
    }

    public void assignCashAccountMv(CashAccount cashAccount) {
        getInstance().setCashAccountMv(cashAccount);
    }

    public void assignCashAccountChargeMn(CashAccount cashAccount) {
        getInstance().setCashAccountChargeMn(cashAccount);
    }

    public void assignCashAccountChargeMe(CashAccount cashAccount) {
        getInstance().setCashAccountChargeMe(cashAccount);
    }
}
