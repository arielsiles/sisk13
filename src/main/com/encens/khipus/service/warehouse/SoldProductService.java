package com.encens.khipus.service.warehouse;

import com.encens.khipus.framework.service.GenericService;
import com.encens.khipus.model.customers.CustomerOrder;
import com.encens.khipus.model.customers.Territoriotrabajo;
import com.encens.khipus.model.customers.VentaDirecta;
import com.encens.khipus.model.warehouse.SoldProduct;

import javax.ejb.Local;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author
 * @version 2.4
 */
@Local
public interface SoldProductService extends GenericService {
    List<SoldProduct> getSoldProducts(String invoiceNumber, String companyNumber);

    public List<SoldProduct> getSoldProductsWithoutCutCheese(String invoiceNumber, String companyNumber);

    List<SoldProduct> getSoldProductsCashSale(String invoiceNumber, String companyNumber);

    List<SoldProduct> getSoldProductsCashOrder(String invoiceNumber, String companyNumber);

    List<String> getSoldProductsCashOrder(Date date,BigDecimal distribuidor);
    public List<String> getSoldProductsCashOrder(Date date);

    public List<SoldProduct> getFindDelivery(String invoiceNumber, String companyNumber);

    Date getDateFromSoldProductOrder(SoldProduct soldProduct);

    List<SoldProduct> getSoldProductsWithoutCutCheeseAndEDAM(String invoiceNumber, String defaultCompanyNumber);

    public List<SoldProduct> getSoldProductsWithoutEDAM(String invoiceNumber, String companyNumber);

    Map<String,Integer> getSoldProductsPackage(String codArt, Integer amountSoldProductTotal);

    public CustomerOrder findPedidoPorCodigo(String numeroPedido);

    List<CustomerOrder> findPedidosPorFechaTerritorio(Date date, Territoriotrabajo territoriotrabajo);

    VentaDirecta findVentaPorCodigo(String orderNumber);
}
