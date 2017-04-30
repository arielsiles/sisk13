package com.encens.khipus.action.employees;

import com.encens.khipus.exception.EntryDuplicatedException;
import com.encens.khipus.exception.employees.DuplicatedScheduleEvaluationCycleException;
import com.encens.khipus.exception.employees.DuplicatedScheduleEvaluationNameException;
import com.encens.khipus.framework.action.GenericAction;
import com.encens.khipus.framework.action.Outcome;
import com.encens.khipus.model.employees.PollFormType;
import com.encens.khipus.model.employees.ScheduleEvaluation;
import com.encens.khipus.model.employees.ScheduleEvaluationForm;
import com.encens.khipus.service.employees.ScheduleEvaluationService;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.*;
import org.jboss.seam.annotations.security.Restrict;
import org.jboss.seam.international.StatusMessage;

import java.util.Date;

/**
 * ScheduleEvaluationAction
 *
 * @author
 * @version 2.24
 */
@Name("scheduleEvaluationAction")
@Scope(ScopeType.CONVERSATION)
public class ScheduleEvaluationAction extends GenericAction<ScheduleEvaluation> {

    @In
    private ScheduleEvaluationService scheduleEvaluationService;

    @Create
    public void init() {
        if (!isManaged()) {
            for (PollFormType pollFormType : PollFormType.values()) {
                getInstance().getScheduleEvaluationFormList().add(new ScheduleEvaluationForm(new Date(), new Date(), pollFormType));
            }
        }
    }

    @Factory(value = "scheduleEvaluation", scope = ScopeType.STATELESS)
    public ScheduleEvaluation initScheduleEvaluation() {
        return getInstance();
    }

    @Override
    @Begin(ifOutcome = Outcome.SUCCESS, flushMode = FlushModeType.MANUAL)
    @Restrict("#{s:hasPermission('SCHEDULEEVALUATION','VIEW')}")
    public String select(ScheduleEvaluation instance) {
        return super.select(instance);
    }

    @Override
    @End
    @Restrict("#{s:hasPermission('SCHEDULEEVALUATION','CREATE')}")
    public String create() {
        try {
            scheduleEvaluationService.createScheduleEvaluation(getInstance());
            addCreatedMessage();
            return Outcome.SUCCESS;
        } catch (EntryDuplicatedException e) {
            addDuplicatedMessage();
            return Outcome.REDISPLAY;
        } catch (DuplicatedScheduleEvaluationCycleException e) {
            addDuplicatedScheduleEvaluationCycleMessage();
            return Outcome.REDISPLAY;
        } catch (DuplicatedScheduleEvaluationNameException e) {
            addDuplicatedScheduleEvaluationNameMessage();
            return Outcome.REDISPLAY;
        }
    }

    @Override
    @Restrict("#{s:hasPermission('SCHEDULEEVALUATION','CREATE')}")
    public void createAndNew() {
        try {
            scheduleEvaluationService.createScheduleEvaluation(getInstance());
            addCreatedMessage();
            createInstance();
        } catch (EntryDuplicatedException e) {
            addDuplicatedMessage();
        } catch (DuplicatedScheduleEvaluationCycleException e) {
            addDuplicatedScheduleEvaluationCycleMessage();
        } catch (DuplicatedScheduleEvaluationNameException e) {
            addDuplicatedScheduleEvaluationNameMessage();
        }
    }

    @Override
    @End
    @Restrict("#{s:hasPermission('SCHEDULEEVALUATION','UPDATE')}")
    public String update() {
        return super.update();
    }

    @Override
    @End
    @Restrict("#{s:hasPermission('SCHEDULEEVALUATION','DELETE')}")
    public String delete() {
        return super.delete();
    }

    private void addDuplicatedScheduleEvaluationCycleMessage() {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                "ScheduleEvaluation.error.duplicatedCycle", getInstance().getCycle().getName());
    }

    private void addDuplicatedScheduleEvaluationNameMessage() {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                "ScheduleEvaluation.error.duplicatedName", getInstance().getName());
    }
}
