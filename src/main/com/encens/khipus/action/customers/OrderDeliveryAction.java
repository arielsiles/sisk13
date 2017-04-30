package com.encens.khipus.action.customers;

import com.encens.khipus.exception.ConcurrencyException;
import com.encens.khipus.exception.EntryDuplicatedException;
import com.encens.khipus.exception.ReferentialIntegrityException;
import com.encens.khipus.exception.finances.CompanyConfigurationNotFoundException;
import com.encens.khipus.exception.finances.FinancesCurrencyNotFoundException;
import com.encens.khipus.exception.finances.FinancesExchangeRateNotFoundException;
import com.encens.khipus.exception.warehouse.*;
import com.encens.khipus.framework.action.GenericAction;
import com.encens.khipus.framework.action.Outcome;
import com.encens.khipus.model.customers.CustomerOrder;
import com.encens.khipus.model.warehouse.*;
import com.encens.khipus.service.warehouse.ProductDeliveryService;
import com.encens.khipus.service.warehouse.SoldProductService;
import com.encens.khipus.util.Constants;
import com.encens.khipus.util.MessageUtils;
import com.encens.khipus.util.ValidatorUtil;
import com.encens.khipus.util.warehouse.InventoryMessage;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.*;
import org.jboss.seam.annotations.security.Restrict;
import org.jboss.seam.international.StatusMessage;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * @author
 * @version 3.0
 */
@Name("orderDeliveryAction")
@Scope(ScopeType.CONVERSATION)
public class OrderDeliveryAction extends GenericAction<ProductDelivery> {

    private List<SoldProduct> soldProducts = new ArrayList<SoldProduct>();

    @In
    private SoldProductService soldProductService;

    @In
    private ProductDeliveryService productDeliveryService;

    private String orderNumber;
    private String messageSearchOrder;

    private ProductDeliveryType productDeliveryType = ProductDeliveryType.CASH_ORDER;

    @Override
    @End
    //@Restrict("#{s:hasPermission('PRODUCTDELIVERY','CREATE')}")
    public String create() {
        System.out.println("...Entregar pedido.... " + getInstance().getInvoiceNumber());
        System.out.println("......ValidatorUtil.isBlankOrNull.... " + ValidatorUtil.isBlankOrNull(getInstance().getInvoiceNumber()));

        if (ValidatorUtil.isBlankOrNull(getInstance().getInvoiceNumber())) {
            addInvoiceNumberRequiredMessage();
            System.out.println("....Outcome.REDISPLAY: " + Outcome.REDISPLAY);
            return Outcome.REDISPLAY;
        }
        try {
            ProductDelivery productDelivery = productDeliveryService.create(getInstance().getInvoiceNumber(),
                    MessageUtils.getMessage("ProductDelivery.warehouseVoucher.description", getInstance().getInvoiceNumber()));
            addSoldProductDeliveredInfoMessage();
            select(productDelivery);
            return Outcome.SUCCESS;
        } catch (SoldProductNotFoundException e) {
            addSoldProductNotFoundMessages();
            return Outcome.FAIL;
        } catch (InventoryException e) {
            addInventoryErrorMessages(e.getInventoryMessages());
            return Outcome.FAIL;
        } catch (PublicCostCenterNotFound publicCostCenterNotFound) {
            return Outcome.FAIL;
        } catch (WarehouseDocumentTypeNotFoundException e) {
            addWarehouseDocumentTypeErrorMessage();
            return Outcome.FAIL;
        } catch (ProductItemAmountException e) {
            addNotEnoughAmountMessage(e.getProductItem(), e.getAvailableAmount());
            return Outcome.FAIL;
        } catch (InventoryUnitaryBalanceException e) {
            addInventoryUnitaryBalanceErrorMessage(e.getAvailableUnitaryBalance(), e.getProductItem());
            return Outcome.FAIL;
        } catch (InventoryProductItemNotFoundException e) {
            addInventoryProductItemNotFoundErrorMessage(e.getExecutorUnitCode(),
                    e.getProductItem(), e.getWarehouse());
            return Outcome.FAIL;
        } catch (SoldProductDeliveredException e) {
            addSoldProductDeliveredErrorMessage();
            return Outcome.FAIL;
        } catch (CompanyConfigurationNotFoundException e) {
            addCompanyConfigurationNotFoundErrorMessage();
            return Outcome.FAIL;
        } catch (FinancesExchangeRateNotFoundException e) {
            addFinancesExchangeRateNotFoundExceptionMessage();
            return Outcome.FAIL;
        } catch (FinancesCurrencyNotFoundException e) {
            addFinancesExchangeRateNotFoundExceptionMessage();
            return Outcome.FAIL;
        } catch (ConcurrencyException e) {
            addUpdateConcurrencyMessage();
            return Outcome.FAIL;
        } catch (EntryDuplicatedException e) {
            addDuplicatedMessage();
            return Outcome.FAIL;
        } catch (ReferentialIntegrityException e) {
            addDeleteReferentialIntegrityMessage();
            return Outcome.FAIL;
        } catch (ProductItemNotFoundException e) {
            addProductItemNotFoundMessage(e.getProductItem().getFullName());
            return Outcome.FAIL;
        }
    }


   /* public void myCreate() {

        String numbers[] = {
//AL CONTADO
                "26002130",
                "26002141",
                "26002142",
                "26002145",
                "26002152",
                "26002153",
                "26002154",
                "26002162",
                "26002167",
                "26002168",
                "26002175",
                "26002176",
                "26002179",
                "26002182",
                "26002193",
                "26002195",
                "26002200",
                "26002201",
                "26002202",
                "26002203",
                "26002204",
                "26002211",
                "26002224",
                "26002226",
                "26002229",
                "26002230",
                "26002231",
                "26002232",
                "26002250",
                "26002253",




        };
        for(String number :numbers)
        {

            try {
                //for()
                System.out.println("NUMERO DE FACTURA -> "+number);
                ProductDelivery productDelivery = productDeliveryService.createAll(number,
                        MessageUtils.getMessage("ProductDelivery.warehouseVoucher.description", number));
                addSoldProductDeliveredInfoMessage();
                select(productDelivery);
                //update();
            } catch (SoldProductNotFoundException e) {
                addSoldProductNotFoundMessages();
                continue;
            } catch (InventoryException e) {
                addInventoryErrorMessages(e.getInventoryMessages());
                continue;
            } catch (PublicCostCenterNotFound publicCostCenterNotFound) {
                continue;
            } catch (WarehouseDocumentTypeNotFoundException e) {
                addWarehouseDocumentTypeErrorMessage();
                continue;
            } catch (ProductItemAmountException e) {
                addNotEnoughAmountMessage(e.getProductItem(), e.getAvailableAmount());
                continue;
            } catch (InventoryUnitaryBalanceException e) {
                addInventoryUnitaryBalanceErrorMessage(e.getAvailableUnitaryBalance(), e.getProductItem());
                continue;
            } catch (InventoryProductItemNotFoundException e) {
                addInventoryProductItemNotFoundErrorMessage(e.getExecutorUnitCode(),
                        e.getProductItem(), e.getWarehouse());
                continue;
            } catch (SoldProductDeliveredException e) {
                addSoldProductDeliveredErrorMessage();
                continue;
            } catch (CompanyConfigurationNotFoundException e) {
                addCompanyConfigurationNotFoundErrorMessage();
                continue;
            } catch (FinancesExchangeRateNotFoundException e) {
                addFinancesExchangeRateNotFoundExceptionMessage();
                continue;
            } catch (FinancesCurrencyNotFoundException e) {
                addFinancesExchangeRateNotFoundExceptionMessage();
                continue;
            } catch (ConcurrencyException e) {
                addUpdateConcurrencyMessage();
                continue;
            } catch (EntryDuplicatedException e) {
                addDuplicatedMessage();
                continue;
            } catch (ReferentialIntegrityException e) {
                addDeleteReferentialIntegrityMessage();
                continue;
            } catch (ProductItemNotFoundException e) {
                addProductItemNotFoundMessage(e.getProductItem().getFullName());
                continue;
            } catch (SoldProductJustNoProducer soldProductJustNoProducer) {
                soldProductJustNoProducer.printStackTrace();
                continue;
            }
        }
    }*/

    @Override
    @Begin(ifOutcome = Outcome.SUCCESS, flushMode = FlushModeType.MANUAL)
    @Restrict("#{s:hasPermission('PRODUCTDELIVERY','VIEW')}")
    public String select(ProductDelivery instance) {
        setOp(OP_UPDATE);
        setInstance(productDeliveryService.select(instance));
        readSoldProducts();
        return Outcome.SUCCESS;
    }

    public void search() {
        if (productDeliveryType.equals(ProductDeliveryType.CASH_SALE))
            searchCashSale();
        if (productDeliveryType.equals(ProductDeliveryType.CASH_ORDER))
            searchCashOrder();


    }

    private void searchCashSale() {

        List<SoldProduct> soldProductList = soldProductService.getSoldProductsCashSale(orderNumber, Constants.defaultCompanyNumber);
        if (ValidatorUtil.isEmptyOrNull(soldProductList)) {
            setMessageSearchOrder(MessageUtils.getMessage("ProductDelivery.messageSearchOrderNotFound"));
            getInstance().setInvoiceNumber(null);
            soldProducts.clear();
        } else {
            setMessageSearchOrder(null);
            if (soldProductList.get(0).getState().equals(SoldProductState.DELIVERED)) {
                setMessageSearchOrder(MessageUtils.getMessage("ProductDelivery.messageSearchOrderDelivered"));
                assignNumberCashSale(soldProductList.get(0));
            } else
                assignNumberCashSale(soldProductList.get(0));
        }
    }

    private void searchCashOrder() {

        List<SoldProduct> soldProductList = soldProductService.getSoldProductsCashOrder(orderNumber, Constants.defaultCompanyNumber);
        if (ValidatorUtil.isEmptyOrNull(soldProductList)) {
            setMessageSearchOrder(MessageUtils.getMessage("ProductDelivery.messageSearchOrderNotFound"));
            getInstance().setInvoiceNumber(null);
            soldProducts.clear();
        } else {
            setMessageSearchOrder(null);
            if (soldProductList.get(0).getState().equals(SoldProductState.DELIVERED)) {
                setMessageSearchOrder(MessageUtils.getMessage("ProductDelivery.messageSearchOrderDelivered"));
                assignNumberCashOrder(soldProductList.get(0));
            } else
                assignNumberCashOrder(soldProductList.get(0));
        }
    }


    /*@Factory(value = "productDeliveryTypes", scope = ScopeType.STATELESS)
    public ProductDeliveryType[] initProductDeliveryTypes() {
        return ProductDeliveryType.values();
    }*/

    public List<SoldProduct> getSoldProducts() {
        return soldProducts;
    }

    public void setSoldProducts(List<SoldProduct> soldProducts) {
        this.soldProducts = soldProducts;
    }

    public void readSoldProducts() {
        setSoldProducts(
                soldProductService.getSoldProducts(getInstance().getInvoiceNumber(), Constants.defaultCompanyNumber));
    }

    public void readSoldProductsCashSale() {
        setSoldProducts(
                soldProductService.getSoldProductsCashSale(getInstance().getInvoiceNumber(), Constants.defaultCompanyNumber));
    }

    public void readSoldProductsCashOrder() {
        setSoldProducts(
                soldProductService.getSoldProductsCashOrder(getInstance().getInvoiceNumber(), Constants.defaultCompanyNumber));
    }

    public boolean isExistsSoldProducts() {
        return !soldProducts.isEmpty();
    }

    public SoldProduct getSoldProduct() {
        return soldProducts.get(0);
    }


    public void assignInvoiceNumber(SoldProduct soldProduct) {
        getInstance().setInvoiceNumber(soldProduct.getInvoiceNumber());
        readSoldProducts();
    }

    public void assignNumberCashSale(SoldProduct soldProduct) {
        getInstance().setInvoiceNumber(soldProduct.getInvoiceNumber());
        readSoldProductsCashSale();
    }

    public void assignNumberCashOrder(SoldProduct soldProduct) {
        getInstance().setInvoiceNumber(soldProduct.getInvoiceNumber());
        readSoldProductsCashOrder();
    }

    public void cleanInvoiceNumber() {
        getInstance().setInvoiceNumber(null);
        soldProducts.clear();
        setOrderNumber(null);
        setMessageSearchOrder(null);
    }

    private void addInventoryErrorMessages(List<InventoryMessage> messages) {
        for (InventoryMessage message : messages) {
            if (message.isNotFound()) {
                facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                        "WarehouseVoucher.error.InventoryNotFound", message.getProductItem().getName());
                continue;
            }

            if (message.isNotEnough()) {
                facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                        "WarehouseVoucher.error.InventoryUnavailableProductItems", message.getProductItem().getName(),
                        message.getAvailableQuantity());
            }
        }
    }

    private void addInventoryUnitaryBalanceErrorMessage(BigDecimal availableUnitaryBalance,
                                                        ProductItem productItem) {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                "ProductDelivery.error.notEnoughUnitaryBalance",
                getInstance().getInvoiceNumber(),
                productItem.getName(),
                availableUnitaryBalance);
    }

    private void addNotEnoughAmountMessage(ProductItem productItem,
                                           BigDecimal availableAmount) {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                "ProductDelivery.error.notEnoughAmount",
                getInstance().getInvoiceNumber(),
                productItem.getName(),
                availableAmount);
    }

    private void addInventoryProductItemNotFoundErrorMessage(String executorUnitCode,
                                                             ProductItem productItem,
                                                             Warehouse warehouse) {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.WARN,
                "ProductDelivery.error.productItemNotFound",
                getInstance().getInvoiceNumber(),
                productItem.getName(),
                warehouse.getName(),
                executorUnitCode);
    }

    private void addWarehouseDocumentTypeErrorMessage() {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                "ProductDelivery.error.consumptionDocumentTypeNotFound", getInstance().getInvoiceNumber());
    }

    private void addSoldProductDeliveredErrorMessage() {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                "ProductDelivery.error.soldProductDelivered", getInstance().getInvoiceNumber());
    }

    private void addSoldProductDeliveredInfoMessage() {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.INFO,
                "ProductDelivery.info.soldProductDelivered", getInstance().getInvoiceNumber());
    }

    private void addFinancesExchangeRateNotFoundExceptionMessage() {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                "PurchaseOrder.financesExchangeRateNotFound");
    }

    private void addInvoiceNumberRequiredMessage() {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                "Common.required", messages.get("ProductDelivery.invoiceNumber"));
    }

    public void addProductItemNotFoundMessage(String productItemName) {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.WARN,
                "ProductItem.error.notFound", productItemName);
    }

    private void addSoldProductNotFoundMessages() {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.WARN,
                "ProductDelivery.info.soldProductNotFoundMessages", getInstance().getInvoiceNumber());
    }

    public String getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(String orderNumber) {
        this.orderNumber = orderNumber;
    }

    public String getMessageSearchOrder() {
        return messageSearchOrder;
    }

    public void setMessageSearchOrder(String messageSearchOrder) {
        this.messageSearchOrder = messageSearchOrder;
    }

    public ProductDeliveryType getProductDeliveryType() {
        return productDeliveryType;
    }

    public void setProductDeliveryType(ProductDeliveryType productDeliveryType) {
        this.productDeliveryType = productDeliveryType;
        System.out.println("___________SET ProductDeliveryType: " + this.productDeliveryType);
    }
}
