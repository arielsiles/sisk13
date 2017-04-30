package com.encens.khipus.action.warehouse;

import com.encens.khipus.exception.ConcurrencyException;
import com.encens.khipus.exception.EntryDuplicatedException;
import com.encens.khipus.exception.EntryNotFoundException;
import com.encens.khipus.exception.ReferentialIntegrityException;
import com.encens.khipus.exception.finances.CompanyConfigurationNotFoundException;
import com.encens.khipus.exception.finances.FinancesCurrencyNotFoundException;
import com.encens.khipus.exception.finances.FinancesExchangeRateNotFoundException;
import com.encens.khipus.exception.warehouse.*;
import com.encens.khipus.framework.action.GenericAction;
import com.encens.khipus.framework.action.Outcome;
import com.encens.khipus.model.warehouse.*;
import com.encens.khipus.service.warehouse.MovementDetailService;
import com.encens.khipus.service.warehouse.ProductItemService;
import com.encens.khipus.service.warehouse.WarehouseService;
import com.encens.khipus.util.BigDecimalUtil;
import com.encens.khipus.util.FormatUtils;
import com.encens.khipus.util.ValidatorUtil;
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
 * @author
 * @version 3.0
 */
@Name("partialWarehouseVoucherAction")
@Scope(ScopeType.CONVERSATION)
public class PartialWarehouseVoucherAction extends GenericAction<WarehouseVoucher> {

    @In
    private WarehouseVoucherUpdateAction warehouseVoucherUpdateAction;
    @In(create = true)
    private WarehouseVoucherCreateAction warehouseVoucherCreateAction;

    @In
    private WarehouseService warehouseService;

    @In
    private MovementDetailService movementDetailService;

    @In
    private ProductItemService productItemService;

    protected InventoryMovement inventoryMovement = new InventoryMovement();

    private Map<MovementDetail, ProductItem> selectedMovementDetailProductItemMap = new HashMap<MovementDetail, ProductItem>();
    private BigDecimal minimalResidue = BigDecimal.ZERO;

    List<MovementDetail> overflowMovementDetailList = new ArrayList<MovementDetail>();

    @Begin(nested = true, ifOutcome = Outcome.SUCCESS, flushMode = FlushModeType.MANUAL)
    @Restrict("#{s:hasPermission('WAREHOUSEVOUCHER','CREATE')}")
    public String addPartialWarehouseVoucher() {
        System.out.println("...Partial 1");
        prepareNewInstance();
        System.out.println("...Partial 2");
        getInstance().setParentWarehouseVoucher(warehouseVoucherUpdateAction.getWarehouseVoucher());
        System.out.println("...Partial 3");
        String rule = validateParentAndState();
        System.out.println("...Partial 4");
        if (rule != null) {
            System.out.println("...Partial 5");
            return rule;
        }
        System.out.println("...Partial 6");
        return Outcome.SUCCESS;
    }

    @Override
    @End(beforeRedirect = true)
    public String create() {
        try {
            validateParent();
        } catch (EntryNotFoundException e) {
            warehouseVoucherUpdateAction.addNotFoundMessage();
            return Outcome.FAIL;
        }
        try {
            if (!checkQuantities()) {
                return Outcome.REDISPLAY;
            }
        } catch (EntryNotFoundException e) {
            addMovementDetailNotFoundErrorMessage();
            return Outcome.REDISPLAY;
        }
        try {
            warehouseVoucherCreateAction.resetValidateQuantityMappings();

            warehouseService.receivePartialVoucher(getInstance(),
                    inventoryMovement,
                    getSelectedMovementDetailList(),
                    warehouseVoucherCreateAction.getMovementDetailUnderMinimalStockMap(),
                    warehouseVoucherCreateAction.getMovementDetailOverMaximumStockMap(),
                    warehouseVoucherCreateAction.getMovementDetailWithoutWarnings());
            addCreatedMessage();
            return Outcome.SUCCESS;
        } catch (EntryDuplicatedException e) {
            addDuplicatedMessage();
            return Outcome.REDISPLAY;
        } catch (WarehouseVoucherStateException e) {
            try {
                // refresh the instance
                warehouseService.findById(WarehouseVoucher.class, getInstance().getParentWarehouseVoucher().getId(), true);
            } catch (EntryNotFoundException e1) {
                warehouseVoucherUpdateAction.addNotFoundMessage();
                return Outcome.FAIL;
            }
            warehouseVoucherUpdateAction.addWarehouseVoucherStateChangedErrorMessage(e);
            return Outcome.SUCCESS;
        } catch (ProductItemNotFoundException e) {
            warehouseVoucherCreateAction.addProductItemNotFoundMessage(e.getProductItem().getFullName());
            return Outcome.FAIL;
        } catch (InventoryException e) {
            warehouseVoucherCreateAction.addInventoryMessages(e.getInventoryMessages());
            return Outcome.REDISPLAY;
        } catch (WarehouseVoucherNotFoundException e) {
            warehouseVoucherUpdateAction.addNotFoundMessage();
            return Outcome.FAIL;
        } catch (CompanyConfigurationNotFoundException e) {
            addCompanyConfigurationNotFoundErrorMessage();
            return Outcome.FAIL;
        } catch (ProductItemAmountException e) {
            warehouseVoucherUpdateAction.addNotEnoughAmountMessage(e.getProductItem(), e.getAvailableAmount());
            return Outcome.FAIL;
        } catch (FinancesCurrencyNotFoundException e) {
            addFinancesCurrencyNotFoundExceptionMessage();
            return Outcome.FAIL;
        } catch (WarehouseVoucherEmptyException e) {
            warehouseVoucherUpdateAction.addWarehouseVoucherEmptyException();
            return Outcome.REDISPLAY;
        } catch (ReferentialIntegrityException e) {
            warehouseVoucherUpdateAction.addDeleteReferentialIntegrityMessage();
            return Outcome.FAIL;
        } catch (ConcurrencyException e) {
            addUpdateConcurrencyMessage();
            return Outcome.FAIL;
        } catch (WarehouseVoucherApprovedException e) {
            warehouseVoucherUpdateAction.addWarehouseVoucherApprovedMessage();
            return Outcome.FAIL;
        } catch (InventoryProductItemNotFoundException e) {
            warehouseVoucherUpdateAction.addInventoryProductItemNotFoundErrorMessage(e.getExecutorUnitCode(),
                    e.getProductItem(), e.getWarehouse());
            return Outcome.FAIL;
        } catch (InventoryUnitaryBalanceException e) {
            warehouseVoucherUpdateAction.addInventoryUnitaryBalanceErrorMessage(e.getAvailableUnitaryBalance(), e.getProductItem());
            return Outcome.FAIL;
        } catch (FinancesExchangeRateNotFoundException e) {
            warehouseVoucherUpdateAction.addFinancesExchangeRateNotFoundExceptionMessage();
            return Outcome.FAIL;
        } catch (MovementDetailTypeException e) {
            addMovementDetailTypeErrorMessage(e);
            return Outcome.FAIL;
        } catch (WarehouseAccountCashNotFoundException e) {
            addWarehouseAccountCashNotFoundMessage();
            return Outcome.FAIL;
        }
    }

    public boolean checkQuantities() throws EntryNotFoundException {
        boolean res = true;
        overflowMovementDetailList.clear();
        for (MovementDetail movementDetail : getSelectedMovementDetailList()) {
            MovementDetail parentMovementDetail = getService().findById(MovementDetail.class, movementDetail.getParentMovementDetail().getId(), true);
            BigDecimal maximum = maximum(parentMovementDetail);
            if (movementDetail.getQuantity().compareTo(maximum) > 0) {
                overflowMovementDetailList.add(movementDetail);
                facesMessages.addFromResourceBundle(StatusMessage.Severity.WARN,
                        "MovementDetail.error.partialMovementDetailOverflow", movementDetail.getProductItem().getFullName(),
                        maximum);
                res = false;
            }
        }
        return res;
    }

    @Override
    public void createAndNew() {
        try {
            warehouseVoucherCreateAction.resetValidateQuantityMappings();

            warehouseService.receivePartialVoucher(getInstance().getParentWarehouseVoucher(),
                    inventoryMovement,
                    warehouseVoucherCreateAction.getMovementDetails(),
                    warehouseVoucherCreateAction.getMovementDetailUnderMinimalStockMap(),
                    warehouseVoucherCreateAction.getMovementDetailOverMaximumStockMap(),
                    warehouseVoucherCreateAction.getMovementDetailWithoutWarnings());
            addCreatedMessage();
            WarehouseVoucher newWarehouseVoucher = buildNewInstance(getInstance());
            prepareNewInstance();
            setInstance(newWarehouseVoucher);
        } catch (EntryDuplicatedException e) {
            addDuplicatedMessage();
        } catch (WarehouseVoucherStateException e) {
            try {
                // refresh the instance
                warehouseService.findById(WarehouseVoucher.class, getInstance().getParentWarehouseVoucher().getId(), true);
            } catch (EntryNotFoundException e1) {
                warehouseVoucherUpdateAction.addNotFoundMessage();
            }
            warehouseVoucherUpdateAction.addWarehouseVoucherStateChangedErrorMessage(e);
        } catch (ProductItemNotFoundException e) {
            warehouseVoucherCreateAction.addProductItemNotFoundMessage(e.getProductItem().getFullName());
        } catch (InventoryException e) {
            warehouseVoucherCreateAction.addInventoryMessages(e.getInventoryMessages());
        } catch (WarehouseVoucherNotFoundException e) {
            warehouseVoucherUpdateAction.addNotFoundMessage();
        } catch (CompanyConfigurationNotFoundException e) {
            addCompanyConfigurationNotFoundErrorMessage();
        } catch (ProductItemAmountException e) {
            warehouseVoucherUpdateAction.addNotEnoughAmountMessage(e.getProductItem(), e.getAvailableAmount());
        } catch (FinancesCurrencyNotFoundException e) {
            addFinancesCurrencyNotFoundExceptionMessage();
        } catch (WarehouseVoucherEmptyException e) {
            warehouseVoucherUpdateAction.addWarehouseVoucherEmptyException();
        } catch (ReferentialIntegrityException e) {
            warehouseVoucherUpdateAction.addDeleteReferentialIntegrityMessage();
        } catch (ConcurrencyException e) {
            addUpdateConcurrencyMessage();
        } catch (WarehouseVoucherApprovedException e) {
            warehouseVoucherUpdateAction.addWarehouseVoucherApprovedMessage();
        } catch (InventoryProductItemNotFoundException e) {
            warehouseVoucherUpdateAction.addInventoryProductItemNotFoundErrorMessage(e.getExecutorUnitCode(),
                    e.getProductItem(), e.getWarehouse());
        } catch (InventoryUnitaryBalanceException e) {
            warehouseVoucherUpdateAction.addInventoryUnitaryBalanceErrorMessage(e.getAvailableUnitaryBalance(), e.getProductItem());
        } catch (FinancesExchangeRateNotFoundException e) {
            warehouseVoucherUpdateAction.addFinancesExchangeRateNotFoundExceptionMessage();
        } catch (MovementDetailTypeException e) {
            addMovementDetailTypeErrorMessage(e);
        } catch (WarehouseAccountCashNotFoundException e) {
            addWarehouseAccountCashNotFoundMessage();
            e.printStackTrace();
        }
    }

    public void addWarehouseAccountCashNotFoundMessage() {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                "WarehouseVoucher.error.WarehouseAccountCashNotFoundMessage");
    }

    /**
     * Prepare new instance with old instance default data
     *
     * @param warehouseVoucher old instance from which default data will be copy
     * @return a new DiscountComment instance with default data
     */
    private WarehouseVoucher buildNewInstance(WarehouseVoucher warehouseVoucher) {
        WarehouseVoucher newWarehouseVoucher = new WarehouseVoucher();
        newWarehouseVoucher.setParentWarehouseVoucher(warehouseVoucher);
        return newWarehouseVoucher;
    }

    @Override
    @End(beforeRedirect = true)
    public String cancel() {
        return super.cancel();
    }

    @Override
    @Begin(nested = true, flushMode = FlushModeType.MANUAL)
    public String select(WarehouseVoucher warehouseVoucher) {
        try {
            setOp(OP_UPDATE);
            setInstance(genericService.findById(WarehouseVoucher.class, warehouseVoucher.getId()));
            return Outcome.SUCCESS;
        } catch (EntryNotFoundException e) {
            addNotFoundMessage();
            return Outcome.FAIL;
        }
    }

    /**
     * Validates if the main instance still exist and if its state is not Annulled
     *
     * @throws com.encens.khipus.exception.EntryNotFoundException
     *          if the instance parent couldn't be found
     */
    private void validateParent() throws EntryNotFoundException {

        getService().findById(WarehouseVoucher.class, getInstance().getParentWarehouseVoucher().getId());
    }

    /**
     * Validates if the main instance still exist and if its state is not Annulled
     *
     * @return a String indicating the navigation rule and null if it is ok
     */
    private String validateParentAndState() {
        try {
            WarehouseVoucher dbParentWarehouseVoucher = getService().findById(WarehouseVoucher.class, getInstance().getParentWarehouseVoucher().getId());
            if (dbParentWarehouseVoucher.getState().equals(WarehouseVoucherState.ANL)) {
                warehouseVoucherUpdateAction.addProductItemNotFoundMessage(getInstance().getParentWarehouseVoucher().getDocumentCode());
                return Outcome.CANCEL;
            }
        } catch (EntryNotFoundException e) {
            warehouseVoucherUpdateAction.addProductItemNotFoundMessage(getInstance().getParentWarehouseVoucher().getDocumentCode());
            return Outcome.CANCEL;
        }
        return null;

    }

    @Override
    protected String getDisplayNameMessage() {
        return FormatUtils.toCodeName(getInstance().getDocumentCode(), messages.get("WarehouseVoucher.title"));
    }

    /*cleans the instance and set the operation mode to create mode*/
    private void prepareNewInstance() {
        //noinspection NullableProblems
        setInstance(null);
        setOp(OP_CREATE);
    }

    // adds a set of new movement details only supported for input type
    public void addProductItems(List<ProductItem> productItemList) {
        productItemList = productItemService.findInProductItemList(productItemList);
        List<MovementDetail> parentMovementDetailList = movementDetailService
                .findDetailListByVoucher(getInstance().getParentWarehouseVoucher());
        Map<ProductItem, MovementDetail> parentProductItemMovementDetailMap = new HashMap<ProductItem, MovementDetail>();
        for (MovementDetail movementDetail : parentMovementDetailList) {
            parentProductItemMovementDetailMap.put(movementDetail.getProductItem(), movementDetail);
        }
        for (ProductItem productItem : productItemList) {
            // check if residue is gt 0 and ProductItem not in selectedMovementDetailProductItemMap values
            if (!getSelectedMovementDetailProductItemMap().containsValue(productItem)) {
                addAndBuildProductItem(parentProductItemMovementDetailMap, productItem);
            }
        }

    }

    // adds a set of new movement details corresponding to all movement details only supported for input type
    public void addAllProductItems() {
        getSelectedMovementDetailProductItemMap().clear();
        List<MovementDetail> parentMovementDetailList = movementDetailService
                .findDetailListByVoucher(getInstance().getParentWarehouseVoucher());
        Map<ProductItem, MovementDetail> parentProductItemMovementDetailMap = new HashMap<ProductItem, MovementDetail>();
        for (MovementDetail movementDetail : parentMovementDetailList) {
            parentProductItemMovementDetailMap.put(movementDetail.getProductItem(), movementDetail);
        }
        for (ProductItem productItem : parentProductItemMovementDetailMap.keySet()) {
            // check if residue is gt 0 and ProductItem not in selectedMovementDetailProductItemMap values
            addAndBuildProductItem(parentProductItemMovementDetailMap, productItem);
        }

    }

    private void addAndBuildProductItem(Map<ProductItem, MovementDetail> parentProductItemMovementDetailMap, ProductItem productItem) {
        MovementDetail parentMovementDetail = parentProductItemMovementDetailMap.get(productItem);
        try {
            parentMovementDetail = getService().findById(MovementDetail.class, parentMovementDetail.getId(), true);
            BigDecimal quantity = null != parentMovementDetail.getResidue() ? parentMovementDetail.getResidue() : parentMovementDetail.getQuantity();
            if (BigDecimalUtil.isPositive(quantity)) {
                MovementDetail detail = new MovementDetail();
                detail.setProductItem(productItem);
                detail.setMeasureUnit(parentMovementDetail.getMeasureUnit());
                detail.setWarehouse(parentMovementDetail.getWarehouse());
                detail.setUnitCost(parentMovementDetail.getUnitCost());
                detail.setProductItemAccount(parentMovementDetail.getProductItemAccount());
                detail.setParentMovementDetail(parentMovementDetail);
                detail.setQuantity(quantity);
                warehouseVoucherCreateAction.updateTotalAmount(detail);
                getSelectedMovementDetailProductItemMap().put(detail, productItem);
            }
        } catch (EntryNotFoundException e) {
            log.debug("don't add nothing parent MovementDetail was not found", e);
        }
    }


    public void removeSelected(MovementDetail movementDetail) {
        if (getSelectedMovementDetailProductItemMap().containsKey(movementDetail)) {
            getSelectedMovementDetailProductItemMap().remove(movementDetail);
        }
    }

    /**
     * @param movementDetail a parent MovementDetail
     * @return the maximum available residue to receive a partial MovementDetail
     */
    public BigDecimal maximum(MovementDetail movementDetail) {
        return null == movementDetail.getResidue() ? movementDetail.getQuantity() : movementDetail.getResidue();
    }

    public BigDecimal getMinimalResidue() {
        return minimalResidue;
    }

    public void setMinimalResidue(BigDecimal minimalResidue) {
        this.minimalResidue = minimalResidue;
    }


    public Map<MovementDetail, ProductItem> getSelectedMovementDetailProductItemMap() {
        return selectedMovementDetailProductItemMap;
    }

    public void setSelectedMovementDetailProductItemMap(Map<MovementDetail, ProductItem> selectedMovementDetailProductItemMap) {
        this.selectedMovementDetailProductItemMap = selectedMovementDetailProductItemMap;
    }

    public List<ProductItem> getSelectedProductItemList() {
        return ValidatorUtil.isEmptyOrNull(getSelectedMovementDetailProductItemMap()) ? new ArrayList<ProductItem>() : new ArrayList<ProductItem>(getSelectedMovementDetailProductItemMap().values());
    }

    public List<MovementDetail> getSelectedMovementDetailList() {
        return ValidatorUtil.isEmptyOrNull(getSelectedMovementDetailProductItemMap()) ? new ArrayList<MovementDetail>() : new ArrayList<MovementDetail>(getSelectedMovementDetailProductItemMap().keySet());
    }

    private void addFinancesCurrencyNotFoundExceptionMessage() {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.INFO,
                "FixedAssets.FinancesCurrencyNotFoundException");
    }

    private void addMovementDetailTypeErrorMessage(MovementDetailTypeException e) {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.INFO,
                "MovementDetail.error.movementDetailType", messages.get(e.getExpectedMovementDetailType().getResourceKey()));
    }

    private void addMovementDetailNotFoundErrorMessage() {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR, "MovementDetail.error.movementDetailType");
    }

}
