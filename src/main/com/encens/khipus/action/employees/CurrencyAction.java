package com.encens.khipus.action.employees;

import com.encens.khipus.framework.action.GenericAction;
import com.encens.khipus.model.employees.Currency;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.security.Restrict;

/**
 * Actions for Currency
 *
 * @author
 * @version 1.1.10
 */

@Name("currencyAction")
@Scope(ScopeType.CONVERSATION)
public class CurrencyAction extends GenericAction<Currency> {

    @Factory(value = "currency", scope = ScopeType.STATELESS)
    @Restrict("#{s:hasPermission('CURRENCY','VIEW')}")
    public Currency initCurrency() {
        return getInstance();
    }

    @Override
    protected String getDisplayNameProperty() {
        return "symbol";
    }
}
