package com.encens.khipus.action.customers;

import com.encens.khipus.framework.action.QueryDataModel;
import com.encens.khipus.model.customers.CustomerDiscountRule;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.security.Restrict;

import java.util.Arrays;
import java.util.List;

/**
 * Data model for Customer Discount Rule
 *
 * @author:
 */

@Name("customerDiscountRuleDataModel")
@Scope(ScopeType.PAGE)
@Restrict("#{s:hasPermission('CUSTOMERDISCOUNTRULE','VIEW')}")
public class CustomerDiscountRuleDataModel extends QueryDataModel<Long, CustomerDiscountRule> {

    private static final String[] RESTRICTIONS =
            {"lower(customerDiscountRule.name) like concat('%', concat(lower(#{customerDiscountRuleDataModel.criteria.name}), '%'))"};

    @Create
    public void init() {
        sortProperty = "customerDiscountRule.name";
    }

    @Override
    public String getEjbql() {
        return "select customerDiscountRule from CustomerDiscountRule customerDiscountRule";
    }

    @Override
    public List<String> getRestrictions() {
        return Arrays.asList(RESTRICTIONS);
    }
}
