package com.encens.khipus.action.customers;

import com.encens.khipus.action.accounting.VoucherCreateAction;
import com.encens.khipus.action.customers.reports.CreditReportAction;
import com.encens.khipus.exception.EntryDuplicatedException;
import com.encens.khipus.framework.action.GenericAction;
import com.encens.khipus.framework.action.Outcome;
import com.encens.khipus.model.accounting.DocType;
import com.encens.khipus.model.customers.*;
import com.encens.khipus.model.finances.Voucher;
import com.encens.khipus.model.finances.VoucherDetail;
import com.encens.khipus.service.accouting.VoucherAccoutingService;
import com.encens.khipus.service.common.SequenceGeneratorService;
import com.encens.khipus.service.customers.CreditService;
import com.encens.khipus.service.customers.CreditTransactionService;
import com.encens.khipus.service.finances.CashAccountService;
import com.encens.khipus.util.BigDecimalUtil;
import com.encens.khipus.util.DateUtils;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.*;
import org.jboss.seam.annotations.security.Restrict;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Actions for Credit
 *
 * @author:
 */

@Name("creditAction")
@Scope(ScopeType.CONVERSATION)
public class CreditAction extends GenericAction<Credit> {

    @In
    private CreditTransactionService creditTransactionService;
    @In
    private CreditService creditService;
    @In
    private SequenceGeneratorService sequenceGeneratorService;
    @In
    private CashAccountService cashAccountService;
    @In
    private VoucherAccoutingService voucherAccoutingService;

    @In(create = true)
    private CreditTransactionAction creditTransactionAction;
    @In(create = true)
    private VoucherCreateAction voucherCreateAction;
    @In(create = true)
    private CreditReportAction creditReportAction;

    private BigDecimal totalPayment;
    private BigDecimal quotaValue;
    private Date paymentDate = new Date();

    @Factory(value = "credit", scope = ScopeType.STATELESS)
    @Restrict("#{s:hasPermission('CREDIT','VIEW')}")
    public Credit initCredit() {
        quotaValue = BigDecimal.ZERO;
        return getInstance();
    }

    @Override
    @Begin(join=true, ifOutcome = Outcome.SUCCESS, flushMode = FlushModeType.MANUAL)
    public String select(Credit instance) {
        String outCome = super.select(instance);
        return outCome;
    }

    @Override
    @End
    public String create() {
        Credit instance = getInstance();
        Partner partner = instance.getPartner();
        Integer num = partner.getNumberCredit()+1;
        String creditNumber = partner.getProductiveZone().getNumber()+'-'+partner.getNumber()+'-'+num;
        getInstance().setCode(creditNumber);
        getInstance().setState(CreditState.VIG);
        getInstance().setCapitalBalance(instance.getAmount());
        getInstance().setQuota(quotaValue);
        super.create();
        sequenceGeneratorService.nextCreditNumber(getInstance().getPartner().getId());
        return Outcome.SUCCESS;
    }

    @Begin(nested = true, flushMode = FlushModeType.MANUAL)
    public String addCreditPayment() {
        setOp(OP_UPDATE);
        return creditTransactionAction.addCreditTransaction();
    }

    @Begin(nested = true, flushMode = FlushModeType.MANUAL)
    public String addPayoutCredit() {
        setOp(OP_UPDATE);
        return creditTransactionAction.addCreditTransactionPayout();
    }

    @Begin(nested = true, flushMode = FlushModeType.MANUAL)
    public String selectCreditTransaction(CreditTransaction creditTransaction) {
        setOp(OP_UPDATE);
        return creditTransactionAction.select(creditTransaction);
    }


    public void assignPartner(Partner partner){
        getInstance().setPartner(partner);
    }

    public void clearPartner(){
        getInstance().setPartner(null);
    }

    public void checkCreditStatus(){
        System.out.println("-------------APERTURA DE CREDITOS-------------");
        for (Credit credit : creditService.getAllCredits()){

            System.out.println(credit.getAmount() + " - " + credit.getCapitalBalance() + " - " + credit.getPartner().getFullName());
            //Date lastPaymentDate = creditTransactionService.findLastPayment(credit);

            int paidQuotas = creditTransactionAction.calculatePaidQuotas(credit);
            System.out.println("----> Coutas Pagadas: " + paidQuotas);

            Collection<CreditReportAction.PaymentPlanData> paymentPlanDatas = creditReportAction.calculatePaymentPlan(credit);

            Integer i=1;
            Date paidDate = null;
            Date currentDate = new Date();

            for (CreditReportAction.PaymentPlanData paymentPlanData : paymentPlanDatas){

                if (i == paidQuotas || paidQuotas == 0) {
                    System.out.println("=====>>>> FECHA PAID: " + paymentPlanData.getPaymentDate());
                    paidDate = DateUtils.parse(paymentPlanData.getPaymentDate(), "dd/MM/yyyy");

                    Calendar cal = Calendar.getInstance();
                    cal.setTime(paidDate);
                    if (paidQuotas == 0) cal.setTime(credit.getGrantDate());

                    cal.add(Calendar.DAY_OF_MONTH, credit.getAmortization());
                    Date nextPaidDate = cal.getTime();

                    long diffDays = DateUtils.differenceBetween(nextPaidDate, currentDate, TimeUnit.DAYS);

                    System.out.println("===>>> Diff DAYS: " + diffDays);

                    if (diffDays <= 0) {
                        System.out.println("===> !!!!CREDITO VIGENTE!!!!");
                        creditService.changeCreditState(credit, CreditState.VIG);
                    }
                    if (diffDays >= 1 &&  diffDays <= 90) {
                        System.out.println("===> CREDITO VENCIDO...");
                        creditService.changeCreditState(credit, CreditState.VEN);
                    }
                    if (diffDays >= 91) {
                        System.out.println("===> CREDITO EJECUCION...");
                        creditService.changeCreditState(credit, CreditState.EJE);
                    }

                }

                i++;
            }
            /*for (CreditReportAction.PaymentPlanData paymentPlanData : paymentPlanDatas){

                if (paidQuotas == 0 && credit.getAmortization() > 30) {

                    Calendar cal = Calendar.getInstance();
                    cal.setTime(credit.getGrantDate());
                    cal.add(Calendar.DAY_OF_MONTH, credit.getAmortization());
                    Date nextPaidDate = cal.getTime();
                    long diffDays = DateUtils.differenceBetween(nextPaidDate, currentDate, TimeUnit.DAYS);
                    System.out.println("--->>> Diff DAYS: " + diffDays);

                    if (diffDays <= 0)
                        System.out.println("---> !!!!CREDITO VIGENTE!!!!");
                    if (diffDays >= 1 &&  diffDays <= 90)
                        System.out.println("---> CREDITO VENCIDO...");
                    if (diffDays >= 91)
                        System.out.println("---> CREDITO EJECUCION...");


                } else {
                    if (i == paidQuotas) {
                        System.out.println("=====>>>> FECHA PAID: " + paymentPlanData.getPaymentDate());
                        paidDate = DateUtils.parse(paymentPlanData.getPaymentDate(), "dd/MM/yyyy");

                        Calendar cal = Calendar.getInstance();
                        cal.setTime(paidDate);
                        cal.add(Calendar.MONTH, credit.getAmortization() / 30);
                        Date nextPaidDate = cal.getTime();

                        long diffDays = DateUtils.differenceBetween(nextPaidDate, currentDate, TimeUnit.DAYS);

                        System.out.println("===>>> Diff DAYS: " + diffDays);

                        if (diffDays <= 0)
                            System.out.println("===> !!!!CREDITO GIGENTE!!!!");
                        if (diffDays >= 1 &&  diffDays <= 90)
                            System.out.println("===> CREDITO VENCIDO...");
                        if (diffDays >= 91)
                            System.out.println("===> CREDITO EJECUCION...");

                    }
                }
                i++;
            }*/

            System.out.println("................................................");

        }

    }

    public void calculateQuota(){
        Credit credit = getInstance();
        quotaValue = BigDecimal.ZERO;

        if (credit.getNumberQuota() != 0)
            quotaValue = BigDecimalUtil.divide(credit.getAmount(), BigDecimalUtil.toBigDecimal(credit.getNumberQuota()), 2);

    }

    public Date getPaymentDate() {
        return paymentDate;
    }

    public void setPaymentDate(Date paymentDate) {
        this.paymentDate = paymentDate;
    }

    public BigDecimal getTotalPayment() {
        return totalPayment;
    }

    public void setTotalPayment(BigDecimal totalPayment) {
        this.totalPayment = totalPayment;
    }

    public BigDecimal getQuotaValue() {
        return quotaValue;
    }

    public void setQuotaValue(BigDecimal quotaValue) {
        this.quotaValue = quotaValue;
    }
}
