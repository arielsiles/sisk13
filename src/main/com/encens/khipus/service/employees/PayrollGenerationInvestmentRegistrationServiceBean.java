package com.encens.khipus.service.employees;

import com.encens.khipus.exception.EntryDuplicatedException;
import com.encens.khipus.exception.EntryNotFoundException;
import com.encens.khipus.exception.finances.FinancesCurrencyNotFoundException;
import com.encens.khipus.exception.finances.FinancesExchangeRateNotFoundException;
import com.encens.khipus.framework.service.GenericServiceBean;
import com.encens.khipus.model.common.Text;
import com.encens.khipus.model.employees.*;
import com.encens.khipus.model.finances.CashAccount;
import com.encens.khipus.model.finances.FinancesCurrencyType;
import com.encens.khipus.model.finances.PayableDocumentType;
import com.encens.khipus.model.finances.Voucher;
import com.encens.khipus.service.finances.FinancesExchangeRateService;
import com.encens.khipus.service.finances.PayableDocumentService;
import com.encens.khipus.service.finances.VoucherService;
import com.encens.khipus.util.*;
import com.encens.khipus.util.employees.TributaryPayrollCalculateResult;
import com.encens.khipus.util.finances.PayableDocumentSourceType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import javax.ejb.Stateless;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author
 * @version 3.5
 */
@Name("payrollGenerationInvestmentRegistrationService")
@AutoCreate
@Stateless
public class PayrollGenerationInvestmentRegistrationServiceBean extends GenericServiceBean implements PayrollGenerationInvestmentRegistrationService {

    @In
    private VoucherService voucherService;
    @In
    private CurrencyService currencyService;
    @In
    private PayableDocumentService payableDocumentService;
    @In
    private TributaryPayrollService tributaryPayrollService;
    @In
    private FinancesExchangeRateService financesExchangeRateService;

    public void createInvestmentRegistrations(PayrollGenerationCycle payrollGenerationCycle,
                                              Map<Long, BigDecimal> socialWelfareEntityGeneralValues,
                                              PayableDocumentType payableDocumentType) throws EntryDuplicatedException, EntryNotFoundException {
        Currency currency = currencyService.findBaseCurrency();
        BigDecimal exchangeRate = getLastExchangeRateByForeignCurrency();

        for (Map.Entry<Long, BigDecimal> entry : socialWelfareEntityGeneralValues.entrySet()) {
            if (BigDecimalUtil.isPositive(entry.getValue())) {
                SocialWelfareEntity socialWelfareEntity = findById(SocialWelfareEntity.class, entry.getKey());
                String gloss = generateInvestmentRegistrationGloss(payrollGenerationCycle, socialWelfareEntity);
                BigDecimal amount = entry.getValue();

                PayrollGenerationInvestmentRegistration investmentRegistration = new PayrollGenerationInvestmentRegistration();
                investmentRegistration.setSocialWelfareEntity(socialWelfareEntity);
                investmentRegistration.setAmount(amount);
                investmentRegistration.setCurrency(currency);
                investmentRegistration.setDocumentType(payableDocumentType);
                investmentRegistration.setDescription(new Text(gloss));
                investmentRegistration.setPayrollGenerationCycle(payrollGenerationCycle);

                investmentRegistration.setTransactionNumber(
                        createPayableDocument(payrollGenerationCycle, socialWelfareEntity, amount, payableDocumentType, gloss, exchangeRate)
                );

                super.create(investmentRegistration);
            }
        }
    }

    private String createPayableDocument(PayrollGenerationCycle payrollGenerationCycle,
                                         SocialWelfareEntity socialWelfareEntity,
                                         BigDecimal amount,
                                         PayableDocumentType payableDocumentType,
                                         String investmentRegistrationGloss,
                                         BigDecimal exchangeRate) {
        String payableDocumentNumber = payableDocumentService.nextPayableDocumentNumberForVoucher(PayableDocumentSourceType.HHRR);
        Voucher payableDocumentVoucher = VoucherBuilder.newPayableDocumentVoucher(
                payableDocumentNumber,
                socialWelfareEntity.getProvider(),
                socialWelfareEntity.getProvider().getEntity(),
                socialWelfareEntity.getProvider().getPayableAccount(),
                payableDocumentType,
                amount,
                socialWelfareEntity.getProvider().getPayableAccount().getCurrency(),
                getExchangeAmountByCashAccount(socialWelfareEntity.getProvider().getPayableAccount(), exchangeRate),
                investmentRegistrationGloss);
        if (SocialWelfareEntityType.SOCIAL_SECURITY.equals(socialWelfareEntity.getType())) {
            addDebitDetailSocialSecurity(payrollGenerationCycle, socialWelfareEntity, payableDocumentVoucher, exchangeRate);
        } else if (SocialWelfareEntityType.PENSION_FUND.equals(socialWelfareEntity.getType())) {
            addDebitDetailForPensionFund(payrollGenerationCycle, socialWelfareEntity, payableDocumentVoucher, exchangeRate);
        }

        voucherService.create(payableDocumentVoucher);
        return payableDocumentVoucher.getTransactionNumber();
    }


    private void addDebitDetailForPensionFund(PayrollGenerationCycle payrollGenerationCycle,
                                              SocialWelfareEntity socialWelfareEntity,
                                              Voucher voucher,
                                              BigDecimal exchangeRate) {

        List<TributaryPayrollCalculateResult> patronalPensionFundValues = tributaryPayrollService.sumPatronalPensionFundRetentionGroupingByCostCenter(payrollGenerationCycle, socialWelfareEntity);

        for (TributaryPayrollCalculateResult calculateResult : patronalPensionFundValues) {
            voucher.addVoucherDetail(VoucherDetailBuilder.newDebitVoucherDetail(
                    calculateResult.getBusinessUnit().getExecutorUnitCode(),
                    calculateResult.getCostCenter().getCode(),
                    calculateResult.getJobCategory().getPensionFundPatronalAccount(),
                    calculateResult.getAmount(),
                    calculateResult.getJobCategory().getPensionFundPatronalAccount().getCurrency(),
                    getExchangeAmountByCashAccount(calculateResult.getJobCategory().getPensionFundPatronalAccount(), exchangeRate)));
        }

        List<TributaryPayrollCalculateResult> laboralPensionFundValues = tributaryPayrollService.sumLaboralPensionFundRetentionGroupingByCostCenter(payrollGenerationCycle, socialWelfareEntity);
        for (TributaryPayrollCalculateResult calculateResult : laboralPensionFundValues) {
            voucher.addVoucherDetail(VoucherDetailBuilder.newDebitVoucherDetail(
                    calculateResult.getBusinessUnit().getExecutorUnitCode(),
                    calculateResult.getCostCenter().getCode(),
                    calculateResult.getJobCategory().getNationalCurrencyDebitAccount(),
                    calculateResult.getAmount(),
                    calculateResult.getJobCategory().getNationalCurrencyDebitAccount().getCurrency(),
                    getExchangeAmountByCashAccount(calculateResult.getJobCategory().getNationalCurrencyDebitAccount(), exchangeRate)));
        }
    }

    private void addDebitDetailSocialSecurity(PayrollGenerationCycle payrollGenerationCycle,
                                              SocialWelfareEntity socialWelfareEntity,
                                              Voucher voucher,
                                              BigDecimal exchangeRate) {
        List<TributaryPayrollCalculateResult> socialSecurityRetentionValues = tributaryPayrollService.sumSocialSecurityRetentionGroupingByCostCenter(payrollGenerationCycle, socialWelfareEntity);
        for (TributaryPayrollCalculateResult calculateResult : socialSecurityRetentionValues) {
            voucher.addVoucherDetail(VoucherDetailBuilder.newDebitVoucherDetail(
                    calculateResult.getBusinessUnit().getExecutorUnitCode(),
                    calculateResult.getCostCenter().getCode(),
                    calculateResult.getJobCategory().getSocialSecurityPatronalAccount(),
                    calculateResult.getAmount(),
                    calculateResult.getJobCategory().getSocialSecurityPatronalAccount().getCurrency(),
                    getExchangeAmountByCashAccount(calculateResult.getJobCategory().getSocialSecurityPatronalAccount(), exchangeRate)));
        }
    }

    private BigDecimal getLastExchangeRateByForeignCurrency() {
        BigDecimal exchangeRate = null;
        try {
            exchangeRate = financesExchangeRateService.findLastExchangeRateByCurrency(FinancesCurrencyType.D.name());
        } catch (FinancesCurrencyNotFoundException ignored) {
        } catch (FinancesExchangeRateNotFoundException ignored) {
        }
        return exchangeRate != null ? exchangeRate : BigDecimal.ONE;
    }

    private BigDecimal getExchangeAmountByCashAccount(CashAccount cashAccount, BigDecimal exchangeRate) {
        return FinancesCurrencyType.D.equals(cashAccount.getCurrency()) ? exchangeRate : BigDecimal.ONE;
    }

    private String generateInvestmentRegistrationGloss(PayrollGenerationCycle payrollGenerationCycle, SocialWelfareEntity socialWelfareEntity) {

        String socialWelfareEntityTypeName = MessageUtils.getMessage(socialWelfareEntity.getType().getResourceKey());
        String socialWelfareEntityName = socialWelfareEntity.getName();
        String businessUnitName = payrollGenerationCycle.getBusinessUnit().getOrganization().getName();
        String monthString = MessageUtils.getMessage(payrollGenerationCycle.getMonth().getResourceKey());
        String yearString = String.valueOf(payrollGenerationCycle.getGestion().getYear()).replace(".", "");
        String dateString = DateUtils.format(new Date(), MessageUtils.getMessage("patterns.dateTime"));

        return MessageUtils.getMessage("PayrollGenerationInvestmentRegistration.error.investmentRegistrationGloss", socialWelfareEntityName, socialWelfareEntityTypeName, businessUnitName, monthString, yearString, dateString);

    }
}
