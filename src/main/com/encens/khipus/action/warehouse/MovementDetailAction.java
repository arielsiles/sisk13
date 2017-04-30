package com.encens.khipus.action.warehouse;

import com.encens.khipus.exception.ConcurrencyException;
import com.encens.khipus.exception.warehouse.*;
import com.encens.khipus.framework.action.GenericAction;
import com.encens.khipus.framework.action.Outcome;
import com.encens.khipus.interceptor.BusinessUnitRestrict;
import com.encens.khipus.interceptor.BusinessUnitRestriction;
import com.encens.khipus.model.finances.MeasureUnit;
import com.encens.khipus.model.finances.MeasureUnitPk;
import com.encens.khipus.model.warehouse.*;
import com.encens.khipus.service.warehouse.WarehouseCatalogService;
import com.encens.khipus.service.warehouse.WarehouseService;
import com.encens.khipus.util.BigDecimalUtil;
import com.encens.khipus.util.MessageUtils;
import com.encens.khipus.util.warehouse.WarehouseUtil;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.*;
import org.jboss.seam.annotations.security.Restrict;
import org.jboss.seam.international.StatusMessage;

import java.math.BigDecimal;

/**
 * @author
 * @version 3.0
 */
@Name("movementDetailAction")
@Scope(ScopeType.CONVERSATION)
@BusinessUnitRestrict
public class MovementDetailAction extends GenericAction<MovementDetail> {

    @In
    private WarehouseCatalogService warehouseCatalogService;

    @In(value = "warehouseVoucherUpdateAction")
    private WarehouseVoucherUpdateAction warehouseVoucherUpdateAction;

    @In
    private WarehouseService warehouseService;

    private MovementDetailUIController uiController;

    @Factory(value = "movementDetail", scope = ScopeType.STATELESS)
    public MovementDetail initMovementDetail() {
        return getInstance();
    }

    @BusinessUnitRestriction(value = "#{warehouseVoucherUpdateAction.warehouseVoucher}")
    @Begin(nested = true, ifOutcome = Outcome.SUCCESS, flushMode = FlushModeType.MANUAL)
    @Restrict("#{s:hasPermission('WAREHOUSEVOUCHER','UPDATE')}")
    public String addMovementDetail() {
        if (warehouseService.isWarehouseVoucherApproved(getWarehouseVoucher().getId())) {
            warehouseVoucherUpdateAction.addWarehouseVoucherApprovedMessage();
            return WarehouseVoucherUpdateAction.APPROVED_OUTCOME;
        }

        initDefaultAttributes();

        return Outcome.SUCCESS;
    }

    public void initDefaultAttributes() {
        setInstance(null);
        setOp(OP_CREATE);

        uiController = new MovementDetailUIController(getWarehouseVoucher());

        getInstance().setTransactionNumber(getWarehouseVoucher().getId().getTransactionNumber());
        getInstance().setState(getWarehouseVoucher().getState());
        getInstance().setExecutorUnit(getWarehouseVoucher().getExecutorUnit());
        getInstance().setCostCenterCode(getWarehouseVoucher().getCostCenterCode());
        getInstance().setWarehouse(getWarehouseVoucher().getWarehouse());
        MovementDetailType detailType = WarehouseUtil.getMovementTye(getWarehouseVoucher().getDocumentType());
        if (getWarehouseVoucher().isTransfer() || getWarehouseVoucher().isExecutorUnitTransfer()) {
            detailType = MovementDetailType.S;
        }
        getInstance().setMovementType(detailType);
    }

    public WarehouseVoucher getWarehouseVoucher() {
        if (null != getInstance().getParentMovementDetail()) {
            try {
                InventoryMovementPK inventoryMovementPK = getInstance().getInventoryMovement().getId();
                return warehouseService.findWarehouseVoucher(new WarehouseVoucherPK(
                        inventoryMovementPK.getCompanyNumber(), inventoryMovementPK.getTransactionNumber()));
            } catch (WarehouseVoucherNotFoundException e) {
                return null;
            }
        } else {
            return warehouseVoucherUpdateAction.getWarehouseVoucher();
        }
    }

    public boolean isTransference() {
        return warehouseVoucherUpdateAction.isTransference();
    }

    @Override
    @BusinessUnitRestriction(value = "#{warehouseVoucherUpdateAction.warehouseVoucher}")
    @Begin(nested = true, flushMode = FlushModeType.MANUAL, join = true)
    public String select(MovementDetail instance) {
        setOp(OP_UPDATE);
        try {
            uiController = new MovementDetailUIController(getWarehouseVoucher());

            setInstance(warehouseService.readMovementDetail(getWarehouseVoucher(), instance));

            return Outcome.SUCCESS;
        } catch (WarehouseVoucherNotFoundException e) {
            warehouseVoucherUpdateAction.addNotFoundMessage();
            return Outcome.FAIL;
        } catch (MovementDetailNotFoundException e) {
            addNotFoundMessage();
            return Outcome.CANCEL;
        }
    }

    @Override
    @BusinessUnitRestriction(value = "#{warehouseVoucherUpdateAction.warehouseVoucher}")
    public String create() {
        String validationOutcome = validations();
        if (!Outcome.SUCCESS.equals(validationOutcome)) {
            return validationOutcome;
        }

        String serviceOutcome = Outcome.SUCCESS;
        warehouseVoucherUpdateAction.resetValidateQuantityMappings();
        try {
            warehouseVoucherUpdateAction.buildValidateQuantityMappings(getInstance());
            warehouseService.createMovementDetail(getWarehouseVoucher(), getInstance(),
                    warehouseVoucherUpdateAction.getMovementDetailUnderMinimalStockMap(),
                    warehouseVoucherUpdateAction.getMovementDetailOverMaximumStockMap(),
                    warehouseVoucherUpdateAction.getMovementDetailWithoutWarnings());
            addCreatedMessage();
            warehouseVoucherUpdateAction.showMovementDetailWarningMessages();
        } catch (WarehouseVoucherApprovedException e) {
            warehouseVoucherUpdateAction.addWarehouseVoucherApprovedMessage();
        } catch (WarehouseVoucherNotFoundException e) {
            warehouseVoucherUpdateAction.addNotFoundMessage();
            serviceOutcome = Outcome.FAIL;
        } catch (InventoryException e) {
            warehouseVoucherUpdateAction.addInventoryMessages(e.getInventoryMessages());
            serviceOutcome = Outcome.REDISPLAY;
        } catch (ProductItemNotFoundException e) {
            warehouseVoucherUpdateAction.addProductItemNotFoundMessage(e.getProductItem().getFullName());
            serviceOutcome = Outcome.FAIL;
        }

        closeConversation(serviceOutcome);
        return serviceOutcome;
    }

    @Override
    @BusinessUnitRestriction(value = "#{warehouseVoucherUpdateAction.warehouseVoucher}")
    public void createAndNew() {
        if (Outcome.SUCCESS.equals(validations())) {
            warehouseVoucherUpdateAction.resetValidateQuantityMappings();
            try {
                warehouseVoucherUpdateAction.buildValidateQuantityMappings(getInstance());
                warehouseService.createMovementDetail(getWarehouseVoucher(), getInstance(),
                        warehouseVoucherUpdateAction.movementDetailUnderMinimalStockMap,
                        warehouseVoucherUpdateAction.movementDetailOverMaximumStockMap,
                        warehouseVoucherUpdateAction.movementDetailWithoutWarnings);
                initDefaultAttributes();
                addCreatedMessage();
                warehouseVoucherUpdateAction.showMovementDetailWarningMessages();
            } catch (WarehouseVoucherApprovedException e) {
                warehouseVoucherUpdateAction.addWarehouseVoucherApprovedMessage();
            } catch (WarehouseVoucherNotFoundException e) {
                warehouseVoucherUpdateAction.addNotFoundMessage();
            } catch (InventoryException e) {
                warehouseVoucherUpdateAction.addInventoryMessages(e.getInventoryMessages());
            } catch (ProductItemNotFoundException e) {
                warehouseVoucherUpdateAction.addProductItemNotFoundMessage(e.getProductItem().getFullName());
            }
        }
    }

    @Override
    @BusinessUnitRestriction(value = "#{warehouseVoucherUpdateAction.warehouseVoucher}")
    public String delete() {
        String outcome = Outcome.SUCCESS;
        try {
            warehouseService.deleteMovementDetail(getWarehouseVoucher(), getInstance());
            addDeletedMessage();
        } catch (WarehouseVoucherNotFoundException e) {
            warehouseVoucherUpdateAction.addNotFoundMessage();
            outcome = Outcome.FAIL;
        } catch (MovementDetailNotFoundException e) {
            addNotFoundMessage();
            outcome = Outcome.CANCEL;
        } catch (WarehouseVoucherApprovedException e) {
            warehouseVoucherUpdateAction.addWarehouseVoucherApprovedMessage();
        }
        closeConversation(outcome);

        return outcome;
    }

    @Override
    @BusinessUnitRestriction(value = "#{warehouseVoucherUpdateAction.warehouseVoucher}")
    @End(beforeRedirect = true)
    public String update() {
        String validationOutcome = validations();
        if (!Outcome.SUCCESS.equals(validationOutcome)) {
            return validationOutcome;
        }

        String outcome = Outcome.SUCCESS;
        warehouseVoucherUpdateAction.resetValidateQuantityMappings();
        try {
            warehouseVoucherUpdateAction.buildValidateQuantityMappings(getInstance());
            warehouseService.updateMovementDetail(warehouseVoucherUpdateAction.getWarehouseVoucher(), getInstance(),
                    warehouseVoucherUpdateAction.getMovementDetailUnderMinimalStockMap(),
                    warehouseVoucherUpdateAction.getMovementDetailOverMaximumStockMap(),
                    warehouseVoucherUpdateAction.getMovementDetailWithoutWarnings());
            addUpdatedMessage();
            warehouseVoucherUpdateAction.showMovementDetailWarningMessages();
        } catch (WarehouseVoucherNotFoundException e) {
            warehouseVoucherUpdateAction.addNotFoundMessage();
            outcome = Outcome.FAIL;
        } catch (MovementDetailNotFoundException e) {
            addNotFoundMessage();
            outcome = Outcome.CANCEL;
        } catch (WarehouseVoucherApprovedException e) {
            warehouseVoucherUpdateAction.addWarehouseVoucherApprovedMessage();
        } catch (ConcurrencyException e) {
            try {
                setInstance(warehouseService.readMovementDetail(getWarehouseVoucher(), getInstance()));
                addUpdateConcurrencyMessage();
                outcome = Outcome.REDISPLAY;
            } catch (WarehouseVoucherNotFoundException e1) {
                warehouseVoucherUpdateAction.addNotFoundMessage();
                outcome = Outcome.FAIL;
            } catch (MovementDetailNotFoundException e1) {
                addNotFoundMessage();
                outcome = Outcome.CANCEL;
            }
        } catch (InventoryException e) {
            warehouseVoucherUpdateAction.addInventoryMessages(e.getInventoryMessages());
            outcome = Outcome.REDISPLAY;
        } catch (ProductItemNotFoundException e) {
            warehouseVoucherUpdateAction.addProductItemNotFoundMessage(e.getProductItem().getFullName());
            outcome = Outcome.FAIL;
        }
        return outcome;
    }

    @Override
    @BusinessUnitRestriction(value = "#{warehouseVoucherUpdateAction.warehouseVoucher}")
    @End(beforeRedirect = true)
    public String cancel() {
        return super.cancel();
    }

    public void assignProductItem(ProductItem productItem) {
        ProductItem dbProductItem = warehouseCatalogService.findWarehouseCatalog(ProductItem.class, productItem.getId());

        if (null != dbProductItem) {
            MeasureUnit measureUnit = getMeasureUnitFromDatabase(dbProductItem);

            if (null != measureUnit) {
                getInstance().setProductItem(dbProductItem);
                getInstance().setMeasureUnit(getMeasureUnitFromDatabase(dbProductItem));
                if (uiController.isShownUnitCostField()) {
                    getInstance().setUnitCost(dbProductItem.getUnitCost());
                }
                updateProductItemAccount();
                calculateTotalAmount();
            }
        }
    }

    public void updateProductItemFields() {
        if (null != getInstance().getProductItem()) {
            MeasureUnit measureUnit = getMeasureUnitFromDatabase(getInstance().getProductItem());

            if (null != measureUnit) {
                getInstance().setMeasureUnit(measureUnit);

                if (isShownUnitCostField()) {
                    getInstance().setUnitCost(getInstance().getProductItem().getUnitCost());
                }
                updateProductItemAccount();
                calculateTotalAmount();
            }
        }
    }

    public void clearProductItem() {
        getInstance().setProductItem(null);
        getInstance().setMeasureUnit(null);
        getInstance().setAmount(null);
        getInstance().setUnitCost(null);
        updateProductItemAccount();
    }

    public boolean isShowAmountField() {
        return uiController.isShownAmountField(getInstance().getProductItem());
    }

    public boolean isEnabledAmountField() {
        return uiController.isEnabledAmountField();
    }

    public boolean isShownUnitCostField() {
        return uiController.isShownUnitCostField();
    }

    public void calculateTotalAmount() {
        if (null != getInstance().getProductItem() && null != getInstance().getQuantity()) {
            BigDecimal totalAmount = BigDecimalUtil.multiply(
                    getInstance().getProductItem().getUnitCost(), getInstance().getQuantity(), 6);
            getInstance().setAmount(totalAmount);
        }
    }

    @Override
    protected String getDisplayNameMessage() {
        if (null != getInstance().getProductItem()) {
            return getInstance().getProductItem().getName();
        }

        return super.getDisplayNameMessage();
    }

    private void updateProductItemAccount() {
        if (null == getInstance().getProductItem()) {
            getInstance().setProductItemAccount(null);
        } else {
            getInstance().setProductItemAccount(getInstance().getProductItem().getProductItemAccount());
        }
    }

    private String validations() {
        boolean existsErrors = false;

        ProductItem productItem = getInstance().getProductItem();
        if (null == productItem) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                    "Common.required", MessageUtils.getMessage("MovementDetail.productItem"));
            existsErrors = true;
        } else {
            if (!warehouseCatalogService.existWarehouseCatalogInDataBase(ProductItem.class, productItem.getId())) {
                facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                        "Common.error.notFound", productItem.getName());
                existsErrors = true;
            } else {
                if (!warehouseCatalogService.isValidState(ProductItem.class, productItem.getId(), ProductItemState.VIG)) {
                    facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                            "WarehouseVoucher.error.productItemInvalid", productItem.getName());
                    existsErrors = true;
                }
            }
        }

        BigDecimal quantity = getInstance().getQuantity();
        if (null != quantity && BigDecimal.ZERO.compareTo(quantity) >= 0) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                    "Common.error.greaterThan",
                    MessageUtils.getMessage("MovementDetail.quantity"), BigDecimal.ZERO);

            existsErrors = true;
        }

        if (existsErrors) {
            return Outcome.REDISPLAY;
        }

        return Outcome.SUCCESS;
    }

    private MeasureUnit getMeasureUnitFromDatabase(ProductItem productItem) {
        MeasureUnitPk measureUnitPk = new MeasureUnitPk(productItem.getId().getCompanyNumber(),
                productItem.getUsageMeasureCode());
        return warehouseCatalogService.findWarehouseCatalog(MeasureUnit.class, measureUnitPk);
    }

    public boolean isApproved() {
        return getInstance().getParentMovementDetail() != null ? getInstance().isApproved() : warehouseVoucherUpdateAction.isApproved();
    }

    public boolean isPending() {
        return getInstance().getParentMovementDetail() != null ? getInstance().isPending() : warehouseVoucherUpdateAction.isPending();
    }
}
