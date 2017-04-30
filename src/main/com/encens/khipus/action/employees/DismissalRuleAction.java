package com.encens.khipus.action.employees;

import com.encens.khipus.exception.EntryDuplicatedException;
import com.encens.khipus.framework.action.GenericAction;
import com.encens.khipus.framework.action.Outcome;
import com.encens.khipus.model.employees.AmountType;
import com.encens.khipus.model.employees.DismissalRule;
import com.encens.khipus.service.employees.DismissalRuleService;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.*;
import org.jboss.seam.annotations.security.Restrict;

/**
 * @author
 * @version 3.4
 */
@Name("dismissalRuleAction")
@Scope(ScopeType.CONVERSATION)
public class DismissalRuleAction extends GenericAction<DismissalRule> {

    @In
    private DismissalRuleService dismissalRuleService;

    @Factory(value = "dismissalRule", scope = ScopeType.STATELESS)
    public DismissalRule init() {
        return getInstance();
    }

    @Override
    public DismissalRule createInstance() {
        DismissalRule result = super.createInstance();
        result.setActive(true);
        return result;
    }

    @Override
    protected String getDisplayNameMessage() {
        return getInstance().getFullName();
    }

    @Override
    @End
    @Restrict("#{s:hasPermission('DISMISSALRULE','CREATE')}")
    public String create() {
        try {
            dismissalRuleService.createDismissalRule(getInstance());
            addCreatedMessage();
            return Outcome.SUCCESS;
        } catch (EntryDuplicatedException e) {
            addDuplicatedMessage();
            return Outcome.FAIL;
        }
    }

    @Override
    @Restrict("#{s:hasPermission('DISMISSALRULE','CREATE')}")
    public void createAndNew() {
        try {
            dismissalRuleService.createDismissalRule(getInstance());
            addCreatedMessage();
            createInstance();
        } catch (EntryDuplicatedException e) {
            addDuplicatedMessage();
        }
    }

    @Override
    @End
    @Restrict("#{s:hasPermission('DISMISSALRULE','UPDATE')}")
    public String update() {
        return super.update();
    }

    @Override
    @End
    @Restrict("#{s:hasPermission('DISMISSALRULE','DELETE')}")
    public String delete() {
        return super.delete();
    }

    @SuppressWarnings({"NullableProblems"})
    public void amountTypeChanged() {
        getInstance().setCurrency(null);
    }

    public boolean isFixed() {
        return isAmountType(AmountType.FIXED);
    }

    public boolean isPercentage() {
        return isAmountType(AmountType.PERCENT);
    }

    private boolean isAmountType(AmountType amountType) {
        AmountType instanceAmountType = getInstance().getAmountType();
        return null != instanceAmountType && instanceAmountType.equals(amountType);
    }
}