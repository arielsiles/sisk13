package com.encens.khipus.service.warehouse;

import com.encens.khipus.exception.ConcurrencyException;
import com.encens.khipus.exception.EntryDuplicatedException;
import com.encens.khipus.exception.ReferentialIntegrityException;
import com.encens.khipus.exception.finances.CompanyConfigurationNotFoundException;
import com.encens.khipus.exception.finances.FinancesCurrencyNotFoundException;
import com.encens.khipus.exception.finances.FinancesExchangeRateNotFoundException;
import com.encens.khipus.exception.warehouse.*;
import com.encens.khipus.framework.service.GenericServiceBean;
import com.encens.khipus.model.customers.ArticleOrder;
import com.encens.khipus.model.customers.ArticulosPromocion;
import com.encens.khipus.model.customers.CustomerOrder;
import com.encens.khipus.model.customers.VentaDirecta;
import com.encens.khipus.model.employees.Employee;
import com.encens.khipus.model.finances.CostCenter;
import com.encens.khipus.model.finances.CostCenterPk;
import com.encens.khipus.model.warehouse.*;
import com.encens.khipus.service.customers.AccountItemService;
import com.encens.khipus.service.customers.OrderItem;
import com.encens.khipus.service.finances.FinancesPkGeneratorService;
import com.encens.khipus.util.Constants;
import com.encens.khipus.util.DateUtils;
import com.encens.khipus.util.MessageUtils;
import com.encens.khipus.util.ValidatorUtil;
import com.encens.khipus.util.query.QueryUtils;
import com.encens.khipus.util.warehouse.InventoryMessage;
import org.apache.commons.lang.StringUtils;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import java.math.BigDecimal;
import java.util.*;

/**
 * @author
 * @version 2.4
 */
@Stateless
@Name("productDeliveryService")
@AutoCreate
public class ProductDeliveryServiceBean extends GenericServiceBean implements ProductDeliveryService {

    @In
    private SoldProductService soldProductService;

    @In
    private ApprovalWarehouseVoucherService approvalWarehouseVoucherService;

    @In
    private WarehouseService warehouseService;

    @In
    private MovementDetailService movementDetailService;

    @In
    private ProductItemService productItemService;

    @In
    protected FacesMessages facesMessages;

    @In
    private AccountItemService accountItemService;

    @In
    private InventoryService inventoryService;

    @In
    private FinancesPkGeneratorService financesPkGeneratorService;

    @In(value = "monthProcessService")
    private MonthProcessService monthProcessService;

    @In(value = "#{listEntityManager}")
    private EntityManager listEm;

    public ProductDelivery select(ProductDelivery entity) {
        return getEntityManager().find(ProductDelivery.class, entity.getId());
    }

    @Override
    public void updateOrderEstate(String invoiceNumber) {

        getEntityManager().createNativeQuery("update pedidos set estado_pedido = :state where pedido = :pedido")
                .setParameter("pedido", invoiceNumber)
                .setParameter("state", Constants.ESTATE_ORDER_DELIVERED)
                .executeUpdate();
    }

    public void updateCustomerOrder(Long idCustomreOrder,String estado) {

        getEntityManager().createNativeQuery("update pedidos set estado = :state where IDPEDIDOS = :pedido")
                .setParameter("pedido", idCustomreOrder)
                .setParameter("state", estado)
                .executeUpdate();
    }

    public void updateVentaDirecta(Long idVentadirecta,String estado) {

        getEntityManager().createNativeQuery("update ventadirecta set estado = :state where IDVENTADIRECTA = :venta")
                .setParameter("venta", idVentadirecta)
                .setParameter("state", estado)
                .executeUpdate();
    }

    public void updateOrderIncashEstate(String invoiceNumber) {

        getEntityManager().createNativeQuery("update inv_ventart set ESTADO = :state where no_fact = :pedido")
                .setParameter("pedido", invoiceNumber)
                .setParameter("state", Constants.ESTATE_ORDER_DELIVERED_INCASH)
                .executeUpdate();
    }

    @SuppressWarnings(value = "unchecked")
    public void deliveryAll(List<String> numberInvoices) throws
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
            EntryDuplicatedException {
        List<String> noProducers = findNoProduceres();
        for (String invoiceNumber : numberInvoices) {

            String warehouseDescription = MessageUtils.getMessage("ProductDelivery.warehouseVoucher.description", invoiceNumber);
            List<SoldProduct> soldProducts = soldProductService.getSoldProducts(invoiceNumber, Constants.defaultCompanyNumber);
            soldProducts = removeNotProduced(soldProducts,noProducers);
            if(soldProducts.size() != 0) {
                int pos = changeEdamToPressed(soldProducts);
                WarehouseDocumentType documentType = getFirstConsumptionType();
                //always exist almost one sold product that will be delivery
                SoldProduct firstSoldProduct = soldProducts.get(0);
                Warehouse warehouse = firstSoldProduct.getWarehouse();
                CostCenter costCenter = findPublicCostCenter(warehouse);
                Employee responsible = getEntityManager().find(Employee.class, warehouse.getResponsibleId());

                WarehouseVoucher warehouseVoucher = createWarehouseVoucherAll(documentType, warehouse, responsible, costCenter, warehouseDescription, soldProducts);

                Map<MovementDetail, BigDecimal> movementDetailUnderMinimalStockMap = new HashMap<MovementDetail, BigDecimal>();
                Map<MovementDetail, BigDecimal> movementDetailOverMaximumStockMap = new HashMap<MovementDetail, BigDecimal>();
                List<MovementDetail> movementDetailWithoutWarnings = new ArrayList<MovementDetail>();

                try {

                    approvalWarehouseVoucherService.approveWarehouseVoucherFromDeliveryProduct(warehouseVoucher.getId(),
                            getGlossMessage(warehouseVoucher, warehouseDescription),
                            movementDetailUnderMinimalStockMap,
                            movementDetailOverMaximumStockMap,
                            movementDetailWithoutWarnings);

                } catch (WarehouseVoucherApprovedException e) {
                    log.debug("This exception never happen because I create a pending WarehouseVoucher.");
                } catch (WarehouseVoucherNotFoundException e) {
                    log.debug("This exception never happen because I create a new WarehouseVoucher.");
                } catch (WarehouseVoucherEmptyException e) {
                    log.debug("This exception never happen because I create a WarehouseVoucher with details inside.");
                } catch (WarehouseAccountCashNotFoundException e) {
                    e.printStackTrace();
                }

                ProductDelivery productDelivery = new ProductDelivery();
                productDelivery.setCompanyNumber(warehouse.getId().getCompanyNumber());
                productDelivery.setInvoiceNumber(firstSoldProduct.getInvoiceNumber());
                productDelivery.setWarehouseVoucher(warehouseVoucher);

                create(productDelivery);

                if (pos > -1) {
                    resetChange(soldProducts.get(pos));
                    approvalWarehouseVoucherService.resetChangeCheeseEdam(warehouseVoucher.getId(), pos);
                    ProductItem cheesePressed = productItemService.findProductItemByCode(Constants.COD_CHEESE_PRESSED);
                    ProductItem cheeseEDAM = productItemService.findProductItemByCode(Constants.COD_CHEESE_EDAM);
                    cheeseEDAM.setUnitCost(cheesePressed.getUnitCost());
                    try {
                        productItemService.updateProductItem(cheeseEDAM);
                    } catch (ProductItemMinimalStockIsGreaterThanMaximumStockException e) {
                        e.printStackTrace();
                    }
                }

                //para que cambio todos los estados iuncluido el queso recorte y edam
                soldProducts = soldProductService.getSoldProducts(invoiceNumber, Constants.defaultCompanyNumber);

                for (SoldProduct soldProduct : soldProducts) {
                    soldProduct.setProductDelivery(productDelivery);
                    soldProduct.setState(SoldProductState.DELIVERED);
                    soldProduct.setNumberVoucher(warehouseVoucher.getNumber());
                    update(soldProduct);
                }

                //update state of order
                if (firstSoldProduct.getOrderNumber() != null)
                    updateOrderEstate(firstSoldProduct.getOrderNumber());
            }
        }
    }

    @Override
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
            EntryDuplicatedException {
            for(CustomerOrder pedido:pedidos) {
                deliveryCustomerOrder(pedido);
            }
    }

    //todo: que hacer con los no producidos
    //todo: en caso que sea un combo verificar sus articulos
    //todo: que hacer con el queso edam y el queso recorte
    //todo: que hacer con los productos de prueba
    @Override
    public boolean verifyAmounts(CustomerOrder customerOrder) {
        Boolean result = false;
        if(customerOrder == null)
            return result;
        if(new ArrayList<ArticleOrder>(customerOrder.getArticulosPedidos()).size() == 0)
            return result;

        List<InventoryMessage> errorMessages = new ArrayList<InventoryMessage>();

        WarehouseDocumentType documentType = getFirstConsumptionType();

        Warehouse warehouse = inventoryService.findWarehouseByItemArticle(new ArrayList<ArticleOrder>(customerOrder.getArticulosPedidos()).get(0).getProductItem());
        CostCenter costCenter = findPublicCostCenter(warehouse);

        for(ArticleOrder articulo:customerOrder.getArticulosPedidos()) {
            if(!StringUtils.isEmpty(articulo.getTipo())){
            if(articulo.getTipo().equals("COMBO")){
                List<ArticulosPromocion> articulosCombo = productItemService.findArticuloCombo(articulo);
                for(ArticulosPromocion articuloCombo :articulosCombo) {
                    try {

                        approvalWarehouseVoucherService.validateOutputQuantity(new BigDecimal(articuloCombo.getCantidad()*articulo.getAmount()),
                                warehouse,
                                articuloCombo.getVentaarticulo().getProductItem(),
                                costCenter);
                    } catch (InventoryException e) {
                        facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                                "ProductDelivery.warehouseVoucher.packError", articuloCombo.getVentaarticulo().getProductItem().getFullName());
                        errorMessages.addAll(e.getInventoryMessages());
                    }
                }

            }else {
                try {

                    approvalWarehouseVoucherService.validateOutputQuantity(new BigDecimal(articulo.getAmount()),
                            warehouse,
                            articulo.getProductItem(),
                            costCenter);
                } catch (InventoryException e) {
                    errorMessages.addAll(e.getInventoryMessages());
                }
            }
            }else{
                try {

                    approvalWarehouseVoucherService.validateOutputQuantity(new BigDecimal(articulo.getAmount()),
                            warehouse,
                            articulo.getProductItem(),
                            costCenter);
                } catch (InventoryException e) {
                    errorMessages.addAll(e.getInventoryMessages());
                }
            }
        }

        if (errorMessages.size() > 0) {
            addInventoryErrorMessages(errorMessages);
            return true;
        }

        if (customerOrder.getArticulosPedidos().size() == 0) {
            addSoldProductNotFoundMessages(customerOrder.getCodigo().toString());
            result = true;
        }

        if (null == documentType) {
            addWarehouseDocumentTypeErrorMessage(customerOrder.getCodigo().toString());
            result = true;
        }

        if (null == costCenter) {
            addcenterCostNotFoundErrorMessage();
            result = true;
        }

        return result;
    }

    public boolean verifyAmounts(VentaDirecta ventaDirecta) {
        Boolean result = false;
        if(ventaDirecta == null)
            return result;

        List<InventoryMessage> errorMessages = new ArrayList<InventoryMessage>();

        WarehouseDocumentType documentType = getFirstConsumptionType();

        Warehouse warehouse = inventoryService.findWarehouseByItemArticle(new ArrayList<ArticleOrder>(ventaDirecta.getArticulosPedidos()).get(0).getProductItem());
        CostCenter costCenter = findPublicCostCenter(warehouse);

        for(ArticleOrder articulo:ventaDirecta.getArticulosPedidos()) {
            if(articulo.getTipo().equals("COMBO")){
                List<ArticulosPromocion> articulosCombo = productItemService.findArticuloCombo(articulo);
                for(ArticulosPromocion articuloCombo :articulosCombo) {
                    try {

                        approvalWarehouseVoucherService.validateOutputQuantity(new BigDecimal(articuloCombo.getCantidad()*articulo.getAmount()),
                                warehouse,
                                articuloCombo.getVentaarticulo().getProductItem(),
                                costCenter);
                    } catch (InventoryException e) {
                        facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                                "ProductDelivery.warehouseVoucher.packError", articuloCombo.getVentaarticulo().getProductItem().getFullName());
                        errorMessages.addAll(e.getInventoryMessages());
                    }
                }

            }else {
                try {

                    approvalWarehouseVoucherService.validateOutputQuantity(new BigDecimal(articulo.getAmount()),
                            warehouse,
                            articulo.getProductItem(),
                            costCenter);
                } catch (InventoryException e) {
                    errorMessages.addAll(e.getInventoryMessages());
                }
            }
        }

        if (errorMessages.size() > 0) {
            addInventoryErrorMessages(errorMessages);
            return true;
        }

        if (ventaDirecta.getArticulosPedidos().size() == 0) {
            addSoldProductNotFoundMessages(ventaDirecta.getCodigo().toString());
            result = true;
        }

        if (null == documentType) {
            addWarehouseDocumentTypeErrorMessage(ventaDirecta.getCodigo().toString());
            result = true;
        }

        if (null == costCenter) {
            addcenterCostNotFoundErrorMessage();
            result = true;
        }

        return result;
    }

    @Override
    public boolean verifyAmounts(List<CustomerOrder> pedidos) {
        Boolean error = false;
        for(CustomerOrder pedido:pedidos)
        {
            if(verifyAmounts(pedido))
                error = true;
        }
        return error;
    }

    @SuppressWarnings(value = "unchecked")
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
            EntryDuplicatedException {


            String warehouseDescription = MessageUtils.getMessage("ProductDelivery.warehouseVoucher.description", customerOrder.getCodigo());
            List<ArticleOrder> articleOrders = new ArrayList<ArticleOrder>(customerOrder.getArticulosPedidos());

            if(articleOrders.size() != 0) {

                WarehouseDocumentType documentType = getFirstConsumptionType();
                //always exist almost one sold product that will be delivery
                ArticleOrder firstSoldProduct = articleOrders.get(0);
                Warehouse warehouse = inventoryService.findWarehouseByItemArticle(firstSoldProduct.getProductItem());
                CostCenter costCenter = findPublicCostCenter(warehouse);
                Employee responsible = getEntityManager().find(Employee.class, warehouse.getResponsibleId());

                WarehouseVoucher warehouseVoucher = createWarehouseVoucherAll(documentType, warehouse, responsible, costCenter, warehouseDescription, customerOrder);

                Map<MovementDetail, BigDecimal> movementDetailUnderMinimalStockMap = new HashMap<MovementDetail, BigDecimal>();
                Map<MovementDetail, BigDecimal> movementDetailOverMaximumStockMap = new HashMap<MovementDetail, BigDecimal>();
                List<MovementDetail> movementDetailWithoutWarnings = new ArrayList<MovementDetail>();

                try {

                    approvalWarehouseVoucherService.approveWarehouseVoucherFromDeliveryProduct(warehouseVoucher.getId(),
                            getGlossMessage(warehouseVoucher, warehouseDescription),
                            movementDetailUnderMinimalStockMap,
                            movementDetailOverMaximumStockMap,
                            movementDetailWithoutWarnings);

                } catch (WarehouseVoucherApprovedException e) {
                    log.debug("This exception never happen because I create a pending WarehouseVoucher.");
                } catch (WarehouseVoucherNotFoundException e) {
                    log.debug("This exception never happen because I create a new WarehouseVoucher.");
                } catch (WarehouseVoucherEmptyException e) {
                    log.debug("This exception never happen because I create a WarehouseVoucher with details inside.");
                } catch (WarehouseAccountCashNotFoundException e) {
                    e.printStackTrace();
                }

                /*ProductDelivery productDelivery = new ProductDelivery();
                productDelivery.setCompanyNumber(warehouse.getId().getCompanyNumber());
                productDelivery.setInvoiceNumber(firstSoldProduct.getInvoiceNumber());
                productDelivery.setWarehouseVoucher(warehouseVoucher);
                create(productDelivery);
*/
                //update state of order
                customerOrder.setEstado("ENTREGADO");
                updateCustomerOrder(customerOrder.getIdpedidos(),"ENTREGADO");
            }

    }

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
            EntryDuplicatedException {


        String warehouseDescription = MessageUtils.getMessage("ProductDelivery.warehouseVoucher.description", ventaDirecta.getCodigo());
        List<ArticleOrder> articleOrders = new ArrayList<ArticleOrder>(ventaDirecta.getArticulosPedidos());

        if(articleOrders.size() != 0) {

            WarehouseDocumentType documentType = getFirstConsumptionType();
            //always exist almost one sold product that will be delivery
            ArticleOrder firstSoldProduct = articleOrders.get(0);
            Warehouse warehouse = inventoryService.findWarehouseByItemArticle(firstSoldProduct.getProductItem());
            CostCenter costCenter = findPublicCostCenter(warehouse);
            Employee responsible = getEntityManager().find(Employee.class, warehouse.getResponsibleId());

            WarehouseVoucher warehouseVoucher = createWarehouseVoucherAll(documentType, warehouse, responsible, costCenter, warehouseDescription, ventaDirecta);

            Map<MovementDetail, BigDecimal> movementDetailUnderMinimalStockMap = new HashMap<MovementDetail, BigDecimal>();
            Map<MovementDetail, BigDecimal> movementDetailOverMaximumStockMap = new HashMap<MovementDetail, BigDecimal>();
            List<MovementDetail> movementDetailWithoutWarnings = new ArrayList<MovementDetail>();

            try {

                approvalWarehouseVoucherService.approveWarehouseVoucherFromDeliveryProduct(warehouseVoucher.getId(),
                        getGlossMessage(warehouseVoucher, warehouseDescription),
                        movementDetailUnderMinimalStockMap,
                        movementDetailOverMaximumStockMap,
                        movementDetailWithoutWarnings);

            } catch (WarehouseVoucherApprovedException e) {
                log.debug("This exception never happen because I create a pending WarehouseVoucher.");
            } catch (WarehouseVoucherNotFoundException e) {
                log.debug("This exception never happen because I create a new WarehouseVoucher.");
            } catch (WarehouseVoucherEmptyException e) {
                log.debug("This exception never happen because I create a WarehouseVoucher with details inside.");
            } catch (WarehouseAccountCashNotFoundException e) {
                e.printStackTrace();
            }

                /*ProductDelivery productDelivery = new ProductDelivery();
                productDelivery.setCompanyNumber(warehouse.getId().getCompanyNumber());
                productDelivery.setInvoiceNumber(firstSoldProduct.getInvoiceNumber());
                productDelivery.setWarehouseVoucher(warehouseVoucher);
                create(productDelivery);
*/
            //update state of order
            ventaDirecta.setEstado("ENTREGADO");
            updateVentaDirecta(ventaDirecta.getIdventadirecta(), "ENTREGADO");
        }

    }



    @SuppressWarnings(value = "unchecked")
    public ProductDelivery createAll(String invoiceNumber, String warehouseDescription)
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
            ProductItemNotFoundException, SoldProductNotFoundException, SoldProductJustNoProducer {

        //check if the sold products keep the pending state
        soldProductStateChecker(invoiceNumber, Constants.defaultCompanyNumber);
        List<SoldProduct> soldProducts = soldProductService.getSoldProducts(invoiceNumber, Constants.defaultCompanyNumber);
        //List<SoldProduct> soldProducts = soldProductService.getSoldProducts(invoiceNumber, Constants.defaultCompanyNumber);
        int pos = changeEdamToPressed(soldProducts);
        WarehouseDocumentType documentType = getFirstConsumptionType();

        if (soldProducts.size() == 0) {
            throw new SoldProductNotFoundException();
        }
        List<String> noProduced = findNoProduceres();
        soldProducts = removeNotProduced(soldProducts,noProduced);
        if(soldProducts.size() == 0)
        {
           throw  new SoldProductJustNoProducer();
        }

        if (null == documentType) {
            throw new WarehouseDocumentTypeNotFoundException("Warehouse document consumption type not found");
        }

        //always exist almost one sold product that will be delivery
        SoldProduct firstSoldProduct = soldProducts.get(0);

        Warehouse warehouse = firstSoldProduct.getWarehouse();
        CostCenter costCenter = findPublicCostCenter(warehouse);

        if (null == costCenter) {
            throw new PublicCostCenterNotFound("Cannot find a public Cost Center to complete the delivery.");
        }

        productItemStockChecker(invoiceNumber, warehouse, costCenter,noProduced);
        //productItemStockChecker(invoiceNumber, warehouse, costCenter);
        Employee responsible = getEntityManager().find(Employee.class, warehouse.getResponsibleId());

        WarehouseVoucher warehouseVoucher = createWarehouseVoucherAll(documentType, warehouse, responsible, costCenter, warehouseDescription, soldProducts);

        Map<MovementDetail, BigDecimal> movementDetailUnderMinimalStockMap = new HashMap<MovementDetail, BigDecimal>();
        Map<MovementDetail, BigDecimal> movementDetailOverMaximumStockMap = new HashMap<MovementDetail, BigDecimal>();
        List<MovementDetail> movementDetailWithoutWarnings = new ArrayList<MovementDetail>();

        /*for (SoldProduct soldProduct : soldProducts) {
            ProductItem productItem = getEntityManager().find(ProductItem.class, soldProduct.getProductItem().getId());
            movementDetailUnderMinimalStockMap.put(movementDetailTemp, productItem.getMinimalStock());
            movementDetailOverMaximumStockMap.put(movementDetailTemp, productItem.getMaximumStock());
            movementDetailWithoutWarnings.add(movementDetailTemp);
        }*/

        try {
            //approvalWarehouseVoucherService.approveWarehouseVoucher(warehouseVoucher.getId(), getGlossMessage(warehouseVoucher, warehouseDescription), null, null, null);
            /*approvalWarehouseVoucherService.approveWarehouseVoucher(warehouseVoucher.getId(),
                                                                    getGlossMessage(warehouseVoucher, warehouseDescription),
                                                                    movementDetailUnderMinimalStockMap,
                                                                    movementDetailOverMaximumStockMap,
                                                                    movementDetailWithoutWarnings);*/

            approvalWarehouseVoucherService.approveWarehouseVoucherFromDeliveryProduct(warehouseVoucher.getId(),
                    getGlossMessage(warehouseVoucher, warehouseDescription),
                    movementDetailUnderMinimalStockMap,
                    movementDetailOverMaximumStockMap,
                    movementDetailWithoutWarnings);

        } catch (WarehouseVoucherApprovedException e) {
            log.debug("This exception never happen because I create a pending WarehouseVoucher.");
        } catch (WarehouseVoucherNotFoundException e) {
            log.debug("This exception never happen because I create a new WarehouseVoucher.");
        } catch (WarehouseVoucherEmptyException e) {
            log.debug("This exception never happen because I create a WarehouseVoucher with details inside.");
        } catch (WarehouseAccountCashNotFoundException e) {
            e.printStackTrace();
        }

        ProductDelivery productDelivery = new ProductDelivery();
        productDelivery.setCompanyNumber(warehouse.getId().getCompanyNumber());
        productDelivery.setInvoiceNumber(firstSoldProduct.getInvoiceNumber());
        productDelivery.setWarehouseVoucher(warehouseVoucher);

        create(productDelivery);

        if(pos>-1)
        {
            resetChange(soldProducts.get(pos));
            approvalWarehouseVoucherService.resetChangeCheeseEdam(warehouseVoucher.getId(), pos);
            ProductItem cheesePressed = productItemService.findProductItemByCode(Constants.COD_CHEESE_PRESSED);
            ProductItem cheeseEDAM = productItemService.findProductItemByCode(Constants.COD_CHEESE_EDAM);
            cheeseEDAM.setUnitCost(cheesePressed.getUnitCost());
            try {
                productItemService.updateProductItem(cheeseEDAM);
            } catch (ProductItemMinimalStockIsGreaterThanMaximumStockException e) {
                e.printStackTrace();
            }
        }
        //para que cambio todos los estados iuncluido el queso recorte y edam
        soldProducts = soldProductService.getSoldProducts(invoiceNumber, Constants.defaultCompanyNumber);

        for (SoldProduct soldProduct : soldProducts) {
            soldProduct.setProductDelivery(productDelivery);
            soldProduct.setState(SoldProductState.DELIVERED);
            soldProduct.setNumberVoucher(warehouseVoucher.getNumber());
            update(soldProduct);
        }

        //update state of order
        if (firstSoldProduct.getOrderNumber() != null)
            updateOrderEstate(firstSoldProduct.getOrderNumber());

        return productDelivery;
    }

    public Integer getAmountSoldProductTotal(OrderItem item, Date date, Employee distribuidor) {
        if (item.getType() == "ESTADO")
            return 0;
        Integer val;
        if (distribuidor != null) {
            val = accountItemService.getAmountByDateAndDistributorOrderDelivery(item.getCodArt(), new BigDecimal(distribuidor.getId()), date);
            val += accountItemService.getAmountByDateAndDistributorInstitutionDelivery(item.getCodArt(), new BigDecimal(distribuidor.getId()), date);
            val += accountItemService.getAmountComboTotalAndDistributor(item.getCodArt(), new BigDecimal(distribuidor.getId()), date);
        } else {
            val = accountItemService.getAmountByDateAndDistributorOrderDelivery(item.getCodArt(), date);
            val += accountItemService.getAmountByDateAndDistributorInstitutionDelivery(item.getCodArt(), date);
            val += accountItemService.getAmountComboTotalDelivery(item.getCodArt(), date);
        }
        return val;
    }

    public Boolean verifyAmounts(List<String> numberInvoices, List<OrderItem> orderItems, Date date, Employee distribuidor) {
        Boolean result = false;
        List<InventoryMessage> errorMessages = new ArrayList<InventoryMessage>();
        BigDecimal totalCheeseEDAM = BigDecimal.ZERO;
        Boolean band = false;
        List<String> noProduceres = findNoProduceres();
        for (OrderItem item : orderItems) {
            if(!notProducer(item.getCodArt(),noProduceres)) {
                List<SoldProduct> soldProducts = soldProductService.getSoldProducts(numberInvoices.get(0), Constants.defaultCompanyNumber);
                SoldProduct firstSoldProduct = soldProducts.get(0);
                Warehouse warehouse = firstSoldProduct.getWarehouse();
                CostCenter costCenter = findPublicCostCenter(warehouse);
                if (item.getType() != "COMBO") {
                    if (item.getCodArt().equals(Constants.COD_CHEESE_EDAM)) {
                        item.setCodArt(Constants.COD_CHEESE_PRESSED);
                        band = true;
                    }

                    try {
                        Integer aux = getAmountSoldProductTotal(item, date, distribuidor);
                        if (aux > 0) {
                            if (item.getCodArt().equals(Constants.COD_CHEESE_PRESSED)) {
                                totalCheeseEDAM = totalCheeseEDAM.add(new BigDecimal(aux));
                                aux = totalCheeseEDAM.intValue();
                            }
                            approvalWarehouseVoucherService.validateOutputQuantity(new BigDecimal(aux),
                                    warehouse,
                                    productItemService.findProductItemByCode(item.getCodArt()),
                                    costCenter);
                        }
                    } catch (InventoryException e) {
                        errorMessages.addAll(e.getInventoryMessages());
                    }

                    if (band) {
                        item.setCodArt(Constants.COD_CHEESE_EDAM);
                        band = false;
                    }
                } else {
                    Integer aux = getAmountSoldProductTotal(item, date, distribuidor);
                    if (aux > 0) {
                        Map<String, Integer> productsPackage = soldProductService.getSoldProductsPackage(item.getCodArt(),aux);

                        for (Map.Entry<String, Integer> entry : productsPackage.entrySet()) {
                            try {

                                approvalWarehouseVoucherService.validateOutputQuantity(new BigDecimal(entry.getValue()),
                                        warehouse,
                                        productItemService.findProductItemByCode(entry.getKey()),
                                        costCenter);
                            } catch (InventoryException e) {
                                facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                                        "ProductDelivery.warehouseVoucher.packError", item.getNameItem());
                                errorMessages.addAll(e.getInventoryMessages());
                            }
                        }

                    }
                }
            }

        }
        if (errorMessages.size() > 0) {
            addInventoryErrorMessages(errorMessages);
            return true;
        }

        for (String invoiceNumber : numberInvoices) {
            try {
                soldProductStateChecker(invoiceNumber, Constants.defaultCompanyNumber);
            } catch (SoldProductDeliveredException e) {
                addSoldProductDeliveredErrorMessage(invoiceNumber);
                return true;
            }
            List<SoldProduct> soldProducts = soldProductService.getSoldProductsWithoutEDAM(invoiceNumber, Constants.defaultCompanyNumber);
            changeEdamToPressed(soldProducts);
            WarehouseDocumentType documentType = getFirstConsumptionType();
            SoldProduct firstSoldProduct = soldProducts.get(0);

            Warehouse warehouse = firstSoldProduct.getWarehouse();
            CostCenter costCenter = findPublicCostCenter(warehouse);

           /* try {
                productItemStockCheckerWithoutCutCheeseAndEDAM(invoiceNumber, warehouse, costCenter);
            } catch (InventoryException e) {
                addInventoryErrorMessages(e.getInventoryMessages());
                return true;
            }*/

            if (soldProducts.size() == 0) {
                addSoldProductNotFoundMessages(invoiceNumber);
                result = true;
            }

            soldProducts = removeNotProduced(soldProducts,noProduceres);

            if (soldProducts.size() == 0) {
                addSoldProductJustNoProducerMessages(invoiceNumber);
            }

            if (null == documentType) {
                addWarehouseDocumentTypeErrorMessage(invoiceNumber);
                result = true;
            }

            if (null == costCenter) {
                addcenterCostNotFoundErrorMessage();
                result = true;
            }
        }

        return result;
    }

    private boolean notProducer(String codArt,List<String> noProduceres) {
        for(String product:noProduceres)
        {
            if(product.equals(codArt))
            {
               return true;
            }
        }

        return false;
    }

    private void addSoldProductJustNoProducerMessages(String invoiceNumber) {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.WARN,
                "ProductDelivery.info.soldProductJustNoProducerMessages", invoiceNumber);
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

    private void addcenterCostNotFoundErrorMessage() {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                "ProductDelivery.warehouseVoucher.centerCostNofound");
    }

    private void addWarehouseDocumentTypeErrorMessage(String invoiceNumber) {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                "ProductDelivery.error.consumptionDocumentTypeNotFound", invoiceNumber);
    }

    private void addSoldProductNotFoundMessages(String invoiceNumber) {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.WARN,
                "ProductDelivery.info.soldProductNotFoundMessages", invoiceNumber);
    }

    private void addSoldProductDeliveredErrorMessage(String invoiceNumber) {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                "ProductDelivery.error.soldProductDelivered", invoiceNumber);
    }

    private void resetChange(SoldProduct soldProduct) {
        ProductItem item = productItemService.findProductItemByCode(Constants.COD_CHEESE_EDAM);
        soldProduct.setProductItemCode(item.getProductItemCode());
        soldProduct.setProductItem(item);
    }

    private int changeEdamToPressed(List<SoldProduct> soldProducts) {
        int pos = -1;
        int cont = 0;
        for (SoldProduct soldProduct : soldProducts) {
            if (soldProduct.getProductItemCode().compareTo(Constants.COD_CHEESE_EDAM) == 0) {
                ProductItem item = productItemService.findProductItemByCode(Constants.COD_CHEESE_PRESSED);
                soldProduct.setProductItemCode(item.getProductItemCode());
                soldProduct.setProductItem(item);
                pos = cont;
            }
            cont++;
        }
        return pos;
    }

    public List<SoldProduct> removeNotProduced(List<SoldProduct> soldProducts,List<String> noProducer)
    {
        List<SoldProduct> result = new ArrayList<SoldProduct>();
        result.addAll(soldProducts);
        for(SoldProduct soldProduct:soldProducts)
        {
            for(String codArt: noProducer) {
                if(soldProduct.getProductItemCode().equals(codArt))
                {
                    result.remove(soldProduct);
                }
            }
        }
        return result;
    }

    private List<String> findNoProduceres() {
        List<String> result = new ArrayList<String>();
        try{
            result = (List<String>) getEntityManager().createNativeQuery("select cod_art from estadoarticulo\n" +
                    "where estado =:state")
                    .setParameter("state",Constants.STATE_ITEM_PRODUCT_NOPRODUCER)
                    .getResultList();
        }catch(NoResultException e){
            return result;
        }
        return result;
    }

    @SuppressWarnings(value = "unchecked")
    public ProductDelivery create(String invoiceNumber, String warehouseDescription)
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
            ProductItemNotFoundException, SoldProductNotFoundException {

        //check if the sold products keep the pending state
        soldProductStateChecker(invoiceNumber, Constants.defaultCompanyNumber);
        //List<SoldProduct> soldProducts = soldProductService.getSoldProducts(invoiceNumber, Constants.defaultCompanyNumber);
        List<SoldProduct> soldProducts = soldProductService.getSoldProductsWithoutCutCheese(invoiceNumber, Constants.defaultCompanyNumber);
        WarehouseDocumentType documentType = getFirstConsumptionType();

        if(soldProducts.size() == 0)
        {
            throw new SoldProductNotFoundException();
        }

        if (null == documentType) {
            throw new WarehouseDocumentTypeNotFoundException("Warehouse document consumption type not found");
        }

        //always exist almost one sold product that will be delivery
        SoldProduct firstSoldProduct = soldProducts.get(0);

        Warehouse warehouse = firstSoldProduct.getWarehouse();
        CostCenter costCenter = findPublicCostCenter(warehouse);
        //update state of order
        if(firstSoldProduct.getOrderNumber() != null)
            updateOrderEstate(firstSoldProduct.getOrderNumber());

        if (null == costCenter) {
            throw new PublicCostCenterNotFound("Cannot find a public Cost Center to complete the delivery.");
        }

        productItemStockCheckerWithoutCutCheese(invoiceNumber, warehouse, costCenter);
        //productItemStockChecker(invoiceNumber, warehouse, costCenter);
        Employee responsible = getEntityManager().find(Employee.class, warehouse.getResponsibleId());

        //WarehouseVoucher warehouseVoucher = createWarehouseVoucher(documentType, warehouse, responsible, costCenter, warehouseDescription, soldProducts);
        WarehouseVoucher warehouseVoucher = createWarehouseVoucherAll(documentType, warehouse, responsible, costCenter, warehouseDescription, soldProducts);

        Map<MovementDetail, BigDecimal> movementDetailUnderMinimalStockMap = new HashMap<MovementDetail, BigDecimal>();
        Map<MovementDetail, BigDecimal> movementDetailOverMaximumStockMap = new HashMap<MovementDetail, BigDecimal>();
        List<MovementDetail> movementDetailWithoutWarnings = new ArrayList<MovementDetail>();

        /*for (SoldProduct soldProduct : soldProducts) {
            ProductItem productItem = getEntityManager().find(ProductItem.class, soldProduct.getProductItem().getId());
            movementDetailUnderMinimalStockMap.put(movementDetailTemp, productItem.getMinimalStock());
            movementDetailOverMaximumStockMap.put(movementDetailTemp, productItem.getMaximumStock());
            movementDetailWithoutWarnings.add(movementDetailTemp);
        }*/

        try {
            //approvalWarehouseVoucherService.approveWarehouseVoucher(warehouseVoucher.getId(), getGlossMessage(warehouseVoucher, warehouseDescription), null, null, null);
            /*approvalWarehouseVoucherService.approveWarehouseVoucher(warehouseVoucher.getId(),
                                                                    getGlossMessage(warehouseVoucher, warehouseDescription),
                                                                    movementDetailUnderMinimalStockMap,
                                                                    movementDetailOverMaximumStockMap,
                                                                    movementDetailWithoutWarnings);*/

            approvalWarehouseVoucherService.approveWarehouseVoucherFromDeliveryProduct(warehouseVoucher.getId(),
                    getGlossMessage(warehouseVoucher, warehouseDescription),
                    movementDetailUnderMinimalStockMap,
                    movementDetailOverMaximumStockMap,
                    movementDetailWithoutWarnings);

        } catch (WarehouseVoucherApprovedException e) {
            log.debug("This exception never happen because I create a pending WarehouseVoucher.");
        } catch (WarehouseVoucherNotFoundException e) {
            log.debug("This exception never happen because I create a new WarehouseVoucher.");
        } catch (WarehouseVoucherEmptyException e) {
            log.debug("This exception never happen because I create a WarehouseVoucher with details inside.");
        } catch (WarehouseAccountCashNotFoundException e) {
            e.printStackTrace();
        }
        ProductDelivery productDelivery = new ProductDelivery();
        productDelivery.setCompanyNumber(warehouse.getId().getCompanyNumber());
        productDelivery.setInvoiceNumber(firstSoldProduct.getInvoiceNumber());
        productDelivery.setWarehouseVoucher(warehouseVoucher);

        create(productDelivery);

        //para que cambio todos los estados iuncluido el queso recorte y edam
        soldProducts = soldProductService.getSoldProducts(invoiceNumber, Constants.defaultCompanyNumber);

        for (SoldProduct soldProduct : soldProducts) {
            soldProduct.setProductDelivery(productDelivery);
            soldProduct.setState(SoldProductState.DELIVERED);
            update(soldProduct);
        }

        return productDelivery;
    }

    private WarehouseVoucher createWarehouseVoucher(WarehouseDocumentType warehouseDocumentType,
                                                    Warehouse warehouse,
                                                    Employee responsible,
                                                    CostCenter publicCostCenter,
                                                    String warehouseVoucherDescription,
                                                    List<SoldProduct> soldProducts)
            throws InventoryException, ProductItemNotFoundException {
        //Create the WarehouseVoucher
        WarehouseVoucher warehouseVoucher = new WarehouseVoucher();
        warehouseVoucher.setDocumentType(warehouseDocumentType);
        warehouseVoucher.setWarehouse(warehouse);
        warehouseVoucher.setDate(soldProductService.getDateFromSoldProductOrder(soldProducts.get(0)));
        //todo: cambiar el estado del vale
        warehouseVoucher.setState(WarehouseVoucherState.PEN);

        warehouseVoucher.setExecutorUnit(warehouse.getExecutorUnit());
        warehouseVoucher.setCostCenterCode(publicCostCenter.getId().getCode());
        warehouseVoucher.setResponsible(responsible);

        InventoryMovement inventoryMovement = new InventoryMovement();
        inventoryMovement.setDescription(warehouseVoucherDescription);

        warehouseService.createWarehouseVoucher(warehouseVoucher, inventoryMovement, null, null, null, null);

        //Create the MovementDetails
        for (SoldProduct soldProduct : soldProducts) {
            ProductItem productItem = getEntityManager()
                    .find(ProductItem.class, soldProduct.getProductItem().getId());

            MovementDetail movementDetailTemp = new MovementDetail();
            movementDetailTemp.setWarehouse(warehouse); //revisar
            movementDetailTemp.setProductItem(productItem);
            movementDetailTemp.setProductItemAccount(productItem.getProductItemAccount());
            movementDetailTemp.setQuantity(soldProduct.getQuantity());
            movementDetailTemp.setUnitCost(productItem.getUnitCost());
            movementDetailTemp.setAmount(null);
            movementDetailTemp.setExecutorUnit(warehouse.getExecutorUnit());
            movementDetailTemp.setCostCenterCode(publicCostCenter.getId().getCode());
            movementDetailTemp.setMeasureUnit(productItem.getUsageMeasureUnit());

            /* revisar */
            Map<MovementDetail, BigDecimal> movementDetailUnderMinimalStockMap = new HashMap<MovementDetail, BigDecimal>();
            movementDetailUnderMinimalStockMap.put(movementDetailTemp, productItem.getMinimalStock());

            Map<MovementDetail, BigDecimal> movementDetailOverMaximumStockMap = new HashMap<MovementDetail, BigDecimal>();
            movementDetailOverMaximumStockMap.put(movementDetailTemp, productItem.getMaximumStock());

            List<MovementDetail> movementDetailWithoutWarnings = new ArrayList<MovementDetail>();
            movementDetailWithoutWarnings.add(movementDetailTemp);
            /* revisar */

            try {
                //warehouseService.createMovementDetail(warehouseVoucher, movementDetailTemp, null, null, null); // revisar
                warehouseService.createMovementDetail(warehouseVoucher, movementDetailTemp, movementDetailUnderMinimalStockMap, movementDetailOverMaximumStockMap, movementDetailWithoutWarnings);
            } catch (WarehouseVoucherApprovedException e) {
                log.debug("This exception never happen because I just created a new WarehouseVoucher" +
                        " and his state is pending");
            } catch (WarehouseVoucherNotFoundException e) {
                log.debug("This exception never happen because I just created a new WarehouseVoucher");
            }
        }

        return warehouseVoucher;
    }

    private WarehouseVoucher createWarehouseVoucherAll(WarehouseDocumentType warehouseDocumentType,
                                                       Warehouse warehouse,
                                                       Employee responsible,
                                                       CostCenter publicCostCenter,
                                                       String warehouseVoucherDescription,
                                                       VentaDirecta ventaDirecta)
            throws InventoryException, ProductItemNotFoundException {
        //Create the WarehouseVoucher
        WarehouseVoucher warehouseVoucher = new WarehouseVoucher();
        warehouseVoucher.setDocumentType(warehouseDocumentType);
        warehouseVoucher.setWarehouse(warehouse);
        warehouseVoucher.setDate(ventaDirecta.getFechaPedido());
        //todo: cambiar el estado del vale
        warehouseVoucher.setState(WarehouseVoucherState.PEN);

        warehouseVoucher.setExecutorUnit(warehouse.getExecutorUnit());
        warehouseVoucher.setCostCenterCode(publicCostCenter.getId().getCode());
        warehouseVoucher.setResponsible(responsible);

        InventoryMovement inventoryMovement = new InventoryMovement();
        inventoryMovement.setDescription(warehouseVoucherDescription);

        warehouseService.createWarehouseVoucher(warehouseVoucher, inventoryMovement, null, null, null, null);

        //Create the MovementDetails
        for (ArticleOrder articleOrder: ventaDirecta.getArticulosPedidos()) {
            if(articleOrder.getTipo().equals("COMBO"))
            {
                List<ArticulosPromocion> articulosCombo = productItemService.findArticuloCombo(articleOrder);
                for(ArticulosPromocion articuloCombo :articulosCombo) {
                    MovementDetail movementDetailTemp = new MovementDetail();
                    movementDetailTemp.setWarehouse(warehouse); //revisar
                    movementDetailTemp.setProductItem(articuloCombo.getVentaarticulo().getProductItem());
                    movementDetailTemp.setProductItemAccount(articuloCombo.getVentaarticulo().getProductItem().getProductItemAccount());
                    movementDetailTemp.setQuantity(new BigDecimal(articuloCombo.getCantidad() * articleOrder.getAmount()));
                    movementDetailTemp.setUnitCost(articuloCombo.getVentaarticulo().getProductItem().getUnitCost());
                    movementDetailTemp.setAmount(null);
                    movementDetailTemp.setExecutorUnit(warehouse.getExecutorUnit());
                    movementDetailTemp.setCostCenterCode(publicCostCenter.getId().getCode());
                    movementDetailTemp.setMeasureUnit(articuloCombo.getVentaarticulo().getProductItem().getUsageMeasureUnit());

            /* revisar */
                    Map<MovementDetail, BigDecimal> movementDetailUnderMinimalStockMap = new HashMap<MovementDetail, BigDecimal>();
                    movementDetailUnderMinimalStockMap.put(movementDetailTemp, articuloCombo.getVentaarticulo().getProductItem().getMinimalStock());

                    Map<MovementDetail, BigDecimal> movementDetailOverMaximumStockMap = new HashMap<MovementDetail, BigDecimal>();
                    movementDetailOverMaximumStockMap.put(movementDetailTemp, articuloCombo.getVentaarticulo().getProductItem().getMaximumStock());

                    List<MovementDetail> movementDetailWithoutWarnings = new ArrayList<MovementDetail>();
                    movementDetailWithoutWarnings.add(movementDetailTemp);
            /* revisar */

                    try {
                        //warehouseService.createMovementDetail(warehouseVoucher, movementDetailTemp, null, null, null); // revisar
                        warehouseService.createMovementDetail(warehouseVoucher, movementDetailTemp, movementDetailUnderMinimalStockMap, movementDetailOverMaximumStockMap, movementDetailWithoutWarnings);
                    } catch (WarehouseVoucherApprovedException e) {
                        log.debug("This exception never happen because I just created a new WarehouseVoucher" +
                                " and his state is pending");
                    } catch (WarehouseVoucherNotFoundException e) {
                        log.debug("This exception never happen because I just created a new WarehouseVoucher");
                    }
                }
            }else{

                MovementDetail movementDetailTemp = new MovementDetail();
                movementDetailTemp.setWarehouse(warehouse); //revisar
                movementDetailTemp.setProductItem(articleOrder.getProductItem());
                movementDetailTemp.setProductItemAccount(articleOrder.getProductItem().getProductItemAccount());
                movementDetailTemp.setQuantity(new BigDecimal(articleOrder.getAmount()));
                movementDetailTemp.setUnitCost(articleOrder.getProductItem().getUnitCost());
                movementDetailTemp.setAmount(null);
                movementDetailTemp.setExecutorUnit(warehouse.getExecutorUnit());
                movementDetailTemp.setCostCenterCode(publicCostCenter.getId().getCode());
                movementDetailTemp.setMeasureUnit(articleOrder.getProductItem().getUsageMeasureUnit());

            /* revisar */
                Map<MovementDetail, BigDecimal> movementDetailUnderMinimalStockMap = new HashMap<MovementDetail, BigDecimal>();
                movementDetailUnderMinimalStockMap.put(movementDetailTemp, articleOrder.getProductItem().getMinimalStock());

                Map<MovementDetail, BigDecimal> movementDetailOverMaximumStockMap = new HashMap<MovementDetail, BigDecimal>();
                movementDetailOverMaximumStockMap.put(movementDetailTemp, articleOrder.getProductItem().getMaximumStock());

                List<MovementDetail> movementDetailWithoutWarnings = new ArrayList<MovementDetail>();
                movementDetailWithoutWarnings.add(movementDetailTemp);
            /* revisar */

                try {
                    //warehouseService.createMovementDetail(warehouseVoucher, movementDetailTemp, null, null, null); // revisar
                    warehouseService.createMovementDetail(warehouseVoucher, movementDetailTemp, movementDetailUnderMinimalStockMap, movementDetailOverMaximumStockMap, movementDetailWithoutWarnings);
                } catch (WarehouseVoucherApprovedException e) {
                    log.debug("This exception never happen because I just created a new WarehouseVoucher" +
                            " and his state is pending");
                } catch (WarehouseVoucherNotFoundException e) {
                    log.debug("This exception never happen because I just created a new WarehouseVoucher");
                }
            }
        }

        return warehouseVoucher;
    }

    private WarehouseVoucher createWarehouseVoucherAll(WarehouseDocumentType warehouseDocumentType,
                                                       Warehouse warehouse,
                                                       Employee responsible,
                                                       CostCenter publicCostCenter,
                                                       String warehouseVoucherDescription,
                                                       CustomerOrder customerOrder)
            throws InventoryException, ProductItemNotFoundException {
        //Create the WarehouseVoucher
        WarehouseVoucher warehouseVoucher = new WarehouseVoucher();
        warehouseVoucher.setDocumentType(warehouseDocumentType);
        warehouseVoucher.setWarehouse(warehouse);
        warehouseVoucher.setDate(customerOrder.getFechaEntrega());
        //todo: cambiar el estado del vale
        warehouseVoucher.setState(WarehouseVoucherState.PEN);

        warehouseVoucher.setExecutorUnit(warehouse.getExecutorUnit());
        warehouseVoucher.setCostCenterCode(publicCostCenter.getId().getCode());
        warehouseVoucher.setResponsible(responsible);

        InventoryMovement inventoryMovement = new InventoryMovement();
        inventoryMovement.setDescription(warehouseVoucherDescription);

        warehouseService.createWarehouseVoucher(warehouseVoucher, inventoryMovement, null, null, null, null);

        //Create the MovementDetails
        for (ArticleOrder articleOrder: customerOrder.getArticulosPedidos()) {
            if(!StringUtils.isEmpty(articleOrder.getTipo())) {
                if (articleOrder.getTipo().equals("COMBO")) {
                    List<ArticulosPromocion> articulosCombo = productItemService.findArticuloCombo(articleOrder);
                    for (ArticulosPromocion articuloCombo : articulosCombo) {
                        MovementDetail movementDetailTemp = new MovementDetail();
                        movementDetailTemp.setWarehouse(warehouse); //revisar
                        movementDetailTemp.setProductItem(articuloCombo.getVentaarticulo().getProductItem());
                        movementDetailTemp.setProductItemAccount(articuloCombo.getVentaarticulo().getProductItem().getProductItemAccount());
                        movementDetailTemp.setQuantity(new BigDecimal(articuloCombo.getCantidad() * articleOrder.getAmount()));
                        movementDetailTemp.setUnitCost(articuloCombo.getVentaarticulo().getProductItem().getUnitCost());
                        movementDetailTemp.setAmount(null);
                        movementDetailTemp.setExecutorUnit(warehouse.getExecutorUnit());
                        movementDetailTemp.setCostCenterCode(publicCostCenter.getId().getCode());
                        movementDetailTemp.setMeasureUnit(articuloCombo.getVentaarticulo().getProductItem().getUsageMeasureUnit());

            /* revisar */
                        Map<MovementDetail, BigDecimal> movementDetailUnderMinimalStockMap = new HashMap<MovementDetail, BigDecimal>();
                        movementDetailUnderMinimalStockMap.put(movementDetailTemp, articuloCombo.getVentaarticulo().getProductItem().getMinimalStock());

                        Map<MovementDetail, BigDecimal> movementDetailOverMaximumStockMap = new HashMap<MovementDetail, BigDecimal>();
                        movementDetailOverMaximumStockMap.put(movementDetailTemp, articuloCombo.getVentaarticulo().getProductItem().getMaximumStock());

                        List<MovementDetail> movementDetailWithoutWarnings = new ArrayList<MovementDetail>();
                        movementDetailWithoutWarnings.add(movementDetailTemp);
            /* revisar */

                        try {
                            //warehouseService.createMovementDetail(warehouseVoucher, movementDetailTemp, null, null, null); // revisar
                            warehouseService.createMovementDetail(warehouseVoucher, movementDetailTemp, movementDetailUnderMinimalStockMap, movementDetailOverMaximumStockMap, movementDetailWithoutWarnings);
                        } catch (WarehouseVoucherApprovedException e) {
                            log.debug("This exception never happen because I just created a new WarehouseVoucher" +
                                    " and his state is pending");
                        } catch (WarehouseVoucherNotFoundException e) {
                            log.debug("This exception never happen because I just created a new WarehouseVoucher");
                        }
                    }
                } else {
                    crearVale(warehouse, articleOrder, publicCostCenter, warehouseVoucher);
                }
            }else {
                crearVale(warehouse, articleOrder, publicCostCenter, warehouseVoucher);
            }
        }

        return warehouseVoucher;
    }

    private void crearVale(Warehouse warehouse,ArticleOrder articleOrder,CostCenter publicCostCenter,WarehouseVoucher warehouseVoucher) throws InventoryException, ProductItemNotFoundException {
        MovementDetail movementDetailTemp = new MovementDetail();
        movementDetailTemp.setWarehouse(warehouse); //revisar
        movementDetailTemp.setProductItem(articleOrder.getProductItem());
        movementDetailTemp.setProductItemAccount(articleOrder.getProductItem().getProductItemAccount());
        movementDetailTemp.setQuantity(new BigDecimal(articleOrder.getAmount()));
        movementDetailTemp.setUnitCost(articleOrder.getProductItem().getUnitCost());
        movementDetailTemp.setAmount(null);
        movementDetailTemp.setExecutorUnit(warehouse.getExecutorUnit());
        movementDetailTemp.setCostCenterCode(publicCostCenter.getId().getCode());
        movementDetailTemp.setMeasureUnit(articleOrder.getProductItem().getUsageMeasureUnit());

            /* revisar */
        Map<MovementDetail, BigDecimal> movementDetailUnderMinimalStockMap = new HashMap<MovementDetail, BigDecimal>();
        movementDetailUnderMinimalStockMap.put(movementDetailTemp, articleOrder.getProductItem().getMinimalStock());

        Map<MovementDetail, BigDecimal> movementDetailOverMaximumStockMap = new HashMap<MovementDetail, BigDecimal>();
        movementDetailOverMaximumStockMap.put(movementDetailTemp, articleOrder.getProductItem().getMaximumStock());

        List<MovementDetail> movementDetailWithoutWarnings = new ArrayList<MovementDetail>();
        movementDetailWithoutWarnings.add(movementDetailTemp);
            /* revisar */

        try {
            //warehouseService.createMovementDetail(warehouseVoucher, movementDetailTemp, null, null, null); // revisar
            warehouseService.createMovementDetail(warehouseVoucher, movementDetailTemp, movementDetailUnderMinimalStockMap, movementDetailOverMaximumStockMap, movementDetailWithoutWarnings);
        } catch (WarehouseVoucherApprovedException e) {
            log.debug("This exception never happen because I just created a new WarehouseVoucher" +
                    " and his state is pending");
        } catch (WarehouseVoucherNotFoundException e) {
            log.debug("This exception never happen because I just created a new WarehouseVoucher");
        }
    }

    private WarehouseVoucher createWarehouseVoucherAll(WarehouseDocumentType warehouseDocumentType,
                                                       Warehouse warehouse,
                                                       Employee responsible,
                                                       CostCenter publicCostCenter,
                                                       String warehouseVoucherDescription,
                                                       List<SoldProduct> soldProducts)
            throws InventoryException, ProductItemNotFoundException {
        //Create the WarehouseVoucher
        WarehouseVoucher warehouseVoucher = new WarehouseVoucher();
        warehouseVoucher.setDocumentType(warehouseDocumentType);
        warehouseVoucher.setWarehouse(warehouse);
        warehouseVoucher.setDate(soldProductService.getDateFromSoldProductOrder(soldProducts.get(0)));
        //todo: cambiar el estado del vale
        warehouseVoucher.setState(WarehouseVoucherState.PEN);

        warehouseVoucher.setExecutorUnit(warehouse.getExecutorUnit());
        warehouseVoucher.setCostCenterCode(publicCostCenter.getId().getCode());
        warehouseVoucher.setResponsible(responsible);

        InventoryMovement inventoryMovement = new InventoryMovement();
        inventoryMovement.setDescription(warehouseVoucherDescription);

        warehouseService.createWarehouseVoucher(warehouseVoucher, inventoryMovement, null, null, null, null);

        //Create the MovementDetails
        for (SoldProduct soldProduct : soldProducts) {
            ProductItem productItem = getEntityManager()
                    .find(ProductItem.class, soldProduct.getProductItem().getId());

            MovementDetail movementDetailTemp = new MovementDetail();
            movementDetailTemp.setWarehouse(warehouse); //revisar
            movementDetailTemp.setProductItem(productItem);
            movementDetailTemp.setProductItemAccount(productItem.getProductItemAccount());
            movementDetailTemp.setQuantity(soldProduct.getQuantity());
            movementDetailTemp.setUnitCost(productItem.getUnitCost());
            movementDetailTemp.setAmount(null);
            movementDetailTemp.setExecutorUnit(warehouse.getExecutorUnit());
            movementDetailTemp.setCostCenterCode(publicCostCenter.getId().getCode());
            movementDetailTemp.setMeasureUnit(productItem.getUsageMeasureUnit());

            /* revisar */
            Map<MovementDetail, BigDecimal> movementDetailUnderMinimalStockMap = new HashMap<MovementDetail, BigDecimal>();
            movementDetailUnderMinimalStockMap.put(movementDetailTemp, productItem.getMinimalStock());

            Map<MovementDetail, BigDecimal> movementDetailOverMaximumStockMap = new HashMap<MovementDetail, BigDecimal>();
            movementDetailOverMaximumStockMap.put(movementDetailTemp, productItem.getMaximumStock());

            List<MovementDetail> movementDetailWithoutWarnings = new ArrayList<MovementDetail>();
            movementDetailWithoutWarnings.add(movementDetailTemp);
            /* revisar */

            try {
                //warehouseService.createMovementDetail(warehouseVoucher, movementDetailTemp, null, null, null); // revisar
                warehouseService.createMovementDetail(warehouseVoucher, movementDetailTemp, movementDetailUnderMinimalStockMap, movementDetailOverMaximumStockMap, movementDetailWithoutWarnings);
            } catch (WarehouseVoucherApprovedException e) {
                log.debug("This exception never happen because I just created a new WarehouseVoucher" +
                        " and his state is pending");
            } catch (WarehouseVoucherNotFoundException e) {
                log.debug("This exception never happen because I just created a new WarehouseVoucher");
            }
        }

        return warehouseVoucher;
    }


    @SuppressWarnings(value = "unchecked")
    private void soldProductStateChecker(String invoiceNumber, String companyNumber) throws SoldProductDeliveredException {
        List<SoldProduct> soldProducts = listEm.createNamedQuery("SoldProduct.findByState")
                .setParameter("invoiceNumber", invoiceNumber)
                .setParameter("companyNumber", companyNumber)
                .setParameter("state", SoldProductState.DELIVERED).getResultList();
        if (null != soldProducts && !soldProducts.isEmpty()) {
            throw new SoldProductDeliveredException("The soldProduct was delivered by other user");
        }
    }

    @SuppressWarnings(value = "unchecked")
    private void productItemStockChecker(String invoiceNumber,
                                         Warehouse warehouse,
                                         CostCenter costCenter,
                                         List<String> listProductNoProduced) throws InventoryException {
        List<ProductItem> productItems = getEntityManager()
                .createNamedQuery("SoldProduct.findByProductItem")
                .setParameter("invoiceNumber", invoiceNumber)
                .setParameter("companyNumber", warehouse.getId().getCompanyNumber())
                .getResultList();
        productItems = removeItemsNoproducer(listProductNoProduced,productItems);
        List<InventoryMessage> errorMessages = new ArrayList<InventoryMessage>();
        for (ProductItem productItem : productItems) {
            BigDecimal total = (BigDecimal) getEntityManager()
                    .createNamedQuery("SoldProduct.sunQuantitiesByProductItem")
                    .setParameter("invoiceNumber", invoiceNumber)
                    .setParameter("productItem", productItem)
                    .setParameter("companyNumber", warehouse.getId().getCompanyNumber()).getSingleResult();

            try {
                approvalWarehouseVoucherService.validateOutputQuantity(total,
                        warehouse,
                        productItem,
                        costCenter);
            } catch (InventoryException e) {
                errorMessages.addAll(e.getInventoryMessages());
            }
        }

        if (!errorMessages.isEmpty()) {
            throw new InventoryException(errorMessages);
        }
    }

    public List<ProductItem> removeItemsNoproducer(List<String> listProductNoProduced,List<ProductItem> productItems)
    {
        List<ProductItem> result = new ArrayList<ProductItem>();
                          result.addAll(productItems);
        for(ProductItem item:productItems)
        {
            for(String codArt: listProductNoProduced)
            {
                if(codArt.equals(item.getProductItemCode()))
                {
                    result.remove(item);
                }
            }
        }
        return result;
    }

    @SuppressWarnings(value = "unchecked")
    private void productItemStockCheckerWithoutCutCheese(String invoiceNumber,
                                                         Warehouse warehouse,
                                                         CostCenter costCenter) throws InventoryException {
        List<ProductItem> productItems = getEntityManager()
                .createNamedQuery("SoldProduct.findByProductItemWithouCutCheese")
                .setParameter("invoiceNumber", invoiceNumber)
                .setParameter("companyNumber", warehouse.getId().getCompanyNumber())
                .setParameter("codCutCheese",Constants.COD_CUT_CHEESE)
                .getResultList();

        List<InventoryMessage> errorMessages = new ArrayList<InventoryMessage>();
        for (ProductItem productItem : productItems) {
            BigDecimal total = (BigDecimal) getEntityManager()
                    .createNamedQuery("SoldProduct.sunQuantitiesByProductItem")
                    .setParameter("invoiceNumber", invoiceNumber)
                    .setParameter("productItem", productItem)
                    .setParameter("companyNumber", warehouse.getId().getCompanyNumber()).getSingleResult();

            try {
                approvalWarehouseVoucherService.validateOutputQuantity(total,
                        warehouse,
                        productItem,
                        costCenter);
            } catch (InventoryException e) {
                errorMessages.addAll(e.getInventoryMessages());
            }
        }

        if (!errorMessages.isEmpty()) {
            throw new InventoryException(errorMessages);
        }
    }

    @SuppressWarnings(value = "unchecked")
    private void productItemStockCheckerWithoutCutCheeseAndEDAM(String invoiceNumber,
                                                                Warehouse warehouse,
                                                                CostCenter costCenter) throws InventoryException {
        List<ProductItem> productItems = getEntityManager()
                .createNamedQuery("SoldProduct.findByProductItemWithouCutCheeseAndEDAM")
                .setParameter("invoiceNumber", invoiceNumber)
                .setParameter("companyNumber", warehouse.getId().getCompanyNumber())
                .setParameter("codCutCheese",Constants.COD_CUT_CHEESE)
                .setParameter("codEDAMCheese",Constants.COD_CHEESE_EDAM)
                .getResultList();

        List<InventoryMessage> errorMessages = new ArrayList<InventoryMessage>();
        for (ProductItem productItem : productItems) {
            BigDecimal total = (BigDecimal) getEntityManager()
                    .createNamedQuery("SoldProduct.sunQuantitiesByProductItem")
                    .setParameter("invoiceNumber", invoiceNumber)
                    .setParameter("productItem", productItem)
                    .setParameter("companyNumber", warehouse.getId().getCompanyNumber()).getSingleResult();

            try {
                approvalWarehouseVoucherService.validateOutputQuantity(total,
                        warehouse,
                        productItem,
                        costCenter);
            } catch (InventoryException e) {
                errorMessages.addAll(e.getInventoryMessages());
            }
        }

        if (!errorMessages.isEmpty()) {
            throw new InventoryException(errorMessages);
        }
    }

    @SuppressWarnings(value = "unchecked")
    private WarehouseDocumentType getFirstConsumptionType() {
        List<WarehouseDocumentType> warehouseDocumentTypeList = getEntityManager()
                .createNamedQuery("WarehouseDocumentType.findByType")
                .setParameter("companyNumber", Constants.defaultCompanyNumber)
                .setParameter("warehouseVoucherType", WarehouseVoucherType.C).getResultList();

        if (!ValidatorUtil.isEmptyOrNull(warehouseDocumentTypeList)) {
            return warehouseDocumentTypeList.get(0);
        }

        return null;
    }

    @SuppressWarnings(value = "unchecked")
    private CostCenter findPublicCostCenter(Warehouse warehouse) {
        List<InventoryDetail> inventoryDetails = getEntityManager()
                .createNamedQuery("InventoryDetail.findByWarehouseCode")
                .setParameter("warehouseCode", warehouse.getId().getWarehouseCode())
                .setParameter("executorUnit", warehouse.getExecutorUnit())
                .setParameter("companyNumber", warehouse.getId().getCompanyNumber()).getResultList();

        for (InventoryDetail inventoryDetail : inventoryDetails) {
            if (isPublicCostCenter(inventoryDetail.getCostCenterCode(), inventoryDetail.getCompanyNumber())) {
                return getEntityManager().find(CostCenter.class, inventoryDetail.getCostCenter().getId());
            }
        }

        return null;
    }

    private boolean isPublicCostCenter(String costCenterCode, String companyNumber) {
        CostCenterPk costCenterPk = new CostCenterPk(companyNumber, costCenterCode);
        CostCenter costCenter = getEntityManager().find(CostCenter.class, costCenterPk);
        return !costCenter.getExclusiveConsumption();
    }

    private String[] getGlossMessage(WarehouseVoucher warehouseVoucher, String movementDescription) {
        String gloss[] = new String[2];
        String dateString = DateUtils.format(warehouseVoucher.getDate(), MessageUtils.getMessage("patterns.dateTime"));
        String productCodes = QueryUtils.toQueryParameter(movementDetailService.findDetailProductCodeByVoucher(warehouseVoucher));
        String voucherTypeName = MessageUtils.getMessage(warehouseVoucher.getDocumentType().getWarehouseVoucherType().getResourceKey());
        String documentName = warehouseVoucher.getDocumentType().getName();
        String sourceWarehouseName = warehouseVoucher.getWarehouse().getName();
        gloss[0] = MessageUtils.getMessage("WarehouseVoucher.message.gloss", voucherTypeName, documentName, sourceWarehouseName, productCodes, dateString, Constants.WAREHOUSEVOUCHER_NUMBER_PARAM, movementDescription);
        System.out.println(warehouseVoucher.getNumber() + "----->>>>>>>>>>> gloss: " + gloss[0]);
        return gloss;

    }
}