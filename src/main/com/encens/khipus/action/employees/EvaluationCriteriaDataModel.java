package com.encens.khipus.action.employees;

import com.encens.khipus.framework.action.QueryDataModel;
import com.encens.khipus.model.employees.EvaluationCriteria;
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

@Name("evaluationCriteriaDataModel")
@Scope(ScopeType.PAGE)
@Restrict("#{s:hasPermission('EVALUATIONCRITERIA','VIEW')}")
public class EvaluationCriteriaDataModel extends QueryDataModel<Long, EvaluationCriteria> {
    private static final String[] RESTRICTIONS =
            {"lower(evaluationCriteria.name) like concat('%', concat(lower(#{evaluationCriteriaDataModel.criteria.name}), '%'))"};

    @Create
    public void init() {
        sortProperty = "evaluationCriteria.name";
    }

    @Override
    public String getEjbql() {
        return "select evaluationCriteria from EvaluationCriteria evaluationCriteria";
    }

    @Override
    public List<String> getRestrictions() {
        return Arrays.asList(RESTRICTIONS);
    }
}
