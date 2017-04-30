package com.encens.khipus.service.fixedassets;

import com.encens.khipus.exception.ConcurrencyException;
import com.encens.khipus.framework.service.GenericService;
import com.encens.khipus.model.fixedassets.PurchaseOrderDetailPart;
import com.encens.khipus.model.purchases.FixedAssetPurchaseOrderDetail;

import javax.ejb.Local;
import java.util.List;

/**
 * @author
 * @version 2.17
 */
@Local
public interface PurchaseOrderDetailPartService extends GenericService {

    void managePurchaseOrderDetailParts(FixedAssetPurchaseOrderDetail detail,
                                        List<PurchaseOrderDetailPart> purchaseOrderDetailParts)
            throws ConcurrencyException;

    List<PurchaseOrderDetailPart> readParts(FixedAssetPurchaseOrderDetail detail);
}
