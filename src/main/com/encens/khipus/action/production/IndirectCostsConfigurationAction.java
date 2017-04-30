package com.encens.khipus.action.production;

import com.encens.khipus.framework.action.GenericAction;
import com.encens.khipus.model.finances.CashAccount;
import com.encens.khipus.model.production.IndirectCostsConfig;
import com.encens.khipus.model.production.ProcessedProduct;
import com.encens.khipus.util.Constants;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

/**
 * Created with IntelliJ IDEA.
 * User: david
 * Date: 6/5/13
 * Time: 10:39 AM
 * To change this template use File | Settings | File Templates.
 */
@Name("indirectCostsConfigurationAction")
@Scope(ScopeType.CONVERSATION)
public class IndirectCostsConfigurationAction extends GenericAction<IndirectCostsConfig> {

    @Factory(value = "indirectCostsConfig", scope = ScopeType.STATELESS)
    public IndirectCostsConfig initProcessedProduct() {
        return getInstance();
    }

    @Override
    protected String getDisplayNameProperty() {
        return "description";
    }

    public void assignCashAccount(CashAccount cashAccount) {
        getInstance().setAccount(cashAccount.getAccountCode());
        getInstance().setCashAccount(cashAccount);
        getInstance().setCompanyNumber(Constants.COD_COMPANY_DEFAULT);
    }

    public void clearCashAccount()
    {
        getInstance().setAccount(null);
        getInstance().setCashAccount(null);
    }
}
