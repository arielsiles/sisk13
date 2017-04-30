package com.encens.khipus.action.budget;

import com.encens.khipus.exception.ConcurrencyException;
import com.encens.khipus.exception.EntryDuplicatedException;
import com.encens.khipus.exception.EntryNotFoundException;
import com.encens.khipus.exception.ReferentialIntegrityException;
import com.encens.khipus.framework.action.GenericAction;
import com.encens.khipus.framework.action.Outcome;
import com.encens.khipus.framework.service.GenericService;
import com.encens.khipus.model.budget.*;
import com.encens.khipus.model.finances.CostCenter;
import com.encens.khipus.service.budget.ExpenseBudgetService;
import com.encens.khipus.util.MessageUtils;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.*;
import org.jboss.seam.annotations.security.Restrict;
import org.jboss.seam.international.StatusMessage;

import javax.faces.event.ValueChangeEvent;

/**
 * ExpenseBudgetAction
 *
 * @author
 * @version 2.0
 */
@Name("expenseBudgetAction")
@Scope(ScopeType.CONVERSATION)
public class ExpenseBudgetAction extends GenericAction<ExpenseBudget> {

    private ClassifierType accountingItemType = ClassifierType.ACCOUNTING_ITEM;
    private boolean includeBudgetDistribution;

    private BudgetDistribution budgetDistribution;

    @In(create = true)
    private BudgetDistributionAction budgetDistributionAction;

    @In
    private ExpenseBudgetService expenseBudgetService;

    private BudgetProgram budgetProgram;

    public ClassifierType getAccountingItemType() {
        return accountingItemType;
    }

    @Create
    public void init() {
        includeBudgetDistribution = true;
        budgetDistribution = budgetDistributionAction.getInstance();
        budgetDistribution.setBudgetDistributionType(BudgetDistributionType.BUDGET);
        budgetDistribution.setType(BudgetType.EXPENSE);
    }

    @Override
    @Begin(ifOutcome = Outcome.SUCCESS, flushMode = FlushModeType.MANUAL)
    @Restrict("#{s:hasPermission('EXPENSEBUDGET','VIEW')}")
    public String select(ExpenseBudget instance) {
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
            setBudgetProgram(getInstance().getBudgetActivity().getBudgetProgram());
            return Outcome.SUCCESS;
        } catch (EntryNotFoundException e) {
            addNotFoundMessage();
            return Outcome.FAIL;
        }
    }

    @Override
    protected GenericService getService() {
        return expenseBudgetService;
    }

    @Override
    protected String getDisplayNameMessage() {
        return MessageUtils.getMessage("ExpenseBudget.title");
    }

    @Factory(value = "expenseBudget", scope = ScopeType.STATELESS)
    @Restrict("#{s:hasPermission('EXPENSEBUDGET','VIEW')}")
    public ExpenseBudget initExpenseBudget() {
        getInstance().setState(BudgetState.ELABORATED);
        return getInstance();
    }

    @Override
    @End
    @Restrict("#{s:hasPermission('EXPENSEBUDGET','CREATE') and (expenseBudget.editable or s:hasPermission('EXPENSEBUDGETSAPPROVAL','VIEW'))}")
    public String create() {
        if (getInstance().getCostCenter() == null) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR, "Common.required", MessageUtils.getMessage("ExpenseBudget.costCenter"));
            return Outcome.REDISPLAY;
        }
        if (includeBudgetDistribution && !budgetDistributionAction.validateDetailSumPercentAmount()) {
            return Outcome.REDISPLAY;
        }
        try {
            expenseBudgetService.create(getInstance(), budgetDistributionAction.getCurrentDetailValues(), budgetDistributionAction.getInstance(), includeBudgetDistribution);
            addCreatedMessage();
            return Outcome.SUCCESS;
        } catch (EntryDuplicatedException e) {
            addDuplicatedMessage();
            return Outcome.REDISPLAY;
        }
    }

    @Override
    @Restrict("#{s:hasPermission('EXPENSEBUDGET','CREATE') and (expenseBudget.editable or s:hasPermission('EXPENSEBUDGETSAPPROVAL','VIEW'))}")
    public void createAndNew() {
        if (!includeBudgetDistribution || budgetDistributionAction.validateDetailSumPercentAmount()) {
            if (getInstance().getCostCenter() == null) {
                facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR, "Common.required", MessageUtils.getMessage("ExpenseBudget.costCenter"));
            } else {
                ExpenseBudget expenseBudgetTemp = new ExpenseBudget(getInstance());
                try {
                    expenseBudgetService.create(getInstance(), budgetDistributionAction.getCurrentDetailValues(), budgetDistributionAction.getInstance(), includeBudgetDistribution);
                    addCreatedMessage();
                    createInstance();
                } catch (EntryDuplicatedException e) {
                    addDuplicatedMessage();
                }
                setInstance(expenseBudgetTemp);
            }
        }
    }

    @Override
    @End
    @Restrict("#{s:hasPermission('EXPENSEBUDGET','UPDATE') and (expenseBudget.editable or s:hasPermission('EXPENSEBUDGETSAPPROVAL','VIEW'))}")
    public String update() {
        if (getInstance().getCostCenter() == null) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR, "Common.required", MessageUtils.getMessage("ExpenseBudget.costCenter"));
            return Outcome.REDISPLAY;
        }
        if (!hasValidBudgetProgramEditableState()) {
            return Outcome.FAIL;
        }
        if (includeBudgetDistribution && !budgetDistributionAction.validateDetailSumPercentAmount()) {
            return Outcome.REDISPLAY;
        }
        Long currentVersion = (Long) getVersion(getInstance());

        try {
            expenseBudgetService.update(getInstance(), budgetDistributionAction.getCurrentDetailValues(),
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
    @Restrict("#{s:hasPermission('ENTRYBUDGETS','DELETE') and (expenseBudget.editable or s:hasPermission('EXPENSEBUDGETSAPPROVAL','VIEW'))}")
    public String delete() {
        if (!hasValidBudgetProgramEditableState()) {
            return Outcome.FAIL;
        }
        try {
            expenseBudgetService.delete(getInstance());
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

    @Override
    @End(beforeRedirect = true)
    public String cancel() {
        return super.cancel();
    }

    @Restrict("#{s:hasPermission('EXPENSEBUDGET','UPDATE') and s:hasPermission('EXPENSEBUDGETSVERIFICATION','VIEW') and (expenseBudget.editable or s:hasPermission('EXPENSEBUDGETSAPPROVAL','VIEW'))}")
    @End
    public String verify() {
        if (getInstance().getCostCenter() == null) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR, "Common.required", MessageUtils.getMessage("ExpenseBudget.costCenter"));
            return Outcome.REDISPLAY;
        }
        if (!hasValidBudgetProgramEditableState()) {
            return Outcome.FAIL;
        }
        getInstance().setEditable(true);
        getInstance().setState(BudgetState.CHECKED);
        return update();
    }

    @Restrict("#{s:hasPermission('EXPENSEBUDGET','UPDATE') and s:hasPermission('EXPENSEBUDGETSAPPROVAL','VIEW')}")
    @End
    public String approve() {
        if (getInstance().getCostCenter() == null) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR, "Common.required", MessageUtils.getMessage("ExpenseBudget.costCenter"));
            return Outcome.REDISPLAY;
        }
        if (!hasValidBudgetProgramEditableState()) {
            return Outcome.FAIL;
        }
        getInstance().setEditable(false);
        getInstance().setState(BudgetState.APPROVED);
        return update();
    }

    @Restrict("#{s:hasPermission('EXPENSEBUDGET','UPDATE') and s:hasPermission('EXPENSEBUDGETSAPPROVAL','VIEW')}")
    @End
    public String freeze() {
        if (getInstance().getCostCenter() == null) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR, "Common.required", MessageUtils.getMessage("ExpenseBudget.costCenter"));
            return Outcome.REDISPLAY;
        }
        if (!hasValidBudgetProgramEditableState()) {
            return Outcome.FAIL;
        }
        getInstance().setEditable(false);
        getInstance().setState(BudgetState.BLOCKED);
        return update();
    }

    public Boolean hasValidBudgetProgramEditableState() {
        Boolean valid = false;
        try {
            if (getInstance() == null) {
                facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR, "ExpenseBudget.error.notFound");
            } else if (!(valid = expenseBudgetService.hasValidEditableState(getInstance()))) {
                facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR, "ExpenseBudget.error.notEditableState");
            }
        } catch (EntryNotFoundException e) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR, "ExpenseBudget.error.notFound");
        }

        return valid;
    }

    public BudgetProgram getBudgetProgram() {
        return budgetProgram;
    }

    public void setBudgetProgram(BudgetProgram budgetProgram) {
        this.budgetProgram = budgetProgram;
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
        facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR, "ExpenseBudget.error.concurrency");
    }

}
