package com.encens.khipus.action.employees;

import com.encens.khipus.exception.ConcurrencyException;
import com.encens.khipus.exception.EntryDuplicatedException;
import com.encens.khipus.exception.EntryNotFoundException;
import com.encens.khipus.exception.ReferentialIntegrityException;
import com.encens.khipus.exception.employees.VacationAnnulledStateException;
import com.encens.khipus.exception.employees.VacationOverlapException;
import com.encens.khipus.exception.employees.VacationPendingStateException;
import com.encens.khipus.exception.employees.VacationPlanningExceedVacationDaysException;
import com.encens.khipus.framework.action.GenericAction;
import com.encens.khipus.framework.action.Outcome;
import com.encens.khipus.model.employees.Vacation;
import com.encens.khipus.model.employees.VacationState;
import com.encens.khipus.service.employees.VacationGestionService;
import com.encens.khipus.service.employees.VacationService;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.*;
import org.jboss.seam.annotations.security.Restrict;
import org.jboss.seam.international.StatusMessage;

import java.util.List;

/**
 * @author
 * @version 3.4
 */
@Name("vacationAction")
@Scope(ScopeType.CONVERSATION)
public class VacationAction extends GenericAction<Vacation> {

    @In
    private VacationService vacationService;
    @In
    private VacationPlanningAction vacationPlanningAction;

    @In
    private VacationGestionService vacationGestionService;

    @Factory(value = "vacation", scope = ScopeType.STATELESS)
    public Vacation initVacation() {
        return getInstance();
    }

    @Create
    public void init() {
        if (!isManaged()) {
            getInstance().setTotalDays(0);
            getInstance().setDaysOff(0);
        }
    }

    @Override
    protected String getDisplayNameMessage() {
        return getInstance().getDescription();
    }

    @Begin(ifOutcome = Outcome.SUCCESS, flushMode = FlushModeType.MANUAL, nested = true)
    public String newInstance() {
        setOp(OP_CREATE);
        return Outcome.SUCCESS;
    }

    @Override
    @Begin(nested = true, ifOutcome = Outcome.SUCCESS, flushMode = FlushModeType.MANUAL)
    @Restrict("#{s:hasPermission('VACATIONPLANNING','VIEW')}")
    public String select(Vacation instance) {
        return super.select(instance);
    }

    @Override
    @End(beforeRedirect = true)
    @Restrict("#{s:hasPermission('VACATIONPLANNING','CREATE')}")
    public String create() {
        try {
            vacationService.createVacation(getInstance());
            addCreatedMessage();
            return Outcome.SUCCESS;
        } catch (EntryDuplicatedException e) {
            addDuplicatedMessage();
            return Outcome.REDISPLAY;
        } catch (VacationOverlapException e) {
            addVacationOverlapMessage(e);
            return Outcome.REDISPLAY;
        } catch (VacationPlanningExceedVacationDaysException e) {
            addVacationPlanningExceedVacationDaysMessage(e);
            return Outcome.REDISPLAY;
        }
    }

    @Override
    @Restrict("#{s:hasPermission('VACATIONPLANNING','CREATE')}")
    public void createAndNew() {
        try {
            vacationService.createVacation(getInstance());
            addCreatedMessage();
            createInstance();
            init();
        } catch (EntryDuplicatedException e) {
            addDuplicatedMessage();
        } catch (VacationOverlapException e) {
            addVacationOverlapMessage(e);
        } catch (VacationPlanningExceedVacationDaysException e) {
            addVacationPlanningExceedVacationDaysMessage(e);
        }
    }

    @Override
    @End(beforeRedirect = true)
    @Restrict("#{s:hasPermission('VACATIONPLANNING','UPDATE')}")
    public String update() {
        Long currentVersion = (Long) getVersion(getInstance());
        try {
            vacationService.updateVacation(getInstance());
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
        } catch (VacationPendingStateException e) {
            addPendingStateMessage();
            return Outcome.REDISPLAY;
        } catch (VacationOverlapException e) {
            addVacationOverlapMessage(e);
            return Outcome.REDISPLAY;
        } catch (VacationPlanningExceedVacationDaysException e) {
            addVacationPlanningExceedVacationDaysMessage(e);
            return Outcome.REDISPLAY;
        }
        addUpdatedMessage();
        return Outcome.SUCCESS;
    }

    @Override
    @End(beforeRedirect = true)
    @Restrict("#{s:hasPermission('VACATIONPLANNING','DELETE')}")
    public String delete() {
        try {
            vacationService.deleteVacation(getInstance());
            addDeletedMessage();
        } catch (ConcurrencyException e) {
            entryNotFoundLog();
            addDeleteConcurrencyMessage();
        } catch (ReferentialIntegrityException e) {
            referentialIntegrityLog();
            addDeleteReferentialIntegrityMessage();
        } catch (VacationPendingStateException e) {
            addPendingStateMessage();
        }
        return Outcome.SUCCESS;
    }


    @End(beforeRedirect = true)
    @Restrict("#{s:hasPermission('VACATIONAPPROVE','VIEW')}")
    public String approve() {
        Long currentVersion = (Long) getVersion(getInstance());
        try {
            vacationService.approveVacation(getInstance());
            addSuccessOperationMessage(messages.get("Vacation.approve"));
            vacationPlanningAction.refreshInstance();
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
        } catch (VacationPendingStateException e) {
            addPendingStateMessage();
            return Outcome.REDISPLAY;
        } catch (VacationOverlapException e) {
            addVacationOverlapMessage(e);
            return Outcome.REDISPLAY;
        } catch (VacationPlanningExceedVacationDaysException e) {
            addVacationPlanningExceedVacationDaysMessage(e);
            return Outcome.REDISPLAY;
        }

        return Outcome.SUCCESS;
    }

    @End(beforeRedirect = true)
    @Restrict("#{s:hasPermission('VACATIONANNUL','VIEW')}")
    public String annul() {
        Long currentVersion = (Long) getVersion(getInstance());
        try {
            vacationService.annulVacation(getInstance());
            addSuccessOperationMessage(messages.get("Vacation.annul"));
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
        } catch (ReferentialIntegrityException e) {
            addDeleteReferentialIntegrityMessage();
            return Outcome.FAIL;
        } catch (VacationAnnulledStateException e) {
            addAnnulledStateMessage();
            return Outcome.FAIL;
        }
        return Outcome.SUCCESS;
    }


    private void addPendingStateMessage() {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR, "Vacation.error.pendingState");
    }

    private void addAnnulledStateMessage() {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR, "Vacation.error.annulledState", getInstance().getDescription());
    }

    private void addVacationOverlapMessage(VacationOverlapException e) {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR, "Vacation.error.overlap", e.getOverlapItems());
    }

    private void addVacationPlanningExceedVacationDaysMessage(VacationPlanningExceedVacationDaysException e) {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                "VacationPlanning.error.exceedVacationDaysException",
                e.getDaysUsed(),
                e.getVacationDays());
    }

    public void calculateValues() {
        vacationService.calculateValues(getInstance());
    }

    public Boolean getIsPending() {
        return !isManaged() || vacationService.hasCurrentState(getInstance(), VacationState.PENDING);
    }

    public Boolean getIsApproved() {
        return isManaged() && vacationService.hasCurrentState(getInstance(), VacationState.APPROVED);
    }

    public Boolean getIsAnnulled() {
        return isManaged() && vacationService.hasCurrentState(getInstance(), VacationState.ANNULLED);
    }

    public List getVacationGestionList() {
        return vacationGestionService.findByVacationPlanningAvailableDaysOff(vacationPlanningAction.getInstance());
    }

}
