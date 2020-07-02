package com.encens.khipus.service.customers;

import com.encens.khipus.framework.service.GenericServiceBean;
import com.encens.khipus.model.contacts.Person;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;

/**
 * Customer service operations
 *
 * @author
 * @version $Id: CustomerServiceBean.java 2008-9-10 10:33:11 $
 */
@Stateless
@Name("distributorService")
@AutoCreate
public class DistributorServiceBean extends GenericServiceBean implements DistributorService {

    @In("#{entityManager}")
    private EntityManager em;


    @Override
    public void addDistributor(Person person) {
        em.createNativeQuery("insert into distribuidor values(:id, :description, :company)")
                .setParameter("id", person.getId())
                .setParameter("description", null)
                .setParameter("company", person.getCompany().getId())
                .executeUpdate();
    }
}
