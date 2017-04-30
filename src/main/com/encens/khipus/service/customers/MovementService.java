package com.encens.khipus.service.customers;

import com.encens.khipus.framework.service.GenericService;
import com.encens.khipus.model.customers.ArticleOrder;
import com.encens.khipus.model.customers.CustomerOrder;
import com.encens.khipus.model.customers.Movement;

import javax.ejb.Local;
import java.util.Date;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: Diego
 * Date: 25/12/14
 * Time: 11:38
 * To change this template use File | Settings | File Templates.
 */
@Local
public interface MovementService extends GenericService {
    List<Movement> findMovementByDate(Date date);

    List<ArticleOrder> findArticleOrdersByCustomerOrder(CustomerOrder order);
}
