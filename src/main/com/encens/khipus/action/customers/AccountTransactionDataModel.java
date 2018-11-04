package com.encens.khipus.action.customers;

import com.encens.khipus.framework.action.QueryDataModel;
import com.encens.khipus.model.customers.AccountTransaction;
import com.encens.khipus.model.customers.CreditTransaction;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import java.util.Arrays;
import java.util.List;

/**
 * Data model for Customer category
 *
 * @author:
 */

@Name("accountTransactionDataModel")
@Scope(ScopeType.PAGE)
public class AccountTransactionDataModel extends QueryDataModel<Long, AccountTransaction> {

    private static final String[] RESTRICTIONS =
            {"accountTransaction.account = #{account}"};
    @Create
    public void init() {
        sortProperty = "accountTransaction.date";
        sortAsc = false;
    }

    @Override
    public String getEjbql() {
        return "select accountTransaction from AccountTransaction accountTransaction";
    }

    @Override
    public List<String> getRestrictions() {
        return Arrays.asList(RESTRICTIONS);
    }
}
