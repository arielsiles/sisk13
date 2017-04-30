package com.encens.khipus.action.budget;

import com.encens.khipus.framework.action.GenericAction;
import com.encens.khipus.framework.action.Outcome;
import com.encens.khipus.model.budget.BudgetProgram;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.*;
import org.jboss.seam.annotations.security.Restrict;

/**
 * BudgetProgramAction
 *
 * @author
 * @version 2.0
 */
@Name("budgetProgramAction")
@Scope(ScopeType.CONVERSATION)
public class BudgetProgramAction extends GenericAction<BudgetProgram> {

    @Factory(value = "budgetProgram", scope = ScopeType.STATELESS)
    @Restrict("#{s:hasPermission('BUDGETPROGRAM','VIEW')}")
    public BudgetProgram initBudgetProgram() {
        return getInstance();
    }

    @Override
    protected String getDisplayNameProperty() {
        return "name";
    }

    @Override
    @Begin(ifOutcome = Outcome.SUCCESS, flushMode = FlushModeType.MANUAL)
    @Restrict("#{s:hasPermission('BUDGETPROGRAM','VIEW')}")
    public String select(BudgetProgram instance) {
        return super.select(instance);
    }

    @Override
    @Restrict("#{s:hasPermission('BUDGETPROGRAM','CREATE')}")
    public String create() {
        String outcome = super.create();
        if (Outcome.SUCCESS.equals(outcome)) {
            select(getInstance());
        }
        return Outcome.REDISPLAY;
    }

    @Override
    @Restrict("#{s:hasPermission('BUDGETPROGRAM','CREATE')}")
    public void createAndNew() {
        super.createAndNew();
    }

    @Override
    @End
    @Restrict("#{s:hasPermission('BUDGETPROGRAM','UPDATE')}")
    public String update() {
        return super.update();
    }

    @Override
    @End
    @Restrict("#{s:hasPermission('BUDGETPROGRAM','DELETE')}")
    public String delete() {
        return super.delete();
    }
}
