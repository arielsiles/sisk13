package com.encens.khipus.service.customers;

import com.encens.khipus.framework.service.GenericServiceBean;
import com.encens.khipus.model.customers.ArticleOrder;
import com.encens.khipus.model.customers.SaleStatus;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import java.util.Date;
import java.util.List;

/**
 * @author
 * @version 3.0
 */
@Stateless
@Name("articleOrderService")
@AutoCreate
public class ArticleOrderServiceBean extends GenericServiceBean implements ArticleOrderService {

    @In(value = "#{entityManager}")
    private EntityManager em;

    @In
    ArticleOrderService articleOrderService;

    @SuppressWarnings(value = "unchecked")
    public List<ArticleOrder> findCashSaleDetailByCodeAndDate(String productItemCode, Date startDate, Date endDate) {
        return em.createNamedQuery("ArticleOrder.findCashSaleDetailByCodeAndDate")
                .setParameter("productItemCode", productItemCode)
                .setParameter("startDate", startDate)
                .setParameter("endDate", endDate)
                .getResultList();
    }

    @SuppressWarnings(value = "unchecked")
    public List<ArticleOrder> findOrderDetailByCodeAndDate(String productItemCode, Date startDate, Date endDate) {
        return em.createNamedQuery("ArticleOrder.findOrderDetailByCodeAndDate")
                .setParameter("productItemCode", productItemCode)
                .setParameter("startDate", startDate)
                .setParameter("endDate", endDate)
                .setParameter("annulledState", SaleStatus.ANULADO)
                .getResultList();
    }

    @SuppressWarnings(value = "unchecked")
    public List<Object> findCashSaleDetailListGroupBy(Date startDate, Date endDate){
        return em.createNamedQuery("ArticleOrder.findCashSaleDetailListGroupBy")
                .setParameter("startDate", startDate)
                .setParameter("endDate", endDate)
                .getResultList();
    }

    @SuppressWarnings(value = "unchecked")
    public List<Object> findCustomerOrderDetailListGroupBy(Date startDate, Date endDate){
        return em.createNamedQuery("ArticleOrder.findCustomerOrderDetailListGroupBy")
                .setParameter("startDate", startDate)
                .setParameter("endDate", endDate)
                .setParameter("annulledState", SaleStatus.ANULADO)
                .getResultList();
    }

    @SuppressWarnings(value = "unchecked")
    public List<ArticleOrder> findCashSaleDetailList(Date startDate, Date endDate){
        return em.createNamedQuery("ArticleOrder.findCashSaleDetailList")
                .setParameter("startDate", startDate)
                .setParameter("endDate", endDate)
                .getResultList();
    }

    @SuppressWarnings(value = "unchecked")
    public List<ArticleOrder> findCustomerOrderDetailList(Date startDate, Date endDate){
        return em.createNamedQuery("ArticleOrder.findCustomerOrderDetailList")
                .setParameter("startDate", startDate)
                .setParameter("endDate", endDate)
                .setParameter("annulledState", SaleStatus.ANULADO)
                .getResultList();
    }

}
