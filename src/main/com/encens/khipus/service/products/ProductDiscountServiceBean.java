package com.encens.khipus.service.products;

import com.encens.khipus.model.products.Product;
import com.encens.khipus.model.products.ProductDiscountRule;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;

/**
 * Product discount service implementation class
 *
 * @author:
 */

@Stateless
@Name("productDiscountService")
@AutoCreate
public class ProductDiscountServiceBean implements ProductDiscountService {

    @In(value = "#{entityManager}")
    private EntityManager em;

    public Product findDiscountByRule(Product product, ProductDiscountRule rule) {
        try {
            Query query = em.createNamedQuery("ProductDiscount.findDiscountByRule");
            query.setParameter("product", product);
            query.setParameter("rule", rule);
            return (Product) query.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    public void newDiscount(ProductDiscountRule rule, Product product) {
        if (!rule.getProducts().contains(product)) {
            rule.getProducts().add(product);
        }
    }
}
