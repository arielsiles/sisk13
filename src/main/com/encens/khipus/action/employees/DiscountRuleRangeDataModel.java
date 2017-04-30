package com.encens.khipus.action.employees;

import com.encens.khipus.framework.action.QueryDataModel;
import com.encens.khipus.model.employees.DiscountRuleRange;
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
@Name("discountRuleRangeDataModel")
@Scope(ScopeType.PAGE)
public class DiscountRuleRangeDataModel extends QueryDataModel<Long, DiscountRuleRange> {

    private static final String[] RESTRICTIONS = {
            "discountRule = #{discountRule}"
    };

    @Create
    public void init() {
        sortProperty = "discountRuleRange.sequence";
    }

    @Override
    public String getEjbql() {
        return "SELECT discountRuleRange FROM DiscountRuleRange discountRuleRange" +
                " LEFT JOIN discountRuleRange.discountRule discountRule";
    }

    @Override
    public List<String> getRestrictions() {
        return Arrays.asList(RESTRICTIONS);
    }
}
