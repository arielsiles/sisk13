package com.encens.khipus.action.budget;

import com.encens.khipus.framework.action.QueryDataModel;
import com.encens.khipus.model.budget.BudgetState;
import com.encens.khipus.model.budget.EntryBudget;
import com.encens.khipus.model.finances.CostCenter;
import com.encens.khipus.service.budget.EntryBudgetService;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.*;
import org.jboss.seam.annotations.security.Restrict;
import org.jboss.seam.core.Manager;

import java.util.Arrays;
import java.util.List;

/**
 * This class is a data model for entry budget list
 *
 * @author
 * @version 2.0
 */
@Name("entryBudgetDataModel")
@Scope(ScopeType.PAGE)
@Restrict("#{s:hasPermission('ENTRYBUDGETS','VIEW')}")
public class EntryBudgetDataModel extends QueryDataModel<Long, EntryBudget> {
    private String costCenterName;
    @In
    private EntryBudgetService entryBudgetService;

    private static final String[] RESTRICTIONS = {
            "entryBudget.businessUnit = #{entryBudgetDataModel.criteria.businessUnit}",
            "lower(entryBudget.costCenter.description) like concat('%',concat(lower(#{entryBudgetDataModel.costCenterName}),'%'))",
            "entryBudget.costCenter=#{entryBudgetDataModel.criteria.costCenter}",
            "entryBudget.gestion = #{entryBudgetDataModel.criteria.gestion}",
            "entryBudget.state = #{entryBudgetDataModel.criteria.state}"};

    @Create
    public void init() {
        sortProperty = "classifier.name";
    }

    @Override
    public String getEjbql() {
        return "select entryBudget from EntryBudget entryBudget";
    }

    @Override
    public List<String> getRestrictions() {
        return Arrays.asList(RESTRICTIONS);
    }

    @Factory(value = "budgetStatesEnum")
    public BudgetState[] getBudgetStatesList() {
        return BudgetState.values();
    }

    public String getCostCenterName() {
        return costCenterName;
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

    public void setCostCenterName(String costCenterName) {
        this.costCenterName = costCenterName;
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

    @Restrict("#{s:hasPermission('ENTRYBUDGETS','UPDATE') and s:hasPermission('ENTRYBUDGETSCHECK','VIEW')}")
    public void verifyInBlock() {
        Manager conversationManager = Manager.instance();
        conversationManager.setDefaultFlushMode(FlushModeType.MANUAL);
        conversationManager.beginConversation();
        entryBudgetService.verifyInBlock(getSelectedIdList());
        conversationManager.endConversation(true);
        updateAndSearch();
    }

    @Restrict("#{s:hasPermission('ENTRYBUDGETS','UPDATE') and s:hasPermission('ENTRYBUDGETSAPPROVAL','VIEW')}")
    public void approveInBlock() {
        Manager conversationManager = Manager.instance();
        conversationManager.setDefaultFlushMode(FlushModeType.MANUAL);
        conversationManager.beginConversation();
        entryBudgetService.approveInBlock(getSelectedIdList());
        conversationManager.endConversation(true);
        updateAndSearch();
    }

    @Restrict("#{s:hasPermission('ENTRYBUDGETS','UPDATE') and s:hasPermission('ENTRYBUDGETSAPPROVAL','VIEW')}")
    public void freezeInBlock() {
        Manager conversationManager = Manager.instance();
        conversationManager.setDefaultFlushMode(FlushModeType.MANUAL);
        conversationManager.beginConversation();
        entryBudgetService.freezeInBlock(getSelectedIdList());
        conversationManager.endConversation(true);
        updateAndSearch();
    }
}
