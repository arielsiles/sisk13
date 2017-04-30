package com.encens.khipus.action.employees;

import com.encens.khipus.framework.action.QueryDataModel;
import com.encens.khipus.model.employees.VacationRule;
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
@Name("vacationRuleDataModel")
@Scope(ScopeType.PAGE)
public class VacationRuleDataModel extends QueryDataModel<Long, VacationRule> {
    private static final String[] RESTRICTIONS =
            {"lower(vacationRule.name) like concat('%', concat(lower(#{vacationRuleDataModel.criteria.name}), '%'))",
                    "vacationRule.code = #{vacationRuleDataModel.criteria.code}"};

    @Create
    public void init() {
        sortProperty = "vacationRule.code";
    }

    @Override
    public String getEjbql() {
        return "select vacationRule from VacationRule vacationRule";
    }

    @Override
    public List<String> getRestrictions() {
        return Arrays.asList(RESTRICTIONS);
    }
}
