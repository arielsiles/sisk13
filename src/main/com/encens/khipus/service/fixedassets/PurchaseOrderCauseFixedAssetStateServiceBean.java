package com.encens.khipus.service.fixedassets;

import com.encens.khipus.framework.service.GenericServiceBean;
import com.encens.khipus.model.fixedassets.FixedAssetState;
import com.encens.khipus.model.fixedassets.PurchaseOrderCause;
import com.encens.khipus.model.fixedassets.PurchaseOrderCauseFixedAssetState;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import java.util.ArrayList;
import java.util.List;

/**
 * Service implementation of PurchaseOrderCauseFixedAssetStateService
 *
 * @author
 * @version 2.26
 */

@Stateless
@Name("purchaseOrderCauseFixedAssetStateService")
@AutoCreate
public class PurchaseOrderCauseFixedAssetStateServiceBean
        extends GenericServiceBean implements PurchaseOrderCauseFixedAssetStateService {

    @In(value = "#{listEntityManager}")
    private EntityManager listEm;

    /**
     * Finds a list of FixedAssetState of PurchaseOrderCauseFixedAssetState  given a purchaseOrderCause
     *
     * @param purchaseOrderCause the instance which is desired to know if it is associated to FixedAssetStates
     * @param entityManager      the entity manager to use in order to execute the query
     * @return a list of FixedAssetStates associated to the purchaseOrderCause
     */
    @SuppressWarnings(value = "unchecked")
    public List<FixedAssetState> findFixedAssetStateByPurchaseOrderCause(PurchaseOrderCause purchaseOrderCause, EntityManager entityManager) {

        try {
            if (null == entityManager) {
                entityManager = getEntityManager();
            }

            return entityManager.createNamedQuery("PurchaseOrderCauseFixedAssetState.findFixedAssetStatesByPurchaseOrderCause")
                    .setParameter("purchaseOrderCause", purchaseOrderCause).getResultList();
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<FixedAssetState>();
        }
    }


    /**
     * Finds the PurchaseOrderCauseFixedAssetState given a set of parameters
     *
     * @param purchaseOrderCause the purchaseOrderCause associated entity
     * @param fixedAssetState    the state property
     * @param entityManager      the entity manager to use in order to execute the query
     * @return an PurchaseOrderCauseFixedAssetState if any or null
     */
    public PurchaseOrderCauseFixedAssetState findByPurchaseOrderCauseAndFixedAssetState(PurchaseOrderCause purchaseOrderCause,
                                                                                        FixedAssetState fixedAssetState,
                                                                                        EntityManager entityManager) {
        try {
            if (null == entityManager) {
                entityManager = getEntityManager();
            }

            return (PurchaseOrderCauseFixedAssetState) entityManager.createNamedQuery("PurchaseOrderCauseFixedAssetState.findFixedAssetStateByPurchaseOrderCause")
                    .setParameter("purchaseOrderCause", purchaseOrderCause)
                    .setParameter("state", fixedAssetState).getSingleResult();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    /**
     * Finds PurchaseOrderCauseFixedAssetStates given a purchaseOrderCause
     *
     * @param purchaseOrderCause the instance which is desired to know if it is associated to FixedAssetStates
     * @param entityManager      the entity manager to use in order to execute the query
     * @return a list of PurchaseOrderCauseFixedAssetState associated to the purchaseOrderCause
     */
    @SuppressWarnings(value = "unchecked")
    public List<PurchaseOrderCauseFixedAssetState> findByPurchaseOrderCause(PurchaseOrderCause purchaseOrderCause, EntityManager entityManager) {
        try {
            if (null == entityManager) {
                entityManager = getEntityManager();
            }

            return entityManager.createNamedQuery("PurchaseOrderCauseFixedAssetState.findByPurchaseOrderCause")
                    .setParameter("purchaseOrderCause", purchaseOrderCause).getResultList();
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<PurchaseOrderCauseFixedAssetState>();
        }
    }

}