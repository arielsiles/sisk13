package com.encens.khipus.service.customers;

import com.encens.khipus.framework.action.Outcome;
import com.encens.khipus.framework.service.GenericServiceBean;
import com.encens.khipus.model.admin.User;
import com.encens.khipus.model.customers.ArticleOrder;
import com.encens.khipus.model.customers.CustomerOrder;
import com.encens.khipus.model.customers.SaleStatus;
import com.encens.khipus.model.customers.SaleTypeEnum;
import com.encens.khipus.model.warehouse.ProductItem;
import com.encens.khipus.service.warehouse.InventoryService;
import com.encens.khipus.util.BigDecimalUtil;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Stateless
@Name("saleService")
@AutoCreate
public class SaleServiceBean extends GenericServiceBean implements SaleService {

    @In(value = "#{entityManager}")
    private EntityManager em;

    @In
    private InventoryService inventoryService;

    @Override
    public String createSale(CustomerOrder customerOrder) {

        for (ArticleOrder articleOrder:customerOrder.getArticleOrderList()){
            articleOrder.setCustomerOrder(customerOrder);
            em.persist(articleOrder);
        }
        em.persist(customerOrder);
        em.flush();

        return Outcome.SUCCESS;
    }

    @Override
    public void updateCustomerOrder(CustomerOrder customerOrder) {
        em.merge(customerOrder);
        em.flush();
    }

    @Override
    public Long findLastSaleId(User user) {
        System.out.println("------>> userid: " + user.getId());
        System.out.println("------>> user: " + user.getUsername());
        Long result =  null;
        result = (Long) em.createQuery("select max(c.id) from CustomerOrder c " +
                " where c.user =:user")
        .setParameter("user", user)
        .getSingleResult();

        return result;
    }

    @Override
    public CustomerOrder findSaleById(Long id) {
        CustomerOrder result;
        result = (CustomerOrder) em.createQuery("select c from CustomerOrder c " +
                " where c.id =:id")
                .setParameter("id", id)
                .getSingleResult();

        System.out.println("----------------> ultima venta:" + result);

        return result;
    }

    @Override
    public void updateArticleForOutputs(ArticleOrder articleOrder) {

        ProductItem productItem = getEntityManager().find(ProductItem.class, articleOrder.getProductItem().getId());

        /** todo Controlar negativos, si cuando se requiera **/

        /** Actualiza CT **/
        BigDecimal total = BigDecimalUtil.multiply(productItem.getCu(), BigDecimalUtil.toBigDecimal(articleOrder.getTotal()), 6);
        BigDecimal newTotalCost = BigDecimalUtil.subtract(productItem.getCt(), total, 6);
        productItem.setCt(newTotalCost);

        /** Actualiza Saldo_Mon **/
        BigDecimal totalCost = BigDecimalUtil.multiply(productItem.getUnitCost(), BigDecimalUtil.toBigDecimal(articleOrder.getTotal()));
        BigDecimal newInvestmentAmount = BigDecimalUtil.subtract(productItem.getInvestmentAmount(), totalCost, 6);
        productItem.setInvestmentAmount(newInvestmentAmount);

        em.merge(productItem);
        em.flush();

    }

    @Override
    public CustomerOrder findCustomerOrderByParams(SaleTypeEnum saleType, Date date, String code) {

        CustomerOrder customerOrder = (CustomerOrder)em.createQuery("select c from CustomerOrder c" +
                " where c.saleType =:saleType " +
                " and c.orderDate =:date " +
                " and c.code =:code ")
                .setParameter("saleType", saleType)
                .setParameter("date", date)
                .setParameter("code", new Long(code))
                .getSingleResult();

        return customerOrder;
    }

    @Override
    public List<CustomerOrder> getCustomerOrderList(User user, Date date) {

        List<CustomerOrder> resultList = new ArrayList<CustomerOrder>();

        resultList = em.createQuery("" +
                " select c from CustomerOrder c " +
                " where c.user =:user " +
                " and c.orderDate =:date " +
                " and c.state =:state")
                .setParameter("user", user)
                .setParameter("date", date)
                .setParameter("state", SaleStatus.PENDIENTE)
                .getResultList();

        return resultList;
    }
}
