package com.encens.khipus.action.customers;

import com.encens.khipus.exception.ConcurrencyException;
import com.encens.khipus.exception.EntryDuplicatedException;
import com.encens.khipus.exception.ReferentialIntegrityException;
import com.encens.khipus.framework.action.GenericAction;
import com.encens.khipus.framework.action.Outcome;
import com.encens.khipus.model.customers.Credit;
import com.encens.khipus.model.customers.CreditState;
import com.encens.khipus.model.customers.CreditTransaction;
import com.encens.khipus.model.customers.CreditTransactionType;
import com.encens.khipus.service.customers.CreditService;
import com.encens.khipus.service.customers.CreditTransactionService;
import com.encens.khipus.util.BigDecimalUtil;
import com.encens.khipus.util.DateUtils;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.*;

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
    @In(create = true)
    private CreditAction creditAction;

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
        capitalBalance = BigDecimalUtil.subtract(capitalBalance, getInstance().getCapital(), 6);

        try {
            //creditTransaction.setDate(new Date());
            creditTransaction.setDate(creditTransaction.getDate());
            creditTransaction.setDays(0);
            creditTransaction.setCapitalBalance(capitalBalance);
            creditTransaction.setCreditTransactionType(CreditTransactionType.ING);
            creditTransaction.setCredit(creditItem);

            creditItem.setCapitalBalance(capitalBalance);
            genericService.create(creditTransaction);
            addCreatedMessage();
            return Outcome.SUCCESS;
        } catch (EntryDuplicatedException e) {
            addDuplicatedMessage();
            return Outcome.REDISPLAY;
        }
    }

    @Override
    public String cancel(){
        cleanValues();
        return Outcome.CANCEL;
    }

    public  void cleanValues(){
        getInstance().setCapital(BigDecimal.ZERO);
        getInstance().setAmount(BigDecimal.ZERO);
        getInstance().setDate(null);
        getInstance().setInterest(BigDecimal.ZERO);
    }

    @End(beforeRedirect = true)
    public String createCreditTransactionPayout(Credit credit){

        CreditTransaction creditTransaction = getInstance();
        creditTransactionService.createCreditTransactionPayout(credit, creditTransaction);
        return  Outcome.SUCCESS;
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

        Date currentPaymentDate = getInstance().getDate();
        currentPaymentDate = DateUtils.removeTime(currentPaymentDate);

        Date lastPaymentDate = creditTransactionService.findLastPaymentForInterest(credit);

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

        return interest;
    }

    public BigDecimal calculateCapital(Credit credit){

        int quotas = 0;

        Date lastPaymentDate = creditTransactionService.findLastPayment(credit);
        Calendar cal = Calendar.getInstance();
        cal.setTime(lastPaymentDate);
        lastPaymentDate = cal.getTime();

        Date currentPaymentDate = getInstance().getDate();
        currentPaymentDate = DateUtils.removeTime(currentPaymentDate);

        CreditState state = credit.getState();
        int amortize = credit.getAmortization();

        if (state.equals(CreditState.VIG)) {
            quotas = 1; //calculateQuotaVig(lastPaymentDate, currentPaymentDate, amortize/30);
        }else {
            if (state.equals(CreditState.VEN)) {
                quotas = calculateQuotasVen(lastPaymentDate, currentPaymentDate, amortize/30);
            }
        }
        return BigDecimalUtil.multiply(credit.getQuota(), BigDecimalUtil.toBigDecimal(quotas), 6);
    }

    public int calculateQuotasVen(Date lastPaymentDate, Date currentDate, int amortize){


        Calendar calendar = Calendar.getInstance();
        calendar.setTime(lastPaymentDate);

        Calendar calendarNext = Calendar.getInstance();
        calendarNext.setTime(lastPaymentDate);
        calendarNext.add(Calendar.MONTH, amortize);
        Date nextPaymentDate = calendarNext.getTime();

        int quotas = 1;

        System.out.println("--------------------");
        while (lastPaymentDate.before(currentDate) || lastPaymentDate.equals(currentDate)){

            if (lastPaymentDate.before(nextPaymentDate)){
                System.out.println("Last datee: " + lastPaymentDate + " quotas: " + quotas);
            }else{
                quotas++;
                System.out.println("Last date: " + lastPaymentDate + " quotas: " + quotas);
                calendarNext.add(Calendar.MONTH, amortize);
                nextPaymentDate = calendarNext.getTime();
            }

            calendar.add(Calendar.DAY_OF_YEAR, 1);
            lastPaymentDate = calendar.getTime();

        }

        System.out.println("--------------------");
        System.out.println("Last Payment   : " + lastPaymentDate);
        System.out.println("Current Payment: " + currentDate);

        return quotas;
    }

    public void calculateTotalAmount() {
        BigDecimal totalAmount = null;
        if (null != getInstance().getCapital() && null != getInstance().getInterest()) {
            totalAmount = BigDecimalUtil.sum(getInstance().getCapital(), getInstance().getInterest(), 6);
        }
        getInstance().setAmount(totalAmount);
    }

}


