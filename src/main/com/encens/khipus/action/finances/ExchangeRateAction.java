package com.encens.khipus.action.finances;

import com.encens.khipus.framework.action.GenericAction;
import com.encens.khipus.model.finances.ExchangeRate;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

/**
 * ExchangeRateAction
 *
 * @author
 */
@Name("exchangeRateAction")
@Scope(ScopeType.CONVERSATION)
public class ExchangeRateAction extends GenericAction<ExchangeRate> {

    @Factory(value = "exchangeRate", scope = ScopeType.STATELESS)
    public ExchangeRate initExchangeRate() {
        return getInstance();
    }

    @Override
    protected String getDisplayNameProperty() {
        return "exchangeRate.rate";
    }

}