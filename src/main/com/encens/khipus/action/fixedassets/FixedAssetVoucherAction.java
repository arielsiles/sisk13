package com.encens.khipus.action.fixedassets;

import com.encens.khipus.action.AppIdentity;
import com.encens.khipus.exception.ConcurrencyException;
import com.encens.khipus.exception.EntryDuplicatedException;
import com.encens.khipus.exception.EntryNotFoundException;
import com.encens.khipus.exception.finances.CompanyConfigurationNotFoundException;
import com.encens.khipus.exception.finances.FinancesCurrencyNotFoundException;
import com.encens.khipus.exception.finances.FinancesExchangeRateNotFoundException;
import com.encens.khipus.exception.fixedassets.*;
import com.encens.khipus.framework.action.GenericAction;
import com.encens.khipus.framework.action.Outcome;
import com.encens.khipus.model.admin.BusinessUnit;
import com.encens.khipus.model.admin.User;
import com.encens.khipus.model.finances.CashAccount;
import com.encens.khipus.model.finances.CostCenter;
import com.encens.khipus.model.finances.FinancesCurrencyType;
import com.encens.khipus.model.finances.JobContract;
import com.encens.khipus.model.fixedassets.*;
import com.encens.khipus.model.purchases.PurchaseOrder;
import com.encens.khipus.model.purchases.PurchaseOrderPaymentType;
import com.encens.khipus.model.warehouse.FixedAssetBeneficiaryType;
import com.encens.khipus.service.common.SequenceGeneratorService;
import com.encens.khipus.service.finances.FinancesExchangeRateService;
import com.encens.khipus.service.finances.SpendDistributionService;
import com.encens.khipus.service.fixedassets.*;
import com.encens.khipus.util.BigDecimalUtil;
import com.encens.khipus.util.BusinessUnitValidatorUtil;
import com.encens.khipus.util.FormatUtils;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.*;
import org.jboss.seam.annotations.security.Restrict;
import org.jboss.seam.international.StatusMessage;

import javax.persistence.EntityManager;
import java.math.BigDecimal;
import java.util.*;

/**
 * FixedAssetVoucher action class
 *
 * @author
 * @version 2.25
 */
@Name("fixedAssetVoucherAction")
@Scope(ScopeType.CONVERSATION)
public class FixedAssetVoucherAction extends GenericAction<FixedAssetVoucher> {
    @In
    private User currentUser;
    @In
    private FixedAssetVoucherService fixedAssetVoucherService;
    @In
    private SpendDistributionService spendDistributionService;
    @In
    private FinancesExchangeRateService financesExchangeRateService;
    @In
    private FixedAssetMonthProcessService fixedAssetMonthProcessService;
    @In
    private SequenceGeneratorService sequenceGeneratorService;
    @In
    private FixedAssetMovementService fixedAssetMovementService;
    @In
    private FixedAssetService fixedAssetService;
    @In
    private FixedAssetMovementTypeService fixedAssetMovementTypeService;

    /*foreign exchange rate value*/
    private BigDecimal foreignExchangeRate;
    private BigDecimal nationalExchangeRate = BigDecimal.ONE;

    @In(required = false)
    private FixedAssetDataModel fixedAssetDataModel;

    /* This is used only for improvement*/
    private FixedAssetPayment fixedAssetPayment;

    /* they are used in transference action when it is required a list of selected fixed asset to apply the transfer operation*/
    private List<FixedAsset> selectedFixedAssetList;
    private List<Long> selectedFixedAssetIdList;

    /* when a voucher of registration type is approved. FixedAssets involved in movement detail*/
    private List<FixedAsset> fixedAssetMovementDetailList;

    /* list to restrict the states in the filters of the dataModel according to the desired operation*/
    private List<FixedAssetState> stateRestrictionList;

    /* map to manage the amount distribution of the payment for improvement voucher */
    private HashMap<FixedAsset, FixedAssetMovement> fixedAssetFixedAssetMovementHashMap;

    private FixedAsset fixedAsset;

    @SuppressWarnings({"SeamBijectionTypeMismatchInspection"})
    @In(value = "org.jboss.seam.security.identity")
    private AppIdentity appIdentity;

    @In(value = "#{listEntityManager}")
    private EntityManager listEm;

    @Create
    public void init() {
        if (!isManaged()) {
            getInstance().setCreationDate(new Date());
            getInstance().setMovementDate(new Date());
            getInstance().setState(FixedAssetVoucherState.PEN);
            getInstance().setCreatedBy(currentUser);
            fixedAssetPayment = new FixedAssetPayment();
            fixedAssetPayment.setCreationDate(new Date());
            fixedAssetPayment.setState(FixedAssetPaymentState.PENDING);
            fixedAssetPayment.setPaymentType(PurchaseOrderPaymentType.PAYMENT_WITH_CHECK);
            fixedAssetPayment.setFixedAssetBeneficiaryType(FixedAssetBeneficiaryType.PERSON);
            fixedAssetPayment.setPayCurrency(FinancesCurrencyType.P);
            selectedFixedAssetList = new ArrayList<FixedAsset>();
            selectedFixedAssetIdList = new ArrayList<Long>();
            fixedAssetFixedAssetMovementHashMap = new HashMap<FixedAsset, FixedAssetMovement>();

            try {
                getInstance().setBsSusRate(financesExchangeRateService.findLastExchangeRateByCurrency(FinancesCurrencyType.D.name()));
                getInstance().setBsUfvRate(financesExchangeRateService.findLastExchangeRateByCurrency(FinancesCurrencyType.U.name()));
                foreignExchangeRate = financesExchangeRateService.findLastExchangeRateByCurrency(FinancesCurrencyType.D.name());
            } catch (FinancesCurrencyNotFoundException e) {
                log.debug("finances currency not found");
            } catch (FinancesExchangeRateNotFoundException e) {
                log.debug("finances exchange rate not found");
            }
        }
    }

    @Factory(value = "fixedAssetVoucher", scope = ScopeType.STATELESS)
    @Restrict("#{s:hasPermission('FIXEDASSETVOUCHER','VIEW')}")
    public FixedAssetVoucher initFixedAssetVoucher() {
        return getInstance();
    }

    @Factory(value = "fixedAssetVoucherTypeList", scope = ScopeType.STATELESS)
    public List<FixedAssetMovementType> initFixedAssetMovementTypes() {
        List<FixedAssetMovementType> fixedAssetMovementTypeList = fixedAssetMovementTypeService.findAll();
        for (FixedAssetMovementType fixedAssetMovementType : fixedAssetMovementTypeList) {

            boolean remove = false;
            if (fixedAssetMovementType.getFixedAssetMovementTypeEnum().equals(FixedAssetMovementTypeEnum.ALT)
                    && !appIdentity.hasPermission("FIXEDASSETREGISTRATION", "VIEW")) {
                remove = true;
            }
            if (fixedAssetMovementType.getFixedAssetMovementTypeEnum().equals(FixedAssetMovementTypeEnum.TRA)
                    && !appIdentity.hasPermission("FIXEDASSETTRANSFERENCE", "VIEW")) {
                remove = true;
            }
            if (fixedAssetMovementType.getFixedAssetMovementTypeEnum().equals(FixedAssetMovementTypeEnum.MEJ)
                    && !appIdentity.hasPermission("FIXEDASSETIMPROVEMENT", "VIEW")) {
                remove = true;
            }
            if (fixedAssetMovementType.getFixedAssetMovementTypeEnum().equals(FixedAssetMovementTypeEnum.BAJ)
                    && !appIdentity.hasPermission("FIXEDASSETDISCHARGE", "VIEW")) {
                remove = true;
            }
            if (remove) {
                fixedAssetMovementTypeList.remove(fixedAssetMovementType);
            }
        }
        return fixedAssetMovementTypeList;
    }

    @Override
    public String getDisplayNameProperty() {
        return "cause";
    }

    @Override
    @Begin(ifOutcome = Outcome.SUCCESS, flushMode = FlushModeType.MANUAL)
    @Restrict("#{s:hasPermission('FIXEDASSETVOUCHER','VIEW')}")
    public String select(FixedAssetVoucher instance) {
        super.select(instance);
        /* in case it is managed refresh the restriction list */
        if (isFixedAssetVoucherPending()) {
            if (isTransferenceMovement() || isDischargeMovement() || isImprovementMovement()) {
                stateRestrictionList = Arrays.asList(FixedAssetState.VIG, FixedAssetState.TDP);
            }
        } else {
            stateRestrictionList = null;
        }

        /*in case the selected instance is a approved or annulled movement of kind ALT*/
        if ((getInstance().getState().equals(FixedAssetVoucherState.APR) || getInstance().getState().equals(FixedAssetVoucherState.ANL))
                && (getInstance().getFixedAssetVoucherType().getFixedAssetMovementTypeEnum().equals(FixedAssetMovementTypeEnum.ALT)
                || getInstance().getFixedAssetVoucherType().getFixedAssetMovementTypeEnum().equals(FixedAssetMovementTypeEnum.TRA)
                || getInstance().getFixedAssetVoucherType().getFixedAssetMovementTypeEnum().equals(FixedAssetMovementTypeEnum.BAJ)
                || getInstance().getFixedAssetVoucherType().getFixedAssetMovementTypeEnum().equals(FixedAssetMovementTypeEnum.MEJ))) {
            fixedAssetMovementDetailList = fixedAssetService.findFixedAssetListByFixedAssetVoucher(getInstance(), null);
        }
        if ((getInstance().getState().equals(FixedAssetVoucherState.PEN))
                && (getInstance().getFixedAssetVoucherType().getFixedAssetMovementTypeEnum().equals(FixedAssetMovementTypeEnum.TRA)
                || getInstance().getFixedAssetVoucherType().getFixedAssetMovementTypeEnum().equals(FixedAssetMovementTypeEnum.BAJ)
                || getInstance().getFixedAssetVoucherType().getFixedAssetMovementTypeEnum().equals(FixedAssetMovementTypeEnum.MEJ))) {

            /*in case the redirect from the same page in voucher creation case do not query
            * do not use getter due to contition logic*/
            selectedFixedAssetList = fixedAssetService.findFixedAssetListByFixedAssetVoucher(getInstance(), null);
            selectedFixedAssetIdList = new ArrayList<Long>();
            for (FixedAsset fixedAsset : getSelectedFixedAssetList()) {
                selectedFixedAssetIdList.add(fixedAsset.getId());
            }
            if (getInstance().getFixedAssetVoucherType().getFixedAssetMovementTypeEnum().equals(FixedAssetMovementTypeEnum.MEJ)) {
                /*fill map for improvement*/
                for (FixedAssetMovement fixedAssetMovement : fixedAssetMovementService.findFixedAssetMovementByFixedAssetVoucher(getInstance(), null)) {
                    try {
                        FixedAsset fixedAsset = getService().findById(FixedAsset.class, fixedAssetMovement.getFixedAsset().getId());
                        fixedAssetFixedAssetMovementHashMap.put(fixedAsset, fixedAssetMovement);
                    } catch (EntryNotFoundException e) {
                        log.error(e, "The fixed asset have not been found");
                    }
                }
                try {
                    fixedAssetPayment = getService().findById(FixedAssetPayment.class, getInstance().getFixedAssetPayment().getId());
                } catch (EntryNotFoundException e) {
                    log.error(e, "The payment have not been found");
                }
                if (!areCurrenciesEqual()
                        || fixedAssetPayment.getSourceCurrency().equals(FinancesCurrencyType.D)
                        || fixedAssetPayment.getPayCurrency().equals(FinancesCurrencyType.D)) {
                    foreignExchangeRate = fixedAssetPayment.getExchangeRate();
                } else {
                    try {
                        foreignExchangeRate = financesExchangeRateService.findLastExchangeRateByCurrency(FinancesCurrencyType.D.name());
                    } catch (FinancesCurrencyNotFoundException e) {
                        log.debug("finances currency not found");
                    } catch (FinancesExchangeRateNotFoundException e) {
                        log.debug("finances exchange rate not found");
                    }
                }
            }
        }
        if (!getInstance().getState().equals(FixedAssetVoucherState.PEN)
                && getInstance().getFixedAssetVoucherType().getFixedAssetMovementTypeEnum().equals(FixedAssetMovementTypeEnum.MEJ)) {
            try {
                fixedAssetPayment = getService().findById(FixedAssetPayment.class, getInstance().getFixedAssetPayment().getId());
                if (!areCurrenciesEqual()
                        || fixedAssetPayment.getSourceCurrency().equals(FinancesCurrencyType.D)
                        || fixedAssetPayment.getPayCurrency().equals(FinancesCurrencyType.D)) {
                    foreignExchangeRate = fixedAssetPayment.getExchangeRate();
                } else {
                    try {
                        foreignExchangeRate = financesExchangeRateService.findLastExchangeRateByCurrency(FinancesCurrencyType.D.name());
                    } catch (FinancesCurrencyNotFoundException e) {
                        log.debug("finances currency not found");
                    } catch (FinancesExchangeRateNotFoundException e) {
                        log.debug("finances exchange rate not found");
                    }
                }
            } catch (EntryNotFoundException e) {
                log.error(e, "The payment have not been found");
            }
        }

        return Outcome.SUCCESS;
    }

    @End(beforeRedirect = true)
    @Restrict("#{s:hasPermission('FIXEDASSETVOUCHER','CREATE') and s:hasPermission('FIXEDASSETREGISTRATION','VIEW') }")
    public String registration() {
        validateBusinessUnit(getInstance().getBusinessUnit());
        boolean isDateAfterFixedAssetProcessMonthDate = fixedAssetMonthProcessService.isDateAfterFixedAssetMothProcessInitDate(getInstance().getMovementDate());
        if (isDateAfterFixedAssetProcessMonthDate) {
            addRegistrationDateBeforeModuleMonthMessage();
            return Outcome.REDISPLAY;
        }
        try {
            fixedAssetVoucherService.registration(getInstance());
            addRegistrationSuccessfulMessage();
            select(getInstance());
            return Outcome.REDISPLAY;
        } catch (EntryDuplicatedException e) {
            addDuplicatedMessage();
            return Outcome.REDISPLAY;
        } catch (DuplicatedFixedAssetCodeException e) {
            addDuplicatedFixedAssetCodeMessage();
            return Outcome.REDISPLAY;
        } catch (CompanyConfigurationNotFoundException e) {
            addCompanyConfigurationNotFoundErrorMessage();
            return Outcome.REDISPLAY;
        }

    }

    @End
    @Restrict("#{s:hasPermission('FIXEDASSETVOUCHER','UPDATE') and s:hasPermission('FIXEDASSETREGISTRATIONAPPROVAL','VIEW') }")
    public String approveRegistration() {
        validateBusinessUnit(getInstance().getBusinessUnit());
        boolean isDateAfterFixedAssetProcessMonthDate = fixedAssetMonthProcessService.isDateAfterFixedAssetMothProcessInitDate(getInstance().getMovementDate());
        if (isDateAfterFixedAssetProcessMonthDate) {
            addRegistrationDateBeforeModuleMonthMessage();
            return Outcome.REDISPLAY;
        }
        try {
            fixedAssetVoucherService.approveRegistration(getInstance());
            addRegistrationApprovalSuccessfulMessage();
            return Outcome.SUCCESS;
        } catch (EntryDuplicatedException e) {
            addDuplicatedMessage();
            return Outcome.REDISPLAY;
        } catch (DuplicatedFixedAssetCodeException e) {
            addDuplicatedFixedAssetCodeMessage();
            return Outcome.REDISPLAY;
        } catch (CompanyConfigurationNotFoundException e) {
            addCompanyConfigurationNotFoundErrorMessage();
            return Outcome.REDISPLAY;
        } catch (FixedAssetVoucherApprovedException e) {
            addFixedAssetVoucherApprovedErrorMessage();
            return Outcome.REDISPLAY;
        } catch (FixedAssetVoucherAnnulledException e) {
            addFixedAssetVoucherAnnulledErrorMessage();
            return Outcome.REDISPLAY;
        } catch (FixedAssetPurchaseOrderAlreadyRegisteredByAnotherFixedAssetVoucherException e) {
            addFixedAssetPurchaseOrderAlreadyRegisteredByAnotherFixedAssetVoucherMessage();
            return Outcome.REDISPLAY;
        } catch (FinancesExchangeRateNotFoundException e) {
            addFinancesExchangeRateForDepreciationNotFoundMessage();
            return Outcome.REDISPLAY;
        } catch (FinancesCurrencyNotFoundException e) {
            addFinancesCurrencyNotFoundMessage();
            return Outcome.REDISPLAY;
        }
    }

    @End
    @Restrict("#{s:hasPermission('FIXEDASSETVOUCHER','CREATE') and s:hasPermission('FIXEDASSETTRANSFERENCE','VIEW') }")
    public String transfer() {
        if (null == getSelectedFixedAssetList() || getSelectedFixedAssetList().size() <= 0) {
            addEmptySelectedFixedAssetListMessage();
            return Outcome.REDISPLAY;
        }
        for (FixedAsset fixedAsset : getSelectedFixedAssetList()) {
            //validate BusinessUnit access manually because the old business unit was over write by the new business unit
            validateBusinessUnit(fixedAssetService.getDataBaseFixedAsset(fixedAsset).getBusinessUnit());
        }
        try {
            fixedAssetVoucherService.transference(getInstance(), getSelectedFixedAssetList());
            addSuccessfulTransferenceMessage();
            return Outcome.SUCCESS;
        } catch (ConcurrencyException e) {
            concurrencyLog();
            try {
                setInstance(getService().findById(getEntityClass(), getId(getInstance()), true));
            } catch (EntryNotFoundException e1) {
                entryNotFoundLog();
                addNotFoundMessage();
                return Outcome.FAIL;
            }
            addUpdateConcurrencyMessage();
            return Outcome.REDISPLAY;
        } catch (EntryDuplicatedException e) {
            addDuplicatedMessage();
            return Outcome.REDISPLAY;
        } catch (FixedAssetInvalidStateException e) {
            addFixedAssetInvalidStateMessage(e);
            return Outcome.REDISPLAY;
        }
    }

    @End
    @Restrict("#{s:hasPermission('FIXEDASSETVOUCHER','UPDATE') and s:hasPermission('FIXEDASSETTRANSFERENCEAPPROVAL','VIEW') }")
    public String approveTransfer() {
        if (null == getSelectedFixedAssetList() || getSelectedFixedAssetList().size() <= 0) {
            addEmptySelectedFixedAssetListMessage();
            return Outcome.REDISPLAY;
        }
        for (FixedAsset fixedAsset : getSelectedFixedAssetList()) {
            //validate BusinessUnit access manually because the old business unit was over write by the new business unit
            validateBusinessUnit(fixedAssetService.getDataBaseFixedAsset(fixedAsset).getBusinessUnit());
        }
        try {
            fixedAssetVoucherService.approveTransference(getInstance(), getSelectedFixedAssetList());
            addSuccessfulTransferenceMessage();
            return Outcome.SUCCESS;
        } catch (ConcurrencyException e) {
            concurrencyLog();
            try {
                setInstance(getService().findById(getEntityClass(), getId(getInstance()), true));
            } catch (EntryNotFoundException e1) {
                entryNotFoundLog();
                addNotFoundMessage();
                return Outcome.FAIL;
            }
            addUpdateConcurrencyMessage();
            return Outcome.REDISPLAY;
        } catch (EntryDuplicatedException e) {
            addDuplicatedMessage();
            return Outcome.REDISPLAY;
        } catch (FixedAssetVoucherApprovedException e) {
            addFixedAssetVoucherApprovedErrorMessage();
            return Outcome.REDISPLAY;
        } catch (FixedAssetVoucherAnnulledException e) {
            addFixedAssetVoucherAnnulledErrorMessage();
            return Outcome.REDISPLAY;
        } catch (FixedAssetInvalidStateException e) {
            addFixedAssetInvalidStateMessage(e);
            return Outcome.REDISPLAY;
        } catch (FixedAssetMovementInvalidStateException e) {
            addFixedAssetMovementInvalidStateMessage(e);
            return Outcome.REDISPLAY;
        }
    }

    @End
    @Restrict("#{s:hasPermission('FIXEDASSETVOUCHER','CREATE') and s:hasPermission('FIXEDASSETDISCHARGE','VIEW') }")
    public String discharge() {
        if (null == getSelectedFixedAssetList() || getSelectedFixedAssetList().size() <= 0) {
            addEmptySelectedFixedAssetListMessage();
            return Outcome.REDISPLAY;
        }
        for (FixedAsset fixedAsset : getSelectedFixedAssetList()) {
            //validate BusinessUnit access manually because the old business unit was over write by the new business unit
            validateBusinessUnit(fixedAssetService.getDataBaseFixedAsset(fixedAsset).getBusinessUnit());
        }
        try {
            fixedAssetVoucherService.discharge(getInstance(), getSelectedFixedAssetList());
            addSuccessfulDischargeMessage();
            return Outcome.SUCCESS;
        } catch (ConcurrencyException e) {
            concurrencyLog();
            try {
                setInstance(getService().findById(getEntityClass(), getId(getInstance()), true));
            } catch (EntryNotFoundException e1) {
                entryNotFoundLog();
                addNotFoundMessage();
                return Outcome.FAIL;
            }
            addUpdateConcurrencyMessage();
            return Outcome.REDISPLAY;
        } catch (EntryDuplicatedException e) {
            addDuplicatedMessage();
            return Outcome.REDISPLAY;
        } catch (FixedAssetInvalidStateException e) {
            addFixedAssetInvalidStateMessage(e);
            return Outcome.REDISPLAY;
        }
    }

    @End
    @Restrict("#{s:hasPermission('FIXEDASSETVOUCHER','UPDATE') and s:hasPermission('FIXEDASSETTRANSFERENCEAPPROVAL','VIEW') }")
    public String approveDischarge() {
        if (null == getSelectedFixedAssetList() || getSelectedFixedAssetList().size() <= 0) {
            addEmptySelectedFixedAssetListMessage();
            return Outcome.REDISPLAY;
        }
        for (FixedAsset fixedAsset : getSelectedFixedAssetList()) {
            //validate BusinessUnit access manually because the old business unit was over write by the new business unit
            validateBusinessUnit(fixedAssetService.getDataBaseFixedAsset(fixedAsset).getBusinessUnit());
        }
        try {
            fixedAssetVoucherService.approveDischarge(getInstance(), getSelectedFixedAssetList());
            addDischargeApprovalSuccessfulMessage();
            return Outcome.SUCCESS;
        } catch (ConcurrencyException e) {
            concurrencyLog();
            try {
                setInstance(getService().findById(FixedAssetVoucher.class, getInstance().getId(), true));
            } catch (EntryNotFoundException e1) {
                entryNotFoundLog();
                addNotFoundMessage();
                return Outcome.FAIL;
            }
            addUpdateConcurrencyMessage();
            return Outcome.REDISPLAY;
        } catch (EntryDuplicatedException e) {
            addDuplicatedMessage();
            return Outcome.REDISPLAY;
        } catch (FixedAssetVoucherApprovedException e) {
            addFixedAssetVoucherApprovedErrorMessage();
            return Outcome.REDISPLAY;
        } catch (FixedAssetVoucherAnnulledException e) {
            addFixedAssetVoucherAnnulledErrorMessage();
            return Outcome.REDISPLAY;
        } catch (FixedAssetInvalidStateException e) {
            addFixedAssetInvalidStateMessage(e);
            return Outcome.REDISPLAY;
        } catch (FixedAssetMovementInvalidStateException e) {
            addFixedAssetMovementInvalidStateMessage(e);
            return Outcome.REDISPLAY;
        }
    }

    private void validateBusinessUnit(BusinessUnit businessUnit) {
        BusinessUnitValidatorUtil.i.validateBusinessUnit(businessUnit);
    }


    @Override
    @End
    @Restrict("#{s:hasPermission('FIXEDASSETVOUCHER','UPDATE')}")
    public String update() {
        Long currentVersion = (Long) getVersion(getInstance());
        updateExchangeRate();

        if (getInstance().getFixedAssetVoucherType().getFixedAssetMovementTypeEnum().equals(FixedAssetMovementTypeEnum.TRA)
                || getInstance().getFixedAssetVoucherType().getFixedAssetMovementTypeEnum().equals(FixedAssetMovementTypeEnum.BAJ)
                || getInstance().getFixedAssetVoucherType().getFixedAssetMovementTypeEnum().equals(FixedAssetMovementTypeEnum.MEJ)) {
            if (null == getSelectedFixedAssetList() || getSelectedFixedAssetList().size() <= 0) {
                addEmptySelectedFixedAssetListMessage();
                return Outcome.REDISPLAY;
            }
            for (FixedAsset fixedAsset : getSelectedFixedAssetList()) {
                //validate BusinessUnit access manually because the old business unit was over write by the new business unit
                validateBusinessUnit(fixedAssetService.getDataBaseFixedAsset(fixedAsset).getBusinessUnit());
            }
        }

        if (getInstance().getFixedAssetVoucherType().getFixedAssetMovementTypeEnum().equals(FixedAssetMovementTypeEnum.MEJ)) {
            /* check payment distribution */
            BigDecimal movementAmountSum = BigDecimal.ZERO;
            for (FixedAssetMovement fixedAssetMovement : fixedAssetFixedAssetMovementHashMap.values()) {
                movementAmountSum = BigDecimalUtil.sum(movementAmountSum, fixedAssetMovement.getBsAmount());
            }
            if (movementAmountSum.compareTo(fixedAssetPayment.getPayAmount()) != 0) {
                addPaymentDistributionMessage();
                return Outcome.REDISPLAY;
            }
        }
        try {
            getInstance().setFixedAssetPayment(fixedAssetPayment);
            fixedAssetVoucherService.updateFixedAssetVoucher(getInstance(), getSelectedFixedAssetList());
            return Outcome.SUCCESS;
        } catch (EntryDuplicatedException e) {
            setVersion(getInstance(), currentVersion);
            addDuplicatedMessage();
            return Outcome.REDISPLAY;
        } catch (ConcurrencyException e) {
            concurrencyLog();
            try {
                setInstance(getService().findById(getEntityClass(), getId(getInstance()), true));
            } catch (EntryNotFoundException e1) {
                entryNotFoundLog();
                addNotFoundMessage();
                return Outcome.FAIL;
            }
            addUpdateConcurrencyMessage();
            return Outcome.REDISPLAY;
        } catch (FixedAssetVoucherApprovedException e) {
            addFixedAssetVoucherApprovedErrorMessage();
            return Outcome.REDISPLAY;
        } catch (FixedAssetVoucherAnnulledException e) {
            addFixedAssetVoucherAnnulledErrorMessage();
            return Outcome.REDISPLAY;
        } catch (FixedAssetInvalidStateException e) {
            addFixedAssetInvalidStateMessage(e);
            return Outcome.REDISPLAY;
        } catch (FixedAssetMovementInvalidStateException e) {
            addFixedAssetMovementInvalidStateMessage(e);
            return Outcome.REDISPLAY;
        }
    }

    @End
    @Restrict("#{s:hasPermission('FIXEDASSETVOUCHER','CREATE') and s:hasPermission('FIXEDASSETIMPROVEMENT','VIEW') }")
    public String improve() {
        getInstance().setCause(fixedAssetPayment.getDescription());
        if (null == getSelectedFixedAssetList() || getSelectedFixedAssetList().size() <= 0) {
            addEmptySelectedFixedAssetListMessage();
            return Outcome.REDISPLAY;
        }
        for (FixedAsset fixedAsset : getSelectedFixedAssetList()) {
            //validate BusinessUnit access manually because the old business unit was over write by the new business unit
            validateBusinessUnit(fixedAssetService.getDataBaseFixedAsset(fixedAsset).getBusinessUnit());
        }
        /* check payment distribution */
        BigDecimal movementAmountSum = BigDecimal.ZERO;
        for (FixedAssetMovement fixedAssetMovement : fixedAssetFixedAssetMovementHashMap.values()) {
            movementAmountSum = BigDecimalUtil.sum(movementAmountSum, fixedAssetMovement.getBsAmount());
        }
        if (movementAmountSum.compareTo(fixedAssetPayment.getPayAmount()) != 0) {
            addPaymentDistributionMessage();
            return Outcome.REDISPLAY;
        }
        fixedAssetPayment.setExchangeRate((fixedAssetPayment.getSourceCurrency().equals(FinancesCurrencyType.P)
                && fixedAssetPayment.getPayCurrency().equals(FinancesCurrencyType.P)) ? nationalExchangeRate : foreignExchangeRate);
        try {
            fixedAssetVoucherService.improve(getInstance(), fixedAssetFixedAssetMovementHashMap, fixedAssetPayment);
            addSuccessfulTransferenceMessage();
            return Outcome.SUCCESS;
        } catch (ConcurrencyException e) {
            concurrencyLog();
            try {
                setInstance(getService().findById(getEntityClass(), getId(getInstance()), true));
            } catch (EntryNotFoundException e1) {
                entryNotFoundLog();
                addNotFoundMessage();
                return Outcome.FAIL;
            }
            addUpdateConcurrencyMessage();
            return Outcome.REDISPLAY;
        } catch (EntryDuplicatedException e) {
            addDuplicatedMessage();
            return Outcome.REDISPLAY;
        } catch (FixedAssetInvalidStateException e) {
            addFixedAssetInvalidStateMessage(e);
            return Outcome.REDISPLAY;
        }

    }

    @End
    @Restrict("#{s:hasPermission('FIXEDASSETVOUCHER','UPDATE') and s:hasPermission('FIXEDASSETTRANSFERENCEAPPROVAL','VIEW') }")
    public String approveImprovement() {
        if (null == getSelectedFixedAssetList() || getSelectedFixedAssetList().size() <= 0) {
            addEmptySelectedFixedAssetListMessage();
            return Outcome.REDISPLAY;
        }
        for (FixedAsset fixedAsset : getSelectedFixedAssetList()) {
            //validate BusinessUnit access manually because the old business unit was over write by the new business unit
            validateBusinessUnit(fixedAssetService.getDataBaseFixedAsset(fixedAsset).getBusinessUnit());
        }
        /* check payment distribution */
        BigDecimal movementAmountSum = BigDecimal.ZERO;
        for (FixedAssetMovement fixedAssetMovement : fixedAssetFixedAssetMovementHashMap.values()) {
            movementAmountSum = BigDecimalUtil.sum(movementAmountSum, fixedAssetMovement.getBsAmount());
        }
        if (movementAmountSum.compareTo(fixedAssetPayment.getPayAmount()) != 0) {
            addPaymentDistributionMessage();
            return Outcome.REDISPLAY;
        }
        try {
            fixedAssetVoucherService.approveImprovement(getInstance(), getSelectedFixedAssetList(), fixedAssetPayment);
            addTransferenceApprovalSuccessfulMessage();
            return Outcome.SUCCESS;
        } catch (ConcurrencyException e) {
            concurrencyLog();
            try {
                setInstance(getService().findById(getEntityClass(), getId(getInstance()), true));
            } catch (EntryNotFoundException e1) {
                entryNotFoundLog();
                addNotFoundMessage();
                return Outcome.FAIL;
            }
            addUpdateConcurrencyMessage();
            return Outcome.REDISPLAY;
        } catch (EntryDuplicatedException e) {
            addDuplicatedMessage();
            return Outcome.REDISPLAY;
        } catch (FixedAssetVoucherApprovedException e) {
            addFixedAssetVoucherApprovedErrorMessage();
            return Outcome.REDISPLAY;
        } catch (FixedAssetVoucherAnnulledException e) {
            addFixedAssetVoucherAnnulledErrorMessage();
            return Outcome.REDISPLAY;
        } catch (FixedAssetInvalidStateException e) {
            addFixedAssetInvalidStateMessage(e);
            return Outcome.REDISPLAY;
        } catch (FixedAssetMovementInvalidStateException e) {
            addFixedAssetMovementInvalidStateMessage(e);
            return Outcome.REDISPLAY;
        }
    }

    @End
    public String annulFixedAssetVoucher() {
        try {
            fixedAssetVoucherService.annulFixedAssetVoucher(getInstance());
            addFixedAssetVoucherAnnulledMessage();
            return Outcome.SUCCESS;
        } catch (FixedAssetVoucherApprovedException e) {
            addFixedAssetVoucherApprovedErrorMessage();
            return Outcome.REDISPLAY;
        } catch (ConcurrencyException e) {
            updateCurrentInstance();
            addUpdateConcurrencyMessage();
            return Outcome.REDISPLAY;
        } catch (FixedAssetVoucherAnnulledException e) {
            addFixedAssetVoucherAnnulledErrorMessage();
            return Outcome.REDISPLAY;
        } catch (EntryDuplicatedException e) {
            addDuplicatedMessage();
            return Outcome.REDISPLAY;
        }
    }

    /**
     * Actyion to navigate
     *
     * @return
     */
    @End
    @Restrict("#{s:hasPermission('DISCOUNTCOMENT','CREATE')}")
    public String addDiscountComment() {
        return Outcome.SUCCESS;
    }

    public void updateCurrentInstance() {
        setInstance(fixedAssetVoucherService.findFixedAssetVoucher(getInstance().getId()));
    }

    public void updateExchangeRate() {
        /* apply only over the fixedAssetVoucher */
        if (getInstance().getCurrency() != null
                && getInstance().getCurrency().equals(FinancesCurrencyType.P)) {
            getInstance().setBsSusRate(BigDecimal.ONE);
        }
    }


    public void paymentTypeChanged() {
        if (getInstance().getCustodianJobContract() != null) {
            getFixedAssetPayment().setBeneficiaryName(getInstance().getCustodianJobContract().getContract().getEmployee().getSingleFullName());
        }
        getFixedAssetPayment().setFixedAssetBeneficiaryType(FixedAssetBeneficiaryType.PERSON);
        if (isPaymentBankAccount() || isPaymentWithCheck()) {
            getFixedAssetPayment().setCashBoxCashAccount(null);
        }
        if (isPaymentCashBox()) {
            getFixedAssetPayment().setBankAccount(null);
        }
    }

    public void bankAccountFieldChanged() {
        updateAmounts();
    }
    /* updates all the data taking into account FixedAssetVoucher amount */

    public void updateAmounts() {
        if (!isManaged() || isFixedAssetVoucherPending()) {
            if (areCurrenciesEqual()) {
                getFixedAssetPayment().setSourceAmount(getFixedAssetPayment().getPayAmount());
            } else if (null != fixedAssetPayment.getSourceCurrency()
                    && null != foreignExchangeRate
                    && null != getFixedAssetPayment().getSourceAmount()) {
                BigDecimal sourceAmount;
                if (getFixedAssetPayment().getSourceCurrency().equals(FinancesCurrencyType.P)) {
                    sourceAmount = BigDecimalUtil.multiply(getFixedAssetPayment().getPayAmount(), nationalExchangeRate);
                } else {
                    sourceAmount = BigDecimalUtil.divide(getFixedAssetPayment().getPayAmount(), foreignExchangeRate);
                }
                getFixedAssetPayment().setSourceAmount(sourceAmount);
            } else {
                getFixedAssetPayment().setSourceAmount(null);
            }
        }
    }

    public void updateSourceAmount() {
        if (areCurrenciesEqual()) {
            getFixedAssetPayment().setSourceAmount(getFixedAssetPayment().getPayAmount());
        } else {
            if (null != getFixedAssetPayment().getSourceCurrency()
                    && null != foreignExchangeRate
                    && null != getFixedAssetPayment().getPayAmount()) {
                BigDecimal sourceAmount;
                if (getFixedAssetPayment().getSourceCurrency().equals(FinancesCurrencyType.P)) {
                    sourceAmount = BigDecimalUtil.multiply(getFixedAssetPayment().getPayAmount(), nationalExchangeRate);
                } else {
                    sourceAmount = BigDecimalUtil.divide(getFixedAssetPayment().getPayAmount(), foreignExchangeRate);
                }
                getFixedAssetPayment().setSourceAmount(sourceAmount);
            } else {
                getFixedAssetPayment().setSourceAmount(null);
            }
        }
    }

    public boolean isEnableExchangeRateField() {
        return (null != getInstance().getCurrency()) &&
                (!FinancesCurrencyType.P.equals(getInstance().getCurrency()));
    }

    public boolean isEnablePaymentExchangeRateField() {
        /* both fields are not empty */
        FinancesCurrencyType paymentAccountCurrency = null;
        if (null != getFixedAssetPayment().getSourceCurrency() && null != getFixedAssetPayment().getPaymentType()) {
            if (null != getFixedAssetPayment().getBankAccount() &&
                    (isPaymentBankAccount() || isPaymentWithCheck())) {
                paymentAccountCurrency = getFixedAssetPayment().getBankAccount().getCurrency();
            }
            if (null != getFixedAssetPayment().getCashBoxCashAccount() && isPaymentCashBox()) {
                paymentAccountCurrency = getFixedAssetPayment().getCashBoxCashAccount().getCurrency();
            }
            if (!FinancesCurrencyType.P.equals(fixedAssetPayment.getPayCurrency())
                    || (null != paymentAccountCurrency && !FinancesCurrencyType.P.equals(paymentAccountCurrency))) {
                return true;
            }
        }
        return false;
    }

    public List<FixedAsset> getFixedAssetMovementDetailList() {
        return fixedAssetMovementDetailList;
    }

    public void setFixedAssetMovementDetailList(List<FixedAsset> fixedAssetMovementDetailList) {
        this.fixedAssetMovementDetailList = fixedAssetMovementDetailList;
    }

    public boolean areCurrenciesEqual() {
        /* both fields are not empty */
        return null != getFixedAssetPayment().getSourceCurrency()
                && getFixedAssetPayment().getSourceCurrency().equals(getFixedAssetPayment().getPayCurrency());
    }

    public boolean isEnableCheckFields() {
        return null != getFixedAssetPayment().getPaymentType()
                && (PurchaseOrderPaymentType.PAYMENT_WITH_CHECK.equals(getFixedAssetPayment().getPaymentType())
                || PurchaseOrderPaymentType.PAYMENT_BANK_ACCOUNT.equals(getFixedAssetPayment().getPaymentType()));
    }

    public boolean isEnableBeneficiaryTypeField() {
        return isEnableCheckFields()
                && getInstance().getFixedAssetVoucherType() != null && (getInstance().getFixedAssetVoucherType().getFixedAssetMovementTypeEnum().equals(FixedAssetMovementTypeEnum.ALT));
    }

    public boolean isEnableBankAccountInfo() {
        return null != getFixedAssetPayment().getPaymentType()
                && (PurchaseOrderPaymentType.PAYMENT_WITH_CHECK.equals(getFixedAssetPayment().getPaymentType())
                || PurchaseOrderPaymentType.PAYMENT_BANK_ACCOUNT.equals(getFixedAssetPayment().getPaymentType()));
    }

    private boolean useEnableComputeButton() {
        return isEnableExchangeRateField();
    }

    public void assignCustodianJobContract(JobContract jobContract) {
        try {
            JobContract jobContractRetrieved = getService().findById(JobContract.class, jobContract.getId());
            getInstance().setCustodianJobContract(jobContractRetrieved);
            if (!isManaged() && isEnableCheckFields()) {
                getFixedAssetPayment().setBeneficiaryName(getInstance().getCustodianJobContract().getContract().getEmployee().getSingleFullName());
            }
            getInstance().setBusinessUnit(jobContractRetrieved.getJob().getOrganizationalUnit().getBusinessUnit());
            getInstance().setCostCenter(getService().findById(CostCenter.class, jobContractRetrieved.getJob().getOrganizationalUnit().getCostCenter().getId()));
        } catch (EntryNotFoundException e) {
            entryNotFoundErrorLog(e);
            log.error(e, "An unexpected error have happened");
        }
    }

    public void clearPurchaseOrder() {
        getInstance().setPurchaseOrder(null);
    }

    public void assignPurchaseOrder(PurchaseOrder purchaseOrder) {
        try {
            getInstance().setPurchaseOrder(getService().findById(PurchaseOrder.class, purchaseOrder.getId()));
            JobContract jobContract = null != getInstance().getPurchaseOrder() ? getService().findById(JobContract.class, getInstance().getPurchaseOrder().getPetitionerJobContract().getId()) : null;

            getInstance().setCustodianJobContract(jobContract);
            getInstance().setBusinessUnit(null != jobContract ? jobContract.getJob().getOrganizationalUnit().getBusinessUnit() : null);
            getInstance().setCostCenter(null != jobContract ? getService().findById(CostCenter.class, jobContract.getJob().getOrganizationalUnit().getCostCenter().getId()) : null);
            getInstance().setCause(getInstance().getPurchaseOrder().getGloss());
        } catch (EntryNotFoundException e) {
            entryNotFoundErrorLog(e);
            log.error(e, "An unexpected error have happened");
        }
    }

    public void assignPurchaseOrder() {
        if (null != getInstance().getPurchaseOrder()) {
            assignPurchaseOrder(getInstance().getPurchaseOrder());
        }
    }

    public void clearCustodianJobContract() {
        getInstance().setCustodianJobContract(null);
        getInstance().setBusinessUnit(null);
        getInstance().setCostCenter(null);
    }

    public void clearEmployeeAndContract() {
        getInstance().setCustodianJobContract(null);
        getInstance().setBusinessUnit(null);
        getInstance().setCostCenter(null);
    }

    public void assignCostCenter(CostCenter costCenter) {
        try {
            getInstance().setCostCenter(getService().findById(CostCenter.class, costCenter.getId()));
        } catch (EntryNotFoundException e) {
            entryNotFoundErrorLog(e);
            log.error(e, "An unexpected error have happened");
        }
    }

    public void assignCostCenter() {
        if (null != getInstance().getCostCenter()) {
            assignCostCenter(getInstance().getCostCenter());
        }
    }

    public void clearCostCenter() {
        getInstance().setCostCenter(null);
    }

    public void assignFixedAssetVoucherType(FixedAssetMovementType fixedAssetMovementType) {
        try {
            getInstance().setFixedAssetVoucherType(getService().findById(FixedAssetMovementType.class, fixedAssetMovementType.getId()));
        } catch (EntryNotFoundException e) {
            entryNotFoundErrorLog(e);
            log.error(e, "An unexpected error have happened");
        }
    }

    public void assignFixedAssetVoucherType() {
        if (null != getInstance().getFixedAssetVoucherType()) {
            assignFixedAssetVoucherType(getInstance().getFixedAssetVoucherType());
        }
    }

    public void assignBusinessUnit(BusinessUnit businessUnit) {
        try {
            getInstance().setBusinessUnit(getService().findById(BusinessUnit.class, businessUnit.getId()));
        } catch (EntryNotFoundException e) {
            entryNotFoundErrorLog(e);
            log.error(e, "An unexpected error have happened");
        }
    }

    public void assignBusinessUnit() {
        if (null != getInstance().getBusinessUnit()) {
            assignBusinessUnit(getInstance().getBusinessUnit());
        }
    }

    public void clearBusinessUnit() {
        getInstance().setBusinessUnit(null);
    }

    public boolean isEnableContractInfo() {
        return getInstance().getPurchaseOrder() != null;
    }

    public boolean isEnableSpendDistributionTab() {
        return isReceivableFund();
    }

    public boolean isReceivableFund() {
        return getInstance().getFixedAssetVoucherType() != null && getInstance().getFixedAssetVoucherType().getFixedAssetMovementTypeEnum().equals(FixedAssetMovementTypeEnum.ALT);
    }

    public boolean isFixedAssetVoucherApproved() {
        return isManaged() && null != getInstance().getState() && FixedAssetVoucherState.APR.equals(getInstance().getState());
    }

    public boolean isFixedAssetVoucherPending() {
        return isManaged() && null != getInstance().getState() && FixedAssetVoucherState.PEN.equals(getInstance().getState());
    }

    public boolean isFixedAssetVoucherAnnulled() {
        return isManaged() && null != getInstance().getState() && FixedAssetVoucherState.ANL.equals(getInstance().getState());
    }


    public void addFixedAssetVoucherApprovedErrorMessage() {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                "FixedAssetVoucher.error.fixedAssetVoucherAlreadyApproved");
    }

    public void addFixedAssetVoucherAnnulledMessage() {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.INFO,
                "FixedAssetVoucher.info.fixedAssetVoucherAnnulled", getInstance().getVoucherCode());
    }

    public void addFixedAssetVoucherAnnulledErrorMessage() {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.INFO,
                "FixedAssetVoucher.error.fixedAssetVoucherAlreadyAnnulled");
    }

    private void addFixedAssetPurchaseOrderAlreadyRegisteredByAnotherFixedAssetVoucherMessage() {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                "FixedAssetVoucher.error.fixedAssetPurchaseOrderAlreadyRegisteredByAnotherFixedAssetVoucher");
    }

    public List<FixedAsset> getSelectedFixedAssetList() {
        if (!isManaged() && null == selectedFixedAssetList) {
            selectedFixedAssetList = new ArrayList<FixedAsset>();
        }
        return selectedFixedAssetList;
    }

    public void setSelectedFixedAssetList(List<FixedAsset> selectedFixedAssetList) {
        this.selectedFixedAssetList = selectedFixedAssetList;
    }

    public List<Long> getSelectedFixedAssetIdList() {
        if (selectedFixedAssetIdList.isEmpty()) {
            selectedFixedAssetIdList.add(new Long(0));
        }
        return selectedFixedAssetIdList;
    }

    public void setSelectedFixedAssetIdList(List<Long> selectedFixedAssetIdList) {
        this.selectedFixedAssetIdList = selectedFixedAssetIdList;
    }

    public List<FixedAssetState> getStateRestrictionList() {
        return stateRestrictionList;
    }

    public void setStateRestrictionList(List<FixedAssetState> stateRestrictionList) {
        this.stateRestrictionList = stateRestrictionList;
    }

    public void fixedAssetVoucherTypeChanged() {
        selectedFixedAssetList.clear();
        selectedFixedAssetIdList.clear();
        /* this checks and fix empty list*/
        fixedAssetFixedAssetMovementHashMap.clear();
        getInstance().setPurchaseOrder(null);
        getInstance().setCustodianJobContract(null);
        getInstance().setBusinessUnit(null);
        getInstance().setCostCenter(null);
        getInstance().setCause(null);
        fixedAssetPayment = new FixedAssetPayment();
        fixedAssetPayment.setCreationDate(new Date());
        fixedAssetPayment.setState(FixedAssetPaymentState.PENDING);
        fixedAssetPayment.setPaymentType(PurchaseOrderPaymentType.PAYMENT_WITH_CHECK);
        fixedAssetPayment.setFixedAssetBeneficiaryType(FixedAssetBeneficiaryType.PERSON);
        fixedAssetPayment.setPayCurrency(FinancesCurrencyType.P);
        try {
            foreignExchangeRate = financesExchangeRateService.findLastExchangeRateByCurrency(FinancesCurrencyType.D.name());
        } catch (FinancesCurrencyNotFoundException e) {
            log.debug("finances currency not found");
        } catch (FinancesExchangeRateNotFoundException e) {
            log.debug("finances exchange rate not found");
        }
        fixedAssetPayment.setBeneficiaryName(null);
        fixedAssetDataModel.clear();

        updateRestrictionList();
    }

    public void updateRestrictionList() {
        if (!isManaged() || isFixedAssetVoucherPending()) {
            if (isTransferenceMovement() || isDischargeMovement() || isImprovementMovement()) {
                stateRestrictionList = Arrays.asList(FixedAssetState.VIG, FixedAssetState.TDP);
            } else {
                stateRestrictionList = null;
            }
        } else {
            stateRestrictionList = null;
        }
    }

    public void addFixedAssetList(List<FixedAsset> fixedAssetList) {

        if (fixedAssetList != null) {
            for (FixedAsset fixedAssetItem : fixedAssetList) {
                FixedAsset fixedAsset;
                try {
                    fixedAsset = getService().findById(FixedAsset.class, fixedAssetItem.getId());
                    if (!getSelectedFixedAssetList().contains(fixedAsset)) {
                        getSelectedFixedAssetList().add(fixedAsset);
                        getSelectedFixedAssetIdList().add(fixedAsset.getId());
                        FixedAssetMovement fixedAssetMovement = new FixedAssetMovement();
                        fixedAssetFixedAssetMovementHashMap.put(fixedAsset, fixedAssetMovement);
                    }
                } catch (EntryNotFoundException e) {
                    log.error(e, "Entry not found");
                }
            }
            redistributePayment();
        }
    }

    public void addSelectedFixedAsset() {
        if (null != fixedAsset) {
            List<FixedAsset> fixedAssetList = new ArrayList<FixedAsset>();
            fixedAssetList.add(fixedAsset);
            addFixedAssetList(fixedAssetList);
        }
        // resets the fixed Asset search
        fixedAsset = null;
    }

    public void removeInstance(FixedAsset fixedAssetItem) {
        FixedAsset fixedAsset = null;
        try {
            fixedAsset = getService().findById(FixedAsset.class, fixedAssetItem.getId());
            if (getSelectedFixedAssetList().contains(fixedAsset)) {
                getSelectedFixedAssetList().remove(fixedAsset);
                getSelectedFixedAssetIdList().remove(fixedAsset.getId());
                fixedAssetFixedAssetMovementHashMap.remove(fixedAsset);
            }
        } catch (EntryNotFoundException e) {
            log.error(e, "Entry not found");
        }
        redistributePayment();
    }

    /* Redistributes the payment to the movements associated to the voucher*/

    public void redistributePayment() {
        /* Redistribute the payment*/
        if (null != getInstance().getFixedAssetVoucherType() &&
                null != fixedAssetPayment.getPayAmount() &&
                getInstance().getFixedAssetVoucherType().getFixedAssetMovementTypeEnum().equals(FixedAssetMovementTypeEnum.MEJ)) {

            int size = 0;
            BigDecimal portion = BigDecimal.ZERO;
            if (null != getSelectedFixedAssetList()) {
                size = getSelectedFixedAssetList().size();
            }
            if (null != fixedAssetPayment && null != fixedAssetPayment.getPayAmount() && size > 0) {
                portion = BigDecimalUtil.divide(fixedAssetPayment.getPayAmount(), BigDecimalUtil.toBigDecimal(size));
            }
            BigDecimal portionSum = BigDecimal.ZERO;
            int selectedFixedAssetListSize = getSelectedFixedAssetList().size();
            for (int i = 0; i < selectedFixedAssetListSize; i++) {
                FixedAsset fixedAsset = getSelectedFixedAssetList().get(i);
                if (i == (selectedFixedAssetListSize - 1)) {
                    portion = BigDecimalUtil.subtract(fixedAssetPayment.getPayAmount(), portionSum);
                }
                fixedAssetFixedAssetMovementHashMap.get(fixedAsset).setBsAmount(portion);
                portionSum = BigDecimalUtil.sum(portionSum, portion);
            }
        }
    }

    public void paymentAmountChanged() {
        updateSourceAmount();
        redistributePayment();
    }

    public void assignCashBoxCashAccount(CashAccount cashAccount) {
        try {
            cashAccount = getService().findById(CashAccount.class, cashAccount.getId());
        } catch (EntryNotFoundException e) {
            entryNotFoundErrorLog(e);
        }
        getFixedAssetPayment().setCashBoxCashAccount(cashAccount);
        updateAmounts();
    }

    public void clearCashBoxCashAccount() {
        getFixedAssetPayment().setCashBoxCashAccount(null);
    }

    /*getters and setters*/

    public BigDecimal getForeignExchangeRate() {
        return foreignExchangeRate;
    }

    public void setForeignExchangeRate(BigDecimal foreignExchangeRate) {
        this.foreignExchangeRate = foreignExchangeRate;
    }

    public BigDecimal getNationalExchangeRate() {
        return nationalExchangeRate;
    }

    public void setNationalExchangeRate(BigDecimal nationalExchangeRate) {
        this.nationalExchangeRate = nationalExchangeRate;
    }

    public FixedAssetPayment getFixedAssetPayment() {
        return fixedAssetPayment;
    }

    public void setFixedAssetPayment(FixedAssetPayment fixedAssetPayment) {
        this.fixedAssetPayment = fixedAssetPayment;
    }

    public String getSourceAmountLabel() {
        if (null != getFixedAssetPayment().getPaymentType()) {
            if (null != getFixedAssetPayment().getBankAccount()
                    && getFixedAssetPayment().getPaymentType().equals(PurchaseOrderPaymentType.PAYMENT_WITH_CHECK)) {
                return messages.get("FixedAssetVoucherPayment.sourceCheckAmount");
            } else if (null != getFixedAssetPayment().getBankAccount()
                    && getFixedAssetPayment().getPaymentType().equals(PurchaseOrderPaymentType.PAYMENT_BANK_ACCOUNT)) {
                return messages.get("FixedAssetVoucherPayment.sourceBankAmount");
            } else {
                return messages.get("FixedAssetVoucherPayment.sourceCashBoxAmount");
            }
        }
        return null;
    }

    public HashMap<FixedAsset, FixedAssetMovement> getFixedAssetFixedAssetMovementHashMap() {
        return fixedAssetFixedAssetMovementHashMap;
    }

    public void setFixedAssetFixedAssetMovementHashMap(HashMap<FixedAsset, FixedAssetMovement> fixedAssetFixedAssetMovementHashMap) {
        this.fixedAssetFixedAssetMovementHashMap = fixedAssetFixedAssetMovementHashMap;
    }

    public boolean isPaymentWithCheck() {
        return null != getFixedAssetPayment().getPaymentType() && getFixedAssetPayment().getPaymentType().equals(PurchaseOrderPaymentType.PAYMENT_WITH_CHECK);
    }

    public boolean isPaymentBankAccount() {
        return null != getFixedAssetPayment().getPaymentType() && getFixedAssetPayment().getPaymentType().equals(PurchaseOrderPaymentType.PAYMENT_BANK_ACCOUNT);
    }

    public boolean isPaymentCashBox() {
        return null != getFixedAssetPayment().getPaymentType() && getFixedAssetPayment().getPaymentType().equals(PurchaseOrderPaymentType.PAYMENT_CASHBOX);
    }

    public boolean isRegistrationMovement() {
        return null != getInstance().getFixedAssetVoucherType() && getInstance().getFixedAssetVoucherType().getFixedAssetMovementTypeEnum().equals(FixedAssetMovementTypeEnum.ALT);
    }

    public boolean isTransferenceMovement() {
        return null != getInstance().getFixedAssetVoucherType() && getInstance().getFixedAssetVoucherType().getFixedAssetMovementTypeEnum().equals(FixedAssetMovementTypeEnum.TRA);
    }

    public boolean isDischargeMovement() {
        return null != getInstance().getFixedAssetVoucherType() && getInstance().getFixedAssetVoucherType().getFixedAssetMovementTypeEnum().equals(FixedAssetMovementTypeEnum.BAJ);
    }

    public boolean isImprovementMovement() {
        return null != getInstance().getFixedAssetVoucherType() && getInstance().getFixedAssetVoucherType().getFixedAssetMovementTypeEnum().equals(FixedAssetMovementTypeEnum.MEJ);
    }

    public boolean isShowFixedAssetByPurchaseOrderTab() {
        return isRegistrationMovement() && isFixedAssetVoucherPending() && null != getInstance().getPurchaseOrder()
                && getInstance().getPurchaseOrder().getPurchaseOrderCause().isFixedassetPurchase();
    }

    public boolean isShowFixedAssetByPurchaseOrderFieldSet() {
        return !isManaged() && isRegistrationMovement()
                && null != getInstance().getPurchaseOrder() && getInstance().getPurchaseOrder().getPurchaseOrderCause().isFixedassetPurchase();
    }

    public boolean isShowFixedAssetPartByPurchaseOrderFieldSet() {
        return !isManaged() && isRegistrationMovement()
                && null != getInstance().getPurchaseOrder() && getInstance().getPurchaseOrder().getPurchaseOrderCause().isFixedassetPartsPurchase();
    }

    public boolean isShowFixedAssetPartByPurchaseOrderTab() {
        return isRegistrationMovement() && isFixedAssetVoucherPending() && null != getInstance().getPurchaseOrder()
                && getInstance().getPurchaseOrder().getPurchaseOrderCause().isFixedassetPartsPurchase();
    }


    public boolean isShowSelectedToTransferFixedAssetsDiv() {
        return !isManaged() && (isTransferenceMovement() || isDischargeMovement() || isImprovementMovement());
    }

    public FixedAsset getFixedAsset() {
        return fixedAsset;
    }

    public void setFixedAsset(FixedAsset fixedAsset) {
        this.fixedAsset = fixedAsset;
    }

/* messages */

    @Override
    protected void addCreatedMessage() {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.INFO,
                "FixedAssetVoucher.message.created", getInstance().getVoucherCode());
    }

    @Override
    protected void addDeletedMessage() {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.INFO,
                "FixedAssetVoucher.message.deleted", getInstance().getVoucherCode());
    }

    @Override
    protected void addUpdatedMessage() {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.INFO,
                "FixedAssetVoucher.message.updated", getInstance().getVoucherCode());
    }

    private void addRegistrationDateBeforeModuleMonthMessage() {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                "FixedAsset.error.registrationDateBeforeModuleMonth");
    }

    private void addRegistrationSuccessfulMessage() {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.INFO,
                "FixedAssetVoucher.info.registrationSuccessful", getInstance().getVoucherCode());
    }

    private void addRegistrationApprovalSuccessfulMessage() {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.INFO,
                "FixedAssetVoucher.info.registrationApprovalSuccessful", getInstance().getVoucherCode());
    }

    private void addDuplicatedFixedAssetCodeMessage() {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR, "FixedAsset.error.duplicatedFixedAssetCode");
    }

    protected void addDuplicatedMessage() {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                "Common.message.duplicated", getInstance().getVoucherCode());
    }

    private void addSuccessfulTransferenceMessage() {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.INFO, "FixedAssetVoucher.info.successfulTransference", getInstance().getVoucherCode());
    }

    private void addSuccessfulDischargeMessage() {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.INFO, "FixedAssetVoucher.info.successfulDischarge", getInstance().getVoucherCode());
    }

    private void addTransferenceApprovalSuccessfulMessage() {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.INFO, "FixedAssetVoucher.info.transferenceApprovalSuccessful", getInstance().getVoucherCode());
    }

    private void addDischargeApprovalSuccessfulMessage() {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.INFO, "FixedAssetVoucher.info.dischargeApprovalSuccessful", getInstance().getVoucherCode());
    }

    private void addEmptySelectedFixedAssetListMessage() {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR, "FixedAssetVoucher.error.emptySelectedFixedAssetList");
    }

    private void addPaymentDistributionMessage() {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR, "FixedAssetVoucher.error.paymentDistribution");
    }

    private void addFixedAssetInvalidStateMessage(FixedAssetInvalidStateException e) {
        for (FixedAsset fixedAsset : e.getInvalidStateFixedAssetList()) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                    "FixedAssetVoucher.error.fixedAssetInvalidStateMessage",
                    FormatUtils.removePoint(fixedAsset.getFixedAssetCode().toString()), fixedAsset.getState());
        }
    }

    private void addFixedAssetMovementInvalidStateMessage(FixedAssetMovementInvalidStateException e) {
        for (FixedAssetMovement fixedAssetMovement : e.getInvalidStateFixedAssetMovementList()) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                    "FixedAssetVoucher.error.fixedAssetMovementInvalidStateMessage",
                    fixedAssetMovement.getMovementNumber(), fixedAssetMovement.getFixedAsset().getFixedAssetCode(), fixedAssetMovement.getState());
        }
    }

    @Override
    public void addNotFoundMessage() {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.WARN,
                "FixedAssetVoucher.error.notFound", getInstance().getVoucherCode());
    }

    private void addFinancesExchangeRateForDepreciationNotFoundMessage() {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                "FixedAssets.FinancesExchangeRateForDepreciationNotFoundException");
    }

    private void addFinancesCurrencyNotFoundMessage() {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                "FixedAssets.FinancesCurrencyNotFoundException");
    }
}
