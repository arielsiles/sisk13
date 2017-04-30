package com.encens.khipus.service.fixedassets;

import com.encens.khipus.framework.service.GenericService;
import com.encens.khipus.model.fixedassets.FixedAssetState;
import com.encens.khipus.model.fixedassets.PurchaseOrderCause;
import com.encens.khipus.model.fixedassets.PurchaseOrderCauseFixedAssetState;

import javax.ejb.Local;
import javax.persistence.EntityManager;
import java.util.List;

/**
 * Encens S.R.L.
 * This class implements the PurchaseOrderCauseFixedAssetState service local interface
 *
 * @author
 * @version 2.26
 */
@Local
public interface PurchaseOrderCauseFixedAssetStateService extends GenericService {

    /**
     * Finds a list of FixedAssetState of PurchaseOrderCauseFixedAssetState  given a purchaseOrderCause
     *
     * @param purchaseOrderCause the instance which is desired to know if it is associated to FixedAssetStates
     * @param entityManager      the entity manager to use in order to execute the query
     * @return a list of FixedAssetStates associated to the purchaseOrderCause
     */
    @SuppressWarnings(value = "unchecked")
    List<FixedAssetState> findFixedAssetStateByPurchaseOrderCause(PurchaseOrderCause purchaseOrderCause, EntityManager entityManager);

    /**
     * Finds the PurchaseOrderCauseFixedAssetState given a set of parameters
     *
     * @param purchaseOrderCause the purchaseOrderCause associated entity
     * @param fixedAssetState    the state property
     * @param entityManager      the entity manager to use in order to execute the query
     * @return an PurchaseOrderCauseFixedAssetState if any or null
     */
    PurchaseOrderCauseFixedAssetState findByPurchaseOrderCauseAndFixedAssetState(PurchaseOrderCause purchaseOrderCause,
                                                                                 FixedAssetState fixedAssetState,
                                                                                 EntityManager entityManager);

    /**
     * Finds PurchaseOrderCauseFixedAssetStates given a purchaseOrderCause
     *
     * @param purchaseOrderCause the instance which is desired to know if it is associated to FixedAssetStates
     * @param entityManager      the entity manager to use in order to execute the query
     * @return a list of PurchaseOrderCauseFixedAssetState associated to the purchaseOrderCause
     */
    @SuppressWarnings(value = "unchecked")
    List<PurchaseOrderCauseFixedAssetState> findByPurchaseOrderCause(PurchaseOrderCause purchaseOrderCause, EntityManager entityManager);
}