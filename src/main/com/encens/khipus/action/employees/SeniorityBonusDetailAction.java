package com.encens.khipus.action.employees;

import com.encens.khipus.framework.action.GenericAction;
import com.encens.khipus.framework.action.Outcome;
import com.encens.khipus.model.employees.SeniorityBonusDetail;
import com.encens.khipus.util.employees.SeniorityBonusDetailAmountCalculator;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.*;
import org.jboss.seam.annotations.security.Restrict;

/**
 * @author
 * @version 2.26
 */
@Name("seniorityBonusDetailAction")
@Scope(ScopeType.CONVERSATION)
public class SeniorityBonusDetailAction extends GenericAction<SeniorityBonusDetail> {

    @In
    private BonusAction bonusAction;

    @Factory(value = "seniorityBonusDetail", scope = ScopeType.STATELESS)
    @Restrict("#{s:hasPermission('BONUS','VIEW')}")
    public SeniorityBonusDetail initSeniorityBonusDetail() {
        return getInstance();
    }

    @Begin(ifOutcome = Outcome.SUCCESS, flushMode = FlushModeType.MANUAL, nested = true)
    @Restrict("#{s:hasPermission('BONUS','CREATE')}")
    public String newInstance() {
        setOp(OP_CREATE);
        getInstance();
        return Outcome.SUCCESS;
    }

    @Override
    @Begin(ifOutcome = Outcome.SUCCESS, flushMode = FlushModeType.MANUAL, nested = true)
    @Restrict("#{s:hasPermission('BONUS','VIEW')}")
    public String select(SeniorityBonusDetail instance) {
        setOp(OP_UPDATE);
        setInstance(instance);
        return Outcome.SUCCESS;
    }

    @Override
    @End(beforeRedirect = true)
    @Restrict("#{s:hasPermission('BONUS','CREATE')}")
    public String create() {
        bonusAction.getSeniorityBonusDetails().add(getInstance());
        return Outcome.SUCCESS;
    }

    @Override
    @End(beforeRedirect = true)
    @Restrict("#{s:hasPermission('BONUS','UPDATE')}")
    public String update() {
        return Outcome.SUCCESS;
    }

    @Override
    @End(beforeRedirect = true)
    @Restrict("#{s:hasPermission('BONUS','DELETE')}")
    public String delete() {
        bonusAction.getSeniorityBonusDetails().remove(getInstance());
        return Outcome.SUCCESS;
    }

    @Override
    @End(beforeRedirect = true)
    public String cancel() {
        return super.cancel();
    }

    public void calculateAmount() {
        SeniorityBonusDetailAmountCalculator
                .getInstance(bonusAction.getInstance().getSmnRate())
                .execute(getInstance());
    }
}
