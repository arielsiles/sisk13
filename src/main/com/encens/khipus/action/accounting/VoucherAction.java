package com.encens.khipus.action.accounting;

import com.encens.khipus.exception.finances.CompanyConfigurationNotFoundException;
import com.encens.khipus.framework.action.GenericAction;
import com.encens.khipus.model.accounting.DocType;
import com.encens.khipus.model.accounting.SaleType;
import com.encens.khipus.model.admin.ProductSaleType;
import com.encens.khipus.model.finances.Voucher;
import com.encens.khipus.model.finances.VoucherDetail;
import com.encens.khipus.service.accouting.VoucherAccoutingService;
import com.encens.khipus.service.finances.VoucherService;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.*;

import java.util.Date;
import java.util.List;

/**
 * OrganizationAction
 *
 * @author
 * @version 2.26
 */
@Name("voucherAction")
@Scope(ScopeType.CONVERSATION)
public class VoucherAction extends GenericAction<Voucher> {

    private Voucher voucher;
    private DocType docType = new DocType();
    private String documentNumber;
    private String gloss;

    private SaleType saleType;
    private ProductSaleType productSaleType;

    private Date startDate;
    private Date endDate;

    @In(required = false)
    VoucherDataModel voucherDataModel;

    @In
    private VoucherService voucherService;

    @Factory(value = "voucher", scope = ScopeType.STATELESS)
    public Voucher initVoucher() {
        return getInstance();
    }

    @Override
    protected String getDisplayNameProperty() {
        return "name";
    }

    @In
    private VoucherAccoutingService voucherAccoutingService;

    @Override
    @Begin(ifOutcome = com.encens.khipus.framework.action.Outcome.SUCCESS, flushMode = FlushModeType.MANUAL, join=true)
    public String select(Voucher instance) {
        String outCome = super.select(instance);
        return outCome;
    }

    @Begin(ifOutcome = com.encens.khipus.framework.action.Outcome.SUCCESS, flushMode = FlushModeType.MANUAL, join=true)
    public String selectVoucher(Voucher voucherInstance){
        String outCome = super.select(voucherInstance);
        this.voucher = getInstance();
        this.docType = voucherService.getDocType(voucher.getDocumentType());

        return outCome;
    }

    public List<VoucherDetail> getVoucherDetailList(String transactionNumber) {

        List<VoucherDetail> voucherDetails = voucherAccoutingService.getVoucherDetailList(transactionNumber);

        return voucherDetails;
    }

    public void search() {

        voucherDataModel.filterByDocumentType(null == docType ? "" : docType.getName());
        voucherDataModel.getCriteria().setGloss(gloss);
        voucherDataModel.getCriteria().setDocumentNumber(documentNumber);

        voucherDataModel.search();
    }

    public void generateCostOfSales()throws CompanyConfigurationNotFoundException {

        if (this.saleType.equals(SaleType.CASH_SALE)){
            voucherAccoutingService.createCostOfCashSales(startDate, endDate, productSaleType);
        }

        if (this.saleType.equals(SaleType.CREDIT_SALE)) {
            voucherAccoutingService.createCostOfSalesCredit(startDate, endDate);
        }



    }

    public Voucher getVoucher() {
        return voucher;
    }

    public void setVoucher(Voucher voucher) {
        this.voucher = voucher;
    }

    public DocType getDocType() {
        return docType;
    }

    public void setDocType(DocType docType) {
        this.docType = docType;
    }

    public String getDocumentNumber() {
        return documentNumber;
    }

    public void setDocumentNumber(String documentNumber) {
        this.documentNumber = documentNumber;
    }

    public String getGloss() {
        return gloss;
    }

    public void setGloss(String gloss) {
        this.gloss = gloss;
    }

    public SaleType getSaleType() {
        return saleType;
    }

    public void setSaleType(SaleType saleType) {
        this.saleType = saleType;
    }

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

    public ProductSaleType getProductSaleType() {
        return productSaleType;
    }

    public void setProductSaleType(ProductSaleType productSaleType) {
        this.productSaleType = productSaleType;
    }
}
