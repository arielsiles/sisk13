package com.encens.khipus.action.customers;

import com.encens.khipus.framework.action.GenericAction;
import com.encens.khipus.framework.action.Outcome;
import com.encens.khipus.model.customers.*;
import com.encens.khipus.model.finances.FinancesCurrencyType;
import com.encens.khipus.model.finances.VoucherDetail;
import com.encens.khipus.service.customers.AccountService;
import com.encens.khipus.util.BigDecimalUtil;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Actions for Credit
 *
 * @author:
 */

@Name("accountAction")
@Scope(ScopeType.CONVERSATION)
public class AccountAction extends GenericAction<Account> {


    @In
    private AccountService accountService;

    private List<AccountTransaction> accountTransactionList = new ArrayList<AccountTransaction>();

    private BigDecimal totalCredit  = BigDecimal.ZERO;
    private BigDecimal totalDebit   = BigDecimal.ZERO;
    private BigDecimal totalBalance = BigDecimal.ZERO;

    private BigDecimal totalCreditMe  = BigDecimal.ZERO;
    private BigDecimal totalDebitMe   = BigDecimal.ZERO;
    private BigDecimal totalBalanceMe = BigDecimal.ZERO;


    @Factory(value = "account", scope = ScopeType.STATELESS)
    public Account initAccount() {
        return getInstance();
    }

    @Factory(value = "currencyTypeEnum", scope = ScopeType.STATELESS)
    public FinancesCurrencyType[] getFinancesCurrencyTypeEnum() {
        return FinancesCurrencyType.values();
    }

    @Factory(value = "accountStateEnum", scope = ScopeType.STATELESS)
    public AccountState[] getAccountStateEnum() {
        return AccountState.values();
    }

    @Override
    @Begin(join=true, ifOutcome = Outcome.SUCCESS, flushMode = FlushModeType.MANUAL)
    public String select(Account instance) {
        calculateTotalAmounts(instance);
        return super.select(instance);

    }

    @Begin(nested = true, flushMode = FlushModeType.MANUAL)
    public String addPayment() {
        //setOp(OP_UPDATE);
        //return creditTransactionAction.addCreditTransaction();
        return Outcome.SUCCESS;
    }

    public List<VoucherDetail> getAccountDetailList(Account account){

        List<VoucherDetail> voucherDetails = accountService.getAccountDetailList(account);

        return voucherDetails;
    }

    public void calculateTotalAmounts(Account account){
        List<VoucherDetail> voucherDetails = accountService.getAccountDetailList(account);

        for (VoucherDetail voucherDetail : voucherDetails){
            setTotalCredit(BigDecimalUtil.sum(getTotalCredit(), voucherDetail.getCredit(), 2));
            setTotalDebit(BigDecimalUtil.sum(getTotalDebit(), voucherDetail.getDebit(), 2));

            setTotalCreditMe(BigDecimalUtil.sum(getTotalCreditMe(), voucherDetail.getCreditMe(), 2));
            setTotalDebitMe(BigDecimalUtil.sum(getTotalDebitMe(), voucherDetail.getDebitMe(), 2));
        }
        setTotalBalance(BigDecimalUtil.subtract(getTotalCredit(), getTotalDebit(), 2));
        setTotalBalanceMe(BigDecimalUtil.subtract(getTotalCreditMe(), getTotalDebitMe(), 2));
    }

    public void assignPartner(Partner partner){
        getInstance().setPartner(partner);
    }

    public void clearPartner(){
        getInstance().setPartner(null);
    }

    public List<AccountTransaction> getAccountTransactionList() {
        return accountTransactionList;
    }

    public boolean isForeignAccount(){

        boolean result = false;
        if (getInstance().getCurrency() != null){
            if (getInstance().getCurrency().equals(FinancesCurrencyType.D) || getInstance().getCurrency().equals(FinancesCurrencyType.M))
                result = true;
        }

        return result;
    }

    public void setAccountTransactionList(List<AccountTransaction> accountTransactionList) {
        this.accountTransactionList = accountTransactionList;
    }

    public BigDecimal getTotalCredit() {
        return totalCredit;
    }

    public void setTotalCredit(BigDecimal totalCredit) {
        this.totalCredit = totalCredit;
    }

    public BigDecimal getTotalDebit() {
        return totalDebit;
    }

    public void setTotalDebit(BigDecimal totalDebit) {
        this.totalDebit = totalDebit;
    }

    public BigDecimal getTotalBalance() {
        return totalBalance;
    }

    public void setTotalBalance(BigDecimal totalBalance) {
        this.totalBalance = totalBalance;
    }

    public BigDecimal getTotalCreditMe() {
        return totalCreditMe;
    }

    public void setTotalCreditMe(BigDecimal totalCreditMe) {
        this.totalCreditMe = totalCreditMe;
    }

    public BigDecimal getTotalDebitMe() {
        return totalDebitMe;
    }

    public void setTotalDebitMe(BigDecimal totalDebitMe) {
        this.totalDebitMe = totalDebitMe;
    }

    public BigDecimal getTotalBalanceMe() {
        return totalBalanceMe;
    }

    public void setTotalBalanceMe(BigDecimal totalBalanceMe) {
        this.totalBalanceMe = totalBalanceMe;
    }
}
