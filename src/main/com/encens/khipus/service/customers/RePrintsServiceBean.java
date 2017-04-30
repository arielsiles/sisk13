package com.encens.khipus.service.customers;

import com.encens.khipus.framework.service.ExtendedGenericServiceBean;
import com.encens.khipus.model.customers.CustomerOrder;
import com.encens.khipus.model.customers.RePrints;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.Name;

import javax.ejb.Stateless;
import javax.persistence.NoResultException;

/**
 * Created with IntelliJ IDEA.
 * User: Diego
 * Date: 26/12/14
 * Time: 1:07
 * To change this template use File | Settings | File Templates.
 */
@Stateless
@Name("rePrintsService")
@AutoCreate
public class RePrintsServiceBean extends ExtendedGenericServiceBean implements RePrintsService {
    @Override
    public RePrints findReprintByCustomerOrder(CustomerOrder order) {
        RePrints rePrints;
            try{
                rePrints = (RePrints)getEntityManager().createQuery(" select rePrints from RePrints rePrints " +
                                                           " where rePrints.customerOrder =:order  ")
                                                        .setParameter("order",order)
                                                        .getSingleResult();
            }catch (NoResultException e)
            {
                return null;
            }
        return rePrints;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public String findNameClient(CustomerOrder order) {
        String name = "";
        String sql;
       /* if(order.getClientOrder().getType().compareTo("P") == 0)
            sql = "select nom||' '||ap||' '||am from user01_daf.PERSONAS where pi_id =:id";
        else
            sql = " select razon_soc from user01_daf.INSTITUCIONES where pi_id =:id";
        try{
            name = (String)getEntityManager().createNativeQuery(sql)
                    .setParameter("id",order.getClientOrder().getId())
                    .getSingleResult();
        }catch (NoResultException e)
        {
            return "s/n";
        }*/
        return name;  //To change body of implemented methods use File | Settings | File Templates.
    }

   /* @Override
    public String findNitClient(CustomerOrder order) {
        String name = "";
        try{
            name = (String)getEntityManager().createNativeQuery(" select razon_soc from INSTITUCIONES where pi_id = :idOrden")
                    .setParameter("idOrder",order.getId())
                    .getSingleResult();
        }catch (NoResultException e)
        {
            return "";
        }
        return name;  //To change body of implemented methods use File | Settings | File Templates.
    }*/
}
