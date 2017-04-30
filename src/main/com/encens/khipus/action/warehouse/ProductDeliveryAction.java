package com.encens.khipus.action.warehouse;

import com.encens.khipus.exception.ConcurrencyException;
import com.encens.khipus.exception.EntryDuplicatedException;
import com.encens.khipus.exception.ReferentialIntegrityException;
import com.encens.khipus.exception.finances.CompanyConfigurationNotFoundException;
import com.encens.khipus.exception.finances.FinancesCurrencyNotFoundException;
import com.encens.khipus.exception.finances.FinancesExchangeRateNotFoundException;
import com.encens.khipus.exception.warehouse.*;
import com.encens.khipus.framework.action.GenericAction;
import com.encens.khipus.framework.action.Outcome;
import com.encens.khipus.model.customers.*;
import com.encens.khipus.model.employees.Employee;
import com.encens.khipus.model.warehouse.*;
import com.encens.khipus.service.customers.AccountItemService;
import com.encens.khipus.service.customers.OrderClient;
import com.encens.khipus.service.customers.OrderItem;
import com.encens.khipus.service.warehouse.ProductDeliveryService;
import com.encens.khipus.service.warehouse.ProductInventoryService;
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
import java.util.Date;
import java.util.List;

/**
 * @author
 * @version 3.0
 */
@Name("productDeliveryAction")
@Scope(ScopeType.CONVERSATION)
public class ProductDeliveryAction extends GenericAction<ProductDelivery> {

    private List<SoldProduct> soldProducts = new ArrayList<SoldProduct>();

    @In
    private SoldProductService soldProductService;

    @In
    private ProductDeliveryService productDeliveryService;

    @In
    private AccountItemService accountItemService;

    @In
    private ProductInventoryService productInventoryService;

    private String orderNumber;
    private String messageSearchOrder;

    private ProductDeliveryType productDeliveryType = ProductDeliveryType.CASH_ORDER;

    private Boolean showDeliveryOrder = true;
    private Date date = new Date();
    private List<OrderClient> orderClients = new ArrayList<OrderClient>();
    private List<OrderItem> orderItems = new ArrayList<OrderItem>();
    private List<String> numberInvoices = new ArrayList<String>();
    private List<BigDecimal> distributors = new ArrayList<BigDecimal>();
    private List<CustomerOrder> pedidos = new ArrayList<CustomerOrder>();
    private String product;
    private Employee distributor;
    private CustomerOrder customerOrder;
    private VentaDirecta ventaDirecta;
    private Territoriotrabajo territorioTrabajo;

    @Factory(value = "productDelivery", scope = ScopeType.STATELESS)
    @Restrict("#{s:hasPermission('PRODUCTDELIVERY','VIEW')}")
    public ProductDelivery initProductDelivery() {
        return getInstance();
    }

    @Override
    @End
    @Restrict("#{s:hasPermission('PRODUCTDELIVERY','CREATE')}")
    public String create() {

       if(customerOrder == null && ventaDirecta == null) {
           busqueUnpedidoMessage();
           return Outcome.REDISPLAY;
       }

        return deliveryOrder();

    }

    @End
    public String productDeliverySalesStock(){

        if(!ventaDirecta.getStockFlag()){
            productInventoryService.outputProducts(ventaDirecta);
            facesMessages.addFromResourceBundle(StatusMessage.Severity.INFO,"La VENTA se entrego exitosamente.");
            return Outcome.SUCCESS;
        }else{
            facesMessages.addFromResourceBundle(StatusMessage.Severity.INFO,"La VENTA ya fue entregada ANTERIORMENTE!!!");
            return Outcome.FAIL;
        }
    }

    @End
    public String productDeliveryOrderStock(){

        if(!customerOrder.getStockFlag()){
            productInventoryService.outputProducts(customerOrder);
            facesMessages.addFromResourceBundle(StatusMessage.Severity.INFO,"El PEDIDO se entrego exitosamente.");
            return Outcome.SUCCESS;
        }else{
            facesMessages.addFromResourceBundle(StatusMessage.Severity.INFO,"El PEDIDO ya fue entregadO ANTERIORMENTE!!!");
            return Outcome.FAIL;
        }
    }

    @End
    public String productDeliveryOrderByDate(){

        productInventoryService.outputProducts(pedidos);
        facesMessages.addFromResourceBundle(StatusMessage.Severity.INFO,"Los PEDIDOS se entregaron exitsamente!!!");
        return Outcome.SUCCESS;

    }

    @End
    @Restrict("#{s:hasPermission('PRODUCTDELIVERY','CREATE')}")
    public String entregarTodos() {

        if(pedidos.size() == 0) {
            busquePedidosMessage();
            return Outcome.REDISPLAY;
        }

        return entregarPedidos();

    }

    private String deliveryOrderWhitoutDate() {
        if (ValidatorUtil.isBlankOrNull(getInstance().getInvoiceNumber())) {
            addInvoiceNumberRequiredMessage();
            return Outcome.REDISPLAY;
        }
        try {
            ProductDelivery productDelivery = productDeliveryService.createAll(getInstance().getInvoiceNumber(),
                    MessageUtils.getMessage("ProductDelivery.warehouseVoucher.description", getInstance().getInvoiceNumber()));
            addSoldProductDeliveredInfoMessage();
            select(productDelivery);
            return Outcome.SUCCESS;
        } catch (SoldProductNotFoundException e) {
            addSoldProductNotFoundMessages();
            return Outcome.REDISPLAY;
        } catch (InventoryException e) {
            addInventoryErrorMessages(e.getInventoryMessages());
            return Outcome.REDISPLAY;
        } catch (PublicCostCenterNotFound publicCostCenterNotFound) {
            return Outcome.REDISPLAY;
        } catch (WarehouseDocumentTypeNotFoundException e) {
            addWarehouseDocumentTypeErrorMessage();
            return Outcome.REDISPLAY;
        } catch (ProductItemAmountException e) {
            addNotEnoughAmountMessage(e.getProductItem(), e.getAvailableAmount());
            return Outcome.REDISPLAY;
        } catch (InventoryUnitaryBalanceException e) {
            addInventoryUnitaryBalanceErrorMessage(e.getAvailableUnitaryBalance(), e.getProductItem());
            return Outcome.REDISPLAY;
        } catch (InventoryProductItemNotFoundException e) {
            addInventoryProductItemNotFoundErrorMessage(e.getExecutorUnitCode(),
                    e.getProductItem(), e.getWarehouse());
            return Outcome.REDISPLAY;
        } catch (SoldProductDeliveredException e) {
            addSoldProductDeliveredErrorMessage();
            return Outcome.REDISPLAY;
        } catch (CompanyConfigurationNotFoundException e) {
            addCompanyConfigurationNotFoundErrorMessage();
            return Outcome.REDISPLAY;
        } catch (FinancesExchangeRateNotFoundException e) {
            addFinancesExchangeRateNotFoundExceptionMessage();
            return Outcome.REDISPLAY;
        } catch (FinancesCurrencyNotFoundException e) {
            addFinancesExchangeRateNotFoundExceptionMessage();
            return Outcome.REDISPLAY;
        } catch (ConcurrencyException e) {
            addUpdateConcurrencyMessage();
            return Outcome.REDISPLAY;
        } catch (EntryDuplicatedException e) {
            addDuplicatedMessage();
            return Outcome.REDISPLAY;
        } catch (ReferentialIntegrityException e) {
            addDeleteReferentialIntegrityMessage();
            return Outcome.REDISPLAY;
        } catch (ProductItemNotFoundException e) {
            addProductItemNotFoundMessage(e.getProductItem().getFullName());
            return Outcome.REDISPLAY;
        } catch (SoldProductJustNoProducer soldProductJustNoProducer) {
            addSoldProductJustNoProducerMessages();
            return Outcome.REDISPLAY;
        }
    }

    private String entregarPedidos() {
        if(productDeliveryService.verifyAmounts(pedidos))
            return Outcome.REDISPLAY;
        else
        {
        /*if(fueEntregado())
        {
            return Outcome.REDISPLAY;
        }*/

        try {
            productDeliveryService.deliveryCustomerOrders(pedidos);
            addAllDeliveryMessage();
            return Outcome.SUCCESS;
        } catch (InventoryException e) {
            addInventoryErrorMessages(e.getInventoryMessages());
            return Outcome.REDISPLAY;
        } catch (ProductItemNotFoundException e) {
            addProductItemNotFoundMessage(e.getProductItem().getFullName());
            return Outcome.REDISPLAY;
        } catch (ProductItemAmountException e) {
            addNotEnoughAmountMessage(e.getProductItem(), e.getAvailableAmount());
            return Outcome.REDISPLAY;
        } catch (CompanyConfigurationNotFoundException e) {
            addCompanyConfigurationNotFoundErrorMessage();
            return Outcome.REDISPLAY;
        } catch (FinancesExchangeRateNotFoundException e) {
            addFinancesExchangeRateNotFoundExceptionMessage();
            return Outcome.REDISPLAY;
        } catch (FinancesCurrencyNotFoundException e) {
            addFinancesExchangeRateNotFoundExceptionMessage();
            return Outcome.REDISPLAY;
        } catch (InventoryProductItemNotFoundException e) {
            addInventoryProductItemNotFoundErrorMessage(e.getExecutorUnitCode(),
                    e.getProductItem(), e.getWarehouse());
            return Outcome.REDISPLAY;
        } catch (ReferentialIntegrityException e) {
            addDeleteReferentialIntegrityMessage();
            return Outcome.REDISPLAY;
        } catch (ConcurrencyException e) {
            addUpdateConcurrencyMessage();
            return Outcome.REDISPLAY;
        } catch (InventoryUnitaryBalanceException e) {
            addInventoryUnitaryBalanceErrorMessage(e.getAvailableUnitaryBalance(), e.getProductItem());
            return Outcome.REDISPLAY;
        } catch (EntryDuplicatedException e) {
            addDuplicatedMessage();
            return Outcome.REDISPLAY;
        }

        }
    }

    private String deliveryOrder() {
        if(productDeliveryService.verifyAmounts(customerOrder) || productDeliveryService.verifyAmounts(ventaDirecta))
            return Outcome.REDISPLAY;
        else
        {
            if(fueEntregado())
            {
                return Outcome.REDISPLAY;
            }

            try {
                if(customerOrder != null)
                productDeliveryService.deliveryCustomerOrder(customerOrder);
                if(ventaDirecta != null)
                productDeliveryService.deliveryVentaDirecta(ventaDirecta);
                fueEntregadoConExitoMessage();
                return Outcome.SUCCESS;
            } catch (InventoryException e) {
                addInventoryErrorMessages(e.getInventoryMessages());
                return Outcome.REDISPLAY;
            } catch (ProductItemNotFoundException e) {
                addProductItemNotFoundMessage(e.getProductItem().getFullName());
                return Outcome.REDISPLAY;
            } catch (ProductItemAmountException e) {
                addNotEnoughAmountMessage(e.getProductItem(), e.getAvailableAmount());
                return Outcome.REDISPLAY;
            } catch (CompanyConfigurationNotFoundException e) {
                addCompanyConfigurationNotFoundErrorMessage();
                return Outcome.REDISPLAY;
            } catch (FinancesExchangeRateNotFoundException e) {
                addFinancesExchangeRateNotFoundExceptionMessage();
                return Outcome.REDISPLAY;
            } catch (FinancesCurrencyNotFoundException e) {
                addFinancesExchangeRateNotFoundExceptionMessage();
                return Outcome.REDISPLAY;
            } catch (InventoryProductItemNotFoundException e) {
                addInventoryProductItemNotFoundErrorMessage(e.getExecutorUnitCode(),
                        e.getProductItem(), e.getWarehouse());
                return Outcome.REDISPLAY;
            } catch (ReferentialIntegrityException e) {
                addDeleteReferentialIntegrityMessage();
                return Outcome.REDISPLAY;
            } catch (ConcurrencyException e) {
                addUpdateConcurrencyMessage();
                return Outcome.REDISPLAY;
            } catch (InventoryUnitaryBalanceException e) {
                addInventoryUnitaryBalanceErrorMessage(e.getAvailableUnitaryBalance(), e.getProductItem());
                return Outcome.REDISPLAY;
            } catch (EntryDuplicatedException e) {
                addDuplicatedMessage();
                return Outcome.REDISPLAY;
            }

        }
    }

    private String deliveryInCatch()
    {
        if (ValidatorUtil.isBlankOrNull(getInstance().getInvoiceNumber())) {
            addInvoiceNumberRequiredMessage();
            return Outcome.REDISPLAY;
        }
        try {
            ProductDelivery productDelivery = productDeliveryService.createAll(getInstance().getInvoiceNumber(),
                    MessageUtils.getMessage("ProductDelivery.warehouseVoucher.description", getInstance().getInvoiceNumber()));
            addSoldProductDeliveredInfoMessage();
            select(productDelivery);
            return Outcome.SUCCESS;
        } catch (SoldProductNotFoundException e) {
            addSoldProductNotFoundMessages();
            return Outcome.REDISPLAY;
        } catch (InventoryException e) {
            addInventoryErrorMessages(e.getInventoryMessages());
            return Outcome.REDISPLAY;
        } catch (PublicCostCenterNotFound publicCostCenterNotFound) {
            return Outcome.REDISPLAY;
        } catch (WarehouseDocumentTypeNotFoundException e) {
            addWarehouseDocumentTypeErrorMessage();
            return Outcome.REDISPLAY;
        } catch (ProductItemAmountException e) {
            addNotEnoughAmountMessage(e.getProductItem(), e.getAvailableAmount());
            return Outcome.REDISPLAY;
        } catch (InventoryUnitaryBalanceException e) {
            addInventoryUnitaryBalanceErrorMessage(e.getAvailableUnitaryBalance(), e.getProductItem());
            return Outcome.REDISPLAY;
        } catch (InventoryProductItemNotFoundException e) {
            addInventoryProductItemNotFoundErrorMessage(e.getExecutorUnitCode(),
                    e.getProductItem(), e.getWarehouse());
            return Outcome.REDISPLAY;
        } catch (SoldProductDeliveredException e) {
            addSoldProductDeliveredErrorMessage();
            return Outcome.REDISPLAY;
        } catch (CompanyConfigurationNotFoundException e) {
            addCompanyConfigurationNotFoundErrorMessage();
            return Outcome.REDISPLAY;
        } catch (FinancesExchangeRateNotFoundException e) {
            addFinancesExchangeRateNotFoundExceptionMessage();
            return Outcome.REDISPLAY;
        } catch (FinancesCurrencyNotFoundException e) {
            addFinancesExchangeRateNotFoundExceptionMessage();
            return Outcome.REDISPLAY;
        } catch (ConcurrencyException e) {
            addUpdateConcurrencyMessage();
            return Outcome.REDISPLAY;
        } catch (EntryDuplicatedException e) {
            addDuplicatedMessage();
            return Outcome.REDISPLAY;
        } catch (ReferentialIntegrityException e) {
            addDeleteReferentialIntegrityMessage();
            return Outcome.REDISPLAY;
        } catch (ProductItemNotFoundException e) {
            addProductItemNotFoundMessage(e.getProductItem().getFullName());
            return Outcome.REDISPLAY;
        } catch (SoldProductJustNoProducer soldProductJustNoProducer) {
            addSoldProductJustNoProducerMessages();
            return Outcome.REDISPLAY;
        }

    }


    public void myCreate() {

        String numbers[] = {
                "26004386"
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
            } catch (InventoryException e) {
                addInventoryErrorMessages(e.getInventoryMessages());
            } catch (PublicCostCenterNotFound publicCostCenterNotFound) {
            } catch (WarehouseDocumentTypeNotFoundException e) {
                addWarehouseDocumentTypeErrorMessage();
            } catch (ProductItemAmountException e) {
                addNotEnoughAmountMessage(e.getProductItem(), e.getAvailableAmount());
            } catch (InventoryUnitaryBalanceException e) {
                addInventoryUnitaryBalanceErrorMessage(e.getAvailableUnitaryBalance(), e.getProductItem());
            } catch (InventoryProductItemNotFoundException e) {
                addInventoryProductItemNotFoundErrorMessage(e.getExecutorUnitCode(),
                        e.getProductItem(), e.getWarehouse());
            } catch (SoldProductDeliveredException e) {
                addSoldProductDeliveredErrorMessage();
            } catch (CompanyConfigurationNotFoundException e) {
                addCompanyConfigurationNotFoundErrorMessage();
            } catch (FinancesExchangeRateNotFoundException e) {
                addFinancesExchangeRateNotFoundExceptionMessage();
            } catch (FinancesCurrencyNotFoundException e) {
                addFinancesExchangeRateNotFoundExceptionMessage();
            } catch (ConcurrencyException e) {
                addUpdateConcurrencyMessage();
            } catch (EntryDuplicatedException e) {
                addDuplicatedMessage();
            } catch (ReferentialIntegrityException e) {
                addDeleteReferentialIntegrityMessage();
            } catch (ProductItemNotFoundException e) {
                addProductItemNotFoundMessage(e.getProductItem().getFullName());
            }catch (SoldProductJustNoProducer soldProductJustNoProducer) {
                addSoldProductJustNoProducerMessages();
            }
        }
    }

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
        customerOrder = soldProductService.findPedidoPorCodigo(orderNumber);
        if(customerOrder == null)
        setMessageSearchOrder(MessageUtils.getMessage("ProductDelivery.messageSearchOrderNotFound"));
        getInstance().setInvoiceNumber(null);
        soldProducts.clear();
    }

    public void searchVentaDirecta() {
        ventaDirecta = soldProductService.findVentaPorCodigo(orderNumber);
        if(ventaDirecta == null)
            setMessageSearchOrder(MessageUtils.getMessage("ProductDelivery.messageSearchOrderNotFound"));
        getInstance().setInvoiceNumber(null);
        soldProducts.clear();
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

    public void searchCashOrderWithoutDate() {

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

    public void buscarPorTerritorioFecha(){
        pedidos.clear();
        orderClients.clear();
        pedidos = soldProductService.findPedidosPorFechaTerritorio(date,territorioTrabajo);
        System.out.println("------ >>> Cant Pedidos Find: " + pedidos.size() );
        setMessageSearchOrder("");
        List<Territoriotrabajo> territorios = new ArrayList<Territoriotrabajo>();
        if(territorioTrabajo == null)
        {
            territorios = accountItemService.findTerritoriosPedido(date);
            for(Territoriotrabajo territorio:territorios) {
                for (CustomerOrder pedido : soldProductService.findPedidosPorFechaTerritorio(date,territorio)) {
                    OrderClient orderClient = new OrderClient();
                    orderClient.setIdOrder(pedido.getCodigo().toString());
                    orderClient.setName(pedido.getCodigo().toString() + "-" + pedido.getCliente().getNombreCompleto());
                    orderClient.setType("CLIENTE");
                    orderClient.setState(pedido.getEstado());
                    orderClient.setCliente(pedido.getCliente());
                    orderClient.setStockFlag(pedido.getStockFlag());
                    orderClients.add(orderClient);
                }
                OrderClient orderClient = new OrderClient();
                orderClient.setName(territorio.getNombre());
                orderClient.setType("TERRITORIO");
                orderClients.add(orderClient);
            }

        }else{
            for (CustomerOrder pedido : pedidos) {
                OrderClient orderClient = new OrderClient();
                orderClient.setIdOrder(pedido.getCodigo().toString());
                orderClient.setName(pedido.getCliente().getNombreCompleto());
                orderClient.setType("CLIENTE");
                orderClient.setStockFlag(pedido.getStockFlag());
                orderClients.add(orderClient);
            }
            OrderClient orderClient = new OrderClient();
            orderClient.setName(territorioTrabajo.getNombre());
            orderClient.setType("TERRITORIO");
            orderClients.add(orderClient);
        }

        orderItems = accountItemService.findArticulosPorFecha(date);
        if(orderItems.size()==0)
        {
            setMessageSearchOrder("Na hay pedidos para entregar en esta fecha.");
        }
    }
     private List<Territoriotrabajo> getTerritorios()
     {
        List<Territoriotrabajo> territorios = new ArrayList<Territoriotrabajo>();
         for(CustomerOrder pedido:pedidos){
             territorios.add(pedido.getCliente().getTerritoriotrabajo());
         }
        return territorios;
     }

    private void searchCashOrder() {
        distributors = accountItemService.findDistributor(date);
        orderClients.clear();
        orderItems.clear();
        numberInvoices.clear();
        setMessageSearchOrder("");
        if(distributor != null)
        {
            orderClients.addAll(accountItemService.findClientsOrder(new BigDecimal(distributor.getId()),date));
            OrderClient distributorNew = new OrderClient();
            distributorNew.setName(accountItemService.getNameEmployeed(new BigDecimal(distributor.getId())));
            distributorNew.setIdDistributor(new BigDecimal(distributor.getId()));
            distributorNew.setType("DISTRIBUTOR");
            orderClients.add(distributorNew);
        }
        else
            for(BigDecimal idDistributor:distributors)
            {
                orderClients.addAll(accountItemService.findClientsOrder(idDistributor,date));
                OrderClient distributor = new OrderClient();
                distributor.setName(accountItemService.getNameEmployeed(idDistributor));
                distributor.setIdDistributor(idDistributor);
                distributor.setType("DISTRIBUTOR");
                orderClients.add(distributor);
            }

        orderItems = accountItemService.findOrderItemByState(date);
        orderItems.addAll(accountItemService.findOrderItemPackByState(date));
        /*OrderItem item = new OrderItem("ESTADO");
        orderItems.add(0,item);*/
        if(distributor != null)
            numberInvoices = soldProductService.getSoldProductsCashOrder(date,new BigDecimal(distributor.getId()));
        else
            numberInvoices = soldProductService.getSoldProductsCashOrder(date);

        /*if(wasDelivery())
            setMessageSearchOrder(MessageUtils.getMessage("ProductDelivery.messageWasDelivery"));
        else*/
        if (ValidatorUtil.isEmptyOrNull(orderClients)) {
            setMessageSearchOrder(MessageUtils.getMessage("ProductDelivery.messageSearchOrderDateNotFound"));
            getInstance().setInvoiceNumber(null);
        }
    }

    private boolean verifyWasDelivery() {
        Boolean result = false;
        for(OrderClient orderClient: orderClients)
        {
            if(orderClient.getState()!= null)
                if(orderClient.getState().equals("ECH")) {
                    setMessageSearchOrder(MessageUtils.getMessage("ProductDelivery.messageWasDeliveryOrder",orderClient.getIdOrder()));
                    result = true;
                }
        }
        return result;
    }

    private boolean fueEntregado() {
        if(customerOrder != null)
            if(customerOrder.getEstado()!= null) {
                if (!customerOrder.getEstado().equals("PREPARAR")) {
                    setMessageSearchOrder(MessageUtils.getMessage("ProductDelivery.messageWasDeliveryOrder", customerOrder.getCodigo()));
                    return true;
                }
            }

        if(ventaDirecta != null)
            if(ventaDirecta.getEstado()!=null){
                if(!ventaDirecta.getEstado().equals("PREPARAR")){
                    setMessageSearchOrder(MessageUtils.getMessage("ProductDelivery.messageWasDeliveryOrder", ventaDirecta.getCodigo()));
                    return true;
                }
            }

        return false;
    }


    private boolean wasDelivery() {
        for(OrderClient orderClient: orderClients)
        {
            if(orderClient.getState()!= null)
                if(orderClient.getState().equals("ECH")) {
                    return true;
                }
        }
        return false;
    }


    @Factory(value = "productDeliveryTypes", scope = ScopeType.STATELESS)
    public ProductDeliveryType[] initProductDeliveryTypes() {
        return ProductDeliveryType.values();
    }

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
        orderItems.clear();
        orderClients.clear();
        numberInvoices.clear();
        distributor = null;
        pedidos.clear();
        setVentaDirecta(null);
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

    private void busqueUnpedidoMessage() {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.INFO,"Primeramente busque un pedido.");
    }

    private void busquePedidosMessage() {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.INFO,"No hay pedidos para entregar.");
    }

    private void fueEntregadoConExitoMessage() {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.INFO,"El pedido fué entegado exitosamente");
    }

    private void addAllDeliveryMessage() {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.INFO,
                "ProductDelivery.allDelive.description",date);
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

    private void addSoldProductJustNoProducerMessages() {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.WARN,
                "ProductDelivery.info.soldProductJustNoProducerMessages", getInstance().getInvoiceNumber());
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
        showDeliveryOrder = productDeliveryType.getResourceKey().compareTo("ProductDeliveryType.cashOrder") == 0 ;
    }

    public Boolean getShowDeliveryOrder() {
        return showDeliveryOrder;
    }

    public void setShowDeliveryOrder(Boolean showDeliveryOrder) {
        this.showDeliveryOrder = showDeliveryOrder;
    }

    public String getProduct() {
        return product;
    }

    public List<OrderClient> getOrderClients() {
        return orderClients;
    }

    public void setOrderClients(List<OrderClient> orderClients) {
        this.orderClients = orderClients;
    }

    public List<OrderItem> getOrderItems() {
        return orderItems;
    }

    public void setOrderItems(List<OrderItem> orderItems) {
        this.orderItems = orderItems;
    }

    public Integer getAmountSoldProduct(OrderClient client,OrderItem item){
        Integer val;
        if(item.getType() == "COMBO" )
            if(client.getType() != null) {
                val = accountItemService.getAmountCombo(item.getCodArt(), client.getIdDistributor(), date);
            }
            else
            val = accountItemService.getAmountCombo(item.getCodArt(),client.getIdOrder());
        else
            if(client.getType() != null) {
                val = accountItemService.getAmountByDateAndDistributorOrder(item.getCodArt(),client.getIdDistributor(),date);
                val += accountItemService.getAmountByDateAndDistributorInstitution(item.getCodArt(), client.getIdDistributor(), date);
            }
            else
            val = accountItemService.getAmount(item.getCodArt(),client.getIdOrder());

        return val;
    }

    public Integer getCantidad(OrderClient client,OrderItem item){
        Integer cant= 0;
        if(!client.getType().equals("TERRITORIO"))
        for(CustomerOrder pedido:pedidos)
        {
            if(pedido.getCodigo().toString().equals(client.getIdOrder()))
            {
                for(ArticleOrder articulo: pedido.getArticulosPedidos()) {
                    if(articulo.getCodArt().equals(item.getCodArt()))
                    {
                        cant = articulo.getTotal();
                    }
                }
            }
        }
        else
            for(CustomerOrder pedido:pedidos)
            {
                if(pedido.getCliente().getTerritoriotrabajo().getNombre().equals(client.getName()))
                {
                    for(ArticleOrder articulo: pedido.getArticulosPedidos()) {
                        if(articulo.getCodArt().equals(item.getCodArt()))
                        {
                            cant += articulo.getTotal();
                        }
                    }
                }
            }


        return cant;
    }

    public Integer getCantidadTotal(OrderItem item){
        Integer total = 0;
        for(CustomerOrder pedido:pedidos)
        {
                for(ArticleOrder articulo: pedido.getArticulosPedidos())
                {
                    if(articulo.getCodArt().equals(item.getCodArt()))
                    {
                        total +=articulo.getTotal();
                    }
                }
        }
        return total;
    }

    public Integer getAmountSoldProductTotal(OrderItem item){
        if(item.getType()=="ESTADO")
            return 0;
        Integer val;

        val = accountItemService.getAmountByDateAndDistributorOrder(item.getCodArt(),date);
        val += accountItemService.getAmountByDateAndDistributorInstitution(item.getCodArt(), date);
        val += accountItemService.getAmountComboTotal(item.getCodArt(), date);
        return val;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Employee getDistributor() {
        return distributor;
    }

    public void setDistributor(Employee distributor) {
        this.distributor = distributor;
    }

    public void cleanDistributor(){
        setDistributor(null);
    }

    public CustomerOrder getCustomerOrder() {
        return customerOrder;
    }

    public void setCustomerOrder(CustomerOrder customerOrder) {
        this.customerOrder = customerOrder;
    }

    public List<CustomerOrder> getPedidos() {
        return pedidos;
    }

    public void setPedidos(List<CustomerOrder> pedidos) {
        this.pedidos = pedidos;
    }

    public Territoriotrabajo getTerritorioTrabajo() {
        return territorioTrabajo;
    }

    public void setTerritorioTrabajo(Territoriotrabajo territorioTrabajo) {
        this.territorioTrabajo = territorioTrabajo;
    }

    public VentaDirecta getVentaDirecta() {
        return ventaDirecta;
    }

    public void setVentaDirecta(VentaDirecta ventaDirecta) {
        this.ventaDirecta = ventaDirecta;
    }
}
