package com.encens.khipus.service.warehouse;

import com.encens.khipus.exception.ConcurrencyException;
import com.encens.khipus.exception.EntryDuplicatedException;
import com.encens.khipus.exception.ReferentialIntegrityException;
import com.encens.khipus.exception.finances.CompanyConfigurationNotFoundException;
import com.encens.khipus.exception.finances.FinancesCurrencyNotFoundException;
import com.encens.khipus.exception.finances.FinancesExchangeRateNotFoundException;
import com.encens.khipus.exception.warehouse.*;
import com.encens.khipus.framework.service.GenericService;
import com.encens.khipus.model.customers.CustomerOrder;
import com.encens.khipus.model.customers.VentaDirecta;
import com.encens.khipus.model.employees.Employee;
import com.encens.khipus.model.warehouse.ProductDelivery;
import com.encens.khipus.service.customers.OrderItem;

import javax.ejb.Local;
import java.util.Date;
import java.util.List;

/**
 * @author
 * @version 3.0
 */

@Local
public interface ProductDeliveryService extends GenericService {
    ProductDelivery create(String invoiceNumber,
                           String warehouseDescription)
            throws InventoryException,
            WarehouseDocumentTypeNotFoundException,
            PublicCostCenterNotFound,
            ProductItemAmountException,
            InventoryUnitaryBalanceException,
            InventoryProductItemNotFoundException,
            SoldProductDeliveredException,
            CompanyConfigurationNotFoundException,
            FinancesCurrencyNotFoundException,
            FinancesExchangeRateNotFoundException,
            EntryDuplicatedException,
            ConcurrencyException,
            ReferentialIntegrityException,
            ProductItemNotFoundException, SoldProductNotFoundException;
    ProductDelivery createAll(String invoiceNumber,
                           String warehouseDescription)
            throws InventoryException,
            WarehouseDocumentTypeNotFoundException,
            PublicCostCenterNotFound,
            ProductItemAmountException,
            InventoryUnitaryBalanceException,
            InventoryProductItemNotFoundException,
            SoldProductDeliveredException,
            CompanyConfigurationNotFoundException,
            FinancesCurrencyNotFoundException,
            FinancesExchangeRateNotFoundException,
            EntryDuplicatedException,
            ConcurrencyException,
            ReferentialIntegrityException,
            ProductItemNotFoundException, SoldProductNotFoundException, SoldProductJustNoProducer;

    public void deliveryCustomerOrder(CustomerOrder customerOrder) throws
            InventoryException,
            ProductItemNotFoundException,
            ProductItemAmountException,
            CompanyConfigurationNotFoundException,
            FinancesExchangeRateNotFoundException,
            FinancesCurrencyNotFoundException,
            InventoryProductItemNotFoundException,
            ReferentialIntegrityException,
            ConcurrencyException,
            InventoryUnitaryBalanceException,
            EntryDuplicatedException;
    public void deliveryVentaDirecta(VentaDirecta ventaDirecta) throws
                InventoryException,
                ProductItemNotFoundException,
                ProductItemAmountException,
                CompanyConfigurationNotFoundException,
                FinancesExchangeRateNotFoundException,
                FinancesCurrencyNotFoundException,
                InventoryProductItemNotFoundException,
                ReferentialIntegrityException,
                ConcurrencyException,
                InventoryUnitaryBalanceException,
                EntryDuplicatedException;

    ProductDelivery select(ProductDelivery entity);

    public void updateOrderEstate(String invoiceNumber);

    public void updateVentaDirecta(Long idVentadirecta,String estado);

    public Boolean verifyAmounts(List<String> numberInvoices,List<OrderItem> orderItems,Date date,Employee distribuitor);

    void deliveryAll(List<String> numberInvoices) throws InventoryException, ProductItemNotFoundException, ProductItemAmountException, CompanyConfigurationNotFoundException, FinancesExchangeRateNotFoundException, FinancesCurrencyNotFoundException, InventoryProductItemNotFoundException, ReferentialIntegrityException, ConcurrencyException, InventoryUnitaryBalanceException, EntryDuplicatedException;

        public void deliveryCustomerOrders(List<CustomerOrder> pedidos) throws
                InventoryException,
                ProductItemNotFoundException,
                ProductItemAmountException,
                CompanyConfigurationNotFoundException,
                FinancesExchangeRateNotFoundException,
                FinancesCurrencyNotFoundException,
                InventoryProductItemNotFoundException,
                ReferentialIntegrityException,
                ConcurrencyException,
                InventoryUnitaryBalanceException,
                EntryDuplicatedException ;

        boolean verifyAmounts(CustomerOrder customerOrder);

        public boolean verifyAmounts(VentaDirecta ventaDirecta);

        boolean verifyAmounts(List<CustomerOrder> pedidos);
}
