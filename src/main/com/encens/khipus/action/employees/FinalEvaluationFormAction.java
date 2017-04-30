package com.encens.khipus.action.employees;

import com.encens.khipus.framework.action.GenericAction;
import com.encens.khipus.framework.action.Outcome;
import com.encens.khipus.framework.service.GenericService;
import com.encens.khipus.model.employees.FinalEvaluationForm;
import com.encens.khipus.model.employees.FinalEvaluationFormType;
import com.encens.khipus.service.employees.FinalEvaluationFormService;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.*;
import org.jboss.seam.annotations.security.Restrict;
import org.jboss.seam.international.StatusMessage;

/**
 * FinalEvaluationFormAction
 *
 * @author
 * @version 2.8
 */
@Name("finalEvaluationFormAction")
@Scope(ScopeType.CONVERSATION)
public class FinalEvaluationFormAction extends GenericAction<FinalEvaluationForm> {

    @In
    private FinalEvaluationFormService finalEvaluationFormService;

    @Factory(value = "finalEvaluationForm", scope = ScopeType.STATELESS)
    @Restrict("#{s:hasPermission('FINALEVALUATIONFORM','VIEW')}")
    public FinalEvaluationForm initFinalEvaluationForm() {
        return getInstance();
    }

    @Factory(value = "finalEvaluationFormTypeEnum")
    public FinalEvaluationFormType[] getFinalEvaluationFormTypeEnum() {
        return FinalEvaluationFormType.values();
    }

    @Override
    protected String getDisplayNameProperty() {
        return "code";
    }

    @Override
    protected GenericService getService() {
        return finalEvaluationFormService;
    }

    @Override
    @End
    @Restrict("#{s:hasPermission('FINALEVALUATIONFORM','CREATE')}")
    public String create() {
        if (!validate()) {
            return Outcome.REDISPLAY;
        }
        return super.create();
    }

    @Override
    @Restrict("#{s:hasPermission('FINALEVALUATIONFORM','CREATE')}")
    public void createAndNew() {
        if (validate()) {
            super.createAndNew();
        }
    }

    @Override
    @End
    @Restrict("#{s:hasPermission('FINALEVALUATIONFORM','UPDATE')}")
    public String update() {
        if (!validate()) {
            return Outcome.REDISPLAY;
        }
        return super.update();
    }

    public Boolean validate() {
        Boolean valid = true;
        if (finalEvaluationFormService.isDuplicatedByCycleAndType(getInstance())) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                    "FinalEvaluationForm.error.duplicated",
                    getInstance().getCycle().getName(),
                    messages.get(getInstance().getType().getResourceKey()));
            valid = false;
        }
        return valid;
    }
}
