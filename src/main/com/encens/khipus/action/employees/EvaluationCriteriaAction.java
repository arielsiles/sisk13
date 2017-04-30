package com.encens.khipus.action.employees;

import com.encens.khipus.framework.action.GenericAction;
import com.encens.khipus.model.employees.EvaluationCriteria;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.security.Restrict;

/**
 * @author
 * @version 1.0.18
 */

@Name("evaluationCriteriaAction")
@Scope(ScopeType.CONVERSATION)
public class EvaluationCriteriaAction extends GenericAction<EvaluationCriteria> {

    @Factory(value = "evaluationCriteria", scope = ScopeType.STATELESS)
    @Restrict("#{s:hasPermission('EVALUATIONCRITERIA','VIEW')}")
    public EvaluationCriteria initEvaluationCriteria() {
        return getInstance();
    }

    @Override
    protected String getDisplayNameProperty() {
        return "name";
    }
}
