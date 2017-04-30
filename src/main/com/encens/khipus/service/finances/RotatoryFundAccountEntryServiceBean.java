package com.encens.khipus.service.finances;

import com.encens.khipus.exception.finances.CompanyConfigurationNotFoundException;
import com.encens.khipus.framework.service.GenericServiceBean;
import com.encens.khipus.model.employees.JobCategory;
import com.encens.khipus.model.employees.PayrollGenerationType;
import com.encens.khipus.model.finances.*;
import com.encens.khipus.service.common.SequenceGeneratorService;
import com.encens.khipus.service.fixedassets.CompanyConfigurationService;
import com.encens.khipus.util.BigDecimalUtil;
import com.encens.khipus.util.Constants;
import com.encens.khipus.util.VoucherBuilder;
import com.encens.khipus.util.VoucherDetailBuilder;
import com.encens.khipus.util.employees.RotatoryFundMigrationData;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import java.math.BigDecimal;
import java.util.List;

/**
 * Service for account entries for RotatoryFunds
 *
 * @author
 * @version 3.5.2.2
 */
@Stateless
@Name("rotatoryFundAccountEntryService")
@AutoCreate
public class RotatoryFundAccountEntryServiceBean extends GenericServiceBean implements RotatoryFundAccountEntryService {
    @In(value = "#{listEntityManager}")
    private EntityManager listEm;

    @In
    private CompanyConfigurationService companyConfigurationService;

    @In
    private SequenceGeneratorService sequenceGeneratorService;

    @In
    private VoucherService voucherService;

    @In
    private RotatoryFundCollectionSpendDistributionService rotatoryFundCollectionSpendDistributionService;

    @In
    private FinanceDocumentService financeDocumentService;

    /*register the corresponding account entry for a rotatory fund collection */

    public void createRotatoryFundCollectionAccountVsBankAccountEntry(String executorUnitCode,
                                                                      String costCenterCode,
                                                                      CashAccount cashAccount,
                                                                      BigDecimal amount,
                                                                      FinancesCurrencyType currency,
                                                                      RotatoryFundCollection rotatoryFundCollection)
            throws CompanyConfigurationNotFoundException {
        Voucher voucher = null;
        CompanyConfiguration companyConfiguration = companyConfigurationService.findCompanyConfiguration();
        BigDecimal bankExchangeRate = FinancesCurrencyType.D.equals(rotatoryFundCollection.getBankAccount().getCurrency()) ?
                rotatoryFundCollection.getExchangeRate() : BigDecimal.ONE;

        BigDecimal payExchangeRate = FinancesCurrencyType.D.equals(cashAccount.getCurrency()) ?
                rotatoryFundCollection.getExchangeRate() : BigDecimal.ONE;

        /* if the payment currency is in $us so convert to equivalent in bs */

        BigDecimal bankAmount = FinancesCurrencyType.D.equals(rotatoryFundCollection.getBankAccount().getCurrency()) ?
                BigDecimalUtil.multiply(rotatoryFundCollection.getSourceAmount(), rotatoryFundCollection.getExchangeRate()) : rotatoryFundCollection.getSourceAmount();

        BigDecimal payAmount = FinancesCurrencyType.D.equals(rotatoryFundCollection.getCollectionCurrency()) ?
                BigDecimalUtil.multiply(rotatoryFundCollection.getCollectionAmount(), rotatoryFundCollection.getExchangeRate(), 6) : rotatoryFundCollection.getCollectionAmount();

        if (RotatoryFundCollectionType.COLLECTION_BANK_ACCOUNT.equals(rotatoryFundCollection.getRotatoryFundCollectionType())) {
            voucher = VoucherBuilder.newBankAccountPaymentTypeVoucher(
                    Constants.BANKACCOUNT_VOUCHERTYPE_FORM,
                    Constants.BANKACCOUNT_VOUCHERTYPE_DEPOSIT_DOCTYPE,
                    rotatoryFundCollection.getBankDepositNumber(),
                    rotatoryFundCollection.getBankAccountNumber(),
                    rotatoryFundCollection.getSourceAmount(),
                    rotatoryFundCollection.getBankAccount().getCurrency(),
                    bankExchangeRate,
                    rotatoryFundCollection.getDescription());
        }
        /*else if (RotatoryFundCollectionType.COLLECTION_WITH_CHECK.equals(rotatoryFundCollection.getRotatoryFundCollectionType())) {
            voucher = VoucherBuilder.newCheckPaymentTypeVoucher(
                    Constants.CHECK_VOUCHERTYPE_FORM,
                    Constants.CHECK_VOUCHERTYPE_DOCTYPE,
                    rotatoryFundCollection.getBankAccountNumber(),
                    rotatoryFundCollection.getBeneficiaryName(),
                    rotatoryFundCollection.getSourceAmount(),
                    rotatoryFundCollection.getSourceCurrency(),
                    bankExchangeRate,
                    rotatoryFundCollection.getDescription());
        }*/

        if (voucher != null) {
            voucher.addVoucherDetail(VoucherDetailBuilder.newCreditVoucherDetail(
                    executorUnitCode,
                    costCenterCode,
                    cashAccount,
                    payAmount,
                    cashAccount.getCurrency(),
                    payExchangeRate));
            BigDecimal balanceAmount = BigDecimalUtil.subtract(bankAmount, payAmount);
            if (balanceAmount.compareTo(BigDecimal.ZERO) > 0) {
                voucher.addVoucherDetail(VoucherDetailBuilder.newCreditVoucherDetail(
                        executorUnitCode,
                        companyConfiguration.getExchangeRateBalanceCostCenter().getCode(),
                        companyConfiguration.getBalanceExchangeRateAccount(),
                        balanceAmount,
                        FinancesCurrencyType.P,
                        BigDecimal.ONE));
            } else if (balanceAmount.compareTo(BigDecimal.ZERO) < 0) {
                voucher.addVoucherDetail(VoucherDetailBuilder.newDebitVoucherDetail(
                        executorUnitCode,
                        companyConfiguration.getExchangeRateBalanceCostCenter().getCode(),
                        companyConfiguration.getBalanceExchangeRateAccount(),
                        balanceAmount.abs(),
                        FinancesCurrencyType.P,
                        BigDecimal.ONE));
            }
            voucherService.create(voucher);
            rotatoryFundCollection.setTransactionNumber(voucher.getTransactionNumber());
        }
    }

    /* Register the corresponding account entry for a rotatory fund collection corresponding
    * to a receivable fund discharge by a document */

    public void createRotatoryFundSpendDistributedCollectionAccountEntry(RotatoryFundCollection rotatoryFundCollection, CashAccount cashAccount)
            throws CompanyConfigurationNotFoundException {
        CompanyConfiguration companyConfiguration = companyConfigurationService.findCompanyConfiguration();
        CollectionDocument collectionDocument = rotatoryFundCollection.getCollectionDocument();
        Voucher voucher = VoucherBuilder.newGeneralVoucher(Constants.RECEIVABLES_VOUCHER_FORM, rotatoryFundCollection.getDescription());

        if (rotatoryFundCollection.getReceiver() != null) {
            voucher.setReceiver(rotatoryFundCollection.getReceiver().getFullName());
        }

        voucher.setObservation(rotatoryFundCollection.getObservation());

        /* Distribute the spend*/
        List<RotatoryFundCollectionSpendDistribution> rotatoryFundCollectionSpendDistributionList = rotatoryFundCollectionSpendDistributionService.getRotatoryFundCollectionSpendDistributionList(rotatoryFundCollection);
        BigDecimal totalNationalAmount = FinancesCurrencyType.D.equals(rotatoryFundCollection.getSourceCurrency())
                ? BigDecimalUtil.multiply(rotatoryFundCollection.getSourceAmount(), rotatoryFundCollection.getExchangeRate())
                : rotatoryFundCollection.getSourceAmount();

        BigDecimal distributable = BigDecimalUtil.subtract(totalNationalAmount, collectionDocument.getIva());
        BigDecimal portionSum = BigDecimal.ZERO;
        for (int i = 0; i < rotatoryFundCollectionSpendDistributionList.size(); i++) {
            RotatoryFundCollectionSpendDistribution rotatoryFundCollectionSpendDistribution = rotatoryFundCollectionSpendDistributionList.get(i);
            BigDecimal portion = FinancesCurrencyType.D.equals(rotatoryFundCollection.getSourceCurrency())
                    ? BigDecimalUtil.multiply(rotatoryFundCollectionSpendDistribution.getAmount(), rotatoryFundCollection.getExchangeRate())
                    : rotatoryFundCollectionSpendDistribution.getAmount();

            if (collectionDocument.getCollectionDocumentType().equals(CollectionDocumentType.INVOICE)) {
                portion = BigDecimalUtil.multiply(portion, Constants.VAT_COMPLEMENT);
            }

            portionSum = BigDecimalUtil.sum(portionSum, portion);

            BigDecimal accountRate = FinancesCurrencyType.D.equals(rotatoryFundCollectionSpendDistribution.getCashAccount().getCurrency()) ?
                    rotatoryFundCollection.getExchangeRate() : Constants.BASE_CURRENCY_EXCHANGE_RATE;

            voucher.addVoucherDetail(
                    VoucherDetailBuilder.newDebitVoucherDetail(
                            rotatoryFundCollectionSpendDistribution.getBusinessUnit().getExecutorUnitCode(),
                            rotatoryFundCollectionSpendDistribution.getCostCenterCode(),
                            rotatoryFundCollectionSpendDistribution.getCashAccount(),
                            portion,
                            rotatoryFundCollectionSpendDistribution.getCashAccount().getCurrency(),
                            accountRate)
            );
        }
        /*VAT (IVA) entry*/
        /*TODO check to wich account fire the VAT info nm or me?*/
        if (collectionDocument.getCollectionDocumentType().equals(CollectionDocumentType.INVOICE)) {
            BigDecimal balance = BigDecimalUtil.subtract(portionSum, distributable);

            if (!BigDecimalUtil.isZeroOrNull(balance)) {
                int lastIndex = voucher.getDetails().size() - 1;
                VoucherDetail voucherDetail = voucher.getDetails().get(lastIndex);
                voucherDetail.setDebit(
                        BigDecimalUtil.isPositive(balance) ?
                                BigDecimalUtil.subtract(voucherDetail.getDebit(), balance) :
                                BigDecimalUtil.sum(voucherDetail.getDebit(), balance.abs())
                );
                voucher.getDetails().set(lastIndex, voucherDetail);
            }

            voucher.addVoucherDetail(
                    VoucherDetailBuilder.newDebitVoucherDetail(
                            null,
                            null,
                            companyConfiguration.getNationalCurrencyVATFiscalCreditAccount(),
                            collectionDocument.getIva(),
                            FinancesCurrencyType.P,
                            Constants.BASE_CURRENCY_EXCHANGE_RATE)
            );
        }

        BigDecimal cashAccountRate = FinancesCurrencyType.D.equals(cashAccount.getCurrency()) ?
                rotatoryFundCollection.getExchangeRate() : Constants.BASE_CURRENCY_EXCHANGE_RATE;

        voucher.addVoucherDetail(
                VoucherDetailBuilder.newCreditVoucherDetail(
                        rotatoryFundCollection.getRotatoryFund().getBusinessUnit().getExecutorUnitCode(),
                        rotatoryFundCollection.getRotatoryFund().getCostCenter().getCode(),
                        cashAccount,
                        totalNationalAmount,
                        cashAccount.getCurrency(),
                        cashAccountRate)
        );

        voucherService.create(voucher);
        rotatoryFundCollection.setTransactionNumber(voucher.getTransactionNumber());
        collectionDocument.setTransactionNumber(voucher.getTransactionNumber());
    }
    /*
    * Register the corresponding account entry for a rotatory fund collection corresponding
    * to a receivable fund discharge by a Payroll 
    **/

    public void createRotatoryFundCollectionByPayrollAccountEntry(RotatoryFundCollection rotatoryFundCollection, CashAccount cashAccount, FinancesCurrencyType defaultCurrency, RotatoryFundMigrationData rotatoryFundMigrationData)
            throws CompanyConfigurationNotFoundException {


        JobCategory jobCategory = rotatoryFundCollection.getRotatoryFund().getJobContract().getJob().getJobCategory();

        BigDecimal totalNationalAmount =
                FinancesCurrencyType.D.equals(rotatoryFundCollection.getCollectionCurrency())
                        ? BigDecimalUtil.multiply(rotatoryFundCollection.getCollectionAmount(), rotatoryFundCollection.getExchangeRate())
                        : rotatoryFundCollection.getCollectionAmount();

        if (rotatoryFundCollection.getGestionPayroll() != null) {
            defaultCurrency = PayrollGenerationType.GENERATION_BY_TIME.equals(jobCategory.getPayrollGenerationType()) ?
                    FinancesCurrencyType.D : FinancesCurrencyType.P;
        }
        CashAccount jobCategoryAccount = FinancesCurrencyType.D.equals(defaultCurrency) ?
                jobCategory.getForeignCurrencyDebitAccount() : jobCategory.getNationalCurrencyDebitAccount();
        BigDecimal accountRate = FinancesCurrencyType.D.equals(defaultCurrency) ?
                rotatoryFundCollection.getExchangeRate() : Constants.BASE_CURRENCY_EXCHANGE_RATE;
        BigDecimal cashAccountRate = FinancesCurrencyType.D.equals(cashAccount.getCurrency()) ?
                rotatoryFundCollection.getExchangeRate() : Constants.BASE_CURRENCY_EXCHANGE_RATE;

        if (rotatoryFundMigrationData == null) {
            Voucher voucher = VoucherBuilder.newGeneralVoucher(Constants.RECEIVABLES_VOUCHER_FORM, rotatoryFundCollection.getDescription());
            voucher.addVoucherDetail(
                    VoucherDetailBuilder.newDebitVoucherDetail(
                            rotatoryFundCollection.getRotatoryFund().getBusinessUnit().getExecutorUnitCode(),
                            rotatoryFundCollection.getRotatoryFund().getCostCenterCode(),
                            jobCategoryAccount,
                            totalNationalAmount,
                            jobCategoryAccount.getCurrency(),
                            accountRate));
            voucher.addVoucherDetail(
                    VoucherDetailBuilder.newCreditVoucherDetail(
                            rotatoryFundCollection.getRotatoryFund().getBusinessUnit().getExecutorUnitCode(),
                            rotatoryFundCollection.getRotatoryFund().getCostCenterCode(),
                            cashAccount,
                            totalNationalAmount,
                            cashAccount.getCurrency(),
                            cashAccountRate));

            voucherService.create(voucher);
            rotatoryFundCollection.setTransactionNumber(voucher.getTransactionNumber());
        } else {
            rotatoryFundMigrationData.addDebit(
                    rotatoryFundCollection.getRotatoryFund().getBusinessUnit().getExecutorUnitCode(),
                    rotatoryFundCollection.getRotatoryFund().getCostCenterCode(),
                    jobCategoryAccount,
                    totalNationalAmount,
                    jobCategoryAccount.getCurrency(),
                    accountRate);
            rotatoryFundMigrationData.addCredit(
                    rotatoryFundCollection.getRotatoryFund().getBusinessUnit().getExecutorUnitCode(),
                    rotatoryFundCollection.getRotatoryFund().getCostCenterCode(),
                    cashAccount,
                    totalNationalAmount,
                    cashAccount.getCurrency(),
                    cashAccountRate);
            rotatoryFundCollection.setTransactionNumber(rotatoryFundMigrationData.getTransactionNumber());
        }
    }

    public void createRotatoryFundCollectionCashAccountAdjustment(RotatoryFundCollection rotatoryFundCollection, CashAccount cashAccount)
            throws CompanyConfigurationNotFoundException {
        Voucher voucher = VoucherBuilder.newGeneralVoucher(Constants.RECEIVABLES_VOUCHER_FORM, rotatoryFundCollection.getDescription());
        voucher.setObservation(rotatoryFundCollection.getObservation());

        String executorUnitCode = rotatoryFundCollection.getRotatoryFund().getBusinessUnit().getExecutorUnitCode();
        String costCenterCode = rotatoryFundCollection.getRotatoryFund().getCostCenterCode();

        BigDecimal totalNationalAmount =
                FinancesCurrencyType.D.equals(rotatoryFundCollection.getCollectionCurrency())
                        ? BigDecimalUtil.multiply(rotatoryFundCollection.getCollectionAmount(), rotatoryFundCollection.getExchangeRate())
                        : rotatoryFundCollection.getCollectionAmount();

        CashAccount cashAccountAdjustment = rotatoryFundCollection.getCashAccountAdjustment();
        BigDecimal cashAccountAdjustmentRate = FinancesCurrencyType.D.equals(cashAccountAdjustment.getCurrency()) ?
                rotatoryFundCollection.getExchangeRate() : Constants.BASE_CURRENCY_EXCHANGE_RATE;

        voucher.addVoucherDetail(
                VoucherDetailBuilder.newDebitVoucherDetail(
                        executorUnitCode,
                        costCenterCode,
                        cashAccountAdjustment,
                        totalNationalAmount,
                        cashAccountAdjustment.getCurrency(),
                        cashAccountAdjustmentRate));

        BigDecimal cashAccountRate = FinancesCurrencyType.D.equals(cashAccount.getCurrency()) ?
                rotatoryFundCollection.getExchangeRate() : Constants.BASE_CURRENCY_EXCHANGE_RATE;

        voucher.addVoucherDetail(
                VoucherDetailBuilder.newCreditVoucherDetail(
                        executorUnitCode,
                        costCenterCode,
                        cashAccount,
                        totalNationalAmount,
                        cashAccount.getCurrency(),
                        cashAccountRate));

        voucherService.create(voucher);
        rotatoryFundCollection.setTransactionNumber(voucher.getTransactionNumber());
    }

    public void createRotatoryFundCollectionDepositAdjustment(RotatoryFundCollection rotatoryFundCollection, CashAccount cashAccount)
            throws CompanyConfigurationNotFoundException {

        Voucher voucher = VoucherBuilder.newGeneralVoucher(Constants.RECEIVABLES_VOUCHER_FORM, rotatoryFundCollection.getDescription());
        voucher.setObservation(rotatoryFundCollection.getObservation());

        String executorUnitCode = rotatoryFundCollection.getRotatoryFund().getBusinessUnit().getExecutorUnitCode();
        String costCenterCode = rotatoryFundCollection.getRotatoryFund().getCostCenterCode();

        BigDecimal totalNationalAmount =
                FinancesCurrencyType.D.equals(rotatoryFundCollection.getCollectionCurrency())
                        ? BigDecimalUtil.multiply(rotatoryFundCollection.getCollectionAmount(), rotatoryFundCollection.getExchangeRate())
                        : rotatoryFundCollection.getCollectionAmount();

        List<AccountingMovementDetail> accountingMovementDetailList = financeDocumentService.findDetail(
                rotatoryFundCollection.getDepositAdjustment().getTransactionNumber(),
                rotatoryFundCollection.getDepositAdjustment().getAccountingMovement(),
                FinanceMovementType.C);
        BigDecimal totalAmountByDetail = BigDecimal.ZERO;
        for (AccountingMovementDetail accountingMovementDetail : accountingMovementDetailList) {
            BigDecimal detailAmount = accountingMovementDetail.getAmount().abs();
            voucher.addVoucherDetail(
                    VoucherDetailBuilder.newDebitVoucherDetail(
                            accountingMovementDetail.getExecutorUnitCode(),
                            accountingMovementDetail.getCostCenterCode(),
                            accountingMovementDetail.getAccount(),
                            accountingMovementDetail.getAmount().abs(),
                            accountingMovementDetail.getCurrency(),
                            accountingMovementDetail.getExchangeRate()));
            totalAmountByDetail = BigDecimalUtil.sum(totalAmountByDetail, detailAmount);
        }

        voucher.addVoucherDetail(
                VoucherDetailBuilder.newCreditVoucherDetail(
                        executorUnitCode,
                        costCenterCode,
                        cashAccount,
                        totalNationalAmount,
                        cashAccount.getCurrency(),
                        rotatoryFundCollection.getExchangeRate()));


        BigDecimal balanceAmount = BigDecimalUtil.subtract(totalAmountByDetail, totalNationalAmount);
        CompanyConfiguration companyConfiguration = companyConfigurationService.findCompanyConfiguration();
        if (balanceAmount.compareTo(BigDecimal.ZERO) > 0) {
            voucher.addVoucherDetail(VoucherDetailBuilder.newCreditVoucherDetail(
                    executorUnitCode,
                    companyConfiguration.getExchangeRateBalanceCostCenter().getCode(),
                    companyConfiguration.getBalanceExchangeRateAccount(),
                    balanceAmount,
                    FinancesCurrencyType.P,
                    BigDecimal.ONE));
        } else if (balanceAmount.compareTo(BigDecimal.ZERO) < 0) {
            voucher.addVoucherDetail(VoucherDetailBuilder.newDebitVoucherDetail(
                    executorUnitCode,
                    companyConfiguration.getExchangeRateBalanceCostCenter().getCode(),
                    companyConfiguration.getBalanceExchangeRateAccount(),
                    balanceAmount.abs(),
                    FinancesCurrencyType.P,
                    BigDecimal.ONE));
        }

        voucherService.create(voucher);
        rotatoryFundCollection.setTransactionNumber(voucher.getTransactionNumber());
    }

    /* account entries by payments*/
    /*register the corresponding account entry for a rotatory fund payment by bank or check when it is approved*/

    public void createRotatoryFundPaymentAccountVsBankAccountEntry(RotatoryFundPayment rotatoryFundPayment, CashAccount cashAccount)
            throws CompanyConfigurationNotFoundException {
        Voucher voucher = null;
        RotatoryFund rotatoryFund = rotatoryFundPayment.getRotatoryFund();
        CompanyConfiguration companyConfiguration = companyConfigurationService.findCompanyConfiguration();
        BigDecimal bankExchangeRate = FinancesCurrencyType.D.equals(rotatoryFundPayment.getSourceCurrency()) ?
                rotatoryFundPayment.getExchangeRate() : BigDecimal.ONE;
        BigDecimal payExchangeRate = FinancesCurrencyType.D.equals(cashAccount.getCurrency()) ?
                rotatoryFundPayment.getExchangeRate() : BigDecimal.ONE;
        /* if the payment currency is in $us so convert to equivalent in bs */
        BigDecimal payAmount = FinancesCurrencyType.D.equals(rotatoryFundPayment.getPaymentCurrency()) ?
                BigDecimalUtil.multiply(rotatoryFundPayment.getPaymentAmount(), rotatoryFundPayment.getExchangeRate()) : rotatoryFundPayment.getPaymentAmount();
        BigDecimal bankNationalAmount = FinancesCurrencyType.D.equals(rotatoryFundPayment.getSourceCurrency()) ?
                BigDecimalUtil.multiply(rotatoryFundPayment.getSourceAmount(), rotatoryFundPayment.getExchangeRate()) : rotatoryFundPayment.getSourceAmount();

        if (RotatoryFundPaymentType.PAYMENT_BANK_ACCOUNT.equals(rotatoryFundPayment.getRotatoryFundPaymentType())) {
            Long sequenceNumber = sequenceGeneratorService.nextValue(Constants.ROTATORYFUND_PAYMENT_DOCUMENT_SEQUENCE);
            voucher = VoucherBuilder.newBankAccountPaymentTypeVoucher(
                    Constants.BANKACCOUNT_VOUCHERTYPE_FORM,
                    Constants.BANKACCOUNT_VOUCHERTYPE_DEBITNOTE_DOCTYPE,
                    Constants.ROTATORYFUND_PAYMENT_DOCNUMBER_PREFFIX + sequenceNumber,
                    rotatoryFundPayment.getBankAccountNumber(),
                    rotatoryFundPayment.getSourceAmount(),
                    rotatoryFundPayment.getSourceCurrency(),
                    bankExchangeRate,
                    rotatoryFundPayment.getDescription());
        } else if (RotatoryFundPaymentType.PAYMENT_WITH_CHECK.equals(rotatoryFundPayment.getRotatoryFundPaymentType())) {
            voucher = VoucherBuilder.newCheckPaymentTypeVoucher(
                    Constants.CHECK_VOUCHERTYPE_FORM,
                    Constants.CHECK_VOUCHERTYPE_DOCTYPE,
                    rotatoryFundPayment.getBankAccountNumber(),
                    rotatoryFundPayment.getBeneficiaryName(),
                    rotatoryFundPayment.getSourceAmount(),
                    rotatoryFundPayment.getSourceCurrency(),
                    bankExchangeRate,
                    rotatoryFundPayment.getCheckDestination(),
                    rotatoryFundPayment.getDescription());
        }
        if (voucher != null) {
            FinanceUser financeUser = rotatoryFundPayment.getRotatoryFund().getDocumentType().getFinanceUser();
            if (null != financeUser) {
                voucher.setUserNumber(financeUser.getId());
            }
            voucher.addVoucherDetail(VoucherDetailBuilder.newDebitVoucherDetail(
                    rotatoryFund.getBusinessUnit().getExecutorUnitCode(),
                    rotatoryFund.getCostCenterCode(),
                    cashAccount,
                    payAmount,
                    cashAccount.getCurrency(),
                    payExchangeRate));

            BigDecimal balanceAmount = BigDecimalUtil.subtract(payAmount, bankNationalAmount);
            if (balanceAmount.compareTo(BigDecimal.ZERO) > 0) {
                voucher.addVoucherDetail(VoucherDetailBuilder.newCreditVoucherDetail(
                        rotatoryFund.getBusinessUnit().getExecutorUnitCode(),
                        companyConfiguration.getExchangeRateBalanceCostCenter().getCode(),
                        companyConfiguration.getBalanceExchangeRateAccount(),
                        balanceAmount,
                        FinancesCurrencyType.P,
                        BigDecimal.ONE));
            } else if (balanceAmount.compareTo(BigDecimal.ZERO) < 0) {
                voucher.addVoucherDetail(VoucherDetailBuilder.newDebitVoucherDetail(
                        rotatoryFund.getBusinessUnit().getExecutorUnitCode(),
                        companyConfiguration.getExchangeRateBalanceCostCenter().getCode(),
                        companyConfiguration.getBalanceExchangeRateAccount(),
                        balanceAmount.abs(),
                        FinancesCurrencyType.P,
                        BigDecimal.ONE));
            }
            voucherService.create(voucher);
            rotatoryFundPayment.setTransactionNumber(voucher.getTransactionNumber());
        }
    }

    /*register the corresponding account entry for a rotatory fund payment by cash box when it is approved*/

    public void createRotatoryFundPaymentAccountVsCashBoxEntry(RotatoryFundPayment rotatoryFundPayment, CashAccount cashAccount)
            throws CompanyConfigurationNotFoundException {
        RotatoryFund rotatoryFund = rotatoryFundPayment.getRotatoryFund();
        CompanyConfiguration companyConfiguration = companyConfigurationService.findCompanyConfiguration();
        BigDecimal cashBoxExchangeRate = FinancesCurrencyType.D.equals(rotatoryFundPayment.getSourceCurrency()) ?
                rotatoryFundPayment.getExchangeRate() : BigDecimal.ONE;
        BigDecimal payExchangeRate = FinancesCurrencyType.D.equals(cashAccount.getCurrency()) ?
                rotatoryFundPayment.getExchangeRate() : BigDecimal.ONE;
        /* if the payment currency is in $us so convert to equivalent in bs */
        BigDecimal payAmount = FinancesCurrencyType.D.equals(rotatoryFundPayment.getPaymentCurrency()) ?
                BigDecimalUtil.multiply(rotatoryFundPayment.getPaymentAmount(), rotatoryFundPayment.getExchangeRate()) : rotatoryFundPayment.getPaymentAmount();
        BigDecimal cashBoxNationalAmount = FinancesCurrencyType.D.equals(rotatoryFundPayment.getSourceCurrency()) ?
                BigDecimalUtil.multiply(rotatoryFundPayment.getSourceAmount(), rotatoryFundPayment.getExchangeRate()) : rotatoryFundPayment.getSourceAmount();

        Voucher voucher = VoucherBuilder.newGeneralVoucher(Constants.CASHBOX_PAYMENT_VOUCHER_FORM, rotatoryFundPayment.getDescription());
        voucher.setEmployeeName(rotatoryFundPayment.getBeneficiaryName());
        FinanceUser financeUser = rotatoryFundPayment.getRotatoryFund().getDocumentType().getFinanceUser();
        if (null != financeUser) {
            voucher.setUserNumber(financeUser.getId());
        }
        voucher.addVoucherDetail(VoucherDetailBuilder.newCreditVoucherDetail(
                rotatoryFund.getBusinessUnit().getExecutorUnitCode(),
                rotatoryFund.getCostCenterCode(),
                rotatoryFundPayment.getCashBoxCashAccount(),
                cashBoxNationalAmount,
                rotatoryFundPayment.getSourceCurrency(),
                cashBoxExchangeRate));

        voucher.addVoucherDetail(VoucherDetailBuilder.newDebitVoucherDetail(
                rotatoryFund.getBusinessUnit().getExecutorUnitCode(),
                rotatoryFund.getCostCenterCode(),
                cashAccount,
                payAmount,
                cashAccount.getCurrency(),
                payExchangeRate));

        BigDecimal balanceAmount = BigDecimalUtil.subtract(payAmount, cashBoxNationalAmount);
        if (balanceAmount.compareTo(BigDecimal.ZERO) > 0) {
            voucher.addVoucherDetail(VoucherDetailBuilder.newCreditVoucherDetail(
                    rotatoryFund.getBusinessUnit().getExecutorUnitCode(),
                    companyConfiguration.getExchangeRateBalanceCostCenter().getCode(),
                    companyConfiguration.getBalanceExchangeRateAccount(),
                    balanceAmount,
                    FinancesCurrencyType.P,
                    BigDecimal.ONE));
        } else if (balanceAmount.compareTo(BigDecimal.ZERO) < 0) {
            voucher.addVoucherDetail(VoucherDetailBuilder.newDebitVoucherDetail(
                    rotatoryFund.getBusinessUnit().getExecutorUnitCode(),
                    companyConfiguration.getExchangeRateBalanceCostCenter().getCode(),
                    companyConfiguration.getBalanceExchangeRateAccount(),
                    balanceAmount.abs(),
                    FinancesCurrencyType.P,
                    BigDecimal.ONE));
        }
        voucherService.create(voucher);
        rotatoryFundPayment.setTransactionNumber(voucher.getTransactionNumber());
    }

    public void createRotatoryFundPaymentAccountVsCashAccountAdjustmentEntry(RotatoryFundPayment rotatoryFundPayment, CashAccount cashAccount)
            throws CompanyConfigurationNotFoundException {
        RotatoryFund rotatoryFund = rotatoryFundPayment.getRotatoryFund();
        CompanyConfiguration companyConfiguration = companyConfigurationService.findCompanyConfiguration();
        BigDecimal cashAccountAdjustmentExchangeRate = FinancesCurrencyType.D.equals(rotatoryFundPayment.getSourceCurrency()) ?
                rotatoryFundPayment.getExchangeRate() : BigDecimal.ONE;
        BigDecimal payExchangeRate = FinancesCurrencyType.D.equals(cashAccount.getCurrency()) ?
                rotatoryFundPayment.getExchangeRate() : BigDecimal.ONE;
        /* if the payment currency is in $us so convert to equivalent in bs */
        BigDecimal payAmount = FinancesCurrencyType.D.equals(rotatoryFundPayment.getPaymentCurrency()) ?
                BigDecimalUtil.multiply(rotatoryFundPayment.getPaymentAmount(), rotatoryFundPayment.getExchangeRate()) : rotatoryFundPayment.getPaymentAmount();
        BigDecimal cashAccountAdjustmentNationalAmount = FinancesCurrencyType.D.equals(rotatoryFundPayment.getSourceCurrency()) ?
                BigDecimalUtil.multiply(rotatoryFundPayment.getSourceAmount(), rotatoryFundPayment.getExchangeRate()) : rotatoryFundPayment.getSourceAmount();

        Voucher voucher = VoucherBuilder.newGeneralVoucher(Constants.RECEIVABLES_VOUCHER_FORM, rotatoryFundPayment.getDescription());
        FinanceUser financeUser = rotatoryFundPayment.getRotatoryFund().getDocumentType().getFinanceUser();
        if (null != financeUser) {
            voucher.setUserNumber(financeUser.getId());
        }
        voucher.addVoucherDetail(VoucherDetailBuilder.newCreditVoucherDetail(
                rotatoryFund.getBusinessUnit().getExecutorUnitCode(),
                rotatoryFund.getCostCenterCode(),
                rotatoryFundPayment.getCashAccountAdjustment(),
                cashAccountAdjustmentNationalAmount,
                rotatoryFundPayment.getSourceCurrency(),
                cashAccountAdjustmentExchangeRate));

        voucher.addVoucherDetail(VoucherDetailBuilder.newDebitVoucherDetail(
                rotatoryFund.getBusinessUnit().getExecutorUnitCode(),
                rotatoryFund.getCostCenterCode(),
                cashAccount,
                payAmount,
                cashAccount.getCurrency(),
                payExchangeRate));

        log.info("******************************************************");
        log.info("rotatoryFundPayment.getPaymentAmount() = " + rotatoryFundPayment.getPaymentAmount());
        log.info("rotatoryFundPayment.getSourceAmount() = " + rotatoryFundPayment.getSourceAmount());
        log.info("******************************************************");
        log.info("rotatoryFund.getBusinessUnit().getExecutorUnitCode() = " + rotatoryFund.getBusinessUnit().getExecutorUnitCode());
        log.info("rotatoryFund.getCostCenterCode() =" + rotatoryFund.getCostCenterCode());
        log.info("rotatoryFundPayment.getCashAccountAdjustment() =" + rotatoryFundPayment.getCashAccountAdjustment());
        log.info("cashAccountAdjustmentNationalAmount =" + cashAccountAdjustmentNationalAmount);
        log.info("rotatoryFundPayment.getSourceCurrency() =" + rotatoryFundPayment.getSourceCurrency());
        log.info("cashAccountAdjustmentExchangeRate =" + cashAccountAdjustmentExchangeRate);
        log.info("******************************************************");
        log.info("rotatoryFund.getBusinessUnit().getExecutorUnitCode() =" + rotatoryFund.getBusinessUnit().getExecutorUnitCode());
        log.info("rotatoryFund.getCostCenterCode() =" + rotatoryFund.getCostCenterCode());
        log.info("cashAccount =" + cashAccount);
        log.info("payAmount =" + payAmount);
        log.info("cashAccount.getCurrency() =" + cashAccount.getCurrency());
        log.info("payExchangeRate =" + payExchangeRate);
        log.info("******************************************************");


        BigDecimal balanceAmount = BigDecimalUtil.subtract(payAmount, cashAccountAdjustmentNationalAmount);
        if (balanceAmount.compareTo(BigDecimal.ZERO) > 0) {
            voucher.addVoucherDetail(VoucherDetailBuilder.newCreditVoucherDetail(
                    rotatoryFund.getBusinessUnit().getExecutorUnitCode(),
                    companyConfiguration.getExchangeRateBalanceCostCenter().getCode(),
                    companyConfiguration.getBalanceExchangeRateAccount(),
                    balanceAmount,
                    FinancesCurrencyType.P,
                    BigDecimal.ONE));
        } else if (balanceAmount.compareTo(BigDecimal.ZERO) < 0) {
            voucher.addVoucherDetail(VoucherDetailBuilder.newDebitVoucherDetail(
                    rotatoryFund.getBusinessUnit().getExecutorUnitCode(),
                    companyConfiguration.getExchangeRateBalanceCostCenter().getCode(),
                    companyConfiguration.getBalanceExchangeRateAccount(),
                    balanceAmount.abs(),
                    FinancesCurrencyType.P,
                    BigDecimal.ONE));
        }
        voucherService.create(voucher);
        rotatoryFundPayment.setTransactionNumber(voucher.getTransactionNumber());
    }
}