package com.encens.khipus.service.customers;

import com.encens.khipus.model.customers.CustomerCategory;
import com.encens.khipus.model.customers.CustomerCategoryType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;

/**
 *
 *
 * @author
 * @version
 */
@Stateless
@Name("customerCategoryService")
@AutoCreate
public class CustomerCategoryServiceBean implements CustomerCategoryService {

    @In("#{entityManager}")
    private EntityManager em;


    @Override
    public CustomerCategory getCustomerCategory(CustomerCategoryType customerCategoryType) {
        return (CustomerCategory) em.createQuery("select c from CustomerCategory c where c.type =:type")
                .setParameter("type", customerCategoryType)
                .getSingleResult();
    }
}
