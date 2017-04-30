package com.encens.khipus.service.fixedassets;

import com.encens.khipus.exception.ConcurrencyException;
import com.encens.khipus.exception.EntryDuplicatedException;
import com.encens.khipus.exception.finances.CompanyConfigurationNotFoundException;
import com.encens.khipus.exception.finances.FinancesCurrencyNotFoundException;
import com.encens.khipus.exception.finances.FinancesExchangeRateNotFoundException;
import com.encens.khipus.exception.purchase.*;
import com.encens.khipus.exception.warehouse.AdvancePaymentPendingException;
import com.encens.khipus.model.finances.*;
import com.encens.khipus.model.fixedassets.FixedAsset;
import com.encens.khipus.model.fixedassets.FixedAssetState;
import com.encens.khipus.model.fixedassets.PurchaseOrderFixedAssetPart;
import com.encens.khipus.model.fixedassets.PurchaseOrdersFixedAssetCollection;
import com.encens.khipus.model.purchases.*;
import com.encens.khipus.service.common.SequenceGeneratorService;
import com.encens.khipus.service.finances.FinanceAccountingDocumentService;
import com.encens.khipus.service.finances.FinancesExchangeRateService;
import com.encens.khipus.service.finances.RotatoryFundCollectionService;
import com.encens.khipus.service.finances.VoucherService;
import com.encens.khipus.service.purchases.GlossGeneratorService;
import com.encens.khipus.service.purchases.PurchaseOrderNumberGeneratorService;
import com.encens.khipus.service.purchases.PurchaseOrderServiceBean;
import com.encens.khipus.service.warehouse.AdvancePaymentService;
import com.encens.khipus.service.warehouse.MonthProcessService;
import com.encens.khipus.service.warehouse.WarehouseAccountEntryService;
import com.encens.khipus.util.*;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceException;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * @author
 * @version 2.26
 */
@Stateless
@Name("fixedAssetPurchaseOrderService")
@AutoCreate
public class FixedAssetPurchaseOrderServiceBean extends PurchaseOrderServiceBean implements FixedAssetPurchaseOrderService {
    @In(value = "#{listEntityManager}")
    private EntityManager listEm;

    @In
    private FixedAssetService fixedAssetService;

    @In(value = "fixedAssetPurchaseOrderDetailService")
    private FixedAssetPurchaseOrderDetailService purchaseOrderDetailService;

    @In(value = "monthProcessService")
    private MonthProcessService monthProcessService;

    @In
    private SequenceGeneratorService sequenceGeneratorService;

    @In
    private VoucherService voucherService;

    @In
    private AdvancePaymentService advancePaymentService;

    @In
    private GlossGeneratorService glossGeneratorService;

    @In
    private WarehouseAccountEntryService warehouseAccountEntryService;

    @In
    private FinancesExchangeRateService financesExchangeRateService;

    @In
    private FixedAssetPartService fixedAssetPartService;

    @In
    CompanyConfigurationService companyConfigurationService;

    @In
    PurchaseOrderNumberGeneratorService purchaseOrderNumberGeneratorService;

    @In
    private RotatoryFundCollectionService rotatoryFundCollectionService;

    @In
    private PurchaseOrdersFixedAssetCollectionService purchaseOrdersFixedAssetCollectionService;

    @In
    private FinanceAccountingDocumentService financeAccountingDocumentService;

    @In
    private PurchaseOrderFixedAssetPartService purchaseOrderFixedAssetPartService;

    @Override
    public void create(Object entity) throws EntryDuplicatedException {
        try {
            PurchaseOrder purchaseOrder = (PurchaseOrder) entity;

            String orderNumber = null;
            if (companyConfigurationService.isPurchaseOrderCodificationEnabled()) {
                orderNumber = purchaseOrderNumberGeneratorService.generatePurchaseOrderNumber(purchaseOrder);
            } else {
                orderNumber = String.valueOf(getNextOrderNumber());
            }

            purchaseOrder.setOrderNumber(orderNumber);
            purchaseOrder.setDate(new Date());
            purchaseOrder.setState(PurchaseOrderState.PEN);
            purchaseOrder.setPaymentStatus(PurchaseOrderPaymentStatus.NO_PAYMENT);
            purchaseOrder.setBalanceAmount(BigDecimal.ZERO);

            getEntityManager().persist(entity);
            getEntityManager().flush();
        } catch (PersistenceException e) {
            throw new EntryDuplicatedException();
        } catch (CompanyConfigurationNotFoundException ignored) {
        }
    }


    public void create(Object entity, List<FixedAssetPurchaseOrderDetail> details, List<FixedAsset> fixedAssetList)
            throws EntryDuplicatedException {
        create(entity);
        PurchaseOrder purchaseOrder = (PurchaseOrder) entity;
        /*creation of PurchaseOrdersFixedAssetCollection elements*/
        if (purchaseOrder.getPurchaseOrderCause().getRequiresFixedAssets()) {
            try {
                for (FixedAsset fixedAsset : fixedAssetList) {
                    PurchaseOrdersFixedAssetCollection purchaseOrdersFixedAssetCollection = new PurchaseOrdersFixedAssetCollection();
                    purchaseOrdersFixedAssetCollection.setFixedAsset(fixedAsset);
                    purchaseOrdersFixedAssetCollection.setPurchaseOrder(purchaseOrder);

                    getEntityManager().persist(purchaseOrdersFixedAssetCollection);
                }
                getEntityManager().flush();
            } catch (PersistenceException e) {
                log.debug("Persistence error..", e);
                throw new EntryDuplicatedException();
            }

        }
        for (FixedAssetPurchaseOrderDetail detail : details) {
            try {
                purchaseOrderDetailService.createFixedAssetPurchaseOrderDetail(detail,
                        (PurchaseOrder) entity,
                        detail.getOrderDetailPartList());
            } catch (PurchaseOrderApprovedException e) {
                //this exception never happen because the purchase order ist created
            } catch (PurchaseOrderFinalizedException e) {
                //this exception never happen because the purchase order ist created
            } catch (PurchaseOrderNullifiedException e) {
                //this exception never happen because the purchase order ist created
            } catch (ConcurrencyException e) {
                //this exception never happen because the purchase order ist created
            } catch (PurchaseOrderLiquidatedException e) {
                //this exception never happen because the purchase order ist created
            }
        }
    }


    public void create(PurchaseOrder purchaseOrder, List<PurchaseOrderFixedAssetPart> fixedAssetPartList)
            throws EntryDuplicatedException {
        create(purchaseOrder);
        for (PurchaseOrderFixedAssetPart fixedAssetPart : fixedAssetPartList) {
            fixedAssetPart.setPurchaseOrder(purchaseOrder);
            getEntityManager().persist(fixedAssetPart);
        }
        getEntityManager().flush();
        purchaseOrder = updateTotalAmountFields(purchaseOrder);
        getEntityManager().merge(purchaseOrder);
        getEntityManager().flush();
        getEntityManager().refresh(purchaseOrder);
    }

    public Long getNextOrderNumber() {
        Long orderNumber = (Long) getEntityManager().createNamedQuery("PurchaseOrder.countByCompanyNumberAndType")
                .setParameter("companyNumber", Constants.defaultCompanyNumber)
                .setParameter("orderType", PurchaseOrderType.FIXEDASSET)
                .getSingleResult();
        if (null == orderNumber) {
            orderNumber = (long) 1;
        } else {
            orderNumber++;
        }
        return orderNumber;
    }

    public void finalizePurchaseOrder(PurchaseOrder purchaseOrder)
            throws PurchaseOrderDetailEmptyException,
            PurchaseOrderFinalizedException,
            EntryDuplicatedException {

        if (isPurchaseOrderFinalized(purchaseOrder)) {
            findPurchaseOrder(purchaseOrder.getId());
            throw new PurchaseOrderFinalizedException(
                    "The purchase order was already finalized, and cannot be changed");
        }

        if (isPurchaseOrderEmpty(purchaseOrder)) {
            throw new PurchaseOrderDetailEmptyException("The purchase order detail cannot be empty");
        }

        purchaseOrder = updateTotalAmountFields(purchaseOrder);
        purchaseOrder.setState(PurchaseOrderState.FIN);
        purchaseOrder.setReceivedType(PurchaseOrderReceivedType.RT);
        getEntityManager().merge(purchaseOrder);
        getEntityManager().flush();

        if (purchaseOrder.getPurchaseOrderCause().isFixedassetPurchase()) {
            List<FixedAssetPurchaseOrderDetail> fixedAssetPurchaseOrderDetailList =
                    purchaseOrderDetailService.getFixedAssetPurchaseOrderDetailList(purchaseOrder);
            createFixedAssetsForPurchaseOrder(purchaseOrder, fixedAssetPurchaseOrderDetailList);
        }
    }

    public void liquidatePurchaseOrder(PurchaseOrder purchaseOrder, PurchaseOrderPayment purchaseOrderPayment)
            throws AdvancePaymentPendingException,
            PurchaseOrderLiquidatedException,
            PurchaseOrderDetailEmptyException,
            CompanyConfigurationNotFoundException,
            FinancesCurrencyNotFoundException,
            FinancesExchangeRateNotFoundException,
            RotatoryFundNullifiedException,
            RotatoryFundLiquidatedException,
            CollectionSumExceedsRotatoryFundAmountException,
            RotatoryFundConcurrencyException {

        if (existsPendingAdvancePayments(purchaseOrder)) {
            throw new AdvancePaymentPendingException("The purchase order contain pending advance payments.");
        }

        if (isPurchaseOrderLiquidated(purchaseOrder)) {
            findPurchaseOrder(purchaseOrder.getId());
            throw new PurchaseOrderLiquidatedException(
                    "The purchase order was already liquidated, and cannot be changed");
        }

        if (isPurchaseOrderEmpty(purchaseOrder)) {
            throw new PurchaseOrderDetailEmptyException("The purchase order detail cannot be empty");
        }

        BigDecimal defaultExchangeRate = null;

        if (purchaseOrderPayment != null && !BigDecimalUtil.isZeroOrNull(purchaseOrderPayment.getPayAmount())
                && !BigDecimalUtil.isZeroOrNull(purchaseOrderPayment.getSourceAmount())) {
            purchaseOrderPayment.setPurchaseOrderPaymentKind(PurchaseOrderPaymentKind.LIQUIDATION_PAYMENT);
            defaultExchangeRate = purchaseOrderPayment.getExchangeRate();

            warehouseAccountEntryService.createEntryAccountForPurchaseOrderPayment(purchaseOrder, purchaseOrderPayment);
            if (PurchaseOrderPaymentType.PAYMENT_ROTATORY_FUND.equals(purchaseOrderPayment.getPaymentType())) {
                try {
                    rotatoryFundCollectionService.generateCollectionForPurchaseOrderPayment(purchaseOrderPayment);
                } catch (com.encens.khipus.exception.finances.RotatoryFundNullifiedException e) {
                    throw new com.encens.khipus.exception.purchase.RotatoryFundNullifiedException(e);
                } catch (com.encens.khipus.exception.finances.RotatoryFundLiquidatedException e) {
                    throw new com.encens.khipus.exception.purchase.RotatoryFundLiquidatedException(e);
                } catch (com.encens.khipus.exception.finances.CollectionSumExceedsRotatoryFundAmountException e) {
                    throw new com.encens.khipus.exception.purchase.CollectionSumExceedsRotatoryFundAmountException(e);
                } catch (ConcurrencyException e) {
                    throw new com.encens.khipus.exception.purchase.RotatoryFundConcurrencyException(e);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }
        createFixedAssetPurchaseOrderLiquidationAccountEntry(purchaseOrder, defaultExchangeRate);

        purchaseOrder.setState(PurchaseOrderState.LIQ);
        purchaseOrder.setBalanceAmount(BigDecimal.ZERO);
        purchaseOrder.setPaymentStatus(PurchaseOrderPaymentStatus.FULLY_PAID);
        purchaseOrder = getEntityManager().merge(purchaseOrder);
        getEntityManager().flush();

        financeAccountingDocumentService.createAccountingVoucherByPurchaseOrder(purchaseOrder);
    }

    private void createFixedAssetsForPurchaseOrder(PurchaseOrder purchaseOrder, List<FixedAssetPurchaseOrderDetail> fixedAssetPurchaseOrderDetailList) throws EntryDuplicatedException {
        BigDecimal operationFactor = BigDecimal.ONE;
        if (CollectionDocumentType.INVOICE.equals(purchaseOrder.getDocumentType())) {
            operationFactor = Constants.VAT_COMPLEMENT;
        }

        for (FixedAssetPurchaseOrderDetail fixedAssetPurchaseOrderDetail : fixedAssetPurchaseOrderDetailList) {
            Integer requestedQuantity = fixedAssetPurchaseOrderDetail.getRequestedQuantity();

            for (int i = 0; i < requestedQuantity; i++) {
                FixedAsset fixedAsset = new FixedAsset();
                fixedAsset.setState(FixedAssetState.PEN);
                fixedAsset.setCurrencyType(FinancesCurrencyType.U);
                fixedAsset.setAcumulatedDepreciation(BigDecimal.ZERO);
                fixedAsset.setImprovement(BigDecimal.ZERO);
                fixedAsset.setDepreciation(BigDecimal.ZERO);

                /* the FixedAsset have to hold only the ufv value and its rate*/
                fixedAsset.setUfvOriginalValue(BigDecimalUtil.multiply(operationFactor, fixedAssetPurchaseOrderDetail.getUfvUnitPriceValue()));
                fixedAsset.setBsOriginalValue(BigDecimalUtil.multiply(operationFactor, fixedAssetPurchaseOrderDetail.getBsUnitPriceValue()));
                fixedAsset.setSusOriginalValue(BigDecimalUtil.multiply(operationFactor, fixedAssetPurchaseOrderDetail.getSusUnitPriceValue()));
                /*save the rates*/
                fixedAsset.setBsUfvRate(fixedAssetPurchaseOrderDetail.getBsUfvRate());
                fixedAsset.setBsSusRate(fixedAssetPurchaseOrderDetail.getBsSusRate());
                fixedAsset.setLastBsSusRate(fixedAsset.getBsSusRate());
                fixedAsset.setLastBsUfvRate(fixedAsset.getBsUfvRate());
                fixedAsset.setCostCenter(fixedAssetPurchaseOrderDetail.getPurchaseOrder().getCostCenter());
                fixedAsset.setBusinessUnit(fixedAssetPurchaseOrderDetail.getPurchaseOrder().getExecutorUnit());
                fixedAsset.setDepreciationRate(fixedAssetPurchaseOrderDetail.getFixedAssetSubGroup().getDepreciationRate());
                fixedAsset.setDescription(fixedAssetPurchaseOrderDetail.getPurchaseOrder().getGloss());
                fixedAsset.setDetail(fixedAssetPurchaseOrderDetail.getDetail());
                fixedAsset.setFixedAssetSubGroup(fixedAssetPurchaseOrderDetail.getFixedAssetSubGroup());
                fixedAsset.setDuration(fixedAssetPurchaseOrderDetail.getNetDuration());
                fixedAsset.setMonthsGuaranty(fixedAssetPurchaseOrderDetail.getMonthsGuaranty());
                fixedAsset.setMeasurement(fixedAssetPurchaseOrderDetail.getMeasurement());
                fixedAsset.setModel(fixedAssetPurchaseOrderDetail.getModel());
                fixedAsset.setPurchaseOrder(fixedAssetPurchaseOrderDetail.getPurchaseOrder());
                fixedAsset.setRubbish(fixedAssetPurchaseOrderDetail.getRubbish());
                fixedAsset.setTrademark(fixedAssetPurchaseOrderDetail.getTrademark());

                try {
                    //this is gonna persist the entity if it is not managed and if for some reason the entity was already added
                    // to the entity manager (duplicated exceptions), the next time it will persist the entity
                    getEntityManager().persist(fixedAsset);
                    getEntityManager().flush();
                    fixedAssetPartService.createFixedAssetParts(fixedAsset, fixedAssetPurchaseOrderDetail);
                } catch (PersistenceException e) { //TODO when hibernate will fix this http://opensource.atlassian.com/projects/hibernate/browse/EJB-382, we have to restore EntityExistsException here.
                    log.debug("Persistence error..", e);
                    e.printStackTrace();
                    throw new EntryDuplicatedException();
                }
            }
        }
    }

    public void updateFixedAssetPurchaseOrder(PurchaseOrder entity, List<FixedAsset> selectedFixedAssetList)
            throws PurchaseOrderApprovedException,
            PurchaseOrderFinalizedException,
            ConcurrencyException,
            PurchaseOrderNullifiedException,
            PurchaseOrderLiquidatedException, EntryDuplicatedException {
        PurchaseOrder dataBasePurchaseOrder = findInDataBase(entity.getId());

        if (canChangePurchaseOrder(dataBasePurchaseOrder)) {
            entity = updateTotalAmountFields(entity);
            getEntityManager().merge(entity);
            getEntityManager().flush();
            getEntityManager().refresh(entity);

            PurchaseOrder purchaseOrder = (PurchaseOrder) entity;
            if (null != selectedFixedAssetList) {
                if (purchaseOrder.getPurchaseOrderCause().getRequiresFixedAssets()) {
                    /*creation of PurchaseOrdersFixedAssetCollection elements that aren't persistent */
                    try {
                        for (FixedAsset fixedAsset : selectedFixedAssetList) {
                            PurchaseOrdersFixedAssetCollection purchaseOrdersFixedAssetCollection = purchaseOrdersFixedAssetCollectionService.findByPurchaseOrderAndFixedAsset(purchaseOrder, fixedAsset, listEm);
                            if (null == purchaseOrdersFixedAssetCollection) {
                                PurchaseOrdersFixedAssetCollection newPurchaseOrdersFixedAssetCollection = new PurchaseOrdersFixedAssetCollection();
                                newPurchaseOrdersFixedAssetCollection.setFixedAsset(fixedAsset);
                                newPurchaseOrdersFixedAssetCollection.setPurchaseOrder(purchaseOrder);
                                getEntityManager().persist(newPurchaseOrdersFixedAssetCollection);
                            }
                        }
                        getEntityManager().flush();
                    } catch (PersistenceException e) {
                        log.debug("Persistence error..", e);
                        throw new EntryDuplicatedException();
                    }
                    /*delete PurchaseOrdersFixedAssetCollection elements that are persistent and aren't in the selectedFixedAssetList */
                    List<PurchaseOrdersFixedAssetCollection> purchaseOrdersFixedAssetCollectionList = purchaseOrdersFixedAssetCollectionService.findByPurchaseOrder(purchaseOrder, listEm);
                    for (PurchaseOrdersFixedAssetCollection purchaseOrdersFixedAssetCollection : purchaseOrdersFixedAssetCollectionList) {
                        FixedAsset fixedAsset = getEntityManager().find(FixedAsset.class, purchaseOrdersFixedAssetCollection.getFixedAsset().getId());
                        if (!selectedFixedAssetList.contains(fixedAsset)) {
                            /*delete item*/
                            listEm.remove(purchaseOrdersFixedAssetCollection);
                            listEm.flush();
                        }
                    }
                } else {
                    /*drop elements if any*/
                    List<PurchaseOrdersFixedAssetCollection> purchaseOrdersFixedAssetCollectionList = purchaseOrdersFixedAssetCollectionService.findByPurchaseOrder(purchaseOrder, listEm);
                    for (PurchaseOrdersFixedAssetCollection purchaseOrdersFixedAssetCollection : purchaseOrdersFixedAssetCollectionList) {
                        listEm.remove(purchaseOrdersFixedAssetCollection);
                    }
                    listEm.flush();
                }
            }
        }
    }

    public void approvePurchaseOrder(PurchaseOrder entity, List<FixedAsset> selectedFixedAssetList)
            throws PurchaseOrderApprovedException,
            PurchaseOrderFinalizedException,
            PurchaseOrderDetailEmptyException,
            ConcurrencyException,
            PurchaseOrderNullifiedException,
            PurchaseOrderLiquidatedException, EntryDuplicatedException {
        PurchaseOrder dataBasePurchaseOrder = findInDataBase(entity.getId());

        if (canChangePurchaseOrder(dataBasePurchaseOrder)) {
            if (isPurchaseOrderEmpty(entity)) {
                throw new PurchaseOrderDetailEmptyException("The purchase order detail cannot be empty");
            }

            entity = updateTotalAmountFields(entity);
            entity.setState(PurchaseOrderState.APR);
            if (!getEntityManager().contains(entity)) {
                getEntityManager().merge(entity);
            }
            getEntityManager().flush();

            PurchaseOrder purchaseOrder = (PurchaseOrder) entity;
            if (null != selectedFixedAssetList) {
                if (purchaseOrder.getPurchaseOrderCause().getRequiresFixedAssets()) {
                    /*creation of PurchaseOrdersFixedAssetCollection elements that aren't persistent */
                    try {
                        for (FixedAsset fixedAsset : selectedFixedAssetList) {
                            PurchaseOrdersFixedAssetCollection purchaseOrdersFixedAssetCollection = purchaseOrdersFixedAssetCollectionService.findByPurchaseOrderAndFixedAsset(purchaseOrder, fixedAsset, listEm);
                            if (null == purchaseOrdersFixedAssetCollection) {
                                PurchaseOrdersFixedAssetCollection newPurchaseOrdersFixedAssetCollection = new PurchaseOrdersFixedAssetCollection();
                                newPurchaseOrdersFixedAssetCollection.setFixedAsset(fixedAsset);
                                newPurchaseOrdersFixedAssetCollection.setPurchaseOrder(purchaseOrder);
                                getEntityManager().persist(newPurchaseOrdersFixedAssetCollection);
                            }
                        }
                        getEntityManager().flush();
                    } catch (PersistenceException e) {
                        log.debug("Persistence error..", e);
                        throw new EntryDuplicatedException();
                    }
                    /*delete PurchaseOrdersFixedAssetCollection elements that are persistent and aren't in the selectedFixedAssetList */
                    List<PurchaseOrdersFixedAssetCollection> purchaseOrdersFixedAssetCollectionList = purchaseOrdersFixedAssetCollectionService.findByPurchaseOrder(purchaseOrder, listEm);
                    for (PurchaseOrdersFixedAssetCollection purchaseOrdersFixedAssetCollection : purchaseOrdersFixedAssetCollectionList) {
                        FixedAsset fixedAsset = getEntityManager().find(FixedAsset.class, purchaseOrdersFixedAssetCollection.getFixedAsset().getId());
                        if (!selectedFixedAssetList.contains(fixedAsset)) {
                            /*delete item*/
                            listEm.remove(purchaseOrdersFixedAssetCollection);
                            listEm.flush();
                        }
                    }

                } else {
                    /*drop elements if any*/
                    List<PurchaseOrdersFixedAssetCollection> purchaseOrdersFixedAssetCollectionList = purchaseOrdersFixedAssetCollectionService.findByPurchaseOrder(purchaseOrder, listEm);
                    for (PurchaseOrdersFixedAssetCollection purchaseOrdersFixedAssetCollection : purchaseOrdersFixedAssetCollectionList) {
                        listEm.remove(purchaseOrdersFixedAssetCollection);
                    }
                    listEm.flush();
                }
            }
        }
    }

    public PurchaseOrder updateTotalAmountFields(PurchaseOrder purchaseOrder) {
        BigDecimal netTotalAmount = calculateFixedAssetPurchaseOrderNetTotalAmount(purchaseOrder);
        BigDecimal discountPercentage = purchaseOrder.getDiscountPercent();
        BigDecimal discountAmount = BigDecimal.ZERO;
        if (BigDecimalUtil.isPositive(discountPercentage) && BigDecimalUtil.isPositive(netTotalAmount)) {
            discountAmount = BigDecimalUtil.multiply(
                    netTotalAmount,
                    BigDecimalUtil.divide(discountPercentage, BigDecimalUtil.ONE_HUNDRED, 7));
        }

        BigDecimal totalAmount = BigDecimalUtil.subtract(netTotalAmount, discountAmount);
        purchaseOrder.setSubTotalAmount(BigDecimalUtil.roundBigDecimal(netTotalAmount));
        purchaseOrder.setTotalAmount(BigDecimalUtil.roundBigDecimal(totalAmount));
        purchaseOrder.setDiscountPercent(BigDecimalUtil.roundBigDecimal(discountPercentage));
        purchaseOrder.setDiscountAmount(BigDecimalUtil.roundBigDecimal(discountAmount));
        purchaseOrder.setBalanceAmount(totalAmount);
        purchaseOrder.setPaymentStatus(PurchaseOrderPaymentStatus.NO_PAYMENT);
        return purchaseOrder;
    }

    private BigDecimal calculateFixedAssetPurchaseOrderNetTotalAmount(PurchaseOrder purchaseOrder) {
        BigDecimal total = BigDecimal.ZERO;
        if (purchaseOrder.getPurchaseOrderCause().isFixedassetPurchase()) {
            total = (BigDecimal) getEntityManager()
                    .createNamedQuery("FixedAssetPurchaseOrderDetail.sumBsTotalAmounts")
                    .setParameter("purchaseOrder", purchaseOrder)
                    .getSingleResult();
        } else if (purchaseOrder.getPurchaseOrderCause().isFixedassetPartsPurchase()) {
            total = (BigDecimal) getEntityManager()
                    .createNamedQuery("PurchaseOrderFixedAssetPart.sumBsTotalAmounts")
                    .setParameter("purchaseOrder", purchaseOrder)
                    .getSingleResult();
        }
        if (null == total) {
            total = BigDecimal.ZERO;
        }

        return total;
    }

    @Override
    protected Boolean isPurchaseOrderEmpty(PurchaseOrder purchaseOrder) {
        return purchaseOrder.getPurchaseOrderCause().isFixedassetPurchase() ? purchaseOrderDetailService.isPurchaseOrderDetailEmpty(purchaseOrder) :
                purchaseOrderFixedAssetPartService.isPurchaseOrderFixedAssetPartEmpty(purchaseOrder);
    }
    /* fixed Asset liquidation case */

    public void createFixedAssetPurchaseOrderLiquidationAccountEntry(PurchaseOrder purchaseOrder, BigDecimal defaultExchangeRate)
            throws CompanyConfigurationNotFoundException, FinancesCurrencyNotFoundException, FinancesExchangeRateNotFoundException {

        BigDecimal sumAdvancePaymentAmount = advancePaymentService.sumAllPaymentAmountsByKind(purchaseOrder, PurchaseOrderPaymentKind.ADVANCE_PAYMENT);
        BigDecimal sumLiquidationPaymentAmount = advancePaymentService.sumAllPaymentAmountsByKind(purchaseOrder, PurchaseOrderPaymentKind.LIQUIDATION_PAYMENT);

        if (BigDecimalUtil.isZeroOrNull(sumAdvancePaymentAmount)) {
            sumAdvancePaymentAmount = BigDecimal.ZERO;
        }

        if (BigDecimalUtil.isZeroOrNull(sumLiquidationPaymentAmount)) {
            sumLiquidationPaymentAmount = BigDecimal.ZERO;
        }

        CompanyConfiguration companyConfiguration = companyConfigurationService.findCompanyConfiguration();

        String executorUnitCode = purchaseOrder.getExecutorUnit().getExecutorUnitCode();
        String costCenterCode = purchaseOrder.getCostCenter().getCode();

        String gloss = glossGeneratorService.generatePurchaseOrderGloss(purchaseOrder,
                MessageUtils.getMessage("FixedAssetPurchaseOrder.fixedAssets"),
                MessageUtils.getMessage("FixedAssetPurchaseOrder.orderNumberAcronym"));

        Voucher voucher = VoucherBuilder.newGeneralVoucher(Constants.FIXEDASSET_VOUCHER_FORM, gloss);
        voucher.setUserNumber(companyConfiguration.getDefaultAccountancyUser().getId());

        if (CollectionDocumentType.INVOICE.equals(purchaseOrder.getDocumentType())) {
            voucher.addVoucherDetail(VoucherDetailBuilder.newDebitVoucherDetail(
                    executorUnitCode,
                    costCenterCode,
                    companyConfiguration.getFixedAssetInTransitAccount(),
                    BigDecimalUtil.multiply(purchaseOrder.getTotalAmount(), Constants.VAT_COMPLEMENT),
                    FinancesCurrencyType.P,
                    BigDecimal.ONE));
            voucher.addVoucherDetail(VoucherDetailBuilder.newDebitVoucherDetail(
                    executorUnitCode,
                    costCenterCode,
                    companyConfiguration.getNationalCurrencyVATFiscalCreditTransientAccount(),
                    BigDecimalUtil.multiply(purchaseOrder.getTotalAmount(), Constants.VAT),
                    FinancesCurrencyType.P,
                    BigDecimal.ONE));
        } else {
            voucher.addVoucherDetail(VoucherDetailBuilder.newDebitVoucherDetail(
                    executorUnitCode,
                    costCenterCode,
                    companyConfiguration.getFixedAssetInTransitAccount(),
                    purchaseOrder.getTotalAmount(),
                    FinancesCurrencyType.P,
                    BigDecimal.ONE));
        }

        if (BigDecimalUtil.isPositive(sumAdvancePaymentAmount)) {
            voucher.addVoucherDetail(VoucherDetailBuilder.newCreditVoucherDetail(
                    executorUnitCode,
                    costCenterCode,
                    companyConfiguration.getAdvancePaymentNationalCurrencyAccount(),
                    sumAdvancePaymentAmount,
                    FinancesCurrencyType.P,
                    BigDecimal.ONE));
        }

        if (BigDecimalUtil.isPositive(sumLiquidationPaymentAmount)) {
            voucher.addVoucherDetail(VoucherDetailBuilder.newCreditVoucherDetail(
                    executorUnitCode,
                    costCenterCode,
                    purchaseOrder.getProvider().getPayableAccount(),
                    sumLiquidationPaymentAmount,
                    purchaseOrder.getProvider().getPayableAccount().getCurrency(),
                    financesExchangeRateService.getExchangeRateByCurrencyType(purchaseOrder.getProvider().getPayableAccount().getCurrency(), defaultExchangeRate)));
        }

        BigDecimal balanceAmount = BigDecimalUtil.subtract(purchaseOrder.getTotalAmount(), sumAdvancePaymentAmount, sumLiquidationPaymentAmount);
        if (balanceAmount.doubleValue() > 0) {
            voucher.addVoucherDetail(VoucherDetailBuilder.newCreditVoucherDetail(
                    executorUnitCode,
                    companyConfiguration.getExchangeRateBalanceCostCenter().getCode(),
                    companyConfiguration.getBalanceExchangeRateAccount(),
                    balanceAmount,
                    FinancesCurrencyType.P,
                    BigDecimal.ONE));
        } else if (balanceAmount.doubleValue() < 0) {
            voucher.addVoucherDetail(VoucherDetailBuilder.newDebitVoucherDetail(
                    executorUnitCode,
                    companyConfiguration.getExchangeRateBalanceCostCenter().getCode(),
                    companyConfiguration.getBalanceExchangeRateAccount(),
                    balanceAmount.abs(),
                    FinancesCurrencyType.P,
                    BigDecimal.ONE));
        }
        voucherService.create(voucher);
    }
}