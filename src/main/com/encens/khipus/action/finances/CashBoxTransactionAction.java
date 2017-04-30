package com.encens.khipus.action.finances;

import com.encens.khipus.framework.action.GenericAction;
import com.encens.khipus.framework.action.Outcome;
import com.encens.khipus.model.finances.CashBoxTransaction;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.*;
import org.jboss.seam.annotations.security.Restrict;

/**
 * Actions for Cash box transaction
 *
 * @author:
 */

@Name("cashBoxTransactionAction")
@Scope(ScopeType.CONVERSATION)
public class CashBoxTransactionAction extends GenericAction<CashBoxTransaction> {

    @Factory(value = "cashBoxTransaction", scope = ScopeType.STATELESS)
    @Restrict("#{s:hasPermission('CASHBOXTRANSACTION','VIEW')}")
    public CashBoxTransaction initCashBoxTransaction() {
        return getInstance();
    }

    @Override
    @Begin(ifOutcome = Outcome.SUCCESS, flushMode = FlushModeType.MANUAL)
    @Restrict("#{s:hasPermission('CASHBOXTRANSACTION','VIEW')}")
    public String select(CashBoxTransaction cashBoxTransaction) {
        return super.select(cashBoxTransaction);
    }

    @Override
    @End
    @Restrict("#{s:hasPermission('CASHBOXTRANSACTION','UPDATE')}")
    public String update() {
        return super.update();
    }
}
