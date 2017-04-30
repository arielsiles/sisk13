package com.encens.khipus.action.finances;

import com.encens.khipus.exception.ConcurrencyException;
import com.encens.khipus.exception.EntryNotFoundException;
import com.encens.khipus.exception.ReferentialIntegrityException;
import com.encens.khipus.exception.finances.*;
import com.encens.khipus.framework.action.GenericAction;
import com.encens.khipus.framework.action.Outcome;
import com.encens.khipus.framework.service.GenericService;
import com.encens.khipus.model.finances.*;
import com.encens.khipus.service.finances.RotatoryFundCollectionService;
import com.encens.khipus.service.finances.RotatoryFundCollectionSpendDistributionService;
import com.encens.khipus.service.finances.RotatoryFundService;
import com.encens.khipus.service.finances.SpendDistributionService;
import com.encens.khipus.util.BigDecimalUtil;
import com.encens.khipus.util.ValidatorUtil;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.*;
import org.jboss.seam.annotations.security.Restrict;
import org.jboss.seam.international.StatusMessage;

import java.util.ArrayList;
import java.util.List;

/**
 * @author
 * @version 2.23
 */
@Name("rotatoryFundCollectionSpendDistributionAction")
@Scope(ScopeType.CONVERSATION)
public class RotatoryFundCollectionSpendDistributionAction extends GenericAction<RotatoryFundCollectionSpendDistribution> {
    public static final String ACCOUNT_CLASS = "G";

    @In
    private RotatoryFundService rotatoryFundService;

    @In
    private RotatoryFundCollectionService rotatoryFundCollectionService;

    @In
    private RotatoryFundCollectionSpendDistributionService rotatoryFundCollectionSpendDistributionService;
    @In
    private SpendDistributionService spendDistributionService;

    @In(value = "rotatoryFundCollectionAction", required = false, create = true)
    private RotatoryFundCollectionAction rotatoryFundCollectionAction;
    @In(value = "rotatoryFundCashAccountDataModel", required = false, create = true)
    private RotatoryFundCashAccountDataModel rotatoryFundCashAccountDataModel;


    private List<RotatoryFundCollectionSpendDistribution> instances = new ArrayList<RotatoryFundCollectionSpendDistribution>();

    @Create
    public void init() {
        getInstance().setAmount(BigDecimalUtil.subtract(getRotatoryFundCollection().getSourceAmount(),
                rotatoryFundCollectionSpendDistributionService.getAmountRotatoryFundCollectionSpendDistributionSum(rotatoryFundCollectionAction.getInstance()), 3));
        getInstance().setBusinessUnit(getRotatoryFund().getBusinessUnit());
        getInstance().setCostCenter(getRotatoryFund().getJobContract().getJob().getOrganizationalUnit().getCostCenter());
        getInstance().setRotatoryFundCollection(getRotatoryFundCollection());
    }

    @Factory(value = "rotatoryFundCollectionSpendDistribution", scope = ScopeType.STATELESS)
    public RotatoryFundCollectionSpendDistribution initRotatoryFundCollectionSpendDistribution() {
        return getInstance();
    }


    public List<CostCenter> getCostCenterList() {
        return spendDistributionService.getCostCenterListBySpendDistribution(getInstance().getRotatoryFundCollection().getRotatoryFund());
    }

    public List<CashAccount> getCashAccountList() {
        List<CashAccount> cashAccountResultList = new ArrayList<CashAccount>();
        List<CashAccount> cashAccountList = spendDistributionService.getCashAccountListBySpendDistribution(getInstance().getRotatoryFundCollection().getRotatoryFund());
        for (CashAccount cashAccount : cashAccountList) {
            if (getRotatoryFundCollection().getSourceCurrency().equals(cashAccount.getCurrency())) {
                cashAccountResultList.add(cashAccount);
            }
        }
        return cashAccountResultList;
    }

    public boolean isEnableCostCenterList() {
        return !ValidatorUtil.isEmptyOrNull(getCostCenterList());
    }

    public boolean isEnableCashAccountList() {
        return !ValidatorUtil.isEmptyOrNull(spendDistributionService.getCashAccountListBySpendDistribution(getInstance().getRotatoryFundCollection().getRotatoryFund()));
    }

    @Begin(nested = true, ifOutcome = Outcome.SUCCESS)
    public String addRotatoryFundCollectionSpendDistribution() {
        try {
            if (rotatoryFundCollectionService.isRotatoryFundCollectionNullified(getRotatoryFundCollection())) {
                /* in order to refresh the instance since the database*/
                rotatoryFundCollectionService.findRotatoryFundCollection(getRotatoryFundCollection().getId());
                addRotatoryFundCollectionAnnulledError();
                return Outcome.REDISPLAY;
            }
            if (rotatoryFundCollectionService.isRotatoryFundCollectionApproved(getRotatoryFundCollection())) {
                /* in order to refresh the instance since the database*/
                rotatoryFundCollectionService.findRotatoryFundCollection(getRotatoryFundCollection().getId());
                addRotatoryFundCollectionApprovedError();
                return Outcome.REDISPLAY;
            }
            /*if the distribution already cover the 100% */
            if (rotatoryFundCollectionSpendDistributionService.getAmountRotatoryFundCollectionSpendDistributionSum(getRotatoryFundCollection()).doubleValue() >= getRotatoryFundCollection().getSourceAmount().doubleValue()) {
                addRotatoryFundCollectionSpendDistributionAmountSumExceedsTotalError();
                return Outcome.REDISPLAY;
            }
        } catch (RotatoryFundCollectionNotFoundException e) {
            addNotFoundMessage();
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
    @Restrict("#{s:hasPermission('ROTATORYFUNDCOLLECTIONSPENDDISTRIBUTION','CREATE')}")
    public String create() {
        if (!validate()) {
            return Outcome.REDISPLAY;
        }
        try {
            rotatoryFundCollectionSpendDistributionService.createRotatoryFundCollectionSpendDistribution(getInstance());
            addCreatedMessage();
        } catch (RotatoryFundCollectionApprovedException e) {
            addRotatoryFundCollectionApprovedError();
            return Outcome.REDISPLAY;
        } catch (RotatoryFundCollectionSpendDistributionPercentageSumExceedsOneHundredException e) {
            addRotatoryFundCollectionSpendDistributionPercentageSumExceedsOneHundredError();
            return Outcome.REDISPLAY;
        } catch (RotatoryFundApprovedException e) {
            addRotatoryFundApprovedError();
            return Outcome.REDISPLAY;
        } catch (RotatoryFundNullifiedException e) {
            addRotatoryFundAnnulledError();
            return Outcome.REDISPLAY;
        } catch (RotatoryFundLiquidatedException e) {
            addRotatoryFundLiquidatedError();
            return Outcome.REDISPLAY;
        } catch (RotatoryFundCollectionNotFoundException e) {
            addNotFoundMessage();
            return Outcome.REDISPLAY;
        } catch (RotatoryFundCollectionSpendDistributionAmountSumExceedsTotalException e) {
            addRotatoryFundCollectionSpendDistributionAmountSumExceedsTotalError();
            return Outcome.REDISPLAY;
        }
        return Outcome.SUCCESS;
    }

    private boolean validate() {
        Boolean valid = isValidAccount();
        if (!valid) {
            addAccountValidError();
        }
        return valid;
    }

    private boolean isValidAccount() {
        List<SpendDistribution> spendDistributionList = spendDistributionService.getSpendDistributionList(getRotatoryFund());
        List<CashAccount> cashAccountList = new ArrayList<CashAccount>();
        for (SpendDistribution spendDistribution : spendDistributionList) {
            if (spendDistribution.getCashAccount() != null) {
                cashAccountList.add(spendDistribution.getCashAccount());
            }
        }
        boolean contains = false;
        if (!ValidatorUtil.isEmptyOrNull(cashAccountList)) {
            for (CashAccount cashAccount : cashAccountList) {
                if (cashAccount.getAccountCode() != null &&
                        cashAccount.getAccountCode().equals(getInstance().getAccountCode())) {
                    contains = true;
                }
            }
        } else {
            return true;
        }
        return contains;
    }

    @Override
    @Restrict("#{s:hasPermission('ROTATORYFUNDCOLLECTIONSPENDDISTRIBUTION','CREATE')}")
    public void createAndNew() {
        if (validate()) {
            try {
                rotatoryFundCollectionSpendDistributionService.createRotatoryFundCollectionSpendDistribution(getInstance());
                addCreatedMessage();
                addRotatoryFundCollectionSpendDistribution();
            } catch (RotatoryFundCollectionApprovedException e) {
                addRotatoryFundCollectionApprovedError();
            } catch (RotatoryFundCollectionSpendDistributionPercentageSumExceedsOneHundredException e) {
                addRotatoryFundCollectionSpendDistributionPercentageSumExceedsOneHundredError();
            } catch (RotatoryFundApprovedException e) {
                addRotatoryFundApprovedError();
            } catch (RotatoryFundNullifiedException e) {
                addRotatoryFundAnnulledError();
            } catch (RotatoryFundLiquidatedException e) {
                addRotatoryFundLiquidatedError();
            } catch (RotatoryFundCollectionNotFoundException e) {
                addNotFoundMessage();
            } catch (RotatoryFundCollectionSpendDistributionAmountSumExceedsTotalException e) {
                addRotatoryFundCollectionSpendDistributionAmountSumExceedsTotalError();
            }
        }
    }

    @Override
    @Begin(nested = true, ifOutcome = Outcome.SUCCESS, flushMode = FlushModeType.MANUAL)
    @Restrict("#{s:hasPermission('ROTATORYFUNDCOLLECTIONSPENDDISTRIBUTION','VIEW')}")
    public String select(RotatoryFundCollectionSpendDistribution instance) {
        try {
            setOp(OP_UPDATE);
            /*refresh the instance from database*/
            setInstance(rotatoryFundCollectionSpendDistributionService.findRotatoryFundCollectionSpendDistribution(instance.getId()));
        } catch (EntryNotFoundException e) {
            addNotFoundMessage();
            return Outcome.FAIL;
        }
        return Outcome.SUCCESS;
    }

    @Override
    @End(beforeRedirect = true)
    @Restrict("#{s:hasPermission('ROTATORYFUNDCOLLECTIONSPENDDISTRIBUTION','UPDATE')}")
    public String update() {
        if (!validate()) {
            return Outcome.REDISPLAY;
        }
        try {
            rotatoryFundCollectionSpendDistributionService.updateRotatoryFundCollection(getInstance());
            addUpdatedMessage();
        } catch (EntryNotFoundException e) {
            addNotFoundMessage();
            return Outcome.FAIL;
        } catch (RotatoryFundCollectionNullifiedException e) {
            addRotatoryFundCollectionAnnulledError();
            return Outcome.REDISPLAY;
        } catch (RotatoryFundCollectionSpendDistributionPercentageSumExceedsOneHundredException e) {
            addRotatoryFundCollectionSpendDistributionPercentageSumExceedsOneHundredError();
            return Outcome.REDISPLAY;
        } catch (ConcurrencyException e) {
            concurrencyLog();
            try {
                rotatoryFundCollectionSpendDistributionService.findRotatoryFundCollectionSpendDistribution(getInstance().getId());
                addUpdateConcurrencyMessage();
                return Outcome.REDISPLAY;
            } catch (EntryNotFoundException e1) {
                addNotFoundMessage();
                return Outcome.FAIL;
            }
        } catch (RotatoryFundNullifiedException e) {
            addRotatoryFundAnnulledError();
            return Outcome.REDISPLAY;
        } catch (RotatoryFundLiquidatedException e) {
            addRotatoryFundLiquidatedError();
            return Outcome.REDISPLAY;
        } catch (RotatoryFundCollectionNotFoundException e) {
            addNotFoundMessage();
            return Outcome.REDISPLAY;
        } catch (RotatoryFundCollectionSpendDistributionAmountSumExceedsTotalException e) {
            addRotatoryFundCollectionSpendDistributionAmountSumExceedsTotalError();
            return Outcome.REDISPLAY;
        }
        return Outcome.SUCCESS;
    }

    @Override
    @End(beforeRedirect = true)
    @Restrict("#{s:hasPermission('ROTATORYFUNDCOLLECTIONSPENDDISTRIBUTION','DELETE')}")
    public String delete() {
        try {
            rotatoryFundCollectionSpendDistributionService.deleteRotatoryFundCollection(getInstance());
            addDeletedMessage();
        } catch (RotatoryFundCollectionApprovedException e) {
            addRotatoryFundCollectionApprovedError();
            return Outcome.REDISPLAY;
        } catch (ReferentialIntegrityException e) {
            addDeleteReferentialIntegrityMessage();
        } catch (EntryNotFoundException e) {
            addNotFoundMessage();
            return Outcome.FAIL;
        } catch (RotatoryFundNullifiedException e) {
            addRotatoryFundAnnulledError();
            return Outcome.REDISPLAY;
        } catch (RotatoryFundLiquidatedException e) {
            addRotatoryFundLiquidatedError();
            return Outcome.REDISPLAY;
        } catch (RotatoryFundCollectionNotFoundException e) {
            addNotFoundMessage();
            return Outcome.REDISPLAY;
        } catch (RotatoryFundApprovedException e) {
            addRotatoryFundApprovedError();
            return Outcome.REDISPLAY;
        } catch (RotatoryFundCollectionNullifiedException e) {
            addRotatoryFundCollectionAnnulledError();
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
        return getInstance().getAmount().toString();
    }

    @Override
    protected GenericService getService() {
        return rotatoryFundCollectionSpendDistributionService;
    }

    public boolean isEnabledOptions() {
        return true;
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

    public RotatoryFundCollection getRotatoryFundCollection() {
        return rotatoryFundCollectionAction.getInstance();
    }

    public RotatoryFund getRotatoryFund() {
        return rotatoryFundCollectionAction.getInstance().getRotatoryFund();
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

    public boolean isEnableCostCenter() {
        return !isManaged() || (isManaged() && rotatoryFundCollectionAction.isRotatoryFundCollectionPending());
    }

    public boolean isEnableCashAccount() {
        return !isManaged() || (isManaged() && rotatoryFundCollectionAction.isRotatoryFundCollectionPending());
    }
    /* Messages*/

    @Override
    protected void addCreatedMessage() {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.INFO,
                "RotatoryFundCollectionSpendDistribution.message.created");
    }

    @Override
    protected void addDeletedMessage() {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.INFO,
                "RotatoryFundCollectionSpendDistribution.message.deleted");
    }

    @Override
    protected void addUpdatedMessage() {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.INFO,
                "RotatoryFundCollectionSpendDistribution.message.updated");
    }

    private void addRotatoryFundCollectionAnnulledError() {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                "RotatoryFundCollectionSpendDistribution.error.rotatoryFundCollectionAlreadyAnnulled");
    }

    private void addRotatoryFundCollectionApprovedError() {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                "RotatoryFundCollectionSpendDistribution.error.rotatoryFundCollectionAlreadyApproved");
    }

    private void addRotatoryFundCollectionSpendDistributionPercentageSumExceedsOneHundredError() {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                "RotatoryFundCollectionSpendDistribution.error.spendDistributionPercentageSumExceedsOneHundred");
    }

    private void addRotatoryFundAnnulledError() {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                "SpendDistribution.error.rotatoryFundAlreadyAnnulled", getRotatoryFund().getCode());
    }

    private void addRotatoryFundApprovedError() {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                "SpendDistribution.error.rotatoryFundAlreadyApproved", getRotatoryFund().getCode());
    }

    private void addRotatoryFundLiquidatedError() {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                "SpendDistribution.error.rotatoryFundAlreadyLiquidated", getRotatoryFund().getCode());
    }

    private void addRotatoryFundCollectionSpendDistributionAmountSumExceedsTotalError() {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                "RotatoryFundCollectionSpendDistribution.error.rotatoryFundCollectionSpendDistributionAmountSumExceedsTotal", getRotatoryFundCollection().getSourceAmount());
    }

    private void addAccountValidError() {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                "RotatoryFundCollectionSpendDistribution.error.accountValid");
    }
}