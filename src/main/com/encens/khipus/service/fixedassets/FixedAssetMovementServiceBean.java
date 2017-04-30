package com.encens.khipus.service.fixedassets;

import com.encens.khipus.exception.EntryNotFoundException;
import com.encens.khipus.framework.service.GenericServiceBean;
import com.encens.khipus.model.fixedassets.*;
import com.encens.khipus.model.purchases.PurchaseOrder;
import com.encens.khipus.util.CurrencyValuesContainer;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import javax.transaction.UserTransaction;
import java.util.Date;
import java.util.List;

/**
 * FixedAssetMovementServiceBean
 *
 * @author
 * @version 2.0
 */
@Name("fixedAssetMovementService")
@Stateless
@AutoCreate
@TransactionManagement(TransactionManagementType.BEAN)
public class FixedAssetMovementServiceBean extends GenericServiceBean implements FixedAssetMovementService {
    @Resource
    private UserTransaction userTransaction;

    @In(value = "#{listEntityManager}")
    private EntityManager listEm;


    @SuppressWarnings(value = "unchecked")
    public List<FixedAssetMovement> findFixedAssetMovementListByFixedAssetByMovementType(FixedAsset fixedAsset,
                                                                                         FixedAssetMovementTypeEnum fixedAssetMovementTypeEnum) {
        try {
            Query query = getEntityManager().createNamedQuery("FixedAssetMovement.findFixedAssetMovementListByFixedAssetByMovementType");
            query.setParameter("fixedAsset", fixedAsset);
            query.setParameter("fixedAssetMovementTypeEnum", fixedAssetMovementTypeEnum);
            return query.getResultList();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @SuppressWarnings(value = "unchecked")
    public List<FixedAssetMovement> findFixedAssetMovementListByFixedAssetByMovementTypeAndState(FixedAsset fixedAsset,
                                                                                                 FixedAssetMovementTypeEnum fixedAssetMovementTypeEnum,
                                                                                                 FixedAssetMovementState fixedAssetMovementState) {
        try {
            Query query = getEntityManager().createNamedQuery("FixedAssetMovement.findFixedAssetMovementListByFixedAssetByMovementTypeAndState");
            query.setParameter("fixedAsset", fixedAsset)
                    .setParameter("state", fixedAssetMovementState)
                    .setParameter("fixedAssetMovementTypeEnum", fixedAssetMovementTypeEnum);
            return query.getResultList();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @SuppressWarnings(value = "unchecked")
    public List<FixedAssetMovement> findFixedAssetMovementByFixedAssetVoucher(FixedAssetVoucher fixedAssetVoucher, EntityManager entityManager) {
        try {
            if (null == entityManager) {
                entityManager = getEntityManager();
            }
            return entityManager.createNamedQuery("FixedAssetMovement.findFixedAssetMovementByFixedAssetVoucher")
                    .setParameter("fixedAssetVoucher", fixedAssetVoucher).getResultList();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @SuppressWarnings(value = "unchecked")
    public List<FixedAssetMovement> findFixedAssetMovementByFixedAssetVoucherAndState(FixedAssetVoucher fixedAssetVoucher, FixedAssetMovementState fixedAssetMovementState) {
        try {
            return listEm.createNamedQuery("FixedAssetMovement.findFixedAssetMovementByFixedAssetVoucherAndState")
                    .setParameter("fixedAssetVoucher", fixedAssetVoucher)
                    .setParameter("state", fixedAssetMovementState).getResultList();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @SuppressWarnings(value = "unchecked")
    public List<FixedAssetMovement> findFixedAssetMovementByPurchaseOrderAndState(PurchaseOrder purchaseOrder, FixedAssetMovementState fixedAssetMovementState, EntityManager entityManager) {
        try {
            if (null == entityManager) {
                entityManager = getEntityManager();
            }
            return entityManager.createNamedQuery("FixedAssetMovement.findFixedAssetMovementByPurchaseOrderAndState")
                    .setParameter("purchaseOrder", purchaseOrder)
                    .setParameter("state", fixedAssetMovementState).getResultList();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    /**
     * (): This method get the last approved movement for a fixedAsset
     *
     * @param fixedAssetId The movements owner
     * @return A movement (if found)
     */
    public FixedAssetMovement findLastApprovedFixedAssetMovement(Long fixedAssetId) {
        FixedAssetMovement res = null;
        try {
            Query query = getEntityManager().createNamedQuery("FixedAssetMovement.findLastApprovedFixedAssetMovement");
            query.setParameter("fixedAssetId", fixedAssetId);
            res = (FixedAssetMovement) query.getSingleResult();
        } catch (NoResultException nrex) {
            log.debug("No result in the query finding last approved movement.");
        } catch (Exception e) {
            log.error("Error when finding last approved fixedAssetMovement.. ", e);
        }
        return res;
    }

    /**
     * Gets the sum of movements by type, group and date
     *
     * @param fixedAssetGroupId Group
     * @param initDate          init date range
     * @param endDate           end date range
     * @param movementType      MovementTypeEnum
     * @return The sum
     */
    public CurrencyValuesContainer getMovementsSum(FixedAssetGroupPk fixedAssetGroupId,
                                                   Date initDate,
                                                   Date endDate,
                                                   FixedAssetMovementTypeEnum movementType) {
        CurrencyValuesContainer res = new CurrencyValuesContainer();
        try {
            Query query = getEntityManager().createNamedQuery("FixedAssetMovement.getMovementsSum");
            query.setParameter("fixedAssetGroupId", fixedAssetGroupId);
            query.setParameter("initDate", initDate);
            query.setParameter("endDate", endDate);
            query.setParameter("movementType", movementType);
            res = (CurrencyValuesContainer) query.getSingleResult();
        } catch (NoResultException nrex) {
            log.debug("No result in the query getting movements sum for movementType:" + movementType);
        } catch (Exception e) {
            log.error("Error when getting movements sum for movementType:" + movementType, e);
        }
        return res;
    }

    /**
     * Gets the sum of movements by type, group, subgroup and date
     *
     * @param fixedAssetGroupId    Group
     * @param fixedAssetSubGroupId subGroup
     * @param initDate             init date range
     * @param endDate              end date range
     * @param movementType         MovementTypeEnum
     * @return The sum
     */
    public CurrencyValuesContainer getMovementsSumByGroupAndSubGroup(FixedAssetGroupPk fixedAssetGroupId,
                                                                     FixedAssetSubGroupPk fixedAssetSubGroupId,
                                                                     Date initDate,
                                                                     Date endDate,
                                                                     FixedAssetMovementTypeEnum movementType) {
        CurrencyValuesContainer res = new CurrencyValuesContainer();
        try {
            Query query = getEntityManager().createNamedQuery("FixedAssetMovement.getMovementsSumByGroupAndSubGroup");
            query.setParameter("fixedAssetGroupId", fixedAssetGroupId);
            query.setParameter("fixedAssetSubGroupId", fixedAssetSubGroupId);
            query.setParameter("initDate", initDate);
            query.setParameter("endDate", endDate);
            query.setParameter("movementType", movementType);
            res = (CurrencyValuesContainer) query.getSingleResult();
        } catch (NoResultException nrex) {
            log.debug("No result in the query getting movements sum for movementType:" + movementType);
        } catch (Exception e) {
            log.error("Error when getting movements sum for movementType:" + movementType, e);
        }
        return res;
    }

    /**
     * Gets the sum of movements by type, group, subgroup, fixedAsset and date
     *
     * @param fixedAssetGroupId    Group
     * @param fixedAssetSubGroupId subGroup
     * @param fixedAssetId         fixedAssetId
     * @param initDate             init date range
     * @param endDate              end date range
     * @param movementType         MovementTypeEnum
     * @return The sum
     */
    public CurrencyValuesContainer getMovementsSumByFixedAsset(FixedAssetGroupPk fixedAssetGroupId,
                                                               FixedAssetSubGroupPk fixedAssetSubGroupId,
                                                               Long fixedAssetId,
                                                               Date initDate,
                                                               Date endDate,
                                                               FixedAssetMovementTypeEnum movementType) {
        CurrencyValuesContainer res = new CurrencyValuesContainer();
        try {
            Query query = getEntityManager().createNamedQuery("FixedAssetMovement.getMovementsSumByFixedAsset");
            query.setParameter("fixedAssetGroupId", fixedAssetGroupId);
            query.setParameter("fixedAssetSubGroupId", fixedAssetSubGroupId);
            query.setParameter("fixedAssetId", fixedAssetId);
            query.setParameter("initDate", initDate);
            query.setParameter("endDate", endDate);
            query.setParameter("movementType", movementType);
            res = (CurrencyValuesContainer) query.getSingleResult();
        } catch (NoResultException nrex) {
            log.debug("No result in the query getting movements sum for movementType:" + movementType);
        } catch (Exception e) {
            log.error("Error when getting movements sum for movementType:" + movementType, e);
        }
        return res;
    }

    /**
     * Gets the sum of movements for a group before the date
     *
     * @param fixedAssetGroupId Group
     * @param upToDate          Up to Date
     * @return The sum
     */
    public CurrencyValuesContainer getMovementsSumUpTo(FixedAssetGroupPk fixedAssetGroupId,
                                                       Date upToDate) {
        CurrencyValuesContainer res = new CurrencyValuesContainer();
        try {
            Query query = getEntityManager().createNamedQuery("FixedAssetMovement.getMovementsSumBefore");
            query.setParameter("fixedAssetGroupId", fixedAssetGroupId);
            query.setParameter("upToDate", upToDate);
            res = (CurrencyValuesContainer) query.getSingleResult();
        } catch (NoResultException nrex) {
            log.debug("No result in the query getting movements sum before:" + upToDate);
        } catch (Exception e) {
            log.error("Error when getting movements sum before:" + upToDate, e);
        }
        return res;
    }

    /**
     * Get's the movement sum by group and subgroup up to a date
     *
     * @param fixedAssetGroupId    fixedAssetGroupId
     * @param fixedAssetSubGroupId fixedAssetSubGroupId
     * @param upToDate             upToDate
     * @return The movements amount sum in several currencies
     */
    public CurrencyValuesContainer getMovementsSumBySubGroupUpTo(FixedAssetGroupPk fixedAssetGroupId,
                                                                 FixedAssetSubGroupPk fixedAssetSubGroupId,
                                                                 Date upToDate) {
        CurrencyValuesContainer res = new CurrencyValuesContainer();
        try {
            Query query = getEntityManager().createNamedQuery("FixedAssetMovement.getMovementsSumBySubGroupBefore");
            query.setParameter("fixedAssetGroupId", fixedAssetGroupId);
            query.setParameter("upToDate", upToDate);
            query.setParameter("fixedAssetSubGroupId", fixedAssetSubGroupId);
            res = (CurrencyValuesContainer) query.getSingleResult();
        } catch (NoResultException nrex) {
            log.debug("No result in the query getting movements sum before:" + upToDate);
        } catch (Exception e) {
            log.error("Error when getting movements sum before:" + upToDate, e);
        }
        return res;
    }

    /**
     * Get's the movement sum by group and subgroup and fixedAsset up to a date
     *
     * @param fixedAssetGroupId    fixedAssetGroupId
     * @param fixedAssetSubGroupId fixedAssetSubGroupId
     * @param upToDate             upToDate
     * @return The movements amount sum in several currencies
     */
    public CurrencyValuesContainer getMovementsSumByFixedAssetUpTo(FixedAssetGroupPk fixedAssetGroupId,
                                                                   FixedAssetSubGroupPk fixedAssetSubGroupId,
                                                                   Long fixedAssetId,
                                                                   Date upToDate) {
        CurrencyValuesContainer res = new CurrencyValuesContainer();
        try {
            Query query = getEntityManager().createNamedQuery("FixedAssetMovement.getMovementsSumByFixedAssetBefore");
            query.setParameter("fixedAssetGroupId", fixedAssetGroupId);
            query.setParameter("upToDate", upToDate);
            query.setParameter("fixedAssetSubGroupId", fixedAssetSubGroupId);
            query.setParameter("fixedAssetId", fixedAssetId);
            res = (CurrencyValuesContainer) query.getSingleResult();
        } catch (NoResultException nrex) {
            log.debug("No result in the query getting movements sum before:" + upToDate);
        } catch (Exception e) {
            log.error("Error when getting movements sum before:" + upToDate, e);
        }
        return res;
    }

    public Long getNextMovementNumberByFixedAsset(FixedAsset fixedAsset) {
        Long orderNumber = (Long) getEntityManager().createNamedQuery("FixedAssetMovement.countByFixedAsset")
                .setParameter("fixedAsset", fixedAsset)
                .getSingleResult();
        if (null == orderNumber) {
            orderNumber = (long) 1;
        } else {
            orderNumber++;
        }
        return orderNumber;
    }

    /**
     * Find the "cancel" fixed asset movement if exist
     *
     * @param fixedAssetId fixed asset id
     * @return FixedAssetMovement
     */
    public FixedAssetMovement findCancelFixedAssetMovement(Long fixedAssetId) {
        log.debug("Executing findCancelFixedAssetMovement...." + fixedAssetId);
        FixedAssetMovement fixedAssetMovement = null;
        FixedAsset fixedAsset = null;
        try {
            fixedAsset = findById(FixedAsset.class, fixedAssetId);
        } catch (EntryNotFoundException e) {
            log.debug("Not found fixed asset: " + fixedAssetId);
        }

        if (fixedAsset != null) {
            List<FixedAssetMovement> fixedAssetMovementList = findFixedAssetMovementListByFixedAssetByMovementType(fixedAsset, FixedAssetMovementTypeEnum.BAJ);
            if (fixedAssetMovementList != null && !fixedAssetMovementList.isEmpty()) {
                fixedAssetMovement = fixedAssetMovementList.get(0);
            }
        }

        return fixedAssetMovement;
    }

    /**
     * This method get the approved registration movement for a fixedasset
     *
     * @param fixedAsset The FixedAsset
     * @return The fixedAssetMovement
     */
    public FixedAssetMovement findApprovedRegistrationMovement(FixedAsset fixedAsset) {
        FixedAssetMovement res = null;
        try {
            Query query = getEntityManager().createNamedQuery("FixedAssetMovement.findFixedAssetMovementByTypeAndState");
            query.setParameter("fixedAsset", fixedAsset);
            query.setParameter("fixedAssetMovementTypeEnum", FixedAssetMovementTypeEnum.ALT);
            query.setParameter("fixedAssetMovementState", FixedAssetMovementState.APR);
            res = (FixedAssetMovement) query.getSingleResult();
        } catch (NoResultException nrex) {
            log.debug("No result in the query getting approved registration movement..", nrex);
        } catch (Exception e) {
            log.error("Can�t find approved registration movement ..", e);
        }
        return res;
    }
}