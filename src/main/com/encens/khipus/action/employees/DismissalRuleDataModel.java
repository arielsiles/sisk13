package com.encens.khipus.action.employees;

import com.encens.khipus.framework.action.QueryDataModel;
import com.encens.khipus.model.employees.DismissalRule;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import java.util.Arrays;
import java.util.List;

/**
 * @author
 * @version 3.5
 */
@Name("dismissalRuleDataModel")
@Scope(ScopeType.PAGE)
public class DismissalRuleDataModel extends QueryDataModel<Long, DismissalRule> {

    private static final String[] RESTRICTIONS = {
            "dismissalRule.code = #{dismissalRuleDataModel.criteria.code}",
            "dismissalRule.active = #{dismissalRuleDataModel.criteria.active}",
            "lower(dismissalRule.description) like concat('%', concat(lower(#{dismissalRuleDataModel.criteria.description}),'%'))",
            "lower(dismissalRule.name) like concat('%',concat(lower(#{dismissalRuleDataModel.criteria.name}), '%'))"
    };

    @Create
    public void init() {
        sortProperty = "dismissalRule.code";
    }

    @Override
    public String getEjbql() {
        return "select dismissalRule from DismissalRule dismissalRule";
    }

    @Override
    public List<String> getRestrictions() {
        return Arrays.asList(RESTRICTIONS);
    }
}