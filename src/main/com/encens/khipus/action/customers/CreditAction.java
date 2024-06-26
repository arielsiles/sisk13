package com.encens.khipus.action.customers;

import com.encens.khipus.action.accounting.VoucherCreateAction;
import com.encens.khipus.action.accounting.reports.VoucherReportAction;
import com.encens.khipus.action.customers.reports.CreditReportAction;
import com.encens.khipus.exception.finances.FinancesCurrencyNotFoundException;
import com.encens.khipus.exception.finances.FinancesExchangeRateNotFoundException;
import com.encens.khipus.framework.action.GenericAction;
import com.encens.khipus.framework.action.Outcome;
import com.encens.khipus.model.customers.Credit;
import com.encens.khipus.model.customers.CreditState;
import com.encens.khipus.model.customers.CreditTransaction;
import com.encens.khipus.model.customers.Partner;
import com.encens.khipus.model.finances.CashAccount;
import com.encens.khipus.model.finances.FinancesCurrencyType;
import com.encens.khipus.model.finances.Voucher;
import com.encens.khipus.model.finances.VoucherDetail;
import com.encens.khipus.service.accouting.VoucherAccoutingService;
import com.encens.khipus.service.common.SequenceGeneratorService;
import com.encens.khipus.service.customers.CreditService;
import com.encens.khipus.service.customers.CreditTransactionService;
import com.encens.khipus.service.finances.CashAccountService;
import com.encens.khipus.service.finances.FinancesExchangeRateService;
import com.encens.khipus.util.BigDecimalUtil;
import com.encens.khipus.util.DateUtils;
import com.encens.khipus.util.MessageUtils;
import com.encens.khipus.util.finances.CashAccountUtil;
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
    @In
    private FinancesExchangeRateService financesExchangeRateService;

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
    private BigDecimal deferredQuota;
    private Date paymentDate = new Date();

    private Date transferDate;
    private String gloss = "TRASPASO DE CARTERA POR MOROSIDAD AL ";

    private CreditState creditStateFIN = CreditState.FIN;

    @Create
    public void init() {
        System.out.println("...........Inicializando credito......");
        getInstance().setQuota(BigDecimal.ZERO);
    }

    @Factory(value = "credit", scope = ScopeType.STATELESS)
    @Restrict("#{s:hasPermission('CREDIT','VIEW')}")
    public Credit initCredit() {
        //quotaValue = BigDecimal.ZERO;
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
        super.create();
        sequenceGeneratorService.nextCreditNumber(getInstance().getPartner().getId());

        approveTransfer();

        return Outcome.SUCCESS;
    }

    @Begin(nested = true, flushMode = FlushModeType.MANUAL)
    public String addCreditPayment() {
        setOp(OP_UPDATE);
        return creditTransactionAction.addCreditTransaction();
    }

    @Begin(nested = true, flushMode = FlushModeType.MANUAL)
    public String addNewGuarantor() {
        setOp(OP_UPDATE);
        return Outcome.SUCCESS;
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

    public void approveTransfer(){
        creditService.approveTransfer(getInstance());
        facesMessages.addFromResourceBundle(StatusMessage.Severity.INFO,"Credit.transfer.approvedMessage");
    }

    public void assignPartner(Partner partner){
        getInstance().setPartner(partner);
    }

    public void assignOriginCredit(Credit credit){
        getInstance().setOriginCredit(credit);
    }

    public void clearPartner(){
        getInstance().setPartner(null);
    }

    /**
     *
     * @param credit
     * @return La fecha siguiente posterior o igual al total pagado segun el plan de pagos del credito.
     */
    public Date findDateOfNextPayment(Credit credit) {
        Date result = null;
        //int paidQuotas = creditTransactionAction.calculatePaidQuotas(credit);
        int paidQuotas = creditTransactionAction.calculateQuotasForCriminal(credit); // Cuotas pagadas

        Collection<CreditReportAction.PaymentPlanData> paymentPlanDatas = creditReportAction.calculatePaymentPlan(credit);

        int i=1;

        List<CreditReportAction.PaymentPlanData> listPaymentPlan = new ArrayList<CreditReportAction.PaymentPlanData>();
        for (CreditReportAction.PaymentPlanData paymentPlanData : paymentPlanDatas)
            listPaymentPlan.add(paymentPlanData);

        if (paidQuotas == 0){

            result = credit.getGrantDate();

            if (credit.getNumberQuota() == 1)
                result = credit.getExpirationDate();

        }else {

        for (int j = 0; j<listPaymentPlan.size(); j++){
            if (i == paidQuotas || paidQuotas == 0) {
                result = DateUtils.parse(listPaymentPlan.get(j).getPaymentDate(), "dd/MM/yyyy");
                System.out.println("=======>>>> POSIBLE FECHA SIGUIENTE: " + listPaymentPlan.get(j).getPaymentDate() + " --> (Fecha sig. al total pagado)");
            }
            i++;
        }

        }

        System.out.println("-----------------------------");
        System.out.println("====> CAPITAL PAGADO: " + credit.getPreviousCode() + " " + BigDecimalUtil.subtract(credit.getAmount(), credit.getCapitalBalance(), 2));
        //System.out.println("====> CAPITAL HASTA QUOTA: " + paidQuotas);
        //System.out.println("====> FECHA ENCONTRADA: " + DateUtils.format(result, "dd/MM/yyyy"));
        System.out.println("-----------------------------");

        return result;
    }

    /**
     * Calcula la fecha siguiente de pago, posterior a las cuotas pagadas (revision ???)
     * @param credit
     * @param endPeriod
     * @return
     */
    public Date findDateOfNextPayment(Credit credit, Date endPeriod) {

        Date result = null;
        int paidQuotas = creditTransactionAction.getNextQuotaAfterPaid(credit, endPeriod);
        System.out.println("-a-a-a-a-a-a--> cuotas2: " + paidQuotas);
        if (paidQuotas <= 0)
            result = endPeriod;

        Collection<CreditReportAction.PaymentPlanData> paymentPlanDatas = creditReportAction.calculatePaymentPlan(credit);

        int i=1;

        List<CreditReportAction.PaymentPlanData> listPaymentPlan = new ArrayList<CreditReportAction.PaymentPlanData>();
        for (CreditReportAction.PaymentPlanData paymentPlanData : paymentPlanDatas) listPaymentPlan.add(paymentPlanData);

        for (int j = 0; j<listPaymentPlan.size(); j++){
            if (i == paidQuotas || paidQuotas == 0) {
                result = DateUtils.parse(listPaymentPlan.get(j).getPaymentDate(), "dd/MM/yyyy");
                System.out.println("-------=>>>> POSIBLE FECHA ok SIGUIENTE: " + listPaymentPlan.get(j).getPaymentDate());
            }
            i++;
        }

        return result;
    }


    /**
     * Calcula la cuota siguiente a la fecha dada, segun su plan de pagos
     * @param credit
     * @param date
     * @return
     */
    public Integer findNextQuotaOfPaymentPlan(Credit credit, Date date){

        Integer result = 0;
        Collection<CreditReportAction.PaymentPlanData> paymentPlanDatas = creditReportAction.calculatePaymentPlan(credit);
        for (CreditReportAction.PaymentPlanData paymentPlanData : paymentPlanDatas){
            Date planDate = DateUtils.parse(paymentPlanData.getPaymentDate(), "dd/MM/yyyy");
            if (planDate.compareTo(date) >= 0 ){
                result = paymentPlanData.getNro();
                break;
            }
        }
        return result;
    }

    /**
     * Calcula la fecha de la cuota siguiente a la fecha dada, segun su plan de pagos
     * @param credit
     * @param date
     * @return
     */
    public Date findNextDateOfPaymentPlan(Credit credit, Date date){

        Date result = date;
        Collection<CreditReportAction.PaymentPlanData> paymentPlanDatas = creditReportAction.calculatePaymentPlan(credit);
        for (CreditReportAction.PaymentPlanData paymentPlanData : paymentPlanDatas){
            Date planDate = DateUtils.parse(paymentPlanData.getPaymentDate(), "dd/MM/yyyy");
            if (planDate.compareTo(date) >= 0 ){
                result = planDate;
                break;
            }
        }
        return result;
    }


    @End
    public String generateTransferCredit(){

        String outcome = Outcome.FAIL;
        BigDecimal exchangeRate = getExchangeRate();

        Voucher voucher = new Voucher();
        voucher.setDocumentType("CD");
        voucher.setDate(this.transferDate);
        voucher.setGloss(this.gloss);

        System.out.println("-------------APERTURA DE CREDITOS-------------");

        for (Credit credit : creditService.getUnfinishedCredits()){

            System.out.println(credit.getAmount() + " - " + credit.getCapitalBalance() + " - " + credit.getPartner().getFullName());
            int paidQuotas = creditTransactionAction.calculatePaidQuotas(credit);
            System.out.println("----> Coutas Pagadas: " + paidQuotas);

            Collection<CreditReportAction.PaymentPlanData> paymentPlanDatas = creditReportAction.calculatePaymentPlan(credit);

            Integer i=1;
            Date paidDate = null;
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
                        System.out.println("===> !!!!CREDITO VIGENTE!!!!");
                        if (credit.getState().equals(CreditState.VEN)){
                            addVoucherDetailVig(credit, voucher);
                            creditService.changeCreditState(credit, CreditState.VIG);
                            //System.out.println("=-=-=-> STATE: " + cont + " - " + CreditState.VIG);
                            cont++;
                        }
                    }
                    if (diffDays >= 1 &&  diffDays <= 90) {

                        System.out.println("===> CREDITO VENCIDO...");
                        if (!credit.getState().equals(CreditState.VEN)){
                            addVoucherDetailVen(credit, voucher);
                            creditService.changeCreditState(credit, CreditState.VEN);
                            //System.out.println("=-=-=-> STATE: " + cont + " - " + CreditState.VEN);
                            cont++;
                        }

                    }
                    if (diffDays >= 91) {
                        System.out.println("===> CREDITO EJECUCION...");
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

        /*for (CreditReportAction.PaymentPlanData paymentPlanData : paymentPlanDatas){

            if (i == paidQuotas || paidQuotas == 0) {
                //System.out.println("=====>>>> FECHA PAID: " + paymentPlanData.getPaymentDate());
                paidDate = DateUtils.parse(paymentPlanData.getPaymentDate(), "dd/MM/yyyy");

                Calendar cal = Calendar.getInstance();
                cal.setTime(paidDate);
                if (paidQuotas == 0) cal.setTime(credit.getGrantDate());

                cal.add(Calendar.DAY_OF_MONTH, credit.getAmortization());
                Date nextPaidDate = cal.getTime();

                Long diffDays = DateUtils.differenceBetween(nextPaidDate, currentDate, TimeUnit.DAYS);
                System.out.println("*******000==> CALCULATE EXPIRED DAYS: " +
                        DateUtils.format(nextPaidDate, "dd/MM/yyyy") + " - " +
                        DateUtils.format(currentDate, "dd/MM/yyyy") + " - DIFF: " + diffDays);


                result = diffDays;
            }
            i++;
        }*/

        Date nextPayment = findDateOfNextPayment(credit, currentDate);
        // Long diffDays = DateUtils.differenceBetween(nextPayment, currentDate, TimeUnit.DAYS);
        Long dias     = DateUtils.daysBetween(nextPayment, currentDate) - 1;
        result = dias;
        System.out.println();
        System.out.println("++++++++=====> CALCULATE EXPIRED DAYS: " + credit.getPreviousCode() + " " +
                DateUtils.format(nextPayment, "dd/MM/yyyy") + " - " +
                DateUtils.format(currentDate, "dd/MM/yyyy") + " - DIFF: " + dias);

        return  result;

    }


    //private void addVoucherDetailVigVen(Credit credit, Voucher voucher){
    private void addVoucherDetailVen(Credit credit, Voucher voucher){

        /*VoucherDetail detailExpiredAccount = new VoucherDetail();
        detailExpiredAccount.setAccount(credit.getCreditType().getExpiredAccountCode());
        detailExpiredAccount.setCashAccount(credit.getCreditType().getExpiredAccount());
        detailExpiredAccount.setDebit(credit.getCapitalBalance());
        detailExpiredAccount.setCredit(BigDecimal.ZERO);*/
        VoucherDetail detailExpiredAccount = CashAccountUtil.createVoucherDetail(   credit.getCreditType().getExpiredAccount(),
                                                                                    credit.getCapitalBalance(),
                                                                                    credit.getFinancesCurrencyType(), true, false, getExchangeRate());
        detailExpiredAccount.setCreditPartner(credit);

        VoucherDetail detailAccount = new VoucherDetail();
        CashAccount cashAccount = null;
        if (credit.getState().equals(CreditState.VIG)){
            /*detailAccount.setAccount(credit.getCreditType().getCurrentAccountCode());
            detailAccount.setCashAccount(credit.getCreditType().getCurrentAccount());*/
            cashAccount = credit.getCreditType().getCurrentAccount();
        }
        if (credit.getState().equals(CreditState.EJE)){
            /*detailAccount.setAccount(credit.getCreditType().getExecutedAccountCode());
            detailAccount.setCashAccount(credit.getCreditType().getExecutedAccount());*/
            cashAccount = credit.getCreditType().getExecutedAccount();
        }
        /*detailAccount.setDebit(BigDecimal.ZERO);
        detailAccount.setCredit(credit.getCapitalBalance());*/
        detailAccount = CashAccountUtil.createVoucherDetail(cashAccount, credit.getCapitalBalance(), credit.getFinancesCurrencyType(), false, true, getExchangeRate());
        detailAccount.setCreditPartner(credit);

        voucher.getDetails().add(detailExpiredAccount);
        voucher.getDetails().add(detailAccount);
    }

    //private void addVoucherDetailVenEje(Credit credit, Voucher voucher){
    private void addVoucherDetailEje(Credit credit, Voucher voucher){

        /*VoucherDetail detailExecutedAccount = new VoucherDetail();
        detailExecutedAccount.setAccount(credit.getCreditType().getExecutedAccountCode());
        detailExecutedAccount.setCashAccount(credit.getCreditType().getExecutedAccount());
        detailExecutedAccount.setDebit(credit.getCapitalBalance());
        detailExecutedAccount.setCredit(BigDecimal.ZERO);*/
        VoucherDetail detailExecutedAccount = CashAccountUtil.createVoucherDetail(  credit.getCreditType().getExecutedAccount(),
                                                                                    credit.getCapitalBalance(),
                                                                                    credit.getFinancesCurrencyType(), true, false, getExchangeRate());

        detailExecutedAccount.setCreditPartner(credit);

        VoucherDetail detailAccount = new VoucherDetail();
        CashAccount cashAccount = null;
        if (credit.getState().equals(CreditState.VIG)){
            //detailAccount.setAccount(credit.getCreditType().getCurrentAccountCode());
            //detailAccount.setCashAccount(credit.getCreditType().getCurrentAccount());
            cashAccount = credit.getCreditType().getCurrentAccount();
        }
        if (credit.getState().equals(CreditState.VEN)){
            //detailAccount.setAccount(credit.getCreditType().getExpiredAccountCode());
            //detailAccount.setCashAccount(credit.getCreditType().getExpiredAccount());
            cashAccount = credit.getCreditType().getExpiredAccount();
        }
        /*detailAccount.setDebit(BigDecimal.ZERO);
        detailAccount.setCredit(credit.getCapitalBalance());*/

        detailAccount = CashAccountUtil.createVoucherDetail(cashAccount, credit.getCapitalBalance(), credit.getFinancesCurrencyType(), false, true, getExchangeRate());
        detailAccount.setCreditPartner(credit);

        voucher.getDetails().add(detailExecutedAccount);
        voucher.getDetails().add(detailAccount);
    }

    //private void addVoucherDetailVenVig(Credit credit, Voucher voucher){
    private void addVoucherDetailVig(Credit credit, Voucher voucher){

        VoucherDetail detailCurrentAccount = CashAccountUtil.createVoucherDetail(credit.getCreditType().getCurrentAccount(),
                                                                                credit.getCapitalBalance(),
                                                                                credit.getFinancesCurrencyType(), true, false, getExchangeRate());
        //VoucherDetail detailCurrentAccount = new VoucherDetail();
        //detailCurrentAccount.setAccount(credit.getCreditType().getCurrentAccountCode());
        //detailCurrentAccount.setCashAccount(credit.getCreditType().getCurrentAccount());
        //detailCurrentAccount.setDebit(credit.getCapitalBalance());
        //detailCurrentAccount.setCredit(BigDecimal.ZERO);
        detailCurrentAccount.setCreditPartner(credit);

        VoucherDetail detailAccount = new VoucherDetail();
        CashAccount cashAccount = null;
        if (credit.getState().equals(CreditState.VEN)){
            //detailAccount.setAccount(credit.getCreditType().getExpiredAccountCode());
            //detailAccount.setCashAccount(credit.getCreditType().getExpiredAccount());
            cashAccount = credit.getCreditType().getExpiredAccount();
        }
        if (credit.getState().equals(CreditState.EJE)){
            //detailAccount.setAccount(credit.getCreditType().getExecutedAccountCode());
            //detailAccount.setCashAccount(credit.getCreditType().getExecutedAccount());
            cashAccount = credit.getCreditType().getExecutedAccount();
        }

        //detailAccount.setDebit(BigDecimal.ZERO);
        //detailAccount.setCredit(credit.getCapitalBalance());

        detailAccount = CashAccountUtil.createVoucherDetail(cashAccount, credit.getCapitalBalance(), credit.getFinancesCurrencyType(), false, true, getExchangeRate());
        detailAccount.setCreditPartner(credit);

        voucher.getDetails().add(detailCurrentAccount);
        voucher.getDetails().add(detailAccount);
    }

    public void calculateQuota(){
        Credit credit = getInstance();
        quotaValue = BigDecimal.ZERO;

        if (credit.getNumberQuota() != 0) {
            //quotaValue = BigDecimalUtil.divide(credit.getAmount(), BigDecimalUtil.toBigDecimal(credit.getNumberQuota()), 2);
            credit.setQuota(BigDecimalUtil.divide(credit.getAmount(), BigDecimalUtil.toBigDecimal(credit.getNumberQuota()), 2));
        }

    }

    public void calculateDeferredQuota(){
        Credit credit = getInstance();
        deferredQuota = BigDecimal.ZERO;

        if (credit.getNumberQuota() != 0)
            credit.setDeferredQuota(BigDecimalUtil.divide(credit.getDeferredAmount(), BigDecimalUtil.toBigDecimal(credit.getNumberQuota()), 2));

    }

    public void calculateDeferredQuota2(){
        Credit credit = getInstance();
        deferredQuota = BigDecimal.ZERO;

        if (credit.getNumberQuota() != 0)
            deferredQuota = BigDecimalUtil.divide(credit.getDeferredAmount(), BigDecimalUtil.toBigDecimal(credit.getNumberQuota()), 2);

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

    public CreditState getCreditStateFIN() {
        return creditStateFIN;
    }

    public void setCreditStateFIN(CreditState creditStateFIN) {
        this.creditStateFIN = creditStateFIN;
    }

    private void addFinancesCurrencyNotFoundMessage() {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                "FixedAssets.FinancesCurrencyNotFoundException");
    }

    private void addFinancesExchangeRateNotFoundExceptionMessage() {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.INFO,
                "FixedAssets.FinancesExchangeRateNotFoundException");
    }

    public BigDecimal getExchangeRate(){
        BigDecimal exchangeRate = BigDecimal.ZERO;
        try {
            exchangeRate = financesExchangeRateService.findLastExchangeRateByCurrency(FinancesCurrencyType.D.toString());
        }catch (FinancesExchangeRateNotFoundException e){
            addFinancesExchangeRateNotFoundExceptionMessage();
        }catch (FinancesCurrencyNotFoundException e){
            addFinancesCurrencyNotFoundMessage();
        }

        return exchangeRate;
    }

    public BigDecimal getDeferredQuota() {
        return deferredQuota;
    }

    public void setDeferredQuota(BigDecimal deferredQuota) {
        this.deferredQuota = deferredQuota;
    }
}
