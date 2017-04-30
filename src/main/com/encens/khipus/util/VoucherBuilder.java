package com.encens.khipus.util;

import com.encens.khipus.model.admin.BusinessUnit;
import com.encens.khipus.model.finances.*;

import java.math.BigDecimal;
import java.util.Date;

/**
 * VoucherBuilder
 *
 * @author
 * @version 3.5.2.2
 */
public final class VoucherBuilder {

    private VoucherBuilder() {
    }

    public static final String defaultVoucherSource = "E";

    public static Voucher newGeneralVoucher(String form, String gloss) {
        Voucher voucher = new Voucher(form, gloss);
        voucher.setDescription(gloss);
        return voucher;
    }

    public static Voucher newGeneralVoucher(String form,
                                            String gloss,
                                            String relatedTransactionNumber) {
        Voucher voucher = newGeneralVoucher(form, gloss);
        voucher.setRelatedTransactionNumber(relatedTransactionNumber);
        return voucher;
    }

    public static Voucher newBankAccountPaymentTypeVoucher(String form,
                                                           String documentType,
                                                           String documentNumber,
                                                           String bankAccountCode,
                                                           BigDecimal amount,
                                                           FinancesCurrencyType currency,
                                                           BigDecimal exchangeRateAmount,
                                                           String gloss) {
        Voucher voucher = newGeneralVoucher(form, gloss);
        voucher.setDocumentType(documentType);
        voucher.setDocumentNumber(documentNumber);
        voucher.setBankAccountCode(bankAccountCode);
        voucher.setSource(defaultVoucherSource);
        voucher.setAmount(amount);
        voucher.setCurrency(currency);
        voucher.setExchangeRateAmount(exchangeRateAmount);
        voucher.setDescription(gloss);
        return voucher;
    }

    public static Voucher newBankAccountPaymentTypeVoucher(String form,
                                                           String documentType,
                                                           String documentNumber,
                                                           String bankAccountCode,
                                                           BigDecimal amount,
                                                           FinancesCurrencyType currency,
                                                           BigDecimal exchangeRateAmount,
                                                           String gloss,
                                                           String relatedTransactionNumber) {
        Voucher voucher = newBankAccountPaymentTypeVoucher(form, documentType, documentNumber, bankAccountCode, amount, currency, exchangeRateAmount, gloss);
        voucher.setRelatedTransactionNumber(relatedTransactionNumber);
        return voucher;
    }

    public static Voucher newCheckPaymentTypeVoucher(String form,
                                                     String documentType,
                                                     String bankAccountCode,
                                                     String employeeName,
                                                     BigDecimal amount,
                                                     FinancesCurrencyType currency,
                                                     BigDecimal exchangeRateAmount,
                                                     BusinessUnit checkDestinationBusinessUnit,
                                                     String gloss) {
        Voucher voucher = newGeneralVoucher(form, gloss);
        voucher.setDocumentType(documentType);
        voucher.setBankAccountCode(bankAccountCode);
        voucher.setEmployeeName(employeeName);
        voucher.setSource(defaultVoucherSource);
        voucher.setAmount(amount);
        voucher.setCurrency(currency);
        voucher.setExchangeRateAmount(exchangeRateAmount);
        voucher.setDescription(gloss);
        voucher.setCheckDestinationExecutorUnitCode(checkDestinationBusinessUnit.getExecutorUnitCode());
        return voucher;
    }

    public static Voucher newCheckPaymentTypeVoucher(String form,
                                                     String documentType,
                                                     String bankAccountCode,
                                                     String employeeName,
                                                     BigDecimal amount,
                                                     FinancesCurrencyType currency,
                                                     BigDecimal exchangeRateAmount,
                                                     String gloss,
                                                     BusinessUnit checkDestinationBusinessUnit,
                                                     String relatedTransactionNumber) {
        Voucher voucher = newCheckPaymentTypeVoucher(form, documentType, bankAccountCode, employeeName, amount, currency, exchangeRateAmount, checkDestinationBusinessUnit, gloss);
        voucher.setRelatedTransactionNumber(relatedTransactionNumber);
        return voucher;
    }

    public static Voucher newPayableDocumentVoucher(String documentNumber,
                                                    Provider provider,
                                                    FinancesEntity financesEntity,
                                                    CashAccount payableAccount,
                                                    PayableDocumentType documentType,
                                                    BigDecimal amount,
                                                    FinancesCurrencyType currency,
                                                    BigDecimal exchangeRateAmount,
                                                    String gloss) {
        Voucher voucher = newGeneralVoucher(Constants.PAYABLES_VOUCHER_FORM, gloss);
        voucher.setDocumentNumber(documentNumber);
        voucher.setProvider(provider);
        voucher.setFinancesEntity(financesEntity);
        voucher.setDocumentType(documentType.getDocumentType());
        voucher.setCashAccount(payableAccount);
        voucher.setExpirationDate(DateUtils.lastDate(new Date()));
        voucher.setAmount(amount);
        voucher.setCurrency(currency);
        voucher.setExchangeRateAmount(exchangeRateAmount);
        voucher.setDescription(gloss);
        return voucher;
    }
}
