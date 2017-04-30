package com.encens.khipus.action.employees;

import com.encens.khipus.exception.ConcurrencyException;
import com.encens.khipus.exception.EntryDuplicatedException;
import com.encens.khipus.exception.EntryNotFoundException;
import com.encens.khipus.exception.ReferentialIntegrityException;
import com.encens.khipus.exception.employees.VacationRuleUndefinedYearException;
import com.encens.khipus.framework.action.GenericAction;
import com.encens.khipus.framework.action.Outcome;
import com.encens.khipus.model.employees.VacationPlanning;
import com.encens.khipus.model.finances.JobContract;
import com.encens.khipus.service.employees.JobContractService;
import com.encens.khipus.service.employees.VacationPlanningService;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.*;
import org.jboss.seam.annotations.security.Restrict;
import org.jboss.seam.international.StatusMessage;

/**
 * @author
 * @version 3.4
 */
@Name("vacationPlanningAction")
@Scope(ScopeType.CONVERSATION)
public class VacationPlanningAction extends GenericAction<VacationPlanning> {

    @In
    private JobContractService jobContractService;

    @In
    private VacationPlanningService vacationPlanningService;

    @Create
    public void initialize() {
        if (!isManaged()) {
            getInstance().setSeniorityYears(0);
            getInstance().setVacationDays(0);
            getInstance().setDaysOff(0);
            getInstance().setDaysUsed(0);
        }
    }

    @Factory(value = "vacationPlanning", scope = ScopeType.STATELESS)
    public VacationPlanning initVacationPlanning() {
        return getInstance();
    }

    @Override
    protected String getDisplayNameMessage() {
        return getInstance().getFullName();
    }

    @Override
    @Restrict("#{s:hasPermission('VACATIONPLANNING','VIEW')}")
    @Begin(ifOutcome = Outcome.SUCCESS, flushMode = FlushModeType.MANUAL)
    public String select(VacationPlanning instance) {
        try {
            setOp(OP_UPDATE);
            setInstance(vacationPlanningService.load(instance));
            return Outcome.SUCCESS;
        } catch (EntryNotFoundException e) {
            addNotFoundMessage();
            return Outcome.FAIL;
        }
    }

    @Override
    public void refreshInstance() {
        try {
            setInstance(vacationPlanningService.load(getInstance()));
        } catch (EntryNotFoundException e) {
            addNotFoundMessage();
        }
    }

    @Override
    @End
    @Restrict("#{s:hasPermission('VACATIONPLANNING','CREATE')}")
    public String create() {
        try {
            vacationPlanningService.createVacationPlanning(getInstance());
            addCreatedMessage();
            return Outcome.SUCCESS;
        } catch (EntryDuplicatedException e) {
            addDuplicatedMessage();
            return Outcome.REDISPLAY;
        }
    }

    @Override
    @Restrict("#{s:hasPermission('VACATIONPLANNING','CREATE')}")
    public void createAndNew() {
        try {
            vacationPlanningService.createVacationPlanning(getInstance());
            addCreatedMessage();
            createInstance();
            initialize();
        } catch (EntryDuplicatedException e) {
            addDuplicatedMessage();
        }
    }

    @Override
    @End
    @Restrict("#{s:hasPermission('VACATIONPLANNING','UPDATE')}")
    public String update() {
        Long currentVersion = (Long) getVersion(getInstance());
        try {
            vacationPlanningService.updateVacationPlanning(getInstance());
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
        }
        addUpdatedMessage();
        return Outcome.SUCCESS;
    }

    @Restrict("#{s:hasPermission('VACATIONPLANNING','UPDATE')}")
    public String synchronizeVacationDays() {
        Long currentVersion = (Long) getVersion(getInstance());
        try {
            vacationPlanningService.synchronizeVacationDays(getInstance());
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
        } catch (VacationRuleUndefinedYearException e) {
            addVacationRuleUndefinedYearMessage(e);
            return Outcome.REDISPLAY;
        }
        addSuccessSynchronizationMessage();
        return Outcome.SUCCESS;
    }

    @Override
    @End
    @Restrict("#{s:hasPermission('VACATIONPLANNING','DELETE')}")
    public String delete() {
        try {
            vacationPlanningService.deleteVacationPlanning(getInstance());
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

    public void assignJobContract(JobContract jobContract) {
        getInstance().setJobContract(jobContract);
        loadJobContractValues();
    }

    public void loadJobContractValues() {
        if (getInstance().getJobContract() != null) {
            getInstance().setJobContract(jobContractService.load(getInstance().getJobContract()));
            getInstance().setInitDate(getInstance().getJobContract().getContract().getInitDate());
        }
    }

    public void clearJobContract() {
        getInstance().setJobContract(null);
        getInstance().setInitDate(null);
    }

    public boolean hasAssignedJobContract() {
        return (getInstance().getJobContract() != null);
    }

    @Override
    protected void addDuplicatedMessage() {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                "VacationPlanning.error.message.duplicated",
                getInstance().getJobContract().getContract().getEmployee().getFullName(),
                getInstance().getJobContract().getJob().getOrganizationalUnit().getName());
    }

    private void addSuccessSynchronizationMessage() {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.INFO,
                "VacationPlanning.successSynchronizeGestionVacation");
    }

    private void addVacationRuleUndefinedYearMessage(VacationRuleUndefinedYearException e) {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                "VacationPlanning.error.synchronizeVacationRuleUndefinedYear", e.getSeniorityYear());
    }
}
