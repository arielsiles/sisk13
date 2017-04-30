package com.encens.khipus.action.employees;

import com.encens.khipus.exception.EntryNotFoundException;
import com.encens.khipus.exception.finances.CompanyConfigurationNotFoundException;
import com.encens.khipus.exception.finances.FinancesCurrencyNotFoundException;
import com.encens.khipus.exception.finances.FinancesExchangeRateNotFoundException;
import com.encens.khipus.framework.action.GenericAction;
import com.encens.khipus.framework.action.Outcome;
import com.encens.khipus.model.admin.User;
import com.encens.khipus.model.common.Text;
import com.encens.khipus.model.employees.*;
import com.encens.khipus.model.finances.*;
import com.encens.khipus.service.employees.EmployeeService;
import com.encens.khipus.service.employees.GeneratedPayrollService;
import com.encens.khipus.service.employees.GestionPayrollService;
import com.encens.khipus.service.finances.AccountingRecordService;
import com.encens.khipus.service.finances.FinancesExchangeRateService;
import com.encens.khipus.service.finances.QuotaService;
import com.encens.khipus.service.finances.RotatoryFundCollectionService;
import com.encens.khipus.util.*;
import com.encens.khipus.util.employees.*;
import org.jboss.ejb3.annotation.TransactionTimeout;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.*;
import org.jboss.seam.annotations.security.Restrict;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.framework.EntityQuery;
import org.jboss.seam.international.StatusMessage;
import org.jboss.seam.log.Log;

import java.math.BigDecimal;
import java.util.*;

/**
 * AccountingRecordAction
 *
 * @author
 * @version 1.4
 */
@Name("accountingRecordAction")
@Scope(ScopeType.CONVERSATION)
@Restrict("#{s:hasPermission('ACCOUNTINGRECORD','VIEW')}")
public class AccountingRecordAction extends GenericAction<AccountingRecord> {

    @Logger
    private Log log;
    @In
    private FacesMessages facesMessages;
    @In
    private GestionPayrollService gestionPayrollService;
    @In
    private GeneratedPayrollService generatedPayrollService;
    @In
    private AccountingRecordService accountingRecordService;
    @In
    private EmployeeService employeeService;
    @In
    private FinancesExchangeRateService financesExchangeRateService;
    @In
    private RotatoryFundCollectionService rotatoryFundCollectionService;
    @In
    private QuotaService quotaService;
    @In(required = false)
    private User currentUser;
    @In(required = false)
    @Out(required = false)
    private ProfessorsPayrollGenerationDataModel professorsPayrollGenerationDataModel;
    @In(required = false)
    @Out(required = false)
    private ManagersPayrollGenerationDataModel managersPayrollGenerationDataModel;
    @In(required = false)
    @Out(required = false)
    private ChristmasPayrollGenerationDataModel christmasPayrollGenerationDataModel;
    @In(required = false)
    @Out(required = false)
    private FiscalProfessorPayrollGenerationDataModel fiscalProfessorPayrollGenerationDataModel;
    @In(create = true)
    private EntityQuery financesBankAccountQuery;
    private AccountingRecordMap<String, AccountingRecordData> nationalAmountForCheckDataMap = new AccountingRecordMap<String, AccountingRecordData>("nationalAmountForCheckDataMap");
    private AccountingRecordMap<String, AccountingRecordData> foreignAmountForCheckDataMap = new AccountingRecordMap<String, AccountingRecordData>("foreignAmountForCheckDataMap");
    private AccountingRecordMap<String, AccountingRecordData> nationalAmountForBankDataMap = new AccountingRecordMap<String, AccountingRecordData>("nationalAmountForBankDataMap");
    private AccountingRecordMap<String, AccountingRecordData> foreignAmountForBankDataMap = new AccountingRecordMap<String, AccountingRecordData>("foreignAmountForBankDataMap");
    private ObservableMap<String, BigDecimal> exchangeRateAmountMap = new ObservableMap<String, BigDecimal>(AccountingRecordData.Property.EXCHANGE_RATE, nationalAmountForCheckDataMap, foreignAmountForCheckDataMap, nationalAmountForBankDataMap, foreignAmountForBankDataMap);
    private Map<Long, FinancesBankAccount> selectedBankAccountMap = new HashMap<Long, FinancesBankAccount>();
    private FinancesBankAccount mainBankAccount;
    private AccountingRecordFixData accountingRecordFixData = new AccountingRecordFixData();

    public AccountingRecordAction() {
    }

    @Create
    public void initAccountingRecordAction() {
        try {
            BigDecimal exchangeRate = financesExchangeRateService.findLastExchangeRateByCurrency(FinancesCurrencyType.D.name());
            List<FinancesBankAccount> financesBankAccountList = financesBankAccountQuery.getResultList();
            for (FinancesBankAccount financesBankAccount : financesBankAccountList) {
                exchangeRateAmountMap.put(financesBankAccount.getAccountNumber(), exchangeRate);
            }
        } catch (FinancesCurrencyNotFoundException e) {
        } catch (FinancesExchangeRateNotFoundException e) {
        }

        if (getInstance().getDescription() == null) {
            getInstance().setDescription(new Text());
        }
    }

    @Factory(value = "accountingRecord", scope = ScopeType.STATELESS)
    public AccountingRecord initAccountingRecord() {
        return getInstance();
    }

    @Factory(value = "financesCurrencyTypeEnum", scope = ScopeType.STATELESS)
    public FinancesCurrencyType[] getFinancesCurrencyTypeEnum() {
        return FinancesCurrencyType.values();
    }

    @Override
    @Restrict("#{s:hasPermission('ACCOUNTINGRECORD','CREATE')}")
    @End
    @TransactionTimeout(6000)
    public String create() {
        Boolean validAmounts = Boolean.TRUE;

        log.debug("*********************************************************************************");
        log.debug("nationalAmountForCheckMap=\t" + nationalAmountForCheckDataMap);
        log.debug("foreignAmountForCheckMap=\t" + foreignAmountForCheckDataMap);
        log.debug("nationalAmountForBankMap=\t" + nationalAmountForBankDataMap);
        log.debug("foreignAmountForBankMap=\t" + foreignAmountForBankDataMap);
        log.debug("exchangeRateAmountMap=\t" + exchangeRateAmountMap);
        log.debug("*********************************************************************************");

        List<Long> payrollGenerationIdList = getPayrollGenerationIdList();

        if (ValidatorUtil.isEmptyOrNull(payrollGenerationIdList)) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR, "AccountingRecord.error.emptyPayroll");
            clearInputAmounts();
            clearTotalAmounts();
            return Outcome.REDISPLAY;
        }

        FinancesCurrencyType payrollPaymentCurrency = GeneralPayroll.class.equals((Class<? extends GenericPayroll>) getCurrentEntityClass()) ? FinancesCurrencyType.D : FinancesCurrencyType.P;

        if (!getInstance().getProvider().getPayableAccount().getCurrency().equals(payrollPaymentCurrency)) {
            addProviderAccountCurrencyMismatchErrorMessage();
            return Outcome.REDISPLAY;
        }

        if (BigDecimalUtil.isZeroOrNull(getInstance().getNationalAmountForBank()) &&
                BigDecimalUtil.isZeroOrNull(getInstance().getForeignAmountForBank()) &&
                BigDecimalUtil.isZeroOrNull(getInstance().getNationalAmountForCheck()) &&
                BigDecimalUtil.isZeroOrNull(getInstance().getForeignAmountForCheck())) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR, "AccountingRecord.error.amounts");
            validAmounts = Boolean.FALSE;
        }

        if (!generatedPayrollService.validateHasAccountingRecordOrHasInactivePayment(getCurrentEntityClass(), getInstance().getGeneratedPayroll(), payrollGenerationIdList)) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR, "GeneratedPayroll.error.hasAccountingRecordOrHasInactivePayment");
            clearInputAmounts();
            clearTotalAmounts();
            validAmounts = Boolean.FALSE;
        }

        if (!validAmounts) {
            return Outcome.REDISPLAY;
        }
        /* the quota info should not vary never ever */
        if (getGestionPayrollSalaryType() && !areQuotasValid()) {
            addQuotasValidErrorMessage();
            return Outcome.REDISPLAY;
        }

        String payrollTypePluralString = MessageUtils.getMessage(getInstance().getGestionPayrollType().getPluralResourceKey());
        String payrollTypeSingularString = MessageUtils.getMessage(getInstance().getGestionPayrollType().getSingularResourceKey());
        String jobCategoryName = FormatUtils.toAcronym(getInstance().getJobCategory().getName(), getInstance().getJobCategory().getAcronym());
        String businessUnitName = getInstance().getBusinessUnit().getOrganization().getName();
        String monthString = MessageUtils.getMessage(getInstance().getMonth().getResourceKey());
        String yearString = String.valueOf(getInstance().getGestion().getYear()).replace(".", "");
        String dateString = DateUtils.format(new Date(), MessageUtils.getMessage("patterns.dateTime"));

        String voucherGlossForPayrollGeneration = MessageUtils.getMessage("AccountingRecord.voucherGlossForPayrollGeneration", payrollTypePluralString, jobCategoryName, businessUnitName, monthString, yearString, dateString);
        String voucherGlossForSalaryMovement = MessageUtils.getMessage("AccountingRecord.voucherGlossForSalaryMovement", payrollTypePluralString, jobCategoryName, businessUnitName, monthString, yearString, dateString);
        String voucherGlossForChristmasProvision = MessageUtils.getMessage("AccountingRecord.voucherGlossForChristmasProvision", payrollTypePluralString, jobCategoryName, businessUnitName, monthString, yearString, dateString);
        String voucherGlossForCompensationPrevision = MessageUtils.getMessage("AccountingRecord.voucherGlossForCompensationPrevision", payrollTypePluralString, jobCategoryName, businessUnitName, monthString, yearString, dateString);
        String voucherGlossForPayment = MessageUtils.getMessage("AccountingRecord.voucherGlossForPayment", payrollTypeSingularString, jobCategoryName, businessUnitName, monthString, yearString, dateString);

        Class<? extends GenericPayroll> accountingRecordClass = getCurrentEntityClass();

        try {
            getInstance().setRecorderUser(currentUser);
            Map<Long, FinancesCurrencyType> currencyMapPaymentByEmployee = new HashMap<Long, FinancesCurrencyType>();

            getInstance().getDescription().setValue(voucherGlossForPayrollGeneration);

            AccountingRecordResult accountingRecordResult = accountingRecordService.create(
                    accountingRecordClass,
                    getInstance(),
                    payrollGenerationIdList,
                    getNationalAmountForCheckDataMap(),
                    getForeignAmountForCheckDataMap(),
                    getNationalAmountForBankDataMap(),
                    getForeignAmountForBankDataMap(),
                    getExchangeRateAmountMap(),
                    getSelectedBankAccountMap(),
                    currencyMapPaymentByEmployee,
                    voucherGlossForPayrollGeneration,
                    voucherGlossForSalaryMovement,
                    voucherGlossForChristmasProvision,
                    voucherGlossForCompensationPrevision,
                    voucherGlossForPayment);
            if (AccountingRecordResult.SUCCESS.equals(accountingRecordResult)) {
                clear();
                facesMessages.addFromResourceBundle(StatusMessage.Severity.INFO, "AccountingRecord.success");
            } else if (AccountingRecordResult.FAIL.equals(accountingRecordResult)) {
                facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR, "AccountingRecord.fail");
                return Outcome.REDISPLAY;
            } else if (AccountingRecordResult.WITHOUT_OFFICIAL_GENERATION.equals(accountingRecordResult)) {
                facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR, "AccountingRecord.error.withoutOfficialGeneration"
                        , monthString
                        , yearString
                        , accountingRecordResult.getResultData()[0]);
                return Outcome.REDISPLAY;
            } else if (AccountingRecordResult.WITHOUT_COSTCENTER.equals(accountingRecordResult)) {
                facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR, "AccountingRecord.error.withoutCostCenter"
                        , accountingRecordResult.getResultData()[0]
                        , monthString
                        , yearString
                        , accountingRecordResult.getResultData()[1]
                        , accountingRecordResult.getResultData()[2]
                );
                return Outcome.REDISPLAY;
            }
        } catch (Exception e) {
            log.debug("-------------------------------------------------------");
            log.error(e, "An unexpected exception have happened");
            log.debug("-------------------------------------------------------");
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR, "AccountingRecord.fail");
            return Outcome.REDISPLAY;
        }

        return Outcome.SUCCESS;
    }

    private boolean areQuotasValid() {
        /* Obtain the payments by GestionPayroll paid by payroll*/
        List<RotatoryFundCollection> databaseRotatoryFundCollectionList =
                rotatoryFundCollectionService.findRotatoryFundCollectionByGestionPayroll(getInstance().getGeneratedPayroll().getGestionPayroll());
        /*check if quota info are still valid*/
        boolean quotasStillValid = true;
        for (int i = 0; (i < databaseRotatoryFundCollectionList.size() && quotasStillValid); i++) {
            RotatoryFundCollection rotatoryFundCollection = databaseRotatoryFundCollectionList.get(i);
            quotasStillValid = quotaService.isQuotaInfoStillValid(rotatoryFundCollection);
        }
        return quotasStillValid;
    }

    private void clear() {
        setInstance(new AccountingRecord());
        clearInputAmounts();
        clearTotalAmounts();
    }

    public void clearInputAmounts() {
        nationalAmountForCheckDataMap.clear();
        foreignAmountForCheckDataMap.clear();
        nationalAmountForBankDataMap.clear();
        foreignAmountForBankDataMap.clear();
        exchangeRateAmountMap.notifyObservers();
    }

    public void clearTotalAmounts() {
        getInstance().setNationalAmountForBank(null);
        getInstance().setForeignAmountForBank(null);
        getInstance().setNationalAmountForCheck(null);
        getInstance().setForeignAmountForCheck(null);
    }

    public void selectAllBankAccounts() {
        for (Map.Entry<Long, FinancesBankAccount> selectEntry : getSelectedBankAccountMap().entrySet()) {
            selectEntry.setValue(getMainBankAccount());
        }
    }

    public List<Long> getPayrollGenerationIdList() {
        return new ArrayList<Long>(selectedBankAccountMap.keySet());
    }

    public void searchPayrollGenerationList() {
        getInstance().setGeneratedPayroll(gestionPayrollService.findOfficialGeneratedPayroll(getInstance().getBusinessUnit(), getInstance().getJobCategory(), getInstance().getGestion(), getInstance().getGestionPayrollType(), getInstance().getMonth()));

        selectedBankAccountMap.clear();
        clearInputAmounts();
        clearTotalAmounts();

        if (getInstance().getGeneratedPayroll() != null) {
            Class<? extends GenericPayroll> entityClass = getCurrentEntityClass();
            if (entityClass != null) {
                try {
                    selectedBankAccountMap = generatedPayrollService.getFinancesBankAccountMapByPayroll(entityClass, getInstance().getGeneratedPayroll());
                } catch (CompanyConfigurationNotFoundException e) {
                    log.debug(e);
                }
            }
        }

        calculateSelectedPayrollGenerationList();
    }

    public void initFixAmount(String companyNumber, String accountNumber, AccountingRecordData accountingRecordData) {
        getAccountingRecordFixData().restart();
        getAccountingRecordFixData().setAccountingRecordData(accountingRecordData);
        getAccountingRecordFixData().setSourceBankAccountNumber(accountNumber);
        if (!ValidatorUtil.isBlankOrNull(companyNumber) && !ValidatorUtil.isBlankOrNull(accountNumber)) {
            try {
                getAccountingRecordFixData().setSourceBankAccount(getService().findById(FinancesBankAccount.class, new FinancesBankAccountPk(companyNumber, accountNumber)));
            } catch (EntryNotFoundException e) {
            }
        }
    }

    public void performFixAmount() {
        performCalculateSelectedPayroll(true);
    }

    public void calculateSelectedPayrollGenerationList() {
        performCalculateSelectedPayroll(false);
    }

    public void performCalculateSelectedPayroll(Boolean isFixAmountOperation) {
        BigDecimal nationalAmountForBank = BigDecimal.ZERO;
        BigDecimal foreignAmountForBank = BigDecimal.ZERO;
        BigDecimal nationalAmountForCheck = BigDecimal.ZERO;
        BigDecimal foreignAmountForCheck = BigDecimal.ZERO;
        clearInputAmounts();

        AccountingRecordDataProvider dataProvider = null;

        if (isFixAmountOperation) {
            dataProvider = getAccountingRecordFixData().getAccountingRecordData().getDataProvider();
        }

        BigDecimal currentTotalAmount = BigDecimal.ZERO;

        if (getProfessorGenerationBySalary()) {
            if (fiscalProfessorPayrollGenerationDataModel != null) {
                fiscalProfessorPayrollGenerationDataModel = new FiscalProfessorPayrollGenerationDataModel();
            }

            Boolean validForm = true;
            List<FiscalProfessorPayroll> fiscalProfessorPayrollList = fiscalProfessorPayrollGenerationDataModel.getResultList();

            if (fiscalProfessorPayrollList.isEmpty()) {
                facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR, "AccountingRecord.error.unselectedPayrollItems");
                validForm = false;
            } else {
                for (int i = 0; i < fiscalProfessorPayrollList.size() && validForm; i++) {
                    if (selectedBankAccountMap.get(fiscalProfessorPayrollList.get(i).getId()) == null) {
                        facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR, "AccountingRecord.error.unselectedBankAccount");
                        validForm = false;
                    }
                }
            }
            if (validForm) {
                for (FiscalProfessorPayroll fiscalProfessorPayroll : fiscalProfessorPayrollList) {
                    PaymentType paymentType = employeeService.getEmployeesPaymentType(fiscalProfessorPayroll.getEmployee());
                    if (isFixAmountOperation) {
                        FinancesBankAccount currentBankAccount = selectedBankAccountMap.get(fiscalProfessorPayroll.getId());
                        if (getAccountingRecordFixData().getSourceBankAccount().equals(currentBankAccount) &&
                                ((PaymentType.PAYMENT_BANK_ACCOUNT.equals(paymentType) && dataProvider.equals(nationalAmountForBankDataMap)) ||
                                        (PaymentType.PAYMENT_WITH_CHECK.equals(paymentType) && dataProvider.equals(nationalAmountForCheckDataMap)))) {
                            BigDecimal sourceLiquidInputAmount = fiscalProfessorPayroll.getLiquid();

                            if (FinancesCurrencyType.D.equals(getAccountingRecordFixData().getSourceBankAccount().getCurrency())) {
                                BigDecimal sourceExchangeRate = BigDecimalUtil.toBigDecimal(exchangeRateAmountMap.get(getAccountingRecordFixData().getSourceBankAccount().getAccountNumber()));
                                sourceLiquidInputAmount = BigDecimalUtil.divide(sourceLiquidInputAmount, sourceExchangeRate);
                            }

                            currentTotalAmount = currentTotalAmount.add(sourceLiquidInputAmount);
                            if (currentTotalAmount.compareTo(getAccountingRecordFixData().getMaximumAmount()) > 0) {
                                selectedBankAccountMap.put(fiscalProfessorPayroll.getId(), getAccountingRecordFixData().getTargetBankAccount());
                            }
                        }
                    }
                    BigDecimal liquidInputAmount = fiscalProfessorPayroll.getLiquid();
                    FinancesBankAccount bankAccount = selectedBankAccountMap.get(fiscalProfessorPayroll.getId());
                    BigDecimal exchangeRate = BigDecimalUtil.toBigDecimal(exchangeRateAmountMap.get(bankAccount.getAccountNumber()));

                    if (FinancesCurrencyType.D.equals(bankAccount.getCurrency()) && !BigDecimalUtil.isZeroOrNull(liquidInputAmount)) {
                        liquidInputAmount = BigDecimalUtil.divide(liquidInputAmount, exchangeRate, 6);
                    }

                    if (PaymentType.PAYMENT_BANK_ACCOUNT.equals(paymentType)) {
                        nationalAmountForBank = nationalAmountForBank.add(fiscalProfessorPayroll.getLiquid());
                        addAmount(nationalAmountForBankDataMap, bankAccount, liquidInputAmount);
                    } else if (PaymentType.PAYMENT_WITH_CHECK.equals(paymentType)) {
                        nationalAmountForCheck = nationalAmountForCheck.add(fiscalProfessorPayroll.getLiquid());
                        addAmount(nationalAmountForCheckDataMap, bankAccount, liquidInputAmount);
                    }
                }
            }
        } else if (getGenerationBySalaryType()) {
            if (managersPayrollGenerationDataModel != null) {
                managersPayrollGenerationDataModel = new ManagersPayrollGenerationDataModel();
            }

            Boolean validForm = true;
            List<ManagersPayroll> managersPayrollList = managersPayrollGenerationDataModel.getResultList();

            if (managersPayrollList.isEmpty()) {
                facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR, "AccountingRecord.error.unselectedPayrollItems");
                validForm = false;
            } else {
                for (int i = 0; i < managersPayrollList.size() && validForm; i++) {
                    if (selectedBankAccountMap.get(managersPayrollList.get(i).getId()) == null) {
                        facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR, "AccountingRecord.error.unselectedBankAccount");
                        validForm = false;
                    }
                }
            }
            if (validForm) {
                for (ManagersPayroll managersPayroll : managersPayrollList) {
                    PaymentType paymentType = employeeService.getEmployeesPaymentType(managersPayroll.getEmployee());
                    if (isFixAmountOperation) {
                        FinancesBankAccount currentBankAccount = selectedBankAccountMap.get(managersPayroll.getId());
                        if (getAccountingRecordFixData().getSourceBankAccount().equals(currentBankAccount) &&
                                ((PaymentType.PAYMENT_BANK_ACCOUNT.equals(paymentType) && dataProvider.equals(nationalAmountForBankDataMap)) ||
                                        (PaymentType.PAYMENT_WITH_CHECK.equals(paymentType) && dataProvider.equals(nationalAmountForCheckDataMap)))) {
                            BigDecimal sourceLiquidInputAmount = managersPayroll.getLiquid();

                            if (FinancesCurrencyType.D.equals(getAccountingRecordFixData().getSourceBankAccount().getCurrency())) {
                                BigDecimal sourceExchangeRate = BigDecimalUtil.toBigDecimal(exchangeRateAmountMap.get(getAccountingRecordFixData().getSourceBankAccount().getAccountNumber()));
                                sourceLiquidInputAmount = BigDecimalUtil.divide(sourceLiquidInputAmount, sourceExchangeRate);
                            }

                            currentTotalAmount = currentTotalAmount.add(sourceLiquidInputAmount);
                            if (currentTotalAmount.compareTo(getAccountingRecordFixData().getMaximumAmount()) > 0) {
                                selectedBankAccountMap.put(managersPayroll.getId(), getAccountingRecordFixData().getTargetBankAccount());
                            }
                        }
                    }
                    BigDecimal liquidInputAmount = managersPayroll.getLiquid();
                    FinancesBankAccount bankAccount = selectedBankAccountMap.get(managersPayroll.getId());
                    BigDecimal exchangeRate = BigDecimalUtil.toBigDecimal(exchangeRateAmountMap.get(bankAccount.getAccountNumber()));

                    if (FinancesCurrencyType.D.equals(bankAccount.getCurrency()) && !BigDecimalUtil.isZeroOrNull(liquidInputAmount)) {
                        liquidInputAmount = BigDecimalUtil.divide(liquidInputAmount, exchangeRate, 6);
                    }

                    if (PaymentType.PAYMENT_BANK_ACCOUNT.equals(paymentType)) {
                        nationalAmountForBank = nationalAmountForBank.add(managersPayroll.getLiquid());
                        addAmount(nationalAmountForBankDataMap, bankAccount, liquidInputAmount);
                    } else if (PaymentType.PAYMENT_WITH_CHECK.equals(paymentType)) {
                        nationalAmountForCheck = nationalAmountForCheck.add(managersPayroll.getLiquid());
                        addAmount(nationalAmountForCheckDataMap, bankAccount, liquidInputAmount);
                    }
                }
            }
        } else if (getGenerationByTimeType()) {
            if (professorsPayrollGenerationDataModel != null) {
                professorsPayrollGenerationDataModel = new ProfessorsPayrollGenerationDataModel();
            }
            Boolean validForm = true;
            List<GeneralPayroll> generalPayrollList = professorsPayrollGenerationDataModel.getResultList();
            if (generalPayrollList.isEmpty()) {
                facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR, "AccountingRecord.error.unselectedPayrollItems");
                validForm = false;
            } else {
                for (int i = 0; i < generalPayrollList.size() && validForm; i++) {
                    if (selectedBankAccountMap.get(generalPayrollList.get(i).getId()) == null) {
                        facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR, "AccountingRecord.error.unselectedBankAccount");
                        validForm = false;
                    }
                }
            }
            if (validForm) {
                for (GeneralPayroll generalPayroll : generalPayrollList) {
                    PaymentType paymentType = employeeService.getEmployeesPaymentType(generalPayroll.getEmployee());
                    if (isFixAmountOperation) {
                        FinancesBankAccount currentBankAccount = selectedBankAccountMap.get(generalPayroll.getId());
                        if (getAccountingRecordFixData().getSourceBankAccount().equals(currentBankAccount)
                                && ((PaymentType.PAYMENT_BANK_ACCOUNT.equals(paymentType) && dataProvider.equals(foreignAmountForBankDataMap)) ||
                                (PaymentType.PAYMENT_WITH_CHECK.equals(paymentType) && dataProvider.equals(foreignAmountForCheckDataMap)))) {
                            BigDecimal sourceLiquidInputAmount = generalPayroll.getLiquid();

                            if (FinancesCurrencyType.P.equals(getAccountingRecordFixData().getSourceBankAccount().getCurrency())) {
                                BigDecimal sourceExchangeRate = BigDecimalUtil.toBigDecimal(exchangeRateAmountMap.get(getAccountingRecordFixData().getSourceBankAccount().getAccountNumber()));
                                sourceLiquidInputAmount = BigDecimalUtil.multiply(sourceLiquidInputAmount, sourceExchangeRate);
                            }
                            currentTotalAmount = currentTotalAmount.add(sourceLiquidInputAmount);

                            if (currentTotalAmount.compareTo(getAccountingRecordFixData().getMaximumAmount()) > 0) {
                                selectedBankAccountMap.put(generalPayroll.getId(), getAccountingRecordFixData().getTargetBankAccount());
                            }
                        }
                    }
                    BigDecimal liquidInputAmount = generalPayroll.getLiquid();
                    FinancesBankAccount bankAccount = selectedBankAccountMap.get(generalPayroll.getId());
                    BigDecimal exchangeRate = BigDecimalUtil.toBigDecimal(exchangeRateAmountMap.get(bankAccount.getAccountNumber()));

                    if (FinancesCurrencyType.P.equals(bankAccount.getCurrency()) && !BigDecimalUtil.isZeroOrNull(liquidInputAmount)) {
                        liquidInputAmount = BigDecimalUtil.multiply(liquidInputAmount, exchangeRate, 6);
                    }

                    if (PaymentType.PAYMENT_BANK_ACCOUNT.equals(paymentType)) {
                        foreignAmountForBank = foreignAmountForBank.add(generalPayroll.getLiquid());
                        addAmount(foreignAmountForBankDataMap, bankAccount, liquidInputAmount);
                    } else if (PaymentType.PAYMENT_WITH_CHECK.equals(paymentType)) {
                        foreignAmountForCheck = foreignAmountForCheck.add(generalPayroll.getLiquid());
                        addAmount(foreignAmountForCheckDataMap, bankAccount, liquidInputAmount);
                    }
                }
            }
        } else if (getGestionPayrollChristmasBonusType()) {
            if (christmasPayrollGenerationDataModel != null) {
                christmasPayrollGenerationDataModel = new ChristmasPayrollGenerationDataModel();
            }

            Boolean validForm = true;
            List<ChristmasPayroll> christmasPayrollList = christmasPayrollGenerationDataModel.getResultList();

            if (christmasPayrollList.isEmpty()) {
                facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR, "AccountingRecord.error.unselectedPayrollItems");
                validForm = false;
            } else {
                for (int i = 0; i < christmasPayrollList.size() && validForm; i++) {
                    if (selectedBankAccountMap.get(christmasPayrollList.get(i).getId()) == null) {
                        facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR, "AccountingRecord.error.unselectedBankAccount");
                        validForm = false;
                    }
                }
            }
            if (validForm) {
                for (ChristmasPayroll christmasPayroll : christmasPayrollList) {
                    PaymentType paymentType = employeeService.getEmployeesPaymentType(christmasPayroll.getEmployee());
                    if (isFixAmountOperation) {
                        FinancesBankAccount currentBankAccount = selectedBankAccountMap.get(christmasPayroll.getId());
                        if (getAccountingRecordFixData().getSourceBankAccount().equals(currentBankAccount) &&
                                ((PaymentType.PAYMENT_BANK_ACCOUNT.equals(paymentType) && dataProvider.equals(nationalAmountForBankDataMap)) ||
                                        (PaymentType.PAYMENT_WITH_CHECK.equals(paymentType) && dataProvider.equals(nationalAmountForCheckDataMap)))) {
                            BigDecimal sourceLiquidInputAmount = christmasPayroll.getLiquid();

                            if (FinancesCurrencyType.D.equals(getAccountingRecordFixData().getSourceBankAccount().getCurrency())) {
                                BigDecimal sourceExchangeRate = BigDecimalUtil.toBigDecimal(exchangeRateAmountMap.get(getAccountingRecordFixData().getSourceBankAccount().getAccountNumber()));
                                sourceLiquidInputAmount = BigDecimalUtil.divide(sourceLiquidInputAmount, sourceExchangeRate);
                            }

                            currentTotalAmount = currentTotalAmount.add(sourceLiquidInputAmount);
                            if (currentTotalAmount.compareTo(getAccountingRecordFixData().getMaximumAmount()) > 0) {
                                selectedBankAccountMap.put(christmasPayroll.getId(), getAccountingRecordFixData().getTargetBankAccount());
                            }
                        }
                    }
                    BigDecimal liquidInputAmount = christmasPayroll.getLiquid();
                    FinancesBankAccount bankAccount = selectedBankAccountMap.get(christmasPayroll.getId());
                    BigDecimal exchangeRate = BigDecimalUtil.toBigDecimal(exchangeRateAmountMap.get(bankAccount.getAccountNumber()));

                    if (FinancesCurrencyType.D.equals(bankAccount.getCurrency()) && !BigDecimalUtil.isZeroOrNull(liquidInputAmount)) {
                        liquidInputAmount = BigDecimalUtil.divide(liquidInputAmount, exchangeRate, 6);
                    }

                    if (PaymentType.PAYMENT_BANK_ACCOUNT.equals(paymentType)) {
                        nationalAmountForBank = nationalAmountForBank.add(christmasPayroll.getLiquid());
                        addAmount(nationalAmountForBankDataMap, bankAccount, liquidInputAmount);
                    } else if (PaymentType.PAYMENT_WITH_CHECK.equals(paymentType)) {
                        nationalAmountForCheck = nationalAmountForCheck.add(christmasPayroll.getLiquid());
                        addAmount(nationalAmountForCheckDataMap, bankAccount, liquidInputAmount);
                    }
                }
            }
        }

        getInstance().setNationalAmountForBank(BigDecimalUtil.roundBigDecimal(nationalAmountForBank));
        getInstance().setForeignAmountForBank(BigDecimalUtil.roundBigDecimal(foreignAmountForBank));
        getInstance().setNationalAmountForCheck(BigDecimalUtil.roundBigDecimal(nationalAmountForCheck));
        getInstance().setForeignAmountForCheck(BigDecimalUtil.roundBigDecimal(foreignAmountForCheck));
        exchangeRateAmountMap.notifyObservers();
        setAccountingRecordFixData(new AccountingRecordFixData());
    }


    public void addAmount(Map<String, AccountingRecordData> currentDataMap, FinancesBankAccount financesBankAccount, BigDecimal amount) {
        BigDecimal currentAmount = currentDataMap.get(financesBankAccount.getAccountNumber()).getAmount();
        if (currentAmount == null) {
            currentAmount = BigDecimal.ZERO;
        }
        currentAmount = currentAmount.add(amount);
        currentDataMap.get(financesBankAccount.getAccountNumber()).setAmount(currentAmount);
        currentDataMap.get(financesBankAccount.getAccountNumber()).setCurrencyBankAccount(financesBankAccount.getCurrency());
    }

    public Class<? extends GenericPayroll> getCurrentEntityClass() {
        return getGestionPayrollChristmasBonusType() ? ChristmasPayroll.class :
                getProfessorGenerationBySalary() ? FiscalProfessorPayroll.class :
                        getGenerationBySalaryType() ? ManagersPayroll.class :
                                getGenerationByTimeType() ? GeneralPayroll.class : null;
    }

    public Boolean getGenerationBySalaryType() {
        return getGestionPayrollSalaryType() && PayrollGenerationType.GENERATION_BY_SALARY.equals(getInstance().getJobCategory() != null ? getInstance().getJobCategory().getPayrollGenerationType() : null);
    }

    public Boolean getGenerationByTimeType() {
        return getGestionPayrollSalaryType() && PayrollGenerationType.GENERATION_BY_TIME.equals(getInstance().getJobCategory() != null ? getInstance().getJobCategory().getPayrollGenerationType() : null);
    }

    public Boolean getGestionPayrollChristmasBonusType() {
        return GestionPayrollType.CHRISTMAS_BONUS.equals(getInstance().getGestionPayrollType());
    }

    public Boolean getGestionPayrollSalaryType() {
        return GestionPayrollType.SALARY.equals(getInstance().getGestionPayrollType());
    }

    public Boolean getProfessorGenerationBySalary() {
        return getGestionPayrollSalaryType() && getInstance().getJobCategory() != null && PayrollGenerationType.GENERATION_BY_PERIODSALARY.equals(getInstance().getJobCategory().getPayrollGenerationType());
    }

    public List<ManagersPayroll> getManagersPayrollGenerationList() {
        return gestionPayrollService.getManagersPayrollGeneration(getInstance().getBusinessUnit(), getInstance().getJobCategory(), getInstance().getGestion(), getInstance().getMonth());
    }

    public List<GeneralPayroll> getProfessorPayrollGenerationList() {
        return gestionPayrollService.getProfessorsPayrollGeneration(getInstance().getBusinessUnit(), getInstance().getJobCategory(), getInstance().getGestion(), getInstance().getMonth());
    }

    public List<ChristmasPayroll> getChristmasPayrollGenerationList() {
        return gestionPayrollService.getChristmasPayrollGeneration(getInstance().getBusinessUnit(), getInstance().getJobCategory(), getInstance().getGestion());
    }

    public List<FiscalProfessorPayroll> getFiscalProfessorPayrollGenerationList() {
        return gestionPayrollService.getFiscalProfessorPayrollGeneration(getInstance().getBusinessUnit(), getInstance().getJobCategory(), getInstance().getGestion());
    }

    public AccountingRecordMap<String, AccountingRecordData> getNationalAmountForCheckDataMap() {
        return nationalAmountForCheckDataMap;
    }

    public void setNationalAmountForCheckDataMap(AccountingRecordMap<String, AccountingRecordData> nationalAmountForCheckDataMap) {
        this.nationalAmountForCheckDataMap = nationalAmountForCheckDataMap;
    }

    public AccountingRecordMap<String, AccountingRecordData> getForeignAmountForCheckDataMap() {
        return foreignAmountForCheckDataMap;
    }

    public void setForeignAmountForCheckDataMap(AccountingRecordMap<String, AccountingRecordData> foreignAmountForCheckDataMap) {
        this.foreignAmountForCheckDataMap = foreignAmountForCheckDataMap;
    }

    public AccountingRecordMap<String, AccountingRecordData> getNationalAmountForBankDataMap() {
        return nationalAmountForBankDataMap;
    }

    public void setNationalAmountForBankDataMap(AccountingRecordMap<String, AccountingRecordData> nationalAmountForBankDataMap) {
        this.nationalAmountForBankDataMap = nationalAmountForBankDataMap;
    }

    public AccountingRecordMap<String, AccountingRecordData> getForeignAmountForBankDataMap() {
        return foreignAmountForBankDataMap;
    }

    public void setForeignAmountForBankDataMap(AccountingRecordMap<String, AccountingRecordData> foreignAmountForBankDataMap) {
        this.foreignAmountForBankDataMap = foreignAmountForBankDataMap;
    }

    public ObservableMap<String, BigDecimal> getExchangeRateAmountMap() {
        return exchangeRateAmountMap;
    }

    public void setExchangeRateAmountMap(ObservableMap<String, BigDecimal> exchangeRateAmountMap) {
        this.exchangeRateAmountMap = exchangeRateAmountMap;
    }

    public Map<Long, FinancesBankAccount> getSelectedBankAccountMap() {
        return selectedBankAccountMap;
    }

    public void setSelectedBankAccountMap(Map<Long, FinancesBankAccount> selectedBankAccountMap) {
        this.selectedBankAccountMap = selectedBankAccountMap;
    }

    public String getCostCenterFullName() {
        return getInstance().getCostCenter() != null ? getInstance().getCostCenter().getFullName() : null;
    }

    public void assignCostCenter(CostCenter costCenter) {
        getInstance().setCostCenter(costCenter);
    }

    public void clearCostCenter() {
        getInstance().setCostCenter(null);
    }

    public BigDecimal getNationalAmountForCheckNationalTotalAmount() {
        return nationalAmountForCheckDataMap.getNationalTotalAmount();
    }

    public BigDecimal getNationalAmountForCheckForeignTotalAmount() {
        return nationalAmountForCheckDataMap.getForeignTotalAmount();
    }

    public BigDecimal getForeignAmountForCheckNationalTotalAmount() {
        return foreignAmountForCheckDataMap.getNationalTotalAmount();
    }

    public BigDecimal getForeignAmountForCheckForeignTotalAmount() {
        return foreignAmountForCheckDataMap.getForeignTotalAmount();
    }

    public BigDecimal getNationalAmountForBankNationalTotalAmount() {
        return nationalAmountForBankDataMap.getNationalTotalAmount();
    }

    public BigDecimal getNationalAmountForBankForeignTotalAmount() {
        return nationalAmountForBankDataMap.getForeignTotalAmount();
    }

    public BigDecimal getForeignAmountForBankNationalTotalAmount() {
        return foreignAmountForBankDataMap.getNationalTotalAmount();
    }

    public BigDecimal getForeignAmountForBankForeignTotalAmount() {
        return foreignAmountForBankDataMap.getForeignTotalAmount();
    }

    public FinancesBankAccount getMainBankAccount() {
        return mainBankAccount;
    }

    public void setMainBankAccount(FinancesBankAccount mainBankAccount) {
        this.mainBankAccount = mainBankAccount;
    }

    public AccountingRecordFixData getAccountingRecordFixData() {
        return accountingRecordFixData;
    }

    public void setAccountingRecordFixData(AccountingRecordFixData accountingRecordFixData) {
        this.accountingRecordFixData = accountingRecordFixData;
    }

    public void updateMothByGestionPayrollType() {
        getInstance().setMonth(getGestionPayrollChristmasBonusType() ? Month.DECEMBER : null);
    }

    public void assignProvider(Provider provider) {
        getInstance().setProvider(provider);
    }

    public void updateProviderInfo() {
        try {
            getInstance().setProvider(getService().findById(Provider.class, getInstance().getProvider().getId()));

        } catch (EntryNotFoundException ignored) {
        }
        getInstance().setPayableAccount(getInstance().getProvider().getPayableAccount());
    }

    public void clearProvider() {
        getInstance().setProvider(null);
    }

    private void addQuotasValidErrorMessage() {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                "AccountingRecord.error.quotasValidError");
    }

    private void addProviderAccountCurrencyMismatchErrorMessage() {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                "AccountingRecord.error.providerAccountCurrencyMismatch");
    }
}
