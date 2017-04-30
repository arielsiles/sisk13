package com.encens.khipus.action.budget;

import com.encens.khipus.framework.action.QueryDataModel;
import com.encens.khipus.model.budget.BudgetDistribution;
import com.encens.khipus.model.budget.BudgetDistributionType;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.security.Restrict;

import java.util.Arrays;
import java.util.List;

/**
 * BudgetDistributionDataModel
 *
 * @author
 * @version 2.5
 */
@Name("budgetDistributionDataModel")
@Scope(ScopeType.PAGE)
@Restrict("#{s:hasPermission('BUDGETDISTRIBUTION','VIEW')}")
public class BudgetDistributionDataModel extends QueryDataModel<Long, BudgetDistribution> {
    private BudgetDistributionType budgetDistributionType;

    private static final String[] RESTRICTIONS = {
            "budgetDistribution.businessUnit = #{budgetDistributionDataModel.criteria.businessUnit}",
            "budgetDistribution.gestion = #{budgetDistributionDataModel.criteria.gestion}",
            "budgetDistribution.type = #{budgetDistributionDataModel.criteria.type}",
            "budgetDistribution.budgetDistributionType = #{budgetDistributionDataModel.budgetDistributionType}"};

    @Create
    public void init() {
        sortProperty = "budgetDistribution.gestion.year,budgetDistribution.type";
        budgetDistributionType = BudgetDistributionType.GLOBAL;
    }

    @Override
    public String getEjbql() {
        return "select budgetDistribution from BudgetDistribution budgetDistribution";
    }

    @Override
    public List<String> getRestrictions() {
        return Arrays.asList(RESTRICTIONS);
    }

    public BudgetDistributionType getBudgetDistributionType() {
        return budgetDistributionType;
    }

    public void setBudgetDistributionType(BudgetDistributionType budgetDistributionType) {
        this.budgetDistributionType = budgetDistributionType;
    }

    public void setGlobal() {
        setBudgetDistributionType(BudgetDistributionType.GLOBAL);
    }
}
