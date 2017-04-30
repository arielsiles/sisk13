package com.encens.khipus.service.customers;

import com.encens.khipus.framework.service.ExtendedGenericServiceBean;
import com.encens.khipus.model.customers.ArticleOrder;
import com.encens.khipus.model.customers.CustomerOrder;
import com.encens.khipus.model.customers.Movement;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.Name;

import javax.ejb.Stateless;
import javax.persistence.NoResultException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: Diego
 * Date: 25/12/14
 * Time: 11:42
 * To change this template use File | Settings | File Templates.
 */
@Stateless
@Name("movementService")
@AutoCreate
public class MovementServiceBean extends ExtendedGenericServiceBean implements MovementService {
    @Override
    public List<Movement> findMovementByDate(Date date) {
        List<Movement> movements = new ArrayList<Movement>();

            try {
                movements = (List<Movement>) getEntityManager().createQuery("select movement from Movement movement " +
                        " where Movement.date = :date ")
                        .setParameter("date", date)
                        .getResultList();
            } catch (NoResultException e) {
                return movements;
            }

        return movements;
    }

    @Override
    public List<ArticleOrder> findArticleOrdersByCustomerOrder(CustomerOrder order) {
        List<ArticleOrder> articleOrders = new ArrayList<ArticleOrder>();
        try{

            articleOrders = (List<ArticleOrder>) getEntityManager().createQuery(" select articleOrder from ArticleOrder articleOrder " +
                                                                                " where articleOrder.customerOrder = :idCustomerOrder ")
                                                                    .setParameter("idCustomerOrder",order.getId())
                                                                    .getResultList();

        }catch(NoResultException e){
            return articleOrders;

        }
        return  articleOrders;
    }

}
