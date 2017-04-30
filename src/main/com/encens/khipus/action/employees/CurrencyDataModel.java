package com.encens.khipus.action.employees;

import com.encens.khipus.framework.action.QueryDataModel;
import com.encens.khipus.model.employees.Currency;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.security.Restrict;

import java.util.Arrays;
import java.util.List;

/**
 * Data model for Currency
 *
 * @author
 * @version 1.1.10
 */

@Name("currencyDataModel")
@Scope(ScopeType.PAGE)
@Restrict("#{s:hasPermission('CURRENCY','VIEW')}")
public class CurrencyDataModel extends QueryDataModel<Long, Currency> {
    private static final String[] RESTRICTIONS = {
            "lower(currency.name) like concat('%', concat(lower(#{currencyDataModel.criteria.name}), '%'))",
            "lower(currency.symbol) like concat(lower(#{currencyDataModel.criteria.symbol}), '%')",
            "lower(currency.currencyCode) like concat(lower(#{currencyDataModel.criteria.currencyCode}), '%')"};

    @Create
    public void init() {
        sortProperty = "currency.name";
    }

    @Override
    public String getEjbql() {
        return "select currency from Currency currency";
    }

    @Override
    public List<String> getRestrictions() {
        return Arrays.asList(RESTRICTIONS);
    }
}