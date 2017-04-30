package com.encens.khipus.service.fixedassets;

import com.encens.khipus.framework.service.GenericServiceBean;
import com.encens.khipus.model.fixedassets.FixedAsset;
import com.encens.khipus.model.fixedassets.PurchaseOrdersFixedAssetCollection;
import com.encens.khipus.model.purchases.PurchaseOrder;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import java.util.ArrayList;
import java.util.List;

/**
 * Service implementation of PurchaseOrderCauseService
 *
 * @author
 * @version 2.26
 */

@Stateless
@Name("purchaseOrdersFixedAssetCollectionService")
@AutoCreate
public class PurchaseOrdersFixedAssetCollectionServiceBean extends GenericServiceBean implements PurchaseOrdersFixedAssetCollectionService {
    @In(value = "#{listEntityManager}")
    private EntityManager listEm;


    /**
     * Finds the PurchaseOrderCauseFixedAssetState given a set of parameters
     *
     * @param purchaseOrder the purchaseOrder associated entity
     * @param fixedAsset    the fixedAsset associated entity
     * @param entityManager the entity manager to use in order to execute the query
     * @return an PurchaseOrdersFixedAssetCollection if any or null
     */
    public PurchaseOrdersFixedAssetCollection findByPurchaseOrderAndFixedAsset(PurchaseOrder purchaseOrder,
                                                                               FixedAsset fixedAsset,
                                                                               EntityManager entityManager) {
        try {
            if (null == entityManager) {
                entityManager = getEntityManager();
            }

            return (PurchaseOrdersFixedAssetCollection) entityManager.createNamedQuery("PurchaseOrdersFixedAssetCollection.findByPurchaseOrderAndFixedAsset")
                    .setParameter("purchaseOrder", purchaseOrder)
                    .setParameter("fixedAsset", fixedAsset).getSingleResult();
        } catch (Exception e) {
            log.error("Entity was not found", e);
            return null;
        }
    }

    /**
     * Finds the PurchaseOrdersFixedAssetCollections given a purchaseOrder
     *
     * @param purchaseOrder the purchaseOrder associated entity
     * @param entityManager the entity manager to use in order to execute the query
     * @return an list of PurchaseOrdersFixedAssetCollection if any or empty list
     */
    @SuppressWarnings("unchecked")
    public List<PurchaseOrdersFixedAssetCollection> findByPurchaseOrder(PurchaseOrder purchaseOrder,
                                                                        EntityManager entityManager) {
        List<PurchaseOrdersFixedAssetCollection> result = new ArrayList<PurchaseOrdersFixedAssetCollection>();
        try {
            if (null == entityManager) {
                entityManager = getEntityManager();
            }
            return entityManager.createNamedQuery("PurchaseOrdersFixedAssetCollection.findByPurchaseOrder")
                    .setParameter("purchaseOrder", purchaseOrder).getResultList();
        } catch (Exception e) {
            e.printStackTrace();
            return result;
        }
    }
}