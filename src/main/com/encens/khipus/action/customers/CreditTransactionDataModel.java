package com.encens.khipus.action.customers;

import com.encens.khipus.framework.action.QueryDataModel;
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

@Name("creditTransactionDataModel")
@Scope(ScopeType.PAGE)
public class CreditTransactionDataModel extends QueryDataModel<Long, CreditTransaction> {

    private static final String[] RESTRICTIONS =
            {"creditTransaction.credit = #{credit}"};
    @Create
    public void init() {
        sortProperty = "creditTransaction.id";
        sortAsc = false;
    }

    @Override
    public String getEjbql() {
        return "select creditTransaction from CreditTransaction creditTransaction";
    }

    @Override
    public List<String> getRestrictions() {
        return Arrays.asList(RESTRICTIONS);
    }
}
