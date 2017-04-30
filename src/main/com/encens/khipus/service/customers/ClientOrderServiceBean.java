package com.encens.khipus.service.customers;

import com.encens.khipus.framework.service.ExtendedGenericServiceBean;
import com.encens.khipus.model.customers.ClientOrder;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: Diego
 * Date: 12/11/13
 * Time: 19:23
 * To change this template use File | Settings | File Templates.
 */
@Name("clientOrderService")
@AutoCreate
@Stateless
public class ClientOrderServiceBean extends ExtendedGenericServiceBean implements ClientOrderService {

    @In(value = "#{entityManager}")
    private EntityManager em;


    @Override
    public List<ClientOrder> findclientOrderByDate(Date date) {
        List<ClientOrder> clientOrders = new ArrayList<ClientOrder>();
        try
        {
            clientOrders = em.createQuery("select clientOrder from  ClientOrder clientOrder " +
                            " inner join clientOrder.customerOrders customerOrder" +
                            " where customerOrder.date = :date")
                            .setParameter("date",date)
                            .getResultList();

        }catch(NoResultException e){
           return clientOrders = new ArrayList<ClientOrder>();
        }
        return clientOrders;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
