package com.encens.khipus.action.employees;

import com.encens.khipus.framework.action.QueryDataModel;
import com.encens.khipus.model.employees.EvaluationCriteriaValue;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.security.Restrict;

import java.util.Arrays;
import java.util.List;

/**
 * @author
 * @version 1.0.18
 */
@Name("evaluationCriteriaValueDataModel")
@Scope(ScopeType.PAGE)
@Restrict("#{s:hasPermission('EVALUATIONCRITERIAVALUE','VIEW')}")
public class EvaluationCriteriaValueDataModel extends QueryDataModel<Long, EvaluationCriteriaValue> {
    private static final String[] RESTRICTIONS =
            {"evaluationCriteriaValue.evaluationCriteria = #{evaluationCriteria}"};

    @Create
    public void init() {
        sortProperty = "evaluationCriteriaValue.title";
    }

    @Override
    public String getEjbql() {
        return "select evaluationCriteriaValue from EvaluationCriteriaValue evaluationCriteriaValue";
    }

    @Override
    public List<String> getRestrictions() {
        return Arrays.asList(RESTRICTIONS);
    }
}
