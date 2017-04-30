package com.encens.khipus.action.finances;

import com.encens.khipus.exception.ConcurrencyException;
import com.encens.khipus.exception.ReferentialIntegrityException;
import com.encens.khipus.exception.finances.*;
import com.encens.khipus.framework.action.GenericAction;
import com.encens.khipus.framework.action.Outcome;
import com.encens.khipus.framework.service.GenericService;
import com.encens.khipus.model.finances.Quota;
import com.encens.khipus.model.finances.QuotaState;
import com.encens.khipus.model.finances.RotatoryFund;
import com.encens.khipus.model.finances.RotatoryFundState;
import com.encens.khipus.service.finances.QuotaService;
import com.encens.khipus.service.finances.RotatoryFundService;
import com.encens.khipus.util.BigDecimalUtil;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.*;
import org.jboss.seam.annotations.security.Restrict;
import org.jboss.seam.international.StatusMessage;


/**
 * @author
 * @version 2.26
 */
@Name("quotaAction")
@Scope(ScopeType.CONVERSATION)
public class QuotaAction extends GenericAction<Quota> {
    @In
    private RotatoryFundService rotatoryFundService;

    @In
    private QuotaService quotaService;

    @In(value = "rotatoryFundAction")
    private RotatoryFundAction rotatoryFundAction;

    @Create
    public void atCreateTime() {
        getInstance().setState(QuotaState.PEN);
    }

    @Factory(value = "quota", scope = ScopeType.STATELESS)
    public Quota initQuota() {
        return getInstance();
    }

    @Begin(nested = true, ifOutcome = Outcome.SUCCESS)
    public String addQuota() {
        if (rotatoryFundService.isRotatoryFundNullified(getRotatoryFund())) {
            /* in order to refresh the instance since the database*/
            rotatoryFundService.findRotatoryFund(getRotatoryFund().getId());
            addRotatoryFundAnnulledError();
            return Outcome.REDISPLAY;
        }
        if (rotatoryFundService.isRotatoryFundApproved(getRotatoryFund())) {
            /* in order to refresh the instance since the database*/
            rotatoryFundService.findRotatoryFund(getRotatoryFund().getId());
            addRotatoryFundApprovedError();
            return Outcome.REDISPLAY;
        }
        if (rotatoryFundService.isRotatoryFundLiquidated(getRotatoryFund())) {
            /* in order to refresh the instance since the database*/
            rotatoryFundService.findRotatoryFund(getRotatoryFund().getId());
            addRotatoryFundLiquidatedError();
            return Outcome.REDISPLAY;
        }
        /*if the quotas already cover the total*/
        if (quotaService.allValidQuotaSum(getRotatoryFund()).longValue() >= getRotatoryFund().getAmount().longValue()) {
            addRotatoryFundAlreadyCoveredError();
            return Outcome.REDISPLAY;
        }
        /* to create the new Quota instance*/
        setInstance(null);
        setOp(OP_CREATE);
        getInstance().setRotatoryFund(rotatoryFundAction.getInstance());
        getInstance().setCurrency(getInstance().getRotatoryFund().getPayCurrency());
        getInstance().setExchangeRate(getInstance().getRotatoryFund().getExchangeRate());
        getInstance().setAmount(BigDecimalUtil.subtract(getRotatoryFund().getAmount(), quotaService.allValidQuotaSum(getRotatoryFund())));
        return Outcome.SUCCESS;
    }

    @Override
    @End(beforeRedirect = true)
    @Restrict("#{s:hasPermission('QUOTA','CREATE')}")
    public String create() {
        try {
            quotaService.createQuota(getInstance());
            addCreatedMessage();
            super.select(getInstance());
        } catch (RotatoryFundApprovedException e) {
            addRotatoryFundApprovedError();
            return Outcome.REDISPLAY;
        } catch (RotatoryFundLiquidatedException e) {
            addRotatoryFundLiquidatedError();
            return Outcome.REDISPLAY;
        } catch (QuotaSumExceedsRotatoryFundAmountException e) {
            addQuotaSumExceedsRotatoryFundAmountError();
            return Outcome.REDISPLAY;
        } catch (ExpirationDateBeforeStartDateException e) {
            addExpirationDateBeforeStartDateError();
            return Outcome.REDISPLAY;
        } catch (RotatoryFundNullifiedException e) {
            addRotatoryFundAnnulledError();
            return Outcome.REDISPLAY;
        }
        return Outcome.SUCCESS;
    }

    @Override
    @Begin(nested = true, ifOutcome = Outcome.SUCCESS, flushMode = FlushModeType.MANUAL)
    @Restrict("#{s:hasPermission('QUOTA','VIEW')}")
    public String select(Quota instance) {
        try {
            setOp(OP_UPDATE);
            /*refresh the instance from database*/
            setInstance(quotaService.findQuota(instance.getId()));
        } catch (QuotaNotFoundException e) {
            addNotFoundMessage();
            return Outcome.FAIL;
        }
        return Outcome.SUCCESS;
    }

    @Override
    @End(beforeRedirect = true)
    @Restrict("#{s:hasPermission('QUOTA','UPDATE')}")
    public String update() {
        try {
            quotaService.updateRotatoryFund(getInstance());
            addUpdatedMessage();
        } catch (QuotaNotFoundException e) {
            addNotFoundMessage();
            return Outcome.FAIL;
        } catch (RotatoryFundLiquidatedException e) {
            addRotatoryFundLiquidatedError();
            return Outcome.REDISPLAY;
        } catch (QuotaSumExceedsRotatoryFundAmountException e) {
            addQuotaSumExceedsRotatoryFundAmountError();
            return Outcome.REDISPLAY;
        } catch (ResidueCannotBeLessThanZeroException e) {
            addResidueCannotBeLessThanZeroError();
            return Outcome.REDISPLAY;
        } catch (ConcurrencyException e) {
            concurrencyLog();
            try {
                quotaService.findQuota(getInstance().getId());
                addUpdateConcurrencyMessage();
                return Outcome.REDISPLAY;
            } catch (QuotaNotFoundException e1) {
                addNotFoundMessage();
                return Outcome.FAIL;
            }
        }
        return Outcome.SUCCESS;
    }

    @Override
    @End(beforeRedirect = true)
    @Restrict("#{s:hasPermission('QUOTA','DELETE')}")
    public String delete() {
        try {
            quotaService.deleteRotatoryFund(getInstance());
            addDeletedMessage();
        } catch (RotatoryFundLiquidatedException e) {
            addRotatoryFundLiquidatedError();
            return Outcome.REDISPLAY;
        } catch (RotatoryFundApprovedException e) {
            addRotatoryFundApprovedError();
            return Outcome.REDISPLAY;
        } catch (ReferentialIntegrityException e) {
            addDeleteReferentialIntegrityMessage();
        } catch (QuotaNotFoundException e) {
            addNotFoundMessage();
            return Outcome.FAIL;
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
        return getInstance().getDescription();
    }

    @Override
    protected GenericService getService() {
        return quotaService;
    }

    /*used in view level*/

    public boolean isRotatoryFundLiquidated() {
        return getInstance().getRotatoryFund() != null && getInstance().getRotatoryFund().getState() != null && (getInstance().getRotatoryFund().getState().equals(RotatoryFundState.LIQ));
    }

    /*used in view level*/

    public boolean isEnableDiscountByPayrollField() {
        return useEnableDiscountByPayrollField();
    }

    private boolean useEnableDiscountByPayrollField() {
        return (getInstance().getRotatoryFund().getDiscountByPayroll() != null);
    }

    public RotatoryFund getRotatoryFund() {
        return rotatoryFundAction.getInstance();
    }

    public boolean isQuotaPending() {
        return !isManaged() || (null != getInstance().getState() && QuotaState.PEN.equals(getInstance().getState()));
    }

    private void addRotatoryFundAnnulledError() {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                "Quota.error.rotatoryFundAlreadyAnnulled", getRotatoryFund().getCode());
    }

    private void addRotatoryFundAlreadyCoveredError() {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                "Quota.error.rotatoryFundAlreadyCovered");
    }

    private void addRotatoryFundApprovedError() {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                "Quota.error.rotatoryFundAlreadyApproved", getRotatoryFund().getCode());
    }

    private void addRotatoryFundLiquidatedError() {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                "Quota.error.rotatoryFundAlreadyLiquidated", getRotatoryFund().getCode());
    }

    private void addQuotaSumExceedsRotatoryFundAmountError() {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                "Quota.error.quotaSumExceedsRotatoryFundAmount", getRotatoryFund().getAmount());
    }

    private void addResidueCannotBeLessThanZeroError() {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                "Quota.error.residueCannotBeLessThanZero", getRotatoryFund().getAmount());
    }

    private void addExpirationDateBeforeStartDateError() {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                "Quota.error.expirationDateBeforeStartDate", getRotatoryFund().getStartDate());
    }

    @Override
    protected void addCreatedMessage() {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.INFO,
                "Quota.message.created");
    }

    @Override
    protected void addDeletedMessage() {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.INFO,
                "Quota.message.deleted");
    }

    @Override
    protected void addUpdatedMessage() {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.INFO,
                "Quota.message.updated");
    }

}