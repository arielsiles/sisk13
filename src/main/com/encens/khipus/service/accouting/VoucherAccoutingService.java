package com.encens.khipus.service.accouting;

import com.encens.khipus.exception.finances.CompanyConfigurationNotFoundException;
import com.encens.khipus.framework.service.GenericService;
import com.encens.khipus.model.accounting.DocType;
import com.encens.khipus.model.admin.ProductSaleType;
import com.encens.khipus.model.finances.CashAccount;
import com.encens.khipus.model.finances.Voucher;
import com.encens.khipus.model.finances.VoucherDetail;
import com.encens.khipus.model.purchases.PurchaseDocument;

import javax.ejb.Local;
import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * @author
 * @version 2.4
 */
@Local
public interface VoucherAccoutingService extends GenericService {

    List<VoucherDetail> getVoucherDetailList(Voucher voucher);

    List<PurchaseDocument> getPurchaseDcumentList(Voucher voucher);

    List<VoucherDetail> getVoucherDetailList(String transactionNumber);

    public Voucher getVoucher(String transactionNumber);

    public Voucher getVoucher(Long id);

    void saveVoucher(Voucher voucher);

    void savePurchaseDocument();

    void updateVoucher(Voucher voucher);

    void updatePurchaseDocumentIfExist(Voucher voucher);

    void removeVoucherDetail(VoucherDetail voucherDetail);

    void updateVoucherModify(Voucher voucher, List<VoucherDetail> voucherDetailList);

    void updateVoucher(Voucher voucher, PurchaseDocument purchaseDocument);

    void approveVoucher(Voucher voucher);

    void annulVoucher(Voucher voucher);

    public Double getBalance(Date startDate, String cashAccountCode);

    public Double getBalanceProvider(Date startDate, String cashAccountCode, String providerCode);

    public Double getTotalProfits(String startDate, String endDate);

    public Double getTotalLosses(String startDate, String endDate);


    public Double getTotalAssets(String startDate, String endDate);

    public Double getTotalLiabilities(String startDate, String endDate);

    public Double getTotalCapital(String startDate, String endDate);

    public BigDecimal totalCapital(String startDate, String endDate, String rootCashAccount);

    public BigDecimal perdidasExcedentesPeriodo(String startDate, String endDate);

    public HashMap<String, BigDecimal> stateBalanceReport(String start, String end);

    public BigDecimal calculateLossesNiv3(String startDate, String endDate, String accountLevel3);

    public BigDecimal calculateLossesNiv2(String startDate, String endDate, String rootCashAccount);

    public void createCostOfCashSales(Date startDate, Date endDate, ProductSaleType productSaleType) throws CompanyConfigurationNotFoundException;
    public void createCostOfSalesCredit(Date startDate, Date endDate, ProductSaleType productSaleType) throws CompanyConfigurationNotFoundException;

    public void createCostOfSale_MilkProducts(Date startDate, Date endDate) throws CompanyConfigurationNotFoundException;
    public void createCostOfSale_MilkProductsReplacement(Date startDate, Date endDate) throws CompanyConfigurationNotFoundException;
    public void createCostOfSale_MilkProductsTastingOrRefreshment(Date startDate, Date endDate, Long orderTypeId, String costAccount) throws CompanyConfigurationNotFoundException;
    public void createCostOfSale_VeterinaryProducts(Date startDate, Date endDate) throws CompanyConfigurationNotFoundException;

    HashMap<String, BigDecimal> getUnitCost_milkProducts(Date startDate, Date endDate);

    public Double calculateCashTransferAmount(Date startDate, Date endDate);

    public void generateCashTransferAccountEntry(Date startDate, Date endDate, Double amountTransfer, String gloss) throws CompanyConfigurationNotFoundException;

    void createPurchaseDocumentVoucher(VoucherDetail voucherDetail);

    public DocType getDocType(String name);

    public List<Object[]> getSumsClosingResults(Date startDate, Date endDate);

    public List<Object[]> getSumsBalanceClosure(Date startDate, Date endDate);

    Integer getNextMaxNumberByDocType(String docType, Date startDate, Date endDate);

    public List<Object[]> getValuedInventory(Date startDate, Date endDate, CashAccount cashAccount);

}
