package com.encens.khipus.action.finances;

import com.encens.khipus.exception.EntryNotFoundException;
import com.encens.khipus.framework.action.GenericAction;
import com.encens.khipus.model.finances.CashAccount;
import com.encens.khipus.model.finances.FinancesBankAccount;
import com.encens.khipus.util.Constants;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.security.Restrict;

/**
 * CRUD de cuentas bancarias (ck_ctas_bco / FinancesBankAccount).
 *
 * @author
 * @version 1.0
 */
@Name("financesBankAccountAction")
@Scope(ScopeType.CONVERSATION)
public class FinancesBankAccountAction extends GenericAction<FinancesBankAccount> {

    @Factory(value = "financesBankAccount", scope = ScopeType.STATELESS)
    @Restrict("#{s:hasPermission('FINANCESBANKACCOUNT','VIEW')}")
    public FinancesBankAccount initFinancesBankAccount() {
        return getInstance();
    }

    /**
     * Una cuenta nueva nace vigente y en la compania actual. La PK
     * (no_cia, cta_bco): el no_cia se fija aca; el cta_bco lo escribe el usuario.
     * super.createInstance() crea la entidad y la fija como instance; aca solo se
     * completan los defaults.
     */
    @Override
    public FinancesBankAccount createInstance() {
        FinancesBankAccount instance = super.createInstance();
        instance.setActive(true);
        instance.getId().setCompanyNumber(Constants.defaultCompanyNumber);
        return instance;
    }

    /**
     * Asigna la cuenta contable elegida en el popup. setCashAccount tambien
     * escribe accountingAccountCode (el codigo es la columna `cuenta` escribible;
     * la asociacion es de solo lectura).
     */
    public void assignCashAccount(CashAccount cashAccount) {
        try {
            cashAccount = getService().findById(CashAccount.class, cashAccount.getId());
        } catch (EntryNotFoundException e) {
            entryNotFoundLog();
        }
        getInstance().setCashAccount(cashAccount);
    }

    public void clearCashAccount() {
        getInstance().setCashAccount(null);
    }

    @Override
    protected String getDisplayNameProperty() {
        return "accountNumber";
    }
}
