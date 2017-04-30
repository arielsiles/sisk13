package com.encens.khipus.action.employees;

import com.encens.khipus.framework.action.GenericAction;
import com.encens.khipus.model.employees.EvaluationCriteria;
import com.encens.khipus.model.employees.EvaluationCriteriaValue;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.*;
import org.jboss.seam.annotations.security.Restrict;
import org.jboss.seam.framework.EntityQuery;


/**
 * @author
 * @version 1.0.18
 */

@Name("evaluationCriteriaValueAction")
@Scope(ScopeType.CONVERSATION)
public class EvaluationCriteriaValueAction extends GenericAction<EvaluationCriteriaValue> {
    private static final Integer ONE = 1;
    private static final Integer ZERO = 0;

    @In(value = "evaluationCriteriaAction")
    private EvaluationCriteriaAction evaluationCriteriaAction;


    @In(value = "countEvaluationCriteriaValueQuery", create = true)
    private EntityQuery countEvaluationCriteriaValueQuery;

    @In(value = "maxEvaluationCriteriaValueSequence", create = true)
    private EntityQuery maxEvaluationCriteriaValueSequence;

    @Factory(value = "evaluationCriteriaValue", scope = ScopeType.STATELESS)
    @Restrict("#{s:hasPermission('EVALUATIONCRITERIAVALUE','VIEW')}")
    public EvaluationCriteriaValue initEvaluationCriteriaValue() {
        return getInstance();
    }

    @Begin(nested = true, flushMode = FlushModeType.MANUAL)
    public String assignCriteriaValue() {
        EvaluationCriteria evaluationCriteria = getEvaluationCriteria();
        getInstance().setEvaluationCriteria(evaluationCriteria);
        return com.encens.khipus.framework.action.Outcome.SUCCESS;
    }

    @Override
    @Begin(nested = true, flushMode = FlushModeType.MANUAL)
    @Restrict("#{s:hasPermission('EVALUATIONCRITERIAVALUE','VIEW')}")
    public String select(EvaluationCriteriaValue instance) {
        return super.select(instance);
    }

    @Override
    @Restrict("#{s:hasPermission('EVALUATIONCRITERIAVALUE','CREATE')}")
    public String create() {
        Integer lastSequence = ONE;

        if (!isTheFirstValue()) {
            lastSequence = new Integer(maxEvaluationCriteriaValueSequence.getSingleResult().toString()) + ONE;
        }

        getInstance().setSequence(lastSequence);

        String outcome = super.create();
        closeConversation(outcome);

        return outcome;
    }

    @Override
    @Restrict("#{s:hasPermission('EVALUATIONCRITERIAVALUE','UPDATE')}")
    public String update() {
        String outcome = super.update();
        closeConversation(outcome);

        return outcome;
    }

    @Override
    @Restrict("#{s:hasPermission('EVALUATIONCRITERIAVALUE','DELETE')}")
    public String delete() {
        String outcome = super.delete();
        closeConversation(outcome);

        return outcome;
    }

    @Override
    @End(beforeRedirect = true)
    public String cancel() {
        return super.cancel();
    }

    public EvaluationCriteria getEvaluationCriteria() {
        return evaluationCriteriaAction.getInstance();
    }

    public Integer getSequence() {
        if (isManaged()) {
            return getInstance().getSequence();
        }

        if (isTheFirstValue()) {
            return ONE;
        }

        return (new Integer(maxEvaluationCriteriaValueSequence.getSingleResult().toString()) + ONE);
    }

    @Override
    protected String getDisplayNameProperty() {
        return "title";
    }

    private boolean isTheFirstValue() {
        Integer counter = new Integer(countEvaluationCriteriaValueQuery.getSingleResult().toString());
        return ZERO.equals(counter);
    }
}
