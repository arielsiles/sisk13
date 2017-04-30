package com.encens.khipus.service.fixedassets;

import com.encens.khipus.framework.service.GenericService;
import com.encens.khipus.model.fixedassets.FixedAsset;
import com.encens.khipus.model.fixedassets.PurchaseOrdersFixedAssetCollection;
import com.encens.khipus.model.purchases.PurchaseOrder;

import javax.ejb.Local;
import javax.persistence.EntityManager;
import java.util.List;

/**
 * Encens S.R.L.
 * This class implements the PurchaseOrdersFixedAssetCollection service local interface
 *
 * @author
 * @version 2.26
 */
@Local
public interface PurchaseOrdersFixedAssetCollectionService extends GenericService {

    /**
     * Finds the PurchaseOrderCauseFixedAssetState given a set of parameters
     *
     * @param purchaseOrder the purchaseOrder associated entity
     * @param fixedAsset    the fixedAsset associated entity
     * @param entityManager the entity manager to use in order to execute the query
     * @return an PurchaseOrdersFixedAssetCollection if any or null
     */
    PurchaseOrdersFixedAssetCollection findByPurchaseOrderAndFixedAsset(PurchaseOrder purchaseOrder,
                                                                        FixedAsset fixedAsset,
                                                                        EntityManager entityManager);

    /**
     * Finds the PurchaseOrdersFixedAssetCollections given a purchaseOrder
     *
     * @param purchaseOrder the purchaseOrder associated entity
     * @param entityManager the entity manager to use in order to execute the query
     * @return an list of PurchaseOrdersFixedAssetCollection if any or empty list
     */
    @SuppressWarnings("unchecked")
    List<PurchaseOrdersFixedAssetCollection> findByPurchaseOrder(PurchaseOrder purchaseOrder,
                                                                 EntityManager entityManager);
}