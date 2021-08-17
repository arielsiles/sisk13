package com.encens.khipus.action.accounting;

import com.encens.khipus.exception.finances.CompanyConfigurationNotFoundException;
import com.encens.khipus.framework.action.GenericAction;
import com.encens.khipus.model.accounting.DocType;
import com.encens.khipus.model.accounting.SaleType;
import com.encens.khipus.model.admin.ProductSaleType;
import com.encens.khipus.model.employees.Month;
import com.encens.khipus.model.finances.CashAccount;
import com.encens.khipus.model.finances.FinancesCurrencyType;
import com.encens.khipus.model.finances.Voucher;
import com.encens.khipus.model.finances.VoucherDetail;
import com.encens.khipus.model.warehouse.Warehouse;
import com.encens.khipus.model.warehouse.WarehouseType;
import com.encens.khipus.service.accouting.VoucherAccoutingService;
import com.encens.khipus.service.finances.VoucherService;
import com.encens.khipus.util.BigDecimalUtil;
import com.encens.khipus.util.Constants;
import com.encens.khipus.util.DateUtils;
import com.encens.khipus.util.VoucherBuilder;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.*;

import java.math.BigDecimal;
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

    private CashAccount cashAccountProductionCost;

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
            voucherAccoutingService.createCostOfSale_MilkProducts(startDate, endDate, this.warehouse);
            voucherAccoutingService.createCostOfSale_MilkProductsReplacement(startDate, endDate, this.warehouse);

            voucherAccoutingService.createCostOfSale_MilkProductsTastingOrRefreshment(startDate, endDate, new Long(2), "4470610218", this.warehouse.getWarehouseCode()); //Degustacion
            voucherAccoutingService.createCostOfSale_MilkProductsTastingOrRefreshment(startDate, endDate, new Long(3), "4470610218", this.warehouse.getWarehouseCode()); //Refrigerio
        }

        if (this.warehouse.getWarehouseType().equals(WarehouseType.VETERINARY)){
            voucherAccoutingService.createCostOfSale_VeterinaryProducts(startDate, endDate, this.warehouse.getWarehouseCode());
        }

        if (this.warehouse.getWarehouseType().equals(WarehouseType.AGENCY)){
            voucherAccoutingService.createCostOfSale_MilkProducts(startDate, endDate, this.warehouse);
            voucherAccoutingService.createCostOfSale_MilkProductsReplacement(startDate, endDate, this.warehouse);
        }

    }


    public void closingProductionCostAccount(){

        List<Object[]> datas = voucherAccoutingService.getProductionCostAccountResults(startDate, endDate, this.cashAccountProductionCost);

        String periodMessage = Month.getMonth(endDate).getMonthLiteral() + "/" + DateUtils.getCurrentYear(endDate);
        Voucher voucher = VoucherBuilder.newGeneralVoucher(null, "ASIENTO DE AJUSTE COSTO DE PRODUCTOS TERMINADOS " + periodMessage.toUpperCase());
        voucher.setDocumentType(Constants.CB_VOUCHER_DOCTYPE);
        voucher.setDate(endDate);


        System.out.println("----------SUMAS-Y-SALDOS-------");
        BigDecimal totalDebit = BigDecimal.ZERO;
        BigDecimal totalCredit = BigDecimal.ZERO;
        for(Object[] obj: datas){
            System.out.println(obj[0] + " - " + obj[2] + " - " + obj[3] + " - " + obj[1] );

            String accountCode = (String)obj[0];
            BigDecimal debit  = BigDecimalUtil.toBigDecimal(obj[2]);
            BigDecimal credit = BigDecimalUtil.toBigDecimal(obj[3]);

            if (credit.doubleValue() > debit.doubleValue()){
                VoucherDetail voucherDebit = new VoucherDetail();
                voucherDebit.setAccount(accountCode);
                voucherDebit.setDebit(BigDecimalUtil.subtract(credit, debit));
                voucherDebit.setCredit(BigDecimal.ZERO);
                voucherDebit.setCurrency(FinancesCurrencyType.P);
                voucherDebit.setExchangeAmount(BigDecimal.ONE);
                voucherDebit.setDebitMe(BigDecimal.ZERO);
                voucherDebit.setCreditMe(BigDecimal.ZERO);
                voucher.addVoucherDetail(voucherDebit);
                totalDebit = BigDecimalUtil.sum(totalDebit, voucherDebit.getDebit());
            }
            if (debit.doubleValue() > credit.doubleValue()){
                VoucherDetail voucherCredit = new VoucherDetail();
                voucherCredit.setAccount(accountCode);
                voucherCredit.setDebit(BigDecimal.ZERO);
                voucherCredit.setCredit(BigDecimalUtil.subtract(debit, credit));
                voucherCredit.setCurrency(FinancesCurrencyType.P);
                voucherCredit.setExchangeAmount(BigDecimal.ONE);
                voucherCredit.setDebitMe(BigDecimal.ZERO);
                voucherCredit.setCreditMe(BigDecimal.ZERO);
                voucher.addVoucherDetail(voucherCredit);
                totalCredit = BigDecimalUtil.sum(totalCredit, voucherCredit.getCredit());
            }

        }

        VoucherDetail totalVoucherDetail = new VoucherDetail();
        totalVoucherDetail.setAccount("4420110201"); // 4420110201 - Costo Productos Mirabel
        if (totalDebit.doubleValue() > totalCredit.doubleValue()){
            totalVoucherDetail.setDebit(BigDecimal.ZERO);
            totalVoucherDetail.setCredit(BigDecimalUtil.subtract(totalDebit, totalCredit));
        }

        if (totalCredit.doubleValue() > totalDebit.doubleValue()) {
            totalVoucherDetail.setDebit(BigDecimalUtil.subtract(totalCredit, totalDebit));
            totalVoucherDetail.setCredit(BigDecimal.ZERO);
        }

        totalVoucherDetail.setCurrency(FinancesCurrencyType.P);
        totalVoucherDetail.setExchangeAmount(BigDecimal.ONE);
        totalVoucherDetail.setDebitMe(BigDecimal.ZERO);
        totalVoucherDetail.setCreditMe(BigDecimal.ZERO);

        voucher.addVoucherDetail(totalVoucherDetail);

        voucherAccoutingService.saveVoucher(voucher);

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

    public CashAccount getCashAccountProductionCost() {
        return cashAccountProductionCost;
    }

    public void setCashAccountProductionCost(CashAccount cashAccountProductionCost) {
        this.cashAccountProductionCost = cashAccountProductionCost;
    }
}
