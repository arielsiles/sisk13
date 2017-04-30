package com.encens.khipus.action.budget;

import com.encens.khipus.framework.action.GenericAction;
import com.encens.khipus.framework.action.Outcome;
import com.encens.khipus.model.budget.BudgetActivity;
import com.encens.khipus.model.budget.BudgetProgram;
import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.*;
import org.jboss.seam.annotations.security.Restrict;

/**
 * BudgetActivityAction
 *
 * @author
 * @version 2.0
 */
@Name("budgetActivityAction")
@Scope(ScopeType.CONVERSATION)
public class BudgetActivityAction extends GenericAction<BudgetActivity> {


    @Factory(value = "budgetActivity", scope = ScopeType.STATELESS)
    public BudgetActivity initBudgetActivity() {
        return getInstance();
    }

    @Override
    protected String getDisplayNameProperty() {
        return "name";
    }

    private void setDefaultValues() {
        getInstance().setBudgetProgram((BudgetProgram) Component.getInstance("budgetProgram"));
    }

    @Begin(nested = true)
    @Restrict("#{s:hasPermission('BUDGETACTIVITY','CREATE')}")
    public String newBudgetActivity() {
        createInstance();
        setDefaultValues();
        setOp(OP_CREATE);
        return Outcome.SUCCESS;
    }

    @Override
    @Begin(nested = true, flushMode = FlushModeType.MANUAL)
    @Restrict("#{s:hasPermission('BUDGETACTIVITY','VIEW')}")
    public String select(BudgetActivity instance) {
        return super.select(instance);
    }

    @Override
    @End(beforeRedirect = true)
    @Restrict("#{s:hasPermission('BUDGETACTIVITY','CREATE')}")
    public String create() {
        setDefaultValues();
        return super.create();
    }

    @Override
    @Restrict("#{s:hasPermission('BUDGETACTIVITY','CREATE')}")
    public void createAndNew() {
        setDefaultValues();
        super.createAndNew();
    }

    @Override
    @End(beforeRedirect = true)
    @Restrict("#{s:hasPermission('BUDGETACTIVITY','UPDATE')}")
    public String update() {
        return super.update();
    }

    @Override
    @End(beforeRedirect = true)
    @Restrict("#{s:hasPermission('BUDGETACTIVITY','DELETE')}")
    public String delete() {
        return super.delete();
    }

    @Override
    @End(beforeRedirect = true)
    public String cancel() {
        return super.cancel();
    }
}
