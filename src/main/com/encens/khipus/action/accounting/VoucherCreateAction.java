package com.encens.khipus.action.accounting;

import com.encens.khipus.framework.action.GenericAction;
import com.encens.khipus.framework.action.Outcome;
import com.encens.khipus.model.accounting.DocType;
import com.encens.khipus.model.customers.Client;
import com.encens.khipus.model.finances.CashAccount;
import com.encens.khipus.model.finances.Provider;
import com.encens.khipus.model.finances.Voucher;
import com.encens.khipus.model.finances.VoucherDetail;
import com.encens.khipus.service.accouting.VoucherAccoutingService;
import com.encens.khipus.service.customers.ClientService;
import com.encens.khipus.service.finances.VoucherService;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.End;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.international.StatusMessage;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author
 * @version 3.0
 */
@Name("voucherCreateAction")
@Scope(ScopeType.CONVERSATION)
public class VoucherCreateAction extends GenericAction<Voucher> {

    CashAccount cashAccount = null;

    private BigDecimal debit = new BigDecimal("0.00");
    private BigDecimal credit = new BigDecimal("0.00");
    private String documentTypeCode = "";

    private DocType docType = new DocType();
    private Voucher voucher = new Voucher();

    private Provider provider;
    private CashAccount account;
    private Client client;

    private List<VoucherDetail> voucherDetails = new ArrayList<VoucherDetail>();
    private List<CashAccount> cashAccounts = new ArrayList<CashAccount>();

    private BigDecimal totalDebit = new BigDecimal("0.00");
    private BigDecimal totalCredit = new BigDecimal("0.00");

    private String clientFullName;
    private String providerFullName;

    @In
    private VoucherAccoutingService voucherAccoutingService;

    @In
    private VoucherService voucherService;

    @In(create = true)
    private VoucherUpdateAction voucherUpdateAction;

    @In(create = true)
    private VoucherDetailAction voucherDetailAction;

    @In
    private ClientService clientService;

    @Override
    @End
    public String create() {

        voucher.setDocumentType(docType.getName());
        voucher.setDetails(voucherDetails);

        BigDecimal totalD = new BigDecimal("0.00");
        BigDecimal totalC = new BigDecimal("0.00");
        try {
            for (VoucherDetail voucherDetail : voucherDetails) {
                totalD = totalD.add(voucherDetail.getDebit());
                totalC = totalC.add(voucherDetail.getCredit());
            }
            //facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,"SalaryMovementProducer.message.insufficientBalance",fullName,totalCollected);
            if(totalD.doubleValue() == 0.00 || totalC.doubleValue() == 0.00){
                facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,"Voucher.message.incorrectAccountingEntry");
                return Outcome.REDISPLAY;
            }

            if(totalD.doubleValue() != totalC.doubleValue()){
                facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,"Voucher.message.incorrectAccountingEntry");
                return Outcome.REDISPLAY;
            }

            voucherAccoutingService.saveVoucher(voucher);
            voucherUpdateAction.setVoucher(voucher);
            voucherUpdateAction.setDocType(voucherService.getDocType(voucher.getDocumentType()));
            voucherUpdateAction.setVoucherDetails(voucherAccoutingService.getVoucherDetailList(voucher));

            voucherUpdateAction.setInstance(voucher);

            return Outcome.SUCCESS;

        } catch (Exception e) {
            return Outcome.FAIL;
        }
    }

    public List<Client> autocomplete(Object suggest){
        String pref = (String)suggest;
        ArrayList<Client> result = new ArrayList<Client>();
        Iterator<Client> iterator = clientService.getAllClients().iterator();

        while (iterator.hasNext()) {
            Client elem = ((Client) iterator.next());
            if ((elem.getName() != null && elem.getName().toLowerCase().indexOf(pref.toLowerCase()) == 0) || "".equals(pref))
            {
                result.add(elem);
            }
        }
        return result;
    }

    public void assignVoucherDetail(CashAccount cashAccount){
        VoucherDetail voucherDetail = new VoucherDetail();
        voucherDetail.setCashAccount(cashAccount);
        voucherDetail.setAccount(cashAccount.getAccountCode());
        voucherDetails.add(voucherDetail);
    }

    public void assignInputVoucherDetail(){
        try {
            VoucherDetail voucherDetail = new VoucherDetail();
            voucherDetail.setCashAccount(this.account);
            voucherDetail.setAccount(this.account.getAccountCode());
            voucherDetail.setClient(this.client);
            voucherDetail.setProvider(this.provider);

            if (this.provider != null)
                voucherDetail.setProviderCode(this.provider.getProviderCode());

            voucherDetail.setDebit(this.debit);
            voucherDetail.setCredit(this.credit);

            voucherDetails.add(voucherDetail);
            clearAccount();
            clearClient();
            clearProvider();
            setDebit(new BigDecimal("0.00"));
            setCredit(new BigDecimal("0.00"));
        }catch (NullPointerException e){
            facesMessages.addFromResourceBundle(StatusMessage.Severity.WARN,"Voucher.message.incomplete");
        }

    }


    public void removeVoucherDetail(VoucherDetail voucherDetail) {
        System.out.println("---> " + voucherDetail.getCashAccount().getDescription() + " - " + voucherDetail.getDebit() + " - " + voucherDetail.getCredit());
        voucherDetails.remove(voucherDetail);
    }

    public void assignProvider(Provider provider) {
        setProvider(provider);
    }

    public void assignProvider(Provider provider, int rowIndex) {

        System.out.println("Provider: " + rowIndex + " - " + provider.getFullName());
        //setProvider(provider);
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
        System.out.println("....getTotalsDebit:::: " + total);
        return total;
    }

    public BigDecimal getTotalsCredit(){
        BigDecimal total = new BigDecimal("0.00");
        for (VoucherDetail voucherDetail : voucherDetails) {
            if(voucherDetail.getCredit() != null)
                total = total.add(voucherDetail.getCredit());
        }
        System.out.println("....getTotalsCredit:::: " + total);
        return total;
    }

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

    public Provider getProvider() {
        return provider;
    }

    public void setProvider(Provider provider) {
        this.provider = provider;
    }

    public Client getClient() {
        System.out.println("Get Client");
        return client;
    }

    public void setClient(Client client) {
        System.out.println("Set Client");
        this.client = client;
    }

    public void assignData(){
        System.out.println("---> assignData . . . . " + voucherDetailAction.getInstance().getClientFullName());
        //System.out.println("---> assignData client... " + client);
    }

    public void printData(){
        System.out.println("-> CLIENTE : " + client);
        System.out.println("-> PROVEEDOR : " + provider);
        System.out.println("-> ACCOUNT : " + account);
    }

    public CashAccount getAccount() {
        return account;
    }

    public void setAccount(CashAccount account) {
        this.account = account;
    }

    public String getClientFullName() {
        return clientFullName;
    }

    public void setClientFullName(String clientFullName) {
        this.clientFullName = clientFullName;
    }

    public String getProviderFullName() {
        return providerFullName;
    }

    public void setProviderFullName(String providerFullName) {
        this.providerFullName = providerFullName;
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
}
