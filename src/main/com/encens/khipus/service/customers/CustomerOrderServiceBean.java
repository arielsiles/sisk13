package com.encens.khipus.service.customers;

import com.encens.khipus.model.customers.CustomerOrderTypeEnum;
import com.encens.khipus.model.customers.SaleStatus;
import com.encens.khipus.model.customers.SaleTypeEnum;
import com.encens.khipus.model.warehouse.Warehouse;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import java.util.Date;
import java.util.List;

@Stateless
@Name("customerOrderService")
@AutoCreate
public class CustomerOrderServiceBean implements CustomerOrderService {

    @In(value = "#{entityManager}")
    private EntityManager em;

    @Override
    public List<Object[]> getSalesCustomer(Date startDate, Date endDate, SaleTypeEnum saleType, Warehouse warehouse) {

        List<Object[]> datas = em.createQuery(" SELECT " +
                " client.id as clientId," +
                " client.nitNumber as doc," +
                " client.name || ' ' || client.lastName || ' ' || client.maidenName as name," +
                " territory.nombre as zone," +
                " sum(articleOrder.amount) as amount," +
                " count(distinct customerOrder.id) as quantity" +
                " FROM ArticleOrder articleOrder" +
                " LEFT JOIN articleOrder.customerOrder customerOrder" +
                " LEFT JOIN customerOrder.client client" +
                " LEFT JOIN articleOrder.productItem productItem" +
                " LEFT JOIN client.territoriotrabajo territory" +
                " WHERE customerOrder.state <> :state" +
                " AND customerOrder.orderDate BETWEEN :startDate AND :endDate" +
                " AND customerOrder.saleType = :saleType" +
                " AND productItem.warehouse = :warehouse" +
                " AND customerOrder.customerOrderType.type IN (:normal, :milk, :veterinary)" +
                " GROUP BY client.id, client.nitNumber, territory.nombre")
                .setParameter("state", SaleStatus.ANULADO)
                .setParameter("startDate", startDate)
                .setParameter("endDate", endDate)
                .setParameter("saleType", saleType)
                .setParameter("warehouse", warehouse)
                .setParameter("normal", CustomerOrderTypeEnum.NORMAL)
                .setParameter("milk", CustomerOrderTypeEnum.MILK)
                .setParameter("veterinary", CustomerOrderTypeEnum.VETERINARY)
                .getResultList();

        /*for (Object[] data : datas){

            Long clientId = (Long)data[0];
            String doc = (String)data[1];
            String name = (String)data[2];
            String zone = (String)data[3];
            Double amount = (Double)data[4];
            Long quantity = (Long)data[5];

            System.out.println(clientId + " - " + doc + " - " + name + " - " + zone + " - " + amount + " - " + quantity);

        }*/

        return datas;
    }

    @Override
    public List<Object[]> getSalesCustomerProduct(Date startDate, Date endDate, SaleTypeEnum saleType, Warehouse warehouse) {

        List<Object[]> datas = em.createQuery(" SELECT " +
                " client.id as clientId," +
                " client.nitNumber as doc," +
                " client.name || ' ' || client.lastName || ' ' || client.maidenName as name," +
                " productItem.name as product," +
                " sum(articleOrder.amount) as amount," +
                " sum(articleOrder.quantity) as quantity" +
                " FROM ArticleOrder articleOrder" +
                " LEFT JOIN articleOrder.customerOrder customerOrder" +
                " LEFT JOIN customerOrder.client client" +
                " LEFT JOIN articleOrder.productItem productItem" +
                " WHERE customerOrder.state <> :state" +
                " AND customerOrder.orderDate BETWEEN :startDate AND :endDate" +
                " AND customerOrder.saleType = :saleType" +
                " AND productItem.warehouse = :warehouse" +
                " AND customerOrder.customerOrderType.type IN (:normal, :milk, :veterinary)" +
                " GROUP BY client.id, client.nitNumber, productItem.name")
                .setParameter("state", SaleStatus.ANULADO)
                .setParameter("startDate", startDate)
                .setParameter("endDate", endDate)
                .setParameter("saleType", saleType)
                .setParameter("warehouse", warehouse)
                .setParameter("normal", CustomerOrderTypeEnum.NORMAL)
                .setParameter("milk", CustomerOrderTypeEnum.MILK)
                .setParameter("veterinary", CustomerOrderTypeEnum.VETERINARY)
                .getResultList();

        return datas;
    }

    @Override
    public List<Object[]> getSalesProduct(Date startDate, Date endDate, SaleTypeEnum saleType, Warehouse warehouse) {

        List<Object[]> datas = em.createQuery(" SELECT " +
                " productItem.productItemCode as code," +
                " productItem.name as product," +
                " sum(articleOrder.amount) as amount," +
                " sum(articleOrder.quantity) as quantity" +
                " FROM ArticleOrder articleOrder" +
                " LEFT JOIN articleOrder.customerOrder customerOrder" +
                " LEFT JOIN articleOrder.productItem productItem" +
                " WHERE customerOrder.state <> :state" +
                " AND customerOrder.orderDate BETWEEN :startDate AND :endDate" +
                " AND customerOrder.saleType = :saleType" +
                " AND productItem.warehouse = :warehouse" +
                " AND customerOrder.customerOrderType.type IN (:normal, :milk, :veterinary)" +
                " GROUP BY productItem.productItemCode, productItem.name")
                .setParameter("state", SaleStatus.ANULADO)
                .setParameter("startDate", startDate)
                .setParameter("endDate", endDate)
                .setParameter("saleType", saleType)
                .setParameter("warehouse", warehouse)
                .setParameter("normal", CustomerOrderTypeEnum.NORMAL)
                .setParameter("milk", CustomerOrderTypeEnum.MILK)
                .setParameter("veterinary", CustomerOrderTypeEnum.VETERINARY)
                .getResultList();

        return datas;
    }
}
