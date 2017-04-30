package com.encens.khipus.service.warehouse;

import com.encens.khipus.exception.ConcurrencyException;
import com.encens.khipus.exception.ReferentialIntegrityException;
import com.encens.khipus.exception.purchase.*;
import com.encens.khipus.exception.warehouse.DiscountAmountException;
import com.encens.khipus.framework.service.GenericService;
import com.encens.khipus.model.finances.Provide;
import com.encens.khipus.model.finances.Provider;
import com.encens.khipus.model.purchases.PurchaseOrder;
import com.encens.khipus.model.purchases.PurchaseOrderDetail;
import com.encens.khipus.model.warehouse.ProductItem;

import javax.ejb.Local;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * @author
 * @version 3.0
 */
@Local
public interface WarehousePurchaseOrderDetailService extends GenericService {
    List<PurchaseOrderDetail> getPurchaseOrderDetailList(PurchaseOrder purchaseOrder);

    PurchaseOrderDetail findPurchaseOrderDetail(Long id) throws PurchaseOrderDetailNotFoundException;

    void updatePurchaseOrderDetail(PurchaseOrderDetail entity,
                                   Map<PurchaseOrderDetail, BigDecimal> purchaseOrderDetailUnderMinimalStockMap,
                                   Map<PurchaseOrderDetail, BigDecimal> purchaseOrderDetailOverMaximumStockMap,
                                   List<PurchaseOrderDetail> purchaseOrderDetailWithoutWarnings)
            throws PurchaseOrderFinalizedException,
            ConcurrencyException,
            PurchaseOrderDetailNotFoundException,
            PurchaseOrderDetailTotalAmountException,
            PurchaseOrderApprovedException,
            PurchaseOrderNullifiedException,
            DiscountAmountException,
            DuplicatedPurchaseOrderDetailException,
            PurchaseOrderLiquidatedException;

    Provide findProvideElement(ProductItem productItem, Provider provider);

    void deletePurchaseOrderDetail(PurchaseOrderDetail entity)
            throws PurchaseOrderFinalizedException,
            PurchaseOrderApprovedException,
            ReferentialIntegrityException,
            PurchaseOrderDetailNotFoundException,
            PurchaseOrderNullifiedException,
            ConcurrencyException,
            DiscountAmountException,
            PurchaseOrderLiquidatedException;

    Boolean isPurchaseOrderDetailEmpty(PurchaseOrder purchaseOrder);

    void createPurchaseOrderDetail(PurchaseOrderDetail entity, BigDecimal unitPriceByProvider,
                                   Map<PurchaseOrderDetail, BigDecimal> purchaseOrderDetailUnderMinimalStockMap,
                                   Map<PurchaseOrderDetail, BigDecimal> purchaseOrderDetailOverMaximumStockMap,
                                   List<PurchaseOrderDetail> purchaseOrderDetailWithoutWarnings)
            throws PurchaseOrderApprovedException,
            PurchaseOrderFinalizedException,
            PurchaseOrderNullifiedException,
            ConcurrencyException,
            DiscountAmountException,
            DuplicatedPurchaseOrderDetailException,
            PurchaseOrderLiquidatedException;
}
