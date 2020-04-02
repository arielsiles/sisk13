package com.encens.khipus.action.fixedassets;

import com.encens.khipus.exception.ConcurrencyException;
import com.encens.khipus.exception.EntryDuplicatedException;
import com.encens.khipus.exception.EntryNotFoundException;
import com.encens.khipus.exception.finances.CompanyConfigurationNotFoundException;
import com.encens.khipus.exception.finances.FinancesCurrencyNotFoundException;
import com.encens.khipus.exception.finances.FinancesExchangeRateNotFoundException;
import com.encens.khipus.exception.fixedassets.*;
import com.encens.khipus.framework.action.GenericAction;
import com.encens.khipus.framework.action.Outcome;
import com.encens.khipus.framework.service.GenericService;
import com.encens.khipus.model.admin.BusinessUnit;
import com.encens.khipus.model.common.File;
import com.encens.khipus.model.finances.CostCenter;
import com.encens.khipus.model.finances.FinancesCurrencyType;
import com.encens.khipus.model.finances.FinancesModule;
import com.encens.khipus.model.finances.JobContract;
import com.encens.khipus.model.fixedassets.*;
import com.encens.khipus.service.finances.FinancesExchangeRateService;
import com.encens.khipus.service.fixedassets.FixedAssetMonthProcessService;
import com.encens.khipus.service.fixedassets.FixedAssetService;
import com.encens.khipus.util.BigDecimalUtil;
import com.encens.khipus.util.BusinessUnitValidatorUtil;
import com.encens.khipus.util.FileUtil;
import com.encens.khipus.util.ValidatorUtil;
import com.encens.khipus.util.barcode.BarcodeData;
import com.encens.khipus.util.barcode.BarcodeRenderer;
import com.encens.khipus.util.fixedassets.FixedAssetDefaultConstants;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.*;
import org.jboss.seam.annotations.security.Restrict;
import org.jboss.seam.international.StatusMessage;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Actions for FixedAssetAction
 *
 * @author
 * @version 2.26
 */

@Name("fixedAssetAction")
@Scope(ScopeType.CONVERSATION)
public class FixedAssetAction extends GenericAction<FixedAsset> {
    @In(required = false)
    private FixedAssetPaymentAction fixedAssetPaymentAction;

    @In(create = true)
    private FixedAssetPartAction fixedAssetPartAction;

    @In
    private FixedAssetService fixedAssetService;

    @In
    private FixedAssetMonthProcessService fixedAssetMonthProcessService;

    @In
    private FinancesExchangeRateService financesExchangeRateService;

    /* this DataModel is injected in order to support the change duration functionality */
    @In(required = false)
    private FixedAssetDataModel fixedAssetDataModel;

    private BarcodeRenderer barcodeRenderer = new BarcodeRenderer("code39");

    private BarcodeData barcodeData = new BarcodeData();


    private boolean showCalculatedValues = false;

    private boolean showRegistrationDetail;

    private boolean showDischargeButton;

    private boolean showTransferenceButton;

    private boolean showPositiveImprovementButton;

    private FixedAssetMovement fixedAssetMovement = new FixedAssetMovement();

    private FixedAssetMovementType fixedAssetMovementType;

    private String gloss;

    private FixedAssetGroup fixedAssetGroup;

    /* this monthsGuaranty is created in order to support the change duration functionality */
    private Integer monthsGuaranty;

    private Boolean payNowConditions;

    @Create
    @SuppressWarnings({"UnusedDeclaration"})
    public String atCreateTime() {
        if (!isManaged()) {
            Date date;
            try {
                date = financesExchangeRateService.findLastFinancesExchangeRateDate4UfvSus(
                        FinancesCurrencyType.U.name(),
                        FinancesCurrencyType.D.name());
                getInstance().setRegistrationDate(date);
                updateRates();
                return Outcome.SUCCESS;
            } catch (FinancesCurrencyNotFoundException e) {
                return Outcome.SUCCESS;
            } catch (FinancesExchangeRateNotFoundException e) {
                return Outcome.SUCCESS;
            }
        }


        return Outcome.SUCCESS;
    }

    @Factory(value = "fixedAsset", scope = ScopeType.STATELESS)
    @Restrict("#{s:hasPermission('FIXEDASSET','VIEW')}")
    public FixedAsset initFixedAsset() {
        return getInstance();
    }

    @Factory(value = "fixedAssetStates", scope = ScopeType.STATELESS)
    public FixedAssetState[] getFixedAssetStates() {
        return FixedAssetState.values();
    }

    /* the values used to register an fixed asset*/

    @Factory(value = "fixedAssetRegistrationStates", scope = ScopeType.STATELESS)
    public FixedAssetState[] getFixedAssetRegistrationStates() {
        List<FixedAssetState> fixedAssetStateList = new ArrayList<FixedAssetState>();
        fixedAssetStateList.add(FixedAssetState.PEN);
        FixedAssetState[] fixedAssetStates = new FixedAssetState[1];
        fixedAssetStateList.toArray(fixedAssetStates);
        return fixedAssetStates;
    }

    @Override
    @Begin(flushMode = FlushModeType.MANUAL)
    @Restrict("#{s:hasPermission('FIXEDASSET','VIEW')}")
    public String select(FixedAsset instance) {
        validateBusinessUnit(instance.getBusinessUnit());

        showCalculatedValues = true;
        String outcome = super.select(instance);
        if (Outcome.SUCCESS.equals(outcome)) {
            fixedAssetGroup = getInstance().getFixedAssetSubGroup().getFixedAssetGroup();
            fixedAssetPartAction.readInstances();
        }

        if (!isPendant() && FileUtil.isEmpty(getInstance().getPhoto())) {
            getInstance().setPhoto(new File());
        }

        if (!ValidatorUtil.isBlankOrNull(getInstance().getBarCode())) {
            barcodeData.setText(getInstance().getBarCode());
            barcodeData.setWidth(200);
            barcodeData.setHeight(100);
        }

        return outcome;
    }

    @End
    public String depreciateAllFixedAssets() {
        if (FileUtil.isEmpty(getInstance().getPhoto())) {
            //noinspection NullableProblems
            getInstance().setPhoto(null);
        }

        validateBusinessUnit(getInstance().getBusinessUnit());

        List<Integer> result = new ArrayList<Integer>();
        Integer depreciated = 0;
        Integer adjusted = 0;
        result.add(depreciated);
        result.add(adjusted);
        try {
            fixedAssetService.depreciate(result, messages.get("FixedAsset.depreciationGloss"));
        } catch (CompanyConfigurationNotFoundException e) {
            addCompanyConfigurationNotFoundErrorMessage();
            return Outcome.REDISPLAY;
        } catch (ThereIsNoActualFixedAssetException e) {
            addThereIsNoActualFixedAssetsExceptionMessage();
            return Outcome.REDISPLAY;
        } catch (FinancesCurrencyNotFoundException e) {
            addFinancesCurrencyNotFoundExceptionMessage();
            return Outcome.REDISPLAY;
        } catch (FinancesExchangeRateNotFoundException e) {
            addFinancesExchangeRateForDepreciationNotFoundExceptionMessage();
            return Outcome.REDISPLAY;
        } catch (OutOfDateException e) {
            addDepreciationDateBeforeModuleMonthMessage();
            return Outcome.REDISPLAY;
        } catch (ConcurrencyException e) {
            addUpdateConcurrencyMessage();
            return Outcome.REDISPLAY;
        } catch (EntryDuplicatedException e) {
            addDuplicatedMessage();
            return Outcome.REDISPLAY;
        } catch (RuntimeException e) {
            log.error("Un unexpected error has happened ", e);
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR, "Un unexpected error has happened");
            return Outcome.FAIL;
        }

        facesMessages.addFromResourceBundle(StatusMessage.Severity.INFO, "FixedAsset.info.depreciatedSucceed", result.get(0));
        facesMessages.addFromResourceBundle(StatusMessage.Severity.INFO, "FixedAsset.info.adjustedSucceed", result.get(1));
        return Outcome.SUCCESS;
    }

    @Override
    protected GenericService getService() {
        return fixedAssetService;
    }

    @Override
    protected String getDisplayNameProperty() {
        return "detail";
    }

    public String getCostCenterFullName() {
        return getInstance().getCostCenter() != null ? getInstance().getCostCenter().getFullName() : null;
    }

    public void assignCostCenter(CostCenter costCenter) {
        getInstance().setCostCenter(costCenter);
    }

    public void clearCostCenter() {
        //noinspection NullableProblems
        getInstance().setCostCenter(null);
    }

    public void assignFixedAssetGroup(FixedAssetGroup fixedAssetGroup) {
        setFixedAssetGroup(fixedAssetGroup);
        clearFixedAssetSubGroup();
    }

    public void clearFixedAssetGroup() {
        //noinspection NullableProblems
        setFixedAssetGroup(null);
        clearFixedAssetSubGroup();
    }

    public void assignFixedAssetSubGroup(FixedAssetSubGroup fixedAssetSubGroup) {
        try {
            FixedAssetSubGroup emFixedAssetSubGroup = getService().findById(FixedAssetSubGroup.class, fixedAssetSubGroup.getId());
            getInstance().setFixedAssetSubGroup(emFixedAssetSubGroup);
            loadSubGroupInfo();
        } catch (EntryNotFoundException e) {
            entryNotFoundLog();
        }
    }

    public void clearFixedAssetSubGroup() {
        //noinspection NullableProblems
        getInstance().setFixedAssetSubGroup(null);
    }

    public String getEmployeeFullName() {
        return getInstance().getCustodianJobContract() != null ? getInstance().getCustodianJobContract().getContract().getEmployee().getFullName() : null;
    }

    public Integer getMonthsGuaranty() {
        return monthsGuaranty;
    }

    public void setMonthsGuaranty(Integer monthsGuaranty) {
        this.monthsGuaranty = monthsGuaranty;
    }

    public void assignJobContract(JobContract jobContract) {
        try {
            JobContract jobContractRetrieved = getService().findById(JobContract.class, jobContract.getId());
            getInstance().setCustodianJobContract(jobContractRetrieved);
        } catch (EntryNotFoundException e) {
            log.error(e, "An unexpected error have happened");
        }
    }

    public void clearJobContract() {
        //noinspection NullableProblems
        getInstance().setCustodianJobContract(null);
    }

    @Override
    @Restrict("#{s:hasPermission('FIXEDASSET','UPDATE')}")
    @End
    public String update() {
        validateBusinessUnit(getInstance().getBusinessUnit());

        if (FileUtil.isEmpty(getInstance().getPhoto())) {
            //noinspection NullableProblems
            getInstance().setPhoto(null);
        } else if (!FileUtil.isImageFormat(getInstance().getPhoto())) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR, "Common.error.image.invalidFormat");
            return Outcome.REDISPLAY;
        }

        try {
            fixedAssetService.update(getInstance());
            addUpdatedMessage();
        } catch (ConcurrencyException e) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR, "Common.error.concurrency");
            return com.encens.khipus.framework.action.Outcome.REDISPLAY;
        } catch (DuplicatedFixedAssetCodeException e) {
            addDuplicatedFixedAssetCodeMessage();
            return Outcome.REDISPLAY;
        } catch (EntryDuplicatedException e) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR, "Common.message.duplicated");
            return Outcome.REDISPLAY;
        }
        return Outcome.SUCCESS;
    }

    public String transfer() {
        //validate BusinessUnit access manually because the old business unit was over write by the new business unit
        validateBusinessUnit(getDatabaseFixedAsset().getBusinessUnit());

        if (FileUtil.isEmpty(getInstance().getPhoto())) {
            //noinspection NullableProblems
            getInstance().setPhoto(null);
        }

        try {
            fixedAssetService.transference(getInstance(), fixedAssetMovementType);
            addSuccessfulTransferenceMessage();
            return Outcome.SUCCESS;
        } catch (NoChangeForTransferenceException e) {
            addNoChangeForTransferenceMessage();
            return Outcome.REDISPLAY;
        } catch (ConcurrencyException e) {
            concurrencyLog();
        } catch (EntryDuplicatedException e) {
            e.printStackTrace();
        }
        return Outcome.SUCCESS;
    }


    public void loadSubGroupInfo() {
        /*update subGroup info to the fixedAsset in case there isn't input values*/
        getInstance().setDepreciationRate(getInstance().getFixedAssetSubGroup().getDepreciationRate());
        getInstance().setDuration(getInstance().getFixedAssetSubGroup().getDuration());
        getInstance().setRubbish(getInstance().getFixedAssetSubGroup().getRubbish());
        getInstance().setDetail(getInstance().getFixedAssetSubGroup().getDetail());
    }

    public FixedAssetMovement getFixedAssetMovement() {
        return fixedAssetMovement;
    }

    public void setFixedAssetMovement(FixedAssetMovement fixedAssetMovement) {
        this.fixedAssetMovement = fixedAssetMovement;
    }

    @End
    public String approveRegistration() {
        validateBusinessUnit(getInstance().getBusinessUnit());

        if (!validate()) {
            return Outcome.REDISPLAY;
        }

        if (!hasCompute() && payNowConditions) {
            addComputeRequiredMessage();
            return Outcome.REDISPLAY;
        }
        if (!checkComputation()) {
            addReComputeRequiredMessage();
            return Outcome.REDISPLAY;
        }
        if (getInstance().getPurchaseOrder() == null && !checkPayment() && payNowConditions) {
            addReComputePaymentRequiredMessage();
            return Outcome.REDISPLAY;
        }
        /* rubbish can't be greater than ufv original value*/
        if (getInstance().getRubbish().compareTo(getInstance().getUfvOriginalValue()) > 0) {
            addRubbishGreaterThanUfvOriginalValueMessage();
            return Outcome.REDISPLAY;
        }
        if (getInstance().getRegistrationDate() == null) {
            getInstance().setRegistrationDate(new Date());
        }

        String validationOutcome = fixedAssetPartAction.validateUnitPrices();
        if (!Outcome.SUCCESS.equals(validationOutcome)) {
            return Outcome.REDISPLAY;
        }

        /*boolean isDateAfterFixedAssetProcessMonthDate = fixedAssetMonthProcessService.isDateAfterFixedAssetMothProcessInitDate(getInstance().getRegistrationDate());
        if (isDateAfterFixedAssetProcessMonthDate) {
            addRegistrationDateBeforeModuleMonthMessage();
            return Outcome.REDISPLAY;
        }*/

        if (FileUtil.isEmpty(getInstance().getPhoto())) {
            //noinspection NullableProblems
            getInstance().setPhoto(null);
        }

        try {
            fixedAssetService.approveRegistration(getInstance(),
                    gloss,
                    fixedAssetMovement,
                    fixedAssetMovementType,
                    getFixedAssetPaymentInstance(),
                    fixedAssetPartAction.getInstances(),
                    payNowConditions);
            addRegistrationSuccessfulMessage();
            return Outcome.SUCCESS;
        } catch (EntryDuplicatedException e) {
            addDuplicatedMessage();
            return Outcome.REDISPLAY;
        } catch (DuplicatedFixedAssetCodeException e) {
            addDuplicatedFixedAssetCodeMessage();
            return Outcome.REDISPLAY;
        }
    }

    private FixedAssetPayment getFixedAssetPaymentInstance() {
        return fixedAssetPaymentAction != null ? fixedAssetPaymentAction.getInstance() : null;
    }

    private boolean hasCompute() {
        return getInstance().getSusOriginalValue() != null &&
                getInstance().getUfvOriginalValue() != null;
    }

    private boolean hasComputeImprovement() {
        return fixedAssetMovement.getUfvAmount() != null;
    }

    public String getGloss() {
        return gloss;
    }

    public void setGloss(String gloss) {
        this.gloss = gloss;
    }

    public FixedAssetGroup getFixedAssetGroup() {
        return fixedAssetGroup;
    }

    public void setFixedAssetGroup(FixedAssetGroup fixedAssetGroup) {
        this.fixedAssetGroup = fixedAssetGroup;
    }

    public boolean isPendant() {
        return getInstance().getState() == null || getInstance().getState().name().equals(FixedAssetState.PEN.name());
    }

    public boolean dischargeState() {
        if (getInstance().getState() == null) {
            return false;
        } else {
            if (getInstance().getState().name().equals(FixedAssetState.BAJ.name())) {
                return true;
            }
        }
        return false;
    }

    public boolean dischargeMovementType() {
        return fixedAssetMovementType != null && (fixedAssetMovementType.getFixedAssetMovementTypeEnum().equals(FixedAssetMovementTypeEnum.BAJ));
    }

    public boolean actualOrTotallyDepreciated() {
        String name = getInstance().getState().name();
        return (name.equals(FixedAssetState.VIG.name()) || name.equals(FixedAssetState.TDP.name()));
    }

    public FixedAssetMovementType getFixedAssetMovementType() {
        return fixedAssetMovementType;
    }

    public void setFixedAssetMovementType(FixedAssetMovementType fixedAssetMovementType) {
        this.fixedAssetMovementType = fixedAssetMovementType;
    }

    public String closeFixedAssetMonth() {
        try {
            fixedAssetService.closeActualMonth();
        } catch (DateBeforeModuleMonthException e) {
            log.trace(e);
            addDateBeforeModuleMonthExceptionMessage();
            return com.encens.khipus.framework.action.Outcome.REDISPLAY;
        } catch (ConcurrencyException e) {
            log.trace(e);
            addUpdateConcurrencyMessage();
            return com.encens.khipus.framework.action.Outcome.REDISPLAY;
        } catch (ThereAreNotAdjustedFixedAssetException e) {
            addThereAreNotAdjustedFixedAssetExceptionMessage();
            return com.encens.khipus.framework.action.Outcome.REDISPLAY;
        } catch (EntryDuplicatedException e) {
            log.trace(e);
            addDuplicatedMessage();
            return com.encens.khipus.framework.action.Outcome.REDISPLAY;
        } catch (ThereAreActualFixedAssetException e) {
            addThereAreActualFixedAssetExceptionMessage();
            return com.encens.khipus.framework.action.Outcome.REDISPLAY;
        }
        addCloseMonthSucceedMessage();
        return com.encens.khipus.framework.action.Outcome.SUCCESS;
    }

    public String discharge() {
        validateBusinessUnit(getInstance().getBusinessUnit());
        if (FileUtil.isEmpty(getInstance().getPhoto())) {
            //noinspection NullableProblems
            getInstance().setPhoto(null);
        }
        if (fixedAssetMovement.getMovementDate() == null) {
            fixedAssetMovement.setMovementDate(new Date());
            fixedAssetMovement.setCreationDate(new Date());
        }
        boolean isDateAfterFixedAssetProcessMonthDate =
                fixedAssetMonthProcessService.isDateAfterFixedAssetMothProcessInitDate(fixedAssetMovement.getMovementDate());
        if (isDateAfterFixedAssetProcessMonthDate) {
            addMovementDateBeforeModuleMonthMessage();
            return Outcome.REDISPLAY;
        }
        try {
            fixedAssetMovement.setBsUfvRate(financesExchangeRateService.findLastExchangeRateByCurrency(FinancesCurrencyType.U.name()));
            fixedAssetMovement.setBsSusRate(financesExchangeRateService.findLastExchangeRateByCurrency(FinancesCurrencyType.D.name()));
            fixedAssetService.dischargeFixedAsset(getInstance(), gloss, fixedAssetMovement, fixedAssetMovementType);
            facesMessages.addFromResourceBundle(StatusMessage.Severity.INFO, "FixedAsset.info.dischargeSucceed");
        } catch (CompanyConfigurationNotFoundException e) {
            addCompanyConfigurationNotFoundErrorMessage();
            return Outcome.REDISPLAY;
        } catch (FinancesCurrencyNotFoundException e) {
            addFinancesCurrencyNotFoundExceptionMessage();
            return Outcome.REDISPLAY;
        } catch (FinancesExchangeRateNotFoundException e) {
            addFinancesExchangeRateNotFoundExceptionMessage();
            return Outcome.REDISPLAY;
        } catch (ConcurrencyException e) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR, "Common.error.concurrency");
            return com.encens.khipus.framework.action.Outcome.REDISPLAY;
        } catch (EntryDuplicatedException e) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR, "Common.message.duplicated");
            return com.encens.khipus.framework.action.Outcome.REDISPLAY;
        }
        return com.encens.khipus.framework.action.Outcome.SUCCESS;
    }

    public boolean transferenceMovementType() {
        return fixedAssetMovementType != null && (fixedAssetMovementType.getFixedAssetMovementTypeEnum().equals(FixedAssetMovementTypeEnum.TRA));
    }

    public boolean improvementMovementType() {
        return fixedAssetMovementType != null && (positiveImprovementMovementType());
    }

    public boolean positiveImprovementMovementType() {
        return fixedAssetMovementType != null && (fixedAssetMovementType.getFixedAssetMovementTypeEnum().equals(FixedAssetMovementTypeEnum.MEJ));
    }

    public String positiveImprovement() {
        validateBusinessUnit(getInstance().getBusinessUnit());

        if (getInstance().getFixedAssetSubGroup().getImprovementAccount() == null) {
            addImprovementAccountNotSetMessage();
            return Outcome.REDISPLAY;
        }
        if (!hasComputeImprovement()) {
            addComputeRequiredMessage();
            return Outcome.REDISPLAY;
        }
        if (!checkComputationForImprovement()) {
            addReComputeRequiredMessage();
            return Outcome.REDISPLAY;
        }
        if (!checkPaymentForImprovement()) {
            addReComputePaymentRequiredMessage();
            return Outcome.REDISPLAY;
        }
        if (fixedAssetMovement.getMovementDate() == null) {
            fixedAssetMovement.setMovementDate(new Date());
            fixedAssetMovement.setCreationDate(new Date());
        }
        boolean isDateAfterFixedAssetProcessMonthDate =
                fixedAssetMonthProcessService.isDateAfterFixedAssetMothProcessInitDate(fixedAssetMovement.getMovementDate());
        if (isDateAfterFixedAssetProcessMonthDate) {
            addMovementDateBeforeModuleMonthMessage();
            return Outcome.REDISPLAY;
        }
        if (FileUtil.isEmpty(getInstance().getPhoto())) {
            //noinspection NullableProblems
            getInstance().setPhoto(null);
        }
        try {
            fixedAssetService.positiveImprovementFixedAsset(getInstance(), gloss, fixedAssetMovement, fixedAssetMovementType, getFixedAssetPaymentInstance());
            facesMessages.addFromResourceBundle(StatusMessage.Severity.INFO, "FixedAssetmovement.info.positiveImproveSucceed");
            return com.encens.khipus.framework.action.Outcome.SUCCESS;
        } catch (ConcurrencyException e) {
            log.debug(e);
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR, "Common.error.concurrency");
            return com.encens.khipus.framework.action.Outcome.REDISPLAY;
        } catch (EntryDuplicatedException e) {
            log.debug(e);
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR, "Common.message.duplicated");
            return com.encens.khipus.framework.action.Outcome.REDISPLAY;
        }
    }

    public String bsToUfvSus() {
        getInstance().setSusOriginalValue(
                BigDecimalUtil.divide(getInstance().getBsOriginalValue(), getInstance().getBsSusRate()));
        getInstance().setUfvOriginalValue(
                BigDecimalUtil.divide(getInstance().getBsOriginalValue(), getInstance().getBsUfvRate()));
        return com.encens.khipus.framework.action.Outcome.SUCCESS;
    }

    public void compute() {
        if (areFieldsValidForCompute()) {
            showCalculatedValues = true;
            bsToUfvSus();
        } else {
            showCalculatedValues = false;
        }
    }

    public boolean areFieldsValidForCompute() {
        return ((getInstance().getBsOriginalValue() != null) &&
                getInstance().getBsSusRate() != null &&
                getInstance().getBsUfvRate() != null);
    }

    public boolean checkComputation() {
        return (areFieldsValidForCompute() &&
                getInstance().getSusOriginalValue() != null &&
                getInstance().getUfvOriginalValue() != null &&
                getInstance().getSusOriginalValue().compareTo(
                        BigDecimalUtil.divide(getInstance().getBsOriginalValue(), getInstance().getBsSusRate())) == 0
                && getInstance().getUfvOriginalValue().compareTo(
                BigDecimalUtil.divide(getInstance().getBsOriginalValue(), getInstance().getBsUfvRate())) == 0);
    }

    public void computeImprovement() {
        try {
            fixedAssetMovement.setBsUfvRate(financesExchangeRateService.findLastExchangeRateByCurrency(FinancesCurrencyType.U.name()));
            fixedAssetMovement.setBsSusRate(financesExchangeRateService.findLastExchangeRateByCurrency(FinancesCurrencyType.D.name()));
            fixedAssetMovement.setLastMonthBsUfvRate(fixedAssetMovement.getBsUfvRate());
            fixedAssetMovement.setLastMonthBsSusRate(fixedAssetMovement.getBsSusRate());
            fixedAssetMovement.setUfvAmount(
                    BigDecimalUtil.divide(fixedAssetMovement.getBsAmount(), fixedAssetMovement.getBsUfvRate()));
        } catch (FinancesCurrencyNotFoundException e) {
            addFinancesCurrencyNotFoundExceptionMessage();
        } catch (FinancesExchangeRateNotFoundException e) {
            addFinancesExchangeRateNotFoundExceptionMessage();
        }
    }

    public boolean checkComputationForImprovement() {
        return (fixedAssetMovement.getUfvAmount() != null &&
                fixedAssetMovement.getUfvAmount().compareTo(
                        BigDecimalUtil.divide(fixedAssetMovement.getBsAmount(), fixedAssetMovement.getBsUfvRate())) == 0);
    }

    /* This method computes the payment for a fixed asset at create time in case it is not generated by a purchase order
    * Advertise: even thought it appears not to be used it is invoked since the corresponding view for the registration
    * of a fixed asset */

    @SuppressWarnings({"UnusedDeclaration"})
    public void computePayment() {
        /* in case of improvement*/
        if (isManaged() && getInstance().getState().equals(FixedAssetState.VIG)) {
            computePaymentForImprovement();
        } else {
            /* in case of registration*/
            computePaymentForRegistration();
        }
    }

    private void computePaymentForRegistration() {
        if (!hasCompute()) {
            addComputeRequiredMessage();
        } else {
            if (!checkComputation()) {
                addReComputeRequiredMessage();
            } else {
                BigDecimal bankAmount = getInstance().getBsOriginalValue();
                if (fixedAssetPaymentAction.getInstance().getSourceCurrency().equals(FinancesCurrencyType.D)) {
                    bankAmount = BigDecimalUtil.divide(bankAmount, fixedAssetPaymentAction.getInstance().getExchangeRate());
                }
                fixedAssetPaymentAction.getInstance().setSourceAmount(bankAmount);
            }
        }
    }

    private void computePaymentForImprovement() {
        if (!hasComputeImprovement()) {
            addComputeRequiredMessage();
        } else {
            if (!checkComputationForImprovement()) {
                addReComputeRequiredMessage();
            } else {
                BigDecimal bankAmount = fixedAssetMovement.getBsAmount();
                if (fixedAssetPaymentAction.getInstance().getSourceCurrency().equals(FinancesCurrencyType.D)) {
                    bankAmount = BigDecimalUtil.divide(bankAmount, fixedAssetPaymentAction.getInstance().getExchangeRate());
                }
                fixedAssetPaymentAction.getInstance().setSourceAmount(bankAmount);
            }
        }
    }

    public boolean checkPayment() {
        if (fixedAssetPaymentAction.getInstance() != null &&
                fixedAssetPaymentAction.getInstance().getSourceAmount() != null &&
                (fixedAssetPaymentAction.getInstance().getBankAccount() != null || fixedAssetPaymentAction.getInstance().getCashBoxCashAccount() != null)
                ) {
            BigDecimal bankAmount = getInstance().getBsOriginalValue();
            if (fixedAssetPaymentAction.getInstance().getSourceCurrency().equals(FinancesCurrencyType.D)) {
                bankAmount = BigDecimalUtil.divide(bankAmount, fixedAssetPaymentAction.getInstance().getExchangeRate());
            }
            return fixedAssetPaymentAction.getInstance().getSourceAmount().compareTo(bankAmount) == 0;
        } else {
            return false;
        }
    }

    public boolean checkPaymentForImprovement() {
        if (fixedAssetPaymentAction.getInstance() != null &&
                fixedAssetPaymentAction.getInstance().getSourceAmount() != null &&
                (fixedAssetPaymentAction.getInstance().getBankAccount() != null || fixedAssetPaymentAction.getInstance().getCashBoxCashAccount() != null)
                ) {
            BigDecimal bankAmount = fixedAssetMovement.getBsAmount();
            if (fixedAssetPaymentAction.getInstance().getSourceCurrency().equals(FinancesCurrencyType.D)) {
                bankAmount = BigDecimalUtil.divide(bankAmount, fixedAssetPaymentAction.getInstance().getExchangeRate());
            }
            return fixedAssetPaymentAction.getInstance().getSourceAmount().compareTo(bankAmount) == 0;
        } else {
            return false;
        }
    }

    public String changeGuaranty() {
        if (monthsGuaranty == null) {
            addMonthsGuarantyRequiredMessage();
            return Outcome.REDISPLAY;
        }
        /* If the user hasn't applied any filter a message should be shown*/
        if (fixedAssetDataModel == null ||
                (fixedAssetDataModel.getCostCenter() == null &&
                        fixedAssetDataModel.getFixedAssetGroup() == null &&
                        fixedAssetDataModel.getFixedAssetSubGroup() == null &&
                        (fixedAssetDataModel.getCriteria() == null || fixedAssetDataModel.getCriteria().getBusinessUnit() == null)
                )) {
            addFiltersRequiredMessage();
            return Outcome.REDISPLAY;
        }
        Integer rows = fixedAssetService.changeGuaranty(fixedAssetDataModel, monthsGuaranty);
        addRowsUpdatedMessage(rows);
        return Outcome.SUCCESS;
    }

    public FixedAsset getDatabaseFixedAsset() {
        return fixedAssetService.getDataBaseFixedAsset(getInstance());
    }

    public boolean showFixedAssetCodeField() {
        return !isManaged() || (fixedAssetService.getDataBaseFixedAsset(getInstance()).getFixedAssetCode() == null);
    }

    public boolean showCostCenterField() {
        return !isManaged() || (fixedAssetService.getDataBaseFixedAsset(getInstance()).getCostCenter() == null);
    }

    public boolean showCustodianJobContractField() {
        return !isManaged() || (fixedAssetService.getDataBaseFixedAsset(getInstance()).getCustodianJobContract() == null);
    }

    public boolean isShowCalculatedValues() {
        return showCalculatedValues;
    }

    public void setShowCalculatedValues(boolean showCalculatedValues) {
        this.showCalculatedValues = showCalculatedValues;
    }
    // !True (False) ||  (  True || True || True)
    public boolean isShowRegistrationDetail() {
        showRegistrationDetail = !isManaged() || (showCostCenterField() || showCustodianJobContractField() || showFixedAssetCodeField());
        return showRegistrationDetail;
    }

    public boolean isShowPaymentDetail() {
        return isShowRegistrationDetail() && null == getInstance().getPurchaseOrder() && getPayNowConditions();
    }

    public void setShowRegistrationDetail(boolean showRegistrationDetail) {
        this.showRegistrationDetail = showRegistrationDetail;
    }

    public boolean isShowTransferenceButton() {
        showTransferenceButton = actualOrTotallyDepreciated() && transferenceMovementType();
        return showTransferenceButton;
    }

    public void setShowTransferenceButton(boolean showTransferenceButton) {
        this.showTransferenceButton = showTransferenceButton;
    }

    public boolean isShowPositiveImprovementButton() {
        showPositiveImprovementButton = actualOrTotallyDepreciated() && positiveImprovementMovementType();
        return showPositiveImprovementButton;
    }

    public void setShowPositiveImprovementButton(boolean showPositiveImprovementButton) {
        this.showPositiveImprovementButton = showPositiveImprovementButton;
    }

    public boolean isShowDischargeButton() {
        showDischargeButton = actualOrTotallyDepreciated() && dischargeMovementType();
        return showDischargeButton;
    }

    public void setShowDischargeButton(boolean showDischargeButton) {
        this.showDischargeButton = showDischargeButton;
    }

    private void addReComputeRequiredMessage() {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR, "FixedAsset.error.reComputeRequired");
    }

    private void addReComputePaymentRequiredMessage() {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR, "FixedAsset.error.reComputePaymentRequired");
    }

    private void addComputeRequiredMessage() {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR, "FixedAsset.error.computeRequired");
    }

    private void addMonthsGuarantyRequiredMessage() {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR, "FixedAsset.error.monthsguarantyRequired");
    }

    private void addFiltersRequiredMessage() {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR, "FixedAsset.error.filtersRequired");
    }

    private void addRowsUpdatedMessage(Integer rows) {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.INFO, "FixedAsset.info.fixedAssetsUpdated", monthsGuaranty, rows);
    }

    private void addImprovementAccountNotSetMessage() {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR, "FixedAsset.error.improvementAccountNotSetMessage");
    }

    public String updateRates() {
        if (getInstance().getRegistrationDate() != null) {
            try {
                BigDecimal susRate = financesExchangeRateService.findExchangeRateByDateByCurrency(getInstance().getRegistrationDate(), FinancesCurrencyType.D.name());
                BigDecimal ufvRate = financesExchangeRateService.findExchangeRateByDateByCurrency(getInstance().getRegistrationDate(), FinancesCurrencyType.U.name());
                getInstance().setBsSusRate(susRate);
                getInstance().setBsUfvRate(ufvRate);
                return Outcome.REDISPLAY;
            } catch (FinancesCurrencyNotFoundException e) {
                addFinancesCurrencyNotFoundExceptionMessage();
                return Outcome.REDISPLAY;
            } catch (FinancesExchangeRateNotFoundException e) {
                addFinancesExchangeRateNotFoundExceptionMessage();
                return Outcome.REDISPLAY;
            }
        }
        return Outcome.REDISPLAY;
    }
    /* Messages*/

    private void addDepreciationDateBeforeModuleMonthMessage() {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                "FixedAsset.error.depreciationDateBeforeModuleMonth");
    }

    private void addRegistrationDateBeforeModuleMonthMessage() {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                "FixedAsset.error.registrationDateBeforeModuleMonth");
    }

    private void addMovementDateBeforeModuleMonthMessage() {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                "FixedAsset.error.movementDateBeforeModuleMonth");
    }

    private void addFinancesExchangeRateNotFoundExceptionMessage() {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.INFO,
                "FixedAssets.FinancesExchangeRateNotFoundException");
    }

    private void addFinancesExchangeRateForDepreciationNotFoundExceptionMessage() {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.INFO,
                "FixedAssets.FinancesExchangeRateForDepreciationNotFoundException");
    }

    private void addFinancesCurrencyNotFoundExceptionMessage() {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.INFO,
                "FixedAssets.FinancesCurrencyNotFoundException");
    }

    private void addRegistrationSuccessfulMessage() {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.INFO,
                "FixedAssets.info.RegistrationSuccessful");
    }

    private void addDateBeforeModuleMonthExceptionMessage() {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR, "FixedAsset.error.dateBeforeModuleMonth");
    }

    private void addThereAreActualFixedAssetExceptionMessage() {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR, "FixedAsset.error.CloseFixedAssetMonth.thereAreActualFixedAssets");
    }

    private void addThereAreNotAdjustedFixedAssetExceptionMessage() {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR, "FixedAsset.error.CloseFixedAssetMonth.thereAreNotAdjustedFixedAssets");
    }

    private void addThereIsNoActualFixedAssetsExceptionMessage() {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR, "FixedAsset.error.Depreciate.ThereIsNoActualFixedAssets");
    }

    private void addCloseMonthSucceedMessage() {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.INFO, "FixedAsset.info.closeMonthSucceed");
    }

    private void addDuplicatedFixedAssetCodeMessage() {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR, "FixedAsset.error.duplicatedFixedAssetCode");
    }

    private void addNoChangeForTransferenceMessage() {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR, "FixedAsset.error.NoChangeForTransference");
    }

    private void addSuccessfulTransferenceMessage() {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.INFO, "FixedAsset.info.SuccessfulTransference");
    }

    private void addRubbishGreaterThanUfvOriginalValueMessage() {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR, "FixedAsset.error.RubbishGreaterThanUfvOriginalValue");
    }

    public boolean validate() {
        Boolean valid = true;
        if (getInstance().getCostCenter() == null) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR, "FixedAsset.error.costCenterRequired");
            valid = false;
        }
        if (getInstance().getCustodianJobContract() == null) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR, "FixedAsset.error.custodianRequired");
            valid = false;
        }
        return valid;
    }

    private void validateBusinessUnit(BusinessUnit businessUnit) {
        BusinessUnitValidatorUtil.i.validateBusinessUnit(businessUnit);
    }

    public BarcodeRenderer getBarcodeRenderer() {
        return barcodeRenderer;
    }

    public void setBarcodeRenderer(BarcodeRenderer barcodeRenderer) {
        this.barcodeRenderer = barcodeRenderer;
    }

    public BarcodeData getBarcodeData() {
        return barcodeData;
    }

    public void setBarcodeData(BarcodeData barcodeData) {
        this.barcodeData = barcodeData;
    }

    public Boolean getPayNowConditions() {
        if(this.payNowConditions == null)
            this.payNowConditions = true;

        return payNowConditions;
    }

    public void setPayNowConditions(Boolean payNowConditions) {
        this.payNowConditions = payNowConditions;
    }

    public Date getMonthProcess(){
        FinancesModule financesModule = fixedAssetMonthProcessService.getFinancesModule(FixedAssetDefaultConstants.getFixedAssetModulePK());
        return financesModule.getDate();
    }


}