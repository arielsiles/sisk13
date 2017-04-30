package com.encens.khipus.service.fixedassets;

import com.encens.khipus.exception.ConcurrencyException;
import com.encens.khipus.exception.ReferentialIntegrityException;
import com.encens.khipus.exception.purchase.*;
import com.encens.khipus.framework.service.GenericService;
import com.encens.khipus.model.fixedassets.PurchaseOrderDetailPart;
import com.encens.khipus.model.purchases.FixedAssetPurchaseOrderDetail;
import com.encens.khipus.model.purchases.PurchaseOrder;

import javax.ejb.Local;
import java.util.List;

/**
 * @author
 * @version 2.2
 */
@Local
public interface FixedAssetPurchaseOrderDetailService extends GenericService {
    void createFixedAssetPurchaseOrderDetail(FixedAssetPurchaseOrderDetail fixedAssetPurchaseOrderDetail,
                                             PurchaseOrder purchaseOrder,
                                             List<PurchaseOrderDetailPart> purchaseOrderDetailParts)
            throws PurchaseOrderApprovedException,
            PurchaseOrderFinalizedException,
            PurchaseOrderNullifiedException,
            ConcurrencyException, PurchaseOrderLiquidatedException;


    List<FixedAssetPurchaseOrderDetail> getFixedAssetPurchaseOrderDetailList(PurchaseOrder purchaseOrder);

    FixedAssetPurchaseOrderDetail findFixedAssetPurchaseOrderDetail(Long id) throws PurchaseOrderDetailNotFoundException;

    void updatePurchaseOrder(FixedAssetPurchaseOrderDetail entity,
                             List<PurchaseOrderDetailPart> purchaseOrderDetailParts)
            throws PurchaseOrderFinalizedException,
            ConcurrencyException,
            PurchaseOrderDetailNotFoundException,
            PurchaseOrderApprovedException,
            PurchaseOrderNullifiedException, PurchaseOrderLiquidatedException;

    void deletePurchaseOrder(FixedAssetPurchaseOrderDetail entity)
            throws PurchaseOrderFinalizedException,
            PurchaseOrderApprovedException,
            ReferentialIntegrityException,
            PurchaseOrderDetailNotFoundException,
            PurchaseOrderNullifiedException,
            ConcurrencyException, PurchaseOrderLiquidatedException;

    Boolean isPurchaseOrderDetailEmpty(PurchaseOrder purchaseOrder);
}
