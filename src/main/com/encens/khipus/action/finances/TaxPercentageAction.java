package com.encens.khipus.action.finances;

import com.encens.khipus.framework.action.GenericAction;
import com.encens.khipus.model.finances.TaxPercentage;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

/**
 * TaxPercentageAction
 *
 * @author:
 */

@Name("taxPercentageAction")
@Scope(ScopeType.CONVERSATION)
public class TaxPercentageAction extends GenericAction<TaxPercentage> {

    @Factory(value = "taxPercentage", scope = ScopeType.STATELESS)
    public TaxPercentage initTaxPercentage() {
        return getInstance();
    }

    @Override
    protected String getDisplayNameProperty() {
        return "percentage";
    }
}
