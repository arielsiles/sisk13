package com.encens.khipus.service.fixedassets;

import com.encens.khipus.framework.service.GenericServiceBean;
import com.encens.khipus.model.fixedassets.FixedAsset;
import com.encens.khipus.model.fixedassets.FixedAssetPart;
import com.encens.khipus.model.fixedassets.PurchaseOrderDetailPart;
import com.encens.khipus.model.fixedassets.PurchaseOrderFixedAssetPart;
import com.encens.khipus.model.purchases.FixedAssetPurchaseOrderDetail;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author
 * @version 2.17
 */
@Stateless
@Name("fixedAssetPartService")
@AutoCreate
public class FixedAssetPartServiceBean extends GenericServiceBean implements FixedAssetPartService {
    @In(value = "#{listEntityManager}")
    private EntityManager eventEm;

    public Long getNextNumber(FixedAsset fixedAsset) {
        Long next = null;
        try {
            next = (Long) eventEm.createNamedQuery("FixedAssetPart.findNextNumber")
                    .setParameter("fixedAsset", fixedAsset)
                    .getSingleResult();
        } catch (NoResultException e) {
            log.debug("No exist FixedAssetParts registered for selected FixedAsset id: " + fixedAsset.getId());
        }

        if (null == next) {
            return (long) 1;
        }

        return next += 1;
    }

    @SuppressWarnings(value = "unchecked")
    public List<FixedAssetPart> readFixedAssetParts(FixedAsset fixedAsset) {
        List<FixedAssetPart> assignedPars = getEntityManager()
                .createNamedQuery("FixedAssetPart.findByFixedAsset")
                .setParameter("fixedAsset", fixedAsset)
                .getResultList();
        if (null == assignedPars) {
            assignedPars = new ArrayList<FixedAssetPart>();
        }

        return assignedPars;
    }

    @SuppressWarnings(value = "unchecked")
    public void createFixedAssetParts(FixedAsset fixedAsset, FixedAssetPurchaseOrderDetail detail) {
        log.debug("Creating a new fixedAssetParts for FixedAssetPurchaseOrderDetail id:" + detail.getId());
        List<PurchaseOrderDetailPart> assignedParts = getEntityManager()
                .createNamedQuery("PurchaseOrderDetailPart.findByDetail")

                .setParameter("detail", detail).getResultList();
        if (null != assignedParts) {
            for (int i = 0; i < assignedParts.size(); i++) {
                PurchaseOrderDetailPart detailPart = assignedParts.get(i);

                FixedAssetPart part = new FixedAssetPart();
                part.setFixedAsset(fixedAsset);
                part.setNumber(detailPart.getNumber());
                part.setDescription(detailPart.getDescription());
                part.setUnitPrice(detailPart.getUnitPrice());
                part.setMeasureUnit(detailPart.getMeasureUnit());
                part.setCompany(detailPart.getCompany());

                getEntityManager().persist(part);
                getEntityManager().flush();
            }
        }
    }

    @SuppressWarnings({"ForLoopReplaceableByForEach"})
    public void createFixedAssetParts(List<PurchaseOrderFixedAssetPart> fixedAssetPartList) {
        for (int i = 0; i < fixedAssetPartList.size(); i++) {
            PurchaseOrderFixedAssetPart fixedAssetPart = fixedAssetPartList.get(i);
            FixedAssetPart part = new FixedAssetPart();
            part.setFixedAsset(fixedAssetPart.getFixedAsset());
            part.setNumber(getNextNumber(fixedAssetPart.getFixedAsset()));
            part.setDescription(fixedAssetPart.getDescription());
            part.setUnitPrice(fixedAssetPart.getUnitPrice());
            part.setMeasureUnit(fixedAssetPart.getMeasureUnit());
            part.setSerialNumber(fixedAssetPart.getSerialNumber());
            part.setCompany(fixedAssetPart.getCompany());
            getEntityManager().persist(part);
            getEntityManager().flush();
        }
    }

    @SuppressWarnings(value = "unchecked")
    public void manageFixedAssetParts(FixedAsset fixedAsset,
                                      List<FixedAssetPart> fixedAssetParts) {
        log.debug("Managing the FixedAssetParts for fixedAsset id:" + fixedAsset.getId());

        List<FixedAssetPart> assignedPars = getEntityManager()
                .createNamedQuery("FixedAssetPart.findByFixedAsset")
                .setParameter("fixedAsset", fixedAsset)
                .getResultList();

        if (null != assignedPars && !assignedPars.isEmpty()) {
            List<FixedAssetPart> partsToDelete = new ArrayList<FixedAssetPart>();

            List<FixedAssetPart> partsToKeep = new ArrayList<FixedAssetPart>();

            for (FixedAssetPart fixedAssetPart : assignedPars) {
                if (!fixedAssetParts.contains(fixedAssetPart)) {
                    partsToDelete.add(fixedAssetPart);
                } else {
                    partsToKeep.add(fixedAssetPart);
                }
            }

            for (FixedAssetPart part : fixedAssetParts) {
                if (!partsToKeep.contains(part)) {
                    createFixedAssetPart(fixedAsset, part);
                }
            }

            for (FixedAssetPart part : partsToDelete) {
                getEntityManager().remove(part);
                getEntityManager().flush();
            }
        } else {
            for (FixedAssetPart part : fixedAssetParts) {
                createFixedAssetPart(fixedAsset, part);
            }
        }

    }

    private void createFixedAssetPart(FixedAsset fixedAsset, FixedAssetPart fixedAssetPart) {
        log.debug("Creating new fixedAssetPart for fixedAsset id: " + fixedAsset.getId());

        fixedAssetPart.setFixedAsset(fixedAsset);
        fixedAssetPart.setNumber(getNextNumber(fixedAsset));

        getEntityManager().persist(fixedAssetPart);
        getEntityManager().flush();
    }
}
