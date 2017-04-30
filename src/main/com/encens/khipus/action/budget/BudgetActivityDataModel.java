package com.encens.khipus.action.budget;

import com.encens.khipus.framework.action.QueryDataModel;
import com.encens.khipus.model.budget.BudgetActivity;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import java.util.Arrays;
import java.util.List;

/**
 * BudgetActivityDataModel
 *
 * @author
 * @version 2.0
 */
@Name("budgetActivityDataModel")
@Scope(ScopeType.PAGE)
public class BudgetActivityDataModel extends QueryDataModel<Long, BudgetActivity> {
    private static final String[] RESTRICTIONS = {"budgetActivity.budgetProgram= #{budgetProgram}"};

    @Create
    public void init() {
        sortProperty = "budgetActivity.name";
    }

    @Override
    public String getEjbql() {
        return "select budgetActivity from BudgetActivity budgetActivity";
    }

    @Override
    public List<String> getRestrictions() {
        return Arrays.asList(RESTRICTIONS);
    }
}
