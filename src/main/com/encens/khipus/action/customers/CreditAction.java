package com.encens.khipus.action.customers;

import com.encens.khipus.action.accounting.VoucherCreateAction;
import com.encens.khipus.action.accounting.reports.VoucherReportAction;
import com.encens.khipus.action.customers.reports.CreditReportAction;
import com.encens.khipus.framework.action.GenericAction;
import com.encens.khipus.framework.action.Outcome;
import com.encens.khipus.model.customers.Credit;
import com.encens.khipus.model.customers.CreditState;
import com.encens.khipus.model.customers.CreditTransaction;
import com.encens.khipus.model.customers.Partner;
import com.encens.khipus.model.finances.Voucher;
import com.encens.khipus.model.finances.VoucherDetail;
import com.encens.khipus.service.accouting.VoucherAccoutingService;
import com.encens.khipus.service.common.SequenceGeneratorService;
import com.encens.khipus.service.customers.CreditService;
import com.encens.khipus.service.customers.CreditTransactionService;
import com.encens.khipus.service.finances.CashAccountService;
import com.encens.khipus.util.BigDecimalUtil;
import com.encens.khipus.util.DateUtils;
import com.encens.khipus.util.MessageUtils;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.*;
import org.jboss.seam.annotations.security.Restrict;
import org.jboss.seam.international.StatusMessage;

import java.math.BigDecimal;
import java.util.*;
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
    private VoucherReportAction voucherReportAction;
    @In(create = true)
    private CreditReportAction creditReportAction;

    private BigDecimal totalPayment;
    private BigDecimal quotaValue;
    private Date paymentDate = new Date();

    private Date transferDate;
    private String gloss = "TRASPASO DE CARTERA POR MOROSIDAD AL ";

    @Factory(value = "credit", scope = ScopeType.STATELESS)
    @Restrict("#{s:hasPermission('CREDIT','VIEW')}")
    public Credit initCredit() {
        quotaValue = BigDecimal.ZERO;
        return getInstance();
    }

    @Factory(value = "creditStateEnum")
    public CreditState[] getExperienceType() {
        return CreditState.values();
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
        return creditTransactionAction.addCreditTransactionPayout(getInstance());
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

    //public void findDateOfNextPayment(Credit credit, Collection<CreditReportAction.PaymentPlanData> paymentPlanDatas) {
    public Date findDateOfNextPayment(Credit credit) {

        Date result = null;

        //int paidQuotas = creditTransactionAction.calculatePaidQuotas(credit);
        int paidQuotas = creditTransactionAction.calculateQuotasForCriminal(credit);

        Collection<CreditReportAction.PaymentPlanData> paymentPlanDatas = creditReportAction.calculatePaymentPlan(credit);

        int i=1;

        List<CreditReportAction.PaymentPlanData> listPaymentPlan = new ArrayList<CreditReportAction.PaymentPlanData>();
        for (CreditReportAction.PaymentPlanData paymentPlanData : paymentPlanDatas) listPaymentPlan.add(paymentPlanData);

        for (int j = 0; j<listPaymentPlan.size(); j++){
            if (i == paidQuotas || paidQuotas == 0) {
                result = DateUtils.parse(listPaymentPlan.get(j).getPaymentDate(), "dd/MM/yyyy");
                System.out.println("=====>>>> POSIBLE FECHA SIGUIENTE: " + listPaymentPlan.get(j).getPaymentDate());
            }
            i++;
        }

        /*for (CreditReportAction.PaymentPlanData paymentPlanData : paymentPlanDatas){
            if (i == paidQuotas || paidQuotas == 0) {
                System.out.println("=====>>>> POSIBLE FECHA SIGUIENTE: " + paymentPlanData.getPaymentDate());
                result = DateUtils.parse(paymentPlanData.getPaymentDate(), "dd/MM/yyyy");
            }
            i++;
        }*/

        System.out.println("-----------------------------");
        System.out.println("====> CAPITAL PAGADO: " + BigDecimalUtil.subtract(credit.getAmount(), credit.getCapitalBalance(), 2));
        System.out.println("====> CAPITAL HASTA QUOTA: " + paidQuotas);
        System.out.println("====> FECHA ENCONTRADA: " + DateUtils.format(result, "dd/MM/yyyy"));
        System.out.println("-----------------------------");

        return result;
    }

    @End
    public String generateTransferCredit(){

        String outcome = Outcome.FAIL;

        Voucher voucher = new Voucher();
        voucher.setDocumentType("CD");
        voucher.setDate(this.transferDate);
        voucher.setGloss(this.gloss);

        System.out.println("-------------APERTURA DE CREDITOS-------------");
        for (Credit credit : creditService.getAllCredits()){

            System.out.println(credit.getAmount() + " - " + credit.getCapitalBalance() + " - " + credit.getPartner().getFullName());
            //Date lastPaymentDate = creditTransactionService.findLastPayment(credit);

            int paidQuotas = creditTransactionAction.calculatePaidQuotas(credit);
            System.out.println("----> Coutas Pagadas: " + paidQuotas);

            Collection<CreditReportAction.PaymentPlanData> paymentPlanDatas = creditReportAction.calculatePaymentPlan(credit);

            /** **/
            //findDateOfNextPayment(credit, paymentPlanDatas);
            /** **/

            Integer i=1;
            Date paidDate = null;
            //Date currentDate = new Date();
            Date currentDate = this.transferDate;

            int cont = 1;

            for (CreditReportAction.PaymentPlanData paymentPlanData : paymentPlanDatas){

                if (i == paidQuotas || paidQuotas == 0) {
                    //System.out.println("=====>>>> FECHA PAID: " + paymentPlanData.getPaymentDate());
                    paidDate = DateUtils.parse(paymentPlanData.getPaymentDate(), "dd/MM/yyyy");

                    Calendar cal = Calendar.getInstance();
                    cal.setTime(paidDate);
                    if (paidQuotas == 0) cal.setTime(credit.getGrantDate());

                    cal.add(Calendar.DAY_OF_MONTH, credit.getAmortization());
                    Date nextPaidDate = cal.getTime();

                    long diffDays = DateUtils.differenceBetween(nextPaidDate, currentDate, TimeUnit.DAYS);

                    //System.out.println("===>>> Diff DAYS: " + diffDays);

                    if (diffDays <= 0) {
                        //System.out.println("===> !!!!CREDITO VIGENTE!!!!");
                        if (credit.getState().equals(CreditState.VEN)){
                            addVoucherDetailVig(credit, voucher);
                            creditService.changeCreditState(credit, CreditState.VIG);
                            //System.out.println("=-=-=-> STATE: " + cont + " - " + CreditState.VIG);
                            cont++;
                        }
                    }
                    if (diffDays >= 1 &&  diffDays <= 90) {

                        //System.out.println("===> CREDITO VENCIDO...");
                        if (!credit.getState().equals(CreditState.VEN)){
                            addVoucherDetailVen(credit, voucher);
                            creditService.changeCreditState(credit, CreditState.VEN);
                            //System.out.println("=-=-=-> STATE: " + cont + " - " + CreditState.VEN);
                            cont++;
                        }

                    }
                    if (diffDays >= 91) {
                        //System.out.println("===> CREDITO EJECUCION...");
                        if (!credit.getState().equals(CreditState.EJE)) {
                            addVoucherDetailEje(credit, voucher);
                            creditService.changeCreditState(credit, CreditState.EJE);
                            System.out.println("=-=-=-> STATE: " + cont + " - " + CreditState.EJE);
                            cont++;
                        }
                    }
                }

                i++;
            }
            System.out.println("................................................");
        }

        if (voucher.getDetails().size() > 0) {
            voucherAccoutingService.saveVoucher(voucher);
            outcome = Outcome.SUCCESS;
        }

        if (outcome.equals(Outcome.FAIL))
            facesMessages.addFromResourceBundle(StatusMessage.Severity.WARN,"Credit.message.transferWarn");
        if (outcome.equals(Outcome.SUCCESS))
            facesMessages.addFromResourceBundle(StatusMessage.Severity.INFO,"Credit.message.transferSuccess");

        return  outcome;

    }


    public Long calculateExpiredDays(Credit credit, Date dateTransaction){

        Long result = new Long(0);

        System.out.println(credit.getAmount() + " - " + credit.getCapitalBalance() + " - " + credit.getPartner().getFullName());
        int paidQuotas = creditTransactionAction.calculatePaidQuotas(credit);
        System.out.println("----> Coutas Pagadas: " + paidQuotas);
        Collection<CreditReportAction.PaymentPlanData> paymentPlanDatas = creditReportAction.calculatePaymentPlan(credit);
        Integer i=1;
        Date paidDate = null;
        Date currentDate = dateTransaction;

        for (CreditReportAction.PaymentPlanData paymentPlanData : paymentPlanDatas){

            if (i == paidQuotas || paidQuotas == 0) {
                //System.out.println("=====>>>> FECHA PAID: " + paymentPlanData.getPaymentDate());
                paidDate = DateUtils.parse(paymentPlanData.getPaymentDate(), "dd/MM/yyyy");

                Calendar cal = Calendar.getInstance();
                cal.setTime(paidDate);
                if (paidQuotas == 0) cal.setTime(credit.getGrantDate());

                cal.add(Calendar.DAY_OF_MONTH, credit.getAmortization());
                Date nextPaidDate = cal.getTime();

                Long diffDays = DateUtils.differenceBetween(nextPaidDate, currentDate, TimeUnit.DAYS);

                //System.out.println("===>>> Diff DAYS: " + diffDays);

                result = diffDays;

                if (diffDays <= 0) {
                    //System.out.println("===> !!!!CREDITO VIGENTE!!!!");
                    if (credit.getState().equals(CreditState.VEN)){
                        //creditService.changeCreditState(credit, CreditState.VIG);
                    }
                }
                if (diffDays >= 1 &&  diffDays <= 90) {

                    //System.out.println("===> CREDITO VENCIDO...");
                    if (!credit.getState().equals(CreditState.VEN)){
                        //creditService.changeCreditState(credit, CreditState.VEN);
                    }
                }
                if (diffDays >= 91) {
                    //System.out.println("===> CREDITO EJECUCION...");
                    if (!credit.getState().equals(CreditState.EJE)) {
                        //creditService.changeCreditState(credit, CreditState.EJE);
                    }
                }
            }
            i++;
        }

        return  result;

    }


    //private void addVoucherDetailVigVen(Credit credit, Voucher voucher){
    private void addVoucherDetailVen(Credit credit, Voucher voucher){

        VoucherDetail detailExpiredAccount = new VoucherDetail();
        detailExpiredAccount.setAccount(credit.getCreditType().getExpiredAccountCode());
        detailExpiredAccount.setCashAccount(credit.getCreditType().getExpiredAccount());
        detailExpiredAccount.setDebit(credit.getCapitalBalance());
        detailExpiredAccount.setCredit(BigDecimal.ZERO);
        detailExpiredAccount.setCreditPartner(credit);

        VoucherDetail detailAccount = new VoucherDetail();
        if (credit.getState().equals(CreditState.VIG)){
            detailAccount.setAccount(credit.getCreditType().getCurrentAccountCode());
            detailAccount.setCashAccount(credit.getCreditType().getCurrentAccount());
        }
        if (credit.getState().equals(CreditState.EJE)){
            detailAccount.setAccount(credit.getCreditType().getExecutedAccountCode());
            detailAccount.setCashAccount(credit.getCreditType().getExecutedAccount());
        }
        detailAccount.setDebit(BigDecimal.ZERO);
        detailAccount.setCredit(credit.getCapitalBalance());
        detailAccount.setCreditPartner(credit);

        voucher.getDetails().add(detailExpiredAccount);
        voucher.getDetails().add(detailAccount);
    }

    //private void addVoucherDetailVenEje(Credit credit, Voucher voucher){
    private void addVoucherDetailEje(Credit credit, Voucher voucher){

        VoucherDetail detailExecutedAccount = new VoucherDetail();
        detailExecutedAccount.setAccount(credit.getCreditType().getExecutedAccountCode());
        detailExecutedAccount.setCashAccount(credit.getCreditType().getExecutedAccount());
        detailExecutedAccount.setDebit(credit.getCapitalBalance());
        detailExecutedAccount.setCredit(BigDecimal.ZERO);
        detailExecutedAccount.setCreditPartner(credit);

        VoucherDetail detailAccount = new VoucherDetail();
        if (credit.getState().equals(CreditState.VIG)){
            detailAccount.setAccount(credit.getCreditType().getCurrentAccountCode());
            detailAccount.setCashAccount(credit.getCreditType().getCurrentAccount());
        }
        if (credit.getState().equals(CreditState.VEN)){
            detailAccount.setAccount(credit.getCreditType().getExpiredAccountCode());
            detailAccount.setCashAccount(credit.getCreditType().getExpiredAccount());
        }
        detailAccount.setDebit(BigDecimal.ZERO);
        detailAccount.setCredit(credit.getCapitalBalance());
        detailAccount.setCreditPartner(credit);

        voucher.getDetails().add(detailExecutedAccount);
        voucher.getDetails().add(detailAccount);
    }

    //private void addVoucherDetailVenVig(Credit credit, Voucher voucher){
    private void addVoucherDetailVig(Credit credit, Voucher voucher){

        VoucherDetail detailCurrentAccount = new VoucherDetail();
        detailCurrentAccount.setAccount(credit.getCreditType().getCurrentAccountCode());
        detailCurrentAccount.setCashAccount(credit.getCreditType().getCurrentAccount());
        detailCurrentAccount.setDebit(credit.getCapitalBalance());
        detailCurrentAccount.setCredit(BigDecimal.ZERO);
        detailCurrentAccount.setCreditPartner(credit);

        VoucherDetail detailAccount = new VoucherDetail();
        if (credit.getState().equals(CreditState.VEN)){
            detailAccount.setAccount(credit.getCreditType().getExpiredAccountCode());
            detailAccount.setCashAccount(credit.getCreditType().getExpiredAccount());
        }
        if (credit.getState().equals(CreditState.EJE)){
            detailAccount.setAccount(credit.getCreditType().getExecutedAccountCode());
            detailAccount.setCashAccount(credit.getCreditType().getExecutedAccount());
        }
        detailAccount.setDebit(BigDecimal.ZERO);
        detailAccount.setCredit(credit.getCapitalBalance());
        detailAccount.setCreditPartner(credit);

        voucher.getDetails().add(detailCurrentAccount);
        voucher.getDetails().add(detailAccount);
    }

    public void calculateQuota(){
        Credit credit = getInstance();
        quotaValue = BigDecimal.ZERO;

        if (credit.getNumberQuota() != 0)
            quotaValue = BigDecimalUtil.divide(credit.getAmount(), BigDecimalUtil.toBigDecimal(credit.getNumberQuota()), 2);

    }

    public void generateTransactionReport(CreditTransaction creditTransaction){

        voucherReportAction.setDescription(MessageUtils.getMessage("Voucher.message.descriptionCredit") + creditTransaction.getCapitalBalance());
        voucherCreateAction.generateReport(creditTransaction.getVoucher());
        //voucherReportAction.generateReport(creditTransaction.getVoucher());

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

    public Date getTransferDate() {
        return transferDate;
    }

    public void setTransferDate(Date transferDate) {
        this.transferDate = transferDate;
    }

    public String getGloss() {
        return gloss;
    }

    public void setGloss(String gloss) {
        this.gloss = gloss;
    }
}
