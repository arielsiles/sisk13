package com.encens.khipus.service.fixedassets;

import com.encens.khipus.action.finances.VoucherDetailType;
import com.encens.khipus.action.fixedassets.FixedAssetDataModel;
import com.encens.khipus.exception.ConcurrencyException;
import com.encens.khipus.exception.EntryDuplicatedException;
import com.encens.khipus.exception.ReferentialIntegrityException;
import com.encens.khipus.exception.finances.CompanyConfigurationNotFoundException;
import com.encens.khipus.exception.finances.FinancesCurrencyNotFoundException;
import com.encens.khipus.exception.finances.FinancesExchangeRateNotFoundException;
import com.encens.khipus.exception.fixedassets.*;
import com.encens.khipus.framework.service.GenericServiceBean;
import com.encens.khipus.interceptor.FinancesUser;
import com.encens.khipus.model.admin.BusinessUnit;
import com.encens.khipus.model.employees.Employee;
import com.encens.khipus.model.finances.*;
import com.encens.khipus.model.fixedassets.*;
import com.encens.khipus.model.purchases.PurchaseOrder;
import com.encens.khipus.model.purchases.PurchaseOrderPaymentType;
import com.encens.khipus.service.accouting.VoucherAccoutingService;
import com.encens.khipus.service.common.SequenceGeneratorService;
import com.encens.khipus.service.finances.FinancesExchangeRateService;
import com.encens.khipus.service.finances.FinancesUserService;
import com.encens.khipus.service.finances.VoucherService;
import com.encens.khipus.util.*;
import com.encens.khipus.util.fixedassets.FixedAssetDefaultConstants;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.End;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.persistence.*;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;
import java.math.BigDecimal;
import java.util.*;

/**
 * FixedAssetServiceBean
 *
 * @author
 * @version 3.5.2.2
 */
@Name("fixedAssetService")
@Stateless
@AutoCreate
@FinancesUser
@TransactionManagement(TransactionManagementType.BEAN)
public class FixedAssetServiceBean extends GenericServiceBean implements FixedAssetService {

    @Resource
    private UserTransaction userTransaction;

    @In(value = "#{entityManager}")
    private EntityManager em;

    @In
    private FixedAssetPartService fixedAssetPartService;

    @In
    private VoucherService voucherService;

    @In
    private VoucherAccoutingService voucherAccoutingService;

    @In
    private SequenceGeneratorService sequenceGeneratorService;

    @In
    private FixedAssetMonthProcessService fixedAssetMonthProcessService;

    @In
    private FinancesUserService financesUserService;

    @In
    private FinancesExchangeRateService financesExchangeRateService;

    @In
    private FixedAssetDepreciationRecordService fixedAssetDepreciationRecordService;

    @In
    private CompanyConfigurationService companyConfigurationService;

    @In
    private FixedAssetMovementService fixedAssetMovementService;

    @In
    protected Map<String, String> messages;

    private static final Integer SCALE = 6;

    @In(value = "#{listEntityManager}")
    private EntityManager listEm;

    public void update(FixedAsset fixedAsset)
            throws ConcurrencyException, EntryDuplicatedException, DuplicatedFixedAssetCodeException {
        FixedAsset databaseFixedAsset = listEm.find(FixedAsset.class, fixedAsset.getId());
        Long dataBaseFixedAssetCode = databaseFixedAsset.getFixedAssetCode();
        /*
        * It is necessary to control duplicity because
        * there is not constraint defined over fixedAssetCode property
        * */
        if (databaseFixedAsset != null && dataBaseFixedAssetCode != null && fixedAsset.getFixedAssetCode() != null &&
                dataBaseFixedAssetCode.longValue() != fixedAsset.getFixedAssetCode().longValue()) {
            throw new DuplicatedFixedAssetCodeException();
        }
        try {
            userTransaction.begin();
            em.joinTransaction();
            getEntityManager().merge(fixedAsset);
            getEntityManager().flush();
            userTransaction.commit();
        } catch (OptimisticLockException e) {
            try {
                userTransaction.rollback();
            } catch (SystemException e1) {
                log.error("An unexpected error have happened rolling back", e1);
            }
            throw new ConcurrencyException(e);
        } catch (PersistenceException ee) {
            try {
                userTransaction.rollback();
            } catch (SystemException e1) {
                log.error("An unexpected error have happened rolling back", e1);
            }
            throw new EntryDuplicatedException(ee);
        } catch (Exception e) {
            log.error("An unexpected error have happened ...", e);
            try {
                userTransaction.rollback();
            } catch (SystemException e1) {
                log.error("An unexpected error have happened rolling back", e1);
            }
            throw new RuntimeException(e);
        }
    }

    public void transference(FixedAsset fixedAsset, FixedAssetMovementType fixedAssetMovementType)
            throws ConcurrencyException, EntryDuplicatedException, NoChangeForTransferenceException {
        FixedAsset databaseFixedAsset = listEm.find(FixedAsset.class, fixedAsset.getId());
        if (!validateTransferenceValues(fixedAsset)) {
            throw new NoChangeForTransferenceException("there isn't any change");
        }
        try {
            userTransaction.begin();
            em.joinTransaction();
            /*create an movement*/
            FixedAssetMovement fixedAssetMovement = new FixedAssetMovement();
            fixedAssetMovement.setMovementDate(new Date());
            fixedAssetMovement.setCreationDate(new Date());
            // Always pendant state
            fixedAssetMovement.setState(FixedAssetMovementState.APR);
            fixedAssetMovement.setCustodian(fixedAsset.getCustodianJobContract().getContract().getEmployee());
            fixedAssetMovement.setCostCenter(fixedAsset.getCostCenter());
            fixedAssetMovement.setBusinessUnit(fixedAsset.getBusinessUnit());
            fixedAssetMovement.setFixedAsset(fixedAsset);
            fixedAssetMovement.setMovementNumber(fixedAssetMovementService.getNextMovementNumberByFixedAsset(fixedAsset));
            fixedAssetMovement.setFixedAssetMovementType(fixedAssetMovementType);
            fixedAssetMovement.setUserNumber(financesUserService.getFinancesUserCode());
            if (!fixedAsset.getCustodianJobContract().getContract().getEmployee().getId().equals(databaseFixedAsset.getCustodianJobContract().getContract().getEmployee().getId())) {
                fixedAssetMovement.setLastCustodian(databaseFixedAsset.getCustodianJobContract().getContract().getEmployee());
            }
            if (!fixedAsset.getBusinessUnit().getId().equals(databaseFixedAsset.getBusinessUnit().getId())) {
                fixedAssetMovement.setLastBusinessUnit(databaseFixedAsset.getBusinessUnit());
            }
            if (!fixedAsset.getCostCenter().getId().getCode().equals(
                    databaseFixedAsset.getCostCenter().getId().getCode())) {
                fixedAssetMovement.setLastCostCenter(databaseFixedAsset.getCostCenter());
            }

            fixedAssetMovement.setLastFixedAssetLocation(databaseFixedAsset.getFixedAssetLocation());
            fixedAssetMovement.setNewFixedAssetLocation(fixedAsset.getFixedAssetLocation());

            getEntityManager().persist(fixedAssetMovement);
            getEntityManager().merge(fixedAsset);
            getEntityManager().flush();
            userTransaction.commit();
        } catch (OptimisticLockException e) {
            try {
                userTransaction.rollback();
            } catch (SystemException e1) {
                log.error("An unexpected error have happened rolling back", e1);
            }
            throw new ConcurrencyException(e);
        } catch (PersistenceException e) {
            try {
                userTransaction.rollback();
            } catch (SystemException e1) {
                log.error("An unexpected error have happened rolling back", e1);
            }
            throw new EntryDuplicatedException(e);
        } catch (Exception e) {
            log.error("An unexpected error have happened ...", e);
            try {
                userTransaction.rollback();
            } catch (SystemException e1) {
                log.error("An unexpected error have happened rolling back", e1);
            }
            throw new RuntimeException(e);
        }
    }

    private boolean validateTransferenceValues(FixedAsset fixedAsset) {
        FixedAsset databaseFixedAsset = listEm.find(FixedAsset.class, fixedAsset.getId());
        return !fixedAsset.getCustodianJobContract().getContract().getEmployee().getId()
                .equals(databaseFixedAsset.getCustodianJobContract().getContract().getEmployee().getId())
                || !fixedAsset.getBusinessUnit().getId().equals(databaseFixedAsset.getBusinessUnit().getId())
                || !fixedAsset.getCostCenter().getId().getCode().equals(
                databaseFixedAsset.getCostCenter().getId().getCode())
                || (databaseFixedAsset.getFixedAssetLocation() == null || !databaseFixedAsset.getFixedAssetLocation().getId().equals(fixedAsset.getFixedAssetLocation().getId()));
    }

    @End
    public void delete(Object entity) throws ConcurrencyException, ReferentialIntegrityException {
        try {
            userTransaction.begin();
            em.joinTransaction();
            getEntityManager().remove(entity);
            getEntityManager().flush();
            userTransaction.commit();
        } catch (OptimisticLockException e) {
            try {
                userTransaction.rollback();
            } catch (SystemException e1) {
                log.error("An unexpected error have happened rolling back", e1);
            }
            throw new ConcurrencyException(e);
        } catch (EntityNotFoundException e) {
            try {
                userTransaction.rollback();
            } catch (SystemException e1) {
                log.error("An unexpected error have happened rolling back", e1);
            }
            throw new ConcurrencyException(e);
        } catch (PersistenceException e) {
            try {
                userTransaction.rollback();
            } catch (SystemException e1) {
                log.error("An unexpected error have happened rolling back", e1);
            }
            throw new ReferentialIntegrityException(e);
        } catch (Exception e) {
            log.error("An unexpected error have happened ...", e);
            try {
                userTransaction.rollback();
            } catch (SystemException e1) {
                log.error("An unexpected error have happened rolling back", e1);
            }
            throw new RuntimeException(e);
        }
    }

    @End
    public void depreciate(List<Integer> result, String gloss)
            throws FinancesCurrencyNotFoundException, FinancesExchangeRateNotFoundException, OutOfDateException,
            ConcurrencyException, EntryDuplicatedException, ThereIsNoActualFixedAssetException, CompanyConfigurationNotFoundException {

        // map that hold info about voucher mappings about Depreciation
        Map<BusinessUnit, Map<CostCenter, Map<CashAccount, Map<VoucherDetailType, BigDecimal>>>> businessUnitDepreciationVoucherMappings = new HashMap<BusinessUnit, Map<CostCenter, Map<CashAccount, Map<VoucherDetailType, BigDecimal>>>>();
        // map that hold info about voucher mappings about Adjust
        Map<BusinessUnit, Map<CostCenter, Map<CashAccount, Map<VoucherDetailType, BigDecimal>>>> businessUnitAdjustVoucherMappings = new HashMap<BusinessUnit, Map<CostCenter, Map<CashAccount, Map<VoucherDetailType, BigDecimal>>>>();

        FinancesModule financesModule = fixedAssetMonthProcessService.getFinancesModule(FixedAssetDefaultConstants.getFixedAssetModulePK());
        Date firstDayOfCurrentProcessMonth = financesModule.getDate();
        Date lastDayOfCurrentProcessMonth = DateUtils.lastDate(firstDayOfCurrentProcessMonth);
        BigDecimal lastDayOfMonthUfvExchangeRate =
                financesExchangeRateService.findLastExchangeRateByCurrency(FinancesCurrencyType.U.name());
        BigDecimal lastDayOfMonthSusExchangeRate =
                financesExchangeRateService.findLastExchangeRateByCurrency(FinancesCurrencyType.D.name());


        /*check current date, do not depreciate if current is less than module date*/
        // check if current date is after depreciation month of the module
        boolean isDateAfterFixedAssetProcessMonthDate = fixedAssetMonthProcessService.isDateAfterFixedAssetMothProcess(new Date());
        /* Comment only for test
        if (!isDateAfterFixedAssetProcessMonthDate) {
            *//*shows a message with a message like the depreciation is not yet applicable because you are still in an earlier date*//*
            throw new OutOfDateException(
                    "The Actual date should be after the last day of the month in process");
        }*/

        /* adjust all the fixedAssets in TDP state whose adjustDate is different to firstDayOfCurrentProcessMonth*/
        List<FixedAsset> totallyDepreciatedFixedAssetList = findTdpFixedAssetsToAdjust(FixedAssetState.TDP, firstDayOfCurrentProcessMonth);

        /* adjust and depreciate all the fixedAssets in VIG state */
        List<FixedAsset> actualFixedAssetList = findActualFixedAssets();
        try {
            userTransaction.begin();
            em.joinTransaction();

            CompanyConfiguration companyConfiguration = companyConfigurationService.findCompanyConfiguration();
            CashAccount adjustmentForInflationCashAccount = companyConfiguration.getAdjustmentForInflationAccount();
            /*depreciate*/
            if (actualFixedAssetList != null && actualFixedAssetList.size() > 0) {
                /* set 30 seconds per actual fixed Asset*/
                userTransaction.setTransactionTimeout(30 * actualFixedAssetList.size());
            }
            /* Adjust left tdp fixed assets*/
            if (!ValidatorUtil.isEmptyOrNull(totallyDepreciatedFixedAssetList)) {
                for (FixedAsset fixedAsset : totallyDepreciatedFixedAssetList) {
                    /*Adjust of the original value*/
                    adjust(fixedAsset.getFixedAssetSubGroup().getOriginalValueCashAccount(), fixedAsset.getUfvOriginalValue(),
                            lastDayOfMonthUfvExchangeRate, fixedAsset.getLastBsUfvRate(),
                            fixedAsset.getCostCenter(), fixedAsset.getBusinessUnit(),
                            adjustmentForInflationCashAccount, true, businessUnitAdjustVoucherMappings);
                    /*Adjust of the accumulatedDepreciation if exists*/
                    if (fixedAsset.getAcumulatedDepreciation() != null && fixedAsset.getAcumulatedDepreciation().compareTo(BigDecimal.ZERO) > 0) {
                        adjust(fixedAsset.getFixedAssetSubGroup().getAccumulatedDepreciationCashAccount(), fixedAsset.getAcumulatedDepreciation(),
                                lastDayOfMonthUfvExchangeRate, fixedAsset.getLastBsUfvRate(),
                                fixedAsset.getCostCenter(), fixedAsset.getBusinessUnit(),
                                adjustmentForInflationCashAccount, false, businessUnitAdjustVoucherMappings);
                    }
                    fixedAsset.setLastBsUfvRate(lastDayOfMonthUfvExchangeRate);
                    fixedAsset.setLastBsSusRate(lastDayOfMonthSusExchangeRate);
                    /* indicates to which date have been adjusted*/
                    fixedAsset.setAdjustDate(firstDayOfCurrentProcessMonth);
                    getEntityManager().merge(fixedAsset);
                    getEntityManager().flush();
                }
            }

            for (FixedAsset fixedAsset : actualFixedAssetList) {
                System.out.println(" ---> AF: " + fixedAsset.getFullName());
                System.out.println("-------------> lastDayOfMonthUfvExchangeRate: " + lastDayOfMonthUfvExchangeRate);
                System.out.println("-------------> fixedAsset.getLastBsUfvRate(): " + fixedAsset.getLastBsUfvRate());
                System.out.println("");

                /*Adjust of the original value*/
                adjust(fixedAsset.getFixedAssetSubGroup().getOriginalValueCashAccount(), fixedAsset.getUfvOriginalValue(),
                        lastDayOfMonthUfvExchangeRate, fixedAsset.getLastBsUfvRate(),
                        fixedAsset.getCostCenter(), fixedAsset.getBusinessUnit(),
                        adjustmentForInflationCashAccount, true, businessUnitAdjustVoucherMappings);
                /*Adjust of the accumulatedDepreciation if exists*/
                if (fixedAsset.getAcumulatedDepreciation() != null && fixedAsset.getAcumulatedDepreciation().compareTo(BigDecimal.ZERO) > 0) {

                    adjust(fixedAsset.getFixedAssetSubGroup().getAccumulatedDepreciationCashAccount(), fixedAsset.getAcumulatedDepreciation(),
                            lastDayOfMonthUfvExchangeRate, fixedAsset.getLastBsUfvRate(),
                            fixedAsset.getCostCenter(), fixedAsset.getBusinessUnit(),
                            adjustmentForInflationCashAccount, false, businessUnitAdjustVoucherMappings);
                }

                fixedAsset.setLastBsUfvRate(lastDayOfMonthUfvExchangeRate);
                fixedAsset.setLastBsSusRate(lastDayOfMonthSusExchangeRate);

                /* indicates to which date have been adjusted*/
                fixedAsset.setAdjustDate(firstDayOfCurrentProcessMonth);
                BigDecimal depreciate;
                /*(vo+mej)*tasa/1200 */
                BigDecimal depreciationCandidate = depreciationCandidate(fixedAsset);
                log.debug("depreciationCandidate (vo+mej)*tasa/1200" + depreciationCandidate);
                /*(VO+MEJ)-DEP_ACUM-DESHECHO*/
                BigDecimal residualCandidate = residualCandidate(fixedAsset);
                log.debug("residualCandidate (VO+MEJ)-DEP_ACUM-DESHECHO" + depreciationCandidate);
                /* if the residualCandidate value is equal or less than depreciation, then you should depreciate the
                   residualCandidate rather than depreciationCandidate*/

                if (residualCandidate.compareTo(depreciationCandidate) <= 0) {
                    depreciate = residualCandidate;
                    fixedAsset.setState(FixedAssetState.TDP);
                } else {
                    depreciate = depreciationCandidate;
                    fixedAsset.setState(FixedAssetState.DEP);
                }

                fixedAsset.setAcumulatedDepreciation(
                        BigDecimalUtil.sum(fixedAsset.getAcumulatedDepreciation(), depreciate));
                fixedAsset.setDepreciation(depreciate);
                getEntityManager().merge(fixedAsset);
                getEntityManager().flush();

                /*createFixedAssetDepreciationRecord*/
                fixedAssetDepreciationRecordService.createFixedAssetDepreciationRecord(fixedAsset, lastDayOfCurrentProcessMonth, lastDayOfMonthUfvExchangeRate);

                // accumulates into the corresponding mapping entry the voucher detail amount
                putInBusinessUnitVoucherMappings(businessUnitDepreciationVoucherMappings,
                        fixedAsset.getBusinessUnit(),
                        fixedAsset.getCostCenter(),
                        fixedAsset.getFixedAssetSubGroup().getExpenseCashAccount(),
                        VoucherDetailType.DEBIT,
                        BigDecimalUtil.multiply(depreciate, lastDayOfMonthUfvExchangeRate));

                // accumulates into the corresponding mapping entry the voucher detail amount
                putInBusinessUnitVoucherMappings(businessUnitDepreciationVoucherMappings,
                        fixedAsset.getBusinessUnit(),
                        fixedAsset.getCostCenter(),
                        fixedAsset.getFixedAssetSubGroup().getAccumulatedDepreciationCashAccount(),
                        VoucherDetailType.CREDIT,
                        BigDecimalUtil.multiply(depreciate, lastDayOfMonthUfvExchangeRate));
            }

            // create the vouchers for depreciation
            if (businessUnitDepreciationVoucherMappings.size() > 0) {
                Voucher depreciationVoucher = createVoucherByDetailMappings(businessUnitDepreciationVoucherMappings, gloss);
                //voucherService.create(depreciationVoucher);
                voucherAccoutingService.saveVoucher(depreciationVoucher);
            }
            if (businessUnitAdjustVoucherMappings.size() > 0) {
                Voucher adjustVoucher = createVoucherByDetailMappings(businessUnitAdjustVoucherMappings, messages.get("FixedAsset.adjustGloss"));
                //voucherService.create(adjustVoucher);
                voucherAccoutingService.saveVoucher(adjustVoucher);
            }

            /*set depreciated size*/
            result.set(0, actualFixedAssetList.size());
            /*set adjusted size*/
            result.set(1, totallyDepreciatedFixedAssetList.size());
            userTransaction.commit();
            userTransaction.setTransactionTimeout(0);
        } catch (OptimisticLockException e) {
            try {
                userTransaction.rollback();
                userTransaction.setTransactionTimeout(0);
            } catch (SystemException e1) {
                log.error("An unexpected error have happened rolling back", e1);
            }
            throw new ConcurrencyException(e);
        } catch (PersistenceException ee) {
            try {
                userTransaction.rollback();
                userTransaction.setTransactionTimeout(0);
            } catch (SystemException e1) {
                log.error("An unexpected error have happened rolling back", e1);
            }
            throw new EntryDuplicatedException(ee);
        } catch (Exception e) {
            log.error("An unexpected error have happened ...", e);
            try {
                userTransaction.rollback();
                userTransaction.setTransactionTimeout(0);
            } catch (SystemException e1) {
                log.error("An unexpected error have happened rolling back", e1);
            }
            throw new RuntimeException(e);
        }
    }

    /**
     * Creates a depreciation voucher according to businessUnitDepreciationVoucherMappings info
     *
     * @param businessUnitDepreciationVoucherMappings
     *              the info to build the details
     * @param gloss a given gloss
     * @return A depreciation voucher according to businessUnitDepreciationVoucherMappings info
     */
    private Voucher createVoucherByDetailMappings(Map<BusinessUnit, Map<CostCenter, Map<CashAccount, Map<VoucherDetailType, BigDecimal>>>> businessUnitDepreciationVoucherMappings,
                                                  String gloss) {
        Voucher voucherForGeneration = VoucherBuilder.newGeneralVoucher(Constants.FIXEDASSET_VOUCHER_FORM, gloss);
        voucherForGeneration.setUserNumber(companyConfigurationService.findDefaultAccountancyUserNumber());

        // add details to depreciation voucher
        for (BusinessUnit businessUnit : businessUnitDepreciationVoucherMappings.keySet()) {
            for (CostCenter costCenter : businessUnitDepreciationVoucherMappings.get(businessUnit).keySet()) {
                for (CashAccount cashAccount : businessUnitDepreciationVoucherMappings.get(businessUnit).get(costCenter).keySet()) {
                    for (VoucherDetailType voucherDetailType : businessUnitDepreciationVoucherMappings.get(businessUnit).get(costCenter).get(cashAccount).keySet()) {
                        BigDecimal amount = businessUnitDepreciationVoucherMappings.get(businessUnit).get(costCenter).get(cashAccount).get(voucherDetailType);
                        if (voucherDetailType.equals(VoucherDetailType.DEBIT)) {
                            voucherForGeneration.addVoucherDetail(
                                    VoucherDetailBuilder.newDebitVoucherDetail(
                                            businessUnit.getExecutorUnitCode(),
                                            costCenter.getCode(),
                                            cashAccount,
                                            amount,
                                            FinancesCurrencyType.P,
                                            Constants.BASE_CURRENCY_EXCHANGE_RATE)
                            );
                        } else {
                            voucherForGeneration.addVoucherDetail(
                                    VoucherDetailBuilder.newCreditVoucherDetail(
                                            businessUnit.getExecutorUnitCode(),
                                            costCenter.getCode(),
                                            cashAccount,
                                            amount,
                                            FinancesCurrencyType.P,
                                            Constants.BASE_CURRENCY_EXCHANGE_RATE)
                            );
                        }
                    }
                }
            }
        }
        return voucherForGeneration;
    }

    /**
     * Puts a given amount given a set of parameters by creating a new entry or by updating the existing entry accumulating the amount stored in the entry
     *
     * @param businessUnitVoucherMappings the map that holds the voucher info according to a set of parameters
     * @param businessUnit                the businessUnit to find the entry in the businessUnitVoucherMappings Map
     * @param costCenter                  the costCenter to find the entry in the businessUnitVoucherMappings Map
     * @param cashAccount                 the cashAccount to find the entry in the businessUnitVoucherMappings Map
     * @param voucherDetailType           the voucherDetailType to find the entry in the businessUnitVoucherMappings Map
     * @param amount                      the amount to put or merge in the businessUnitVoucherMappings Map
     */
    public void putInBusinessUnitVoucherMappings(Map<BusinessUnit, Map<CostCenter, Map<CashAccount, Map<VoucherDetailType, BigDecimal>>>> businessUnitVoucherMappings,
                                                 BusinessUnit businessUnit, CostCenter costCenter, CashAccount cashAccount,
                                                 VoucherDetailType voucherDetailType, BigDecimal amount) {
        if (businessUnitVoucherMappings.containsKey(businessUnit)) {
            if (businessUnitVoucherMappings.get(businessUnit).containsKey(costCenter)) {
                if (businessUnitVoucherMappings.get(businessUnit).get(costCenter).containsKey(cashAccount)) {
                    if (businessUnitVoucherMappings.get(businessUnit).get(costCenter).get(cashAccount).containsKey(voucherDetailType)) {
                        BigDecimal accumulatedAmount = BigDecimalUtil.sum(businessUnitVoucherMappings.get(businessUnit).get(costCenter).get(cashAccount).get(voucherDetailType), amount, SCALE);
                        businessUnitVoucherMappings.get(businessUnit).get(costCenter).get(cashAccount).put(voucherDetailType, accumulatedAmount);
                    } else {
                        businessUnitVoucherMappings.get(businessUnit).get(costCenter).get(cashAccount).put(voucherDetailType, amount);
                    }
                } else {
                    Map<VoucherDetailType, BigDecimal> voucherDetailTypeAmountMap = createVoucherDetailTypeAmountMap(voucherDetailType, amount);
                    businessUnitVoucherMappings.get(businessUnit).get(costCenter).put(cashAccount, voucherDetailTypeAmountMap);
                }
            } else {
                Map<VoucherDetailType, BigDecimal> voucherDetailTypeAmountMap = createVoucherDetailTypeAmountMap(voucherDetailType, amount);
                Map<CashAccount, Map<VoucherDetailType, BigDecimal>> cashAccountMappings = createCashAccountMap(cashAccount, voucherDetailTypeAmountMap);
                businessUnitVoucherMappings.get(businessUnit).put(costCenter, cashAccountMappings);
            }
        } else {
            Map<VoucherDetailType, BigDecimal> voucherDetailTypeAmountMap = createVoucherDetailTypeAmountMap(voucherDetailType, amount);
            Map<CashAccount, Map<VoucherDetailType, BigDecimal>> cashAccountMappings = createCashAccountMap(cashAccount, voucherDetailTypeAmountMap);
            Map<CostCenter, Map<CashAccount, Map<VoucherDetailType, BigDecimal>>> businessUnitValueMappings = createBusinessUnitValueMap(costCenter, cashAccountMappings);
            // finally add the build businessUnitValueMappings to businessUnitVoucherMappings
            businessUnitVoucherMappings.put(businessUnit, businessUnitValueMappings);
        }
    }

    private Map<CostCenter, Map<CashAccount, Map<VoucherDetailType, BigDecimal>>> createBusinessUnitValueMap(CostCenter costCenter, Map<CashAccount, Map<VoucherDetailType, BigDecimal>> cashAccountMappings) {
        // add a cashAccountMappings to businessUnitValueMappings
        Map<CostCenter, Map<CashAccount, Map<VoucherDetailType, BigDecimal>>> businessUnitValueMappings = new HashMap<CostCenter, Map<CashAccount, Map<VoucherDetailType, BigDecimal>>>();
        businessUnitValueMappings.put(costCenter, cashAccountMappings);
        return businessUnitValueMappings;
    }

    private Map<CashAccount, Map<VoucherDetailType, BigDecimal>> createCashAccountMap(CashAccount cashAccount, Map<VoucherDetailType, BigDecimal> voucherDetailTypeAmountMap) {
        // add voucherDetailTypeAmountMap to cashAccountMappings
        Map<CashAccount, Map<VoucherDetailType, BigDecimal>> cashAccountMappings = new HashMap<CashAccount, Map<VoucherDetailType, BigDecimal>>();
        cashAccountMappings.put(cashAccount, voucherDetailTypeAmountMap);
        return cashAccountMappings;
    }

    private Map<VoucherDetailType, BigDecimal> createVoucherDetailTypeAmountMap(VoucherDetailType voucherDetailType, BigDecimal amount) {
        // add an amount to detailType map
        Map<VoucherDetailType, BigDecimal> voucherDetailTypeAmountMap = new HashMap<VoucherDetailType, BigDecimal>();
        voucherDetailTypeAmountMap.put(voucherDetailType, amount);
        return voucherDetailTypeAmountMap;
    }

    /**
     * This method Registers the accounting account that adjusts the value
     *
     * @param cashAccount      The account to adjust
     * @param ufvAmount        The amount in ufv to adjust
     * @param newExchangeRate  The new exchange rate to adjust to
     * @param lastExchangeRate The old exchange rate to adjust based on
     * @param costCenter       Cost center
     * @param businessUnit     The Business Unit
     * @param adjustmentForInflationCashAccount
     *                         The opposite balance account used to be possible any adjustment
     * @param accountWay       true if the positive amount difference will be accredited to adjustmentForInflationAccountCode.
     *                         false if the positive amount difference will be debited to adjustmentForInflationAccountCode.
     * @param businessUnitAdjustVoucherMappings
     *                         the map into put or merge the voucher detail entries
     */

    private void adjust(CashAccount cashAccount, BigDecimal ufvAmount, BigDecimal newExchangeRate, BigDecimal lastExchangeRate,
                        CostCenter costCenter, BusinessUnit businessUnit,
                        CashAccount adjustmentForInflationCashAccount, boolean accountWay,
                        Map<BusinessUnit, Map<CostCenter, Map<CashAccount, Map<VoucherDetailType, BigDecimal>>>> businessUnitAdjustVoucherMappings) {
        BigDecimal ufvRateDifference =
                BigDecimalUtil.subtract(
                        newExchangeRate,
                        lastExchangeRate, SCALE
                );
        /* originalValue(newExchangeRate-lastExchangeRate)*/
        BigDecimal bsAdjustAmount =
                BigDecimalUtil.multiply(
                        ufvAmount,
                        ufvRateDifference
                );

        /* make the adjust only if there is a difference*/
        if (ufvRateDifference.compareTo(BigDecimal.ZERO) != 0) {
            if (ufvRateDifference.compareTo(BigDecimal.ZERO) > 0 && accountWay) {
                // add voucher detail info
                putInBusinessUnitVoucherMappings(businessUnitAdjustVoucherMappings, businessUnit, costCenter, cashAccount, VoucherDetailType.DEBIT, bsAdjustAmount);
                // add voucher detail info
                putInBusinessUnitVoucherMappings(businessUnitAdjustVoucherMappings, businessUnit, costCenter, adjustmentForInflationCashAccount, VoucherDetailType.CREDIT, bsAdjustAmount);

            } else {
                // add voucher detail info
                putInBusinessUnitVoucherMappings(businessUnitAdjustVoucherMappings, businessUnit, costCenter, cashAccount, VoucherDetailType.CREDIT, bsAdjustAmount.abs());

                // add voucher detail info
                putInBusinessUnitVoucherMappings(businessUnitAdjustVoucherMappings, businessUnit, costCenter, adjustmentForInflationCashAccount, VoucherDetailType.DEBIT, bsAdjustAmount.abs());

            }
        }
    }

    public void closeActualMonth() throws DateBeforeModuleMonthException,
            ConcurrencyException, EntryDuplicatedException, ThereAreActualFixedAssetException, ThereAreNotAdjustedFixedAssetException {
        /*check current date, do not depreciate if current is less than module date*/
        //compares current date with depreciation month of the module
        if (!fixedAssetMonthProcessService.isDateAfterFixedAssetMothProcess(new Date())) {
            /*shows a message with a message like the closing is not yet applicable because you are still in an earlier date*/
            throw new DateBeforeModuleMonthException();
        }
        if (findActualFixedAssets().size() > 0) {
            /* shows a message indicating that there are still fixed assets not yet depreciated*/
            throw new ThereAreActualFixedAssetException();
        }
        FinancesModule financesModule = fixedAssetMonthProcessService.getFinancesModule(FixedAssetDefaultConstants.getFixedAssetModulePK());
        Date firstDayOfCurrentProcessMonth = financesModule.getDate();
        /* adjust all the fixedAssets in TDP state whose adjustDate is different to firstDayOfCurrentProcessMonth*/
        List<FixedAsset> totallyDepreciatedFixedAssetList = findTdpFixedAssetsToAdjust(FixedAssetState.TDP, firstDayOfCurrentProcessMonth);

        if (totallyDepreciatedFixedAssetList.size() > 0) {
            /* shows a message indicating that there are still fixed assets not yet depreciated*/
            throw new ThereAreNotAdjustedFixedAssetException();
        }
//        ThereAreNotAdjustedFixedAssetException
        try {
            userTransaction.begin();
            em.joinTransaction();
            /*move date to next month*/
            Calendar newDate = DateUtils.toCalendar(financesModule.getDate());
            newDate.add(Calendar.MONTH, 1);
            financesModule.setDate(DateUtils.getFirstDayOfMonth((newDate).getTime()));
            getEntityManager().merge(financesModule);
            getEntityManager().flush();
            /*changes the state of the depreciated fixedAssets to actual*/
            changeFixedAssetState(FixedAssetState.DEP, FixedAssetState.VIG);
            userTransaction.commit();
        } catch (OptimisticLockException e) {
            try {
                userTransaction.rollback();
            } catch (SystemException e1) {
                log.error("An unexpected error have happened rolling back", e1);
            }
            throw new ConcurrencyException(e);
        } catch (PersistenceException ee) {
            try {
                userTransaction.rollback();
            } catch (SystemException e1) {
                log.error("An unexpected error have happened rolling back", e1);
            }
            throw new EntryDuplicatedException(ee);
        } catch (Exception e) {
            log.error("An unexpected error have happened ...", e);
            try {
                userTransaction.rollback();
            } catch (SystemException e1) {
                log.error("An unexpected error have happened rolling back", e1);
            }
            throw new RuntimeException(e);
        }
    }

    public boolean dischargableFixedAsset(FixedAsset fixedAsset) {
        fixedAsset.setState(FixedAssetState.BAJ);
        return (fixedAsset.getState().equals(FixedAssetState.VIG) ||
                fixedAsset.getState().equals(FixedAssetState.TDP));
    }

    public void transferCustodianFixedAssets(Employee custodian, String newCostCenter, BusinessUnit newBusinessUnit) throws ConcurrencyException, EntryDuplicatedException {
        List<FixedAsset> fixedAssetList = findActualFixedAssetsByCustodian(custodian);
        try {
            userTransaction.begin();
            em.joinTransaction();
            for (FixedAsset fixedAsset : fixedAssetList) {
                fixedAsset.setCostCenterCode(newCostCenter);
                fixedAsset.setBusinessUnit(newBusinessUnit);
                getEntityManager().merge(fixedAsset);
            }
            userTransaction.commit();
        } catch (OptimisticLockException e) {
            try {
                userTransaction.rollback();
            } catch (SystemException e1) {
                log.error("An unexpected error have happened rolling back", e1);
            }
            throw new ConcurrencyException(e);
        } catch (PersistenceException ee) {
            try {
                userTransaction.rollback();
            } catch (SystemException e1) {
                log.error("An unexpected error have happened rolling back", e1);
            }
            throw new EntryDuplicatedException(ee);
        } catch (Exception e) {
            log.error("An unexpected error have happened ...", e);
            try {
                userTransaction.rollback();
            } catch (SystemException e1) {
                log.error("An unexpected error have happened rolling back", e1);
            }
            throw new RuntimeException(e);
        }
    }

    public void approveRegistration(FixedAsset fixedAsset,
                                    String gloss,
                                    FixedAssetMovement fixedAssetMovement,
                                    FixedAssetMovementType fixedAssetMovementType,
                                    FixedAssetPayment fixedAssetPayment,
                                    List<FixedAssetPart> fixedAssetParts,
                                    Boolean payNow
    ) throws EntryDuplicatedException,
            DuplicatedFixedAssetCodeException {

        fixedAsset = generateCodes(fixedAsset);
/*
        FixedAsset fixedAssetDB = findFixedAssetByCode(fixedAsset);
        if (fixedAssetDB != null) {
            fixedAsset.setFixedAssetCode(null);
            fixedAsset.setBarCode(null);
            throw new DuplicatedFixedAssetCodeException();
        }*/

        try {
            userTransaction.begin();
            em.joinTransaction();

            fillFixedAssetDefaultValues(fixedAsset);
            fixedAsset.setState(FixedAssetState.VIG);

            getEntityManager().persist(fixedAsset);
            getEntityManager().flush();

            fixedAssetPartService.manageFixedAssetParts(fixedAsset, fixedAssetParts);

            fixedAssetMovement = createFixedAssetRegistrationMovement(fixedAsset,
                    fixedAssetMovement,
                    fixedAssetMovementType);

            fixedAssetMovement.setNewFixedAssetLocation(fixedAsset.getFixedAssetLocation());

            if(payNow){
            if (fixedAsset.getPurchaseOrder() != null) {
                Voucher voucherForGeneration = createAccountEntryForApprovedFixedAssets(fixedAsset, gloss);
                fixedAssetMovement.setTransactionNumber(voucherForGeneration.getTransactionNumber());
            } else {
                fixedAssetPayment.setPayCurrency(FinancesCurrencyType.P);
                fixedAssetPayment.setState(FixedAssetPaymentState.APPROVED);
                fixedAssetPayment.setPayAmount(fixedAsset.getBsOriginalValue());
                fixedAssetPayment.setCreationDate(new Date());

                if (fixedAssetPayment.getExchangeRate() == null) {
                    fixedAssetPayment.setExchangeRate(BigDecimal.ONE);
                }

                getEntityManager().persist(fixedAssetPayment);
                getEntityManager().flush();

                fixedAssetMovement.setFixedAssetPayment(fixedAssetPayment);
                createFixedAssetAccountVsBankAccountEntry(
                        fixedAsset.getBusinessUnit().getExecutorUnitCode(),
                        fixedAsset.getCostCenter().getCode(),
                        fixedAsset.getFixedAssetSubGroup().getOriginalValueCashAccount(),
                        fixedAsset.getBsOriginalValue(),
                        fixedAssetPayment, fixedAssetMovement);
            }

            getEntityManager().persist(fixedAssetMovement);
            getEntityManager().flush();
            }
            userTransaction.commit();
        } catch (PersistenceException e) {
            try {
                userTransaction.rollback();
            } catch (SystemException e1) {
                log.error("An unexpected error have happened rolling back", e1);
            }
            throw new EntryDuplicatedException();
        } catch (Exception e) {
            log.error("An unexpected error have happened ...", e);
            try {
                userTransaction.rollback();
            } catch (SystemException e1) {
                log.error("An unexpected error have happened rolling back", e1);
            }
            throw new RuntimeException(e);
        }
    }

    public void dischargeFixedAsset(FixedAsset fixedAsset, String gloss, FixedAssetMovement fixedAssetMovement,
                                    FixedAssetMovementType fixedAssetMovementType)
            throws ConcurrencyException, EntryDuplicatedException, CompanyConfigurationNotFoundException {
        try {
            userTransaction.begin();
            em.joinTransaction();

            CompanyConfiguration companyConfiguration = companyConfigurationService.findCompanyConfiguration();
            CashAccount provisionByTangibleFixedAssetObsolescenceCashAccount = companyConfiguration.getProvisionByTangibleFixedAssetObsolescenceAccount();
            fixedAsset.setState(FixedAssetState.BAJ);
            fixedAsset.setEndDate(new Date());
            getEntityManager().merge(fixedAsset);
            // Always approved state
            fixedAssetMovement.setState(FixedAssetMovementState.APR);
            fixedAssetMovement.setMovementNumber(fixedAssetMovementService.getNextMovementNumberByFixedAsset(fixedAsset));
            fixedAssetMovement.setUfvAmount(
                    BigDecimalUtil.sum(fixedAsset.getImprovement(),
                            BigDecimalUtil.subtract(
                                    fixedAsset.getUfvOriginalValue(),
                                    fixedAsset.getAcumulatedDepreciation()
                            )
                    )
            );
            /* It is necessary to compute each movement because they may have different rates, because when a improvement is registered
             * its lastMonthBsSusRate and lastMonthBsUfvRate are the rates given by the date retrieved from the ARGCTC table */
            BigDecimal bsImprovement = BigDecimal.ZERO;
            List<FixedAssetMovement> fixedAssetMovementList =
                    fixedAssetMovementService.findFixedAssetMovementListByFixedAssetByMovementTypeAndState(fixedAsset, FixedAssetMovementTypeEnum.MEJ, FixedAssetMovementState.APR);
            for (FixedAssetMovement fixedAssetMovementAux : fixedAssetMovementList) {
                bsImprovement = BigDecimalUtil.multiply(fixedAssetMovementAux.getUfvAmount(), fixedAssetMovementAux.getLastMonthBsUfvRate());
            }
            BigDecimal bsAmount =
                    BigDecimalUtil.sum(
                            BigDecimalUtil.multiply(
                                    BigDecimalUtil.subtract(
                                            fixedAsset.getUfvOriginalValue(),
                                            fixedAsset.getAcumulatedDepreciation()
                                    ),
                                    fixedAsset.getLastBsUfvRate()
                            ),
                            bsImprovement
                    );
            fixedAssetMovement.setBsAmount(bsAmount);
            fixedAssetMovement.setCustodian(fixedAsset.getCustodianJobContract().getContract().getEmployee());
            fixedAssetMovement.setCostCenterCode(fixedAsset.getCostCenterCode());
            fixedAssetMovement.setBusinessUnit(fixedAsset.getBusinessUnit());
            fixedAssetMovement.setFixedAssetMovementType(fixedAssetMovementType);
            fixedAssetMovement.setUserNumber(financesUserService.getFinancesUserCode());
            fixedAssetMovement.setFixedAssetMovementAccount(provisionByTangibleFixedAssetObsolescenceCashAccount.getAccountCode());
            fixedAssetMovement.setCurrency(FinancesCurrencyType.U);
            fixedAssetMovement.setFixedAsset(fixedAsset);

            Voucher voucherForGeneration = VoucherBuilder.newGeneralVoucher(Constants.FIXEDASSET_VOUCHER_FORM, gloss);
            voucherForGeneration.setUserNumber(companyConfigurationService.findDefaultAccountancyUserNumber());
            voucherForGeneration.addVoucherDetail(
                    VoucherDetailBuilder.newDebitVoucherDetail(
                            fixedAsset.getBusinessUnit().getExecutorUnitCode(), fixedAsset.getCostCenterCode(),
                            provisionByTangibleFixedAssetObsolescenceCashAccount,
                            fixedAssetMovement.getBsAmount(), FinancesCurrencyType.P,
                            Constants.BASE_CURRENCY_EXCHANGE_RATE));
            voucherForGeneration.addVoucherDetail(
                    VoucherDetailBuilder.newDebitVoucherDetail(
                            fixedAsset.getBusinessUnit().getExecutorUnitCode(), fixedAsset.getCostCenterCode(),
                            fixedAsset.getFixedAssetSubGroup().getAccumulatedDepreciationCashAccount(),
                            BigDecimalUtil.multiply(fixedAsset.getAcumulatedDepreciation(), fixedAsset.getLastBsUfvRate()),
                            FinancesCurrencyType.P, Constants.BASE_CURRENCY_EXCHANGE_RATE));
            voucherForGeneration.addVoucherDetail(
                    VoucherDetailBuilder.newCreditVoucherDetail(
                            fixedAsset.getBusinessUnit().getExecutorUnitCode(), fixedAsset.getCostCenterCode(),
                            fixedAsset.getFixedAssetSubGroup().getOriginalValueCashAccount(),
                            BigDecimalUtil.sum(
                                    BigDecimalUtil.multiply(fixedAsset.getLastBsUfvRate(),
                                            fixedAsset.getUfvOriginalValue()
                                    ),
                                    bsImprovement
                            ),
                            FinancesCurrencyType.P, Constants.BASE_CURRENCY_EXCHANGE_RATE));

            voucherService.create(voucherForGeneration);
            fixedAssetMovement.setTransactionNumber(voucherForGeneration.getTransactionNumber());
            getEntityManager().persist(fixedAssetMovement);
            getEntityManager().flush();
            userTransaction.commit();
        } catch (OptimisticLockException e) {
            try {
                userTransaction.rollback();
            } catch (SystemException e1) {
                log.error("An unexpected error have happened rolling back", e1);
            }
            throw new ConcurrencyException(e);
        } catch (PersistenceException ee) {
            try {
                userTransaction.rollback();
            } catch (SystemException e1) {
                log.error("An unexpected error have happened rolling back", e1);
            }
            throw new EntryDuplicatedException(ee);
        } catch (Exception e) {
            log.error("An unexpected error have happened ...", e);
            try {
                userTransaction.rollback();
            } catch (SystemException e1) {
                log.error("An unexpected error have happened rolling back", e1);
            }
            throw new RuntimeException(e);
        }
    }

    public void positiveImprovementFixedAsset(FixedAsset fixedAsset, String gloss, FixedAssetMovement fixedAssetMovement,
                                              FixedAssetMovementType fixedAssetMovementType, FixedAssetPayment fixedAssetPayment)
            throws ConcurrencyException, EntryDuplicatedException {
        try {
            userTransaction.begin();
            em.joinTransaction();
            fixedAsset.setImprovement(
                    BigDecimalUtil.sum(fixedAsset.getImprovement(), fixedAssetMovement.getUfvAmount()));
            getEntityManager().merge(fixedAsset);
            getEntityManager().flush();

            // Always approved state
            fixedAssetMovement.setState(FixedAssetMovementState.APR);
            fixedAssetMovement.setCurrency(FinancesCurrencyType.U);
            fixedAssetMovement.setCustodian(fixedAsset.getCustodianJobContract().getContract().getEmployee());
            fixedAssetMovement.setCostCenterCode(fixedAsset.getCostCenterCode());
            fixedAssetMovement.setBusinessUnit(fixedAsset.getBusinessUnit());
            fixedAssetMovement.setFixedAssetMovementType(fixedAssetMovementType);
            fixedAssetMovement.setUserNumber(financesUserService.getFinancesUserCode());
            fixedAssetMovement.setFixedAssetMovementAccount(fixedAsset.getFixedAssetSubGroup().getImprovementAccount());
            fixedAssetMovement.setFixedAsset(fixedAsset);
            fixedAssetMovement.setMovementNumber(fixedAssetMovementService.getNextMovementNumberByFixedAsset(fixedAsset));
            getEntityManager().persist(fixedAssetMovement);
            getEntityManager().flush();

            fixedAssetPayment.setPayCurrency(FinancesCurrencyType.P);
            fixedAssetPayment.setState(FixedAssetPaymentState.APPROVED);
            fixedAssetPayment.setPayAmount(fixedAssetMovement.getBsAmount());
            fixedAssetPayment.setCreationDate(new Date());

            if (fixedAssetPayment.getExchangeRate() == null) {
                fixedAssetPayment.setExchangeRate(BigDecimal.ONE);
            }
            getEntityManager().persist(fixedAssetPayment);
            getEntityManager().flush();
            fixedAssetMovement.setFixedAssetPayment(fixedAssetPayment);

            createFixedAssetAccountVsBankAccountEntry(
                    fixedAsset.getBusinessUnit().getExecutorUnitCode(),
                    fixedAsset.getCostCenter().getCode(),
                    fixedAsset.getFixedAssetSubGroup().getImprovementCashAccount(),
                    fixedAssetMovement.getBsAmount(),
                    fixedAssetPayment, fixedAssetMovement);


            Voucher voucherForGeneration = VoucherBuilder.newGeneralVoucher(Constants.FIXEDASSET_VOUCHER_FORM, gloss);
            voucherForGeneration.setUserNumber(companyConfigurationService.findDefaultAccountancyUserNumber());
            voucherForGeneration.addVoucherDetail(
                    VoucherDetailBuilder.newDebitVoucherDetail(
                            fixedAsset.getBusinessUnit().getExecutorUnitCode(), fixedAsset.getCostCenterCode(),
                            fixedAsset.getFixedAssetSubGroup().getOriginalValueCashAccount(),
                            fixedAssetMovement.getBsAmount(),
                            FinancesCurrencyType.P, BigDecimal.ONE)
            );
            voucherForGeneration.addVoucherDetail(
                    VoucherDetailBuilder.newCreditVoucherDetail(
                            fixedAsset.getBusinessUnit().getExecutorUnitCode(), fixedAsset.getCostCenterCode(),
                            fixedAsset.getFixedAssetSubGroup().getImprovementCashAccount(),
                            fixedAssetMovement.getBsAmount(),
                            FinancesCurrencyType.P, BigDecimal.ONE)
            );
            voucherService.create(voucherForGeneration);
            fixedAssetMovement.setTransactionNumber(voucherForGeneration.getTransactionNumber());
            fixedAssetMovement.setState(FixedAssetMovementState.APR);

            getEntityManager().merge(fixedAssetMovement);
            getEntityManager().flush();
            userTransaction.commit();
        } catch (OptimisticLockException e) {
            try {
                userTransaction.rollback();
            } catch (SystemException e1) {
                log.error("An unexpected error have happened rolling back", e1);
            }
            throw new ConcurrencyException(e);
        } catch (PersistenceException ee) {
            try {
                userTransaction.rollback();
            } catch (SystemException e1) {
                log.error("An unexpected error have happened rolling back", e1);
            }
            throw new EntryDuplicatedException(ee);
        } catch (Exception e) {
            log.error("An unexpected error have happened ...", e);
            try {
                userTransaction.rollback();
            } catch (SystemException e1) {
                log.error("An unexpected error have happened rolling back", e1);
            }
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings(value = "unchecked")
    public List<FixedAsset> findActualFixedAssets() {
        try {
            Query query = getEntityManager().createNamedQuery("FixedAsset.findFixedAssetByState");
            query.setParameter("state", FixedAssetState.VIG);
            return query.getResultList();
        } catch (Exception e) {
            log.error("An unexpected error have happened ...", e);
            e.printStackTrace();
        }
        return new ArrayList<FixedAsset>();
    }

    @SuppressWarnings(value = "unchecked")
    public List<FixedAsset> findFixedAssetsByState(FixedAssetState state) {
        try {
            Query query = getEntityManager().createNamedQuery("FixedAsset.findFixedAssetByState");
            query.setParameter("state", state);
            return query.getResultList();
        } catch (Exception e) {
            log.error("An unexpected error have happened ...", e);
            e.printStackTrace();
        }
        return new ArrayList<FixedAsset>();
    }

    @SuppressWarnings(value = "unchecked")
    public List<FixedAsset> findTdpFixedAssetsToAdjust(FixedAssetState state, Date adjustDate) {
        try {
            Query query = getEntityManager().createNamedQuery("FixedAsset.findTdpFixedAssetsToAdjust");
            query.setParameter("state", state);
            query.setParameter("adjustDate", adjustDate);
            return query.getResultList();
        } catch (Exception e) {
            log.error("An unexpected error have happened ...", e);
            e.printStackTrace();
        }
        return new ArrayList<FixedAsset>();
    }

    @SuppressWarnings(value = "unchecked")
    public List<FixedAsset> findActualFixedAssetsByCustodian(Employee custodian) {
        try {
            List<FixedAsset> resultList;
            Query query = getEntityManager().createNamedQuery("FixedAsset.findFixedAssetsByCustodianByState");
            query.setParameter("state", FixedAssetState.VIG);
            query.setParameter("custodian", custodian);
            resultList = query.getResultList();
            return resultList;
        } catch (Exception e) {
            log.error("An unexpected error have happened ...", e);
            e.printStackTrace();
        }
        return new ArrayList<FixedAsset>();
    }

    @SuppressWarnings(value = "unchecked")
    public List<FixedAsset> findFixedAssetByPurchaseOrderAndState(PurchaseOrder purchaseOrder, FixedAssetState fixedAssetState) {
        try {
            Query query = listEm.createNamedQuery("FixedAsset.findFixedAssetByPurchaseOrder");
            return query.setParameter("purchaseOrder", purchaseOrder)
                    .setParameter("state", fixedAssetState).getResultList();
        } catch (Exception e) {
            return null;
        }
    }

    @SuppressWarnings(value = "unchecked")
    public List<FixedAsset> findFixedAssetListByFixedAssetVoucher(FixedAssetVoucher fixedAssetVoucher, EntityManager entityManager) {
        try {
            if (null == entityManager) {
                entityManager = getEntityManager();
            }
            return entityManager.createNamedQuery("FixedAsset.findFixedAssetListByFixedAssetVoucher")
                    .setParameter("fixedAssetVoucher", fixedAssetVoucher).getResultList();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @SuppressWarnings(value = "unchecked")
    public List<FixedAsset> findFixedAssetListByFixedAssetPurchaseOrder(PurchaseOrder fixedAssetPurchaseOrder, EntityManager entityManager) {
        try {
            if (null == entityManager) {
                entityManager = getEntityManager();
            }
            return entityManager.createNamedQuery("FixedAsset.findFixedAssetListByFixedAssetPurchaseOrder")
                    .setParameter("purchaseOrder", fixedAssetPurchaseOrder).getResultList();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @SuppressWarnings(value = "unchecked")
    public List<FixedAsset> findFixedAssetListByFixedAssetVoucherAndMovementState(FixedAssetVoucher fixedAssetVoucher, FixedAssetMovementState fixedAssetMovementState, EntityManager entityManager) {
        try {
            if (null == entityManager) {
                entityManager = getEntityManager();
            }
            return entityManager.createNamedQuery("FixedAsset.findFixedAssetListByFixedAssetVoucherAndMovementState")
                    .setParameter("fixedAssetVoucher", fixedAssetVoucher)
                    .setParameter("fixedAssetMovementState", fixedAssetMovementState).getResultList();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @SuppressWarnings(value = "unchecked")
    public void changeFixedAssetState(FixedAssetState oldState, FixedAssetState newState) throws FixedAssetStateCanNotBeChangedException {
        try {
            Query query = getEntityManager().createNamedQuery("FixedAsset.changeFixedAssetState");
            query.setParameter("oldState", oldState);
            query.setParameter("newState", newState);
            query.executeUpdate();
            getEntityManager().flush();
        } catch (Exception e) {
            throw new FixedAssetStateCanNotBeChangedException();
        }
    }

    public EntityManager getListEm() {
        return listEm;
    }


    public boolean hasCustodianInDataBase(FixedAsset fixedAsset) {
        FixedAsset dataBaseFixedAsset = getListEm().find(FixedAsset.class, fixedAsset.getId());
        return !((null == dataBaseFixedAsset) || (dataBaseFixedAsset.getCustodianJobContract() == null));
    }


    public boolean hasFixedAssetCodeInDataBase(FixedAsset fixedAsset) {
        FixedAsset dataBaseFixedAsset = getListEm().find(FixedAsset.class, fixedAsset.getId());
        return !((null == dataBaseFixedAsset) || (dataBaseFixedAsset.getFixedAssetCode() == null));
    }

    private BigDecimal depreciationCandidate(FixedAsset fixedAsset) {
        return BigDecimalUtil.divide(
                BigDecimalUtil.multiply(
                        BigDecimalUtil.sum(
                                fixedAsset.getUfvOriginalValue(),
                                fixedAsset.getImprovement()
                        ),
                        fixedAsset.getDepreciationRate()
                ),
                BigDecimalUtil.toBigDecimal(FixedAssetDefaultConstants.DEPRECIATION_FUNCTION_DIVIDER)
        );

    }

    private BigDecimal residualCandidate(FixedAsset fixedAsset) {
        /*(VO+MEJ)-DEP_ACUM-DESHECHO*/
        return BigDecimalUtil.subtract(
                BigDecimalUtil.subtract(
                        BigDecimalUtil.sum(
                                fixedAsset.getUfvOriginalValue(),
                                fixedAsset.getImprovement()
                        ),
                        fixedAsset.getAcumulatedDepreciation()
                ),
                fixedAsset.getRubbish()
        );
    }

    public FixedAsset findFixedAssetByCode(FixedAsset fixedAsset) {
        return findFixedAssetByCode(fixedAsset.getFixedAssetGroupCode(), fixedAsset.getFixedAssetSubGroupCode(), fixedAsset.getFixedAssetCode());
    }

    @SuppressWarnings({"unchecked"})
    public FixedAsset findFixedAssetByCode(String fixedAssetGroupCode, String fixedAssetSubGroupCode, Long fixedAssetCode) {
        try {

            List<FixedAsset> fixedAssetList = getEntityManager().createNamedQuery("FixedAsset.findFixedAssetByCode")
                    .setParameter("fixedAssetGroupCode", fixedAssetGroupCode)
                    .setParameter("fixedAssetSubGroupCode", fixedAssetSubGroupCode)
                    .setParameter("fixedAssetCode", fixedAssetCode)
                    .setParameter("companyNumber", Constants.defaultCompanyNumber)
                    .getResultList();
            if (!ValidatorUtil.isEmptyOrNull(fixedAssetList)) {
                return fixedAssetList.get(0);
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    public FixedAsset getDataBaseFixedAsset(FixedAsset fixedAsset) {
        Query query = listEm.createNamedQuery("FixedAsset.findFixedAsset");
        query.setParameter("id", fixedAsset.getId());
        return (FixedAsset) query.getSingleResult();
    }

    public FixedAsset findFixedAsset(FixedAsset fixedAsset) {
        Query query = getEntityManager().createNamedQuery("FixedAsset.findFixedAsset");
        query.setParameter("id", fixedAsset.getId());
        return (FixedAsset) query.getSingleResult();
    }

    public void fillFixedAssetDefaultValues(FixedAsset fixedAsset) {
        fixedAsset.setCurrencyType(FinancesCurrencyType.U);
        fixedAsset.setAcumulatedDepreciation(BigDecimal.ZERO);
        fixedAsset.setImprovement(BigDecimal.ZERO);
        fixedAsset.setDepreciation(BigDecimal.ZERO);
        fixedAsset.setLastBsSusRate(fixedAsset.getBsSusRate());
        fixedAsset.setLastBsUfvRate(fixedAsset.getBsUfvRate());
    }

    private FixedAssetMovement createFixedAssetRegistrationMovement(FixedAsset fixedAsset, FixedAssetMovement fixedAssetMovement, FixedAssetMovementType fixedAssetMovementType) {
        fillDefaultValuesForFixedAssetMovement(fixedAsset, fixedAssetMovement);

        fixedAssetMovement.setBsSusRate(fixedAsset.getBsSusRate());
        fixedAssetMovement.setBsUfvRate(fixedAsset.getBsUfvRate());
        fixedAssetMovement.setUfvAmount(fixedAsset.getUfvOriginalValue());
        fixedAssetMovement.setBsAmount(fixedAsset.getBsOriginalValue());
        fixedAssetMovement.setInitialDepreciation(fixedAsset.getDepreciation());
        fixedAssetMovement.setCustodian(fixedAsset.getCustodianJobContract().getContract().getEmployee());
        fixedAssetMovement.setCostCenterCode(fixedAsset.getCostCenterCode());
        fixedAssetMovement.setBusinessUnit(fixedAsset.getBusinessUnit());
        fixedAssetMovement.setFixedAssetMovementType(fixedAssetMovementType);
        fixedAssetMovement.setFixedAssetMovementAccount(fixedAsset.getFixedAssetSubGroup().getWarehouseAccount());
        fixedAssetMovement.setMovementNumber(fixedAssetMovementService.getNextMovementNumberByFixedAsset(fixedAsset));
        return fixedAssetMovement;
    }

    private void fillDefaultValuesForFixedAssetMovement(FixedAsset fixedAsset, FixedAssetMovement fixedAssetMovement) {
        // Always current date
        fixedAssetMovement.setMovementDate(new Date());
        fixedAssetMovement.setCreationDate(new Date());
        fixedAssetMovement.setState(FixedAssetMovementState.APR);
        fixedAssetMovement.setCurrency(FinancesCurrencyType.U);
        fixedAssetMovement.setUserNumber(financesUserService.getFinancesUserCode());
        fixedAssetMovement.setFixedAsset(fixedAsset);
    }

    protected EntityManager getEntityManager() {
        return em;
    }

    public void createFixedAssetAccountVsBankAccountEntry(String executorUnitCode, String costCenterCode,
                                                          CashAccount cashAccount, BigDecimal accountAmount,
                                                          FixedAssetPayment fixedAssetPayment, FixedAssetMovement fixedAssetMovement)
            throws CompanyConfigurationNotFoundException {
        Voucher voucher = null;
        CompanyConfiguration companyConfiguration = companyConfigurationService.findCompanyConfiguration();

        BigDecimal bankExchangeRate = fixedAssetPayment.getExchangeRate();
        BigDecimal payExchangeRate = fixedAssetPayment.getExchangeRate();

        BigDecimal voucherAmountNationalAmount = BigDecimalUtil.multiply(fixedAssetPayment.getSourceAmount(), payExchangeRate);

        /* if the payment currency is in $us so convert to equivalent in bs */
        BigDecimal payAmount = FinancesCurrencyType.D.equals(fixedAssetPayment.getPayCurrency()) ?
                BigDecimalUtil.multiply(fixedAssetPayment.getPayAmount(), payExchangeRate) : fixedAssetPayment.getPayAmount();
        if (PurchaseOrderPaymentType.PAYMENT_BANK_ACCOUNT.equals(fixedAssetPayment.getPaymentType())) {
            Long sequenceNumber = sequenceGeneratorService.nextValue(Constants.FIXEDASSET_PAYMENT_DOCUMENT_SEQUENCE);
            voucher = VoucherBuilder.newBankAccountPaymentTypeVoucher(
                    Constants.BANKACCOUNT_VOUCHERTYPE_FORM,
                    Constants.BANKACCOUNT_VOUCHERTYPE_DEBITNOTE_DOCTYPE,
                    Constants.FIXEDASSET_PAYMENT_DOCNUMBER_PREFFIX + sequenceNumber,
                    fixedAssetPayment.getBankAccountNumber(),
                    fixedAssetPayment.getSourceAmount(),
                    fixedAssetPayment.getSourceCurrency(),
                    bankExchangeRate,
                    fixedAssetPayment.getDescription());
        } else if (PurchaseOrderPaymentType.PAYMENT_WITH_CHECK.equals(fixedAssetPayment.getPaymentType())) {
            voucher = VoucherBuilder.newCheckPaymentTypeVoucher(
                    Constants.CHECK_VOUCHERTYPE_FORM,
                    Constants.CHECK_VOUCHERTYPE_DOCTYPE,
                    fixedAssetPayment.getBankAccountNumber(),
                    fixedAssetPayment.getBeneficiaryName(),
                    fixedAssetPayment.getSourceAmount(),
                    fixedAssetPayment.getSourceCurrency(),
                    bankExchangeRate,
                    fixedAssetPayment.getCheckDestination(),
                    fixedAssetPayment.getDescription());
        } else if (PurchaseOrderPaymentType.PAYMENT_CASHBOX.equals(fixedAssetPayment.getPaymentType())) {
            voucher = VoucherBuilder.newGeneralVoucher(Constants.CASHBOX_PAYMENT_VOUCHER_FORM, fixedAssetPayment.getDescription());
            voucher.addVoucherDetail(VoucherDetailBuilder.newCreditVoucherDetail(
                    executorUnitCode,
                    costCenterCode,
                    fixedAssetPayment.getCashBoxCashAccount(),
                    voucherAmountNationalAmount,
                    fixedAssetPayment.getCashBoxCashAccount().getCurrency(),
                    bankExchangeRate));
        }
        if (voucher != null) {
            voucher.setUserNumber(companyConfigurationService.findDefaultTreasuryUserNumber());
            voucher.addVoucherDetail(VoucherDetailBuilder.newDebitVoucherDetail(
                    executorUnitCode,
                    costCenterCode,
                    cashAccount,
                    accountAmount,
                    FinancesCurrencyType.P,
                    BigDecimal.ONE));
            BigDecimal balanceAmount = BigDecimalUtil.subtract(payAmount, voucherAmountNationalAmount);
            if (balanceAmount.doubleValue() > 0) {
                voucher.addVoucherDetail(VoucherDetailBuilder.newCreditVoucherDetail(
                        executorUnitCode,
                        companyConfiguration.getExchangeRateBalanceCostCenter().getCode(),
                        companyConfiguration.getBalanceExchangeRateAccount(),
                        balanceAmount,
                        FinancesCurrencyType.P,
                        BigDecimal.ONE));
            } else if (balanceAmount.doubleValue() < 0) {
                voucher.addVoucherDetail(VoucherDetailBuilder.newDebitVoucherDetail(
                        executorUnitCode,
                        companyConfiguration.getExchangeRateBalanceCostCenter().getCode(),
                        companyConfiguration.getBalanceExchangeRateAccount(),
                        balanceAmount.abs(),
                        FinancesCurrencyType.P,
                        BigDecimal.ONE));
            }
            //voucherService.create(voucher);
            voucherAccoutingService.saveVoucher(voucher);

            fixedAssetPayment.setTransactionNumber(voucher.getTransactionNumber());
            fixedAssetMovement.setTransactionNumber(voucher.getTransactionNumber());
            getEntityManager().flush();
        }
    }
    /*
     * This method changes or sets the guaranty attribute of the entities that fulfill the
     * criteria determined by the fixedAsset object
     * @param fixedAssetCriteria the object that contains the criteria used to cover the update sentence
     * @param monthsGuaranty the guaranty in months to set under the criteria specified by fixedAssetCriteria
     */

    public Integer changeGuaranty(FixedAssetDataModel fixedAssetDataModel, Integer monthsGuaranty) {
        Integer rows;
        FixedAsset fixedAssetCriteria = fixedAssetDataModel.getCriteria();
        try {
            userTransaction.begin();
            em.joinTransaction();
            StringBuilder sb = new StringBuilder("update FixedAsset o set o.monthsGuaranty=:monthsGuaranty where o.companyNumber=:companyNumber ");
            if (fixedAssetCriteria.getBusinessUnit() != null) {
                sb.append("and o.businessUnit=:businessUnit ");
            }
            if (fixedAssetDataModel.getCostCenter() != null) {
                sb.append("and o.costCenterCode=:costCenterCode ");
            }
            if (fixedAssetDataModel.getFixedAssetGroup() != null) {
                sb.append("and o.fixedAssetGroupCode=:fixedAssetGroupCode ");
            }
            if (fixedAssetDataModel.getFixedAssetSubGroup() != null) {
                sb.append("and o.fixedAssetSubGroupCode=:fixedAssetSubGroupCode ");
            }
            Query query = em.createQuery(sb.toString());
            query.setParameter("monthsGuaranty", monthsGuaranty);
            query.setParameter("companyNumber", Constants.defaultCompanyNumber);
            if (fixedAssetCriteria.getBusinessUnit() != null) {
                query.setParameter("businessUnit", fixedAssetCriteria.getBusinessUnit());
            }
            if (fixedAssetDataModel.getCostCenter() != null) {
                query.setParameter("costCenterCode", fixedAssetDataModel.getCostCenter().getCode());
            }
            if (fixedAssetDataModel.getFixedAssetGroup() != null) {
                query.setParameter("fixedAssetGroupCode", fixedAssetDataModel.getFixedAssetGroup().getId().getGroupCode());
            }
            if (fixedAssetDataModel.getFixedAssetSubGroup() != null) {
                query.setParameter("fixedAssetSubGroupCode", fixedAssetDataModel.getFixedAssetSubGroup().getId().getFixedAssetSubGroupCode());
            }
            rows = query.executeUpdate();
            em.flush();
            userTransaction.commit();
        } catch (Exception e) {
            log.error("An unexpected error have happened ...", e);
            try {
                userTransaction.rollback();
            } catch (SystemException e1) {
                log.error("An unexpected error have happened rolling back", e1);
            }
            throw new RuntimeException(e);
        }
        return rows;
    }

    public Voucher createAccountEntryForApprovedFixedAssets(FixedAsset fixedAsset, String gloss) throws CompanyConfigurationNotFoundException {
        Voucher voucherForGeneration = VoucherBuilder.newGeneralVoucher(Constants.FIXEDASSET_VOUCHER_FORM, gloss);
        CompanyConfiguration companyConfiguration = companyConfigurationService.findCompanyConfiguration();
        voucherForGeneration.setUserNumber(companyConfigurationService.findDefaultAccountancyUserNumber());
        voucherForGeneration.addVoucherDetail(
                VoucherDetailBuilder.newDebitVoucherDetail(
                        fixedAsset.getBusinessUnit().getExecutorUnitCode(), fixedAsset.getCostCenterCode(),
                        fixedAsset.getFixedAssetSubGroup().getOriginalValueCashAccount(),
                        fixedAsset.getBsOriginalValue(),
                        FinancesCurrencyType.P,
                        BigDecimal.ONE
                )
        );
        voucherForGeneration.addVoucherDetail(
                VoucherDetailBuilder.newCreditVoucherDetail(
                        fixedAsset.getBusinessUnit().getExecutorUnitCode(), fixedAsset.getCostCenterCode(),
                        companyConfiguration.getFixedAssetInTransitAccount(),
                        fixedAsset.getBsOriginalValue(), FinancesCurrencyType.P, BigDecimal.ONE));
        voucherService.create(voucherForGeneration);

        return voucherForGeneration;
    }

    public Boolean validateFixedAssetCode(Long fixedAssetCode) {
        if (fixedAssetCode != null) {
            Long countResult = ((Long) listEm.createNamedQuery("FixedAsset.countByCode")
                    .setParameter("companyNumber", Constants.defaultCompanyNumber)
                    .setParameter("fixedAssetCode", fixedAssetCode).getSingleResult());
            return countResult == null || countResult == 0;
        }
        return false;
    }

    public Boolean validateGroupCode(String groupCode) {
        if (!ValidatorUtil.isBlankOrNull(groupCode)) {
            Long countResult = ((Long) listEm.createNamedQuery("FixedAssetGroup.countByCode")
                    .setParameter("companyNumber", Constants.defaultCompanyNumber)
                    .setParameter("groupCode", groupCode).getSingleResult());
            return countResult == null || countResult == 0;
        }
        return false;
    }

    public Boolean validateSubGroupCode(String fixedAssetGroupCode, String fixedAssetSubGroupCode) {
        if (!ValidatorUtil.isBlankOrNull(fixedAssetGroupCode) && !ValidatorUtil.isBlankOrNull(fixedAssetSubGroupCode)) {
            Long countResult = ((Long) listEm.createNamedQuery("FixedAssetSubGroup.countByCode")
                    .setParameter("companyNumber", Constants.defaultCompanyNumber)
                    .setParameter("fixedAssetGroupCode", fixedAssetGroupCode)
                    .setParameter("fixedAssetSubGroupCode", fixedAssetSubGroupCode).getSingleResult());
            return countResult == null || countResult == 0;
        }
        return false;
    }


    public BigDecimal findRegisteredFixedAssets(Integer businessUnitId) {

        String sql = " SELECT COUNT(A.IDACTIVO) \n" +
                "FROM " + Constants.FINANCES_SCHEMA + ".AF_ACTIVOS A \n" +
                "WHERE A.ESTADO!='PEN' \n";
        if (null != businessUnitId) {
            sql += " AND A.IDUNIDADNEGOCIO = " + businessUnitId + " \n";
        }
        return (BigDecimal) (em.createNativeQuery(sql).getSingleResult());
    }

    public BigDecimal findFADischargedBeforeLifetime(Integer businessUnitId) {

        String sql = "SELECT COUNT(A.IDACTIVO) \n" +
                "FROM " + Constants.FINANCES_SCHEMA + ".AF_ACTIVOS A \n" +
                "WHERE A.ESTADO='BAJ' \n";
        if (null != businessUnitId) {
            sql += "AND A.IDUNIDADNEGOCIO = " + businessUnitId + " \n";
        }
        sql += "AND A.FCH_BAJA < ADD_MONTHS(A.FCH_ALTA, A.DURACION) \n";
        return (BigDecimal) em.createNativeQuery(sql).getSingleResult();
    }

    public FixedAsset generateCodes(FixedAsset fixedAsset) {
        Long countByBarCode;
        do {
            fixedAsset = generateFixedAssetCode(fixedAsset);
            fixedAsset = generateBarCode(fixedAsset);
            countByBarCode = countFixedAssetByBarCode(fixedAsset);
            log.debug("fixedAsset.getBarCode()= " + fixedAsset.getBarCode() + " countByBarCode= " + countByBarCode);
        } while (countByBarCode != null && countByBarCode > 0);
        return fixedAsset;
    }

    public FixedAsset generateFixedAssetCode(FixedAsset fixedAsset) {
        /*Long maxByGroupAndSubGroup = (Long) getEntityManager().createNamedQuery("FixedAsset.maxByGroupAndSubGroup")
                .setParameter("fixedAssetGroupCode", fixedAsset.getFixedAssetGroupCode())
                .setParameter("fixedAssetSubGroupCode", fixedAsset.getFixedAssetSubGroupCode())
                .setParameter("companyNumber", Constants.defaultCompanyNumber)
                .getSingleResult();
        fixedAsset.setFixedAssetCode(maxByGroupAndSubGroup != null ? maxByGroupAndSubGroup + 1 : 1);
        return fixedAsset;*/
        fixedAsset.setFixedAssetCode(sequenceGeneratorService.nextValue(
                Constants.FIXEDASSET_ITEM_SEQUENCE +
                        Constants.UNDERSCORE_SEPARATOR + fixedAsset.getFixedAssetGroupCode() +
                        Constants.UNDERSCORE_SEPARATOR + fixedAsset.getFixedAssetSubGroupCode())
        );
        return fixedAsset;
    }

    public FixedAsset generateBarCode(FixedAsset fixedAsset) {
        String barcode = "";
        if (ValidatorUtil.isLong(fixedAsset.getFixedAssetGroupCode())) {
            barcode += FormatUtils.beforeFillingWithZeros(ValidatorUtil.formatLong(fixedAsset.getFixedAssetGroupCode()).toString(), 2);
        } else {
            barcode += fixedAsset.getFixedAssetGroupCode();
        }
//        barcode += Constants.DOT_SEPARATOR;

        if (ValidatorUtil.isLong(fixedAsset.getFixedAssetSubGroupCode())) {
            barcode += FormatUtils.beforeFillingWithZeros(ValidatorUtil.formatLong(fixedAsset.getFixedAssetSubGroupCode()).toString(), 4);
        } else {
            barcode += fixedAsset.getFixedAssetSubGroupCode();
        }
//        barcode += Constants.DOT_SEPARATOR;

        barcode += FormatUtils.beforeFillingWithZeros(fixedAsset.getFixedAssetCode().toString(), 6);

        fixedAsset.setBarCode(barcode);

        return fixedAsset;
    }

    public Long countFixedAssetByBarCode(FixedAsset fixedAsset) {
        return (Long) getEntityManager().createNamedQuery("FixedAsset.countByBarCode")
                .setParameter("barCode", fixedAsset.getBarCode())
                .setParameter("companyNumber", fixedAsset.getCompanyNumber()).getSingleResult();
    }

    public FixedAsset findFixedAssetByBarCode(String barCode) {
        try {
            return (FixedAsset) getEntityManager().createNamedQuery("FixedAsset.findByBarCode")
                    .setParameter("barCode", barCode).getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }
}
