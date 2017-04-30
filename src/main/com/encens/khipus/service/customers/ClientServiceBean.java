package com.encens.khipus.service.customers;

import com.encens.khipus.model.customers.Client;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import java.util.List;

/**
 * Customer service operations
 *
 * @author
 * @version $Id: CustomerServiceBean.java 2008-9-10 10:33:11 $
 */
@Stateless
@Name("clientService")
@AutoCreate
public class ClientServiceBean implements ClientService {

    @In("#{entityManager}")
    private EntityManager em;


    /**
     * Returns all Entity instances which have customer information defined. So it retrives
     * all customers.
     *
     * @return
     */
    @SuppressWarnings({"unchecked"})
    public List<Client> getAllClients() {
        return em.createNamedQuery("Client.findAll").getResultList();
    }


}
