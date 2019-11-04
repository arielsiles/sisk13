package com.encens.khipus.action.customers;

import com.encens.khipus.exception.finances.CompanyConfigurationNotFoundException;
import com.encens.khipus.framework.action.GenericAction;
import com.encens.khipus.model.customers.Account;
import com.encens.khipus.model.customers.SavingType;
import com.encens.khipus.model.finances.CashAccount;
import com.encens.khipus.model.finances.FinancesCurrencyType;
import com.encens.khipus.service.accouting.VoucherAccoutingService;
import com.encens.khipus.service.customers.AccountService;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import java.util.Date;
import java.util.List;

/**
 * Actions for Customer category
 *
 * @author:
 */

@Name("provisionInterestPayableAction")
@Scope(ScopeType.CONVERSATION)
public class ProvisionInterestPayableAction extends GenericAction {

    /*private CreditState creditState;
    private CreditType creditType;*/
    private Date endPeriodDate;
    private CashAccount cashAccount;

    private SavingType savingType;
    private FinancesCurrencyType currencyType;

    @In
    private VoucherAccoutingService voucherAccoutingService;
    @In
    private AccountService accountService;

    /*@In(create = true)
    private CreditTransactionAction creditTransactionAction;*/
    /*@In
    private CreditService creditService;
    @In
    private CreditTransactionService creditTransactionService;*/

    /*@In
    CompanyConfigurationService companyConfigurationService;*/

    public void generateProvisionInterest() throws CompanyConfigurationNotFoundException{

        List<Account> accountList = accountService.getSavingsAccounts(this.savingType, this.currencyType);

        for (Account account : accountList){
            System.out.println("-----------------> " + account.getPartner() + " - " + account.getFullAccountName());
        }

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

    public FinancesCurrencyType getCurrencyType() {
        return currencyType;
    }

    public void setCurrencyType(FinancesCurrencyType currencyType) {
        this.currencyType = currencyType;
    }

    public SavingType getSavingType() {
        return savingType;
    }

    public void setSavingType(SavingType savingType) {
        this.savingType = savingType;
    }
}
