package com.encens.khipus.action.accounting;

import com.encens.khipus.exception.finances.CompanyConfigurationNotFoundException;
import com.encens.khipus.framework.action.GenericAction;
import com.encens.khipus.model.accounting.DocType;
import com.encens.khipus.model.accounting.SaleType;
import com.encens.khipus.model.admin.ProductSaleType;
import com.encens.khipus.model.finances.Voucher;
import com.encens.khipus.model.finances.VoucherDetail;
import com.encens.khipus.model.warehouse.Warehouse;
import com.encens.khipus.model.warehouse.WarehouseType;
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

    private Warehouse warehouse;

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

        if (this.productSaleType.equals(ProductSaleType.DAIRY_PRODUCT)){
            voucherAccoutingService.createCostOfSale_MilkProducts(startDate, endDate);
            voucherAccoutingService.createCostOfSale_MilkProductsReplacement(startDate, endDate);
            voucherAccoutingService.createCostOfSale_MilkProductsTastingOrRefreshment(startDate, endDate, new Long(2), "4470610218"); //Degustacion
            voucherAccoutingService.createCostOfSale_MilkProductsTastingOrRefreshment(startDate, endDate, new Long(3), "4460310104"); //Refrigerio
        }

        if (this.productSaleType.equals(ProductSaleType.VETERINARY_PRODUCT)){
            voucherAccoutingService.createCostOfSale_VeterinaryProducts(startDate, endDate);
        }

    }

    public void generateCostOfSalesByWarehouse()throws CompanyConfigurationNotFoundException {

        if (this.warehouse.getWarehouseType().equals(WarehouseType.DAIRY)){
            voucherAccoutingService.createCostOfSale_MilkProducts(startDate, endDate, this.warehouse.getWarehouseCode());
            voucherAccoutingService.createCostOfSale_MilkProductsReplacement(startDate, endDate, this.warehouse.getWarehouseCode());

            voucherAccoutingService.createCostOfSale_MilkProductsTastingOrRefreshment(startDate, endDate, new Long(2), "4470610218", this.warehouse.getWarehouseCode()); //Degustacion
            voucherAccoutingService.createCostOfSale_MilkProductsTastingOrRefreshment(startDate, endDate, new Long(3), "4470610218", this.warehouse.getWarehouseCode()); //Refrigerio
        }

        if (this.warehouse.getWarehouseType().equals(WarehouseType.VETERINARY)){
            voucherAccoutingService.createCostOfSale_VeterinaryProducts(startDate, endDate, this.warehouse.getWarehouseCode());
        }

        if (this.warehouse.getWarehouseType().equals(WarehouseType.AGENCY)){
            //voucherAccoutingService.createCostOfSale_MilkProducts(startDate, endDate, this.warehouse.getWarehouseCode());
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

    public Warehouse getWarehouse() {
        return warehouse;
    }

    public void setWarehouse(Warehouse warehouse) {
        this.warehouse = warehouse;
    }
}
