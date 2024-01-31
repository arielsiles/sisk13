package com.encens.khipus.service.warehouse;

import com.encens.khipus.exception.ConcurrencyException;
import com.encens.khipus.exception.EntryDuplicatedException;
import com.encens.khipus.exception.finances.CompanyConfigurationNotFoundException;
import com.encens.khipus.exception.finances.FinancesCurrencyNotFoundException;
import com.encens.khipus.exception.finances.FinancesExchangeRateNotFoundException;
import com.encens.khipus.exception.purchase.*;
import com.encens.khipus.exception.warehouse.AdvancePaymentPendingException;
import com.encens.khipus.exception.warehouse.DiscountAmountException;
import com.encens.khipus.exception.warehouse.ProductItemNotFoundException;
import com.encens.khipus.exception.warehouse.WarehouseDocumentTypeNotFoundException;
import com.encens.khipus.model.finances.Voucher;
import com.encens.khipus.model.purchases.PurchaseOrder;
import com.encens.khipus.model.purchases.PurchaseOrderDetail;
import com.encens.khipus.model.purchases.PurchaseOrderPayment;
import com.encens.khipus.model.warehouse.WarehouseDocumentType;
import com.encens.khipus.service.purchases.PurchaseOrderService;

import javax.ejb.Local;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * @author
 * @version 3.0
 */
@Local
public interface WarehousePurchaseOrderService extends PurchaseOrderService {
    void createBasic(Object entity, List<PurchaseOrderDetail> purchaseOrderDetails,
                     Map<PurchaseOrderDetail, BigDecimal> purchaseOrderDetailUnderMinimalStockMap,
                     Map<PurchaseOrderDetail, BigDecimal> purchaseOrderDetailOverMaximumStockMap,
                     List<PurchaseOrderDetail> purchaseOrderDetailWithoutWarnings)
            throws EntryDuplicatedException,
            DuplicatedPurchaseOrderDetailException;
    void create(Object entity, List<PurchaseOrderDetail> purchaseOrderDetails,
                Map<PurchaseOrderDetail, BigDecimal> purchaseOrderDetailUnderMinimalStockMap,
                Map<PurchaseOrderDetail, BigDecimal> purchaseOrderDetailOverMaximumStockMap,
                List<PurchaseOrderDetail> purchaseOrderDetailWithoutWarnings)
            throws EntryDuplicatedException,
            DuplicatedPurchaseOrderDetailException;

    void finalizePurchaseOrder(PurchaseOrder entity)
            throws WarehouseDocumentTypeNotFoundException,
            PurchaseOrderDetailEmptyException,
            PurchaseOrderFinalizedException, ProductItemNotFoundException;

    PurchaseOrder updateTotalAmountFields(PurchaseOrder entity);

    void approveWarehousePurchaseOrder(PurchaseOrder entity,
                                       Map<PurchaseOrderDetail, BigDecimal> purchaseOrderDetailUnderMinimalStockMap,
                                       Map<PurchaseOrderDetail, BigDecimal> purchaseOrderDetailOverMaximumStockMap,
                                       List<PurchaseOrderDetail> purchaseOrderDetailWithoutWarnings)
            throws PurchaseOrderApprovedException,
            PurchaseOrderFinalizedException,
            PurchaseOrderDetailEmptyException,
            ConcurrencyException,
            PurchaseOrderNullifiedException,
            DiscountAmountException, PurchaseOrderLiquidatedException, CompanyConfigurationNotFoundException;

    void updateWarehousePurchaseOrder(PurchaseOrder entity,
                                      Map<PurchaseOrderDetail, BigDecimal> purchaseOrderDetailUnderMinimalStockMap,
                                      Map<PurchaseOrderDetail, BigDecimal> purchaseOrderDetailOverMaximumStockMap,
                                      List<PurchaseOrderDetail> purchaseOrderDetailWithoutWarnings)
            throws PurchaseOrderApprovedException,
            PurchaseOrderFinalizedException,
            ConcurrencyException,
            PurchaseOrderNullifiedException,
            DiscountAmountException, PurchaseOrderLiquidatedException,
            PurchaseOrderDetailNotFoundException, DuplicatedPurchaseOrderDetailException, EntryDuplicatedException;

    void updateWarehousePurchaseOrder(PurchaseOrder entity)
            throws PurchaseOrderApprovedException,
            PurchaseOrderFinalizedException,
            ConcurrencyException,
            PurchaseOrderNullifiedException,
            DiscountAmountException, PurchaseOrderLiquidatedException;

    void liquidatePurchaseOrder(PurchaseOrder entity, PurchaseOrderPayment purchaseOrderPayment)
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
            RotatoryFundConcurrencyException;

    void onlyLiquidatePurchaseOrder(PurchaseOrder entity, PurchaseOrderPayment purchaseOrderPayment)
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
            RotatoryFundConcurrencyException;

    void liquidateCashPurchaseOrder(PurchaseOrder purchaseOrder);

    void onlyLiquidatePurchaseOrder(List<PurchaseOrder> purchaseOrders, PurchaseOrder entity)
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
            RotatoryFundConcurrencyException;

    void liquidatePurchaseOrderWithCashBox(List<PurchaseOrder> purchaseOrders, PurchaseOrder entity)
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
            RotatoryFundConcurrencyException;

    Voucher liquidatePurchaseOrder(PurchaseOrder entity)
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
            RotatoryFundConcurrencyException;

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
    void fillPurchaseOrderDetail(PurchaseOrderDetail purchaseOrderDetail,
                                 Map<PurchaseOrderDetail, BigDecimal> purchaseOrderDetailUnderMinimalStockMap,
                                 Map<PurchaseOrderDetail, BigDecimal> purchaseOrderDetailOverMaximumStockMap,
                                 List<PurchaseOrderDetail> purchaseOrderDetailWithoutWarnings);

    public void updateliquidatePurchaseOrder(PurchaseOrder purchaseOrder) throws CompanyConfigurationNotFoundException;

    WarehouseDocumentType getFirstReceptionType();
}

