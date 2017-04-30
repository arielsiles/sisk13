package com.encens.khipus.action.finances;

import com.encens.khipus.exception.ConcurrencyException;
import com.encens.khipus.exception.EntryNotFoundException;
import com.encens.khipus.exception.ReferentialIntegrityException;
import com.encens.khipus.exception.finances.RotatoryFundLiquidatedException;
import com.encens.khipus.exception.finances.RotatoryFundNullifiedException;
import com.encens.khipus.exception.finances.SpendDistributionPercentageSumExceedsOneHundredException;
import com.encens.khipus.framework.action.GenericAction;
import com.encens.khipus.framework.action.Outcome;
import com.encens.khipus.framework.service.GenericService;
import com.encens.khipus.model.finances.CashAccount;
import com.encens.khipus.model.finances.CostCenter;
import com.encens.khipus.model.finances.RotatoryFund;
import com.encens.khipus.model.finances.SpendDistribution;
import com.encens.khipus.service.finances.RotatoryFundService;
import com.encens.khipus.service.finances.SpendDistributionService;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.*;
import org.jboss.seam.annotations.security.Restrict;
import org.jboss.seam.international.StatusMessage;

import java.util.ArrayList;
import java.util.List;

/**
 * @author
 * @version 2.22
 */
@Name("spendDistributionAction")
@Scope(ScopeType.CONVERSATION)
public class SpendDistributionAction extends GenericAction<SpendDistribution> {
    public static final String ACCOUNT_CLASS = "G";

    @In
    private RotatoryFundService rotatoryFundService;

    @In
    private SpendDistributionService spendDistributionService;

    @In(value = "rotatoryFundAction")
    private RotatoryFundAction rotatoryFundAction;

    private List<SpendDistribution> instances = new ArrayList<SpendDistribution>();

    @Create
    public void init() {
        getInstance().setBusinessUnit(rotatoryFundAction.getInstance().getBusinessUnit());
        getInstance().setRotatoryFund(rotatoryFundAction.getInstance());
    }

    @Factory(value = "spendDistribution", scope = ScopeType.STATELESS)
    public SpendDistribution initSpendDistribution() {
        return getInstance();
    }

    @Begin(nested = true, ifOutcome = Outcome.SUCCESS)
    public String addSpendDistribution() {
        if (rotatoryFundService.isRotatoryFundNullified(getRotatoryFund())) {
            /* in order to refresh the instance since the database*/
            rotatoryFundService.findRotatoryFund(getRotatoryFund().getId());
            addRotatoryFundAnnulledError();
            return Outcome.REDISPLAY;
        }
        if (rotatoryFundService.isRotatoryFundLiquidated(getRotatoryFund())) {
            /* in order to refresh the instance since the database*/
            rotatoryFundService.findRotatoryFund(getRotatoryFund().getId());
            addRotatoryFundLiquidatedError();
            return Outcome.REDISPLAY;
        }
        /* to create the new SpendDistribution instance*/
        setInstance(null);
        setOp(OP_CREATE);
        init();
        return Outcome.SUCCESS;
    }

    @Override
    @End(beforeRedirect = true)
    @Restrict("#{s:hasPermission('SPENDDISTRIBUTION','CREATE')}")
    public String create() {
        if (getInstance().getCashAccount() == null && getInstance().getCostCenter() == null) {
            addEmptyError();
            return Outcome.REDISPLAY;
        }
        try {
            spendDistributionService.createSpendDistribution(getInstance());
            addCreatedMessage();
        } catch (RotatoryFundNullifiedException e) {
            addRotatoryFundAnnulledError();
            return Outcome.REDISPLAY;
        } catch (RotatoryFundLiquidatedException e) {
            addRotatoryFundLiquidatedError();
            return Outcome.REDISPLAY;
        } catch (SpendDistributionPercentageSumExceedsOneHundredException e) {
            addSpendDistributionPercentageSumExceedsOneHundredError();
            return Outcome.REDISPLAY;
        }
        return Outcome.SUCCESS;
    }

    @Override
    @Restrict("#{s:hasPermission('SPENDDISTRIBUTION','CREATE')}")
    public void createAndNew() {
        if (getInstance().getCashAccount() == null && getInstance().getCostCenter() == null) {
            addEmptyError();
        }
        try {
            spendDistributionService.createSpendDistribution(getInstance());
            addCreatedMessage();
            addSpendDistribution();
        } catch (RotatoryFundNullifiedException e) {
            addRotatoryFundAnnulledError();
        } catch (RotatoryFundLiquidatedException e) {
            addRotatoryFundLiquidatedError();
        } catch (SpendDistributionPercentageSumExceedsOneHundredException e) {
            addSpendDistributionPercentageSumExceedsOneHundredError();
        }
    }

    @Override
    @Begin(nested = true, ifOutcome = Outcome.SUCCESS, flushMode = FlushModeType.MANUAL)
    @Restrict("#{s:hasPermission('SPENDDISTRIBUTION','VIEW')}")
    public String select(SpendDistribution instance) {
        try {
            setOp(OP_UPDATE);
            /*refresh the instance from database*/
            setInstance(spendDistributionService.findSpendDistribution(instance.getId()));
        } catch (EntryNotFoundException e) {
            addNotFoundMessage();
            return Outcome.FAIL;
        }
        return Outcome.SUCCESS;
    }

    @Override
    @End(beforeRedirect = true)
    @Restrict("#{s:hasPermission('SPENDDISTRIBUTION','UPDATE')}")
    public String update() {
        if (getInstance().getCashAccount() == null && getInstance().getCostCenter() == null) {
            addEmptyError();
            return Outcome.REDISPLAY;
        }
        try {
            spendDistributionService.updateRotatoryFund(getInstance());
            addUpdatedMessage();
        } catch (EntryNotFoundException e) {
            addNotFoundMessage();
            return Outcome.FAIL;
        } catch (RotatoryFundLiquidatedException e) {
            addRotatoryFundLiquidatedError();
            return Outcome.REDISPLAY;
        } catch (RotatoryFundNullifiedException e) {
            addRotatoryFundAnnulledError();
            return Outcome.REDISPLAY;
        } catch (SpendDistributionPercentageSumExceedsOneHundredException e) {
            addSpendDistributionPercentageSumExceedsOneHundredError();
            return Outcome.REDISPLAY;
        } catch (ConcurrencyException e) {
            concurrencyLog();
            try {
                spendDistributionService.findSpendDistribution(getInstance().getId());
                addUpdateConcurrencyMessage();
                return Outcome.REDISPLAY;
            } catch (EntryNotFoundException e1) {
                addNotFoundMessage();
                return Outcome.FAIL;
            }
        }
        return Outcome.SUCCESS;
    }

    @Override
    @End(beforeRedirect = true)
    @Restrict("#{s:hasPermission('SPENDDISTRIBUTION','DELETE')}")
    public String delete() {
        try {
            spendDistributionService.deleteRotatoryFund(getInstance());
            addDeletedMessage();
        } catch (RotatoryFundLiquidatedException e) {
            addRotatoryFundLiquidatedError();
            return Outcome.REDISPLAY;
        } catch (ReferentialIntegrityException e) {
            addDeleteReferentialIntegrityMessage();
        } catch (EntryNotFoundException e) {
            addNotFoundMessage();
            return Outcome.FAIL;
        } catch (RotatoryFundNullifiedException e) {
            addRotatoryFundAnnulledError();
            return Outcome.REDISPLAY;
        }
        return Outcome.SUCCESS;
    }

    @Override
    @End(beforeRedirect = true)
    public String cancel() {
        return super.cancel();
    }

    @Override
    protected String getDisplayNameMessage() {
        return getInstance().getPercentage().toString();
    }

    @Override
    protected GenericService getService() {
        return spendDistributionService;
    }

    public boolean isEnabledOptions() {
        return true;
    }

    public boolean isEnableBusinessUnit() {
        return !isManaged() || (isManaged() && (rotatoryFundAction.isRotatoryFundPending() || rotatoryFundAction.isRotatoryFundApproved()));
    }

    public boolean isEnableCashAccount() {
        return !isManaged() || (isManaged() && (rotatoryFundAction.isRotatoryFundPending() || rotatoryFundAction.isRotatoryFundApproved()));
    }

    public boolean isEnableCostCenter() {
        return !isManaged() || (isManaged() && (rotatoryFundAction.isRotatoryFundPending() || rotatoryFundAction.isRotatoryFundApproved()));
    }

    public String getCostCenterFullName() {
        return getInstance().getCostCenter() != null ? getInstance().getCostCenter().getFullName() : null;
    }

    public void assignCostCenter(CostCenter costCenter) {
        getInstance().setCostCenter(costCenter);
    }

    public void clearCostCenter() {
        getInstance().setCostCenter(null);
    }

    public RotatoryFund getRotatoryFund() {
        return rotatoryFundAction.getInstance();
    }

    public void assignCashAccount(CashAccount cashAccount) {
        try {
            cashAccount = getService().findById(CashAccount.class, cashAccount.getId());
        } catch (EntryNotFoundException e) {
            entryNotFoundLog();
        }
        getInstance().setCashAccount(cashAccount);
    }

    public void clearCashAccount() {
        getInstance().setCashAccount(null);
    }

    public String getAccountClass() {
        return ACCOUNT_CLASS;
    }

    /* Messages*/

    @Override
    protected void addCreatedMessage() {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.INFO,
                "SpendDistribution.message.created");
    }

    protected void addEmptyError() {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                "SpendDistribution.error.empty");
    }

    @Override
    protected void addDeletedMessage() {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.INFO,
                "SpendDistribution.message.deleted");
    }

    @Override
    protected void addUpdatedMessage() {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.INFO,
                "SpendDistribution.message.updated");
    }

    private void addRotatoryFundAnnulledError() {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                "SpendDistribution.error.rotatoryFundAlreadyAnnulled", getRotatoryFund().getCode());
    }

    private void addRotatoryFundLiquidatedError() {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                "SpendDistribution.error.rotatoryFundAlreadyLiquidated", getRotatoryFund().getCode());
    }

    private void addSpendDistributionPercentageSumExceedsOneHundredError() {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                "SpendDistribution.error.spendDistributionPercentageSumExceedsOneHundred", getRotatoryFund().getAmount());
    }

}