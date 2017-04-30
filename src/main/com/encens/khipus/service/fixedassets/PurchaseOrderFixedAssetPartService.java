package com.encens.khipus.service.fixedassets;

import com.encens.khipus.exception.ConcurrencyException;
import com.encens.khipus.exception.EntryDuplicatedException;
import com.encens.khipus.exception.ReferentialIntegrityException;
import com.encens.khipus.exception.fixedassets.PurchaseOrderFixedAssetPartNotFoundException;
import com.encens.khipus.exception.purchase.PurchaseOrderApprovedException;
import com.encens.khipus.exception.purchase.PurchaseOrderFinalizedException;
import com.encens.khipus.exception.purchase.PurchaseOrderLiquidatedException;
import com.encens.khipus.exception.purchase.PurchaseOrderNullifiedException;
import com.encens.khipus.framework.service.GenericService;
import com.encens.khipus.model.fixedassets.PurchaseOrderFixedAssetPart;
import com.encens.khipus.model.purchases.PurchaseOrder;

import javax.ejb.Local;
import java.util.List;

/**
 * @author
 * @version 3.3
 */
@Local
public interface PurchaseOrderFixedAssetPartService extends GenericService {

    PurchaseOrderFixedAssetPart findById(Long id) throws PurchaseOrderFixedAssetPartNotFoundException;

    void createPurchaseOrderFixedAssetPart(PurchaseOrderFixedAssetPart entity,
                                           PurchaseOrder purchaseOrder)
            throws PurchaseOrderApprovedException,
            PurchaseOrderFinalizedException,
            PurchaseOrderNullifiedException,
            ConcurrencyException,
            PurchaseOrderLiquidatedException;

    void updatePurchaseOrderFixedAssetPart(PurchaseOrderFixedAssetPart entity)
            throws PurchaseOrderFinalizedException,
            ConcurrencyException,
            PurchaseOrderFixedAssetPartNotFoundException,
            PurchaseOrderApprovedException,
            PurchaseOrderNullifiedException,
            PurchaseOrderLiquidatedException,
            EntryDuplicatedException;

    void deletePurchaseOrderFixedAssetPart(PurchaseOrderFixedAssetPart entity)
            throws PurchaseOrderFinalizedException,
            PurchaseOrderApprovedException,
            ReferentialIntegrityException,
            PurchaseOrderFixedAssetPartNotFoundException,
            PurchaseOrderNullifiedException,
            ConcurrencyException,
            PurchaseOrderLiquidatedException;

    List<PurchaseOrderFixedAssetPart> getPurchaseOrderFixedAssetPartList(PurchaseOrder purchaseOrder);

    Boolean isPurchaseOrderFixedAssetPartEmpty(PurchaseOrder purchaseOrder);
}
