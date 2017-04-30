package com.encens.khipus.action.employees;

import com.encens.khipus.exception.ConcurrencyException;
import com.encens.khipus.exception.EntryDuplicatedException;
import com.encens.khipus.exception.EntryNotFoundException;
import com.encens.khipus.exception.ReferentialIntegrityException;
import com.encens.khipus.exception.employees.VacationRuleOverlapException;
import com.encens.khipus.framework.action.GenericAction;
import com.encens.khipus.framework.action.Outcome;
import com.encens.khipus.model.employees.VacationRule;
import com.encens.khipus.service.employees.VacationRuleService;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.*;
import org.jboss.seam.annotations.security.Restrict;
import org.jboss.seam.international.StatusMessage;

/**
 * @author
 * @version 3.4
 */
@Name("vacationRuleAction")
@Scope(ScopeType.CONVERSATION)
public class VacationRuleAction extends GenericAction<VacationRule> {

    @In
    private VacationRuleService vacationRuleService;

    @Create
    public void initialize() {
        if (!isManaged()) {
            getInstance().setFromYears(calculateStartYearRangeValue());
        }
    }

    @Factory(value = "vacationRule", scope = ScopeType.STATELESS)
    public VacationRule initVacationRule() {
        return getInstance();
    }

    @Override
    protected String getDisplayNameMessage() {
        return getInstance().getFullName();
    }

    @Override
    @Restrict("#{s:hasPermission('VACATIONRULE','VIEW')}")
    @Begin(ifOutcome = Outcome.SUCCESS, flushMode = FlushModeType.MANUAL)
    public String select(VacationRule instance) {
        return super.select(instance);
    }

    @Override
    @End
    @Restrict("#{s:hasPermission('VACATIONRULE','CREATE')}")
    public String create() {
        try {
            vacationRuleService.createVacationRule(getInstance());
            addCreatedMessage();
            return Outcome.SUCCESS;
        } catch (EntryDuplicatedException e) {
            addDuplicatedMessage();
            return Outcome.REDISPLAY;
        } catch (VacationRuleOverlapException e) {
            addVacationRuleOverlapMessage(e);
            return Outcome.REDISPLAY;
        }
    }

    @Override
    @Restrict("#{s:hasPermission('VACATIONRULE','CREATE')}")
    public void createAndNew() {
        try {
            vacationRuleService.createVacationRule(getInstance());
            addCreatedMessage();
            createInstance();
            initialize();
        } catch (EntryDuplicatedException e) {
            addDuplicatedMessage();
        } catch (VacationRuleOverlapException e) {
            addVacationRuleOverlapMessage(e);
        }
    }

    @Override
    @End
    @Restrict("#{s:hasPermission('VACATIONRULE','UPDATE')}")
    public String update() {
        Long currentVersion = (Long) getVersion(getInstance());
        try {
            vacationRuleService.updateVacationRule(getInstance());
        } catch (EntryDuplicatedException e) {
            addDuplicatedMessage();
            setVersion(getInstance(), currentVersion);
            return Outcome.REDISPLAY;
        } catch (ConcurrencyException e) {
            concurrencyLog();
            try {
                setInstance(getService().findById(getEntityClass(), getId(getInstance()), true));
            } catch (EntryNotFoundException e1) {
                entryNotFoundLog();
                addNotFoundMessage();
                return Outcome.FAIL;
            }
            addUpdateConcurrencyMessage();
            return Outcome.REDISPLAY;
        } catch (VacationRuleOverlapException e) {
            addVacationRuleOverlapMessage(e);
            return Outcome.REDISPLAY;
        }

        addUpdatedMessage();
        return Outcome.SUCCESS;
    }

    @Override
    @End
    @Restrict("#{s:hasPermission('VACATIONRULE','DELETE')}")
    public String delete() {
        try {
            vacationRuleService.deleteVacationRule(getInstance());
            addDeletedMessage();
        } catch (ConcurrencyException e) {
            entryNotFoundLog();
            addDeleteConcurrencyMessage();
        } catch (ReferentialIntegrityException e) {
            referentialIntegrityLog();
            addDeleteReferentialIntegrityMessage();
        }

        return Outcome.SUCCESS;
    }

    private int calculateStartYearRangeValue() {
        int startYearRange = 1;
        VacationRule lastVacationRule = vacationRuleService.findLastVacationRule();
        if (lastVacationRule != null) {
            startYearRange = lastVacationRule.getToYears() != null ? lastVacationRule.getToYears() + 1 : lastVacationRule.getFromYears();
        }

        return startYearRange;
    }

    public boolean isNewOrLastVacationRule() {
        boolean isNewOrLast;
        if (isManaged()) {
            VacationRule lastVacationRule = vacationRuleService.findLastVacationRule();
            isNewOrLast = lastVacationRule != null && lastVacationRule.getId().equals(getInstance().getId());
        } else {
            isNewOrLast = true;
        }
        return isNewOrLast;
    }

    private void addVacationRuleOverlapMessage(VacationRuleOverlapException e) {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR, "VacationRule.error.overlap", e.getOverlapRuleName());
    }

}
