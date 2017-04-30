package com.encens.khipus.action.fixedassets;

import com.encens.khipus.exception.ConcurrencyException;
import com.encens.khipus.exception.ReferentialIntegrityException;
import com.encens.khipus.exception.finances.FinancesCurrencyNotFoundException;
import com.encens.khipus.exception.finances.FinancesExchangeRateNotFoundException;
import com.encens.khipus.exception.purchase.*;
import com.encens.khipus.framework.action.GenericAction;
import com.encens.khipus.framework.action.Outcome;
import com.encens.khipus.framework.service.GenericService;
import com.encens.khipus.interceptor.BusinessUnitRestrict;
import com.encens.khipus.interceptor.BusinessUnitRestriction;
import com.encens.khipus.model.finances.FinancesCurrencyType;
import com.encens.khipus.model.fixedassets.FixedAssetGroup;
import com.encens.khipus.model.fixedassets.FixedAssetSubGroup;
import com.encens.khipus.model.fixedassets.PurchaseOrderDetailPart;
import com.encens.khipus.model.purchases.FixedAssetPurchaseOrderDetail;
import com.encens.khipus.model.purchases.PurchaseOrder;
import com.encens.khipus.model.purchases.PurchaseOrderState;
import com.encens.khipus.service.finances.FinancesExchangeRateService;
import com.encens.khipus.service.fixedassets.FixedAssetPurchaseOrderDetailService;
import com.encens.khipus.service.fixedassets.FixedAssetPurchaseOrderService;
import com.encens.khipus.util.BigDecimalUtil;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.*;
import org.jboss.seam.annotations.security.Restrict;
import org.jboss.seam.international.StatusMessage;

import java.math.BigDecimal;
import java.util.ArrayList;


/**
 * @author
 * @version 2.3
 */
@BusinessUnitRestrict
@Name("fixedAssetPurchaseOrderDetailAction")
@Scope(ScopeType.CONVERSATION)
public class FixedAssetPurchaseOrderDetailAction extends GenericAction<FixedAssetPurchaseOrderDetail> {
    private boolean showCalculatedValues = false;
    @In
    private FixedAssetPurchaseOrderService fixedAssetPurchaseOrderService;

    @In
    private FixedAssetPurchaseOrderDetailService fixedAssetPurchaseOrderDetailService;

    @In(value = "fixedAssetPurchaseOrderAction", required = false)
    private FixedAssetPurchaseOrderAction fixedAssetPurchaseOrderAction;

    @In
    private FinancesExchangeRateService financesExchangeRateService;

    @In(create = true)
    private PurchaseOrderDetailPartAction purchaseOrderDetailPartAction;

    private FixedAssetGroup fixedAssetGroup;

    @Factory(value = "fixedAssetPurchaseOrderDetail", scope = ScopeType.STATELESS)
    public FixedAssetPurchaseOrderDetail initFixedAssetPurchaseOrderDetail() {
        return getInstance();
    }

    @Factory(value = "paymentCurrencies", scope = ScopeType.STATELESS)
    public FinancesCurrencyType[] getFinancesCurrencyTypes() {
        return FinancesCurrencyType.values();
    }

    @BusinessUnitRestriction(value = "#{fixedAssetPurchaseOrderAction.instance}")
    @Begin(nested = true, ifOutcome = Outcome.SUCCESS)
    @Restrict("#{s:hasPermission('FIXEDASSETPURCHASEORDERADDDETAIL','VIEW')}")
    public String addFixedAssetPurchaseOrderDetail() {
        if (fixedAssetPurchaseOrderService.isPurchaseOrderApproved(getPurchaseOrder())) {
            /* in order to refresh the instance since the database*/
            fixedAssetPurchaseOrderService.findPurchaseOrder(getPurchaseOrder().getId());
            addFixedAssetPurchaseOrderApprovedError();
            return FixedAssetPurchaseOrderAction.APPROVED_OUTCOME;
        }

        if (fixedAssetPurchaseOrderService.isPurchaseOrderFinalized(getPurchaseOrder())) {
            /* in order to refresh the instance since the database*/
            fixedAssetPurchaseOrderService.findPurchaseOrder(getPurchaseOrder().getId());
            addFixedAssetPurchaseOrderFinalizedError();
            return FixedAssetPurchaseOrderAction.FINALIZED_OUTCOME;
        }

        if (fixedAssetPurchaseOrderService.isPurchaseOrderLiquidated(getPurchaseOrder())) {
            /* in order to refresh the instance since the database*/
            fixedAssetPurchaseOrderService.findPurchaseOrder(getPurchaseOrder().getId());
            addFixedAssetPurchaseOrderLiquidatedError();
            return FixedAssetPurchaseOrderAction.LIQUIDATED_OUTCOME;
        }

        /* to create the new FixedAssetPurchaseOrderDetail instance*/
        setInstance(null);
        setOp(OP_CREATE);
        getInstance().setPurchaseOrder(fixedAssetPurchaseOrderAction.getInstance());
        try {
            getInstance().setBsSusRate(financesExchangeRateService.findLastExchangeRateByCurrency(FinancesCurrencyType.D.name()));
            getInstance().setBsUfvRate(financesExchangeRateService.findLastExchangeRateByCurrency(FinancesCurrencyType.U.name()));
        } catch (FinancesCurrencyNotFoundException e) {
        } catch (FinancesExchangeRateNotFoundException e) {
        }
        return Outcome.SUCCESS;
    }

    @Override
    @BusinessUnitRestriction(value = "#{fixedAssetPurchaseOrderAction.instance}")
    @End(beforeRedirect = true)
    public String create() {
        if (checkComputeApplied()) {
            addUfvSusComputeRequiredMessage();
            return Outcome.REDISPLAY;
        }
        if (!checkCalculatedValues()) {
            addReComputeRequiredMessage();
            return Outcome.REDISPLAY;
        }
        /* rubbish can't be greater than ufv original value*/
        if (getInstance().getRubbish().compareTo(getInstance().getUfvUnitPriceValue()) > 0) {
            addRubbishGreaterThanUfvUnitPriceValueMessage();
            return Outcome.REDISPLAY;
        }

        String partValidationOutcome = purchaseOrderDetailPartAction.validateUnitPrices();
        if (!Outcome.SUCCESS.equals(partValidationOutcome)) {
            return Outcome.REDISPLAY;
        }

        if (!Outcome.SUCCESS.equals(validateSubGroupPartsRequired())) {
            return Outcome.REDISPLAY;
        }

        try {
            fixedAssetPurchaseOrderDetailService.createFixedAssetPurchaseOrderDetail(getInstance(),
                    fixedAssetPurchaseOrderAction.getInstance(),
                    purchaseOrderDetailPartAction.getInstances());

            addCreatedMessage();
            return Outcome.SUCCESS;
        } catch (PurchaseOrderApprovedException e) {
            fixedAssetPurchaseOrderService.findPurchaseOrder(getPurchaseOrder().getId());
            addFixedAssetPurchaseOrderApprovedError();
            return FixedAssetPurchaseOrderAction.APPROVED_OUTCOME;
        } catch (PurchaseOrderFinalizedException e) {
            fixedAssetPurchaseOrderService.findPurchaseOrder(getPurchaseOrder().getId());
            addFixedAssetPurchaseOrderFinalizedError();
            return FixedAssetPurchaseOrderAction.FINALIZED_OUTCOME;
        } catch (PurchaseOrderNullifiedException e) {
            fixedAssetPurchaseOrderService.findPurchaseOrder(getPurchaseOrder().getId());
            fixedAssetPurchaseOrderAction.addPurchaseOrderAnnulledErrorMessage();
            return FixedAssetPurchaseOrderAction.FINALIZED_OUTCOME;
        } catch (ConcurrencyException e) {
            fixedAssetPurchaseOrderAction.updateCurrentInstance();
            addUpdateConcurrencyMessage();
            return Outcome.REDISPLAY;
        } catch (PurchaseOrderLiquidatedException e) {
            fixedAssetPurchaseOrderService.findPurchaseOrder(getPurchaseOrder().getId());
            fixedAssetPurchaseOrderAction.addPurchaseOrderLiquidatedErrorMessage();
            return FixedAssetPurchaseOrderAction.LIQUIDATED_OUTCOME;
        }

    }

    @Override
    @BusinessUnitRestriction(value = "#{fixedAssetPurchaseOrderAction.instance}")
    public void createAndNew() {
        if (checkComputeApplied()) {
            addUfvSusComputeRequiredMessage();
            return;
        }
        if (!checkCalculatedValues()) {
            addReComputeRequiredMessage();
            return;
        }
        if (getInstance().getRubbish().compareTo(getInstance().getUfvUnitPriceValue()) > 0) {
            addRubbishGreaterThanUfvUnitPriceValueMessage();
            return;
        }

        String partValidationOutcome = purchaseOrderDetailPartAction.validateUnitPrices();
        if (!Outcome.SUCCESS.equals(partValidationOutcome)) {
            return;
        }

        if (!Outcome.SUCCESS.equals(validateSubGroupPartsRequired())) {
            return;
        }

        try {
            fixedAssetPurchaseOrderDetailService.createFixedAssetPurchaseOrderDetail(getInstance(),
                    fixedAssetPurchaseOrderAction.getInstance(),
                    purchaseOrderDetailPartAction.getInstances());
            addCreatedMessage();
            createInstance();
            getInstance().setPurchaseOrder(fixedAssetPurchaseOrderAction.getInstance());
            try {
                getInstance().setBsSusRate(financesExchangeRateService.findLastExchangeRateByCurrency(FinancesCurrencyType.D.name()));
                getInstance().setBsUfvRate(financesExchangeRateService.findLastExchangeRateByCurrency(FinancesCurrencyType.U.name()));
            } catch (FinancesCurrencyNotFoundException e) {
            } catch (FinancesExchangeRateNotFoundException e) {
            }
            setFixedAssetGroup(null);
            purchaseOrderDetailPartAction.setInstances(new ArrayList<PurchaseOrderDetailPart>());
        } catch (PurchaseOrderApprovedException e) {
            fixedAssetPurchaseOrderService.findPurchaseOrder(getPurchaseOrder().getId());
            addFixedAssetPurchaseOrderApprovedError();
        } catch (PurchaseOrderFinalizedException e) {
            fixedAssetPurchaseOrderService.findPurchaseOrder(getPurchaseOrder().getId());
            addFixedAssetPurchaseOrderFinalizedError();
        } catch (PurchaseOrderNullifiedException e) {
            fixedAssetPurchaseOrderService.findPurchaseOrder(getPurchaseOrder().getId());
            fixedAssetPurchaseOrderAction.addPurchaseOrderAnnulledErrorMessage();
        } catch (ConcurrencyException e) {
            fixedAssetPurchaseOrderAction.updateCurrentInstance();
            addUpdateConcurrencyMessage();
        } catch (PurchaseOrderLiquidatedException e) {
            fixedAssetPurchaseOrderService.findPurchaseOrder(getPurchaseOrder().getId());
            fixedAssetPurchaseOrderAction.addPurchaseOrderLiquidatedErrorMessage();
        }
    }

    @Override
    @BusinessUnitRestriction(value = "#{fixedAssetPurchaseOrderAction.instance}")
    @End(beforeRedirect = true)
    public String cancel() {
        return super.cancel();
    }

    @Override
    @BusinessUnitRestriction(value = "#{fixedAssetPurchaseOrderAction.instance}")
    @Begin(nested = true, ifOutcome = Outcome.SUCCESS, flushMode = FlushModeType.MANUAL)
    @Restrict("#{s:hasPermission('FIXEDASSETPURCHASEORDERADDDETAIL','VIEW')}")
    public String select(FixedAssetPurchaseOrderDetail instance) {
        showCalculatedValues = true;
        try {
            setOp(OP_UPDATE);

            /*refresh the instance from database*/
            setInstance(fixedAssetPurchaseOrderDetailService.findFixedAssetPurchaseOrderDetail(instance.getId()));
            purchaseOrderDetailPartAction.readInstances();
        } catch (PurchaseOrderDetailNotFoundException e) {
            addNotFoundMessage();

            return Outcome.FAIL;
        }
        return Outcome.SUCCESS;
    }

    @Override
    @BusinessUnitRestriction(value = "#{fixedAssetPurchaseOrderAction.instance}")
    @End(beforeRedirect = true)
    public String update() {
        if (!checkCalculatedValues()) {
            addReComputeRequiredMessage();
            return Outcome.REDISPLAY;
        }
        /* rubbish can't be greater than ufv original value*/
        if (getInstance().getRubbish().compareTo(getInstance().getUfvUnitPriceValue()) > 0) {
            addRubbishGreaterThanUfvUnitPriceValueMessage();
            return Outcome.REDISPLAY;
        }

        String partValidationOutcome = purchaseOrderDetailPartAction.validateUnitPrices();
        if (!Outcome.SUCCESS.equals(partValidationOutcome)) {
            return Outcome.REDISPLAY;
        }

        try {
            fixedAssetPurchaseOrderDetailService.updatePurchaseOrder(getInstance(),
                    purchaseOrderDetailPartAction.getInstances());
            addUpdatedMessage();
        } catch (PurchaseOrderDetailNotFoundException e) {
            addNotFoundMessage();
            return Outcome.FAIL;
        } catch (PurchaseOrderFinalizedException e) {
            fixedAssetPurchaseOrderService.findPurchaseOrder(getPurchaseOrder().getId());
            addFixedAssetPurchaseOrderFinalizedError();
            return FixedAssetPurchaseOrderAction.FINALIZED_OUTCOME;
        } catch (ConcurrencyException e) {
            concurrencyLog();
            try {
                fixedAssetPurchaseOrderDetailService.findFixedAssetPurchaseOrderDetail(getInstance().getId());
                purchaseOrderDetailPartAction.readInstances();
                addUpdateConcurrencyMessage();
                return Outcome.REDISPLAY;
            } catch (PurchaseOrderDetailNotFoundException e1) {
                addNotFoundMessage();
                return Outcome.FAIL;
            }
        } catch (PurchaseOrderNullifiedException e) {
            fixedAssetPurchaseOrderService.findPurchaseOrder(getPurchaseOrder().getId());
            fixedAssetPurchaseOrderAction.addPurchaseOrderAnnulledErrorMessage();
            return FixedAssetPurchaseOrderAction.FINALIZED_OUTCOME;
        } catch (PurchaseOrderApprovedException e) {
            fixedAssetPurchaseOrderService.findPurchaseOrder(getPurchaseOrder().getId());
            addFixedAssetPurchaseOrderApprovedError();
            return FixedAssetPurchaseOrderAction.APPROVED_OUTCOME;
        } catch (PurchaseOrderLiquidatedException e) {
            fixedAssetPurchaseOrderService.findPurchaseOrder(getPurchaseOrder().getId());
            fixedAssetPurchaseOrderAction.addPurchaseOrderLiquidatedErrorMessage();
            return FixedAssetPurchaseOrderAction.LIQUIDATED_OUTCOME;
        }

        return Outcome.SUCCESS;
    }

    @Override
    @BusinessUnitRestriction(value = "#{fixedAssetPurchaseOrderAction.instance}")
    @End(beforeRedirect = true)
    public String delete() {
        try {
            fixedAssetPurchaseOrderDetailService.deletePurchaseOrder(getInstance());
            addDeletedMessage();
        } catch (PurchaseOrderFinalizedException e) {
            fixedAssetPurchaseOrderService.findPurchaseOrder(getPurchaseOrder().getId());
            addFixedAssetPurchaseOrderFinalizedError();
            return FixedAssetPurchaseOrderAction.FINALIZED_OUTCOME;
        } catch (PurchaseOrderApprovedException e) {
            fixedAssetPurchaseOrderService.findPurchaseOrder(getPurchaseOrder().getId());
            addFixedAssetPurchaseOrderApprovedError();
            return FixedAssetPurchaseOrderAction.APPROVED_OUTCOME;
        } catch (ReferentialIntegrityException e) {
            addDeleteReferentialIntegrityMessage();
        } catch (PurchaseOrderDetailNotFoundException e) {
            addNotFoundMessage();
            return Outcome.FAIL;
        } catch (PurchaseOrderNullifiedException e) {
            fixedAssetPurchaseOrderService.findPurchaseOrder(getPurchaseOrder().getId());
            fixedAssetPurchaseOrderAction.addPurchaseOrderAnnulledErrorMessage();
            return FixedAssetPurchaseOrderAction.FINALIZED_OUTCOME;
        } catch (ConcurrencyException e) {
            fixedAssetPurchaseOrderAction.updateCurrentInstance();
            addUpdateConcurrencyMessage();
            return Outcome.REDISPLAY;
        } catch (PurchaseOrderLiquidatedException e) {
            fixedAssetPurchaseOrderService.findPurchaseOrder(getPurchaseOrder().getId());
            fixedAssetPurchaseOrderAction.addPurchaseOrderLiquidatedErrorMessage();
            return FixedAssetPurchaseOrderAction.LIQUIDATED_OUTCOME;
        }

        return Outcome.SUCCESS;
    }

    @Override
    protected String getDisplayNameMessage() {
        return getInstance().getDetail();
    }

    @Override
    protected GenericService getService() {
        return fixedAssetPurchaseOrderDetailService;
    }

    public boolean isShowCalculatedValues() {
        return showCalculatedValues;
    }

    public void putFixedAssetGroup(FixedAssetGroup fixedAssetGroup) {
        this.fixedAssetGroup = fixedAssetGroup;
        cleanFixedAssetSubGroup();
    }

    public void cleanFixedAssetGroup() {
        this.fixedAssetGroup = null;
        cleanFixedAssetSubGroup();
    }

    public void putFixedAssetSubGroup(FixedAssetSubGroup fixedAssetSubGroup) {
        getInstance().setFixedAssetSubGroup(fixedAssetSubGroup);
        loadSubGroupInfo();
    }

    public void cleanFixedAssetSubGroup() {
        getInstance().setFixedAssetSubGroup(null);
    }


    public void setShowCalculatedValues(boolean showCalculatedValues) {
        this.showCalculatedValues = showCalculatedValues;
    }

    private PurchaseOrder getPurchaseOrder() {
        return fixedAssetPurchaseOrderAction.getInstance();
    }

    public FixedAssetGroup getFixedAssetGroup() {
        return fixedAssetGroup;
    }

    public void setFixedAssetGroup(FixedAssetGroup fixedAssetGroup) {
        this.fixedAssetGroup = fixedAssetGroup;
    }

    public boolean isPurchaseOrderFinalized() {
        return getInstance().getPurchaseOrder() != null && getInstance().getPurchaseOrder().getState() != null && (getInstance().getPurchaseOrder().getState().equals(PurchaseOrderState.FIN));
    }

    /* based on total amount, calculate unit prices*/

    public String bsToUfvSus() {
        BigDecimal total = BigDecimalUtil.toBigDecimal(getInstance().getRequestedQuantity());

        /* Unit amounts*/
        getInstance().setUfvUnitPriceValue(
                BigDecimalUtil.divide(getInstance().getBsUnitPriceValue(), getInstance().getBsUfvRate(), 6
                ));
        getInstance().setSusUnitPriceValue(
                BigDecimalUtil.divide(getInstance().getBsUnitPriceValue(), getInstance().getBsSusRate(), 6
                ));
        /* Total amounts*/
        getInstance().setBsTotalAmount(
                BigDecimalUtil.multiply(getInstance().getBsUnitPriceValue(), total)
        );
        getInstance().setUfvTotalAmount(
                BigDecimalUtil.multiply(getInstance().getUfvUnitPriceValue(), total)
        );
        getInstance().setSusTotalAmount(
                BigDecimalUtil.multiply(getInstance().getSusUnitPriceValue(), total)
        );
        return Outcome.REDISPLAY;
    }

    private void showErrorMessagesForCompute() {
        if (getInstance().getRequestedQuantity() != null && getInstance().getRequestedQuantity() <= 0) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR, "FixedAssetPurchaseOrderDetail.error.requestedQuantityLessOrEqualThanZero");
        }
    }

    public boolean areFieldsValidForCompute() {
        return ((getInstance().getRequestedQuantity() != null && getInstance().getRequestedQuantity() > 0));
    }

    /* computes the corresponding values from bs to ufv and Sus values*/

    public void compute() {
        if (areFieldsValidForCompute()) {
            showCalculatedValues = true;
            bsToUfvSus();
        } else {
            showCalculatedValues = false;
            showErrorMessagesForCompute();
        }
    }

    /*
    checkCalculatedValues Validates calculated values
    @return true if all the calculated values can be reproduced at the moment of save operation
    */

    public boolean checkCalculatedValues() {
        BigDecimal total = BigDecimalUtil.toBigDecimal(getInstance().getRequestedQuantity());

        /* If unit prices are different recompute is necessary*/
        return (checkBsTotalAmountCalculatedValue(total) &&
                checkUfvTotalAmountCalculatedValue(total) &&
                checkSusTotalAmountCalculatedValue(total));
    }

    private boolean checkSusTotalAmountCalculatedValue(BigDecimal total) {
        return BigDecimalUtil.isPositive(getInstance().getSusTotalAmount()) ?
                (getInstance().getSusTotalAmount().compareTo(
                        BigDecimalUtil.multiply(
                                BigDecimalUtil.divide(getInstance().getBsUnitPriceValue(), getInstance().getBsSusRate(), 6)
                                , total)) == 0) : false;

    }

    private boolean checkUfvTotalAmountCalculatedValue(BigDecimal total) {
        return BigDecimalUtil.isPositive(getInstance().getUfvTotalAmount()) ?
                (getInstance().getUfvTotalAmount().compareTo(
                        BigDecimalUtil.multiply(
                                BigDecimalUtil.divide(getInstance().getBsUnitPriceValue(), getInstance().getBsUfvRate(), 6)
                                , total)) == 0) : false;

    }

    private boolean checkBsTotalAmountCalculatedValue(BigDecimal total) {
        return BigDecimalUtil.isPositive(getInstance().getBsTotalAmount()) ?
                (getInstance().getBsTotalAmount().compareTo(
                        BigDecimalUtil.multiply(
                                getInstance().getBsUnitPriceValue()
                                , total)) == 0) : false;
    }

    private boolean checkComputeApplied() {
        return getInstance().getSusTotalAmount() == null ||
                getInstance().getUfvTotalAmount() == null ||
                getInstance().getSusUnitPriceValue() == null ||
                getInstance().getUfvUnitPriceValue() == null;
    }

    public void loadSubGroupInfo() {
        /*update subGroup info to the fixedAssetPurchaseOrderDetail in case there isn't input values*/
        getInstance().setRubbish(getInstance().getFixedAssetSubGroup().getRubbish());
        getInstance().setDetail(getInstance().getFixedAssetSubGroup().getDetail());
        getInstance().setTotalDuration(getInstance().getFixedAssetSubGroup().getDuration());
        getInstance().setUsageDuration(0);
        getInstance().setNetDuration(getInstance().getFixedAssetSubGroup().getDuration());
    }


    public void updateTotalDuration() {
        if (getInstance().getTotalDuration() != null) {
            if (getInstance().getUsageDuration() != null) {
                getInstance().setNetDuration(getInstance().getTotalDuration() - getInstance().getUsageDuration());
            } else if (getInstance().getNetDuration() != null) {
                getInstance().setUsageDuration(getInstance().getTotalDuration() - getInstance().getNetDuration());
            }
        }
    }

    public void updateUsageDuration() {
        if (getInstance().getUsageDuration() != null) {
            if (getInstance().getTotalDuration() != null) {
                getInstance().setNetDuration(getInstance().getTotalDuration() - getInstance().getUsageDuration());
            } else if (getInstance().getNetDuration() != null) {
                getInstance().setTotalDuration(getInstance().getNetDuration() + getInstance().getUsageDuration());
            }
        }
    }

    public void updateNetDuration() {
        if (getInstance().getNetDuration() != null) {
            if (getInstance().getTotalDuration() != null) {
                getInstance().setUsageDuration(getInstance().getTotalDuration() - getInstance().getNetDuration());
            } else if (getInstance().getUsageDuration() != null) {
                getInstance().setTotalDuration(getInstance().getNetDuration() + getInstance().getUsageDuration());
            }
        }
    }


    private void addReComputeRequiredMessage() {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR, "FixedAssetPurchaseOrderDetail.error.reComputeRequired");
    }

    private void addUfvSusComputeRequiredMessage() {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR, "FixedAssetPurchaseOrderDetail.error.UfvSusComputeRequired");
    }

    private void addFixedAssetPurchaseOrderApprovedError() {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                "PurchaseOrderDetail.error.purchaseOrderAlreadyApproved",
                getPurchaseOrder().getOrderNumber());
    }

    private void addFixedAssetPurchaseOrderFinalizedError() {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                "PurchaseOrderDetail.error.purchaseOrderAlreadyFinalized",
                fixedAssetPurchaseOrderAction.getInstance().getOrderNumber());
    }

    private void addFixedAssetPurchaseOrderLiquidatedError() {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                "PurchaseOrderDetail.error.purchaseOrderAlreadyLiquidated",
                fixedAssetPurchaseOrderAction.getInstance().getOrderNumber());
    }

    private void addRubbishGreaterThanUfvUnitPriceValueMessage() {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR, "FixedAsset.error.RubbishGreaterThanUfvUnitPriceValue");
    }

    private String validateSubGroupPartsRequired() {
        String validationOutcome = Outcome.SUCCESS;
        if (null != getInstance().getFixedAssetSubGroup()
                && getInstance().getFixedAssetSubGroup().getRequireParts()
                && purchaseOrderDetailPartAction.getInstances().isEmpty()) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                    "FixedAssetPurchaseOrderDetail.error.requireParts");

            validationOutcome = Outcome.REDISPLAY;
        }
        return validationOutcome;
    }
}
