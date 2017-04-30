package com.encens.khipus.action.finances;

import com.encens.khipus.framework.action.QueryDataModel;
import com.encens.khipus.model.finances.TaxRule;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import java.util.Arrays;
import java.util.List;

/**
 * TaxRuleDataModel
 *
 * @author:
 */

@Name("taxRuleDataModel")
@Scope(ScopeType.PAGE)
public class TaxRuleDataModel extends QueryDataModel<Long, TaxRule> {

    private static final String[] RESTRICTIONS =
            {"lower(taxRule.orderNumber) like concat(lower(#{taxRuleDataModel.criteria.orderNumber}), '%')"};

    @Create
    public void init() {
        sortProperty = "taxRule.orderNumber";
    }

    @Override
    public String getEjbql() {
        return "select taxRule from TaxRule taxRule";
    }

    @Override
    public List<String> getRestrictions() {
        return Arrays.asList(RESTRICTIONS);
    }
}
