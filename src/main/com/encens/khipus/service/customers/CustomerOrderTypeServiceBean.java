package com.encens.khipus.service.customers;

import com.encens.khipus.model.customers.CustomerOrderType;
import com.encens.khipus.model.customers.CustomerOrderTypeEnum;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;

@Stateless
@Name("customerOrderTypeService")
@AutoCreate
public class CustomerOrderTypeServiceBean implements CustomerOrderTypeService {

    @In("#{entityManager}")
    private EntityManager em;

    @Override
    public CustomerOrderType findCustomerOrderTypeDefault() {
        return (CustomerOrderType) em.createQuery("select c from CustomerOrderType c " +
                " where c.type =:type")
                .setParameter("type", CustomerOrderTypeEnum.NORMAL)
                .getSingleResult();
    }

    @Override
    public CustomerOrderType findCustomerOrderTypeSpecial() {
        return (CustomerOrderType) em.createQuery("select c from CustomerOrderType c " +
                " where c.type =:type")
                .setParameter("type", CustomerOrderTypeEnum.SPECIAL)
                .getSingleResult();
    }
}
