package com.encens.khipus.service.customers;

import com.encens.khipus.model.contacts.Entity;
import com.encens.khipus.model.contacts.Person;
import com.encens.khipus.model.customers.Customer;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import java.util.List;
import java.util.Map;

/**
 * Customer service operations
 *
 * @author
 * @version $Id: CustomerServiceBean.java 2008-9-10 10:33:11 $
 */
@Stateless
@Name("customerService")
@AutoCreate
public class CustomerServiceBean implements CustomerService {

    @In("#{entityManager}")
    private EntityManager em;

    @In
    protected Map<String, String> messages;

    /**
     * Test
     */
    public void createCustomer() {
        System.out.println("Testing saving customers....");
        /*Person p = new Person();
        p.setIdNumber("44444444444444444444");
        p.setFirstName("Ariel Siles");
        p.setLastName("Siles");
        p.setMaidenName("Encinas");
        Customer c = new Customer();
        c.setFirstPurchase(new Date());
        c.setLastPurchase(new Date());
        c.setTotalPurchasedAmount(BigDecimal.valueOf(455.2));
        c.setEntity(p);
        p.setCustomer(c);

        Organization o = new Organization();
        o.setIdNumber("88888888888888888");
        o.setName("BUSINESS");

        Customer c1 = new Customer();
        c1.setFirstPurchase(new Date());
        c1.setLastPurchase(new Date());
        c1.setTotalPurchasedAmount(BigDecimal.valueOf(455.2));
        c1.setEntity(o);
        o.setCustomer(c1);

         em.persist(p);
        em.persist(o);*/

        ///Entity p = em.find(Entity.class, 140L);
        Person p = new Person();
        p.setIdNumber("7956464454");
        p.setFirstName("Ariel Randy");
        p.setLastName("Siles");
        p.setMaidenName("Encinas");


        em.persist(p);

        Customer customer = new Customer();
        customer.setEntity(p);

        em.persist(customer);


    }

    /**
     * Returns all Entity instances which have customer information defined. So it retrives
     * all customers.
     *
     * @return
     */
    @SuppressWarnings({"unchecked"})
    public List<Customer> getAllCustomers() {
        return em.createNamedQuery("Customer.findAll").getResultList();
    }


    /**
     * Merges an entity and creates the customer related to it if necesary.
     *
     * @param customer the customer to be merged
     */
    public void createOrUpdate(Customer customer) {
        if (customer.getId() == null) {
            em.persist(customer);
        } else {
            em.merge(customer);
        }
        em.flush();
    }

    /**
     * Returns an entity with the specified number
     *
     * @param idNumber the number
     * @return the Entity if found, otherwise
     */
    public Customer findByIdNumber(String idNumber) {
        try {
            return (Customer) em.createNamedQuery("Customer.findByIdNumber")
                    .setParameter("number", idNumber).getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    public Entity findEntityByIdNumber(String idNumber) {
        try {
            return (Entity) em.createNamedQuery("Entity.findByIdNumber")
                    .setParameter("number", idNumber).getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    public Customer findByCustomerNumber(String customerNumber) {
        try {
            return (Customer) em.createNamedQuery("Customer.findByCustomerNumber")
                    .setParameter("number", customerNumber).getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    public boolean numberExists(String customerNumber, String idNumber) {
        try {
            Query query = em.createNamedQuery("Customer.findByNumberExists");
            query.setParameter("number", customerNumber);
            query.setParameter("idNumber", idNumber);
            query.getSingleResult();
            return true;
        } catch (NoResultException e) {
            return false;
        }
    }

    public Customer findByEntity(Entity entity) {
        if (entity != null && entity.getId() != null) {
            try {
                return (Customer) em.createNamedQuery("Customer.findByEntity")
                        .setParameter("entity", entity).getSingleResult();
            } catch (NoResultException e) {
            }
        }
        return null;
    }


}
