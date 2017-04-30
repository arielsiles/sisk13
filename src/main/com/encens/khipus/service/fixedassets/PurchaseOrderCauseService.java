package com.encens.khipus.service.fixedassets;

import com.encens.khipus.exception.ConcurrencyException;
import com.encens.khipus.exception.EntryDuplicatedException;
import com.encens.khipus.exception.ReferentialIntegrityException;
import com.encens.khipus.framework.service.GenericService;
import com.encens.khipus.model.fixedassets.FixedAssetState;
import com.encens.khipus.model.fixedassets.PurchaseOrderCause;

import javax.ejb.Local;
import javax.ejb.TransactionAttribute;
import java.util.List;

import static javax.ejb.TransactionAttributeType.REQUIRES_NEW;

/**
 * Encens S.R.L.
 * This class implements the PurchaseOrderCause service local interface
 *
 * @author
 * @version 2.26
 */
@Local
public interface PurchaseOrderCauseService extends GenericService {

    /**
     * Creates a new instance of PurchaseOrderCause, based on instance info and a list of FixedAssetState
     *
     * @param purchaseOrderCause  the instance to persist
     * @param fixedAssetStateList list of FixedAssetState associated to the instance
     * @throws EntryDuplicatedException persistence exception
     */
    @TransactionAttribute(REQUIRES_NEW)
    void create(PurchaseOrderCause purchaseOrderCause, List<FixedAssetState> fixedAssetStateList)
            throws EntryDuplicatedException;

    /**
     * Updates purchaseOrderCause instance and its associated FixedAssetStates
     *
     * @param purchaseOrderCause          the instance to update
     * @param selectedFixedAssetStateList associated FixedAssetStates list
     * @throws com.encens.khipus.exception.ConcurrencyException
     *          concurrency
     * @throws com.encens.khipus.exception.EntryDuplicatedException
     *          persistence exception
     */
    @TransactionAttribute(REQUIRES_NEW)
    void updatePurchaseOrderCause(PurchaseOrderCause purchaseOrderCause, List<FixedAssetState> selectedFixedAssetStateList)
            throws ConcurrencyException, EntryDuplicatedException;

    @TransactionAttribute(REQUIRES_NEW)
    void deletePurchaseOrderCause(PurchaseOrderCause purchaseOrderCause)
            throws ConcurrencyException, ReferentialIntegrityException;
}