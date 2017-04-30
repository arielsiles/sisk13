package com.encens.khipus.action.budget;

import com.encens.khipus.framework.action.QueryDataModel;
import com.encens.khipus.model.budget.BudgetProgram;
import com.encens.khipus.model.budget.BudgetState;
import com.encens.khipus.model.budget.ExpenseBudget;
import com.encens.khipus.model.finances.CostCenter;
import com.encens.khipus.service.budget.ExpenseBudgetService;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.*;
import org.jboss.seam.annotations.security.Restrict;
import org.jboss.seam.core.Manager;

import java.util.Arrays;
import java.util.List;

/**
 * ExpenseBudgetDataModel
 *
 * @author
 * @version 2.0
 */
@Name("expenseBudgetDataModel")
@Scope(ScopeType.PAGE)
@Restrict("#{s:hasPermission('EXPENSEBUDGET','VIEW')}")
public class ExpenseBudgetDataModel extends QueryDataModel<Long, ExpenseBudget> {
    private String costCenterName;
    private BudgetProgram budgetProgram;
    @In
    private ExpenseBudgetService expenseBudgetService;

    private static final String[] RESTRICTIONS = {
            "expenseBudget.businessUnit = #{expenseBudgetDataModel.criteria.businessUnit}",
            "lower(expenseBudget.costCenter.description) like concat('%',concat(lower(#{expenseBudgetDataModel.costCenterName}),'%')) ",
            "expenseBudget.costCenter=#{expenseBudgetDataModel.criteria.costCenter}",
            "expenseBudget.budgetActivity.budgetProgram = #{expenseBudgetDataModel.budgetProgram}",
            "expenseBudget.budgetActivity = #{expenseBudgetDataModel.criteria.budgetActivity}",
            "expenseBudget.gestion = #{expenseBudgetDataModel.criteria.gestion}",
            "expenseBudget.state = #{expenseBudgetDataModel.criteria.state}"
    };

    @Create
    public void init() {

        sortProperty = "expenseBudget.creationDate";
    }

    @Override
    public String getEjbql() {
        return "select expenseBudget from ExpenseBudget expenseBudget";
    }

    public Boolean getStateForApproveInBlockOperation() {
        return BudgetState.CHECKED.equals(getCriteria().getState()) || BudgetState.BLOCKED.equals(getCriteria().getState());
    }

    public Boolean getStateForVerifyInBlockOperation() {
        return BudgetState.ELABORATED.equals(getCriteria().getState()) || BudgetState.BLOCKED.equals(getCriteria().getState());
    }

    public Boolean getStateForFreezeInBlockOperation() {
        return getCriteria().getState() != null && !BudgetState.BLOCKED.equals(getCriteria().getState());
    }

    @Restrict("#{s:hasPermission('EXPENSEBUDGET','UPDATE') and s:hasPermission('EXPENSEBUDGETSVERIFICATION','VIEW')}")
    public void verifyInBlock() {
        Manager conversationManager = Manager.instance();
        conversationManager.setDefaultFlushMode(FlushModeType.MANUAL);
        conversationManager.beginConversation();
        expenseBudgetService.verifyInBlock(getSelectedIdList());
        conversationManager.endConversation(true);
        updateAndSearch();
    }

    @Restrict("#{s:hasPermission('EXPENSEBUDGET','UPDATE') and s:hasPermission('EXPENSEBUDGETSAPPROVAL','VIEW')}")
    public void approveInBlock() {
        Manager conversationManager = Manager.instance();
        conversationManager.setDefaultFlushMode(FlushModeType.MANUAL);
        conversationManager.beginConversation();
        expenseBudgetService.approveInBlock(getSelectedIdList());
        conversationManager.endConversation(true);
        updateAndSearch();
    }

    @Restrict("#{s:hasPermission('EXPENSEBUDGET','UPDATE') and s:hasPermission('EXPENSEBUDGETSAPPROVAL','VIEW')}")
    public void freezeInBlock() {
        Manager conversationManager = Manager.instance();
        conversationManager.setDefaultFlushMode(FlushModeType.MANUAL);
        conversationManager.beginConversation();
        expenseBudgetService.freezeInBlock(getSelectedIdList());
        conversationManager.endConversation(true);
        updateAndSearch();
    }

    @Override
    public List<String> getRestrictions() {
        return Arrays.asList(RESTRICTIONS);
    }

    public String getCostCenterName() {
        return costCenterName;
    }

    public void setCostCenterName(String costCenterName) {
        this.costCenterName = costCenterName;
    }

    public CostCenter getCostCenter() {
        return getCriteria().getCostCenter();
    }

    public void setCostCenter(CostCenter costCenter) {
        getCriteria().setCostCenter(costCenter);
    }

    public void assignCostCenter(CostCenter costCenter) {
        setCostCenter(costCenter);
    }

    public void clearCostCenter() {
        setCostCenter(null);
    }

    public BudgetProgram getBudgetProgram() {
        return budgetProgram;
    }

    public void setBudgetProgram(BudgetProgram budgetProgram) {
        this.budgetProgram = budgetProgram;
    }
}
