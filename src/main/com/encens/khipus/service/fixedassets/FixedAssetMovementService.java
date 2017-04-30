package com.encens.khipus.service.fixedassets;

import com.encens.khipus.framework.service.GenericService;
import com.encens.khipus.model.fixedassets.*;
import com.encens.khipus.model.purchases.PurchaseOrder;
import com.encens.khipus.util.CurrencyValuesContainer;

import javax.ejb.Local;
import javax.persistence.EntityManager;
import java.util.Date;
import java.util.List;

/**
 * FixedAssetMovementService
 *
 * @author
 * @version 2.1
 */
@Local
public interface FixedAssetMovementService extends GenericService {
    FixedAssetMovement findLastApprovedFixedAssetMovement(Long fixedAssetId);

    CurrencyValuesContainer getMovementsSum(FixedAssetGroupPk fixedAssetGroupId,
                                            Date initDate,
                                            Date endDate,
                                            FixedAssetMovementTypeEnum movementType);

    CurrencyValuesContainer getMovementsSumByGroupAndSubGroup(FixedAssetGroupPk fixedAssetGroupId,
                                                              FixedAssetSubGroupPk fixedAssetSubGroupPk,
                                                              Date initDate,
                                                              Date endDate,
                                                              FixedAssetMovementTypeEnum movementType);

    CurrencyValuesContainer getMovementsSumByFixedAsset(FixedAssetGroupPk fixedAssetGroupId,
                                                        FixedAssetSubGroupPk fixedAssetSubGroupId,
                                                        Long fixedAssetId,
                                                        Date initDate,
                                                        Date endDate,
                                                        FixedAssetMovementTypeEnum movementType);

    CurrencyValuesContainer getMovementsSumUpTo(FixedAssetGroupPk fixedAssetGroupId,
                                                Date upToDate);

    CurrencyValuesContainer getMovementsSumBySubGroupUpTo(FixedAssetGroupPk fixedAssetGroupId,
                                                          FixedAssetSubGroupPk fixedAssetSubGroupId,
                                                          Date upToDate);

    CurrencyValuesContainer getMovementsSumByFixedAssetUpTo(FixedAssetGroupPk fixedAssetGroupId,
                                                            FixedAssetSubGroupPk fixedAssetSubGroupId,
                                                            Long fixedAssetId,
                                                            Date upToDate);

    @SuppressWarnings(value = "unchecked")
    List<FixedAssetMovement> findFixedAssetMovementListByFixedAssetByMovementType(FixedAsset fixedAsset,
                                                                                  FixedAssetMovementTypeEnum fixedAssetMovementTypeEnum);

    Long getNextMovementNumberByFixedAsset(FixedAsset fixedAsset);

    FixedAssetMovement findCancelFixedAssetMovement(Long fixedAssetId);

    @SuppressWarnings(value = "unchecked")
    List<FixedAssetMovement> findFixedAssetMovementByFixedAssetVoucherAndState(FixedAssetVoucher fixedAssetVoucher, FixedAssetMovementState fixedAssetMovementState);

    @SuppressWarnings(value = "unchecked")
    List<FixedAssetMovement> findFixedAssetMovementByPurchaseOrderAndState(PurchaseOrder purchaseOrder, FixedAssetMovementState fixedAssetMovementState, EntityManager entityManager);

    @SuppressWarnings(value = "unchecked")
    List<FixedAssetMovement> findFixedAssetMovementByFixedAssetVoucher(FixedAssetVoucher fixedAssetVoucher, EntityManager entityManager);

    @SuppressWarnings(value = "unchecked")
    FixedAssetMovement findApprovedRegistrationMovement(FixedAsset fixedAsset);

    @SuppressWarnings(value = "unchecked")
    List<FixedAssetMovement> findFixedAssetMovementListByFixedAssetByMovementTypeAndState(FixedAsset fixedAsset,
                                                                                          FixedAssetMovementTypeEnum fixedAssetMovementTypeEnum,
                                                                                          FixedAssetMovementState fixedAssetMovementState);
}