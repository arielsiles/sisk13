package com.encens.khipus.service.warehouse;

import com.encens.khipus.framework.service.GenericServiceBean;
import com.encens.khipus.model.customers.CustomerOrder;
import com.encens.khipus.model.customers.Territoriotrabajo;
import com.encens.khipus.model.customers.VentaDirecta;
import com.encens.khipus.model.warehouse.SoldProduct;
import com.encens.khipus.util.Constants;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.Name;

import javax.ejb.Stateless;
import javax.persistence.NoResultException;
import javax.persistence.TemporalType;
import java.math.BigDecimal;
import java.util.*;

/**
 * @author
 * @version 2.4
 */
@Stateless
@Name("soldProductService")
@AutoCreate
public class SoldProductServiceBean extends GenericServiceBean implements SoldProductService {

    @SuppressWarnings(value = "unchecked")
    public List<SoldProduct> getSoldProducts(String invoiceNumber, String companyNumber) {
        List<SoldProduct> result = getEntityManager()
                .createNamedQuery("SoldProduct.findByInvoiceNumber")
                .setParameter("invoiceNumber", invoiceNumber)
                .setParameter("companyNumber", companyNumber)
                .getResultList();
        if (null == result) {
            result = new ArrayList<SoldProduct>();
        }

        return result;
    }

    @SuppressWarnings(value = "unchecked")
    public List<SoldProduct> getSoldProductsWithoutCutCheese(String invoiceNumber, String companyNumber) {
        List<SoldProduct> result = getEntityManager()
                .createNamedQuery("SoldProduct.findByInvoiceNumberWithoutCutCheese")
                .setParameter("codCutCheese", Constants.COD_CUT_CHEESE)
                .setParameter("invoiceNumber", invoiceNumber)
                .setParameter("companyNumber", companyNumber)
                .getResultList();
        if (null == result) {
            result = new ArrayList<SoldProduct>();
        }

        return result;
    }



    @SuppressWarnings(value = "unchecked")
    public List<SoldProduct> getSoldProductsCashSale(String invoiceNumber, String companyNumber) {
        List<SoldProduct> result = getEntityManager()
                .createNamedQuery("SoldProduct.findByCashSale")
                .setParameter("invoiceNumber", invoiceNumber)
                .setParameter("companyNumber", companyNumber)
                .getResultList();
        if (null == result) {
            result = new ArrayList<SoldProduct>();
        }
        return result;
    }

    public List<SoldProduct> getFindDelivery(String invoiceNumber, String companyNumber) {
        List<SoldProduct> result = getEntityManager()
                .createNamedQuery("SoldProduct.findDelivery")
                .setParameter("invoiceNumber", invoiceNumber)
                .setParameter("companyNumber", companyNumber)
                .getResultList();
        if (null == result) {
            result = new ArrayList<SoldProduct>();
        }
        return result;
    }

    @SuppressWarnings(value = "unchecked")
    public List<SoldProduct> getSoldProductsCashOrder(String invoiceNumber, String companyNumber) {
        List<SoldProduct> result = getEntityManager()
                .createNamedQuery("SoldProduct.findByCashOrder")
                .setParameter("invoiceNumber", invoiceNumber)
                .setParameter("companyNumber", companyNumber)
                .getResultList();
        if (null == result) {
            result = new ArrayList<SoldProduct>();
        }
        return result;
    }

    public List<String> getSoldProductsCashOrder(Date date,BigDecimal distribuidor) {
        List<String> result = getEntityManager()
                .createNativeQuery("select distinct iv.no_fact \n" +
                        "from WISE.inv_ventart iv\n" +
                        "inner join USER01_DAF.pedidos pe\n" +
                        "on iv.pedido = pe.pedido\n" +
                        "where pe.fecha_entrega = :date\n" +
                        "and iv.estado = 'PENDING'\n" +
                        "and pe.estado_pedido <> 'ANL'\n" +
                        "and pe.distribuidor = :distribuidor\n")
                .setParameter("date", date, TemporalType.DATE)
                .setParameter("distribuidor",distribuidor)
                .getResultList();
        if (null == result) {
            result = new ArrayList<String>();
        }
        return result;
    }

    public List<String> getSoldProductsCashOrder(Date date) {
        List<String> result = getEntityManager()
                .createNativeQuery("select distinct iv.no_fact \n" +
                        "from WISE.inv_ventart iv\n" +
                        "inner join USER01_DAF.pedidos pe\n" +
                        "on iv.pedido = pe.pedido\n" +
                        "where pe.fecha_entrega = :date\n" +
                        "and iv.estado = 'PENDING'\n" +
                        "and pe.estado_pedido <> 'ANL'\n" )
                .setParameter("date", date, TemporalType.DATE)
                .getResultList();
        if (null == result) {
            result = new ArrayList<String>();
        }
        return result;
    }

    @Override
    public Date getDateFromSoldProductOrder(SoldProduct soldProduct) {
        Date result;
        try{
            result = (Date)getEntityManager().createNativeQuery("select pe.fecha_entrega from WISE.inv_ventart iv\n" +
                    "inner join USER01_DAF.pedidos pe\n" +
                    "on iv.pedido = pe.pedido\n" +
                    "where iv.id_mov = :idSoldProduct")
                    .setParameter("idSoldProduct",soldProduct.getId())
                    .getSingleResult();
        }catch (NoResultException e){
            result = (Date)getEntityManager().createNativeQuery("select * from (\n" +
                    "select nvl(mo.fecha,mov.fecha) from USER01_DAF.b_movimientos mo\n" +
                    "full join USER01_DAF.movimientos mov\n" +
                    "on mo.nrofactura = mov.nrofactura\n" +
                    "where mov.nrofactura = :idSoldProduct\n" +
                    "or mo.nrofactura = :idSoldProduct\n" +
                    ")\n" +
                    "where rownum = 1")
                    .setParameter("idSoldProduct",soldProduct.getInvoiceNumber())
                    .getSingleResult();
        }
        return result;
    }

    @Override
    public List<SoldProduct> getSoldProductsWithoutCutCheeseAndEDAM(String invoiceNumber, String companyNumber) {
        List<SoldProduct> result = getEntityManager()
                .createNamedQuery("SoldProduct.findByInvoiceNumberWithoutCutCheeseAndEDAM")
                .setParameter("codCutCheese", Constants.COD_CUT_CHEESE)
                .setParameter("codEDAMCheese", Constants.COD_CHEESE_EDAM)
                .setParameter("invoiceNumber", invoiceNumber)
                .setParameter("companyNumber", companyNumber)
                .getResultList();
        if (null == result) {
            result = new ArrayList<SoldProduct>();
        }

        return result;
    }

    @Override
    public List<SoldProduct> getSoldProductsWithoutEDAM(String invoiceNumber, String companyNumber) {
        List<SoldProduct> result = getEntityManager()
                .createNamedQuery("SoldProduct.findByInvoiceNumberWithoutEDAM")
                .setParameter("codEDAMCheese", Constants.COD_CHEESE_EDAM)
                .setParameter("invoiceNumber", invoiceNumber)
                .setParameter("companyNumber", companyNumber)
                .getResultList();
        if (null == result) {
            result = new ArrayList<SoldProduct>();
        }

        return result;
    }

    @Override
    public CustomerOrder findPedidoPorCodigo(String numeroPedido)
    {
        CustomerOrder customerOrder;
        try{
            customerOrder = (CustomerOrder)getEntityManager()
                    .createQuery("select pe from CustomerOrder pe where pe.codigo =:codigo and pe.estado = 'PREPARAR'")
                    .setParameter("codigo",Long.valueOf(numeroPedido).longValue() )
            .getSingleResult();
        }catch (NoResultException e){
            return null;
        }
        return customerOrder;
    }

    @Override
    public List<CustomerOrder> findPedidosPorFechaTerritorio(Date date, Territoriotrabajo territoriotrabajo) {
        List<CustomerOrder> customerOrders = new ArrayList<CustomerOrder>();
        if(territoriotrabajo == null)
        {
            try {
                customerOrders = (List<CustomerOrder>) getEntityManager()
                        .createQuery("select pe from CustomerOrder pe " +
                                "where pe.fechaEntrega =:fecha " +
                                "and pe.estado <> 'ANULADO'")
                        .setParameter("fecha", date, TemporalType.DATE)
                        .getResultList();
                /*customerOrders = (List<CustomerOrder>) getEntityManager()
                        .createQuery("select pe from CustomerOrder pe " +
                                "where pe.fechaEntrega =:fecha ")
                        .setParameter("fecha", date, TemporalType.DATE)
                        .getResultList();*/
            } catch (NoResultException e) {
                return customerOrders;
            }
        }else {
            try {
                customerOrders = (List<CustomerOrder>) getEntityManager()
                        .createQuery("select pe from CustomerOrder pe " +
                                "where pe.fechaEntrega =:fecha " +
                                "and pe.estado <> 'ANULADO' " +
                                "and pe.cliente.territoriotrabajo =:territorio")
                        .setParameter("territorio", territoriotrabajo)
                        .setParameter("fecha", date, TemporalType.DATE)
                        .getResultList();
                /*customerOrders = (List<CustomerOrder>) getEntityManager()
                        .createQuery("select pe from CustomerOrder pe " +
                                "where pe.fechaEntrega =:fecha " +
                                "and pe.cliente.territoriotrabajo =:territorio")
                        .setParameter("territorio", territoriotrabajo)
                        .setParameter("fecha", date, TemporalType.DATE)
                        .getResultList();*/
            } catch (NoResultException e) {
                return customerOrders;
            }
        }
        return customerOrders;
    }

    @Override
    public VentaDirecta findVentaPorCodigo(String orderNumber) {
        VentaDirecta ventaDirecta;
        try{
            ventaDirecta = (VentaDirecta)getEntityManager()
                    .createQuery("select venta from VentaDirecta venta where venta.codigo =:codigo and venta.estado = 'PREPARAR'")
                    .setParameter("codigo", Long.valueOf(orderNumber).longValue())
                    .getSingleResult();
        }catch (NoResultException e){
            return null;
        }
        return ventaDirecta;
    }

    @Override
    public Map<String, Integer> getSoldProductsPackage(String codArt, Integer amountSoldProductTotal) {
        Map<String, Integer> products = new HashMap<String, Integer>();
            List<Object[]> datas = getEntityManager().createNativeQuery("select cod_art,cantidad \n" +
                    "from USER01_DAF.articulos_paquete\n" +
                    "where paquete = :codPack")
                    .setParameter("codPack",codArt)
                    .getResultList();
        for(Object[] obj:datas)
        {
            products.put((String)obj[0],(((BigDecimal)obj[1]).intValue()*amountSoldProductTotal));
        }
        return products;
    }

}
