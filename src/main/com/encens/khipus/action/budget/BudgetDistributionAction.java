package com.encens.khipus.action.budget;

import com.encens.khipus.exception.ConcurrencyException;
import com.encens.khipus.exception.EntryDuplicatedException;
import com.encens.khipus.exception.EntryNotFoundException;
import com.encens.khipus.framework.action.GenericAction;
import com.encens.khipus.framework.action.Outcome;
import com.encens.khipus.framework.service.GenericService;
import com.encens.khipus.model.budget.BudgetDistribution;
import com.encens.khipus.model.budget.BudgetDistributionDetail;
import com.encens.khipus.model.budget.BudgetDistributionType;
import com.encens.khipus.model.budget.BudgetType;
import com.encens.khipus.model.employees.Month;
import com.encens.khipus.service.budget.BudgetDistributionService;
import com.encens.khipus.util.BigDecimalUtil;
import com.encens.khipus.util.MapUtil;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.*;
import org.jboss.seam.annotations.security.Restrict;
import org.jboss.seam.international.StatusMessage;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * BudgetDistributionAction
 *
 * @author
 * @version 2.5
 */
@Name("budgetDistributionAction")
@Scope(ScopeType.CONVERSATION)
public class BudgetDistributionAction extends GenericAction<BudgetDistribution> {

    @In
    private BudgetDistributionService budgetDistributionService;
    private Map<Month, BudgetDistributionDetail> percentDistributionDetailMap = new HashMap<Month, BudgetDistributionDetail>() {
        @Override
        public BudgetDistributionDetail get(Object key) {
            if (key != null) {
                BudgetDistributionDetail value = MapUtil.getNotNullValue(this, (Month) key, super.get(key));
                value.setMonth((Month) key);
                return value;
            }
            return null;
        }
    };
    private BigDecimal detailSumPercentAmount = BigDecimal.ZERO;

    @Override
    public BudgetDistribution createInstance() {
        BudgetDistribution createdInstance = super.createInstance();
        createdInstance.setBudgetDistributionType(BudgetDistributionType.GLOBAL);
        setDetailSumPercentAmount(BigDecimal.ZERO);
        return createdInstance;
    }

    @Factory(value = "budgetDistribution", scope = ScopeType.STATELESS)
    @Restrict("#{s:hasPermission('BUDGETDISTRIBUTION','VIEW')}")
    public BudgetDistribution init() {
        return getInstance();
    }

    @Factory(value = "budgetTypeEnum")
    public BudgetType[] getBudgetTypeEnum() {
        return BudgetType.values();
    }

    @Override
    protected GenericService getService() {
        return budgetDistributionService;
    }

    @Override
    protected String getDisplayNameMessage() {
        return messages.get("BudgetDistribution.title");
    }

    public Map<Month, BudgetDistributionDetail> getPercentDistributionDetailMap() {
        return percentDistributionDetailMap;
    }

    public void setPercentDistributionDetailMap(Map<Month, BudgetDistributionDetail> percentDistributionDetailMap) {
        this.percentDistributionDetailMap = percentDistributionDetailMap;
    }

    public BigDecimal getDetailSumPercentAmount() {
        return detailSumPercentAmount;
    }

    public void setDetailSumPercentAmount(BigDecimal detailSumPercentAmount) {
        this.detailSumPercentAmount = detailSumPercentAmount;
    }

    public void performDetailSumPercentAmount() {
        setDetailSumPercentAmount(BigDecimal.ZERO);
        for (BudgetDistributionDetail budgetDistributionDetail : percentDistributionDetailMap.values()) {
            if (!BigDecimalUtil.isZeroOrNull(budgetDistributionDetail.getPercentDistribution())) {
                setDetailSumPercentAmount(BigDecimalUtil.sum(getDetailSumPercentAmount(), budgetDistributionDetail.getPercentDistribution()));
            }
        }
    }

    public void putAllCurrentDetailValues() {
        for (BudgetDistributionDetail budgetDistributionDetail : getInstance().getBudgetDistributionDetailList()) {
            percentDistributionDetailMap.put(budgetDistributionDetail.getMonth(), budgetDistributionDetail);
        }
    }

    public List<BudgetDistributionDetail> getCurrentDetailValues() {
        return new ArrayList<BudgetDistributionDetail>(percentDistributionDetailMap.values());
    }

    @Override
    @Begin(ifOutcome = Outcome.SUCCESS, flushMode = FlushModeType.MANUAL)
    @Restrict("#{s:hasPermission('BUDGETDISTRIBUTION','VIEW')}")
    public String select(BudgetDistribution instance) {
        String outcome = super.select(instance);
        putAllCurrentDetailValues();
        performDetailSumPercentAmount();
        return outcome;
    }

    @Override
    @End
    @Restrict("#{s:hasPermission('BUDGETDISTRIBUTION','CREATE')}")
    public String create() {
        if (!validateDetailSumPercentAmount() || !validateDuplicated()) {
            return Outcome.REDISPLAY;
        }

        try {
            budgetDistributionService.create(getInstance(), getCurrentDetailValues());
            addCreatedMessage();
            return Outcome.SUCCESS;
        } catch (EntryDuplicatedException e) {
            addDuplicatedMessage();
            return Outcome.REDISPLAY;
        }
    }

    @Override
    @Restrict("#{s:hasPermission('BUDGETDISTRIBUTION','CREATE')}")
    public void createAndNew() {
        if (validateDetailSumPercentAmount() && validateDuplicated()) {
            try {
                budgetDistributionService.create(getInstance(), getCurrentDetailValues());
                addCreatedMessage();
                createInstance();
                percentDistributionDetailMap.clear();
            } catch (EntryDuplicatedException e) {
                addDuplicatedMessage();
            }
        }
    }

    @Override
    @End
    @Restrict("#{s:hasPermission('BUDGETDISTRIBUTION','UPDATE')}")
    public String update() {
        if (!validateDetailSumPercentAmount() || !validateDuplicated()) {
            return Outcome.REDISPLAY;
        }

        Long currentVersion = (Long) getVersion(getInstance());
        try {
            budgetDistributionService.update(getInstance(), getCurrentDetailValues());
        } catch (EntryDuplicatedException e) {
            addDuplicatedMessage();
            setVersion(getInstance(), currentVersion);
            return Outcome.REDISPLAY;
        } catch (ConcurrencyException e) {
            concurrencyLog();
            try {
                setInstance(getService().findById(getEntityClass(), getId(getInstance()), true));
                putAllCurrentDetailValues();
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

    public Boolean validateDetailSumPercentAmount() {
        performDetailSumPercentAmount();
        Boolean valid = BigDecimalUtil.ONE_HUNDRED.compareTo(getDetailSumPercentAmount()) == 0;
        if (!valid) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR, "BudgetDistribution.error.detailSumPercentAmount");
        }
        return valid;
    }

    public Boolean validateDuplicated() {
        Boolean valid = budgetDistributionService.validateDuplicated(getInstance());
        if (!valid) {
            addDuplicatedMessage();
        }
        return valid;
    }

    @Override
    protected void addDuplicatedMessage() {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                "BudgetDistribution.error.duplicated", getInstance().getBusinessUnit().getOrganization().getName(),
                getInstance().getGestion().getYear(), messages.get(getInstance().getType().getResourceKey()));
    }

    @Override
    public void addDeleteReferentialIntegrityMessage() {
        super.addDeleteReferentialIntegrityMessage();
    }
}
