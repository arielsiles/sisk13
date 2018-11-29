package com.encens.khipus.action.accounting;

import com.encens.khipus.action.purchases.PurchaseDocumentAction;
import com.encens.khipus.framework.action.GenericAction;
import com.encens.khipus.framework.action.Outcome;
import com.encens.khipus.model.accounting.DocType;
import com.encens.khipus.model.customers.Account;
import com.encens.khipus.model.customers.Client;
import com.encens.khipus.model.finances.*;
import com.encens.khipus.model.purchases.PurchaseDocument;
import com.encens.khipus.model.purchases.PurchaseOrder;
import com.encens.khipus.model.finances.CashAccount;
import com.encens.khipus.model.finances.Provider;
import com.encens.khipus.model.finances.Voucher;
import com.encens.khipus.model.finances.VoucherDetail;
import com.encens.khipus.model.purchases.PurchaseDocument;
import com.encens.khipus.model.warehouse.ProductItem;
import com.encens.khipus.service.accouting.VoucherAccoutingService;
import com.encens.khipus.service.customers.ClientService;
import com.encens.khipus.service.finances.CashAccountService;
import com.encens.khipus.service.finances.VoucherService;
import com.encens.khipus.service.purchases.PurchaseDocumentService;
import com.encens.khipus.util.BigDecimalUtil;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.End;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.international.StatusMessage;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
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
    private ProductItem productItem;
    private Account partnerAccount;

    //private PurchaseDocument purchaseDocument;
    private List<PurchaseDocument> purchaseDocumentList = new ArrayList<PurchaseDocument>();

    private Integer quantity;
    private BigDecimal amountDeposit;

    private List<VoucherDetail> voucherDetails = new ArrayList<VoucherDetail>();
    private List<CashAccount> cashAccounts = new ArrayList<CashAccount>();

    private BigDecimal totalDebit = new BigDecimal("0.00");
    private BigDecimal totalCredit = new BigDecimal("0.00");

    private String clientFullName;
    private String providerFullName;

    private Boolean fiscalCredit = false;

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

    @In
    private CashAccountService cashAccountService;

    @In
    private PurchaseDocumentService purchaseDocumentService;

    @In(create = true)
    private PurchaseDocumentAction purchaseDocumentAction;

    @Override
    @End
    public String create() {

        voucher.setDocumentType(docType.getName());
        voucher.setDetails(voucherDetails);

        //voucher.setPurchaseDocumentList(purchaseDocumentList);

        Boolean hasFiscalCredit = false;

        BigDecimal totalD = BigDecimal.ZERO;
        BigDecimal totalC = BigDecimal.ZERO;
        BigDecimal totalI = BigDecimal.ZERO;

        try {

            for (VoucherDetail voucherDetail : voucherDetails) {
                totalD = totalD.add(voucherDetail.getDebit());
                totalC = totalC.add(voucherDetail.getCredit());

                if (isFiscalCredit(voucherDetail)) hasFiscalCredit = true;

            }

            for (PurchaseDocument purchaseDocument : purchaseDocumentList){
                totalI = totalI.add(purchaseDocument.getAmount());
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

            if ((totalI.doubleValue() != totalD.doubleValue()) && hasFiscalCredit ){
                facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,"Voucher.message.incorrectFiscalCredit");
                return Outcome.REDISPLAY;
            }

            /** For Create Invoice **/
            for (PurchaseDocument purchaseDocument : purchaseDocumentList){
                //purchaseDocument.setVoucher(voucher);
                purchaseDocument.setNetAmount(purchaseDocument.getAmount());
                purchaseDocument.setType(CollectionDocumentType.INVOICE);
                purchaseDocumentService.createDocumentSimple(purchaseDocument);
            }

            voucherAccoutingService.saveVoucher(voucher);
            voucherUpdateAction.setVoucher(voucher);
            voucherUpdateAction.setDocType(voucherService.getDocType(voucher.getDocumentType()));
            voucherUpdateAction.setVoucherDetails(voucherAccoutingService.getVoucherDetailList(voucher));
            voucherUpdateAction.setInstance(voucher);

            System.out.println("-------------> Relacionando....");
            for (PurchaseDocument purchaseDocument : purchaseDocumentList){
                voucherAccoutingService.updateVoucher(voucher, purchaseDocument);
            }


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

    public Boolean isFiscalCredit(VoucherDetail voucherDetail){
        return voucherDetail.getAccount().equals("1420710000");
    }

    public void assignVoucherDetail(CashAccount cashAccount){
        VoucherDetail voucherDetail = new VoucherDetail();
        voucherDetail.setCashAccount(cashAccount);
        voucherDetail.setAccount(cashAccount.getAccountCode());
        voucherDetails.add(voucherDetail);
    }

    /*public void assignCashAccountFiscalCredit(){
        this.account = cashAccountService.findByAccountCode("1420710000");
        assignCashAccountVoucherDetail();
    }*/

    public void assignCashAccountDefault(String accountCode){
        this.account = cashAccountService.findByAccountCode(accountCode);
        assignCashAccountVoucherDetail();
    }


    public void assignCashAccountVoucherDetail(){

        if (account != null){
            if (account.getAccountCode().equals("1420710000")){ /** MODIFYID Credito Fiscal **/
                setFiscalCredit(true);
                PurchaseDocument purchaseDocument = new PurchaseDocument();
                purchaseDocumentList.add(purchaseDocument);

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
                voucherDetail.setCashAccount(this.account);
                voucherDetail.setAccount(this.account.getAccountCode());
                voucherDetail.setClient(this.client);
                voucherDetail.setProvider(this.provider);

                if (this.provider != null) voucherDetail.setProviderCode(this.provider.getProviderCode());

                voucherDetail.setDebit(this.debit);
                voucherDetail.setCredit(this.credit);

                voucherDetails.add(voucherDetail);
                clearAll();
                setDebit(BigDecimal.ZERO);
                setCredit(BigDecimal.ZERO);
            } catch (NullPointerException e) {
                facesMessages.addFromResourceBundle(StatusMessage.Severity.WARN, "Voucher.message.incomplete");
            }
    }

    public void clearAll(){
        clearAccount();
        clearClient();
        clearProvider();
    }

    public void assignProductItemVoucherDetail(){

        try {
            CashAccount ctaCaja     = cashAccountService.findByAccountCode("1110110100");
            CashAccount ctaIngreso  = cashAccountService.findByAccountCode("5451010000");

            VoucherDetail voucherCaja = new VoucherDetail();
            voucherCaja.setCashAccount(ctaCaja);
            voucherCaja.setAccount(ctaCaja.getAccountCode());
            voucherCaja.setDebit(BigDecimalUtil.multiply(productItem.getSalePrice(), BigDecimalUtil.toBigDecimal(quantity), 2));
            voucherCaja.setCredit(BigDecimal.ZERO);

            VoucherDetail voucherIngreso = new VoucherDetail();
            voucherIngreso.setCashAccount(ctaIngreso);
            voucherIngreso.setAccount(ctaIngreso.getAccountCode());


            VoucherDetail voucherDetail = new VoucherDetail(); //Cta Almacen
            voucherDetail.setCashAccount(productItem.getWarehouse().getWarehouseCashAccount());
            voucherDetail.setAccount(productItem.getWarehouse().getWarehouseCashAccount().getAccountCode());
            voucherDetail.setClient(this.client);
            voucherDetail.setProvider(this.provider);
            voucherDetail.setProductItem(productItem);

            if (this.provider != null)
                voucherDetail.setProviderCode(this.provider.getProviderCode());
            if (productItem != null)
                voucherDetail.setProductItemCode(productItem.getProductItemCode());

            voucherDetail.setDebit(BigDecimal.ZERO);
            voucherDetail.setCredit(BigDecimalUtil.multiply(productItem.getUnitCost(), BigDecimalUtil.toBigDecimal(quantity), 2));

            voucherIngreso.setDebit(BigDecimal.ZERO);
            voucherIngreso.setCredit(BigDecimalUtil.subtract(voucherCaja.getDebit(), voucherDetail.getCredit(), 2));

            voucherDetails.add(voucherCaja);
            voucherDetails.add(voucherDetail);
            voucherDetails.add(voucherIngreso);

            clearAccount();
            clearClient();
            clearProvider();
            setDebit(BigDecimal.ZERO);
            setCredit(BigDecimal.ZERO);

        }catch (NullPointerException e){
            facesMessages.addFromResourceBundle(StatusMessage.Severity.WARN,"Voucher.message.incomplete");
        }

    }

    public void assignPartnerAccountVoucherDetail(){
        try {

            System.out.println("---> Partner Account: " + partnerAccount.getFullAccountName());
            System.out.println("---> Account Type: " + partnerAccount.getAccountType().getName());
            System.out.println("---> Account Type: " + partnerAccount.getAccountType().getCashAccountMe().getFullName());
            System.out.println("---> Account Type: " + partnerAccount.getAccountType().getCashAccountMn().getFullName());

            CashAccount ctaCaja     = cashAccountService.findByAccountCode("1110110100");

            VoucherDetail voucherCaja = new VoucherDetail();
            voucherCaja.setCashAccount(ctaCaja);
            voucherCaja.setAccount(ctaCaja.getAccountCode());
            voucherCaja.setDebit(getAmountDeposit());
            voucherCaja.setCredit(BigDecimal.ZERO);


            VoucherDetail voucherSaving = new VoucherDetail();

            voucherSaving.setCashAccount(partnerAccount.getAccountType().getCashAccountMn());
            voucherSaving.setAccount(partnerAccount.getAccountType().getCashAccountMn().getAccountCode());
            voucherSaving.setClient(this.client);
            voucherSaving.setProvider(this.provider);
            voucherSaving.setPartnerAccount(partnerAccount);

            if (this.provider != null)
                voucherSaving.setProviderCode(this.provider.getProviderCode());

            voucherSaving.setDebit(BigDecimal.ZERO);
            voucherSaving.setCredit(getAmountDeposit());

            voucherDetails.add(voucherCaja);
            voucherDetails.add(voucherSaving);

            clearAccount();
            clearClient();
            clearProvider();
            clearPartnerAccount();
            setDebit(BigDecimal.ZERO);
            setCredit(BigDecimal.ZERO);
        }catch (NullPointerException e){
            facesMessages.addFromResourceBundle(StatusMessage.Severity.WARN,"Voucher.message.incomplete");
        }

    }


    public void removeVoucherDetail(VoucherDetail voucherDetail) {
        System.out.println("---> " + voucherDetail.getCashAccount().getDescription() + " - " + voucherDetail.getDebit() + " - " + voucherDetail.getCredit());
        voucherDetails.remove(voucherDetail);
    }

    public void removePurchaseDocument(PurchaseDocument purchaseDocument){
        purchaseDocumentList.remove(purchaseDocument);
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

    public BigDecimal getTotalInvoice(){
        BigDecimal total = new BigDecimal("0.00");
        for (PurchaseDocument purchaseDocument : purchaseDocumentList) {
            if(purchaseDocument.getAmount() != null)
                total = total.add(purchaseDocument.getAmount());
        }
        System.out.println("....TOTAL INVOICE:::: " + total);
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

    public void clearProductItem(){
        setProductItem(null);
    }

    public void clearPartnerAccount(){
        setPartnerAccount(null);
        setAmountDeposit(BigDecimal.ZERO);
    }

    public void clearProvider(){
        setProvider(null);
    }

    public ProductItem getProductItem() {
        return productItem;
    }

    public void setProductItem(ProductItem productItem) {
        this.productItem = productItem;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public Account getPartnerAccount() {
        return partnerAccount;
    }

    public void setPartnerAccount(Account partnerAccount) {
        this.partnerAccount = partnerAccount;
    }

    public Boolean getFiscalCredit() {
        return fiscalCredit;
    }

    public void setFiscalCredit(Boolean fiscalCredit) {
        this.fiscalCredit = fiscalCredit;
    }

    public List<PurchaseDocument> getPurchaseDocumentList() {
        return purchaseDocumentList;
    }

    public void setPurchaseDocumentList(List<PurchaseDocument> purchaseDocumentList) {
        this.purchaseDocumentList = purchaseDocumentList;
    }

    public BigDecimal getAmountDeposit() {
        return amountDeposit;
    }

    public void setAmountDeposit(BigDecimal amountDeposit) {
        this.amountDeposit = amountDeposit;
    }
}
