package com.encens.khipus.action.fixedassets;

import com.encens.khipus.exception.ConcurrencyException;
import com.encens.khipus.exception.EntryDuplicatedException;
import com.encens.khipus.exception.ReferentialIntegrityException;
import com.encens.khipus.exception.fixedassets.PurchaseOrderFixedAssetPartNotFoundException;
import com.encens.khipus.exception.purchase.PurchaseOrderApprovedException;
import com.encens.khipus.exception.purchase.PurchaseOrderFinalizedException;
import com.encens.khipus.exception.purchase.PurchaseOrderLiquidatedException;
import com.encens.khipus.exception.purchase.PurchaseOrderNullifiedException;
import com.encens.khipus.framework.action.GenericAction;
import com.encens.khipus.framework.action.Outcome;
import com.encens.khipus.framework.service.GenericService;
import com.encens.khipus.interceptor.BusinessUnitRestrict;
import com.encens.khipus.interceptor.BusinessUnitRestriction;
import com.encens.khipus.model.fixedassets.FixedAsset;
import com.encens.khipus.model.fixedassets.PurchaseOrderFixedAssetPart;
import com.encens.khipus.model.purchases.PurchaseOrder;
import com.encens.khipus.model.purchases.PurchaseOrderState;
import com.encens.khipus.service.fixedassets.FixedAssetPurchaseOrderService;
import com.encens.khipus.service.fixedassets.PurchaseOrderFixedAssetPartService;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.*;
import org.jboss.seam.annotations.security.Restrict;
import org.jboss.seam.international.StatusMessage;

/**
 * @author
 * @version 3.3
 */
@BusinessUnitRestrict
@Name("purchaseOrderFixedAssetPartAction")
@Scope(ScopeType.CONVERSATION)
public class PurchaseOrderFixedAssetPartAction extends GenericAction<PurchaseOrderFixedAssetPart> {

    @In
    private FixedAssetPurchaseOrderService fixedAssetPurchaseOrderService;

    @In
    private PurchaseOrderFixedAssetPartService purchaseOrderFixedAssetPartService;

    @In(value = "fixedAssetPurchaseOrderAction", required = false)
    private FixedAssetPurchaseOrderAction fixedAssetPurchaseOrderAction;

    @Factory(value = "purchaseOrderFixedAssetPart", scope = ScopeType.STATELESS)
    public PurchaseOrderFixedAssetPart initPurchaseOrderFixedAssetPart() {
        return getInstance();
    }

    @BusinessUnitRestriction(value = "#{fixedAssetPurchaseOrderAction.instance}")
    @Begin(nested = true, ifOutcome = Outcome.SUCCESS)
    @Restrict("#{s:hasPermission('FIXEDASSETPURCHASEORDERADDDETAIL','VIEW')}")
    public String addPurchaseOrderFixedAssetPart() {
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

        /* to create the new PurchaseOrderFixedAssetPart instance*/
        setInstance(null);
        setOp(OP_CREATE);
        getInstance().setPurchaseOrder(fixedAssetPurchaseOrderAction.getInstance());
        return Outcome.SUCCESS;
    }

    @Override
    @BusinessUnitRestriction(value = "#{fixedAssetPurchaseOrderAction.instance}")
    @End(beforeRedirect = true)
    public String create() {
        try {
            purchaseOrderFixedAssetPartService.createPurchaseOrderFixedAssetPart(getInstance(),
                    fixedAssetPurchaseOrderAction.getInstance());
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
        try {
            purchaseOrderFixedAssetPartService.createPurchaseOrderFixedAssetPart(getInstance(),
                    fixedAssetPurchaseOrderAction.getInstance());
            addCreatedMessage();
            createInstance();
            getInstance().setPurchaseOrder(fixedAssetPurchaseOrderAction.getInstance());
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

    @BusinessUnitRestriction(value = "#{fixedAssetPurchaseOrderAction.instance}")
    @Begin(nested = true, ifOutcome = Outcome.SUCCESS, flushMode = FlushModeType.MANUAL)
    @Restrict("#{s:hasPermission('FIXEDASSETPURCHASEORDERADDDETAIL','VIEW')}")
    public String select(PurchaseOrderFixedAssetPart instance) {
        try {
            setOp(OP_UPDATE);
            setInstance(purchaseOrderFixedAssetPartService.findById(instance.getId()));
        } catch (PurchaseOrderFixedAssetPartNotFoundException e) {
            addNotFoundMessage();
            return Outcome.FAIL;
        }
        return Outcome.SUCCESS;
    }

    @Override
    @BusinessUnitRestriction(value = "#{fixedAssetPurchaseOrderAction.instance}")
    @End(beforeRedirect = true)
    public String update() {
        try {
            purchaseOrderFixedAssetPartService.updatePurchaseOrderFixedAssetPart(getInstance());
            addUpdatedMessage();
        } catch (PurchaseOrderFinalizedException e) {
            fixedAssetPurchaseOrderService.findPurchaseOrder(getPurchaseOrder().getId());
            addFixedAssetPurchaseOrderFinalizedError();
            return FixedAssetPurchaseOrderAction.FINALIZED_OUTCOME;
        } catch (ConcurrencyException e) {
            concurrencyLog();
            try {
                purchaseOrderFixedAssetPartService.findById(getInstance().getId());
                addUpdateConcurrencyMessage();
                return Outcome.REDISPLAY;
            } catch (PurchaseOrderFixedAssetPartNotFoundException e1) {
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
        } catch (EntryDuplicatedException e) {
            addDuplicatedMessage();
            return Outcome.FAIL;
        } catch (PurchaseOrderFixedAssetPartNotFoundException e) {
            addNotFoundMessage();
            return Outcome.FAIL;
        }
        return Outcome.SUCCESS;
    }

    @Override
    @BusinessUnitRestriction(value = "#{fixedAssetPurchaseOrderAction.instance}")
    @End(beforeRedirect = true)
    public String delete() {
        try {
            purchaseOrderFixedAssetPartService.deletePurchaseOrderFixedAssetPart(getInstance());
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
        } catch (PurchaseOrderFixedAssetPartNotFoundException e) {
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
        return getInstance().getDescription();
    }

    @Override
    protected GenericService getService() {
        return purchaseOrderFixedAssetPartService;
    }

    public void putFixedAsset(FixedAsset fixedAsset) {
        getInstance().setFixedAsset(fixedAsset);
    }

    public void cleanFixedAsset() {
        getInstance().setFixedAsset(null);
    }

    private PurchaseOrder getPurchaseOrder() {
        return fixedAssetPurchaseOrderAction.getInstance();
    }

    public boolean isPurchaseOrderFinalized() {
        return getInstance().getPurchaseOrder() != null && getInstance().getPurchaseOrder().getState() != null && (getInstance().getPurchaseOrder().getState().equals(PurchaseOrderState.FIN));
    }

    private void addFixedAssetPurchaseOrderApprovedError() {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                "PurchaseOrderFixedAssetPart.error.purchaseOrderAlreadyApproved",
                getPurchaseOrder().getOrderNumber());
    }

    private void addFixedAssetPurchaseOrderFinalizedError() {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                "PurchaseOrderFixedAssetPart.error.purchaseOrderAlreadyFinalized",
                fixedAssetPurchaseOrderAction.getInstance().getOrderNumber());
    }

    private void addFixedAssetPurchaseOrderLiquidatedError() {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                "PurchaseOrderFixedAssetPart.error.purchaseOrderAlreadyLiquidated",
                fixedAssetPurchaseOrderAction.getInstance().getOrderNumber());
    }
}
