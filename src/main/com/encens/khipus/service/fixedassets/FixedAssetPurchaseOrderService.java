package com.encens.khipus.service.fixedassets;

import com.encens.khipus.exception.ConcurrencyException;
import com.encens.khipus.exception.EntryDuplicatedException;
import com.encens.khipus.exception.finances.CompanyConfigurationNotFoundException;
import com.encens.khipus.exception.finances.FinancesCurrencyNotFoundException;
import com.encens.khipus.exception.finances.FinancesExchangeRateNotFoundException;
import com.encens.khipus.exception.purchase.*;
import com.encens.khipus.exception.warehouse.AdvancePaymentPendingException;
import com.encens.khipus.model.fixedassets.FixedAsset;
import com.encens.khipus.model.fixedassets.PurchaseOrderFixedAssetPart;
import com.encens.khipus.model.purchases.FixedAssetPurchaseOrderDetail;
import com.encens.khipus.model.purchases.PurchaseOrder;
import com.encens.khipus.model.purchases.PurchaseOrderPayment;
import com.encens.khipus.service.purchases.PurchaseOrderService;

import javax.ejb.Local;
import java.util.List;

/**
 * @author
 * @version 2.26
 */
@Local
public interface FixedAssetPurchaseOrderService extends PurchaseOrderService {
    void create(Object entity, List<FixedAssetPurchaseOrderDetail> details, List<FixedAsset> fixedAssetList) throws EntryDuplicatedException;

    void create(PurchaseOrder purchaseOrder, List<PurchaseOrderFixedAssetPart> fixedAssetPartList) throws EntryDuplicatedException;

    void finalizePurchaseOrder(PurchaseOrder entity)
            throws PurchaseOrderDetailEmptyException, PurchaseOrderFinalizedException, EntryDuplicatedException;

    PurchaseOrder updateTotalAmountFields(PurchaseOrder entity);

    void updateFixedAssetPurchaseOrder(PurchaseOrder entity, List<FixedAsset> selectedFixedAssetList)
            throws PurchaseOrderApprovedException,
            PurchaseOrderFinalizedException,
            ConcurrencyException,
            PurchaseOrderNullifiedException, PurchaseOrderLiquidatedException, EntryDuplicatedException;

    void liquidatePurchaseOrder(PurchaseOrder purchaseOrder, PurchaseOrderPayment purchaseOrderPayment)
            throws AdvancePaymentPendingException,
            PurchaseOrderLiquidatedException,
            PurchaseOrderDetailEmptyException,
            CompanyConfigurationNotFoundException,
            FinancesCurrencyNotFoundException,
            FinancesExchangeRateNotFoundException,
            RotatoryFundNullifiedException,
            RotatoryFundLiquidatedException,
            CollectionSumExceedsRotatoryFundAmountException,
            RotatoryFundConcurrencyException;

    void approvePurchaseOrder(PurchaseOrder entity, List<FixedAsset> selectedFixedAssetList)
            throws PurchaseOrderApprovedException,
            PurchaseOrderFinalizedException,
            PurchaseOrderDetailEmptyException,
            ConcurrencyException,
            PurchaseOrderNullifiedException,
            PurchaseOrderLiquidatedException, EntryDuplicatedException;
}
