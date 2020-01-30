package com.encens.khipus.action.customers;

import com.encens.khipus.exception.finances.CompanyConfigurationNotFoundException;
import com.encens.khipus.framework.action.GenericAction;
import com.encens.khipus.model.customers.Credit;
import com.encens.khipus.model.customers.CreditState;
import com.encens.khipus.model.customers.CreditType;
import com.encens.khipus.model.finances.CashAccount;
import com.encens.khipus.model.finances.CompanyConfiguration;
import com.encens.khipus.model.finances.Voucher;
import com.encens.khipus.model.finances.VoucherDetail;
import com.encens.khipus.service.accouting.VoucherAccoutingService;
import com.encens.khipus.service.customers.CreditService;
import com.encens.khipus.service.customers.CreditTransactionService;
import com.encens.khipus.service.fixedassets.CompanyConfigurationService;
import com.encens.khipus.util.BigDecimalUtil;
import com.encens.khipus.util.Constants;
import com.encens.khipus.util.DateUtils;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * Actions for Customer category
 *
 * @author:
 */

@Name("provisionInterestReceivableAction")
@Scope(ScopeType.CONVERSATION)
public class ProvisionInterestReceivableAction extends GenericAction {

    private CreditState creditState;
    private CreditType creditType;
    private Date endPeriodDate;
    private CashAccount cashAccount;

    @In(create = true)
    private CreditTransactionAction creditTransactionAction;

    @In
    private CreditService creditService;
    @In
    private CreditTransactionService creditTransactionService;
    @In
    private VoucherAccoutingService voucherAccoutingService;
    @In
    CompanyConfigurationService companyConfigurationService;

    public void generateProvisionInterest() throws CompanyConfigurationNotFoundException{

        CompanyConfiguration companyConfiguration = companyConfigurationService.findCompanyConfiguration();
        List<Credit> creditList = creditService.getCredits(this.creditState, this.creditType);

        BigDecimal totalAccruedInterest = BigDecimal.ZERO; //Interes acumulado
        for (Credit credit : creditList){

            BigDecimal value = creditTransactionAction.calculateAccruedInterest(credit, this.endPeriodDate);
            totalAccruedInterest = BigDecimalUtil.sum(totalAccruedInterest, value);
            System.out.println("----------> " + credit.getPartner().getFullName() + " - " + credit.getPreviousCode() + " : " + value + " - Total: " + totalAccruedInterest);
        }

        System.out.println("==========> Total: " + totalAccruedInterest);
        Double balance = voucherAccoutingService.getBalance(this.endPeriodDate, cashAccount.getAccountCode());
        System.out.println("==========> Total MAYOR: " + balance);
        BigDecimal interestProvision = BigDecimalUtil.subtract(totalAccruedInterest, BigDecimalUtil.toBigDecimal(balance));
        System.out.println("==========> PROVISION: " + interestProvision);

        VoucherDetail currentCreditProductsNationalCurrency = new VoucherDetail();
        currentCreditProductsNationalCurrency.setCashAccount(this.cashAccount);
        currentCreditProductsNationalCurrency.setAccount(this.cashAccount.getAccountCode());

        VoucherDetail fixedTermInterestNationalCurrency = new VoucherDetail();
        fixedTermInterestNationalCurrency.setCashAccount(companyConfiguration.getFixedTermInterestNationalCurrency());
        fixedTermInterestNationalCurrency.setAccount(companyConfiguration.getFixedTermInterestNationalCurrency().getAccountCode());

        if (interestProvision.doubleValue() > 0){
            currentCreditProductsNationalCurrency.setDebit(interestProvision);
            currentCreditProductsNationalCurrency.setCredit(BigDecimal.ZERO);

            fixedTermInterestNationalCurrency.setDebit(BigDecimal.ZERO);
            fixedTermInterestNationalCurrency.setCredit(interestProvision);
        }

        if (interestProvision.doubleValue() < 0){
            interestProvision = BigDecimalUtil.toBigDecimal(Math.abs(interestProvision.doubleValue()));
            currentCreditProductsNationalCurrency.setDebit(BigDecimal.ZERO);
            currentCreditProductsNationalCurrency.setCredit(interestProvision);

            fixedTermInterestNationalCurrency.setDebit(interestProvision);
            fixedTermInterestNationalCurrency.setCredit(BigDecimal.ZERO);
        }

        Voucher voucher = new Voucher();
        voucher.setDocumentType(Constants.CD_VOUCHER_DOCTYPE);
        voucher.setDate(this.endPeriodDate);
        voucher.setGloss("PROVISION DE INTERESES POR COBRAR S/CARTERA VIGENTE " + DateUtils.format(this.endPeriodDate, "dd/MM/yyyy"));

        voucher.getDetails().add(currentCreditProductsNationalCurrency);
        voucher.getDetails().add(fixedTermInterestNationalCurrency);
        voucherAccoutingService.saveVoucher(voucher);
    }


    public CreditState getCreditState() {
        return creditState;
    }

    public void setCreditState(CreditState creditState) {
        this.creditState = creditState;
    }

    public CreditType getCreditType() {
        return creditType;
    }

    public void setCreditType(CreditType creditType) {
        this.creditType = creditType;
    }

    public Date getEndPeriodDate() {
        return endPeriodDate;
    }

    public void setEndPeriodDate(Date endPeriodDate) {
        this.endPeriodDate = endPeriodDate;
    }

    public CashAccount getCashAccount() {
        return cashAccount;
    }

    public void setCashAccount(CashAccount cashAccount) {
        this.cashAccount = cashAccount;
    }
}
