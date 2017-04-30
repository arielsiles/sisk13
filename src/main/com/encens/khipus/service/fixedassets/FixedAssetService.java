package com.encens.khipus.service.fixedassets;

import com.encens.khipus.action.fixedassets.FixedAssetDataModel;
import com.encens.khipus.exception.ConcurrencyException;
import com.encens.khipus.exception.EntryDuplicatedException;
import com.encens.khipus.exception.finances.CompanyConfigurationNotFoundException;
import com.encens.khipus.exception.finances.FinancesCurrencyNotFoundException;
import com.encens.khipus.exception.finances.FinancesExchangeRateNotFoundException;
import com.encens.khipus.exception.fixedassets.*;
import com.encens.khipus.framework.service.GenericService;
import com.encens.khipus.model.admin.BusinessUnit;
import com.encens.khipus.model.employees.Employee;
import com.encens.khipus.model.finances.Voucher;
import com.encens.khipus.model.fixedassets.*;
import com.encens.khipus.model.purchases.PurchaseOrder;

import javax.ejb.Local;
import javax.persistence.EntityManager;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * FixedAssetService
 *
 * @author
 * @version 2.26
 */
@Local
public interface FixedAssetService extends GenericService {

    void depreciate(List<Integer> result, String gloss) throws FinancesCurrencyNotFoundException, FinancesExchangeRateNotFoundException,
            OutOfDateException, ConcurrencyException, EntryDuplicatedException, ThereIsNoActualFixedAssetException, CompanyConfigurationNotFoundException;

    void closeActualMonth() throws DateBeforeModuleMonthException, ConcurrencyException, EntryDuplicatedException, ThereAreActualFixedAssetException, ThereAreNotAdjustedFixedAssetException;

    boolean dischargableFixedAsset(FixedAsset fixedAsset);

    void transferCustodianFixedAssets(Employee custodian, String newCostCenter, BusinessUnit newBusinessUnit) throws ConcurrencyException, EntryDuplicatedException;

    void approveRegistration(FixedAsset fixedAsset,
                             String gloss,
                             FixedAssetMovement fixedAssetMovement,
                             FixedAssetMovementType fixedAssetMovementType,
                             FixedAssetPayment fixedAssetPayment,
                             List<FixedAssetPart> fixedAssetParts,
                             Boolean payNow
    ) throws EntryDuplicatedException,
            DuplicatedFixedAssetCodeException;

    void update(FixedAsset fixedAsset) throws ConcurrencyException, EntryDuplicatedException, DuplicatedFixedAssetCodeException;

    void dischargeFixedAsset
            (FixedAsset fixedAsset, String gloss, FixedAssetMovement fixedAssetMovement,
             FixedAssetMovementType fixedAssetMovementType)
            throws ConcurrencyException, EntryDuplicatedException, CompanyConfigurationNotFoundException;

    void positiveImprovementFixedAsset(FixedAsset instance, String gloss, FixedAssetMovement fixedAssetMovement, FixedAssetMovementType fixedAssetMovementType, FixedAssetPayment fixedAssetPayment) throws ConcurrencyException, EntryDuplicatedException;

    boolean hasCustodianInDataBase(FixedAsset fixedAsset);

    boolean hasFixedAssetCodeInDataBase(FixedAsset fixedAsset);

    FixedAsset findFixedAssetByCode(FixedAsset fixedAsset);

    FixedAsset getDataBaseFixedAsset(FixedAsset fixedAsset);

    void transference(FixedAsset fixedAsset, FixedAssetMovementType fixedAssetMovementType)
            throws ConcurrencyException, EntryDuplicatedException, NoChangeForTransferenceException;

    List<FixedAsset> findFixedAssetsByState(FixedAssetState state);

    @SuppressWarnings(value = "unchecked")
    List<FixedAsset> findTdpFixedAssetsToAdjust(FixedAssetState state, Date adjustDate);

    Integer changeGuaranty(FixedAssetDataModel fixedAssetDataModel, Integer monthsGuaranty);

    Boolean validateFixedAssetCode(Long fixedAssetCode);

    Boolean validateGroupCode(String groupCode);

    Boolean validateSubGroupCode(String fixedAssetGroupCode, String fixedAssetSubGroupCode);

    @SuppressWarnings(value = "unchecked")
    List<FixedAsset> findFixedAssetByPurchaseOrderAndState(PurchaseOrder purchaseOrder, FixedAssetState fixedAssetState);

    void fillFixedAssetDefaultValues(FixedAsset fixedAsset);

    Voucher createAccountEntryForApprovedFixedAssets(FixedAsset fixedAsset, String gloss) throws CompanyConfigurationNotFoundException;

    @SuppressWarnings(value = "unchecked")
    List<FixedAsset> findFixedAssetListByFixedAssetVoucherAndMovementState(FixedAssetVoucher fixedAssetVoucher, FixedAssetMovementState fixedAssetMovementState, EntityManager entityManager);

    FixedAsset findFixedAsset(FixedAsset fixedAsset);

    @SuppressWarnings(value = "unchecked")
    List<FixedAsset> findFixedAssetListByFixedAssetVoucher(FixedAssetVoucher fixedAssetVoucher, EntityManager entityManager);

    @SuppressWarnings(value = "unchecked")
    List<FixedAsset> findFixedAssetListByFixedAssetPurchaseOrder(PurchaseOrder fixedAssetPurchaseOrder, EntityManager entityManager);

    BigDecimal findFADischargedBeforeLifetime(Integer businessUnitId);

    BigDecimal findRegisteredFixedAssets(Integer businessUnitId);

    FixedAsset generateCodes(FixedAsset fixedAsset);

    FixedAsset findFixedAssetByCode(String fixedAssetGroupCode, String fixedAssetSubGroupCode, Long fixedAssetCode);

    FixedAsset findFixedAssetByBarCode(String barCode);
}
