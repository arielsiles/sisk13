package com.encens.khipus.service.products;

import com.encens.khipus.model.customers.DiscountPolicyMeasurementType;
import com.encens.khipus.model.products.Product;
import com.encens.khipus.model.products.ProductDiscountRule;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import java.math.BigDecimal;
import java.util.List;

/**
 * Products services
 *
 * @author
 * @version $Id: ProductServiceBean.java 2008-9-11 13:50:57 $
 */
@Stateless
@Name("productService")
@AutoCreate
public class ProductServiceBean implements ProductService {

    @In
    private EntityManager entityManager;

    /**
     * Finds a product by its code.
     *
     * @param code the product to look for.
     * @return a product instance if it's found, null otherwise
     */
    public Product findByCode(String code) {

        try {
            return (Product) entityManager.createNamedQuery("Product.findByCode")
                    .setParameter("code", code).getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    @SuppressWarnings({"unchecked"})
    public List<ProductDiscountRule> findDiscountRules(Product product) {
        try {
            return entityManager.createNamedQuery("Product.findDiscountRules").setParameter("product", product).getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    public BigDecimal getDiscountPercentage(Product product) {
        try {
            Query query = entityManager.createQuery("select sum(r.amount) from Product p inner join p.discountRules r " +
                    "where p =:product and r.discountPolicy .discountPolicyType.measurement =:measurement");
            query.setParameter("product", product);
            query.setParameter("measurement", DiscountPolicyMeasurementType.PERCENTAGE);
            return (BigDecimal) query.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    public BigDecimal getDiscount(Product product) {
        try {
            Query query = entityManager.createQuery("select sum(r.amount) from Product p inner join p.discountRules r " +
                    "where p =:product and r.discountPolicy .discountPolicyType.measurement =:measurement");
            query.setParameter("product", product);
            query.setParameter("measurement", DiscountPolicyMeasurementType.AMOUNT);
            return (BigDecimal) query.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

}
