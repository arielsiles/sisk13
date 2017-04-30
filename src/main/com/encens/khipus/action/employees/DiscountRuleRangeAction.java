package com.encens.khipus.action.employees;

import com.encens.khipus.exception.ConcurrencyException;
import com.encens.khipus.exception.EntryDuplicatedException;
import com.encens.khipus.exception.EntryNotFoundException;
import com.encens.khipus.exception.ReferentialIntegrityException;
import com.encens.khipus.exception.employees.DiscountRuleRangeOverlapException;
import com.encens.khipus.framework.action.GenericAction;
import com.encens.khipus.framework.action.Outcome;
import com.encens.khipus.model.employees.DiscountRule;
import com.encens.khipus.model.employees.DiscountRuleRange;
import com.encens.khipus.model.employees.DiscountUnitType;
import com.encens.khipus.service.employees.DiscountRuleRangeService;
import com.encens.khipus.util.MessageUtils;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.*;
import org.jboss.seam.annotations.security.Restrict;
import org.jboss.seam.international.StatusMessage;

import java.util.List;

/**
 * @author
 * @version 3.4
 */
@Name("discountRuleRangeAction")
@Scope(ScopeType.CONVERSATION)
public class DiscountRuleRangeAction extends GenericAction<DiscountRuleRange> {

    @In
    private DiscountRuleRangeService discountRuleRangeService;

    @In
    private DiscountRuleAction discountRuleAction;

    @Create
    public void init() {
        if (!isManaged() && discountRuleAction.isContiguousType()) {
            getInstance().setInitRange(calculateInitRangeValue());
        }
    }

    @Factory(value = "discountRuleRange", scope = ScopeType.STATELESS)
    public DiscountRuleRange initDiscountRuleRange() {
        return getInstance();
    }


    @Override
    protected String getDisplayNameMessage() {
        return getInstance().getName();
    }

    @Begin(ifOutcome = Outcome.SUCCESS, flushMode = FlushModeType.MANUAL, nested = true)
    public String newInstance() {
        setOp(OP_CREATE);
        setDiscountRule();
        return Outcome.SUCCESS;
    }

    @Override
    @Begin(nested = true, ifOutcome = Outcome.SUCCESS, flushMode = FlushModeType.MANUAL)
    @Restrict("#{s:hasPermission('DISCOUNTRULE','VIEW')}")
    public String select(DiscountRuleRange instance) {
        return super.select(instance);
    }

    @Override
    @End(beforeRedirect = true)
    @Restrict("#{s:hasPermission('DISCOUNTRULE','CREATE')}")
    public String create() {
        if (!isValidRange()) {
            showEmptyRangeMessage();
            return Outcome.REDISPLAY;
        }
        try {
            discountRuleRangeService.createDiscountRuleRange(getInstance());
            addCreatedMessage();
            return Outcome.SUCCESS;
        } catch (EntryDuplicatedException e) {
            addDuplicatedMessage();
            return Outcome.REDISPLAY;
        } catch (DiscountRuleRangeOverlapException e) {
            addRangeOverlapMessage(e);
            return Outcome.REDISPLAY;
        }
    }

    private void showEmptyRangeMessage() {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR, "DiscountRuleRange.error.emptyRange");
    }

    private boolean isValidRange() {
        return null != getInstance().getInitRange() || null != getInstance().getEndRange();
    }

    @Override
    @Restrict("#{s:hasPermission('DISCOUNTRULE','CREATE')}")
    public void createAndNew() {
        if (!isValidRange()) {
            showEmptyRangeMessage();
            return;
        }
        try {
            discountRuleRangeService.createDiscountRuleRange(getInstance());
            addCreatedMessage();
            createInstance();
            setDiscountRule();
            init();
        } catch (EntryDuplicatedException e) {
            addDuplicatedMessage();
        } catch (DiscountRuleRangeOverlapException e) {
            addRangeOverlapMessage(e);
        }
    }

    @Override
    @End(beforeRedirect = true)
    @Restrict("#{s:hasPermission('DISCOUNTRULE','UPDATE')}")
    public String update() {
        if (!isValidRange()) {
            showEmptyRangeMessage();
            return Outcome.REDISPLAY;
        }
        Long currentVersion = (Long) getVersion(getInstance());
        try {
            discountRuleRangeService.updateDiscountRuleRange(getInstance());
        } catch (EntryDuplicatedException e) {
            addDuplicatedMessage();
            setVersion(getInstance(), currentVersion);
            return Outcome.REDISPLAY;
        } catch (ConcurrencyException e) {
            concurrencyLog();
            try {
                setInstance(getService().findById(getEntityClass(), getId(getInstance()), true));
            } catch (EntryNotFoundException e1) {
                entryNotFoundLog();
                addNotFoundMessage();
                return Outcome.FAIL;
            }
            addUpdateConcurrencyMessage();
            return Outcome.REDISPLAY;
        } catch (DiscountRuleRangeOverlapException e) {
            addRangeOverlapMessage(e);
            return Outcome.REDISPLAY;
        }

        addUpdatedMessage();
        return Outcome.SUCCESS;
    }

    @Override
    @End(beforeRedirect = true)
    @Restrict("#{s:hasPermission('DISCOUNTRULE','DELETE')}")
    public String delete() {
        try {
            discountRuleRangeService.deleteDiscountRuleRange(getInstance());
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

    private void setDiscountRule() {
        getInstance().setDiscountRule(discountRuleAction.getInstance());
    }

    /**
     * Calculate the default init range, this is before rule end range + 1
     *
     * @return Integer
     */
    private Integer calculateInitRangeValue() {
        Integer initRange = null;

        DiscountRuleRange lastDiscountRuleRange = discountRuleRangeService.findLastDiscountRuleRange(discountRuleAction.getInstance());
        if (lastDiscountRuleRange != null) {
            initRange = lastDiscountRuleRange.getEndRange() != null ? lastDiscountRuleRange.getEndRange() + 1 : lastDiscountRuleRange.getInitRange();
        }
        return initRange;
    }

    public boolean isNewOrLastDiscountRuleRange() {
        boolean isNewOrLast;
        if (isManaged()) {
            DiscountRuleRange lastDiscountRuleRange = discountRuleRangeService.findLastDiscountRuleRange(discountRuleAction.getInstance());
            isNewOrLast = lastDiscountRuleRange != null && lastDiscountRuleRange.getId().equals(getInstance().getId());
        } else {
            isNewOrLast = true;
        }
        return isNewOrLast;
    }

    public boolean isFirstInitRange() {
        List discountRuleRangeList = discountRuleRangeService.findByDiscountRule(discountRuleAction.getInstance());
        return discountRuleRangeList.isEmpty() || (discountRuleRangeList.size() == 1 && isManaged());
    }

    public boolean isInitRangeEditable() {
        return isFirstInitRange() || discountRuleAction.isOverlapType();
    }

    public boolean isEndRangeEditable() {
        return isNewOrLastDiscountRuleRange() || discountRuleAction.isOverlapType();
    }

    public boolean isShowDeleteButton() {
        return isNewOrLastDiscountRuleRange() || discountRuleAction.isOverlapType();
    }

    public String getDiscountUnitMessage() {
        String unit = "";
        DiscountRule discountRule = discountRuleAction.getInstance();
        if (DiscountUnitType.CURRENCY.equals(discountRule.getDiscountUnitType())) {
            if (discountRule.getCurrency() != null) {
                unit = discountRule.getCurrency().getSymbol();
            }
        }
        if (DiscountUnitType.PERCENT.equals(discountRule.getDiscountUnitType())) {
            unit = MessageUtils.getMessage(discountRule.getDiscountUnitType().getResourceKey());
        }
        return unit;
    }

    private void addRangeOverlapMessage(DiscountRuleRangeOverlapException e) {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR, "DiscountRuleRange.error.overlap", e.getOverlapRuleName());
    }

}
