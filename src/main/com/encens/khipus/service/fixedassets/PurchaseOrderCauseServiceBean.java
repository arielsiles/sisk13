package com.encens.khipus.service.fixedassets;

import com.encens.khipus.exception.ConcurrencyException;
import com.encens.khipus.exception.EntryDuplicatedException;
import com.encens.khipus.exception.ReferentialIntegrityException;
import com.encens.khipus.framework.service.GenericServiceBean;
import com.encens.khipus.model.fixedassets.FixedAssetState;
import com.encens.khipus.model.fixedassets.PurchaseOrderCause;
import com.encens.khipus.model.fixedassets.PurchaseOrderCauseFixedAssetState;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.persistence.EntityManager;
import javax.persistence.EntityNotFoundException;
import javax.persistence.OptimisticLockException;
import javax.persistence.PersistenceException;
import java.util.List;

import static javax.ejb.TransactionAttributeType.REQUIRES_NEW;

/**
 * Service implementation of PurchaseOrderCauseService
 *
 * @author
 * @version 2.26
 */

@Stateless
@Name("purchaseOrderCauseService")
@AutoCreate
public class PurchaseOrderCauseServiceBean extends GenericServiceBean implements PurchaseOrderCauseService {
    @In(value = "#{listEntityManager}")
    private EntityManager listEm;
    @In
    private PurchaseOrderCauseFixedAssetStateService purchaseOrderCauseFixedAssetStateService;

    /**
     * Creates a new instance of PurchaseOrderCause, based on instance info and a list of FixedAssetState
     *
     * @param purchaseOrderCause  the instance to persist
     * @param fixedAssetStateList list of FixedAssetState associated to the instance
     * @throws EntryDuplicatedException persistence exception
     */
    @TransactionAttribute(REQUIRES_NEW)
    public void create(PurchaseOrderCause purchaseOrderCause, List<FixedAssetState> fixedAssetStateList)
            throws EntryDuplicatedException {
        try {
            purchaseOrderCause.setCode(getNextCodeNumber().intValue());
            getEntityManager().persist(purchaseOrderCause);
            /* Create state list associated to the purchaseOrderCause */
            for (FixedAssetState fixedAssetState : fixedAssetStateList) {
                PurchaseOrderCauseFixedAssetState purchaseOrderCauseFixedAssetState = new PurchaseOrderCauseFixedAssetState();
                purchaseOrderCauseFixedAssetState.setPurchaseOrderCause(purchaseOrderCause);
                purchaseOrderCauseFixedAssetState.setState(fixedAssetState);
                getEntityManager().persist(purchaseOrderCauseFixedAssetState);
            }
            getEntityManager().flush();
        } catch (PersistenceException e) {
            log.debug("Persistence error..", e);
            throw new EntryDuplicatedException();
        }
    }

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
    public void updatePurchaseOrderCause(PurchaseOrderCause purchaseOrderCause, List<FixedAssetState> selectedFixedAssetStateList)
            throws ConcurrencyException, EntryDuplicatedException {
        try {
            if (!getEntityManager().contains(purchaseOrderCause)) {
                getEntityManager().merge(purchaseOrderCause);
            }

            /*List of enums associateds*/
            List<FixedAssetState> fixedAssetStates = purchaseOrderCauseFixedAssetStateService.findFixedAssetStateByPurchaseOrderCause(purchaseOrderCause, null);

            /* Create state list associated to the purchaseOrderCause that where not persistent*/
            for (FixedAssetState fixedAssetState : selectedFixedAssetStateList) {
                if (!fixedAssetStates.contains(fixedAssetState)) {
                    PurchaseOrderCauseFixedAssetState purchaseOrderCauseFixedAssetState = new PurchaseOrderCauseFixedAssetState();
                    purchaseOrderCauseFixedAssetState.setPurchaseOrderCause(purchaseOrderCause);
                    purchaseOrderCauseFixedAssetState.setState(fixedAssetState);
                    getEntityManager().persist(purchaseOrderCauseFixedAssetState);
                }
            }
            for (FixedAssetState fixedAssetState : fixedAssetStates) {
                if (!selectedFixedAssetStateList.contains(fixedAssetState)) {
                    getEntityManager().remove(purchaseOrderCauseFixedAssetStateService
                            .findByPurchaseOrderCauseAndFixedAssetState(purchaseOrderCause, fixedAssetState, null));
                }
            }
            getEntityManager().flush();
        } catch (OptimisticLockException e) {
            throw new ConcurrencyException(e);
        } catch (PersistenceException ee) {
            throw new EntryDuplicatedException(ee);
        }
    }


    @TransactionAttribute(REQUIRES_NEW)
    public void deletePurchaseOrderCause(PurchaseOrderCause purchaseOrderCause)
            throws ConcurrencyException, ReferentialIntegrityException {
        try {
            for (PurchaseOrderCauseFixedAssetState purchaseOrderCauseFixedAssetState : purchaseOrderCauseFixedAssetStateService.findByPurchaseOrderCause(purchaseOrderCause, null)) {
                getEntityManager().remove(purchaseOrderCauseFixedAssetState);
            }
            getEntityManager().remove(purchaseOrderCause);
            getEntityManager().flush();
        } catch (OptimisticLockException e) {
            throw new ConcurrencyException(e);
        } catch (EntityNotFoundException e) {
            throw new ConcurrencyException(e);
        } catch (PersistenceException e) {
            throw new ReferentialIntegrityException(e);
        }
    }

    /**
     * @return The next code that follows Max code number
     */
    public Long getNextCodeNumber() {
        Integer codeNumber = ((Integer) listEm.createNamedQuery("PurchaseOrderCause.maxCode").getSingleResult());
        if (null == codeNumber) {
            codeNumber = 1;
        } else {
            codeNumber++;
        }
        return codeNumber.longValue();
    }
}