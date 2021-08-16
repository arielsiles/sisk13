package com.encens.khipus.service.warehouse;

import com.encens.khipus.action.production.ProductionPlanningAction;
import com.encens.khipus.exception.finances.CompanyConfigurationNotFoundException;
import com.encens.khipus.exception.finances.FinancesCurrencyNotFoundException;
import com.encens.khipus.exception.finances.FinancesExchangeRateNotFoundException;
import com.encens.khipus.exception.warehouse.WarehouseAccountCashNotFoundException;
import com.encens.khipus.framework.service.GenericService;
import com.encens.khipus.model.admin.BusinessUnit;
import com.encens.khipus.model.finances.Voucher;
import com.encens.khipus.model.purchases.PurchaseOrder;
import com.encens.khipus.model.purchases.PurchaseOrderPayment;
import com.encens.khipus.model.warehouse.WarehouseVoucher;

import javax.ejb.Local;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * @author
 * @version 2.24
 */
@Local
public interface WarehouseAccountEntryService extends GenericService {
    void createAccountEntry(WarehouseVoucher warehouseVoucher, String[] gloss) throws CompanyConfigurationNotFoundException, FinancesCurrencyNotFoundException, FinancesExchangeRateNotFoundException, WarehouseAccountCashNotFoundException;

    void createAccountEntryFromProductDelivery(WarehouseVoucher warehouseVoucher, String[] gloss) throws CompanyConfigurationNotFoundException, FinancesCurrencyNotFoundException, FinancesExchangeRateNotFoundException, WarehouseAccountCashNotFoundException;

    String createAccountEntryFromCollection(WarehouseVoucher warehouseVoucher, String[] gloss) throws CompanyConfigurationNotFoundException, FinancesCurrencyNotFoundException, FinancesExchangeRateNotFoundException, WarehouseAccountCashNotFoundException;

    void createAdvancePaymentAccountEntry(PurchaseOrderPayment purchaseOrderPayment) throws CompanyConfigurationNotFoundException;

    void createEntryAccountForLiquidatedPurchaseOrder(PurchaseOrder purchaseOrder, BigDecimal defaultExchangeRate)
            throws CompanyConfigurationNotFoundException,
            FinancesCurrencyNotFoundException,
            FinancesExchangeRateNotFoundException;

    public Voucher createEntryAccountForValidatePurchaseOrder(PurchaseOrder purchaseOrder, BigDecimal defaultExchangeRate)
            throws CompanyConfigurationNotFoundException,
            FinancesCurrencyNotFoundException,
            FinancesExchangeRateNotFoundException;

    Voucher createEntryAccountForPurchaseOrder(WarehouseVoucher warehouseVoucher)
            throws CompanyConfigurationNotFoundException,
            FinancesCurrencyNotFoundException,
            FinancesExchangeRateNotFoundException;

    void createEntryAccountForPurchaseOrderPayment(PurchaseOrder purchaseOrder, PurchaseOrderPayment purchaseOrderPayment)
            throws CompanyConfigurationNotFoundException,
            FinancesCurrencyNotFoundException,
            FinancesExchangeRateNotFoundException;

    void setPurchaseOrderForPaymentCheck(PurchaseOrder purchaseOrder, PurchaseOrderPayment purchaseOrderPayment, String transactionNumber)
            throws CompanyConfigurationNotFoundException,
            FinancesCurrencyNotFoundException,
            FinancesExchangeRateNotFoundException;

    public String createEntryAccountPurchaseOrderForPaymentCheck(PurchaseOrder purchaseOrder, PurchaseOrderPayment purchaseOrderPayment
            ,BigDecimal totalSourceAmount,BigDecimal totalPayAmount)
            throws CompanyConfigurationNotFoundException,
            FinancesCurrencyNotFoundException,
            FinancesExchangeRateNotFoundException;

    String createEntryAccountPurchaseOrderForPaymentWithCashBox(PurchaseOrder purchaseOrder, PurchaseOrderPayment purchaseOrderPayment
            ,BigDecimal totalSourceAmount,BigDecimal totalPayAmount)
            throws CompanyConfigurationNotFoundException,
            FinancesCurrencyNotFoundException,
            FinancesExchangeRateNotFoundException;

    public String createAccountEntryForReceptionProductionOrder(WarehouseVoucher warehouseVoucher,
                                                  BusinessUnit executorUnit,
                                                  String costCenterCode,
                                                  String gloss,
                                                  List<ProductionPlanningAction.AccountOrderProduction> accountOrderProductions)
            throws CompanyConfigurationNotFoundException;

    void createAccountEntryForProductTransfer(WarehouseVoucher warehouseVoucherFrom, WarehouseVoucher warehouseVoucherTo, BusinessUnit executorUnit, String costCenterCode, String gloss)  throws CompanyConfigurationNotFoundException;

    List<WarehouseVoucher> getVouchersWithoutAccounting(Date startDate, Date endDate);

    List<WarehouseVoucher> getVouchersFromTransferCustomerOrder(Date startDate, Date endDate);

    //List<CustomerOrder> getTransferOrderList(Date startDate, Date endDate);

}
