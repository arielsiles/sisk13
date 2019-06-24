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

    @In(create = true)
    private CreditAction creditAction;
    @In(create = true)
    private CreditReportAction creditReportAction;

    private Date dateTransaction = new Date();

    private BigDecimal interestValue;
    private BigDecimal criminalInterestValue;
    private BigDecimal capitalValue;
    private BigDecimal totalAmountValue;

    private Boolean transferSaving = Boolean.FALSE;
    private Account partnerAccount;
    private BigDecimal amountTransfer;


    @Factory(value = "creditTransaction", scope = ScopeType.STATELESS)
    public CreditTransaction initCredit() {
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

        creditItem.setCapitalBalance(capitalBalance);
        creditItem.setLastPayment(dateTransaction);

        creditTransactionService.createCreditTransactionPayFee(creditItem, creditTransaction);

        /** Uncomment for Production **/
        createIncomeAccountingRecord(creditTransaction);

        return  Outcome.SUCCESS;
    }

    public void createIncomeAccountingRecord(CreditTransaction creditTransaction){

        BigDecimal exchangeRate = BigDecimal.ZERO;
        try {
            exchangeRate = financesExchangeRateService.findLastExchangeRateByCurrency(FinancesCurrencyType.D.toString());
        }catch (FinancesExchangeRateNotFoundException e){addFinancesExchangeRateNotFoundExceptionMessage();
        }catch (FinancesCurrencyNotFoundException e){addFinancesCurrencyNotFoundMessage();}

        if (    creditTransaction.getCredit().getState().equals(CreditState.VIG) ||
                creditTransaction.getCredit().getState().equals(CreditState.VEN) ||
                creditTransaction.getCredit().getState().equals(CreditState.EJE)    ){

            Voucher voucher = new Voucher();
            voucher.setDocumentType(Constants.RI_VOUCHER_DOCTYPE);

            VoucherDetail voucherDetailBox = new VoucherDetail();
            /** Si es traspaso **/
            if (creditTransaction.getTransfer()){
                String cashAccountCode = "";
                voucher.setDocumentType(Constants.CT_VOUCHER_DOCTYPE);

                if (partnerAccount.getCurrency().equals(FinancesCurrencyType.P))
                    cashAccountCode = this.partnerAccount.getAccountType().getCashAccountMn().getAccountCode();
                if (partnerAccount.getCurrency().equals(FinancesCurrencyType.D))
                    cashAccountCode = this.partnerAccount.getAccountType().getCashAccountMe().getAccountCode();
                if (partnerAccount.getCurrency().equals(FinancesCurrencyType.M))
                    cashAccountCode = this.partnerAccount.getAccountType().getCashAccountMv().getAccountCode();

                voucherDetailBox.setAccount(cashAccountCode);
                voucherDetailBox.setPartnerAccount(this.partnerAccount);
                if (partnerAccount.getCurrency().equals(FinancesCurrencyType.D) || partnerAccount.getCurrency().equals(FinancesCurrencyType.M)){
                    voucherDetailBox.setDebit(creditTransaction.getAmount());
                    voucherDetailBox.setCredit(BigDecimal.ZERO);
                    voucherDetailBox.setDebitMe(BigDecimalUtil.divide(creditTransaction.getAmount(), exchangeRate));
                    voucherDetailBox.setCreditMe(BigDecimal.ZERO);
                    voucherDetailBox.setCurrency(partnerAccount.getCurrency());
                }else {
                    voucherDetailBox.setDebit(creditTransaction.getAmount());
                    voucherDetailBox.setCredit(BigDecimal.ZERO);
                    voucherDetailBox.setDebitMe(BigDecimal.ZERO);
                    voucherDetailBox.setCreditMe(BigDecimal.ZERO);
                }

            }else {
                voucherDetailBox.setAccount(Constants.ACCOUNT_GENERALCASH_CISC); /** todo **/
                voucherDetailBox.setDebit(creditTransaction.getAmount());
                voucherDetailBox.setCredit(BigDecimal.ZERO);
            }

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

            voucherDetailCurrentLoan.setCreditPartner(creditTransaction.getCredit());
            voucherDetailInterest.setCreditPartner(creditTransaction.getCredit());

            voucher.setGloss(creditTransaction.getCredit().getPreviousCode() + " " + creditTransaction.getCredit().getPartner().getFullName() + ", " + creditTransaction.getGloss());
            voucher.getDetails().add(voucherDetailBox);
            voucher.getDetails().add(voucherDetailCurrentLoan);
            voucher.getDetails().add(voucherDetailInterest);

            voucherAccoutingService.saveVoucher(voucher);

            creditTransactionService.updateTransaction(creditTransaction, voucher);

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
        voucherDetailCredit.setAccount(Constants.ACCOUNT_GENERALCASH_CISC); /** todo **/
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

    public String addCreditTransactionPayout() {
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
        //BigDecimal fullPayment = BigDecimalUtil.sum(credit.getQuota(), interest, 6);

        getInstance().setInterest(interest);
        BigDecimal currentCapital = calculateCapital(credit);
        BigDecimal totalPayment = BigDecimalUtil.sum(currentCapital, interest, 6);
        getInstance().setCapital(currentCapital);
        getInstance().setAmount(totalPayment);

        this.interestValue = interest;
        this.capitalValue = currentCapital;
        this.totalAmountValue = totalPayment;
        this.criminalInterestValue = calculateCriminalInterest();

        System.out.println("--------------------------> Capital: " + capitalValue);
        System.out.println("--------------------------> Interes: " + interestValue);
        System.out.println("--------------------------> Total  : " + totalAmountValue);

        return interest;
    }

    public BigDecimal calculateInterestTodate(Credit credit, Date date){
        return credit.getCapitalBalance();
    }


    public BigDecimal calculateCriminalInterest(){
        return BigDecimal.ZERO;
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
        setAmountTransfer(totalAmount);
    }

    public void calculateTotalCapital(){

        //BigDecimal totalCapital = null;
        if (null != getInstance().getCapital() && null != getInstance().getInterest()) {
            //totalCapital = BigDecimalUtil.subtract(getInstance().getAmount(), getInstance().getInterest(), 6);
            //getInstance().setCapital(BigDecimalUtil.subtract(getInstance().getAmount(), getInstance().getInterest(), 6));
            setCapitalValue(BigDecimalUtil.subtract(totalAmountValue, interestValue, 6));

        }
        setAmountTransfer(getTotalAmountValue());
    }

    public void adjustCents(){

        BigDecimal totalAmountRound = BigDecimalUtil.toBigDecimal(Math.round(totalAmountValue.doubleValue()));
        BigDecimal diff = BigDecimalUtil.subtract(totalAmountRound, totalAmountValue, 6 );

        interestValue    = BigDecimalUtil.sum(interestValue, diff, 6);
        totalAmountValue = totalAmountRound;

    }

    public void clearPartnerAccount(){
        setPartnerAccount(null);
        setAmountTransfer(null);
    }

    public void changeCapital(){
        setCapitalValue(getAmountTransfer());
        calculateTotalAmount();
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

    public Account getPartnerAccount() {
        return partnerAccount;
    }

    public void setPartnerAccount(Account partnerAccount) {
        this.partnerAccount = partnerAccount;
    }

    public BigDecimal getAmountTransfer() {
        return amountTransfer;
    }

    public void setAmountTransfer(BigDecimal amountTransfer) {
        this.amountTransfer = amountTransfer;
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


