package com.encens.khipus.service.warehouse;

import com.encens.khipus.action.SessionUser;
import com.encens.khipus.action.fixedassets.LiquidationPaymentAction;
import com.encens.khipus.exception.ConcurrencyException;
import com.encens.khipus.exception.EntryDuplicatedException;
import com.encens.khipus.exception.EntryNotFoundException;
import com.encens.khipus.exception.finances.CompanyConfigurationNotFoundException;
import com.encens.khipus.exception.finances.FinancesCurrencyNotFoundException;
import com.encens.khipus.exception.finances.FinancesExchangeRateNotFoundException;
import com.encens.khipus.exception.purchase.*;
import com.encens.khipus.exception.warehouse.*;
import com.encens.khipus.interceptor.FinancesUser;
import com.encens.khipus.model.employees.Employee;
import com.encens.khipus.model.finances.CollectionDocumentType;
import com.encens.khipus.model.finances.Provide;
import com.encens.khipus.model.finances.PurchaseOrderPaymentKind;
import com.encens.khipus.model.finances.Voucher;
import com.encens.khipus.model.purchases.*;
import com.encens.khipus.model.warehouse.*;
import com.encens.khipus.service.finances.FinanceAccountingDocumentService;
import com.encens.khipus.service.finances.RotatoryFundCollectionService;
import com.encens.khipus.service.fixedassets.CompanyConfigurationService;
import com.encens.khipus.service.purchases.PurchaseOrderNumberGeneratorService;
import com.encens.khipus.service.purchases.PurchaseOrderServiceBean;
import com.encens.khipus.util.*;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceException;
import java.math.BigDecimal;
import java.util.*;

import static javax.ejb.TransactionAttributeType.REQUIRES_NEW;

/**
 * @author
 * @version 3.5.2.2
 */
@Stateless
@Name("warehousePurchaseOrderService")
@FinancesUser
@AutoCreate
public class WarehousePurchaseOrderServiceBean extends PurchaseOrderServiceBean implements WarehousePurchaseOrderService {
    @In(value = "#{listEntityManager}")
    private EntityManager eventEm;

    @In
    private WarehouseService warehouseService;

    @In(value = "warehousePurchaseOrderDetailService")
    private WarehousePurchaseOrderDetailService purchaseOrderDetailService;

    @In(value = "monthProcessService")
    private MonthProcessService monthProcessService;

    @In
    private WarehouseAccountEntryService warehouseAccountEntryService;

    @In
    CompanyConfigurationService companyConfigurationService;

    @In
    PurchaseOrderNumberGeneratorService purchaseOrderNumberGeneratorService;

    @In
    private RotatoryFundCollectionService rotatoryFundCollectionService;

    @In
    private ProvideService provideService;

    @In
    private ProductItemByProviderHistoryService productItemByProviderHistoryService;

    @In
    private FinanceAccountingDocumentService financeAccountingDocumentService;

    @In
    private InventoryService inventoryService;

    @In
    private SessionUser sessionUser;

    @In
    protected Map<String, String> messages;

    @In(create = true, value = "liquidationPaymentAction")
    private LiquidationPaymentAction liquidationPaymentAction;

    private static final Integer SCALE = 6;

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
        } catch (CompanyConfigurationNotFoundException e) {
        }
    }

    /**
     * Fills the warning attribute according to the Maps and List mappings
     *
     * @param purchaseOrderDetail the instance to modify
     * @param purchaseOrderDetailUnderMinimalStockMap
     *                            the map that holds under minimal stock purchaseOrderDetails
     * @param purchaseOrderDetailOverMaximumStockMap
     *                            the map that holds over maximum stock purchaseOrderDetails
     * @param purchaseOrderDetailWithoutWarnings
     *                            the list that holds purchaseOrderDetails without warnings
     */
    public void fillPurchaseOrderDetail(PurchaseOrderDetail purchaseOrderDetail,
                                        Map<PurchaseOrderDetail, BigDecimal> purchaseOrderDetailUnderMinimalStockMap,
                                        Map<PurchaseOrderDetail, BigDecimal> purchaseOrderDetailOverMaximumStockMap,
                                        List<PurchaseOrderDetail> purchaseOrderDetailWithoutWarnings) {
        if (purchaseOrderDetailUnderMinimalStockMap.containsKey(purchaseOrderDetail)) {
            purchaseOrderDetail.setWarning(MessageUtils.getMessage("WarehousePurchaseOrderDetail.underMinimalStockWarning",
                    FormatUtils.formatNumber(purchaseOrderDetail.getProductItem().getMinimalStock(), messages.get("patterns.decimal6FNumber"), sessionUser.getLocale())));
        }
        if (purchaseOrderDetailOverMaximumStockMap.containsKey(purchaseOrderDetail)) {
            purchaseOrderDetail.setWarning(MessageUtils.getMessage("WarehousePurchaseOrderDetail.overMaximumStockWarning",
                    FormatUtils.formatNumber(purchaseOrderDetail.getProductItem().getMaximumStock(), messages.get("patterns.decimal6FNumber"), sessionUser.getLocale())));
        }
        if (purchaseOrderDetailWithoutWarnings.contains(purchaseOrderDetail)) {
            purchaseOrderDetail.setWarning(messages.get("WarehousePurchaseOrderDetail.idealStockWarning"));
        }
    }

    public void create(Object entity, List<PurchaseOrderDetail> purchaseOrderDetails,
                       Map<PurchaseOrderDetail, BigDecimal> purchaseOrderDetailUnderMinimalStockMap,
                       Map<PurchaseOrderDetail, BigDecimal> purchaseOrderDetailOverMaximumStockMap,
                       List<PurchaseOrderDetail> purchaseOrderDetailWithoutWarnings)
            throws EntryDuplicatedException,
            DuplicatedPurchaseOrderDetailException {
        create(entity);
        PurchaseOrder purchaseOrder = (PurchaseOrder) entity;
        for (PurchaseOrderDetail purchaseOrderDetail : purchaseOrderDetails) {
            try {
                purchaseOrderDetail.setPurchaseOrder((PurchaseOrder) entity);
                Provide dbProvide = provideService.findByProviderAndProductItem(purchaseOrder.getProvider(), purchaseOrderDetail.getProductItem(), eventEm);
                Provide provide = provideService.findByProviderAndProductItem(purchaseOrder.getProvider(), purchaseOrderDetail.getProductItem(), null);
                ProductItemByProviderHistory productItemByProviderHistory = productItemByProviderHistoryService.findLastUnitCostByProductItem(provide.getProvider(), provide.getProductItem(), null);
                if (productItemByProviderHistory == null || purchaseOrderDetail.getUnitCost().compareTo(dbProvide.getGroupAmount()) != 0) {
                    /*update group amount to the corresponding provide */
                    provide.setGroupAmount(purchaseOrderDetail.getUnitCost());
                    if (!getEntityManager().contains(provide)) {
                        getEntityManager().merge(provide);
                    }

                    productItemByProviderHistory = new ProductItemByProviderHistory();
                    productItemByProviderHistory.setDate(new Date());
                    productItemByProviderHistory.setProvide(provide);
                    productItemByProviderHistory.setUnitCost(purchaseOrderDetail.getUnitCost());
                    getEntityManager().persist(productItemByProviderHistory);
                    getEntityManager().flush();
                }
                purchaseOrderDetail.setProductItemByProviderHistory(productItemByProviderHistory);
                purchaseOrderDetailService.createPurchaseOrderDetail(purchaseOrderDetail, purchaseOrderDetail.getUnitCost(),
                        purchaseOrderDetailUnderMinimalStockMap,
                        purchaseOrderDetailOverMaximumStockMap,
                        purchaseOrderDetailWithoutWarnings);
            } catch (PurchaseOrderApprovedException e) {
                //this exception never happen because the purchase order ist created
            } catch (PurchaseOrderFinalizedException e) {
                //this exception never happen because the purchase order ist created
            } catch (PurchaseOrderNullifiedException e) {
                //this exception never happen because the purchase order ist created
            } catch (ConcurrencyException e) {
                //this exception never happen because the purchase order ist created
            } catch (DiscountAmountException e) {
                //this exception never happen because the purchase order ist created
            } catch (PurchaseOrderLiquidatedException e) {
                //this exception never happen because the purchase order ist created
            }
        }
    }

    public PurchaseOrder updateTotalAmountFields(PurchaseOrder purchaseOrder) {
        BigDecimal netTotalAmount = calculatePurchaseOrderNetTotalAmount(purchaseOrder);
        BigDecimal discountPercentage = purchaseOrder.getDiscountPercent();
        BigDecimal discountAmount = BigDecimal.ZERO;
        if (BigDecimalUtil.isPositive(discountPercentage) && BigDecimalUtil.isPositive(netTotalAmount)) {
            discountAmount = BigDecimalUtil.multiply(
                    netTotalAmount,
                    BigDecimalUtil.divide(discountPercentage, BigDecimalUtil.ONE_HUNDRED, 7));
        }
        BigDecimal totalAmount = BigDecimalUtil.subtract(netTotalAmount, discountAmount);
        purchaseOrder.setSubTotalAmount(netTotalAmount);
        purchaseOrder.setTotalAmount(totalAmount);
        purchaseOrder.setDiscountPercent(discountPercentage);
        purchaseOrder.setDiscountAmount(discountAmount);
        purchaseOrder.setBalanceAmount(totalAmount);
        purchaseOrder.setPaymentStatus(PurchaseOrderPaymentStatus.NO_PAYMENT);
        return purchaseOrder;
    }

    @TransactionAttribute(REQUIRES_NEW)
    public void updateWarehousePurchaseOrder(PurchaseOrder purchaseOrder,
                                             Map<PurchaseOrderDetail, BigDecimal> purchaseOrderDetailUnderMinimalStockMap,
                                             Map<PurchaseOrderDetail, BigDecimal> purchaseOrderDetailOverMaximumStockMap,
                                             List<PurchaseOrderDetail> purchaseOrderDetailWithoutWarnings)
            throws PurchaseOrderApprovedException,
            PurchaseOrderFinalizedException,
            ConcurrencyException,
            PurchaseOrderNullifiedException,
            DiscountAmountException,
            PurchaseOrderLiquidatedException, PurchaseOrderDetailNotFoundException,
            DuplicatedPurchaseOrderDetailException, EntryDuplicatedException {
        if (canChangePurchaseOrder(purchaseOrder)) {
            purchaseOrder = updateTotalAmountFields(purchaseOrder);
            if (BigDecimalUtil.ONE_HUNDRED.compareTo(purchaseOrder.getDiscountPercent()) == -1) {
                throw new DiscountAmountException(purchaseOrder.getSubTotalAmount());
            }

            // update detail warnings
            for (PurchaseOrderDetail purchaseOrderDetail : purchaseOrder.getPurchaseOrderDetailList()) {
                // update detail warnings
                fillPurchaseOrderDetail(purchaseOrderDetail, purchaseOrderDetailUnderMinimalStockMap,
                        purchaseOrderDetailOverMaximumStockMap,
                        purchaseOrderDetailWithoutWarnings);
            }
            update(purchaseOrder);
            getEntityManager().refresh(purchaseOrder);
        }
    }

    public void updateWarehousePurchaseOrder(PurchaseOrder entity)
            throws PurchaseOrderApprovedException,
            PurchaseOrderFinalizedException,
            ConcurrencyException,
            PurchaseOrderNullifiedException,
            DiscountAmountException,
            PurchaseOrderLiquidatedException {

        if (canChangePurchaseOrder(entity)) {
            entity = updateTotalAmountFields(entity);

            if (BigDecimalUtil.ONE_HUNDRED.compareTo(entity.getDiscountPercent()) == -1) {
                throw new DiscountAmountException(entity.getSubTotalAmount());
            }

            try {
                update(entity);
            } catch (EntryDuplicatedException e) {
                throw new RuntimeException("Unexpected error was happen", e);
            }

            getEntityManager().refresh(entity);
        }
    }

    public void approveWarehousePurchaseOrder(PurchaseOrder entity,
                                              Map<PurchaseOrderDetail, BigDecimal> purchaseOrderDetailUnderMinimalStockMap,
                                              Map<PurchaseOrderDetail, BigDecimal> purchaseOrderDetailOverMaximumStockMap,
                                              List<PurchaseOrderDetail> purchaseOrderDetailWithoutWarnings)
            throws PurchaseOrderApprovedException,
            PurchaseOrderFinalizedException,
            PurchaseOrderDetailEmptyException,
            ConcurrencyException,
            PurchaseOrderNullifiedException,
            DiscountAmountException,
            PurchaseOrderLiquidatedException {
        PurchaseOrder purchaseOrder = findInDataBase(entity.getId());

        if (canChangePurchaseOrder(purchaseOrder)) {
            if (isPurchaseOrderEmpty(entity)) {
                throw new PurchaseOrderDetailEmptyException("The purchase order detail cannot be empty");
            }

            entity = updateTotalAmountFields(entity);

            if (BigDecimalUtil.ONE_HUNDRED.compareTo(entity.getDiscountPercent()) == -1) {
                throw new DiscountAmountException(entity.getSubTotalAmount());
            }
            entity.setState(PurchaseOrderState.APR);

            try {
                update(entity);
                // update detail warnings
                for (PurchaseOrderDetail purchaseOrderDetail : entity.getPurchaseOrderDetailList()) {
                    fillPurchaseOrderDetail(purchaseOrderDetail, purchaseOrderDetailUnderMinimalStockMap,
                            purchaseOrderDetailOverMaximumStockMap,
                            purchaseOrderDetailWithoutWarnings);
                    update(purchaseOrderDetail);
                }
            } catch (EntryDuplicatedException e) {
                throw new RuntimeException("Unexpected error was happen", e);
            }

            getEntityManager().refresh(entity);
        }
    }

    @Override
    public void approvePurchaseOrder(PurchaseOrder entity)
            throws PurchaseOrderApprovedException,
            PurchaseOrderFinalizedException,
            PurchaseOrderDetailEmptyException,
            ConcurrencyException,
            PurchaseOrderNullifiedException,
            PurchaseOrderLiquidatedException {
        PurchaseOrder purchaseOrder = findInDataBase(entity.getId());

        if (canChangePurchaseOrder(purchaseOrder)) {
            if (isPurchaseOrderEmpty(entity)) {
                throw new PurchaseOrderDetailEmptyException("The purchase order detail cannot be empty");
            }

            entity = updateTotalAmountFields(entity);
            entity.setState(PurchaseOrderState.APR);

            try {
                update(entity);
            } catch (EntryDuplicatedException e) {
                throw new RuntimeException("Unexpected error was happen", e);
            }
        }
    }

    public void finalizePurchaseOrder(PurchaseOrder entity)
            throws WarehouseDocumentTypeNotFoundException,
            PurchaseOrderDetailEmptyException,
            PurchaseOrderFinalizedException,
            ProductItemNotFoundException {

        if (isPurchaseOrderFinalized(entity)) {
            findPurchaseOrder(entity.getId());
            throw new PurchaseOrderFinalizedException("The purchase order was already finalized, and cannot be changed");
        }

        WarehouseDocumentType warehouseDocumentType = getFirstReceptionType();
        if (null == warehouseDocumentType) {
            throw new WarehouseDocumentTypeNotFoundException("Warehouse document reception type not found");
        }

        List<PurchaseOrderDetail> purchaseOrderDetails = purchaseOrderDetailService.getPurchaseOrderDetailList(entity);
        if (ValidatorUtil.isEmptyOrNull(purchaseOrderDetails)) {
            throw new PurchaseOrderDetailEmptyException("The purchase order detail cannot be empty");
        }

        entity = updateTotalAmountFields(entity);

        entity.setState(PurchaseOrderState.FIN);
        entity.setReceivedType(PurchaseOrderReceivedType.RT);
        getEntityManager().merge(entity);
        getEntityManager().flush();

        Employee responsible = getEntityManager().find(Employee.class, entity.getWarehouse().getResponsibleId());
        createWarehouseVoucher(entity, purchaseOrderDetails, responsible, warehouseDocumentType);
    }

    public Voucher liquidatePurchaseOrder(PurchaseOrder purchaseOrder)
            throws WarehouseDocumentTypeNotFoundException,
            PurchaseOrderDetailEmptyException,
            PurchaseOrderLiquidatedException,
            AdvancePaymentPendingException,
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
        //primeramente verificar verificar si ya se hizo la liquidacion
/*        if (purchaseOrder.getState() != PurchaseOrderState.LIQ) {
            throw new PurchaseOrderLiquidatedException("The purchase order was not liquidated");
        }*/

       /* if (isPurchaseOrderLiquidated(purchaseOrder)) {
            findPurchaseOrder(purchaseOrder.getId());
            throw new PurchaseOrderLiquidatedException("The purchase order was already liquidated, and cannot be changed");
        }*/

        if (isPurchaseOrderEmpty(purchaseOrder)) {
            throw new PurchaseOrderDetailEmptyException("The purchase order detail cannot be empty");
        }

        BigDecimal defaultExchangeRate = null;


        Voucher voucher = warehouseAccountEntryService.createEntryAccountForValidatePurchaseOrder(purchaseOrder, defaultExchangeRate);

        purchaseOrder.setBalanceAmount(BigDecimal.ZERO);
        purchaseOrder.setPaymentStatus(PurchaseOrderPaymentStatus.FULLY_PAID);

        return voucher;
    }

    public void updateliquidatePurchaseOrder(PurchaseOrder purchaseOrder) throws CompanyConfigurationNotFoundException {
        purchaseOrder = getEntityManager().merge(purchaseOrder);
        getEntityManager().flush();

        financeAccountingDocumentService.createAccountingVoucherByPurchaseOrder(purchaseOrder);
    }

    public void liquidatePurchaseOrder(PurchaseOrder purchaseOrder, PurchaseOrderPayment purchaseOrderPayment)
            throws WarehouseDocumentTypeNotFoundException,
            PurchaseOrderDetailEmptyException,
            PurchaseOrderLiquidatedException,
            AdvancePaymentPendingException,
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
            throw new PurchaseOrderLiquidatedException("The purchase order was already liquidated, and cannot be changed");
        }

        if (isPurchaseOrderEmpty(purchaseOrder)) {
            throw new PurchaseOrderDetailEmptyException("The purchase order detail cannot be empty");
        }

        BigDecimal defaultExchangeRate = null;

        if (purchaseOrderPayment != null && !BigDecimalUtil.isZeroOrNull(purchaseOrderPayment.getPayAmount())
                && !BigDecimalUtil.isZeroOrNull(purchaseOrderPayment.getSourceAmount())) {
            purchaseOrderPayment.setPurchaseOrderPaymentKind(PurchaseOrderPaymentKind.LIQUIDATION_PAYMENT);
            if (!purchaseOrderPayment.getPaymentType().equals(PurchaseOrderPaymentType.PAYMENT_WITH_CHECK)) {
                purchaseOrderPayment.setCheckDestination(null);
            }
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
        warehouseAccountEntryService.createEntryAccountForLiquidatedPurchaseOrder(purchaseOrder, defaultExchangeRate);

        purchaseOrder.setState(PurchaseOrderState.LIQ);
        purchaseOrder.setBalanceAmount(BigDecimal.ZERO);
        purchaseOrder.setPaymentStatus(PurchaseOrderPaymentStatus.FULLY_PAID);
        purchaseOrder = getEntityManager().merge(purchaseOrder);
        getEntityManager().flush();

        financeAccountingDocumentService.createAccountingVoucherByPurchaseOrder(purchaseOrder);
    }

    public void liquidateCashPurchaseOrder(PurchaseOrder purchaseOrder){
        System.out.println("=======> Liquidando Orden de Compra Al contado...");
        purchaseOrder.setState(PurchaseOrderState.LIQ);
        purchaseOrder.setBalanceAmount(BigDecimal.ZERO);
        purchaseOrder.setPaymentStatus(PurchaseOrderPaymentStatus.FULLY_PAID);
        getEntityManager().merge(purchaseOrder);
        getEntityManager().flush();

    }

    public void onlyLiquidatePurchaseOrder(List<PurchaseOrder> purchaseOrders, PurchaseOrder entity)
            throws WarehouseDocumentTypeNotFoundException,
            PurchaseOrderDetailEmptyException,
            PurchaseOrderLiquidatedException,
            AdvancePaymentPendingException,
            CompanyConfigurationNotFoundException,
            FinancesCurrencyNotFoundException,
            FinancesExchangeRateNotFoundException,
            RotatoryFundNullifiedException,
            RotatoryFundLiquidatedException,
            CollectionSumExceedsRotatoryFundAmountException,
            RotatoryFundConcurrencyException {
        PurchaseOrderPayment purchasePayment = (currentBalanceAmount(entity).compareTo(BigDecimal.ZERO) > 0) ? liquidationPaymentAction.getLiquidationPayment() : null;
        Double totalSourceAmount = purchasePayment.getSourceAmount().doubleValue();
        Double totalPayAmount = purchasePayment.getPayAmount().doubleValue();

        /*for(PurchaseOrder purchaseOrder: purchaseOrders){
            PurchaseOrderPayment purchaseOrderPayment = (currentBalanceAmount(purchaseOrder).compareTo(BigDecimal.ZERO) > 0) ? liquidationPaymentAction.getLiquidationPayment() : null;

            if (purchaseOrderPayment != null && !BigDecimalUtil.isZeroOrNull(purchaseOrderPayment.getPayAmount())
                    && !BigDecimalUtil.isZeroOrNull(purchaseOrderPayment.getSourceAmount())) {
                totalSourceAmount += purchaseOrderPayment.getSourceAmount().doubleValue();
                totalPayAmount += purchaseOrderPayment.getPayAmount().doubleValue();
            }
        }*/
        if (purchasePayment != null &&!BigDecimalUtil.isZeroOrNull(new BigDecimal(totalPayAmount)) && !BigDecimalUtil.isZeroOrNull(new BigDecimal(totalSourceAmount))){

            String transactionNumber = warehouseAccountEntryService.createEntryAccountPurchaseOrderForPaymentCheck(entity,purchasePayment,new BigDecimal(totalSourceAmount),new BigDecimal(totalPayAmount));
            if (PurchaseOrderPaymentType.PAYMENT_ROTATORY_FUND.equals(purchasePayment.getPaymentType())) {
                try {
                    rotatoryFundCollectionService.generateCollectionForPurchaseOrderPayment(purchasePayment);
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

            String ordersNumbers = MessageUtils.getMessage("WarehousePurchaseOrder.orderNumberAcronym")+" ";

            for(PurchaseOrder purchaseOrder: purchaseOrders)
            {
                ordersNumbers += purchaseOrder.getOrderNumber();
                ordersNumbers += "; ";
            }

            for(PurchaseOrder purchaseOrder: purchaseOrders){

                BigDecimal defaultExchangeRate = null;
                liquidationPaymentAction.setDefaultDescription(entity,
                        MessageUtils.getMessage("WarehousePurchaseOrder.warehouses"),
                        ordersNumbers);
                //MessageUtils.getMessage("WarehousePurchaseOrder.orderNumberAcronym"));
                liquidationPaymentAction.setPurchaseOrder(entity);
                PurchaseOrderPayment purchaseOrderPayment = (currentBalanceAmount(purchaseOrder).compareTo(BigDecimal.ZERO) > 0) ? liquidationPaymentAction.getLiquidationPayment() : null;
                purchaseOrderPayment.setCheckDestination(purchasePayment.getCheckDestination());
                purchaseOrderPayment.setBankAccount(purchasePayment.getBankAccount());
                purchaseOrderPayment.setBeneficiaryType(purchasePayment.getBeneficiaryType());
                purchaseOrderPayment.setBeneficiaryName(purchaseOrderPayment.getBeneficiaryName());
                purchaseOrderPayment.setPaymentType(purchaseOrderPayment.getPaymentType());

                if (purchaseOrderPayment != null && !BigDecimalUtil.isZeroOrNull(purchaseOrderPayment.getPayAmount())
                        && !BigDecimalUtil.isZeroOrNull(purchaseOrderPayment.getSourceAmount())) {
                    purchaseOrderPayment.setPurchaseOrderPaymentKind(PurchaseOrderPaymentKind.LIQUIDATION_PAYMENT);
                    if (!purchaseOrderPayment.getPaymentType().equals(PurchaseOrderPaymentType.PAYMENT_WITH_CHECK)) {
                        purchaseOrderPayment.setCheckDestination(null);
                    }
                    defaultExchangeRate = purchaseOrderPayment.getExchangeRate();
                    warehouseAccountEntryService.setPurchaseOrderForPaymentCheck(purchaseOrder, purchaseOrderPayment,transactionNumber);
                    //todo:verificar con Claudia
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
                purchaseOrder.setState(PurchaseOrderState.LIQ);
                getEntityManager().merge(purchaseOrder);
                getEntityManager().flush();
            }
        }
        entity.setState(PurchaseOrderState.LIQ);
        getEntityManager().flush();
    }

    public void liquidatePurchaseOrderWithCashBox(List<PurchaseOrder> purchaseOrders, PurchaseOrder entity)
            throws WarehouseDocumentTypeNotFoundException,
            PurchaseOrderDetailEmptyException,
            PurchaseOrderLiquidatedException,
            AdvancePaymentPendingException,
            CompanyConfigurationNotFoundException,
            FinancesCurrencyNotFoundException,
            FinancesExchangeRateNotFoundException,
            CollectionSumExceedsRotatoryFundAmountException {

        PurchaseOrderPayment purchasePayment = (currentBalanceAmount(entity).compareTo(BigDecimal.ZERO) > 0) ? liquidationPaymentAction.getLiquidationPayment() : null;
        Double totalSourceAmount = purchasePayment.getSourceAmount().doubleValue();
        Double totalPayAmount    = purchasePayment.getPayAmount().doubleValue();
        System.out.println(".....Liquidate Purchase Order With CashBox....");
        System.out.println("...purchasePayment: " + purchasePayment.toString());
        System.out.println("...totalSourceAmount: " + totalSourceAmount);
        System.out.println("...totalPayAmount: " + totalPayAmount);

        if (purchasePayment != null &&!BigDecimalUtil.isZeroOrNull(new BigDecimal(totalPayAmount)) && !BigDecimalUtil.isZeroOrNull(new BigDecimal(totalSourceAmount))){

            String transactionNumber = warehouseAccountEntryService.createEntryAccountPurchaseOrderForPaymentWithCashBox(entity,
                                                                                                                         purchasePayment,
                                                                                                                         new BigDecimal(totalSourceAmount),
                                                                                                                         new BigDecimal(totalPayAmount));
            String ordersNumbers = MessageUtils.getMessage("WarehousePurchaseOrder.orderNumberAcronym")+" ";

            for(PurchaseOrder purchaseOrder: purchaseOrders){
                ordersNumbers += purchaseOrder.getOrderNumber();
                ordersNumbers += "; ";
            }

            for(PurchaseOrder purchaseOrder: purchaseOrders){

                BigDecimal defaultExchangeRate = null;
                liquidationPaymentAction.setDefaultDescription(entity, MessageUtils.getMessage("WarehousePurchaseOrder.warehouses"), ordersNumbers);
                //MessageUtils.getMessage("WarehousePurchaseOrder.orderNumberAcronym"));
                liquidationPaymentAction.setPurchaseOrder(entity);
                PurchaseOrderPayment purchaseOrderPayment = (currentBalanceAmount(purchaseOrder).compareTo(BigDecimal.ZERO) > 0) ? liquidationPaymentAction.getLiquidationPayment() : null;
                purchaseOrderPayment.setCheckDestination(purchasePayment.getCheckDestination());
                purchaseOrderPayment.setBankAccount(purchasePayment.getBankAccount());
                purchaseOrderPayment.setBeneficiaryType(purchasePayment.getBeneficiaryType());
                purchaseOrderPayment.setBeneficiaryName(purchaseOrderPayment.getBeneficiaryName());
                purchaseOrderPayment.setPaymentType(purchaseOrderPayment.getPaymentType());

                if (purchaseOrderPayment != null && !BigDecimalUtil.isZeroOrNull(purchaseOrderPayment.getPayAmount())
                        && !BigDecimalUtil.isZeroOrNull(purchaseOrderPayment.getSourceAmount())) {
                    purchaseOrderPayment.setPurchaseOrderPaymentKind(PurchaseOrderPaymentKind.LIQUIDATION_PAYMENT);
                    if (!purchaseOrderPayment.getPaymentType().equals(PurchaseOrderPaymentType.PAYMENT_WITH_CHECK)) {
                        purchaseOrderPayment.setCheckDestination(null);
                    }
                    defaultExchangeRate = purchaseOrderPayment.getExchangeRate();
                    warehouseAccountEntryService.setPurchaseOrderForPaymentCheck(purchaseOrder, purchaseOrderPayment,transactionNumber);
                }
                purchaseOrder.setState(PurchaseOrderState.LIQ);
                getEntityManager().merge(purchaseOrder);
                getEntityManager().flush();
            }
        }
        entity.setState(PurchaseOrderState.LIQ);
        getEntityManager().flush();
    }

    public void onlyLiquidatePurchaseOrder(PurchaseOrder purchaseOrder, PurchaseOrderPayment purchaseOrderPayment)
            throws WarehouseDocumentTypeNotFoundException,
            PurchaseOrderDetailEmptyException,
            PurchaseOrderLiquidatedException,
            AdvancePaymentPendingException,
            CompanyConfigurationNotFoundException,
            FinancesCurrencyNotFoundException,
            FinancesExchangeRateNotFoundException,
            RotatoryFundNullifiedException,
            RotatoryFundLiquidatedException,
            CollectionSumExceedsRotatoryFundAmountException,
            RotatoryFundConcurrencyException {


        BigDecimal defaultExchangeRate = null;

        if (purchaseOrderPayment != null && !BigDecimalUtil.isZeroOrNull(purchaseOrderPayment.getPayAmount())
                && !BigDecimalUtil.isZeroOrNull(purchaseOrderPayment.getSourceAmount())) {
            purchaseOrderPayment.setPurchaseOrderPaymentKind(PurchaseOrderPaymentKind.LIQUIDATION_PAYMENT);
            if (!purchaseOrderPayment.getPaymentType().equals(PurchaseOrderPaymentType.PAYMENT_WITH_CHECK)) {
                purchaseOrderPayment.setCheckDestination(null);
            }
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
        purchaseOrder.setState(PurchaseOrderState.LIQ);
        getEntityManager().flush();

    }

    @Override
    protected Long getNextOrderNumber() {
        Long orderNumber = (Long) getEntityManager().createNamedQuery("PurchaseOrder.countByCompanyNumberAndType")
                .setParameter("companyNumber", Constants.defaultCompanyNumber)
                .setParameter("orderType", PurchaseOrderType.WAREHOUSE)
                .getSingleResult();
        if (null == orderNumber) {
            orderNumber = (long) 1;
        } else {
            orderNumber++;
        }

        return orderNumber;
    }

    @SuppressWarnings(value = "unchecked")
    public WarehouseDocumentType getFirstReceptionType() {
        List<WarehouseDocumentType> warehouseDocumentTypeList = getEntityManager()
                .createNamedQuery("WarehouseDocumentType.findByType")
                .setParameter("companyNumber", Constants.defaultCompanyNumber)
                .setParameter("warehouseVoucherType", WarehouseVoucherType.R).getResultList();

        if (!ValidatorUtil.isEmptyOrNull(warehouseDocumentTypeList)) {
            return warehouseDocumentTypeList.get(0);
        }

        return null;
    }

    private BigDecimal calculatePurchaseOrderNetTotalAmount(PurchaseOrder purchaseOrder) {
        BigDecimal total = (BigDecimal) getEntityManager()
                .createNamedQuery("PurchaseOrderDetail.sumTotalAmounts")
                .setParameter("purchaseOrder", purchaseOrder)
                .getSingleResult();

        if (null == total) {
            total = BigDecimal.ZERO;
        }

        return total;
    }

    @Override
    protected Boolean isPurchaseOrderEmpty(PurchaseOrder instance) {
        return purchaseOrderDetailService.isPurchaseOrderDetailEmpty(instance);
    }

    private void createWarehouseVoucher(PurchaseOrder entity,
                                        List<PurchaseOrderDetail> purchaseOrderDetails,
                                        Employee responsible,
                                        WarehouseDocumentType warehouseDocumentType)
            throws ProductItemNotFoundException {

        /*create WarehouseVoucher*/
        WarehouseVoucher warehouseVoucher = new WarehouseVoucher();
        warehouseVoucher.setDocumentType(warehouseDocumentType);
        warehouseVoucher.setWarehouse(entity.getWarehouse());
        warehouseVoucher.setDate(monthProcessService.getMothProcessDate(new Date()));
        warehouseVoucher.setState(WarehouseVoucherState.PEN);
        warehouseVoucher.setPurchaseOrder(entity);
        warehouseVoucher.setExecutorUnit(entity.getExecutorUnit());
        warehouseVoucher.setCostCenterCode(entity.getCostCenterCode());
        warehouseVoucher.setResponsible(responsible);

        InventoryMovement inventoryMovement = new InventoryMovement();
        String obs = entity.getGloss();
        if (entity.getGloss().length() > 250) {
            obs = obs.substring(0, 249);
        }
        inventoryMovement.setDescription(entity.getOrderNumber() + " " +obs);

        // creates an empty voucher without movement details
        try {
            warehouseService.createWarehouseVoucher(warehouseVoucher, inventoryMovement, null, null, null, null);
        } catch (InventoryException e) {
            log.debug(e, "This exception never happen because I just created a new input WarehouseVoucher");
        }

        for (PurchaseOrderDetail purchaseOrderDetail : purchaseOrderDetails) {
            ProductItem productItem = getEntityManager().find(ProductItem.class,
                    purchaseOrderDetail.getProductItem().getId());

            BigDecimal requestedQuantity = purchaseOrderDetail.getRequestedQuantity();
            if (null != productItem.getGroupMeasureCode() &&
                    productItem.getGroupMeasureCode().equals(purchaseOrderDetail.getPurchaseMeasureCode())) {

                BigDecimal equivalent = productItem.getEquivalentQuantity();
                if (BigDecimal.ZERO.compareTo(equivalent) == 0) {
                    equivalent = BigDecimal.ONE;
                }

                requestedQuantity = BigDecimalUtil.multiply(purchaseOrderDetail.getRequestedQuantity(), equivalent);
            }

            MovementDetail movementDetailTemp = new MovementDetail();
            movementDetailTemp.setProductItem(productItem);
            movementDetailTemp.setProductItemAccount(productItem.getProductItemAccount());
            movementDetailTemp.setQuantity(requestedQuantity);
            if (CollectionDocumentType.INVOICE.equals(entity.getDocumentType())) {
                movementDetailTemp.setAmount(BigDecimalUtil.multiply(purchaseOrderDetail.getTotalAmount(), Constants.VAT_COMPLEMENT, 6));
            } else {
                movementDetailTemp.setAmount(purchaseOrderDetail.getTotalAmount());
            }

            movementDetailTemp.setUnitCost(BigDecimalUtil.divide(movementDetailTemp.getAmount(), requestedQuantity, 6));
            movementDetailTemp.setExecutorUnit(entity.getExecutorUnit());
            movementDetailTemp.setCostCenterCode(entity.getCostCenterCode());
            movementDetailTemp.setMeasureUnit(productItem.getUsageMeasureUnit());

            movementDetailTemp.setPurchasePrice(purchaseOrderDetail.getTotalAmount());
            //movementDetailTemp.setUnitPurchasePrice(BigDecimalUtil.divide(purchaseOrderDetail.getTotalAmount(), requestedQuantity, 6));
            System.out.println("-----> UnitPurchasePrice: " + purchaseOrderDetail.getUnitCost());
            movementDetailTemp.setUnitPurchasePrice(purchaseOrderDetail.getUnitCost());
            movementDetailTemp.setPurchasePrice(BigDecimalUtil.multiply(purchaseOrderDetail.getRequestedQuantity(), purchaseOrderDetail.getUnitCost()));
            movementDetailTemp.setWarehouse(purchaseOrderDetail.getPurchaseOrder().getWarehouse());
            movementDetailTemp.setMovementType(MovementDetailType.E);

            // this map stores the MovementDetails that are under the minimal stock and the unitaryBalance of the Inventory
            Map<MovementDetail, BigDecimal> movementDetailUnderMinimalStockMap = new HashMap<MovementDetail, BigDecimal>();
            // this map stores the MovementDetails that are over the maximum stock and the unitaryBalance of the Inventory
            Map<MovementDetail, BigDecimal> movementDetailOverMaximumStockMap = new HashMap<MovementDetail, BigDecimal>();
            // this list stores the MovementDetails that should not show warnings
            List<MovementDetail> movementDetailWithoutWarnings = new ArrayList<MovementDetail>();
            buildValidateQuantityMappings(movementDetailTemp, movementDetailUnderMinimalStockMap,
                    movementDetailOverMaximumStockMap, movementDetailWithoutWarnings);
            try {
                warehouseService.createMovementDetail(warehouseVoucher, movementDetailTemp,
                        movementDetailUnderMinimalStockMap,
                        movementDetailOverMaximumStockMap, movementDetailWithoutWarnings);
            } catch (WarehouseVoucherApprovedException e) {
                log.debug("This exception never happen because I just created a new WarehouseVoucher" +
                        " and his state is pending");
            } catch (WarehouseVoucherNotFoundException e) {
                log.debug("This exception never happen because I just created a new WarehouseVoucher");
            } catch (InventoryException e) {
                log.debug("This exception never happen because I just created a new WarehouseVoucher for inputs");
            }
        }
    }

    public void buildValidateQuantityMappings(MovementDetail movementDetail,
                                              Map<MovementDetail, BigDecimal> movementDetailUnderMinimalStockMap,
                                              Map<MovementDetail, BigDecimal> movementDetailOverMaximumStockMap,
                                              List<MovementDetail> movementDetailWithoutWarnings) throws ProductItemNotFoundException {
        BigDecimal requiredQuantity = movementDetail.getQuantity();
        if (null != requiredQuantity) {
            ProductItem productItem = null;
            try {
                productItem = findById(ProductItem.class, movementDetail.getProductItem().getId(), true);
            } catch (EntryNotFoundException e) {
                throw new ProductItemNotFoundException(productItem);
            }
            Warehouse warehouse = movementDetail.getWarehouse();
            BigDecimal minimalStock = productItem.getMinimalStock();
            BigDecimal maximumStock = productItem.getMaximumStock();
            BigDecimal unitaryBalance = inventoryService.findUnitaryBalanceByProductItemAndArticle(warehouse.getId(), productItem.getId());
            BigDecimal totalQuantity = movementDetail.getMovementType().equals(MovementDetailType.E) ?
                    BigDecimalUtil.sum(unitaryBalance, requiredQuantity, SCALE) :
                    BigDecimalUtil.subtract(unitaryBalance, requiredQuantity, SCALE);
            // by default does not show warning until is verified
            boolean showWarning = false;

            if (null != minimalStock) {
                // minimalStock is not null
                int minimalComparison = totalQuantity.compareTo(minimalStock);
                if (minimalComparison < 0) {
                    // if under minimalStock
                    movementDetailUnderMinimalStockMap.put(movementDetail, unitaryBalance);
                    showWarning = true;
                }
            }
            if (null != maximumStock) {
                // maximumStock is not null
                int maximumComparison = totalQuantity.compareTo(maximumStock);
                if (maximumComparison > 0) {
                    // if over maximumStock
                    movementDetailOverMaximumStockMap.put(movementDetail, unitaryBalance);
                    showWarning = true;
                }
            }
            if (!showWarning) {
                movementDetailWithoutWarnings.add(movementDetail);
            }
        }
    }

}
