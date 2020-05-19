package com.encens.khipus.service.customers;

import com.encens.khipus.framework.action.Outcome;
import com.encens.khipus.framework.service.GenericServiceBean;
import com.encens.khipus.model.customers.ArticleOrder;
import com.encens.khipus.model.customers.CustomerOrder;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;

@Stateless
@Name("saleService")
@AutoCreate
public class SaleServiceBean extends GenericServiceBean implements SaleService {

    @In(value = "#{entityManager}")
    private EntityManager em;

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
}
