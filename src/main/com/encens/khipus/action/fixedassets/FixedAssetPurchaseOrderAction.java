package com.encens.khipus.action.fixedassets;

import com.encens.khipus.exception.ConcurrencyException;
import com.encens.khipus.exception.EntryDuplicatedException;
import com.encens.khipus.exception.EntryNotFoundException;
import com.encens.khipus.exception.finances.CompanyConfigurationNotFoundException;
import com.encens.khipus.exception.finances.FinancesCurrencyNotFoundException;
import com.encens.khipus.exception.finances.FinancesExchangeRateNotFoundException;
import com.encens.khipus.exception.purchase.*;
import com.encens.khipus.exception.warehouse.AdvancePaymentPendingException;
import com.encens.khipus.framework.action.GenericAction;
import com.encens.khipus.framework.action.Outcome;
import com.encens.khipus.framework.service.GenericService;
import com.encens.khipus.interceptor.BusinessUnitRestrict;
import com.encens.khipus.interceptor.BusinessUnitRestriction;
import com.encens.khipus.model.admin.User;
import com.encens.khipus.model.finances.CostCenter;
import com.encens.khipus.model.finances.JobContract;
import com.encens.khipus.model.finances.Provider;
import com.encens.khipus.model.fixedassets.FixedAsset;
import com.encens.khipus.model.fixedassets.FixedAssetState;
import com.encens.khipus.model.purchases.PurchaseOrder;
import com.encens.khipus.model.purchases.PurchaseOrderPayment;
import com.encens.khipus.model.purchases.PurchaseOrderType;
import com.encens.khipus.service.employees.JobContractService;
import com.encens.khipus.service.fixedassets.FixedAssetPurchaseOrderService;
import com.encens.khipus.service.fixedassets.FixedAssetService;
import com.encens.khipus.service.fixedassets.PurchaseOrderCauseFixedAssetStateService;
import com.encens.khipus.util.BigDecimalUtil;
import com.encens.khipus.util.MessageUtils;
import com.encens.khipus.util.purchases.PurchaseOrderValidator;
import com.encens.khipus.util.query.QueryUtils;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.*;
import org.jboss.seam.annotations.security.Restrict;
import org.jboss.seam.international.StatusMessage;

import javax.persistence.EntityManager;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * @author
 * @version 2.26
 */

@BusinessUnitRestrict
@Name("fixedAssetPurchaseOrderAction")
@Scope(ScopeType.CONVERSATION)
public class FixedAssetPurchaseOrderAction extends GenericAction<PurchaseOrder> {

    @In(value = "fixedAssetPurchaseOrderService")
    private FixedAssetPurchaseOrderService service;

    @In(create = true)
    private LiquidationPaymentAction liquidationPaymentAction;

    @In
    private User currentUser;

    @In(create = true, value = "fixedAssetPurchaseOrderDetailListCreateAction")
    private FixedAssetPurchaseOrderDetailListCreateAction detailListCreateAction;

    @In(create = true, value = "purchaseOrderFixedAssetPartsCreateAction")
    private PurchaseOrderFixedAssetPartsCreateAction fixedAssetPartsCreateAction;

    @In(create = true)
    private PurchaseOrderValidator purchaseOrderValidator;
    @In
    private FixedAssetService fixedAssetService;
    @In
    private PurchaseOrderCauseFixedAssetStateService purchaseOrderCauseFixedAssetStateService;
    @In
    private JobContractService jobContractService;

    @In(value = "#{listEntityManager}")
    private EntityManager listEm;

    @In(value = "#{entityManager}")
    private EntityManager em;

    private String activeTabName = "detailPurchaseOrderTab";

    public static final String APPROVED_OUTCOME = "Approved";

    public static final String FINALIZED_OUTCOME = "Finalized";

    public static final String LIQUIDATED_OUTCOME = "Liquidated";

    /* they are used in transference action when it is required a list of selected fixed asset to apply the transfer operation*/
    private List<FixedAsset> selectedFixedAssetList;
    private List<Long> selectedFixedAssetIdList;

    /* FixedAssetState restriction list associated to the purchaseOrderCause if it requires any*/
    private List<FixedAssetState> fixedAssetStateRestrictionList;

    @Create
    public void init() {
        if (!isManaged()) {
            selectedFixedAssetList = new ArrayList<FixedAsset>();
            fixedAssetStateRestrictionList = new ArrayList<FixedAssetState>();
            selectedFixedAssetIdList = new ArrayList<Long>();
        }
    }


    @Factory(value = "fixedAssetPurchaseOrder", scope = ScopeType.STATELESS)
    public PurchaseOrder initPurchaseOrder() {
        getInstance().setOrderType(PurchaseOrderType.FIXEDASSET);
        return getInstance();
    }

    public void updateCurrentInstance() {
        setInstance(service.findPurchaseOrder(getInstance().getId()));
    }

    public void resetTotalAmount() {
        getInstance().setTotalAmount(BigDecimal.ZERO);
    }


    public void calculatePercentAmountByTotalAmount() {
        BigDecimal discountAmount = BigDecimal.ZERO;
        BigDecimal discountPercent = BigDecimal.ZERO;
        if (!BigDecimalUtil.isZeroOrNull(getInstance().getSubTotalAmount())) {
            discountAmount = BigDecimalUtil.subtract(getInstance().getSubTotalAmount(), getInstance().getTotalAmount());
            if (!BigDecimalUtil.isZeroOrNull(discountAmount)) {
                discountPercent = BigDecimalUtil.divide(BigDecimalUtil.multiply(discountAmount, BigDecimalUtil.ONE_HUNDRED), getInstance().getSubTotalAmount(), 4);
            }
        }
        getInstance().setDiscountAmount(discountAmount);
        getInstance().setDiscountPercent(discountPercent);
    }

    public void calculateTotalAmountByPercentAmount() {
        BigDecimal discountPercentage = getInstance().getDiscountPercent();
        BigDecimal discountAmount = BigDecimal.ZERO;
        if (BigDecimalUtil.isPositive(discountPercentage) && BigDecimalUtil.isPositive(getInstance().getSubTotalAmount())) {
            discountAmount = BigDecimalUtil.multiply(
                    getInstance().getSubTotalAmount(),
                    BigDecimalUtil.divide(discountPercentage, BigDecimalUtil.ONE_HUNDRED, 7));
        }
        BigDecimal totalAmount = BigDecimalUtil.subtract(getInstance().getSubTotalAmount(), discountAmount);
        getInstance().setTotalAmount(totalAmount);
        getInstance().setDiscountPercent(discountPercentage);
        getInstance().setDiscountAmount(discountAmount);
    }


    @BusinessUnitRestriction(value = "#{fixedAssetPurchaseOrderAction.instance}", postValidation = true)
    @Begin(ifOutcome = Outcome.SUCCESS, flushMode = FlushModeType.MANUAL)
    @Override
    @Restrict("#{s:hasPermission('FIXEDASSETPURCHASEORDER','VIEW')}")
    public String select(PurchaseOrder instance) {
        String outcome = super.select(instance);
        loadDefaultInformation();
        return outcome;
    }

    public void loadDefaultInformation() {
        if (getInstance().isPurchaseOrderFinalized()) {
            liquidationPaymentAction.setDefaultDescription(getInstance(),
                    MessageUtils.getMessage("FixedAssetPurchaseOrder.fixedAssets"),
                    MessageUtils.getMessage("FixedAssetPurchaseOrder.orderNumberAcronym"));
            liquidationPaymentAction.setPurchaseOrder(getInstance());
        }

        /*in case the page have not been redirected or rerendered since the same page (create and post loadDefaultInformation operation)*/
        if (isManaged()) {
            /*in case the redirect from the same page in voucher creation case do not query*/
            selectedFixedAssetList = fixedAssetService.findFixedAssetListByFixedAssetPurchaseOrder(getInstance(), null);
            if (isPurchaseOrderPending()) {
                fixedAssetStateRestrictionList = purchaseOrderCauseFixedAssetStateService.findFixedAssetStateByPurchaseOrderCause(getInstance().getPurchaseOrderCause(), null);
            } else {
                fixedAssetStateRestrictionList = new ArrayList<FixedAssetState>();
            }
            selectedFixedAssetIdList = new ArrayList<Long>();
            for (FixedAsset fixedAsset : getSelectedFixedAssetList()) {
                selectedFixedAssetIdList.add(fixedAsset.getId());
            }
        }
        setOp(OP_UPDATE);
    }

    @Override
    @End
    @Restrict("#{s:hasPermission('FIXEDASSETPURCHASEORDER','CREATE')}")
    public String create() {
        String validationOutcome = validateInputFields();
        if (!Outcome.SUCCESS.equals(validationOutcome)) {
            return validationOutcome;
        }
        if (!validateAssociatedFixedAssetList()) {
            return Outcome.REDISPLAY;
        }
        if (!validatePurchaseOrderDetail()) {
            return Outcome.REDISPLAY;
        }

        try {
            if (getInstance().getPurchaseOrderCause().isFixedassetPurchase()) {
                service.create(getInstance(), detailListCreateAction.getInstances(), selectedFixedAssetList);
            } else {
                service.create(getInstance(), fixedAssetPartsCreateAction.getInstances());
            }
            loadDefaultInformation();
            return Outcome.SUCCESS;
        } catch (EntryDuplicatedException e) {
            addDuplicatedMessage();
            return Outcome.REDISPLAY;
        }
    }

    @BusinessUnitRestriction(value = "#{fixedAssetPurchaseOrderAction.instance}")
    @Override
    @End
    @Restrict("#{s:hasPermission('FIXEDASSETPURCHASEORDER','UPDATE')}")
    public String update() {
        if (!purchaseOrderValidator.isValidThePurchaseDocuments(getInstance(), false)) {
            return Outcome.REDISPLAY;
        }

        String validationOutcome = validateInputFields();
        if (!Outcome.SUCCESS.equals(validationOutcome)) {
            return validationOutcome;
        }
        if (!validateAssociatedFixedAssetList()) {
            return Outcome.REDISPLAY;
        }
        /*reset lists if they are not necessary */
        if (!getInstance().getPurchaseOrderCause().getRequiresFixedAssets()) {
            selectedFixedAssetList.clear();
            selectedFixedAssetIdList.clear();
            selectedFixedAssetIdList.add(new Long(0));
            fixedAssetStateRestrictionList = new ArrayList<FixedAssetState>();
        }
        try {
            service.updateFixedAssetPurchaseOrder(getInstance(), selectedFixedAssetList);
            addUpdatedMessage();
            return Outcome.SUCCESS;
        } catch (PurchaseOrderApprovedException e) {
            addPurchaseOrderApprovedErrorMessage();
            return APPROVED_OUTCOME;
        } catch (PurchaseOrderFinalizedException e) {
            addPurchaseOrderFinalizedErrorMessage();
            return FINALIZED_OUTCOME;
        } catch (ConcurrencyException e) {
            updateCurrentInstance();
            addUpdateConcurrencyMessage();
            return Outcome.REDISPLAY;
        } catch (PurchaseOrderNullifiedException e) {
            addPurchaseOrderAnnulledErrorMessage();
            return FINALIZED_OUTCOME;
        } catch (PurchaseOrderLiquidatedException e) {
            addPurchaseOrderLiquidatedErrorMessage();
            return LIQUIDATED_OUTCOME;
        } catch (EntryDuplicatedException e) {
            addDuplicatedMessage();
            return Outcome.REDISPLAY;
        }
    }

    @BusinessUnitRestriction(value = "#{fixedAssetPurchaseOrderAction.instance}")
    @End
    @Restrict("#{s:hasPermission('SPECIALUPDATEPURCHASEORDER','VIEW')}")
    public String specialUpdate() {
        if (!purchaseOrderValidator.isValidThePurchaseDocuments(getInstance(), true)) {
            return Outcome.REDISPLAY;
        }

        String validationOutcome = validateInputFields();
        if (!Outcome.SUCCESS.equals(validationOutcome)) {
            return validationOutcome;
        }
        try {
            service.specialUpdatePurchaseOrder(getInstance());
            addUpdatedMessage();
        } catch (EntryNotFoundException e) {
            addNotFoundMessage();
            return Outcome.FAIL;
        } catch (ConcurrencyException e) {
            updateCurrentInstance();
            addUpdateConcurrencyMessage();
        } catch (EntryDuplicatedException e) {
            addDuplicatedMessage();
        }
        return Outcome.REDISPLAY;
    }

    @BusinessUnitRestriction(value = "#{fixedAssetPurchaseOrderAction.instance}")
    @End
    @Restrict("#{s:hasPermission('FIXEDASSETPURCHASEORDERAPPROVE','VIEW')}")
    public String approveFixedAssetPurchaseOrder() {
        if (!purchaseOrderValidator.isValidThePurchaseDocuments(getInstance(), true)) {
            return Outcome.REDISPLAY;
        }

        String validationOutcome = validateInputFields();
        if (!Outcome.SUCCESS.equals(validationOutcome)) {
            return validationOutcome;
        }
        if (!validateAssociatedFixedAssetList()) {
            return Outcome.REDISPLAY;
        }
        /*reset lists if they are not necessary */
        if (!getInstance().getPurchaseOrderCause().getRequiresFixedAssets()) {
            selectedFixedAssetList.clear();
            selectedFixedAssetIdList.clear();
            selectedFixedAssetIdList.add(new Long(0));
            fixedAssetStateRestrictionList = new ArrayList<FixedAssetState>();
        }
        try {
            service.approvePurchaseOrder(getInstance(), selectedFixedAssetList);
            addPurchaseOrderApprovedMessage();
            return Outcome.SUCCESS;
        } catch (PurchaseOrderApprovedException e) {
            addPurchaseOrderApprovedErrorMessage();
            return APPROVED_OUTCOME;
        } catch (PurchaseOrderFinalizedException e) {
            addPurchaseOrderFinalizedErrorMessage();
            return FINALIZED_OUTCOME;
        } catch (PurchaseOrderDetailEmptyException e) {
            addPurchaseOrderEmptyMessage();
            return Outcome.REDISPLAY;
        } catch (ConcurrencyException e) {
            updateCurrentInstance();
            addUpdateConcurrencyMessage();
            return Outcome.REDISPLAY;
        } catch (PurchaseOrderNullifiedException e) {
            addPurchaseOrderAnnulledErrorMessage();
            return FINALIZED_OUTCOME;
        } catch (PurchaseOrderLiquidatedException e) {
            addPurchaseOrderLiquidatedErrorMessage();
            return LIQUIDATED_OUTCOME;
        } catch (EntryDuplicatedException e) {
            addDuplicatedMessage();
            return Outcome.REDISPLAY;
        }
    }

    @BusinessUnitRestriction(value = "#{fixedAssetPurchaseOrderAction.instance}")
    @End
    @Restrict("#{s:hasPermission('FIXEDASSETPURCHASEORDERFINALIZE','VIEW')}")
    public String finalizeFixedAssetPurchaseOrder() {
        if (!purchaseOrderValidator.isValidThePurchaseDocuments(getInstance(), true)) {
            return Outcome.REDISPLAY;
        }

        try {
            service.finalizePurchaseOrder(getInstance());
            select(getInstance());
            addPurchaseOrderFinalizedMessage();
            return Outcome.SUCCESS;
        } catch (PurchaseOrderDetailEmptyException e) {
            addPurchaseOrderEmptyMessage();
            return Outcome.REDISPLAY;
        } catch (PurchaseOrderFinalizedException e) {
            addPurchaseOrderFinalizedErrorMessage();
            return FINALIZED_OUTCOME;
        } catch (EntryDuplicatedException e) {
            addDuplicatedMessage();
            return Outcome.REDISPLAY;
        }

    }

    @BusinessUnitRestriction(value = "#{fixedAssetPurchaseOrderAction.instance}")
    @End
    @Restrict("#{s:hasPermission('FIXEDASSETPURCHASEORDERLIQUIDATE','VIEW')}")
    public String liquidateFixedAssetPurchaseOrder() {
        if (!purchaseOrderValidator.isValidThePurchaseDocuments(getInstance(), true)) {
            return Outcome.REDISPLAY;
        }

        if (!checkPayment()) {
            addReComputePaymentRequiredMessage();
            return Outcome.REDISPLAY;
        }
        try {
            service.liquidatePurchaseOrder(getInstance(), getLiquidationPayment());
            addPurchaseOrderLiquidatedMessage();
            return Outcome.SUCCESS;
        } catch (CompanyConfigurationNotFoundException e) {
            addCompanyConfigurationNotFoundErrorMessage();
            return Outcome.REDISPLAY;
        } catch (PurchaseOrderDetailEmptyException e) {
            addPurchaseOrderEmptyMessage();
            return Outcome.REDISPLAY;
        } catch (AdvancePaymentPendingException e) {
            addAdvancePaymentPendingErrorMessage();
            return Outcome.REDISPLAY;
        } catch (FinancesExchangeRateNotFoundException e) {
            addFinancesExchangeRateNotFoundExceptionMessage();
            return Outcome.REDISPLAY;
        } catch (FinancesCurrencyNotFoundException e) {
            addFinancesExchangeRateNotFoundExceptionMessage();
            return Outcome.REDISPLAY;
        } catch (PurchaseOrderLiquidatedException e) {
            addPurchaseOrderLiquidatedErrorMessage();
            return LIQUIDATED_OUTCOME;
        } catch (RotatoryFundConcurrencyException e) {
            liquidationPaymentAction.addRotatoryFundConcurrencyMessage();
            return Outcome.FAIL;
        } catch (CollectionSumExceedsRotatoryFundAmountException e) {
            liquidationPaymentAction.addCollectionSumExceedsRotatoryFundAmountError();
            return Outcome.FAIL;
        } catch (RotatoryFundLiquidatedException e) {
            liquidationPaymentAction.addRotatoryFundLiquidatedError();
            return Outcome.FAIL;
        } catch (RotatoryFundNullifiedException e) {
            liquidationPaymentAction.addRotatoryFundAnnulledError();
            return Outcome.FAIL;
        }
    }


    @BusinessUnitRestriction(value = "#{fixedAssetPurchaseOrderAction.instance}")
    @End
    @Restrict("#{s:hasPermission('FIXEDASSETPURCHASEORDERANNUL','VIEW')}")
    public String annulFixedAssetPurchaseOrder() {
        try {
            service.nullifyPurchaseOrder(getInstance());
            addPurchaseOrderAnnulledMessage();
            return Outcome.SUCCESS;
        } catch (PurchaseOrderFinalizedException e) {
            addPurchaseOrderFinalizedErrorMessage();
            return FINALIZED_OUTCOME;
        } catch (ConcurrencyException e) {
            updateCurrentInstance();
            addUpdateConcurrencyMessage();
            return Outcome.REDISPLAY;
        } catch (PurchaseOrderNullifiedException e) {
            addPurchaseOrderAnnulledErrorMessage();
            return FINALIZED_OUTCOME;
        } catch (PurchaseOrderLiquidatedException e) {
            addPurchaseOrderLiquidatedErrorMessage();
            return LIQUIDATED_OUTCOME;
        }
    }


    /**
     * Validates if the selected fixedAssets states are still valid with the states associated to the purchaseOrderCause in database by event
     * and that its states have not been changed by other users.
     *
     * @return true if all the states of the selected fixed assets are still valid and their states have not been changed by other users
     *         otherwise returns false.
     */
    private boolean validateAssociatedFixedAssetList() {
        boolean result = true;

        if (getInstance().getPurchaseOrderCause().getRequiresFixedAssets() && getInstance().getPurchaseOrderCause().isFixedassetPurchase()) {
            List<FixedAssetState> databaseFixedAssetStateRestrictionList =
                    purchaseOrderCauseFixedAssetStateService.findFixedAssetStateByPurchaseOrderCause(getInstance().getPurchaseOrderCause(), listEm);
            String validStates = "";
            for (FixedAssetState fixedAssetState : databaseFixedAssetStateRestrictionList) {
                validStates += messages.get(fixedAssetState);
            }
            for (FixedAsset fixedAsset : selectedFixedAssetList) {
                /*validating possible changes in fixed asset state*/
                FixedAsset databaseFixedAsset = listEm.find(FixedAsset.class, fixedAsset.getId());
                if (null != databaseFixedAsset) {
                    if (!databaseFixedAsset.getState().equals(fixedAsset.getState())) {
                        result = false;
                        addSelectedFixedAssetStateError(fixedAsset.getFixedAssetCode().toString(), databaseFixedAsset.getState().toString());
                        try {
                            fixedAsset = getService().findById(FixedAsset.class, fixedAsset.getId(), true);
                        } catch (EntryNotFoundException e) {
                            addFixedAssetDeletedByOtherUserErrorMessage(fixedAsset.getFixedAssetCode().toString());
                        }
                    }
                } else {
                    result = false;
                    addFixedAssetDeletedByOtherUserErrorMessage(fixedAsset.getFixedAssetCode().toString());
                }
                /*validating that selectedFixedAssetList to be in fixedAssetStateRestrictionList*/
                if (!databaseFixedAssetStateRestrictionList.contains(fixedAsset.getState())) {
                    result = false;
                    addSelectedFixedAssetStatesError(fixedAsset.getFixedAssetCode().toString(), validStates);
                }
            }
        }
        return result;
    }

    @SuppressWarnings({"UnusedAssignment", "UnnecessaryUnboxing"})
    private boolean validatePurchaseOrderDetail() {
        boolean valid = true;
        if (!isManaged()) {
            if (getInstance().getPurchaseOrderCause().isFixedassetPurchase()) {
                if (!(valid = detailListCreateAction.getDetailListRowCounter().intValue() > 0)) {
                    addRequiredDetailErrorMessage();
                }
            } else if (getInstance().getPurchaseOrderCause().isFixedassetPartsPurchase() && !(valid = fixedAssetPartsCreateAction.getInstancesRowCounter().intValue() > 0)) {
                addRequiredFixedAssetPartErrorMessage();
            }
        } else {
            if (getInstance().getPurchaseOrderCause().isFixedassetPurchase()) {
                if (!(valid = detailListCreateAction.getDetailListRowCounter().intValue() > 0)) {
                    addRequiredDetailErrorMessage();
                }
            } else if (getInstance().getPurchaseOrderCause().isFixedassetPartsPurchase() && !(valid = fixedAssetPartsCreateAction.getInstancesRowCounter().intValue() > 0)) {
                addRequiredFixedAssetPartErrorMessage();
            }
        }
        return valid;
    }

    @SuppressWarnings(value = "unchecked")
    public void addFixedAssetList(List<Long> fixedAssetIdList) {

        if (fixedAssetIdList != null) {
            List<FixedAsset> fixedAssetList = QueryUtils.selectAllIn(em, FixedAsset.class, fixedAssetIdList).getResultList();
            for (FixedAsset fixedAssetItem : fixedAssetList) {
                if (!getSelectedFixedAssetList().contains(fixedAssetItem)) {
                    getSelectedFixedAssetList().add(fixedAssetItem);
                    getSelectedFixedAssetIdList().add(fixedAssetItem.getId());
                }
            }
        }
    }

    public void removeInstance(FixedAsset fixedAssetItem) {
        FixedAsset fixedAsset;
        try {
            fixedAsset = getService().findById(FixedAsset.class, fixedAssetItem.getId());
            if (getSelectedFixedAssetList().contains(fixedAsset)) {
                getSelectedFixedAssetList().remove(fixedAsset);
                getSelectedFixedAssetIdList().remove(fixedAsset.getId());
            }
        } catch (EntryNotFoundException e) {
            log.error(e, "Entry not found");
        }
    }

    public PurchaseOrderPayment getLiquidationPayment() {
        return hasBalanceAmount() ? liquidationPaymentAction.getLiquidationPayment() : null;
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

    public void assignProvider(Provider provider) {
        getInstance().setProvider(provider);
    }

    public void clearProvider() {
        getInstance().setProvider(null);
    }

    public String getResponsibleFullName() {
        if (getInstance().getResponsible() == null) {
            getInstance().setResponsible(currentUser.getEmployee());
        }
        return getInstance().getResponsible().getFullName();
    }

    public void assignPetitionerJobContract(JobContract jobContract) {
        getInstance().setPetitionerJobContract(jobContract);
        loadPetitionerJobContractValues();
    }

    public void loadPetitionerJobContractValues() {
        if (getInstance().getPetitionerJobContract() != null) {
            getInstance().setPetitionerJobContract(jobContractService.load(getInstance().getPetitionerJobContract()));
            getInstance().setExecutorUnit(getInstance().getPetitionerJobContract().getJob().getOrganizationalUnit().getBusinessUnit());
            getInstance().setCostCenter(getInstance().getPetitionerJobContract().getJob().getOrganizationalUnit().getCostCenter());
        }
    }

    public void clearPetitionerJobContract() {
        getInstance().setPetitionerJobContract(null);
        getInstance().setExecutorUnit(null);
        getInstance().setCostCenter(null);
    }

    public boolean isPurchaseOrderApproved() {
        return isManaged() && getInstance().isPurchaseOrderApproved();
    }

    public boolean isPurchaseOrderPending() {
        return !isManaged() || getInstance().isPurchaseOrderPending();
    }

    public boolean isPurchaseOrderFinalized() {
        return isManaged() && getInstance().isPurchaseOrderFinalized();
    }

    public boolean isPurchaseOrderLiquidated() {
        return isManaged() && getInstance().isPurchaseOrderLiquidated();
    }

    public boolean isPurchaseOrderNullified() {
        return isManaged() && getInstance().isPurchaseOrderNullified();
    }

    public boolean checkPayment() {
        return getLiquidationPayment() == null || liquidationPaymentAction.checkPayment(getCurrentBalanceAmount());
    }

    public void purchaseOrderCauseChanged() {
        fixedAssetStateRestrictionList = purchaseOrderCauseFixedAssetStateService.findFixedAssetStateByPurchaseOrderCause(getInstance().getPurchaseOrderCause(), null);
    }

    public List<FixedAsset> getSelectedFixedAssetList() {
        if (!isManaged() && null == selectedFixedAssetList) {
            selectedFixedAssetList = new ArrayList<FixedAsset>();
        }
        return selectedFixedAssetList;
    }

    public void setSelectedFixedAssetList(List<FixedAsset> selectedFixedAssetList) {
        this.selectedFixedAssetList = selectedFixedAssetList;
    }

    public List<FixedAssetState> getFixedAssetStateRestrictionList() {
        return fixedAssetStateRestrictionList;
    }

    public void setFixedAssetStateRestrictionList(List<FixedAssetState> fixedAssetStateRestrictionList) {
        this.fixedAssetStateRestrictionList = fixedAssetStateRestrictionList;
    }

    public List<Long> getSelectedFixedAssetIdList() {
        if (selectedFixedAssetIdList.isEmpty()) {
            selectedFixedAssetIdList.add((long) 0);
        }
        return selectedFixedAssetIdList;
    }

    public void setSelectedFixedAssetIdList(List<Long> selectedFixedAssetIdList) {
        this.selectedFixedAssetIdList = selectedFixedAssetIdList;
    }

    public boolean isEnableContractInfo() {
        return getInstance().getPetitionerJobContract() != null;
    }

    /* messages*/

    private void addFinancesExchangeRateNotFoundExceptionMessage() {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.INFO,
                "PurchaseOrder.financesExchangeRateNotFound");
    }

    public void addPurchaseOrderApprovedErrorMessage() {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                "PurchaseOrder.error.purchaseOrderAlreadyApproved", getInstance().getOrderNumber());
    }

    public void addPurchaseOrderFinalizedErrorMessage() {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                "PurchaseOrder.error.purchaseOrderAlreadyFinalized", getInstance().getOrderNumber());
    }

    private void addPurchaseOrderLiquidatedMessage() {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.INFO,
                "PurchaseOrder.liquidateMessage", getInstance().getOrderNumber());
    }

    public void addPurchaseOrderLiquidatedErrorMessage() {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                "PurchaseOrder.error.purchaseOrderAlreadyLiquidated", getInstance().getOrderNumber());
    }

    public void addPurchaseOrderEmptyMessage() {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                "PurchaseOrder.error.purchaseOrderDetailEmpty", getInstance().getOrderNumber());
    }

    private void addPurchaseOrderApprovedMessage() {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.INFO,
                "PurchaseOrder.approveMessage", getInstance().getOrderNumber());
    }

    private void addPurchaseOrderFinalizedMessage() {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.INFO,
                "PurchaseOrder.finalizeMessage", getInstance().getOrderNumber());
    }

    @Override
    protected void addCreatedMessage() {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.INFO,
                "PurchaseOrder.message.created", getInstance().getOrderNumber());
    }

    @Override
    protected void addUpdatedMessage() {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.INFO,
                "PurchaseOrder.message.updated", getInstance().getOrderNumber());
    }

    private void addPurchaseOrderAnnulledMessage() {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.INFO,
                "FixedAssetPurchaseOrder.annulledMessage", getInstance().getOrderNumber());
    }

    public void addPurchaseOrderAnnulledErrorMessage() {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                "FixedAssetPurchaseOrder.error.purchaseOrderAlreadyAnnulled", getInstance().getOrderNumber());
    }

    private void addReComputePaymentRequiredMessage() {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR, "PurchaseOrder.error.reComputePaymentRequired");
    }

    private void addAdvancePaymentPendingErrorMessage() {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                "PurchaseOrder.error.purchaseOrderPaymentPending", getInstance().getOrderNumber());
    }

    private void addSelectedFixedAssetStatesError(String code, String validStates) {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                "FixedAssetPurchaseOrder.selectedFixedAssetStatesInvalid", code, validStates);
    }

    private void addSelectedFixedAssetStateError(String code, String newState) {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                "FixedAssetPurchaseOrder.selectedFixedAssetStateInvalid", code, newState);
    }

    private void addFixedAssetDeletedByOtherUserErrorMessage(String code) {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.WARN,
                "Common.error.notFound", code);
    }

    private void addRequiredDetailErrorMessage() {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                "FixedAssetPurchaseOrder.error.requiredDetail");
    }

    private void addRequiredFixedAssetPartErrorMessage() {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                "FixedAssetPurchaseOrder.error.requiredFixedAssetPart");
    }


    public BigDecimal getCurrentBalanceAmount() {
        return service.currentBalanceAmount(getInstance());
    }

    public Boolean hasBalanceAmount() {
        return getCurrentBalanceAmount().compareTo(BigDecimal.ZERO) > 0;
    }

    @Override
    protected GenericService getService() {
        return service;
    }

    public PurchaseOrder getPurchaseOrder() {
        return getInstance();
    }
    /* In order to require the input for this fields which are modal panel components
* so we can´t apply the required tag for them*/

    private String validateInputFields() {
        String outcome = Outcome.SUCCESS;
        if (null == getInstance().getProvider()) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                    "Common.required", MessageUtils.getMessage("FixedAssetPurchaseOrder.provider"));
            outcome = Outcome.REDISPLAY;
        }

        if (null == getInstance().getResponsible()) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                    "Common.required", MessageUtils.getMessage("FixedAssetPurchaseOrder.responsible"));
            outcome = Outcome.REDISPLAY;
        }

        if (null == getInstance().getCostCenterCode() || "".equals(getInstance().getCostCenterCode().trim())) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                    "Common.required", MessageUtils.getMessage("FixedAssetPurchaseOrder.costCenter"));
            outcome = Outcome.REDISPLAY;
        }
        if (null == getInstance().getPetitionerJobContract()) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                    "Common.required", MessageUtils.getMessage("FixedAssetPurchaseOrder.petitioner"));
            outcome = Outcome.REDISPLAY;
        }
        return outcome;
    }

    public String getActiveTabName() {
        return activeTabName;
    }

    public void setActiveTabName(String activeTabName) {
        this.activeTabName = activeTabName;
    }

    public void enablePurchaseOrderDetailTab() {
        setActiveTabName("detailPurchaseOrderTab");
    }

    public void enablePurchaseOrderPaymentTab() {
        setActiveTabName("purchaseOrderPaymentTab");
    }

    @Override
    public void addNotFoundMessage() {
        super.addNotFoundMessage();
    }
}
