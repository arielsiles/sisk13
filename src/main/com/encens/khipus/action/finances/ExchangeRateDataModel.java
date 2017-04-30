package com.encens.khipus.action.finances;

import com.encens.khipus.framework.action.QueryDataModel;
import com.encens.khipus.model.finances.ExchangeRate;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import java.util.Arrays;
import java.util.List;

/**
 * ExchangeRateDataModel
 *
 * @author
 */

@Name("exchangeRateDataModel")
@Scope(ScopeType.PAGE)
public class ExchangeRateDataModel extends QueryDataModel<Long, ExchangeRate> {

    private static final String[] RESTRICTIONS =
            {"exchangeRate.rate = #{exchangeRateDataModel.criteria.rate}"};

    @Create
    public void init() {
        sortProperty = "exchangeRate.rate";
    }

    @Override
    public String getEjbql() {
        return "select exchangeRate from ExchangeRate exchangeRate";
    }

    @Override
    public List<String> getRestrictions() {
        return Arrays.asList(RESTRICTIONS);
    }
}