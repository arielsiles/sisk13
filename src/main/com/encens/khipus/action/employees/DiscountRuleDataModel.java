package com.encens.khipus.action.employees;

import com.encens.khipus.framework.action.QueryDataModel;
import com.encens.khipus.model.employees.DiscountRule;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import java.util.Arrays;
import java.util.List;

/**
 * @author
 * @version 3.4
 */
@Name("discountRuleDataModel")
@Scope(ScopeType.PAGE)
public class DiscountRuleDataModel extends QueryDataModel<Long, DiscountRule> {
    private static final String[] RESTRICTIONS =
            {"lower(discountRule.name) like concat('%', concat(lower(#{discountRuleDataModel.criteria.name}), '%'))",
                    "discountRule.discountRuleType = #{discountRuleDataModel.criteria.discountRuleType}"
            };

    @Create
    public void init() {
        sortProperty = "discountRule.name";
    }

    @Override
    public String getEjbql() {
        return "SELECT discountRule FROM DiscountRule discountRule" +
                " LEFT JOIN discountRule.gestion gestion " +
                " LEFT JOIN discountRule.businessUnit businessUnit " +
                " LEFT JOIN businessUnit.organization organization " +
                " LEFT JOIN discountRule.currency currency " +
                " LEFT JOIN discountRule.jobCategory jobCategory ";
    }

    @Override
    public List<String> getRestrictions() {
        return Arrays.asList(RESTRICTIONS);
    }
}
