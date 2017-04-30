package com.encens.khipus.service.fixedassets;

import com.encens.khipus.exception.ConcurrencyException;
import com.encens.khipus.exception.EntryDuplicatedException;
import com.encens.khipus.framework.service.GenericServiceBean;
import com.encens.khipus.model.fixedassets.PurchaseOrderDetailPart;
import com.encens.khipus.model.purchases.FixedAssetPurchaseOrderDetail;
import com.encens.khipus.util.BigDecimalUtil;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * @author
 * @version 2.17
 */
@Stateless
@Name("purchaseOrderDetailPartService")
@AutoCreate
public class PurchaseOrderDetailPartServiceBean extends GenericServiceBean implements PurchaseOrderDetailPartService {
    @In(value = "#{listEntityManager}")
    private EntityManager eventEm;

    @SuppressWarnings(value = "unchecked")
    public void managePurchaseOrderDetailParts(FixedAssetPurchaseOrderDetail detail,
                                               List<PurchaseOrderDetailPart> purchaseOrderDetailParts) throws ConcurrencyException {
        log.debug("Managing the PurchaseOrderDetails for purchaseOrderDetail id: " + detail.getId());

        List<PurchaseOrderDetailPart> assignedParts = getEntityManager()
                .createNamedQuery("PurchaseOrderDetailPart.findByDetail")
                .setParameter("detail", detail).getResultList();

        if (null != assignedParts && !assignedParts.isEmpty()) {
            List<PurchaseOrderDetailPart> partsToDelete = new ArrayList<PurchaseOrderDetailPart>();

            List<PurchaseOrderDetailPart> partsToKeep = new ArrayList<PurchaseOrderDetailPart>();

            List<PurchaseOrderDetailPart> partsToUpdate = new ArrayList<PurchaseOrderDetailPart>();

            for (PurchaseOrderDetailPart part : assignedParts) {
                if (!purchaseOrderDetailParts.contains(part)) {
                    partsToDelete.add(part);
                } else {
                    partsToKeep.add(part);
                }
            }

            for (PurchaseOrderDetailPart part : purchaseOrderDetailParts) {
                if (!partsToKeep.contains(part)) {
                    createPurchaseOrderDetailPart(detail, part);
                } else {
                    partsToUpdate.add(part);
                }
            }

            for (PurchaseOrderDetailPart part : partsToDelete) {
                getEntityManager().remove(part);
                getEntityManager().flush();
            }

            for (PurchaseOrderDetailPart part : partsToUpdate) {
                part.setTotalPrice(calculateTotalPrice(detail, part));
                try {
                    super.update(part);
                } catch (EntryDuplicatedException e) {
                    throw new RuntimeException("An Unexpected error has happened ", e);
                }
            }
        } else {
            for (PurchaseOrderDetailPart part : purchaseOrderDetailParts) {
                createPurchaseOrderDetailPart(detail, part);
            }
        }
    }

    @SuppressWarnings(value = "unchecked")
    public List<PurchaseOrderDetailPart> readParts(FixedAssetPurchaseOrderDetail detail) {
        List<PurchaseOrderDetailPart> assignedParts = getEntityManager()
                .createNamedQuery("PurchaseOrderDetailPart.findByDetail")
                .setParameter("detail", detail).getResultList();

        if (null == assignedParts) {
            assignedParts = new ArrayList<PurchaseOrderDetailPart>();
        }

        return assignedParts;
    }

    private void createPurchaseOrderDetailPart(FixedAssetPurchaseOrderDetail detail,
                                               PurchaseOrderDetailPart part) {
        log.debug("Creating new PurchaseOrderDetailPart for FixedAssetPurchaseOrderDetail id: " + detail.getId());

        part.setDetail(detail);
        part.setNumber(getNextNumber(detail));
        part.setTotalPrice(calculateTotalPrice(detail, part));

        getEntityManager().persist(part);
        getEntityManager().flush();
    }

    private BigDecimal calculateTotalPrice(FixedAssetPurchaseOrderDetail detail,
                                           PurchaseOrderDetailPart part) {
        return BigDecimalUtil.multiply(BigDecimalUtil.toBigDecimal(detail.getRequestedQuantity()),
                part.getUnitPrice());
    }

    private Long getNextNumber(FixedAssetPurchaseOrderDetail detail) {
        Long next = null;
        try {
            next = (Long) eventEm.createNamedQuery("PurchaseOrderDetailPart.findNextNumber")
                    .setParameter("detail", detail)
                    .getSingleResult();
        } catch (NoResultException e) {
            log.debug("No exist PurchaseOrderDetailParts registered for selected PurchaseOrderDetail id: " + detail.getId());
        }

        if (null == next) {
            return (long) 1;
        }

        return next += 1;
    }
}
