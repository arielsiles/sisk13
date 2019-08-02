package com.encens.khipus.action.customers;

import com.encens.khipus.exception.EntryDuplicatedException;
import com.encens.khipus.exception.finances.FinancesCurrencyNotFoundException;
import com.encens.khipus.exception.finances.FinancesExchangeRateNotFoundException;
import com.encens.khipus.framework.action.GenericAction;
import com.encens.khipus.framework.action.Outcome;
import com.encens.khipus.model.customers.*;
import com.encens.khipus.model.finances.FinancesCurrencyType;
import com.encens.khipus.model.finances.Voucher;
import com.encens.khipus.model.finances.VoucherDetail;
import com.encens.khipus.service.accouting.VoucherAccoutingService;
import com.encens.khipus.service.common.SequenceGeneratorService;
import com.encens.khipus.service.customers.AccountService;
import com.encens.khipus.service.finances.FinancesExchangeRateService;
import com.encens.khipus.util.BigDecimalUtil;
import com.encens.khipus.util.Constants;
import com.encens.khipus.util.DateUtils;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.*;
import org.jboss.seam.international.StatusMessage;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
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
    @In
    private VoucherAccoutingService voucherAccoutingService;
    @In
    private FinancesExchangeRateService financesExchangeRateService;
    @In
    private SequenceGeneratorService sequenceGeneratorService;

    private List<AccountTransaction> accountTransactionList = new ArrayList<AccountTransaction>();

    private BigDecimal totalCredit  = BigDecimal.ZERO;
    private BigDecimal totalDebit   = BigDecimal.ZERO;
    private BigDecimal totalBalance = BigDecimal.ZERO;

    private BigDecimal totalCreditMe  = BigDecimal.ZERO;
    private BigDecimal totalDebitMe   = BigDecimal.ZERO;
    private BigDecimal totalBalanceMe = BigDecimal.ZERO;

    /** For capitalization **/
    private Date startDate;
    private Date endDate;

    private SavingType savingType;

    /** End **/

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

    @Factory(value = "savingTypeEnum", scope = ScopeType.STATELESS)
    public SavingType[] getSavingTypeEnum() {
        return SavingType.values();
    }

    @Override
    @Begin(join=true, ifOutcome = Outcome.SUCCESS, flushMode = FlushModeType.MANUAL)
    public String select(Account instance) {
        calculateTotalAmounts(instance);
        return super.select(instance);

    }

    @End
    @Override
    public String create() {
        try {
            getInstance().setAccountNumber(generateAccountNumber());
            getService().create(getInstance());
            addCreatedMessage();
            return Outcome.SUCCESS;
        } catch (EntryDuplicatedException e) {
            addDuplicatedMessage();
            return Outcome.REDISPLAY;
        }
    }


    public String generateAccountNumber(){
        String result = String.valueOf(sequenceGeneratorService.nextValue(Constants.SAVINGS_ACCOUNT_NUMBER));
        return result;
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

    public BigDecimal getTotalBalanceAccount(Account account){

        BigDecimal result = BigDecimal.ZERO;

        BigDecimal balanceMN = BigDecimal.ZERO;
        BigDecimal balanceME = BigDecimal.ZERO;

        BigDecimal totalDebitMN  = BigDecimal.ZERO;
        BigDecimal totalCreditMN = BigDecimal.ZERO;

        BigDecimal totalDebitME  = BigDecimal.ZERO;
        BigDecimal totalCreditME = BigDecimal.ZERO;

        List<VoucherDetail> voucherDetails = accountService.getAccountDetailList(account);
        for (VoucherDetail voucherDetail : voucherDetails){
            totalCreditMN = BigDecimalUtil.sum(totalCreditMN, voucherDetail.getCredit());
            totalDebitMN  = BigDecimalUtil.sum(totalDebitMN, voucherDetail.getDebit());

            totalCreditME = BigDecimalUtil.sum(totalCreditME, voucherDetail.getCreditMe());
            totalDebitME  = BigDecimalUtil.sum(totalDebitME, voucherDetail.getDebitMe());
        }
        balanceMN = BigDecimalUtil.subtract(totalCreditMN, totalDebitMN);
        balanceME = BigDecimalUtil.subtract(totalCreditME, totalDebitME);

        if (account.getCurrency().equals(FinancesCurrencyType.P))
            result = balanceMN;
        if (account.getCurrency().equals(FinancesCurrencyType.D) || account.getCurrency().equals(FinancesCurrencyType.M))
            result = balanceME;

        return result;
    }

    public String getFullAccountBalance(Account account){
        return account.getFullAccount() + " (" + getTotalBalanceAccount(account) + ")";
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


    public void capitaliationOfInterests(){
        if (this.savingType.equals(SavingType.SOC)){
            capitaliationOfInterestsCAJ(SavingType.SOC);
        }

        if (this.savingType.equals(SavingType.CAJ)){
            capitaliationOfInterestsCAJ(SavingType.CAJ);
        }

        /*if (this.savingType.equals(SavingType.DPF)){

        }*/
    }

    public void capitaliationOfInterestsCAJ(SavingType savingType){

        BigDecimal exchangeRate = BigDecimal.ZERO;
        try {   exchangeRate = financesExchangeRateService.findLastExchangeRateByCurrency(FinancesCurrencyType.D.toString());
        }catch (FinancesExchangeRateNotFoundException e){ addFinancesExchangeRateNotFoundExceptionMessage();
        }catch (FinancesCurrencyNotFoundException e){addFinancesCurrencyNotFoundMessage();}

        Voucher voucher = new Voucher();
        voucher.setDocumentType("CD");
        voucher.setDate(this.endDate);
        voucher.setGloss("Capitalizacion de intereses, Ahorros en cuentas...");


        BigDecimal totalInterestME = BigDecimal.ZERO;
        BigDecimal totalInterestMV = BigDecimal.ZERO;


        /** For MN **/
        //List<Account> accountsMnList = accountService.getSavingsAccounts(SavingType.CAJ, FinancesCurrencyType.P); /** MN **/
        List<Account> accountsMnList = accountService.getSavingsAccounts(savingType); /** MN **/
        BigDecimal totalInterestMN = BigDecimal.ZERO;
        BigDecimal totalIvaTax = BigDecimal.ZERO;

        for (Account account : accountsMnList){
            BigDecimal accountInterest = BigDecimal.ZERO;
            List<VoucherDetail> accountMovements = accountService.getMovementAccountBetweenDates(account, startDate, endDate);
            List<AccountKardex> kardexList       = calculateAccountKardex(accountMovements, account.getCurrency());

            /** change for conditions: with credit, without credit **/
            BigDecimal percentage = account.getAccountType().getInta();

            System.out.println(kardexList.get(0).getDate() + " - " + kardexList.get(0).getDebit() + " - " + kardexList.get(0).getCredit() + " - Dias: 0 - " + kardexList.get(0).getInterest());
            for (int i=1 ; i < kardexList.size() ; i++){

                AccountKardex previous = kardexList.get(i-1);
                AccountKardex current = kardexList.get(i);
                Long days = DateUtils.daysBetween(previous.getDate(), current.getDate()) - 1;

                current.setInterest(calculateInterest(previous.getDate(), current.getDate(), previous.balance, percentage));
                accountInterest = BigDecimalUtil.sum(accountInterest, current.getInterest(), 6);

                System.out.println(current.getDate() + " - " + current.getDebit() + " - " + current.getCredit() + " - Diff: " + days + " - " + current.getInterest());

                // Si ultima transaccion es menor al 31/mm/aaaaa
                if (i == kardexList.size()-1){
                    if (kardexList.get(i).getDate().compareTo(this.endDate) < 0){
                        Long daysZ = DateUtils.daysBetween(kardexList.get(i).getDate(), this.endDate) - 1;
                        BigDecimal endInterest = calculateInterest(kardexList.get(i).getDate(), this.endDate, current.getBalance(), percentage);
                        accountInterest = BigDecimalUtil.sum(accountInterest, endInterest, 6);
                        System.out.println(this.endDate + " - " + current.getDebit() + " - " + current.getCredit() + " - Diff: " + daysZ + " - " + endInterest);
                    }
                }
            }
            BigDecimal ivaTax = BigDecimalUtil.multiply(accountInterest, Constants.VAT, 6);

            String cashAccountCode = "";
            if (account.getCurrency().equals(FinancesCurrencyType.P))
                cashAccountCode = account.getAccountType().getCashAccountMn().getAccountCode();
            if (account.getCurrency().equals(FinancesCurrencyType.D))
                cashAccountCode = account.getAccountType().getCashAccountMe().getAccountCode();
            if (account.getCurrency().equals(FinancesCurrencyType.M))
                cashAccountCode = account.getAccountType().getCashAccountMv().getAccountCode();

            VoucherDetail detailInterest = buildAccountEntryDetail(cashAccountCode, accountInterest, "CREDIT", account.getCurrency());
            VoucherDetail detailIvaTax   = buildAccountEntryDetail(cashAccountCode, ivaTax, "DEBIT", account.getCurrency());

            detailInterest.setPartnerAccount(account);
            detailIvaTax.setPartnerAccount(account);

            voucher.getDetails().add(detailInterest);
            voucher.getDetails().add(detailIvaTax);

            totalInterestMN = BigDecimalUtil.sum(totalInterestMN, accountInterest, 6);
            totalIvaTax     = BigDecimalUtil.sum(totalIvaTax,ivaTax, 6);

            System.out.println("====> TOTAL INTERES: " + accountInterest);
        }

        VoucherDetail detailTotalInterest = buildAccountEntryDetail(Constants.ACOUNT_INTEREST_4110210100, totalInterestMN, "DEBIT", FinancesCurrencyType.P);
        VoucherDetail detailTotalIvaTax   = buildAccountEntryDetail(Constants.ACOUNT_RCIVA_2420310100, totalIvaTax, "CREDIT", FinancesCurrencyType.P);

        voucher.getDetails().add(detailTotalInterest);
        voucher.getDetails().add(detailTotalIvaTax);

        voucherAccoutingService.saveVoucher(voucher);

    }

    public VoucherDetail buildAccountEntryDetail(String cashAccountCode, BigDecimal amount, String flag, FinancesCurrencyType currencyType){

        BigDecimal exchangeRate = BigDecimal.ZERO;
        try {
            exchangeRate = financesExchangeRateService.findLastExchangeRateByCurrency(FinancesCurrencyType.D.toString());
        }catch (FinancesExchangeRateNotFoundException e){
            addFinancesExchangeRateNotFoundExceptionMessage();
        }catch (FinancesCurrencyNotFoundException e){
            addFinancesCurrencyNotFoundMessage();
        }

        VoucherDetail detail = new VoucherDetail();

        if (currencyType.equals(FinancesCurrencyType.P)){
            if (flag.equals("DEBIT")){
                detail.setDebit(amount);
                detail.setCredit(BigDecimal.ZERO);
                detail.setAccount(cashAccountCode);

                detail.setExchangeAmount(BigDecimal.ONE);
                detail.setDebitMe(BigDecimal.ZERO);
                detail.setCreditMe(BigDecimal.ZERO);
            }
            if (flag.equals("CREDIT")){
                detail.setDebit(BigDecimal.ZERO);
                detail.setCredit(amount);
                detail.setAccount(cashAccountCode);

                detail.setExchangeAmount(BigDecimal.ONE);
                detail.setDebitMe(BigDecimal.ZERO);
                detail.setCreditMe(BigDecimal.ZERO);
            }

        }

        if (currencyType.equals(FinancesCurrencyType.D) || currencyType.equals(FinancesCurrencyType.M)){
            if (flag.equals("DEBIT")){
                detail.setDebitMe(amount);
                detail.setCreditMe(BigDecimal.ZERO);
                detail.setAccount(cashAccountCode);

                detail.setExchangeAmount(exchangeRate);
                detail.setDebit(BigDecimalUtil.multiply(detail.getDebitMe(), exchangeRate, 2));
                detail.setCredit(BigDecimal.ZERO);
            }
            if (flag.equals("CREDIT")){
                detail.setDebitMe(BigDecimal.ZERO);
                detail.setCreditMe(amount);
                detail.setAccount(cashAccountCode);

                detail.setExchangeAmount(exchangeRate);
                detail.setDebit(BigDecimal.ZERO);
                detail.setCredit(BigDecimalUtil.multiply(detail.getCreditMe(), exchangeRate, 2));
            }

        }

        return detail;
    }

    public List<AccountKardex> calculateAccountKardex(List<VoucherDetail> accountMovements, FinancesCurrencyType currencyType){

        List<AccountKardex> dataList = new ArrayList<AccountKardex>();
        BigDecimal balance = BigDecimal.ZERO;
        /** For M.N. **/
        if (currencyType.equals(FinancesCurrencyType.P)){
            for (VoucherDetail detail : accountMovements){
                balance = BigDecimalUtil.subtract(balance, detail.getDebit(), 6);
                balance = BigDecimalUtil.sum(balance, detail.getCredit(), 6);

                AccountKardex data = new AccountKardex(detail.getVoucher().getDate(), detail.getDebit(),detail.getCredit(),balance);
                dataList.add(data);
            }
        }

        /** For M.E. **/
        if (currencyType.equals(FinancesCurrencyType.D) || currencyType.equals(FinancesCurrencyType.M)){
            for (VoucherDetail detail : accountMovements){
                balance = BigDecimalUtil.subtract(balance, detail.getDebitMe(), 6);
                balance = BigDecimalUtil.sum(balance, detail.getCreditMe(), 6);

                AccountKardex data = new AccountKardex(detail.getVoucher().getDate(), detail.getDebitMe(), detail.getCreditMe(), balance);
                dataList.add(data);
            }
        }
        return dataList;
    }

    public BigDecimal calculateInterest(Date previousDate, Date currentDate, BigDecimal balance, BigDecimal percentage){

        BigDecimal saldoCapital = balance;

        Date currentPaymentDate = currentDate;
        currentPaymentDate      = DateUtils.removeTime(currentPaymentDate);
        Date lastPaymentDate    = previousDate; // previous

        Long days = DateUtils.daysBetween(lastPaymentDate, currentPaymentDate) - 1;

        BigDecimal var_interest = BigDecimalUtil.divide(percentage, BigDecimalUtil.toBigDecimal(100), 6);
        BigDecimal var_time = BigDecimalUtil.divide(BigDecimalUtil.toBigDecimal(days.toString()), BigDecimalUtil.toBigDecimal(360), 6);
        BigDecimal interest = BigDecimalUtil.multiply(saldoCapital, var_interest, 6);
        interest = BigDecimalUtil.multiply(interest, var_time, 6);


        return interest;
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

    /** For capitalize interest **/
    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public SavingType getSavingType() {
        return savingType;
    }

    public void setSavingType(SavingType savingType) {
        this.savingType = savingType;
    }


    private class AccountKardex{

        private Date date;
        private BigDecimal debit;
        private BigDecimal credit;
        private BigDecimal balance;
        private BigDecimal interest;

        AccountKardex(Date date, BigDecimal debit, BigDecimal credit, BigDecimal balance){
            this.setDate(date);
            this.setDebit(debit);
            this.setCredit(credit);
            this.setBalance(balance);
            this.setInterest(BigDecimal.ZERO);
        }

        public Date getDate() {
            return date;
        }

        public void setDate(Date date) {
            this.date = date;
        }

        public BigDecimal getDebit() {
            return debit;
        }

        public void setDebit(BigDecimal debit) {
            this.debit = debit;
        }

        public BigDecimal getCredit() {
            return credit;
        }

        public void setCredit(BigDecimal credit) {
            this.credit = credit;
        }

        public BigDecimal getBalance() {
            return balance;
        }

        public void setBalance(BigDecimal balance) {
            this.balance = balance;
        }

        public BigDecimal getInterest() {
            return interest;
        }

        public void setInterest(BigDecimal interest) {
            this.interest = interest;
        }
    }

    private void addFinancesCurrencyNotFoundMessage() {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                "FixedAssets.FinancesCurrencyNotFoundException");
    }

    private void addFinancesExchangeRateNotFoundExceptionMessage() {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.INFO,
                "FixedAssets.FinancesExchangeRateNotFoundException");
    }

}
