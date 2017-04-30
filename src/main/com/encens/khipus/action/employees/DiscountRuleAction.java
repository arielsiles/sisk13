package com.encens.khipus.action.employees;

import com.encens.khipus.exception.ConcurrencyException;
import com.encens.khipus.exception.EntryDuplicatedException;
import com.encens.khipus.exception.EntryNotFoundException;
import com.encens.khipus.exception.ReferentialIntegrityException;
import com.encens.khipus.framework.action.GenericAction;
import com.encens.khipus.framework.action.Outcome;
import com.encens.khipus.model.employees.*;
import com.encens.khipus.service.employees.DiscountRuleRangeService;
import com.encens.khipus.service.employees.DiscountRuleService;
import com.encens.khipus.service.employees.TaxPayrollUtilService;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.*;
import org.jboss.seam.annotations.security.Restrict;
import org.jboss.seam.international.StatusMessage;

import java.util.ArrayList;
import java.util.List;

/**
 * @author
 * @version 3.4
 */
@Name("discountRuleAction")
@Scope(ScopeType.CONVERSATION)
public class DiscountRuleAction extends GenericAction<DiscountRule> {

    @In
    private DiscountRuleService discountRuleService;

    @In
    private DiscountRuleRangeService discountRuleRangeService;

    @In
    private TaxPayrollUtilService taxPayrollUtilService;

    @Factory(value = "discountRule", scope = ScopeType.STATELESS)
    public DiscountRule initDiscountRule() {
        return getInstance();
    }

    @Override
    protected String getDisplayNameMessage() {
        return getInstance().getName();
    }

    @Override
    @Restrict("#{s:hasPermission('DISCOUNTRULE','VIEW')}")
    @Begin(ifOutcome = Outcome.SUCCESS, flushMode = FlushModeType.MANUAL)
    public String select(DiscountRule instance) {
        return super.select(instance);
    }

    @Override
    public void refreshInstance() {
        super.refreshInstance();
    }

    @Override
    @End
    @Restrict("#{s:hasPermission('DISCOUNTRULE','CREATE')}")
    public String create() {
        if (!passCreateSolidaryValidation()) {
            addThereIsActiveSolidaryAFPRule();
            return Outcome.REDISPLAY;
        }
        taxPayrollUtilService.findActiveNationalSolidaryAfpDiscountRule();
        try {
            discountRuleService.createDiscountRule(getInstance());
            addCreatedMessage();
            return Outcome.SUCCESS;
        } catch (EntryDuplicatedException e) {
            addDuplicatedMessage();
            return Outcome.REDISPLAY;
        }
    }

    private void addThereIsActiveSolidaryAFPRule() {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR, "DiscountRule.error.thereIsActiveSolidaryAFPRule");
    }

    private boolean passCreateSolidaryValidation() {
        return !DiscountRuleType.SOLIDARY_AFP.equals(getInstance().getDiscountRuleType()) || null == taxPayrollUtilService.findActiveNationalSolidaryAfpDiscountRule();
    }

    @Override
    @Restrict("#{s:hasPermission('DISCOUNTRULE','CREATE')}")
    public void createAndNew() {
        if (!passCreateSolidaryValidation()) {
            addThereIsActiveSolidaryAFPRule();
            return;
        }

        try {
            discountRuleService.createDiscountRule(getInstance());
            addCreatedMessage();
            createInstance();
        } catch (EntryDuplicatedException e) {
            addDuplicatedMessage();
        }
    }

    @Override
    @End
    @Restrict("#{s:hasPermission('DISCOUNTRULE','UPDATE')}")
    public String update() {
        Long currentVersion = (Long) getVersion(getInstance());
        try {
            discountRuleService.updateDiscountRule(getInstance());
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
        }
        addUpdatedMessage();
        return Outcome.SUCCESS;
    }

    @Override
    @End
    @Restrict("#{s:hasPermission('DISCOUNTRULE','DELETE')}")
    public String delete() {
        try {
            discountRuleService.deleteDiscountRule(getInstance());
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

    public boolean isCurrencyDiscountUnitType() {
        return DiscountUnitType.CURRENCY.equals(getInstance().getDiscountUnitType());
    }

    public boolean isPercentDiscountUnitType() {
        return DiscountUnitType.PERCENT.equals(getInstance().getDiscountUnitType());
    }

    public void cleanCurrency() {
        getInstance().setCurrency(null);
    }

    public boolean hasRangeRuleDefined() {
        List discountRuleRangeList = discountRuleRangeService.findByDiscountRule(getInstance());
        return (!discountRuleRangeList.isEmpty());
    }

    public void discountRuleRangeTypeChanged() {
        getInstance().setJobCategory(null);
        getInstance().setGestion(null);
        getInstance().setBusinessUnit(null);
    }

    public boolean isLatenessType() {
        return null != getInstance().getDiscountRuleType() && DiscountRuleType.LATENESS.equals(getInstance().getDiscountRuleType());
    }

    public boolean isAFPType() {
        return null != getInstance().getDiscountRuleType() && DiscountRuleType.SOLIDARY_AFP.equals(getInstance().getDiscountRuleType());
    }

    public boolean isContiguousType() {
        return null != getInstance().getIntervalType() && IntervalType.CONTIGUOUS.equals(getInstance().getIntervalType());
    }

    public boolean isOverlapType() {
        return null != getInstance().getIntervalType() && IntervalType.OVERLAP.equals(getInstance().getIntervalType());
    }

    public List<DiscountRuleRangeType> getDiscountRuleRangeType() {
        List<DiscountRuleRangeType> discountRuleRangeTypes = new ArrayList<DiscountRuleRangeType>();
        if (isLatenessType()) {
            discountRuleRangeTypes.add(DiscountRuleRangeType.MINUTE);
        }
        if (isAFPType()) {
            discountRuleRangeTypes.add(DiscountRuleRangeType.AMOUNT);
        }
        return discountRuleRangeTypes;
    }

    public List<IntervalType> getIntervalType() {
        List<IntervalType> intervalTypes = new ArrayList<IntervalType>();
        if (isLatenessType()) {
            intervalTypes.add(IntervalType.CONTIGUOUS);
        }

        if (isAFPType()) {
            intervalTypes.add(IntervalType.OVERLAP);
        }
        return intervalTypes;
    }

    public boolean isDisableActive() {
        DiscountRule activeNationalSolidaryAfpDiscountRule = taxPayrollUtilService.findActiveNationalSolidaryAfpDiscountRule();
        return isManaged() && null != activeNationalSolidaryAfpDiscountRule && activeNationalSolidaryAfpDiscountRule.getId().compareTo(getInstance().getId()) == 0;
    }
}
