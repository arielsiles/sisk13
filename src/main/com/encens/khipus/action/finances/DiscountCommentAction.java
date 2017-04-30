package com.encens.khipus.action.finances;

import com.encens.khipus.action.fixedassets.FixedAssetPurchaseOrderAction;
import com.encens.khipus.action.fixedassets.FixedAssetVoucherAction;
import com.encens.khipus.action.warehouse.WarehousePurchaseOrderAction;
import com.encens.khipus.exception.ConcurrencyException;
import com.encens.khipus.exception.EntryDuplicatedException;
import com.encens.khipus.exception.EntryNotFoundException;
import com.encens.khipus.exception.ReferentialIntegrityException;
import com.encens.khipus.exception.finances.DiscountCommentNotFoundException;
import com.encens.khipus.exception.finances.RotatoryFundNotFoudException;
import com.encens.khipus.exception.finances.RotatoryFundNullifiedException;
import com.encens.khipus.exception.fixedassets.FixedAssetPurchaseOrderAnnulledException;
import com.encens.khipus.exception.fixedassets.FixedAssetPurchaseOrderNotFoudException;
import com.encens.khipus.exception.fixedassets.FixedAssetVoucherAnnulledException;
import com.encens.khipus.exception.fixedassets.FixedAssetVoucherNotFoudException;
import com.encens.khipus.exception.purchase.PurchaseOrderNullifiedException;
import com.encens.khipus.exception.warehouse.WarehousePurchaseOrderNotFoundException;
import com.encens.khipus.framework.action.GenericAction;
import com.encens.khipus.framework.action.Outcome;
import com.encens.khipus.model.finances.DiscountComment;
import com.encens.khipus.model.finances.DiscountCommentType;
import com.encens.khipus.model.finances.RotatoryFund;
import com.encens.khipus.model.fixedassets.FixedAssetVoucher;
import com.encens.khipus.model.purchases.PurchaseOrder;
import com.encens.khipus.service.finances.DiscountCommentService;
import com.encens.khipus.service.finances.RotatoryFundService;
import com.encens.khipus.service.fixedassets.FixedAssetPurchaseOrderService;
import com.encens.khipus.service.fixedassets.FixedAssetVoucherService;
import com.encens.khipus.service.warehouse.WarehousePurchaseOrderService;
import com.encens.khipus.util.FormatUtils;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.*;
import org.jboss.seam.annotations.security.Restrict;
import org.jboss.seam.international.StatusMessage;

/**
 * @author
 * @version 3.0
 */
@Name("discountCommentAction")
@Scope(ScopeType.CONVERSATION)
public class DiscountCommentAction extends GenericAction<DiscountComment> {

    @In
    private FixedAssetVoucherService fixedAssetVoucherService;
    @In
    private RotatoryFundService rotatoryFundService;
    @In
    private WarehousePurchaseOrderService warehousePurchaseOrderService;
    @In
    private FixedAssetPurchaseOrderService fixedAssetPurchaseOrderService;
    @In
    private DiscountCommentService discountCommentService;

    @In(value = "fixedAssetVoucherAction", required = false)
    private FixedAssetVoucherAction fixedAssetVoucherAction;

    @In(value = "rotatoryFundAction", required = false)
    private RotatoryFundAction rotatoryFundAction;

    @In(value = "warehousePurchaseOrderAction", required = false)
    private WarehousePurchaseOrderAction warehousePurchaseOrderAction;

    @In(value = "fixedAssetPurchaseOrderAction", required = false)
    private FixedAssetPurchaseOrderAction fixedAssetPurchaseOrderAction;

    @Factory(value = "discountComment", scope = ScopeType.STATELESS)
    public DiscountComment initDiscountComment() {
        return getInstance();
    }

    @Begin(nested = true, ifOutcome = Outcome.SUCCESS, flushMode = FlushModeType.MANUAL)
    @Restrict("#{s:hasPermission('DISCOUNTCOMMENT','CREATE')}")
    public String addFixedAssetVoucherDiscountComment() {
        prepareNewInstance();
        getInstance().setFixedAssetVoucher(fixedAssetVoucherAction.getInstance());
        getInstance().setType(DiscountCommentType.FIXED_ASSET_VOUCHER);
        String rule = validateParentAndState();
        if (rule != null) {
            return rule;
        }
        return Outcome.SUCCESS;
    }

    @Begin(nested = true, ifOutcome = Outcome.SUCCESS, flushMode = FlushModeType.MANUAL)
    @Restrict("#{s:hasPermission('DISCOUNTCOMMENT','CREATE')}")
    public String addWarehousePurchaseOrderDiscountComment() {
        prepareNewInstance();
        getInstance().setPurchaseOrder(warehousePurchaseOrderAction.getInstance());
        getInstance().setType(DiscountCommentType.WAREHOUSE_PURCHASE_ORDER);
        String rule = validateParentAndState();
        if (rule != null) {
            return rule;
        }
        return Outcome.SUCCESS;
    }

    @Begin(nested = true, ifOutcome = Outcome.SUCCESS, flushMode = FlushModeType.MANUAL)
    @Restrict("#{s:hasPermission('DISCOUNTCOMMENT','CREATE')}")
    public String addFixedAssetPurchaseOrderDiscountComment() {
        prepareNewInstance();
        getInstance().setPurchaseOrder(fixedAssetPurchaseOrderAction.getInstance());
        getInstance().setType(DiscountCommentType.FIXED_ASSET_PURCHASE_ORDER);
        String rule = validateParentAndState();
        if (rule != null) {
            return rule;
        }
        return Outcome.SUCCESS;
    }

    @Begin(nested = true, ifOutcome = Outcome.SUCCESS, flushMode = FlushModeType.MANUAL)
    @Restrict("#{s:hasPermission('DISCOUNTCOMMENT','CREATE')}")
    public String addRotatoryFundDiscountComment() {
        prepareNewInstance();
        getInstance().setRotatoryFund(rotatoryFundAction.getInstance());
        getInstance().setType(DiscountCommentType.ROTATORY_FUND);
        String rule = validateParentAndState();
        if (rule != null) {
            return rule;
        }
        return Outcome.SUCCESS;
    }

    @Override
    @End(beforeRedirect = true)
    public String create() {
        try {
            validateParent();
        } catch (WarehousePurchaseOrderNotFoundException e) {
            warehousePurchaseOrderAction.addNotFoundMessage();
            return Outcome.FAIL;
        } catch (RotatoryFundNotFoudException e) {
            rotatoryFundAction.addNotFoundMessage();
            return Outcome.FAIL;
        } catch (FixedAssetVoucherNotFoudException e) {
            fixedAssetVoucherAction.addNotFoundMessage();
            return Outcome.FAIL;
        } catch (FixedAssetPurchaseOrderNotFoudException e) {
            fixedAssetPurchaseOrderAction.addNotFoundMessage();
            return Outcome.FAIL;
        }
        try {
            discountCommentService.createDiscountComment(getInstance());
            addCreatedMessage();
            return Outcome.SUCCESS;
        } catch (EntryDuplicatedException e) {
            addDuplicatedMessage();
            return Outcome.REDISPLAY;
        } catch (PurchaseOrderNullifiedException e) {
            warehousePurchaseOrderService.findPurchaseOrder(getInstance().getPurchaseOrder().getId());
            addWarehousePurchaseOrderNullifiedError();
            return Outcome.SUCCESS;
        } catch (FixedAssetVoucherAnnulledException e) {
            fixedAssetVoucherService.findFixedAssetVoucher(getInstance().getFixedAssetVoucher().getId());
            fixedAssetVoucherAction.addFixedAssetVoucherAnnulledErrorMessage();
            return Outcome.SUCCESS;
        } catch (RotatoryFundNullifiedException e) {
            rotatoryFundService.findRotatoryFund(getInstance().getRotatoryFund().getId());
            rotatoryFundAction.addRotatoryFundAnnulledErrorMessage();
            return Outcome.SUCCESS;
        }
    }

    @Override
    public void createAndNew() {
        try {
            discountCommentService.createDiscountComment(getInstance());
            addCreatedMessage();
            DiscountComment newDiscountComment = buildNewInstance(getInstance());
            prepareNewInstance();
            setInstance(newDiscountComment);
        } catch (EntryDuplicatedException e) {
            addDuplicatedMessage();
        } catch (PurchaseOrderNullifiedException e) {
            warehousePurchaseOrderService.findPurchaseOrder(getInstance().getPurchaseOrder().getId());
            addWarehousePurchaseOrderNullifiedError();
        } catch (FixedAssetVoucherAnnulledException e) {
            fixedAssetVoucherService.findFixedAssetVoucher(getInstance().getFixedAssetVoucher().getId());
            fixedAssetVoucherAction.addFixedAssetVoucherAnnulledErrorMessage();
        } catch (RotatoryFundNullifiedException e) {
            rotatoryFundService.findRotatoryFund(getInstance().getRotatoryFund().getId());
            rotatoryFundAction.addRotatoryFundAnnulledErrorMessage();
        }
    }

    /**
     * Prepare new instance with old instance default data
     *
     * @param discountComment old instance from which default data will be copy
     * @return a new DiscountComment instance with default data
     */
    private DiscountComment buildNewInstance(DiscountComment discountComment) {
        DiscountComment newDiscountComment = new DiscountComment();
        newDiscountComment.setType(discountComment.getType());
        if (null != discountComment.getFixedAssetVoucher()) {
            newDiscountComment.setFixedAssetVoucher(discountComment.getFixedAssetVoucher());
        }
        if (null != discountComment.getPurchaseOrder()) {
            newDiscountComment.setPurchaseOrder(discountComment.getPurchaseOrder());
        }
        if (null != discountComment.getRotatoryFund()) {
            newDiscountComment.setRotatoryFund(discountComment.getRotatoryFund());
        }
        return newDiscountComment;
    }

    @Override
    @End(beforeRedirect = true)
    public String cancel() {
        return super.cancel();
    }

    @Override
    @Begin(nested = true, flushMode = FlushModeType.MANUAL)
    public String select(DiscountComment discountComment) {
        try {
            setOp(OP_UPDATE);
            setInstance(genericService.findById(DiscountComment.class, discountComment.getId()));
            return Outcome.SUCCESS;
        } catch (EntryNotFoundException e) {
            addNotFoundMessage();
            return Outcome.REDISPLAY;
        }
    }

    @Override
    @End(beforeRedirect = true)
    public String update() {
        Long currentVersion = (Long) getVersion(getInstance());
        try {
            discountCommentService.updateDiscountComment(getInstance());
            addUpdatedMessage();
            return Outcome.SUCCESS;
        } catch (EntryDuplicatedException e) {
            addDuplicatedMessage();
            setVersion(getInstance(), currentVersion);
            return Outcome.REDISPLAY;
        } catch (ConcurrencyException e) {
            try {
                updateCurrentInstance();
                addUpdateConcurrencyMessage();
                return Outcome.REDISPLAY;
            } catch (DiscountCommentNotFoundException e1) {
                addNotFoundMessage();
                return Outcome.FAIL;
            }
        } catch (DiscountCommentNotFoundException e) {
            addNotFoundMessage();
            return Outcome.SUCCESS;
        } catch (PurchaseOrderNullifiedException e) {
            // refresh the instance
            warehousePurchaseOrderService.findPurchaseOrder(getInstance().getPurchaseOrder().getId());
            addPurchaseOrderNullifiedErrorMessage();
            return Outcome.REDISPLAY;
        } catch (FixedAssetVoucherAnnulledException e) {
            // refresh the instance
            fixedAssetVoucherService.findFixedAssetVoucher(getInstance().getFixedAssetVoucher().getId());
            fixedAssetVoucherAction.addFixedAssetVoucherAnnulledErrorMessage();
            return Outcome.REDISPLAY;
        } catch (RotatoryFundNullifiedException e) {
            // refresh the instance
            rotatoryFundService.findRotatoryFund(getInstance().getRotatoryFund().getId());
            rotatoryFundAction.addRotatoryFundAnnulledErrorMessage();
            return Outcome.REDISPLAY;
        }
    }

    public void updateCurrentInstance() throws DiscountCommentNotFoundException {
        setInstance(discountCommentService.findDiscountComment(getInstance().getId()));
    }

    private void addPurchaseOrderNullifiedErrorMessage() {
        if (getInstance().getType().equals(DiscountCommentType.WAREHOUSE_PURCHASE_ORDER)) {
            warehousePurchaseOrderAction.addPurchaseOrderNullifiedErrorMessage();
        } else {
            fixedAssetPurchaseOrderAction.addPurchaseOrderAnnulledErrorMessage();
        }
    }

    @Override
    @End(beforeRedirect = true)
    public String delete() {
        try {
            discountCommentService.deleteDiscountComment(getInstance());
            addDeletedMessage();
            return Outcome.SUCCESS;
        } catch (ReferentialIntegrityException e) {
            addDeleteReferentialIntegrityMessage();
            return Outcome.REDISPLAY;
        } catch (DiscountCommentNotFoundException e) {
            addNotFoundMessage();
            return Outcome.FAIL;
        } catch (ConcurrencyException e) {
            fixedAssetVoucherAction.updateCurrentInstance();
            addDeleteConcurrencyMessage();
            return Outcome.SUCCESS;
        } catch (PurchaseOrderNullifiedException e) {
            // refresh the instance
            warehousePurchaseOrderService.findPurchaseOrder(getInstance().getPurchaseOrder().getId());
            addWarehousePurchaseOrderNullifiedError();
            return Outcome.REDISPLAY;
        } catch (FixedAssetVoucherAnnulledException e) {
            // refresh the instance
            fixedAssetVoucherService.findFixedAssetVoucher(getInstance().getFixedAssetVoucher().getId());
            fixedAssetVoucherAction.addFixedAssetVoucherAnnulledErrorMessage();
            return Outcome.REDISPLAY;
        } catch (RotatoryFundNullifiedException e) {
            // refresh the instance
            rotatoryFundService.findRotatoryFund(getInstance().getRotatoryFund().getId());
            rotatoryFundAction.addRotatoryFundAnnulledErrorMessage();
            return Outcome.REDISPLAY;
        }
    }

    /**
     * Validates if the main instance still exist and if its state is not Annulled
     *
     * @throws FixedAssetVoucherNotFoudException
     *                                      in case FixedAssetVoucher not found
     * @throws com.encens.khipus.exception.warehouse.WarehousePurchaseOrderNotFoundException
     *                                      in case WarehousePurchaseOrder not found
     * @throws RotatoryFundNotFoudException in case RotatoryFund not found
     * @throws FixedAssetPurchaseOrderNotFoudException
     *                                      in case FixedAssetPurchaseOrder not found
     */
    private void validateParent() throws FixedAssetVoucherNotFoudException,
            WarehousePurchaseOrderNotFoundException, RotatoryFundNotFoudException,
            FixedAssetPurchaseOrderNotFoudException {
        switch (getInstance().getType()) {
            case FIXED_ASSET_VOUCHER:
                try {
                    getService().findById(FixedAssetVoucher.class, getInstance().getFixedAssetVoucher().getId());
                } catch (EntryNotFoundException e) {
                    throw new FixedAssetPurchaseOrderNotFoudException();
                }
                break;
            case FIXED_ASSET_PURCHASE_ORDER:
                try {
                    getService().findById(PurchaseOrder.class, getInstance().getPurchaseOrder().getId());
                } catch (EntryNotFoundException e) {
                    throw new FixedAssetPurchaseOrderNotFoudException();
                }
                break;
            case WAREHOUSE_PURCHASE_ORDER:
                try {
                    getService().findById(PurchaseOrder.class, getInstance().getPurchaseOrder().getId());
                } catch (EntryNotFoundException e) {
                    throw new WarehousePurchaseOrderNotFoundException();
                }
                break;
            case ROTATORY_FUND:
                try {
                    getService().findById(RotatoryFund.class, getInstance().getRotatoryFund().getId());
                } catch (EntryNotFoundException e) {
                    throw new RotatoryFundNotFoudException();
                }
                break;
        }
    }

    /**
     * Validates if the main instance still exist and if its state is not Annulled
     *
     * @return a String indicating the navigation rule and null if it is ok
     */
    private String validateParentAndState() {
        try {
            switch (getInstance().getType()) {
                case FIXED_ASSET_VOUCHER:
                    try {
                        getService().findById(FixedAssetVoucher.class, getInstance().getFixedAssetVoucher().getId());
                        if (fixedAssetVoucherService.isFixedAssetVoucherNullified(getInstance().getFixedAssetVoucher())) {
                            throw new FixedAssetVoucherAnnulledException();
                        }
                    } catch (EntryNotFoundException e) {
                        throw new FixedAssetVoucherNotFoudException();
                    }
                    break;
                case FIXED_ASSET_PURCHASE_ORDER:
                    try {
                        getService().findById(PurchaseOrder.class, getInstance().getPurchaseOrder().getId());
                        if (fixedAssetPurchaseOrderService.isPurchaseOrderNullified(getInstance().getPurchaseOrder())) {
                            throw new FixedAssetPurchaseOrderAnnulledException();
                        }
                    } catch (EntryNotFoundException e) {
                        throw new FixedAssetPurchaseOrderNotFoudException();
                    }
                    break;
                case WAREHOUSE_PURCHASE_ORDER:
                    try {
                        getService().findById(PurchaseOrder.class, getInstance().getPurchaseOrder().getId());
                        if (warehousePurchaseOrderService.isPurchaseOrderNullified(getInstance().getPurchaseOrder())) {
                            throw new PurchaseOrderNullifiedException();
                        }
                    } catch (EntryNotFoundException e) {
                        throw new WarehousePurchaseOrderNotFoundException();
                    }
                    break;
                case ROTATORY_FUND:
                    try {
                        getService().findById(RotatoryFund.class, getInstance().getRotatoryFund().getId());
                        if (rotatoryFundService.isRotatoryFundNullified(getInstance().getRotatoryFund())) {
                            throw new RotatoryFundNullifiedException();
                        }
                    } catch (EntryNotFoundException e) {
                        throw new RotatoryFundNotFoudException();
                    }
                    break;
            }
            return null;
        } catch (WarehousePurchaseOrderNotFoundException e) {
            warehousePurchaseOrderAction.addNotFoundMessage();
            return Outcome.CANCEL;
        } catch (PurchaseOrderNullifiedException e) {
            addPurchaseOrderNullifiedErrorMessage();
            return Outcome.CANCEL;
        } catch (FixedAssetVoucherAnnulledException e) {
            fixedAssetVoucherAction.addFixedAssetVoucherAnnulledErrorMessage();
            return Outcome.CANCEL;
        } catch (RotatoryFundNullifiedException e) {
            rotatoryFundAction.addRotatoryFundAnnulledErrorMessage();
            return Outcome.CANCEL;
        } catch (RotatoryFundNotFoudException e) {
            rotatoryFundAction.addNotFoundMessage();
            return Outcome.CANCEL;
        } catch (FixedAssetVoucherNotFoudException e) {
            fixedAssetVoucherAction.addNotFoundMessage();
            return Outcome.CANCEL;
        } catch (FixedAssetPurchaseOrderNotFoudException e) {
            fixedAssetPurchaseOrderAction.addNotFoundMessage();
            return Outcome.CANCEL;
        } catch (FixedAssetPurchaseOrderAnnulledException e) {
            fixedAssetPurchaseOrderAction.addPurchaseOrderAnnulledErrorMessage();
            return Outcome.CANCEL;
        }
    }

    @Override
    protected String getDisplayNameMessage() {
        return FormatUtils.toCodeName(getInstance().getCode(), messages.get("DiscountComment.title"));
    }

    /*cleans the instance and set the operation mode to create mode*/
    private void prepareNewInstance() {
        //noinspection NullableProblems
        setInstance(null);
        setOp(OP_CREATE);
    }

    /*messages*/
    private void addWarehousePurchaseOrderNullifiedError() {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                "PurchaseOrderDetail.error.purchaseOrderAlreadyNullified",
                warehousePurchaseOrderAction.getInstance().getOrderNumber());
    }

}
