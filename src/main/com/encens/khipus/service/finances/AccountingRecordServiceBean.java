package com.encens.khipus.service.finances;

import com.encens.khipus.exception.ConcurrencyException;
import com.encens.khipus.exception.finances.*;
import com.encens.khipus.framework.service.GenericService;
import com.encens.khipus.model.admin.BusinessUnit;
import com.encens.khipus.model.employees.*;
import com.encens.khipus.model.employees.Currency;
import com.encens.khipus.model.finances.*;
import com.encens.khipus.service.common.SequenceGeneratorService;
import com.encens.khipus.service.employees.EmployeeService;
import com.encens.khipus.service.employees.GeneratedPayrollService;
import com.encens.khipus.service.employees.SalaryMovementService;
import com.encens.khipus.util.*;
import com.encens.khipus.util.employees.*;
import com.encens.khipus.util.finances.PayableDocumentSourceType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.log.Log;

import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;
import java.math.BigDecimal;
import java.util.*;

/**
 * AccountingRecordServiceBean
 *
 * @author
 * @version 3.5.2.2
 */
@Name("accountingRecordService")
@Stateless
@AutoCreate
@TransactionManagement(TransactionManagementType.BEAN)
public class AccountingRecordServiceBean implements AccountingRecordService {

    @Resource
    private UserTransaction userTransaction;
    @PersistenceContext(unitName = "khipus")
    private EntityManager em;
    @In
    private GeneratedPayrollService generatedPayrollService;
    @In
    private EmployeeService employeeService;
    @In
    private VoucherService voucherService;
    @In
    private SequenceGeneratorService sequenceGeneratorService;
    @In
    private FinancesBankAccountService financesBankAccountService;
    @In
    private CashAccountService cashAccountService;
    @In
    private GenericService genericService;
    @In
    private SalaryMovementService salaryMovementService;
    @In
    private RotatoryFundCollectionService rotatoryFundCollectionService;
    @In
    private FinancesPkGeneratorService financesPkGeneratorService;
    @In
    private PayableDocumentService payableDocumentService;
    @Logger
    protected Log log;


    public AccountingRecordResult create(Class<? extends GenericPayroll> genericPayrollClass,
                                         AccountingRecord accountingRecord,
                                         List<Long> payrollGenerationIdList,
                                         AccountingRecordMap<String, AccountingRecordData> nationalAmountForCheckDataMap,
                                         AccountingRecordMap<String, AccountingRecordData> foreignAmountForCheckDataMap,
                                         AccountingRecordMap<String, AccountingRecordData> nationalAmountForBankDataMap,
                                         AccountingRecordMap<String, AccountingRecordData> foreignAmountForBankDataMap,
                                         ObservableMap<String, BigDecimal> exchangeRateAmountMap,
                                         Map<Long, FinancesBankAccount> selectedBankAccountMap,
                                         Map<Long, FinancesCurrencyType> currencyMapPaymentByEmployee,
                                         String voucherGlossForPayrollGeneration,
                                         String voucherGlossForSalaryMovement,
                                         String voucherGlossForChristmasProvision,
                                         String voucherGlossForCompensationPrevision,
                                         String voucherGlossForPayment) throws RotatoryFundNullifiedException, IceCanNotBeGreaterThanAmountException, ConcurrencyException, RotatoryFundCollectionNullifiedException, RotatoryFundLiquidatedException, ExemptCanNotBeGreaterThanAmountException, CompanyConfigurationNotFoundException, ExemptPlusIceCanNotBeGreaterThanAmountException, RotatoryFundCollectionNotFoundException, CollectionSumExceedsRotatoryFundAmountException, RotatoryFundCollectionApprovedException {

        AccountingRecordResult accountingRecordResult = AccountingRecordResult.SUCCESS;
        Boolean isSalaryPayroll = !ChristmasPayroll.class.equals(genericPayrollClass);

        List<AccountingRecordRelatedTransaction> relatedTransactionList = new ArrayList<AccountingRecordRelatedTransaction>(0);
        Map<String, CostCenterMigrationData> resultMap = new HashMap<String, CostCenterMigrationData>();
        Map<String, SalaryMovementMigrationData> salaryMovementMap = new HashMap<String, SalaryMovementMigrationData>();
        Map<String, CostCenterMigrationData> christmasProvisionDetail = new HashMap<String, CostCenterMigrationData>();
        Map<String, CostCenterMigrationData> compensationPrevisionDetail = new HashMap<String, CostCenterMigrationData>();

        Map<String, Map<String, BankAccountPaymentTypeMigrationData>> resultMapForNationalBAPT = new HashMap<String, Map<String, BankAccountPaymentTypeMigrationData>>();
        Map<String, Map<String, CheckPaymentTypeMigrationData>> resultMapForNationalCPT = new HashMap<String, Map<String, CheckPaymentTypeMigrationData>>();

        Map<String, Map<String, BankAccountPaymentTypeMigrationData>> resultMapForForeignBAPT = new HashMap<String, Map<String, BankAccountPaymentTypeMigrationData>>();
        Map<String, Map<String, CheckPaymentTypeMigrationData>> resultMapForForeignCPT = new HashMap<String, Map<String, CheckPaymentTypeMigrationData>>();

        Map<String, CashAccount> cashAccountMap = new HashMap<String, CashAccount>();
        FinancesCurrencyType mainCurrency = null;
        BigDecimal mainExchangeRateAmount = null;
        String relatedTransactionForPayments;

        List<ManagersPayroll> managersPayrollList = null;
        List<GeneralPayroll> generalPayrollList = null;
        List<ChristmasPayroll> christmasPayrollList = null;
        List<FiscalProfessorPayroll> fiscalProfessorPayrollList = null;

        Map<Long, List<SalaryMovement>> salaryMovementByEmployeeMap = salaryMovementService.findByPayrollGenerationIdList(genericPayrollClass, payrollGenerationIdList, accountingRecord.getGeneratedPayroll().getGestionPayroll());

        if (FiscalProfessorPayroll.class.equals(genericPayrollClass)) {
            mainCurrency = FinancesCurrencyType.P;
            fiscalProfessorPayrollList = generatedPayrollService.loadFiscalProfessorPayrollList(payrollGenerationIdList);
            for (int j = 0; j < fiscalProfessorPayrollList.size() && AccountingRecordResult.SUCCESS.equals(accountingRecordResult); j++) {
                FiscalProfessorPayroll fiscalProfessorPayroll = fiscalProfessorPayrollList.get(j);
                mainExchangeRateAmount = mainExchangeRateAmount == null ? fiscalProfessorPayroll.getGeneratedPayroll().getGestionPayroll().getExchangeRate().getRate() : mainExchangeRateAmount;
                CostCenter costCenter = fiscalProfessorPayroll.getCostCenter();
                if (costCenter == null) {
                    costCenter = employeeService.getCostCenterByGeneratedPayrollAndEmployee(fiscalProfessorPayroll.getGeneratedPayroll(), fiscalProfessorPayroll.getEmployee());
                }
                String bankAccountCode = selectedBankAccountMap.get(fiscalProfessorPayroll.getId()).getAccountNumber();

                if (costCenter != null) {
                    PaymentType paymentType = employeeService.getEmployeesPaymentType(fiscalProfessorPayroll.getEmployee());
                    Currency currency = employeeService.getEmployeesCurrencyByPaymentType(fiscalProfessorPayroll.getGeneratedPayroll(), fiscalProfessorPayroll.getEmployee(), paymentType);
                    FinancesCurrencyType financesCurrencyType = getFinancesCurrencyType(currency);
                    currencyMapPaymentByEmployee.put(fiscalProfessorPayroll.getEmployee().getId(), financesCurrencyType);

                    // add SalaryMovement amounts
                    addAmount(salaryMovementMap, accountingRecord.getBusinessUnit(), costCenter, accountingRecord.getJobCategory(), salaryMovementByEmployeeMap.get(fiscalProfessorPayroll.getEmployee().getId()), mainExchangeRateAmount);
                    // add amounts by CostCenter
                    addAmount(resultMap, fiscalProfessorPayroll.getId(), accountingRecord.getBusinessUnit(), costCenter, accountingRecord.getJobCategory(), fiscalProfessorPayroll.getLiquid(), BigDecimal.ONE, CostCenterMigrationData.CostCenterMigrationDataType.PAYROLL_PROVISION);
                    // add total income for prevision and provision
                    addAmount(christmasProvisionDetail, fiscalProfessorPayroll.getId(), accountingRecord.getBusinessUnit(), costCenter, accountingRecord.getJobCategory(), getCurrentTotalIncomeValue(fiscalProfessorPayroll.getTotalIncome()), BigDecimal.ONE, CostCenterMigrationData.CostCenterMigrationDataType.CHRISTMAS_PROVISION);
                    addAmount(compensationPrevisionDetail, fiscalProfessorPayroll.getId(), accountingRecord.getBusinessUnit(), costCenter, accountingRecord.getJobCategory(), getCurrentTotalIncomeValue(fiscalProfessorPayroll.getTotalIncome()), BigDecimal.ONE, CostCenterMigrationData.CostCenterMigrationDataType.COMPENSATION_PREVISION);

                    if (PaymentType.PAYMENT_BANK_ACCOUNT.equals(paymentType)) {
                        addAmount(resultMapForNationalBAPT, fiscalProfessorPayroll.getId(), accountingRecord.getBusinessUnit(), costCenter, accountingRecord.getJobCategory(), bankAccountCode, fiscalProfessorPayroll.getLiquid(), mainCurrency, isSalaryPayroll);
                    } else if (PaymentType.PAYMENT_WITH_CHECK.equals(paymentType)) {
                        addAmount(resultMapForNationalCPT, fiscalProfessorPayroll.getId(), accountingRecord.getBusinessUnit(), costCenter, accountingRecord.getJobCategory(), fiscalProfessorPayroll.getEmployee(), bankAccountCode, fiscalProfessorPayroll.getLiquid(), mainCurrency, isSalaryPayroll);
                    }
                } else {
                    accountingRecordResult = AccountingRecordResult.WITHOUT_COSTCENTER.assignResultData(fiscalProfessorPayroll.getEmployee().getIdNumberAndFullName(), fiscalProfessorPayroll.getGeneratedPayroll().getGestionPayroll().getBusinessUnit().getPublicity(), fiscalProfessorPayroll.getGeneratedPayroll().getName());
                }
            }

        } else if (ManagersPayroll.class.equals(genericPayrollClass)) {
            mainCurrency = FinancesCurrencyType.P;
            managersPayrollList = generatedPayrollService.loadManagersPayrollList(payrollGenerationIdList);
            for (int j = 0; j < managersPayrollList.size() && AccountingRecordResult.SUCCESS.equals(accountingRecordResult); j++) {
                ManagersPayroll managersPayroll = managersPayrollList.get(j);
                mainExchangeRateAmount = mainExchangeRateAmount == null ? managersPayroll.getGeneratedPayroll().getGestionPayroll().getExchangeRate().getRate() : mainExchangeRateAmount;
                CostCenter costCenter = managersPayroll.getCostCenter();
                if (costCenter == null) {
                    costCenter = employeeService.getCostCenterByGeneratedPayrollAndEmployee(managersPayroll.getGeneratedPayroll(), managersPayroll.getEmployee());
                }
                String bankAccountCode = selectedBankAccountMap.get(managersPayroll.getId()).getAccountNumber();

                if (costCenter != null) {
                    PaymentType paymentType = employeeService.getEmployeesPaymentType(managersPayroll.getEmployee());
                    Currency currency = employeeService.getEmployeesCurrencyByPaymentType(managersPayroll.getGeneratedPayroll(), managersPayroll.getEmployee(), paymentType);
                    FinancesCurrencyType financesCurrencyType = getFinancesCurrencyType(currency);
                    currencyMapPaymentByEmployee.put(managersPayroll.getEmployee().getId(), financesCurrencyType);

                    // add SalaryMovement amounts
                    addAmount(salaryMovementMap, accountingRecord.getBusinessUnit(), costCenter, accountingRecord.getJobCategory(), salaryMovementByEmployeeMap.get(managersPayroll.getEmployee().getId()), mainExchangeRateAmount);
                    // add amounts by CostCenter
                    addAmount(resultMap, managersPayroll.getId(), accountingRecord.getBusinessUnit(), costCenter, accountingRecord.getJobCategory(), managersPayroll.getLiquid(), BigDecimal.ONE, CostCenterMigrationData.CostCenterMigrationDataType.PAYROLL_PROVISION);
                    // add total income for compensation
                    addAmount(christmasProvisionDetail, managersPayroll.getId(), accountingRecord.getBusinessUnit(), costCenter, accountingRecord.getJobCategory(), getCurrentTotalIncomeValue(managersPayroll.getTotalIncome()), BigDecimal.ONE, CostCenterMigrationData.CostCenterMigrationDataType.CHRISTMAS_PROVISION);
                    addAmount(compensationPrevisionDetail, managersPayroll.getId(), accountingRecord.getBusinessUnit(), costCenter, accountingRecord.getJobCategory(), getCurrentTotalIncomeValue(managersPayroll.getTotalIncome()), BigDecimal.ONE, CostCenterMigrationData.CostCenterMigrationDataType.COMPENSATION_PREVISION);

                    if (PaymentType.PAYMENT_BANK_ACCOUNT.equals(paymentType)) {
                        addAmount(resultMapForNationalBAPT, managersPayroll.getId(), accountingRecord.getBusinessUnit(), costCenter, accountingRecord.getJobCategory(), bankAccountCode, managersPayroll.getLiquid(), mainCurrency, isSalaryPayroll);
                    } else if (PaymentType.PAYMENT_WITH_CHECK.equals(paymentType)) {
                        addAmount(resultMapForNationalCPT, managersPayroll.getId(), accountingRecord.getBusinessUnit(), costCenter, accountingRecord.getJobCategory(), managersPayroll.getEmployee(), bankAccountCode, managersPayroll.getLiquid(), mainCurrency, isSalaryPayroll);
                    }
                } else {
                    accountingRecordResult = AccountingRecordResult.WITHOUT_COSTCENTER.assignResultData(managersPayroll.getEmployee().getIdNumberAndFullName(), managersPayroll.getGeneratedPayroll().getGestionPayroll().getBusinessUnit().getPublicity(), managersPayroll.getGeneratedPayroll().getName());
                }
            }

        } else if (GeneralPayroll.class.equals(genericPayrollClass)) {
            mainCurrency = FinancesCurrencyType.D;
            generalPayrollList = generatedPayrollService.loadGeneralPayrollList(payrollGenerationIdList);
            for (int j = 0; j < generalPayrollList.size() && AccountingRecordResult.SUCCESS.equals(accountingRecordResult); j++) {
                GeneralPayroll generalPayroll = generalPayrollList.get(j);
                mainExchangeRateAmount = mainExchangeRateAmount == null ? generalPayroll.getGeneratedPayroll().getGestionPayroll().getExchangeRate().getRate() : mainExchangeRateAmount;
                CostCenter costCenter = generalPayroll.getCostCenter();
                if (costCenter == null) {
                    costCenter = employeeService.getCostCenterByGeneratedPayrollAndEmployee(generalPayroll.getGeneratedPayroll(), generalPayroll.getEmployee());
                }
                String bankAccountCode = selectedBankAccountMap.get(generalPayroll.getId()).getAccountNumber();
                BigDecimal exchangeRate = BigDecimalUtil.toBigDecimal(exchangeRateAmountMap.get(bankAccountCode));

                if (costCenter != null) {
                    PaymentType paymentType = employeeService.getEmployeesPaymentType(generalPayroll.getEmployee());
                    Currency currency = employeeService.getEmployeesCurrencyByPaymentType(generalPayroll.getGeneratedPayroll(), generalPayroll.getEmployee(), paymentType);
                    FinancesCurrencyType financesCurrencyType = getFinancesCurrencyType(currency);
                    currencyMapPaymentByEmployee.put(generalPayroll.getEmployee().getId(), financesCurrencyType);

                    // add SalaryMovement amounts
                    addAmount(salaryMovementMap, accountingRecord.getBusinessUnit(), costCenter, accountingRecord.getJobCategory(), salaryMovementByEmployeeMap.get(generalPayroll.getEmployee().getId()), mainExchangeRateAmount);
                    // add amounts by CostCenter
                    addAmount(resultMap, generalPayroll.getId(), accountingRecord.getBusinessUnit(), costCenter, accountingRecord.getJobCategory(), generalPayroll.getLiquid(), exchangeRate, CostCenterMigrationData.CostCenterMigrationDataType.PAYROLL_PROVISION);

                    if (PaymentType.PAYMENT_BANK_ACCOUNT.equals(paymentType)) {
                        addAmount(resultMapForForeignBAPT, generalPayroll.getId(), accountingRecord.getBusinessUnit(), costCenter, accountingRecord.getJobCategory(), bankAccountCode, generalPayroll.getLiquid(), mainCurrency, isSalaryPayroll);
                    } else if (PaymentType.PAYMENT_WITH_CHECK.equals(paymentType)) {
                        addAmount(resultMapForForeignCPT, generalPayroll.getId(), accountingRecord.getBusinessUnit(), costCenter, accountingRecord.getJobCategory(), generalPayroll.getEmployee(), bankAccountCode, generalPayroll.getLiquid(), mainCurrency, isSalaryPayroll);
                    }

                } else {
                    accountingRecordResult = AccountingRecordResult.WITHOUT_COSTCENTER.assignResultData(generalPayroll.getEmployee().getIdNumberAndFullName(), generalPayroll.getGeneratedPayroll().getGestionPayroll().getBusinessUnit().getPublicity(), generalPayroll.getGeneratedPayroll().getName());
                }
            }
        } else if (ChristmasPayroll.class.equals(genericPayrollClass)) {
            mainCurrency = FinancesCurrencyType.P;
            christmasPayrollList = generatedPayrollService.loadChristmasPayrollList(payrollGenerationIdList);

            for (int j = 0; j < christmasPayrollList.size() && AccountingRecordResult.SUCCESS.equals(accountingRecordResult); j++) {
                ChristmasPayroll christmasPayroll = christmasPayrollList.get(j);
                mainExchangeRateAmount = mainExchangeRateAmount == null ? christmasPayroll.getGeneratedPayroll().getGestionPayroll().getExchangeRate().getRate() : mainExchangeRateAmount;
                CostCenter costCenter = christmasPayroll.getCostCenter();
                if (costCenter == null) {
                    costCenter = employeeService.getCostCenterByGeneratedPayrollAndEmployee(christmasPayroll.getGeneratedPayroll(), christmasPayroll.getEmployee());
                }
                String bankAccountCode = selectedBankAccountMap.get(christmasPayroll.getId()).getAccountNumber();

                if (costCenter != null) {
                    PaymentType paymentType = employeeService.getEmployeesPaymentType(christmasPayroll.getEmployee());
                    Currency currency = employeeService.getEmployeesCurrencyByPaymentType(christmasPayroll.getGeneratedPayroll(), christmasPayroll.getEmployee(), paymentType);
                    FinancesCurrencyType financesCurrencyType = getFinancesCurrencyType(currency);
                    currencyMapPaymentByEmployee.put(christmasPayroll.getEmployee().getId(), financesCurrencyType);

//                    addAmount(resultMap, christmasPayroll.getId(), accountingRecord.getBusinessUnit(), costCenter, accountingRecord.getJobCategory(), christmasPayroll.getLiquid(), BigDecimal.ONE, CostCenterMigrationData.CostCenterMigrationDataType.CHRISTMAS_PROVISION);

                    if (PaymentType.PAYMENT_BANK_ACCOUNT.equals(paymentType)) {
                        addAmount(resultMapForNationalBAPT, christmasPayroll.getId(), accountingRecord.getBusinessUnit(), costCenter, accountingRecord.getJobCategory(), bankAccountCode, christmasPayroll.getLiquid(), mainCurrency, isSalaryPayroll);
                    } else if (PaymentType.PAYMENT_WITH_CHECK.equals(paymentType)) {
                        addAmount(resultMapForNationalCPT, christmasPayroll.getId(), accountingRecord.getBusinessUnit(), costCenter, accountingRecord.getJobCategory(), christmasPayroll.getEmployee(), bankAccountCode, christmasPayroll.getLiquid(), mainCurrency, isSalaryPayroll);
                    }
                } else {
                    accountingRecordResult = AccountingRecordResult.WITHOUT_COSTCENTER.assignResultData(christmasPayroll.getEmployee().getIdNumberAndFullName(), christmasPayroll.getGeneratedPayroll().getGestionPayroll().getBusinessUnit().getPublicity(), christmasPayroll.getGeneratedPayroll().getName());
                }
            }

        }

        if (AccountingRecordResult.SUCCESS.equals(accountingRecordResult)) {
            try {
                CompanyConfiguration companyConfiguration = genericService.findById(CompanyConfiguration.class, Constants.defaultCompanyNumber);
                CashAccount balanceExchangeRateAccount = companyConfiguration.getBalanceExchangeRateAccount();
                CostCenter balanceExchangeRateCostCenter = companyConfiguration.getExchangeRateBalanceCostCenter();

                userTransaction.setTransactionTimeout(payrollGenerationIdList.size() * 60);
                userTransaction.begin();
                //set main transaction number
                String mainTransactionNumber = financesPkGeneratorService.getNextPK();
                accountingRecord.setTransactionNumber(mainTransactionNumber);

                RotatoryFundMigrationData rotatoryFundMigrationData = new RotatoryFundMigrationData(financesPkGeneratorService.getNextPK());
                rotatoryFundCollectionService.approveRotatoryFundCollectionsByPayroll(rotatoryFundMigrationData, accountingRecord.getGeneratedPayroll(), genericPayrollClass, payrollGenerationIdList, currencyMapPaymentByEmployee);

                if (!ChristmasPayroll.class.equals(genericPayrollClass)) {
                    // save payable document for payroll provision
                    CashAccount payableCashAccount = FinancesCurrencyType.P.equals(mainCurrency) ?
                            accountingRecord.getJobCategory().getNationalCurrencyCreditAccount() :
                            accountingRecord.getJobCategory().getForeignCurrencyCreditAccount();
                    CashAccount detailCashAccount = FinancesCurrencyType.P.equals(mainCurrency) ?
                            accountingRecord.getJobCategory().getNationalCurrencyDebitAccount() :
                            accountingRecord.getJobCategory().getForeignCurrencyDebitAccount();
                    BigDecimal payableExchangeRate = getExchangeAmountByCashAccount(payableCashAccount, mainExchangeRateAmount);

                    String payableDocumentNumber = payableDocumentService.nextPayableDocumentNumberForVoucher(PayableDocumentSourceType.HHRR);
                    Voucher payableDocumentVoucher = VoucherBuilder.newPayableDocumentVoucher(payableDocumentNumber,
                            accountingRecord.getProvider(),
                            accountingRecord.getProvider().getEntity(),
                            accountingRecord.getProvider().getPayableAccount(),
                            accountingRecord.getDocumentType(),
                            BigDecimal.ZERO,
                            accountingRecord.getProvider().getPayableAccount().getCurrency(),
                            payableExchangeRate,
                            voucherGlossForPayrollGeneration);
                    payableDocumentVoucher.setRelatedTransactionNumber(mainTransactionNumber);
                    BigDecimal payableDocumentTotalAmount = BigDecimal.ZERO;
                    CostCenterMigrationData lastCostCenterMigrationData = null;
                    for (Map.Entry<String, CostCenterMigrationData> entry : resultMap.entrySet()) {
                        CostCenterMigrationData costCenterMigrationData = entry.getValue();
                        lastCostCenterMigrationData = costCenterMigrationData;
                        BigDecimal currentAmount = BigDecimalUtil.multiply(costCenterMigrationData.getAmount(), costCenterMigrationData.getExchangeRate());
                        payableDocumentTotalAmount = BigDecimalUtil.sum(payableDocumentTotalAmount, currentAmount);

                        payableDocumentVoucher.addVoucherDetail(VoucherDetailBuilder.newDebitVoucherDetail(
                                costCenterMigrationData.getBusinessUnitCode(),
                                costCenterMigrationData.getCostCenterCode(),
                                detailCashAccount,
                                currentAmount,
                                detailCashAccount.getCurrency(),
                                getExchangeAmountByCashAccount(detailCashAccount, mainExchangeRateAmount)));
                    }
                    payableDocumentVoucher.setAmount(
                            FinancesCurrencyType.P.equals(mainCurrency) ? payableDocumentTotalAmount :
                                    BigDecimalUtil.divide(payableDocumentTotalAmount, payableExchangeRate)
                    );

                    if (!FinancesCurrencyType.P.equals(mainCurrency)) {
                        BigDecimal payableDocumentVoucherConvertAmount = BigDecimalUtil.multiply(payableDocumentVoucher.getAmount(), payableExchangeRate);
                        BigDecimal balanceAmount = BigDecimalUtil.subtract(payableDocumentVoucherConvertAmount, payableDocumentTotalAmount);
                        if (balanceAmount.compareTo(BigDecimal.ZERO) > 0) {
                            payableDocumentVoucher.addVoucherDetail(VoucherDetailBuilder.newDebitVoucherDetail(lastCostCenterMigrationData.getBusinessUnitCode(), balanceExchangeRateCostCenter.getCode(), balanceExchangeRateAccount, balanceAmount, FinancesCurrencyType.P, BigDecimal.ONE));
                        } else if (balanceAmount.compareTo(BigDecimal.ZERO) < 0) {
                            payableDocumentVoucher.addVoucherDetail(VoucherDetailBuilder.newCreditVoucherDetail(lastCostCenterMigrationData.getBusinessUnitCode(), balanceExchangeRateCostCenter.getCode(), balanceExchangeRateAccount, balanceAmount.abs(), FinancesCurrencyType.P, BigDecimal.ONE));
                        }
                    }
                    payableDocumentVoucher.setUserNumber(companyConfiguration.getDefaultPayableFinanceUser().getId());
                    voucherService.create(payableDocumentVoucher);
                    relatedTransactionForPayments = payableDocumentVoucher.getTransactionNumber();
                    relatedTransactionList.add(new AccountingRecordRelatedTransaction(AccountingRecordRelatedTransactionType.PAYABLE_DOCUMENT, payableDocumentVoucher.getTransactionNumber()));

                    // save SalaryMovement values
                    Voucher salaryMovementVoucher = VoucherBuilder.newGeneralVoucher(Constants.HUMANRESOURCE_VOUCHER_FORM, voucherGlossForSalaryMovement, mainTransactionNumber);
                    salaryMovementVoucher.setTransactionNumber(rotatoryFundMigrationData.getTransactionNumber());

                    // add debit values for SalaryMovement
                    for (SalaryMovementMigrationData salaryMovementMigrationData : salaryMovementMap.values()) {
                        BigDecimal debitAmount = BigDecimalUtil.multiply(salaryMovementMigrationData.getAmount(), salaryMovementMigrationData.getExchangeRate());
                        CashAccount debitAccount = FinancesCurrencyType.P.equals(mainCurrency) ? salaryMovementMigrationData.getNationalCurrencyDebitAccount() : salaryMovementMigrationData.getForeignCurrencyDebitAccount();
                        salaryMovementVoucher.addVoucherDetail(VoucherDetailBuilder.newDebitVoucherDetail(
                                salaryMovementMigrationData.getBusinessUnitCode(),
                                salaryMovementMigrationData.getCostCenterCode(),
                                debitAccount,
                                debitAmount,
                                debitAccount.getCurrency(),
                                getExchangeAmountByCashAccount(debitAccount, mainExchangeRateAmount)));
                    }
                    // add debit values from RotatoryFundMigrationValue
                    for (RotatoryFundMigrationValue value : rotatoryFundMigrationData.getDebitValues()) {
                        salaryMovementVoucher.addVoucherDetail(VoucherDetailBuilder.newDebitVoucherDetail(
                                value.getBusinessUnitCode(),
                                value.getCostCenterCode(),
                                value.getCashAccount(),
                                value.getAmount(),
                                value.getCurrency(),
                                value.getExchangeAmount()));
                    }

                    // add credit values for SalaryMovement
                    for (SalaryMovementMigrationData salaryMovementMigrationData : salaryMovementMap.values()) {
                        for (Map.Entry<SalaryMovementType, BigDecimal> amountByType : salaryMovementMigrationData.getAmountsBySalaryMovementType().entrySet()) {
                            BigDecimal detailAmount = BigDecimalUtil.multiply(amountByType.getValue(), salaryMovementMigrationData.getExchangeRate());
                            salaryMovementVoucher.addVoucherDetail(VoucherDetailBuilder.newCreditVoucherDetail(
                                    salaryMovementMigrationData.getBusinessUnitCode(),
                                    salaryMovementMigrationData.getCostCenterCode(),
                                    amountByType.getKey().getCashAccount(),
                                    detailAmount,
                                    amountByType.getKey().getCashAccount().getCurrency(),
                                    getExchangeAmountByCashAccount(amountByType.getKey().getCashAccount(), mainExchangeRateAmount)));
                        }
                    }
                    // add credit values from RotatoryFundMigrationValue
                    for (RotatoryFundMigrationValue value : rotatoryFundMigrationData.getCreditValues()) {
                        salaryMovementVoucher.addVoucherDetail(VoucherDetailBuilder.newCreditVoucherDetail(
                                value.getBusinessUnitCode(),
                                value.getCostCenterCode(),
                                value.getCashAccount(),
                                value.getAmount(),
                                value.getCurrency(),
                                value.getExchangeAmount()));
                    }

                    if (!ValidatorUtil.isEmptyOrNull(salaryMovementVoucher.getDetails())) {
                        salaryMovementVoucher.setUserNumber(companyConfiguration.getDefaultAccountancyUser().getId());
                        voucherService.create(salaryMovementVoucher);
                        relatedTransactionList.add(new AccountingRecordRelatedTransaction(AccountingRecordRelatedTransactionType.SALARY_MOVEMENT, salaryMovementVoucher.getTransactionNumber()));
                    }

                    if (!ValidatorUtil.isEmptyOrNull(christmasProvisionDetail)) {
                        //save christmas prevision voucher
                        Voucher christmasProvisionVoucher = VoucherBuilder.newGeneralVoucher(Constants.HUMANRESOURCE_VOUCHER_FORM, voucherGlossForChristmasProvision, mainTransactionNumber);
                        //Add debit amounts
                        for (CostCenterMigrationData costCenterMigrationData : christmasProvisionDetail.values()) {
                            christmasProvisionVoucher.addVoucherDetail(VoucherDetailBuilder.newDebitVoucherDetail(
                                    costCenterMigrationData.getBusinessUnitCode(),
                                    costCenterMigrationData.getCostCenterCode(),
                                    costCenterMigrationData.getNationalCurrencyDebitAccount(),
                                    costCenterMigrationData.getAmount(),
                                    costCenterMigrationData.getNationalCurrencyDebitAccount().getCurrency(),
                                    getExchangeAmountByCashAccount(costCenterMigrationData.getNationalCurrencyDebitAccount(), mainExchangeRateAmount)));
                        }
                        //Add credit amounts
                        for (CostCenterMigrationData costCenterMigrationData : christmasProvisionDetail.values()) {
                            christmasProvisionVoucher.addVoucherDetail(VoucherDetailBuilder.newCreditVoucherDetail(
                                    costCenterMigrationData.getBusinessUnitCode(),
                                    costCenterMigrationData.getCostCenterCode(),
                                    costCenterMigrationData.getNationalCurrencyCreditAccount(),
                                    costCenterMigrationData.getAmount(),
                                    costCenterMigrationData.getNationalCurrencyCreditAccount().getCurrency(),
                                    getExchangeAmountByCashAccount(costCenterMigrationData.getNationalCurrencyCreditAccount(), mainExchangeRateAmount)));
                        }
                        christmasProvisionVoucher.setUserNumber(companyConfiguration.getDefaultAccountancyUser().getId());
                        voucherService.create(christmasProvisionVoucher);
                        relatedTransactionList.add(new AccountingRecordRelatedTransaction(AccountingRecordRelatedTransactionType.CHRISTMAS_PROVISION, christmasProvisionVoucher.getTransactionNumber()));
                    }

                    if (!ValidatorUtil.isEmptyOrNull(compensationPrevisionDetail)) {
                        //save compensation prevision voucher
                        Voucher compensationPrevisionVoucher = VoucherBuilder.newGeneralVoucher(Constants.HUMANRESOURCE_VOUCHER_FORM, voucherGlossForCompensationPrevision, mainTransactionNumber);
                        compensationPrevisionVoucher.setUserNumber(companyConfiguration.getDefaultAccountancyUser().getId());
                        //Add debit amounts
                        for (CostCenterMigrationData costCenterMigrationData : compensationPrevisionDetail.values()) {
                            compensationPrevisionVoucher.addVoucherDetail(VoucherDetailBuilder.newDebitVoucherDetail(
                                    costCenterMigrationData.getBusinessUnitCode(),
                                    costCenterMigrationData.getCostCenterCode(),
                                    costCenterMigrationData.getNationalCurrencyDebitAccount(),
                                    costCenterMigrationData.getAmount(),
                                    costCenterMigrationData.getNationalCurrencyDebitAccount().getCurrency(),
                                    getExchangeAmountByCashAccount(costCenterMigrationData.getNationalCurrencyDebitAccount(), mainExchangeRateAmount)));
                        }
                        //Add credit amounts
                        for (CostCenterMigrationData costCenterMigrationData : compensationPrevisionDetail.values()) {
                            compensationPrevisionVoucher.addVoucherDetail(VoucherDetailBuilder.newCreditVoucherDetail(
                                    costCenterMigrationData.getBusinessUnitCode(),
                                    costCenterMigrationData.getCostCenterCode(),
                                    costCenterMigrationData.getNationalCurrencyCreditAccount(),
                                    costCenterMigrationData.getAmount(),
                                    costCenterMigrationData.getNationalCurrencyCreditAccount().getCurrency(),
                                    getExchangeAmountByCashAccount(costCenterMigrationData.getNationalCurrencyCreditAccount(), mainExchangeRateAmount)));
                        }
                        voucherService.create(compensationPrevisionVoucher);
                        relatedTransactionList.add(new AccountingRecordRelatedTransaction(AccountingRecordRelatedTransactionType.COMPENSATION_PREVISION, compensationPrevisionVoucher.getTransactionNumber()));
                    }
                } else {
                    relatedTransactionForPayments = mainTransactionNumber;
                }

                addBankPaymentTypeAccountMovement(resultMapForNationalBAPT, cashAccountMap, balanceExchangeRateAccount,
                        balanceExchangeRateCostCenter, nationalAmountForBankDataMap, exchangeRateAmountMap, voucherGlossForPayment,
                        relatedTransactionForPayments, relatedTransactionList, companyConfiguration);
                addCheckPaymentTypeMovement(resultMapForNationalCPT, cashAccountMap, balanceExchangeRateAccount,
                        balanceExchangeRateCostCenter, nationalAmountForCheckDataMap, exchangeRateAmountMap, voucherGlossForPayment,
                        relatedTransactionForPayments, relatedTransactionList, accountingRecord.getBusinessUnit(), companyConfiguration);
                addBankPaymentTypeAccountMovement(resultMapForForeignBAPT, cashAccountMap, balanceExchangeRateAccount,
                        balanceExchangeRateCostCenter, foreignAmountForBankDataMap, exchangeRateAmountMap, voucherGlossForPayment,
                        relatedTransactionForPayments, relatedTransactionList, companyConfiguration);
                addCheckPaymentTypeMovement(resultMapForForeignCPT, cashAccountMap, balanceExchangeRateAccount,
                        balanceExchangeRateCostCenter, foreignAmountForCheckDataMap, exchangeRateAmountMap, voucherGlossForPayment,
                        relatedTransactionForPayments, relatedTransactionList, accountingRecord.getBusinessUnit(), companyConfiguration);

                accountingRecord.setRecordDate(new Date());
                em.persist(accountingRecord);
                em.flush();

                if (!ValidatorUtil.isEmptyOrNull(relatedTransactionList)) {
                    for (AccountingRecordRelatedTransaction relatedTransaction : relatedTransactionList) {
                        relatedTransaction.setAccountingRecord(accountingRecord);
                        em.persist(relatedTransaction);
                    }
                }

                if (!ValidatorUtil.isEmptyOrNull(managersPayrollList)) {
                    for (ManagersPayroll managersPayroll : managersPayrollList) {
                        managersPayroll.setHasAccountingRecord(true);
                        em.merge(managersPayroll);
                        FinancesBankAccount bankAccount = selectedBankAccountMap.get(managersPayroll.getId());
                        em.persist(new AccountingRecordDetail(bankAccount.getCompanyNumber(), bankAccount.getAccountNumber(), managersPayroll, accountingRecord));
                    }
                } else if (!ValidatorUtil.isEmptyOrNull(generalPayrollList)) {
                    for (GeneralPayroll generalPayroll : generalPayrollList) {
                        generalPayroll.setHasAccountingRecord(true);
                        em.merge(generalPayroll);
                        FinancesBankAccount bankAccount = selectedBankAccountMap.get(generalPayroll.getId());
                        em.persist(new AccountingRecordDetail(bankAccount.getCompanyNumber(), bankAccount.getAccountNumber(), generalPayroll, accountingRecord));
                    }
                } else if (!ValidatorUtil.isEmptyOrNull(christmasPayrollList)) {
                    for (ChristmasPayroll christmasPayroll : christmasPayrollList) {
                        christmasPayroll.setHasAccountingRecord(true);
                        em.merge(christmasPayroll);
                        FinancesBankAccount bankAccount = selectedBankAccountMap.get(christmasPayroll.getId());
                        em.persist(new AccountingRecordDetail(bankAccount.getCompanyNumber(), bankAccount.getAccountNumber(), christmasPayroll, accountingRecord));
                    }
                } else if (!ValidatorUtil.isEmptyOrNull(fiscalProfessorPayrollList)) {
                    for (FiscalProfessorPayroll fiscalProfessorPayroll : fiscalProfessorPayrollList) {
                        fiscalProfessorPayroll.setHasAccountingRecord(true);
                        em.merge(fiscalProfessorPayroll);
                        FinancesBankAccount bankAccount = selectedBankAccountMap.get(fiscalProfessorPayroll.getId());
                        em.persist(new AccountingRecordDetail(bankAccount.getCompanyNumber(), bankAccount.getAccountNumber(), fiscalProfessorPayroll, accountingRecord));
                    }
                }
                em.flush();
                userTransaction.commit();
                userTransaction.setTransactionTimeout(0);
            } catch (Exception e) {
                log.error("Unexpected error ", e);
                try {
                    userTransaction.rollback();
                } catch (SystemException ignored) {
                }
                accountingRecordResult = AccountingRecordResult.FAIL;
            }
        }


        return accountingRecordResult;
    }

    private BigDecimal getCurrentTotalIncomeValue(BigDecimal totalIncome) {
        return BigDecimalUtil.multiply(totalIncome, Constants.PREVISION_PROPORTION);
    }

    private void addAmount(Map<String, CostCenterMigrationData> resultMap, Long payrollGenerationId, BusinessUnit businessUnit, CostCenter costCenter, JobCategory jobCategory, BigDecimal amount, BigDecimal exchangeRate, CostCenterMigrationData.CostCenterMigrationDataType migrationDataType) {
        CostCenterMigrationData costCenterMigrationData = new CostCenterMigrationData(payrollGenerationId, businessUnit, costCenter, jobCategory, exchangeRate, migrationDataType);
        if (!resultMap.containsKey(costCenterMigrationData.getKeyCode())) {
            costCenterMigrationData.setAmount(amount);
            resultMap.put(costCenterMigrationData.getKeyCode(), costCenterMigrationData);
        } else {
            resultMap.get(costCenterMigrationData.getKeyCode()).addAmount(6, amount);
        }
    }

    private void addAmount(Map<String, SalaryMovementMigrationData> resultMap, BusinessUnit businessUnit, CostCenter costCenter, JobCategory jobCategory, List<SalaryMovement> salaryMovementList, BigDecimal exchangeRate) {
        if (!ValidatorUtil.isEmptyOrNull(salaryMovementList)) {
            for (SalaryMovement salaryMovement : salaryMovementList) {
                FinancesCurrencyType currency = getFinancesCurrencyType(salaryMovement.getCurrency());
                SalaryMovementMigrationData salaryMovementMigrationData = new SalaryMovementMigrationData(businessUnit, costCenter, jobCategory, currency, FinancesCurrencyType.P.equals(currency) ? BigDecimal.ONE : exchangeRate);
                if (!resultMap.containsKey(salaryMovementMigrationData.getKeyCode())) {
                    salaryMovementMigrationData.addAmount(salaryMovement.getSalaryMovementType(), salaryMovement.getAmount());
                    resultMap.put(salaryMovementMigrationData.getKeyCode(), salaryMovementMigrationData);
                } else {
                    resultMap.get(salaryMovementMigrationData.getKeyCode()).addAmount(salaryMovement.getSalaryMovementType(), salaryMovement.getAmount());
                }
            }
        }
    }

    private void addAmount(Map<String, Map<String, BankAccountPaymentTypeMigrationData>> resultMap, Long payrollGenerationId, BusinessUnit businessUnit, CostCenter costCenter, JobCategory jobCategory, String bankAccountCode, BigDecimal amount, FinancesCurrencyType financesCurrencyType, Boolean salaryPayroll) {
        Map<String, BankAccountPaymentTypeMigrationData> dataMap = resultMap.get(bankAccountCode);
        if (dataMap == null) {
            dataMap = new HashMap<String, BankAccountPaymentTypeMigrationData>();
        }

        BankAccountPaymentTypeMigrationData bankAccountPaymentTypeMigrationData = new BankAccountPaymentTypeMigrationData(payrollGenerationId, businessUnit, costCenter, jobCategory, bankAccountCode, financesCurrencyType, salaryPayroll);
        if (!dataMap.containsKey(bankAccountPaymentTypeMigrationData.getKeyCode())) {
            bankAccountPaymentTypeMigrationData.setAmount(amount);
            dataMap.put(bankAccountPaymentTypeMigrationData.getKeyCode(), bankAccountPaymentTypeMigrationData);
        } else {
            dataMap.get(bankAccountPaymentTypeMigrationData.getKeyCode()).addAmount(amount);
        }

        resultMap.put(bankAccountCode, dataMap);
    }

    private void addAmount(Map<String, Map<String, CheckPaymentTypeMigrationData>> resultMap, Long payrollGenerationId, BusinessUnit businessUnit, CostCenter costCenter, JobCategory jobCategory, Employee employee, String bankAccountCode, BigDecimal amount, FinancesCurrencyType financesCurrencyType, Boolean salaryPayroll) {
        Map<String, CheckPaymentTypeMigrationData> dataMap = resultMap.get(bankAccountCode);
        if (dataMap == null) {
            dataMap = new HashMap<String, CheckPaymentTypeMigrationData>();
        }

        CheckPaymentTypeMigrationData checkPaymentTypeMigrationData = new CheckPaymentTypeMigrationData(payrollGenerationId, businessUnit, costCenter, jobCategory, employee, bankAccountCode, financesCurrencyType, salaryPayroll);
        if (!resultMap.containsKey(checkPaymentTypeMigrationData.getKeyCode())) {
            checkPaymentTypeMigrationData.setAmount(amount);
            dataMap.put(checkPaymentTypeMigrationData.getKeyCode(), checkPaymentTypeMigrationData);
        } else {
            dataMap.get(checkPaymentTypeMigrationData.getKeyCode()).addAmount(amount);
        }
        resultMap.put(bankAccountCode, dataMap);
    }

    private void addBankPaymentTypeAccountMovement(Map<String, Map<String, BankAccountPaymentTypeMigrationData>> resultMap,
                                                   Map<String, CashAccount> cashAccountMap,
                                                   CashAccount balanceExchangeRateAccount,
                                                   CostCenter balanceExchangeRateCostCenter,
                                                   AccountingRecordMap<String, AccountingRecordData> accountingRecordMap,
                                                   ObservableMap<String, BigDecimal> exchangeRateAmountMap,
                                                   String description,
                                                   String relatedTransactionNumber,
                                                   List<AccountingRecordRelatedTransaction> relatedTransactionList, CompanyConfiguration companyConfiguration) throws Exception {
        for (Map.Entry<String, Map<String, BankAccountPaymentTypeMigrationData>> mapEntry : resultMap.entrySet()) {
            String bankAccountCode = mapEntry.getKey();
            AccountingRecordData accountingRecordData = accountingRecordMap.get(bankAccountCode);
            BigDecimal exchangeRateAmount = BigDecimalUtil.toBigDecimal(exchangeRateAmountMap.get(bankAccountCode));
            Map<String, BankAccountPaymentTypeMigrationData> dataMap = mapEntry.getValue();

            if (!accountingRecordData.isEmpty() && !ValidatorUtil.isEmptyOrNull(dataMap)) {
                Long sequenceNumber = sequenceGeneratorService.forceNextValue(Constants.PAYROLL_DOCUMENT_SEQUENCE);
                FinancesBankAccount financesBankAccount = financesBankAccountService.findFinancesBankAccount(new FinancesBankAccountPk(Constants.defaultCompanyNumber, bankAccountCode));
                Boolean isBankForeignCurrency = FinancesCurrencyType.D.equals(financesBankAccount.getCurrency());

                BigDecimal voucherAmount = financesBankAccount.getCurrency().equals(accountingRecordData.getCurrency()) ? accountingRecordData.getAmount() : accountingRecordData.getEquivalentAmount();
                BigDecimal voucherAmountExchangeAmount = isBankForeignCurrency ? exchangeRateAmount : BigDecimal.ONE;

                Voucher voucher = VoucherBuilder.newBankAccountPaymentTypeVoucher(
                        Constants.BANKACCOUNT_VOUCHERTYPE_FORM,
                        Constants.BANKACCOUNT_VOUCHERTYPE_DEBITNOTE_DOCTYPE,
                        Constants.PAYROLL_DOCNUMBER_PREFFIX + sequenceNumber,
                        bankAccountCode, voucherAmount,
                        financesBankAccount.getCurrency(),
                        voucherAmountExchangeAmount,
                        description,
                        relatedTransactionNumber);
                voucher.setUserNumber(companyConfiguration.getDefaultTreasuryUser().getId());

                BigDecimal totalAmount = BigDecimal.ZERO;
                String lastBusinessUnitCode = null;
                for (BankAccountPaymentTypeMigrationData data : dataMap.values()) {
                    Boolean isDataForeignCurrency = FinancesCurrencyType.D.equals(data.getCurrency());

//                    BigDecimal detailAmount = !isBankForeignCurrency && !isDataForeignCurrency ? data.getAmount() :
//                            isBankForeignCurrency && !isDataForeignCurrency ? BigDecimalUtil.divide(data.getAmount(), exchangeRateAmount) :
//                                    BigDecimalUtil.multiply(data.getAmount(), exchangeRateAmount);

                    BigDecimal detailAmount = isDataForeignCurrency ? BigDecimalUtil.multiply(data.getAmount(), exchangeRateAmount) : data.getAmount();

                    BigDecimal detailAmountExchangeAmount = isDataForeignCurrency ? exchangeRateAmount : BigDecimal.ONE;
                    String cashAccountCode = isDataForeignCurrency ? data.getForeignCurrencyDebitAccountCode() : data.getNationalCurrencyDebitAccountCode();
                    CashAccount cashAccount = getCashAccount(cashAccountMap, cashAccountCode);

                    voucher.addVoucherDetail(VoucherDetailBuilder.newDebitVoucherDetail(lastBusinessUnitCode = data.getBusinessUnitCode(), data.getCostCenterCode(), cashAccount, detailAmount, cashAccount.getCurrency(), detailAmountExchangeAmount));
                    totalAmount = BigDecimalUtil.sum(totalAmount, detailAmount);

                }

//                if (bankAccountAmount.compareTo(totalAmount) != 0) {
//                    if (FinancesCurrencyType.D.equals(financesBankAccount.getCurrency())) {
//                        BigDecimal balanceAmount = BigDecimalUtil.subtract(totalAmount, bankAccountAmount.multiply(exchangeRateAmount));
//                        if (balanceAmount.doubleValue() > 0) {
//                            voucher.addVoucherDetail(VoucherDetailBuilder.newCreditVoucherDetail(lastBusinessUnitCode, lastCostCenterCode, balanceExchangeRateAccount, balanceAmount, FinancesCurrencyType.P, BigDecimal.ONE));
//                        } else if (balanceAmount.doubleValue() < 0) {
//                            voucher.addVoucherDetail(VoucherDetailBuilder.newDebitVoucherDetail(lastBusinessUnitCode, lastCostCenterCode, balanceExchangeRateAccount, balanceAmount, FinancesCurrencyType.P, BigDecimal.ONE));
//                        }
//                    }
//                }

                voucherAmount = BigDecimalUtil.roundBigDecimal(voucherAmount);
                BigDecimal voucherAmountNationalAmount = isBankForeignCurrency ? BigDecimalUtil.multiply(voucherAmount, exchangeRateAmount) : voucherAmount;
                BigDecimal balanceAmount = BigDecimalUtil.subtract(totalAmount, voucherAmountNationalAmount);

                if (balanceAmount.compareTo(BigDecimal.ZERO) > 0) {
                    voucher.addVoucherDetail(VoucherDetailBuilder.newCreditVoucherDetail(lastBusinessUnitCode, balanceExchangeRateCostCenter.getCode(), balanceExchangeRateAccount, balanceAmount, FinancesCurrencyType.P, BigDecimal.ONE));
                } else if (balanceAmount.compareTo(BigDecimal.ZERO) < 0) {
                    voucher.addVoucherDetail(VoucherDetailBuilder.newDebitVoucherDetail(lastBusinessUnitCode, balanceExchangeRateCostCenter.getCode(), balanceExchangeRateAccount, balanceAmount.abs(), FinancesCurrencyType.P, BigDecimal.ONE));
                }

                voucherService.create(voucher);
                relatedTransactionList.add(new AccountingRecordRelatedTransaction(AccountingRecordRelatedTransactionType.BANK_PAYMENT, voucher.getTransactionNumber()));
            }
        }
    }

    private void addCheckPaymentTypeMovement(Map<String, Map<String, CheckPaymentTypeMigrationData>> resultMap,
                                             Map<String, CashAccount> cashAccountMap,
                                             CashAccount balanceExchangeRateAccount,
                                             CostCenter balanceExchangeRateCostCenter, AccountingRecordMap<String, AccountingRecordData> accountingRecordMap,
                                             ObservableMap<String, BigDecimal> exchangeRateAmountMap,
                                             String description,
                                             String relatedTransactionNumber,
                                             List<AccountingRecordRelatedTransaction> relatedTransactionList,
                                             BusinessUnit businessUnit, CompanyConfiguration companyConfiguration) throws Exception {

        for (Map.Entry<String, Map<String, CheckPaymentTypeMigrationData>> mapEntry : resultMap.entrySet()) {
            String bankAccountCode = mapEntry.getKey();
            AccountingRecordData accountingRecordData = accountingRecordMap.get(bankAccountCode);
            BigDecimal exchangeRateAmount = BigDecimalUtil.toBigDecimal(exchangeRateAmountMap.get(bankAccountCode));
            Map<String, CheckPaymentTypeMigrationData> dataMap = mapEntry.getValue();

            if (!accountingRecordData.isEmpty() && !ValidatorUtil.isEmptyOrNull(dataMap)) {
                FinancesBankAccount financesBankAccount = financesBankAccountService.findFinancesBankAccount(new FinancesBankAccountPk(Constants.defaultCompanyNumber, bankAccountCode));
                for (CheckPaymentTypeMigrationData data : dataMap.values()) {
                    Boolean isBankForeignCurrency = FinancesCurrencyType.D.equals(financesBankAccount.getCurrency());
                    Boolean isDataForeignCurrency = FinancesCurrencyType.D.equals(data.getCurrency());

                    BigDecimal voucherAmount = financesBankAccount.getCurrency().equals(data.getCurrency()) ? data.getAmount() :
                            isBankForeignCurrency && !isDataForeignCurrency ? BigDecimalUtil.divide(data.getAmount(), exchangeRateAmount) :
                                    BigDecimalUtil.multiply(data.getAmount(), exchangeRateAmount);
//                    BigDecimal detailAmount = !isBankForeignCurrency && !isDataForeignCurrency ? data.getAmount() :
//                            isBankForeignCurrency && !isDataForeignCurrency ? BigDecimalUtil.divide(data.getAmount(), exchangeRateAmount) :
//                                    BigDecimalUtil.multiply(data.getAmount(), exchangeRateAmount);

                    BigDecimal detailAmount = isDataForeignCurrency ? BigDecimalUtil.multiply(data.getAmount(), exchangeRateAmount) : data.getAmount();

                    BigDecimal voucherAmountExchangeAmount = isBankForeignCurrency ? exchangeRateAmount : BigDecimal.ONE;
                    BigDecimal detailAmountExchangeAmount = isDataForeignCurrency ? exchangeRateAmount : BigDecimal.ONE;

                    String cashAccountCode = isDataForeignCurrency ? data.getForeignCurrencyDebitAccountCode() : data.getNationalCurrencyDebitAccountCode();

                    Voucher voucher = VoucherBuilder.newCheckPaymentTypeVoucher(Constants.CHECK_VOUCHERTYPE_FORM,
                            Constants.CHECK_VOUCHERTYPE_DOCTYPE,
                            bankAccountCode,
                            data.getEmployeeName(),
                            voucherAmount,
                            financesBankAccount.getCurrency(),
                            voucherAmountExchangeAmount,
                            description,
                            businessUnit,
                            relatedTransactionNumber);
                    voucher.setUserNumber(companyConfiguration.getDefaultTreasuryUser().getId());
                    CashAccount cashAccount = getCashAccount(cashAccountMap, cashAccountCode);
                    voucher.addVoucherDetail(VoucherDetailBuilder.newDebitVoucherDetail(data.getBusinessUnitCode(), data.getCostCenterCode(), cashAccount, detailAmount, cashAccount.getCurrency(), detailAmountExchangeAmount));

                    voucherAmount = BigDecimalUtil.roundBigDecimal(voucherAmount);
                    BigDecimal voucherAmountNationalAmount = isBankForeignCurrency ? BigDecimalUtil.multiply(voucherAmount, exchangeRateAmount) : voucherAmount;
                    BigDecimal balanceAmount = BigDecimalUtil.subtract(detailAmount, voucherAmountNationalAmount);
                    if (balanceAmount.compareTo(BigDecimal.ZERO) > 0) {
                        voucher.addVoucherDetail(VoucherDetailBuilder.newCreditVoucherDetail(data.getBusinessUnitCode(), balanceExchangeRateCostCenter.getCode(), balanceExchangeRateAccount, balanceAmount, FinancesCurrencyType.P, BigDecimal.ONE));
                    } else if (balanceAmount.compareTo(BigDecimal.ZERO) < 0) {
                        voucher.addVoucherDetail(VoucherDetailBuilder.newDebitVoucherDetail(data.getBusinessUnitCode(), balanceExchangeRateCostCenter.getCode(), balanceExchangeRateAccount, balanceAmount.abs(), FinancesCurrencyType.P, BigDecimal.ONE));
                    }

                    voucherService.create(voucher);
                    relatedTransactionList.add(new AccountingRecordRelatedTransaction(AccountingRecordRelatedTransactionType.CHECK_PAYMENT, voucher.getTransactionNumber()));
                }
            }
        }
    }

    private CashAccount getCashAccount(Map<String, CashAccount> cashAccountMap, String cashAccountCode) {
        CashAccount cashAccount = cashAccountMap.get(cashAccountCode);
        if (cashAccount == null) {
            cashAccount = cashAccountService.findByAccountCode(cashAccountCode);
            cashAccountMap.put(cashAccountCode, cashAccount);
        }
        return cashAccount;
    }

    private FinancesCurrencyType getFinancesCurrencyType(Currency currency) {
        return Constants.currencyIdBs.equals(currency.getId()) ? FinancesCurrencyType.P : FinancesCurrencyType.D;
    }

    private BigDecimal getExchangeAmountByCashAccount(CashAccount cashAccount, BigDecimal mainExchangeRateAmount) {
        return FinancesCurrencyType.D.equals(cashAccount.getCurrency()) ? mainExchangeRateAmount : BigDecimal.ONE;
    }
}
