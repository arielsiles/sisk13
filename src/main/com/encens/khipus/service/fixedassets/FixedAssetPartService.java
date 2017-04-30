package com.encens.khipus.service.fixedassets;

import com.encens.khipus.framework.service.GenericService;
import com.encens.khipus.model.fixedassets.FixedAsset;
import com.encens.khipus.model.fixedassets.FixedAssetPart;
import com.encens.khipus.model.fixedassets.PurchaseOrderFixedAssetPart;
import com.encens.khipus.model.purchases.FixedAssetPurchaseOrderDetail;

import javax.ejb.Local;
import java.util.List;

/**
 * @author
 * @version 2.17
 */
@Local
public interface FixedAssetPartService extends GenericService {
    Long getNextNumber(FixedAsset fixedAsset);

    List<FixedAssetPart> readFixedAssetParts(FixedAsset fixedAsset);

    void createFixedAssetParts(FixedAsset fixedAsset,
                               FixedAssetPurchaseOrderDetail detail);

    void manageFixedAssetParts(FixedAsset fixedAsset,
                               List<FixedAssetPart> fixedAssetParts);

    void createFixedAssetParts(List<PurchaseOrderFixedAssetPart> fixedAssetPartList);
}
