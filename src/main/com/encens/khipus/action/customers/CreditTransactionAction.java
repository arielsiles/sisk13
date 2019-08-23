package com.encens.khipus.action.customers;

import com.encens.khipus.action.customers.reports.CreditReportAction;
import com.encens.khipus.exception.ConcurrencyException;
import com.encens.khipus.exception.EntryDuplicatedException;
import com.encens.khipus.exception.ReferentialIntegrityException;
import com.encens.khipus.exception.finances.FinancesCurrencyNotFoundException;
import com.encens.khipus.exception.finances.FinancesExchangeRateNotFoundException;
import com.encens.khipus.framework.action.GenericAction;
import com.encens.khipus.framework.action.Outcome;
import com.encens.khipus.model.customers.*;
import com.encens.khipus.model.finances.FinancesCurrencyType;
import com.encens.khipus.model.finances.Voucher;
import com.encens.khipus.model.finances.VoucherDetail;
import com.encens.khipus.service.accouting.VoucherAccoutingService;
import com.encens.khipus.service.customers.AccountService;
import com.encens.khipus.service.customers.CreditService;
import com.encens.khipus.service.customers.CreditTransactionService;
import com.encens.khipus.service.finances.CashAccountService;
import com.encens.khipus.service.finances.FinancesExchangeRateService;
import com.encens.khipus.util.BigDecimalUtil;
import com.encens.khipus.util.Constants;
import com.encens.khipus.util.DateUtils;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.*;
import org.jboss.seam.international.StatusMessage;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Actions for Credit
 *
 * @author:
 */

@Name("creditTransactionAction")
@Scope(ScopeType.CONVERSATION)
public class CreditTransactionAction extends GenericAction<CreditTransaction> {

    @In
    private CreditService creditService;
    @In
    private CreditTransactionService creditTransactionService;
    @In
    private VoucherAccoutingService voucherAccoutingService;
    @In
    private CashAccountService cashAccountService;
    @In
    private FinancesExchangeRateService financesExchangeRateService;
    @In
    private AccountService accountService;

    @In(create = true)
    private CreditAction creditAction;
    @In(create = true)
    private CreditReportAction creditReportAction;

    private Date dateTransaction = new Date();
    BigDecimal exchangeRate = BigDecimal.ZERO;
    private BigDecimal totalTransferAmount = BigDecimal.ZERO;

    private BigDecimal interestValue;
    private BigDecimal criminalInterestValue;
    private BigDecimal capitalValue;
    private BigDecimal totalAmountValue;
    private BigDecimal transferAmount;
    private BigDecimal differenceAvailable = BigDecimal.ZERO;

    private Boolean transferSaving = Boolean.FALSE;
    //private Account account;
    private String gloss;

    private List<Account> accountTransferList;

    @Factory(value = "creditTransaction")
    public CreditTransaction initCredit() {
        try {

            exchangeRate = financesExchangeRateService.findLastExchangeRateByCurrency(FinancesCurrencyType.D.toString());
            System.out.println("----> Obteniendo tipo de cambio... " + exchangeRate);
        }catch (FinancesExchangeRateNotFoundException e){addFinancesExchangeRateNotFoundExceptionMessage();
        }catch (FinancesCurrencyNotFoundException e){addFinancesCurrencyNotFoundMessage();}
        return getInstance();
    }

    //@Begin(ifOutcome = Outcome.SUCCESS, flushMode = FlushModeType.MANUAL)
    @Override
    public String select(CreditTransaction instance) {
        setOp(OP_UPDATE);
        String outCome = super.select(instance);
        System.out.println(".....SELECT CreditTransactionAction: " + outCome);
        return outCome;
    }

    @End(beforeRedirect = true)
    public String create(Credit creditItem) {

        CreditTransaction creditTransaction = getInstance();
        BigDecimal capitalBalance = creditItem.getCapitalBalance();
        capitalBalance = BigDecimalUtil.subtract(capitalBalance, capitalValue, 6);

        try {
            //creditTransaction.setDate(new Date());
            creditTransaction.setDate(dateTransaction);
            creditTransaction.setDays(0);
            creditTransaction.setCapitalBalance(capitalBalance);

            creditTransaction.setInterest(interestValue);
            creditTransaction.setCapital(capitalValue);
            creditTransaction.setAmount(totalAmountValue);

            creditTransaction.setCreditTransactionType(CreditTransactionType.ING);
            creditTransaction.setCredit(creditItem);

            creditItem.setCapitalBalance(capitalBalance);
            genericService.create(creditTransaction);
            addCreatedMessage();
            cleanValues();



            return Outcome.SUCCESS;
        } catch (EntryDuplicatedException e) {
            addDuplicatedMessage();
            return Outcome.REDISPLAY;
        }
    }

    @End(beforeRedirect = true)
    public String createCreditTransaction(Credit creditItem){

        if (getTransferSaving()){
            if (getTotalTransferAmount().compareTo(getTotalAmountValue()) < 0){
                facesMessages.addFromResourceBundle(StatusMessage.Severity.WARN,"CreditTransaction.message.invalidTransferAmount");
                return Outcome.REDISPLAY;
            }
        }

        CreditTransaction creditTransaction = getInstance();
        BigDecimal capitalBalance = creditItem.getCapitalBalance();
        capitalBalance = BigDecimalUtil.subtract(capitalBalance, capitalValue, 6);
        creditTransaction.setDate(dateTransaction);
        creditTransaction.setDays(0);
        creditTransaction.setCapitalBalance(capitalBalance);
        creditTransaction.setInterest(interestValue);
        creditTransaction.setCriminalInterest(criminalInterestValue);
        creditTransaction.setCapital(capitalValue);
        creditTransaction.setAmount(totalAmountValue);
        creditTransaction.setCreditTransactionType(CreditTransactionType.ING);
        creditTransaction.setCredit(creditItem);
        creditTransaction.setTransfer(this.transferSaving);
        creditTransaction.setGloss(this.gloss);
        creditTransaction.setDifference(differenceAvailable);

        creditItem.setCapitalBalance(capitalBalance);
        creditItem.setLastPayment(dateTransaction);

        creditTransactionService.createCreditTransactionPayFee(creditItem, creditTransaction);

        if (getTransferSaving())
            createIncomeAccountingRecordForTransfer(creditTransaction);
        else
            createIncomeAccountingRecord(creditTransaction);

        cleanValues();
        return  Outcome.SUCCESS;
    }

    private void createIncomeAccountingRecordForTransfer(CreditTransaction creditTransaction){
        //setDifferenceTransfer();

        //if (getTotalTransferAmount().compareTo(getTotalAmountValue()) < 0)
        BigDecimal difference = BigDecimalUtil.subtract(getTotalTransferAmount(), getTotalAmountValue(), 2);

        Voucher voucher = new Voucher();
        voucher.setDocumentType(Constants.CT_VOUCHER_DOCTYPE);

        for (Account account : this.getAccountTransferList()){
            VoucherDetail voucherDetailDebit = new VoucherDetail();
            String cashAccountCodeDebit = "";
            if (account.getCurrency().equals(FinancesCurrencyType.P))
                cashAccountCodeDebit = account.getAccountType().getCashAccountMn().getAccountCode();
            if (account.getCurrency().equals(FinancesCurrencyType.D))
                cashAccountCodeDebit = account.getAccountType().getCashAccountMe().getAccountCode();
            if (account.getCurrency().equals(FinancesCurrencyType.M))
                cashAccountCodeDebit = account.getAccountType().getCashAccountMv().getAccountCode();

            voucherDetailDebit.setAccount(cashAccountCodeDebit);
            voucherDetailDebit.setPartnerAccount(account);

            voucherDetailDebit.setDebit(account.getTransferAmount());
            voucherDetailDebit.setCredit(BigDecimal.ZERO);
            voucherDetailDebit.setDebitMe(BigDecimal.ZERO);
            voucherDetailDebit.setCreditMe(BigDecimal.ZERO);

            if (account.getCurrency().equals(FinancesCurrencyType.D) || account.getCurrency().equals(FinancesCurrencyType.M)){
                voucherDetailDebit.setDebit(BigDecimalUtil.multiply(account.getTransferAmount(), this.exchangeRate, 2));
                voucherDetailDebit.setCredit(BigDecimal.ZERO);
                voucherDetailDebit.setDebitMe(account.getTransferAmount());
                voucherDetailDebit.setCreditMe(BigDecimal.ZERO);
                voucherDetailDebit.setCurrency(account.getCurrency());
            }
            voucher.getDetails().add(voucherDetailDebit);
        }

        VoucherDetail voucherDetailCapitalCredit = new VoucherDetail();
        if (creditTransaction.getCredit().getState().equals(CreditState.VIG)) {
            voucherDetailCapitalCredit.setAccount(creditTransaction.getCredit().getCreditType().getCurrentAccountCode());
        }
        if (creditTransaction.getCredit().getState().equals(CreditState.VEN)){
            voucherDetailCapitalCredit.setAccount(creditTransaction.getCredit().getCreditType().getExpiredAccountCode());
        }
        if (creditTransaction.getCredit().getState().equals(CreditState.EJE)){
            voucherDetailCapitalCredit.setAccount(creditTransaction.getCredit().getCreditType().getExecutedAccountCode());
        }

        voucherDetailCapitalCredit.setDebit(BigDecimal.ZERO);
        voucherDetailCapitalCredit.setCredit(creditTransaction.getCapital());
        voucherDetailCapitalCredit.setDebitMe(BigDecimal.ZERO);
        voucherDetailCapitalCredit.setCreditMe(BigDecimal.ZERO);

        VoucherDetail voucherDetailInterestCredit = new VoucherDetail();
        if (creditTransaction.getCredit().getState().equals(CreditState.VIG)) {
            voucherDetailInterestCredit.setAccount(creditTransaction.getCredit().getCreditType().getCurrentInterestAccountCode());
        }
        if (creditTransaction.getCredit().getState().equals(CreditState.VEN)){
            voucherDetailInterestCredit.setAccount(creditTransaction.getCredit().getCreditType().getExpiredInterestAccountCode());
        }
        if (creditTransaction.getCredit().getState().equals(CreditState.EJE)){
            voucherDetailInterestCredit.setAccount(creditTransaction.getCredit().getCreditType().getExecutedInterestAccountCode());
        }

        voucherDetailInterestCredit.setDebit(BigDecimal.ZERO);
        voucherDetailInterestCredit.setCredit(creditTransaction.getInterest());
        voucherDetailInterestCredit.setDebitMe(BigDecimal.ZERO);
        voucherDetailInterestCredit.setCreditMe(BigDecimal.ZERO);

        voucher.setGloss(creditTransaction.getGloss());
        voucherDetailCapitalCredit.setCreditPartner(creditTransaction.getCredit());
        voucherDetailInterestCredit.setCreditPartner(creditTransaction.getCredit());

        voucher.getDetails().add(voucherDetailCapitalCredit);
        if (voucherDetailInterestCredit.getCredit().doubleValue() > 0)
            voucher.getDetails().add(voucherDetailInterestCredit);

        VoucherDetail voucherDetailDifferenceChange = new VoucherDetail();
        /** Si existe diferencia de cambio **/
        if (difference.doubleValue() > 0){
            voucherDetailDifferenceChange.setAccount(Constants.ACCOUNT_DIFFERENCE_AVAILABLE_CHANGE);
            voucherDetailDifferenceChange.setDebit(BigDecimal.ZERO);
            voucherDetailDifferenceChange.setCredit(difference);
            voucherDetailDifferenceChange.setCurrency(FinancesCurrencyType.P);
            voucherDetailDifferenceChange.setDebitMe(BigDecimal.ZERO);
            voucherDetailDifferenceChange.setCreditMe(BigDecimal.ZERO);

            voucher.getDetails().add(voucherDetailDifferenceChange);
        }

        /** Si tiene monto de interes penal **/
        addVoucherDetailCriminalInterest(voucher, creditTransaction);

        voucherAccoutingService.saveVoucher(voucher);
        creditTransactionService.updateTransaction(creditTransaction, voucher);


    }

    public void createIncomeAccountingRecord(CreditTransaction creditTransaction){

        //if (creditTransaction.getTransfer()) setDifferenceTransfer();

        if (    creditTransaction.getCredit().getState().equals(CreditState.VIG) ||
                creditTransaction.getCredit().getState().equals(CreditState.VEN) ||
                creditTransaction.getCredit().getState().equals(CreditState.EJE)    ){

            Voucher voucher = new Voucher();
            voucher.setDocumentType(Constants.CI_VOUCHER_DOCTYPE);

            VoucherDetail voucherDetailBox = new VoucherDetail();
            VoucherDetail voucherDetailDifferenceChange = new VoucherDetail();

            voucherDetailBox.setAccount(Constants.ACCOUNT_GENERALCASH); /** todo **/
            voucherDetailBox.setDebit(creditTransaction.getAmount());
            voucherDetailBox.setCredit(BigDecimal.ZERO);

            VoucherDetail voucherDetailCurrentLoan = new VoucherDetail();

            if (creditTransaction.getCredit().getState().equals(CreditState.VIG)) {
                voucherDetailCurrentLoan.setAccount(creditTransaction.getCredit().getCreditType().getCurrentAccountCode());
            }

            if (creditTransaction.getCredit().getState().equals(CreditState.VEN)){
                voucherDetailCurrentLoan.setAccount(creditTransaction.getCredit().getCreditType().getExpiredAccountCode());
            }

            if (creditTransaction.getCredit().getState().equals(CreditState.EJE)){
                voucherDetailCurrentLoan.setAccount(creditTransaction.getCredit().getCreditType().getExecutedAccountCode());
            }

            voucherDetailCurrentLoan.setDebit(BigDecimal.ZERO);
            voucherDetailCurrentLoan.setCredit(creditTransaction.getCapital());
            voucherDetailCurrentLoan.setDebitMe(BigDecimal.ZERO);
            voucherDetailCurrentLoan.setCreditMe(BigDecimal.ZERO);

            VoucherDetail voucherDetailInterest = new VoucherDetail();
            if (creditTransaction.getCredit().getState().equals(CreditState.VIG)) {
                voucherDetailInterest.setAccount(creditTransaction.getCredit().getCreditType().getCurrentInterestAccountCode());
            }

            if (creditTransaction.getCredit().getState().equals(CreditState.VEN)){
                voucherDetailInterest.setAccount(creditTransaction.getCredit().getCreditType().getExpiredInterestAccountCode());
            }

            if (creditTransaction.getCredit().getState().equals(CreditState.EJE)){
                voucherDetailInterest.setAccount(creditTransaction.getCredit().getCreditType().getExecutedInterestAccountCode());
            }

            voucherDetailInterest.setDebit(BigDecimal.ZERO);
            voucherDetailInterest.setCredit(creditTransaction.getInterest());
            voucherDetailInterest.setDebitMe(BigDecimal.ZERO);
            voucherDetailInterest.setCreditMe(BigDecimal.ZERO);

            voucherDetailCurrentLoan.setCreditPartner(creditTransaction.getCredit());
            voucherDetailInterest.setCreditPartner(creditTransaction.getCredit());

            voucher.setGloss(creditTransaction.getGloss());
            voucher.getDetails().add(voucherDetailBox);
            voucher.getDetails().add(voucherDetailCurrentLoan);

            if (creditTransaction.getInterest().doubleValue() > 0)
                voucher.getDetails().add(voucherDetailInterest);
            if (differenceAvailable.doubleValue() > 0)
                voucher.getDetails().add(voucherDetailDifferenceChange);

            /** Si tiene monto de interes penal **/
            addVoucherDetailCriminalInterest(voucher, creditTransaction);
            /*if (creditTransaction.getCriminalInterest().doubleValue() > 0){
                VoucherDetail voucherDetailCriminalInterest = new VoucherDetail();
                voucherDetailCriminalInterest.setAccount(creditTransaction.getCredit().getCreditType().getCriminalInterestAccountCode());
                voucherDetailCriminalInterest.setDebit(BigDecimal.ZERO);
                voucherDetailCriminalInterest.setCredit(creditTransaction.getCriminalInterest());
                voucherDetailCriminalInterest.setDebitMe(BigDecimal.ZERO);
                voucherDetailCriminalInterest.setCreditMe(BigDecimal.ZERO);
                voucher.getDetails().add(voucherDetailCriminalInterest);
            }*/

            voucherAccoutingService.saveVoucher(voucher);

            creditTransactionService.updateTransaction(creditTransaction, voucher);
        }
    }

    private void addVoucherDetailCriminalInterest(Voucher voucher, CreditTransaction creditTransaction){
        /** Si tiene monto de interes penal **/
        if (creditTransaction.getCriminalInterest().doubleValue() > 0){
            VoucherDetail voucherDetailCriminalInterest = new VoucherDetail();
            voucherDetailCriminalInterest.setAccount(creditTransaction.getCredit().getCreditType().getCriminalInterestAccountCode());
            voucherDetailCriminalInterest.setDebit(BigDecimal.ZERO);
            voucherDetailCriminalInterest.setCredit(creditTransaction.getCriminalInterest());
            voucherDetailCriminalInterest.setDebitMe(BigDecimal.ZERO);
            voucherDetailCriminalInterest.setCreditMe(BigDecimal.ZERO);
            voucher.getDetails().add(voucherDetailCriminalInterest);
        }
    }

    @Override
    public String cancel(){
        cleanValues();
        return Outcome.CANCEL;
    }

    public  void cleanValues(){
        setInterestValue(BigDecimal.ZERO);
        setCapitalValue(BigDecimal.ZERO);
        setTotalAmountValue(BigDecimal.ZERO);
        setDateTransaction(new Date());
        clearPartnerAccount();
        setTransferSaving(Boolean.FALSE);
    }

    @End(beforeRedirect = true)
    public String createCreditTransactionPayout(Credit credit){

        CreditTransaction creditTransaction = getInstance();
        creditTransaction.setDate(dateTransaction);
        creditTransactionService.createCreditTransactionPayout(credit, creditTransaction);

        /** Uncomment for Production **/
        createCashAccountForCreditTransactionPayout(credit, creditTransaction);

        return  Outcome.SUCCESS;
    }

    public void createCashAccountForCreditTransactionPayout(Credit credit, CreditTransaction creditTransaction){
        Voucher voucher = new Voucher();
        voucher.setDocumentType(Constants.CE_VOUCHER_DOCTYPE);

        VoucherDetail voucherDetailDebit = new VoucherDetail();
        voucherDetailDebit.setAccount(credit.getCreditType().getCurrentAccountCode());
        voucherDetailDebit.setDebit(getInstance().getAmount());
        voucherDetailDebit.setCredit(BigDecimal.ZERO);
        voucherDetailDebit.setCreditPartner(credit);

        VoucherDetail voucherDetailCredit = new VoucherDetail();
        voucherDetailCredit.setAccount(Constants.ACCOUNT_GENERALCASH); /** todo **/
        voucherDetailCredit.setDebit(BigDecimal.ZERO);
        voucherDetailCredit.setCredit(getInstance().getAmount());

        voucher.getDetails().add(voucherDetailDebit);
        voucher.getDetails().add(voucherDetailCredit);

        voucher.setGloss(creditTransaction.getGloss());
        voucherAccoutingService.saveVoucher(voucher);
        creditTransactionService.updateTransaction(creditTransaction, voucher);
    }

    @End(beforeRedirect = true)
    public String delete() {
        try {
            getService().delete(getInstance());
            addDeletedMessage();
        } catch (ConcurrencyException e) {
            entryNotFoundLog();
            addDeleteConcurrencyMessage();
        } catch (ReferentialIntegrityException e) {
            referentialIntegrityLog();
            addDeleteReferentialIntegrityMessage();
        }

        return Outcome.SUCCESS;
    }

    public String addCreditTransaction() {
        setOp(OP_CREATE);
        //set a null v in the current instance to force a create the new instance.
        setInstance(null);

        System.out.println(".....CALCULANDO INTERES.....: " + calculateInterest());

        return Outcome.SUCCESS;
    }

    public String addCreditTransactionPayout(Credit credit) {
        setOp(OP_CREATE);
        //set a null v in the current instance to force a create the new instance.
        setInstance(null);
        return Outcome.SUCCESS;
    }

    public BigDecimal calculateInterest(){

        Credit credit = creditAction.getInstance();
        BigDecimal saldoCapital = credit.getCapitalBalance();

        Date currentPaymentDate = dateTransaction;
        currentPaymentDate      = DateUtils.removeTime(currentPaymentDate);
        Date lastPaymentDate    = creditTransactionService.findLastPaymentForInterest(credit);

        Long days = DateUtils.daysBetween(lastPaymentDate, currentPaymentDate) - 1;

        BigDecimal var_interest = BigDecimalUtil.divide(BigDecimalUtil.toBigDecimal(credit.getAnnualRate()), BigDecimalUtil.toBigDecimal(100), 6);
        BigDecimal var_time = BigDecimalUtil.divide(BigDecimalUtil.toBigDecimal(days.toString()), BigDecimalUtil.toBigDecimal(360), 6);
        BigDecimal interest = BigDecimalUtil.multiply(saldoCapital, var_interest, 6);
        interest = BigDecimalUtil.multiply(interest, var_time, 6);

        /** For criminal interest **/
        BigDecimal var_criminalInterest = BigDecimalUtil.divide(credit.getCriminalInterest(), BigDecimalUtil.ONE_HUNDRED, 6);
        BigDecimal criminalInterest =  BigDecimalUtil.multiply(saldoCapital, var_criminalInterest, 6);
        criminalInterest = BigDecimalUtil.multiply(criminalInterest, var_time, 6);
        /** --- **/


        getInstance().setInterest(interest);
        BigDecimal currentCapital = calculateCapital(credit);
        BigDecimal totalPayment = BigDecimalUtil.sum(currentCapital, interest, 6);
        getInstance().setCapital(currentCapital);
        getInstance().setAmount(totalPayment);

        this.interestValue = interest;
        this.capitalValue = currentCapital;
        this.totalAmountValue = totalPayment;
        this.criminalInterestValue = criminalInterest;




        if (saldoCapital.doubleValue() < capitalValue.doubleValue()) {
            capitalValue = saldoCapital;
            totalPayment = BigDecimalUtil.sum(capitalValue, interest, 6);
            this.totalAmountValue = totalPayment;
        }

        System.out.println("--------------------------> Capital: " + capitalValue);
        System.out.println("--------------------------> Interes: " + interestValue);
        System.out.println("--------------------------> Total  : " + totalAmountValue);

        return interest;
    }

    public BigDecimal calculateInterestTodate(Credit credit, Date date){
        return credit.getCapitalBalance();
    }


    /** ? **/
    public BigDecimal calculateCapital(Credit credit){

        int quotas = 0;

        Date lastPaymentDate = creditTransactionService.findLastPayment(credit);

        System.out.println("--------> LASTPAYMENT: " + lastPaymentDate);

        Calendar cal = Calendar.getInstance();
        cal.setTime(lastPaymentDate);
        lastPaymentDate = cal.getTime();

        //Date currentPaymentDate = getInstance().getDate();
        Date currentPaymentDate = this.dateTransaction;

        currentPaymentDate = DateUtils.removeTime(currentPaymentDate);

        CreditState state = credit.getState();
        int amortize = credit.getAmortization();

        if (state.equals(CreditState.VIG)) {
            quotas = 1; //calculateQuotaVig(lastPaymentDate, currentPaymentDate, amortize/30);
        }else {
            if (state.equals(CreditState.VEN) || state.equals(CreditState.EJE)) {
                quotas = calculateQuotasVen(credit, lastPaymentDate, currentPaymentDate, amortize/30, credit.getNumberQuota()); //revisar error
                //quotas = 1;
            }
        }
        return BigDecimalUtil.multiply(credit.getQuota(), BigDecimalUtil.toBigDecimal(quotas), 6);
    }

    /** ? **/
    public int calculateQuotasVen(Credit credit, Date lastPaymentDate, Date currentDate, int amortize, int totalQuotas){

        Calendar calendarLast = Calendar.getInstance();
        calendarLast.setTime(lastPaymentDate);

        Calendar calendarNext = Calendar.getInstance();
        calendarNext.setTime(lastPaymentDate);
        calendarNext.add(Calendar.MONTH, amortize);
        Date nextPaymentDate = calendarNext.getTime();

        int quotas = 1;

        System.out.println("---> currentDate: " + currentDate);
        System.out.println("---> lastPaymentDate: " + lastPaymentDate);
        System.out.println("---> nextPaymentDate: " + nextPaymentDate);
        System.out.println("---> lastPaymentDate.before(currentDate): " + lastPaymentDate.before(currentDate));
        System.out.println("---> lastPaymentDate.equals(currentDate): " + lastPaymentDate.equals(currentDate));
        System.out.println("--------------------");

        while (lastPaymentDate.before(currentDate)){

            if (lastPaymentDate.before(nextPaymentDate)){
                System.out.println("Last datee: " + lastPaymentDate + " quotas: " + quotas);

            }else{

                System.out.println("====> QUOTAS: " + quotas + " - TOTAL QUOTAS: " + totalQuotas);

                if (quotas < totalQuotas-calculatePaidQuotas(credit)){
                    quotas++;
                    System.out.println("Last date: " + lastPaymentDate + " quotas: " + quotas);
                    calendarNext.add(Calendar.MONTH, amortize);
                    nextPaymentDate = calendarNext.getTime();
                }
            }

            calendarLast.add(Calendar.DAY_OF_YEAR, 1);
            lastPaymentDate = calendarLast.getTime();

        }

        System.out.println("--------------------");
        System.out.println("Last Payment   : " + lastPaymentDate);
        System.out.println("Current Payment: " + currentDate);
        System.out.println("nextPaymentDate: " + nextPaymentDate);

        return quotas;
    }

    public Integer calculatePaidQuotas(Credit credit){
        System.out.println("===================> REV calculatePaidQuotas: " + credit.getPreviousCode());
        BigDecimal totalPaid = BigDecimalUtil.subtract(credit.getAmount(), credit.getCapitalBalance(), 2);
        Integer quota = BigDecimalUtil.divide(totalPaid, credit.getQuota(), 2).intValue();

        System.out.println("----> QUOTAS PAGADAS: " + quota);
        return quota;

    }

    public Integer calculatePendingFees(Credit credit){

        BigDecimal quota = BigDecimalUtil.divide(credit.getCapitalBalance(), credit.getQuota(), 2);

        System.out.println("----> QUOTAS PENDIENTES: " + quota);
        return quota.intValue();
    }


    public void calculateTotalAmount() {
        BigDecimal totalAmount = null;
        //if (null != getInstance().getCapital() && null != getInstance().getInterest() && null != getInstance().getCriminalInterest()) {
        if (null != getInstance().getCapital() && null != getInstance().getInterest()) {
            totalAmount = BigDecimalUtil.sum(capitalValue, interestValue, 6);
            totalAmount = BigDecimalUtil.sum(totalAmount, criminalInterestValue, 6);
        }
        //getInstance().setAmount(totalAmount);
        setTotalAmountValue(totalAmount);
    }

    public void calculateTotalCapital(){

        //BigDecimal totalCapital = null;
        if (null != getInstance().getCapital() && null != getInstance().getInterest()) {
            //totalCapital = BigDecimalUtil.subtract(getInstance().getAmount(), getInstance().getInterest(), 6);
            //getInstance().setCapital(BigDecimalUtil.subtract(getInstance().getAmount(), getInstance().getInterest(), 6));
            setCapitalValue(BigDecimalUtil.subtract(totalAmountValue, interestValue, 6));

        }
    }

    public void adjustCents(){

        BigDecimal totalAmountRound = BigDecimalUtil.toBigDecimal(Math.round(totalAmountValue.doubleValue()));
        BigDecimal diff = BigDecimalUtil.subtract(totalAmountRound, totalAmountValue, 6 );

        interestValue    = BigDecimalUtil.sum(interestValue, diff, 6);
        totalAmountValue = totalAmountRound;

    }

    public void clearPartnerAccount(){
        setAccountTransferList(null);
    }

    public Date getDateTransaction() {
        return dateTransaction;
    }

    public void setDateTransaction(Date dateTransaction) {
        this.dateTransaction = dateTransaction;
    }

    public BigDecimal getInterestValue() {
        return interestValue;
    }

    public void setInterestValue(BigDecimal interestValue) {
        this.interestValue = interestValue;
    }

    public BigDecimal getCapitalValue() {
        return capitalValue;
    }

    public void setCapitalValue(BigDecimal capitalValue) {
        this.capitalValue = capitalValue;
    }

    public BigDecimal getTotalAmountValue() {
        return totalAmountValue;
    }

    public BigDecimal getTotalAmountValueToMe(){
        BigDecimal exchangeRate = BigDecimal.ZERO;
        try {
            exchangeRate = financesExchangeRateService.findLastExchangeRateByCurrency(FinancesCurrencyType.D.toString());
        }catch (FinancesExchangeRateNotFoundException e){addFinancesExchangeRateNotFoundExceptionMessage();
        }catch (FinancesCurrencyNotFoundException e){addFinancesCurrencyNotFoundMessage();}

        return BigDecimalUtil.divide(getTotalAmountValue(), exchangeRate);
    }

    public void setTotalAmountValue(BigDecimal totalAmountValue) {
        this.totalAmountValue = totalAmountValue;
    }

    public BigDecimal getCriminalInterestValue() {
        return criminalInterestValue;
    }

    public void setCriminalInterestValue(BigDecimal criminalInterestValue) {
        this.criminalInterestValue = criminalInterestValue;
    }

    public Boolean getTransferSaving() {
        return transferSaving;
    }

    public void setTransferSaving(Boolean transferSaving) {
        this.transferSaving = transferSaving;
    }

    private void addFinancesCurrencyNotFoundMessage() {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                "FixedAssets.FinancesCurrencyNotFoundException");
    }

    private void addFinancesExchangeRateNotFoundExceptionMessage() {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.INFO,
                "FixedAssets.FinancesExchangeRateNotFoundException");
    }

    /*public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }*/

    public String getGloss() {
        gloss = creditAction.getInstance().getPartner().getFullName() + " (" + creditAction.getInstance().getPreviousCode() + ") ";
        return gloss;
    }

    public void setGloss(String gloss) {
        this.gloss = gloss;
    }

    public BigDecimal getTransferAmount() {
        return transferAmount;
    }

    public void setTransferAmount(BigDecimal transferAmount) {
        this.transferAmount = transferAmount;
    }

    /*public void setTransferAmountDefault(){
        BigDecimal amount = getTotalAmountValue();
        if (getAccount().getCurrency().equals(FinancesCurrencyType.D) || getAccount().getCurrency().equals(FinancesCurrencyType.M)) {
            amount = getTotalAmountValueToMe();
        }
        setTransferAmount(amount);
    }*/

    public void setDifferenceTransfer(){
        BigDecimal amountValue = getTotalAmountValue();
        BigDecimal transferAmountValue = getTransferAmount();

        differenceAvailable = BigDecimalUtil.subtract(transferAmountValue, amountValue);
        /*if (getAccount().getCurrency().equals(FinancesCurrencyType.D) || getAccount().getCurrency().equals(FinancesCurrencyType.M)) {
            transferAmountValue = BigDecimalUtil.multiply(transferAmountValue, exchangeRate);
            System.out.println("---------> transferAmountValue: " + transferAmountValue);
            differenceAvailable = BigDecimalUtil.subtract(transferAmountValue, amountValue);
        }*/
        System.out.println("---------> differenceAvailable: " + differenceAvailable);
        if (differenceAvailable.doubleValue() < 0){
            facesMessages.addFromResourceBundle(StatusMessage.Severity.WARN,"CreditTransaction.message.invalidTransferAmount");
        }
    }

    public List<Account> getAccountList(Partner partner){
        this.setAccountTransferList(accountService.getAccountList(partner));
        return getAccountTransferList();
    }

    public BigDecimal getTotalTransferAmount(){
        BigDecimal result = BigDecimal.ZERO;

        for (Account account : this.getAccountTransferList()){
            System.out.println("----> Transf amount: " + account.getTransferAmount());
            if (account.getCurrency().equals(FinancesCurrencyType.D) || account.getCurrency().equals(FinancesCurrencyType.M)){
                result = BigDecimalUtil.sum(result, BigDecimalUtil.multiply(account.getTransferAmount(), this.exchangeRate));
            }

            if (account.getCurrency().equals(FinancesCurrencyType.P))
                result = BigDecimalUtil.sum(result, account.getTransferAmount());
        }
        setTotalTransferAmount(result);
        return result;
    }

    public void setTotalTransferAmount(BigDecimal totalTransferAmount) {
        this.totalTransferAmount = totalTransferAmount;
    }

    public List<Account> getAccountTransferList() {
        return accountTransferList;
    }

    public void setAccountTransferList(List<Account> accountTransferList) {
        this.accountTransferList = accountTransferList;
    }
}


