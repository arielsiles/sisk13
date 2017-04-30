package com.encens.khipus.service.fixedassets;

import com.encens.khipus.exception.ConcurrencyException;
import com.encens.khipus.exception.EntryDuplicatedException;
import com.encens.khipus.exception.ReferentialIntegrityException;
import com.encens.khipus.exception.fixedassets.PurchaseOrderFixedAssetPartNotFoundException;
import com.encens.khipus.exception.purchase.PurchaseOrderApprovedException;
import com.encens.khipus.exception.purchase.PurchaseOrderFinalizedException;
import com.encens.khipus.exception.purchase.PurchaseOrderLiquidatedException;
import com.encens.khipus.exception.purchase.PurchaseOrderNullifiedException;
import com.encens.khipus.framework.service.GenericServiceBean;
import com.encens.khipus.model.fixedassets.PurchaseOrderFixedAssetPart;
import com.encens.khipus.model.purchases.PurchaseOrder;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import java.util.List;

/**
 * @author
 * @version 3.3
 */
@Name("purchaseOrderFixedAssetPartService")
@AutoCreate
@Stateless
public class PurchaseOrderFixedAssetPartServiceBean extends GenericServiceBean implements PurchaseOrderFixedAssetPartService {
    @In(value = "#{listEntityManager}")
    private EntityManager listEm;
    @In
    private FixedAssetPurchaseOrderService fixedAssetPurchaseOrderService;

    public PurchaseOrderFixedAssetPart findById(Long id) throws PurchaseOrderFixedAssetPartNotFoundException {
        PurchaseOrderFixedAssetPart result = null;
        try {
            result = (PurchaseOrderFixedAssetPart) getEntityManager().createNamedQuery("PurchaseOrderFixedAssetPart.findAllById")
                    .setParameter("id", id).getSingleResult();
        } catch (NoResultException e) {
            throw new PurchaseOrderFixedAssetPartNotFoundException();
        }
        return result;
    }

    public void createPurchaseOrderFixedAssetPart(PurchaseOrderFixedAssetPart entity,
                                                  PurchaseOrder purchaseOrder)
            throws PurchaseOrderApprovedException,
            PurchaseOrderFinalizedException,
            PurchaseOrderNullifiedException,
            ConcurrencyException,
            PurchaseOrderLiquidatedException {

        if (fixedAssetPurchaseOrderService.canChangePurchaseOrder(purchaseOrder)) {
            entity.setPurchaseOrder(purchaseOrder);
            try {
                super.create(entity);
            } catch (EntryDuplicatedException e) {
                throw new RuntimeException("An Unexpected error has happened ", e);
            }
            getEntityManager().refresh(entity);
            try {
                fixedAssetPurchaseOrderService.updateFixedAssetPurchaseOrder(purchaseOrder, null);
            } catch (EntryDuplicatedException e) {
                log.error(e, "Un unexpected error has happened. Since the fixedAssetList is null this exception should never happen managing this list");
            }
        }
    }

    public void updatePurchaseOrderFixedAssetPart(PurchaseOrderFixedAssetPart entity)
            throws PurchaseOrderFinalizedException,
            ConcurrencyException,
            PurchaseOrderFixedAssetPartNotFoundException,
            PurchaseOrderApprovedException,
            PurchaseOrderNullifiedException,
            PurchaseOrderLiquidatedException,
            EntryDuplicatedException {

        findInDataBase(entity.getId());

        PurchaseOrder purchaseOrder = entity.getPurchaseOrder();

        if (fixedAssetPurchaseOrderService.canChangePurchaseOrder(purchaseOrder)) {
            super.update(entity);
            getEntityManager().refresh(entity);
            try {
                fixedAssetPurchaseOrderService.updateFixedAssetPurchaseOrder(purchaseOrder, null);
            } catch (EntryDuplicatedException e) {
                log.error(e, "Un unexpected error has happened. Since the fixedAssetList is null this exception should never happen managing this list");
            }
        }
    }

    public void deletePurchaseOrderFixedAssetPart(PurchaseOrderFixedAssetPart entity)
            throws PurchaseOrderFinalizedException,
            PurchaseOrderApprovedException,
            ReferentialIntegrityException,
            PurchaseOrderFixedAssetPartNotFoundException,
            PurchaseOrderNullifiedException,
            ConcurrencyException,
            PurchaseOrderLiquidatedException {

        findInDataBase(entity.getId());

        PurchaseOrder purchaseOrder = entity.getPurchaseOrder();

        if (fixedAssetPurchaseOrderService.canChangePurchaseOrder(purchaseOrder)) {
            super.delete(entity);
            try {
                fixedAssetPurchaseOrderService.updateFixedAssetPurchaseOrder(purchaseOrder, null);
            } catch (EntryDuplicatedException e) {
                log.error(e, "Un unexpected error has happened. Since the fixedAssetList is null this exception should never happen managing this list");
            }
        }
    }

    @SuppressWarnings({"unchecked"})
    public List<PurchaseOrderFixedAssetPart> getPurchaseOrderFixedAssetPartList(PurchaseOrder purchaseOrder) {
        return getEntityManager()
                .createNamedQuery("PurchaseOrderFixedAssetPart.findByPurchaseOrder")
                .setParameter("purchaseOrder", purchaseOrder)
                .getResultList();
    }

    public Boolean isPurchaseOrderFixedAssetPartEmpty(PurchaseOrder purchaseOrder) {
        Long count = (Long) getEntityManager()
                .createNamedQuery("PurchaseOrderFixedAssetPart.countByPurchaseOrder")
                .setParameter("purchaseOrder", purchaseOrder)
                .getSingleResult();
        return count == null || count == 0;
    }

    private PurchaseOrderFixedAssetPart findInDataBase(Long id) throws PurchaseOrderFixedAssetPartNotFoundException {
        PurchaseOrderFixedAssetPart purchaseOrderFixedAssetPart = listEm.find(PurchaseOrderFixedAssetPart.class, id);
        if (null == purchaseOrderFixedAssetPart) {
            throw new PurchaseOrderFixedAssetPartNotFoundException("Cannot find the PurchaseOrderFixedAssetPart entity for id=" + id);
        }

        return purchaseOrderFixedAssetPart;
    }
}
