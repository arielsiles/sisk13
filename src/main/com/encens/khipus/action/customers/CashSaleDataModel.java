package com.encens.khipus.action.customers;

import com.encens.khipus.framework.action.QueryDataModel;
import com.encens.khipus.model.customers.CashSale;
import com.encens.khipus.model.customers.Client;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import java.util.Arrays;
import java.util.List;

/**
 * ClientDataModel
 *
 * @author
 * @version 2.0
 */
@Name("cashSaleDataModel")
@Scope(ScopeType.PAGE)
public class CashSaleDataModel extends QueryDataModel<Long, CashSale> {

    private static final String[] RESTRICTIONS = {
            "lower(cashSale.code) like concat('%', concat(lower(#{cashSaleDataModel.criteria.code}), '%'))"
    };

    @Create
    public void init() {
        sortProperty = "cashSale.date";
        sortAsc = false;
    }

    @Override
    public String getEjbql() {
        return "select cashSale from CashSale cashSale";
    }

    @Override
    public List<String> getRestrictions() {
        return Arrays.asList(RESTRICTIONS);
    }

}
