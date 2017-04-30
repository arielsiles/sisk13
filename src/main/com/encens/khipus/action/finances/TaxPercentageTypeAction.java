package com.encens.khipus.action.finances;

import com.encens.khipus.framework.action.GenericAction;
import com.encens.khipus.model.finances.TaxPercentageType;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

/**
 * Actions for Tax percentage type
 *
 * @author:
 */

@Name("taxPercentageTypeAction")
@Scope(ScopeType.CONVERSATION)
public class TaxPercentageTypeAction extends GenericAction<TaxPercentageType> {

    @Factory(value = "taxPercentageType", scope = ScopeType.STATELESS)
    public TaxPercentageType initTaxPercentageType() {
        return getInstance();
    }

    @Override
    protected String getDisplayNameProperty() {
        return "name";
    }
}
