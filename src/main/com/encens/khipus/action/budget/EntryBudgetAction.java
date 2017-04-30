package com.encens.khipus.action.budget;

import com.encens.khipus.exception.ConcurrencyException;
import com.encens.khipus.exception.EntryDuplicatedException;
import com.encens.khipus.exception.EntryNotFoundException;
import com.encens.khipus.exception.ReferentialIntegrityException;
import com.encens.khipus.framework.action.GenericAction;
import com.encens.khipus.framework.action.Outcome;
import com.encens.khipus.model.budget.*;
import com.encens.khipus.model.finances.CostCenter;
import com.encens.khipus.service.budget.EntryBudgetService;
import com.encens.khipus.util.MessageUtils;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.*;
import org.jboss.seam.annotations.security.Restrict;
import org.jboss.seam.international.StatusMessage;

import javax.faces.event.ValueChangeEvent;

/**
 * This class is an action for EntryBudgets
 *
 * @author
 * @version 2.0
 */
@Name("entryBudgetAction")
@Scope(ScopeType.CONVERSATION)
public class EntryBudgetAction extends GenericAction<EntryBudget> {
    private ClassifierType burdenType = ClassifierType.BURDEN;
    private boolean includeBudgetDistribution;

    private BudgetDistribution budgetDistribution;

    @In(create = true)
    private BudgetDistributionAction budgetDistributionAction;
    @In
    private EntryBudgetService entryBudgetService;

    @Create
    public void init() {
        includeBudgetDistribution = true;
        budgetDistribution = budgetDistributionAction.getInstance();
        budgetDistribution.setBudgetDistributionType(BudgetDistributionType.BUDGET);
        budgetDistribution.setType(BudgetType.ENTRY);
    }

    @Override
    @Begin(ifOutcome = Outcome.SUCCESS, flushMode = FlushModeType.MANUAL)
    @Restrict("#{s:hasPermission('ENTRYBUDGETS','VIEW')}")
    public String select(EntryBudget instance) {
        try {
            setOp(OP_UPDATE);
            //define the unmanaged instance as current instance
            setInstance(instance);
            //Ensure the instance exists in the database, find it
            setInstance(getService().findById(getEntityClass(), getId(instance), true));
            includeBudgetDistribution = getInstance().getBudgetDistribution() != null;
            if (getInstance().getBudgetDistribution() != null) {
                budgetDistributionAction.setOp(OP_UPDATE);
                budgetDistributionAction.setInstance(getInstance().getBudgetDistribution());
                budgetDistributionAction.putAllCurrentDetailValues();
                budgetDistributionAction.performDetailSumPercentAmount();
            } else {
                budgetDistribution = budgetDistributionAction.getInstance();
                budgetDistribution.setBudgetDistributionType(BudgetDistributionType.BUDGET);
                budgetDistribution.setType(BudgetType.ENTRY);
            }

            return Outcome.SUCCESS;
        } catch (EntryNotFoundException e) {
            addNotFoundMessage();
            return Outcome.FAIL;
        }
    }

    @Factory(value = "entryBudget", scope = ScopeType.STATELESS)
    @Restrict("#{s:hasPermission('ENTRYBUDGETS','VIEW')}")
    public EntryBudget initClassifier() {
        return getInstance();
    }

    @Override
    protected String getDisplayNameMessage() {
        return messages.get("EntryBudget.title");
    }

    @Override
    @End
    @Restrict("#{s:hasPermission('ENTRYBUDGETS','CREATE')}")
    public String create() {
        if (getInstance().getCostCenter() == null) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR, "Common.required", MessageUtils.getMessage("EntryBudget.costCenter"));
            return Outcome.REDISPLAY;
        }
        if (includeBudgetDistribution && !budgetDistributionAction.validateDetailSumPercentAmount()) {
            return Outcome.REDISPLAY;
        }
        log.debug("Creating EntryBudget.....");
        getInstance().setEditable(Boolean.TRUE);
        getInstance().setState(BudgetState.ELABORATED);
        try {
            entryBudgetService.create(getInstance(), budgetDistributionAction.getCurrentDetailValues(), budgetDistributionAction.getInstance(), includeBudgetDistribution);
            addCreatedMessage();
            return Outcome.SUCCESS;
        } catch (EntryDuplicatedException e) {
            addDuplicatedMessage();
            return Outcome.REDISPLAY;
        }
    }

    @Override
    @Restrict("#{s:hasPermission('ENTRYBUDGETS','CREATE')}")
    public void createAndNew() {
        if (!includeBudgetDistribution || budgetDistributionAction.validateDetailSumPercentAmount()) {
            if (getInstance().getCostCenter() == null) {
                facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR, "Common.required", MessageUtils.getMessage("EntryBudget.costCenter"));
            } else {
                getInstance().setEditable(Boolean.TRUE);
                getInstance().setState(BudgetState.ELABORATED);
                EntryBudget entryBudgetTemp = new EntryBudget(getInstance());
                try {
                    entryBudgetService.create(getInstance(), budgetDistributionAction.getCurrentDetailValues(), budgetDistributionAction.getInstance(), includeBudgetDistribution);
                    addCreatedMessage();
                    createInstance();
                } catch (EntryDuplicatedException e) {
                    addDuplicatedMessage();
                }
                setInstance(entryBudgetTemp);
            }
        }
    }

    @Override
    @End
    @Restrict("#{s:hasPermission('ENTRYBUDGETS','UPDATE')}")
    public String update() {
        if (getInstance().getCostCenter() == null) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR, "Common.required", MessageUtils.getMessage("EntryBudget.costCenter"));
            return Outcome.REDISPLAY;
        }
        if (includeBudgetDistribution && !budgetDistributionAction.validateDetailSumPercentAmount()) {
            return Outcome.REDISPLAY;
        }
        Long currentVersion = (Long) getVersion(getInstance());

        try {
            entryBudgetService.update(getInstance(), budgetDistributionAction.getCurrentDetailValues(),
                    budgetDistributionAction.getInstance(), includeBudgetDistribution);
        } catch (EntryDuplicatedException e) {
            addDuplicatedMessage();
            setVersion(getInstance(), currentVersion);
            return Outcome.REDISPLAY;
        } catch (ConcurrencyException e) {
            concurrencyLog();
            try {
                select(getService().findById(getEntityClass(), getId(getInstance()), true));
            } catch (EntryNotFoundException e1) {
                entryNotFoundLog();
                addNotFoundMessage();
                return Outcome.FAIL;
            }
            addUpdateConcurrencyMessage();
            return Outcome.FAIL;
        } catch (ReferentialIntegrityException e) {
            referentialIntegrityLog();
            budgetDistributionAction.addDeleteReferentialIntegrityMessage();
        }
        addUpdatedMessage();
        return Outcome.SUCCESS;
    }

    @Override
    @End
    @Restrict("#{s:hasPermission('ENTRYBUDGETS','DELETE')}")
    public String delete() {
        try {
            entryBudgetService.delete(getInstance());
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

    public String getCostCenterFullName() {
        return getInstance().getCostCenter() != null ? getInstance().getCostCenter().getFullName() : null;
    }

    public void clearCostCenter() {
        getInstance().setCostCenter(null);
    }

    public void assignCostCenter(CostCenter costCenter) {
        if (costCenter != null) {
            try {
                costCenter = getService().findById(CostCenter.class, costCenter.getId());
            } catch (EntryNotFoundException e) {
            }
        }
        getInstance().setCostCenter(costCenter);
    }

    public ClassifierType getBurdenType() {
        return burdenType;
    }

    @End
    public String verify() {
        if (getInstance().getCostCenter() == null) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR, "Common.required", MessageUtils.getMessage("EntryBudget.costCenter"));
            return Outcome.REDISPLAY;
        }
        getInstance().setEditable(true);
        getInstance().setState(BudgetState.CHECKED);
        return update();
    }

    @End
    public String approve() {
        if (getInstance().getCostCenter() == null) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR, "Common.required", MessageUtils.getMessage("EntryBudget.costCenter"));
            return Outcome.REDISPLAY;
        }
        getInstance().setEditable(false);
        getInstance().setState(BudgetState.APPROVED);
        return update();
    }

    @End
    public String freeze() {
        if (getInstance().getCostCenter() == null) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR, "Common.required", MessageUtils.getMessage("EntryBudget.costCenter"));
            return Outcome.REDISPLAY;
        }
        getInstance().setEditable(false);
        getInstance().setState(BudgetState.BLOCKED);
        return update();
    }

    public boolean isIncludeBudgetDistribution() {
        return includeBudgetDistribution;
    }

    public void setIncludeBudgetDistribution(boolean includeBudgetDistribution) {
        this.includeBudgetDistribution = includeBudgetDistribution;
    }

    public void changeIncludeBudgetDistribution(ValueChangeEvent event) {
        boolean flag = ((Boolean) event.getNewValue());
        setIncludeBudgetDistribution(flag);
    }

    @Override
    protected void addUpdateConcurrencyMessage() {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR, "EntryBudget.error.concurrency");
    }
}
