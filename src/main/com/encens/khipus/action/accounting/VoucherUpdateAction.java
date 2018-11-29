package com.encens.khipus.action.accounting;

import com.encens.khipus.action.accounting.reports.VoucherReportAction;
import com.encens.khipus.action.purchases.PurchaseDocumentAction;
import com.encens.khipus.framework.action.GenericAction;
import com.encens.khipus.framework.action.Outcome;
import com.encens.khipus.model.accounting.DocType;
import com.encens.khipus.model.customers.Client;
import com.encens.khipus.model.finances.*;
import com.encens.khipus.model.purchases.PurchaseDocument;
import com.encens.khipus.service.accouting.VoucherAccoutingService;
import com.encens.khipus.service.finances.CashAccountService;
import com.encens.khipus.service.finances.VoucherService;
import com.encens.khipus.util.BigDecimalUtil;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.*;
import org.jboss.seam.international.StatusMessage;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * @author
 * @version 3.0
 */
@Name("voucherUpdateAction")
@Scope(ScopeType.CONVERSATION)
public class VoucherUpdateAction extends GenericAction<Voucher> {

    public static String APPROVED_OUTCOME = "Approved";
    public static String ANNUL_OUTCOME = "Annul";

    CashAccount cashAccount = null;
    private BigDecimal debit = new BigDecimal("0.00");
    private BigDecimal credit = new BigDecimal("0.00");
    private String documentTypeCode = "";

    private DocType docType = new DocType();
    private Voucher voucher;

    //private List<PurchaseDocument> purchaseDocumentList = new ArrayList<PurchaseDocument>();
    private List<VoucherDetail> voucherDetails;

    private List<CashAccount> cashAccounts = new ArrayList<CashAccount>();

    private BigDecimal totalDebit = new BigDecimal("0.00");
    private BigDecimal totalCredit = new BigDecimal("0.00");

    private Provider provider;
    private CashAccount account;
    private Client client;

    private Boolean fiscalCredit = false;

    @In
    private CashAccountService cashAccountService;
    @In
    private VoucherAccoutingService voucherAccoutingService;
    @In
    private VoucherService voucherService;
    @In(create = true)
    private PurchaseDocumentAction purchaseDocumentAction;
    @In(create = true)
    private VoucherReportAction voucherReportAction;

    @Factory(value = "voucherUpdate", scope = ScopeType.STATELESS)
    public Voucher initVoucher() {
        return getInstance();
    }

    @Override
    @Begin(ifOutcome = Outcome.SUCCESS, flushMode = FlushModeType.MANUAL)
    public String select(Voucher instance) {
        String outCome = super.select(instance);
        this.voucher = instance;
        this.docType = voucherService.getDocType(voucher.getDocumentType());
        setVoucherDetails(voucherAccoutingService.getVoucherDetailList(voucher));

        return outCome;
    }

    public void generateReport(Voucher instance){
        select(instance);
        voucherReportAction.generateReport(instance);
    }

    @Override
    @End
    public String update(){

        voucher.setDocumentType(docType.getName());
        voucher.setDetails(voucherDetails);
        BigDecimal totalD = new BigDecimal("0.00");
        BigDecimal totalC = new BigDecimal("0.00");

        try {
            for (VoucherDetail voucherDetail : voucherDetails) {
                totalD = totalD.add(voucherDetail.getDebit());
                totalC = totalC.add(voucherDetail.getCredit());
            }
            System.out.println("__ __ Total D: " + totalD);
            System.out.println("__ __ Total C: " + totalC);

            if(totalD.doubleValue() == 0.00 || totalC.doubleValue() == 0.00){
                facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,"Voucher.message.incorrectAccountingEntry");
                return Outcome.REDISPLAY;
            }

            if(totalD.doubleValue() != totalC.doubleValue()){
                facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,"Voucher.message.incorrectAccountingEntry");
                return Outcome.REDISPLAY;
            }

            voucherAccoutingService.updateVoucher(voucher);

            facesMessages.addFromResourceBundle(StatusMessage.Severity.INFO,"Voucher.message.updated");
            return Outcome.SUCCESS;

        } catch (Exception e) {
            return Outcome.FAIL;
        }
    }

    public String createPurchaseDocument() {
        return purchaseDocumentAction.createFromAccounting();
    }

    @End(beforeRedirect = true)
    public String approvePurchaseDocument() {
        return purchaseDocumentAction.approveFromAccounting();
    }

    @End(beforeRedirect = true)
    public String nullify() {
        return purchaseDocumentAction.nullify();
    }

    public String approveVoucher(){

        voucher.setState(VoucherState.APR.toString());
        voucherAccoutingService.approveVoucher(voucher);
        facesMessages.addFromResourceBundle(StatusMessage.Severity.INFO,"Voucher.message.approveAccountingEntry");
        return APPROVED_OUTCOME;
    }

    public String annulVoucher(){

        voucher.setState(VoucherState.ANL.toString());
        voucherAccoutingService.annulVoucher(voucher);
        facesMessages.addFromResourceBundle(StatusMessage.Severity.INFO,"Voucher.message.annulAccountingEntry");
        return ANNUL_OUTCOME;
    }

    public void assignVoucherDetail(CashAccount cashAccount){
        VoucherDetail voucherDetail = new VoucherDetail();
        voucherDetail.setCashAccount(cashAccount);
        voucherDetail.setAccount(cashAccount.getAccountCode());
        voucherDetails.add(voucherDetail);
        System.out.println("... ... ... Add Voucher Detail: " + cashAccount.getFullName());
    }

    public void assignCashAccountDefault(String accountCode){
        this.account = cashAccountService.findByAccountCode(accountCode);
        assignCashAccountVoucherDetail();
    }

    public void assignCashAccountDefault(String accountCode, PurchaseDocument purchaseDocument){
        this.account = cashAccountService.findByAccountCode(accountCode);
        assignCashAccountVoucherDetail(purchaseDocument);
    }

    public void assignCashAccountVoucherDetail(PurchaseDocument purchaseDocument){

        if (account != null){
            if (account.getAccountCode().equals("1420710000")){ /** MODIFYID Credito Fiscal **/
                setFiscalCredit(true);
                //PurchaseDocument purchaseDocument = new PurchaseDocument();
                //getPurchaseDocumentList().add(purchaseDocument);
                addFiscalCreditCashAccount(purchaseDocument);

            }else {
                assignInputVoucherDetail();
            }
        }
    }

    public void assignCashAccountVoucherDetail(){

        if (account != null){
            if (account.getAccountCode().equals("1420710000")){ /** MODIFYID Credito Fiscal **/
                setFiscalCredit(true);
                PurchaseDocument purchaseDocument = new PurchaseDocument();
                //getPurchaseDocumentList().add(purchaseDocument);
                addFiscalCreditCashAccount(purchaseDocument);

            }else {
                assignInputVoucherDetail();
            }
        }
    }

    public void addFiscalCreditCashAccount(PurchaseDocument purchaseDocument){

        purchaseDocument.setName(purchaseDocument.getFinancesEntity().getAcronym());
        purchaseDocument.setNit(purchaseDocument.getFinancesEntity().getNitNumber());

        BigDecimal fiscalCredit = BigDecimalUtil.multiply(BigDecimalUtil.subtract(purchaseDocument.getAmount(), purchaseDocument.getExempt(), 2), BigDecimalUtil.toBigDecimal(0.13),2 );
        try {
            VoucherDetail voucherDetail = new VoucherDetail();
            voucherDetail.setCashAccount(this.account);
            voucherDetail.setAccount(this.account.getAccountCode());
            voucherDetail.setClient(this.client);
            voucherDetail.setProvider(this.provider);

            if (this.provider != null)
                voucherDetail.setProviderCode(this.provider.getProviderCode());

            voucherDetail.setDebit(fiscalCredit);
            voucherDetail.setCredit(this.credit);

            voucherDetail.setPurchaseDocument(purchaseDocument);

            voucherDetails.add(voucherDetail);
            clearAll();
            //setDebit(BigDecimal.ZERO);
            //setCredit(BigDecimal.ZERO);
        } catch (NullPointerException e) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.WARN, "Voucher.message.incomplete");
        }
    }

    public void assignInputVoucherDetail(){
        try {
            VoucherDetail voucherDetail = new VoucherDetail();
            voucherDetail.setCashAccount(this.getAccount());
            voucherDetail.setAccount(this.getAccount().getAccountCode());
            voucherDetail.setClient(this.getClient());
            voucherDetail.setProvider(this.getProvider());

            if (provider != null) voucherDetail.setProviderCode(provider.getProviderCode());

            voucherDetail.setDebit(this.debit);
            voucherDetail.setCredit(this.credit);

            voucherDetails.add(voucherDetail);
            clearAll();
            setDebit(BigDecimal.ZERO);
            setCredit(BigDecimal.ZERO);
        }catch (NullPointerException e){
            facesMessages.addFromResourceBundle(StatusMessage.Severity.WARN, "Voucher.message.incomplete");
        }

    }

    public void clearAll(){
        clearAccount();
        clearClient();
        clearProvider();
    }

    public void removeVoucherDetail(VoucherDetail voucherDetail) {
        System.out.println("---> " + voucherDetail.getCashAccount().getDescription() + " - " + voucherDetail.getDebit() + " - " + voucherDetail.getCredit());
        voucherDetails.remove(voucherDetail);
    }

    public boolean isApproved() {
        return VoucherState.APR.toString().equals(voucher.getState());
    }

    public boolean isPending() {
        return VoucherState.PEN.toString().equals(voucher.getState());
    }

    public void assignProvider(Provider provider) {
        /*getInstance().setProvider(provider);
        this.voucher.setProvider(provider);*/
        setProvider(provider);
    }

    /* todo */
    private void cleanMovementDetailFields() {

    }
    /* todo */
    public void cleanMainFields() {

        cleanMovementDetailFields();
    }

    public BigDecimal getTotalsDebit(){
        BigDecimal total = new BigDecimal("0.00");
        for (VoucherDetail voucherDetail : voucherDetails) {
            if(voucherDetail.getDebit() != null)
                total = total.add(voucherDetail.getDebit());
        }
        //System.out.println("_____getTotalsDebit::::" + total);
        return total;
    }

    public BigDecimal getTotalsCredit(){
        BigDecimal total = new BigDecimal("0.00");
        for (VoucherDetail voucherDetail : voucherDetails) {
            if(voucherDetail.getCredit() != null)
                total = total.add(voucherDetail.getCredit());
        }
        //System.out.println("_____getTotalsCredit:::: " + total);
        return total;
    }

    /**
     * For Purchase Document
     */
    @Begin(nested = true, flushMode = FlushModeType.MANUAL)
    public String addPurchaseDocument() {
        setOp(OP_CREATE);
        return purchaseDocumentAction.addPurchaseDocumentFromAccounting();
        //return Outcome.SUCCESS;
    }

    @Begin(nested = true, flushMode = FlushModeType.MANUAL)
    public String selectPurchaseDocument(PurchaseDocument purchaseDocument) {
        setOp(OP_UPDATE);
        return purchaseDocumentAction.select(purchaseDocument);
    }

    public List<CollectionDocumentType> getPurchaseDocumentTypeList() {

        List<CollectionDocumentType> collectionDocumentTypeList = new ArrayList<CollectionDocumentType>();
        collectionDocumentTypeList.add(CollectionDocumentType.INVOICE);

        return collectionDocumentTypeList;
    }

    public void assignFinancesEntity(FinancesEntity financesEntity) {
        purchaseDocumentAction.assignFinancesEntity(financesEntity);
    }

    public void assignProviderAndFinancesEntity(Provider provider) {
        setProvider(provider);
        purchaseDocumentAction.assignFinancesEntity(provider.getEntity());
    }

    /**
     * End Purchase Document
     */

    public List<CashAccount> getCashAccounts() {
        return cashAccounts;
    }

    public void setCashAccounts(List<CashAccount> cashAccounts) {
        this.cashAccounts = cashAccounts;
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

    public String getDocumentTypeCode() {
        return documentTypeCode;
    }

    public void setDocumentTypeCode(String documentTypeCode) {
        this.documentTypeCode = documentTypeCode;
    }


    public Voucher getVoucher() {
        return voucher;
    }

    public void setVoucher(Voucher voucher) {
        this.voucher = voucher;
    }

    public VoucherAccoutingService getVoucherAccoutingService() {
        return voucherAccoutingService;
    }

    public void setVoucherAccoutingService(VoucherAccoutingService voucherAccoutingService) {
        this.voucherAccoutingService = voucherAccoutingService;
    }

    public List<VoucherDetail> getVoucherDetails() {
        return voucherDetails;
    }

    public void setVoucherDetails(List<VoucherDetail> voucherDetails) {
        this.voucherDetails = voucherDetails;
    }

    public DocType getDocType() {
        return docType;
    }

    public void setDocType(DocType docType) {
        this.docType = docType;
    }

    public BigDecimal getTotalDebit() {
        return totalDebit;
    }

    public void setTotalDebit(BigDecimal totalDebit) {
        this.totalDebit = totalDebit;
    }

    public BigDecimal getTotalCredit() {
        return totalCredit;
    }

    public void setTotalCredit(BigDecimal totalCredit) {
        this.totalCredit = totalCredit;
    }

    public CashAccount getAccount() {
        return account;
    }

    public void setAccount(CashAccount account) {
        this.account = account;
    }

    public Client getClient() {
        return client;
    }

    public void setClient(Client client) {
        this.client = client;
    }

    public Provider getProvider() {
        return provider;
    }

    public void setProvider(Provider provider) {
        this.provider = provider;
    }

    public void clearAccount() {
        setAccount(null);
    }

    public void clearClient(){
        setClient(null);
    }

    public void clearProvider(){
        setProvider(null);
    }

    public void clearFinancesEntityAndProvider() {
        setProvider(null);
        purchaseDocumentAction.clearFinancesEntity();
    }

    public Boolean getFiscalCredit() {
        return fiscalCredit;
    }

    public void setFiscalCredit(Boolean fiscalCredit) {
        this.fiscalCredit = fiscalCredit;
    }

    /*public List<PurchaseDocument> getPurchaseDocumentList() {
        return purchaseDocumentList;
    }

    public void setPurchaseDocumentList(List<PurchaseDocument> purchaseDocumentList) {
        this.purchaseDocumentList = purchaseDocumentList;
    }*/
}
