package com.encens.khipus.service.fixedassets;

import com.encens.khipus.exception.ConcurrencyException;
import com.encens.khipus.exception.EntryDuplicatedException;
import com.encens.khipus.exception.finances.CompanyConfigurationNotFoundException;
import com.encens.khipus.exception.finances.FinancesCurrencyNotFoundException;
import com.encens.khipus.exception.finances.FinancesExchangeRateNotFoundException;
import com.encens.khipus.exception.fixedassets.*;
import com.encens.khipus.framework.service.GenericService;
import com.encens.khipus.model.fixedassets.FixedAsset;
import com.encens.khipus.model.fixedassets.FixedAssetMovement;
import com.encens.khipus.model.fixedassets.FixedAssetPayment;
import com.encens.khipus.model.fixedassets.FixedAssetVoucher;

import javax.ejb.Local;
import javax.ejb.TransactionAttribute;
import java.util.HashMap;
import java.util.List;

import static javax.ejb.TransactionAttributeType.REQUIRES_NEW;

/**
 * Encens S.R.L.
 * This class implements the FixedAssetVoucher service local interface
 *
 * @author
 * @version 2.24
 */
@Local
public interface FixedAssetVoucherService extends GenericService {

    FixedAssetVoucher findFixedAssetVoucher(Long id);

    @TransactionAttribute(REQUIRES_NEW)
    void annulFixedAssetVoucher(FixedAssetVoucher fixedAssetVoucher)
            throws FixedAssetVoucherApprovedException,
            ConcurrencyException, EntryDuplicatedException,
            FixedAssetVoucherAnnulledException;

    void registration(FixedAssetVoucher fixedAssetVoucher)
            throws EntryDuplicatedException,
            DuplicatedFixedAssetCodeException, CompanyConfigurationNotFoundException;

    void transference(FixedAssetVoucher fixedAssetVoucher, List<FixedAsset> selectedFixedAssetList)
            throws ConcurrencyException, EntryDuplicatedException, FixedAssetInvalidStateException;

    void approveRegistration(FixedAssetVoucher fixedAssetVoucher)
            throws EntryDuplicatedException,
            DuplicatedFixedAssetCodeException, CompanyConfigurationNotFoundException, FixedAssetVoucherApprovedException, FixedAssetVoucherAnnulledException, FixedAssetPurchaseOrderAlreadyRegisteredByAnotherFixedAssetVoucherException, FinancesCurrencyNotFoundException, FinancesExchangeRateNotFoundException;

    void approveTransference(FixedAssetVoucher fixedAssetVoucher, List<FixedAsset> selectedFixedAssetList)
            throws ConcurrencyException, EntryDuplicatedException, FixedAssetVoucherApprovedException, FixedAssetVoucherAnnulledException, FixedAssetInvalidStateException, FixedAssetMovementInvalidStateException;

    void updateFixedAssetVoucher(FixedAssetVoucher fixedAssetVoucher, List<FixedAsset> selectedFixedAssetList)
            throws ConcurrencyException, EntryDuplicatedException, FixedAssetVoucherApprovedException, FixedAssetVoucherAnnulledException, FixedAssetInvalidStateException, FixedAssetMovementInvalidStateException;

    void discharge(FixedAssetVoucher fixedAssetVoucher, List<FixedAsset> selectedFixedAssetList)
            throws ConcurrencyException, EntryDuplicatedException, FixedAssetInvalidStateException;

    void approveDischarge(FixedAssetVoucher fixedAssetVoucher, List<FixedAsset> selectedFixedAssetList)
            throws ConcurrencyException, EntryDuplicatedException, FixedAssetVoucherApprovedException, FixedAssetVoucherAnnulledException, FixedAssetInvalidStateException, FixedAssetMovementInvalidStateException;

    void approveImprovement(FixedAssetVoucher fixedAssetVoucher, List<FixedAsset> selectedFixedAssetList, FixedAssetPayment fixedAssetPayment)
            throws ConcurrencyException, EntryDuplicatedException, FixedAssetVoucherApprovedException, FixedAssetVoucherAnnulledException, FixedAssetInvalidStateException, FixedAssetMovementInvalidStateException;

    void improve(FixedAssetVoucher fixedAssetVoucher, HashMap<FixedAsset, FixedAssetMovement> selectedFixedAssetListMap, FixedAssetPayment fixedAssetPayment)
            throws ConcurrencyException, EntryDuplicatedException, FixedAssetInvalidStateException;

    Boolean isFixedAssetVoucherNullified(FixedAssetVoucher instance);
}