package com.encens.khipus.action.employees;

import com.encens.khipus.framework.action.GenericAction;
import com.encens.khipus.framework.action.Outcome;
import com.encens.khipus.model.employees.FinalEvaluationPunctuationRange;
import com.encens.khipus.service.employees.FinalEvaluationPunctuationRangeService;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.*;
import org.jboss.seam.annotations.security.Restrict;
import org.jboss.seam.international.StatusMessage;

/**
 * FinalEvaluationPunctuationRangeAction
 *
 * @author
 * @version 2.8
 */
@Name("finalEvaluationPunctuationRangeAction")
@Scope(ScopeType.CONVERSATION)
public class FinalEvaluationPunctuationRangeAction extends GenericAction<FinalEvaluationPunctuationRange> {

    @In
    private FinalEvaluationPunctuationRangeService finalEvaluationPunctuationRangeService;

    @In
    private FinalEvaluationFormAction finalEvaluationFormAction;

    @Factory(value = "finalEvaluationPunctuationRange", scope = ScopeType.STATELESS)
    @Restrict("#{s:hasPermission('FEPUNCTUATIONRANGE','VIEW')}")
    public FinalEvaluationPunctuationRange initFinalEvaluationPunctuationRange() {
        return getInstance();
    }

    @Override
    protected String getDisplayNameProperty() {
        return "name";
    }

    @Begin(flushMode = FlushModeType.MANUAL, nested = true)
    public String newInstance() {
        createInstance();
        return Outcome.SUCCESS;
    }

    @Override
    @Begin(ifOutcome = Outcome.SUCCESS, flushMode = FlushModeType.MANUAL, nested = true)
    @Restrict("#{s:hasPermission('FEPUNCTUATIONRANGE','VIEW')}")
    public String select(FinalEvaluationPunctuationRange instance) {
        return super.select(instance);
    }

    @Override
    @End(beforeRedirect = true)
    @Restrict("#{s:hasPermission('FEPUNCTUATIONRANGE','CREATE')}")
    public String create() {
        getInstance().setFinalEvaluationForm(finalEvaluationFormAction.getInstance());
        if (!validate()) {
            return Outcome.REDISPLAY;
        }
        return super.create();
    }

    @Override
    @Restrict("#{s:hasPermission('FEPUNCTUATIONRANGE','CREATE')}")
    public void createAndNew() {
        getInstance().setFinalEvaluationForm(finalEvaluationFormAction.getInstance());
        if (validate()) {
            super.createAndNew();
        }
    }

    @Override
    @End(beforeRedirect = true)
    @Restrict("#{s:hasPermission('FEPUNCTUATIONRANGE','UPDATE')}")
    public String update() {
        if (!validate()) {
            return Outcome.REDISPLAY;
        }
        return super.update();
    }

    @Override
    @End(beforeRedirect = true)
    @Restrict("#{s:hasPermission('FEPUNCTUATIONRANGE','DELETE')}")
    public String delete() {
        return super.delete();
    }

    @Override
    @End(beforeRedirect = true)
    public String cancel() {
        return super.cancel();
    }

    public Boolean validate() {
        Boolean valid = true;
        if (finalEvaluationPunctuationRangeService.isOverlapRange(getInstance())) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                    "FinalEvaluationPunctuationRange.error.overlap",
                    getInstance().getStartRange(),
                    getInstance().getEndRange());
            valid = false;
        }
        if (finalEvaluationPunctuationRangeService.isDuplicatedByName(getInstance())) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                    "FinalEvaluationPunctuationRange.error.duplicatedName",
                    getInstance().getName());
            valid = false;
        }
        if (finalEvaluationPunctuationRangeService.isDuplicatedByInterpretation(getInstance())) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                    "FinalEvaluationPunctuationRange.error.duplicatedInterpretation",
                    getInstance().getInterpretation());
            valid = false;
        }
        return valid;
    }
}
