package com.encens.khipus.action.finances;

import com.encens.khipus.framework.action.GenericAction;
import com.encens.khipus.model.finances.TaxRule;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

/**
 * TaxRuleAction
 *
 * @author:
 */
@Name("taxRuleAction")
@Scope(ScopeType.CONVERSATION)
public class TaxRuleAction extends GenericAction<TaxRule> {

    @Factory(value = "taxRule", scope = ScopeType.STATELESS)
    public TaxRule initTaxRule() {
        return getInstance();
    }

    @Override
    protected String getDisplayNameProperty() {
        return "orderNumber";
    }

}
